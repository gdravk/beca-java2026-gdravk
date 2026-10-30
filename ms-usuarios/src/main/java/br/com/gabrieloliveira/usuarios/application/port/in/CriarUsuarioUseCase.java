package br.com.gabrieloliveira.usuarios.application.port.in;

import br.com.gabrieloliveira.usuarios.domain.model.Usuario;

// porta de entrada: diz O QUE o sistema sabe fazer, sem dizer COMO
// o controller conversa so com essa interface, e nao com a classe que implementa
public interface CriarUsuarioUseCase {

    // recebe o usuario com a senha ainda pura e devolve ele ja salvo (com id e data de criacao)
    Usuario criar(Usuario novoUsuario);
}
