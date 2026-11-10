package br.com.gabrieloliveira.transacoes.domain.exception;

// lancada pelos adaptadores de saida quando um servico externo (api mock, brasilapi, ms1) falha ou demora demais
// os casos de uso tratam esta excecao: sem saldo a transacao e rejeitada, e sem cambio ela segue sem conversao
public class ServicoExternoIndisponivelException extends RuntimeException {

    public ServicoExternoIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public ServicoExternoIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
