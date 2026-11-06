package br.com.gabrieloliveira.usuarios.adapter.in.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

// junta as mensagens de erro de validacao por campo, para o "campos" da resposta 400
// por que nao um Map simples? porque um campo pode ter dois erros ao mesmo tempo
// (ex: valor negativo E com 3 casas decimais) e o put de um Map apagaria a primeira mensagem
// aqui as mensagens do mesmo campo ficam juntas, separadas por "; "
// TreeMap e TreeSet deixam campos e mensagens em ordem alfabetica: a resposta sai sempre igual
// (o bean validation nao garante a ordem em que os erros chegam)
class ErrosPorCampo {

    private final Map<String, Set<String>> mensagensPorCampo = new TreeMap<>();

    void adicionar(String campo, String mensagem) {
        // computeIfAbsent cria a lista do campo na primeira vez e depois so reaproveita
        mensagensPorCampo.computeIfAbsent(campo, nome -> new TreeSet<>()).add(mensagem);
    }

    Map<String, String> paraResposta() {
        Map<String, String> campos = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> campo : mensagensPorCampo.entrySet()) {
            campos.put(campo.getKey(), String.join("; ", campo.getValue()));
        }
        return campos;
    }
}
