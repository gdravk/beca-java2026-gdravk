package br.com.gabrieloliveira.usuarios.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// o resumo que o POST /users/import devolve
// totalLinhas = linhas de usuario lidas (sem contar o cabecalho e as linhas em branco)
// importados + rejeitados = totalLinhas
// falhas = cada linha rejeitada, com o numero da linha no arquivo e o motivo
@JsonPropertyOrder({"totalLinhas", "importados", "rejeitados", "falhas"})
@Schema(description = "resumo da importacao do csv")
public class ImportacaoResultadoDTO {

    @Schema(description = "linhas de usuario lidas (sem o cabecalho e sem linhas em branco)", example = "20")
    private int totalLinhas;
    @Schema(description = "usuarios cadastrados", example = "18")
    private int importados;
    @Schema(description = "linhas que nao entraram", example = "2")
    private int rejeitados;
    @Schema(description = "cada linha rejeitada, com o motivo")
    private List<FalhaImportacaoDTO> falhas;

    public ImportacaoResultadoDTO() {
    }

    public ImportacaoResultadoDTO(int totalLinhas, int importados, int rejeitados, List<FalhaImportacaoDTO> falhas) {
        this.totalLinhas = totalLinhas;
        this.importados = importados;
        this.rejeitados = rejeitados;
        this.falhas = falhas;
    }

    public int getTotalLinhas() {
        return totalLinhas;
    }

    public void setTotalLinhas(int totalLinhas) {
        this.totalLinhas = totalLinhas;
    }

    public int getImportados() {
        return importados;
    }

    public void setImportados(int importados) {
        this.importados = importados;
    }

    public int getRejeitados() {
        return rejeitados;
    }

    public void setRejeitados(int rejeitados) {
        this.rejeitados = rejeitados;
    }

    public List<FalhaImportacaoDTO> getFalhas() {
        return falhas;
    }

    public void setFalhas(List<FalhaImportacaoDTO> falhas) {
        this.falhas = falhas;
    }
}
