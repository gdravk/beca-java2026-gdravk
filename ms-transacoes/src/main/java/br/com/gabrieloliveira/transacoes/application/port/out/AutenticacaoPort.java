package br.com.gabrieloliveira.transacoes.application.port.out;

import java.util.Optional;

// porta de saida da seguranca: pergunta "para alguem" se o email e a senha estao certos
// hoje quem responde e o ms1 (Ms1AutenticacaoAdapter, que chama o POST /auth/verificar): os usuarios
// moram no ms1, e o ms2 nao tem tabela de usuarios nem guarda senha nenhuma
public interface AutenticacaoPort {

    // devolve o id do usuario quando o email e a senha estao certos, e vazio quando estao errados
    // lanca ServicoExternoIndisponivelException quando nao da para perguntar (ms1 fora do ar)
    Optional<Long> verificar(String email, String senha);
}
