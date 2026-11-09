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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// traduz as excecoes em respostas http com o ErroResponseDTO, no mesmo formato do ms1
@RestControllerAdvice
public class GlobalExceptionHandler {

    // log para os erros inesperados (o 500): o cliente recebe uma mensagem generica e os detalhes ficam aqui
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
    // (o ErrosPorCampo junta as mensagens quando o mesmo campo tem mais de um erro)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacao(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        ErrosPorCampo erros = new ErrosPorCampo();
        for (FieldError erroDoCampo : ex.getBindingResult().getFieldErrors()) {
            erros.adicionar(erroDoCampo.getField(), erroDoCampo.getDefaultMessage());
        }
        return montarResposta(HttpStatus.BAD_REQUEST, "existem campos invalidos na requisicao", request,
                erros.paraResposta());
    }

    // bean validation dos parametros da url (fase 13), ex: GET /transactions/0 ou ?usuarioId=-1
    // cada parametro errado vira uma linha em "campos", no mesmo formato dos erros do corpo
    // se o metodo tiver parametro validado e corpo com @Valid juntos, os erros do corpo tambem chegam aqui
    // (dentro de um ParameterErrors), entao pegamos os campos dele um por um
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacaoDosParametros(HandlerMethodValidationException ex,
                                                                       HttpServletRequest request) {
        ErrosPorCampo erros = new ErrosPorCampo();
        for (ParameterValidationResult resultado : ex.getParameterValidationResults()) {
            if (resultado instanceof ParameterErrors errosDoCorpo) {
                for (FieldError erroDoCampo : errosDoCorpo.getFieldErrors()) {
                    erros.adicionar(erroDoCampo.getField(), erroDoCampo.getDefaultMessage());
                }
            } else {
                String nomeDoParametro = resultado.getMethodParameter().getParameterName();
                for (MessageSourceResolvable erro : resultado.getResolvableErrors()) {
                    erros.adicionar(nomeDoParametro, erro.getDefaultMessage());
                }
            }
        }
        return montarResposta(HttpStatus.BAD_REQUEST, "existem campos invalidos na requisicao", request,
                erros.paraResposta());
    }

    // json quebrado, ou um campo com valor que nao da pra converter (ex: "tipo": "PIX" ou "valor": "dez")
    // quando o jackson sabe qual campo deu problema, a resposta diz o campo e, se for um enum, os valores aceitos
    // assim a pessoa nao precisa adivinhar o que esta errado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> tratarJsonInvalido(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        if (ex.getCause() instanceof MismatchedInputException erroDoCampo && !erroDoCampo.getPath().isEmpty()) {
            // o "path" e o caminho ate o campo; o ultimo pedaco e o nome do campo (ex: "tipo")
            List<JacksonException.Reference> caminho = erroDoCampo.getPath();
            String campo = caminho.get(caminho.size() - 1).getPropertyName();
            // quando o erro e num item de lista (ex: o corpo inteiro veio como [ ... ]), o ultimo pedaco do
            // caminho e uma posicao (0, 1...) e nao um nome: o getPropertyName volta null
            // uma chave null quebra o json da resposta, entao nesse caso o erro fica no "corpo"
            // (ajuste da revisao do professor 06/11)
            if (campo == null) {
                campo = "corpo";
            }
            Map<String, String> campos = new LinkedHashMap<>();
            campos.put(campo, descreverValorInvalido(erroDoCampo));
            return montarResposta(HttpStatus.BAD_REQUEST, "existem campos invalidos na requisicao", request, campos);
        }
        return montarResposta(HttpStatus.BAD_REQUEST, "o corpo da requisicao esta mal formatado", request, null);
    }

    // parametro de url com tipo errado (ex: /transactions/abc, inicio=31-12-2026 ou tipo=PIX)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> tratarTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                             HttpServletRequest request) {
        String mensagem = "o valor '" + ex.getValue() + "' nao e valido para o parametro '" + ex.getName() + "'"
                + dicaDoFormato(ex.getRequiredType());
        return montarResposta(HttpStatus.BAD_REQUEST, mensagem, request, null);
    }

    // parametro obrigatorio da url que nao foi enviado (ex: GET /transactions sem usuarioId)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponseDTO> tratarParametroAusente(MissingServletRequestParameterException ex,
                                                                 HttpServletRequest request) {
        String mensagem = "o parametro '" + ex.getParameterName() + "' e obrigatorio";
        return montarResposta(HttpStatus.BAD_REQUEST, mensagem, request, null);
    }

    // 405: a rota existe, mas nao com esse metodo http (ex: PATCH /transactions/1)
    // lembrando que transacao nunca e alterada nem apagada: correcao e so pelo estorno
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tratarMetodoNaoAceito(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest request) {
        String mensagem = "o metodo " + ex.getMethod() + " nao e aceito nesta rota, aceitos: "
                + ex.getSupportedHttpMethods();
        return montarResposta(HttpStatus.METHOD_NOT_ALLOWED, mensagem, request, null);
    }

    // 415: mandaram um formato que a rota nao aceita (ex: texto puro no POST /transactions, que espera json)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tratarFormatoNaoSuportado(HttpMediaTypeNotSupportedException ex,
                                                                    HttpServletRequest request) {
        String mensagem = "o formato '" + ex.getContentType() + "' nao e aceito nesta rota, aceitos: "
                + ex.getSupportedMediaTypes();
        return montarResposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, mensagem, request, null);
    }

    // 404: a rota nao existe (ex: GET /transacoes em vez de /transactions)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponseDTO> tratarRotaInexistente(NoResourceFoundException ex,
                                                                HttpServletRequest request) {
        return montarResposta(HttpStatus.NOT_FOUND, "a rota " + request.getRequestURI() + " nao existe",
                request, null);
    }

    // rede de seguranca: qualquer erro que nao caiu em nenhum handler acima
    // sem isso, o spring responderia no formato padrao dele (diferente do nosso) e, com o devtools ligado,
    // ainda mandaria o stack trace inteiro pro cliente, mostrando por dentro como o codigo funciona
    // - se for um erro http conhecido do spring (ErrorResponse), mantemos o status que ele escolheu
    // - se for um erro inesperado, respondemos 500 com uma mensagem generica e guardamos os detalhes so no log
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> tratarErroInesperado(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse erroHttp) {
            HttpStatus status = HttpStatus.valueOf(erroHttp.getStatusCode().value());
            return montarResposta(status, "a requisicao nao pode ser atendida: " + status.getReasonPhrase(),
                    request, null);
        }
        LOG.error("erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return montarResposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "ocorreu um erro inesperado, tente novamente mais tarde", request, null);
    }

    // monta a mensagem do campo do json que nao deu pra converter
    // InvalidFormatException e quando veio um valor do tipo certo, mas que nao serve (ex: "PIX" no tipo)
    // os outros casos sao de tipo errado mesmo (ex: uma lista onde era pra ser numero)
    private String descreverValorInvalido(MismatchedInputException erroDoCampo) {
        Class<?> tipoEsperado = erroDoCampo.getTargetType();
        if (erroDoCampo instanceof InvalidFormatException formatoInvalido) {
            return "o valor '" + formatoInvalido.getValue() + "' nao e valido" + dicaDoFormato(tipoEsperado);
        }
        return "o campo veio com um tipo de dado errado" + dicaDoFormato(tipoEsperado);
    }

    // uma dica de como mandar o valor certo, conforme o tipo que o campo ou parametro espera
    private String dicaDoFormato(Class<?> tipoEsperado) {
        if (tipoEsperado == null) {
            return "";
        }
        if (tipoEsperado.isEnum()) {
            // getEnumConstants devolve todos os valores do enum, ex: [DEPOSITO, RETIRADA, TRANSFERENCIA, COMPRA]
            return ", aceitos: " + Arrays.toString(tipoEsperado.getEnumConstants());
        }
        if (tipoEsperado == LocalDate.class) {
            return ", use o formato aaaa-mm-dd (ex: 2026-11-05)";
        }
        if (Number.class.isAssignableFrom(tipoEsperado)) {
            return ", envie um numero (ex: 150.75)";
        }
        return "";
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
        // contentType(JSON) explicito: o erro sai em json mesmo quando o cliente pediu outro formato
        // (ex: o download do relatorio pede "Accept: text/csv"); sem isso o spring tentaria escrever o erro
        // em csv, nao conseguiria, e a resposta sairia sem corpo nenhum
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(corpo);
    }
}
