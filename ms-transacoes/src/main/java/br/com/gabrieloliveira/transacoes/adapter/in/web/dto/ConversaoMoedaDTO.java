package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

// valor da transacao convertido para uma moeda estrangeira, com a taxa usada e a data dela
@Schema(description = "valor da transacao em outra moeda")
public class ConversaoMoedaDTO {

    @Schema(description = "codigo da moeda", example = "USD")
    private String moeda;
    @Schema(description = "quantos reais vale 1 unidade da moeda", example = "5.4321")
    private BigDecimal taxa;
    @Schema(description = "dia da cotacao usada (no fim de semana e feriado, o ultimo dia util)", example = "2026-11-05")
    private LocalDate dataTaxa;
    @Schema(description = "valor da transacao nessa moeda, com 2 casas", example = "27.75")
    private BigDecimal valorConvertido;

    public ConversaoMoedaDTO() {
    }

    public ConversaoMoedaDTO(String moeda, BigDecimal taxa, LocalDate dataTaxa, BigDecimal valorConvertido) {
        this.moeda = moeda;
        this.taxa = taxa;
        this.dataTaxa = dataTaxa;
        this.valorConvertido = valorConvertido;
    }

    public String getMoeda() {
        return moeda;
    }

    public void setMoeda(String moeda) {
        this.moeda = moeda;
    }

    public BigDecimal getTaxa() {
        return taxa;
    }

    public void setTaxa(BigDecimal taxa) {
        this.taxa = taxa;
    }

    public LocalDate getDataTaxa() {
        return dataTaxa;
    }

    public void setDataTaxa(LocalDate dataTaxa) {
        this.dataTaxa = dataTaxa;
    }

    public BigDecimal getValorConvertido() {
        return valorConvertido;
    }

    public void setValorConvertido(BigDecimal valorConvertido) {
        this.valorConvertido = valorConvertido;
    }
}
