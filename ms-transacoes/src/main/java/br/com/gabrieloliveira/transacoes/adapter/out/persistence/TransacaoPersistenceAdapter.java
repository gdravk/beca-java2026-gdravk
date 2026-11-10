package br.com.gabrieloliveira.transacoes.adapter.out.persistence;

import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.model.StatusTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// implementa a TransacaoRepositoryPort com jpa e postgres
// recebe e devolve a Transacao do dominio; a TransacaoEntity nunca sai deste pacote
@Component
public class TransacaoPersistenceAdapter implements TransacaoRepositoryPort {

    private static final List<TipoTransacao> TIPOS_DE_SAIDA =
            List.of(TipoTransacao.RETIRADA, TipoTransacao.TRANSFERENCIA, TipoTransacao.COMPRA);

    private final TransacaoJpaRepository transacaoJpaRepository;
    private final TransacaoPersistenceMapper transacaoPersistenceMapper;

    public TransacaoPersistenceAdapter(TransacaoJpaRepository transacaoJpaRepository,
                                       TransacaoPersistenceMapper transacaoPersistenceMapper) {
        this.transacaoJpaRepository = transacaoJpaRepository;
        this.transacaoPersistenceMapper = transacaoPersistenceMapper;
    }

    @Override
    public Transacao salvar(Transacao transacao) {
        TransacaoEntity salva = transacaoJpaRepository.save(transacaoPersistenceMapper.paraEntity(transacao));
        return transacaoPersistenceMapper.paraDominio(salva);
    }

    @Override
    public Optional<Transacao> buscarPorId(Long id) {
        return transacaoJpaRepository.findById(id)
                .map(entity -> transacaoPersistenceMapper.paraDominio(entity));
    }

    @Override
    public List<Transacao> listarPorUsuario(Long usuarioId, LocalDateTime de, LocalDateTime ate, TipoTransacao tipo) {
        List<TransacaoEntity> encontradas;
        if (tipo == null) {
            encontradas = transacaoJpaRepository.listarPorUsuarioNoPeriodo(usuarioId, de, ate);
        } else {
            encontradas = transacaoJpaRepository.listarPorUsuarioETipoNoPeriodo(usuarioId, tipo, de, ate);
        }

        List<Transacao> transacoes = new ArrayList<>();
        for (TransacaoEntity entity : encontradas) {
            transacoes.add(transacaoPersistenceMapper.paraDominio(entity));
        }
        return transacoes;
    }

    @Override
    public BigDecimal somarSaidasAprovadasNoDia(Long usuarioId, LocalDate dia) {
        LocalDateTime inicioDoDia = dia.atStartOfDay();
        LocalDateTime inicioDoDiaSeguinte = dia.plusDays(1).atStartOfDay();
        return transacaoJpaRepository.somarValores(usuarioId, StatusTransacao.APPROVED, TIPOS_DE_SAIDA,
                inicioDoDia, inicioDoDiaSeguinte);
    }

    @Override
    public boolean existeEstornoAprovado(Long idTransacaoOriginal) {
        return transacaoJpaRepository.existsByTransacaoOriginalIdAndStatus(idTransacaoOriginal, StatusTransacao.APPROVED);
    }

    @Override
    public List<Transacao> listarDespesas(Long usuarioId, LocalDateTime de, LocalDateTime ate) {
        List<TransacaoEntity> encontradas = transacaoJpaRepository.listarDespesas(usuarioId,
                StatusTransacao.APPROVED, TIPOS_DE_SAIDA, de, ate);

        List<Transacao> despesas = new ArrayList<>();
        for (TransacaoEntity entity : encontradas) {
            despesas.add(transacaoPersistenceMapper.paraDominio(entity));
        }
        return despesas;
    }
}
