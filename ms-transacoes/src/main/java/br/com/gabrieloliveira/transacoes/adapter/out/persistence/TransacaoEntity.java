package br.com.gabrieloliveira.transacoes.adapter.out.persistence;

import br.com.gabrieloliveira.transacoes.domain.model.Categoria;
import br.com.gabrieloliveira.transacoes.domain.model.StatusTransacao;
import br.com.gabrieloliveira.transacoes.domain.model.TipoTransacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// tabela "transacoes" do banco transactions-db
// os enums sao gravados pelo nome (EnumType.STRING), assim a ordem deles no codigo pode mudar sem estragar os dados
// nenhuma coluna e "updatable = false" por exigencia do jpa, mas o sistema nunca altera uma transacao depois de gravada
@Entity
@Table(name = "transacoes")
public class TransacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTransacao tipo;

    // valor em reais, com ate 13 digitos inteiros e 2 decimais
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Categoria categoria;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(name = "usuario_destino_id")
    private Long usuarioDestinoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusTransacao status;

    @Column(name = "motivo_rejeicao", length = 255)
    private String motivoRejeicao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "transacao_original_id")
    private Long transacaoOriginalId;

    @Column(name = "taxa_dolar", precision = 12, scale = 4)
    private BigDecimal taxaDolar;

    @Column(name = "data_taxa_dolar")
    private LocalDate dataTaxaDolar;

    @Column(name = "taxa_euro", precision = 12, scale = 4)
    private BigDecimal taxaEuro;

    @Column(name = "data_taxa_euro")
    private LocalDate dataTaxaEuro;

    public TransacaoEntity() {
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
