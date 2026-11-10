package br.com.gabrieloliveira.usuarios.adapter.in.web.validacao;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

// faz a conferencia de verdade da anotacao @SenhaCabeNoBcrypt
public class SenhaCabeNoBcryptValidator implements ConstraintValidator<SenhaCabeNoBcrypt, String> {

    // limite do proprio algoritmo bcrypt: o que passar disso ele recusa
    public static final int MAXIMO_DE_BYTES = 72;

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext contexto) {
        // senha null fica por conta do @NotBlank (no cadastro) ou e permitida (no PUT, onde a senha e opcional)
        if (senha == null) {
            return true;
        }
        // o getBytes(UTF_8) transforma o texto nos bytes que o bcrypt vai receber, e ai e so contar
        return senha.getBytes(StandardCharsets.UTF_8).length <= MAXIMO_DE_BYTES;
    }
}
