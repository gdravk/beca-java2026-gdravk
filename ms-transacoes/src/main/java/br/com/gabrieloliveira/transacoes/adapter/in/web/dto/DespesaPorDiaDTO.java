package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

// uma linha do resumo por dia no json
// ex: { "data": "2026-11-05", "quantidade": 2, "total": 80.00 }
@JsonPropertyOrder({"data", "quantidade", "total"})
@Schema(description = "total de um dia")
public class DespesaPorDiaDTO {

    @Schema(description = "dia", example = "2026-11-05")
    private LocalDate data;
    @Schema(description = "quantas despesas nesse dia", example = "2")
    private int quantidade;
    @Schema(description = "soma em reais", example = "80.00")
    private BigDecimal total;

    public DespesaPorDiaDTO() {
    }

    public DespesaPorDiaDTO(LocalDate data, int quantidade, BigDecimal total) {
        this.data = data;
        this.quantidade = quantidade;
        this.total = total;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
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
