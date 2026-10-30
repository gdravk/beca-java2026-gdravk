package br.com.gabrieloliveira.usuarios.domain.exception;

// erro de negocio pra quando os dados ate estao no formato certo, mas quebram uma regra do sistema
// exemplo: tentar cadastrar um email que outro usuario ja usa
// o GlobalExceptionHandler (no adaptador web) pega ela e transforma em 409 (conflict)
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
