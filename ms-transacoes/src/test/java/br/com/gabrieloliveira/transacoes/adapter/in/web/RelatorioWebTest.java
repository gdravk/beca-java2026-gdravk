package br.com.gabrieloliveira.transacoes.adapter.in.web;

import br.com.gabrieloliveira.transacoes.adapter.in.web.csv.GeradorCsvRelatorio;
import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.ErroDeSegurancaHandler;
import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.UsuarioLogado;
import br.com.gabrieloliveira.transacoes.application.port.in.AutenticarUsuarioUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.GerarRelatorioUseCase;
import br.com.gabrieloliveira.transacoes.config.SecurityConfig;
import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.Periodo;
import br.com.gabrieloliveira.transacoes.domain.model.RelatorioTransacoes;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// testes web do download do relatorio em csv (pedido 7)
@WebMvcTest(controllers = RelatorioController.class)
@Import({SecurityConfig.class, ErroDeSegurancaHandler.class, UsuarioLogado.class, GeradorCsvRelatorio.class})
class RelatorioWebTest {

    private static final String EMAIL = "ana@email.com";
    private static final String SENHA = "Senha@123";
    private static final Long ID_DA_ANA = 7L;
    private static final Periodo NOVEMBRO =
            Periodo.resolver(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 15));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    @MockitoBean
    private GerarRelatorioUseCase gerarRelatorioUseCase;

    @Test
    void baixaOArquivoCsvComONomeEOTipoCertos() throws Exception {
        logarComoAna();
        when(gerarRelatorioUseCase.gerar(ID_DA_ANA, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)))
                .thenReturn(RelatorioTransacoes.montar(ID_DA_ANA, NOVEMBRO, List.of(compraDaAna())));

        MvcResult resultado = mockMvc.perform(get("/transactions/report").with(httpBasic(EMAIL, SENHA))
                        .param("inicio", "2026-11-01").param("fim", "2026-11-30"))
                .andExpect(status().isOk())
                // attachment = o navegador baixa o arquivo, com este nome
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"relatorio-transacoes.csv\""))
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andReturn();

        String arquivo = new String(resultado.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        assertTrue(arquivo.startsWith("\uFEFFId;Data e hora;Tipo;Categoria;Descrição;"), arquivo);
        assertTrue(arquivo.contains("10;05/11/2026 14:30;Compra;Alimentação;mercado do mês;150,75;Aprovada;"), arquivo);
    }

    @Test
    void semDatasOCasoDeUsoRecebeNullERespondeOMesAtual() throws Exception {
        logarComoAna();
        when(gerarRelatorioUseCase.gerar(ID_DA_ANA, null, null))
                .thenReturn(RelatorioTransacoes.montar(ID_DA_ANA, NOVEMBRO, List.of()));

        mockMvc.perform(get("/transactions/report").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isOk());
    }

    @Test
    void relatorioDeOutroUsuarioResponde403() throws Exception {
        logarComoAna();

        mockMvc.perform(get("/transactions/report").with(httpBasic(EMAIL, SENHA)).param("usuarioId", "99"))
                .andExpect(status().isForbidden());

        // o arquivo so traz transacoes de quem esta logado: aqui o caso de uso nem e chamado
        verify(gerarRelatorioUseCase, never()).gerar(anyLong(), any(), any());
    }

    @Test
    void semLoginResponde401() throws Exception {
        mockMvc.perform(get("/transactions/report"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void semLoginComOClientePedindoCsvO401SaiEmJson() throws Exception {
        // o 401 e montado num metodo separado do handler, e ele tambem precisa sair em json mesmo quando
        // o cliente pede "Accept: text/csv" (senao a resposta sairia sem corpo)
        mockMvc.perform(get("/transactions/report").header("Accept", "text/csv"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.mensagem", startsWith("faca login")));
    }

    @Test
    void erroComOClientePedindoCsvContinuaSaindoEmJson() throws Exception {
        // o swagger e o navegador pedem "Accept: text/csv" nesta rota, e mesmo assim o erro (data em formato
        // errado) tem que chegar como o nosso json de erro, e nao como um 406 vazio
        logarComoAna();

        mockMvc.perform(get("/transactions/report").with(httpBasic(EMAIL, SENHA))
                        .header("Accept", "text/csv").param("inicio", "31/12/2026"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.mensagem", startsWith("o valor '31/12/2026' nao e valido")));

        verify(gerarRelatorioUseCase, never()).gerar(anyLong(), any(), any());
    }

    private Transacao compraDaAna() {
        Transacao compra = new Transacao();
        compra.setId(10L);
        compra.setUsuarioId(ID_DA_ANA);
        compra.setTipo(TipoTransacao.COMPRA);
        compra.setValor(new BigDecimal("150.75"));
        compra.setCategoria(Categoria.ALIMENTACAO);
        compra.setDescricao("mercado do mês");
        compra.setDataHora(LocalDateTime.of(2026, 11, 5, 14, 30));
        compra.aprovar();
        return compra;
    }

    private void logarComoAna() {
        when(autenticarUsuarioUseCase.autenticar(EMAIL, SENHA)).thenReturn(ID_DA_ANA);
    }
}
