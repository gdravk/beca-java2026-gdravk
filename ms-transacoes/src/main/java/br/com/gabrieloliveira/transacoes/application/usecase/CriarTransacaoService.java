package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.in.CriarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.out.SaldoContaPort;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.exception.ServicoExternoIndisponivelException;
import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.SaldoConta;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import br.com.gabrieloliveira.transacoes.domain.service.ValidadorDeTransacao;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

// o caminho da criacao de uma transacao:
// 1. valida os dados (valor, categoria da compra, destino da transferencia)
// 2. se for uma saida, consulta o saldo e os limites (SaldoContaPort) e aplica as regras do dominio
// 3. define APPROVED ou REJECTED (com o motivo)
// 4. guarda as cotacoes de dolar e euro do momento (PreenchedorDeCotacoes, que usa a CambioPort)
// 5. salva (TransacaoRepositoryPort) e devolve
// a transacao e sempre gravada, seja aprovada ou rejeitada
public class CriarTransacaoService implements CriarTransacaoUseCase {

    private static final String MOTIVO_SALDO_INDISPONIVEL =
            "nao foi possivel consultar o saldo agora (servico de saldo indisponivel), tente novamente mais tarde";
    private static final String MOTIVO_CONTA_NAO_ENCONTRADA =
            "conta do usuario nao encontrada no servico de saldo";

    private final TransacaoRepositoryPort transacaoRepositoryPort;
    private final SaldoContaPort saldoContaPort;
    private final PreenchedorDeCotacoes preenchedorDeCotacoes;
    private final ValidadorDeTransacao validadorDeTransacao;

    // relogio no fuso de brasilia (bean do BeanConfig); sem ele, num container em utc, uma compra feita
    // as 22h no brasil seria gravada no dia seguinte e cairia no dia errado no limite diario e na analise
    private final Clock relogio;

    public CriarTransacaoService(TransacaoRepositoryPort transacaoRepositoryPort,
                                 SaldoContaPort saldoContaPort,
                                 PreenchedorDeCotacoes preenchedorDeCotacoes,
                                 ValidadorDeTransacao validadorDeTransacao,
                                 Clock relogio) {
        this.transacaoRepositoryPort = transacaoRepositoryPort;
        this.saldoContaPort = saldoContaPort;
        this.preenchedorDeCotacoes = preenchedorDeCotacoes;
        this.validadorDeTransacao = validadorDeTransacao;
        this.relogio = relogio;
    }

    @Override
    public Transacao criar(Transacao novaTransacao) {
        novaTransacao.setDataHora(LocalDateTime.now(relogio));

        Optional<String> erroNosDados = validadorDeTransacao.validarDados(novaTransacao);
        if (erroNosDados.isPresent()) {
            novaTransacao.rejeitar(erroNosDados.get());
        } else {
            // a compra ja foi validada com categoria; nos outros tipos a categoria e opcional (e vira OUTROS)
            if (novaTransacao.getCategoria() == null) {
                novaTransacao.setCategoria(Categoria.OUTROS);
            }

            // deposito nao precisa consultar o saldo
            if (novaTransacao.getTipo().isSaida()) {
                aplicarRegrasDeSaldo(novaTransacao);
            } else {
                novaTransacao.aprovar();
            }
        }

        // guarda as taxas de dolar e euro do momento da criacao
        // se a brasilapi estiver fora do ar, a transacao e gravada sem taxa (o cambio e so informativo e nao pode
        // impedir a transacao), e a consulta tenta de novo depois (ver PreenchedorDeCotacoes)
        preenchedorDeCotacoes.preencherFaltantes(novaTransacao);
        return transacaoRepositoryPort.salvar(novaTransacao);
    }

    // sem resposta do servico de saldo, a transacao nunca e aprovada as cegas: fica REJECTED com o motivo
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

}
