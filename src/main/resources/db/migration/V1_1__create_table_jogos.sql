-- =============================================================================
-- Migration: V1_1 - Criação da tabela JOGOS
-- Description: Tabela que armazena os jogos/campanhas de RPG
-- Author: System
-- Date: 2026-02-01
-- =============================================================================

CREATE TABLE jogos (
    -- Identificação
    id BIGSERIAL PRIMARY KEY,

    -- Informações Básicas
    nome VARCHAR(200) NOT NULL,
    descricao TEXT,
    imagem_url TEXT,

    -- Controle
    ativo BOOLEAN NOT NULL DEFAULT true,

    -- Auditoria
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- Índices
-- =============================================================================

-- Busca por nome
CREATE INDEX idx_jogo_nome ON jogos(nome);

-- Busca por jogos ativos
CREATE INDEX idx_jogo_ativo ON jogos(ativo);

-- =============================================================================
-- Comentários
-- =============================================================================

COMMENT ON TABLE jogos IS 'Jogos/Campanhas de RPG criados pelos mestres';
COMMENT ON COLUMN jogos.nome IS 'Nome do jogo/campanha';
COMMENT ON COLUMN jogos.descricao IS 'Descrição detalhada do jogo';
COMMENT ON COLUMN jogos.imagem_url IS 'URL ou Base64 da imagem de capa do jogo';
COMMENT ON COLUMN jogos.ativo IS 'Indica se o jogo está ativo (soft delete)';
