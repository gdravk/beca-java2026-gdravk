package br.com.gabrieloliveira.usuarios.repository;

import br.com.gabrieloliveira.usuarios.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// so de estender o JpaRepository a gente ja ganha pronto: save, findById, findAll, deleteById, existsById...
// <Usuario, Long> = a entidade que ele cuida e o tipo do id dela
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // o spring le o nome do metodo e monta o sql sozinho
    // existsByEmail vira algo como: select count(*) > 0 from usuarios where email = ?
    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    // usados no update: "existe outro usuario (com id diferente do meu) usando esse email/cpf?"
    // sem isso, o proprio usuario seria barrado ao salvar o email que ja e dele
    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByCpfAndIdNot(String cpf, Long id);
}
