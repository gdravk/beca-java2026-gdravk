package br.com.gabrieloliveira.transacoes.application.port.in;

import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

// registra uma nova transacao, ja decidindo e gravando o status (APPROVED ou REJECTED)
public interface CriarTransacaoUseCase {

    Transacao criar(Transacao novaTransacao);
}
