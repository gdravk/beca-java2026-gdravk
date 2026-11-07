package br.com.gabrieloliveira.usuarios.adapter.in.web.seguranca;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

// confere as regras da chave interna na hora de criar o verificador (ou seja, quando a aplicacao sobe)
class VerificadorDeChaveInternaTest {

    @Test
    void chaveAusenteImpedeAAplicacaoDeSubir() {
        assertThrows(IllegalStateException.class, () -> new VerificadorDeChaveInterna(null));
    }

    @Test
    void chaveCurtaImpedeAAplicacaoDeSubir() {
        assertThrows(IllegalStateException.class, () -> new VerificadorDeChaveInterna("curta"));
    }

    @Test
    void chaveDeExemploDoEnvExampleImpedeAAplicacaoDeSubir() {
        // sugestao da revisao 06/11: a chave de exemplo esta no github, entao nao e segredo
        assertThrows(IllegalStateException.class,
                () -> new VerificadorDeChaveInterna(VerificadorDeChaveInterna.CHAVE_DE_EXEMPLO));
    }

    @Test
    void chaveLongaEDiferenteDoExemploEAceita() {
        assertDoesNotThrow(() -> new VerificadorDeChaveInterna("chave-interna-dos-testes-123"));
    }
}
