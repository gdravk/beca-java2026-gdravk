# Layout do CSV de importação de usuários (Pedido 4)

Arquivo de exemplo: [`usuarios-exemplo.csv`](usuarios-exemplo.csv), enviado em `POST /users/import` (multipart/form-data, campo `file`).

A rota exige login (autenticação básica com e-mail e senha de um usuário já cadastrado). No Swagger, use o botão **Authorize** antes de enviar; no Postman, a aba **Authorization → Basic Auth**. Limites: até 1000 usuários e 2 MB por arquivo.

## Formato

- **Separador:** ponto e vírgula (`;`). O Excel em português usa esse separador, e assim um nome ou endereço com vírgula não quebra a coluna.
- **Codificação:** UTF-8 (com ou sem BOM), para nomes acentuados como "João" e "Vinícius".
- **Primeira linha:** cabeçalho obrigatório, com as colunas nesta ordem:

| Coluna | Regra (a mesma do cadastro `POST /users`) |
|---|---|
| `nome` | obrigatório, máximo de 100 caracteres |
| `email` | obrigatório, formato válido, máximo de 150 caracteres, único (não pode existir no banco nem se repetir no arquivo) |
| `cpf` | obrigatório, exatamente 11 números (sem ponto e sem traço), único |
| `senha` | obrigatória, de 6 a 72 caracteres (limite do BCrypt: 72 bytes, letra com acento conta como 2; gravada criptografada) |

As senhas do exemplo são fictícias (`Senha@123`).

## Linhas inválidas de propósito

Uma linha inválida não interrompe a importação: as demais são cadastradas e o resumo informa a linha e o motivo da falha. A numeração conta o cabeçalho como linha 1.

| Linha | Problema |
|---|---|
| 6 | e-mail em formato inválido (`eduarda.rocha-exemplo.com`) |
| 12 | e-mail repetido (o mesmo da linha 2) |
| 17 | nome vazio |
| 21 | CPF com ponto e traço (`123.456.789-00`) |
| 24 | senha com menos de 6 caracteres |

Resultado esperado com o banco sem esses usuários: **25 linhas lidas, 20 importadas e 5 rejeitadas**.
