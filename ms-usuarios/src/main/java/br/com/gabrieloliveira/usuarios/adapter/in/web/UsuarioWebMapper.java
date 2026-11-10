package br.com.gabrieloliveira.usuarios.adapter.in.web;

import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.FalhaImportacaoDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.ImportacaoResultadoDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.PaginaResponseDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.UsuarioRequestDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.UsuarioResponseDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.UsuarioResumoResponseDTO;
import br.com.gabrieloliveira.usuarios.adapter.in.web.dto.UsuarioUpdateRequestDTO;
import br.com.gabrieloliveira.usuarios.domain.model.FalhaImportacao;
import br.com.gabrieloliveira.usuarios.domain.model.Pagina;
import br.com.gabrieloliveira.usuarios.domain.model.ResultadoImportacao;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// tradutor entre o mundo http (os dtos) e o centro do sistema (o Usuario do dominio)
// os casos de uso nunca recebem nem devolvem dto, so objetos do dominio
@Component
public class UsuarioWebMapper {

    // json do cadastro -> Usuario do dominio
    // o id e a data de criacao ficam null: quem define os dois e o caso de uso e o banco
    public Usuario paraDominio(UsuarioRequestDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setCpf(dto.getCpf());
        usuario.setSenha(dto.getSenha());
        return usuario;
    }

    // json da atualizacao -> Usuario do dominio
    // igual ao de cima, mas aqui a senha pode chegar null (quando a pessoa nao quer trocar), e o
    // AtualizarUsuarioService entende esse null como "mantem a senha atual"
    // sao dois metodos com o mesmo nome e parametros diferentes; o java escolhe qual usar pelo tipo do dto
    // (isso se chama sobrecarga de metodo)
    public Usuario paraDominio(UsuarioUpdateRequestDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setCpf(dto.getCpf());
        usuario.setSenha(dto.getSenha());
        return usuario;
    }

    // Usuario do dominio -> json de resposta (a senha fica de fora)
    public UsuarioResponseDTO paraResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCpf(),
                usuario.getDataCriacao()
        );
    }

    // resumo da importacao do dominio -> json de resposta do POST /users/import
    public ImportacaoResultadoDTO paraImportacaoResponse(ResultadoImportacao resultado) {
        List<FalhaImportacaoDTO> falhas = new ArrayList<>();
        for (FalhaImportacao falha : resultado.getFalhas()) {
            falhas.add(new FalhaImportacaoDTO(falha.getLinha(), falha.getMotivo()));
        }

        return new ImportacaoResultadoDTO(
                resultado.getTotalLinhas(),
                resultado.getImportados(),
                resultado.getRejeitados(),
                falhas
        );
    }

    // converte a pagina inteira, usuario por usuario
    // na listagem cada usuario vai no formato resumido, sem cpf (ver UsuarioResumoResponseDTO)
    public PaginaResponseDTO<UsuarioResumoResponseDTO> paraPaginaResponse(Pagina<Usuario> pagina) {
        List<UsuarioResumoResponseDTO> conteudo = new ArrayList<>();
        for (Usuario usuario : pagina.getConteudo()) {
            conteudo.add(new UsuarioResumoResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                    usuario.getDataCriacao()));
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
