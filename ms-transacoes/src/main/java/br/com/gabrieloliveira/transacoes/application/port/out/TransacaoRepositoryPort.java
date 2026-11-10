package br.com.gabrieloliveira.transacoes.application.port.out;

import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// o que os casos de uso precisam do banco de transacoes; quem implementa e o TransacaoPersistenceAdapter
// nao existe metodo de exclusao: transacao nunca e apagada
public interface TransacaoRepositoryPort {

    Transacao salvar(Transacao transacao);

    Optional<Transacao> buscarPorId(Long id);

    // o periodo inclui o "de" e nao inclui o "ate" ([de, ate)); tipo null = todos os tipos
    List<Transacao> listarPorUsuario(Long usuarioId, LocalDateTime de, LocalDateTime ate, TipoTransacao tipo);

    // soma das saidas aprovadas (retirada, transferencia e compra) do usuario no dia; e usada no limite diario
    BigDecimal somarSaidasAprovadasNoDia(Long usuarioId, LocalDate dia);

    // true se a transacao ja tem um estorno aprovado
    boolean existeEstornoAprovado(Long idTransacaoOriginal);

    // despesas do usuario no periodo [de, ate), usadas na analise, da mais antiga para a mais nova
    // despesa = saida (retirada, transferencia, compra) aprovada, que nao e estorno e que nao foi estornada
    List<Transacao> listarDespesas(Long usuarioId, LocalDateTime de, LocalDateTime ate);
}
