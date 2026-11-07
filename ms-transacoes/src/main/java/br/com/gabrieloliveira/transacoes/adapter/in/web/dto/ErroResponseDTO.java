package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

// formato padrao de erro da api, o mesmo usado no ms1
// campos so vem preenchido nos erros de validacao (400), no formato "campo -> mensagem"
public class ErroResponseDTO {

    private static final ZoneId FUSO_BRASIL = ZoneId.of("America/Sao_Paulo");

    private LocalDateTime dataHora;
    private int status;
    private String erro;
    private String mensagem;
    private String caminho;
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
