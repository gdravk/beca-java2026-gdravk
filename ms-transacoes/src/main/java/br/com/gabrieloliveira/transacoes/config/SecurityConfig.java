package br.com.gabrieloliveira.transacoes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

// configuracao provisoria: todas as rotas liberadas para testar o fluxo de transacoes no postman e no swagger
// com a seguranca basica (pedido 6), transacoes, saldo, analise e relatorio passam a exigir login
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // api rest sem sessao de navegador: csrf desligado para os POST funcionarem
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}
