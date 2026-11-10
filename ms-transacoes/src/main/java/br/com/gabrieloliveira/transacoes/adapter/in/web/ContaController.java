package br.com.gabrieloliveira.transacoes.adapter.in.web;

import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.ErroResponseDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.SaldoContaResponseDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.UsuarioLogado;
import br.com.gabrieloliveira.transacoes.application.port.in.ConsultarSaldoUseCase;
import br.com.gabrieloliveira.transacoes.domain.model.SaldoConta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDateTime;

// rotas da conta do usuario (saldo e limites)
// fica separado do TransacaoController porque e outro recurso da api: /accounts, e nao /transactions
@RestController
@RequestMapping("/accounts")
// os ids da url sao validados direto no parametro: zero ou negativo responde 400
// (o spring mvc faz essa validacao sozinho, sem @Validated, igual no ms1)
@Tag(name = "Conta", description = "saldo e limites do usuario logado, lidos da API Mock")
public class ContaController {

    private static final String ID_POSITIVO = "o id deve ser um numero positivo";

    private final ConsultarSaldoUseCase consultarSaldoUseCase;
    private final UsuarioLogado usuarioLogado;
    // relogio no fuso de brasilia, para o "consultadoEm" da resposta sair no horario certo
    private final Clock relogio;

    public ContaController(ConsultarSaldoUseCase consultarSaldoUseCase, UsuarioLogado usuarioLogado, Clock relogio) {
        this.consultarSaldoUseCase = consultarSaldoUseCase;
        this.usuarioLogado = usuarioLogado;
        this.relogio = relogio;
    }

    // GET /accounts/{usuarioId}/balance: 200 com o saldo e os limites lidos agora mesmo da api mock
    // 404 se o usuario nao tiver conta la, 503 se a api mock estiver fora do ar
    // 403 se o usuarioId da url nao for o de quem esta logado (ninguem ve o saldo de outra pessoa)
    @Operation(summary = "Consulta o saldo e os limites da propria conta",
            description = "Os valores vem na hora da API Mock (nao ficam guardados no ms2).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "saldo e limites"),
            @ApiResponse(responseCode = "400", description = "id zero ou negativo",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "sem login ou com email/senha errados",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "o usuarioId e de outro usuario",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "o usuario nao tem conta na API Mock",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "503", description = "API Mock ou ms1 fora do ar",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @GetMapping("/{usuarioId}/balance")
    public ResponseEntity<SaldoContaResponseDTO> consultarSaldo(
            @Parameter(description = "id do usuario logado", example = "1")
            @PathVariable @Positive(message = ID_POSITIVO) Long usuarioId) {
        usuarioLogado.garantirQueEDoLogado(usuarioId);
        SaldoConta conta = consultarSaldoUseCase.consultar(usuarioId);
        SaldoContaResponseDTO resposta = new SaldoContaResponseDTO(
                conta.getUsuarioId(),
                conta.getSaldo(),
                conta.getLimiteDiario(),
                conta.getLimitePorTransacao(),
                LocalDateTime.now(relogio)
        );
        return ResponseEntity.ok(resposta);
    }
}
