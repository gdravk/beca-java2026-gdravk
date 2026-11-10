package br.com.gabrieloliveira.transacoes.domain.exception;

// o ms1 respondeu que o email ou a senha estao errados (seguranca, pedido 6); vira 401 (unauthorized)
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("email ou senha invalidos");
    }
}
