package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

// formato padrao de erro da api, o mesmo usado no ms1
// campos so vem preenchido nos erros de validacao (400), no formato "campo -> mensagem"
@Schema(description = "formato padrao de todos os erros da api (o mesmo do ms1)")
public class ErroResponseDTO {

    private static final ZoneId FUSO_BRASIL = ZoneId.of("America/Sao_Paulo");

    @Schema(description = "quando o erro aconteceu (horario de Brasilia)", example = "2026-11-05T14:30:00")
    private LocalDateTime dataHora;
    @Schema(description = "codigo http", example = "400")
    private int status;
    @Schema(description = "nome do codigo http", example = "Bad Request")
    private String erro;
    @Schema(description = "o que deu errado, em portugues", example = "existem campos invalidos na requisicao")
    private String mensagem;
    @Schema(description = "rota chamada", example = "/transactions")
    private String caminho;
    @Schema(description = "so nos erros de validacao (400): cada campo errado e a mensagem dele; nos outros erros nao aparece",
            example = "{\"valor\": \"o valor deve ser maior que zero\"}")
    // NON_NULL: quando nao ha campos (todo erro que nao e de validacao), o "campos" nem aparece no json
    // sem isso a resposta sairia com "campos": null
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> campos;

    public ErroResponseDTO() {
    }

    public ErroResponseDTO(int status, String erro, String mensagem, String caminho, Map<String, String> campos) {
        // horario de brasilia, igual ao resto da api (em container utc, o now() sozinho daria o horario de londres)
        this.dataHora = LocalDateTime.now(FUSO_BRASIL);
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.caminho = caminho;
        this.campos = campos;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getErro() {
        return erro;
    }

    public void setErro(String erro) {
        this.erro = erro;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getCaminho() {
        return caminho;
    }

    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }

    public Map<String, String> getCampos() {
        return campos;
    }

    public void setCampos(Map<String, String> campos) {
        this.campos = campos;
    }
}
