package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// resposta do GET /transactions/analysis/monthly
// "meses" so traz os meses que tiveram despesa, do mais antigo para o mais novo
@JsonPropertyOrder({"usuarioId", "inicio", "fim", "quantidadeDespesas", "totalGeral", "meses"})
@Schema(description = "despesas do periodo agrupadas por mes")
public class DespesasPorMesResponseDTO {

    @Schema(description = "dono das despesas", example = "1")
    private Long usuarioId;
    @Schema(description = "primeiro dia do periodo usado", example = "2026-01-01")
    private LocalDate inicio;
    @Schema(description = "ultimo dia do periodo usado", example = "2026-12-31")
    private LocalDate fim;
    @Schema(description = "quantas despesas entraram na conta", example = "12")
    private int quantidadeDespesas;
    @Schema(description = "soma de todas as despesas do periodo, em reais", example = "1250.00")
    private BigDecimal totalGeral;
    @Schema(description = "so os meses com despesa, do mais antigo para o mais novo")
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
