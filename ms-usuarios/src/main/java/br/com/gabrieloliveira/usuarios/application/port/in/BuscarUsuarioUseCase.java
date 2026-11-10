package br.com.gabrieloliveira.usuarios.application.port.in;

import br.com.gabrieloliveira.usuarios.domain.model.Usuario;

// porta de entrada para buscar um usuario pelo id
public interface BuscarUsuarioUseCase {

    // se ele nao existir, lanca RecursoNaoEncontradoException (que vira 404)
    Usuario buscarPorId(Long id);
}
