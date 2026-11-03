package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// resposta do GET /accounts/{usuarioId}/balance
// consultadoEm mostra o momento da consulta, ja que o saldo vem "ao vivo" da api mock e pode mudar
public class SaldoContaResponseDTO {

    private Long usuarioId;
    private BigDecimal saldo;
    private BigDecimal limiteDiario;
    private BigDecimal limitePorTransacao;
    private LocalDateTime consultadoEm;

    public SaldoContaResponseDTO() {
    }

    public SaldoContaResponseDTO(Long usuarioId, BigDecimal saldo, BigDecimal limiteDiario,
                                 BigDecimal limitePorTransacao, LocalDateTime consultadoEm) {
        this.usuarioId = usuarioId;
        this.saldo = saldo;
        this.limiteDiario = limiteDiario;
        this.limitePorTransacao = limitePorTransacao;
        this.consultadoEm = consultadoEm;
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

    public LocalDateTime getConsultadoEm() {
        return consultadoEm;
    }

    public void setConsultadoEm(LocalDateTime consultadoEm) {
        this.consultadoEm = consultadoEm;
    }
}
