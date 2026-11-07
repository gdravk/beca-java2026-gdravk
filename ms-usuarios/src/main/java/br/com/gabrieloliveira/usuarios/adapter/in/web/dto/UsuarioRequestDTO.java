package br.com.gabrieloliveira.usuarios.adapter.in.web.dto;

import br.com.gabrieloliveira.usuarios.adapter.in.web.validacao.SenhaCabeNoBcrypt;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// o que o cliente manda no corpo (json) do POST e do PUT
// as anotacoes de validacao barram dados ruins antes de chegar nos casos de uso
// se algo falhar aqui, a api responde 400 com a lista dos campos errados
public class UsuarioRequestDTO {

    @NotBlank(message = "o nome e obrigatorio")
    @Size(max = 100, message = "o nome pode ter no maximo 100 caracteres")
    private String nome;

    @NotBlank(message = "o email e obrigatorio")
    @Email(message = "o email esta em formato invalido")
    @Size(max = 150, message = "o email pode ter no maximo 150 caracteres")
    private String email;

    // \\d{11} quer dizer: exatamente 11 digitos, nada de ponto ou traco
    @NotBlank(message = "o cpf e obrigatorio")
    @Pattern(regexp = "\\d{11}", message = "o cpf deve ter exatamente 11 numeros, sem ponto e sem traco")
    private String cpf;

    @NotBlank(message = "a senha e obrigatoria")
    @Size(min = 6, message = "a senha deve ter pelo menos 6 caracteres")
    @SenhaCabeNoBcrypt
    private String senha;

    // construtor vazio pro jackson conseguir criar o objeto a partir do json
    public UsuarioRequestDTO() {
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

    // o jackson chama esse setter quando le o json, ANTES das validacoes (@Email, @NotBlank) rodarem
    // por isso o trim e feito aqui tambem: sem ele, " ana@email.com " (com espaco, comum ao copiar e colar)
    // seria barrado pelo @Email com erro 400, e nem chegaria na normalizacao do dominio
    // o resto da normalizacao (deixar em minusculo) fica no Usuario do dominio
    public void setEmail(String email) {
        if (email == null) {
            this.email = null;
        } else {
            this.email = email.trim();
        }
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
}
