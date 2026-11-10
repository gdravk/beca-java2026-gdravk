package br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca;

// quem esta logado na requisicao atual (o id veio do ms1, pelo POST /auth/verificar)
// guarda so o id e o email: a senha nunca fica guardada depois do login
public class UsuarioAutenticado {

    private final Long id;
    private final String email;

    public UsuarioAutenticado(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    // o spring usa o toString como nome do usuario em alguns lugares; por isso devolvemos o email, e nunca a senha
    @Override
    public String toString() {
        return email;
    }
}
