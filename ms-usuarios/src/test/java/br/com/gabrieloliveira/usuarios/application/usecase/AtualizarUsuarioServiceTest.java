package br.com.gabrieloliveira.usuarios.application.usecase;

import br.com.gabrieloliveira.usuarios.application.port.out.SenhaEncoderPort;
import br.com.gabrieloliveira.usuarios.application.port.out.UsuarioRepositoryPort;
import br.com.gabrieloliveira.usuarios.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.usuarios.domain.exception.RegraNegocioException;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// testes do caso de uso de atualizar usuario
// cobrem as regras de duplicidade (so contra OUTROS usuarios) e a senha opcional no update
@ExtendWith(MockitoExtension.class)
class AtualizarUsuarioServiceTest {

    // data fixa pra conseguir conferir que ela nao muda no update
    private static final LocalDateTime DATA_CRIACAO = LocalDateTime.of(2026, 10, 30, 10, 0);

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private SenhaEncoderPort senhaEncoderPort;

    private AtualizarUsuarioService atualizarUsuarioService;

    @BeforeEach
    void setUp() {
        atualizarUsuarioService = new AtualizarUsuarioService(usuarioRepositoryPort, senhaEncoderPort);
    }

    @Test
    void deveAtualizarOsDadosETrocarASenhaQuandoElaForInformada() {
        // dado: o usuario 1 existe no banco com a senha antiga
        when(usuarioRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(usuarioDoBanco()));
        when(senhaEncoderPort.codificar("senhaNova1")).thenReturn("hash-novo");
        // thenAnswer devolvendo o proprio argumento = "o banco salvou e devolveu o mesmo objeto"
        when(usuarioRepositoryPort.salvar(any(Usuario.class))).thenAnswer(chamada -> chamada.getArgument(0));

        // quando: chegam dados novos COM senha
        Usuario atualizado = atualizarUsuarioService.atualizar(1L,
                dadosNovos("Ana Lima", "ana.lima@email.com", "11111111111", "senhaNova1"));

        // entao: os dados mudaram e a senha virou o hash novo
        assertEquals("Ana Lima", atualizado.getNome());
        assertEquals("ana.lima@email.com", atualizado.getEmail());
        assertEquals("11111111111", atualizado.getCpf());
        assertEquals("hash-novo", atualizado.getSenha());
        // id e data de criacao continuam os do banco
        assertEquals(1L, atualizado.getId());
        assertEquals(DATA_CRIACAO, atualizado.getDataCriacao());
    }

    @Test
    void deveManterASenhaAtualQuandoASenhaNaoForInformada() {
        // dado: o usuario existe com a senha antiga
        when(usuarioRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(usuarioDoBanco()));
        when(usuarioRepositoryPort.salvar(any(Usuario.class))).thenAnswer(chamada -> chamada.getArgument(0));

        // quando: chegam dados novos SEM senha (null), so trocando o nome
        Usuario atualizado = atualizarUsuarioService.atualizar(1L,
                dadosNovos("Ana Lima", "ana@email.com", "12345678901", null));

        // entao: o nome mudou, mas a senha continua o hash antigo
        assertEquals("Ana Lima", atualizado.getNome());
        assertEquals("hash-antigo", atualizado.getSenha());
        // e o bcrypt nem foi chamado (antes, ele recriptografava a senha em todo update)
        verify(senhaEncoderPort, never()).codificar(anyString());
    }

    @Test
    void deveLancarRecursoNaoEncontradoQuandoOUsuarioNaoExiste() {
        when(usuarioRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> atualizarUsuarioService.atualizar(99L, dadosNovos("X", "x@email.com", "12345678901", null)));

        verify(usuarioRepositoryPort, never()).salvar(any());
    }

    @Test
    void naoDevePermitirUsarOEmailDeOutroUsuario() {
        // dado: o email novo ja pertence a OUTRO usuario (id diferente de 1)
        when(usuarioRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(usuarioDoBanco()));
        when(usuarioRepositoryPort.existePorEmailEmOutroUsuario("bruno@email.com", 1L)).thenReturn(true);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> atualizarUsuarioService.atualizar(1L,
                        dadosNovos("Ana", "bruno@email.com", "12345678901", null)));
        assertEquals("ja existe outro usuario cadastrado com esse email", erro.getMessage());

        verify(usuarioRepositoryPort, never()).salvar(any());
    }

    @Test
    void naoDevePermitirUsarOCpfDeOutroUsuario() {
        when(usuarioRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(usuarioDoBanco()));
        when(usuarioRepositoryPort.existePorEmailEmOutroUsuario("ana@email.com", 1L)).thenReturn(false);
        when(usuarioRepositoryPort.existePorCpfEmOutroUsuario("99999999999", 1L)).thenReturn(true);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> atualizarUsuarioService.atualizar(1L,
                        dadosNovos("Ana", "ana@email.com", "99999999999", null)));
        assertEquals("ja existe outro usuario cadastrado com esse cpf", erro.getMessage());

        verify(usuarioRepositoryPort, never()).salvar(any());
    }

    // usuario como ele "estaria" no banco antes do update
    private Usuario usuarioDoBanco() {
        return new Usuario(1L, "Ana Souza", "ana@email.com", "12345678901", "hash-antigo", DATA_CRIACAO);
    }

    // dados que chegariam da api (senha pode ser null = nao trocar)
    private Usuario dadosNovos(String nome, String email, String cpf, String senha) {
        Usuario dados = new Usuario();
        dados.setNome(nome);
        dados.setEmail(email);
        dados.setCpf(cpf);
        dados.setSenha(senha);
        return dados;
    }
}
