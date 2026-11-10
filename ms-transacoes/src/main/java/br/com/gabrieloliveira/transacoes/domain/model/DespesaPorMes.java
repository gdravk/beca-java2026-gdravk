package br.com.gabrieloliveira.transacoes.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;

// uma linha do resumo por mes: quanto foi gasto naquele mes e em quantas transacoes
// o YearMonth guarda so o ano e o mes (2026-11), sem o dia, que e exatamente o que precisamos aqui
public class DespesaPorMes {

    private final YearMonth mes;
    private final int quantidade;
    private final BigDecimal total;

    public DespesaPorMes(YearMonth mes, int quantidade, BigDecimal total) {
        this.mes = mes;
        this.quantidade = quantidade;
        this.total = total;
    }

    public YearMonth getMes() {
        return mes;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
