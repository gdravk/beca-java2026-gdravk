package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.in.CriarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.out.CambioPort;
import br.com.gabrieloliveira.transacoes.application.port.out.SaldoContaPort;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.Cotacao;
import br.com.gabrieloliveira.transacoes.domain.model.SaldoConta;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import br.com.gabrieloliveira.transacoes.domain.service.ValidadorDeTransacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

// fluxo de criacao de transacao:
// 1. valida os dados da transacao (valor, categoria da compra, destino da transferencia)
// 2. se for saida, consulta saldo e limites (SaldoContaPort) e aplica as regras do dominio
// 3. define APPROVED ou REJECTED (com motivo)
// 4. guarda as cotacoes de dolar e euro do momento (CambioPort)
// 5. salva (TransacaoRepositoryPort) e devolve
// a transacao e sempre gravada, aprovada ou rejeitada
public class CriarTransacaoService implements CriarTransacaoUseCase {

    private static final String MOTIVO_SALDO_INDISPONIVEL =
            "nao foi possivel consultar o saldo agora (servico de saldo indisponivel), tente novamente mais tarde";
    private static final String MOTIVO_CONTA_NAO_ENCONTRADA =
            "conta do usuario nao encontrada no servico de saldo";

    private final TransacaoRepositoryPort transacaoRepositoryPort;
    private final SaldoContaPort saldoContaPort;
    private final CambioPort cambioPort;
    private final ValidadorDeTransacao validadorDeTransacao;

    public CriarTransacaoService(TransacaoRepositoryPort transacaoRepositoryPort,
                                 SaldoContaPort saldoContaPort,
                                 CambioPort cambioPort,
                                 ValidadorDeTransacao validadorDeTransacao) {
        this.transacaoRepositoryPort = transacaoRepositoryPort;
        this.saldoContaPort = saldoContaPort;
        this.cambioPort = cambioPort;
        this.validadorDeTransacao = validadorDeTransacao;
    }

    @Override
    public Transacao criar(Transacao novaTransacao) {
        novaTransacao.setDataHora(LocalDateTime.now());

        Optional<String> erroNosDados = validadorDeTransacao.validarDados(novaTransacao);
        if (erroNosDados.isPresent()) {
            novaTransacao.rejeitar(erroNosDados.get());
        } else {
            // a compra ja foi validada com categoria; nos outros tipos a categoria e opcional
            if (novaTransacao.getCategoria() == null) {
                novaTransacao.setCategoria(Categoria.OUTROS);
            }

            // deposito nao precisa consultar saldo
            if (novaTransacao.getTipo().isSaida()) {
                aplicarRegrasDeSaldo(novaTransacao);
            } else {
                novaTransacao.aprovar();
            }
        }

        registrarCotacoes(novaTransacao);
        return transacaoRepositoryPort.salvar(novaTransacao);
    }

    // sem resposta do servico de saldo a transacao nunca e aprovada as cegas: fica REJECTED com o motivo
    private void aplicarRegrasDeSaldo(Transacao transacao) {
        Optional<SaldoConta> conta;
        try {
            conta = saldoContaPort.buscarPorUsuario(transacao.getUsuarioId());
        } catch (ServicoExternoIndisponivelException e) {
            transacao.rejeitar(MOTIVO_SALDO_INDISPONIVEL);
            return;
        }

        if (conta.isEmpty()) {
            transacao.rejeitar(MOTIVO_CONTA_NAO_ENCONTRADA);
            return;
        }

        BigDecimal totalSaidasHoje = transacaoRepositoryPort.somarSaidasAprovadasNoDia(
                transacao.getUsuarioId(), transacao.getDataHora().toLocalDate());

        Optional<String> erroNoSaldo = validadorDeTransacao.validarSaldoELimites(transacao, conta.get(), totalSaidasHoje);
        if (erroNoSaldo.isPresent()) {
            transacao.rejeitar(erroNoSaldo.get());
        } else {
            transacao.aprovar();
        }
    }

    // o cambio e so informativo: se o servico falhar, a transacao segue sem a taxa
    // e a consulta sinaliza que a conversao nao esta disponivel
    private void registrarCotacoes(Transacao transacao) {
        try {
            Cotacao dolar = cambioPort.buscarCotacaoAtual("USD");
            transacao.setTaxaDolar(dolar.getTaxa());
            transacao.setDataTaxaDolar(dolar.getData());
        } catch (ServicoExternoIndisponivelException e) {
            transacao.setTaxaDolar(null);
            transacao.setDataTaxaDolar(null);
        }

        try {
            Cotacao euro = cambioPort.buscarCotacaoAtual("EUR");
            transacao.setTaxaEuro(euro.getTaxa());
            transacao.setDataTaxaEuro(euro.getData());
        } catch (ServicoExternoIndisponivelException e) {
            transacao.setTaxaEuro(null);
            transacao.setDataTaxaEuro(null);
        }
    }
}
