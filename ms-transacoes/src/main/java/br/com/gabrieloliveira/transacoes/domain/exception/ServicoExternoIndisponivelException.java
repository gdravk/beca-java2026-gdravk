package br.com.gabrieloliveira.transacoes.domain.exception;

// lancada pelos adaptadores de saida quando um servico externo (api mock, brasilapi) falha ou demora demais
// os casos de uso tratam essa excecao: sem saldo a transacao e rejeitada, sem cambio ela segue sem conversao
public class ServicoExternoIndisponivelException extends RuntimeException {

    public ServicoExternoIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public ServicoExternoIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
