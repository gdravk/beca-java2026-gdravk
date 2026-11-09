package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.out.CambioPort;
import br.com.gabrieloliveira.transacoes.application.port.out.SaldoContaPort;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.Cotacao;
import br.com.gabrieloliveira.transacoes.domain.model.SaldoConta;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// testes do fluxo de criacao de transacao (fase 14 do guia) e do fuso de brasilia (revisao do pedido 5)
// as portas (banco, api mock de saldo e cambio) sao de mentira (mocks do mockito): nada sai do computador
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
        // cambio fora do ar: nao importa pra maioria dos testes (e nao pode atrapalhar a criacao)
        // lenient = "tolerante": o mockito nao reclama no teste que troca esse comportamento
        lenient().when(cambioPort.buscarCotacaoAtual(anyString())).thenThrow(new ServicoExternoIndisponivelException("fora"));
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

    @Test
    void depositoSemCategoriaFicaComOutros() {
        Transacao deposito = transacao(TipoTransacao.DEPOSITO, "80.00");

        Transacao criada = service.criar(deposito);

        assertEquals(Categoria.OUTROS, criada.getCategoria());
    }

    @Test
    void retiradaComSaldoEAprovadaESalva() {
        // dado: conta com saldo e limites folgados, nada gasto hoje
        when(saldoContaPort.buscarPorUsuario(1L)).thenReturn(Optional.of(contaPadrao()));
        when(transacaoRepositoryPort.somarSaidasAprovadasNoDia(1L, LocalDate.of(2026, 11, 5)))
                .thenReturn(BigDecimal.ZERO);

        // quando
        Transacao criada = service.criar(transacao(TipoTransacao.RETIRADA, "200.00"));

        // entao
        assertEquals(StatusTransacao.APPROVED, criada.getStatus());
        assertNull(criada.getMotivoRejeicao());
        verify(transacaoRepositoryPort).salvar(criada);
    }

    @Test
    void retiradaMaiorQueOSaldoEGravadaComoRejeitada() {
        when(saldoContaPort.buscarPorUsuario(1L)).thenReturn(Optional.of(contaPadrao()));
        when(transacaoRepositoryPort.somarSaidasAprovadasNoDia(any(), any())).thenReturn(BigDecimal.ZERO);

        Transacao criada = service.criar(transacao(TipoTransacao.RETIRADA, "1000.01"));

        // a transacao rejeitada tambem e salva, com o motivo (regra do PO)
        assertEquals(StatusTransacao.REJECTED, criada.getStatus());
        assertEquals("saldo insuficiente", criada.getMotivoRejeicao());
        verify(transacaoRepositoryPort).salvar(criada);
    }

    @Test
    void limiteDiarioContaOQueJaFoiGastoNoDiaDeBrasilia() {
        // ja gastou 700 no dia 05/11 (dia de brasilia, e nao o 06/11 do utc); limite diario 800
        when(saldoContaPort.buscarPorUsuario(1L)).thenReturn(Optional.of(contaPadrao()));
        when(transacaoRepositoryPort.somarSaidasAprovadasNoDia(1L, LocalDate.of(2026, 11, 5)))
                .thenReturn(new BigDecimal("700.00"));

        Transacao criada = service.criar(transacao(TipoTransacao.RETIRADA, "150.00"));

        assertEquals(StatusTransacao.REJECTED, criada.getStatus());
        assertEquals("limite diario excedido (limite: 800.00, ja utilizado hoje: 700.00)",
                criada.getMotivoRejeicao());
    }

    @Test
    void apiMockForaDoArRejeitaASaidaEmVezDeAprovarAsCegas() {
        when(saldoContaPort.buscarPorUsuario(1L)).thenThrow(new ServicoExternoIndisponivelException("fora do ar"));

        Transacao criada = service.criar(transacao(TipoTransacao.RETIRADA, "10.00"));

        assertEquals(StatusTransacao.REJECTED, criada.getStatus());
        assertEquals("nao foi possivel consultar o saldo agora (servico de saldo indisponivel), "
                + "tente novamente mais tarde", criada.getMotivoRejeicao());
        verify(transacaoRepositoryPort).salvar(criada);
    }

    @Test
    void usuarioSemContaNaApiMockTemASaidaRejeitada() {
        when(saldoContaPort.buscarPorUsuario(1L)).thenReturn(Optional.empty());

        Transacao criada = service.criar(transacao(TipoTransacao.RETIRADA, "10.00"));

        assertEquals(StatusTransacao.REJECTED, criada.getStatus());
        assertEquals("conta do usuario nao encontrada no servico de saldo", criada.getMotivoRejeicao());
    }

    @Test
    void transferenciaParaSiMesmoERejeitadaSemConsultarOSaldo() {
        Transacao transferencia = transacao(TipoTransacao.TRANSFERENCIA, "10.00");
        transferencia.setUsuarioDestinoId(1L);

        Transacao criada = service.criar(transferencia);

        assertEquals(StatusTransacao.REJECTED, criada.getStatus());
        assertEquals("nao e permitido transferir para a propria conta", criada.getMotivoRejeicao());
        // com os dados errados nem vale a pena chamar a api mock
        verify(saldoContaPort, never()).buscarPorUsuario(any());
    }

    @Test
    void cotacoesDoDolarEDoEuroSaoGuardadasNaTransacao() {
        // dado: brasilapi respondendo (troca o "fora do ar" do setUp so neste teste)
        doReturn(new Cotacao("USD", new BigDecimal("5.4321"), LocalDate.of(2026, 11, 5)))
                .when(cambioPort).buscarCotacaoAtual("USD");
        doReturn(new Cotacao("EUR", new BigDecimal("6.1234"), LocalDate.of(2026, 11, 5)))
                .when(cambioPort).buscarCotacaoAtual("EUR");

        // quando
        Transacao criada = service.criar(transacao(TipoTransacao.DEPOSITO, "100.00"));

        // entao
        assertEquals(new BigDecimal("5.4321"), criada.getTaxaDolar());
        assertEquals(new BigDecimal("6.1234"), criada.getTaxaEuro());
        assertEquals(LocalDate.of(2026, 11, 5), criada.getDataTaxaDolar());
    }

    // ---------- ajudantes ----------

    private Transacao transacao(TipoTransacao tipo, String valor) {
        Transacao transacao = new Transacao();
        transacao.setUsuarioId(1L);
        transacao.setTipo(tipo);
        transacao.setValor(new BigDecimal(valor));
        transacao.setDescricao("teste");
        return transacao;
    }

    // saldo 1000, limite diario 800, limite por transacao 500
    private SaldoConta contaPadrao() {
        return new SaldoConta(1L, new BigDecimal("1000.00"), new BigDecimal("800.00"), new BigDecimal("500.00"));
    }
}
