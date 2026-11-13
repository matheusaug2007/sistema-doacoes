# Código-fonte

Este diretório armazena o código-fonte da aplicação, desenvolvida em Java e Spring Boot.

- `main/java/com/doacoes/` — código da aplicação, organizado por módulo de negócio (`cadastro`, `doacao`, `distribuicao`, `estoque`) e, dentro de cada um, em camadas (Model, Repository, Service e Controller);
- `main/resources/` — configuração (`application.yml`);
- `test/java/com/doacoes/` — testes automatizados das regras de negócio e da API.

As decisões de projeto estão em [docs/arquitetura.md](../docs/arquitetura.md), e as instruções de execução, no [README principal](../README.md).
