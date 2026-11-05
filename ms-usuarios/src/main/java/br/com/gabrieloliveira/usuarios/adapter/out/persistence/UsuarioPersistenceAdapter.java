package br.com.gabrieloliveira.usuarios.adapter.out.persistence;

import br.com.gabrieloliveira.usuarios.application.port.out.UsuarioRepositoryPort;
import br.com.gabrieloliveira.usuarios.domain.model.Pagina;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// adaptador de saida: e aqui que a porta UsuarioRepositoryPort "se pluga" no banco de verdade
// ele recebe e devolve Usuario (dominio), e por dentro usa UsuarioEntity + UsuarioJpaRepository
// se um dia trocar o postgres por outro banco, so essa parte (adapter.out.persistence) muda
@Component
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository usuarioJpaRepository;
    private final UsuarioPersistenceMapper usuarioPersistenceMapper;

    public UsuarioPersistenceAdapter(UsuarioJpaRepository usuarioJpaRepository,
                                     UsuarioPersistenceMapper usuarioPersistenceMapper) {
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.usuarioPersistenceMapper = usuarioPersistenceMapper;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioEntity entity = usuarioPersistenceMapper.paraEntity(usuario);
        UsuarioEntity salvo = usuarioJpaRepository.save(entity);
        return usuarioPersistenceMapper.paraDominio(salvo);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        // o map so roda se a caixinha tiver algo dentro; se estiver vazia, continua vazia
        return usuarioJpaRepository.findById(id)
                .map(entity -> usuarioPersistenceMapper.paraDominio(entity));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioJpaRepository.findByEmail(email)
                .map(entity -> usuarioPersistenceMapper.paraDominio(entity));
    }

    @Override
    public Pagina<Usuario> listar(int numeroPagina, int tamanhoPagina) {
        // PageRequest diz pro banco qual pagina queremos, de que tamanho, e ordenado por id
        PageRequest pedidoDePagina = PageRequest.of(numeroPagina, tamanhoPagina, Sort.by("id"));
        Page<UsuarioEntity> paginaDoBanco = usuarioJpaRepository.findAll(pedidoDePagina);

        // converte cada entity da pagina em Usuario do dominio
        List<Usuario> usuarios = new ArrayList<>();
        for (UsuarioEntity entity : paginaDoBanco.getContent()) {
            usuarios.add(usuarioPersistenceMapper.paraDominio(entity));
        }

        // troca o Page do spring pela nossa Pagina, pra o spring nao "vazar" pro centro do sistema
        return new Pagina<>(
                usuarios,
                paginaDoBanco.getNumber(),
                paginaDoBanco.getSize(),
                paginaDoBanco.getTotalElements(),
                paginaDoBanco.getTotalPages()
        );
    }

    @Override
    public void excluirPorId(Long id) {
        usuarioJpaRepository.deleteById(id);
    }

    @Override
    public boolean existePorEmail(String email) {
        return usuarioJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existePorCpf(String cpf) {
        return usuarioJpaRepository.existsByCpf(cpf);
    }

    @Override
    public boolean existePorEmailEmOutroUsuario(String email, Long idDoUsuario) {
        return usuarioJpaRepository.existsByEmailAndIdNot(email, idDoUsuario);
    }

    @Override
    public boolean existePorCpfEmOutroUsuario(String cpf, Long idDoUsuario) {
        return usuarioJpaRepository.existsByCpfAndIdNot(cpf, idDoUsuario);
    }
}
