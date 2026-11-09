package br.com.gabrieloliveira.usuarios.adapter.in.web;

import br.com.gabrieloliveira.usuarios.adapter.in.web.csv.ArquivoCsvInvalidoException;
import br.com.gabrieloliveira.usuarios.adapter.in.web.csv.LeitorCsvUsuarios;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.ErroResponseDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.ImportacaoResultadoDTO;
import br.com.gabrieloliveira.usuarios.application.port.in.ImportarUsuariosUseCase;
import br.com.gabrieloliveira.usuarios.domain.model.LinhaImportacao;
import br.com.gabrieloliveira.usuarios.domain.model.ResultadoImportacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

// adaptador de entrada do cadastro em massa (pedido 4 do PO): POST /users/import
// fica separado do UsuarioController pra cada controller cuidar de uma coisa so
// (ele continua em /users, entao a rota final e /users/import)
//
// o caminho de uma importacao:
// 1. aqui: confere se veio um arquivo .csv e nao vazio
// 2. LeitorCsvUsuarios: le e valida linha por linha
// 3. ImportarUsuariosUseCase: cadastra cada linha valida e monta o resumo
// 4. aqui de novo: converte o resumo em json e responde 200
@RestController
@RequestMapping("/users")
@Tag(name = "Importacao de usuarios", description = "cadastro em massa a partir de um arquivo csv")
public class ImportacaoUsuariosController {

    private static final String EXTENSAO_CSV = ".csv";

    private final LeitorCsvUsuarios leitorCsvUsuarios;
    private final ImportarUsuariosUseCase importarUsuariosUseCase;
    private final UsuarioWebMapper usuarioWebMapper;

    public ImportacaoUsuariosController(LeitorCsvUsuarios leitorCsvUsuarios,
                                        ImportarUsuariosUseCase importarUsuariosUseCase,
                                        UsuarioWebMapper usuarioWebMapper) {
        this.leitorCsvUsuarios = leitorCsvUsuarios;
        this.importarUsuariosUseCase = importarUsuariosUseCase;
        this.usuarioWebMapper = usuarioWebMapper;
    }

    // o arquivo chega como multipart/form-data, que e o formato de "envio de arquivo" do http
    // (no postman: Body -> form-data -> chave "file" do tipo File)
    // o consumes avisa o spring (e o swagger) que essa rota recebe arquivo, e nao json;
    // por causa dele o swagger mostra um botao de escolher arquivo
    // a resposta e 200 mesmo quando algumas linhas falham: o arquivo foi processado, e o resumo diz o que entrou
    @Operation(summary = "Importa usuarios de um arquivo csv",
            description = "Arquivo .csv em UTF-8, separado por ponto e virgula, com o cabecalho nome;email;cpf;senha. "
                    + "Ate 1000 usuarios e 2MB. Linhas com erro nao impedem as outras: a resposta conta "
                    + "quantas entraram e lista o motivo de cada linha rejeitada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "arquivo processado (veja importados, rejeitados e falhas)"),
            @ApiResponse(responseCode = "400", description = "arquivo vazio, nao e .csv, cabecalho errado ou faltou o arquivo",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "sem login ou com email/senha errados",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "413", description = "arquivo maior que 2MB",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportacaoResultadoDTO> importar(
            @Parameter(description = "o arquivo .csv com os usuarios")
            @RequestParam("file") MultipartFile file) {
        conferirArquivo(file);

        List<LinhaImportacao> linhas = leitorCsvUsuarios.ler(lerConteudo(file));
        ResultadoImportacao resultado = importarUsuariosUseCase.importar(linhas);

        return ResponseEntity.ok(usuarioWebMapper.paraImportacaoResponse(resultado));
    }

    private void conferirArquivo(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ArquivoCsvInvalidoException("o arquivo enviado esta vazio");
        }

        // getOriginalFilename e o nome que o arquivo tinha no computador de quem enviou (ex: usuarios-exemplo.csv)
        String nome = file.getOriginalFilename();
        if (nome == null || !nome.toLowerCase(Locale.ROOT).endsWith(EXTENSAO_CSV)) {
            throw new ArquivoCsvInvalidoException("o arquivo precisa ser .csv");
        }
    }

    // getBytes pode dar IOException se a leitura do upload falhar no meio; ai respondemos 400 com uma mensagem clara
    private byte[] lerConteudo(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new ArquivoCsvInvalidoException("nao foi possivel ler o arquivo enviado");
        }
    }
}
