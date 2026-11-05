package br.com.gabrieloliveira.transacoes.adapter.out.ms1;

// json enviado pro ms1 no POST /auth/verificar: { "email": "...", "senha": "..." }
public class CredenciaisMs1RequestDTO {

    private String email;
    private String senha;

    public CredenciaisMs1RequestDTO() {
    }

    public CredenciaisMs1RequestDTO(String email, String senha) {
        this.email = email;
        this.senha = senha;
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
