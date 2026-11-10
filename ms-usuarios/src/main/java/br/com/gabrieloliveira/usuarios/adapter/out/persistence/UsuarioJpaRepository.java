package br.com.gabrieloliveira.usuarios.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// o repositorio do spring data, que conversa direto com o banco
// so de estender o JpaRepository a gente ja ganha pronto: save, findById, findAll, deleteById...
// <UsuarioEntity, Long> quer dizer: a entidade que ele cuida e o tipo do id dela
// os casos de uso nao usam ele direto; quem usa e o UsuarioPersistenceAdapter
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    // o spring le o nome do metodo e monta o sql sozinho
    // existsByEmail vira algo como: select count(*) > 0 from usuarios where email = ?
    boolean existsByEmail(String email);

    // findByEmail vira: select * from usuarios where email = ?
    // devolve Optional porque pode nao existir ninguem com esse email
    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByCpf(String cpf);

    // "AndIdNot" = e com o id diferente do informado (ou seja, ignora o proprio usuario)
    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByCpfAndIdNot(String cpf, Long id);
}
