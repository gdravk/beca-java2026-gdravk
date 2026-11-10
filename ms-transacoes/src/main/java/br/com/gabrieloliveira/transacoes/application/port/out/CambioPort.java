package br.com.gabrieloliveira.transacoes.application.port.out;

import br.com.gabrieloliveira.transacoes.domain.model.Cotacao;

// busca a cotacao mais recente de uma moeda em reais ("USD", "EUR")
public interface CambioPort {

    // lanca ServicoExternoIndisponivelException se o servico de cambio estiver fora do ar ou demorar demais
    Cotacao buscarCotacaoAtual(String moeda);
}
