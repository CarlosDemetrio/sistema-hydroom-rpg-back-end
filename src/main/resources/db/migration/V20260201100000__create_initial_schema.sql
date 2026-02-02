-- ============================================================================
-- Migration: Create Initial Schema
-- Versão: V20260201100000
-- Descrição: Cria todas as tabelas base do sistema (usuarios, jogos, fichas)
-- Autor: Backend Team
-- Data: 2026-02-01
-- ============================================================================

-- ===========================================================================
-- TABELA: usuarios
-- Descrição: Armazena usuários autenticados via OAuth2
-- ===========================================================================
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nome VARCHAR(200) NOT NULL,
    imagem_url VARCHAR(500),
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(255) NOT NULL UNIQUE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_provider_id ON usuarios(provider_id);
CREATE INDEX idx_usuarios_ativo ON usuarios(ativo);

-- ===========================================================================
-- TABELA: jogos
-- Descrição: Representa um jogo/campanha de RPG
-- ===========================================================================
CREATE TABLE jogos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    descricao TEXT,
    imagem_url VARCHAR(500),
    data_inicio DATE,
    data_fim DATE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_jogos_ativo ON jogos(ativo);

-- ===========================================================================
-- TABELA: jogo_participantes
-- Descrição: Relacionamento N:N entre jogos e usuários (com papel)
-- ===========================================================================
CREATE TABLE jogo_participantes (
    jogo_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    papel VARCHAR(20) NOT NULL CHECK (papel IN ('MESTRE', 'JOGADOR')),
    PRIMARY KEY (jogo_id, usuario_id),
    FOREIGN KEY (jogo_id) REFERENCES jogos(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_jogo_participantes_jogo ON jogo_participantes(jogo_id);
CREATE INDEX idx_jogo_participantes_usuario ON jogo_participantes(usuario_id);
CREATE INDEX idx_jogo_participantes_papel ON jogo_participantes(papel);

-- ===========================================================================
-- TABELA: fichas
-- Descrição: Ficha de personagem de um jogador em um jogo específico
-- ===========================================================================
CREATE TABLE fichas (
    id BIGSERIAL PRIMARY KEY,
    jogo_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(200) NOT NULL,
    nivel INT NOT NULL DEFAULT 0,
    experiencia BIGINT NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (jogo_id) REFERENCES jogos(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_fichas_jogo ON fichas(jogo_id);
CREATE INDEX idx_fichas_usuario ON fichas(usuario_id);
CREATE INDEX idx_fichas_ativo ON fichas(ativo);

-- ===========================================================================
-- ROLLBACK MANUAL (se necessário):
-- ===========================================================================
-- DROP TABLE IF EXISTS fichas CASCADE;
-- DROP TABLE IF EXISTS jogo_participantes CASCADE;
-- DROP TABLE IF EXISTS jogos CASCADE;
-- DROP TABLE IF EXISTS usuarios CASCADE;
