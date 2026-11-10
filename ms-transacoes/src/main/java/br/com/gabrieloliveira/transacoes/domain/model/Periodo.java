package br.com.gabrieloliveira.transacoes.domain.model;

import br.com.gabrieloliveira.transacoes.domain.exception.ParametroInvalidoException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

// um intervalo de datas fechado nas duas pontas: de "inicio" ate "fim", com os dois dias inteiros
// usado na analise de despesas (pedido 5) e no relatorio (pedido 7)
//
// regra do PO: quando o periodo nao e informado, vale o mes atual
// o metodo "resolver" abaixo decide o periodo final a partir do que o cliente mandou:
// - nada informado: do dia 1 ao ultimo dia do mes atual
// - so o inicio: do inicio ate o ultimo dia do mes atual
// - so o fim: do dia 1 do mes do fim ate o fim
// - os dois: exatamente o que veio
// em todos os casos, o inicio nao pode ser depois do fim (vira 400)
public class Periodo {

    private final LocalDate inicio;
    private final LocalDate fim;

    private Periodo(LocalDate inicio, LocalDate fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    // o "hoje" vem de fora (e nao de um LocalDate.now() aqui dentro) para os testes conseguirem fixar a data
    public static Periodo resolver(LocalDate inicioInformado, LocalDate fimInformado, LocalDate hoje) {
        // o YearMonth representa so o ano e o mes (2026-11) e sabe qual e o primeiro e o ultimo dia dele
        // (o atEndOfMonth ja cuida dos meses de 30 e 31 dias e do fevereiro de ano bissexto)
        YearMonth mesAtual = YearMonth.from(hoje);

        LocalDate inicio = inicioInformado;
        LocalDate fim = fimInformado;

        if (inicio == null && fim == null) {
            inicio = mesAtual.atDay(1);
            fim = mesAtual.atEndOfMonth();
        } else if (inicio == null) {
            inicio = YearMonth.from(fim).atDay(1);
        } else if (fim == null) {
            fim = mesAtual.atEndOfMonth();
        }

        if (inicio.isAfter(fim)) {
            throw new ParametroInvalidoException("a data inicial (" + inicio + ") nao pode ser depois da data final ("
                    + fim + ")");
        }

        return new Periodo(inicio, fim);
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFim() {
        return fim;
    }

    // primeiro instante do periodo: 00:00 do dia inicial
    public LocalDateTime inicioDoPrimeiroDia() {
        return inicio.atStartOfDay();
    }

    // o banco compara com "menor que", entao usamos 00:00 do dia seguinte ao fim
    // assim uma compra feita as 23:59 do ultimo dia ainda entra no periodo
    public LocalDateTime inicioDoDiaSeguinteAoFim() {
        return fim.plusDays(1).atStartOfDay();
    }
}
