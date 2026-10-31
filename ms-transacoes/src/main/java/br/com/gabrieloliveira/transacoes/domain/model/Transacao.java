package br.com.gabrieloliveira.transacoes.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// transacao financeira de um usuario, em java puro
// o valor e sempre em reais; dolar e euro sao so conversoes, calculadas com as taxas guardadas aqui
// uma transacao nunca e apagada nem alterada depois de gravada: a correcao e feita por estorno,
// que e uma nova transacao apontando para a original (transacaoOriginalId)
public class Transacao {

    private Long id;
    private Long usuarioId;
    private TipoTransacao tipo;
    private BigDecimal valor;
    private Categoria categoria;
    private String descricao;
    // so preenchido em transferencia
    private Long usuarioDestinoId;
    private StatusTransacao status;
    // so preenchido quando o status e REJECTED
    private String motivoRejeicao;
    private LocalDateTime dataHora;
    // so preenchido quando esta transacao e o estorno de outra
    private Long transacaoOriginalId;

    // taxas de cambio do momento em que a transacao foi criada
    // ficam null quando o servico de cambio nao respondeu
    private BigDecimal taxaDolar;
    private LocalDate dataTaxaDolar;
    private BigDecimal taxaEuro;
    private LocalDate dataTaxaEuro;

    public Transacao() {
    }

    public void aprovar() {
        this.status = StatusTransacao.APPROVED;
        this.motivoRejeicao = null;
    }

    public void rejeitar(String motivo) {
        this.status = StatusTransacao.REJECTED;
        this.motivoRejeicao = motivo;
    }

    public boolean isAprovada() {
        return status == StatusTransacao.APPROVED;
    }

    public boolean isEstorno() {
        return transacaoOriginalId != null;
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

    public BigDecimal getTaxaDolar() {
        return taxaDolar;
    }

    public void setTaxaDolar(BigDecimal taxaDolar) {
        this.taxaDolar = taxaDolar;
    }

    public LocalDate getDataTaxaDolar() {
        return dataTaxaDolar;
    }

    public void setDataTaxaDolar(LocalDate dataTaxaDolar) {
        this.dataTaxaDolar = dataTaxaDolar;
    }

    public BigDecimal getTaxaEuro() {
        return taxaEuro;
    }

    public void setTaxaEuro(BigDecimal taxaEuro) {
        this.taxaEuro = taxaEuro;
    }

    public LocalDate getDataTaxaEuro() {
        return dataTaxaEuro;
    }

    public void setDataTaxaEuro(LocalDate dataTaxaEuro) {
        this.dataTaxaEuro = dataTaxaEuro;
    }
}
