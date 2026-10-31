package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// corpo do POST /transactions
// aqui fica a validacao de formato (bean validation, responde 400)
// regras de negocio (saldo, limites, categoria da compra, destino da transferencia) ficam no dominio
// e resultam em transacao REJECTED com motivo, nao em erro 400
// usuarioId vem no corpo por enquanto; com a seguranca (pedido 6) passa a vir do usuario autenticado
public class TransacaoRequestDTO {

    @NotNull(message = "o usuarioId e obrigatorio")
    @Positive(message = "o usuarioId deve ser positivo")
    private Long usuarioId;

    @NotNull(message = "o tipo e obrigatorio (DEPOSITO, RETIRADA, TRANSFERENCIA ou COMPRA)")
    private TipoTransacao tipo;

    @NotNull(message = "o valor e obrigatorio")
    @Positive(message = "o valor deve ser maior que zero")
    @Digits(integer = 13, fraction = 2, message = "o valor deve ter no maximo 2 casas decimais")
    private BigDecimal valor;

    // obrigatoria so para COMPRA (regra do dominio)
    private Categoria categoria;

    @NotBlank(message = "a descricao e obrigatoria")
    @Size(max = 255, message = "a descricao pode ter no maximo 255 caracteres")
    private String descricao;

    // obrigatorio so para TRANSFERENCIA (regra do dominio)
    @Positive(message = "o usuarioDestinoId deve ser positivo")
    private Long usuarioDestinoId;

    public TransacaoRequestDTO() {
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransacao tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Long getUsuarioDestinoId() {
        return usuarioDestinoId;
    }

    public void setUsuarioDestinoId(Long usuarioDestinoId) {
        this.usuarioDestinoId = usuarioDestinoId;
    }
}
