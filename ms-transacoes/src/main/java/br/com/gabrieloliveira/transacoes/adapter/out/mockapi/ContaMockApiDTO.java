package br.com.gabrieloliveira.transacoes.adapter.out.mockapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

// o formato do json que a api mock devolve em GET /accounts, por exemplo:
// { "userId": 1, "saldo": 5000, "limiteDiario": 2000, "limitePorTransacao": 1000 }
// os nomes dos atributos precisam ser iguais aos nomes dos campos do json, porque e assim que o jackson
// sabe onde colocar cada valor
// essa classe so existe dentro do adaptador: o resto do sistema usa a SaldoConta do dominio
// @JsonIgnoreProperties(ignoreUnknown = true): se a api mock mandar algum campo a mais (ex: "id"),
// o jackson ignora em vez de dar erro
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContaMockApiDTO {

    private Long userId;
    // BigDecimal em vez de double: com dinheiro, double da erro de arredondamento (0.1 + 0.2 = 0.30000000000000004)
    private BigDecimal saldo;
    private BigDecimal limiteDiario;
    private BigDecimal limitePorTransacao;

    public ContaMockApiDTO() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public BigDecimal getLimiteDiario() {
        return limiteDiario;
    }

    public void setLimiteDiario(BigDecimal limiteDiario) {
        this.limiteDiario = limiteDiario;
    }

    public BigDecimal getLimitePorTransacao() {
        return limitePorTransacao;
    }

    public void setLimitePorTransacao(BigDecimal limitePorTransacao) {
        this.limitePorTransacao = limitePorTransacao;
    }
}
