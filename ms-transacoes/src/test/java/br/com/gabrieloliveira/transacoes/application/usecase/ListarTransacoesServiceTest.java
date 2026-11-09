package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.out.CambioPort;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.exception.ParametroInvalidoException;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarTransacoesServiceTest {

    @Mock
    private TransacaoRepositoryPort transacaoRepositoryPort;

    @Mock
    private CambioPort cambioPort;

    private ListarTransacoesService service;

    @BeforeEach
    void setUp() {
        service = new ListarTransacoesService(transacaoRepositoryPort, new PreenchedorDeCotacoes(cambioPort));
    }

    @Test
    void dataInicialDepoisDaFinalERecusada() {
        assertThrows(ParametroInvalidoException.class, () -> service.listar(1L,
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 1), null));

        // nem chega a consultar o banco
        verify(transacaoRepositoryPort, never()).listarPorUsuario(any(), any(), any(), any());
    }

    @Test
    void oDiaFinalEntraInteiroNaConsulta() {
        // dado: periodo de 01/11 a 05/11, so compras
        // o repositorio trata o fim como exclusivo, entao tem que receber 06/11 00:00 para o dia 05 vir completo
        List<Transacao> doBanco = List.of();
        when(transacaoRepositoryPort.listarPorUsuario(1L, LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 11, 6, 0, 0), TipoTransacao.COMPRA)).thenReturn(doBanco);

        // quando
        List<Transacao> resultado = service.listar(1L, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 5),
                TipoTransacao.COMPRA);

        // entao
        assertSame(doBanco, resultado);
    }

    @Test
    void semDatasConsultaTodoOHistorico() {
        List<Transacao> doBanco = List.of();
        when(transacaoRepositoryPort.listarPorUsuario(1L, LocalDateTime.of(1900, 1, 1, 0, 0),
                LocalDateTime.of(9999, 12, 31, 0, 0), null)).thenReturn(doBanco);

        List<Transacao> resultado = service.listar(1L, null, null, null);

        assertSame(doBanco, resultado);
    }
}
