package br.com.gabrieloliveira.transacoes.adapter.out.brasilapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// a resposta inteira da brasilapi em GET /cambio/v1/cotacao/{moeda}/{data}, por exemplo:
// { "moeda": "USD", "data": "2026-10-30", "cotacoes": [ {boletim}, {boletim}, ... ] }
// detalhe importante: "data" e o dia da cotacao que ela REALMENTE devolveu
// pedindo um sabado, domingo ou feriado, ela devolve o ultimo dia util antes dele (ex: pediu 01/11, veio 30/10)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CotacaoBrasilApiDTO {

    private String moeda;
    // texto no formato aaaa-mm-dd; o adaptador converte pra LocalDate
    private String data;
    private List<BoletimBrasilApiDTO> cotacoes;

    public CotacaoBrasilApiDTO() {
    }

    public String getMoeda() {
        return moeda;
    }

    public void setMoeda(String moeda) {
        this.moeda = moeda;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<BoletimBrasilApiDTO> getCotacoes() {
        return cotacoes;
    }

    public void setCotacoes(List<BoletimBrasilApiDTO> cotacoes) {
        this.cotacoes = cotacoes;
    }
}
