package br.com.gabrieloliveira.transacoes.domain.model;

// os quatro tipos de transacao aceitos pelo produto
public enum TipoTransacao {

    DEPOSITO,
    RETIRADA,
    TRANSFERENCIA,
    COMPRA;

    // saida = dinheiro saindo da conta; so esses tipos passam pela checagem de saldo e limites
    public boolean isSaida() {
        return this == RETIRADA || this == TRANSFERENCIA || this == COMPRA;
    }

    // tipo usado no estorno: devolve o dinheiro de uma saida, ou retira de volta um deposito
    public TipoTransacao inverso() {
        if (this == DEPOSITO) {
            return RETIRADA;
        }
        return DEPOSITO;
    }
}
