-- V2__criar_tabela_fichas.sql
-- Tabela para armazenar fichas de personagem de RPG
-- SEM COLUNAS JSON - Totalmente normalizado

CREATE TABLE fichas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    jogo_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    classe_personagem_id BIGINT,
    raca_id BIGINT,

    -- Dados básicos do personagem
    nome_personagem VARCHAR(100) NOT NULL,
    jogador_nome VARCHAR(100),
    titulo_heroico VARCHAR(200),
    insolitus VARCHAR(200),
    origem VARCHAR(200),
    genero VARCHAR(20),
    arquetipo_referencia VARCHAR(200),

    -- Aparência física
    idade INT,
    altura_cm INT,
    peso_kg DECIMAL(5,2),
    cor_cabelo VARCHAR(50),
    tamanho_cabelo VARCHAR(50),
    cor_olhos VARCHAR(50),

    -- Personalidade (campos texto livre - configurável)
    indole VARCHAR(50),
    presenca VARCHAR(50),

    -- Progressão
    nivel INT NOT NULL DEFAULT 1,
    experiencia BIGINT NOT NULL DEFAULT 0,
    renascimentos INT NOT NULL DEFAULT 0,

    -- Campos de controle
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    compartilhada_com_jogadores BOOLEAN NOT NULL DEFAULT FALSE,

    -- Campos de auditoria
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    version INT NOT NULL DEFAULT 0,

    -- Constraints
    CONSTRAINT fk_ficha_jogo FOREIGN KEY (jogo_id) REFERENCES jogos(id) ON DELETE CASCADE,
    CONSTRAINT fk_ficha_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_ficha_classe FOREIGN KEY (classe_personagem_id) REFERENCES classes_personagem(id) ON DELETE SET NULL,
    CONSTRAINT fk_ficha_raca FOREIGN KEY (raca_id) REFERENCES racas(id) ON DELETE SET NULL,
    CONSTRAINT chk_ficha_nivel CHECK (nivel >= 1 AND nivel <= 99),
    CONSTRAINT chk_ficha_experiencia CHECK (experiencia >= 0),
    CONSTRAINT chk_ficha_renascimentos CHECK (renascimentos >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices para melhor performance
CREATE INDEX idx_ficha_jogo_id ON fichas(jogo_id);
CREATE INDEX idx_ficha_usuario_id ON fichas(usuario_id);
CREATE INDEX idx_ficha_classe_id ON fichas(classe_personagem_id);
CREATE INDEX idx_ficha_raca_id ON fichas(raca_id);
CREATE INDEX idx_ficha_ativo ON fichas(ativo);
CREATE INDEX idx_ficha_nivel ON fichas(nivel);
CREATE INDEX idx_ficha_nome_personagem ON fichas(nome_personagem);

-- Índice composto para buscar fichas ativas de um jogador em um jogo
CREATE INDEX idx_ficha_jogo_usuario_ativo ON fichas(jogo_id, usuario_id, ativo);

-- Comentários
ALTER TABLE fichas COMMENT = 'Armazena as fichas de personagem dos jogadores - totalmente normalizado sem JSON';
