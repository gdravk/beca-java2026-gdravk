package br.com.gabrieloliveira.transacoes.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipoTransacaoTest {

    @Test
    void soODepositoNaoESaida() {
        // saida = passa pela checagem de saldo e limites
        assertFalse(TipoTransacao.DEPOSITO.isSaida());
        assertTrue(TipoTransacao.RETIRADA.isSaida());
        assertTrue(TipoTransacao.TRANSFERENCIA.isSaida());
        assertTrue(TipoTransacao.COMPRA.isSaida());
    }

    @Test
    void estornoDeUmaSaidaDevolveODinheiroComoDeposito() {
        assertEquals(TipoTransacao.DEPOSITO, TipoTransacao.RETIRADA.inverso());
        assertEquals(TipoTransacao.DEPOSITO, TipoTransacao.TRANSFERENCIA.inverso());
        assertEquals(TipoTransacao.DEPOSITO, TipoTransacao.COMPRA.inverso());
    }

    @Test
    void estornoDeUmDepositoTiraODinheiroComoRetirada() {
        assertEquals(TipoTransacao.RETIRADA, TipoTransacao.DEPOSITO.inverso());
    }
}
