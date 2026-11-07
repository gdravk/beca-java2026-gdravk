package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.out.CambioPort;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import br.com.gabrieloliveira.transacoes.domain.model.Cotacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

import java.util.List;

// classe de apoio dos casos de uso: coloca nas transacoes as taxas de dolar e euro que estiverem faltando
//
// regra do pedido 3 do PO (e a decisao que tomamos):
// - na CRIACAO, a transacao guarda as taxas do momento (assim o valor historico fica certo
//   e a consulta nao depende da brasilapi estar no ar)
// - se a brasilapi estava fora do ar na criacao, a transacao e gravada sem taxa;
//   na CONSULTA (buscar e listar), essa classe tenta de novo e mostra a taxa atual
//   (nesse caso a resposta mostra a data da taxa usada, que vai ser mais nova que a da transacao)
// - se mesmo assim nao conseguir, a transacao segue sem taxa e a resposta avisa "conversao indisponivel"
//   (a consulta NUNCA quebra por causa do cambio)
//
// a taxa preenchida na consulta nao e gravada no banco: a transacao continua exatamente como foi criada
//
// e java puro, sem @Component: o BeanConfig cria e entrega pros casos de uso que precisam dela
public class PreenchedorDeCotacoes {

    private static final String DOLAR = "USD";
    private static final String EURO = "EUR";

    private final CambioPort cambioPort;

    public PreenchedorDeCotacoes(CambioPort cambioPort) {
        this.cambioPort = cambioPort;
    }

    // versao pra uma transacao so (usada no criar e no buscar)
    public void preencherFaltantes(Transacao transacao) {
        preencherFaltantes(List.of(transacao));
    }

    // versao pra uma lista (usada no listar)
    // a brasilapi e chamada no maximo UMA vez por moeda, mesmo que varias transacoes da lista estejam sem taxa;
    // assim a listagem nao fica lenta fazendo uma chamada por transacao
    public void preencherFaltantes(List<Transacao> transacoes) {
        if (algumaSemTaxaDeDolar(transacoes)) {
            Cotacao dolar = buscarOuNull(DOLAR);
            if (dolar != null) {
                for (Transacao transacao : transacoes) {
                    if (transacao.getTaxaDolar() == null) {
                        transacao.setTaxaDolar(dolar.getTaxa());
                        transacao.setDataTaxaDolar(dolar.getData());
                    }
                }
            }
        }

        if (algumaSemTaxaDeEuro(transacoes)) {
            Cotacao euro = buscarOuNull(EURO);
            if (euro != null) {
                for (Transacao transacao : transacoes) {
                    if (transacao.getTaxaEuro() == null) {
                        transacao.setTaxaEuro(euro.getTaxa());
                        transacao.setDataTaxaEuro(euro.getData());
                    }
                }
            }
        }
    }

    // pergunta pra porta de cambio; se o servico falhar, devolve null em vez de deixar a excecao subir,
    // porque o cambio e so informativo e nao pode derrubar a criacao nem a consulta da transacao
    private Cotacao buscarOuNull(String moeda) {
        try {
            return cambioPort.buscarCotacaoAtual(moeda);
        } catch (ServicoExternoIndisponivelException e) {
            return null;
        }
    }

    private boolean algumaSemTaxaDeDolar(List<Transacao> transacoes) {
        for (Transacao transacao : transacoes) {
            if (transacao.getTaxaDolar() == null) {
                return true;
            }
        }
        return false;
    }

    private boolean algumaSemTaxaDeEuro(List<Transacao> transacoes) {
        for (Transacao transacao : transacoes) {
            if (transacao.getTaxaEuro() == null) {
                return true;
            }
        }
        return false;
    }
}
