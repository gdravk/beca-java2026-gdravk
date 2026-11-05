package br.com.gabrieloliveira.usuarios.adapter.in.web.seguranca;

// quem esta logado na requisicao atual
// depois que o login da certo, o spring security guarda esse objeto, e os controllers podem pedir ele
// com @AuthenticationPrincipal (ex: pra conferir se o usuario esta mexendo nos proprios dados)
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

    // o spring usa o toString do "principal" como nome do usuario em alguns lugares (ex: logs dele)
    // por isso devolvemos o email, e nunca algo com senha
    @Override
    public String toString() {
        return email;
    }
}
