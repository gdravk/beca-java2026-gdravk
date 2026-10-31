package br.com.gabrieloliveira.transacoes.application.port.in;

import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

// busca uma transacao pelo identificador; lanca RecursoNaoEncontradoException se nao existir
public interface BuscarTransacaoUseCase {

    Transacao buscarPorId(Long id);
}
