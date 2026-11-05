package br.com.gabrieloliveira.usuarios.config;

import br.com.gabrieloliveira.usuarios.adapter.in.web.seguranca.CredenciaisAuthenticationProvider;
import br.com.gabrieloliveira.usuarios.adapter.in.web.seguranca.ErroDeSegurancaHandler;
import br.com.gabrieloliveira.usuarios.application.port.in.VerificarCredenciaisUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// seguranca do ms1 (pedido 6 do PO): autenticacao basica com o email e a senha dos usuarios cadastrados
//
// rotas abertas (sem login de usuario):
// - POST /users          -> cadastro (quem ainda nao tem conta precisa conseguir se cadastrar)
// - POST /auth/verificar -> rota que o ms2 usa pra conferir o login; nao exige login de usuario,
//                           mas exige a chave interna no cabecalho (ver AuthController)
// - swagger              -> a documentacao fica aberta, como o PO pediu
// todo o resto exige login, inclusive a importacao por csv (POST /users/import):
// aberta, ela deixaria qualquer pessoa criar mil contas numa requisicao so (ajuste da revisao do professor)
@Configuration
public class SecurityConfig {

    // o encoder bcrypt; quem usa ele e o BCryptSenhaEncoderAdapter, pra gerar e conferir o hash das senhas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // o "gerente de autenticacao": e ele que o filtro da autenticacao basica chama pra conferir o login
    // o provider e criado aqui com new (e nao como @Component) pra existir uma vez so:
    // assim cada login errado confere o bcrypt uma vez, e nao duas
    // ter esse bean tambem desliga o usuario padrao "user" com senha gerada que o spring boot criaria sozinho
    @Bean
    public AuthenticationManager authenticationManager(VerificarCredenciaisUseCase verificarCredenciaisUseCase) {
        return new ProviderManager(new CredenciaisAuthenticationProvider(verificarCredenciaisUseCase));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authenticationManager,
                                                   ErroDeSegurancaHandler erroDeSegurancaHandler) throws Exception {
        http
                // csrf protege formularios de sites com sessao; numa api rest ele so atrapalharia (daria 403 nos POST)
                .csrf(csrf -> csrf.disable())
                // STATELESS = o servidor nao guarda sessao: o email e a senha vao em TODA requisicao
                // (e o jeito normal de uma api rest funcionar, e o que a autenticacao basica espera)
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.POST, "/users", "/auth/verificar").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // /error e a rota interna que o spring usa pra montar respostas de erro;
                        // se ela exigisse login, alguns erros virariam 401 sem motivo
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                // liga a autenticacao basica (cabecalho "Authorization: Basic ...")
                .httpBasic(basic -> basic.authenticationEntryPoint(erroDeSegurancaHandler))
                // quando a seguranca barra a requisicao, a resposta sai no mesmo json de erro da api
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(erroDeSegurancaHandler)
                        .accessDeniedHandler(erroDeSegurancaHandler))
                // quem confere o email e a senha e o gerente acima, com o nosso provider
                .authenticationManager(authenticationManager);

        return http.build();
    }
}
