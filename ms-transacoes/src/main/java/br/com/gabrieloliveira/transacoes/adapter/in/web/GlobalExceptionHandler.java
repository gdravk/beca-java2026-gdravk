package br.com.gabrieloliveira.transacoes.adapter.in.web;

import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.ErroResponseDTO;
import br.com.gabrieloliveira.transacoes.domain.exception.AcessoNegadoException;
import br.com.gabrieloliveira.transacoes.domain.exception.ParametroInvalidoException;
import br.com.gabrieloliveira.transacoes.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.transacoes.domain.exception.RegraNegocioException;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

// traduz as excecoes em respostas http com o ErroResponseDTO, no mesmo formato do ms1
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
                                                              HttpServletRequest request) {
        return montarResposta(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDTO> tratarRegraNegocio(RegraNegocioException ex,
                                                             HttpServletRequest request) {
        return montarResposta(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ParametroInvalidoException.class)
    public ResponseEntity<ErroResponseDTO> tratarParametroInvalido(ParametroInvalidoException ex,
                                                                  HttpServletRequest request) {
        return montarResposta(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    // 403: o usuario logado tentou ver ou mexer nos dados de outro usuario
    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponseDTO> tratarAcessoNegado(AcessoNegadoException ex,
                                                             HttpServletRequest request) {
        return montarResposta(HttpStatus.FORBIDDEN, ex.getMessage(), request, null);
    }

    // 403 vindo do proprio spring security (chega aqui pelo ErroDeSegurancaHandler)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponseDTO> tratarAcessoNegadoPelaSeguranca(AccessDeniedException ex,
                                                                          HttpServletRequest request) {
        return montarResposta(HttpStatus.FORBIDDEN, "voce nao tem permissao para acessar este recurso",
                request, null);
    }

    // erros de login (chegam aqui pelo ErroDeSegurancaHandler):
    // - AuthenticationServiceException: o ms1 esta fora do ar -> 503, porque o problema nao e do usuario
    // - BadCredentialsException: email ou senha errados -> 401
    // - outras (ex: InsufficientAuthenticationException): nao mandou login nenhum -> 401
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponseDTO> tratarNaoAutenticado(AuthenticationException ex,
                                                               HttpServletRequest request) {
        if (ex instanceof AuthenticationServiceException) {
            return montarResposta(HttpStatus.SERVICE_UNAVAILABLE,
                    "nao foi possivel conferir o login agora (servico de usuarios indisponivel), tente novamente",
                    request, null);
        }

        String mensagem = "faca login: envie o email e a senha do usuario (autenticacao basica)";
        if (ex instanceof BadCredentialsException) {
            mensagem = "email ou senha invalidos";
        }
        ErroResponseDTO corpo = new ErroResponseDTO(HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(), mensagem, request.getRequestURI(), null);
        // o cabecalho WWW-Authenticate avisa o cliente que a rota espera autenticacao basica
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"ms-transacoes\"")
                .body(corpo);
    }

    // 503 (service unavailable): um servico externo (api mock, brasilapi) nao respondeu
    // aparece na rota de saldo; na criacao de transacao isso nao chega aqui, porque o caso de uso
    // trata a excecao e grava a transacao como REJECTED com o motivo
    // a mensagem tecnica da excecao (com detalhes da conexao) nao vai pro cliente, so pro log
    @ExceptionHandler(ServicoExternoIndisponivelException.class)
    public ResponseEntity<ErroResponseDTO> tratarServicoExternoIndisponivel(ServicoExternoIndisponivelException ex,
                                                                           HttpServletRequest request) {
        return montarResposta(HttpStatus.SERVICE_UNAVAILABLE,
                "o servico de saldo esta indisponivel no momento, tente novamente mais tarde",
                request, null);
    }

    // bean validation do corpo: devolve todos os campos invalidos de uma vez
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacao(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erroDoCampo : ex.getBindingResult().getFieldErrors()) {
            campos.put(erroDoCampo.getField(), erroDoCampo.getDefaultMessage());
        }
        return montarResposta(HttpStatus.BAD_REQUEST, "existem campos invalidos na requisicao", request, campos);
    }

    // json quebrado ou valor fora da lista de um enum (ex: "tipo": "PIX")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> tratarJsonInvalido(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        return montarResposta(HttpStatus.BAD_REQUEST,
                "o corpo da requisicao esta mal formatado ou tem um valor invalido (confira tipo e categoria)",
                request, null);
    }

    // parametro de url com tipo errado (ex: /transactions/abc ou inicio=31-12-2026)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> tratarTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                             HttpServletRequest request) {
        String mensagem = "o valor '" + ex.getValue() + "' nao e valido para o parametro '" + ex.getName() + "'";
        return montarResposta(HttpStatus.BAD_REQUEST, mensagem, request, null);
    }

    // parametro obrigatorio da url que nao foi enviado (ex: GET /transactions sem usuarioId)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponseDTO> tratarParametroAusente(MissingServletRequestParameterException ex,
                                                                 HttpServletRequest request) {
        String mensagem = "o parametro '" + ex.getParameterName() + "' e obrigatorio";
        return montarResposta(HttpStatus.BAD_REQUEST, mensagem, request, null);
    }

    private ResponseEntity<ErroResponseDTO> montarResposta(HttpStatus status, String mensagem,
                                                           HttpServletRequest request, Map<String, String> campos) {
        ErroResponseDTO corpo = new ErroResponseDTO(
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI(),
                campos
        );
        return ResponseEntity.status(status).body(corpo);
    }
}
