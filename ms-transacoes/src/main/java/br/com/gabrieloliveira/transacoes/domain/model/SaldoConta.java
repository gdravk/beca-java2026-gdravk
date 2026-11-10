package br.com.gabrieloliveira.transacoes.domain.model;

import java.math.BigDecimal;

// saldo e limites de um usuario, do jeito que chegam do servico externo de contas (a api mock)
// esses dados nao ficam guardados no banco do ms2
public class SaldoConta {

    private Long usuarioId;
    private BigDecimal saldo;
    private BigDecimal limiteDiario;
    private BigDecimal limitePorTransacao;

    public SaldoConta() {
    }

    public SaldoConta(Long usuarioId, BigDecimal saldo, BigDecimal limiteDiario, BigDecimal limitePorTransacao) {
        this.usuarioId = usuarioId;
        this.saldo = saldo;
        this.limiteDiario = limiteDiario;
        this.limitePorTransacao = limitePorTransacao;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
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
