package br.com.gabrieloliveira.usuarios.application.usecase;

import br.com.gabrieloliveira.usuarios.application.port.in.ListarUsuariosUseCase;
import br.com.gabrieloliveira.usuarios.application.port.out.UsuarioRepositoryPort;
import br.com.gabrieloliveira.usuarios.domain.model.Pagina;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;

// implementa o caso de uso de listar usuarios com paginacao
public class ListarUsuariosService implements ListarUsuariosUseCase {

    // limite para ninguem pedir 1 milhao de usuarios de uma vez e travar a api
    private static final int TAMANHO_MAXIMO_PAGINA = 100;

    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public ListarUsuariosService(UsuarioRepositoryPort usuarioRepositoryPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    public Pagina<Usuario> listar(int numeroPagina, int tamanhoPagina) {
        // se chegarem valores sem sentido, ajustamos em vez de dar erro: pagina negativa vira 0, tamanho menor
        // que 1 vira 1 e maior que 100 vira 100 (o controller ja barra esses casos com 400; isto e uma garantia a mais)
        int paginaAjustada = Math.max(numeroPagina, 0);
        int tamanhoAjustado = Math.min(Math.max(tamanhoPagina, 1), TAMANHO_MAXIMO_PAGINA);

        return usuarioRepositoryPort.listar(paginaAjustada, tamanhoAjustado);
    }
}
