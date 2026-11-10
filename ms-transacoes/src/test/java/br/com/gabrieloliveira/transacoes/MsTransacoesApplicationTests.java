package br.com.gabrieloliveira.transacoes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

// sobe a aplicacao inteira (banco, api mock, brasilapi...) so para ver se tudo se conecta
// precisa das variaveis de ambiente do banco; sem elas o teste e ignorado em vez de falhar, e assim
// os testes unitarios rodam em qualquer maquina, mesmo sem docker (igual no ms1)
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "TRANSACTIONS_DB_NAME", matches = ".+")
class MsTransacoesApplicationTests {

	@Test
	void contextLoads() {
	}

}
