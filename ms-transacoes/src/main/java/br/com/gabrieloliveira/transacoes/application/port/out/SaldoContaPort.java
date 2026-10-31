package br.com.gabrieloliveira.transacoes.application.port.out;

import br.com.gabrieloliveira.transacoes.domain.model.SaldoConta;

import java.util.Optional;

// consulta saldo e limites de um usuario no servico externo de contas (api mock)
public interface SaldoContaPort {

    // Optional vazio = o usuario nao tem conta cadastrada no servico
    // lanca ServicoExternoIndisponivelException se o servico estiver fora do ar ou demorar demais
    Optional<SaldoConta> buscarPorUsuario(Long usuarioId);
}
