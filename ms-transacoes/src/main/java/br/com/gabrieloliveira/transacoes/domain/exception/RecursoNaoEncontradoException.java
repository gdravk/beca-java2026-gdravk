package br.com.gabrieloliveira.transacoes.domain.exception;

// lancada quando o que foi pedido nao existe (uma transacao com id inexistente, por exemplo); vira 404
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
