package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.in.CriarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.EstornarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.exception.AcessoNegadoException;
import br.com.gabrieloliveira.transacoes.domain.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.transacoes.domain.exception.RegraNegocioException;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

// estorno = nova transacao do tipo inverso, com o mesmo valor e a mesma categoria, ligada a original
// ex: estorno de uma compra vira um deposito; estorno de um deposito vira uma retirada
// o estorno passa pelo mesmo fluxo de criacao, entao um estorno de deposito tambem confere saldo e limites
public class EstornarTransacaoService implements EstornarTransacaoUseCase {

    private final TransacaoRepositoryPort transacaoRepositoryPort;
    private final CriarTransacaoUseCase criarTransacaoUseCase;

    public EstornarTransacaoService(TransacaoRepositoryPort transacaoRepositoryPort,
                                    CriarTransacaoUseCase criarTransacaoUseCase) {
        this.transacaoRepositoryPort = transacaoRepositoryPort;
        this.criarTransacaoUseCase = criarTransacaoUseCase;
    }

    @Override
    public Transacao estornar(Long idTransacaoOriginal, Long usuarioSolicitanteId) {
        Transacao original = transacaoRepositoryPort.buscarPorId(idTransacaoOriginal)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "transacao com id " + idTransacaoOriginal + " nao encontrada"));

        // seguranca (pedido 6): ninguem estorna a transacao de outro usuario
        // essa conferencia vem antes de todas as outras, pra nao contar nada sobre a transacao de outra pessoa
        if (!original.getUsuarioId().equals(usuarioSolicitanteId)) {
            throw new AcessoNegadoException("voce so pode estornar as suas proprias transacoes");
        }

        // transacao rejeitada nunca movimentou dinheiro, entao nao tem o que corrigir
        if (!original.isAprovada()) {
            throw new RegraNegocioException("so transacoes aprovadas podem ser estornadas");
        }
        if (original.isEstorno()) {
            throw new RegraNegocioException("um estorno nao pode ser estornado; registre uma nova transacao");
        }
        if (transacaoRepositoryPort.existeEstornoAprovado(idTransacaoOriginal)) {
            throw new RegraNegocioException("essa transacao ja foi estornada");
        }

        Transacao estorno = new Transacao();
        estorno.setUsuarioId(original.getUsuarioId());
        estorno.setTipo(original.getTipo().inverso());
        estorno.setValor(original.getValor());
        estorno.setCategoria(original.getCategoria());
        estorno.setDescricao("estorno da transacao " + original.getId());
        estorno.setTransacaoOriginalId(original.getId());

        return criarTransacaoUseCase.criar(estorno);
    }
}
