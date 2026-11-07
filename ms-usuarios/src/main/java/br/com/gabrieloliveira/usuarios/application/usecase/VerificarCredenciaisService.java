package br.com.gabrieloliveira.usuarios.application.usecase;

import br.com.gabrieloliveira.usuarios.application.port.in.VerificarCredenciaisUseCase;
import br.com.gabrieloliveira.usuarios.application.port.out.SenhaEncoderPort;
import br.com.gabrieloliveira.usuarios.application.port.out.UsuarioRepositoryPort;
import br.com.gabrieloliveira.usuarios.domain.exception.CredenciaisInvalidasException;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;

import java.util.Locale;
import java.util.Optional;

// confere email e senha:
// 1. procura o usuario pelo email (normalizado do mesmo jeito que no cadastro)
// 2. confere a senha digitada com o hash bcrypt guardado
// 3. se qualquer um dos dois falhar, lanca a mesma excecao, com a mesma mensagem
//    (quem esta de fora nao consegue saber se o email existe ou se so a senha esta errada)
public class VerificarCredenciaisService implements VerificarCredenciaisUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final SenhaEncoderPort senhaEncoderPort;

    public VerificarCredenciaisService(UsuarioRepositoryPort usuarioRepositoryPort,
                                       SenhaEncoderPort senhaEncoderPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.senhaEncoderPort = senhaEncoderPort;
    }

    @Override
    public Usuario verificar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isEmpty()) {
            throw new CredenciaisInvalidasException();
        }

        // o email e guardado sem espacos e em minusculo (ver Usuario.normalizarEmail),
        // entao "Ana@Email.com " tambem precisa encontrar "ana@email.com"
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);

        Optional<Usuario> encontrado = usuarioRepositoryPort.buscarPorEmail(emailNormalizado);
        if (encontrado.isEmpty()) {
            throw new CredenciaisInvalidasException();
        }

        Usuario usuario = encontrado.get();
        if (!senhaEncoderPort.confere(senha, usuario.getSenha())) {
            throw new CredenciaisInvalidasException();
        }

        return usuario;
    }
}
