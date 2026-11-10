package br.com.gabrieloliveira.transacoes.config;

import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.ErroDeSegurancaHandler;
import br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca.Ms1AuthenticationProvider;
import br.com.gabrieloliveira.transacoes.application.port.in.AutenticarUsuarioUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// seguranca do ms2 (pedido 6 do PO): autenticacao basica, conferida no ms1
//
// - transacoes, saldo, analise e relatorio exigem login
// - o swagger fica aberto, como o PO pediu
// - o ms2 nao guarda usuario nem senha: cada login e conferido no ms1 (Ms1AuthenticationProvider)
@Configuration
public class SecurityConfig {

    // o "gerente de autenticacao", com o nosso provider criado aqui com new (e nao como @Component):
    // assim ele existe uma vez so, e cada login errado pergunta ao ms1 uma vez, e nao duas
    // de quebra, ter este bean desliga o usuario padrao "user" (com senha gerada) que o spring boot criaria sozinho
    @Bean
    public AuthenticationManager authenticationManager(AutenticarUsuarioUseCase autenticarUsuarioUseCase) {
        return new ProviderManager(new Ms1AuthenticationProvider(autenticarUsuarioUseCase));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authenticationManager,
                                                   ErroDeSegurancaHandler erroDeSegurancaHandler) throws Exception {
        http
                // api rest sem sessao de navegador: o csrf fica desligado para os POST funcionarem
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
                .authenticationManager(authenticationManager);

        return http.build();
    }
}
