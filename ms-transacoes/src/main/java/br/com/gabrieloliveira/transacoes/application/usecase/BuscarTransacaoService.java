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
    public Transacao buscarPorId(Long id, Long usuarioSolicitanteId) {
        Transacao transacao = transacaoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> naoEncontrada(id));

        // seguranca (pedido 6, ajuste da revisao do professor): a transacao de outro usuario responde 404,
        // igual a uma que nao existe (um 403 contaria para quem esta tentando que aquele id existe e e de
        // outra pessoa)
        // essa conferencia vem ANTES do cambio, para nao chamar a brasilapi por uma transacao que nem vai ser mostrada
        if (!transacao.getUsuarioId().equals(usuarioSolicitanteId)) {
            throw naoEncontrada(id);
        }

        // se a transacao foi gravada sem taxa (brasilapi fora do ar na criacao), tenta buscar agora
        // quando ela ja tem as taxas guardadas, nada e consultado e a resposta usa as taxas da criacao
        preenchedorDeCotacoes.preencherFaltantes(transacao);
        return transacao;
    }

    // a mesma mensagem para os dois casos (nao existe / e de outro usuario)
    private RecursoNaoEncontradoException naoEncontrada(Long id) {
        return new RecursoNaoEncontradoException("transacao com id " + id + " nao encontrada");
    }
}
