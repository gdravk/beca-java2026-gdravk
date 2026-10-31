package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// valor da transacao convertido para uma moeda estrangeira, com a taxa usada e a data dela
public class ConversaoMoedaDTO {

    private String moeda;
    private BigDecimal taxa;
    private LocalDate dataTaxa;
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
