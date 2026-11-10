package br.com.gabrieloliveira.transacoes.domain.exception;

// lancada quando o pedido conflita com a situacao atual (estornar uma transacao ja estornada, por exemplo); vira 409
// uma rejeicao por saldo ou limite nao usa esta excecao: nesses casos a transacao e gravada como REJECTED
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
