package br.com.gabrieloliveira.usuarios.adapter.in.web.seguranca;

// alguem chamou o POST /auth/verificar sem a chave interna (ou com a chave errada)
// essa rota e so pro ms2 usar; sem a chave, a resposta e 403 e a senha nem chega a ser conferida
// assim ninguem de fora consegue usar a rota pra testar senhas por forca bruta
public class ChaveInternaInvalidaException extends RuntimeException {

    public ChaveInternaInvalidaException() {
        super("esta rota e de uso interno entre os servicos (falta a chave interna ou ela esta errada)");
    }
}
