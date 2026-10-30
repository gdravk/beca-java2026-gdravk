package br.com.gabrieloliveira.usuarios.application.port.in;

import br.com.gabrieloliveira.usuarios.domain.model.Usuario;

// porta de entrada pra atualizar os dados de um usuario que ja existe
public interface AtualizarUsuarioUseCase {

    // dadosNovos traz nome, email, cpf e senha pura; o id vem separado (da url)
    Usuario atualizar(Long id, Usuario dadosNovos);
}
