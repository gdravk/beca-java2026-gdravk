package br.com.gabrieloliveira.usuarios.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;

// o que a listagem (GET /users) mostra de cada usuario
// e uma versao "resumida" do UsuarioResponseDTO: sem o cpf, porque a listagem mostra todo mundo
// pra qualquer usuario logado, e cpf e dado pessoal (ajuste da revisao do professor)
// o cpf completo so aparece pro proprio usuario, no GET /users/{id} e nas respostas de cadastro e update
@JsonPropertyOrder({"id", "nome", "email", "dataCriacao"})
public class UsuarioResumoResponseDTO {

    private Long id;
    private String nome;
    private String email;
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
