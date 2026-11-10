package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.exception.ParametroInvalidoException;
import br.com.gabrieloliveira.transacoes.domain.model.AnaliseDeDespesas;
import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorCategoria;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorDia;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorMes;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import br.com.gabrieloliveira.transacoes.domain.service.AnalisadorDeDespesas;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// testes do caso de uso da analise de despesas
// o banco e um mock (TransacaoRepositoryPort) e o relogio fica parado em 05/11/2026, entao o
// "mes atual" e sempre novembro de 2026, nao importa quando o teste roda
@ExtendWith(MockitoExtension.class)
class AnalisarDespesasServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    @Mock
    private TransacaoRepositoryPort transacaoRepositoryPort;

    private AnalisarDespesasService service;

    @BeforeEach
    void setUp() {
        // o Clock.fixed cria um relogio que marca sempre o mesmo instante
        Clock relogioParado = Clock.fixed(Instant.parse("2026-11-05T15:00:00Z"), FUSO);
        service = new AnalisarDespesasService(transacaoRepositoryPort, new AnalisadorDeDespesas(), relogioParado);
    }

    @Test
    void semPeriodoConsultaOBancoNoMesAtualInteiro() {
        // criterio do PO: "sem informar periodo, recebo o mes atual"
        when(transacaoRepositoryPort.listarDespesas(any(), any(), any())).thenReturn(List.of());

        AnaliseDeDespesas<DespesaPorCategoria> analise = service.porCategoria(1L, null, null);

        // o periodo devolvido e novembro inteiro...
        assertEquals(LocalDate.of(2026, 11, 1), analise.getPeriodo().getInicio());
        assertEquals(LocalDate.of(2026, 11, 30), analise.getPeriodo().getFim());
        // ...e o banco foi consultado de 01/11 00:00 ate 01/12 00:00 (sem incluir)
        verify(transacaoRepositoryPort).listarDespesas(1L,
                LocalDateTime.of(2026, 11, 1, 0, 0), LocalDateTime.of(2026, 12, 1, 0, 0));
    }

    @Test
    void periodoInvertidoNemConsultaOBanco() {
        assertThrows(ParametroInvalidoException.class,
                () -> service.porDia(1L, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 10)));

        verify(transacaoRepositoryPort, never()).listarDespesas(any(), any(), any());
    }

    @Test
    void resumoPorCategoriaTrazTotalGeralQuantidadeELinhas() {
        when(transacaoRepositoryPort.listarDespesas(any(), any(), any())).thenReturn(List.of(
                despesa(TipoTransacao.COMPRA, "120.00", Categoria.ALIMENTACAO, 3),
                despesa(TipoTransacao.COMPRA, "80.00", Categoria.ALIMENTACAO, 4),
                despesa(TipoTransacao.RETIRADA, "50.00", Categoria.OUTROS, 4)));

        AnaliseDeDespesas<DespesaPorCategoria> analise = service.porCategoria(1L, null, null);

        assertEquals(1L, analise.getUsuarioId());
        assertEquals(3, analise.getQuantidadeDespesas());
        assertEquals(new BigDecimal("250.00"), analise.getTotalGeral());
        assertEquals(2, analise.getItens().size());
        assertEquals(Categoria.ALIMENTACAO, analise.getItens().get(0).getCategoria());
        assertEquals(new BigDecimal("200.00"), analise.getItens().get(0).getTotal());
    }

    @Test
    void segundaConferenciaDoDominioTiraOQueNaoEDespesa() {
        // mesmo que o banco devolvesse um deposito ou uma rejeitada por engano, o dominio tira da conta
        Transacao rejeitada = despesa(TipoTransacao.COMPRA, "999.00", Categoria.LAZER, 2);
        rejeitada.rejeitar("saldo insuficiente");
        when(transacaoRepositoryPort.listarDespesas(any(), any(), any())).thenReturn(List.of(
                despesa(TipoTransacao.COMPRA, "10.00", Categoria.LAZER, 2),
                despesa(TipoTransacao.DEPOSITO, "500.00", Categoria.OUTROS, 2),
                rejeitada));

        AnaliseDeDespesas<DespesaPorDia> analise = service.porDia(1L, null, null);

        assertEquals(1, analise.getQuantidadeDespesas());
        assertEquals(new BigDecimal("10.00"), analise.getTotalGeral());
    }

    @Test
    void resumoPorMesUsaOPeriodoInformado() {
        when(transacaoRepositoryPort.listarDespesas(any(), any(), any())).thenReturn(List.of(
                despesa(TipoTransacao.COMPRA, "10.00", Categoria.LAZER, 2)));

        AnaliseDeDespesas<DespesaPorMes> analise = service.porMes(7L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertEquals(LocalDate.of(2026, 1, 1), analise.getPeriodo().getInicio());
        assertEquals(LocalDate.of(2026, 12, 31), analise.getPeriodo().getFim());
        assertEquals(1, analise.getItens().size());
        verify(transacaoRepositoryPort).listarDespesas(7L,
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2027, 1, 1, 0, 0));
    }

    private Transacao despesa(TipoTransacao tipo, String valor, Categoria categoria, int diaDeNovembro) {
        Transacao transacao = new Transacao();
        transacao.setUsuarioId(1L);
        transacao.setTipo(tipo);
        transacao.setValor(new BigDecimal(valor));
        transacao.setCategoria(categoria);
        transacao.setDataHora(LocalDateTime.of(2026, 11, diaDeNovembro, 10, 0));
        transacao.aprovar();
        return transacao;
    }
}
