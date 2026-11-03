package br.com.gabrieloliveira.transacoes.adapter.in.web;

import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.SaldoContaResponseDTO;
import br.com.gabrieloliveira.transacoes.application.port.in.ConsultarSaldoUseCase;
import br.com.gabrieloliveira.transacoes.domain.model.SaldoConta;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

// endpoints da conta do usuario (saldo e limites)
// fica separado do TransacaoController porque e outro recurso da api: /accounts, e nao /transactions
@RestController
@RequestMapping("/accounts")
public class ContaController {

    private final ConsultarSaldoUseCase consultarSaldoUseCase;

    public ContaController(ConsultarSaldoUseCase consultarSaldoUseCase) {
        this.consultarSaldoUseCase = consultarSaldoUseCase;
    }

    // GET /accounts/{usuarioId}/balance -> 200 com saldo e limites lidos agora da api mock
    // 404 se o usuario nao tiver conta, 503 se a api mock estiver fora do ar
    @GetMapping("/{usuarioId}/balance")
    public ResponseEntity<SaldoContaResponseDTO> consultarSaldo(@PathVariable Long usuarioId) {
        SaldoConta conta = consultarSaldoUseCase.consultar(usuarioId);
        SaldoContaResponseDTO resposta = new SaldoContaResponseDTO(
                conta.getUsuarioId(),
                conta.getSaldo(),
                conta.getLimiteDiario(),
                conta.getLimitePorTransacao(),
                LocalDateTime.now()
        );
        return ResponseEntity.ok(resposta);
    }
}
