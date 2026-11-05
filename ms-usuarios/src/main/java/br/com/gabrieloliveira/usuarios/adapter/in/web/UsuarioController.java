package br.com.gabrieloliveira.usuarios.adapter.in.web;

import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.PaginaResponseDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.UsuarioRequestDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.UsuarioResponseDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.UsuarioResumoResponseDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.UsuarioUpdateRequestDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.seguranca.UsuarioAutenticado;
import br.com.gabrieloliveira.usuarios.application.port.in.AtualizarUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.in.BuscarUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.in.CriarUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.in.ExcluirUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.in.ListarUsuariosUseCase;
import br.com.gabrieloliveira.usuarios.domain.exception.AcessoNegadoException;
import br.com.gabrieloliveira.usuarios.domain.model.Pagina;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// adaptador de entrada: a porta http do ms1, recebe as requisicoes em /users e devolve json
// ele nao tem regra de negocio: converte o dto, chama a porta de entrada certa e devolve o status
// repara que ele so conhece as interfaces (UseCase), nunca as classes que implementam
@RestController
@RequestMapping("/users")
public class UsuarioController {

    private final CriarUsuarioUseCase criarUsuarioUseCase;
    private final ListarUsuariosUseCase listarUsuariosUseCase;
    private final BuscarUsuarioUseCase buscarUsuarioUseCase;
    private final AtualizarUsuarioUseCase atualizarUsuarioUseCase;
    private final ExcluirUsuarioUseCase excluirUsuarioUseCase;
    private final UsuarioWebMapper usuarioWebMapper;

    public UsuarioController(CriarUsuarioUseCase criarUsuarioUseCase,
                             ListarUsuariosUseCase listarUsuariosUseCase,
                             BuscarUsuarioUseCase buscarUsuarioUseCase,
                             AtualizarUsuarioUseCase atualizarUsuarioUseCase,
                             ExcluirUsuarioUseCase excluirUsuarioUseCase,
                             UsuarioWebMapper usuarioWebMapper) {
        this.criarUsuarioUseCase = criarUsuarioUseCase;
        this.listarUsuariosUseCase = listarUsuariosUseCase;
        this.buscarUsuarioUseCase = buscarUsuarioUseCase;
        this.atualizarUsuarioUseCase = atualizarUsuarioUseCase;
        this.excluirUsuarioUseCase = excluirUsuarioUseCase;
        this.usuarioWebMapper = usuarioWebMapper;
    }

    // POST /users -> cria um usuario e responde 201 created
    // @Valid manda o spring rodar as validacoes do dto antes de entrar no metodo
    // @RequestBody diz que os dados vem no corpo da requisicao, em json
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioRequestDTO dto) {
        Usuario novoUsuario = usuarioWebMapper.paraDominio(dto);
        Usuario criado = criarUsuarioUseCase.criar(novoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioWebMapper.paraResponse(criado));
    }

    // GET /users -> lista paginada, responde 200
    // da pra escolher a pagina pela url, ex: /users?pagina=0&tamanho=5
    // se nao mandar nada, vem a primeira pagina (0) com 10 usuarios, ordenados por id
    // cada usuario vem sem o cpf (dado pessoal), so com id, nome, email e data de criacao
    @GetMapping
    public ResponseEntity<PaginaResponseDTO<UsuarioResumoResponseDTO>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        Pagina<Usuario> resultado = listarUsuariosUseCase.listar(pagina, tamanho);
        return ResponseEntity.ok(usuarioWebMapper.paraPaginaResponse(resultado));
    }

    // GET /users/{id} -> busca um usuario com todos os dados (inclusive cpf), responde 200 (ou 404 se nao existir)
    // so o proprio usuario pode ver os dados completos dele; o id de outra pessoa responde 403
    // @PathVariable pega o {id} que veio na url
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id,
                                                          @AuthenticationPrincipal UsuarioAutenticado logado) {
        garantirQueEOProprioUsuario(id, logado, "voce so pode ver os dados da sua propria conta");
        Usuario usuario = buscarUsuarioUseCase.buscarPorId(id);
        return ResponseEntity.ok(usuarioWebMapper.paraResponse(usuario));
    }

    // PUT /users/{id} -> atualiza um usuario, responde 200
    // usa o UsuarioUpdateRequestDTO (e nao o UsuarioRequestDTO do cadastro) porque aqui a senha e opcional:
    // se o json nao trouxer "senha", a senha atual e mantida
    // @AuthenticationPrincipal entrega quem esta logado (o UsuarioAutenticado criado no login)
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id,
                                                        @Valid @RequestBody UsuarioUpdateRequestDTO dto,
                                                        @AuthenticationPrincipal UsuarioAutenticado logado) {
        garantirQueEOProprioUsuario(id, logado, "voce so pode atualizar a sua propria conta");
        Usuario dadosNovos = usuarioWebMapper.paraDominio(dto);
        Usuario atualizado = atualizarUsuarioUseCase.atualizar(id, dadosNovos);
        return ResponseEntity.ok(usuarioWebMapper.paraResponse(atualizado));
    }

    // DELETE /users/{id} -> exclui um usuario, responde 204 no content (sucesso, mas sem corpo)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id,
                                        @AuthenticationPrincipal UsuarioAutenticado logado) {
        garantirQueEOProprioUsuario(id, logado, "voce so pode excluir a sua propria conta");
        excluirUsuarioUseCase.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // seguranca (pedido 6): cada usuario so pode ver os dados completos, alterar ou excluir a propria conta
    // a listagem continua liberada pra qualquer usuario logado, mas sem cpf (e nunca com senha)
    private void garantirQueEOProprioUsuario(Long idDaRota, UsuarioAutenticado logado, String mensagem) {
        if (!idDaRota.equals(logado.getId())) {
            throw new AcessoNegadoException(mensagem);
        }
    }
}
