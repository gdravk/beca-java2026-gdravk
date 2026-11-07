package br.com.gabrieloliveira.transacoes.adapter.in.web;

import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.DespesaPorCategoriaDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.DespesaPorDiaDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.DespesaPorMesDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.DespesasPorCategoriaResponseDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.DespesasPorDiaResponseDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.DespesasPorMesResponseDTO;
import br.com.gabrieloliveira.transacoes.domain.model.AnaliseDeDespesas;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorCategoria;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorDia;
import br.com.gabrieloliveira.transacoes.domain.model.DespesaPorMes;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// converte o resultado da analise (dominio) nos json de resposta dos tres resumos
@Component
public class AnaliseDespesasWebMapper {

    public DespesasPorCategoriaResponseDTO paraCategoriaResponse(AnaliseDeDespesas<DespesaPorCategoria> analise) {
        List<DespesaPorCategoriaDTO> categorias = new ArrayList<>();
        for (DespesaPorCategoria item : analise.getItens()) {
            categorias.add(new DespesaPorCategoriaDTO(item.getCategoria(), item.getQuantidade(), item.getTotal(),
                    item.getPercentual()));
        }

        return new DespesasPorCategoriaResponseDTO(analise.getUsuarioId(), analise.getPeriodo().getInicio(),
                analise.getPeriodo().getFim(), analise.getQuantidadeDespesas(), analise.getTotalGeral(), categorias);
    }

    public DespesasPorDiaResponseDTO paraDiaResponse(AnaliseDeDespesas<DespesaPorDia> analise) {
        List<DespesaPorDiaDTO> dias = new ArrayList<>();
        for (DespesaPorDia item : analise.getItens()) {
            dias.add(new DespesaPorDiaDTO(item.getData(), item.getQuantidade(), item.getTotal()));
        }

        return new DespesasPorDiaResponseDTO(analise.getUsuarioId(), analise.getPeriodo().getInicio(),
                analise.getPeriodo().getFim(), analise.getQuantidadeDespesas(), analise.getTotalGeral(), dias);
    }

    public DespesasPorMesResponseDTO paraMesResponse(AnaliseDeDespesas<DespesaPorMes> analise) {
        List<DespesaPorMesDTO> meses = new ArrayList<>();
        for (DespesaPorMes item : analise.getItens()) {
            // YearMonth.toString() ja devolve no formato "2026-11"
            meses.add(new DespesaPorMesDTO(item.getMes().toString(), item.getQuantidade(), item.getTotal()));
        }

        return new DespesasPorMesResponseDTO(analise.getUsuarioId(), analise.getPeriodo().getInicio(),
                analise.getPeriodo().getFim(), analise.getQuantidadeDespesas(), analise.getTotalGeral(), meses);
    }
}
