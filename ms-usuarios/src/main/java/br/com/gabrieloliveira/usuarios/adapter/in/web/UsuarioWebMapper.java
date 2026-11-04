package br.com.gabrieloliveira.usuarios.adapter.in.web;

import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.PaginaResponseDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.UsuarioRequestDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.UsuarioResponseDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.UsuarioUpdateRequestDTO;
import br.com.gabrieloliveira.usuarios.domain.model.Pagina;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// tradutor entre o mundo http (dtos) e o centro do sistema (Usuario do dominio)
// os casos de uso nunca recebem nem devolvem dto, so objetos do dominio
@Component
public class UsuarioWebMapper {

    // json que chegou -> Usuario do dominio
    // id e data de criacao ficam null, quem decide eles e o caso de uso e o banco
    public Usuario paraDominio(UsuarioRequestDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setCpf(dto.getCpf());
        usuario.setSenha(dto.getSenha());
        return usuario;
    }

    // json do update -> Usuario do dominio
    // igual ao de cima, mas aqui a senha pode chegar null (quando o cliente nao quer trocar)
    // o AtualizarUsuarioService entende o null como "mantem a senha atual"
    // sao dois metodos com o mesmo nome e parametros diferentes: o java escolhe qual usar pelo tipo do dto
    // (isso se chama sobrecarga de metodo)
    public Usuario paraDominio(UsuarioUpdateRequestDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setCpf(dto.getCpf());
        usuario.setSenha(dto.getSenha());
        return usuario;
    }

    // Usuario do dominio -> json de resposta (aqui a senha fica de fora)
    public UsuarioResponseDTO paraResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCpf(),
                usuario.getDataCriacao()
        );
    }

    // converte a pagina inteira, usuario por usuario
    public PaginaResponseDTO<UsuarioResponseDTO> paraPaginaResponse(Pagina<Usuario> pagina) {
        List<UsuarioResponseDTO> conteudo = new ArrayList<>();
        for (Usuario usuario : pagina.getConteudo()) {
            conteudo.add(paraResponse(usuario));
        }

        return new PaginaResponseDTO<>(
                conteudo,
                pagina.getNumeroPagina(),
                pagina.getTamanhoPagina(),
                pagina.getTotalElementos(),
                pagina.getTotalPaginas()
        );
    }
}
