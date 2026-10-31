package br.com.gabrieloliveira.transacoes.domain.model;

// toda transacao termina em um desses dois estados, sempre gravada no banco
// REJECTED vem acompanhado do motivo (campo motivoRejeicao da Transacao)
public enum StatusTransacao {

    APPROVED,
    REJECTED
}
