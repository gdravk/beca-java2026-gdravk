package br.com.gabrieloliveira.usuarios.domain.exception;

// email ou senha errados na hora de conferir quem e o usuario (seguranca, pedido 6)
// vira 401 (unauthorized) no GlobalExceptionHandler
// a mensagem e sempre a mesma, sem dizer se o erro foi no email ou na senha:
// assim ninguem consegue descobrir quais emails estao cadastrados testando um por um
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("email ou senha invalidos");
    }
}
