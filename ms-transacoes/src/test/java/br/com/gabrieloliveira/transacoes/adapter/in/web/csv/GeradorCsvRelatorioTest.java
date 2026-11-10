package br.com.gabrieloliveira.transacoes.adapter.in.web.csv;

import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.Periodo;
import br.com.gabrieloliveira.transacoes.domain.model.RelatorioTransacoes;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// confere o arquivo csv do relatorio exatamente como o excel vai receber (pedido 7)
class GeradorCsvRelatorioTest {

    private static final Periodo NOVEMBRO =
            Periodo.resolver(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 15));
    private static final String CABECALHO = "Id;Data e hora;Tipo;Categoria;Descrição;Valor (R$);Status;"
            + "Motivo da rejeição;Usuário destino;Estorno da transação;Cotação do dólar (R$);Valor em dólar (US$);"
            + "Cotação do euro (R$);Valor em euro (€)";

    private final GeradorCsvRelatorio gerador = new GeradorCsvRelatorio();

    @Test
    void arquivoComecaComOBomDoUtf8ParaOExcelLerOsAcentos() {
        byte[] arquivo = gerador.gerar(RelatorioTransacoes.montar(1L, NOVEMBRO, List.of()));

        // EF BB BF = o BOM; sem esses 3 bytes, o excel em portugues quebra os acentos
        byte[] inicio = {arquivo[0], arquivo[1], arquivo[2]};
        assertArrayEquals(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, inicio);
    }

    @Test
    void compraAprovadaViraUmaLinhaNoFormatoDoExcelEmPortugues() {
        // dado: uma compra de 150,75, com o dolar a 5,4321 e o euro a 6,1234
        Transacao compra = transacao(10L, TipoTransacao.COMPRA, "150.75", "mercado do mês");
        compra.setCategoria(Categoria.ALIMENTACAO);
        compra.setTaxaDolar(new BigDecimal("5.4321"));
        compra.setTaxaEuro(new BigDecimal("6.1234"));
        compra.aprovar();

        // quando
        List<String> linhas = linhas(RelatorioTransacoes.montar(1L, NOVEMBRO, List.of(compra)));

        // entao: separador ";", virgula decimal, data dd/mm/aaaa, nomes com acento e o valor convertido
        // (150,75 / 5,4321 = 27,75 dolares e 150,75 / 6,1234 = 24,62 euros)
        assertEquals(CABECALHO, linhas.get(0));
        assertEquals("10;05/11/2026 14:30;Compra;Alimentação;mercado do mês;150,75;Aprovada;;;;5,4321;27,75;"
                + "6,1234;24,62", linhas.get(1));
    }

    @Test
    void transacaoRejeitadaTrazOMotivoESemCotacaoFicaComAsColunasDeCambioVazias() {
        Transacao retirada = transacao(11L, TipoTransacao.RETIRADA, "5000.00", "saque");
        retirada.setCategoria(Categoria.OUTROS);
        retirada.rejeitar("saldo insuficiente");

        List<String> linhas = linhas(RelatorioTransacoes.montar(1L, NOVEMBRO, List.of(retirada)));

        assertEquals("11;05/11/2026 14:30;Retirada;Outros;saque;5000,00;Rejeitada;saldo insuficiente;;;;;;",
                linhas.get(1));
    }

    @Test
    void transferenciaEEstornoMostramODestinoEATransacaoOriginal() {
        Transacao transferencia = transacao(12L, TipoTransacao.TRANSFERENCIA, "30", "para o Bruno");
        transferencia.setCategoria(Categoria.OUTROS);
        transferencia.setUsuarioDestinoId(2L);
        transferencia.aprovar();
        Transacao estorno = transacao(13L, TipoTransacao.DEPOSITO, "30", "estorno");
        estorno.setCategoria(Categoria.OUTROS);
        estorno.setTransacaoOriginalId(12L);
        estorno.aprovar();

        List<String> linhas = linhas(RelatorioTransacoes.montar(1L, NOVEMBRO, List.of(transferencia, estorno)));

        assertEquals("12;05/11/2026 14:30;Transferência;Outros;para o Bruno;30,00;Aprovada;;2;;;;;", linhas.get(1));
        assertEquals("13;05/11/2026 14:30;Depósito;Outros;estorno;30,00;Aprovada;;;12;;;;", linhas.get(2));
    }

    @Test
    void textoComPontoEVirgulaOuAspasVaiEntreAspasPraNaoQuebrarAsColunas() {
        Transacao compra = transacao(14L, TipoTransacao.COMPRA, "10", "presente \"especial\"; parcelado");
        compra.setCategoria(Categoria.LAZER);
        compra.aprovar();

        List<String> linhas = linhas(RelatorioTransacoes.montar(1L, NOVEMBRO, List.of(compra)));

        // cada aspa de dentro vira duas aspas, e o texto todo fica entre aspas
        assertTrue(linhas.get(1).contains(";\"presente \"\"especial\"\"; parcelado\";"), linhas.get(1));
    }

    @Test
    void descricaoQueComecaComSinalDeFormulaNaoViraFormulaNoExcel() {
        // csv injection: sem a protecao, o excel executaria isto como formula ao abrir o arquivo
        Transacao compra = transacao(15L, TipoTransacao.COMPRA, "10", "=HYPERLINK(\"http://site-malicioso\")");
        compra.setCategoria(Categoria.LAZER);
        compra.aprovar();

        List<String> linhas = linhas(RelatorioTransacoes.montar(1L, NOVEMBRO, List.of(compra)));

        // a aspa simples na frente faz o excel mostrar como texto
        assertTrue(linhas.get(1).contains(";\"'=HYPERLINK(\"\"http://site-malicioso\"\")\";"), linhas.get(1));
    }

    @Test
    void resumoNoFimComPeriodoQuantidadesETotais() {
        Transacao deposito = transacao(1L, TipoTransacao.DEPOSITO, "1000", "salário");
        deposito.setCategoria(Categoria.OUTROS);
        deposito.aprovar();
        Transacao compra = transacao(2L, TipoTransacao.COMPRA, "150.75", "mercado");
        compra.setCategoria(Categoria.ALIMENTACAO);
        compra.aprovar();
        Transacao rejeitada = transacao(3L, TipoTransacao.COMPRA, "9999", "tv");
        rejeitada.setCategoria(Categoria.LAZER);
        rejeitada.rejeitar("saldo insuficiente");

        List<String> linhas = linhas(RelatorioTransacoes.montar(1L, NOVEMBRO, List.of(deposito, compra, rejeitada)));

        // cabecalho + 3 transacoes + uma linha em branco + 7 linhas de resumo
        assertEquals("", linhas.get(4));
        assertEquals(List.of("Resumo",
                "Período;01/11/2026 a 30/11/2026",
                "Transações no período;3",
                "Aprovadas;2",
                "Rejeitadas;1",
                "Total de depósitos aprovados (R$);1000,00",
                "Total de saídas aprovadas (R$);150,75"), linhas.subList(5, 12));
    }

    @Test
    void todaLinhaTerminaComQuebraDeLinhaDoPadraoCsv() {
        String texto = new String(gerador.gerar(RelatorioTransacoes.montar(1L, NOVEMBRO, List.of())),
                StandardCharsets.UTF_8);

        assertTrue(texto.endsWith("\r\n"));
        // nenhuma quebra de linha "solta" (um \n sem o \r antes)
        assertEquals(texto.split("\n", -1).length, texto.split("\r\n", -1).length);
    }

    // le o arquivo como o excel leria: em utf-8, sem o BOM, uma linha por \r\n
    private List<String> linhas(RelatorioTransacoes relatorio) {
        String texto = new String(gerador.gerar(relatorio), StandardCharsets.UTF_8);
        return List.of(texto.substring(1).split("\r\n", -1));
    }

    private Transacao transacao(Long id, TipoTransacao tipo, String valor, String descricao) {
        Transacao transacao = new Transacao();
        transacao.setId(id);
        transacao.setUsuarioId(1L);
        transacao.setTipo(tipo);
        transacao.setValor(new BigDecimal(valor));
        transacao.setDescricao(descricao);
        transacao.setDataHora(LocalDateTime.of(2026, 11, 5, 14, 30));
        return transacao;
    }
}
