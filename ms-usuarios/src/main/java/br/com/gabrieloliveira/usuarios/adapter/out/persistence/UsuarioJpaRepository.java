package br.com.gabrieloliveira.usuarios.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

// o repositorio do spring data, que fala direto com o banco
// so de estender o JpaRepository a gente ja ganha pronto: save, findById, findAll, deleteById...
// <UsuarioEntity, Long> = a entidade que ele cuida e o tipo do id dela
// ele nao e usado pelos casos de uso direto: quem usa ele e o UsuarioPersistenceAdapter
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    // o spring le o nome do metodo e monta o sql sozinho
    // existsByEmail vira algo como: select count(*) > 0 from usuarios where email = ?
    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    // "AndIdNot" = e o id e diferente do informado (ignora o proprio usuario)
    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByCpfAndIdNot(String cpf, Long id);
}
