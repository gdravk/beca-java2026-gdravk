package br.com.gabrieloliveira.transacoes.domain.model;

// toda transacao termina num destes dois estados e fica sempre gravada no banco
// o REJECTED vem acompanhado do motivo (o campo motivoRejeicao da Transacao)
public enum StatusTransacao {

    APPROVED,
    REJECTED
}
