package br.com.gabrieloliveira.transacoes.domain.service;

import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.SaldoConta;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// testes das regras que aprovam ou rejeitam uma transacao (fase 14 do guia)
// o validador e java puro, entao nao precisa de mockito nem de spring: e so criar e chamar
// cada teste segue o padrao "dado / quando / entao":
//   dado   -> monta a transacao e a conta
//   quando -> chama o metodo do validador
//   entao  -> confere o resultado (Optional vazio = passou na regra, Optional com texto = motivo da rejeicao)
class ValidadorDeTransacaoTest {

    private static final Long ANA = 1L;
    private static final Long BRUNO = 2L;

    private final ValidadorDeTransacao validador = new ValidadorDeTransacao();

    // ---------- regras dos dados da transacao ----------

    @Test
    void depositoValidoEAprovado() {
        Transacao deposito = transacao(TipoTransacao.DEPOSITO, "100.00");

        Optional<String> erro = validador.validarDados(deposito);

        assertTrue(erro.isEmpty());
    }

    @Test
    void valorZeroERejeitado() {
        Transacao deposito = transacao(TipoTransacao.DEPOSITO, "0.00");

        Optional<String> erro = validador.validarDados(deposito);

        assertEquals("o valor da transacao deve ser maior que zero", erro.orElseThrow());
    }

    @Test
    void valorNegativoERejeitado() {
        Transacao retirada = transacao(TipoTransacao.RETIRADA, "-10.00");

        Optional<String> erro = validador.validarDados(retirada);

        assertEquals("o valor da transacao deve ser maior que zero", erro.orElseThrow());
    }

    @Test
    void valorAusenteERejeitado() {
        Transacao deposito = transacao(TipoTransacao.DEPOSITO, null);

        Optional<String> erro = validador.validarDados(deposito);

        assertEquals("o valor da transacao deve ser maior que zero", erro.orElseThrow());
    }

    @Test
    void compraSemCategoriaERejeitada() {
        Transacao compra = transacao(TipoTransacao.COMPRA, "50.00");

        Optional<String> erro = validador.validarDados(compra);

        assertEquals("compra exige uma categoria", erro.orElseThrow());
    }

    @Test
    void compraComCategoriaPassa() {
        Transacao compra = transacao(TipoTransacao.COMPRA, "50.00");
        compra.setCategoria(Categoria.OUTROS);

        Optional<String> erro = validador.validarDados(compra);

        assertTrue(erro.isEmpty());
    }

    @Test
    void transferenciaSemDestinoERejeitada() {
        Transacao transferencia = transacao(TipoTransacao.TRANSFERENCIA, "30.00");

        Optional<String> erro = validador.validarDados(transferencia);

        assertEquals("transferencia exige o usuario de destino", erro.orElseThrow());
    }

    @Test
    void transferenciaParaSiMesmoERejeitada() {
        Transacao transferencia = transacao(TipoTransacao.TRANSFERENCIA, "30.00");
        transferencia.setUsuarioDestinoId(ANA);

        Optional<String> erro = validador.validarDados(transferencia);

        assertEquals("nao e permitido transferir para a propria conta", erro.orElseThrow());
    }

    @Test
    void transferenciaParaOutraPessoaPassa() {
        Transacao transferencia = transacao(TipoTransacao.TRANSFERENCIA, "30.00");
        transferencia.setUsuarioDestinoId(BRUNO);

        Optional<String> erro = validador.validarDados(transferencia);

        assertTrue(erro.isEmpty());
    }

    // ---------- regras de saldo e limites (so para saidas) ----------
    // conta padrao dos testes: saldo 1000, limite diario 800, limite por transacao 500

    @Test
    void retiradaDentroDoSaldoEDosLimitesEAprovada() {
        Transacao retirada = transacao(TipoTransacao.RETIRADA, "200.00");

        Optional<String> erro = validador.validarSaldoELimites(retirada, conta(), new BigDecimal("100.00"));

        assertTrue(erro.isEmpty());
    }

    @Test
    void retiradaMaiorQueOSaldoERejeitada() {
        SaldoConta contaComPoucoSaldo = new SaldoConta(ANA, new BigDecimal("50.00"),
                new BigDecimal("800.00"), new BigDecimal("500.00"));
        Transacao retirada = transacao(TipoTransacao.RETIRADA, "50.01");

        Optional<String> erro = validador.validarSaldoELimites(retirada, contaComPoucoSaldo, BigDecimal.ZERO);

        assertEquals("saldo insuficiente", erro.orElseThrow());
    }

    @Test
    void retiradaDoSaldoExatoEAprovada() {
        // a regra e "maior que o saldo", entao usar o saldo inteiro ainda pode
        SaldoConta conta = new SaldoConta(ANA, new BigDecimal("300.00"),
                new BigDecimal("800.00"), new BigDecimal("500.00"));
        Transacao retirada = transacao(TipoTransacao.RETIRADA, "300.00");

        Optional<String> erro = validador.validarSaldoELimites(retirada, conta, BigDecimal.ZERO);

        assertTrue(erro.isEmpty());
    }

    @Test
    void valorAcimaDoLimitePorTransacaoERejeitado() {
        Transacao compra = transacao(TipoTransacao.COMPRA, "500.01");

        Optional<String> erro = validador.validarSaldoELimites(compra, conta(), BigDecimal.ZERO);

        assertEquals("valor acima do limite por transacao (limite: 500.00)", erro.orElseThrow());
    }

    @Test
    void estouroDoLimiteDiarioERejeitado() {
        // ja gastou 700 hoje; com mais 150 vai para 850, passando do limite diario de 800
        Transacao retirada = transacao(TipoTransacao.RETIRADA, "150.00");

        Optional<String> erro = validador.validarSaldoELimites(retirada, conta(), new BigDecimal("700.00"));

        assertEquals("limite diario excedido (limite: 800.00, ja utilizado hoje: 700.00)", erro.orElseThrow());
    }

    @Test
    void chegarExatamenteNoLimiteDiarioEAprovado() {
        // 700 + 100 = 800: bate o limite, mas nao passa
        Transacao retirada = transacao(TipoTransacao.RETIRADA, "100.00");

        Optional<String> erro = validador.validarSaldoELimites(retirada, conta(), new BigDecimal("700.00"));

        assertTrue(erro.isEmpty());
    }

    // ---------- ajudantes para montar os dados dos testes ----------

    private Transacao transacao(TipoTransacao tipo, String valor) {
        Transacao transacao = new Transacao();
        transacao.setUsuarioId(ANA);
        transacao.setTipo(tipo);
        if (valor != null) {
            transacao.setValor(new BigDecimal(valor));
        }
        return transacao;
    }

    private SaldoConta conta() {
        return new SaldoConta(ANA, new BigDecimal("1000.00"), new BigDecimal("800.00"), new BigDecimal("500.00"));
    }
}
