package br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca;

import br.com.gabrieloliveira.transacoes.domain.exception.AcessoNegadoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// ajuda os controllers com a regra do pedido 6: "o usuario so ve e cria as proprias transacoes"
//
// as rotas continuam aceitando o usuarioId (na url ou no corpo), mas agora ele e opcional:
// - nao veio: usa o id de quem esta logado
// - veio igual ao logado: segue normal
// - veio diferente: 403, porque e uma tentativa de mexer nos dados de outro usuario
@Component
public class UsuarioLogado {

    // o SecurityContextHolder e onde o spring security guarda quem esta logado na requisicao atual
    // (quem colocou o UsuarioAutenticado la foi o Ms1AuthenticationProvider)
    public Long id() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof UsuarioAutenticado logado)) {
            // nao deveria acontecer: o SecurityConfig ja barra quem nao esta logado antes de chegar aqui
            throw new IllegalStateException("nenhum usuario logado na requisicao");
        }
        return logado.getId();
    }

    // decide de qual usuario sao os dados pedidos, aplicando a regra descrita acima
    public Long resolverUsuario(Long usuarioIdInformado) {
        Long idLogado = id();
        if (usuarioIdInformado == null) {
            return idLogado;
        }
        garantirQueEDoLogado(usuarioIdInformado);
        return idLogado;
    }

    // lanca 403 se o dono dos dados nao for quem esta logado
    public void garantirQueEDoLogado(Long usuarioIdDoDono) {
        if (!id().equals(usuarioIdDoDono)) {
            throw new AcessoNegadoException("voce so pode acessar os seus proprios dados");
        }
    }
}
