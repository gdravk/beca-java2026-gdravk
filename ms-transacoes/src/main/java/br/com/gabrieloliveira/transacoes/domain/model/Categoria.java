package br.com.gabrieloliveira.transacoes.domain.model;

// categorias de despesa, usadas na analise de despesas
// e uma lista fixa definida pelo time; a compra exige categoria, e os outros tipos usam OUTROS quando nao informam
public enum Categoria {

    ALIMENTACAO,
    TRANSPORTE,
    LAZER,
    SAUDE,
    EDUCACAO,
    MORADIA,
    OUTROS
}
