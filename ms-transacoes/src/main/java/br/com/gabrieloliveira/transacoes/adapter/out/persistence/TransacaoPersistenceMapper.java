package br.com.gabrieloliveira.transacoes.adapter.out.persistence;

import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import org.springframework.stereotype.Component;

// converte entre a Transacao do dominio e a TransacaoEntity do banco, campo a campo
@Component
public class TransacaoPersistenceMapper {

    public TransacaoEntity paraEntity(Transacao transacao) {
        TransacaoEntity entity = new TransacaoEntity();
        entity.setId(transacao.getId());
        entity.setUsuarioId(transacao.getUsuarioId());
        entity.setTipo(transacao.getTipo());
        entity.setValor(transacao.getValor());
        entity.setCategoria(transacao.getCategoria());
        entity.setDescricao(transacao.getDescricao());
        entity.setUsuarioDestinoId(transacao.getUsuarioDestinoId());
        entity.setStatus(transacao.getStatus());
        entity.setMotivoRejeicao(transacao.getMotivoRejeicao());
        entity.setDataHora(transacao.getDataHora());
        entity.setTransacaoOriginalId(transacao.getTransacaoOriginalId());
        entity.setTaxaDolar(transacao.getTaxaDolar());
        entity.setDataTaxaDolar(transacao.getDataTaxaDolar());
        entity.setTaxaEuro(transacao.getTaxaEuro());
        entity.setDataTaxaEuro(transacao.getDataTaxaEuro());
        return entity;
    }

    public Transacao paraDominio(TransacaoEntity entity) {
        Transacao transacao = new Transacao();
        transacao.setId(entity.getId());
        transacao.setUsuarioId(entity.getUsuarioId());
        transacao.setTipo(entity.getTipo());
        transacao.setValor(entity.getValor());
        transacao.setCategoria(entity.getCategoria());
        transacao.setDescricao(entity.getDescricao());
        transacao.setUsuarioDestinoId(entity.getUsuarioDestinoId());
        transacao.setStatus(entity.getStatus());
        transacao.setMotivoRejeicao(entity.getMotivoRejeicao());
        transacao.setDataHora(entity.getDataHora());
        transacao.setTransacaoOriginalId(entity.getTransacaoOriginalId());
        transacao.setTaxaDolar(entity.getTaxaDolar());
        transacao.setDataTaxaDolar(entity.getDataTaxaDolar());
        transacao.setTaxaEuro(entity.getTaxaEuro());
        transacao.setDataTaxaEuro(entity.getDataTaxaEuro());
        return transacao;
    }
}
