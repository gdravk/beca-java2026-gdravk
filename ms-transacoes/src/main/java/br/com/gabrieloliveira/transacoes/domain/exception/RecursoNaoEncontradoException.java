package br.com.gabrieloliveira.transacoes.domain.exception;

// lancada quando o recurso pedido nao existe (ex: transacao com id inexistente); vira 404
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
