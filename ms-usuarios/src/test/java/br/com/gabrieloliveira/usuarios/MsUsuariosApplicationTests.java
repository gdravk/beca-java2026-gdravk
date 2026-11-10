package br.com.gabrieloliveira.usuarios;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

// teste que ja veio pronto do spring initializr: sobe a aplicacao inteira e confere se ela liga sem erro
// diferente dos testes unitarios, ele PRECISA do banco users-db ligado e das variaveis de ambiente
// do banco (USERS_DB_NAME, USERS_DB_USER, USERS_DB_PASSWORD)
// o @EnabledIfEnvironmentVariable faz ele so rodar quando essas variaveis existem; sem elas (rodando os
// testes sem o docker, por exemplo), ele aparece como "pulado" em vez de falhar
// assim os testes unitarios continuam rodando em qualquer maquina, mesmo sem banco
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "USERS_DB_NAME", matches = ".+")
class MsUsuariosApplicationTests {

	@Test
	void contextLoads() {
	}

}
