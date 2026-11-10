package br.com.gabrieloliveira.transacoes.domain.exception;

// a pessoa esta logada, mas tentou ver ou mexer nos dados de outro usuario
// e a regra do pedido 6 do PO ("so eu vejo e crio as minhas transacoes"); vira 403 (forbidden)
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
