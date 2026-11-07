package br.com.gabrieloliveira.usuarios.application.usecase;

import br.com.gabrieloliveira.usuarios.application.port.out.SenhaEncoderPort;
import br.com.gabrieloliveira.usuarios.application.port.out.UsuarioRepositoryPort;
import br.com.gabrieloliveira.usuarios.domain.exception.CredenciaisInvalidasException;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// testes do caso de uso que confere email e senha (seguranca, pedido 6)
// o banco e o bcrypt sao mocks: cada teste diz se o email existe e se a senha "bate"
@ExtendWith(MockitoExtension.class)
class VerificarCredenciaisServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private SenhaEncoderPort senhaEncoderPort;

    private VerificarCredenciaisService service;

    @BeforeEach
    void setUp() {
        service = new VerificarCredenciaisService(usuarioRepositoryPort, senhaEncoderPort);
    }

    @Test
    void emailESenhaCertosDevolvemOUsuario() {
        when(usuarioRepositoryPort.buscarPorEmail("ana@email.com")).thenReturn(Optional.of(usuarioComHash()));
        when(senhaEncoderPort.confere("Senha@123", "hash-guardado")).thenReturn(true);

        Usuario usuario = service.verificar("ana@email.com", "Senha@123");

        assertEquals(1L, usuario.getId());
    }

    @Test
    void emailComEspacosEMaiusculasTambemEncontraOUsuario() {
        // o email e guardado normalizado, entao a busca tambem precisa ser feita normalizada
        when(usuarioRepositoryPort.buscarPorEmail("ana@email.com")).thenReturn(Optional.of(usuarioComHash()));
        when(senhaEncoderPort.confere("Senha@123", "hash-guardado")).thenReturn(true);

        Usuario usuario = service.verificar("  Ana@Email.COM ", "Senha@123");

        assertEquals(1L, usuario.getId());
    }

    @Test
    void senhaErradaEhCredencialInvalida() {
        when(usuarioRepositoryPort.buscarPorEmail("ana@email.com")).thenReturn(Optional.of(usuarioComHash()));
        when(senhaEncoderPort.confere("errada", "hash-guardado")).thenReturn(false);

        CredenciaisInvalidasException erro = assertThrows(CredenciaisInvalidasException.class,
                () -> service.verificar("ana@email.com", "errada"));
        assertEquals("email ou senha invalidos", erro.getMessage());
    }

    @Test
    void emailInexistenteDaAMesmaMensagemDaSenhaErrada() {
        // a mesma mensagem nos dois casos: assim ninguem descobre quais emails existem
        when(usuarioRepositoryPort.buscarPorEmail("nao@existe.com")).thenReturn(Optional.empty());

        CredenciaisInvalidasException erro = assertThrows(CredenciaisInvalidasException.class,
                () -> service.verificar("nao@existe.com", "Senha@123"));
        assertEquals("email ou senha invalidos", erro.getMessage());
        verify(senhaEncoderPort, never()).confere(anyString(), anyString());
    }

    @Test
    void emailOuSenhaVaziosNemConsultamOBanco() {
        assertThrows(CredenciaisInvalidasException.class, () -> service.verificar("", "Senha@123"));
        assertThrows(CredenciaisInvalidasException.class, () -> service.verificar("ana@email.com", null));

        verify(usuarioRepositoryPort, never()).buscarPorEmail(any());
    }

    private Usuario usuarioComHash() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Ana Souza");
        usuario.setEmail("ana@email.com");
        usuario.setSenha("hash-guardado");
        return usuario;
    }
}
