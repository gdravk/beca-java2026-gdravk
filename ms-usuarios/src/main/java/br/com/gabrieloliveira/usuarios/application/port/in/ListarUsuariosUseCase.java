package br.com.gabrieloliveira.usuarios.application.port.in;

import br.com.gabrieloliveira.usuarios.domain.model.Pagina;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;

// porta de entrada para listar os usuarios aos poucos, uma pagina por vez
public interface ListarUsuariosUseCase {

    Pagina<Usuario> listar(int numeroPagina, int tamanhoPagina);
}
