package br.com.gabrieloliveira.usuarios.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

// como fica o json da listagem paginada:
// { "conteudo": [ ...usuarios... ], "pagina": 0, "tamanho": 10, "totalElementos": 25, "totalPaginas": 3 }
@Schema(description = "uma pagina da listagem")
public class PaginaResponseDTO<T> {

    @Schema(description = "os itens desta pagina")
    private List<T> conteudo;
    @Schema(description = "numero desta pagina, comecando em 0", example = "0")
    private int pagina;
    @Schema(description = "quantos itens cabem por pagina", example = "10")
    private int tamanho;
    @Schema(description = "total de itens em todas as paginas", example = "25")
    private long totalElementos;
    @Schema(description = "quantas paginas existem", example = "3")
    private int totalPaginas;

    public PaginaResponseDTO() {
    }

    public PaginaResponseDTO(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {
        this.conteudo = conteudo;
        this.pagina = pagina;
        this.tamanho = tamanho;
        this.totalElementos = totalElementos;
        this.totalPaginas = totalPaginas;
    }

    public List<T> getConteudo() {
        return conteudo;
    }

    public void setConteudo(List<T> conteudo) {
        this.conteudo = conteudo;
    }

    public int getPagina() {
        return pagina;
    }

    public void setPagina(int pagina) {
        this.pagina = pagina;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void setTamanho(int tamanho) {
        this.tamanho = tamanho;
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
