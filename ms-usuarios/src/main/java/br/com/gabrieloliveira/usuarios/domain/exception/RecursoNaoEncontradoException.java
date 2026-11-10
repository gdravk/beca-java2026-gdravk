package br.com.gabrieloliveira.usuarios.domain.exception;

// erro de negocio para quando a pessoa pede algo que nao existe (o usuario com id 999, por exemplo)
// o GlobalExceptionHandler, no adaptador web, pega esta excecao e transforma em 404
// ela estende RuntimeException para nao precisar colocar "throws" em todo metodo
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
