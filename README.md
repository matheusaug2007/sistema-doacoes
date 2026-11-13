# Sistema de Gestão de Doações de Alimentos

Projeto desenvolvido para a Atividade de Estudo Programada (AEP) do 4º semestre de 2026.

O sistema tem como objetivo auxiliar instituições sociais, igrejas, associações e outras organizações que recebem e distribuem alimentos para pessoas ou famílias em situação de vulnerabilidade.

A solução busca centralizar o controle das doações, permitindo registrar doadores, alimentos recebidos, beneficiários, distribuições e acompanhar o estoque disponível.

## Objetivo de Desenvolvimento Sustentável

O projeto está alinhado principalmente ao **ODS 2 - Fome Zero e Agricultura Sustentável**.

A proposta contribui para esse objetivo ao auxiliar instituições na organização e distribuição de alimentos doados, buscando melhorar o aproveitamento dos produtos recebidos e reduzir desperdícios.

## Partes Interessadas

As principais partes interessadas no projeto são:

- Instituições responsáveis pelo recebimento e distribuição dos alimentos;
- Responsáveis pela administração das doações;
- Doadores;
- Beneficiários.

O responsável pela instituição será o principal usuário do sistema.

## Requisitos Funcionais

### RF01 - Cadastro de Doadores
O sistema deve permitir o cadastro de doadores, contendo informações como nome, telefone, e-mail e tipo de doador.

### RF02 - Cadastro de Alimentos
O sistema deve permitir o cadastro de alimentos, contendo nome, categoria e unidade de medida.

### RF03 - Registro de Doações
O sistema deve permitir registrar uma doação, relacionando o doador aos alimentos doados, suas quantidades, data de validade e data de recebimento.

### RF04 - Cadastro de Beneficiários
O sistema deve permitir o cadastro de pessoas, famílias ou instituições beneficiárias que receberão os alimentos distribuídos.

### RF05 - Registro de Distribuição
O sistema deve permitir registrar a distribuição de alimentos para um beneficiário, informando os itens entregues e suas respectivas quantidades.

### RF06 - Consulta de Estoque
O sistema deve permitir consultar a quantidade disponível dos alimentos recebidos.

### RF07 - Consulta de Histórico
O sistema deve permitir consultar o histórico de doações recebidas e distribuições realizadas.

### RF08 - Atualização de Registros
O sistema deve permitir alterar informações de doadores, alimentos e beneficiários cadastrados.

### RF09 - Exclusão de Registros
O sistema deve permitir excluir registros quando a exclusão não violar as regras de negócio e a integridade dos dados.

## Regras de Negócio

- RN01: Uma doação deve estar vinculada a um doador.
- RN02: Uma doação deve possuir pelo menos um item de alimento registrado.
- RN03: A quantidade de alimentos distribuída não pode ser maior do que a quantidade disponível no lote de origem.
- RN04: Toda distribuição de alimentos deve estar vinculada a um beneficiário.
- RN05: A quantidade disponível deve ser atualizada de acordo com as entradas registradas nas doações e as saídas registradas nas distribuições.
- RN06: Alimentos com data de validade vencida não devem ser disponibilizados para distribuição.
- RN07: Um doador pode realizar várias doações.
- RN08: Um beneficiário pode receber alimentos em diferentes distribuições ao longo do tempo.

## Tecnologias

- Java 21;
- Spring Boot 4 (Web MVC, Data JPA, Validation, Actuator);
- MySQL 8.4, com o esquema versionado pelo Flyway;
- H2 em memória para os testes e para rodar sem MySQL;
- springdoc-openapi (Swagger UI) para documentar e experimentar a API;
- JUnit 5 e AssertJ nos testes;
- Maven (via Maven Wrapper, sem instalação), Docker, Git e GitHub.

A aplicação é um monólito organizado por módulo de negócio (`cadastro`, `doacao`, `distribuicao`, `estoque`). Dentro de cada módulo as responsabilidades seguem em camadas: Model, Repository, Service e Controller.

As decisões de projeto e o modelo de dados estão em [docs/arquitetura.md](docs/arquitetura.md).

## Como executar

O único pré-requisito é o **JDK 21 ou superior**. O Maven vem embutido no projeto pelo Maven Wrapper (`mvnw`).

Os comandos abaixo aparecem em duas versões: a primeira para Linux, macOS e Git Bash; a segunda para o PowerShell do Windows. No PowerShell, os parâmetros que começam com `-D` precisam ficar entre aspas.

### Opção 1: sem banco instalado (H2 em memória)

A forma mais rápida de ver o sistema funcionando. Os dados somem quando a aplicação para.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"
```

### Opção 2: com MySQL no Docker

```bash
docker compose up -d db
```

```bash
./mvnw spring-boot:run
```

```powershell
.\mvnw.cmd spring-boot:run
```

### Opção 3: tudo no Docker

Não exige Java na máquina.

```bash
docker compose up --build
```

Em qualquer opção, a aplicação sobe em `http://localhost:8080`:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Saúde da aplicação:** http://localhost:8080/actuator/health

A conexão com o banco é configurada pelas variáveis de ambiente `DB_URL`, `DB_USER` e `DB_PASSWORD`. Os valores padrão servem apenas para desenvolvimento local.

Se aparecer a mensagem `JAVA_HOME environment variable is not defined correctly`, o JDK não está instalado ou a variável de ambiente `JAVA_HOME` não aponta para a pasta dele.

### Testes

```bash
./mvnw test
```

```powershell
.\mvnw.cmd test
```

Os testes sobem a aplicação com H2 em modo MySQL e aplicam os mesmos scripts de `database/migration`.

## API

Todos os endereços começam com `/api/v1`. A descrição completa, com os campos de cada requisição, está no Swagger UI.

| Recurso | Endpoints | Requisitos |
|---|---|---|
| Doadores | `GET, POST /doadores` · `GET, PUT, DELETE /doadores/{id}` · `POST /doadores/{id}/reativacao` | RF01, RF08, RF09 |
| Alimentos | `GET, POST /alimentos` · `GET, PUT, DELETE /alimentos/{id}` · `POST /alimentos/{id}/reativacao` | RF02, RF08, RF09 |
| Beneficiários | `GET, POST /beneficiarios` · `GET, PUT, DELETE /beneficiarios/{id}` · `POST /beneficiarios/{id}/reativacao` | RF04, RF08, RF09 |
| Pessoas | `GET /pessoas?busca=&ativo=` (procura em doadores e beneficiários ao mesmo tempo) | |
| Doações | `POST /doacoes` · `GET /doacoes?doadorId=&de=&ate=` · `GET /doacoes/{id}` · `POST /doacoes/{id}/cancelamento` | RF03, RF07 |
| Distribuições | `POST /distribuicoes` · `GET /distribuicoes?beneficiarioId=&de=&ate=` · `GET /distribuicoes/{id}` · `POST /distribuicoes/{id}/cancelamento` | RF05, RF07 |
| Estoque | `GET /estoque` · `GET /estoque/lotes?alimentoId=&vencidos=&venceEmDias=` · `POST /estoque/lotes/{id}/descarte` · `GET /estoque/movimentacoes` | RF06, RF07 |
| Instituição | `GET, PUT /instituicao` | |

Exemplo: registrar uma doação e, depois, uma distribuição.

```bash
curl -X POST http://localhost:8080/api/v1/doacoes -H "Content-Type: application/json" -d '{"doadorId": 1, "itens": [{"alimentoId": 1, "quantidade": 10, "dataValidade": "2027-03-01"}]}'
```

```bash
curl -X POST http://localhost:8080/api/v1/distribuicoes -H "Content-Type: application/json" -d '{"beneficiarioId": 1, "itens": [{"alimentoId": 1, "quantidade": 7}]}'
```

Na distribuição informa-se apenas o alimento e a quantidade. O sistema retira dos lotes que vencem primeiro e devolve na resposta quanto saiu de cada lote. Erros seguem o formato Problem Details (RFC 9457): `400` para dados inválidos, `404` para registro inexistente, `409` para conflito (como estoque insuficiente) e `422` para regra de negócio violada.

## Cronograma

| Data | Atividade | Responsável |
|---|---|---|
| 29/08/2026 | Definição do tema, problema e ODS | Matheus |
| 30/08/2026 | Levantamento e documentação dos requisitos | Matheus |
| 05/09/2026 | Elaboração do Diagrama de Classes | André |
| 05/09/2026 | Elaboração do DER | André |
| 10/09/2026 | Estruturação do repositório GitHub | Matheus |
| 30/10/2026 | Desenvolvimento do CRUD | Cauã |
| 31/10/2026 | Integração e testes com banco de dados | Cauã |
| 06/11/2026 | Revisão e entrega final | André |

Prazo de entrega da 2ª etapa: 13/11/2026.

## Estrutura do Repositório

```text
sistema-doacoes/
├── src/
│   ├── main/java/com/doacoes/   código da aplicação, por módulo
│   ├── main/resources/          configuração
│   └── test/java/com/doacoes/   testes automatizados
├── database/
│   └── migration/               scripts SQL do banco (Flyway)
├── docs/
│   ├── arquitetura.md
│   ├── Diagrama-de-Classes.png
│   ├── DER.png
│   └── documentacao.pdf
├── .github/workflows/ci.yml     compila e testa a cada push
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── mvnw, mvnw.cmd               Maven Wrapper
└── README.md
```
