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
import br.com.gabrieloliveira.usuarios.domain.exception.CredenciaisInvalidasException;
import br.com.gabrieloliveira.usuarios.domain.model.Pagina;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// testes web da seguranca do ms1: fazem requisicoes http "de mentira" e conferem o status da resposta
//
// o @WebMvcTest sobe so a camada web (controllers, GlobalExceptionHandler e spring security), sem banco
// o @Import traz a nossa configuracao de seguranca e as pecas que ela usa
// o @MockitoBean troca os casos de uso por mocks, e cada teste diz o que eles respondem
// o MockMvc e quem faz as requisicoes, sem precisar subir um servidor de verdade
@WebMvcTest(controllers = {UsuarioController.class, AuthController.class, ImportacaoUsuariosController.class})
@Import({SecurityConfig.class, ErroDeSegurancaHandler.class, UsuarioWebMapper.class, VerificadorDeChaveInterna.class})
@TestPropertySource(properties = "seguranca.chave-interna=" + SegurancaWebTest.CHAVE_INTERNA)
class SegurancaWebTest {

    static final String CHAVE_INTERNA = "chave-interna-dos-testes-123";
    private static final String EMAIL = "ana@email.com";
    private static final String SENHA = "Senha@123";

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
    void rotaProtegidaSemLoginResponde401ComOCabecalhoDaAutenticacaoBasica() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"ms-usuarios\""))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void senhaErradaResponde401EConfereASenhaUmaVezSo() throws Exception {
        when(verificarCredenciaisUseCase.verificar(EMAIL, "errada")).thenThrow(new CredenciaisInvalidasException());

        mockMvc.perform(get("/users").with(httpBasic(EMAIL, "errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("email ou senha invalidos"));

        // o provider existe uma vez so: com a senha errada, o bcrypt nao roda duas vezes
        verify(verificarCredenciaisUseCase, times(1)).verificar(EMAIL, "errada");
    }

    @Test
    void cadastroContinuaAbertoSemLogin() throws Exception {
        when(criarUsuarioUseCase.criar(any(Usuario.class))).thenReturn(usuario(5L));

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@email.com\",\"cpf\":\"12345678901\",\"senha\":\"Senha@123\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void importacaoPorCsvExigeLogin() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile("file", "usuarios.csv", "text/csv",
                "nome;email;cpf;senha\n".getBytes());

        mockMvc.perform(multipart("/users/import").file(arquivo))
                .andExpect(status().isUnauthorized());

        verify(importarUsuariosUseCase, never()).importar(any());
    }

    @Test
    void buscarOsDadosDeOutroUsuarioResponde403() throws Exception {
        logarComo(1L);

        mockMvc.perform(get("/users/2").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("voce so pode ver os dados da sua propria conta"));

        verify(buscarUsuarioUseCase, never()).buscarPorId(any());
    }

    @Test
    void buscarOsPropriosDadosResponde200ComCpf() throws Exception {
        logarComo(1L);
        when(buscarUsuarioUseCase.buscarPorId(1L)).thenReturn(usuario(1L));

        mockMvc.perform(get("/users/1").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("12345678901"));
    }

    @Test
    void listagemNaoMostraOCpfDeNinguem() throws Exception {
        logarComo(1L);
        when(listarUsuariosUseCase.listar(0, 10)).thenReturn(new Pagina<>(List.of(usuario(1L), usuario(2L)), 0, 10, 2, 1));

        mockMvc.perform(get("/users").with(httpBasic(EMAIL, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].email").value(EMAIL))
                .andExpect(jsonPath("$.conteudo[0].cpf").doesNotExist())
                .andExpect(jsonPath("$.conteudo[1].cpf").doesNotExist());
    }

    @Test
    void authVerificarSemAChaveInternaResponde403ENemConfereASenha() throws Exception {
        mockMvc.perform(post("/auth/verificar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@email.com\",\"senha\":\"Senha@123\"}"))
                .andExpect(status().isForbidden());

        verify(verificarCredenciaisUseCase, never()).verificar(anyString(), anyString());
    }

    @Test
    void authVerificarComAChaveErradaResponde403() throws Exception {
        mockMvc.perform(post("/auth/verificar").header(VerificadorDeChaveInterna.CABECALHO, "chave-errada-qualquer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@email.com\",\"senha\":\"Senha@123\"}"))
                .andExpect(status().isForbidden());

        verify(verificarCredenciaisUseCase, never()).verificar(anyString(), anyString());
    }

    @Test
    void authVerificarComAChaveCertaDevolveOIdDoUsuario() throws Exception {
        when(verificarCredenciaisUseCase.verificar(EMAIL, SENHA)).thenReturn(usuario(1L));

        mockMvc.perform(post("/auth/verificar").header(VerificadorDeChaveInterna.CABECALHO, CHAVE_INTERNA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@email.com\",\"senha\":\"Senha@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(1));
    }

    // apoio: faz o login de mentira dar certo como o usuario com esse id
    private void logarComo(Long id) {
        when(verificarCredenciaisUseCase.verificar(EMAIL, SENHA)).thenReturn(usuario(id));
    }

    private Usuario usuario(Long id) {
        return new Usuario(id, "Ana Souza", EMAIL, "12345678901", "hash", LocalDateTime.of(2026, 11, 5, 10, 0));
    }
}
