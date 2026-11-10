package br.com.gabrieloliveira.transacoes.adapter.out.brasilapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

// um "boletim" de cotacao do banco central; durante o dia saem varios (abertura, intermediarios e o fechamento ptax)
// exemplo do json da brasilapi:
// { "cotacao_compra": 5.4772, "cotacao_venda": 5.4778, "data_hora_cotacao": "2026-11-04 13:05:19", "tipo_boletim": "FECHAMENTO PTAX" }
// a brasilapi usa nomes com underline (snake_case) e no java usamos camelCase; o @JsonProperty("nome_no_json")
// faz a ponte, dizendo ao jackson de qual campo do json vem cada atributo
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoletimBrasilApiDTO {

    @JsonProperty("cotacao_compra")
    private BigDecimal cotacaoCompra;

    // cotacao de venda = quanto custa COMPRAR 1 unidade da moeda estrangeira, em reais (e a que usamos)
    @JsonProperty("cotacao_venda")
    private BigDecimal cotacaoVenda;

    @JsonProperty("data_hora_cotacao")
    private String dataHoraCotacao;

    @JsonProperty("tipo_boletim")
    private String tipoBoletim;

    public BoletimBrasilApiDTO() {
    }

    public BigDecimal getCotacaoCompra() {
        return cotacaoCompra;
    }

    public void setCotacaoCompra(BigDecimal cotacaoCompra) {
        this.cotacaoCompra = cotacaoCompra;
    }

    public BigDecimal getCotacaoVenda() {
        return cotacaoVenda;
    }

    public void setCotacaoVenda(BigDecimal cotacaoVenda) {
        this.cotacaoVenda = cotacaoVenda;
    }

    public String getDataHoraCotacao() {
        return dataHoraCotacao;
    }

    public void setDataHoraCotacao(String dataHoraCotacao) {
        this.dataHoraCotacao = dataHoraCotacao;
    }

    public String getTipoBoletim() {
        return tipoBoletim;
    }

    public void setTipoBoletim(String tipoBoletim) {
        this.tipoBoletim = tipoBoletim;
    }
}
