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

// essa classe fica "escutando" os erros que escapam dos controllers
// cada metodo com @ExceptionHandler cuida de um tipo de erro e monta a resposta json certinha
// sem ela, o cliente receberia um erro 500 generico e feio
// ela fica no adaptador web porque transformar erro em status http e coisa do mundo http
@RestControllerAdvice
public class GlobalExceptionHandler {

    // log para os erros inesperados (o 500): o cliente recebe uma mensagem generica e os detalhes ficam aqui
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404: o usuario (ou outra coisa) nao foi encontrado
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

    // 401: a seguranca barrou uma rota protegida (chega aqui pelo ErroDeSegurancaHandler)
    // - BadCredentialsException: mandou email e senha, mas estao errados
    // - outras (ex: InsufficientAuthenticationException): nao mandou login nenhum
    // o cabecalho WWW-Authenticate avisa o cliente que a rota espera autenticacao basica
    // (o navegador, por exemplo, abre a janelinha de usuario e senha quando ve esse cabecalho)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponseDTO> tratarNaoAutenticado(AuthenticationException ex,
                                                               HttpServletRequest request) {
        String mensagem = "faca login: envie o email e a senha do usuario (autenticacao basica)";
        if (ex instanceof BadCredentialsException) {
            mensagem = "email ou senha invalidos";
        }
        ErroResponseDTO corpo = new ErroResponseDTO(HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(), mensagem, request.getRequestURI(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"ms-usuarios\"")
                .body(corpo);
    }

    // 403: chamaram o POST /auth/verificar sem a chave interna (so o ms2 pode usar essa rota)
    @ExceptionHandler(ChaveInternaInvalidaException.class)
    public ResponseEntity<ErroResponseDTO> tratarChaveInternaInvalida(ChaveInternaInvalidaException ex,
                                                                     HttpServletRequest request) {
        return montarResposta(HttpStatus.FORBIDDEN, ex.getMessage(), request, null);
    }

    // 403: o usuario esta logado, mas tentou mexer nos dados de outro usuario
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

    // 409: quebrou uma regra, tipo email ou cpf repetido
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDTO> tratarRegraNegocio(RegraNegocioException ex,
                                                             HttpServletRequest request) {
        return montarResposta(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    // 409 tambem: rede de seguranca pro caso raro de duas requisicoes cadastrarem o mesmo email
    // ao mesmo tempo e as duas passarem pela conferencia do caso de uso
    // ai quem barra e o unique do banco, e a gente devolve 409 em vez de 500
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponseDTO> tratarDadoDuplicadoNoBanco(DataIntegrityViolationException ex,
                                                                     HttpServletRequest request) {
        return montarResposta(HttpStatus.CONFLICT, "ja existe um registro com esses dados (email ou cpf repetido)",
                request, null);
    }

    // 400: alguma validacao do UsuarioRequestDTO falhou (@NotBlank, @Email...)
    // aqui juntamos todos os campos errados de uma vez, pra pessoa corrigir tudo numa tacada so
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

    // 400: a validacao dos parametros da url falhou (fase 13), ex: GET /users?tamanho=0 ou GET /users/-5
    // o spring junta os erros por parametro; cada um vira uma linha em "campos", igual aos erros do corpo
    // quando o metodo tem parametro validado E corpo com @Valid (como o PUT), os erros do corpo tambem chegam aqui,
    // dentro de um ParameterErrors, e por isso pegamos os campos dele um por um
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

    // 400: o json veio quebrado (faltou uma virgula, aspas, chave...)
    // ou um campo veio com o tipo de dado errado (ex: "nome": {"a": 1}); nesse caso dizemos qual campo foi
    // (o mesmo tratamento do ms2, pra os dois servicos responderem igual)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> tratarJsonInvalido(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        if (ex.getCause() instanceof MismatchedInputException erroDoCampo && !erroDoCampo.getPath().isEmpty()) {
            // o "path" e o caminho ate o campo; o ultimo pedaco e o nome do campo (ex: "nome")
            List<JacksonException.Reference> caminho = erroDoCampo.getPath();
            String campo = caminho.get(caminho.size() - 1).getPropertyName();
            Map<String, String> campos = new LinkedHashMap<>();
            campos.put(campo, "o campo veio com um tipo de dado errado, envie um texto");
            return montarResposta(HttpStatus.BAD_REQUEST, "existem campos invalidos na requisicao", request, campos);
        }
        return montarResposta(HttpStatus.BAD_REQUEST, "o corpo da requisicao esta mal formatado", request, null);
    }

    // 400: mandaram um texto onde era pra ser numero, tipo GET /users/abc ou ?pagina=abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> tratarTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                             HttpServletRequest request) {
        String mensagem = "o valor '" + ex.getValue() + "' nao e valido para o parametro '" + ex.getName() + "'";
        return montarResposta(HttpStatus.BAD_REQUEST, mensagem, request, null);
    }

    // 400: problema no arquivo inteiro da importacao (vazio, nao e .csv, cabecalho errado, nao e utf-8...)
    // lembrando: erro de uma linha so nao cai aqui, ele aparece no resumo da importacao
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

    // 413: o arquivo passou do tamanho maximo configurado no application.properties (spring.servlet.multipart)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErroResponseDTO> tratarArquivoGrandeDemais(MaxUploadSizeExceededException ex,
                                                                    HttpServletRequest request) {
        return montarResposta(HttpStatus.CONTENT_TOO_LARGE, "o arquivo pode ter no maximo 2MB", request, null);
    }

    // 415: mandaram um formato que a rota nao aceita
    // ex: json no /users/import (que espera form-data) ou form-data no POST /users (que espera json)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tratarFormatoNaoSuportado(HttpMediaTypeNotSupportedException ex,
                                                                    HttpServletRequest request) {
        String mensagem = "o formato '" + ex.getContentType() + "' nao e aceito nesta rota, aceitos: "
                + ex.getSupportedMediaTypes();
        return montarResposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, mensagem, request, null);
    }

    // 405: a rota existe, mas nao com esse metodo http (ex: PATCH /users/1, que so aceita GET, PUT e DELETE)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tratarMetodoNaoAceito(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest request) {
        String mensagem = "o metodo " + ex.getMethod() + " nao e aceito nesta rota, aceitos: "
                + ex.getSupportedHttpMethods();
        return montarResposta(HttpStatus.METHOD_NOT_ALLOWED, mensagem, request, null);
    }

    // 404: a rota nao existe (ex: GET /usuarios em vez de /users)
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

    // metodo de apoio pra nao repetir o mesmo codigo em todos os handlers acima
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
