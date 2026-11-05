package br.com.gabrieloliveira.usuarios.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// o resumo da importacao que o PO pediu: quantas linhas vieram, quantas entraram e quais falharam (com o motivo)
// o caso de uso vai preenchendo esse objeto linha por linha, sem parar no primeiro erro
public class ResultadoImportacao {

    private int totalLinhas;
    private int importados;
    private final List<FalhaImportacao> falhas = new ArrayList<>();

    public void registrarImportado() {
        totalLinhas++;
        importados++;
    }

    public void registrarFalha(int linha, String motivo) {
        totalLinhas++;
        falhas.add(new FalhaImportacao(linha, motivo));
    }

    public int getTotalLinhas() {
        return totalLinhas;
    }

    public int getImportados() {
        return importados;
    }

    // rejeitados nao precisa de um contador proprio: e o tamanho da lista de falhas
    public int getRejeitados() {
        return falhas.size();
    }

    // devolve a lista "so de leitura": quem recebe o resultado consegue ler as falhas, mas nao alterar
    public List<FalhaImportacao> getFalhas() {
        return Collections.unmodifiableList(falhas);
    }
}
