package br.com.gabrieloliveira.usuarios.application.usecase;

import br.com.gabrieloliveira.usuarios.application.port.out.SenhaEncoderPort;
import br.com.gabrieloliveira.usuarios.application.port.out.UsuarioRepositoryPort;
import br.com.gabrieloliveira.usuarios.domain.exception.RegraNegocioException;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// testes unitarios do caso de uso de criar usuario
// o CriarUsuarioService depende de duas portas de saida: o banco (UsuarioRepositoryPort) e o bcrypt (SenhaEncoderPort)
// no teste a gente nao usa o banco nem o bcrypt de verdade: o mockito cria versoes "de mentira" (mocks)
// e cada teste diz o que esses mocks respondem; assim testamos so a regra do caso de uso, sem docker e bem rapido
// isso so e possivel por causa da arquitetura hexagonal: o caso de uso conhece so as interfaces (portas)

// @ExtendWith(MockitoExtension.class) liga o mockito no junit: ele cria os @Mock antes de cada teste
// e, no fim, avisa se algum "when" foi configurado e nunca usado (ajuda a manter o teste limpo)
@ExtendWith(MockitoExtension.class)
class CriarUsuarioServiceTest {

    // @Mock = objeto falso criado pelo mockito; por padrao, metodos boolean devolvem false e os outros devolvem null
    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private SenhaEncoderPort senhaEncoderPort;

    // a classe que esta sendo testada (o "alvo" do teste)
    private CriarUsuarioService criarUsuarioService;

    // @BeforeEach roda antes de CADA teste: assim todo teste comeca com um service novo e limpo
    // criamos o service "na mao", igual o BeanConfig faz na aplicacao, so que entregando os mocks
    @BeforeEach
    void setUp() {
        // relogio parado em 06/11/2026 01:00 em utc, que no brasil ainda e 05/11 as 22h
        Clock relogio = Clock.fixed(Instant.parse("2026-11-06T01:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        criarUsuarioService = new CriarUsuarioService(usuarioRepositoryPort, senhaEncoderPort, relogio);
    }

    @Test
    void deveCriarUsuarioComSenhaCriptografadaEDataDeCriacao() {
        // dado: email e cpf ainda nao cadastrados, e um bcrypt de mentira que devolve um hash fixo
        Usuario novo = novoUsuario("ana@email.com", "12345678901", "segredo123");
        when(usuarioRepositoryPort.existePorEmail("ana@email.com")).thenReturn(false);
        when(usuarioRepositoryPort.existePorCpf("12345678901")).thenReturn(false);
        when(senhaEncoderPort.codificar("segredo123")).thenReturn("hash-da-senha");
        // thenAnswer: em vez de um valor fixo, o mock roda esse codigo quando "salvar" for chamado
        // aqui ele imita o banco: pega o usuario recebido, coloca o id 1 e devolve
        when(usuarioRepositoryPort.salvar(any(Usuario.class))).thenAnswer(chamada -> {
            Usuario recebido = chamada.getArgument(0);
            recebido.setId(1L);
            return recebido;
        });

        // quando: o caso de uso cria o usuario
        Usuario criado = criarUsuarioService.criar(novo);

        // entao: ele foi salvo com o id do banco, com o hash (nunca a senha pura) e com a data de criacao
        assertEquals(1L, criado.getId());
        assertEquals("hash-da-senha", criado.getSenha());
        assertNotNull(criado.getDataCriacao());
        // a data de criacao e a de brasilia (05/11 as 22h), e nao a de utc (06/11 a 01h)
        assertEquals(LocalDateTime.of(2026, 11, 5, 22, 0), criado.getDataCriacao());
        // verify confere se um metodo do mock foi chamado; aqui: o salvar foi chamado uma vez
        verify(usuarioRepositoryPort).salvar(novo);
    }

    @Test
    void naoDeveCriarUsuarioComEmailRepetido() {
        // dado: o banco (de mentira) diz que o email ja existe
        Usuario novo = novoUsuario("ana@email.com", "12345678901", "segredo123");
        when(usuarioRepositoryPort.existePorEmail("ana@email.com")).thenReturn(true);

        // quando / entao: criar tem que lancar RegraNegocioException (que vira 409 na api)
        // assertThrows roda o codigo dentro do "() -> ..." e falha se a excecao NAO acontecer
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> criarUsuarioService.criar(novo));
        assertEquals("ja existe um usuario cadastrado com esse email", erro.getMessage());

        // e o mais importante: nada foi salvo e a senha nem chegou a ser criptografada
        verify(usuarioRepositoryPort, never()).salvar(any());
        verify(senhaEncoderPort, never()).codificar(anyString());
    }

    @Test
    void naoDeveCriarUsuarioComCpfRepetido() {
        // dado: email livre, mas o cpf ja existe
        Usuario novo = novoUsuario("ana@email.com", "12345678901", "segredo123");
        when(usuarioRepositoryPort.existePorEmail("ana@email.com")).thenReturn(false);
        when(usuarioRepositoryPort.existePorCpf("12345678901")).thenReturn(true);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> criarUsuarioService.criar(novo));
        assertEquals("ja existe um usuario cadastrado com esse cpf", erro.getMessage());

        verify(usuarioRepositoryPort, never()).salvar(any());
    }

    @Test
    void deveConferirDuplicidadeUsandoOEmailNormalizado() {
        // dado: o email chega com espacos e maiusculas
        Usuario novo = novoUsuario("  Ana@Email.COM ", "12345678901", "segredo123");

        // quando
        criarUsuarioService.criar(novo);

        // entao: a consulta de duplicidade foi feita com o email ja normalizado
        // (sem isso, "Ana@Email.COM" passaria mesmo existindo "ana@email.com" no banco)
        verify(usuarioRepositoryPort).existePorEmail("ana@email.com");
    }

    // metodo de apoio pra nao repetir a montagem do usuario em todo teste
    private Usuario novoUsuario(String email, String cpf, String senha) {
        Usuario usuario = new Usuario();
        usuario.setNome("Ana Souza");
        usuario.setEmail(email);
        usuario.setCpf(cpf);
        usuario.setSenha(senha);
        return usuario;
    }
}
