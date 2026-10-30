package br.com.gabrieloliveira.usuarios.exception;

// erro nosso pra quando os dados ate estao no formato certo, mas quebram uma regra do sistema
// exemplo: tentar cadastrar um email que outro usuario ja usa
// o GlobalExceptionHandler pega ela e transforma em 409 (conflict)
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
