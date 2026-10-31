package br.com.gabrieloliveira.transacoes.adapter.out.brasilapi;

import br.com.gabrieloliveira.transacoes.application.port.out.CambioPort;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import br.com.gabrieloliveira.transacoes.domain.model.Cotacao;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

// adaptador provisorio: devolve taxas fixas de dolar e euro, com a data de hoje
// os valores nao sao cotacoes reais; servem so para o fluxo de transacao funcionar antes da integracao
// sera substituido pelo BrasilApiCambioAdapter (fase 9 / pedido 3), sem nenhuma mudanca no dominio ou nos casos de uso
@Component
public class CambioFakeAdapter implements CambioPort {

    private static final BigDecimal TAXA_FIXA_DOLAR = new BigDecimal("5.0000");
    private static final BigDecimal TAXA_FIXA_EURO = new BigDecimal("5.5000");

    @Override
    public Cotacao buscarCotacaoAtual(String moeda) {
        if ("USD".equals(moeda)) {
            return new Cotacao("USD", TAXA_FIXA_DOLAR, LocalDate.now());
        }
        if ("EUR".equals(moeda)) {
            return new Cotacao("EUR", TAXA_FIXA_EURO, LocalDate.now());
        }
        throw new ServicoExternoIndisponivelException("moeda sem cotacao fixa no adaptador provisorio: " + moeda);
    }
}
