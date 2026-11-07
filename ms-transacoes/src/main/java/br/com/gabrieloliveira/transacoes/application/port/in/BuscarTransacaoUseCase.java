package br.com.gabrieloliveira.transacoes.application.port.in;

import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

// busca uma transacao pelo identificador, so entre as transacoes do usuario que pediu
// lanca RecursoNaoEncontradoException (404) se ela nao existir OU se for de outro usuario
public interface BuscarTransacaoUseCase {

    Transacao buscarPorId(Long id, Long usuarioSolicitanteId);
}
