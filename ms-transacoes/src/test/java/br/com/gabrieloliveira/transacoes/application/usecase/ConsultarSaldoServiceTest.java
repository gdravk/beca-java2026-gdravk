package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.out.SaldoContaPort;
import br.com.gabrieloliveira.transacoes.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import br.com.gabrieloliveira.transacoes.domain.model.SaldoConta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

// o SaldoContaPort e de mentira (mock): nenhum teste chama a api mock de verdade
@ExtendWith(MockitoExtension.class)
class ConsultarSaldoServiceTest {

    @Mock
    private SaldoContaPort saldoContaPort;

    @InjectMocks
    private ConsultarSaldoService service;

    @Test
    void devolveOSaldoQueVeioDaApiMock() {
        // dado
        SaldoConta conta = new SaldoConta(1L, new BigDecimal("1000.00"), new BigDecimal("800.00"),
                new BigDecimal("500.00"));
        when(saldoContaPort.buscarPorUsuario(1L)).thenReturn(Optional.of(conta));

        // quando
        SaldoConta resultado = service.consultar(1L);

        // entao
        assertEquals(new BigDecimal("1000.00"), resultado.getSaldo());
    }

    @Test
    void usuarioSemContaNaApiMockViraNaoEncontrado() {
        when(saldoContaPort.buscarPorUsuario(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException erro =
                assertThrows(RecursoNaoEncontradoException.class, () -> service.consultar(99L));

        assertEquals("conta do usuario 99 nao encontrada no servico de saldo", erro.getMessage());
    }

    @Test
    void apiMockForaDoArDeixaOErroSubirParaVirar503() {
        when(saldoContaPort.buscarPorUsuario(1L)).thenThrow(new ServicoExternoIndisponivelException("fora do ar"));

        assertThrows(ServicoExternoIndisponivelException.class, () -> service.consultar(1L));
    }
}
