package br.com.gabrieloliveira.transacoes.application.port.in;

import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;

import java.time.LocalDate;
import java.util.List;

// lista as transacoes de um usuario, da mais recente para a mais antiga
// inicio, fim e tipo sao filtros opcionais (null = sem filtro), e as datas incluem o dia inteiro
public interface ListarTransacoesUseCase {

    List<Transacao> listar(Long usuarioId, LocalDate inicio, LocalDate fim, TipoTransacao tipo);
}
