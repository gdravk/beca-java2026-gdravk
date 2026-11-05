package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;

// uma linha do resumo por mes no json
// ex: { "mes": "2026-11", "quantidade": 7, "total": 420.00 }
// o mes vai como texto "ano-mes" (o mapper usa o toString do YearMonth, que ja sai nesse formato)
@JsonPropertyOrder({"mes", "quantidade", "total"})
public class DespesaPorMesDTO {

    private String mes;
    private int quantidade;
    private BigDecimal total;

    public DespesaPorMesDTO() {
    }

    public DespesaPorMesDTO(String mes, int quantidade, BigDecimal total) {
        this.mes = mes;
        this.quantidade = quantidade;
        this.total = total;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
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
}
