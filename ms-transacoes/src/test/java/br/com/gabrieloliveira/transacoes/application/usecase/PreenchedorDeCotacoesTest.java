package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.out.CambioPort;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import br.com.gabrieloliveira.transacoes.domain.model.Cotacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// testes do preenchedor de cotacoes: ele completa so as taxas que faltam e nunca deixa o cambio quebrar nada
@ExtendWith(MockitoExtension.class)
class PreenchedorDeCotacoesTest {

    private static final Cotacao DOLAR = new Cotacao("USD", new BigDecimal("5.4778"), LocalDate.of(2026, 11, 4));
    private static final Cotacao EURO = new Cotacao("EUR", new BigDecimal("6.1549"), LocalDate.of(2026, 11, 4));

    @Mock
    private CambioPort cambioPort;

    private PreenchedorDeCotacoes preenchedor;

    @BeforeEach
    void setUp() {
        preenchedor = new PreenchedorDeCotacoes(cambioPort);
    }

    @Test
    void transacaoSemTaxaRecebeDolarEEuroComAData() {
        when(cambioPort.buscarCotacaoAtual("USD")).thenReturn(DOLAR);
        when(cambioPort.buscarCotacaoAtual("EUR")).thenReturn(EURO);
        Transacao transacao = new Transacao();

        preenchedor.preencherFaltantes(transacao);

        assertEquals(new BigDecimal("5.4778"), transacao.getTaxaDolar());
        assertEquals(LocalDate.of(2026, 11, 4), transacao.getDataTaxaDolar());
        assertEquals(new BigDecimal("6.1549"), transacao.getTaxaEuro());
        assertEquals(LocalDate.of(2026, 11, 4), transacao.getDataTaxaEuro());
    }

    @Test
    void transacaoQueJaTemAsTaxasNaoConsultaOCambio() {
        // as taxas guardadas na criacao valem pra sempre: nao podem ser trocadas pela de hoje
        Transacao transacao = transacaoComTaxas();

        preenchedor.preencherFaltantes(transacao);

        assertEquals(new BigDecimal("5.00"), transacao.getTaxaDolar());
        verify(cambioPort, never()).buscarCotacaoAtual(anyString());
    }

    @Test
    void listaComVariasSemTaxaChamaOCambioSoUmaVezPorMoeda() {
        when(cambioPort.buscarCotacaoAtual("USD")).thenReturn(DOLAR);
        when(cambioPort.buscarCotacaoAtual("EUR")).thenReturn(EURO);
        Transacao semTaxa1 = new Transacao();
        Transacao semTaxa2 = new Transacao();
        Transacao comTaxa = transacaoComTaxas();

        preenchedor.preencherFaltantes(List.of(semTaxa1, comTaxa, semTaxa2));

        verify(cambioPort, times(1)).buscarCotacaoAtual("USD");
        verify(cambioPort, times(1)).buscarCotacaoAtual("EUR");
        assertEquals(new BigDecimal("5.4778"), semTaxa1.getTaxaDolar());
        assertEquals(new BigDecimal("5.4778"), semTaxa2.getTaxaDolar());
        // a que ja tinha taxa continua com a dela
        assertEquals(new BigDecimal("5.00"), comTaxa.getTaxaDolar());
    }

    @Test
    void cambioForaDoArNaoQuebraEATransacaoSegueSemTaxa() {
        when(cambioPort.buscarCotacaoAtual("USD")).thenThrow(new ServicoExternoIndisponivelException("fora"));
        when(cambioPort.buscarCotacaoAtual("EUR")).thenThrow(new ServicoExternoIndisponivelException("fora"));
        Transacao transacao = new Transacao();

        // nao lanca excecao: o cambio e so informativo
        preenchedor.preencherFaltantes(transacao);

        assertNull(transacao.getTaxaDolar());
        assertNull(transacao.getTaxaEuro());
    }

    @Test
    void soUmaMoedaForaDoArAOutraAindaEPreenchida() {
        when(cambioPort.buscarCotacaoAtual("USD")).thenThrow(new ServicoExternoIndisponivelException("fora"));
        when(cambioPort.buscarCotacaoAtual("EUR")).thenReturn(EURO);
        Transacao transacao = new Transacao();

        preenchedor.preencherFaltantes(transacao);

        assertNull(transacao.getTaxaDolar());
        assertEquals(new BigDecimal("6.1549"), transacao.getTaxaEuro());
    }

    @Test
    void listaVaziaNaoConsultaNada() {
        preenchedor.preencherFaltantes(List.of());

        verify(cambioPort, never()).buscarCotacaoAtual(anyString());
    }

    private Transacao transacaoComTaxas() {
        Transacao transacao = new Transacao();
        transacao.setTaxaDolar(new BigDecimal("5.00"));
        transacao.setDataTaxaDolar(LocalDate.of(2026, 11, 1));
        transacao.setTaxaEuro(new BigDecimal("5.50"));
        transacao.setDataTaxaEuro(LocalDate.of(2026, 11, 1));
        return transacao;
    }
}
