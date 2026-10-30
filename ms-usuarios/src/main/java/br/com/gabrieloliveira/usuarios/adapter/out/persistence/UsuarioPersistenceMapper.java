package br.com.gabrieloliveira.usuarios.adapter.out.persistence;

import br.com.gabrieloliveira.usuarios.domain.model.Usuario;
import org.springframework.stereotype.Component;

// tradutor entre o Usuario do dominio e o UsuarioEntity do banco
// os dois tem os mesmos campos, mas um e java puro e o outro tem as anotacoes do jpa
@Component
public class UsuarioPersistenceMapper {

    public UsuarioEntity paraEntity(Usuario usuario) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(usuario.getId());
        entity.setNome(usuario.getNome());
        entity.setEmail(usuario.getEmail());
        entity.setCpf(usuario.getCpf());
        entity.setSenha(usuario.getSenha());
        entity.setDataCriacao(usuario.getDataCriacao());
        return entity;
    }

    public Usuario paraDominio(UsuarioEntity entity) {
        return new Usuario(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getCpf(),
                entity.getSenha(),
                entity.getDataCriacao()
        );
    }
}
