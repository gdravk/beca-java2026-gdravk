package br.com.gabrieloliveira.usuarios.adapter.in.web.csv;

import br.com.gabrieloliveira.usuarios.adapter.in.web.UsuarioWebMapper;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.UsuarioRequestDTO;
import br.com.gabrieloliveira.usuarios.domain.model.LinhaImportacao;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// le o arquivo csv de usuarios e transforma cada linha numa LinhaImportacao (valida ou invalida)
// faz parte do adaptador web: o csv e so o "formato de entrada" que o cliente mandou,
// o caso de uso (ImportarUsuariosService) nem sabe que existe csv
//
// layout do arquivo (documentado em docs/exemplos/usuarios-exemplo.md):
// - primeira linha: cabecalho nome;email;cpf;senha
// - separador: ponto e virgula
// - codificacao: utf-8 (com ou sem BOM)
//
// a leitura e feita "na mao", linha por linha, sem biblioteca de csv:
// o layout e simples (4 colunas, sem aspas), entao um split resolve e nao precisa mexer no pom
@Component
public class LeitorCsvUsuarios {

    private static final String SEPARADOR = ";";
    private static final String CABECALHO_ESPERADO = "nome;email;cpf;senha";
    private static final int QUANTIDADE_COLUNAS = 4;

    // limite pra ninguem mandar um arquivo gigante e travar a api
    private static final int MAXIMO_LINHAS = 1000;

    // o BOM e um caractere invisivel que o excel e o bloco de notas colocam no comeco de arquivos utf-8
    // se ele nao for removido, o cabecalho vira "\uFEFFnome;email;cpf;senha" e nao bate com o esperado
    private static final char BOM = '\uFEFF';

    // a ordem dos campos no arquivo, usada pra montar a mensagem de erro sempre na mesma ordem
    private static final List<String> ORDEM_DOS_CAMPOS = List.of("nome", "email", "cpf", "senha");

    // o Validator e quem roda as anotacoes de validacao (@NotBlank, @Email...) "na mao"
    // no POST /users quem chama ele e o @Valid do controller; aqui chamamos direto, linha por linha
    private final Validator validator;
    private final UsuarioWebMapper usuarioWebMapper;

    public LeitorCsvUsuarios(Validator validator, UsuarioWebMapper usuarioWebMapper) {
        this.validator = validator;
        this.usuarioWebMapper = usuarioWebMapper;
    }

    public List<LinhaImportacao> ler(byte[] conteudo) {
        String texto = paraTextoUtf8(conteudo);

        // tira o BOM, se tiver
        if (!texto.isEmpty() && texto.charAt(0) == BOM) {
            texto = texto.substring(1);
        }

        // lines() quebra o texto nas linhas, funcionando tanto com quebra do windows (\r\n) quanto do linux (\n)
        List<String> linhasDoArquivo = texto.lines().toList();

        if (linhasDoArquivo.isEmpty() || linhasDoArquivo.get(0).isBlank()) {
            throw new ArquivoCsvInvalidoException("o arquivo esta vazio ou a primeira linha (cabecalho) esta em branco");
        }

        conferirCabecalho(linhasDoArquivo.get(0));

        List<LinhaImportacao> linhas = new ArrayList<>();
        // comeca do indice 1 pra pular o cabecalho
        for (int indice = 1; indice < linhasDoArquivo.size(); indice++) {
            String conteudoDaLinha = linhasDoArquivo.get(indice);

            // linha em branco (ex: um enter sobrando no fim do arquivo) e ignorada, nao conta como falha
            if (conteudoDaLinha.isBlank()) {
                continue;
            }

            // o indice comeca em 0 e o editor de texto comeca em 1, por isso o + 1
            // assim a "linha 12" do resumo e a mesma linha 12 que a pessoa ve no editor
            int numeroLinha = indice + 1;
            linhas.add(lerLinha(numeroLinha, conteudoDaLinha));
        }

        if (linhas.isEmpty()) {
            throw new ArquivoCsvInvalidoException("o arquivo so tem o cabecalho, nenhum usuario para importar");
        }
        if (linhas.size() > MAXIMO_LINHAS) {
            throw new ArquivoCsvInvalidoException(
                    "o arquivo pode ter no maximo " + MAXIMO_LINHAS + " usuarios, mas tem " + linhas.size());
        }

        return linhas;
    }

    // converte os bytes do arquivo em texto, exigindo utf-8
    // o decoder com REPORT reclama se aparecer um byte que nao e utf-8 valido
    // (ex: arquivo salvo como ANSI/latin-1 no bloco de notas antigo); sem isso, "Joao" com til viraria "Jo?o"
    private String paraTextoUtf8(byte[] conteudo) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(conteudo))
                    .toString();
        } catch (CharacterCodingException e) {
            throw new ArquivoCsvInvalidoException("o arquivo precisa estar salvo com a codificacao UTF-8");
        }
    }

    // o cabecalho precisa ter as 4 colunas na ordem certa
    // maiusculas e espacos em volta dos nomes sao aceitos (ex: "Nome ; Email ; CPF ; Senha")
    private void conferirCabecalho(String cabecalho) {
        String normalizado = cabecalho.toLowerCase(Locale.ROOT).replace(" ", "").trim();

        if (normalizado.equals(CABECALHO_ESPERADO)) {
            return;
        }

        // erro bem comum: salvar com virgula no lugar do ponto e virgula; vale uma mensagem especifica
        if (normalizado.equals(CABECALHO_ESPERADO.replace(SEPARADOR, ","))) {
            throw new ArquivoCsvInvalidoException("o separador do arquivo deve ser ponto e virgula (;), e nao virgula");
        }

        throw new ArquivoCsvInvalidoException(
                "cabecalho invalido: a primeira linha deve ser exatamente " + CABECALHO_ESPERADO);
    }

    private LinhaImportacao lerLinha(int numeroLinha, String conteudoDaLinha) {
        // o -1 no split faz ele manter as colunas vazias do fim
        // sem ele, "Ana;ana@email.com;12345678901;" viraria 3 colunas em vez de 4 (com a senha vazia)
        String[] colunas = conteudoDaLinha.split(SEPARADOR, -1);

        if (colunas.length != QUANTIDADE_COLUNAS) {
            return LinhaImportacao.invalida(numeroLinha, "a linha deve ter " + QUANTIDADE_COLUNAS
                    + " colunas (nome;email;cpf;senha), mas tem " + colunas.length);
        }

        // monta o mesmo dto do POST /users, pra usar exatamente as mesmas validacoes do cadastro
        // o trim tira espacos sobrando nas pontas, que sao comuns quando o arquivo e editado na mao
        // a senha e a excecao: ela vai exatamente como esta no arquivo, igual no POST /users
        // (se tirassemos o espaco de "Senha@123 ", a pessoa digitaria a senha com espaco no login e nao entraria)
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome(colunas[0].trim());
        dto.setEmail(colunas[1].trim());
        dto.setCpf(colunas[2].trim());
        dto.setSenha(colunas[3]);

        Set<ConstraintViolation<UsuarioRequestDTO>> violacoes = validator.validate(dto);
        if (!violacoes.isEmpty()) {
            return LinhaImportacao.invalida(numeroLinha, juntarMensagens(violacoes));
        }

        return LinhaImportacao.valida(numeroLinha, usuarioWebMapper.paraDominio(dto));
    }

    // junta todos os problemas da linha numa mensagem so, ex: "o nome e obrigatorio; o cpf deve ter..."
    // o Set de violacoes nao tem ordem garantida, entao percorremos os campos na ordem do arquivo
    // e, dentro do mesmo campo, ordenamos as mensagens (um cpf vazio, por exemplo, quebra duas regras:
    // obrigatorio e 11 numeros); assim a mensagem sai sempre igual (e o teste consegue conferir)
    private String juntarMensagens(Set<ConstraintViolation<UsuarioRequestDTO>> violacoes) {
        List<String> mensagens = new ArrayList<>();
        for (String campo : ORDEM_DOS_CAMPOS) {
            List<String> mensagensDoCampo = new ArrayList<>();
            for (ConstraintViolation<UsuarioRequestDTO> violacao : violacoes) {
                if (violacao.getPropertyPath().toString().equals(campo)) {
                    mensagensDoCampo.add(violacao.getMessage());
                }
            }
            Collections.sort(mensagensDoCampo);
            mensagens.addAll(mensagensDoCampo);
        }
        return String.join("; ", mensagens);
    }
}
