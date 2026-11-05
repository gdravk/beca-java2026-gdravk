package br.com.gabrieloliveira.transacoes.config;

import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.ErroDeSegurancaHandler;
import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.Ms1AuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// seguranca do ms2 (pedido 6 do PO): autenticacao basica, conferida no ms1
//
// - transacoes, saldo, analise (e o relatorio, quando existir) exigem login
// - o swagger fica aberto, como o PO pediu
// - o ms2 nao guarda usuario nem senha: cada login e conferido no ms1 (Ms1AuthenticationProvider)
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   Ms1AuthenticationProvider ms1AuthenticationProvider,
                                                   ErroDeSegurancaHandler erroDeSegurancaHandler) throws Exception {
        http
                // api rest sem sessao de navegador: csrf desligado para os POST funcionarem
                .csrf(csrf -> csrf.disable())
                // sem sessao: o email e a senha vao em toda requisicao
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.authenticationEntryPoint(erroDeSegurancaHandler))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(erroDeSegurancaHandler)
                        .accessDeniedHandler(erroDeSegurancaHandler))
                .authenticationProvider(ms1AuthenticationProvider);

        return http.build();
    }
}
