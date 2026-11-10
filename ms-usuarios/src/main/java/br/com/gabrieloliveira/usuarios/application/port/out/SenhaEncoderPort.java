package br.com.gabrieloliveira.usuarios.application.port.out;

// porta de saida para transformar a senha em hash
// o caso de uso so sabe que "alguem" faz isso; hoje quem faz e o bcrypt do spring security,
// e se um dia ele for trocado por outro algoritmo, so o adaptador muda
public interface SenhaEncoderPort {

    String codificar(String senhaPura);

    // confere se a senha digitada corresponde ao hash guardado no banco (usado no login, pedido 6)
    // como o hash nao tem volta, a conferencia gera o hash da senha digitada do mesmo jeito e compara os dois
    boolean confere(String senhaPura, String hashGuardado);
}
