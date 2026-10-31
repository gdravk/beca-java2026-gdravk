package br.com.gabrieloliveira.transacoes.domain.exception;

// lancada quando um parametro de consulta nao faz sentido (ex: data inicial depois da final); vira 400
public class ParametroInvalidoException extends RuntimeException {

    public ParametroInvalidoException(String mensagem) {
        super(mensagem);
    }
}
