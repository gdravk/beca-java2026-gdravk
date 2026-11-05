package br.com.gabrieloliveira.usuarios.adapter.in.web;

import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.CredenciaisRequestDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.CredenciaisVerificadasResponseDTO;
import br.com.gabrieloliveira.usuarios.application.port.in.VerificarCredenciaisUseCase;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// rota que o ms2 usa pra saber quem e o usuario (seguranca, pedido 6)
//
// o caminho de uma requisicao no ms2:
// 1. o cliente chama o ms2 com email e senha (autenticacao basica)
// 2. o ms2 chama aqui: POST /auth/verificar { "email": "...", "senha": "..." }
// 3. aqui conferimos a senha com o bcrypt e respondemos o id do usuario (200) ou 401
// 4. o ms2 usa esse id pra garantir que o usuario so mexe nas proprias transacoes
//
// a rota fica aberta (sem login) porque e ela mesma que confere o login
// e POST (e nao GET) pra senha ir no corpo, e nao na url (url aparece em log de servidor)
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final VerificarCredenciaisUseCase verificarCredenciaisUseCase;

    public AuthController(VerificarCredenciaisUseCase verificarCredenciaisUseCase) {
        this.verificarCredenciaisUseCase = verificarCredenciaisUseCase;
    }

    // 200 com o usuario, 401 se o email ou a senha estiverem errados, 400 se faltar campo
    @PostMapping("/verificar")
    public ResponseEntity<CredenciaisVerificadasResponseDTO> verificar(@Valid @RequestBody CredenciaisRequestDTO dto) {
        Usuario usuario = verificarCredenciaisUseCase.verificar(dto.getEmail(), dto.getSenha());
        return ResponseEntity.ok(new CredenciaisVerificadasResponseDTO(usuario.getId(), usuario.getNome(),
                usuario.getEmail()));
    }
}
