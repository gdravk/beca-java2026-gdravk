package br.com.gabrieloliveira.usuarios.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

// resposta do POST /auth/verificar quando o email e a senha estao certos
// o mais importante aqui e o usuarioId: e com ele que o ms2 sabe de quem sao as transacoes
// a senha nunca vem aqui, nem o hash dela
@JsonPropertyOrder({"usuarioId", "nome", "email"})
@Schema(description = "usuario dono do email e da senha conferidos")
public class CredenciaisVerificadasResponseDTO {

    @Schema(description = "id do usuario; o ms2 usa esse id como dono das transacoes", example = "1")
    private Long usuarioId;
    @Schema(description = "nome completo", example = "Ana Souza")
    private String nome;
    @Schema(description = "email, sempre em minusculo", example = "ana.souza@email.com")
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
