package br.com.gabrieloliveira.usuarios.adapter.in.web.dto;

import br.com.gabrieloliveira.usuarios.adapter.in.web.validacao.SenhaCabeNoBcrypt;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// o que o cliente manda no corpo (json) do PUT /users/{id}
// e quase igual ao UsuarioRequestDTO (do cadastro), com uma diferenca importante: aqui a senha e OPCIONAL
// - se o json trouxer a senha, ela e trocada (e criptografada de novo)
// - se nao trouxer, a senha atual continua a mesma
// assim da pra mudar so o nome, por exemplo, sem precisar mandar a senha junto
// foi um pedido do professor na revisao de 03/11
public class UsuarioUpdateRequestDTO {

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

    // repara que aqui NAO tem @NotBlank: por isso a senha pode faltar no json
    // o @Size so e checado quando a senha vem preenchida (para valor null ele nao faz nada)
    // entao: senha ausente = ok (mantem a atual) / senha "123" = erro 400 / senha "nova123" = troca
    @Size(min = 6, message = "a senha deve ter pelo menos 6 caracteres")
    @SenhaCabeNoBcrypt
    private String senha;

    // construtor vazio pro jackson conseguir criar o objeto a partir do json
    public UsuarioUpdateRequestDTO() {
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

    // mesmo motivo do UsuarioRequestDTO: tira os espacos antes do @Email validar
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
