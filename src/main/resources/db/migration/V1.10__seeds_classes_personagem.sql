-- Seeds para classes de personagem
-- Baseado no sistema legado: 12 classes base

INSERT INTO classes_personagem (jogo_id, nome, descricao, nivel_minimo, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Guerreiro' as nome,
    'Especialista em combate corpo a corpo' as descricao,
    1 as nivel_minimo,
    true as ativo,
    1 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO classes_personagem (jogo_id, nome, descricao, nivel_minimo, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Arqueiro' as nome,
    'Mestre em armas de longo alcance' as descricao,
    1 as nivel_minimo,
    true as ativo,
    2 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO classes_personagem (jogo_id, nome, descricao, nivel_minimo, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Monge' as nome,
    'Lutador marcial com disciplina mental' as descricao,
    1 as nivel_minimo,
    true as ativo,
    3 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO classes_personagem (jogo_id, nome, descricao, nivel_minimo, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Berserker' as nome,
    'Guerreiro selvagem movido pela fúria' as descricao,
    1 as nivel_minimo,
    true as ativo,
    4 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO classes_personagem (jogo_id, nome, descricao, nivel_minimo, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Assassino' as nome,
    'Especialista em ataques furtivos' as descricao,
    1 as nivel_minimo,
    true as ativo,
    5 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO classes_personagem (jogo_id, nome, descricao, nivel_minimo, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Fauno (Herdeiro)' as nome,
    'Criatura mística ligada à natureza' as descricao,
    1 as nivel_minimo,
    true as ativo,
    6 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO classes_personagem (jogo_id, nome, descricao, nivel_minimo, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Mago' as nome,
    'Conjurador de magias arcanas' as descricao,
    1 as nivel_minimo,
    true as ativo,
    7 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO classes_personagem (jogo_id, nome, descricao, nivel_minimo, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Feiticeiro' as nome,
    'Usuário de magia inata' as descricao,
    1 as nivel_minimo,
    true as ativo,
    8 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO classes_personagem (jogo_id, nome, descricao, nivel_minimo, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Necromante' as nome,
    'Mestre das artes da morte' as descricao,
    1 as nivel_minimo,
    true as ativo,
    9 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO classes_personagem (jogo_id, nome, descricao, nivel_minimo, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Sacerdote' as nome,
    'Canalizador de poder divino' as descricao,
    1 as nivel_minimo,
    true as ativo,
    10 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO classes_personagem (jogo_id, nome, descricao, nivel_minimo, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Ladrão' as nome,
    'Especialista em furtos e trapaças' as descricao,
    1 as nivel_minimo,
    true as ativo,
    11 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

INSERT INTO classes_personagem (jogo_id, nome, descricao, nivel_minimo, ativo, ordem, created_at, updated_at)
SELECT
    id as jogo_id,
    'Negociante' as nome,
    'Mestre em comércio e persuasão' as descricao,
    1 as nivel_minimo,
    true as ativo,
    12 as ordem,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM jogos;

COMMENT ON TABLE classes_personagem IS 'Seed completo - 12 classes: Guerreiro, Arqueiro, Monge, Berserker, Assassino, Fauno, Mago, Feiticeiro, Necromante, Sacerdote, Ladrão, Negociante';
