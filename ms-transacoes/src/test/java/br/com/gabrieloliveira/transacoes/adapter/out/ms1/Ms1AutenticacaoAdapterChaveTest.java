package br.com.gabrieloliveira.transacoes.adapter.out.ms1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

// confere a chave interna na hora de criar o adaptador (ou seja, quando a aplicacao sobe)
// nenhuma chamada de verdade ao ms1 e feita aqui
class Ms1AutenticacaoAdapterChaveTest {

    private static final String URL_DO_MS1 = "http://localhost:8081";

    @Test
    void chaveVaziaImpedeAAplicacaoDeSubir() {
        assertThrows(IllegalStateException.class, () -> new Ms1AutenticacaoAdapter(URL_DO_MS1, 1000, " "));
    }

    @Test
    void chaveDeExemploDoEnvExampleImpedeAAplicacaoDeSubir() {
        // sugestao do professor na revisao de 06/11: a chave de exemplo esta no github, entao nao e segredo
        assertThrows(IllegalStateException.class,
                () -> new Ms1AutenticacaoAdapter(URL_DO_MS1, 1000, Ms1AutenticacaoAdapter.CHAVE_DE_EXEMPLO));
    }

    @Test
    void chaveDeVerdadeEAceita() {
        assertDoesNotThrow(() -> new Ms1AutenticacaoAdapter(URL_DO_MS1, 1000, "chave-interna-dos-testes-123"));
    }
}
