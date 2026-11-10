package br.com.gabrieloliveira.transacoes.adapter.out.brasilapi;

import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import br.com.gabrieloliveira.transacoes.domain.model.Cotacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

// testes do adaptador da brasilapi, sem internet
// o MockRestServiceServer e um "servidor http de mentira": cada teste diz qual url ele vai receber e o
// que ele responde (json, 404, 500, timeout...). no fim, o server.verify() confere se todas as chamadas
// esperadas aconteceram, e o servidor reclama sozinho se chegar uma chamada que ninguem esperava
//
// o relogio fica parado em 05/11/2026 (uma quinta-feira) as 10h de brasilia, e pode ser adiantado nos testes de cache
class BrasilApiCambioAdapterTest {

    private static final String URL_BASE = "http://brasilapi.teste";
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private MockRestServiceServer server;
    private RelogioDeTeste relogio;
    private BrasilApiCambioAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(URL_BASE);
        // o bindTo liga o servidor de mentira no RestClient: as chamadas nao saem para a internet
        server = MockRestServiceServer.bindTo(builder).build();
        relogio = new RelogioDeTeste(Instant.parse("2026-11-05T13:00:00Z"));
        adapter = new BrasilApiCambioAdapter(builder.build(), relogio);
    }

    @Test
    void diaUtilUsaACotacaoDeVendaDoBoletimMaisRecenteMesmoForaDeOrdem() {
        // os boletins vem fora da ordem de horario: o mais recente (13:09) esta no meio da lista
        esperar("USD", "2026-11-05").andRespond(withSuccess("""
                {"moeda":"USD","data":"2026-11-05","cotacoes":[
                  {"cotacao_compra":5.40,"cotacao_venda":5.41,"data_hora_cotacao":"2026-11-05 10:08:00.123","tipo_boletim":"ABERTURA"},
                  {"cotacao_compra":5.47,"cotacao_venda":5.4778,"data_hora_cotacao":"2026-11-05 13:09:23.164","tipo_boletim":"FECHAMENTO PTAX"},
                  {"cotacao_compra":5.44,"cotacao_venda":5.45,"data_hora_cotacao":"2026-11-05 11:05:00.5","tipo_boletim":"INTERMEDIARIO"}
                ]}""", MediaType.APPLICATION_JSON));

        Cotacao cotacao = adapter.buscarCotacaoAtual("USD");

        assertEquals(new BigDecimal("5.4778"), cotacao.getTaxa());
        assertEquals(LocalDate.of(2026, 11, 5), cotacao.getData());
        server.verify();
    }

    @Test
    void semCotacaoHojeVoltaDiaADiaAteAcharEUsaADataDaResposta() {
        // hoje bloqueado (a regra do utc da brasilapi), ontem sem cotacao (404) e anteontem com cotacao
        esperar("EUR", "2026-11-05").andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .body("{\"type\":\"today_date_error\"}").contentType(MediaType.APPLICATION_JSON));
        esperar("EUR", "2026-11-04").andRespond(withStatus(HttpStatus.NOT_FOUND));
        esperar("EUR", "2026-11-03").andRespond(withSuccess(jsonComUmBoletim("EUR", "2026-11-03", "6.1549"),
                MediaType.APPLICATION_JSON));

        Cotacao cotacao = adapter.buscarCotacaoAtual("EUR");

        assertEquals(new BigDecimal("6.1549"), cotacao.getTaxa());
        assertEquals(LocalDate.of(2026, 11, 3), cotacao.getData());
        server.verify();
    }

    @Test
    void seteDiasSemCotacaoDesiste() {
        for (int dia = 5; dia >= -1; dia--) {
            LocalDate data = LocalDate.of(2026, 11, 5).minusDays(5 - dia);
            esperar("USD", data.toString()).andRespond(withStatus(HttpStatus.NOT_FOUND));
        }

        assertThrows(ServicoExternoIndisponivelException.class, () -> adapter.buscarCotacaoAtual("USD"));
        server.verify();
    }

    @Test
    void erro5xxDesisteNaHoraSemTentarOutrosDias() {
        // so uma chamada esperada: se o adaptador tentasse o dia anterior, o servidor de mentira reclamaria
        esperar("USD", "2026-11-05").andRespond(withServerError());

        assertThrows(ServicoExternoIndisponivelException.class, () -> adapter.buscarCotacaoAtual("USD"));
        server.verify();
    }

    @Test
    void timeoutViraServicoIndisponivel() {
        // o withException simula a conexao estourando o tempo maximo de espera
        esperar("USD", "2026-11-05").andRespond(withException(new SocketTimeoutException("demorou demais")));

        assertThrows(ServicoExternoIndisponivelException.class, () -> adapter.buscarCotacaoAtual("USD"));
        server.verify();
    }

    @Test
    void depoisDeUmaFalhaAsProximasConsultasNemChamamABrasilApiDuranteAPausa() {
        // so o dolar chega no servidor; o euro, logo em seguida, falha na hora, sem esperar outro timeout
        esperar("USD", "2026-11-05").andRespond(withException(new SocketTimeoutException("demorou demais")));

        assertThrows(ServicoExternoIndisponivelException.class, () -> adapter.buscarCotacaoAtual("USD"));
        assertThrows(ServicoExternoIndisponivelException.class, () -> adapter.buscarCotacaoAtual("EUR"));
        server.verify();
    }

    @Test
    void passadaAPausaTentaABrasilApiDeNovo() {
        // o servidor de mentira precisa saber de todas as chamadas antes de a primeira acontecer:
        // a primeira falha, e a segunda (2 minutos depois) responde a cotacao
        esperar("USD", "2026-11-05").andRespond(withServerError());
        esperar("USD", "2026-11-05").andRespond(withSuccess(jsonComUmBoletim("USD", "2026-11-05", "5.40"),
                MediaType.APPLICATION_JSON));

        assertThrows(ServicoExternoIndisponivelException.class, () -> adapter.buscarCotacaoAtual("USD"));
        relogio.adiantar(Duration.ofMinutes(2));

        assertEquals(new BigDecimal("5.40"), adapter.buscarCotacaoAtual("USD").getTaxa());
        server.verify();
    }

    @Test
    void taxaZeroEhCotacaoInvalida() {
        esperar("USD", "2026-11-05").andRespond(withSuccess(jsonComUmBoletim("USD", "2026-11-05", "0"),
                MediaType.APPLICATION_JSON));

        assertThrows(ServicoExternoIndisponivelException.class, () -> adapter.buscarCotacaoAtual("USD"));
        server.verify();
    }

    @Test
    void moedaRecusadaPelaBrasilApiEhIndisponivelMasNaoLigaAPausa() {
        // um 400 que nao e o erro do "dia de hoje" (uma moeda invalida, por exemplo): a brasilapi esta no ar
        esperar("XYZ", "2026-11-05").andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .body("{\"type\":\"invalid_currency\"}").contentType(MediaType.APPLICATION_JSON));
        esperar("USD", "2026-11-05").andRespond(withSuccess(jsonComUmBoletim("USD", "2026-11-05", "5.40"),
                MediaType.APPLICATION_JSON));

        assertThrows(ServicoExternoIndisponivelException.class, () -> adapter.buscarCotacaoAtual("XYZ"));
        // logo depois, o dolar ainda e consultado normalmente (a brasilapi nao entrou em pausa)
        assertEquals(new BigDecimal("5.40"), adapter.buscarCotacaoAtual("USD").getTaxa());
        server.verify();
    }

    @Test
    void cotacaoGuardadaNoCacheEvitaNovaChamada() {
        // uma chamada so, mesmo pedindo o dolar duas vezes
        esperar("USD", "2026-11-05").andRespond(withSuccess(jsonComUmBoletim("USD", "2026-11-05", "5.40"),
                MediaType.APPLICATION_JSON));

        adapter.buscarCotacaoAtual("USD");
        relogio.adiantar(Duration.ofMinutes(10));
        Cotacao segunda = adapter.buscarCotacaoAtual("USD");

        assertEquals(new BigDecimal("5.40"), segunda.getTaxa());
        server.verify();
    }

    @Test
    void cacheVenceDepoisDeMeiaHoraEBuscaACotacaoNova() {
        esperar("USD", "2026-11-05").andRespond(withSuccess(jsonComUmBoletim("USD", "2026-11-05", "5.40"),
                MediaType.APPLICATION_JSON));
        esperar("USD", "2026-11-05").andRespond(withSuccess(jsonComUmBoletim("USD", "2026-11-05", "5.50"),
                MediaType.APPLICATION_JSON));

        adapter.buscarCotacaoAtual("USD");
        relogio.adiantar(Duration.ofMinutes(31));

        assertEquals(new BigDecimal("5.50"), adapter.buscarCotacaoAtual("USD").getTaxa());
        server.verify();
    }

    // apoio: avisa o servidor de mentira que vai chegar um GET nesta url
    private org.springframework.test.web.client.ResponseActions esperar(String moeda, String data) {
        return server.expect(requestTo(URL_BASE + "/cambio/v1/cotacao/" + moeda + "/" + data))
                .andExpect(method(HttpMethod.GET));
    }

    private String jsonComUmBoletim(String moeda, String data, String venda) {
        return "{\"moeda\":\"" + moeda + "\",\"data\":\"" + data + "\",\"cotacoes\":[{\"cotacao_compra\":" + venda
                + ",\"cotacao_venda\":" + venda + ",\"data_hora_cotacao\":\"" + data
                + " 13:09:23.164\",\"tipo_boletim\":\"FECHAMENTO PTAX\"}]}";
    }

    // um relogio que da para adiantar no meio do teste (o Clock.fixed do java fica parado para sempre)
    private static class RelogioDeTeste extends Clock {

        private Instant agora;

        RelogioDeTeste(Instant inicio) {
            this.agora = inicio;
        }

        void adiantar(Duration tempo) {
            agora = agora.plus(tempo);
        }

        @Override
        public ZoneId getZone() {
            return FUSO;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }
}
