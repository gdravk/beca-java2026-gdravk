package br.com.gabrieloliveira.usuarios.adapter.in.web.seguranca;

import br.com.gabrieloliveira.usuarios.application.port.in.VerificarCredenciaisUseCase;
import br.com.gabrieloliveira.usuarios.domain.exception.CredenciaisInvalidasException;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

// a "ponte" entre o spring security e o nosso caso de uso de verificar credenciais
//
// como funciona a autenticacao basica:
// - o cliente manda o cabecalho "Authorization: Basic xxx", onde xxx e "email:senha" em base64
// - o filtro do spring security le esse cabecalho e chama o metodo authenticate abaixo
// - aqui chamamos o VerificarCredenciaisUseCase (o mesmo do POST /auth/verificar),
//   assim a regra de login fica num lugar so
// - deu certo: devolvemos um "Authentication" com o UsuarioAutenticado dentro
// - deu errado: lancamos BadCredentialsException, e o spring responde 401
//
// nao tem @Component de proposito (ajuste da revisao do professor): quem cria essa classe com new
// e o SecurityConfig; como bean, o spring registrava o provider duas vezes e, com a senha errada,
// conferia o bcrypt duas vezes na mesma requisicao
public class CredenciaisAuthenticationProvider implements AuthenticationProvider {

    private final VerificarCredenciaisUseCase verificarCredenciaisUseCase;

    public CredenciaisAuthenticationProvider(VerificarCredenciaisUseCase verificarCredenciaisUseCase) {
        this.verificarCredenciaisUseCase = verificarCredenciaisUseCase;
    }

    @Override
    public Authentication authenticate(Authentication autenticacao) {
        // getName() e o usuario digitado (pra nos, o email); getCredentials() e a senha
        String email = autenticacao.getName();
        String senha = null;
        if (autenticacao.getCredentials() != null) {
            senha = autenticacao.getCredentials().toString();
        }

        try {
            Usuario usuario = verificarCredenciaisUseCase.verificar(email, senha);
            UsuarioAutenticado logado = new UsuarioAutenticado(usuario.getId(), usuario.getEmail());
            // authenticated(...) cria o "cracha" de usuario logado
            // a senha vai como null: depois de conferida, ela nao fica guardada em lugar nenhum
            // List.of() = sem perfis (admin, etc.), porque o pedido do PO nao tem perfis de acesso
            return UsernamePasswordAuthenticationToken.authenticated(logado, null, List.of());
        } catch (CredenciaisInvalidasException e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    // avisa o spring que esse provider sabe tratar login de usuario e senha (que e o que a autenticacao basica gera)
    @Override
    public boolean supports(Class<?> tipoDeAutenticacao) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(tipoDeAutenticacao);
    }
}
