package br.com.gabrieloliveira.transacoes.adapter.in.web;

import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.ErroDeSegurancaHandler;
import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.UsuarioLogado;
import br.com.gabrieloliveira.transacoes.application.port.in.AnalisarDespesasUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.AutenticarUsuarioUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.BuscarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.ConsultarSaldoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.CriarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.EstornarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.ListarTransacoesUseCase;
import br.com.gabrieloliveira.transacoes.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.ZoneId;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// testes web da validacao das entradas do ms2
// eles conferem que cada entrada errada responde 400 com uma mensagem clara, e que o caso de uso nem e chamado
// lembrando a diferenca: aqui e erro de FORMATO (400); uma regra de negocio (saldo, limite, categoria da
// compra) nao e erro 400, e sim uma transacao REJECTED com o motivo (isso e testado no dominio)
@WebMvcTest(controllers = {TransacaoController.class, AnaliseDespesasController.class, ContaController.class})
@Import({SecurityConfig.class, ErroDeSegurancaHandler.class, UsuarioLogado.class, TransacaoWebMapper.class,
        AnaliseDespesasWebMapper.class, ValidacaoWebTest.RelogioDoTeste.class})
class ValidacaoWebTest {

    private static final String EMAIL = "ana@email.com";
    private static final String SENHA = "Senha@123";
    private static final Long ID_DA_ANA = 7L;

    // o ContaController precisa de um relogio, entao no teste entregamos um no fuso de brasilia
    @TestConfiguration
    static class RelogioDoTeste {
        @Bean
        Clock relogio() {
            return Clock.system(ZoneId.of("America/Sao_Paulo"));
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    @MockitoBean
    private CriarTransacaoUseCase criarTransacaoUseCase;
    @MockitoBean
    private BuscarTransacaoUseCase buscarTransacaoUseCase;
    @MockitoBean
    private ListarTransacoesUseCase listarTransacoesUseCase;
    @MockitoBean
    private EstornarTransacaoUseCase estornarTransacaoUseCase;
    @MockitoBean
    private AnalisarDespesasUseCase analisarDespesasUseCase;
    @MockitoBean
    private ConsultarSaldoUseCase consultarSaldoUseCase;

    @Test
    void transacaoSemOsCamposObrigatoriosRespondeTodosDeUmaVez() throws Exception {
        logarComoAna();

        mockMvc.perform(post("/transactions").with(httpBasic(EMAIL, SENHA))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("existem campos invalidos na requisicao"))
                .andExpect(jsonPath("$.campos.tipo")
                        .value("o tipo e obrigatorio (DEPOSITO, RETIRADA, TRANSFERENCIA ou COMPRA)"))
                .andExpect(jsonPath("$.campos.valor").value("o valor e obrigatorio"))
                .andExpect(jsonPath("$.campos.descricao").value("a descricao e obrigatoria"));

        verify(criarTransacaoUseCase, never()).criar(any());
    }

    @Test
    void valorZeroOuNegativoResponde400() throws Exception {
        logarComoAna();

        mockMvc.perform(post("/transactions").with(httpBasic(EMAIL, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"DEPOSITO\",\"valor\":-10.00,\"descricao\":\"salario\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.valor").value("o valor deve ser maior que zero"));

        verify(criarTransacaoUseCase, never()).criar(any());
    }

    @Test
    void valorComDoisErrosMostraAsDuasMensagensSempreNaMesmaOrdem() throws Exception {
        logarComoAna();

        // negativo E com 3 casas: antes so uma das mensagens aparecia, e a cada vez podia ser uma diferente
        mockMvc.perform(post("/transactions").with(httpBasic(EMAIL, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"DEPOSITO\",\"valor\":-1.555,\"descricao\":\"salario\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.valor").value(
                        "o valor deve ser maior que zero; o valor deve ter no maximo 2 casas decimais"));
    }

    @Test
    void metodoHttpNaoAceitoResponde405NoNossoFormato() throws Exception {
        logarComoAna();

        // nao existe PATCH nem PUT de transacao: transacao nao se altera, so se estorna
        mockMvc.perform(patch("/transactions/1").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.mensagem").value(startsWith("o metodo PATCH nao e aceito nesta rota")))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void formatoDoCorpoNaoAceitoResponde415NoNossoFormato() throws Exception {
        logarComoAna();

        mockMvc.perform(post("/transactions").with(httpBasic(EMAIL, SENHA))
                        .contentType(MediaType.TEXT_PLAIN).content("deposito de 10 reais"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.mensagem").value(startsWith("o formato 'text/plain")))
                .andExpect(jsonPath("$.mensagem").value(containsString("nao e aceito nesta rota, aceitos:")))
                .andExpect(jsonPath("$.trace").doesNotExist());

        verify(criarTransacaoUseCase, never()).criar(any());
    }

    @Test
    void rotaInexistenteResponde404NoNossoFormato() throws Exception {
        logarComoAna();

        mockMvc.perform(get("/transacoes").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("a rota /transacoes nao existe"))
                // confere o texto do json, porque o jsonPath doesNotExist tambem passaria com um "campos": null
                .andExpect(content().string(not(containsString("\"campos\""))));
    }

    @Test
    void valorComTresCasasDecimaisResponde400() throws Exception {
        logarComoAna();

        mockMvc.perform(post("/transactions").with(httpBasic(EMAIL, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"DEPOSITO\",\"valor\":10.555,\"descricao\":\"salario\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.valor").value("o valor deve ter no maximo 2 casas decimais"));
    }

    @Test
    void descricaoMaiorQue255CaracteresResponde400() throws Exception {
        logarComoAna();
        String descricaoGigante = "a".repeat(256);

        mockMvc.perform(post("/transactions").with(httpBasic(EMAIL, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"DEPOSITO\",\"valor\":10.00,\"descricao\":\"" + descricaoGigante + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.descricao").value("a descricao pode ter no maximo 255 caracteres"));
    }

    @Test
    void tipoForaDaListaDizOCampoEOsValoresAceitos() throws Exception {
        logarComoAna();

        mockMvc.perform(post("/transactions").with(httpBasic(EMAIL, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"PIX\",\"valor\":10.00,\"descricao\":\"teste\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.tipo").value(
                        "o valor 'PIX' nao e valido, aceitos: [DEPOSITO, RETIRADA, TRANSFERENCIA, COMPRA]"));

        verify(criarTransacaoUseCase, never()).criar(any());
    }

    @Test
    void valorComTextoNoLugarDoNumeroDizOCampo() throws Exception {
        logarComoAna();

        mockMvc.perform(post("/transactions").with(httpBasic(EMAIL, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"DEPOSITO\",\"valor\":\"dez\",\"descricao\":\"teste\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.valor")
                        .value("o valor 'dez' nao e valido, envie um numero (ex: 150.75)"));
    }

    @Test
    void jsonQuebradoResponde400SemCampos() throws Exception {
        logarComoAna();

        mockMvc.perform(post("/transactions").with(httpBasic(EMAIL, SENHA))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\": \"DEPOSITO\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("o corpo da requisicao esta mal formatado"));
    }

    @Test
    void idZeroNaUrlResponde400() throws Exception {
        logarComoAna();

        mockMvc.perform(get("/transactions/0").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.id").value("o id deve ser um numero positivo"));

        verify(buscarTransacaoUseCase, never()).buscarPorId(anyLong(), anyLong());
    }

    @Test
    void estornoComIdNegativoResponde400() throws Exception {
        logarComoAna();

        mockMvc.perform(post("/transactions/-3/estorno").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.id").value("o id deve ser um numero positivo"));

        verify(estornarTransacaoUseCase, never()).estornar(anyLong(), anyLong());
    }

    @Test
    void filtroComUsuarioIdNegativoResponde400() throws Exception {
        logarComoAna();

        mockMvc.perform(get("/transactions").param("usuarioId", "-1").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.usuarioId").value("o id deve ser um numero positivo"));

        verify(listarTransacoesUseCase, never()).listar(any(), any(), any(), any());
    }

    @Test
    void filtroComTipoInvalidoMostraOsValoresAceitos() throws Exception {
        logarComoAna();

        mockMvc.perform(get("/transactions").param("tipo", "PIX").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("o valor 'PIX' nao e valido para o parametro 'tipo'"
                        + ", aceitos: [DEPOSITO, RETIRADA, TRANSFERENCIA, COMPRA]"));
    }

    @Test
    void analiseComDataNoFormatoErradoMostraOFormatoCerto() throws Exception {
        logarComoAna();

        mockMvc.perform(get("/transactions/analysis/category").param("inicio", "31/12/2026")
                        .with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("o valor '31/12/2026' nao e valido para o parametro 'inicio'"
                        + ", use o formato aaaa-mm-dd (ex: 2026-11-05)"));

        verify(analisarDespesasUseCase, never()).porCategoria(any(), any(), any());
    }

    @Test
    void analiseComUsuarioIdZeroResponde400() throws Exception {
        logarComoAna();

        mockMvc.perform(get("/transactions/analysis/monthly").param("usuarioId", "0").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.usuarioId").value("o id deve ser um numero positivo"));

        verify(analisarDespesasUseCase, never()).porMes(any(), any(), any());
    }

    @Test
    void saldoComIdZeroResponde400() throws Exception {
        logarComoAna();

        mockMvc.perform(get("/accounts/0/balance").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.usuarioId").value("o id deve ser um numero positivo"));

        verify(consultarSaldoUseCase, never()).consultar(anyLong());
    }

    private void logarComoAna() {
        when(autenticarUsuarioUseCase.autenticar(EMAIL, SENHA)).thenReturn(ID_DA_ANA);
    }
}
