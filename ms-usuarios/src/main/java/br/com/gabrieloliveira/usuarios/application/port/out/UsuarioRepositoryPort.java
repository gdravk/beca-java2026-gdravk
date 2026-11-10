package br.com.gabrieloliveira.usuarios.application.port.out;

import br.com.gabrieloliveira.usuarios.domain.model.Pagina;
import br.com.gabrieloliveira.usuarios.domain.model.Usuario;

import java.util.Optional;

// porta de saida: o que o centro do sistema PRECISA do banco, sem saber qual banco e
// quem implementa e o UsuarioPersistenceAdapter (com jpa e postgres); os casos de uso so enxergam esta interface
public interface UsuarioRepositoryPort {

    // serve tanto para criar (id null) quanto para atualizar (id preenchido)
    Usuario salvar(Usuario usuario);

    // o Optional e uma "caixinha" que pode vir vazia, quando o usuario nao existe
    Optional<Usuario> buscarPorId(Long id);

    // usado no login, que e feito pelo email
    // o email ja precisa chegar normalizado (sem espacos e em minusculo), do mesmo jeito que e guardado no banco
    Optional<Usuario> buscarPorEmail(String email);

    Pagina<Usuario> listar(int numeroPagina, int tamanhoPagina);

    void excluirPorId(Long id);

    boolean existePorEmail(String email);

    boolean existePorCpf(String cpf);

    // usados na atualizacao: "existe OUTRO usuario (com id diferente deste) usando esse email ou cpf?"
    boolean existePorEmailEmOutroUsuario(String email, Long idDoUsuario);

    boolean existePorCpfEmOutroUsuario(String cpf, Long idDoUsuario);
}
