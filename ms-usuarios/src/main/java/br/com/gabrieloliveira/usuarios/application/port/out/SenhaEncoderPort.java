package br.com.gabrieloliveira.usuarios.application.port.out;

// porta de saida pra transformar a senha em hash
// o caso de uso so sabe que "alguem" faz isso; hoje quem faz e o bcrypt do spring security
// se um dia trocar o bcrypt por outro algoritmo, so o adaptador muda
public interface SenhaEncoderPort {

    String codificar(String senhaPura);

    // confere se a senha digitada corresponde ao hash guardado no banco (usado na seguranca, pedido 6)
    // o hash nao tem volta, entao a conferencia e feita gerando o hash da senha digitada do mesmo jeito
    // e comparando os dois
    boolean confere(String senhaPura, String hashGuardado);
}
