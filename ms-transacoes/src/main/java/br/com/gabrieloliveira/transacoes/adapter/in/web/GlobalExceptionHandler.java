package br.com.gabrieloliveira.transacoes.adapter.in.web;

import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.ErroResponseDTO;
import br.com.gabrieloliveira.transacoes.domain.exception.ParametroInvalidoException;
import br.com.gabrieloliveira.transacoes.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.transacoes.domain.exception.RegraNegocioException;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
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
