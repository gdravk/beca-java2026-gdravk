package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.out.CambioPort;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.transacoes.domain.model.Cotacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// testes da busca de uma transacao pelo id, com a regra do dono (seguranca, pedido 6)
@ExtendWith(MockitoExtension.class)
class BuscarTransacaoServiceTest {

    @Mock
    private TransacaoRepositoryPort transacaoRepositoryPort;

    @Mock
    private CambioPort cambioPort;

    private BuscarTransacaoService service;

    @BeforeEach
    void setUp() {
        service = new BuscarTransacaoService(transacaoRepositoryPort, new PreenchedorDeCotacoes(cambioPort));
    }

    @Test
    void donoVeATransacaoComOCambioPreenchido() {
        when(transacaoRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(transacaoDoUsuario(1L)));
        when(cambioPort.buscarCotacaoAtual("USD")).thenReturn(new Cotacao("USD", new BigDecimal("5.00"), LocalDate.now()));
        when(cambioPort.buscarCotacaoAtual("EUR")).thenReturn(new Cotacao("EUR", new BigDecimal("6.00"), LocalDate.now()));

        Transacao transacao = service.buscarPorId(10L, 1L);

        assertEquals(new BigDecimal("5.00"), transacao.getTaxaDolar());
    }

    @Test
    void transacaoDeOutroUsuarioResponde404ESemChamarOCambio() {
        when(transacaoRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(transacaoDoUsuario(1L)));

        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> service.buscarPorId(10L, 2L));

        // a mesma mensagem de uma transacao que nao existe: quem pediu nao descobre que o id e de outra pessoa
        assertEquals("transacao com id 10 nao encontrada", erro.getMessage());
        // o dono e conferido antes do cambio, entao a brasilapi nem e chamada
        verify(cambioPort, never()).buscarCotacaoAtual(anyString());
    }

    @Test
    void transacaoInexistenteResponde404() {
        when(transacaoRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId(99L, 1L));
    }

    private Transacao transacaoDoUsuario(Long usuarioId) {
        Transacao transacao = new Transacao();
        transacao.setId(10L);
        transacao.setUsuarioId(usuarioId);
        transacao.setValor(new BigDecimal("100.00"));
        transacao.aprovar();
        return transacao;
    }
}
