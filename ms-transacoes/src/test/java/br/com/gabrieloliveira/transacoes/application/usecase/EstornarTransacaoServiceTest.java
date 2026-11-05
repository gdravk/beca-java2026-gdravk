package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.in.CriarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.exception.AcessoNegadoException;
import br.com.gabrieloliveira.transacoes.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.transacoes.domain.exception.RegraNegocioException;
import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// testes do estorno: as regras que ja existiam e a nova regra de seguranca (so o dono estorna)
@ExtendWith(MockitoExtension.class)
class EstornarTransacaoServiceTest {

    private static final Long DONO = 1L;
    private static final Long OUTRO_USUARIO = 2L;

    @Mock
    private TransacaoRepositoryPort transacaoRepositoryPort;

    @Mock
    private CriarTransacaoUseCase criarTransacaoUseCase;

    private EstornarTransacaoService service;

    @BeforeEach
    void setUp() {
        service = new EstornarTransacaoService(transacaoRepositoryPort, criarTransacaoUseCase);
    }

    @Test
    void donoEstornaUmaCompraEOEstornoViraDepositoLigadoAOriginal() {
        when(transacaoRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(compraAprovada()));
        when(transacaoRepositoryPort.existeEstornoAprovado(10L)).thenReturn(false);
        // o criar de mentira so devolve o que recebeu, pra conferirmos como o estorno foi montado
        when(criarTransacaoUseCase.criar(any(Transacao.class))).thenAnswer(chamada -> chamada.getArgument(0));

        Transacao estorno = service.estornar(10L, DONO);

        assertEquals(TipoTransacao.DEPOSITO, estorno.getTipo());
        assertEquals(new BigDecimal("50.00"), estorno.getValor());
        assertEquals(10L, estorno.getTransacaoOriginalId());
        assertEquals(DONO, estorno.getUsuarioId());
    }

    @Test
    void outroUsuarioNaoPodeEstornarERecebeAcessoNegado() {
        when(transacaoRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(compraAprovada()));

        AcessoNegadoException erro = assertThrows(AcessoNegadoException.class,
                () -> service.estornar(10L, OUTRO_USUARIO));
        assertEquals("voce so pode estornar as suas proprias transacoes", erro.getMessage());

        // nada foi criado
        verify(criarTransacaoUseCase, never()).criar(any());
    }

    @Test
    void transacaoInexistenteDa404() {
        when(transacaoRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.estornar(99L, DONO));
    }

    @Test
    void transacaoRejeitadaNaoPodeSerEstornada() {
        Transacao rejeitada = compraAprovada();
        rejeitada.rejeitar("saldo insuficiente");
        when(transacaoRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(rejeitada));

        assertThrows(RegraNegocioException.class, () -> service.estornar(10L, DONO));
    }

    @Test
    void transacaoJaEstornadaNaoPodeSerEstornadaDeNovo() {
        when(transacaoRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(compraAprovada()));
        when(transacaoRepositoryPort.existeEstornoAprovado(10L)).thenReturn(true);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> service.estornar(10L, DONO));
        assertEquals("essa transacao ja foi estornada", erro.getMessage());
    }

    private Transacao compraAprovada() {
        Transacao compra = new Transacao();
        compra.setId(10L);
        compra.setUsuarioId(DONO);
        compra.setTipo(TipoTransacao.COMPRA);
        compra.setValor(new BigDecimal("50.00"));
        compra.setCategoria(Categoria.LAZER);
        compra.aprovar();
        return compra;
    }
}
