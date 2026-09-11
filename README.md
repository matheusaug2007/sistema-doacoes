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
O sistema deve permitir registrar uma doação, relacionando o doador aos alimentos doados e à data de recebimento.

### RF04 - Cadastro de Beneficiários
O sistema deve permitir o cadastro de pessoas ou famílias beneficiárias que recebem os alimentos distribuídos pela instituição.

### RF05 - Registro de Distribuição
O sistema deve permitir registrar a entrega de alimentos para um beneficiário, informando os alimentos entregues e suas respectivas quantidades.

### RF06 - Consulta de Estoque
O sistema deve permitir consultar os alimentos disponíveis e suas respectivas quantidades.

### RF07 - Consulta de Histórico
O sistema deve permitir consultar o histórico de doações recebidas e distribuições realizadas.

### RF08 - Atualização de Registros
O sistema deve permitir alterar informações de doadores, alimentos e beneficiários cadastrados.

### RF09 - Exclusão de Registros
O sistema deve permitir excluir registros quando permitido pelas regras de negócio.

## Regras de Negócio

- RN01: Uma doação deve estar vinculada a pelo menos um doador.
- RN02: Uma doação deve possuir pelo menos um alimento registrado.
- RN03: A quantidade de alimentos distribuída não pode ser maior do que a quantidade disponível em estoque.
- RN04: Toda distribuição de alimentos deve estar vinculada a um beneficiário.
- RN05: O estoque deve ser atualizado sempre que uma nova doação ou distribuição for registrada.
- RN06: Alimentos com data de validade vencida não devem ser disponibilizados para distribuição.
- RN07: Um doador pode realizar várias doações.
- RN08: Um beneficiário pode receber alimentos em diferentes distribuições ao longo do tempo.

## Tecnologias

O projeto será desenvolvido utilizando principalmente:

- Java;
- Spring Boot;
- Spring Data JPA;
- MySQL;
- Git;
- GitHub.

A aplicação será organizada utilizando arquitetura em camadas, separando as responsabilidades entre Model, Repository, Service e Controller.

## Cronograma

| Data | Atividade | Responsável |
|---|---|---|
| 29/08/2026 | Definição do tema, problema e ODS | Matheus |
| 30/08/2026 | Levantamento e documentação dos requisitos | Matheus |
| 05/09/2026 | Elaboração do Diagrama de Classes | André |
| 05/09/2026 | Elaboração do DER | André |
| 10/09/2026 | Organização do repositório no GitHub | Matheus |
| 30/10/2026 | Desenvolvimento do CRUD | Cauã |
| 31/10/2026 | Integração e testes com banco de dados | Cauã |
| 06/11/2026 | Revisão e entrega final | André |

## Estrutura do Repositório

```text
sistema-doacoes/
├── src/
├── docs/
│   ├── Diagrama-Caso-de-Uso.png
│   ├── DER.png
│   └── documentacao.pdf
├── database/
└── README.md
