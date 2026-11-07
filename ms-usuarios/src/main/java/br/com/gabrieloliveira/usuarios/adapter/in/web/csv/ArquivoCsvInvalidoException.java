package br.com.gabrieloliveira.usuarios.adapter.in.web.csv;

// erro do arquivo inteiro (e nao de uma linha): nao veio arquivo, esta vazio, nao e .csv,
// nao esta em utf-8, cabecalho errado...
// nesses casos nao da nem pra comecar a importacao, entao a api responde 400 (ver GlobalExceptionHandler)
// os erros de uma linha so (email errado, campo vazio) nao usam essa excecao: viram falha no resumo
public class ArquivoCsvInvalidoException extends RuntimeException {

    public ArquivoCsvInvalidoException(String mensagem) {
        super(mensagem);
    }
}
