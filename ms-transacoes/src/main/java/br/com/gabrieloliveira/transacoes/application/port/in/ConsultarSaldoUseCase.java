package br.com.gabrieloliveira.transacoes.application.port.in;

import br.com.gabrieloliveira.transacoes.domain.model.SaldoConta;

// porta de entrada para mostrar o saldo e os limites atuais de um usuario, lidos na hora da api mock
public interface ConsultarSaldoUseCase {

    // lanca RecursoNaoEncontradoException se o usuario nao tiver conta (404)
    // e ServicoExternoIndisponivelException se a api mock estiver fora do ar (503)
    SaldoConta consultar(Long usuarioId);
}
