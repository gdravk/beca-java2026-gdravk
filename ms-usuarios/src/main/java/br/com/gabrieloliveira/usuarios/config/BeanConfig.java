package br.com.gabrieloliveira.usuarios.config;

import br.com.gabrieloliveira.usuarios.application.port.in.AtualizarUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.in.BuscarUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.in.CriarUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.in.ExcluirUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.in.ListarUsuariosUseCase;
import br.com.gabrieloliveira.usuarios.application.port.out.SenhaEncoderPort;
import br.com.gabrieloliveira.usuarios.application.port.out.UsuarioRepositoryPort;
import br.com.gabrieloliveira.usuarios.application.usecase.AtualizarUsuarioService;
import br.com.gabrieloliveira.usuarios.application.usecase.BuscarUsuarioService;
import br.com.gabrieloliveira.usuarios.application.usecase.CriarUsuarioService;
import br.com.gabrieloliveira.usuarios.application.usecase.ExcluirUsuarioService;
import br.com.gabrieloliveira.usuarios.application.usecase.ListarUsuariosService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// aqui e onde as pecas se encaixam
// os casos de uso sao java puro (sem @Service), entao o spring nao cria eles sozinho
// cada metodo @Bean abaixo cria um caso de uso e entrega pra ele as portas de saida que ele precisa
// o spring descobre sozinho quem implementa cada porta (UsuarioPersistenceAdapter, BCryptSenhaEncoderAdapter)
// e o controller recebe esses casos de uso pelas interfaces (portas de entrada)
@Configuration
public class BeanConfig {

    @Bean
    public CriarUsuarioUseCase criarUsuarioUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                                                   SenhaEncoderPort senhaEncoderPort) {
        return new CriarUsuarioService(usuarioRepositoryPort, senhaEncoderPort);
    }

    @Bean
    public ListarUsuariosUseCase listarUsuariosUseCase(UsuarioRepositoryPort usuarioRepositoryPort) {
        return new ListarUsuariosService(usuarioRepositoryPort);
    }

    @Bean
    public BuscarUsuarioUseCase buscarUsuarioUseCase(UsuarioRepositoryPort usuarioRepositoryPort) {
        return new BuscarUsuarioService(usuarioRepositoryPort);
    }

    @Bean
    public AtualizarUsuarioUseCase atualizarUsuarioUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                                                           SenhaEncoderPort senhaEncoderPort) {
        return new AtualizarUsuarioService(usuarioRepositoryPort, senhaEncoderPort);
    }

    @Bean
    public ExcluirUsuarioUseCase excluirUsuarioUseCase(UsuarioRepositoryPort usuarioRepositoryPort) {
        return new ExcluirUsuarioService(usuarioRepositoryPort);
    }
}
