package br.com.gabrieloliveira.transacoes.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

// conversao de reais para dolar/euro, usada para mostrar o valor da transacao nas outras moedas (pedido 3)
class CotacaoTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 5);

    @Test
    void converteReaisParaAMoedaComDuasCasas() {
        // dado: 1 dolar custa 5,4321 reais
        Cotacao dolar = new Cotacao("USD", new BigDecimal("5.4321"), DIA);

        // quando: 100 reais em dolar
        BigDecimal emDolar = dolar.converterDeReais(new BigDecimal("100.00"));

        // entao: 100 / 5,4321 = 18,4091... -> 18,41
        assertEquals(new BigDecimal("18.41"), emDolar);
    }

    @Test
    void empateNoArredondamentoVaiParaOParMaisProximo() {
        // HALF_EVEN ("arredondamento do banqueiro"): quando cai exatamente no meio,
        // arredonda para o numero par; assim, em muitas contas, os arredondamentos se compensam
        Cotacao moeda = new Cotacao("USD", new BigDecimal("10"), DIA);

        // 1,25 / 10 = 0,125 -> 0,12 (o 2 ja e par)
        assertEquals(new BigDecimal("0.12"), moeda.converterDeReais(new BigDecimal("1.25")));
        // 1,35 / 10 = 0,135 -> 0,14 (sobe para o 4, que e par)
        assertEquals(new BigDecimal("0.14"), moeda.converterDeReais(new BigDecimal("1.35")));
    }
}
