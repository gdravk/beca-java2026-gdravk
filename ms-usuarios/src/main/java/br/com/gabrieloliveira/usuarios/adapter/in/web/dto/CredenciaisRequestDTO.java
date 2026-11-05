package br.com.gabrieloliveira.usuarios.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

// corpo do POST /auth/verificar: o email e a senha que o ms2 recebeu do usuario
public class CredenciaisRequestDTO {

    @NotBlank(message = "o email e obrigatorio")
    private String email;

    @NotBlank(message = "a senha e obrigatoria")
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
