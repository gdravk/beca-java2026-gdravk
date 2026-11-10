package br.com.gabrieloliveira.usuarios.domain.exception;

// erro de negocio para quando os dados estao no formato certo, mas quebram uma regra do sistema,
// como cadastrar um email que outro usuario ja usa
// o GlobalExceptionHandler, no adaptador web, pega esta excecao e transforma em 409 (conflict)
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
