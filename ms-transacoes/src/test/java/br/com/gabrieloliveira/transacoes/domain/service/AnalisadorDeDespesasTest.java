package br.com.gabrieloliveira.transacoes.domain.service;

import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorCategoria;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorDia;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorMes;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// testes das regras da analise de despesas
// o AnalisadorDeDespesas e java puro e nao depende de nada, entao nem precisa de mock: e so criar e chamar
class AnalisadorDeDespesasTest {

    private final AnalisadorDeDespesas analisador = new AnalisadorDeDespesas();

    @Test
    void cincoComprasEmTresCategoriasGeramTresLinhasEOTotalBateComASoma() {
        // criterio do PO: "com cinco compras em tres categorias, o resumo mostra tres linhas
        // e o total bate com a soma"
        List<Transacao> despesas = List.of(
                compra("50.00", Categoria.ALIMENTACAO, dia(3)),
                compra("30.00", Categoria.ALIMENTACAO, dia(4)),
                compra("20.00", Categoria.TRANSPORTE, dia(4)),
                compra("70.00", Categoria.LAZER, dia(5)),
                compra("30.00", Categoria.LAZER, dia(6)));

        List<DespesaPorCategoria> resumo = analisador.agruparPorCategoria(despesas);

        assertEquals(3, resumo.size());
        assertEquals(new BigDecimal("200.00"), analisador.somar(despesas));

        // ordem: do maior total pro menor (LAZER 100, ALIMENTACAO 80, TRANSPORTE 20)
        assertEquals(Categoria.LAZER, resumo.get(0).getCategoria());
        assertEquals(new BigDecimal("100.00"), resumo.get(0).getTotal());
        assertEquals(2, resumo.get(0).getQuantidade());
        assertEquals(new BigDecimal("50.00"), resumo.get(0).getPercentual());

        assertEquals(Categoria.ALIMENTACAO, resumo.get(1).getCategoria());
        assertEquals(new BigDecimal("80.00"), resumo.get(1).getTotal());
        assertEquals(new BigDecimal("40.00"), resumo.get(1).getPercentual());

        assertEquals(Categoria.TRANSPORTE, resumo.get(2).getCategoria());
        assertEquals(new BigDecimal("20.00"), resumo.get(2).getTotal());
        assertEquals(new BigDecimal("10.00"), resumo.get(2).getPercentual());

        // e a soma das linhas bate com o total geral
        BigDecimal somaDasLinhas = BigDecimal.ZERO;
        for (DespesaPorCategoria linha : resumo) {
            somaDasLinhas = somaDasLinhas.add(linha.getTotal());
        }
        assertEquals(analisador.somar(despesas), somaDasLinhas);
    }

    @Test
    void rejeitadasEDepositosNaoSaoDespesa() {
        // criterio do PO: "transacoes rejeitadas e depositos nao aparecem no resumo"
        Transacao compraAprovada = compra("40.00", Categoria.SAUDE, dia(2));
        Transacao compraRejeitada = compra("999.00", Categoria.LAZER, dia(2));
        compraRejeitada.rejeitar("saldo insuficiente");
        Transacao deposito = transacao(TipoTransacao.DEPOSITO, "500.00", null, dia(2));

        List<Transacao> despesas = analisador.somenteDespesas(List.of(compraAprovada, compraRejeitada, deposito));

        assertEquals(1, despesas.size());
        assertEquals(compraAprovada, despesas.get(0));
    }

    @Test
    void retiradaETransferenciaAprovadasSaoDespesa() {
        assertTrue(analisador.isDespesa(transacao(TipoTransacao.RETIRADA, "10.00", Categoria.OUTROS, dia(1))));
        assertTrue(analisador.isDespesa(transacao(TipoTransacao.TRANSFERENCIA, "10.00", Categoria.OUTROS, dia(1))));
        assertTrue(analisador.isDespesa(compra("10.00", Categoria.MORADIA, dia(1))));
    }

    @Test
    void estornoNaoEDespesaMesmoSendoUmaRetirada() {
        // o estorno de um deposito vira uma RETIRADA, mas ele so corrige o deposito errado: nao e gasto
        Transacao estornoDeDeposito = transacao(TipoTransacao.RETIRADA, "100.00", Categoria.OUTROS, dia(3));
        estornoDeDeposito.setTransacaoOriginalId(10L);

        assertFalse(analisador.isDespesa(estornoDeDeposito));
    }

    @Test
    void resumoPorDiaSaiEmOrdemDeDataEJuntaAsDoMesmoDia() {
        // mesmo dia em horarios diferentes, e fora de ordem na lista
        List<Transacao> despesas = List.of(
                compra("15.00", Categoria.ALIMENTACAO, LocalDateTime.of(2026, 11, 4, 20, 30)),
                compra("10.00", Categoria.ALIMENTACAO, LocalDateTime.of(2026, 11, 2, 9, 0)),
                compra("5.50", Categoria.TRANSPORTE, LocalDateTime.of(2026, 11, 4, 8, 15)));

        List<DespesaPorDia> resumo = analisador.agruparPorDia(despesas);

        assertEquals(2, resumo.size());
        assertEquals(LocalDate.of(2026, 11, 2), resumo.get(0).getData());
        assertEquals(new BigDecimal("10.00"), resumo.get(0).getTotal());
        assertEquals(LocalDate.of(2026, 11, 4), resumo.get(1).getData());
        assertEquals(new BigDecimal("20.50"), resumo.get(1).getTotal());
        assertEquals(2, resumo.get(1).getQuantidade());
    }

    @Test
    void resumoPorMesSaiEmOrdemEJuntaAsDoMesmoMes() {
        List<Transacao> despesas = List.of(
                compra("100.00", Categoria.MORADIA, LocalDateTime.of(2026, 11, 1, 10, 0)),
                compra("40.00", Categoria.LAZER, LocalDateTime.of(2026, 9, 30, 23, 59)),
                compra("60.00", Categoria.LAZER, LocalDateTime.of(2026, 11, 30, 12, 0)),
                compra("25.00", Categoria.SAUDE, LocalDateTime.of(2026, 10, 1, 0, 0)));

        List<DespesaPorMes> resumo = analisador.agruparPorMes(despesas);

        assertEquals(3, resumo.size());
        assertEquals(YearMonth.of(2026, 9), resumo.get(0).getMes());
        assertEquals(new BigDecimal("40.00"), resumo.get(0).getTotal());
        assertEquals(YearMonth.of(2026, 10), resumo.get(1).getMes());
        assertEquals(YearMonth.of(2026, 11), resumo.get(2).getMes());
        assertEquals(new BigDecimal("160.00"), resumo.get(2).getTotal());
        assertEquals(2, resumo.get(2).getQuantidade());
    }

    @Test
    void semDespesasOResumoVemVazioEOTotalEZero() {
        assertTrue(analisador.agruparPorCategoria(List.of()).isEmpty());
        assertTrue(analisador.agruparPorDia(List.of()).isEmpty());
        assertTrue(analisador.agruparPorMes(List.of()).isEmpty());
        assertEquals(new BigDecimal("0.00"), analisador.somar(List.of()));
    }

    @Test
    void transacaoSemCategoriaEntraComoOutros() {
        List<DespesaPorCategoria> resumo = analisador.agruparPorCategoria(
                List.of(transacao(TipoTransacao.RETIRADA, "30.00", null, dia(1))));

        assertEquals(Categoria.OUTROS, resumo.get(0).getCategoria());
        assertEquals(new BigDecimal("100.00"), resumo.get(0).getPercentual());
    }

    @Test
    void percentualComDizimaEArredondadoComDuasCasas() {
        // 3 categorias com o mesmo valor: cada uma e 33.33% (e empatadas saem na ordem do enum)
        List<DespesaPorCategoria> resumo = analisador.agruparPorCategoria(List.of(
                compra("10.00", Categoria.LAZER, dia(1)),
                compra("10.00", Categoria.ALIMENTACAO, dia(1)),
                compra("10.00", Categoria.SAUDE, dia(1))));

        assertEquals(new BigDecimal("33.33"), resumo.get(0).getPercentual());
        assertEquals(Categoria.ALIMENTACAO, resumo.get(0).getCategoria());
        assertEquals(Categoria.LAZER, resumo.get(1).getCategoria());
        assertEquals(Categoria.SAUDE, resumo.get(2).getCategoria());
    }

    // metodos de apoio pra montar as transacoes dos testes

    private Transacao compra(String valor, Categoria categoria, LocalDateTime dataHora) {
        return transacao(TipoTransacao.COMPRA, valor, categoria, dataHora);
    }

    private Transacao transacao(TipoTransacao tipo, String valor, Categoria categoria, LocalDateTime dataHora) {
        Transacao transacao = new Transacao();
        transacao.setUsuarioId(1L);
        transacao.setTipo(tipo);
        transacao.setValor(new BigDecimal(valor));
        transacao.setCategoria(categoria);
        transacao.setDataHora(dataHora);
        transacao.aprovar();
        return transacao;
    }

    private LocalDateTime dia(int diaDeNovembro) {
        return LocalDateTime.of(2026, 11, diaDeNovembro, 12, 0);
    }
}
