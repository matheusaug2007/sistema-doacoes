# Banco de Dados

Este diretório armazena os scripts SQL do banco de dados do projeto.

Os scripts ficam em `migration/` e são aplicados automaticamente pelo Flyway quando a aplicação inicia, em ordem de versão:

| Script | Conteúdo |
|---|---|
| `V1__esquema_inicial.sql` | Criação das tabelas, chaves primárias e estrangeiras, restrições e índices |
| `V2__instituicao_padrao.sql` | Instituição inicial, usada enquanto o sistema não tem login |

Um script já aplicado não deve ser alterado. Para mudar o banco, crie um novo arquivo com a próxima versão (`V3__descricao.sql`).

O modelo de dados está explicado em [docs/arquitetura.md](../docs/arquitetura.md).
