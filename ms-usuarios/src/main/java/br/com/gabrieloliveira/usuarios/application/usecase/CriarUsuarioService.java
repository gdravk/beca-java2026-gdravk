package br.com.gabrieloliveira.usuarios.application.usecase;

import br.com.gabrieloliveira.usuarios.application.port.in.CriarUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.out.SenhaEncoderPort;
import br.com.gabrieloliveira.usuarios.application.port.out.UsuarioRepositoryPort;
import br.com.gabrieloliveira.usuarios.domain.exception.RegraNegocioException;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;

import java.time.Clock;
import java.time.LocalDateTime;

// implementa o caso de uso de criar usuario
// repara que nao existe nenhuma anotacao do spring aqui (@Service, @Autowired...): quem cria este objeto
// e entrega as portas para ele e o BeanConfig, la no pacote config
public class CriarUsuarioService implements CriarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final SenhaEncoderPort senhaEncoderPort;

    // relogio no fuso de brasilia (bean do BeanConfig), o mesmo cuidado do ms2: num container em utc,
    // um cadastro feito as 22h no brasil ficaria com a data do dia seguinte
    private final Clock relogio;

    public CriarUsuarioService(UsuarioRepositoryPort usuarioRepositoryPort, SenhaEncoderPort senhaEncoderPort,
                               Clock relogio) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.senhaEncoderPort = senhaEncoderPort;
        this.relogio = relogio;
    }

    @Override
    public Usuario criar(Usuario novoUsuario) {
        // primeiro conferimos as regras; so depois mexemos no banco
        if (usuarioRepositoryPort.existePorEmail(novoUsuario.getEmail())) {
            throw new RegraNegocioException("ja existe um usuario cadastrado com esse email");
        }
        if (usuarioRepositoryPort.existePorCpf(novoUsuario.getCpf())) {
            throw new RegraNegocioException("ja existe um usuario cadastrado com esse cpf");
        }

        // a senha nunca vai para o banco do jeito que veio, so o hash dela
        novoUsuario.setSenha(senhaEncoderPort.codificar(novoUsuario.getSenha()));
        // a data de criacao e definida aqui, no momento do cadastro
        novoUsuario.setDataCriacao(LocalDateTime.now(relogio));

        return usuarioRepositoryPort.salvar(novoUsuario);
    }
}
