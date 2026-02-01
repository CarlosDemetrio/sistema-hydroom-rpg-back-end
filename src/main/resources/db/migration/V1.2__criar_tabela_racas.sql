-- V1.2__criar_tabela_racas.sql
-- Tabela para armazenar raças de personagem configuráveis pelo Mestre

CREATE TABLE racas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    jogo_id BIGINT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    ordem_exibicao INT NOT NULL DEFAULT 0,

    -- Campos de auditoria
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0,

    -- Constraints
    CONSTRAINT fk_raca_jogo FOREIGN KEY (jogo_id) REFERENCES jogos(id) ON DELETE CASCADE,
    CONSTRAINT uk_raca_jogo_nome UNIQUE (jogo_id, nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices para melhor performance
CREATE INDEX idx_raca_jogo_id ON racas(jogo_id);
CREATE INDEX idx_raca_ativo ON racas(ativo);
CREATE INDEX idx_raca_ordem ON racas(ordem_exibicao);

-- Comentários
ALTER TABLE racas COMMENT = 'Armazena as raças de personagem configuráveis pelo Mestre para cada jogo';
