package br.com.gabrieloliveira.usuarios.domain.model;

import java.time.LocalDateTime;

// o usuario do jeito que o negocio enxerga: java puro, sem nenhuma anotacao do spring ou do jpa
// quem sabe transformar isso numa linha do banco e o adaptador de persistencia (UsuarioEntity)
// assim, se um dia trocar o postgres por outro banco, essa classe nao muda nada
public class Usuario {

    private Long id;
    private String nome;
    private String email;
    private String cpf;
    // aqui sempre fica o hash da senha, nunca a senha pura (o caso de uso cuida disso antes de salvar)
    private String senha;
    private LocalDateTime dataCriacao;

    public Usuario() {
    }

    public Usuario(Long id, String nome, String email, String cpf, String senha, LocalDateTime dataCriacao) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.senha = senha;
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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
