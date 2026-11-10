package br.com.gabrieloliveira.transacoes.domain.model;

// os quatro tipos de transacao que o produto aceita
public enum TipoTransacao {

    DEPOSITO,
    RETIRADA,
    TRANSFERENCIA,
    COMPRA;

    // saida = dinheiro saindo da conta; so esses tipos passam pela conferencia de saldo e limites
    public boolean isSaida() {
        return this == RETIRADA || this == TRANSFERENCIA || this == COMPRA;
    }

    // o tipo usado no estorno: devolve o dinheiro de uma saida, ou tira de volta o de um deposito
    public TipoTransacao inverso() {
        if (this == DEPOSITO) {
            return RETIRADA;
        }
        return DEPOSITO;
    }
}
