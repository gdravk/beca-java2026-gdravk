package br.com.gabrieloliveira.transacoes.adapter.out.brasilapi;

import br.com.gabrieloliveira.transacoes.application.port.out.CambioPort;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import br.com.gabrieloliveira.transacoes.domain.model.Cotacao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// adaptador de saida do cambio: busca a cotacao do dolar e do euro na brasilapi, via http
// ele implementa a porta CambioPort; no comeco do projeto quem ocupava essa porta era um adaptador falso,
// e na troca o resto do sistema nao precisou mudar nada (e isso que a arquitetura hexagonal promete)
//
// como a cotacao e escolhida (decisao do time, registrada no epico do pedido 3 e no readme):
// - endpoint: GET /cambio/v1/cotacao/{moeda}/{data}
// - usamos a cotacao de VENDA do boletim mais recente do dia (o de maior data_hora_cotacao)
// - comecamos pelo dia de hoje (horario de brasilia) e, se a brasilapi ainda nao tiver cotacao para esse
//   dia, voltamos um dia de cada vez, ate no maximo MAXIMO_DIAS_PARA_TRAS
//
// para nao depender da brasilapi a cada transacao (ajuste pedido na revisao do professor):
// - cache: a cotacao encontrada fica guardada por moeda e data durante TEMPO_DO_CACHE, e nesse tempo
//   as proximas transacoes usam a cotacao guardada, sem chamar a brasilapi
// - pausa depois de falha: se a brasilapi cair ou demorar demais, o adaptador nem tenta de novo durante
//   PAUSA_APOS_FALHA; sem isso, com ela fora do ar, cada transacao esperaria dois timeouts (dolar e euro)
@Component
public class BrasilApiCambioAdapter implements CambioPort {

    // uma semana cobre com folga um fim de semana somado a um feriado prolongado
    private static final int MAXIMO_DIAS_PARA_TRAS = 7;

    // a brasilapi publica alguns boletins por dia, entao a cotacao do dia pode mudar algumas vezes;
    // 30 minutos e um meio-termo entre economizar chamadas e mostrar a cotacao mais nova
    private static final Duration TEMPO_DO_CACHE = Duration.ofMinutes(30);

    // depois de uma falha de conexao, espera 1 minuto antes de tentar a brasilapi de novo
    private static final Duration PAUSA_APOS_FALHA = Duration.ofMinutes(1);

    // pedaco do corpo do erro 400 que a brasilapi manda quando nao deixa consultar o dia de hoje:
    // {"message":"Não é possível consultar a cotação do dia de hoje...","type":"today_date_error",...}
    // ela usa o horario utc, entao a noite, no brasil, ela ja considera que e o dia seguinte
    private static final String ERRO_DIA_DE_HOJE = "today_date_error";

    // formato do data_hora_cotacao da brasilapi, como "2026-11-04 13:09:23.164"
    // os milissegundos sao opcionais e podem ter de 0 a 9 casas, por isso o appendFraction
    private static final DateTimeFormatter FORMATO_DATA_HORA = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .optionalEnd()
            .toFormatter();

    private final RestClient restClient;

    // relogio no fuso de brasilia (vem do BeanConfig); nos testes, um relogio parado numa data fixa
    private final Clock relogio;

    // cache das cotacoes, com a chave "moeda|data" (ex: "USD|2026-11-05")
    // o ConcurrentHashMap e um Map que aguenta varias requisicoes mexendo nele ao mesmo tempo
    // (o spring atende cada requisicao numa thread diferente, e um HashMap comum poderia se corromper)
    private final Map<String, CotacaoGuardada> cache = new ConcurrentHashMap<>();

    // ate quando a brasilapi fica "de castigo" depois de uma falha (null = sem pausa)
    // o volatile garante que todas as threads enxergam o valor mais novo desta variavel
    private volatile Instant semConsultarAte;

    // construtor usado pelo spring: monta o RestClient com a url e os timeouts do application.properties
    // o @Autowired diz ao spring qual construtor usar, ja que a classe tem dois
    @Autowired
    public BrasilApiCambioAdapter(@Value("${brasilapi.base-url}") String baseUrl,
                                  @Value("${brasilapi.timeout-ms}") int timeoutMs,
                                  Clock relogio) {
        this(criarRestClient(baseUrl, timeoutMs), relogio);
    }

    // construtor usado nos testes: recebe um RestClient pronto, ligado num servidor http de mentira
    BrasilApiCambioAdapter(RestClient restClient, Clock relogio) {
        this.restClient = restClient;
        this.relogio = relogio;
    }

    private static RestClient criarRestClient(String baseUrl, int timeoutMs) {
        // os mesmos tempos maximos de espera usados na api mock: para conectar e para receber a resposta
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(timeoutMs));
        fabrica.setReadTimeout(Duration.ofMillis(timeoutMs));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(fabrica)
                .build();
    }

    @Override
    public Cotacao buscarCotacaoAtual(String moeda) {
        LocalDate hoje = LocalDate.now(relogio);
        Instant agora = relogio.instant();

        // 1. ja temos essa cotacao guardada e ainda valendo? entao usa ela, sem chamar a brasilapi
        String chave = moeda + "|" + hoje;
        CotacaoGuardada guardada = cache.get(chave);
        if (guardada != null && agora.isBefore(guardada.getValidaAte())) {
            return guardada.getCotacao();
        }

        // 2. a brasilapi falhou faz pouco tempo? nem tenta: responde na hora que ela esta indisponivel
        Instant pausa = semConsultarAte;
        if (pausa != null && agora.isBefore(pausa)) {
            throw new ServicoExternoIndisponivelException("brasilapi em pausa depois de uma falha recente");
        }

        // 3. consulta: tenta hoje, depois ontem, anteontem... ate achar uma cotacao (ou desistir)
        LocalDate dia = hoje;
        for (int tentativa = 0; tentativa < MAXIMO_DIAS_PARA_TRAS; tentativa++) {
            CotacaoBrasilApiDTO resposta = consultarDia(moeda, dia);

            // resposta null = esse dia (ainda) nao tem cotacao; tenta o dia anterior
            if (resposta != null && resposta.getCotacoes() != null && !resposta.getCotacoes().isEmpty()) {
                Cotacao cotacao = paraDominio(moeda, dia, resposta);
                cache.put(chave, new CotacaoGuardada(cotacao, agora.plus(TEMPO_DO_CACHE)));
                return cotacao;
            }
            dia = dia.minusDays(1);
        }

        throw new ServicoExternoIndisponivelException(
                "nenhuma cotacao de " + moeda + " encontrada nos ultimos " + MAXIMO_DIAS_PARA_TRAS + " dias");
    }

    // consulta um dia so
    // devolve null quando o dia simplesmente nao tem cotacao (para o loop tentar o dia anterior)
    // lanca ServicoExternoIndisponivelException quando o problema e da brasilapi (fora do ar, lenta, erro 500);
    // nesse caso nao adianta tentar outros dias, entao desistimos na hora em vez de esperar 7 timeouts
    private CotacaoBrasilApiDTO consultarDia(String moeda, LocalDate dia) {
        try {
            // GET {baseUrl}/cambio/v1/cotacao/USD/2026-11-04
            // o LocalDate.toString() ja sai no formato aaaa-mm-dd que a brasilapi pede
            return restClient.get()
                    .uri("/cambio/v1/cotacao/{moeda}/{data}", moeda, dia.toString())
                    .retrieve()
                    .body(CotacaoBrasilApiDTO.class);
        } catch (HttpClientErrorException e) {
            // erros 4xx: alguns deles so querem dizer "esse dia ainda nao tem cotacao"
            boolean naoEncontrado = e.getStatusCode() == HttpStatus.NOT_FOUND;
            boolean diaDeHojeBloqueado = e.getStatusCode() == HttpStatus.BAD_REQUEST
                    && e.getResponseBodyAsString().contains(ERRO_DIA_DE_HOJE);
            if (naoEncontrado || diaDeHojeBloqueado) {
                return null;
            }
            // qualquer outro 4xx (uma moeda invalida, por exemplo) nao melhora trocando de dia
            // (e aqui nao ligamos a pausa: a brasilapi esta no ar, so recusou esta consulta)
            throw new ServicoExternoIndisponivelException("a brasilapi recusou a consulta de " + moeda, e);
        } catch (RestClientException e) {
            // fora do ar, timeout, erro 5xx...: liga a pausa, para as proximas consultas nao esperarem de novo
            semConsultarAte = relogio.instant().plus(PAUSA_APOS_FALHA);
            throw new ServicoExternoIndisponivelException("nao foi possivel consultar a brasilapi", e);
        }
    }

    // transforma o json da brasilapi na Cotacao do dominio
    private Cotacao paraDominio(String moeda, LocalDate diaPedido, CotacaoBrasilApiDTO resposta) {
        BoletimBrasilApiDTO boletim = boletimMaisRecente(resposta.getCotacoes());

        BigDecimal taxa = boletim.getCotacaoVenda();
        // uma taxa vazia ou zero quebraria a conversao (seria uma divisao por zero no Cotacao.converterDeReais)
        if (taxa == null || taxa.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServicoExternoIndisponivelException("a brasilapi devolveu uma cotacao invalida para " + moeda);
        }

        return new Cotacao(moeda, taxa, lerData(resposta.getData(), diaPedido));
    }

    // escolhe o boletim com o maior data_hora_cotacao, ou seja, o mais recente do dia
    // nao confiamos na posicao dele na lista: se um dia a brasilapi mudar a ordem, continuamos pegando o certo
    // um boletim com data_hora vazia ou num formato estranho so e usado se nenhum outro tiver data valida
    private BoletimBrasilApiDTO boletimMaisRecente(List<BoletimBrasilApiDTO> boletins) {
        BoletimBrasilApiDTO maisRecente = null;
        LocalDateTime horarioMaisRecente = null;

        for (BoletimBrasilApiDTO boletim : boletins) {
            LocalDateTime horario = lerDataHora(boletim.getDataHoraCotacao());
            if (horario != null && (horarioMaisRecente == null || horario.isAfter(horarioMaisRecente))) {
                maisRecente = boletim;
                horarioMaisRecente = horario;
            }
        }

        if (maisRecente == null) {
            return boletins.get(boletins.size() - 1);
        }
        return maisRecente;
    }

    private LocalDateTime lerDataHora(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(texto, FORMATO_DATA_HORA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // a data que vale e a que a brasilapi devolveu (num domingo ela devolve a sexta, por exemplo)
    // se por algum motivo ela vier vazia ou num formato estranho, usamos o dia que foi pedido
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

    // uma cotacao guardada no cache, e ate quando ela vale
    private static class CotacaoGuardada {

        private final Cotacao cotacao;
        private final Instant validaAte;

        CotacaoGuardada(Cotacao cotacao, Instant validaAte) {
            this.cotacao = cotacao;
            this.validaAte = validaAte;
        }

        Cotacao getCotacao() {
            return cotacao;
        }

        Instant getValidaAte() {
            return validaAte;
        }
    }
}
