package br.com.gabrieloliveira.usuarios.adapter.in.web.csv;

import br.com.gabrieloliveira.usuarios.adapter.in.web.UsuarioWebMapper;
import br.com.gabrieloliveira.usuarios.domain.model.LinhaImportacao;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// testes do leitor de csv
// aqui nao tem mock: o leitor so precisa do validador (o mesmo que o spring usa) e do mapper,
// e os dois sao objetos simples que funcionam sem subir a aplicacao nem o banco
class LeitorCsvUsuariosTest {

    private static final String CABECALHO = "nome;email;cpf;senha\n";

    // a fabrica de validadores e "pesada" de criar, entao criamos uma vez so pra todos os testes (@BeforeAll)
    // e fechamos no fim (@AfterAll); por isso esses dois metodos sao static
    private static ValidatorFactory fabricaDeValidadores;

    private LeitorCsvUsuarios leitor;

    @BeforeAll
    static void criarValidador() {
        fabricaDeValidadores = Validation.buildDefaultValidatorFactory();
    }

    @AfterAll
    static void fecharValidador() {
        fabricaDeValidadores.close();
    }

    @BeforeEach
    void setUp() {
        leitor = new LeitorCsvUsuarios(fabricaDeValidadores.getValidator(), new UsuarioWebMapper());
    }

    @Test
    void deveLerLinhasValidasComNumeroDaLinhaDoArquivo() {
        String csv = CABECALHO
                + "Ana Souza;ana@email.com;12345678901;Senha@123\n"
                + "Bruno Lima;bruno@email.com;12345678902;Senha@123\n";

        List<LinhaImportacao> linhas = leitor.ler(bytes(csv));

        // o cabecalho e a linha 1, entao os usuarios sao as linhas 2 e 3
        assertEquals(2, linhas.size());
        assertTrue(linhas.get(0).isValida());
        assertEquals(2, linhas.get(0).getNumeroLinha());
        assertEquals("Ana Souza", linhas.get(0).getUsuario().getNome());
        assertEquals(3, linhas.get(1).getNumeroLinha());
    }

    @Test
    void deveAceitarArquivoComBomEQuebraDeLinhaDoWindows() {
        // ﻿ = BOM (o excel coloca no comeco), \r\n = quebra de linha do windows
        String csv = "﻿nome;email;cpf;senha\r\nJoão Pereira;joao@email.com;12345678901;Senha@123\r\n";

        List<LinhaImportacao> linhas = leitor.ler(bytes(csv));

        assertEquals(1, linhas.size());
        assertTrue(linhas.get(0).isValida());
        // o acento do utf-8 chegou certinho
        assertEquals("João Pereira", linhas.get(0).getUsuario().getNome());
    }

    @Test
    void deveNormalizarEmailETirarEspacosDasColunas() {
        String csv = CABECALHO + "  Ana Souza ; Ana@Email.COM ; 12345678901 ;Senha@123\n";

        LinhaImportacao linha = leitor.ler(bytes(csv)).get(0);

        assertTrue(linha.isValida());
        assertEquals("Ana Souza", linha.getUsuario().getNome());
        // o dominio deixa o email em minusculo, igual no cadastro normal
        assertEquals("ana@email.com", linha.getUsuario().getEmail());
        assertEquals("12345678901", linha.getUsuario().getCpf());
    }

    @Test
    void senhaFicaExatamenteComoNoArquivoComEspacos() {
        // igual ao POST /users: a senha nao passa por trim, entao o espaco faz parte dela
        String csv = CABECALHO + "Ana Souza;ana@email.com;12345678901; Senha@123 \n";

        LinhaImportacao linha = leitor.ler(bytes(csv)).get(0);

        assertTrue(linha.isValida());
        assertEquals(" Senha@123 ", linha.getUsuario().getSenha());
    }

    @Test
    void linhaComEmailInvalidoViraLinhaInvalidaComOMotivo() {
        String csv = CABECALHO + "Ana Souza;ana-email.com;12345678901;Senha@123\n";

        LinhaImportacao linha = leitor.ler(bytes(csv)).get(0);

        assertFalse(linha.isValida());
        assertEquals("o email esta em formato invalido", linha.getErro());
    }

    @Test
    void linhaComVariosErrosJuntaAsMensagensNaOrdemDasColunas() {
        // nome vazio e senha curta na mesma linha
        String csv = CABECALHO + ";ana@email.com;12345678901;123\n";

        LinhaImportacao linha = leitor.ler(bytes(csv)).get(0);

        assertFalse(linha.isValida());
        assertEquals("o nome e obrigatorio; a senha deve ter entre 6 e 100 caracteres", linha.getErro());
    }

    @Test
    void linhaComQuantidadeErradaDeColunasViraLinhaInvalida() {
        String csv = CABECALHO + "Ana Souza;ana@email.com;12345678901\n";

        LinhaImportacao linha = leitor.ler(bytes(csv)).get(0);

        assertFalse(linha.isValida());
        assertEquals("a linha deve ter 4 colunas (nome;email;cpf;senha), mas tem 3", linha.getErro());
    }

    @Test
    void linhasEmBrancoSaoIgnoradasMasANumeracaoContinuaCerta() {
        // linha 2 em branco: o usuario esta na linha 3 do arquivo
        String csv = CABECALHO + "\n" + "Ana Souza;ana@email.com;12345678901;Senha@123\n\n";

        List<LinhaImportacao> linhas = leitor.ler(bytes(csv));

        assertEquals(1, linhas.size());
        assertEquals(3, linhas.get(0).getNumeroLinha());
    }

    @Test
    void arquivoVazioEErroDoArquivo() {
        ArquivoCsvInvalidoException erro = assertThrows(ArquivoCsvInvalidoException.class,
                () -> leitor.ler(bytes("")));
        assertEquals("o arquivo esta vazio ou a primeira linha (cabecalho) esta em branco", erro.getMessage());
    }

    @Test
    void arquivoSoComCabecalhoEErroDoArquivo() {
        ArquivoCsvInvalidoException erro = assertThrows(ArquivoCsvInvalidoException.class,
                () -> leitor.ler(bytes(CABECALHO)));
        assertEquals("o arquivo so tem o cabecalho, nenhum usuario para importar", erro.getMessage());
    }

    @Test
    void cabecalhoComVirgulaDaUmaMensagemEspecifica() {
        String csv = "nome,email,cpf,senha\nAna Souza,ana@email.com,12345678901,Senha@123\n";

        ArquivoCsvInvalidoException erro = assertThrows(ArquivoCsvInvalidoException.class,
                () -> leitor.ler(bytes(csv)));
        assertEquals("o separador do arquivo deve ser ponto e virgula (;), e nao virgula", erro.getMessage());
    }

    @Test
    void cabecalhoEmMaiusculoEComEspacosEAceito() {
        String csv = "Nome ; Email ; CPF ; Senha\nAna Souza;ana@email.com;12345678901;Senha@123\n";

        assertEquals(1, leitor.ler(bytes(csv)).size());
    }

    @Test
    void cabecalhoComColunasErradasEErroDoArquivo() {
        String csv = "nome;email;senha\nAna Souza;ana@email.com;Senha@123\n";

        assertThrows(ArquivoCsvInvalidoException.class, () -> leitor.ler(bytes(csv)));
    }

    @Test
    void arquivoQueNaoEstaEmUtf8EErroDoArquivo() {
        // "João" salvo em ISO-8859-1 (o "ANSI" do bloco de notas antigo): o "ã" vira um byte que nao e utf-8 valido
        byte[] latin1 = (CABECALHO + "João;joao@email.com;12345678901;Senha@123\n")
                .getBytes(StandardCharsets.ISO_8859_1);

        ArquivoCsvInvalidoException erro = assertThrows(ArquivoCsvInvalidoException.class,
                () -> leitor.ler(latin1));
        assertEquals("o arquivo precisa estar salvo com a codificacao UTF-8", erro.getMessage());
    }

    private byte[] bytes(String texto) {
        return texto.getBytes(StandardCharsets.UTF_8);
    }
}
