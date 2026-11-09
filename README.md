Desafio Beca Java 2026 - Gabriel Oliveira Silva / gdravk

## Testes e cobertura

Os testes usam JUnit 5 e Mockito (as portas de saída são simuladas, então nenhum teste chama banco, API Mock ou BrasilAPI de verdade). A cobertura é medida pelo JaCoCo, que roda junto com o build.

Para rodar os testes e gerar o relatório, dentro da pasta de cada microsserviço:

```bash
# windows
mvnw.cmd clean verify
# linux / mac / git bash
./mvnw clean verify
```

O relatório fica em `target/site/jacoco/index.html` (abra no navegador).

O build também confere a cobertura: se o domínio ou os casos de uso ficarem com menos de 80% das linhas testadas, o `verify` falha e mostra qual pacote ficou abaixo.

Cobertura de linhas alcançada:

| Microsserviço | Domínio | Casos de uso | Total | Testes |
|---|---|---|---|---|
| ms-usuarios | 87,0% | 100% | 66,6% | 83 |
| ms-transacoes | 92,2% | 100% | 62,0% | 124 |

A tabela acima conta linhas. Os relatórios abaixo mostram também instruções (coluna "Cov." da esquerda) e ramos (`if`/`else`, coluna "Cov." da direita); por isso os totais aparecem um pouco diferentes.

**ms-usuarios**

![Cobertura do ms-usuarios](docs/cobertura-ms-usuarios.png)

**ms-transacoes**

![Cobertura do ms-transacoes](docs/cobertura-ms-transacoes.png)

Regras críticas cobertas:

- **ms-transacoes:** aprovação e rejeição de transação (valor zero ou negativo, compra sem categoria, transferência para si mesmo, saldo insuficiente, limite por transação, limite diário), API Mock fora do ar (a saída é rejeitada, nunca aprovada às cegas), cotações do dólar e do euro, análise de despesas (ignora rejeitadas, depósitos e estornos), câmbio que volta para o último dia com cotação (fim de semana e feriado), BrasilAPI fora do ar ou lenta, validação das entradas e segurança.
- **ms-usuarios:** cadastro (e-mail duplicado, senha criptografada), busca de usuário inexistente (404), atualização com senha opcional, importação de CSV (linhas inválidas com motivo, arquivo vazio), validação das entradas e segurança.
