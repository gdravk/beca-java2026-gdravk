package br.com.gabrieloliveira.usuarios.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

// uma linha que nao entrou na importacao, do jeito que aparece no json de resposta:
// { "linha": 12, "motivo": "ja existe um usuario cadastrado com esse email" }
// o @JsonPropertyOrder garante essa ordem no json (sem ele o jackson colocaria os campos em ordem alfabetica)
@JsonPropertyOrder({"linha", "motivo"})
@Schema(description = "uma linha do csv que nao entrou")
public class FalhaImportacaoDTO {

    @Schema(description = "numero da linha no arquivo (o cabecalho e a linha 1)", example = "12")
    private int linha;
    @Schema(description = "por que a linha foi rejeitada", example = "ja existe um usuario cadastrado com esse email")
    private String motivo;

    public FalhaImportacaoDTO() {
    }

    public FalhaImportacaoDTO(int linha, String motivo) {
        this.linha = linha;
        this.motivo = motivo;
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
