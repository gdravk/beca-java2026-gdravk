package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// resposta do GET /transactions/analysis/category
// o inicio e o fim mostram o periodo usado de verdade (se o cliente nao mandar, e o mes atual),
// assim quem le a resposta sabe exatamente a que periodo os totais se referem
@JsonPropertyOrder({"usuarioId", "inicio", "fim", "quantidadeDespesas", "totalGeral", "categorias"})
@Schema(description = "despesas do periodo agrupadas por categoria")
public class DespesasPorCategoriaResponseDTO {

    @Schema(description = "dono das despesas", example = "1")
    private Long usuarioId;
    @Schema(description = "primeiro dia do periodo usado", example = "2026-11-01")
    private LocalDate inicio;
    @Schema(description = "ultimo dia do periodo usado", example = "2026-11-30")
    private LocalDate fim;
    @Schema(description = "quantas despesas entraram na conta", example = "5")
    private int quantidadeDespesas;
    @Schema(description = "soma de todas as despesas do periodo, em reais", example = "250.00")
    private BigDecimal totalGeral;
    @Schema(description = "uma linha por categoria, da que mais gastou para a que menos gastou")
    private List<DespesaPorCategoriaDTO> categorias;

    public DespesasPorCategoriaResponseDTO() {
    }

    public DespesasPorCategoriaResponseDTO(Long usuarioId, LocalDate inicio, LocalDate fim, int quantidadeDespesas,
                                           BigDecimal totalGeral, List<DespesaPorCategoriaDTO> categorias) {
        this.usuarioId = usuarioId;
        this.inicio = inicio;
        this.fim = fim;
        this.quantidadeDespesas = quantidadeDespesas;
        this.totalGeral = totalGeral;
        this.categorias = categorias;
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

    public List<DespesaPorCategoriaDTO> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<DespesaPorCategoriaDTO> categorias) {
        this.categorias = categorias;
    }
}
