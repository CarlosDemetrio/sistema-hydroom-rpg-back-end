-- =============================================================================
-- Migration: V1_2 - Criação da tabela CLASSES_PERSONAGEM
-- Description: Tabela de configuração de classes de personagem
-- Author: System
-- Date: 2026-02-01
-- =============================================================================

CREATE TABLE classes_personagem (
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
    CONSTRAINT fk_classe_jogo FOREIGN KEY (jogo_id)
        REFERENCES jogos(id) ON DELETE CASCADE,

    -- Unique Constraints
    CONSTRAINT uk_classe_jogo_nome UNIQUE (jogo_id, nome)
);

-- =============================================================================
-- Índices
-- =============================================================================

-- Busca por jogo
CREATE INDEX idx_classe_jogo ON classes_personagem(jogo_id, ativo);

-- Ordenação
CREATE INDEX idx_classe_ordem ON classes_personagem(jogo_id, ordem_exibicao);

-- =============================================================================
-- Comentários
-- =============================================================================

COMMENT ON TABLE classes_personagem IS 'Classes de personagem configuradas pelo mestre (Guerreiro, Mago, etc)';
COMMENT ON COLUMN classes_personagem.nome IS 'Nome da classe (ex: Guerreiro, Mago, Ladrão)';
COMMENT ON COLUMN classes_personagem.descricao IS 'Descrição e características da classe';
COMMENT ON COLUMN classes_personagem.ordem_exibicao IS 'Ordem de exibição na interface';
COMMENT ON COLUMN classes_personagem.ativo IS 'Indica se a classe está ativa (soft delete)';
