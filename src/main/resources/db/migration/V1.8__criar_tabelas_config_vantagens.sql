-- Cria tabelas de configuração de vantagens

-- Tabela de categorias de vantagens
CREATE TABLE categorias_vantagem (
    id BIGSERIAL PRIMARY KEY,
    jogo_id BIGINT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    ordem INT NOT NULL,
    cor VARCHAR(7),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_categoria_vantagem_nome_jogo UNIQUE (jogo_id, nome),
    CONSTRAINT fk_categoria_vantagem_jogo FOREIGN KEY (jogo_id) REFERENCES jogos(id),
    CONSTRAINT chk_cor_hex CHECK (cor IS NULL OR cor ~ '^#[0-9A-Fa-f]{6}$')
);

CREATE INDEX idx_categoria_vantagem_jogo_ativo ON categorias_vantagem(jogo_id, ativo);

COMMENT ON TABLE categorias_vantagem IS 'Categorias de vantagens configuráveis pelo Mestre';
COMMENT ON COLUMN categorias_vantagem.cor IS 'Cor em hexadecimal (#RRGGBB) para representação visual';

-- Tabela de pontos de vantagem por nível
CREATE TABLE pontos_vantagem_config (
    id BIGSERIAL PRIMARY KEY,
    jogo_id BIGINT NOT NULL,
    nivel INT NOT NULL,
    pontos_ganhos INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_pontos_vantagem_nivel_jogo UNIQUE (jogo_id, nivel),
    CONSTRAINT fk_pontos_vantagem_jogo FOREIGN KEY (jogo_id) REFERENCES jogos(id),
    CONSTRAINT chk_pontos_vantagem_nivel_positivo CHECK (nivel > 0),
    CONSTRAINT chk_pontos_vantagem_nao_negativos CHECK (pontos_ganhos >= 0)
);

CREATE INDEX idx_pontos_vantagem_jogo ON pontos_vantagem_config(jogo_id, nivel);

COMMENT ON TABLE pontos_vantagem_config IS 'Configuração de pontos de vantagem ganhos por nível';
COMMENT ON COLUMN pontos_vantagem_config.pontos_ganhos IS 'Quantidade de pontos ganhos ao atingir este nível';

-- Auditoria (Envers)
CREATE TABLE categorias_vantagem_aud (LIKE categorias_vantagem);
ALTER TABLE categorias_vantagem_aud ADD COLUMN rev INTEGER NOT NULL;
ALTER TABLE categorias_vantagem_aud ADD COLUMN revtype SMALLINT;
ALTER TABLE categorias_vantagem_aud ADD PRIMARY KEY (id, rev);
ALTER TABLE categorias_vantagem_aud ADD CONSTRAINT fk_categoria_vantagem_rev FOREIGN KEY (rev) REFERENCES revinfo(rev);

CREATE TABLE pontos_vantagem_config_aud (LIKE pontos_vantagem_config);
ALTER TABLE pontos_vantagem_config_aud ADD COLUMN rev INTEGER NOT NULL;
ALTER TABLE pontos_vantagem_config_aud ADD COLUMN revtype SMALLINT;
ALTER TABLE pontos_vantagem_config_aud ADD PRIMARY KEY (id, rev);
ALTER TABLE pontos_vantagem_config_aud ADD CONSTRAINT fk_pontos_vantagem_rev FOREIGN KEY (rev) REFERENCES revinfo(rev);
