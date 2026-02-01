-- Configuração de dados de prospecção (d4, d6, d8, d10, d12, d20, d100)
CREATE TABLE dado_prospeccao_config (
    id BIGSERIAL PRIMARY KEY,
    jogo_id BIGINT NOT NULL,
    nome VARCHAR(20) NOT NULL,
    descricao VARCHAR(200),
    numero_faces INTEGER NOT NULL CHECK (numero_faces >= 1 AND numero_faces <= 100),
    ordem_exibicao INTEGER DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por VARCHAR(255),
    atualizado_por VARCHAR(255),
    CONSTRAINT fk_dado_prospeccao_config_jogo FOREIGN KEY (jogo_id) REFERENCES jogos(id),
    CONSTRAINT uk_dado_prospeccao_config_jogo_nome UNIQUE (jogo_id, nome)
);

CREATE INDEX idx_dado_prospeccao_config_jogo ON dado_prospeccao_config(jogo_id, ativo);

COMMENT ON TABLE dado_prospeccao_config IS 'Configuração de dados de prospecção disponíveis no jogo';
COMMENT ON COLUMN dado_prospeccao_config.numero_faces IS 'Número de faces do dado (4, 6, 8, 10, 12, 20, 100)';

-- Configuração de vantagens
CREATE TABLE vantagem_config (
    id BIGSERIAL PRIMARY KEY,
    jogo_id BIGINT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(1000),
    nivel_maximo INTEGER NOT NULL DEFAULT 10 CHECK (nivel_maximo >= 1),
    formula_custo VARCHAR(100) NOT NULL,
    descricao_efeito VARCHAR(500),
    ordem_exibicao INTEGER DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    criado_por VARCHAR(255),
    atualizado_por VARCHAR(255),
    CONSTRAINT fk_vantagem_config_jogo FOREIGN KEY (jogo_id) REFERENCES jogos(id),
    CONSTRAINT uk_vantagem_config_jogo_nome UNIQUE (jogo_id, nome)
);

CREATE INDEX idx_vantagem_config_jogo ON vantagem_config(jogo_id, ativo);

COMMENT ON TABLE vantagem_config IS 'Configuração de vantagens disponíveis no jogo';
COMMENT ON COLUMN vantagem_config.formula_custo IS 'Fórmula para calcular o custo (ex: NIVEL * 2, NIVEL * NIVEL)';
COMMENT ON COLUMN vantagem_config.descricao_efeito IS 'Descrição dos efeitos mecânicos da vantagem';

-- Tabelas de auditoria Envers
CREATE TABLE dado_prospeccao_config_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    jogo_id BIGINT,
    nome VARCHAR(20),
    descricao VARCHAR(200),
    numero_faces INTEGER,
    ordem_exibicao INTEGER,
    ativo BOOLEAN,
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP,
    criado_por VARCHAR(255),
    atualizado_por VARCHAR(255),
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_dado_prospeccao_config_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
);

CREATE TABLE vantagem_config_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    jogo_id BIGINT,
    nome VARCHAR(100),
    descricao VARCHAR(1000),
    nivel_maximo INTEGER,
    formula_custo VARCHAR(100),
    descricao_efeito VARCHAR(500),
    ordem_exibicao INTEGER,
    ativo BOOLEAN,
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP,
    criado_por VARCHAR(255),
    atualizado_por VARCHAR(255),
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_vantagem_config_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
);
