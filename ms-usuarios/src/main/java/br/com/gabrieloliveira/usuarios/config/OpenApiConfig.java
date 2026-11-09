package br.com.gabrieloliveira.usuarios.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// configuracao do swagger
// o @SecurityScheme cria o botao "Authorize" no swagger, onde a pessoa digita email e senha uma vez
// e o swagger passa a mandar o cabecalho "Authorization: Basic ..." em todas as chamadas
// as rotas abertas (cadastro, importacao, /auth/verificar) continuam funcionando mesmo sem preencher
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "MS1 - Usuarios", version = "1.0",
                description = "Cadastro de usuarios, importacao por csv e verificacao de login. O cadastro (POST /users) e "
                        + "aberto; para as outras rotas, clique em Authorize e entre com o email e a senha de um usuario "
                        + "cadastrado. Todos os erros vem no formato ErroResponseDTO."),
        security = @SecurityRequirement(name = "basicAuth"))
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic",
        description = "use o email e a senha de um usuario cadastrado")
public class OpenApiConfig {
}
