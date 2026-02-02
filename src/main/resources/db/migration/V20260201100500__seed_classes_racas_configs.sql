-- ============================================================================
-- Migration: Seed Configuration Data - Part 3 (Classes, Raças e Outros)
-- Versão: V20260201100500
-- Descrição: Popula classes, raças, prospecção, gêneros e outros configs
-- Autor: Backend Team
-- Data: 2026-02-01
-- ============================================================================

-- ===========================================================================
-- SEED: configuracao_classes (12 classes padrão)
-- ===========================================================================
INSERT INTO configuracao_classes (nome, descricao, ordem_exibicao, ativo) VALUES
('Guerreiro', 'Especialista em combate corpo a corpo', 1, TRUE),
('Arqueiro', 'Mestre em combate à distância', 2, TRUE),
('Monge', 'Lutador desarmado com disciplina espiritual', 3, TRUE),
('Berserker', 'Guerreiro selvagem de fúria incontrolável', 4, TRUE),
('Assassino', 'Especialista em ataques furtivos e letais', 5, TRUE),
('Fauno (Herdeiro)', 'Herdeiro com poderes especiais', 6, TRUE),
('Mago', 'Conjurador de magias arcanas', 7, TRUE),
('Feiticeiro', 'Usuário de magia inata', 8, TRUE),
('Necromance', 'Manipulador de forças da morte', 9, TRUE),
('Sacerdote', 'Servo divino com poderes sagrados', 10, TRUE),
('Ladrão', 'Especialista em subterfúgio e furto', 11, TRUE),
('Negociante', 'Mestre em comércio e persuasão', 12, TRUE);

-- ===========================================================================
-- SEED: configuracao_racas (4 raças básicas)
-- ===========================================================================
INSERT INTO configuracao_racas (nome, descricao, ordem_exibicao, ativo) VALUES
('Humano', 'Raça versátil e adaptável', 1, TRUE),
('Elfo', 'Seres longevos com afinidade mágica', 2, TRUE),
('Anão', 'Raça resistente e trabalhadora', 3, TRUE),
('Meio-Elfo', 'Híbrido entre humano e elfo', 4, TRUE);

-- ===========================================================================
-- SEED: configuracao_prospeccao (6 tipos de dados)
-- ===========================================================================
INSERT INTO configuracao_prospeccao (nome, num_lados, ordem_exibicao, ativo) VALUES
('d3', 3, 1, TRUE),
('d4', 4, 2, TRUE),
('d6', 6, 3, TRUE),
('d8', 8, 4, TRUE),
('d10', 10, 5, TRUE),
('d12', 12, 6, TRUE);

-- ===========================================================================
-- SEED: configuracao_generos (Gêneros disponíveis)
-- ===========================================================================
INSERT INTO configuracao_generos (nome, ordem_exibicao, ativo) VALUES
('Masculino', 1, TRUE),
('Feminino', 2, TRUE),
('Não-Binário', 3, TRUE),
('Prefiro não informar', 4, TRUE);

-- ===========================================================================
-- SEED: configuracao_indoles (Alinhamentos)
-- ===========================================================================
INSERT INTO configuracao_indoles (nome, ordem_exibicao, ativo) VALUES
('Ordeiro Bondoso', 1, TRUE),
('Neutro Bondoso', 2, TRUE),
('Caótico Bondoso', 3, TRUE),
('Ordeiro Neutro', 4, TRUE),
('Neutro', 5, TRUE),
('Caótico Neutro', 6, TRUE),
('Ordeiro Maligno', 7, TRUE),
('Neutro Maligno', 8, TRUE),
('Caótico Maligno', 9, TRUE);

-- ===========================================================================
-- SEED: configuracao_presencas (Níveis de presença)
-- ===========================================================================
INSERT INTO configuracao_presencas (nome, ordem_exibicao, ativo) VALUES
('Insignificante', 1, TRUE),
('Fraco', 2, TRUE),
('Normal', 3, TRUE),
('Notável', 4, TRUE),
('Impressionante', 5, TRUE),
('Dominante', 6, TRUE);

-- ===========================================================================
-- SEED: configuracao_membros_corpo (Integridade física)
-- ===========================================================================
INSERT INTO configuracao_membros_corpo (nome, ordem_exibicao, ativo) VALUES
('Cabeça', 1, TRUE),
('Tronco', 2, TRUE),
('Braço Direito', 3, TRUE),
('Braço Esquerdo', 4, TRUE),
('Perna Direita', 5, TRUE),
('Perna Esquerda', 6, TRUE);

-- ===========================================================================
-- SEED: raca_bonus_atributos (Bônus raciais - Exemplos básicos)
-- ===========================================================================

-- Humano: Nenhum bônus específico (versatilidade)

-- Elfo: +2 Agilidade, -1 Vigor
INSERT INTO raca_bonus_atributos (raca_id, atributo_id, bonus)
SELECT r.id, a.id, 2
FROM configuracao_racas r, configuracao_atributos a
WHERE r.nome = 'Elfo' AND a.nome = 'Agilidade';

INSERT INTO raca_bonus_atributos (raca_id, atributo_id, bonus)
SELECT r.id, a.id, -1
FROM configuracao_racas r, configuracao_atributos a
WHERE r.nome = 'Elfo' AND a.nome = 'Vigor';

-- Anão: +2 Vigor, -1 Agilidade
INSERT INTO raca_bonus_atributos (raca_id, atributo_id, bonus)
SELECT r.id, a.id, 2
FROM configuracao_racas r, configuracao_atributos a
WHERE r.nome = 'Anão' AND a.nome = 'Vigor';

INSERT INTO raca_bonus_atributos (raca_id, atributo_id, bonus)
SELECT r.id, a.id, -1
FROM configuracao_racas r, configuracao_atributos a
WHERE r.nome = 'Anão' AND a.nome = 'Agilidade';

-- Meio-Elfo: +1 Agilidade, +1 Inteligência
INSERT INTO raca_bonus_atributos (raca_id, atributo_id, bonus)
SELECT r.id, a.id, 1
FROM configuracao_racas r, configuracao_atributos a
WHERE r.nome = 'Meio-Elfo' AND a.nome = 'Agilidade';

INSERT INTO raca_bonus_atributos (raca_id, atributo_id, bonus)
SELECT r.id, a.id, 1
FROM configuracao_racas r, configuracao_atributos a
WHERE r.nome = 'Meio-Elfo' AND a.nome = 'Inteligência';

-- ===========================================================================
-- VERIFICAÇÃO (Opcional - comentar em produção)
-- ===========================================================================
-- SELECT COUNT(*) as total_classes FROM configuracao_classes WHERE ativo = TRUE;
-- Expected: 12

-- SELECT COUNT(*) as total_racas FROM configuracao_racas WHERE ativo = TRUE;
-- Expected: 4

-- SELECT COUNT(*) as total_dados FROM configuracao_prospeccao WHERE ativo = TRUE;
-- Expected: 6

-- SELECT r.nome as raca, a.nome as atributo, rb.bonus
-- FROM raca_bonus_atributos rb
-- JOIN configuracao_racas r ON r.id = rb.raca_id
-- JOIN configuracao_atributos a ON a.id = rb.atributo_id
-- ORDER BY r.nome, a.nome;
-- Expected: Bônus raciais configurados
