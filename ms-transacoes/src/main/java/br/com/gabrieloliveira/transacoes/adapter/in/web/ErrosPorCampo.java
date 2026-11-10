package br.com.gabrieloliveira.transacoes.adapter.in.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

// guarda as mensagens de erro de validacao de cada campo, para montar o "campos" da resposta 400
// por que nao usar um Map simples? porque um campo pode ter dois erros ao mesmo tempo (um valor negativo
// e com 3 casas decimais, por exemplo), e o put do Map apagaria a primeira mensagem. aqui elas ficam juntas,
// separadas por "; "
// o bean validation nao garante a ordem em que os erros chegam, entao o TreeMap e o TreeSet colocam
// campos e mensagens em ordem alfabetica: a resposta sai sempre igual
class ErrosPorCampo {

    private final Map<String, Set<String>> mensagensPorCampo = new TreeMap<>();

    void adicionar(String campo, String mensagem) {
        // na primeira mensagem do campo o computeIfAbsent cria a lista dele; nas proximas, so reaproveita
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
