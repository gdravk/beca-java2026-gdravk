package br.com.gabrieloliveira.usuarios.application.port.in;

import br.com.gabrieloliveira.usuarios.domain.model.Usuario;

// porta de entrada para atualizar os dados de um usuario que ja existe
public interface AtualizarUsuarioUseCase {

    // o id vem separado (da url); dadosNovos traz nome, email, cpf e, se a pessoa quiser trocar, a senha pura
    // senha null em dadosNovos quer dizer "manter a senha atual"
    Usuario atualizar(Long id, Usuario dadosNovos);
}
