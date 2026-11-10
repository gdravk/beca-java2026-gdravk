package br.com.gabrieloliveira.usuarios.adapter.in.web.csv;

// erro no arquivo inteiro, e nao numa linha so: nao veio arquivo, esta vazio, nao e .csv, nao esta
// em utf-8, o cabecalho esta errado...
// nesses casos nem da para comecar a importacao, entao a api responde 400 (ver GlobalExceptionHandler)
// os erros de uma linha so (email errado, campo vazio) nao usam esta excecao: eles viram falha no resumo
public class ArquivoCsvInvalidoException extends RuntimeException {

    public ArquivoCsvInvalidoException(String mensagem) {
        super(mensagem);
    }
}
