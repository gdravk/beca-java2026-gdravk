package br.com.gabrieloliveira.usuarios.application.usecase;

import br.com.gabrieloliveira.usuarios.application.port.in.BuscarUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.out.UsuarioRepositoryPort;
import br.com.gabrieloliveira.usuarios.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;

// implementa o caso de uso de buscar um usuario pelo id
public class BuscarUsuarioService implements BuscarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public BuscarUsuarioService(UsuarioRepositoryPort usuarioRepositoryPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    public Usuario buscarPorId(Long id) {
        // se a "caixinha" (Optional) vier vazia, o orElseThrow lanca a nossa excecao, que vira 404
        return usuarioRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("usuario com id " + id + " nao encontrado"));
    }
}
