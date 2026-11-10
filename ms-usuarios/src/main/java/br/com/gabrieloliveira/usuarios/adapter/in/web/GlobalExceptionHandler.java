package br.com.gabrieloliveira.usuarios.adapter.in.web;

import br.com.gabrieloliveira.usuarios.adapter.in.web.csv.ArquivoCsvInvalidoException;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.ErroResponseDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.seguranca.ChaveInternaInvalidaException;
import br.com.gabrieloliveira.usuarios.domain.exception.AcessoNegadoException;
import br.com.gabrieloliveira.usuarios.domain.exception.CredenciaisInvalidasException;
import br.com.gabrieloliveira.usuarios.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.usuarios.domain.exception.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// esta classe "escuta" os erros que escapam dos controllers e transforma cada um numa resposta json
// caprichada: cada metodo com @ExceptionHandler cuida de um tipo de erro
// sem ela, quem chamasse a api receberia um erro 500 generico e sem explicacao
// ela mora no adaptador web porque traduzir erro em status http e assunto do mundo http, e nao do dominio
@RestControllerAdvice
public class GlobalExceptionHandler {

    // log dos erros inesperados (500): o cliente recebe uma mensagem generica e os detalhes ficam guardados aqui
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404: o usuario (ou outro recurso) nao foi encontrado
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
                                                              HttpServletRequest request) {
        return montarResposta(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    // 401: o POST /auth/verificar recebeu email ou senha errados
    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponseDTO> tratarCredenciaisInvalidas(CredenciaisInvalidasException ex,
                                                                     HttpServletRequest request) {
        return montarResposta(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
    }

    // 401: o spring security barrou uma rota protegida (o erro chega aqui pelo ErroDeSegurancaHandler)
    // - BadCredentialsException: a pessoa mandou email e senha, mas eles estao errados
    // - qualquer outra (InsufficientAuthenticationException, por exemplo): nao mandou login nenhum
    // o cabecalho WWW-Authenticate avisa que a rota espera autenticacao basica; e por causa dele que
    // o navegador abre aquela janelinha pedindo usuario e senha
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponseDTO> tratarNaoAutenticado(AuthenticationException ex,
                                                               HttpServletRequest request) {
        String mensagem = "faca login: envie o email e a senha do usuario (autenticacao basica)";
        if (ex instanceof BadCredentialsException) {
            mensagem = "email ou senha invalidos";
        }
        ErroResponseDTO corpo = new ErroResponseDTO(HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(), mensagem, request.getRequestURI(), null);
        // o contentType(JSON) e explicito pelo mesmo motivo explicado no montarResposta (o 401 nao passa por ele)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"ms-usuarios\"")
                .body(corpo);
    }

    // 403: chamaram o POST /auth/verificar sem a chave interna (essa rota e so para o ms2)
    @ExceptionHandler(ChaveInternaInvalidaException.class)
    public ResponseEntity<ErroResponseDTO> tratarChaveInternaInvalida(ChaveInternaInvalidaException ex,
                                                                     HttpServletRequest request) {
        return montarResposta(HttpStatus.FORBIDDEN, ex.getMessage(), request, null);
    }

    // 403: a pessoa esta logada, mas tentou mexer nos dados de outro usuario
    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponseDTO> tratarAcessoNegado(AcessoNegadoException ex,
                                                             HttpServletRequest request) {
        return montarResposta(HttpStatus.FORBIDDEN, ex.getMessage(), request, null);
    }

    // 403 vindo do proprio spring security (tambem chega aqui pelo ErroDeSegurancaHandler)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponseDTO> tratarAcessoNegadoPelaSeguranca(AccessDeniedException ex,
                                                                          HttpServletRequest request) {
        return montarResposta(HttpStatus.FORBIDDEN, "voce nao tem permissao para acessar este recurso",
                request, null);
    }

    // 409: alguma regra foi quebrada, como cadastrar um email ou cpf que ja existe
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDTO> tratarRegraNegocio(RegraNegocioException ex,
                                                             HttpServletRequest request) {
        return montarResposta(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    // 409 tambem: rede de seguranca para o caso raro de duas requisicoes cadastrarem o mesmo email no mesmo
    // instante e as duas passarem pela conferencia do caso de uso. nessa hora quem barra e o "unique" do banco,
    // e devolvemos 409 em vez de deixar virar um erro 500
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponseDTO> tratarDadoDuplicadoNoBanco(DataIntegrityViolationException ex,
                                                                     HttpServletRequest request) {
        return montarResposta(HttpStatus.CONFLICT, "ja existe um registro com esses dados (email ou cpf repetido)",
                request, null);
    }

    // 400: alguma validacao do corpo falhou (@NotBlank, @Email...)
    // juntamos todos os campos errados de uma vez, para a pessoa corrigir tudo numa tacada so
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

    // 400: a validacao dos parametros da url falhou, como em GET /users?tamanho=0 ou GET /users/-5
    // o spring agrupa os erros por parametro, e cada um vira uma linha em "campos", igual aos erros do corpo
    // quando o metodo tem parametro validado E corpo com @Valid (caso do PUT), os erros do corpo tambem chegam
    // por aqui, dentro de um ParameterErrors; por isso pegamos os campos dele um por um
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

    // 400: o json chegou quebrado (faltou virgula, aspas, chave...) ou algum campo veio com o tipo errado,
    // como "nome": {"a": 1}. no segundo caso dizemos qual foi o campo, do mesmo jeito que o ms2 faz,
    // para os dois servicos responderem igual
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> tratarJsonInvalido(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        if (ex.getCause() instanceof MismatchedInputException erroDoCampo && !erroDoCampo.getPath().isEmpty()) {
            // o "path" e o caminho ate o campo com problema; o ultimo pedaco dele e o nome do campo (ex: "nome")
            List<JacksonException.Reference> caminho = erroDoCampo.getPath();
            String campo = caminho.get(caminho.size() - 1).getPropertyName();
            // se o erro estiver num item de lista (o corpo inteiro mandado como [ ... ], por exemplo), o ultimo pedaco
            // do caminho e uma posicao (0, 1...) e nao um nome, e o getPropertyName volta null. uma chave null quebraria
            // o json da resposta, entao nesse caso o erro fica no "corpo" (pedido do professor na revisao de 06/11)
            if (campo == null) {
                campo = "corpo";
            }
            Map<String, String> campos = new LinkedHashMap<>();
            campos.put(campo, "o campo veio com um tipo de dado errado, envie um texto");
            return montarResposta(HttpStatus.BAD_REQUEST, "existem campos invalidos na requisicao", request, campos);
        }
        return montarResposta(HttpStatus.BAD_REQUEST, "o corpo da requisicao esta mal formatado", request, null);
    }

    // 400: veio texto onde era para vir numero, como GET /users/abc ou ?pagina=abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> tratarTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                             HttpServletRequest request) {
        String mensagem = "o valor '" + ex.getValue() + "' nao e valido para o parametro '" + ex.getName() + "'";
        return montarResposta(HttpStatus.BAD_REQUEST, mensagem, request, null);
    }

    // 400: problema no arquivo inteiro da importacao (vazio, nao e .csv, cabecalho errado, nao e utf-8...)
    // o erro de uma linha so nao cai aqui: ele aparece no resumo da importacao, junto com as outras linhas
    @ExceptionHandler(ArquivoCsvInvalidoException.class)
    public ResponseEntity<ErroResponseDTO> tratarArquivoInvalido(ArquivoCsvInvalidoException ex,
                                                                HttpServletRequest request) {
        return montarResposta(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    // 400: chamaram o POST /users/import sem mandar o arquivo no campo "file" do form-data
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErroResponseDTO> tratarArquivoFaltando(MissingServletRequestPartException ex,
                                                                HttpServletRequest request) {
        String mensagem = "envie o arquivo no campo '" + ex.getRequestPartName() + "' (Body -> form-data, tipo File)";
        return montarResposta(HttpStatus.BAD_REQUEST, mensagem, request, null);
    }

    // 413: o arquivo passou do tamanho maximo (spring.servlet.multipart, no application.properties)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErroResponseDTO> tratarArquivoGrandeDemais(MaxUploadSizeExceededException ex,
                                                                    HttpServletRequest request) {
        return montarResposta(HttpStatus.CONTENT_TOO_LARGE, "o arquivo pode ter no maximo 2MB", request, null);
    }

    // 415: mandaram um formato que a rota nao aceita, como json no /users/import (que espera form-data)
    // ou form-data no POST /users (que espera json)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tratarFormatoNaoSuportado(HttpMediaTypeNotSupportedException ex,
                                                                    HttpServletRequest request) {
        String mensagem = "o formato '" + ex.getContentType() + "' nao e aceito nesta rota, aceitos: "
                + ex.getSupportedMediaTypes();
        return montarResposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, mensagem, request, null);
    }

    // 405: a rota existe, mas nao com esse metodo http (PATCH /users/1, por exemplo, que so aceita GET, PUT e DELETE)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tratarMetodoNaoAceito(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest request) {
        String mensagem = "o metodo " + ex.getMethod() + " nao e aceito nesta rota, aceitos: "
                + ex.getSupportedHttpMethods();
        return montarResposta(HttpStatus.METHOD_NOT_ALLOWED, mensagem, request, null);
    }

    // 404: a rota nem existe (GET /usuarios em vez de /users, por exemplo)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponseDTO> tratarRotaInexistente(NoResourceFoundException ex,
                                                                HttpServletRequest request) {
        return montarResposta(HttpStatus.NOT_FOUND, "a rota " + request.getRequestURI() + " nao existe",
                request, null);
    }

    // rede de seguranca: pega qualquer erro que nao caiu em nenhum dos metodos acima
    // sem ela, o spring responderia no formato padrao dele (diferente do nosso) e, com o devtools ligado,
    // ainda mandaria o stack trace inteiro para o cliente, mostrando como o codigo funciona por dentro
    // - se for um erro http que o spring ja conhece (ErrorResponse), mantemos o status que ele escolheu
    // - se for um erro inesperado de verdade, respondemos 500 com uma mensagem generica e os detalhes vao so para o log
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

    // metodo de apoio, para nao repetir a montagem da resposta em todos os metodos acima
    private ResponseEntity<ErroResponseDTO> montarResposta(HttpStatus status, String mensagem,
                                                           HttpServletRequest request, Map<String, String> campos) {
        ErroResponseDTO corpo = new ErroResponseDTO(
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI(),
                campos
        );
        // o contentType(JSON) explicito garante que o erro sai em json mesmo quando o cliente pediu outro formato
        // (o download do relatorio, por exemplo, pede "Accept: text/csv"); sem ele o spring tentaria escrever
        // o erro em csv, nao conseguiria, e a resposta sairia sem corpo nenhum
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(corpo);
    }
}
