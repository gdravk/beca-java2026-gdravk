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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// testes do caso de uso de excluir usuario
@ExtendWith(MockitoExtension.class)
class ExcluirUsuarioServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    private ExcluirUsuarioService excluirUsuarioService;

    @BeforeEach
    void setUp() {
        excluirUsuarioService = new ExcluirUsuarioService(usuarioRepositoryPort);
    }

    @Test
    void deveExcluirQuandoOUsuarioExiste() {
        // dado: o usuario 1 existe
        when(usuarioRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(new Usuario()));

        // quando
        excluirUsuarioService.excluir(1L);

        // entao: o banco recebeu o pedido de excluir o id 1
        verify(usuarioRepositoryPort).excluirPorId(1L);
    }

    @Test
    void deveLancarRecursoNaoEncontradoENaoExcluirNadaQuandoOUsuarioNaoExiste() {
        when(usuarioRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> excluirUsuarioService.excluir(99L));

        // anyLong() = "com qualquer id": o excluirPorId nao pode ter sido chamado nenhuma vez
        verify(usuarioRepositoryPort, never()).excluirPorId(anyLong());
    }
}
