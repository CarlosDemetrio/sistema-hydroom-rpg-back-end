-- V1.3__adicionar_imagem_url_em_jogos.sql
-- Adiciona campo para URL da imagem do jogo

ALTER TABLE jogos
ADD COLUMN imagem_url VARCHAR(2000) AFTER descricao;

-- Comentário
ALTER TABLE jogos MODIFY COLUMN imagem_url VARCHAR(2000) COMMENT 'URL da imagem de capa do jogo';
