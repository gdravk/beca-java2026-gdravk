package br.com.gabrieloliveira.transacoes.domain.exception;

// lancada quando o pedido conflita com o estado atual (ex: estornar uma transacao ja estornada); vira 409
// rejeicao por saldo ou limite nao usa esta excecao: nesses casos a transacao e gravada como REJECTED
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
