package br.com.gabrieloliveira.transacoes.adapter.out.ms1;

import br.com.gabrieloliveira.transacoes.application.port.out.AutenticacaoPort;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Optional;

// adaptador de saida que confere o login perguntando ao ms1 (POST /auth/verificar)
// e aqui que os dois microsservicos "conversam": o ms2 nao sabe as senhas, quem sabe e o ms1
//
// a url do ms1 vem da variavel MS1_URL:
// - rodando no intellij: http://localhost:8081
// - no docker compose (pedido 9): http://ms-usuarios:8081 (o nome do container)
//
// cuidado com a senha: ela vai so no corpo da requisicao pro ms1, nunca na url e nunca no log
@Component
public class Ms1AutenticacaoAdapter implements AutenticacaoPort {

    // nome do cabecalho onde vai a chave interna (o mesmo que o ms1 espera)
    private static final String CABECALHO_CHAVE_INTERNA = "X-Chave-Interna";

    private final RestClient restClient;

    // segredo compartilhado com o ms1 (variavel MS1_CHAVE_INTERNA do .env)
    // sem ele o ms1 recusa o POST /auth/verificar com 403 (ajuste da revisao do professor:
    // a rota deixou de ser aberta pra qualquer um testar senhas)
    private final String chaveInterna;

    public Ms1AutenticacaoAdapter(@Value("${ms1.base-url}") String baseUrl,
                                  @Value("${ms1.timeout-ms}") int timeoutMs,
                                  @Value("${ms1.chave-interna}") String chaveInterna) {
        this.chaveInterna = chaveInterna;
        // mesmos tempos maximos de espera usados na api mock: sem eles, com o ms1 travado,
        // toda requisicao do ms2 ficaria pendurada esperando
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(timeoutMs));
        fabrica.setReadTimeout(Duration.ofMillis(timeoutMs));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(fabrica)
                .build();
    }

    @Override
    public Optional<Long> verificar(String email, String senha) {
        UsuarioVerificadoMs1DTO resposta;
        try {
            resposta = restClient.post()
                    .uri("/auth/verificar")
                    .header(CABECALHO_CHAVE_INTERNA, chaveInterna)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CredenciaisMs1RequestDTO(email, senha))
                    .retrieve()
                    .body(UsuarioVerificadoMs1DTO.class);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest e) {
            // 401: email ou senha errados; 400: veio campo vazio
            // nos dois casos o ms1 funcionou, so o login que nao vale
            return Optional.empty();
        } catch (RestClientException e) {
            // ms1 fora do ar, demorou demais ou respondeu um erro inesperado (500...)
            // o 403 tambem cai aqui: quer dizer que a chave interna dos dois servicos nao bate,
            // um erro de configuracao nosso, e nao do usuario (por isso vira 503 e nao 401)
            // nao da pra saber quem e o usuario, entao ninguem entra (nunca libera "as cegas")
            throw new ServicoExternoIndisponivelException("nao foi possivel conferir o login no ms1", e);
        }

        if (resposta == null || resposta.getUsuarioId() == null) {
            throw new ServicoExternoIndisponivelException("o ms1 respondeu sem o id do usuario");
        }
        return Optional.of(resposta.getUsuarioId());
    }
}
