package br.com.gabrieloliveira.usuarios.application.port.in;

import br.com.gabrieloliveira.usuarios.domain.model.Usuario;

// porta de entrada da seguranca (pedido 6): confere se o email e a senha estao certos
// ela e usada em dois lugares:
// - no proprio ms1, a cada requisicao com login (autenticacao basica)
// - no POST /auth/verificar, que o ms2 chama para saber quem e o dono das transacoes
// devolve o usuario quando esta tudo certo, ou lanca CredenciaisInvalidasException
public interface VerificarCredenciaisUseCase {

    Usuario verificar(String email, String senha);
}
