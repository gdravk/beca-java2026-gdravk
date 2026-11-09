package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.in.GerarRelatorioUseCase;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.model.Periodo;
import br.com.gabrieloliveira.transacoes.domain.model.RelatorioTransacoes;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// fluxo do relatorio:
// 1. resolve o periodo (sem datas = mes atual; inicio depois do fim = 400), igual a analise de despesas
// 2. busca todas as transacoes do usuario no periodo (aprovadas e rejeitadas, de todos os tipos)
// 3. completa as cotacoes de dolar e euro que faltarem, pra o arquivo trazer o valor convertido
// 4. ordena da mais antiga para a mais nova (numa planilha, o natural e ler de cima pra baixo no tempo)
// 5. monta o relatorio com o resumo
public class GerarRelatorioService implements GerarRelatorioUseCase {

    private final TransacaoRepositoryPort transacaoRepositoryPort;
    private final PreenchedorDeCotacoes preenchedorDeCotacoes;
    // relogio no fuso de brasilia: decide qual e o "mes atual" quando nao vem data
    private final Clock relogio;

    public GerarRelatorioService(TransacaoRepositoryPort transacaoRepositoryPort,
                                 PreenchedorDeCotacoes preenchedorDeCotacoes,
                                 Clock relogio) {
        this.transacaoRepositoryPort = transacaoRepositoryPort;
        this.preenchedorDeCotacoes = preenchedorDeCotacoes;
        this.relogio = relogio;
    }

    @Override
    public RelatorioTransacoes gerar(Long usuarioId, LocalDate inicio, LocalDate fim) {
        Periodo periodo = Periodo.resolver(inicio, fim, LocalDate.now(relogio));

        // tipo null = todos os tipos
        List<Transacao> transacoes = new ArrayList<>(transacaoRepositoryPort.listarPorUsuario(usuarioId,
                periodo.inicioDoPrimeiroDia(), periodo.inicioDoDiaSeguinteAoFim(), null));

        preenchedorDeCotacoes.preencherFaltantes(transacoes);

        // o banco devolve da mais nova pra mais antiga; aqui invertemos
        // o id desempata duas transacoes com a mesma data e hora
        transacoes.sort(Comparator.comparing(Transacao::getDataHora).thenComparing(Transacao::getId));

        return RelatorioTransacoes.montar(usuarioId, periodo, transacoes);
    }
}
