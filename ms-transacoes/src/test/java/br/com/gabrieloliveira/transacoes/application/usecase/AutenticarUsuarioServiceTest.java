package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.out.AutenticacaoPort;
import br.com.gabrieloliveira.transacoes.domain.exception.CredenciaisInvalidasException;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// testes da autenticacao do ms2
// o ms1 e simulado pelo mock da AutenticacaoPort: cada teste diz o que o "ms1" respondeu
@ExtendWith(MockitoExtension.class)
class AutenticarUsuarioServiceTest {

    @Mock
    private AutenticacaoPort autenticacaoPort;

    private AutenticarUsuarioService service;

    @BeforeEach
    void setUp() {
        service = new AutenticarUsuarioService(autenticacaoPort);
    }

    @Test
    void loginCertoDevolveOIdDoUsuario() {
        when(autenticacaoPort.verificar("ana@email.com", "Senha@123")).thenReturn(Optional.of(7L));

        assertEquals(7L, service.autenticar("ana@email.com", "Senha@123"));
    }

    @Test
    void loginErradoNoMs1EhCredencialInvalida() {
        when(autenticacaoPort.verificar("ana@email.com", "errada")).thenReturn(Optional.empty());

        assertThrows(CredenciaisInvalidasException.class, () -> service.autenticar("ana@email.com", "errada"));
    }

    @Test
    void emailOuSenhaVaziosNemChamamOMs1() {
        assertThrows(CredenciaisInvalidasException.class, () -> service.autenticar(" ", "Senha@123"));
        assertThrows(CredenciaisInvalidasException.class, () -> service.autenticar("ana@email.com", ""));

        verify(autenticacaoPort, never()).verificar(any(), any());
    }

    @Test
    void ms1ForaDoArNaoViraLoginErrado() {
        // ms1 fora do ar e um problema do sistema (503), e nao do usuario (401): a excecao sobe do jeito que veio
        when(autenticacaoPort.verificar("ana@email.com", "Senha@123"))
                .thenThrow(new ServicoExternoIndisponivelException("ms1 fora"));

        assertThrows(ServicoExternoIndisponivelException.class,
                () -> service.autenticar("ana@email.com", "Senha@123"));
    }
}
