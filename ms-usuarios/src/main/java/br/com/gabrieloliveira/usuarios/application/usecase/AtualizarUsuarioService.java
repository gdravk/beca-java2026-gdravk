package br.com.gabrieloliveira.usuarios.application.usecase;

import br.com.gabrieloliveira.usuarios.application.port.in.AtualizarUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.out.SenhaEncoderPort;
import br.com.gabrieloliveira.usuarios.application.port.out.UsuarioRepositoryPort;
import br.com.gabrieloliveira.usuarios.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.usuarios.domain.exception.RegraNegocioException;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;

// implementa o caso de uso de atualizar um usuario
public class AtualizarUsuarioService implements AtualizarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final SenhaEncoderPort senhaEncoderPort;

    public AtualizarUsuarioService(UsuarioRepositoryPort usuarioRepositoryPort, SenhaEncoderPort senhaEncoderPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.senhaEncoderPort = senhaEncoderPort;
    }

    @Override
    public Usuario atualizar(Long id, Usuario dadosNovos) {
        // primeiro garante que o usuario existe (senao, 404)
        Usuario usuario = usuarioRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("usuario com id " + id + " nao encontrado"));

        // aqui conferimos so os OUTROS usuarios, senao o proprio usuario seria barrado
        // ao salvar o email ou o cpf que ja sao dele
        if (usuarioRepositoryPort.existePorEmailEmOutroUsuario(dadosNovos.getEmail(), id)) {
            throw new RegraNegocioException("ja existe outro usuario cadastrado com esse email");
        }
        if (usuarioRepositoryPort.existePorCpfEmOutroUsuario(dadosNovos.getCpf(), id)) {
            throw new RegraNegocioException("ja existe outro usuario cadastrado com esse cpf");
        }

        // copia os dados novos pro usuario que veio do banco
        // o id e a data de criacao continuam os mesmos de antes
        usuario.setNome(dadosNovos.getNome());
        usuario.setEmail(dadosNovos.getEmail());
        usuario.setCpf(dadosNovos.getCpf());

        // a senha e opcional no update:
        // - veio senha nova -> gera o hash dela e troca
        // - nao veio (null) -> nao mexe, o usuario continua com o hash que ja estava no banco
        // antes a senha era obrigatoria e recriptografada em todo update, entao nem dava pra mudar so o nome
        if (dadosNovos.getSenha() != null) {
            usuario.setSenha(senhaEncoderPort.codificar(dadosNovos.getSenha()));
        }

        return usuarioRepositoryPort.salvar(usuario);
    }
}
