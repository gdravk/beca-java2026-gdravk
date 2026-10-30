package br.com.gabrieloliveira.usuarios.service;

import br.com.gabrieloliveira.usuarios.dto.UsuarioRequestDTO;
import br.com.gabrieloliveira.usuarios.dto.UsuarioResponseDTO;
import br.com.gabrieloliveira.usuarios.entity.Usuario;
import br.com.gabrieloliveira.usuarios.exception.RecursoNaoEncontradoException;
import br.com.gabrieloliveira.usuarios.exception.RegraNegocioException;
import br.com.gabrieloliveira.usuarios.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// aqui ficam as regras de negocio do usuario
// o controller so recebe a requisicao e repassa pra ca; quem decide o que pode ou nao e o service
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // injecao por construtor: o spring ve que o service precisa dessas duas coisas e entrega prontas
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // @Transactional: tudo dentro do metodo acontece numa transacao so
    // se der erro no meio, o banco desfaz o que ja tinha feito
    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        // primeiro conferimos as regras, so depois mexemos no banco
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RegraNegocioException("ja existe um usuario cadastrado com esse email");
        }
        if (usuarioRepository.existsByCpf(dto.getCpf())) {
            throw new RegraNegocioException("ja existe um usuario cadastrado com esse cpf");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setCpf(dto.getCpf());
        // a senha nunca vai pro banco do jeito que veio, so o hash dela
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));

        Usuario salvo = usuarioRepository.save(usuario);
        return converterParaResponse(salvo);
    }

    // readOnly = true avisa que aqui so tem leitura, o que deixa a consulta um pouco mais leve
    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listar(Pageable pageable) {
        // o findAll com pageable traz so uma "pagina" de usuarios, e nao a tabela inteira de uma vez
        // o map converte cada Usuario da pagina em UsuarioResponseDTO
        Page<Usuario> pagina = usuarioRepository.findAll(pageable);
        return pagina.map(usuario -> converterParaResponse(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = buscarEntidadePorId(id);
        return converterParaResponse(usuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = buscarEntidadePorId(id);

        // aqui usamos o "AndIdNot" pra ignorar o proprio usuario na conferencia
        if (usuarioRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new RegraNegocioException("ja existe outro usuario cadastrado com esse email");
        }
        if (usuarioRepository.existsByCpfAndIdNot(dto.getCpf(), id)) {
            throw new RegraNegocioException("ja existe outro usuario cadastrado com esse cpf");
        }

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setCpf(dto.getCpf());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));

        Usuario atualizado = usuarioRepository.save(usuario);
        return converterParaResponse(atualizado);
    }

    @Transactional
    public void excluir(Long id) {
        // busca antes pra devolver 404 se nao existir, em vez de "excluir" algo que nem estava la
        Usuario usuario = buscarEntidadePorId(id);
        usuarioRepository.delete(usuario);
    }

    // metodo de apoio usado por buscar, atualizar e excluir
    // o findById devolve um Optional (uma "caixinha" que pode estar vazia)
    // se estiver vazia, o orElseThrow lanca nossa excecao, que vira 404
    private Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("usuario com id " + id + " nao encontrado"));
    }

    // converte a entidade (que tem a senha) no dto de resposta (que nao tem)
    private UsuarioResponseDTO converterParaResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCpf(),
                usuario.getDataCriacao()
        );
    }
}
