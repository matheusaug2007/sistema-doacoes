-- Esquema inicial do Sistema de Gestão de Doações de Alimentos.
-- Escrito em SQL comum ao MySQL 8 e ao H2 (modo MySQL), para que os testes usem o mesmo script.

CREATE TABLE instituicao (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nome        VARCHAR(150) NOT NULL,
    cnpj        VARCHAR(18),
    telefone    VARCHAR(20),
    email       VARCHAR(150),
    endereco    VARCHAR(255),
    responsavel VARCHAR(150),
    PRIMARY KEY (id),
    CONSTRAINT uk_instituicao_cnpj UNIQUE (cnpj)
);

CREATE TABLE doador (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    instituicao_id BIGINT       NOT NULL,
    nome           VARCHAR(150) NOT NULL,
    tipo           VARCHAR(20)  NOT NULL,
    documento      VARCHAR(20),
    telefone       VARCHAR(20),
    email          VARCHAR(150),
    endereco       VARCHAR(255),
    observacoes    VARCHAR(500),
    ativo          BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_doador_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id),
    CONSTRAINT uk_doador_documento UNIQUE (instituicao_id, documento)
);

CREATE TABLE beneficiario (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    instituicao_id     BIGINT       NOT NULL,
    nome               VARCHAR(150) NOT NULL,
    tipo               VARCHAR(20)  NOT NULL,
    documento          VARCHAR(20),
    telefone           VARCHAR(20),
    email              VARCHAR(150),
    endereco           VARCHAR(255),
    quantidade_pessoas INT          NOT NULL DEFAULT 1,
    observacoes        VARCHAR(500),
    ativo              BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_beneficiario_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id),
    CONSTRAINT uk_beneficiario_documento UNIQUE (instituicao_id, documento),
    CONSTRAINT ck_beneficiario_pessoas CHECK (quantidade_pessoas >= 1)
);

CREATE TABLE alimento (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    instituicao_id BIGINT       NOT NULL,
    nome           VARCHAR(100) NOT NULL,
    categoria      VARCHAR(60)  NOT NULL,
    unidade_medida VARCHAR(20)  NOT NULL,
    ativo          BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_alimento_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id),
    CONSTRAINT uk_alimento_nome UNIQUE (instituicao_id, nome)
);

CREATE TABLE doacao (
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    instituicao_id      BIGINT      NOT NULL,
    doador_id           BIGINT      NOT NULL,
    data_recebimento    DATE        NOT NULL,
    observacoes         VARCHAR(500),
    status              VARCHAR(20) NOT NULL,
    motivo_cancelamento VARCHAR(255),
    criado_em           DATETIME(6) NOT NULL,
    cancelado_em        DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_doacao_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id),
    CONSTRAINT fk_doacao_doador FOREIGN KEY (doador_id) REFERENCES doador (id)
);

CREATE INDEX idx_doacao_data ON doacao (instituicao_id, data_recebimento);

-- Cada item de doação é um lote: guarda a validade e o saldo ainda disponível.
CREATE TABLE item_doacao (
    id                    BIGINT         NOT NULL AUTO_INCREMENT,
    doacao_id             BIGINT         NOT NULL,
    alimento_id           BIGINT         NOT NULL,
    quantidade            DECIMAL(12, 3) NOT NULL,
    quantidade_disponivel DECIMAL(12, 3) NOT NULL,
    data_validade         DATE,
    versao                BIGINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_item_doacao_doacao FOREIGN KEY (doacao_id) REFERENCES doacao (id),
    CONSTRAINT fk_item_doacao_alimento FOREIGN KEY (alimento_id) REFERENCES alimento (id),
    CONSTRAINT ck_item_doacao_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_item_doacao_saldo CHECK (quantidade_disponivel >= 0 AND quantidade_disponivel <= quantidade)
);

-- Atende a busca dos lotes de um alimento em ordem de validade (FEFO).
CREATE INDEX idx_item_doacao_fefo ON item_doacao (alimento_id, data_validade);

CREATE TABLE distribuicao (
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    instituicao_id      BIGINT      NOT NULL,
    beneficiario_id     BIGINT      NOT NULL,
    data_distribuicao   DATE        NOT NULL,
    observacoes         VARCHAR(500),
    status              VARCHAR(20) NOT NULL,
    motivo_cancelamento VARCHAR(255),
    criado_em           DATETIME(6) NOT NULL,
    cancelado_em        DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_distribuicao_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id),
    CONSTRAINT fk_distribuicao_beneficiario FOREIGN KEY (beneficiario_id) REFERENCES beneficiario (id)
);

CREATE INDEX idx_distribuicao_data ON distribuicao (instituicao_id, data_distribuicao);

CREATE TABLE item_distribuicao (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    distribuicao_id BIGINT         NOT NULL,
    item_doacao_id  BIGINT         NOT NULL,
    quantidade      DECIMAL(12, 3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_item_distribuicao_distribuicao FOREIGN KEY (distribuicao_id) REFERENCES distribuicao (id),
    CONSTRAINT fk_item_distribuicao_lote FOREIGN KEY (item_doacao_id) REFERENCES item_doacao (id),
    CONSTRAINT ck_item_distribuicao_quantidade CHECK (quantidade > 0)
);

-- Livro de movimentações: só recebe inserções. O saldo de um lote é a soma das suas linhas.
CREATE TABLE movimentacao_estoque (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    item_doacao_id  BIGINT         NOT NULL,
    distribuicao_id BIGINT,
    tipo            VARCHAR(20)    NOT NULL,
    quantidade      DECIMAL(12, 3) NOT NULL,
    motivo          VARCHAR(255),
    criado_em       DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_movimentacao_lote FOREIGN KEY (item_doacao_id) REFERENCES item_doacao (id),
    CONSTRAINT fk_movimentacao_distribuicao FOREIGN KEY (distribuicao_id) REFERENCES distribuicao (id),
    CONSTRAINT ck_movimentacao_quantidade CHECK (quantidade > 0)
);

CREATE INDEX idx_movimentacao_criado_em ON movimentacao_estoque (criado_em);
