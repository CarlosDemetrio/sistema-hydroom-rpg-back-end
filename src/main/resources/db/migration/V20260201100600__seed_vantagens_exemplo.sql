-- ============================================================================
-- Migration: Seed Configuration Data - Part 4 (Vantagens Exemplo)
-- Versão: V20260201100600
-- Descrição: Popula vantagens de exemplo para testes
-- Autor: Backend Team
-- Data: 2026-02-01
-- ============================================================================

-- ===========================================================================
-- SEED: configuracao_vantagens (Exemplos básicos)
-- ===========================================================================

-- Vantagens de Atributos
INSERT INTO configuracao_vantagens (
    nome, descricao, tipo_bonus, valor_bonus_formula,
    custo_base, formula_custo, nivel_minimo_personagem,
    pode_evoluir, nivel_maximo_vantagem, ativo
) VALUES
(
    'Fortitude',
    'Aumenta o atributo Vigor permanentemente',
    'ATRIBUTO_VIGOR',
    'nivel_vantagem * 2',
    3,
    'custo_base * nivel_vantagem',
    1,
    TRUE,
    10,
    TRUE
),
(
    'Força Aprimorada',
    'Aumenta o atributo Força permanentemente',
    'ATRIBUTO_FORCA',
    'nivel_vantagem * 2',
    3,
    'custo_base * nivel_vantagem',
    1,
    TRUE,
    10,
    TRUE
),
(
    'Agilidade Aprimorada',
    'Aumenta o atributo Agilidade permanentemente',
    'ATRIBUTO_AGILIDADE',
    'nivel_vantagem * 2',
    3,
    'custo_base * nivel_vantagem',
    1,
    TRUE,
    10,
    TRUE
);

-- Vantagens de Combate
INSERT INTO configuracao_vantagens (
    nome, descricao, tipo_bonus, valor_bonus_formula,
    custo_base, formula_custo, nivel_minimo_personagem,
    pode_evoluir, nivel_maximo_vantagem, ativo
) VALUES
(
    'Ataque Aprimorado',
    'Aumenta BBA (Bônus Base de Ataque) em +1 por nível da vantagem',
    'BBA',
    'nivel_vantagem * 1',
    2,
    'custo_base * nivel_vantagem',
    1,
    TRUE,
    NULL,
    TRUE
),
(
    'Defesa Mágica',
    'Aumenta BBM (Bônus Base Mágico) em +1 por nível da vantagem',
    'BBM',
    'nivel_vantagem * 1',
    2,
    'custo_base * nivel_vantagem',
    3,
    TRUE,
    NULL,
    TRUE
),
(
    'Golpe Crítico',
    'Aumenta chance de crítico em combate',
    'CRITICO',
    'nivel_vantagem * 5',
    4,
    'custo_base * nivel_vantagem',
    5,
    TRUE,
    5,
    TRUE
);

-- Vantagens de Vida/Essência
INSERT INTO configuracao_vantagens (
    nome, descricao, tipo_bonus, valor_bonus_formula,
    custo_base, formula_custo, nivel_minimo_personagem,
    pode_evoluir, nivel_maximo_vantagem, ativo
) VALUES
(
    'Vida Extra',
    'Aumenta pontos de vida em +5 por nível da vantagem',
    'VIDA',
    'nivel_vantagem * 5',
    1,
    'custo_base * nivel_vantagem',
    1,
    TRUE,
    NULL,
    TRUE
),
(
    'Essência Ampliada',
    'Aumenta pontos de essência/mana em +10 por nível da vantagem',
    'ESSENCIA',
    'nivel_vantagem * 10',
    2,
    'custo_base * nivel_vantagem',
    1,
    TRUE,
    NULL,
    TRUE
);

-- Vantagens Especiais (não evoluem)
INSERT INTO configuracao_vantagens (
    nome, descricao, tipo_bonus, valor_bonus_formula,
    custo_base, formula_custo, nivel_minimo_personagem,
    pode_evoluir, nivel_maximo_vantagem, ativo
) VALUES
(
    'Visão no Escuro',
    'Permite enxergar no escuro até 18 metros',
    'ESPECIAL',
    NULL,
    2,
    'custo_base',
    1,
    FALSE,
    1,
    TRUE
),
(
    'Resistência a Veneno',
    'Adiciona +4 em testes contra venenos',
    'ESPECIAL',
    NULL,
    3,
    'custo_base',
    3,
    FALSE,
    1,
    TRUE
),
(
    'Ambidestria',
    'Remove penalidades de usar armas com a mão inábil',
    'ESPECIAL',
    NULL,
    4,
    'custo_base',
    5,
    FALSE,
    1,
    TRUE
);

-- ===========================================================================
-- VERIFICAÇÃO (Opcional - comentar em produção)
-- ===========================================================================
-- SELECT nome, tipo_bonus, custo_base, pode_evoluir, nivel_maximo_vantagem
-- FROM configuracao_vantagens
-- WHERE ativo = TRUE
-- ORDER BY tipo_bonus, nome;
-- Expected: 11 vantagens cadastradas
