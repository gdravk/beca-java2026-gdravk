package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// resposta do GET /transactions/analysis/category
// inicio e fim mostram o periodo que foi usado de verdade (quando o cliente nao manda, e o mes atual),
// assim quem le a resposta sabe exatamente a que periodo os totais se referem
@JsonPropertyOrder({"usuarioId", "inicio", "fim", "quantidadeDespesas", "totalGeral", "categorias"})
public class DespesasPorCategoriaResponseDTO {

    private Long usuarioId;
    private LocalDate inicio;
    private LocalDate fim;
    private int quantidadeDespesas;
    private BigDecimal totalGeral;
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
