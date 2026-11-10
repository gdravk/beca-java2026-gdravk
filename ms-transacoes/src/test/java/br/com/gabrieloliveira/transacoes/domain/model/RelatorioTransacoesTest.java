package br.com.gabrieloliveira.transacoes.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// resumo do relatorio em csv (pedido 7)
class RelatorioTransacoesTest {

    private static final Periodo NOVEMBRO =
            Periodo.resolver(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 15));

    @Test
    void somaDepositosESaidasAprovadasEContaAsRejeitadas() {
        // dado: deposito de 1000, compra de 150,75 e retirada de 49,25 (aprovados), e uma compra rejeitada de 5000
        Transacao rejeitada = transacao(TipoTransacao.COMPRA, "5000.00");
        rejeitada.rejeitar("saldo insuficiente");
        List<Transacao> transacoes = List.of(
                aprovada(TipoTransacao.DEPOSITO, "1000.00"),
                aprovada(TipoTransacao.COMPRA, "150.75"),
                aprovada(TipoTransacao.RETIRADA, "49.25"),
                rejeitada);

        // quando
        RelatorioTransacoes relatorio = RelatorioTransacoes.montar(1L, NOVEMBRO, transacoes);

        // entao: a rejeitada aparece na lista, mas nao entra nos totais em dinheiro
        assertEquals(4, relatorio.getTransacoes().size());
        assertEquals(3, relatorio.getQuantidadeAprovadas());
        assertEquals(1, relatorio.getQuantidadeRejeitadas());
        assertEquals(new BigDecimal("1000.00"), relatorio.getTotalDepositosAprovados());
        assertEquals(new BigDecimal("200.00"), relatorio.getTotalSaidasAprovadas());
    }

    @Test
    void semTransacoesOsTotaisSaoZero() {
        RelatorioTransacoes relatorio = RelatorioTransacoes.montar(1L, NOVEMBRO, List.of());

        assertEquals(0, relatorio.getQuantidadeAprovadas());
        assertEquals(0, relatorio.getQuantidadeRejeitadas());
        assertEquals(0, BigDecimal.ZERO.compareTo(relatorio.getTotalDepositosAprovados()));
        assertEquals(0, BigDecimal.ZERO.compareTo(relatorio.getTotalSaidasAprovadas()));
    }

    @Test
    void oRelatorioGuardaUmaCopiaQueNaoPodeSerAlterada() {
        List<Transacao> original = new ArrayList<>(List.of(aprovada(TipoTransacao.DEPOSITO, "10.00")));
        RelatorioTransacoes relatorio = RelatorioTransacoes.montar(1L, NOVEMBRO, original);

        // mexer na lista original depois nao muda o relatorio
        original.add(aprovada(TipoTransacao.DEPOSITO, "99.00"));
        assertEquals(1, relatorio.getTransacoes().size());
        // e a lista do relatorio nao aceita alteracao
        assertThrows(UnsupportedOperationException.class,
                () -> relatorio.getTransacoes().add(aprovada(TipoTransacao.DEPOSITO, "1.00")));
    }

    private Transacao aprovada(TipoTransacao tipo, String valor) {
        Transacao transacao = transacao(tipo, valor);
        transacao.aprovar();
        return transacao;
    }

    private Transacao transacao(TipoTransacao tipo, String valor) {
        Transacao transacao = new Transacao();
        transacao.setUsuarioId(1L);
        transacao.setTipo(tipo);
        transacao.setValor(new BigDecimal(valor));
        return transacao;
    }
}
