package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.out.CambioPort;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.exception.ParametroInvalidoException;
import br.com.gabrieloliveira.transacoes.domain.model.Cotacao;
import br.com.gabrieloliveira.transacoes.domain.model.RelatorioTransacoes;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
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

// o relogio fica parado em 15/11/2026 (horario de brasilia), entao o "mes atual" dos testes e novembro de 2026
@ExtendWith(MockitoExtension.class)
class GerarRelatorioServiceTest {

    @Mock
    private TransacaoRepositoryPort transacaoRepositoryPort;

    @Mock
    private CambioPort cambioPort;

    private GerarRelatorioService service;

    @BeforeEach
    void setUp() {
        Clock relogio = Clock.fixed(Instant.parse("2026-11-15T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        service = new GerarRelatorioService(transacaoRepositoryPort, new PreenchedorDeCotacoes(cambioPort), relogio);
    }

    @Test
    void semDatasOMesAtualInteiroEUsadoETodosOsTiposEntram() {
        // dado: o repositorio recebe de 01/11 00:00 ate 01/12 00:00 (fim exclusivo) e tipo null (todos os tipos)
        when(transacaoRepositoryPort.listarPorUsuario(1L, LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0), null)).thenReturn(List.of());

        // quando
        RelatorioTransacoes relatorio = service.gerar(1L, null, null);

        // entao
        assertEquals(LocalDate.of(2026, 11, 1), relatorio.getPeriodo().getInicio());
        assertEquals(LocalDate.of(2026, 11, 30), relatorio.getPeriodo().getFim());
        assertEquals(1L, relatorio.getUsuarioId());
    }

    @Test
    void ordenaDaMaisAntigaParaAMaisNovaMesmoComOBancoDevolvendoAoContrario() {
        // o banco devolve da mais nova para a mais antiga
        Transacao dia10 = transacao(3L, LocalDateTime.of(2026, 11, 10, 9, 0));
        Transacao dia5 = transacao(2L, LocalDateTime.of(2026, 11, 5, 18, 30));
        Transacao dia5MesmoHorario = transacao(1L, LocalDateTime.of(2026, 11, 5, 18, 30));
        when(transacaoRepositoryPort.listarPorUsuario(any(), any(), any(), any()))
                .thenReturn(List.of(dia10, dia5, dia5MesmoHorario));
        cotacoesDisponiveis();

        RelatorioTransacoes relatorio = service.gerar(1L, null, null);

        // no mesmo horario, o menor id vem primeiro
        assertEquals(List.of(1L, 2L, 3L), relatorio.getTransacoes().stream().map(Transacao::getId).toList());
    }

    @Test
    void completaAsCotacoesQueFaltamPraOArquivoTrazerOValorConvertido() {
        Transacao semCotacao = transacao(1L, LocalDateTime.of(2026, 11, 5, 10, 0));
        when(transacaoRepositoryPort.listarPorUsuario(any(), any(), any(), any())).thenReturn(List.of(semCotacao));
        cotacoesDisponiveis();

        RelatorioTransacoes relatorio = service.gerar(1L, null, null);

        assertEquals(new BigDecimal("5.4321"), relatorio.getTransacoes().get(0).getTaxaDolar());
        assertEquals(new BigDecimal("6.1234"), relatorio.getTransacoes().get(0).getTaxaEuro());
    }

    @Test
    void inicioDepoisDoFimERecusadoSemConsultarOBanco() {
        assertThrows(ParametroInvalidoException.class,
                () -> service.gerar(1L, LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 1)));

        verify(transacaoRepositoryPort, never()).listarPorUsuario(any(), any(), any(), any());
    }

    private void cotacoesDisponiveis() {
        when(cambioPort.buscarCotacaoAtual("USD"))
                .thenReturn(new Cotacao("USD", new BigDecimal("5.4321"), LocalDate.of(2026, 11, 13)));
        when(cambioPort.buscarCotacaoAtual("EUR"))
                .thenReturn(new Cotacao("EUR", new BigDecimal("6.1234"), LocalDate.of(2026, 11, 13)));
    }

    private Transacao transacao(Long id, LocalDateTime dataHora) {
        Transacao transacao = new Transacao();
        transacao.setId(id);
        transacao.setUsuarioId(1L);
        transacao.setTipo(TipoTransacao.DEPOSITO);
        transacao.setValor(new BigDecimal("10.00"));
        transacao.setDataHora(dataHora);
        transacao.aprovar();
        return transacao;
    }
}
