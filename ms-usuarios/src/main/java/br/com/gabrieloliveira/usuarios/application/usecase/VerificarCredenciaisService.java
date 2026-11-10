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
// 2. confere a senha digitada com o hash do bcrypt que esta guardado
// 3. se qualquer um dos dois falhar, lanca a mesma excecao, com a mesma mensagem: quem esta de fora
//    nao consegue saber se o email existe ou se so a senha esta errada
public class VerificarCredenciaisService implements VerificarCredenciaisUseCase {

    // uma senha qualquer, usada so para gerar o hash falso abaixo (nao e a senha de ninguem)
    private static final String SENHA_DO_HASH_FALSO = "senha-que-nenhum-usuario-tem";

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final SenhaEncoderPort senhaEncoderPort;

    // hash usado quando o email nao existe (a explicacao esta no metodo verificar)
    // ele e gerado uma vez so, quando a aplicacao sobe
    private final String hashFalso;

    public VerificarCredenciaisService(UsuarioRepositoryPort usuarioRepositoryPort,
                                       SenhaEncoderPort senhaEncoderPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.senhaEncoderPort = senhaEncoderPort;
        this.hashFalso = senhaEncoderPort.codificar(SENHA_DO_HASH_FALSO);
    }

    @Override
    public Usuario verificar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isEmpty()) {
            throw new CredenciaisInvalidasException();
        }

        // o email e guardado sem espacos e em minusculo (ver Usuario.normalizarEmail), entao
        // "Ana@Email.com " tambem precisa encontrar "ana@email.com"
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);

        Optional<Usuario> encontrado = usuarioRepositoryPort.buscarPorEmail(emailNormalizado);
        if (encontrado.isEmpty()) {
            // o bcrypt e lento de proposito (cerca de 100ms). se ele so rodasse quando o email existe, um email
            // inexistente responderia bem mais rapido, e medindo esse tempo daria para descobrir quais emails estao
            // cadastrados. por isso conferimos a senha contra um hash falso mesmo assim: os dois casos demoram o
            // mesmo tempo (e o resultado e ignorado, porque aqui e sempre "invalido")
            senhaEncoderPort.confere(senha, hashFalso);
            throw new CredenciaisInvalidasException();
        }

        Usuario usuario = encontrado.get();
        if (!senhaEncoderPort.confere(senha, usuario.getSenha())) {
            throw new CredenciaisInvalidasException();
        }

        return usuario;
    }
}
