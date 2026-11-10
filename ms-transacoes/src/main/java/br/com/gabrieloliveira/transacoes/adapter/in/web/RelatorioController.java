package br.com.gabrieloliveira.transacoes.adapter.in.web;

import br.com.gabrieloliveira.transacoes.adapter.in.web.csv.GeradorCsvRelatorio;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.ErroResponseDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.UsuarioLogado;
import br.com.gabrieloliveira.transacoes.application.port.in.GerarRelatorioUseCase;
import br.com.gabrieloliveira.transacoes.domain.model.RelatorioTransacoes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

// relatorio de transacoes em csv, para baixar (pedido 7 do PO)
//   GET /transactions/report?inicio=2026-11-01&fim=2026-11-30
// o arquivo so traz transacoes de quem esta logado: o usuarioId e opcional e, se vier, tem que ser o dele (senao 403)
// fica num controller separado porque devolve um arquivo, e nao json como as outras rotas de /transactions
@RestController
@RequestMapping("/transactions")
@Tag(name = "Relatorio", description = "download das transacoes do usuario logado em csv, para abrir no Excel")
public class RelatorioController {

    private static final String ID_POSITIVO = "o id deve ser um numero positivo";
    private static final String NOME_DO_ARQUIVO = "relatorio-transacoes.csv";
    // text/csv com charset utf-8: avisa o navegador (e o postman) qual e o formato e a codificacao do arquivo
    private static final MediaType TEXT_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final GerarRelatorioUseCase gerarRelatorioUseCase;
    private final GeradorCsvRelatorio geradorCsvRelatorio;
    private final UsuarioLogado usuarioLogado;

    public RelatorioController(GerarRelatorioUseCase gerarRelatorioUseCase,
                               GeradorCsvRelatorio geradorCsvRelatorio,
                               UsuarioLogado usuarioLogado) {
        this.gerarRelatorioUseCase = gerarRelatorioUseCase;
        this.geradorCsvRelatorio = geradorCsvRelatorio;
        this.usuarioLogado = usuarioLogado;
    }

    // o cabecalho "Content-Disposition: attachment" e o que faz o navegador BAIXAR o arquivo, em vez de mostrar
    // o texto na tela, e tambem da o nome dele; no swagger aparece o link "Download file"
    @Operation(summary = "Baixa o relatorio de transacoes em csv",
            description = "Arquivo UTF-8 separado por ponto e virgula, com numeros no formato brasileiro (150,75), "
                    + "pronto para abrir no Excel em portugues. Traz todas as transacoes do periodo (aprovadas e "
                    + "rejeitadas), da mais antiga para a mais nova, com o valor em reais, dolar e euro, e um "
                    + "resumo no fim. Sem datas, usa o mes atual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "arquivo csv para download",
                    content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "400", description = "data em formato errado ou inicio depois do fim",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "sem login ou com email/senha errados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "o usuarioId e de outro usuario",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "503", description = "ms1 fora do ar (nao da para conferir o login)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @GetMapping("/report")
    public ResponseEntity<byte[]> baixarRelatorio(
            @Parameter(description = "opcional; se vier, tem que ser o id do usuario logado", example = "1")
            @RequestParam(required = false) @Positive(message = ID_POSITIVO) Long usuarioId,
            @Parameter(description = "data inicial (aaaa-mm-dd); sem ela, comeca no dia 1 do mes do fim "
                    + "(ou do mes atual)", example = "2026-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "data final (aaaa-mm-dd), o dia inteiro entra; sem ela, vai ate o fim "
                    + "do mes atual", example = "2026-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        Long dono = usuarioLogado.resolverUsuario(usuarioId);
        RelatorioTransacoes relatorio = gerarRelatorioUseCase.gerar(dono, inicio, fim);
        byte[] arquivo = geradorCsvRelatorio.gerar(relatorio);

        ContentDisposition download = ContentDisposition.attachment().filename(NOME_DO_ARQUIVO).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, download.toString())
                .contentType(TEXT_CSV)
                .body(arquivo);
    }
}
