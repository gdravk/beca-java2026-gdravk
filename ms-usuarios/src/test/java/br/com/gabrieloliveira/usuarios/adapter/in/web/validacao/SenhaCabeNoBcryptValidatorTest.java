package br.com.gabrieloliveira.usuarios.adapter.in.web.validacao;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// teste simples, sem spring: so chama o isValid direto
class SenhaCabeNoBcryptValidatorTest {

    // "ç" e o c com cedilha escrito de um jeito que nao depende da codificacao do arquivo
    // em utf-8 ele ocupa 2 bytes
    private static final String C_CEDILHA = "ç";

    private final SenhaCabeNoBcryptValidator validador = new SenhaCabeNoBcryptValidator();

    @Test
    void senhaNulaPassaPorqueQuemCuidaDissoEOutraAnotacao() {
        assertTrue(validador.isValid(null, null));
    }

    @Test
    void setentaEDuasLetrasSemAcentoPassam() {
        assertTrue(validador.isValid("a".repeat(72), null));
    }

    @Test
    void setentaETresLetrasSemAcentoNaoPassam() {
        assertFalse(validador.isValid("a".repeat(73), null));
    }

    @Test
    void letraComAcentoContaComoDoisBytes() {
        // 36 letras com cedilha = 72 bytes: ainda cabe
        assertTrue(validador.isValid(C_CEDILHA.repeat(36), null));
        // 37 letras = 74 bytes: um @Size(max = 72) deixaria passar, mas o bcrypt nao aceita
        assertFalse(validador.isValid(C_CEDILHA.repeat(37), null));
    }

    @Test
    void oBcryptDeVerdadeAceita72BytesERecusaMaisQueIsso() {
        // mostra o motivo da anotacao existir: o proprio bcrypt lanca excecao acima de 72 bytes
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(4);
        assertDoesNotThrow(() -> bcrypt.encode("a".repeat(72)));
        assertThrows(IllegalArgumentException.class, () -> bcrypt.encode(C_CEDILHA.repeat(37)));
    }
}
