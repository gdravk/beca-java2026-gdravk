package br.com.gabrieloliveira.usuarios.adapter.in.web.seguranca;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

// confere a chave interna que o ms2 manda no cabecalho X-Chave-Interna quando chama o POST /auth/verificar
// (ajuste da revisao do professor: sem ela, a rota era publica e servia para adivinhar senhas)
//
// a chave e um "segredo compartilhado": o mesmo valor fica na variavel MS1_CHAVE_INTERNA dos dois servicos
// ela vem do .env (nunca fica escrita no codigo) e precisa ter pelo menos 16 caracteres
@Component
public class VerificadorDeChaveInterna {

    // nome do cabecalho http onde a chave vai
    public static final String CABECALHO = "X-Chave-Interna";

    private static final int TAMANHO_MINIMO = 16;

    // o valor de exemplo do .env.example, que esta no github para todo mundo ver
    // se alguem copiar o .env.example e esquecer de trocar a chave, qualquer pessoa que leu o repositorio
    // saberia a chave (sugestao do professor na revisao de 06/11)
    static final String CHAVE_DE_EXEMPLO = "troque_por_uma_chave_aleatoria";

    private final byte[] chaveEsperada;

    public VerificadorDeChaveInterna(@Value("${seguranca.chave-interna}") String chave) {
        // sem chave, com uma chave curta demais ou com a de exemplo, a aplicacao nem sobe: e melhor descobrir
        // o erro de configuracao na hora de rodar do que deixar a rota protegida por uma chave fraca
        if (chave == null || chave.length() < TAMANHO_MINIMO) {
            throw new IllegalStateException("a variavel MS1_CHAVE_INTERNA precisa ter pelo menos "
                    + TAMANHO_MINIMO + " caracteres");
        }
        if (chave.equals(CHAVE_DE_EXEMPLO)) {
            throw new IllegalStateException("a variavel MS1_CHAVE_INTERNA ainda esta com o valor de exemplo do "
                    + ".env.example; troque por uma chave aleatoria sua");
        }
        this.chaveEsperada = chave.getBytes(StandardCharsets.UTF_8);
    }

    public void verificar(String chaveRecebida) {
        if (chaveRecebida == null) {
            throw new ChaveInternaInvalidaException();
        }
        // o MessageDigest.isEqual compara sempre todos os bytes, demore o que demorar
        // um equals comum para no primeiro caractere diferente, e medindo esse tempo daria para descobrir
        // a chave aos poucos (isso se chama "ataque de tempo")
        byte[] recebida = chaveRecebida.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(chaveEsperada, recebida)) {
            throw new ChaveInternaInvalidaException();
        }
    }
}
