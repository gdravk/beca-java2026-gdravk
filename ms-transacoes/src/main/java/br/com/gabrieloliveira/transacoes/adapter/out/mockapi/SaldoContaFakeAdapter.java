package br.com.gabrieloliveira.transacoes.adapter.out.mockapi;

import br.com.gabrieloliveira.transacoes.application.port.out.SaldoContaPort;
import br.com.gabrieloliveira.transacoes.domain.model.SaldoConta;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

// adaptador provisorio: devolve o mesmo saldo e os mesmos limites para qualquer usuario
// existe so para o fluxo de transacao funcionar de ponta a ponta antes da integracao com a api mock
// sera substituido pelo MockApiSaldoAdapter (fase 8 / pedido 2), sem nenhuma mudanca no dominio ou nos casos de uso
@Component
public class SaldoContaFakeAdapter implements SaldoContaPort {

    private static final BigDecimal SALDO = new BigDecimal("5000.00");
    private static final BigDecimal LIMITE_DIARIO = new BigDecimal("2000.00");
    private static final BigDecimal LIMITE_POR_TRANSACAO = new BigDecimal("1000.00");

    @Override
    public Optional<SaldoConta> buscarPorUsuario(Long usuarioId) {
        return Optional.of(new SaldoConta(usuarioId, SALDO, LIMITE_DIARIO, LIMITE_POR_TRANSACAO));
    }
}
