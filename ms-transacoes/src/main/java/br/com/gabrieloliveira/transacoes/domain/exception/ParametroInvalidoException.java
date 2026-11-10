package br.com.gabrieloliveira.transacoes.domain.exception;

// lancada quando um parametro de consulta nao faz sentido (data inicial depois da final, por exemplo); vira 400
public class ParametroInvalidoException extends RuntimeException {

    public ParametroInvalidoException(String mensagem) {
        super(mensagem);
    }
}
