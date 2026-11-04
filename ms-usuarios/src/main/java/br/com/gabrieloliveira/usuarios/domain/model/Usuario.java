package br.com.gabrieloliveira.usuarios.domain.model;

import java.time.LocalDateTime;
import java.util.Locale;

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
        this.email = normalizarEmail(email);
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
        this.email = normalizarEmail(email);
    }

    // deixa o email sempre no mesmo formato: sem espacos nas pontas e todo em minusculo
    // sem isso, "Ana@Email.com" e "ana@email.com" seriam vistos como dois emails diferentes,
    // e a mesma pessoa conseguiria se cadastrar duas vezes
    // o metodo fica aqui no dominio (e e chamado pelo construtor e pelo setEmail) pra valer em qualquer caminho:
    // cadastro, update e, mais pra frente, a importacao por csv; assim ninguem precisa lembrar de chamar
    // e "static" porque nao depende de nenhum dado do objeto, so do texto que recebe
    private static String normalizarEmail(String email) {
        // sem esse if, um email null daria NullPointerException no trim()
        // quem barra o email vazio e o @NotBlank do dto, nao essa classe
        if (email == null) {
            return null;
        }
        // trim() tira os espacos do comeco e do fim
        // Locale.ROOT faz o toLowerCase funcionar igual em qualquer computador, independente do idioma
        // (em turco, por exemplo, o "I" maiusculo viraria um "i" sem pingo, e o email ficaria errado)
        return email.trim().toLowerCase(Locale.ROOT);
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
