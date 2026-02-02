-- ============================================================================
-- Migration: Integrate Fichas with Configuration System
-- Versão: V20260201100200
-- Descrição: Cria tabelas de relacionamento entre fichas e configurações
-- Autor: Backend Team
-- Data: 2026-02-01
-- Referência: ISSUE-004
-- ============================================================================

-- ===========================================================================
-- EXTENSÃO DA TABELA: fichas
-- Adiciona relacionamentos com classe e raça
-- ===========================================================================
ALTER TABLE fichas ADD COLUMN classe_id BIGINT;
ALTER TABLE fichas ADD COLUMN raca_id BIGINT;
ALTER TABLE fichas ADD COLUMN genero_id BIGINT;
ALTER TABLE fichas ADD COLUMN indole_id BIGINT;
ALTER TABLE fichas ADD COLUMN presenca_id BIGINT;

ALTER TABLE fichas ADD CONSTRAINT fk_fichas_classe
    FOREIGN KEY (classe_id) REFERENCES configuracao_classes(id);
ALTER TABLE fichas ADD CONSTRAINT fk_fichas_raca
    FOREIGN KEY (raca_id) REFERENCES configuracao_racas(id);
ALTER TABLE fichas ADD CONSTRAINT fk_fichas_genero
    FOREIGN KEY (genero_id) REFERENCES configuracao_generos(id);
ALTER TABLE fichas ADD CONSTRAINT fk_fichas_indole
    FOREIGN KEY (indole_id) REFERENCES configuracao_indoles(id);
ALTER TABLE fichas ADD CONSTRAINT fk_fichas_presenca
    FOREIGN KEY (presenca_id) REFERENCES configuracao_presencas(id);

CREATE INDEX idx_fichas_classe ON fichas(classe_id);
CREATE INDEX idx_fichas_raca ON fichas(raca_id);

-- ===========================================================================
-- TABELA: ficha_atributos
-- Descrição: Valores dos atributos de uma ficha
-- ===========================================================================
CREATE TABLE ficha_atributos (
    id BIGSERIAL PRIMARY KEY,
    ficha_id BIGINT NOT NULL,
    atributo_id BIGINT NOT NULL,
    valor_base INT NOT NULL DEFAULT 0,
    valor_nivel INT NOT NULL DEFAULT 0,
    valor_outros INT NOT NULL DEFAULT 0,
    valor_total INT NOT NULL DEFAULT 0,
    modificador INT NOT NULL DEFAULT 0,
    FOREIGN KEY (ficha_id) REFERENCES fichas(id) ON DELETE CASCADE,
    FOREIGN KEY (atributo_id) REFERENCES configuracao_atributos(id),
    CONSTRAINT uk_ficha_atributo UNIQUE (ficha_id, atributo_id)
);

CREATE INDEX idx_ficha_atributos_ficha ON ficha_atributos(ficha_id);
CREATE INDEX idx_ficha_atributos_atributo ON ficha_atributos(atributo_id);

-- ===========================================================================
-- TABELA: ficha_aptidoes
-- Descrição: Valores das aptidões de uma ficha
-- ===========================================================================
CREATE TABLE ficha_aptidoes (
    id BIGSERIAL PRIMARY KEY,
    ficha_id BIGINT NOT NULL,
    aptidao_id BIGINT NOT NULL,
    valor INT NOT NULL DEFAULT 0,
    FOREIGN KEY (ficha_id) REFERENCES fichas(id) ON DELETE CASCADE,
    FOREIGN KEY (aptidao_id) REFERENCES configuracao_aptidoes(id),
    CONSTRAINT uk_ficha_aptidao UNIQUE (ficha_id, aptidao_id)
);

CREATE INDEX idx_ficha_aptidoes_ficha ON ficha_aptidoes(ficha_id);
CREATE INDEX idx_ficha_aptidoes_aptidao ON ficha_aptidoes(aptidao_id);

-- ===========================================================================
-- TABELA: ficha_vantagens
-- Descrição: Vantagens compradas pela ficha (histórico imutável)
-- ===========================================================================
CREATE TABLE ficha_vantagens (
    id BIGSERIAL PRIMARY KEY,
    ficha_id BIGINT NOT NULL,
    vantagem_id BIGINT NOT NULL,
    nivel_vantagem INT NOT NULL DEFAULT 1,
    pontos_gastos INT NOT NULL,
    data_compra TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ficha_id) REFERENCES fichas(id) ON DELETE CASCADE,
    FOREIGN KEY (vantagem_id) REFERENCES configuracao_vantagens(id),
    CONSTRAINT uk_ficha_vantagem UNIQUE (ficha_id, vantagem_id)
);

CREATE INDEX idx_ficha_vantagens_ficha ON ficha_vantagens(ficha_id);
CREATE INDEX idx_ficha_vantagens_vantagem ON ficha_vantagens(vantagem_id);

-- ===========================================================================
-- TABELA: ficha_vida
-- Descrição: Controle de pontos de vida da ficha
-- ===========================================================================
CREATE TABLE ficha_vida (
    id BIGSERIAL PRIMARY KEY,
    ficha_id BIGINT NOT NULL UNIQUE,
    vida_vigor INT NOT NULL DEFAULT 0,
    vida_nivel INT NOT NULL DEFAULT 0,
    vida_outros INT NOT NULL DEFAULT 0,
    vida_total INT NOT NULL DEFAULT 0,
    vida_atual INT NOT NULL DEFAULT 0,
    FOREIGN KEY (ficha_id) REFERENCES fichas(id) ON DELETE CASCADE
);

CREATE INDEX idx_ficha_vida_ficha ON ficha_vida(ficha_id);

-- ===========================================================================
-- TABELA: ficha_vida_membros
-- Descrição: Integridade física de membros do corpo
-- ===========================================================================
CREATE TABLE ficha_vida_membros (
    id BIGSERIAL PRIMARY KEY,
    ficha_vida_id BIGINT NOT NULL,
    membro_id BIGINT NOT NULL,
    pv_max INT NOT NULL,
    pv_atual INT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'INTACTO'
        CHECK (estado IN ('INTACTO', 'FERIDO', 'MUTILADO', 'PERDIDO')),
    FOREIGN KEY (ficha_vida_id) REFERENCES ficha_vida(id) ON DELETE CASCADE,
    FOREIGN KEY (membro_id) REFERENCES configuracao_membros_corpo(id),
    CONSTRAINT uk_ficha_vida_membro UNIQUE (ficha_vida_id, membro_id)
);

CREATE INDEX idx_ficha_vida_membros_vida ON ficha_vida_membros(ficha_vida_id);
CREATE INDEX idx_ficha_vida_membros_membro ON ficha_vida_membros(membro_id);

-- ===========================================================================
-- TABELA: ficha_prospeccao
-- Descrição: Dados de prospecção/rolagem disponíveis para a ficha
-- ===========================================================================
CREATE TABLE ficha_prospeccao (
    id BIGSERIAL PRIMARY KEY,
    ficha_id BIGINT NOT NULL,
    dado_id BIGINT NOT NULL,
    quantidade INT NOT NULL DEFAULT 1,
    FOREIGN KEY (ficha_id) REFERENCES fichas(id) ON DELETE CASCADE,
    FOREIGN KEY (dado_id) REFERENCES configuracao_prospeccao(id),
    CONSTRAINT uk_ficha_prospeccao UNIQUE (ficha_id, dado_id)
);

CREATE INDEX idx_ficha_prospeccao_ficha ON ficha_prospeccao(ficha_id);
CREATE INDEX idx_ficha_prospeccao_dado ON ficha_prospeccao(dado_id);

-- ===========================================================================
-- TABELA: ficha_essencia
-- Descrição: Informações de essência/mana da ficha
-- ===========================================================================
CREATE TABLE ficha_essencia (
    id BIGSERIAL PRIMARY KEY,
    ficha_id BIGINT NOT NULL UNIQUE,
    essencia_base INT NOT NULL DEFAULT 0,
    essencia_nivel INT NOT NULL DEFAULT 0,
    essencia_outros INT NOT NULL DEFAULT 0,
    essencia_total INT NOT NULL DEFAULT 0,
    essencia_atual INT NOT NULL DEFAULT 0,
    FOREIGN KEY (ficha_id) REFERENCES fichas(id) ON DELETE CASCADE
);

CREATE INDEX idx_ficha_essencia_ficha ON ficha_essencia(ficha_id);

-- ===========================================================================
-- TABELA: ficha_bonus
-- Descrição: Bônus diversos aplicados à ficha
-- ===========================================================================
CREATE TABLE ficha_bonus (
    id BIGSERIAL PRIMARY KEY,
    ficha_id BIGINT NOT NULL,
    bonus_config_id BIGINT,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    tipo VARCHAR(50) NOT NULL,
    valor INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (ficha_id) REFERENCES fichas(id) ON DELETE CASCADE
);

CREATE INDEX idx_ficha_bonus_ficha ON ficha_bonus(ficha_id);
CREATE INDEX idx_ficha_bonus_tipo ON ficha_bonus(tipo);

-- ===========================================================================
-- TABELA: ficha_ameacas
-- Descrição: Registro de ameaças/inimigos conhecidos
-- ===========================================================================
CREATE TABLE ficha_ameacas (
    id BIGSERIAL PRIMARY KEY,
    ficha_id BIGINT NOT NULL,
    nome VARCHAR(200) NOT NULL,
    descricao TEXT,
    nivel_ameaca INT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (ficha_id) REFERENCES fichas(id) ON DELETE CASCADE
);

CREATE INDEX idx_ficha_ameacas_ficha ON ficha_ameacas(ficha_id);
CREATE INDEX idx_ficha_ameacas_ativo ON ficha_ameacas(ativo);

-- ===========================================================================
-- ROLLBACK MANUAL (se necessário):
-- ===========================================================================
-- DROP TABLE IF EXISTS ficha_ameacas CASCADE;
-- DROP TABLE IF EXISTS ficha_bonus CASCADE;
-- DROP TABLE IF EXISTS ficha_essencia CASCADE;
-- DROP TABLE IF EXISTS ficha_prospeccao CASCADE;
-- DROP TABLE IF EXISTS ficha_vida_membros CASCADE;
-- DROP TABLE IF EXISTS ficha_vida CASCADE;
-- DROP TABLE IF EXISTS ficha_vantagens CASCADE;
-- DROP TABLE IF EXISTS ficha_aptidoes CASCADE;
-- DROP TABLE IF EXISTS ficha_atributos CASCADE;
-- ALTER TABLE fichas DROP CONSTRAINT IF EXISTS fk_fichas_presenca;
-- ALTER TABLE fichas DROP CONSTRAINT IF EXISTS fk_fichas_indole;
-- ALTER TABLE fichas DROP CONSTRAINT IF EXISTS fk_fichas_genero;
-- ALTER TABLE fichas DROP CONSTRAINT IF EXISTS fk_fichas_raca;
-- ALTER TABLE fichas DROP CONSTRAINT IF EXISTS fk_fichas_classe;
-- ALTER TABLE fichas DROP COLUMN IF EXISTS presenca_id;
-- ALTER TABLE fichas DROP COLUMN IF EXISTS indole_id;
-- ALTER TABLE fichas DROP COLUMN IF EXISTS genero_id;
-- ALTER TABLE fichas DROP COLUMN IF EXISTS raca_id;
-- ALTER TABLE fichas DROP COLUMN IF EXISTS classe_id;
