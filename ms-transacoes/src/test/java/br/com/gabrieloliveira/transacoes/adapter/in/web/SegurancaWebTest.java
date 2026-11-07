package br.com.gabrieloliveira.transacoes.adapter.in.web;

import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.ErroDeSegurancaHandler;
import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.UsuarioLogado;
import br.com.gabrieloliveira.transacoes.application.port.in.AutenticarUsuarioUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.BuscarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.CriarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.EstornarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.ListarTransacoesUseCase;
import br.com.gabrieloliveira.transacoes.config.SecurityConfig;
import br.com.gabrieloliveira.transacoes.domain.exception.CredenciaisInvalidasException;
import br.com.gabrieloliveira.transacoes.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// testes web da seguranca do ms2: requisicoes http "de mentira" no TransacaoController
// cobrem os erros que o professor pediu: 401 (sem login ou login errado), 403 (dados de outro usuario),
// 503 (ms1 fora do ar) e o 404 da transacao de outra pessoa
//
// o ms1 e simulado pelo mock do AutenticarUsuarioUseCase: "ana" com a senha certa e o usuario 7
@WebMvcTest(controllers = TransacaoController.class)
@Import({SecurityConfig.class, ErroDeSegurancaHandler.class, UsuarioLogado.class, TransacaoWebMapper.class})
class SegurancaWebTest {

    private static final String EMAIL = "ana@email.com";
    private static final String SENHA = "Senha@123";
    private static final Long ID_DA_ANA = 7L;

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

    @Test
    void semLoginResponde401ComOCabecalhoDaAutenticacaoBasica() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"ms-transacoes\""))
                .andExpect(jsonPath("$.mensagem").value(
                        "faca login: envie o email e a senha do usuario (autenticacao basica)"));

        verify(listarTransacoesUseCase, never()).listar(any(), any(), any(), any());
    }

    @Test
    void senhaErradaResponde401EPerguntaAoMs1UmaVezSo() throws Exception {
        when(autenticarUsuarioUseCase.autenticar(EMAIL, "errada")).thenThrow(new CredenciaisInvalidasException());

        mockMvc.perform(get("/transactions").with(httpBasic(EMAIL, "errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("email ou senha invalidos"));

        // o provider existe uma vez so: um login errado e uma chamada ao ms1, e nao duas
        verify(autenticarUsuarioUseCase, times(1)).autenticar(EMAIL, "errada");
    }

    @Test
    void ms1ForaDoArResponde503SemDeixarEntrar() throws Exception {
        when(autenticarUsuarioUseCase.autenticar(EMAIL, SENHA))
                .thenThrow(new ServicoExternoIndisponivelException("ms1 fora"));

        mockMvc.perform(get("/transactions").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));

        verify(listarTransacoesUseCase, never()).listar(any(), any(), any(), any());
    }

    @Test
    void listarAsTransacoesDeOutroUsuarioResponde403() throws Exception {
        // criterio do PO: "tentando listar as transacoes de outro, recebo acesso negado (403)"
        logarComoAna();

        mockMvc.perform(get("/transactions").param("usuarioId", "99").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("voce so pode acessar os seus proprios dados"));

        verify(listarTransacoesUseCase, never()).listar(any(), any(), any(), any());
    }

    @Test
    void criarTransacaoEmNomeDeOutroUsuarioResponde403() throws Exception {
        logarComoAna();

        mockMvc.perform(post("/transactions").with(httpBasic(EMAIL, SENHA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":99,\"tipo\":\"DEPOSITO\",\"valor\":10.00,\"descricao\":\"teste\"}"))
                .andExpect(status().isForbidden());

        verify(criarTransacaoUseCase, never()).criar(any());
    }

    @Test
    void listarSemUsuarioIdUsaOUsuarioLogado() throws Exception {
        logarComoAna();
        when(listarTransacoesUseCase.listar(ID_DA_ANA, null, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/transactions").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isOk());

        verify(listarTransacoesUseCase).listar(ID_DA_ANA, null, null, null);
    }

    @Test
    void buscarTransacaoDeOutroUsuarioResponde404() throws Exception {
        logarComoAna();
        // o caso de uso recebe o id de quem esta logado e responde 404 se a transacao nao for dela
        when(buscarTransacaoUseCase.buscarPorId(10L, ID_DA_ANA))
                .thenThrow(new RecursoNaoEncontradoException("transacao com id 10 nao encontrada"));

        mockMvc.perform(get("/transactions/10").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("transacao com id 10 nao encontrada"));

        verify(buscarTransacaoUseCase).buscarPorId(10L, ID_DA_ANA);
    }

    @Test
    void estornoPassaOUsuarioLogadoProCasoDeUso() throws Exception {
        logarComoAna();
        when(estornarTransacaoUseCase.estornar(10L, ID_DA_ANA))
                .thenThrow(new RecursoNaoEncontradoException("transacao com id 10 nao encontrada"));

        mockMvc.perform(post("/transactions/10/estorno").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isNotFound());

        verify(estornarTransacaoUseCase).estornar(10L, ID_DA_ANA);
        verify(estornarTransacaoUseCase, never()).estornar(10L, 99L);
        verify(estornarTransacaoUseCase, times(1)).estornar(anyLong(), anyLong());
    }

    private void logarComoAna() {
        when(autenticarUsuarioUseCase.autenticar(EMAIL, SENHA)).thenReturn(ID_DA_ANA);
    }
}
