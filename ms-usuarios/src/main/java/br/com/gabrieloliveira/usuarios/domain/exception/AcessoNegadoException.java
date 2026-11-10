package br.com.gabrieloliveira.usuarios.domain.exception;

// a pessoa esta logada, mas tentou mexer em algo que nao e dela (excluir outro usuario, por exemplo)
// o GlobalExceptionHandler transforma em 403 (forbidden). a diferenca entre 401 e 403:
// - 401: "nao sei quem voce e" (sem login ou com login errado)
// - 403: "sei quem voce e, mas voce nao pode fazer isso"
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
