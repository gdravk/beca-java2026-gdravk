package br.com.gabrieloliveira.usuarios.application.port.in;

import br.com.gabrieloliveira.usuarios.domain.model.Usuario;

// porta de entrada pra atualizar os dados de um usuario que ja existe
public interface AtualizarUsuarioUseCase {

    // dadosNovos traz nome, email, cpf e, se o cliente quiser trocar, a senha pura; o id vem separado (da url)
    // senha null em dadosNovos = manter a senha atual
    Usuario atualizar(Long id, Usuario dadosNovos);
}
