package br.com.gabrieloliveira.transacoes.domain.model;

import java.math.BigDecimal;
import java.util.List;

// o relatorio de transacoes de um usuario num periodo (pedido 7 do PO)
// guarda as transacoes (da mais antiga para a mais nova) e os totais do resumo
// e java puro: nao sabe nada de csv; quem transforma isso em arquivo e o adaptador web
public class RelatorioTransacoes {

    private final Long usuarioId;
    private final Periodo periodo;
    private final List<Transacao> transacoes;
    private final int quantidadeAprovadas;
    private final int quantidadeRejeitadas;
    private final BigDecimal totalDepositosAprovados;
    private final BigDecimal totalSaidasAprovadas;

    private RelatorioTransacoes(Long usuarioId, Periodo periodo, List<Transacao> transacoes,
                                int quantidadeAprovadas, int quantidadeRejeitadas,
                                BigDecimal totalDepositosAprovados, BigDecimal totalSaidasAprovadas) {
        this.usuarioId = usuarioId;
        this.periodo = periodo;
        this.transacoes = transacoes;
        this.quantidadeAprovadas = quantidadeAprovadas;
        this.quantidadeRejeitadas = quantidadeRejeitadas;
        this.totalDepositosAprovados = totalDepositosAprovados;
        this.totalSaidasAprovadas = totalSaidasAprovadas;
    }

    // monta o relatorio e ja calcula o resumo
    // so as transacoes APROVADAS entram nos totais em dinheiro: uma rejeitada nunca movimentou a conta
    // os estornos entram como o tipo que eles sao (estorno de compra e um DEPOSITO, por exemplo),
    // porque e assim que o dinheiro realmente se moveu
    public static RelatorioTransacoes montar(Long usuarioId, Periodo periodo, List<Transacao> transacoes) {
        int aprovadas = 0;
        int rejeitadas = 0;
        BigDecimal depositos = BigDecimal.ZERO;
        BigDecimal saidas = BigDecimal.ZERO;

        for (Transacao transacao : transacoes) {
            if (!transacao.isAprovada()) {
                rejeitadas++;
                continue;
            }
            aprovadas++;
            if (transacao.getTipo().isSaida()) {
                saidas = saidas.add(transacao.getValor());
            } else {
                depositos = depositos.add(transacao.getValor());
            }
        }

        // List.copyOf: o relatorio guarda uma copia que ninguem consegue alterar depois
        return new RelatorioTransacoes(usuarioId, periodo, List.copyOf(transacoes), aprovadas, rejeitadas,
                depositos, saidas);
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public List<Transacao> getTransacoes() {
        return transacoes;
    }

    public int getQuantidadeAprovadas() {
        return quantidadeAprovadas;
    }

    public int getQuantidadeRejeitadas() {
        return quantidadeRejeitadas;
    }

    public BigDecimal getTotalDepositosAprovados() {
        return totalDepositosAprovados;
    }

    public BigDecimal getTotalSaidasAprovadas() {
        return totalSaidasAprovadas;
    }
}
