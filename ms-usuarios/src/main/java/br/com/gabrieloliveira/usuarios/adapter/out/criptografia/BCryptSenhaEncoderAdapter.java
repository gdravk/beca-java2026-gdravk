package br.com.gabrieloliveira.usuarios.adapter.out.criptografia;

import br.com.gabrieloliveira.usuarios.application.port.out.SenhaEncoderPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// adaptador de saida que liga a porta SenhaEncoderPort ao bcrypt do spring security
// o PasswordEncoder que chega aqui e criado no SecurityConfig
@Component
public class BCryptSenhaEncoderAdapter implements SenhaEncoderPort {

    private final PasswordEncoder passwordEncoder;

    public BCryptSenhaEncoderAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String codificar(String senhaPura) {
        // o hash do bcrypt nao tem volta: nem a gente consegue descobrir a senha original olhando o banco
        return passwordEncoder.encode(senhaPura);
    }

    @Override
    public boolean confere(String senhaPura, String hashGuardado) {
        // o matches le o "sal" que fica guardado dentro do proprio hash, gera o hash da senha digitada com esse
        // mesmo sal e compara os dois; e por isso que a mesma senha sempre "bate" com o hash guardado
        return passwordEncoder.matches(senhaPura, hashGuardado);
    }
}
