package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// resposta do GET /transactions/analysis/daily
// "dias" so traz os dias que tiveram despesa, do mais antigo pro mais novo
@JsonPropertyOrder({"usuarioId", "inicio", "fim", "quantidadeDespesas", "totalGeral", "dias"})
public class DespesasPorDiaResponseDTO {

    private Long usuarioId;
    private LocalDate inicio;
    private LocalDate fim;
    private int quantidadeDespesas;
    private BigDecimal totalGeral;
    private List<DespesaPorDiaDTO> dias;

    public DespesasPorDiaResponseDTO() {
    }

    public DespesasPorDiaResponseDTO(Long usuarioId, LocalDate inicio, LocalDate fim, int quantidadeDespesas,
                                     BigDecimal totalGeral, List<DespesaPorDiaDTO> dias) {
        this.usuarioId = usuarioId;
        this.inicio = inicio;
        this.fim = fim;
        this.quantidadeDespesas = quantidadeDespesas;
        this.totalGeral = totalGeral;
        this.dias = dias;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public void setInicio(LocalDate inicio) {
        this.inicio = inicio;
    }

    public LocalDate getFim() {
        return fim;
    }

    public void setFim(LocalDate fim) {
        this.fim = fim;
    }

    public int getQuantidadeDespesas() {
        return quantidadeDespesas;
    }

    public void setQuantidadeDespesas(int quantidadeDespesas) {
        this.quantidadeDespesas = quantidadeDespesas;
    }

    public BigDecimal getTotalGeral() {
        return totalGeral;
    }

    public void setTotalGeral(BigDecimal totalGeral) {
        this.totalGeral = totalGeral;
    }

    public List<DespesaPorDiaDTO> getDias() {
        return dias;
    }

    public void setDias(List<DespesaPorDiaDTO> dias) {
        this.dias = dias;
    }
}
