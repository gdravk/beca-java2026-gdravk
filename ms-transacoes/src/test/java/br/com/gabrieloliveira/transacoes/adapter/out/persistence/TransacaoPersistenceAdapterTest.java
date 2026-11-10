package br.com.gabrieloliveira.transacoes.adapter.out.persistence;

import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.StatusTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// teste de repositorio: roda a consulta listarDespesas de verdade, num banco h2 em memoria
// (a regra de "transacao estornada nao conta" so existe dentro da query, entao precisa de um banco para testar)
//
// o @DataJpaTest sobe so a parte do jpa (entidades, repositorios e um banco em memoria), sem controllers,
// sem api mock e sem brasilapi; cada teste roda numa transacao que e desfeita no fim, entao um teste
// nao enxerga os dados do outro
// o @Import traz o adaptador e o mapper, que nao sao repositorios e por isso nao viriam sozinhos
@DataJpaTest
@Import({TransacaoPersistenceAdapter.class, TransacaoPersistenceMapper.class})
class TransacaoPersistenceAdapterTest {

    private static final Long USUARIO = 1L;
    private static final LocalDateTime INICIO_NOVEMBRO = LocalDateTime.of(2026, 11, 1, 0, 0);
    private static final LocalDateTime INICIO_DEZEMBRO = LocalDateTime.of(2026, 12, 1, 0, 0);

    @Autowired
    private TransacaoPersistenceAdapter adapter;

    @Test
    void compraEstornadaNaoContaComoDespesaEOEstornoTambemNao() {
        Transacao compra = adapter.salvar(transacao(TipoTransacao.COMPRA, "40.00", 3, StatusTransacao.APPROVED, null));
        // o estorno de uma compra e um DEPOSITO ligado a ela
        adapter.salvar(transacao(TipoTransacao.DEPOSITO, "40.00", 3, StatusTransacao.APPROVED, compra.getId()));

        List<Transacao> despesas = adapter.listarDespesas(USUARIO, INICIO_NOVEMBRO, INICIO_DEZEMBRO);

        assertTrue(despesas.isEmpty());
    }

    @Test
    void estornoDeDepositoViraRetiradaMasNaoEDespesa() {
        Transacao deposito = adapter.salvar(transacao(TipoTransacao.DEPOSITO, "300.00", 1, StatusTransacao.APPROVED, null));
        adapter.salvar(transacao(TipoTransacao.RETIRADA, "300.00", 1, StatusTransacao.APPROVED, deposito.getId()));

        assertTrue(adapter.listarDespesas(USUARIO, INICIO_NOVEMBRO, INICIO_DEZEMBRO).isEmpty());
    }

    @Test
    void estornoRejeitadoNaoDesfazACompraEElaContinuaContando() {
        // o estorno foi REJEITADO (sem saldo, por exemplo), entao o dinheiro nao voltou e a compra ainda e despesa
        Transacao compra = adapter.salvar(transacao(TipoTransacao.COMPRA, "40.00", 3, StatusTransacao.APPROVED, null));
        adapter.salvar(transacao(TipoTransacao.DEPOSITO, "40.00", 3, StatusTransacao.REJECTED, compra.getId()));

        List<Transacao> despesas = adapter.listarDespesas(USUARIO, INICIO_NOVEMBRO, INICIO_DEZEMBRO);

        assertEquals(1, despesas.size());
        assertEquals(compra.getId(), despesas.get(0).getId());
    }

    @Test
    void trazSoSaidasAprovadasDoUsuarioNoPeriodoEmOrdemDeData() {
        Transacao compra = adapter.salvar(transacao(TipoTransacao.COMPRA, "50.00", 5, StatusTransacao.APPROVED, null));
        Transacao retirada = adapter.salvar(transacao(TipoTransacao.RETIRADA, "100.00", 2, StatusTransacao.APPROVED, null));
        Transacao transferencia = adapter.salvar(
                transacao(TipoTransacao.TRANSFERENCIA, "60.00", 4, StatusTransacao.APPROVED, null));
        // nada disto pode aparecer:
        adapter.salvar(transacao(TipoTransacao.DEPOSITO, "1000.00", 2, StatusTransacao.APPROVED, null));
        adapter.salvar(transacao(TipoTransacao.COMPRA, "999.00", 2, StatusTransacao.REJECTED, null));
        Transacao deOutroUsuario = transacao(TipoTransacao.COMPRA, "70.00", 2, StatusTransacao.APPROVED, null);
        deOutroUsuario.setUsuarioId(2L);
        adapter.salvar(deOutroUsuario);
        Transacao foraDoPeriodo = transacao(TipoTransacao.COMPRA, "5.00", 1, StatusTransacao.APPROVED, null);
        foraDoPeriodo.setDataHora(INICIO_DEZEMBRO);
        adapter.salvar(foraDoPeriodo);

        List<Transacao> despesas = adapter.listarDespesas(USUARIO, INICIO_NOVEMBRO, INICIO_DEZEMBRO);

        List<Long> ids = new ArrayList<>();
        for (Transacao despesa : despesas) {
            ids.add(despesa.getId());
        }
        // em ordem de data: retirada (dia 2), transferencia (dia 4), compra (dia 5)
        assertEquals(List.of(retirada.getId(), transferencia.getId(), compra.getId()), ids);
    }

    private Transacao transacao(TipoTransacao tipo, String valor, int diaDeNovembro, StatusTransacao status,
                                Long transacaoOriginalId) {
        Transacao transacao = new Transacao();
        transacao.setUsuarioId(USUARIO);
        transacao.setTipo(tipo);
        transacao.setValor(new BigDecimal(valor));
        transacao.setCategoria(Categoria.OUTROS);
        transacao.setDescricao("teste de repositorio");
        transacao.setDataHora(LocalDateTime.of(2026, 11, diaDeNovembro, 12, 0));
        transacao.setStatus(status);
        transacao.setTransacaoOriginalId(transacaoOriginalId);
        return transacao;
    }
}
