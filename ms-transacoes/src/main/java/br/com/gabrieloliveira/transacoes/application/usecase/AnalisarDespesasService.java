package br.com.gabrieloliveira.transacoes.application.usecase;

import br.com.gabrieloliveira.transacoes.application.port.in.AnalisarDespesasUseCase;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.domain.model.AnaliseDeDespesas;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorCategoria;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorDia;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorMes;
import br.com.gabrieloliveira.transacoes.domain.model.Periodo;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import br.com.gabrieloliveira.transacoes.domain.service.AnalisadorDeDespesas;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

// analise de despesas: quanto o usuario gastou por categoria, por dia e por mes
//
// os tres resumos seguem o mesmo caminho:
// 1. resolve o periodo (sem datas = mes atual; inicio depois do fim = 400) -> Periodo
// 2. busca as despesas do usuario nesse periodo no banco -> TransacaoRepositoryPort
// 3. confere de novo a definicao de despesa e agrupa -> AnalisadorDeDespesas (dominio)
// 4. devolve o periodo usado, o total geral e as linhas do resumo -> AnaliseDeDespesas
//
// o agrupamento e feito aqui no java, e nao com "group by" no banco, porque:
// - as regras ficam no dominio, faceis de testar sem banco
// - agrupar por mes no banco usa funcoes diferentes em cada banco de dados (postgres, mysql...)
// - o volume de transacoes de um usuario num periodo e pequeno, entao nao pesa
public class AnalisarDespesasService implements AnalisarDespesasUseCase {

    private final TransacaoRepositoryPort transacaoRepositoryPort;
    private final AnalisadorDeDespesas analisadorDeDespesas;

    // o Clock e o "relogio" usado pra saber qual e o mes atual
    // na aplicacao ele e o relogio do sistema; nos testes, um relogio parado numa data fixa
    // (sem isso, o teste do "mes atual" daria resultado diferente a cada mes)
    private final Clock relogio;

    public AnalisarDespesasService(TransacaoRepositoryPort transacaoRepositoryPort,
                                   AnalisadorDeDespesas analisadorDeDespesas,
                                   Clock relogio) {
        this.transacaoRepositoryPort = transacaoRepositoryPort;
        this.analisadorDeDespesas = analisadorDeDespesas;
        this.relogio = relogio;
    }

    @Override
    public AnaliseDeDespesas<DespesaPorCategoria> porCategoria(Long usuarioId, LocalDate inicio, LocalDate fim) {
        Periodo periodo = resolverPeriodo(inicio, fim);
        List<Transacao> despesas = buscarDespesas(usuarioId, periodo);
        return new AnaliseDeDespesas<>(usuarioId, periodo, despesas.size(), analisadorDeDespesas.somar(despesas),
                analisadorDeDespesas.agruparPorCategoria(despesas));
    }

    @Override
    public AnaliseDeDespesas<DespesaPorDia> porDia(Long usuarioId, LocalDate inicio, LocalDate fim) {
        Periodo periodo = resolverPeriodo(inicio, fim);
        List<Transacao> despesas = buscarDespesas(usuarioId, periodo);
        return new AnaliseDeDespesas<>(usuarioId, periodo, despesas.size(), analisadorDeDespesas.somar(despesas),
                analisadorDeDespesas.agruparPorDia(despesas));
    }

    @Override
    public AnaliseDeDespesas<DespesaPorMes> porMes(Long usuarioId, LocalDate inicio, LocalDate fim) {
        Periodo periodo = resolverPeriodo(inicio, fim);
        List<Transacao> despesas = buscarDespesas(usuarioId, periodo);
        return new AnaliseDeDespesas<>(usuarioId, periodo, despesas.size(), analisadorDeDespesas.somar(despesas),
                analisadorDeDespesas.agruparPorMes(despesas));
    }

    private Periodo resolverPeriodo(LocalDate inicio, LocalDate fim) {
        return Periodo.resolver(inicio, fim, LocalDate.now(relogio));
    }

    // a consulta do banco ja traz so despesas; o filtro do dominio e uma segunda conferencia
    // e garante a regra mesmo se um dia a porta for implementada por outro adaptador
    private List<Transacao> buscarDespesas(Long usuarioId, Periodo periodo) {
        List<Transacao> encontradas = transacaoRepositoryPort.listarDespesas(usuarioId,
                periodo.inicioDoPrimeiroDia(), periodo.inicioDoDiaSeguinteAoFim());
        return analisadorDeDespesas.somenteDespesas(encontradas);
    }
}
