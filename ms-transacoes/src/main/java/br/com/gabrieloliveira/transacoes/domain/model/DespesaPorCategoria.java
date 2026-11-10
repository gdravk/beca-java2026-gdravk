package br.com.gabrieloliveira.transacoes.domain.model;

import java.math.BigDecimal;

// uma linha do resumo por categoria: quanto foi gasto nela, em quantas transacoes e quanto isso
// representa do total gasto no periodo (o percentual)
public class DespesaPorCategoria {

    private final Categoria categoria;
    private final int quantidade;
    private final BigDecimal total;
    private final BigDecimal percentual;

    public DespesaPorCategoria(Categoria categoria, int quantidade, BigDecimal total, BigDecimal percentual) {
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.total = total;
        this.percentual = percentual;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getPercentual() {
        return percentual;
    }
}
