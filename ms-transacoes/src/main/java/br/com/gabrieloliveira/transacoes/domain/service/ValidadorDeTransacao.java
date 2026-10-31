package br.com.gabrieloliveira.transacoes.domain.service;

import br.com.gabrieloliveira.transacoes.domain.model.SaldoConta;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

import java.math.BigDecimal;
import java.util.Optional;

// regras de negocio que decidem se uma transacao e aprovada ou rejeitada
// cada metodo devolve o motivo da rejeicao, ou Optional vazio quando a transacao passa na regra
// java puro: nao consulta banco nem api, recebe tudo pronto de quem chama (o caso de uso)
public class ValidadorDeTransacao {

    // regras que dependem so dos dados da propria transacao
    public Optional<String> validarDados(Transacao transacao) {
        if (transacao.getValor() == null || transacao.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.of("o valor da transacao deve ser maior que zero");
        }
        if (transacao.getTipo() == TipoTransacao.COMPRA && transacao.getCategoria() == null) {
            return Optional.of("compra exige uma categoria");
        }
        if (transacao.getTipo() == TipoTransacao.TRANSFERENCIA) {
            if (transacao.getUsuarioDestinoId() == null) {
                return Optional.of("transferencia exige o usuario de destino");
            }
            if (transacao.getUsuarioDestinoId().equals(transacao.getUsuarioId())) {
                return Optional.of("nao e permitido transferir para a propria conta");
            }
        }
        return Optional.empty();
    }

    // regras de saida (retirada, transferencia e compra), comparando sempre em reais
    // totalSaidasAprovadasHoje = soma das saidas ja aprovadas do usuario no dia, sem contar esta
    public Optional<String> validarSaldoELimites(Transacao transacao, SaldoConta conta,
                                                 BigDecimal totalSaidasAprovadasHoje) {
        BigDecimal valor = transacao.getValor();

        if (valor.compareTo(conta.getSaldo()) > 0) {
            return Optional.of("saldo insuficiente");
        }
        if (valor.compareTo(conta.getLimitePorTransacao()) > 0) {
            return Optional.of("valor acima do limite por transacao (limite: " + conta.getLimitePorTransacao() + ")");
        }
        BigDecimal totalComEstaTransacao = totalSaidasAprovadasHoje.add(valor);
        if (totalComEstaTransacao.compareTo(conta.getLimiteDiario()) > 0) {
            return Optional.of("limite diario excedido (limite: " + conta.getLimiteDiario()
                    + ", ja utilizado hoje: " + totalSaidasAprovadasHoje + ")");
        }
        return Optional.empty();
    }
}
