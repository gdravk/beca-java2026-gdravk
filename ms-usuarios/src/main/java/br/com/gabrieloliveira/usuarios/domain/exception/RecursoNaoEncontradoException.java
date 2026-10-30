package br.com.gabrieloliveira.usuarios.domain.exception;

// erro de negocio pra quando a pessoa pede algo que nao existe (ex: usuario com id 999)
// o GlobalExceptionHandler (no adaptador web) pega ela e transforma em 404
// estende RuntimeException pra nao precisar ficar colocando "throws" em todo metodo
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
