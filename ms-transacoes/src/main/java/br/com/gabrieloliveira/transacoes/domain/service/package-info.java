// regras de negocio puras do ms2:
// - ValidadorDeTransacao: decide se uma transacao e aprovada ou rejeitada (valor maior que zero, saldo
//   suficiente, limite por transacao e limite diario)
// - AnalisadorDeDespesas: define o que e despesa e agrupa por categoria, dia e mes
// elas recebem e devolvem objetos do dominio, sem acessar banco nem api
package br.com.gabrieloliveira.transacoes.domain.service;
