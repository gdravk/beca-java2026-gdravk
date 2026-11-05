package br.com.gabrieloliveira.transacoes.application.port.in;

import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

// corrige uma transacao sem apagar: cria uma nova transacao inversa ligada a original
// a original continua gravada exatamente como estava
public interface EstornarTransacaoUseCase {

    // devolve a transacao de estorno criada
    // usuarioSolicitanteId = quem esta pedindo o estorno; so o dono da transacao pode estornar
    // (transacao de outro usuario responde 404, como se nao existisse)
    Transacao estornar(Long idTransacaoOriginal, Long usuarioSolicitanteId);
}
