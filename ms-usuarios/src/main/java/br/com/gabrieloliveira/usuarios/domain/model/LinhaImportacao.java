package br.com.gabrieloliveira.usuarios.domain.model;

// uma linha do arquivo de importacao, ja lida e conferida; ela termina de um destes dois jeitos:
// - valida: tem um Usuario pronto para ser cadastrado
// - invalida: nao deu para montar o usuario (campo vazio, email errado...) e o motivo fica em "erro"
// o numero da linha e guardado para o resumo dizer exatamente onde esta o problema no arquivo
// (a contagem e a mesma do editor de texto: o cabecalho e a linha 1)
public class LinhaImportacao {

    private final int numeroLinha;
    private final Usuario usuario;
    private final String erro;

    // construtor privado: para criar uma linha, use os metodos "valida" ou "invalida" abaixo
    // assim nunca existe uma linha com usuario e erro ao mesmo tempo
    private LinhaImportacao(int numeroLinha, Usuario usuario, String erro) {
        this.numeroLinha = numeroLinha;
        this.usuario = usuario;
        this.erro = erro;
    }

    public static LinhaImportacao valida(int numeroLinha, Usuario usuario) {
        return new LinhaImportacao(numeroLinha, usuario, null);
    }

    public static LinhaImportacao invalida(int numeroLinha, String erro) {
        return new LinhaImportacao(numeroLinha, null, erro);
    }

    public boolean isValida() {
        return erro == null;
    }

    public int getNumeroLinha() {
        return numeroLinha;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getErro() {
        return erro;
    }
}
