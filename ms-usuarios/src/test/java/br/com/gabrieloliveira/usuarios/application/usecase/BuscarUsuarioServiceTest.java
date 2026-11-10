package br.com.gabrieloliveira.usuarios.application.usecase;

import br.com.gabrieloliveira.usuarios.application.port.out.UsuarioRepositoryPort;
import br.com.gabrieloliveira.usuarios.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

// testes do caso de uso de buscar usuario pelo id
@ExtendWith(MockitoExtension.class)
class BuscarUsuarioServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    private BuscarUsuarioService buscarUsuarioService;

    @BeforeEach
    void setUp() {
        buscarUsuarioService = new BuscarUsuarioService(usuarioRepositoryPort);
    }

    @Test
    void deveDevolverOUsuarioQuandoEleExiste() {
        // dado: o banco de mentira encontra o usuario 1
        // (Optional.of(...) = a "caixinha" com o usuario dentro)
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        when(usuarioRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(usuario));

        // quando
        Usuario encontrado = buscarUsuarioService.buscarPorId(1L);

        // entao
        assertSame(usuario, encontrado);
    }

    @Test
    void deveLancarRecursoNaoEncontradoQuandoOUsuarioNaoExiste() {
        // dado: o banco de mentira nao encontra nada (Optional.empty() = a caixinha vazia)
        when(usuarioRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

        // quando / entao: lanca a excecao que vira 404 na api, com o id na mensagem
        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> buscarUsuarioService.buscarPorId(99L));
        assertEquals("usuario com id 99 nao encontrado", erro.getMessage());
    }
}
