package br.com.gabrieloliveira.usuarios.adapter.in.web;

import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.ErroResponseDTO;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
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

// adaptador de entrada: e a porta http do ms1, recebe as requisicoes em /users e devolve json
// nao tem regra de negocio aqui: ele converte o dto, chama o caso de uso certo e devolve o status http
// repara que ele so conhece as interfaces (UseCase), nunca as classes que fazem o trabalho
@RestController
@RequestMapping("/users")
// validacao dos parametros da url: @Positive, @Min e @Max vao direto no parametro
// desde o spring 6.1 o proprio spring mvc valida esses parametros sozinho, sem @Validated na classe
// (com @Validated a validacao rodaria duas vezes). quando algo falha, ele lanca a
// HandlerMethodValidationException, que o GlobalExceptionHandler transforma em 400
//
// swagger: o @Tag da o nome do grupo na tela, o @Operation resume cada rota e o @ApiResponses lista
// os codigos que ela pode responder (os erros vem sempre no formato do ErroResponseDTO)
@Tag(name = "Usuarios", description = "cadastro, consulta, atualizacao e exclusao de usuarios")
public class UsuarioController {

    private static final String ID_POSITIVO = "o id deve ser um numero positivo";

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

    // POST /users: cria um usuario e responde 201 (created)
    // o @Valid manda o spring rodar as validacoes do dto antes de entrar no metodo, e o @RequestBody
    // diz que os dados vem no corpo da requisicao, em json
    // o @SecurityRequirements() vazio tira o cadeado do swagger: o cadastro e uma rota aberta, sem login
    @Operation(summary = "Cadastra um usuario",
            description = "Rota aberta (sem login). A senha e gravada criptografada com BCrypt e nunca volta na resposta.")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "usuario cadastrado"),
            @ApiResponse(responseCode = "400", description = "campos invalidos (a resposta lista cada campo)",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "ja existe um usuario com esse email ou cpf",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioRequestDTO dto) {
        Usuario novoUsuario = usuarioWebMapper.paraDominio(dto);
        Usuario criado = criarUsuarioUseCase.criar(novoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioWebMapper.paraResponse(criado));
    }

    // GET /users: lista paginada, responde 200
    // da para escolher a pagina pela url, como em /users?pagina=0&tamanho=5; sem nada, vem a primeira
    // pagina (0) com 10 usuarios, ordenados por id
    // cada usuario vem sem o cpf (que e dado pessoal), so com id, nome, email e data de criacao
    @Operation(summary = "Lista os usuarios (paginado)",
            description = "Mostra id, nome, email e data de criacao, ordenados por id. O cpf nao aparece na listagem.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "pagina de usuarios"),
            @ApiResponse(responseCode = "400", description = "pagina negativa ou tamanho fora de 1 a 100",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "sem login ou com email/senha errados",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<PaginaResponseDTO<UsuarioResumoResponseDTO>> listar(
            @Parameter(description = "numero da pagina, comecando em 0", example = "0")
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "a pagina comeca em 0 e nao pode ser negativa") int pagina,
            @Parameter(description = "quantos usuarios por pagina (de 1 a 100)", example = "10")
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "o tamanho da pagina deve ser de pelo menos 1")
            @Max(value = 100, message = "o tamanho da pagina pode ser de no maximo 100") int tamanho) {
        Pagina<Usuario> resultado = listarUsuariosUseCase.listar(pagina, tamanho);
        return ResponseEntity.ok(usuarioWebMapper.paraPaginaResponse(resultado));
    }

    // GET /users/{id}: devolve o usuario com todos os dados, inclusive o cpf (ou 404 se ele nao existir)
    // so o proprio usuario pode ver os dados completos dele; o id de outra pessoa responde 403
    // o @PathVariable pega o {id} que veio na url
    @Operation(summary = "Busca os dados completos da propria conta",
            description = "Inclui o cpf. So funciona com o id do usuario logado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "dados do usuario"),
            @ApiResponse(responseCode = "400", description = "id zero ou negativo",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "sem login ou com email/senha errados",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "o id e de outro usuario",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "usuario nao encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(
            @Parameter(description = "id do usuario logado", example = "1")
            @PathVariable @Positive(message = ID_POSITIVO) Long id,
                                                          @AuthenticationPrincipal UsuarioAutenticado logado) {
        garantirQueEOProprioUsuario(id, logado, "voce so pode ver os dados da sua propria conta");
        Usuario usuario = buscarUsuarioUseCase.buscarPorId(id);
        return ResponseEntity.ok(usuarioWebMapper.paraResponse(usuario));
    }

    // PUT /users/{id}: atualiza o usuario e responde 200
    // usa o UsuarioUpdateRequestDTO, e nao o do cadastro, porque aqui a senha e opcional:
    // se o json nao trouxer "senha", a senha atual continua valendo
    // o @AuthenticationPrincipal entrega quem esta logado (o UsuarioAutenticado criado no login)
    @Operation(summary = "Atualiza a propria conta",
            description = "Nome, email e cpf sao obrigatorios. A senha e opcional: sem ela, a senha atual e mantida.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "usuario atualizado"),
            @ApiResponse(responseCode = "400", description = "campos invalidos ou id zero/negativo",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "sem login ou com email/senha errados",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "o id e de outro usuario",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "usuario nao encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "o novo email ou cpf ja e de outro usuario",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @Parameter(description = "id do usuario logado", example = "1")
            @PathVariable @Positive(message = ID_POSITIVO) Long id,
                                                        @Valid @RequestBody UsuarioUpdateRequestDTO dto,
                                                        @AuthenticationPrincipal UsuarioAutenticado logado) {
        garantirQueEOProprioUsuario(id, logado, "voce so pode atualizar a sua propria conta");
        Usuario dadosNovos = usuarioWebMapper.paraDominio(dto);
        Usuario atualizado = atualizarUsuarioUseCase.atualizar(id, dadosNovos);
        return ResponseEntity.ok(usuarioWebMapper.paraResponse(atualizado));
    }

    // DELETE /users/{id}: exclui o usuario e responde 204 (no content: deu certo, mas nao ha corpo na resposta)
    @Operation(summary = "Exclui a propria conta",
            description = "So funciona com o id do usuario logado. Depois disso, o login dessa conta deixa de valer.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "usuario excluido (resposta sem corpo)"),
            @ApiResponse(responseCode = "400", description = "id zero ou negativo",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "sem login ou com email/senha errados",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "o id e de outro usuario",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "usuario nao encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "id do usuario logado", example = "1")
            @PathVariable @Positive(message = ID_POSITIVO) Long id,
                                        @AuthenticationPrincipal UsuarioAutenticado logado) {
        garantirQueEOProprioUsuario(id, logado, "voce so pode excluir a sua propria conta");
        excluirUsuarioUseCase.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // seguranca (pedido 6): cada usuario so pode ver os dados completos, alterar ou excluir a propria conta
    // a listagem continua aberta para qualquer usuario logado, mas sem cpf (e nunca com senha)
    private void garantirQueEOProprioUsuario(Long idDaRota, UsuarioAutenticado logado, String mensagem) {
        if (!idDaRota.equals(logado.getId())) {
            throw new AcessoNegadoException(mensagem);
        }
    }
}
