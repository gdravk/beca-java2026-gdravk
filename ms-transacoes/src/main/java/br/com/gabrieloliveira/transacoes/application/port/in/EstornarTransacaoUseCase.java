package br.com.gabrieloliveira.transacoes.application.port.in;

import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

// corrige uma transacao sem apagar nada: cria uma nova transacao, do tipo inverso, ligada a original,
// que continua gravada exatamente como estava
public interface EstornarTransacaoUseCase {

    // devolve a transacao de estorno que foi criada
    // usuarioSolicitanteId = quem esta pedindo o estorno; so o dono da transacao pode estornar
    // (a transacao de outro usuario responde 404, como se nao existisse)
    Transacao estornar(Long idTransacaoOriginal, Long usuarioSolicitanteId);
}
