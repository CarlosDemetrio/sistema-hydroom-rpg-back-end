-- =============================================================================
-- Migration: V1_3 - Criação da tabela RACAS
-- Description: Tabela de configuração de raças de personagem
-- Author: System
-- Date: 2026-02-01
-- =============================================================================

CREATE TABLE racas (
    -- Identificação
    id BIGSERIAL PRIMARY KEY,
    jogo_id BIGINT NOT NULL,

    -- Informações Básicas
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,

    -- Controle
    ativo BOOLEAN NOT NULL DEFAULT true,
    ordem_exibicao INTEGER NOT NULL DEFAULT 0,

    -- Auditoria
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_raca_jogo FOREIGN KEY (jogo_id)
        REFERENCES jogos(id) ON DELETE CASCADE,

    -- Unique Constraints
    CONSTRAINT uk_raca_jogo_nome UNIQUE (jogo_id, nome)
);

-- =============================================================================
-- Índices
-- =============================================================================

-- Busca por jogo
CREATE INDEX idx_raca_jogo ON racas(jogo_id, ativo);

-- Ordenação
CREATE INDEX idx_raca_ordem ON racas(jogo_id, ordem_exibicao);

-- =============================================================================
-- Comentários
-- =============================================================================

COMMENT ON TABLE racas IS 'Raças de personagem configuradas pelo mestre (Humano, Elfo, etc)';
COMMENT ON COLUMN racas.nome IS 'Nome da raça (ex: Humano, Elfo, Anão)';
COMMENT ON COLUMN racas.descricao IS 'Descrição e características da raça';
COMMENT ON COLUMN racas.ordem_exibicao IS 'Ordem de exibição na interface';
COMMENT ON COLUMN racas.ativo IS 'Indica se a raça está ativa (soft delete)';
