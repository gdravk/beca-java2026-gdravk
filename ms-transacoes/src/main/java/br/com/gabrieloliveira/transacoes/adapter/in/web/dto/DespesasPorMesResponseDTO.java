package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// resposta do GET /transactions/analysis/monthly
// "meses" so traz os meses que tiveram despesa, do mais antigo pro mais novo
@JsonPropertyOrder({"usuarioId", "inicio", "fim", "quantidadeDespesas", "totalGeral", "meses"})
public class DespesasPorMesResponseDTO {

    private Long usuarioId;
    private LocalDate inicio;
    private LocalDate fim;
    private int quantidadeDespesas;
    private BigDecimal totalGeral;
    private List<DespesaPorMesDTO> meses;

    public DespesasPorMesResponseDTO() {
    }

    public DespesasPorMesResponseDTO(Long usuarioId, LocalDate inicio, LocalDate fim, int quantidadeDespesas,
                                     BigDecimal totalGeral, List<DespesaPorMesDTO> meses) {
        this.usuarioId = usuarioId;
        this.inicio = inicio;
        this.fim = fim;
        this.quantidadeDespesas = quantidadeDespesas;
        this.totalGeral = totalGeral;
        this.meses = meses;
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

    public List<DespesaPorMesDTO> getMeses() {
        return meses;
    }

    public void setMeses(List<DespesaPorMesDTO> meses) {
        this.meses = meses;
    }
}
