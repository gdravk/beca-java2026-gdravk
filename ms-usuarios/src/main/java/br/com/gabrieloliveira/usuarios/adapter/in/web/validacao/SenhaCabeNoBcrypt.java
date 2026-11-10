package br.com.gabrieloliveira.usuarios.adapter.in.web.validacao;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// anotacao de validacao criada por nos, usada no campo senha dos dtos
// por que nao basta um @Size(max = 72)? porque o bcrypt (que gera o hash da senha) aceita no maximo
// 72 BYTES, e nao 72 letras: em utf-8 uma letra sem acento ocupa 1 byte, mas um "c" com cedilha ou um
// "a" com acento ocupam 2. como o @Size conta letras, 72 letras com acento passariam e o bcrypt lancaria
// uma excecao, virando erro 500 (ajuste da revisao do professor de 06/11)
//
// o @Constraint liga a anotacao a classe que faz a conferencia de verdade (SenhaCabeNoBcryptValidator)
// message, groups e payload sao os tres campos que toda anotacao de bean validation precisa ter
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SenhaCabeNoBcryptValidator.class)
public @interface SenhaCabeNoBcrypt {

    String message() default "a senha pode ter no maximo 72 bytes (72 letras sem acento; letra com acento conta como 2)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
