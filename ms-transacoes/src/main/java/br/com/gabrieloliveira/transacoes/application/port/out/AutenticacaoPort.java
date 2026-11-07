package br.com.gabrieloliveira.transacoes.application.port.out;

import java.util.Optional;

// porta de saida da seguranca: pergunta "pra alguem" se o email e a senha estao certos
// hoje quem responde e o ms1 (Ms1AutenticacaoAdapter, que chama POST /auth/verificar)
// os usuarios moram no ms1; o ms2 nao tem tabela de usuarios nem guarda senha nenhuma
public interface AutenticacaoPort {

    // devolve o id do usuario quando o email e a senha estao certos
    // devolve vazio quando estao errados
    // lanca ServicoExternoIndisponivelException quando nao da pra perguntar (ms1 fora do ar)
    Optional<Long> verificar(String email, String senha);
}
