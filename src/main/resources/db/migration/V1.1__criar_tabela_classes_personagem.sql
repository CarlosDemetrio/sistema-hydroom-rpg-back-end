-- V1.1__criar_tabela_classes_personagem.sql
-- Tabela para armazenar classes de personagem configuráveis pelo Mestre

CREATE TABLE classes_personagem (
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
    CONSTRAINT fk_classe_jogo FOREIGN KEY (jogo_id) REFERENCES jogos(id) ON DELETE CASCADE,
    CONSTRAINT uk_classe_jogo_nome UNIQUE (jogo_id, nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices para melhor performance
CREATE INDEX idx_classe_jogo_id ON classes_personagem(jogo_id);
CREATE INDEX idx_classe_ativo ON classes_personagem(ativo);
CREATE INDEX idx_classe_ordem ON classes_personagem(ordem_exibicao);

-- Comentários
ALTER TABLE classes_personagem COMMENT = 'Armazena as classes de personagem configuráveis pelo Mestre para cada jogo';
