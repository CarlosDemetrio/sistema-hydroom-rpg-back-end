-- Seeds para configurações de personalidade
-- Baseado no sistema legado

-- ===== GÊNEROS =====
-- Inserir para todos os jogos existentes
INSERT INTO generos_config (jogo_id, nome, descricao, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Masculino' as nome,
    'Personagem do gênero masculino' as descricao,
    true as ativo,
    1 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO generos_config (jogo_id, nome, descricao, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Feminino' as nome,
    'Personagem do gênero feminino' as descricao,
    true as ativo,
    2 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO generos_config (jogo_id, nome, descricao, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Outro' as nome,
    'Outros gêneros ou não-binário' as descricao,
    true as ativo,
    3 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

-- ===== ÍNDOLES =====
INSERT INTO indoles_config (jogo_id, nome, descricao, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Bom' as nome,
    'Personagem de índole bondosa' as descricao,
    true as ativo,
    1 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO indoles_config (jogo_id, nome, descricao, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Mau' as nome,
    'Personagem de índole maligna' as descricao,
    true as ativo,
    2 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO indoles_config (jogo_id, nome, descricao, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Neutro' as nome,
    'Personagem de índole neutra' as descricao,
    true as ativo,
    3 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

-- ===== PRESENÇAS =====
INSERT INTO presencas_config (jogo_id, nome, descricao, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Leal' as nome,
    'Segue códigos e leis rigorosamente' as descricao,
    true as ativo,
    1 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO presencas_config (jogo_id, nome, descricao, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Caótico' as nome,
    'Age com liberdade e imprevisibilidade' as descricao,
    true as ativo,
    2 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO presencas_config (jogo_id, nome, descricao, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Neutro' as nome,
    'Não tende nem para ordem nem para caos' as descricao,
    true as ativo,
    3 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO presencas_config (jogo_id, nome, descricao, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Bom' as nome,
    'Presença bondosa e altruísta' as descricao,
    true as ativo,
    4 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

-- ===== NÍVEIS (Tabela de XP do sistema legado) =====
-- Inserir tabela de progressão de XP para cada jogo
DO $$
DECLARE
    jogo_record RECORD;
    niveis_data JSON := '[
        {"nivel": 1, "xp": 1000, "limitador": 10, "pontos": 3},
        {"nivel": 2, "xp": 3000, "limitador": 50, "pontos": 3},
        {"nivel": 3, "xp": 6000, "limitador": 50, "pontos": 3},
        {"nivel": 4, "xp": 10000, "limitador": 50, "pontos": 3},
        {"nivel": 5, "xp": 15000, "limitador": 50, "pontos": 3},
        {"nivel": 6, "xp": 21000, "limitador": 50, "pontos": 3},
        {"nivel": 7, "xp": 28000, "limitador": 50, "pontos": 3},
        {"nivel": 8, "xp": 36000, "limitador": 50, "pontos": 3},
        {"nivel": 9, "xp": 45000, "limitador": 50, "pontos": 3},
        {"nivel": 10, "xp": 55000, "limitador": 50, "pontos": 3},
        {"nivel": 11, "xp": 66000, "limitador": 50, "pontos": 3},
        {"nivel": 12, "xp": 78000, "limitador": 50, "pontos": 3},
        {"nivel": 13, "xp": 91000, "limitador": 50, "pontos": 3},
        {"nivel": 14, "xp": 105000, "limitador": 50, "pontos": 3},
        {"nivel": 15, "xp": 120000, "limitador": 50, "pontos": 3},
        {"nivel": 16, "xp": 136000, "limitador": 50, "pontos": 3},
        {"nivel": 17, "xp": 153000, "limitador": 50, "pontos": 3},
        {"nivel": 18, "xp": 171000, "limitador": 50, "pontos": 3},
        {"nivel": 19, "xp": 190000, "limitador": 50, "pontos": 3},
        {"nivel": 20, "xp": 210000, "limitador": 50, "pontos": 3},
        {"nivel": 21, "xp": 231000, "limitador": 75, "pontos": 3},
        {"nivel": 22, "xp": 253000, "limitador": 75, "pontos": 3},
        {"nivel": 23, "xp": 276000, "limitador": 75, "pontos": 3},
        {"nivel": 24, "xp": 300000, "limitador": 75, "pontos": 3},
        {"nivel": 25, "xp": 325000, "limitador": 75, "pontos": 3},
        {"nivel": 26, "xp": 351000, "limitador": 100, "pontos": 3},
        {"nivel": 27, "xp": 378000, "limitador": 100, "pontos": 3},
        {"nivel": 28, "xp": 406000, "limitador": 100, "pontos": 3},
        {"nivel": 29, "xp": 435000, "limitador": 100, "pontos": 3},
        {"nivel": 30, "xp": 465000, "limitador": 100, "pontos": 3},
        {"nivel": 31, "xp": 496000, "limitador": 120, "pontos": 3},
        {"nivel": 32, "xp": 528000, "limitador": 120, "pontos": 3},
        {"nivel": 33, "xp": 561000, "limitador": 120, "pontos": 3},
        {"nivel": 34, "xp": 595000, "limitador": 120, "pontos": 3},
        {"nivel": 35, "xp": 630000, "limitador": 120, "pontos": 3}
    ]'::JSON;
    nivel_item JSON;
BEGIN
    FOR jogo_record IN SELECT id FROM jogos LOOP
        FOR nivel_item IN SELECT * FROM json_array_elements(niveis_data) LOOP
            INSERT INTO niveis_config (
                jogo_id,
                nivel,
                xp_necessaria,
                pontos_atributo,
                limitador_atributo,
                created_at,
                updated_at
            ) VALUES (
                jogo_record.id,
                (nivel_item->>'nivel')::INT,
                (nivel_item->>'xp')::BIGINT,
                (nivel_item->>'pontos')::INT,
                (nivel_item->>'limitador')::INT,
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            );
        END LOOP;
    END LOOP;
END $$;

COMMENT ON TABLE generos_config IS 'Seed completo - Masculino, Feminino, Outro';
COMMENT ON TABLE indoles_config IS 'Seed completo - Bom, Mau, Neutro';
COMMENT ON TABLE presencas_config IS 'Seed completo - Leal, Caótico, Neutro, Bom';
COMMENT ON TABLE niveis_config IS 'Seed completo - 35 níveis com tabela de XP do sistema legado';
