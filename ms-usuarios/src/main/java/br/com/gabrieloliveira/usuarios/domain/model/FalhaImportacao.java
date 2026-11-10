package br.com.gabrieloliveira.usuarios.domain.model;

// uma linha do arquivo que nao entrou, com o motivo
// (por exemplo: linha 12, "ja existe um usuario cadastrado com esse email")
public class FalhaImportacao {

    private final int linha;
    private final String motivo;

    public FalhaImportacao(int linha, String motivo) {
        this.linha = linha;
        this.motivo = motivo;
    }

    public int getLinha() {
        return linha;
    }

    public String getMotivo() {
        return motivo;
    }
}
