package br.com.gabrieloliveira.usuarios.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

// resposta do POST /auth/verificar quando o email e a senha estao certos
// o mais importante e o usuarioId: e com ele que o ms2 sabe de quem sao as transacoes
// a senha (nem o hash dela) nunca vem aqui
@JsonPropertyOrder({"usuarioId", "nome", "email"})
public class CredenciaisVerificadasResponseDTO {

    private Long usuarioId;
    private String nome;
    private String email;

    public CredenciaisVerificadasResponseDTO() {
    }

    public CredenciaisVerificadasResponseDTO(Long usuarioId, String nome, String email) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.email = email;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
