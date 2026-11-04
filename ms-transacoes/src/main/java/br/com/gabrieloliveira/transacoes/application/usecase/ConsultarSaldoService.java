package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.in.ConsultarSaldoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.out.SaldoContaPort;
import br.com.gabrieloliveira.transacoes.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.transacoes.domain.model.SaldoConta;

// implementa a consulta de saldo: so pergunta pra porta de saida (SaldoContaPort) e devolve
// o saldo nao fica guardado no nosso banco; ele sempre vem fresquinho da api mock (pedido 2 do PO)
// se a api mock estiver fora do ar, a ServicoExternoIndisponivelException "sobe" sem ser tratada aqui
// e o GlobalExceptionHandler transforma ela em 503 (servico indisponivel)
public class ConsultarSaldoService implements ConsultarSaldoUseCase {

    private final SaldoContaPort saldoContaPort;

    public ConsultarSaldoService(SaldoContaPort saldoContaPort) {
        this.saldoContaPort = saldoContaPort;
    }

    @Override
    public SaldoConta consultar(Long usuarioId) {
        // se a "caixinha" (Optional) vier vazia, o usuario nao tem conta na api mock -> 404
        return saldoContaPort.buscarPorUsuario(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "conta do usuario " + usuarioId + " nao encontrada no servico de saldo"));
    }
}
