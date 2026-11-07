package br.com.gabrieloliveira.usuarios.adapter.in.web;

import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.ErroResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.exc.MismatchedInputException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// teste do handler chamado direto, sem subir o spring
// serve para montar na mao um erro do jackson que hoje nenhum dto nosso produz:
// um erro dentro de um item de lista (nenhum corpo de requisicao tem lista ainda, mas pode vir a ter)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void erroNumItemDeListaFicaNoCampoCorpoEmVezDeUmaChaveNula() {
        // o caminho do erro fica [itens, 0]: o ultimo pedaco e a posicao 0, que nao tem nome
        // (getPropertyName volta null); ajuste da revisao do professor 06/11
        MismatchedInputException erroDoJackson =
                MismatchedInputException.from(null, Integer.class, "tipo errado no item da lista");
        erroDoJackson.prependPath(new Object(), 0);
        erroDoJackson.prependPath(new Object(), "itens");
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "json invalido", erroDoJackson, new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ErroResponseDTO> resposta =
                handler.tratarJsonInvalido(ex, new MockHttpServletRequest("POST", "/users"));

        assertEquals(400, resposta.getStatusCode().value());
        Map<String, String> campos = resposta.getBody().getCampos();
        assertFalse(campos.containsKey(null));
        assertTrue(campos.containsKey("corpo"));
    }

    @Test
    void erroNumCampoComNomeContinuaUsandoONomeDoCampo() {
        MismatchedInputException erroDoJackson =
                MismatchedInputException.from(null, String.class, "tipo errado");
        erroDoJackson.prependPath(new Object(), "nome");
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "json invalido", erroDoJackson, new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ErroResponseDTO> resposta =
                handler.tratarJsonInvalido(ex, new MockHttpServletRequest("POST", "/users"));

        assertTrue(resposta.getBody().getCampos().containsKey("nome"));
    }
}
