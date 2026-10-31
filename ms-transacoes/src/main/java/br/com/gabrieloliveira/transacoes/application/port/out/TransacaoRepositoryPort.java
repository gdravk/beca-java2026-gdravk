package br.com.gabrieloliveira.transacoes.application.port.out;

import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// o que os casos de uso precisam do banco de transacoes; implementada por TransacaoPersistenceAdapter
// nao existe metodo de exclusao: transacao nunca e apagada
public interface TransacaoRepositoryPort {

    Transacao salvar(Transacao transacao);

    Optional<Transacao> buscarPorId(Long id);

    // periodo fechado no inicio e aberto no fim: [de, ate)
    // tipo null = todos os tipos
    List<Transacao> listarPorUsuario(Long usuarioId, LocalDateTime de, LocalDateTime ate, TipoTransacao tipo);

    // soma das saidas aprovadas (retirada, transferencia e compra) do usuario no dia, usada no limite diario
    BigDecimal somarSaidasAprovadasNoDia(Long usuarioId, LocalDate dia);

    // true se a transacao ja tem um estorno aprovado
    boolean existeEstornoAprovado(Long idTransacaoOriginal);
}
