package br.com.gabrieloliveira.usuarios.adapter.in.web;

import br.com.gabrieloliveira.usuarios.adapter.in.web.csv.LeitorCsvUsuarios;
import br.com.gabrieloliveira.usuarios.adapter.in.web.seguranca.ErroDeSegurancaHandler;
import br.com.gabrieloliveira.usuarios.adapter.in.web.seguranca.VerificadorDeChaveInterna;
import br.com.gabrieloliveira.usuarios.application.port.in.AtualizarUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.in.BuscarUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.in.CriarUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.in.ExcluirUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.in.ImportarUsuariosUseCase;
import br.com.gabrieloliveira.usuarios.application.port.in.ListarUsuariosUseCase;
import br.com.gabrieloliveira.usuarios.application.port.in.VerificarCredenciaisUseCase;
import br.com.gabrieloliveira.usuarios.config.SecurityConfig;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// testes web da validacao das entradas do ms1
// eles conferem que cada dado errado responde 400 com uma mensagem clara para cada campo em "campos" e,
// o mais importante, que o caso de uso nem chega a ser chamado quando a entrada e invalida
// a montagem e a mesma do SegurancaWebTest: so a camada web, com os casos de uso trocados por mocks
@WebMvcTest(controllers = {UsuarioController.class, AuthController.class, ImportacaoUsuariosController.class})
@Import({SecurityConfig.class, ErroDeSegurancaHandler.class, UsuarioWebMapper.class, VerificadorDeChaveInterna.class})
@TestPropertySource(properties = "seguranca.chave-interna=" + ValidacaoWebTest.CHAVE_INTERNA)
class ValidacaoWebTest {

    static final String CHAVE_INTERNA = "chave-interna-dos-testes-123";

    private static final String SENHA_ACIMA_DO_BCRYPT =
            "a senha pode ter no maximo 72 bytes (72 letras sem acento; letra com acento conta como 2)";
    private static final String EMAIL = "ana@email.com";
    private static final String SENHA = "Senha@123";
    private static final Long ID_DA_ANA = 7L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VerificarCredenciaisUseCase verificarCredenciaisUseCase;
    @MockitoBean
    private CriarUsuarioUseCase criarUsuarioUseCase;
    @MockitoBean
    private ListarUsuariosUseCase listarUsuariosUseCase;
    @MockitoBean
    private BuscarUsuarioUseCase buscarUsuarioUseCase;
    @MockitoBean
    private AtualizarUsuarioUseCase atualizarUsuarioUseCase;
    @MockitoBean
    private ExcluirUsuarioUseCase excluirUsuarioUseCase;
    @MockitoBean
    private ImportarUsuariosUseCase importarUsuariosUseCase;
    @MockitoBean
    private LeitorCsvUsuarios leitorCsvUsuarios;

    @Test
    void cadastroComTodosOsCamposErradosRespondeTodosDeUmaVez() throws Exception {
        String corpo = "{\"nome\":\"\",\"email\":\"ana-email.com\",\"cpf\":\"123.456.789-00\",\"senha\":\"123\"}";

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("existem campos invalidos na requisicao"))
                .andExpect(jsonPath("$.campos.nome").value("o nome e obrigatorio"))
                .andExpect(jsonPath("$.campos.email").value("o email esta em formato invalido"))
                .andExpect(jsonPath("$.campos.cpf")
                        .value("o cpf deve ter exatamente 11 numeros, sem ponto e sem traco"))
                .andExpect(jsonPath("$.campos.senha").value("a senha deve ter pelo menos 6 caracteres"));

        verify(criarUsuarioUseCase, never()).criar(any());
    }

    @Test
    void cadastroSemCorpoNenhumAvisaOsCamposObrigatorios() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value("o nome e obrigatorio"))
                .andExpect(jsonPath("$.campos.email").value("o email e obrigatorio"))
                .andExpect(jsonPath("$.campos.senha").value("a senha e obrigatoria"));
    }

    @Test
    void campoComDoisErrosMostraAsDuasMensagens() throws Exception {
        // um cpf vazio quebra duas regras ao mesmo tempo: @NotBlank e @Pattern
        String corpo = "{\"nome\":\"Ana\",\"email\":\"ana@email.com\",\"cpf\":\"\",\"senha\":\"Senha@123\"}";

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cpf").value(
                        "o cpf deve ter exatamente 11 numeros, sem ponto e sem traco; o cpf e obrigatorio"));
    }

    @Test
    void campoComTipoDeDadoErradoDizQualCampo() throws Exception {
        String corpo = "{\"nome\":{\"a\":1},\"email\":\"ana@email.com\",\"cpf\":\"12345678901\",\"senha\":\"Senha@123\"}";

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value("o campo veio com um tipo de dado errado, envie um texto"));

        verify(criarUsuarioUseCase, never()).criar(any());
    }

    @Test
    void metodoHttpNaoAceitoResponde405NoNossoFormato() throws Exception {
        logarComoAna();

        mockMvc.perform(patch("/users/" + ID_DA_ANA).with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.mensagem").value(startsWith("o metodo PATCH nao e aceito nesta rota")))
                // o formato padrao do spring traria "trace" e "error"; o nosso nao
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void rotaInexistenteResponde404NoNossoFormato() throws Exception {
        logarComoAna();

        mockMvc.perform(get("/usuarios").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("a rota /usuarios nao existe"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                // confere o texto do json, porque o jsonPath doesNotExist tambem passaria com um "campos": null
                .andExpect(content().string(not(containsString("\"campos\""))));
    }

    @Test
    void cadastroComJsonQuebradoResponde400() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{\"nome\": \"Ana\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("o corpo da requisicao esta mal formatado"));
    }

    @Test
    void atualizacaoComEmailInvalidoResponde400ENaoAtualiza() throws Exception {
        logarComoAna();
        String corpo = "{\"nome\":\"Ana Souza\",\"email\":\"sem-arroba\",\"cpf\":\"12345678901\"}";

        // este metodo tem o id validado e o corpo com @Valid: o erro do corpo chega por outro handler,
        // mas a resposta precisa ser a mesma
        mockMvc.perform(put("/users/" + ID_DA_ANA).with(httpBasic(EMAIL, SENHA))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("existem campos invalidos na requisicao"))
                .andExpect(jsonPath("$.campos.email").value("o email esta em formato invalido"));

        verify(atualizarUsuarioUseCase, never()).atualizar(anyLong(), any());
    }

    @Test
    void atualizacaoComSenhaCurtaResponde400() throws Exception {
        logarComoAna();
        // a senha e opcional no PUT, mas se vier tem que seguir a mesma regra do cadastro
        String corpo = "{\"nome\":\"Ana Souza\",\"email\":\"ana@email.com\",\"cpf\":\"12345678901\",\"senha\":\"12\"}";

        mockMvc.perform(put("/users/" + ID_DA_ANA).with(httpBasic(EMAIL, SENHA))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.senha").value("a senha deve ter pelo menos 6 caracteres"));
    }

    @Test
    void tamanhoDePaginaZeroResponde400() throws Exception {
        logarComoAna();

        mockMvc.perform(get("/users").param("tamanho", "0").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.tamanho").value("o tamanho da pagina deve ser de pelo menos 1"));

        verify(listarUsuariosUseCase, never()).listar(anyInt(), anyInt());
    }

    @Test
    void tamanhoDePaginaAcimaDoMaximoEPaginaNegativaRespondem400Juntos() throws Exception {
        logarComoAna();

        mockMvc.perform(get("/users").param("pagina", "-1").param("tamanho", "500").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.pagina").value("a pagina comeca em 0 e nao pode ser negativa"))
                .andExpect(jsonPath("$.campos.tamanho").value("o tamanho da pagina pode ser de no maximo 100"));
    }

    @Test
    void paginaComTextoNoLugarDeNumeroResponde400() throws Exception {
        logarComoAna();

        mockMvc.perform(get("/users").param("pagina", "abc").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("o valor 'abc' nao e valido para o parametro 'pagina'"));
    }

    @Test
    void idNegativoNaUrlResponde400() throws Exception {
        logarComoAna();

        mockMvc.perform(delete("/users/-5").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.id").value("o id deve ser um numero positivo"));

        verify(excluirUsuarioUseCase, never()).excluir(anyLong());
    }

    @Test
    void verificarCredenciaisComCamposVaziosResponde400() throws Exception {
        mockMvc.perform(post("/auth/verificar").header(VerificadorDeChaveInterna.CABECALHO, CHAVE_INTERNA)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").value("o email e obrigatorio"))
                .andExpect(jsonPath("$.campos.senha").value("a senha e obrigatoria"));

        verify(verificarCredenciaisUseCase, never()).verificar(anyString(), anyString());
    }

    @Test
    void verificarCredenciaisComSenhaGiganteResponde400SemProcurarNoBanco() throws Exception {
        // 73 letras: uma a mais do que o bcrypt aceita
        String senhaGigante = "a".repeat(73);
        String corpo = "{\"email\":\"ana@email.com\",\"senha\":\"" + senhaGigante + "\"}";

        mockMvc.perform(post("/auth/verificar").header(VerificadorDeChaveInterna.CABECALHO, CHAVE_INTERNA)
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.senha").value(SENHA_ACIMA_DO_BCRYPT));

        verify(verificarCredenciaisUseCase, never()).verificar(anyString(), anyString());
    }

    @Test
    void cadastroComSenhaDeLetrasComAcentoAcimaDe72BytesResponde400() throws Exception {
        // 37 letras "c com cedilha" = 74 bytes: passariam num @Size(max = 72), mas quebrariam o bcrypt
        String senha = "ç".repeat(37);
        String corpo = "{\"nome\":\"Ana Souza\",\"email\":\"ana@email.com\",\"cpf\":\"12345678901\",\"senha\":\""
                + senha + "\"}";

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.senha").value(SENHA_ACIMA_DO_BCRYPT));

        verify(criarUsuarioUseCase, never()).criar(any());
    }

    @Test
    void corpoEnviadoComoListaRespondeMalFormatado() throws Exception {
        // mandaram [ {...} ] no lugar de { ... }: o jackson nao chega em nenhum campo, entao a resposta e a de
        // corpo mal formatado (sem "campos")
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("[{\"nome\":\"Ana\"}]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("o corpo da requisicao esta mal formatado"))
                .andExpect(jsonPath("$.campos").doesNotExist());

        verify(criarUsuarioUseCase, never()).criar(any());
    }

    // apoio: faz o login de mentira dar certo como a ana (id 7)
    private void logarComoAna() {
        Usuario ana = new Usuario(ID_DA_ANA, "Ana Souza", EMAIL, "12345678901", "hash",
                LocalDateTime.of(2026, 11, 5, 10, 0));
        when(verificarCredenciaisUseCase.verificar(EMAIL, SENHA)).thenReturn(ana);
    }
}
