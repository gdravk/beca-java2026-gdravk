package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// resposta do GET /accounts/{usuarioId}/balance
// consultadoEm mostra o momento da consulta, ja que o saldo vem "ao vivo" da api mock e pode mudar
@Schema(description = "saldo e limites da conta, lidos na hora da API Mock")
public class SaldoContaResponseDTO {

    @Schema(description = "dono da conta", example = "1")
    private Long usuarioId;
    @Schema(description = "saldo atual em reais", example = "2500.00")
    private BigDecimal saldo;
    @Schema(description = "quanto pode sair da conta por dia, somando as saidas aprovadas", example = "1000.00")
    private BigDecimal limiteDiario;
    @Schema(description = "valor maximo de uma unica saida", example = "500.00")
    private BigDecimal limitePorTransacao;
    @Schema(description = "momento da consulta (horario de Brasilia)", example = "2026-11-05T14:30:00")
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
