-- ============================================================================
-- Migration: Seed Configuration Data - Part 2 (Níveis e Limitadores)
-- Versão: V20260201100400
-- Descrição: Popula tabelas de XP por nível e limitadores de atributos
-- Autor: Backend Team
-- Data: 2026-02-01
-- ============================================================================

-- ===========================================================================
-- SEED: configuracao_niveis (Nível 0 até 35)
-- ===========================================================================
INSERT INTO configuracao_niveis (nivel, experiencia_necessaria, pontos_atributo, pontos_vantagem, ativo) VALUES
(0, 0, 0, 0, TRUE),
(1, 1000, 3, 1, TRUE),
(2, 3000, 3, 1, TRUE),
(3, 6000, 3, 1, TRUE),
(4, 10000, 3, 1, TRUE),
(5, 15000, 3, 1, TRUE),
(6, 21000, 3, 1, TRUE),
(7, 28000, 3, 1, TRUE),
(8, 36000, 3, 1, TRUE),
(9, 45000, 3, 1, TRUE),
(10, 55000, 3, 1, TRUE),
(11, 66000, 3, 1, TRUE),
(12, 78000, 3, 1, TRUE),
(13, 91000, 3, 1, TRUE),
(14, 105000, 3, 1, TRUE),
(15, 120000, 3, 1, TRUE),
(16, 136000, 3, 1, TRUE),
(17, 153000, 3, 1, TRUE),
(18, 171000, 3, 1, TRUE),
(19, 190000, 3, 1, TRUE),
(20, 210000, 3, 1, TRUE),
(21, 231000, 3, 1, TRUE),
(22, 253000, 3, 1, TRUE),
(23, 276000, 3, 1, TRUE),
(24, 300000, 3, 1, TRUE),
(25, 325000, 3, 1, TRUE),
(26, 351000, 3, 1, TRUE),
(27, 378000, 3, 1, TRUE),
(28, 406000, 3, 1, TRUE),
(29, 435000, 3, 1, TRUE),
(30, 465000, 3, 1, TRUE),
(31, 496000, 3, 1, TRUE),
(32, 528000, 3, 1, TRUE),
(33, 561000, 3, 1, TRUE),
(34, 595000, 3, 1, TRUE),
(35, 595000, 3, 1, TRUE);

-- ===========================================================================
-- SEED: configuracao_limitadores (5 faixas de nível)
-- ===========================================================================
INSERT INTO configuracao_limitadores (nivel_inicio, nivel_fim, limite_atributo, ativo) VALUES
(0, 1, 10, TRUE),
(2, 20, 50, TRUE),
(21, 25, 75, TRUE),
(26, 30, 100, TRUE),
(31, 35, 120, TRUE);

-- ===========================================================================
-- VERIFICAÇÃO (Opcional - comentar em produção)
-- ===========================================================================
-- SELECT COUNT(*) as total_niveis FROM configuracao_niveis WHERE ativo = TRUE;
-- Expected: 36 (0 a 35)

-- SELECT nivel_inicio, nivel_fim, limite_atributo
-- FROM configuracao_limitadores
-- WHERE ativo = TRUE
-- ORDER BY nivel_inicio;
-- Expected: 5 rows
