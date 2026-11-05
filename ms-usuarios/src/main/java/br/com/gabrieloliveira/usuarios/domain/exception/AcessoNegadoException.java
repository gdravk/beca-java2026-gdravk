package br.com.gabrieloliveira.usuarios.domain.exception;

// o usuario esta logado, mas tentou mexer em algo que nao e dele (ex: excluir outro usuario)
// vira 403 (forbidden) no GlobalExceptionHandler
// diferenca entre 401 e 403:
// - 401: "nao sei quem voce e" (sem login ou login errado)
// - 403: "sei quem voce e, mas voce nao pode fazer isso"
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
