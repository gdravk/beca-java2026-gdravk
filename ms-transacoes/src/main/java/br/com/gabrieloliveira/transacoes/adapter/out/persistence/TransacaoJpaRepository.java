package br.com.gabrieloliveira.transacoes.adapter.out.persistence;

import br.com.gabrieloliveira.transacoes.domain.model.StatusTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

// repositorio spring data da tabela transacoes; usado apenas pelo TransacaoPersistenceAdapter
// as consultas com @Query sao jpql: falam em nomes de classe e atributo (TransacaoEntity, dataHora), nao de tabela
public interface TransacaoJpaRepository extends JpaRepository<TransacaoEntity, Long> {

    @Query("select t from TransacaoEntity t"
            + " where t.usuarioId = :usuarioId and t.dataHora >= :de and t.dataHora < :ate"
            + " order by t.dataHora desc")
    List<TransacaoEntity> listarPorUsuarioNoPeriodo(@Param("usuarioId") Long usuarioId,
                                                    @Param("de") LocalDateTime de,
                                                    @Param("ate") LocalDateTime ate);

    @Query("select t from TransacaoEntity t"
            + " where t.usuarioId = :usuarioId and t.tipo = :tipo and t.dataHora >= :de and t.dataHora < :ate"
            + " order by t.dataHora desc")
    List<TransacaoEntity> listarPorUsuarioETipoNoPeriodo(@Param("usuarioId") Long usuarioId,
                                                         @Param("tipo") TipoTransacao tipo,
                                                         @Param("de") LocalDateTime de,
                                                         @Param("ate") LocalDateTime ate);

    // coalesce devolve 0 quando nao ha nenhuma transacao no periodo (sem ele, a soma viria null)
    @Query("select coalesce(sum(t.valor), 0) from TransacaoEntity t"
            + " where t.usuarioId = :usuarioId and t.status = :status and t.tipo in :tipos"
            + " and t.dataHora >= :de and t.dataHora < :ate")
    BigDecimal somarValores(@Param("usuarioId") Long usuarioId,
                            @Param("status") StatusTransacao status,
                            @Param("tipos") Collection<TipoTransacao> tipos,
                            @Param("de") LocalDateTime de,
                            @Param("ate") LocalDateTime ate);

    boolean existsByTransacaoOriginalIdAndStatus(Long transacaoOriginalId, StatusTransacao status);
}
