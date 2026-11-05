package br.com.gabrieloliveira.transacoes.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

// uma linha do resumo por dia: quanto foi gasto naquela data e em quantas transacoes
public class DespesaPorDia {

    private final LocalDate data;
    private final int quantidade;
    private final BigDecimal total;

    public DespesaPorDia(LocalDate data, int quantidade, BigDecimal total) {
        this.data = data;
        this.quantidade = quantidade;
        this.total = total;
    }

    public LocalDate getData() {
        return data;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
