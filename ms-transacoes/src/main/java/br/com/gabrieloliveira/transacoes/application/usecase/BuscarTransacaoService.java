package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.in.BuscarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

public class BuscarTransacaoService implements BuscarTransacaoUseCase {

    private final TransacaoRepositoryPort transacaoRepositoryPort;

    public BuscarTransacaoService(TransacaoRepositoryPort transacaoRepositoryPort) {
        this.transacaoRepositoryPort = transacaoRepositoryPort;
    }

    @Override
    public Transacao buscarPorId(Long id) {
        return transacaoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("transacao com id " + id + " nao encontrada"));
    }
}
