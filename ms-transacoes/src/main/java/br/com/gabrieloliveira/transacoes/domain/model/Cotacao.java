package br.com.gabrieloliveira.transacoes.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

// cotacao de uma moeda estrangeira em reais, na data informada
// taxa = quantos reais vale 1 unidade da moeda (ex: USD 5.4321 = 1 dolar custa R$ 5,4321)
public class Cotacao {

    private String moeda;
    private BigDecimal taxa;
    private LocalDate data;

    public Cotacao() {
    }

    public Cotacao(String moeda, BigDecimal taxa, LocalDate data) {
        this.moeda = moeda;
        this.taxa = taxa;
        this.data = data;
    }

    // converte um valor em reais para a moeda desta cotacao, com 2 casas decimais
    public BigDecimal converterDeReais(BigDecimal valorEmReais) {
        return valorEmReais.divide(taxa, 2, RoundingMode.HALF_EVEN);
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

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }
}
