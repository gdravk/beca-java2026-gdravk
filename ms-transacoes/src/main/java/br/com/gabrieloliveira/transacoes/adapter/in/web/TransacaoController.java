package br.com.gabrieloliveira.transacoes.adapter.in.web;

import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.ErroResponseDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.TransacaoRequestDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.TransacaoResponseDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.UsuarioLogado;
import br.com.gabrieloliveira.transacoes.application.port.in.BuscarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.CriarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.EstornarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.ListarTransacoesUseCase;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

// endpoints de transacao do ms2
// nao existe DELETE nem PUT: transacao nunca e apagada ou alterada, a correcao e feita por estorno
// todas as rotas exigem login (SecurityConfig) e so mostram ou criam transacoes do usuario logado
@RestController
@RequestMapping("/transactions")
// os ids da url e do filtro sao validados direto no parametro (fase 13): zero ou negativo responde 400
// o spring mvc faz essa validacao sozinho (sem @Validated), igual no ms1
//
// swagger (fase 15): @Tag da o nome do grupo na tela, @Operation o resumo de cada rota
// e @ApiResponses lista os codigos que a rota pode responder (os erros sempre no formato ErroResponseDTO)
// o 503 aparece em todas as rotas porque o login e conferido no ms1: com o ms1 fora do ar, ninguem entra
@Tag(name = "Transacoes", description = "registro, consulta e estorno de transacoes do usuario logado")
public class TransacaoController {

    private static final String ID_POSITIVO = "o id deve ser um numero positivo";

    private final CriarTransacaoUseCase criarTransacaoUseCase;
    private final BuscarTransacaoUseCase buscarTransacaoUseCase;
    private final ListarTransacoesUseCase listarTransacoesUseCase;
    private final EstornarTransacaoUseCase estornarTransacaoUseCase;
    private final TransacaoWebMapper transacaoWebMapper;
    private final UsuarioLogado usuarioLogado;

    public TransacaoController(CriarTransacaoUseCase criarTransacaoUseCase,
                               BuscarTransacaoUseCase buscarTransacaoUseCase,
                               ListarTransacoesUseCase listarTransacoesUseCase,
                               EstornarTransacaoUseCase estornarTransacaoUseCase,
                               TransacaoWebMapper transacaoWebMapper,
                               UsuarioLogado usuarioLogado) {
        this.criarTransacaoUseCase = criarTransacaoUseCase;
        this.buscarTransacaoUseCase = buscarTransacaoUseCase;
        this.listarTransacoesUseCase = listarTransacoesUseCase;
        this.estornarTransacaoUseCase = estornarTransacaoUseCase;
        this.transacaoWebMapper = transacaoWebMapper;
        this.usuarioLogado = usuarioLogado;
    }

    // POST /transactions -> 201 com id e status, mesmo quando a transacao e rejeitada
    // (a rejeicao e um resultado de negocio gravado, nao um erro da requisicao)
    // a transacao sempre fica no nome de quem esta logado; mandar o usuarioId de outro responde 403
    @Operation(summary = "Registra uma transacao",
            description = "Responde 201 mesmo quando a transacao e rejeitada (status REJECTED com o motivo): "
                    + "a rejeicao e uma decisao de negocio gravada, e nao um erro. Saidas (RETIRADA, TRANSFERENCIA "
                    + "e COMPRA) conferem saldo e limites na API Mock; COMPRA exige categoria e TRANSFERENCIA "
                    + "exige usuarioDestinoId.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "transacao gravada (APPROVED ou REJECTED)"),
            @ApiResponse(responseCode = "400", description = "campos invalidos (a resposta lista cada campo)",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "sem login ou com email/senha errados",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "o usuarioId do corpo e de outro usuario",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "503", description = "ms1 fora do ar (nao da para conferir o login)",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> criar(@Valid @RequestBody TransacaoRequestDTO dto) {
        Transacao novaTransacao = transacaoWebMapper.paraDominio(dto);
        novaTransacao.setUsuarioId(usuarioLogado.resolverUsuario(dto.getUsuarioId()));
        Transacao criada = criarTransacaoUseCase.criar(novaTransacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoWebMapper.paraResponse(criada));
    }

    // GET /transactions/{id} -> 200 com valor em reais, dolar e euro (ou 404)
    // transacao de outro usuario tambem responde 404 (o caso de uso confere o dono antes de buscar o cambio)
    @Operation(summary = "Busca uma transacao pelo id",
            description = "Mostra o valor em reais e convertido para dolar e euro. "
                    + "Transacao de outro usuario responde 404, como se nao existisse.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "transacao encontrada"),
            @ApiResponse(responseCode = "400", description = "id zero ou negativo",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "sem login ou com email/senha errados",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "transacao nao existe ou e de outro usuario",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "503", description = "ms1 fora do ar (nao da para conferir o login)",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransacaoResponseDTO> buscarPorId(
            @Parameter(description = "id da transacao", example = "1")
            @PathVariable @Positive(message = ID_POSITIVO) Long id) {
        Transacao transacao = buscarTransacaoUseCase.buscarPorId(id, usuarioLogado.id());
        return ResponseEntity.ok(transacaoWebMapper.paraResponse(transacao));
    }

    // GET /transactions?inicio=2026-11-01&fim=2026-11-30&tipo=COMPRA
    // todos os filtros sao opcionais; datas no formato aaaa-mm-dd
    // lista as transacoes de quem esta logado; o usuarioId so e aceito se for o do proprio usuario (senao 403)
    @Operation(summary = "Lista as transacoes do usuario logado",
            description = "Todos os filtros sao opcionais. Sem datas, traz todo o historico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "lista de transacoes (pode vir vazia)"),
            @ApiResponse(responseCode = "400", description = "data em formato errado, inicio depois do fim ou tipo invalido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "sem login ou com email/senha errados",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "o usuarioId e de outro usuario",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "503", description = "ms1 fora do ar (nao da para conferir o login)",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<TransacaoResponseDTO>> listar(
            @Parameter(description = "opcional; se vier, tem que ser o id do usuario logado", example = "1")
            @RequestParam(required = false) @Positive(message = ID_POSITIVO) Long usuarioId,
            @Parameter(description = "data inicial (aaaa-mm-dd), entra no resultado", example = "2026-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "data final (aaaa-mm-dd), o dia inteiro entra no resultado", example = "2026-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @Parameter(description = "filtra por um tipo de transacao")
            @RequestParam(required = false) TipoTransacao tipo) {
        Long dono = usuarioLogado.resolverUsuario(usuarioId);
        List<Transacao> transacoes = listarTransacoesUseCase.listar(dono, inicio, fim, tipo);
        return ResponseEntity.ok(transacaoWebMapper.paraListaResponse(transacoes));
    }

    // POST /transactions/{id}/estorno -> 201 com a nova transacao de estorno
    // so o dono da transacao pode estornar (a conferencia e feita no caso de uso, antes de criar o estorno)
    @Operation(summary = "Estorna uma transacao aprovada",
            description = "Cria uma transacao nova com o tipo inverso, ligada a original pelo transacaoOriginalId: "
                    + "uma saida volta como DEPOSITO e um deposito volta como RETIRADA. A original nunca e apagada.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "estorno criado"),
            @ApiResponse(responseCode = "400", description = "id zero ou negativo",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "sem login ou com email/senha errados",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "transacao nao existe ou e de outro usuario",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "transacao rejeitada, ja estornada ou que ja e um estorno",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "503", description = "ms1 fora do ar (nao da para conferir o login)",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PostMapping("/{id}/estorno")
    public ResponseEntity<TransacaoResponseDTO> estornar(
            @Parameter(description = "id da transacao a estornar", example = "1")
            @PathVariable @Positive(message = ID_POSITIVO) Long id) {
        Transacao estorno = estornarTransacaoUseCase.estornar(id, usuarioLogado.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoWebMapper.paraResponse(estorno));
    }
}
