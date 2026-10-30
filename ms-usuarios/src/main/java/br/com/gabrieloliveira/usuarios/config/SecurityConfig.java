package br.com.gabrieloliveira.usuarios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// como o spring security esta no pom, ele bloqueia tudo por padrao (401 em qualquer rota)
// por enquanto liberamos tudo pra conseguir testar o crud no postman e no swagger
// na fase de seguranca essa classe vai ser trocada: so login, cadastro e swagger ficam livres
@Configuration
public class SecurityConfig {

    // o encoder bcrypt; quem usa ele e o BCryptSenhaEncoderAdapter, pra gerar o hash das senhas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // csrf protege formularios de sites com sessao; numa api rest ele so atrapalharia (daria 403 nos POST)
                .csrf(csrf -> csrf.disable())
                // temporario: todas as rotas liberadas, sem precisar de login
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}
