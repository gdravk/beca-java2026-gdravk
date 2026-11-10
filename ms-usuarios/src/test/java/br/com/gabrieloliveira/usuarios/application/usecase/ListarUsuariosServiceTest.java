package br.com.gabrieloliveira.usuarios.application.usecase;

import br.com.gabrieloliveira.usuarios.application.port.out.UsuarioRepositoryPort;
import br.com.gabrieloliveira.usuarios.domain.model.Pagina;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// testes do caso de uso de listar usuarios com paginacao
// a regra principal aqui e o "ajuste" dos parametros: pagina negativa vira 0, e o tamanho fica entre 1 e 100;
// os testes conferem, com verify, quais valores o caso de uso realmente pediu ao banco
@ExtendWith(MockitoExtension.class)
class ListarUsuariosServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    private ListarUsuariosService listarUsuariosService;

    @BeforeEach
    void setUp() {
        listarUsuariosService = new ListarUsuariosService(usuarioRepositoryPort);
    }

    @Test
    void deveRepassarPaginaETamanhoQuandoForemValidos() {
        // dado: o banco de mentira devolve uma pagina pronta quando pedirem a pagina 2 com 5 itens
        Pagina<Usuario> paginaDoBanco = new Pagina<>(List.of(), 2, 5, 0, 0);
        when(usuarioRepositoryPort.listar(2, 5)).thenReturn(paginaDoBanco);

        // quando
        Pagina<Usuario> resultado = listarUsuariosService.listar(2, 5);

        // entao: o caso de uso devolve exatamente a pagina que o banco mandou
        // (o assertSame confere se e o MESMO objeto, e nao so um objeto igual)
        assertSame(paginaDoBanco, resultado);
    }

    @Test
    void deveTrocarPaginaNegativaPorZero() {
        listarUsuariosService.listar(-3, 10);

        // entao: ao banco foi pedida a pagina 0, e nao a -3
        verify(usuarioRepositoryPort).listar(0, 10);
    }

    @Test
    void deveLimitarOTamanhoDaPaginaEmCem() {
        listarUsuariosService.listar(0, 500);

        verify(usuarioRepositoryPort).listar(0, 100);
    }

    @Test
    void deveTrocarTamanhoMenorQueUmPorUm() {
        listarUsuariosService.listar(0, 0);

        verify(usuarioRepositoryPort).listar(0, 1);
    }
}
