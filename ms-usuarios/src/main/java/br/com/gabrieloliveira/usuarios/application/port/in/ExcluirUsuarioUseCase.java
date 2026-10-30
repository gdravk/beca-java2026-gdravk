package br.com.gabrieloliveira.usuarios.application.port.in;

// porta de entrada pra excluir um usuario
public interface ExcluirUsuarioUseCase {

    void excluir(Long id);
}
