package br.com.gabrieloliveira.transacoes.application.port.in;

import br.com.gabrieloliveira.transacoes.domain.model.RelatorioTransacoes;

import java.time.LocalDate;

// porta de entrada do relatorio de transacoes (pedido 7 do PO)
// inicio e fim seguem a mesma regra da analise de despesas: sem nenhum dos dois, o relatorio e do mes atual
public interface GerarRelatorioUseCase {

    RelatorioTransacoes gerar(Long usuarioId, LocalDate inicio, LocalDate fim);
}
