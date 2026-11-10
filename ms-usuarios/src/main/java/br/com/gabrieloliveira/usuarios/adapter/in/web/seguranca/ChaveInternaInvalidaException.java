package br.com.gabrieloliveira.usuarios.adapter.in.web.seguranca;

// alguem chamou o POST /auth/verificar sem a chave interna, ou com a chave errada
// essa rota e so para o ms2: sem a chave a resposta e 403 e a senha nem chega a ser conferida,
// entao ninguem de fora consegue usar a rota para adivinhar senhas na tentativa e erro
public class ChaveInternaInvalidaException extends RuntimeException {

    public ChaveInternaInvalidaException() {
        super("esta rota e de uso interno entre os servicos (falta a chave interna ou ela esta errada)");
    }
}
