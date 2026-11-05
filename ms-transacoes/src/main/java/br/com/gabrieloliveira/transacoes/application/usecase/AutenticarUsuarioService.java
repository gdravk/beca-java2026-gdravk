package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.in.AutenticarUsuarioUseCase;
import br.com.gabrieloliveira.transacoes.application.port.out.AutenticacaoPort;
import br.com.gabrieloliveira.transacoes.domain.exception.CredenciaisInvalidasException;

import java.util.Optional;

// autenticacao do ms2: confere email e senha pela AutenticacaoPort (que pergunta ao ms1)
// e chamado pelo spring security em toda requisicao a uma rota protegida
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

    private final AutenticacaoPort autenticacaoPort;

    public AutenticarUsuarioService(AutenticacaoPort autenticacaoPort) {
        this.autenticacaoPort = autenticacaoPort;
    }

    @Override
    public Long autenticar(String email, String senha) {
        // sem email ou senha nem vale a pena chamar o ms1
        if (email == null || email.isBlank() || senha == null || senha.isEmpty()) {
            throw new CredenciaisInvalidasException();
        }

        Optional<Long> usuarioId = autenticacaoPort.verificar(email, senha);
        if (usuarioId.isEmpty()) {
            throw new CredenciaisInvalidasException();
        }
        return usuarioId.get();
    }
}
