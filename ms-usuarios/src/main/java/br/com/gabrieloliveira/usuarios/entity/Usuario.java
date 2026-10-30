package br.com.gabrieloliveira.usuarios.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// essa classe vira a tabela "usuarios" no banco
// cada atributo aqui embaixo vira uma coluna, e cada objeto Usuario vira uma linha
@Entity
@Table(name = "usuarios")
public class Usuario {

    // chave primaria, quem gera o numero e o proprio postgres (1, 2, 3...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    // unique = true: o banco nao deixa ter dois usuarios com o mesmo email
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // cpf so com os 11 numeros, sem ponto e sem traco
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    // aqui nunca fica a senha "pura", so o hash dela (o service cuida disso)
    @Column(nullable = false)
    private String senha;

    // updatable = false: depois de gravada, essa data nao muda mais
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    // o jpa precisa de um construtor vazio pra conseguir montar o objeto quando le do banco
    public Usuario() {
    }

    // roda sozinho logo antes do primeiro insert, entao ninguem precisa lembrar de preencher a data
    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
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
