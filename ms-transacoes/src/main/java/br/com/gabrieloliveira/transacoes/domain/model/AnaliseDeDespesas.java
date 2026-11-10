package br.com.gabrieloliveira.transacoes.domain.model;

import java.math.BigDecimal;
import java.util.List;

// o resultado de uma analise de despesas: o periodo usado, o total geral e as linhas do resumo
// o <T> e o tipo das linhas (DespesaPorCategoria, DespesaPorDia ou DespesaPorMes); assim uma classe so
// serve para os tres resumos (isso se chama "classe generica", igual a List<String>)
public class AnaliseDeDespesas<T> {

    private final Long usuarioId;
    private final Periodo periodo;
    private final int quantidadeDespesas;
    private final BigDecimal totalGeral;
    private final List<T> itens;

    public AnaliseDeDespesas(Long usuarioId, Periodo periodo, int quantidadeDespesas, BigDecimal totalGeral,
                             List<T> itens) {
        this.usuarioId = usuarioId;
        this.periodo = periodo;
        this.quantidadeDespesas = quantidadeDespesas;
        this.totalGeral = totalGeral;
        this.itens = itens;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public int getQuantidadeDespesas() {
        return quantidadeDespesas;
    }

    public BigDecimal getTotalGeral() {
        return totalGeral;
    }

    public List<T> getItens() {
        return itens;
    }
}
