package br.com.gabrieloliveira.transacoes.adapter.in.web.dto;

import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.StatusTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// resposta de uma transacao: valor em reais + conversoes em dolar e euro
// quando alguma cotacao nao estiver disponivel, conversaoDisponivel = false e mensagemConversao explica
// a ordem dos campos no json e fixada aqui; sem isso o jackson 3 ordena em ordem alfabetica
@JsonPropertyOrder({"id", "usuarioId", "tipo", "valorEmReais", "categoria", "descricao", "usuarioDestinoId",
        "status", "motivoRejeicao", "dataHora", "transacaoOriginalId",
        "conversoes", "conversaoDisponivel", "mensagemConversao"})
@Schema(description = "uma transacao gravada, com o valor em reais e convertido para dolar e euro")
public class TransacaoResponseDTO {

    @Schema(description = "id da transacao", example = "10")
    private Long id;
    @Schema(description = "dono da transacao", example = "1")
    private Long usuarioId;
    @Schema(description = "tipo da transacao", example = "COMPRA")
    private TipoTransacao tipo;
    @Schema(description = "valor em reais", example = "150.75")
    private BigDecimal valorEmReais;
    @Schema(description = "categoria (OUTROS quando nao foi informada)", example = "ALIMENTACAO")
    private Categoria categoria;
    @Schema(description = "descricao livre", example = "mercado do mes")
    private String descricao;
    @Schema(description = "quem recebeu (so em TRANSFERENCIA)", example = "2")
    private Long usuarioDestinoId;
    @Schema(description = "APPROVED (aprovada) ou REJECTED (rejeitada)", example = "APPROVED")
    private StatusTransacao status;
    @Schema(description = "por que foi rejeitada (so quando REJECTED)", example = "saldo insuficiente")
    private String motivoRejeicao;
    @Schema(description = "data e hora do registro (horario de Brasilia)", example = "2026-11-05T14:30:00")
    private LocalDateTime dataHora;
    @Schema(description = "so nos estornos: o id da transacao que foi estornada", example = "7")
    private Long transacaoOriginalId;
    @Schema(description = "o valor convertido para dolar (USD) e euro (EUR), com a cotacao da BrasilAPI")
    private List<ConversaoMoedaDTO> conversoes;
    @Schema(description = "false quando alguma cotacao nao estava disponivel", example = "true")
    private boolean conversaoDisponivel;
    @Schema(description = "explica quais moedas ficaram sem conversao (so quando conversaoDisponivel = false)", example = "conversao indisponivel no momento para: EUR")
    private String mensagemConversao;

    public TransacaoResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getValorEmReais() {
        return valorEmReais;
    }

    public void setValorEmReais(BigDecimal valorEmReais) {
        this.valorEmReais = valorEmReais;
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

    public StatusTransacao getStatus() {
        return status;
    }

    public void setStatus(StatusTransacao status) {
        this.status = status;
    }

    public String getMotivoRejeicao() {
        return motivoRejeicao;
    }

    public void setMotivoRejeicao(String motivoRejeicao) {
        this.motivoRejeicao = motivoRejeicao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Long getTransacaoOriginalId() {
        return transacaoOriginalId;
    }

    public void setTransacaoOriginalId(Long transacaoOriginalId) {
        this.transacaoOriginalId = transacaoOriginalId;
    }

    public List<ConversaoMoedaDTO> getConversoes() {
        return conversoes;
    }

    public void setConversoes(List<ConversaoMoedaDTO> conversoes) {
        this.conversoes = conversoes;
    }

    public boolean isConversaoDisponivel() {
        return conversaoDisponivel;
    }

    public void setConversaoDisponivel(boolean conversaoDisponivel) {
        this.conversaoDisponivel = conversaoDisponivel;
    }

    public String getMensagemConversao() {
        return mensagemConversao;
    }

    public void setMensagemConversao(String mensagemConversao) {
        this.mensagemConversao = mensagemConversao;
    }
}
