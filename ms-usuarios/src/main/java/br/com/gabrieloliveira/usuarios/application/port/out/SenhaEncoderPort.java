package br.com.gabrieloliveira.usuarios.application.port.out;

// porta de saida pra transformar a senha em hash
// o caso de uso so sabe que "alguem" faz isso; hoje quem faz e o bcrypt do spring security
// se um dia trocar o bcrypt por outro algoritmo, so o adaptador muda
public interface SenhaEncoderPort {

    String codificar(String senhaPura);
}
