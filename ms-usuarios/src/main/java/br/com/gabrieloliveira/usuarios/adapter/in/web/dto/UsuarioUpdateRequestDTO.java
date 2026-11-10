package br.com.gabrieloliveira.usuarios.adapter.in.web.dto;

import br.com.gabrieloliveira.usuarios.adapter.in.web.validacao.SenhaCabeNoBcrypt;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// o que o cliente manda no corpo (json) do PUT /users/{id}
// e quase igual ao dto do cadastro, com uma diferenca importante: aqui a senha e OPCIONAL
// - se o json trouxer a senha, ela e trocada (e criptografada de novo)
// - se nao trouxer, a senha atual continua a mesma
// assim da para mudar so o nome, por exemplo, sem mandar a senha junto (pedido do professor na revisao de 03/11)
@Schema(description = "dados para atualizar a propria conta; a senha e opcional")
public class UsuarioUpdateRequestDTO {

    @NotBlank(message = "o nome e obrigatorio")
    @Size(max = 100, message = "o nome pode ter no maximo 100 caracteres")
    @Schema(description = "nome completo", example = "Ana Souza Lima")
    private String nome;

    @NotBlank(message = "o email e obrigatorio")
    @Email(message = "o email esta em formato invalido")
    @Size(max = 150, message = "o email pode ter no maximo 150 caracteres")
    @Schema(description = "email (vira o login)", example = "ana.souza@email.com")
    private String email;

    // \\d{11} quer dizer: exatamente 11 digitos, sem ponto e sem traco
    @NotBlank(message = "o cpf e obrigatorio")
    @Pattern(regexp = "\\d{11}", message = "o cpf deve ter exatamente 11 numeros, sem ponto e sem traco")
    @Schema(description = "exatamente 11 numeros, sem ponto e sem traco", example = "12345678901")
    private String cpf;

    // repara que aqui NAO tem @NotBlank, por isso a senha pode faltar no json
    // o @Size e o @SenhaCabeNoBcrypt so conferem quando a senha vem preenchida (com null eles nao fazem nada)
    // resumindo: senha ausente = mantem a atual / senha "123" = erro 400 / senha "nova123" = troca
    @Size(min = 6, message = "a senha deve ter pelo menos 6 caracteres")
    @SenhaCabeNoBcrypt
    @Schema(description = "opcional: sem ela, a senha atual e mantida; se vier, de 6 caracteres ate 72 bytes", example = "NovaSenha@456", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String senha;

    // construtor vazio para o jackson conseguir criar o objeto a partir do json
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
    // (deixar em minusculo continua sendo trabalho do Usuario do dominio)
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
