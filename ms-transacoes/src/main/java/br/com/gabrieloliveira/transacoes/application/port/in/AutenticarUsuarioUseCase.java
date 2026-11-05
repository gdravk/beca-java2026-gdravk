package br.com.gabrieloliveira.transacoes.application.port.in;

// porta de entrada da seguranca do ms2: descobre quem e o usuario a partir do email e da senha
// devolve o id do usuario, ou lanca CredenciaisInvalidasException (401)
// se o ms1 estiver fora do ar, lanca ServicoExternoIndisponivelException
public interface AutenticarUsuarioUseCase {

    Long autenticar(String email, String senha);
}
