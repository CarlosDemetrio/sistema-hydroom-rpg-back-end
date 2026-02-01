-- Cria tabelas de configuração de personalidade

-- Tabela de configuração de gêneros
CREATE TABLE generos_config (
    id BIGSERIAL PRIMARY KEY,
    jogo_id BIGINT NOT NULL,
    nome VARCHAR(50) NOT NULL,
    descricao VARCHAR(200),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    ordem INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_genero_nome_jogo UNIQUE (jogo_id, nome),
    CONSTRAINT fk_genero_jogo FOREIGN KEY (jogo_id) REFERENCES jogos(id)
);

CREATE INDEX idx_genero_jogo_ativo ON generos_config(jogo_id, ativo);

COMMENT ON TABLE generos_config IS 'Configuração de gêneros disponíveis no jogo';
COMMENT ON COLUMN generos_config.ordem IS 'Ordem de exibição no formulário';

-- Tabela de configuração de índoles
CREATE TABLE indoles_config (
    id BIGSERIAL PRIMARY KEY,
    jogo_id BIGINT NOT NULL,
    nome VARCHAR(50) NOT NULL,
    descricao VARCHAR(200),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    ordem INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_indole_nome_jogo UNIQUE (jogo_id, nome),
    CONSTRAINT fk_indole_jogo FOREIGN KEY (jogo_id) REFERENCES jogos(id)
);

CREATE INDEX idx_indole_jogo_ativo ON indoles_config(jogo_id, ativo);

COMMENT ON TABLE indoles_config IS 'Configuração de índoles disponíveis (Bom, Mau, Neutro, etc)';

-- Tabela de configuração de presenças
CREATE TABLE presencas_config (
    id BIGSERIAL PRIMARY KEY,
    jogo_id BIGINT NOT NULL,
    nome VARCHAR(50) NOT NULL,
    descricao VARCHAR(200),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    ordem INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_presenca_nome_jogo UNIQUE (jogo_id, nome),
    CONSTRAINT fk_presenca_jogo FOREIGN KEY (jogo_id) REFERENCES jogos(id)
);

CREATE INDEX idx_presenca_jogo_ativo ON presencas_config(jogo_id, ativo);

COMMENT ON TABLE presencas_config IS 'Configuração de presenças disponíveis (Leal, Caótico, Neutro, etc)';

-- Tabela de configuração de níveis e progressão
CREATE TABLE niveis_config (
    id BIGSERIAL PRIMARY KEY,
    jogo_id BIGINT NOT NULL,
    nivel INT NOT NULL,
    xp_necessaria BIGINT NOT NULL,
    pontos_atributo INT NOT NULL DEFAULT 3,
    limitador_atributo INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_nivel_jogo UNIQUE (jogo_id, nivel),
    CONSTRAINT fk_nivel_jogo FOREIGN KEY (jogo_id) REFERENCES jogos(id),
    CONSTRAINT chk_nivel_positivo CHECK (nivel > 0),
    CONSTRAINT chk_xp_nao_negativa CHECK (xp_necessaria >= 0),
    CONSTRAINT chk_pontos_atributo_nao_negativos CHECK (pontos_atributo >= 0),
    CONSTRAINT chk_limitador_positivo CHECK (limitador_atributo > 0)
);

CREATE INDEX idx_nivel_jogo ON niveis_config(jogo_id, nivel);

COMMENT ON TABLE niveis_config IS 'Tabela de progressão de níveis - XP necessária e limitadores';
COMMENT ON COLUMN niveis_config.pontos_atributo IS 'Pontos de atributo ganhos ao atingir este nível';
COMMENT ON COLUMN niveis_config.limitador_atributo IS 'Valor máximo que atributos podem ter neste nível';

-- Auditoria (Envers)
CREATE TABLE generos_config_aud (LIKE generos_config);
ALTER TABLE generos_config_aud ADD COLUMN rev INTEGER NOT NULL;
ALTER TABLE generos_config_aud ADD COLUMN revtype SMALLINT;
ALTER TABLE generos_config_aud ADD PRIMARY KEY (id, rev);
ALTER TABLE generos_config_aud ADD CONSTRAINT fk_genero_rev FOREIGN KEY (rev) REFERENCES revinfo(rev);

CREATE TABLE indoles_config_aud (LIKE indoles_config);
ALTER TABLE indoles_config_aud ADD COLUMN rev INTEGER NOT NULL;
ALTER TABLE indoles_config_aud ADD COLUMN revtype SMALLINT;
ALTER TABLE indoles_config_aud ADD PRIMARY KEY (id, rev);
ALTER TABLE indoles_config_aud ADD CONSTRAINT fk_indole_rev FOREIGN KEY (rev) REFERENCES revinfo(rev);

CREATE TABLE presencas_config_aud (LIKE presencas_config);
ALTER TABLE presencas_config_aud ADD COLUMN rev INTEGER NOT NULL;
ALTER TABLE presencas_config_aud ADD COLUMN revtype SMALLINT;
ALTER TABLE presencas_config_aud ADD PRIMARY KEY (id, rev);
ALTER TABLE presencas_config_aud ADD CONSTRAINT fk_presenca_rev FOREIGN KEY (rev) REFERENCES revinfo(rev);

CREATE TABLE niveis_config_aud (LIKE niveis_config);
ALTER TABLE niveis_config_aud ADD COLUMN rev INTEGER NOT NULL;
ALTER TABLE niveis_config_aud ADD COLUMN revtype SMALLINT;
ALTER TABLE niveis_config_aud ADD PRIMARY KEY (id, rev);
ALTER TABLE niveis_config_aud ADD CONSTRAINT fk_nivel_rev FOREIGN KEY (rev) REFERENCES revinfo(rev);
