package br.com.gabrieloliveira.usuarios.application.port.in;

import br.com.gabrieloliveira.usuarios.domain.model.Usuario;

// porta de entrada pra buscar um usuario so, pelo id
public interface BuscarUsuarioUseCase {

    // se nao existir, lanca RecursoNaoEncontradoException (que vira 404)
    Usuario buscarPorId(Long id);
}
