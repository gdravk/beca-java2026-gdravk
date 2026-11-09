package br.com.gabrieloliveira.usuarios.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

// o que a api devolve pro cliente
// repara que aqui nao tem o campo senha: assim nao tem como ela vazar numa resposta
@Schema(description = "dados completos de um usuario (a senha nunca aparece)")
public class UsuarioResponseDTO {

    @Schema(description = "id do usuario", example = "1")
    private Long id;
    @Schema(description = "nome completo", example = "Ana Souza")
    private String nome;
    @Schema(description = "email, sempre em minusculo", example = "ana.souza@email.com")
    private String email;
    @Schema(description = "cpf com 11 numeros", example = "12345678901")
    private String cpf;
    @Schema(description = "data e hora do cadastro (horario de Brasilia)", example = "2026-11-05T14:30:00")
    private LocalDateTime dataCriacao;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(Long id, String nome, String email, String cpf, LocalDateTime dataCriacao) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.dataCriacao = dataCriacao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
