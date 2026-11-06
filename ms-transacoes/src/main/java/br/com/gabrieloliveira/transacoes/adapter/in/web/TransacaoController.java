package br.com.gabrieloliveira.transacoes.adapter.in.web;

import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.TransacaoRequestDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.dto.TransacaoResponseDTO;
import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.UsuarioLogado;
import br.com.gabrieloliveira.transacoes.application.port.in.BuscarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.CriarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.EstornarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.ListarTransacoesUseCase;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.Transacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
// todas as rotas exigem login (SecurityConfig) e so mostram ou criam transacoes do usuario logado
@RestController
@RequestMapping("/transactions")
// os ids da url e do filtro sao validados direto no parametro (fase 13): zero ou negativo responde 400
// o spring mvc faz essa validacao sozinho (sem @Validated), igual no ms1
public class TransacaoController {

    private static final String ID_POSITIVO = "o id deve ser um numero positivo";

    private final CriarTransacaoUseCase criarTransacaoUseCase;
    private final BuscarTransacaoUseCase buscarTransacaoUseCase;
    private final ListarTransacoesUseCase listarTransacoesUseCase;
    private final EstornarTransacaoUseCase estornarTransacaoUseCase;
    private final TransacaoWebMapper transacaoWebMapper;
    private final UsuarioLogado usuarioLogado;

    public TransacaoController(CriarTransacaoUseCase criarTransacaoUseCase,
                               BuscarTransacaoUseCase buscarTransacaoUseCase,
                               ListarTransacoesUseCase listarTransacoesUseCase,
                               EstornarTransacaoUseCase estornarTransacaoUseCase,
                               TransacaoWebMapper transacaoWebMapper,
                               UsuarioLogado usuarioLogado) {
        this.criarTransacaoUseCase = criarTransacaoUseCase;
        this.buscarTransacaoUseCase = buscarTransacaoUseCase;
        this.listarTransacoesUseCase = listarTransacoesUseCase;
        this.estornarTransacaoUseCase = estornarTransacaoUseCase;
        this.transacaoWebMapper = transacaoWebMapper;
        this.usuarioLogado = usuarioLogado;
    }

    // POST /transactions -> 201 com id e status, mesmo quando a transacao e rejeitada
    // (a rejeicao e um resultado de negocio gravado, nao um erro da requisicao)
    // a transacao sempre fica no nome de quem esta logado; mandar o usuarioId de outro responde 403
    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> criar(@Valid @RequestBody TransacaoRequestDTO dto) {
        Transacao novaTransacao = transacaoWebMapper.paraDominio(dto);
        novaTransacao.setUsuarioId(usuarioLogado.resolverUsuario(dto.getUsuarioId()));
        Transacao criada = criarTransacaoUseCase.criar(novaTransacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoWebMapper.paraResponse(criada));
    }

    // GET /transactions/{id} -> 200 com valor em reais, dolar e euro (ou 404)
    // transacao de outro usuario tambem responde 404 (o caso de uso confere o dono antes de buscar o cambio)
    @GetMapping("/{id}")
    public ResponseEntity<TransacaoResponseDTO> buscarPorId(@PathVariable @Positive(message = ID_POSITIVO) Long id) {
        Transacao transacao = buscarTransacaoUseCase.buscarPorId(id, usuarioLogado.id());
        return ResponseEntity.ok(transacaoWebMapper.paraResponse(transacao));
    }

    // GET /transactions?inicio=2026-11-01&fim=2026-11-30&tipo=COMPRA
    // todos os filtros sao opcionais; datas no formato aaaa-mm-dd
    // lista as transacoes de quem esta logado; o usuarioId so e aceito se for o do proprio usuario (senao 403)
    @GetMapping
    public ResponseEntity<List<TransacaoResponseDTO>> listar(
            @RequestParam(required = false) @Positive(message = ID_POSITIVO) Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) TipoTransacao tipo) {
        Long dono = usuarioLogado.resolverUsuario(usuarioId);
        List<Transacao> transacoes = listarTransacoesUseCase.listar(dono, inicio, fim, tipo);
        return ResponseEntity.ok(transacaoWebMapper.paraListaResponse(transacoes));
    }

    // POST /transactions/{id}/estorno -> 201 com a nova transacao de estorno
    // so o dono da transacao pode estornar (a conferencia e feita no caso de uso, antes de criar o estorno)
    @PostMapping("/{id}/estorno")
    public ResponseEntity<TransacaoResponseDTO> estornar(@PathVariable @Positive(message = ID_POSITIVO) Long id) {
        Transacao estorno = estornarTransacaoUseCase.estornar(id, usuarioLogado.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoWebMapper.paraResponse(estorno));
    }
}
