package br.com.gabrieloliveira.transacoes.adapter.in.web;

import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.ConversaoMoedaDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.TransacaoRequestDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.TransacaoResponseDTO;
import br.com.gabrieloliveira.transacoes.domain.model.Cotacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// traduz entre os dtos http e a Transacao do dominio
// tambem monta as conversoes de moeda da resposta, a partir das taxas guardadas na transacao
@Component
public class TransacaoWebMapper {

    public Transacao paraDominio(TransacaoRequestDTO dto) {
        Transacao transacao = new Transacao();
        transacao.setUsuarioId(dto.getUsuarioId());
        transacao.setTipo(dto.getTipo());
        transacao.setValor(dto.getValor());
        transacao.setCategoria(dto.getCategoria());
        transacao.setDescricao(dto.getDescricao());
        transacao.setUsuarioDestinoId(dto.getUsuarioDestinoId());
        return transacao;
    }

    public TransacaoResponseDTO paraResponse(Transacao transacao) {
        TransacaoResponseDTO dto = new TransacaoResponseDTO();
        dto.setId(transacao.getId());
        dto.setUsuarioId(transacao.getUsuarioId());
        dto.setTipo(transacao.getTipo());
        dto.setValorEmReais(transacao.getValor());
        dto.setCategoria(transacao.getCategoria());
        dto.setDescricao(transacao.getDescricao());
        dto.setUsuarioDestinoId(transacao.getUsuarioDestinoId());
        dto.setStatus(transacao.getStatus());
        dto.setMotivoRejeicao(transacao.getMotivoRejeicao());
        dto.setDataHora(transacao.getDataHora());
        dto.setTransacaoOriginalId(transacao.getTransacaoOriginalId());

        List<ConversaoMoedaDTO> conversoes = new ArrayList<>();
        List<String> moedasSemCotacao = new ArrayList<>();

        if (transacao.getTaxaDolar() != null) {
            Cotacao dolar = new Cotacao("USD", transacao.getTaxaDolar(), transacao.getDataTaxaDolar());
            conversoes.add(paraConversao(dolar, transacao));
        } else {
            moedasSemCotacao.add("USD");
        }

        if (transacao.getTaxaEuro() != null) {
            Cotacao euro = new Cotacao("EUR", transacao.getTaxaEuro(), transacao.getDataTaxaEuro());
            conversoes.add(paraConversao(euro, transacao));
        } else {
            moedasSemCotacao.add("EUR");
        }

        dto.setConversoes(conversoes);
        dto.setConversaoDisponivel(moedasSemCotacao.isEmpty());
        if (!moedasSemCotacao.isEmpty()) {
            dto.setMensagemConversao("conversao indisponivel no momento para: " + String.join(", ", moedasSemCotacao));
        }
        return dto;
    }

    public List<TransacaoResponseDTO> paraListaResponse(List<Transacao> transacoes) {
        List<TransacaoResponseDTO> resposta = new ArrayList<>();
        for (Transacao transacao : transacoes) {
            resposta.add(paraResponse(transacao));
        }
        return resposta;
    }

    private ConversaoMoedaDTO paraConversao(Cotacao cotacao, Transacao transacao) {
        return new ConversaoMoedaDTO(
                cotacao.getMoeda(),
                cotacao.getTaxa(),
                cotacao.getData(),
                cotacao.converterDeReais(transacao.getValor())
        );
    }
}
