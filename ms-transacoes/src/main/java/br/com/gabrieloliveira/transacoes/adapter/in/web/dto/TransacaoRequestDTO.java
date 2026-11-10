package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// corpo do POST /transactions
// aqui fica so a validacao de formato (bean validation, que responde 400); as regras de negocio (saldo,
// limites, categoria da compra, destino da transferencia) ficam no dominio e terminam numa transacao
// REJECTED com o motivo, e nao num erro 400
// o dono da transacao e quem esta logado (seguranca, pedido 6), por isso o usuarioId do corpo e opcional
@Schema(description = "dados para registrar uma transacao")
public class TransacaoRequestDTO {

    // opcional desde a seguranca (pedido 6): sem ele, a transacao fica no nome de quem esta logado
    // se vier, tem que ser o id do proprio usuario logado (outro id responde 403)
    @Positive(message = "o usuarioId deve ser positivo")
    @Schema(description = "opcional: sem ele, a transacao e do usuario logado; se vier, tem que ser o id dele", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long usuarioId;

    @NotNull(message = "o tipo e obrigatorio (DEPOSITO, RETIRADA, TRANSFERENCIA ou COMPRA)")
    @Schema(description = "DEPOSITO entra dinheiro; RETIRADA, TRANSFERENCIA e COMPRA sao saidas e conferem saldo e limites", example = "COMPRA")
    private TipoTransacao tipo;

    @NotNull(message = "o valor e obrigatorio")
    @Positive(message = "o valor deve ser maior que zero")
    @Digits(integer = 13, fraction = 2, message = "o valor deve ter no maximo 2 casas decimais")
    @Schema(description = "valor em reais, maior que zero e com no maximo 2 casas decimais", example = "150.75")
    private BigDecimal valor;

    // obrigatoria so para COMPRA (regra que fica no dominio)
    @Schema(description = "obrigatoria so para COMPRA; nos outros tipos, sem ela vira OUTROS", example = "ALIMENTACAO", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Categoria categoria;

    @NotBlank(message = "a descricao e obrigatoria")
    @Size(max = 255, message = "a descricao pode ter no maximo 255 caracteres")
    @Schema(description = "descricao livre", example = "mercado do mes")
    private String descricao;

    // obrigatorio so para TRANSFERENCIA (regra que fica no dominio)
    @Positive(message = "o usuarioDestinoId deve ser positivo")
    @Schema(description = "obrigatorio so para TRANSFERENCIA: o id de quem recebe (nao pode ser o proprio usuario)", example = "2", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
