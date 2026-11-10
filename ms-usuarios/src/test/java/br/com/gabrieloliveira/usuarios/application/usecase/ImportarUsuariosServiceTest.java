package br.com.gabrieloliveira.usuarios.application.usecase;

import br.com.gabrieloliveira.usuarios.application.port.in.CriarUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.domain.exception.RegraNegocioException;
import br.com.gabrieloliveira.usuarios.domain.model.LinhaImportacao;
import br.com.gabrieloliveira.usuarios.domain.model.ResultadoImportacao;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// testes do caso de uso de importacao em massa
// o ImportarUsuariosService depende so do CriarUsuarioUseCase, que aqui e um mock: cada teste diz se o
// "criar" deu certo ou lancou erro, e conferimos se o resumo saiu certo
@ExtendWith(MockitoExtension.class)
class ImportarUsuariosServiceTest {

    @Mock
    private CriarUsuarioUseCase criarUsuarioUseCase;

    private ImportarUsuariosService importarUsuariosService;

    @BeforeEach
    void setUp() {
        importarUsuariosService = new ImportarUsuariosService(criarUsuarioUseCase);
    }

    @Test
    void deveImportarTodasAsLinhasValidas() {
        // dado: tres linhas validas
        List<LinhaImportacao> linhas = List.of(
                LinhaImportacao.valida(2, usuario("ana@email.com")),
                LinhaImportacao.valida(3, usuario("bruno@email.com")),
                LinhaImportacao.valida(4, usuario("carla@email.com")));

        // quando
        ResultadoImportacao resultado = importarUsuariosService.importar(linhas);

        // entao: as tres entraram e nenhuma falhou
        assertEquals(3, resultado.getTotalLinhas());
        assertEquals(3, resultado.getImportados());
        assertEquals(0, resultado.getRejeitados());
        verify(criarUsuarioUseCase, times(3)).criar(any(Usuario.class));
    }

    @Test
    void linhaInvalidaDaLeituraViraFalhaESemChamarOCadastro() {
        // dado: uma linha so, que ja veio invalida do leitor do csv
        List<LinhaImportacao> linhas = List.of(LinhaImportacao.invalida(5, "o email esta em formato invalido"));

        ResultadoImportacao resultado = importarUsuariosService.importar(linhas);

        // entao: ela virou falha, com a linha e o motivo, e o cadastro nem foi chamado
        assertEquals(1, resultado.getRejeitados());
        assertEquals(5, resultado.getFalhas().get(0).getLinha());
        assertEquals("o email esta em formato invalido", resultado.getFalhas().get(0).getMotivo());
        verify(criarUsuarioUseCase, never()).criar(any());
    }

    @Test
    void emailRepetidoEFalhaDaLinhaENaoDerrubaAsOutras() {
        // dado: a linha 3 tem um email que ja existe; as linhas 2 e 4 sao normais
        Usuario repetido = usuario("repetido@email.com");
        List<LinhaImportacao> linhas = List.of(
                LinhaImportacao.valida(2, usuario("ana@email.com")),
                LinhaImportacao.valida(3, repetido),
                LinhaImportacao.valida(4, usuario("carla@email.com")));
        // o criar de mentira lanca a mesma excecao do cadastro de verdade, so para o usuario repetido
        when(criarUsuarioUseCase.criar(any(Usuario.class))).thenAnswer(chamada -> {
            Usuario recebido = chamada.getArgument(0);
            if (recebido == repetido) {
                throw new RegraNegocioException("ja existe um usuario cadastrado com esse email");
            }
            return recebido;
        });

        ResultadoImportacao resultado = importarUsuariosService.importar(linhas);

        // entao: a linha 3 falhou com o motivo do cadastro, e a linha 4 (que vem depois do erro) entrou mesmo assim
        assertEquals(3, resultado.getTotalLinhas());
        assertEquals(2, resultado.getImportados());
        assertEquals(1, resultado.getRejeitados());
        assertEquals(3, resultado.getFalhas().get(0).getLinha());
        assertEquals("ja existe um usuario cadastrado com esse email", resultado.getFalhas().get(0).getMotivo());
        verify(criarUsuarioUseCase, times(3)).criar(any(Usuario.class));
    }

    @Test
    void erroInesperadoAoSalvarTambemViraFalhaDaLinha() {
        // dado: o banco falha de um jeito inesperado na unica linha
        when(criarUsuarioUseCase.criar(any(Usuario.class))).thenThrow(new IllegalStateException("banco caiu"));

        ResultadoImportacao resultado = importarUsuariosService.importar(
                List.of(LinhaImportacao.valida(2, usuario("ana@email.com"))));

        // entao: a excecao nao escapa (a importacao nao quebra) e vira falha da linha
        assertEquals(0, resultado.getImportados());
        assertEquals(1, resultado.getRejeitados());
        assertEquals(2, resultado.getFalhas().get(0).getLinha());
    }

    @Test
    void misturaDeLinhasValidasEInvalidasMantemAOrdemDasFalhas() {
        // dado: uma linha invalida, uma valida e outra invalida
        List<LinhaImportacao> linhas = List.of(
                LinhaImportacao.invalida(2, "o nome e obrigatorio"),
                LinhaImportacao.valida(3, usuario("bruno@email.com")),
                LinhaImportacao.invalida(4, "a senha deve ter pelo menos 6 caracteres"));

        ResultadoImportacao resultado = importarUsuariosService.importar(linhas);

        // entao: as falhas aparecem na mesma ordem do arquivo
        assertEquals(3, resultado.getTotalLinhas());
        assertEquals(1, resultado.getImportados());
        assertEquals(2, resultado.getRejeitados());
        assertEquals(2, resultado.getFalhas().get(0).getLinha());
        assertEquals(4, resultado.getFalhas().get(1).getLinha());
    }

    private Usuario usuario(String email) {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuario Teste");
        usuario.setEmail(email);
        usuario.setCpf("12345678901");
        usuario.setSenha("Senha@123");
        return usuario;
    }
}
