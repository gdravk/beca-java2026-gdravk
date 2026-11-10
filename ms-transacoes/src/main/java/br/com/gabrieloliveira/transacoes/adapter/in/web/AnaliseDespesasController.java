package br.com.gabrieloliveira.transacoes.adapter.in.web;

import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.DespesasPorCategoriaResponseDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.DespesasPorDiaResponseDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.DespesasPorMesResponseDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.ErroResponseDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.UsuarioLogado;
import br.com.gabrieloliveira.transacoes.application.port.in.AnalisarDespesasUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// rotas da analise de despesas (pedido 5 do PO), uma para cada resumo:
//   GET /transactions/analysis/category?inicio=2026-11-01&fim=2026-11-30
//   GET /transactions/analysis/daily
//   GET /transactions/analysis/monthly?inicio=2026-01-01&fim=2026-12-31
// inicio e fim sao opcionais (formato aaaa-mm-dd); sem eles, a analise e do mes atual
// a analise e sempre do usuario logado: o usuarioId e opcional e, se vier, tem que ser o dele (senao 403)
@RestController
@RequestMapping("/transactions/analysis")
// os ids da url e do filtro sao validados direto no parametro: zero ou negativo responde 400
// (o spring mvc faz essa validacao sozinho, sem @Validated, igual no ms1)
//
// no swagger, as tres rotas recebem os mesmos parametros e podem dar os mesmos erros
// despesa = saida aprovada (RETIRADA, TRANSFERENCIA ou COMPRA) que nao e um estorno e nao foi estornada
@Tag(name = "Analise de despesas", description = "quanto o usuario logado gastou, por categoria, por dia e por mes")
public class AnaliseDespesasController {

    private static final String ID_POSITIVO = "o id deve ser um numero positivo";

    private final AnalisarDespesasUseCase analisarDespesasUseCase;
    private final AnaliseDespesasWebMapper analiseDespesasWebMapper;
    private final UsuarioLogado usuarioLogado;

    public AnaliseDespesasController(AnalisarDespesasUseCase analisarDespesasUseCase,
                                     AnaliseDespesasWebMapper analiseDespesasWebMapper,
                                     UsuarioLogado usuarioLogado) {
        this.analisarDespesasUseCase = analisarDespesasUseCase;
        this.analiseDespesasWebMapper = analiseDespesasWebMapper;
        this.usuarioLogado = usuarioLogado;
    }

    // total de cada categoria e o total geral, da categoria em que mais gastou para a que menos gastou
    @Operation(summary = "Despesas por categoria",
            description = "Total e percentual de cada categoria, da que mais gastou para a que menos gastou. "
                    + "Sem datas, usa o mes atual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "resumo das despesas do periodo"),
            @ApiResponse(responseCode = "400", description = "data em formato errado ou inicio depois do fim",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "sem login ou com email/senha errados",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "o usuarioId e de outro usuario",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "503", description = "ms1 fora do ar (nao da para conferir o login)",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @GetMapping("/category")
    public ResponseEntity<DespesasPorCategoriaResponseDTO> porCategoria(
            @Parameter(description = "opcional; se vier, tem que ser o id do usuario logado", example = "1")
            @RequestParam(required = false) @Positive(message = ID_POSITIVO) Long usuarioId,
            @Parameter(description = "data inicial (aaaa-mm-dd); sem ela, comeca no dia 1 do mes do fim "
                    + "(ou do mes atual)", example = "2026-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "data final (aaaa-mm-dd), o dia inteiro entra; sem ela, vai ate o fim "
                    + "do mes atual", example = "2026-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        Long dono = usuarioLogado.resolverUsuario(usuarioId);
        return ResponseEntity.ok(analiseDespesasWebMapper.paraCategoriaResponse(
                analisarDespesasUseCase.porCategoria(dono, inicio, fim)));
    }

    // total gasto em cada dia do periodo (so aparecem os dias com despesa)
    @Operation(summary = "Despesas por dia",
            description = "Total gasto em cada dia do periodo (so os dias com despesa). Sem datas, usa o mes atual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "resumo das despesas do periodo"),
            @ApiResponse(responseCode = "400", description = "data em formato errado ou inicio depois do fim",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "sem login ou com email/senha errados",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "o usuarioId e de outro usuario",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "503", description = "ms1 fora do ar (nao da para conferir o login)",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @GetMapping("/daily")
    public ResponseEntity<DespesasPorDiaResponseDTO> porDia(
            @Parameter(description = "opcional; se vier, tem que ser o id do usuario logado", example = "1")
            @RequestParam(required = false) @Positive(message = ID_POSITIVO) Long usuarioId,
            @Parameter(description = "data inicial (aaaa-mm-dd); sem ela, comeca no dia 1 do mes do fim "
                    + "(ou do mes atual)", example = "2026-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "data final (aaaa-mm-dd), o dia inteiro entra; sem ela, vai ate o fim "
                    + "do mes atual", example = "2026-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        Long dono = usuarioLogado.resolverUsuario(usuarioId);
        return ResponseEntity.ok(analiseDespesasWebMapper.paraDiaResponse(
                analisarDespesasUseCase.porDia(dono, inicio, fim)));
    }

    // total gasto em cada mes do periodo (so aparecem os meses com despesa)
    @Operation(summary = "Despesas por mes",
            description = "Total gasto em cada mes do periodo (so os meses com despesa). Sem datas, usa o mes atual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "resumo das despesas do periodo"),
            @ApiResponse(responseCode = "400", description = "data em formato errado ou inicio depois do fim",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "sem login ou com email/senha errados",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "o usuarioId e de outro usuario",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "503", description = "ms1 fora do ar (nao da para conferir o login)",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @GetMapping("/monthly")
    public ResponseEntity<DespesasPorMesResponseDTO> porMes(
            @Parameter(description = "opcional; se vier, tem que ser o id do usuario logado", example = "1")
            @RequestParam(required = false) @Positive(message = ID_POSITIVO) Long usuarioId,
            @Parameter(description = "data inicial (aaaa-mm-dd); sem ela, comeca no dia 1 do mes do fim "
                    + "(ou do mes atual)", example = "2026-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "data final (aaaa-mm-dd), o dia inteiro entra; sem ela, vai ate o fim "
                    + "do mes atual", example = "2026-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        Long dono = usuarioLogado.resolverUsuario(usuarioId);
        return ResponseEntity.ok(analiseDespesasWebMapper.paraMesResponse(
                analisarDespesasUseCase.porMes(dono, inicio, fim)));
    }
}
