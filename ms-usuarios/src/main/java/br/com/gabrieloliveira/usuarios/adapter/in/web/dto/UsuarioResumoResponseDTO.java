package br.com.gabrieloliveira.usuarios.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// o que a listagem (GET /users) mostra de cada usuario: uma versao resumida do UsuarioResponseDTO, sem o cpf
// como a listagem mostra todo mundo para qualquer usuario logado, o cpf (dado pessoal) fica de fora
// (ajuste pedido pelo professor). ele so aparece para o proprio usuario, no GET /users/{id} e nas
// respostas do cadastro e da atualizacao
@JsonPropertyOrder({"id", "nome", "email", "dataCriacao"})
@Schema(description = "usuario na listagem: sem cpf e sem senha")
public class UsuarioResumoResponseDTO {

    @Schema(description = "id do usuario", example = "1")
    private Long id;
    @Schema(description = "nome completo", example = "Ana Souza")
    private String nome;
    @Schema(description = "email, sempre em minusculo", example = "ana.souza@email.com")
    private String email;
    @Schema(description = "data e hora do cadastro (horario de Brasilia)", example = "2026-11-05T14:30:00")
    private LocalDateTime dataCriacao;

    public UsuarioResumoResponseDTO() {
    }

    public UsuarioResumoResponseDTO(Long id, String nome, String email, LocalDateTime dataCriacao) {
        this.id = id;
        this.nome = nome;
        this.email = email;
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

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
