-- V1.4__alterar_descricao_jogos_para_text.sql
-- Altera o campo descrição de VARCHAR(1000) para TEXT

ALTER TABLE jogos
MODIFY COLUMN descricao TEXT COMMENT 'Descrição detalhada do jogo/campanha';
