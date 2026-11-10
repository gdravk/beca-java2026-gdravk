package br.com.gabrieloliveira.usuarios.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// esta classe vira a tabela "usuarios" no banco: cada atributo e uma coluna, e cada objeto e uma linha
// ela so existe aqui no adaptador; o resto do sistema usa o Usuario do dominio, e o
// UsuarioPersistenceMapper faz a traducao entre os dois
@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    // chave primaria; quem gera o numero e o proprio postgres (1, 2, 3...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    // unique = true: o banco nao deixa existir dois usuarios com o mesmo email
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // cpf guardado so com os 11 numeros, sem ponto e sem traco
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    // aqui fica o hash da senha, nunca a senha pura
    @Column(nullable = false)
    private String senha;

    // updatable = false: depois de gravada, essa data nao muda mais
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    // o jpa precisa de um construtor vazio para conseguir montar o objeto quando le do banco
    public UsuarioEntity() {
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
