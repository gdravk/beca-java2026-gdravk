package br.com.gabrieloliveira.usuarios.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// testes da classe Usuario do dominio
// aqui nao tem mockito: o Usuario e java puro e nao depende de nada, entao e so criar e conferir
// cada teste segue o padrao "dado / quando / entao":
// - dado: prepara os dados
// - quando: executa o que esta sendo testado
// - entao: confere se o resultado foi o esperado
class UsuarioTest {

    // @Test marca o metodo como um teste; o junit roda cada um separado
    // o nome do metodo ja diz o cenario, pra quem ler o relatorio entender sem abrir o codigo
    @Test
    void deveNormalizarEmailAoUsarOSetter() {
        // dado: um usuario novo
        Usuario usuario = new Usuario();

        // quando: o email chega com espacos e letras maiusculas
        usuario.setEmail("  Ana.Souza@Email.COM ");

        // entao: fica guardado sem espacos e todo em minusculo
        // assertEquals(esperado, real): se forem diferentes, o teste falha e mostra os dois valores
        assertEquals("ana.souza@email.com", usuario.getEmail());
    }

    @Test
    void deveNormalizarEmailAoUsarOConstrutor() {
        // o construtor completo e o que o adaptador de persistencia usa ao ler do banco,
        // entao ele tambem precisa normalizar
        Usuario usuario = new Usuario(1L, "Ana", " ANA@EMAIL.COM", "12345678901", "hash", null);

        assertEquals("ana@email.com", usuario.getEmail());
    }

    @Test
    void deveAceitarEmailNullSemDarErro() {
        // email null nao pode quebrar com NullPointerException;
        // quem barra email vazio na api e o @NotBlank do dto
        Usuario usuario = new Usuario();

        usuario.setEmail(null);

        assertNull(usuario.getEmail());
    }
}
