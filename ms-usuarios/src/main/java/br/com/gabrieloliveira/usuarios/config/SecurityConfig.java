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
// - POST /users: o cadastro (quem ainda nao tem conta precisa conseguir se cadastrar)
// - POST /auth/verificar: a rota que o ms2 usa para conferir o login; nao pede login de usuario,
//   mas pede a chave interna no cabecalho (ver AuthController)
// - swagger: a documentacao fica aberta, como o PO pediu
// todo o resto exige login, inclusive a importacao por csv (POST /users/import): se ela fosse aberta,
// qualquer pessoa conseguiria criar mil contas numa requisicao so (ajuste da revisao do professor)
@Configuration
public class SecurityConfig {

    // o encoder do bcrypt; quem usa ele e o BCryptSenhaEncoderAdapter, para gerar e conferir o hash das senhas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // o "gerente de autenticacao": e ele que o filtro da autenticacao basica chama para conferir o login
    // o provider e criado aqui com new (e nao como @Component) para existir uma vez so: assim cada login
    // errado roda o bcrypt uma vez, e nao duas
    // de quebra, ter este bean desliga o usuario padrao "user" (com senha gerada) que o spring boot criaria sozinho
    @Bean
    public AuthenticationManager authenticationManager(VerificarCredenciaisUseCase verificarCredenciaisUseCase) {
        return new ProviderManager(new CredenciaisAuthenticationProvider(verificarCredenciaisUseCase));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authenticationManager,
                                                   ErroDeSegurancaHandler erroDeSegurancaHandler) throws Exception {
        http
                // o csrf protege formularios de sites com sessao; numa api rest ele so atrapalharia (daria 403 nos POST)
                .csrf(csrf -> csrf.disable())
                // STATELESS: o servidor nao guarda sessao, entao o email e a senha vao em TODA requisicao
                // (e assim que uma api rest costuma funcionar, e e o que a autenticacao basica espera)
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.POST, "/users", "/auth/verificar").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // /error e a rota interna que o spring usa para montar respostas de erro; se ela exigisse login,
                        // alguns erros virariam 401 sem motivo
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                // liga a autenticacao basica (o cabecalho "Authorization: Basic ...")
                .httpBasic(basic -> basic.authenticationEntryPoint(erroDeSegurancaHandler))
                // quando a seguranca barra a requisicao, a resposta sai no mesmo json de erro do resto da api
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(erroDeSegurancaHandler)
                        .accessDeniedHandler(erroDeSegurancaHandler))
                // quem confere o email e a senha e o gerente de autenticacao acima, com o nosso provider
                .authenticationManager(authenticationManager);

        return http.build();
    }
}
