package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.out.CambioPort;
import br.com.gabrieloliveira.transacoes.application.port.out.SaldoContaPort;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.StatusTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import br.com.gabrieloliveira.transacoes.domain.service.ValidadorDeTransacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// testes do fuso de brasilia na criacao da transacao (ajuste da revisao do pedido 5)
// o relogio fica parado em 06/11/2026 01:00 em utc, que no brasil ainda e 05/11 as 22h
@ExtendWith(MockitoExtension.class)
class CriarTransacaoServiceTest {

    @Mock
    private TransacaoRepositoryPort transacaoRepositoryPort;

    @Mock
    private SaldoContaPort saldoContaPort;

    @Mock
    private CambioPort cambioPort;

    private CriarTransacaoService service;

    @BeforeEach
    void setUp() {
        Clock relogio = Clock.fixed(Instant.parse("2026-11-06T01:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        service = new CriarTransacaoService(transacaoRepositoryPort, saldoContaPort,
                new PreenchedorDeCotacoes(cambioPort), new ValidadorDeTransacao(), relogio);
        // o salvar de mentira devolve a propria transacao recebida
        when(transacaoRepositoryPort.salvar(any(Transacao.class))).thenAnswer(chamada -> chamada.getArgument(0));
        // cambio fora do ar: nao importa pra esses testes (e nao pode atrapalhar a criacao)
        when(cambioPort.buscarCotacaoAtual(anyString())).thenThrow(new ServicoExternoIndisponivelException("fora"));
    }

    @Test
    void depositoAs22hNoBrasilFicaGravadoNoDiaCertoMesmoComOServidorEmUtc() {
        Transacao deposito = new Transacao();
        deposito.setUsuarioId(1L);
        deposito.setTipo(TipoTransacao.DEPOSITO);
        deposito.setValor(new BigDecimal("100.00"));
        deposito.setDescricao("salario");

        Transacao criada = service.criar(deposito);

        // dia 05 as 22h (horario de brasilia), e nao dia 06 as 01h (horario utc)
        assertEquals(LocalDateTime.of(2026, 11, 5, 22, 0), criada.getDataHora());
        assertEquals(StatusTransacao.APPROVED, criada.getStatus());
        // deposito nao consulta saldo
        verify(saldoContaPort, never()).buscarPorUsuario(any());
    }

    @Test
    void cambioForaDoArNaoImpedeACriacao() {
        Transacao deposito = new Transacao();
        deposito.setUsuarioId(1L);
        deposito.setTipo(TipoTransacao.DEPOSITO);
        deposito.setValor(new BigDecimal("50.00"));
        deposito.setCategoria(Categoria.OUTROS);
        deposito.setDescricao("pix");

        Transacao criada = service.criar(deposito);

        assertEquals(StatusTransacao.APPROVED, criada.getStatus());
        assertEquals(null, criada.getTaxaDolar());
    }
}
