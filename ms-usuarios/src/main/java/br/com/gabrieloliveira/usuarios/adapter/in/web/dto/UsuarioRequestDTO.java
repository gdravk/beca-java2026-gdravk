package br.com.gabrieloliveira.usuarios.adapter.in.web.dto;

import br.com.gabrieloliveira.usuarios.adapter.in.web.validacao.SenhaCabeNoBcrypt;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// o que o cliente manda no corpo (json) do POST /users
// as anotacoes de validacao barram os dados ruins antes de chegarem nos casos de uso; se algo falhar,
// a api responde 400 com a lista dos campos errados
@Schema(description = "dados para cadastrar um usuario")
public class UsuarioRequestDTO {

    @NotBlank(message = "o nome e obrigatorio")
    @Size(max = 100, message = "o nome pode ter no maximo 100 caracteres")
    @Schema(description = "nome completo", example = "Ana Souza")
    private String nome;

    @NotBlank(message = "o email e obrigatorio")
    @Email(message = "o email esta em formato invalido")
    @Size(max = 150, message = "o email pode ter no maximo 150 caracteres")
    @Schema(description = "email (vira o login); espacos nas pontas sao removidos e tudo vai para minusculo", example = "ana.souza@email.com")
    private String email;

    // \\d{11} quer dizer: exatamente 11 digitos, sem ponto e sem traco
    @NotBlank(message = "o cpf e obrigatorio")
    @Pattern(regexp = "\\d{11}", message = "o cpf deve ter exatamente 11 numeros, sem ponto e sem traco")
    @Schema(description = "exatamente 11 numeros, sem ponto e sem traco", example = "12345678901")
    private String cpf;

    @NotBlank(message = "a senha e obrigatoria")
    @Size(min = 6, message = "a senha deve ter pelo menos 6 caracteres")
    @SenhaCabeNoBcrypt
    @Schema(description = "de 6 caracteres ate 72 bytes (letra com acento conta como 2); gravada criptografada", example = "Senha@123")
    private String senha;

    // construtor vazio para o jackson conseguir criar o objeto a partir do json
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

    // o jackson chama este setter quando le o json, ANTES das validacoes (@Email, @NotBlank) rodarem
    // por isso o trim tambem e feito aqui: sem ele, " ana@email.com " (com espaco, comum ao copiar e colar)
    // seria barrado pelo @Email com erro 400 e nem chegaria na normalizacao do dominio
    // o resto da normalizacao (deixar tudo em minusculo) fica no Usuario do dominio
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
