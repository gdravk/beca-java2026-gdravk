package br.com.gabrieloliveira.usuarios.adapter.in.web.dto;

import br.com.gabrieloliveira.usuarios.adapter.in.web.validacao.SenhaCabeNoBcrypt;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// corpo do POST /auth/verificar: o email e a senha que o ms2 recebeu do usuario
// os limites de tamanho sao os mesmos do cadastro: um texto gigante nem chega a ser procurado no banco
// nao usamos @Email aqui de proposito: email em formato errado so nao bate com ninguem e vira "login invalido"
public class CredenciaisRequestDTO {

    @NotBlank(message = "o email e obrigatorio")
    @Size(max = 150, message = "o email pode ter no maximo 150 caracteres")
    private String email;

    @NotBlank(message = "a senha e obrigatoria")
    @SenhaCabeNoBcrypt
    private String senha;

    public CredenciaisRequestDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
