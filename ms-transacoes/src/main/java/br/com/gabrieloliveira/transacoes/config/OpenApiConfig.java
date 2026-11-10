package br.com.gabrieloliveira.transacoes.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// configuracao do swagger
// o @SecurityScheme cria o botao "Authorize": a pessoa digita uma vez o email e a senha de um usuario
// do ms1, e o swagger passa a mandar o cabecalho "Authorization: Basic ..." em todas as chamadas
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "MS2 - Transacoes", version = "1.0",
                description = "Transacoes, saldo, cambio e analise de despesas. Para usar: clique em Authorize e entre com o "
                        + "email e a senha de um usuario cadastrado no ms1 (o login e conferido no ms1 a cada chamada). "
                        + "Todos os erros vem no formato ErroResponseDTO."),
        security = @SecurityRequirement(name = "basicAuth"))
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic",
        description = "use o email e a senha de um usuario cadastrado no ms1")
public class OpenApiConfig {
}
