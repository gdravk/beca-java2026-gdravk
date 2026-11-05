package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;

// uma linha do resumo por categoria no json
// ex: { "categoria": "ALIMENTACAO", "quantidade": 3, "total": 150.00, "percentual": 60.00 }
@JsonPropertyOrder({"categoria", "quantidade", "total", "percentual"})
public class DespesaPorCategoriaDTO {

    private Categoria categoria;
    private int quantidade;
    private BigDecimal total;
    // quanto essa categoria representa do total gasto no periodo, em %
    private BigDecimal percentual;

    public DespesaPorCategoriaDTO() {
    }

    public DespesaPorCategoriaDTO(Categoria categoria, int quantidade, BigDecimal total, BigDecimal percentual) {
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.total = total;
        this.percentual = percentual;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getPercentual() {
        return percentual;
    }

    public void setPercentual(BigDecimal percentual) {
        this.percentual = percentual;
    }
}
