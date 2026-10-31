package br.com.gabrieloliveira.transacoes.application.port.in;

import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

// corrige uma transacao sem apagar: cria uma nova transacao inversa ligada a original
// a original continua gravada exatamente como estava
public interface EstornarTransacaoUseCase {

    // devolve a transacao de estorno criada
    Transacao estornar(Long idTransacaoOriginal);
}
