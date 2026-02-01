-- ========================================
-- Migração V1: Criação de Tabelas Base
-- Sistema: Klayrah RPG - Ficha Controlador
-- Data: 2026-02-01
-- Descrição: Tabelas de usuários e jogos
-- ========================================

-- ========================================
-- TABELA: usuarios
-- Descrição: Armazena dados dos usuários autenticados via OAuth2
-- ========================================
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL,
    imagem_url VARCHAR(500),
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(255) NOT NULL UNIQUE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para melhor performance
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_provider_id ON usuarios(provider_id);
CREATE INDEX idx_usuarios_ativo ON usuarios(ativo) WHERE ativo = TRUE;

-- Comentários
COMMENT ON TABLE usuarios IS 'Usuários autenticados via OAuth2 (Google)';
COMMENT ON COLUMN usuarios.id IS 'Identificador único do usuário';
COMMENT ON COLUMN usuarios.email IS 'Email do usuário (único)';
COMMENT ON COLUMN usuarios.nome IS 'Nome completo do usuário';
COMMENT ON COLUMN usuarios.imagem_url IS 'URL da foto de perfil (Google)';
COMMENT ON COLUMN usuarios.provider IS 'Provider OAuth2 (GOOGLE, etc)';
COMMENT ON COLUMN usuarios.provider_id IS 'ID único do provider (sub)';
COMMENT ON COLUMN usuarios.ativo IS 'Flag de usuário ativo';
COMMENT ON COLUMN usuarios.criado_em IS 'Data/hora de criação do registro';
COMMENT ON COLUMN usuarios.atualizado_em IS 'Data/hora da última atualização';

-- ========================================
-- FIM DA MIGRAÇÃO V1
-- ========================================
