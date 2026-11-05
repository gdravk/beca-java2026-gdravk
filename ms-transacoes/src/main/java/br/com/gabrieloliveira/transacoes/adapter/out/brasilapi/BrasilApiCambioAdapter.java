package br.com.gabrieloliveira.transacoes.adapter.out.brasilapi;

import br.com.gabrieloliveira.transacoes.application.port.out.CambioPort;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import br.com.gabrieloliveira.transacoes.domain.model.Cotacao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

// adaptador de saida REAL do cambio: busca a cotacao do dolar e do euro na brasilapi via http
// implementa a mesma porta (CambioPort) que o adaptador falso implementava, entao o resto do sistema nao mudou
//
// como a cotacao e escolhida (decisao do time, registrada no epico do pedido 3 e no readme):
// - endpoint: GET /cambio/v1/cotacao/{moeda}/{data}
// - usa a cotacao de VENDA do ULTIMO boletim do dia (normalmente o "fechamento ptax", o mais recente)
// - comeca pelo dia de hoje (horario de brasilia) e, se a brasilapi ainda nao tiver cotacao pra esse dia,
//   volta um dia de cada vez, ate no maximo MAXIMO_DIAS_PARA_TRAS
@Component
public class BrasilApiCambioAdapter implements CambioPort {

    // fuso de brasilia: sem isso, o "hoje" seria o do relogio do servidor (que pode estar em outro fuso)
    private static final ZoneId FUSO_BRASIL = ZoneId.of("America/Sao_Paulo");

    // uma semana cobre fim de semana + feriado prolongado com folga
    private static final int MAXIMO_DIAS_PARA_TRAS = 7;

    // pedaco do corpo do erro 400 que a brasilapi manda quando nao deixa consultar o dia de hoje:
    // {"message":"Não é possível consultar a cotação do dia de hoje...","type":"today_date_error",...}
    // ela usa o horario utc, entao a noite no brasil ela ja considera que e o dia seguinte
    private static final String ERRO_DIA_DE_HOJE = "today_date_error";

    private final RestClient restClient;

    public BrasilApiCambioAdapter(@Value("${brasilapi.base-url}") String baseUrl,
                                  @Value("${brasilapi.timeout-ms}") int timeoutMs) {
        // mesmos tempos maximos de espera usados na api mock: conectar e receber a resposta
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(timeoutMs));
        fabrica.setReadTimeout(Duration.ofMillis(timeoutMs));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(fabrica)
                .build();
    }

    @Override
    public Cotacao buscarCotacaoAtual(String moeda) {
        LocalDate dia = LocalDate.now(FUSO_BRASIL);

        // tenta hoje, depois ontem, anteontem... ate achar uma cotacao (ou desistir)
        for (int tentativa = 0; tentativa < MAXIMO_DIAS_PARA_TRAS; tentativa++) {
            CotacaoBrasilApiDTO resposta = consultarDia(moeda, dia);

            // resposta null = esse dia nao tem cotacao (ainda); tenta o dia anterior
            if (resposta != null && resposta.getCotacoes() != null && !resposta.getCotacoes().isEmpty()) {
                return paraDominio(moeda, dia, resposta);
            }
            dia = dia.minusDays(1);
        }

        throw new ServicoExternoIndisponivelException(
                "nenhuma cotacao de " + moeda + " encontrada nos ultimos " + MAXIMO_DIAS_PARA_TRAS + " dias");
    }

    // consulta um dia so
    // devolve null quando o dia simplesmente nao tem cotacao (pra o loop tentar o dia anterior)
    // lanca ServicoExternoIndisponivelException quando o problema e da brasilapi (fora do ar, lenta, erro 500)
    // nesse caso nao adianta tentar outros dias, entao desiste na hora em vez de esperar 7 timeouts
    private CotacaoBrasilApiDTO consultarDia(String moeda, LocalDate dia) {
        try {
            // GET {baseUrl}/cambio/v1/cotacao/USD/2026-11-04
            // LocalDate.toString() ja sai no formato aaaa-mm-dd que a brasilapi pede
            return restClient.get()
                    .uri("/cambio/v1/cotacao/{moeda}/{data}", moeda, dia.toString())
                    .retrieve()
                    .body(CotacaoBrasilApiDTO.class);
        } catch (HttpClientErrorException e) {
            // erros 4xx: alguns significam so "esse dia ainda nao tem cotacao"
            boolean naoEncontrado = e.getStatusCode() == HttpStatus.NOT_FOUND;
            boolean diaDeHojeBloqueado = e.getStatusCode() == HttpStatus.BAD_REQUEST
                    && e.getResponseBodyAsString().contains(ERRO_DIA_DE_HOJE);
            if (naoEncontrado || diaDeHojeBloqueado) {
                return null;
            }
            // qualquer outro 4xx (ex: moeda invalida) nao vai melhorar trocando de dia
            throw new ServicoExternoIndisponivelException("a brasilapi recusou a consulta de " + moeda, e);
        } catch (RestClientException e) {
            // fora do ar, timeout, erro 5xx...
            throw new ServicoExternoIndisponivelException("nao foi possivel consultar a brasilapi", e);
        }
    }

    // transforma o json da brasilapi na Cotacao do dominio
    private Cotacao paraDominio(String moeda, LocalDate diaPedido, CotacaoBrasilApiDTO resposta) {
        // os boletins vem em ordem de horario, entao o ultimo da lista e o mais recente
        List<BoletimBrasilApiDTO> boletins = resposta.getCotacoes();
        BoletimBrasilApiDTO ultimoBoletim = boletins.get(boletins.size() - 1);

        BigDecimal taxa = ultimoBoletim.getCotacaoVenda();
        // taxa vazia ou zero quebraria a conversao (divisao por zero no Cotacao.converterDeReais)
        if (taxa == null || taxa.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServicoExternoIndisponivelException("a brasilapi devolveu uma cotacao invalida para " + moeda);
        }

        return new Cotacao(moeda, taxa, lerData(resposta.getData(), diaPedido));
    }

    // a data que vale e a que a brasilapi devolveu (num domingo ela devolve a sexta, por exemplo)
    // se por algum motivo ela vier vazia ou num formato estranho, usa o dia que foi pedido
    private LocalDate lerData(String dataDaResposta, LocalDate diaPedido) {
        if (dataDaResposta == null) {
            return diaPedido;
        }
        try {
            return LocalDate.parse(dataDaResposta);
        } catch (DateTimeParseException e) {
            return diaPedido;
        }
    }
}
