package br.com.gabrieloliveira.usuarios.adapter.in.web;

import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.ErroResponseDTO;
import br.com.gabrieloliveira.usuarios.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.usuarios.domain.exception.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

// essa classe fica "escutando" os erros que escapam dos controllers
// cada metodo com @ExceptionHandler cuida de um tipo de erro e monta a resposta json certinha
// sem ela, o cliente receberia um erro 500 generico e feio
// ela fica no adaptador web porque transformar erro em status http e coisa do mundo http
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404: o usuario (ou outra coisa) nao foi encontrado
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
                                                              HttpServletRequest request) {
        return montarResposta(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
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
        // LinkedHashMap mantem a ordem em que os erros foram adicionados
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erroDoCampo : ex.getBindingResult().getFieldErrors()) {
            campos.put(erroDoCampo.getField(), erroDoCampo.getDefaultMessage());
        }
        return montarResposta(HttpStatus.BAD_REQUEST, "existem campos invalidos na requisicao", request, campos);
    }

    // 400: o json veio quebrado (faltou uma virgula, aspas, chave...)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> tratarJsonInvalido(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        return montarResposta(HttpStatus.BAD_REQUEST, "o corpo da requisicao esta mal formatado", request, null);
    }

    // 400: mandaram um texto onde era pra ser numero, tipo GET /users/abc ou ?pagina=abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> tratarTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                             HttpServletRequest request) {
        String mensagem = "o valor '" + ex.getValue() + "' nao e valido para o parametro '" + ex.getName() + "'";
        return montarResposta(HttpStatus.BAD_REQUEST, mensagem, request, null);
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
