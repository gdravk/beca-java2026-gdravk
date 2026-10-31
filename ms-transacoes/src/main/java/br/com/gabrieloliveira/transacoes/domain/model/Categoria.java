package br.com.gabrieloliveira.transacoes.domain.model;

// categorias de despesa, usadas na analise de despesas
// lista fixa definida pelo time; compra exige categoria, os outros tipos usam OUTROS quando nao informam
public enum Categoria {

    ALIMENTACAO,
    TRANSPORTE,
    LAZER,
    SAUDE,
    EDUCACAO,
    MORADIA,
    OUTROS
}
