package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.in.BuscarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

public class BuscarTransacaoService implements BuscarTransacaoUseCase {

    private final TransacaoRepositoryPort transacaoRepositoryPort;
    private final PreenchedorDeCotacoes preenchedorDeCotacoes;

    public BuscarTransacaoService(TransacaoRepositoryPort transacaoRepositoryPort,
                                  PreenchedorDeCotacoes preenchedorDeCotacoes) {
        this.transacaoRepositoryPort = transacaoRepositoryPort;
        this.preenchedorDeCotacoes = preenchedorDeCotacoes;
    }

    @Override
    public Transacao buscarPorId(Long id) {
        Transacao transacao = transacaoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("transacao com id " + id + " nao encontrada"));

        // se a transacao foi gravada sem taxa (brasilapi fora do ar na criacao), tenta buscar agora
        // quando ela ja tem as taxas guardadas, nada e consultado e a resposta usa as taxas da criacao
        preenchedorDeCotacoes.preencherFaltantes(transacao);
        return transacao;
    }
}
