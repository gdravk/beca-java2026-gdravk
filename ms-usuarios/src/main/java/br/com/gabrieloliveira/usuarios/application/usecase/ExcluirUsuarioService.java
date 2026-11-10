package br.com.gabrieloliveira.usuarios.application.usecase;

import br.com.gabrieloliveira.usuarios.application.port.in.ExcluirUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.out.UsuarioRepositoryPort;
import br.com.gabrieloliveira.usuarios.domain.exception.RecursoNaoEncontradoException;

// implementa o caso de uso de excluir um usuario
public class ExcluirUsuarioService implements ExcluirUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public ExcluirUsuarioService(UsuarioRepositoryPort usuarioRepositoryPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    public void excluir(Long id) {
        // confere antes, para devolver 404 em vez de "excluir" algo que nem existia
        if (usuarioRepositoryPort.buscarPorId(id).isEmpty()) {
            throw new RecursoNaoEncontradoException("usuario com id " + id + " nao encontrado");
        }
        usuarioRepositoryPort.excluirPorId(id);
    }
}
