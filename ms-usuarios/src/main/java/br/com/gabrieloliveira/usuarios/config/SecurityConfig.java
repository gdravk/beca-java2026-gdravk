package br.com.gabrieloliveira.usuarios.config;

import br.com.gabrieloliveira.usuarios.adapter.in.web.seguranca.CredenciaisAuthenticationProvider;
import br.com.gabrieloliveira.usuarios.adapter.in.web.seguranca.ErroDeSegurancaHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// seguranca do ms1 (pedido 6 do PO): autenticacao basica com o email e a senha dos usuarios cadastrados
//
// rotas abertas (sem login):
// - POST /users          -> cadastro (quem ainda nao tem conta precisa conseguir se cadastrar)
// - POST /users/import   -> cadastro em massa por csv
// - POST /auth/verificar -> e a rota que o ms2 usa pra conferir o login, entao nao pode exigir login
// - swagger              -> a documentacao fica aberta, como o PO pediu
// todo o resto (listar, buscar, atualizar e excluir usuario) exige login
@Configuration
public class SecurityConfig {

    // o encoder bcrypt; quem usa ele e o BCryptSenhaEncoderAdapter, pra gerar e conferir o hash das senhas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CredenciaisAuthenticationProvider credenciaisAuthenticationProvider,
                                                   ErroDeSegurancaHandler erroDeSegurancaHandler) throws Exception {
        http
                // csrf protege formularios de sites com sessao; numa api rest ele so atrapalharia (daria 403 nos POST)
                .csrf(csrf -> csrf.disable())
                // STATELESS = o servidor nao guarda sessao: o email e a senha vao em TODA requisicao
                // (e o jeito normal de uma api rest funcionar, e o que a autenticacao basica espera)
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.POST, "/users", "/users/import", "/auth/verificar").permitAll()
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
                // quem confere o email e a senha e o nosso provider, que chama o caso de uso
                .authenticationProvider(credenciaisAuthenticationProvider);

        return http.build();
    }
}
