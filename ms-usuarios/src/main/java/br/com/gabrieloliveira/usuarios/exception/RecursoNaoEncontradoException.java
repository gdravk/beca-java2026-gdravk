package br.com.gabrieloliveira.usuarios.exception;

// erro nosso pra quando a pessoa pede algo que nao existe (ex: usuario com id 999)
// o GlobalExceptionHandler pega ela e transforma em 404
// estende RuntimeException pra nao precisar ficar colocando "throws" em todo metodo
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
