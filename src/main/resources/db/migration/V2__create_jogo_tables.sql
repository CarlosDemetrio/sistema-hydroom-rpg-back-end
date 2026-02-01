-- Migration V2: Criar tabelas de Jogo e Participantes
-- Author: Carlos Demétrio
-- Date: 2026-02-01

-- Tabela: jogos
CREATE TABLE jogos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    descricao VARCHAR(1000),
    data_inicio DATE,
    data_fim DATE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para jogos
CREATE INDEX idx_jogos_ativo ON jogos(ativo);
CREATE INDEX idx_jogos_data_inicio ON jogos(data_inicio);

-- Comentários
COMMENT ON TABLE jogos IS 'Jogos/Campanhas de RPG';
COMMENT ON COLUMN jogos.nome IS 'Nome do jogo/campanha';
COMMENT ON COLUMN jogos.descricao IS 'Descrição do jogo';
COMMENT ON COLUMN jogos.data_inicio IS 'Data de início do jogo';
COMMENT ON COLUMN jogos.data_fim IS 'Data de encerramento do jogo';
COMMENT ON COLUMN jogos.ativo IS 'Indica se o jogo está ativo (soft delete)';

-- Tabela: jogo_participantes
CREATE TABLE jogo_participantes (
    id BIGSERIAL PRIMARY KEY,
    jogo_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'JOGADOR',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_jogo_participante_jogo FOREIGN KEY (jogo_id)
        REFERENCES jogos(id) ON DELETE CASCADE,
    CONSTRAINT fk_jogo_participante_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT uk_jogo_usuario UNIQUE (jogo_id, usuario_id),
    CONSTRAINT chk_role CHECK (role IN ('MESTRE', 'JOGADOR'))
);

-- Índices para jogo_participantes
CREATE INDEX idx_jogo_participantes_jogo ON jogo_participantes(jogo_id);
CREATE INDEX idx_jogo_participantes_usuario ON jogo_participantes(usuario_id);
CREATE INDEX idx_jogo_participantes_role ON jogo_participantes(role);
CREATE INDEX idx_jogo_participantes_ativo ON jogo_participantes(ativo);

-- Comentários
COMMENT ON TABLE jogo_participantes IS 'Participantes de um jogo (Mestre + Jogadores)';
COMMENT ON COLUMN jogo_participantes.role IS 'Papel do usuário no jogo (MESTRE ou JOGADOR)';
COMMENT ON COLUMN jogo_participantes.ativo IS 'Indica se o participante está ativo no jogo';
COMMENT ON CONSTRAINT uk_jogo_usuario ON jogo_participantes IS 'Um usuário só pode participar uma vez de cada jogo';
