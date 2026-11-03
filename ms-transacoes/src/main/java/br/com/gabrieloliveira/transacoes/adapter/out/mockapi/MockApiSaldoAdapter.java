package br.com.gabrieloliveira.transacoes.adapter.out.mockapi;

import br.com.gabrieloliveira.transacoes.application.port.out.SaldoContaPort;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import br.com.gabrieloliveira.transacoes.domain.model.SaldoConta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Optional;

// adaptador de saida REAL do saldo: busca saldo e limites do usuario na api mock (mockapi.io) via http
// ele implementa a mesma porta (SaldoContaPort) que o adaptador falso implementava,
// entao o caso de uso CriarTransacaoService nao mudou nada: so trocamos a peca que fica "plugada" na porta
// (e isso que a arquitetura hexagonal promete: trocar o detalhe tecnico sem mexer no centro do sistema)
@Component
public class MockApiSaldoAdapter implements SaldoContaPort {

    // RestClient e o cliente http do spring: e com ele que o ms2 "chama" outra api, parecido com o postman
    private final RestClient restClient;

    // @Value le as propriedades do application.properties (que por sua vez le o MOCKAPI_BASE_URL do .env)
    public MockApiSaldoAdapter(@Value("${mockapi.base-url}") String baseUrl,
                               @Value("${mockapi.timeout-ms}") int timeoutMs) {
        // a "fabrica de requisicoes" e onde ficam os tempos maximos de espera:
        // - connectTimeout: quanto tempo esperar pra conseguir CONECTAR no servidor
        // - readTimeout: depois de conectado, quanto tempo esperar a RESPOSTA chegar
        // sem isso, se a api mock travar, a requisicao do usuario ficaria pendurada por muito tempo
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(timeoutMs));
        fabrica.setReadTimeout(Duration.ofMillis(timeoutMs));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(fabrica)
                .build();
    }

    @Override
    public Optional<SaldoConta> buscarPorUsuario(Long usuarioId) {
        ContaMockApiDTO[] contas;
        try {
            // GET {baseUrl}/accounts?userId=1
            // o {userId} e trocado pelo valor de usuarioId (o spring monta a url com seguranca, sem concatenar texto)
            // .retrieve() executa a requisicao e .body(...) converte o json da resposta pra uma lista (array) de contas
            contas = restClient.get()
                    .uri("/accounts?userId={userId}", usuarioId)
                    .retrieve()
                    .body(ContaMockApiDTO[].class);
        } catch (HttpClientErrorException.NotFound e) {
            // o mockapi responde 404 "Not found" quando o filtro nao encontra nenhuma conta
            // isso nao e falha do servico: so quer dizer que esse usuario nao tem conta cadastrada
            return Optional.empty();
        } catch (RestClientException e) {
            // qualquer outro problema (fora do ar, demorou mais que o timeout, erro 500, url errada...)
            // vira a nossa excecao de dominio; o caso de uso trata e rejeita a transacao com um motivo claro
            // (a regra do PO e: sem saldo confirmado, a transacao nunca e aprovada as cegas)
            throw new ServicoExternoIndisponivelException("nao foi possivel consultar a api de saldo", e);
        }

        if (contas == null) {
            return Optional.empty();
        }

        // o filtro do mockapi e por "contem", nao por "igual": ?userId=1 tambem traria as contas 10, 11, 21...
        // por isso conferimos aqui o userId exato, uma conta por vez
        for (ContaMockApiDTO conta : contas) {
            if (usuarioId.equals(conta.getUserId())) {
                return Optional.of(paraDominio(conta));
            }
        }
        return Optional.empty();
    }

    // traduz o json da api mock (ContaMockApiDTO) para o objeto do dominio (SaldoConta)
    private SaldoConta paraDominio(ContaMockApiDTO conta) {
        // se algum campo vier vazio no mockapi, as regras de saldo e limite nao teriam como ser aplicadas
        // (e dariam NullPointerException); entao tratamos como dado indisponivel, e a transacao e rejeitada com motivo
        if (conta.getSaldo() == null || conta.getLimiteDiario() == null || conta.getLimitePorTransacao() == null) {
            throw new ServicoExternoIndisponivelException(
                    "a conta do usuario " + conta.getUserId() + " esta incompleta na api de saldo");
        }
        return new SaldoConta(conta.getUserId(), conta.getSaldo(), conta.getLimiteDiario(), conta.getLimitePorTransacao());
    }
}
