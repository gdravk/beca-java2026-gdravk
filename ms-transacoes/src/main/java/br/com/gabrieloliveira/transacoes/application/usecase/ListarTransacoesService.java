package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.in.ListarTransacoesUseCase;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.exception.ParametroInvalidoException;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ListarTransacoesService implements ListarTransacoesUseCase {

    // limites usados quando a data nao vem, para a consulta cobrir todo o historico
    private static final LocalDateTime SEM_DATA_INICIAL = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime SEM_DATA_FINAL = LocalDateTime.of(9999, 12, 31, 0, 0);

    private final TransacaoRepositoryPort transacaoRepositoryPort;
    private final PreenchedorDeCotacoes preenchedorDeCotacoes;

    public ListarTransacoesService(TransacaoRepositoryPort transacaoRepositoryPort,
                                   PreenchedorDeCotacoes preenchedorDeCotacoes) {
        this.transacaoRepositoryPort = transacaoRepositoryPort;
        this.preenchedorDeCotacoes = preenchedorDeCotacoes;
    }

    @Override
    public List<Transacao> listar(Long usuarioId, LocalDate inicio, LocalDate fim, TipoTransacao tipo) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new ParametroInvalidoException("a data inicial nao pode ser depois da data final");
        }

        LocalDateTime de = SEM_DATA_INICIAL;
        if (inicio != null) {
            de = inicio.atStartOfDay();
        }

        // no repositorio o fim nao entra no periodo, por isso usamos o comeco do dia seguinte: assim o dia final entra inteiro
        LocalDateTime ate = SEM_DATA_FINAL;
        if (fim != null) {
            ate = fim.plusDays(1).atStartOfDay();
        }

        List<Transacao> transacoes = transacaoRepositoryPort.listarPorUsuario(usuarioId, de, ate, tipo);

        // completa as taxas de quem foi gravado sem cambio; a brasilapi e chamada no maximo uma vez por moeda
        preenchedorDeCotacoes.preencherFaltantes(transacoes);
        return transacoes;
    }
}
