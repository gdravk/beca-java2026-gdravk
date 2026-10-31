package br.com.gabrieloliveira.transacoes.adapter.in.web;

import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.TransacaoRequestDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.TransacaoResponseDTO;
import br.com.gabrieloliveira.transacoes.application.port.in.BuscarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.CriarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.EstornarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.ListarTransacoesUseCase;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

// endpoints de transacao do ms2
// nao existe DELETE nem PUT: transacao nunca e apagada ou alterada, a correcao e feita por estorno
@RestController
@RequestMapping("/transactions")
public class TransacaoController {

    private final CriarTransacaoUseCase criarTransacaoUseCase;
    private final BuscarTransacaoUseCase buscarTransacaoUseCase;
    private final ListarTransacoesUseCase listarTransacoesUseCase;
    private final EstornarTransacaoUseCase estornarTransacaoUseCase;
    private final TransacaoWebMapper transacaoWebMapper;

    public TransacaoController(CriarTransacaoUseCase criarTransacaoUseCase,
                               BuscarTransacaoUseCase buscarTransacaoUseCase,
                               ListarTransacoesUseCase listarTransacoesUseCase,
                               EstornarTransacaoUseCase estornarTransacaoUseCase,
                               TransacaoWebMapper transacaoWebMapper) {
        this.criarTransacaoUseCase = criarTransacaoUseCase;
        this.buscarTransacaoUseCase = buscarTransacaoUseCase;
        this.listarTransacoesUseCase = listarTransacoesUseCase;
        this.estornarTransacaoUseCase = estornarTransacaoUseCase;
        this.transacaoWebMapper = transacaoWebMapper;
    }

    // POST /transactions -> 201 com id e status, mesmo quando a transacao e rejeitada
    // (a rejeicao e um resultado de negocio gravado, nao um erro da requisicao)
    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> criar(@Valid @RequestBody TransacaoRequestDTO dto) {
        Transacao criada = criarTransacaoUseCase.criar(transacaoWebMapper.paraDominio(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoWebMapper.paraResponse(criada));
    }

    // GET /transactions/{id} -> 200 com valor em reais, dolar e euro (ou 404)
    @GetMapping("/{id}")
    public ResponseEntity<TransacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        Transacao transacao = buscarTransacaoUseCase.buscarPorId(id);
        return ResponseEntity.ok(transacaoWebMapper.paraResponse(transacao));
    }

    // GET /transactions?usuarioId=1&inicio=2026-11-01&fim=2026-11-30&tipo=COMPRA
    // inicio, fim e tipo sao opcionais; datas no formato aaaa-mm-dd
    @GetMapping
    public ResponseEntity<List<TransacaoResponseDTO>> listar(
            @RequestParam Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) TipoTransacao tipo) {
        List<Transacao> transacoes = listarTransacoesUseCase.listar(usuarioId, inicio, fim, tipo);
        return ResponseEntity.ok(transacaoWebMapper.paraListaResponse(transacoes));
    }

    // POST /transactions/{id}/estorno -> 201 com a nova transacao de estorno
    @PostMapping("/{id}/estorno")
    public ResponseEntity<TransacaoResponseDTO> estornar(@PathVariable Long id) {
        Transacao estorno = estornarTransacaoUseCase.estornar(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoWebMapper.paraResponse(estorno));
    }
}
