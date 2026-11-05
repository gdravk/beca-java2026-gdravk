package br.com.gabrieloliveira.transacoes.domain.service;

import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorCategoria;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorDia;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorMes;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// regras da analise de despesas (pedido 5 do PO), em java puro
//
// o que e despesa (decisao do PO + do time):
// - so transacao APROVADA (rejeitada nunca tirou dinheiro da conta)
// - so transacao de saida: RETIRADA, TRANSFERENCIA enviada e COMPRA (deposito e entrada, nao e gasto)
// - estorno nao e despesa: o estorno de um deposito vira uma RETIRADA, mas ele so corrige um erro,
//   ninguem gastou aquele dinheiro
// - uma saida que foi estornada tambem nao conta, porque o dinheiro voltou
//   (essa ultima regra precisa olhar outras transacoes, entao quem cuida dela e a consulta no banco;
//   ver TransacaoRepositoryPort.listarDespesas)
//
// todas as somas sao em reais, que e a moeda em que a transacao foi registrada
// (dolar e euro sao so conversoes informativas)
public class AnalisadorDeDespesas {

    private static final BigDecimal CEM = new BigDecimal("100");
    private static final BigDecimal ZERO_REAIS = new BigDecimal("0.00");

    // aplica a definicao de despesa acima e devolve so as transacoes que contam
    public List<Transacao> somenteDespesas(List<Transacao> transacoes) {
        List<Transacao> despesas = new ArrayList<>();
        for (Transacao transacao : transacoes) {
            if (isDespesa(transacao)) {
                despesas.add(transacao);
            }
        }
        return despesas;
    }

    public boolean isDespesa(Transacao transacao) {
        return transacao.isAprovada()
                && transacao.getTipo().isSaida()
                && !transacao.isEstorno();
    }

    public BigDecimal somar(List<Transacao> despesas) {
        // comeca em 0.00 (e nao em 0) pra que, sem nenhuma despesa, o json mostre 0.00 como os outros valores
        BigDecimal total = ZERO_REAIS;
        for (Transacao despesa : despesas) {
            total = total.add(despesa.getValor());
        }
        return total;
    }

    // resumo por categoria, da que mais gastou pra que menos gastou
    public List<DespesaPorCategoria> agruparPorCategoria(List<Transacao> despesas) {
        BigDecimal totalGeral = somar(despesas);

        // EnumMap e um Map feito pra chaves que sao enum: mais leve e mantem a ordem do enum
        Map<Categoria, BigDecimal> totais = new EnumMap<>(Categoria.class);
        Map<Categoria, Integer> quantidades = new EnumMap<>(Categoria.class);

        for (Transacao despesa : despesas) {
            // retirada e transferencia sem categoria ja sao gravadas como OUTROS na criacao;
            // o if e so uma garantia pra transacoes antigas que possam estar sem categoria
            Categoria categoria = despesa.getCategoria();
            if (categoria == null) {
                categoria = Categoria.OUTROS;
            }
            // getOrDefault: se a categoria ainda nao esta no mapa, comeca do zero
            totais.put(categoria, totais.getOrDefault(categoria, BigDecimal.ZERO).add(despesa.getValor()));
            quantidades.put(categoria, quantidades.getOrDefault(categoria, 0) + 1);
        }

        List<DespesaPorCategoria> resumo = new ArrayList<>();
        for (Map.Entry<Categoria, BigDecimal> item : totais.entrySet()) {
            Categoria categoria = item.getKey();
            BigDecimal total = item.getValue();
            resumo.add(new DespesaPorCategoria(categoria, quantidades.get(categoria), total,
                    calcularPercentual(total, totalGeral)));
        }

        // ordena pelo total, do maior pro menor; em caso de empate, pela ordem do enum (pra sair sempre igual)
        resumo.sort(Comparator.comparing(DespesaPorCategoria::getTotal).reversed()
                .thenComparing(DespesaPorCategoria::getCategoria));
        return resumo;
    }

    // resumo por dia, do dia mais antigo pro mais novo
    // so aparecem os dias que tiveram despesa (dia sem gasto nao vira linha com zero)
    public List<DespesaPorDia> agruparPorDia(List<Transacao> despesas) {
        // TreeMap mantem as chaves ordenadas; como a chave e uma data, os dias ja saem em ordem
        Map<LocalDate, BigDecimal> totais = new TreeMap<>();
        Map<LocalDate, Integer> quantidades = new TreeMap<>();

        for (Transacao despesa : despesas) {
            LocalDate dia = despesa.getDataHora().toLocalDate();
            totais.put(dia, totais.getOrDefault(dia, BigDecimal.ZERO).add(despesa.getValor()));
            quantidades.put(dia, quantidades.getOrDefault(dia, 0) + 1);
        }

        List<DespesaPorDia> resumo = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> item : totais.entrySet()) {
            resumo.add(new DespesaPorDia(item.getKey(), quantidades.get(item.getKey()), item.getValue()));
        }
        return resumo;
    }

    // resumo por mes, do mes mais antigo pro mais novo
    // mesma ideia do resumo por dia, so que a chave e o ano-mes (YearMonth)
    public List<DespesaPorMes> agruparPorMes(List<Transacao> despesas) {
        Map<YearMonth, BigDecimal> totais = new TreeMap<>();
        Map<YearMonth, Integer> quantidades = new TreeMap<>();

        for (Transacao despesa : despesas) {
            YearMonth mes = YearMonth.from(despesa.getDataHora());
            totais.put(mes, totais.getOrDefault(mes, BigDecimal.ZERO).add(despesa.getValor()));
            quantidades.put(mes, quantidades.getOrDefault(mes, 0) + 1);
        }

        List<DespesaPorMes> resumo = new ArrayList<>();
        for (Map.Entry<YearMonth, BigDecimal> item : totais.entrySet()) {
            resumo.add(new DespesaPorMes(item.getKey(), quantidades.get(item.getKey()), item.getValue()));
        }
        return resumo;
    }

    // quanto "parte" representa de "todo", em porcentagem com 2 casas (ex: 37.50)
    // HALF_EVEN e o arredondamento "do banqueiro", o mesmo usado na conversao de moeda
    private BigDecimal calcularPercentual(BigDecimal parte, BigDecimal todo) {
        if (todo.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return parte.multiply(CEM).divide(todo, 2, RoundingMode.HALF_EVEN);
    }
}
