package br.com.gabrieloliveira.usuarios.domain.model;

import java.util.List;

// um "pedaco" de uma lista grande: os itens da pagina atual e as informacoes pra navegar nas outras
// criamos a nossa propria classe em vez de usar o Page do spring pra o centro do sistema nao depender do spring
// o <T> quer dizer que ela serve pra qualquer tipo de item (Pagina<Usuario>, Pagina<UsuarioResponseDTO>...)
public class Pagina<T> {

    private List<T> conteudo;
    // a contagem comeca em 0: a primeira pagina e a 0
    private int numeroPagina;
    private int tamanhoPagina;
    private long totalElementos;
    private int totalPaginas;

    public Pagina() {
    }

    public Pagina(List<T> conteudo, int numeroPagina, int tamanhoPagina, long totalElementos, int totalPaginas) {
        this.conteudo = conteudo;
        this.numeroPagina = numeroPagina;
        this.tamanhoPagina = tamanhoPagina;
        this.totalElementos = totalElementos;
        this.totalPaginas = totalPaginas;
    }

    public List<T> getConteudo() {
        return conteudo;
    }

    public void setConteudo(List<T> conteudo) {
        this.conteudo = conteudo;
    }

    public int getNumeroPagina() {
        return numeroPagina;
    }

    public void setNumeroPagina(int numeroPagina) {
        this.numeroPagina = numeroPagina;
    }

    public int getTamanhoPagina() {
        return tamanhoPagina;
    }

    public void setTamanhoPagina(int tamanhoPagina) {
        this.tamanhoPagina = tamanhoPagina;
    }

    public long getTotalElementos() {
        return totalElementos;
    }

    public void setTotalElementos(long totalElementos) {
        this.totalElementos = totalElementos;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }
}
