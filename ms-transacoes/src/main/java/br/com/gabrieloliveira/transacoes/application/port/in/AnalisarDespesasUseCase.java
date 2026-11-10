package br.com.gabrieloliveira.transacoes.application.port.in;

import br.com.gabrieloliveira.transacoes.domain.model.AnaliseDeDespesas;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorCategoria;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorDia;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorMes;

import java.time.LocalDate;

// porta de entrada da analise de despesas (pedido 5 do PO)
// inicio e fim sao opcionais: quando nenhum dos dois vem, a analise usa o mes atual
public interface AnalisarDespesasUseCase {

    AnaliseDeDespesas<DespesaPorCategoria> porCategoria(Long usuarioId, LocalDate inicio, LocalDate fim);

    AnaliseDeDespesas<DespesaPorDia> porDia(Long usuarioId, LocalDate inicio, LocalDate fim);

    AnaliseDeDespesas<DespesaPorMes> porMes(Long usuarioId, LocalDate inicio, LocalDate fim);
}
