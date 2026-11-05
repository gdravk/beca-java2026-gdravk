package br.com.gabrieloliveira.transacoes.adapter.in.web;

import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.DespesasPorCategoriaResponseDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.DespesasPorDiaResponseDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.DespesasPorMesResponseDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.UsuarioLogado;
import br.com.gabrieloliveira.transacoes.application.port.in.AnalisarDespesasUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// endpoints da analise de despesas (pedido 5 do PO)
// tres rotas, uma pra cada resumo:
//   GET /transactions/analysis/category?inicio=2026-11-01&fim=2026-11-30
//   GET /transactions/analysis/daily
//   GET /transactions/analysis/monthly?inicio=2026-01-01&fim=2026-12-31
// inicio e fim sao opcionais (formato aaaa-mm-dd); sem eles, a analise e do mes atual
// a analise e sempre do usuario logado; o usuarioId e opcional e, se vier, tem que ser o dele (senao 403)
@RestController
@RequestMapping("/transactions/analysis")
public class AnaliseDespesasController {

    private final AnalisarDespesasUseCase analisarDespesasUseCase;
    private final AnaliseDespesasWebMapper analiseDespesasWebMapper;
    private final UsuarioLogado usuarioLogado;

    public AnaliseDespesasController(AnalisarDespesasUseCase analisarDespesasUseCase,
                                     AnaliseDespesasWebMapper analiseDespesasWebMapper,
                                     UsuarioLogado usuarioLogado) {
        this.analisarDespesasUseCase = analisarDespesasUseCase;
        this.analiseDespesasWebMapper = analiseDespesasWebMapper;
        this.usuarioLogado = usuarioLogado;
    }

    // total de cada categoria e o total geral, da categoria que mais gastou pra que menos gastou
    @GetMapping("/category")
    public ResponseEntity<DespesasPorCategoriaResponseDTO> porCategoria(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        Long dono = usuarioLogado.resolverUsuario(usuarioId);
        return ResponseEntity.ok(analiseDespesasWebMapper.paraCategoriaResponse(
                analisarDespesasUseCase.porCategoria(dono, inicio, fim)));
    }

    // total gasto em cada dia do periodo (so os dias com despesa)
    @GetMapping("/daily")
    public ResponseEntity<DespesasPorDiaResponseDTO> porDia(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        Long dono = usuarioLogado.resolverUsuario(usuarioId);
        return ResponseEntity.ok(analiseDespesasWebMapper.paraDiaResponse(
                analisarDespesasUseCase.porDia(dono, inicio, fim)));
    }

    // total gasto em cada mes do periodo (so os meses com despesa)
    @GetMapping("/monthly")
    public ResponseEntity<DespesasPorMesResponseDTO> porMes(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        Long dono = usuarioLogado.resolverUsuario(usuarioId);
        return ResponseEntity.ok(analiseDespesasWebMapper.paraMesResponse(
                analisarDespesasUseCase.porMes(dono, inicio, fim)));
    }
}
