-- Seeds para configurações de vantagens
-- Baseado no sistema legado

-- ===== CATEGORIAS DE VANTAGENS =====
INSERT INTO categorias_vantagem (jogo_id, nome, descricao, ativo, ordem, cor, created_at, updated_at)
SELECT
    id as jogo_id,
    'Atributo' as nome,
    'Vantagens que aumentam atributos permanentemente' as descricao,
    true as ativo,
    1 as ordem,
    '#FF5733' as cor,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO categorias_vantagem (jogo_id, nome, descricao, ativo, ordem, cor, created_at, updated_at)
SELECT
    id as jogo_id,
    'Combate' as nome,
    'Vantagens relacionadas a combate físico' as descricao,
    true as ativo,
    2 as ordem,
    '#C70039' as cor,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO categorias_vantagem (jogo_id, nome, descricao, ativo, ordem, cor, created_at, updated_at)
SELECT
    id as jogo_id,
    'Magia' as nome,
    'Vantagens mágicas e sobrenaturais' as descricao,
    true as ativo,
    3 as ordem,
    '#9C27B0' as cor,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO categorias_vantagem (jogo_id, nome, descricao, ativo, ordem, cor, created_at, updated_at)
SELECT
    id as jogo_id,
    'Social' as nome,
    'Vantagens de interação social e diplomacia' as descricao,
    true as ativo,
    4 as ordem,
    '#2196F3' as cor,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO categorias_vantagem (jogo_id, nome, descricao, ativo, ordem, cor, created_at, updated_at)
SELECT
    id as jogo_id,
    'Técnica' as nome,
    'Vantagens de perícia e habilidades técnicas' as descricao,
    true as ativo,
    5 as ordem,
    '#4CAF50' as cor,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO categorias_vantagem (jogo_id, nome, descricao, ativo, ordem, cor, created_at, updated_at)
SELECT
    id as jogo_id,
    'Sobrenatural' as nome,
    'Vantagens sobrenaturais e extraordinárias' as descricao,
    true as ativo,
    6 as ordem,
    '#FF9800' as cor,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

-- ===== PONTOS DE VANTAGEM POR NÍVEL =====
-- Sistema legado: 1 ponto de vantagem por nível
DO $$
DECLARE
    jogo_record RECORD;
    nivel_atual INT;
BEGIN
    FOR jogo_record IN SELECT id FROM jogos LOOP
        FOR nivel_atual IN 1..35 LOOP
            INSERT INTO pontos_vantagem_config (
                jogo_id,
                nivel,
                pontos_ganhos,
                created_at,
                updated_at
            ) VALUES (
                jogo_record.id,
                nivel_atual,
                1, -- 1 ponto por nível (padrão do sistema legado)
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            );
        END LOOP;
    END LOOP;
END $$;

COMMENT ON TABLE categorias_vantagem IS 'Seed completo - 6 categorias: Atributo, Combate, Magia, Social, Técnica, Sobrenatural';
COMMENT ON TABLE pontos_vantagem_config IS 'Seed completo - 1 ponto por nível (níveis 1-35)';
