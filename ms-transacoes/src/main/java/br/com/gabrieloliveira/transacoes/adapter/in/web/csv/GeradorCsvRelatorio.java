package br.com.gabrieloliveira.transacoes.adapter.in.web.csv;

import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.Cotacao;
import br.com.gabrieloliveira.transacoes.domain.model.RelatorioTransacoes;
import br.com.gabrieloliveira.transacoes.domain.model.StatusTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// transforma o relatorio num arquivo csv que abre certinho no excel em portugues (pedido 7 do PO)
// fica no adaptador web porque csv e um formato de saida da api, e nao uma regra de negocio
//
// os cuidados para o excel em portugues:
// - BOM no comeco do arquivo: 3 bytes invisiveis que avisam "este texto e utf-8"; sem ele o excel le o
//   arquivo em outra codificacao e cada letra com acento vira dois simbolos estranhos
// - separador ";": como o excel em portugues usa a virgula para separar os decimais, o separador de
//   colunas dele e o ponto e virgula
// - numeros com virgula (150,75): com ponto, o excel em portugues trata como texto e nao deixa somar
// - datas no formato dd/mm/aaaa, que o excel reconhece como data
// - quebra de linha \r\n, que e o padrao do csv (rfc 4180) e do windows
@Component
public class GeradorCsvRelatorio {

    private static final String SEPARADOR = ";";
    private static final String QUEBRA_DE_LINHA = "\r\n";
    // o BOM e o caractere de codigo FEFF; escrito em utf-8, ele vira os bytes EF BB BF
    // como ele e invisivel, fica escrito pelo codigo (\ uFEFF, sem o espaco) e nao colado direto no texto
    private static final String BOM = "\uFEFF";
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // a primeira coluna e "Id", e nao "ID": quando um arquivo comeca com as letras ID maiusculas, o excel
    // acha que e outro formato (SYLK) e mostra o erro "o formato do arquivo nao e valido"
    private static final List<String> CABECALHO = List.of(
            "Id", "Data e hora", "Tipo", "Categoria", "Descrição", "Valor (R$)", "Status", "Motivo da rejeição",
            "Usuário destino", "Estorno da transação", "Cotação do dólar (R$)", "Valor em dólar (US$)",
            "Cotação do euro (R$)", "Valor em euro (€)");

    public byte[] gerar(RelatorioTransacoes relatorio) {
        StringBuilder csv = new StringBuilder(BOM);
        escreverLinha(csv, CABECALHO);

        for (Transacao transacao : relatorio.getTransacoes()) {
            escreverLinha(csv, colunasDa(transacao));
        }

        escreverResumo(csv, relatorio);
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // uma linha por transacao, na mesma ordem do CABECALHO
    // o que nao se aplica fica vazio (o motivo da rejeicao numa transacao aprovada, por exemplo)
    private List<String> colunasDa(Transacao transacao) {
        List<String> colunas = new ArrayList<>();
        colunas.add(String.valueOf(transacao.getId()));
        colunas.add(transacao.getDataHora().format(DATA_HORA));
        colunas.add(nomeDoTipo(transacao.getTipo()));
        colunas.add(nomeDaCategoria(transacao.getCategoria()));
        colunas.add(texto(transacao.getDescricao()));
        colunas.add(dinheiro(transacao.getValor()));
        colunas.add(nomeDoStatus(transacao.getStatus()));
        colunas.add(texto(transacao.getMotivoRejeicao()));
        colunas.add(numeroOuVazio(transacao.getUsuarioDestinoId()));
        colunas.add(numeroOuVazio(transacao.getTransacaoOriginalId()));
        colunas.add(taxa(transacao.getTaxaDolar()));
        colunas.add(convertido(transacao.getValor(), transacao.getTaxaDolar()));
        colunas.add(taxa(transacao.getTaxaEuro()));
        colunas.add(convertido(transacao.getValor(), transacao.getTaxaEuro()));
        return colunas;
    }

    // o resumo fica no fim do arquivo, separado das transacoes por uma linha em branco
    // (no fim, e nao no comeco, para o cabecalho continuar na primeira linha e o filtro do excel funcionar)
    private void escreverResumo(StringBuilder csv, RelatorioTransacoes relatorio) {
        csv.append(QUEBRA_DE_LINHA);
        escreverLinha(csv, List.of("Resumo"));
        escreverLinha(csv, List.of("Período", relatorio.getPeriodo().getInicio().format(DATA) + " a "
                + relatorio.getPeriodo().getFim().format(DATA)));
        escreverLinha(csv, List.of("Transações no período", String.valueOf(relatorio.getTransacoes().size())));
        escreverLinha(csv, List.of("Aprovadas", String.valueOf(relatorio.getQuantidadeAprovadas())));
        escreverLinha(csv, List.of("Rejeitadas", String.valueOf(relatorio.getQuantidadeRejeitadas())));
        escreverLinha(csv, List.of("Total de depósitos aprovados (R$)",
                dinheiro(relatorio.getTotalDepositosAprovados())));
        escreverLinha(csv, List.of("Total de saídas aprovadas (R$)", dinheiro(relatorio.getTotalSaidasAprovadas())));
    }

    private void escreverLinha(StringBuilder csv, List<String> colunas) {
        List<String> protegidas = new ArrayList<>();
        for (String coluna : colunas) {
            protegidas.add(protegerSeparadores(coluna));
        }
        csv.append(String.join(SEPARADOR, protegidas)).append(QUEBRA_DE_LINHA);
    }

    // se o texto tiver ponto e virgula, aspas ou quebra de linha, ele vai entre aspas (senao quebraria as colunas),
    // e cada aspa de dentro vira duas aspas, que e o jeito do csv de escrever uma aspa dentro de um texto:
    // compra "especial"; parcelada  vira  "compra ""especial""; parcelada"
    private String protegerSeparadores(String valor) {
        if (valor.contains(SEPARADOR) || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    // texto livre, digitado pelo usuario (descricao) ou gerado pelo sistema (motivo)
    // seguranca: se o texto comecar com = + - @ (ou tab/enter), o excel trataria como FORMULA e executaria;
    // uma descricao "=HYPERLINK(...)" viraria um link perigoso, por exemplo (isso se chama "csv injection")
    // a aspa simples na frente faz o excel mostrar o texto como texto, sem executar nada
    private String texto(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "";
        }
        char primeiro = valor.charAt(0);
        if (primeiro == '=' || primeiro == '+' || primeiro == '-' || primeiro == '@'
                || primeiro == '\t' || primeiro == '\r') {
            return "'" + valor;
        }
        return valor;
    }

    // 150.75 vira "150,75" (sempre com 2 casas)
    private String dinheiro(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_EVEN).toPlainString().replace('.', ',');
    }

    // taxa de cambio com as casas que vieram da brasilapi (5.4321 vira "5,4321"); sem taxa, fica vazio
    private String taxa(BigDecimal taxa) {
        if (taxa == null) {
            return "";
        }
        return taxa.toPlainString().replace('.', ',');
    }

    // a mesma conta da api (Cotacao.converterDeReais), para o arquivo e o json mostrarem o mesmo valor
    private String convertido(BigDecimal valorEmReais, BigDecimal taxa) {
        if (taxa == null) {
            return "";
        }
        return dinheiro(new Cotacao(null, taxa, null).converterDeReais(valorEmReais));
    }

    private String numeroOuVazio(Long numero) {
        if (numero == null) {
            return "";
        }
        return String.valueOf(numero);
    }

    // nomes em portugues e com acento, para quem le a planilha
    // o switch sem "default" obriga a tratar todos os valores do enum: se um dia entrar um tipo novo,
    // o codigo nem compila ate alguem escrever o nome dele aqui
    private String nomeDoTipo(TipoTransacao tipo) {
        return switch (tipo) {
            case DEPOSITO -> "Depósito";
            case RETIRADA -> "Retirada";
            case TRANSFERENCIA -> "Transferência";
            case COMPRA -> "Compra";
        };
    }

    private String nomeDaCategoria(Categoria categoria) {
        if (categoria == null) {
            return "";
        }
        return switch (categoria) {
            case ALIMENTACAO -> "Alimentação";
            case TRANSPORTE -> "Transporte";
            case LAZER -> "Lazer";
            case SAUDE -> "Saúde";
            case EDUCACAO -> "Educação";
            case MORADIA -> "Moradia";
            case OUTROS -> "Outros";
        };
    }

    private String nomeDoStatus(StatusTransacao status) {
        return switch (status) {
            case APPROVED -> "Aprovada";
            case REJECTED -> "Rejeitada";
        };
    }
}
