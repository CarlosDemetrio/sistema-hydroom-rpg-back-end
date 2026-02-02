-- ============================================================================
-- Migration: Create Configuration Tables
-- Versão: V20260201100100
-- Descrição: Cria todas as tabelas de configuração do sistema
-- Autor: Backend Team
-- Data: 2026-02-01
-- Referência: ISSUE-003
-- ============================================================================

-- ===========================================================================
-- TABELA: configuracao_atributos
-- Descrição: Define os atributos configuráveis (Força, Agilidade, etc)
-- ===========================================================================
CREATE TABLE configuracao_atributos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT,
    formula_impeto VARCHAR(255) NOT NULL,
    unidade_impeto VARCHAR(50),
    ordem_exibicao INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_config_atributos_ativo ON configuracao_atributos(ativo);
CREATE INDEX idx_config_atributos_ordem ON configuracao_atributos(ordem_exibicao);

-- ===========================================================================
-- TABELA: configuracao_aptidoes
-- Descrição: Define as aptidões configuráveis (Acrobacia, Guarda, etc)
-- ===========================================================================
CREATE TABLE configuracao_aptidoes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('FISICA', 'MENTAL')),
    descricao TEXT,
    ordem_exibicao INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_config_aptidoes_ativo ON configuracao_aptidoes(ativo);
CREATE INDEX idx_config_aptidoes_tipo ON configuracao_aptidoes(tipo);
CREATE INDEX idx_config_aptidoes_ordem ON configuracao_aptidoes(ordem_exibicao);

-- ===========================================================================
-- TABELA: configuracao_niveis
-- Descrição: Define requisitos de XP e recompensas por nível
-- ===========================================================================
CREATE TABLE configuracao_niveis (
    id BIGSERIAL PRIMARY KEY,
    nivel INT NOT NULL UNIQUE,
    experiencia_necessaria BIGINT NOT NULL,
    pontos_atributo INT NOT NULL DEFAULT 3,
    pontos_vantagem INT NOT NULL DEFAULT 1,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_config_niveis_nivel ON configuracao_niveis(nivel);
CREATE INDEX idx_config_niveis_exp ON configuracao_niveis(experiencia_necessaria);
CREATE INDEX idx_config_niveis_ativo ON configuracao_niveis(ativo);

-- ===========================================================================
-- TABELA: configuracao_limitadores
-- Descrição: Define limites máximos de atributos por faixa de nível
-- ===========================================================================
CREATE TABLE configuracao_limitadores (
    id BIGSERIAL PRIMARY KEY,
    nivel_inicio INT NOT NULL,
    nivel_fim INT NOT NULL,
    limite_atributo INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_nivel_range CHECK (nivel_fim >= nivel_inicio)
);

CREATE INDEX idx_config_limitadores_nivel ON configuracao_limitadores(nivel_inicio, nivel_fim);
CREATE INDEX idx_config_limitadores_ativo ON configuracao_limitadores(ativo);

-- ===========================================================================
-- TABELA: configuracao_classes
-- Descrição: Define classes disponíveis (Guerreiro, Mago, etc)
-- ===========================================================================
CREATE TABLE configuracao_classes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT,
    ordem_exibicao INT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_config_classes_ativo ON configuracao_classes(ativo);
CREATE INDEX idx_config_classes_ordem ON configuracao_classes(ordem_exibicao);

-- ===========================================================================
-- TABELA: configuracao_racas
-- Descrição: Define raças disponíveis (Humano, Elfo, etc)
-- ===========================================================================
CREATE TABLE configuracao_racas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT,
    ordem_exibicao INT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_config_racas_ativo ON configuracao_racas(ativo);
CREATE INDEX idx_config_racas_ordem ON configuracao_racas(ordem_exibicao);

-- ===========================================================================
-- TABELA: raca_bonus_atributos
-- Descrição: Relacionamento N:N entre raças e bônus de atributos
-- ===========================================================================
CREATE TABLE raca_bonus_atributos (
    id BIGSERIAL PRIMARY KEY,
    raca_id BIGINT NOT NULL,
    atributo_id BIGINT NOT NULL,
    bonus INT NOT NULL,
    FOREIGN KEY (raca_id) REFERENCES configuracao_racas(id) ON DELETE CASCADE,
    FOREIGN KEY (atributo_id) REFERENCES configuracao_atributos(id),
    CONSTRAINT uk_raca_atributo UNIQUE (raca_id, atributo_id)
);

CREATE INDEX idx_raca_bonus_raca ON raca_bonus_atributos(raca_id);
CREATE INDEX idx_raca_bonus_atributo ON raca_bonus_atributos(atributo_id);

-- ===========================================================================
-- TABELA: configuracao_vantagens
-- Descrição: Define vantagens compráveis para fichas
-- ===========================================================================
CREATE TABLE configuracao_vantagens (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    descricao TEXT,
    tipo_bonus VARCHAR(50) NOT NULL,
    valor_bonus_formula VARCHAR(255),
    custo_base INT NOT NULL,
    formula_custo VARCHAR(255) NOT NULL DEFAULT 'custo_base * nivel_vantagem',
    nivel_minimo_personagem INT DEFAULT 1,
    pode_evoluir BOOLEAN NOT NULL DEFAULT TRUE,
    nivel_maximo_vantagem INT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_config_vantagens_ativo ON configuracao_vantagens(ativo);
CREATE INDEX idx_config_vantagens_tipo ON configuracao_vantagens(tipo_bonus);

-- ===========================================================================
-- TABELA: configuracao_prospeccao
-- Descrição: Define dados disponíveis para rolagens (d3, d4, d6, etc)
-- ===========================================================================
CREATE TABLE configuracao_prospeccao (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    num_lados INT NOT NULL,
    ordem_exibicao INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_config_prospeccao_ativo ON configuracao_prospeccao(ativo);
CREATE INDEX idx_config_prospeccao_ordem ON configuracao_prospeccao(ordem_exibicao);

-- ===========================================================================
-- TABELA: configuracao_generos
-- Descrição: Define gêneros disponíveis para personagens
-- ===========================================================================
CREATE TABLE configuracao_generos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    ordem_exibicao INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_config_generos_ativo ON configuracao_generos(ativo);

-- ===========================================================================
-- TABELA: configuracao_indoles
-- Descrição: Define indoles/alinhamentos disponíveis
-- ===========================================================================
CREATE TABLE configuracao_indoles (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    ordem_exibicao INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_config_indoles_ativo ON configuracao_indoles(ativo);

-- ===========================================================================
-- TABELA: configuracao_presencas
-- Descrição: Define níveis de presença de personagens
-- ===========================================================================
CREATE TABLE configuracao_presencas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    ordem_exibicao INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_config_presencas_ativo ON configuracao_presencas(ativo);

-- ===========================================================================
-- TABELA: configuracao_membros_corpo
-- Descrição: Define membros do corpo para integridade física
-- ===========================================================================
CREATE TABLE configuracao_membros_corpo (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    ordem_exibicao INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_config_membros_ativo ON configuracao_membros_corpo(ativo);

-- ===========================================================================
-- ROLLBACK MANUAL (se necessário):
-- ===========================================================================
-- DROP TABLE IF EXISTS configuracao_membros_corpo CASCADE;
-- DROP TABLE IF EXISTS configuracao_presencas CASCADE;
-- DROP TABLE IF EXISTS configuracao_indoles CASCADE;
-- DROP TABLE IF EXISTS configuracao_generos CASCADE;
-- DROP TABLE IF EXISTS configuracao_prospeccao CASCADE;
-- DROP TABLE IF EXISTS configuracao_vantagens CASCADE;
-- DROP TABLE IF EXISTS raca_bonus_atributos CASCADE;
-- DROP TABLE IF EXISTS configuracao_racas CASCADE;
-- DROP TABLE IF EXISTS configuracao_classes CASCADE;
-- DROP TABLE IF EXISTS configuracao_limitadores CASCADE;
-- DROP TABLE IF EXISTS configuracao_niveis CASCADE;
-- DROP TABLE IF EXISTS configuracao_aptidoes CASCADE;
-- DROP TABLE IF EXISTS configuracao_atributos CASCADE;
