package br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca;

import br.com.gabrieloliveira.transacoes.application.port.in.AutenticarUsuarioUseCase;
import br.com.gabrieloliveira.transacoes.domain.exception.CredenciaisInvalidasException;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

// a "ponte" entre o spring security e o caso de uso de autenticar (que pergunta ao ms1)
//
// a cada requisicao para uma rota protegida:
// 1. o filtro da autenticacao basica le o cabecalho "Authorization: Basic ..." (email:senha em base64)
// 2. ele chama o authenticate abaixo, que chama o AutenticarUsuarioUseCase
// 3. login certo: guarda o UsuarioAutenticado (com o id do usuario) e a requisicao segue para o controller
// 4. login errado: BadCredentialsException, que vira 401
// 5. ms1 fora do ar: AuthenticationServiceException, que vira 503 (ver GlobalExceptionHandler)
//
// nao tem @Component de proposito (ajuste da revisao do professor): quem cria esta classe com new e o
// SecurityConfig. como bean, o spring registrava o provider duas vezes e, com a senha errada,
// perguntava ao ms1 duas vezes na mesma requisicao
public class Ms1AuthenticationProvider implements AuthenticationProvider {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    public Ms1AuthenticationProvider(AutenticarUsuarioUseCase autenticarUsuarioUseCase) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
    }

    @Override
    public Authentication authenticate(Authentication autenticacao) {
        String email = autenticacao.getName();
        String senha = null;
        if (autenticacao.getCredentials() != null) {
            senha = autenticacao.getCredentials().toString();
        }

        try {
            Long usuarioId = autenticarUsuarioUseCase.autenticar(email, senha);
            UsuarioAutenticado logado = new UsuarioAutenticado(usuarioId, email);
            // a senha vai como null: depois de conferida, ela nao fica guardada
            return UsernamePasswordAuthenticationToken.authenticated(logado, null, List.of());
        } catch (CredenciaisInvalidasException e) {
            throw new BadCredentialsException(e.getMessage());
        } catch (ServicoExternoIndisponivelException e) {
            // e diferente de senha errada: aqui o problema e nosso (o ms1 esta fora do ar), e nao do usuario
            throw new AuthenticationServiceException("servico de usuarios indisponivel", e);
        }
    }

    @Override
    public boolean supports(Class<?> tipoDeAutenticacao) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(tipoDeAutenticacao);
    }
}
