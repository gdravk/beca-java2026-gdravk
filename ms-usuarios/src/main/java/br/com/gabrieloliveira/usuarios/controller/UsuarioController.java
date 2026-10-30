package br.com.gabrieloliveira.usuarios.controller;

import br.com.gabrieloliveira.usuarios.dto.UsuarioRequestDTO;
import br.com.gabrieloliveira.usuarios.dto.UsuarioResponseDTO;
import br.com.gabrieloliveira.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// porta de entrada do ms1: recebe as requisicoes http em /users e devolve json
// o controller nao tem regra de negocio, ele so recebe, chama o service e devolve o status certo
@RestController
@RequestMapping("/users")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // POST /users -> cria um usuario e responde 201 created
    // @Valid manda o spring rodar as validacoes do dto antes de entrar no metodo
    // @RequestBody diz que os dados vem no corpo da requisicao, em json
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO criado = usuarioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    // GET /users -> lista paginada, responde 200
    // da pra escolher a pagina pela url, ex: /users?page=0&size=5&sort=nome
    // se nao mandar nada, vem a primeira pagina com 10 usuarios ordenados por id
    // o PagedModel deixa o json da paginacao num formato fixo e organizado
    @GetMapping
    public ResponseEntity<PagedModel<UsuarioResponseDTO>> listar(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<UsuarioResponseDTO> pagina = usuarioService.listar(pageable);
        return ResponseEntity.ok(new PagedModel<>(pagina));
    }

    // GET /users/{id} -> busca um usuario, responde 200 (ou 404 se nao existir)
    // @PathVariable pega o {id} que veio na url
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuario);
    }

    // PUT /users/{id} -> atualiza um usuario, responde 200
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id,
                                                        @Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO atualizado = usuarioService.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    // DELETE /users/{id} -> exclui um usuario, responde 204 no content (sucesso, mas sem corpo)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
