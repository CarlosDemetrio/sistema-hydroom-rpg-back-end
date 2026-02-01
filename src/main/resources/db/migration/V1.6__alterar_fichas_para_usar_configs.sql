-- Altera tabela fichas para usar configurações ao invés de campos texto

-- Adiciona novas colunas com FK
ALTER TABLE fichas ADD COLUMN genero_id BIGINT;
ALTER TABLE fichas ADD COLUMN indole_id BIGINT;
ALTER TABLE fichas ADD COLUMN presenca_id BIGINT;

-- Cria FKs
ALTER TABLE fichas ADD CONSTRAINT fk_ficha_genero
    FOREIGN KEY (genero_id) REFERENCES generos_config(id);

ALTER TABLE fichas ADD CONSTRAINT fk_ficha_indole
    FOREIGN KEY (indole_id) REFERENCES indoles_config(id);

ALTER TABLE fichas ADD CONSTRAINT fk_ficha_presenca
    FOREIGN KEY (presenca_id) REFERENCES presencas_config(id);

-- Remove colunas antigas
ALTER TABLE fichas DROP COLUMN genero;
ALTER TABLE fichas DROP COLUMN indole;
ALTER TABLE fichas DROP COLUMN presenca;

COMMENT ON COLUMN fichas.genero_id IS 'Referência para configuração de gênero';
COMMENT ON COLUMN fichas.indole_id IS 'Referência para configuração de índole';
COMMENT ON COLUMN fichas.presenca_id IS 'Referência para configuração de presença';
