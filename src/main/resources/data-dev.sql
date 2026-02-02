-- ========================================
-- SEED DATA - IDEMPOTENT (Safe for Production)
-- ========================================
-- Usa INSERT ON CONFLICT DO NOTHING
-- Pode executar multiplas vezes sem duplicar
-- ========================================

-- ========================================
-- USUARIO DE TESTE
-- ========================================
INSERT INTO usuarios (id, nome, email, provider, provider_id, ativo, role, created_at, updated_at)
VALUES (1, 'Admin Teste', 'admin@teste.com', 'LOCAL', 'admin-local-1', true, 'MESTRE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- ========================================
-- JOGO DE TESTE
-- ========================================
INSERT INTO jogos (id, nome, descricao, ativo, created_at, updated_at)
VALUES (1, 'Campanha Padrao', 'Jogo com configuracoes padrao', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- JOGO PARTICIPANTE
-- ========================================
INSERT INTO jogo_participantes (id, jogo_id, usuario_id, role, ativo, created_at, updated_at)
VALUES (1, 1, 1, 'MESTRE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- ATRIBUTOS
-- ========================================
INSERT INTO atributo_config (id, jogo_id, nome, descricao, ordem_exibicao, formula_impeto, ativo, created_at, updated_at) VALUES
(1, 1, 'Forca', 'Representa poder fisico', 1, 'FOR', true, NOW(), NOW()),
(2, 1, 'Destreza', 'Agilidade e reflexos', 2, 'DES', true, NOW(), NOW()),
(3, 1, 'Constituicao', 'Resistencia fisica', 3, 'CON', true, NOW(), NOW()),
(4, 1, 'Inteligencia', 'Capacidade mental', 4, 'INT', true, NOW(), NOW()),
(5, 1, 'Sabedoria', 'Percepcao e intuicao', 5, 'SAB', true, NOW(), NOW()),
(6, 1, 'Carisma', 'Forca de personalidade', 6, 'CAR', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- TIPOS DE APTIDAO
-- ========================================
INSERT INTO tipo_aptidao (id, jogo_id, nome, descricao, ordem_exibicao, ativo, created_at, updated_at) VALUES
(1, 1, 'Fisica', 'Aptidoes fisicas', 1, true, NOW(), NOW()),
(2, 1, 'Mental', 'Aptidoes mentais', 2, true, NOW(), NOW()),
(3, 1, 'Social', 'Aptidoes sociais', 3, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- APTIDOES
-- ========================================
INSERT INTO aptidao_config (id, jogo_id, nome, descricao, tipo_aptidao_id, ordem_exibicao, ativo, created_at, updated_at) VALUES
(1, 1, 'Acrobacia', 'Movimentos acrobaticos', 1, 1, true, NOW(), NOW()),
(2, 1, 'Atletismo', 'Forca fisica', 1, 2, true, NOW(), NOW()),
(3, 1, 'Furtividade', 'Mover-se sem ser detectado', 1, 3, true, NOW(), NOW()),
(4, 1, 'Arcanismo', 'Conhecimento magico', 2, 4, true, NOW(), NOW()),
(5, 1, 'Historia', 'Conhecimento historico', 2, 5, true, NOW(), NOW()),
(6, 1, 'Investigacao', 'Analise de pistas', 2, 6, true, NOW(), NOW()),
(7, 1, 'Enganacao', 'Mentir e blefar', 3, 7, true, NOW(), NOW()),
(8, 1, 'Intimidacao', 'Coagir pelo medo', 3, 8, true, NOW(), NOW()),
(9, 1, 'Persuasao', 'Convencer por argumentos', 3, 9, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- CLASSES
-- ========================================
INSERT INTO classes_personagem (id, jogo_id, nome, descricao, ordem_exibicao, ativo, created_at, updated_at) VALUES
(1, 1, 'Guerreiro', 'Especialista em combate', 1, true, NOW(), NOW()),
(2, 1, 'Mago', 'Manipulador de magia', 2, true, NOW(), NOW()),
(3, 1, 'Ladino', 'Especialista em furtividade', 3, true, NOW(), NOW()),
(4, 1, 'Clerigo', 'Canal divino', 4, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- RACAS
-- ========================================
INSERT INTO racas (id, jogo_id, nome, descricao, ordem_exibicao, ativo, created_at, updated_at) VALUES
(1, 1, 'Humano', 'Versateis', 1, true, NOW(), NOW()),
(2, 1, 'Elfo', 'Longevos', 2, true, NOW(), NOW()),
(3, 1, 'Anao', 'Resistentes', 3, true, NOW(), NOW()),
(4, 1, 'Halfling', 'Pequenos e ageis', 4, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- GENEROS
-- ========================================
INSERT INTO generos_config (id, jogo_id, nome, descricao, ordem, ativo, created_at, updated_at) VALUES
(1, 1, 'Masculino', 'Genero masculino', 1, true, NOW(), NOW()),
(2, 1, 'Feminino', 'Genero feminino', 2, true, NOW(), NOW()),
(3, 1, 'Nao-binario', 'Genero nao-binario', 3, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- INDOLES
-- ========================================
INSERT INTO indoles_config (id, jogo_id, nome, descricao, ordem, ativo, created_at, updated_at) VALUES
(1, 1, 'Bom', 'Tende ao bem', 1, true, NOW(), NOW()),
(2, 1, 'Neutro', 'Equilibrado', 2, true, NOW(), NOW()),
(3, 1, 'Mau', 'Tende ao mal', 3, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- PRESENCAS
-- ========================================
INSERT INTO presencas_config (id, jogo_id, nome, descricao, ordem, ativo, created_at, updated_at) VALUES
(1, 1, 'Leal', 'Segue regras', 1, true, NOW(), NOW()),
(2, 1, 'Neutro', 'Pragmatico', 2, true, NOW(), NOW()),
(3, 1, 'Caotico', 'Valoriza liberdade', 3, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- NIVEIS
-- ========================================
INSERT INTO niveis_config (id, jogo_id, nivel, xp_necessaria, pontos_atributo, limitador_atributo, ativo, created_at, updated_at) VALUES
(1, 1, 1, 0, 3, 18, true, NOW(), NOW()),
(2, 1, 2, 300, 3, 18, true, NOW(), NOW()),
(3, 1, 3, 900, 3, 18, true, NOW(), NOW()),
(4, 1, 4, 2700, 3, 19, true, NOW(), NOW()),
(5, 1, 5, 6500, 3, 19, true, NOW(), NOW()),
(6, 1, 6, 14000, 3, 20, true, NOW(), NOW()),
(7, 1, 7, 23000, 3, 20, true, NOW(), NOW()),
(8, 1, 8, 34000, 3, 20, true, NOW(), NOW()),
(9, 1, 9, 48000, 3, 20, true, NOW(), NOW()),
(10, 1, 10, 64000, 3, 20, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- MEMBROS DO CORPO
-- ========================================
INSERT INTO membro_corpo_config (id, jogo_id, nome, porcentagem_vida, ordem_exibicao, ativo, created_at, updated_at) VALUES
(1, 1, 'Cabeca', 0.15, 1, true, NOW(), NOW()),
(2, 1, 'Torso', 0.40, 2, true, NOW(), NOW()),
(3, 1, 'Braco Direito', 0.10, 3, true, NOW(), NOW()),
(4, 1, 'Braco Esquerdo', 0.10, 4, true, NOW(), NOW()),
(5, 1, 'Perna Direita', 0.125, 5, true, NOW(), NOW()),
(6, 1, 'Perna Esquerda', 0.125, 6, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- DADOS DE PROSPECCAO
-- ========================================
INSERT INTO dado_prospeccao_config (id, jogo_id, nome, descricao, numero_faces, ordem_exibicao, ativo, created_at, updated_at) VALUES
(1, 1, 'd4', 'Dado de 4 faces', 4, 1, true, NOW(), NOW()),
(2, 1, 'd6', 'Dado de 6 faces', 6, 2, true, NOW(), NOW()),
(3, 1, 'd8', 'Dado de 8 faces', 8, 3, true, NOW(), NOW()),
(4, 1, 'd10', 'Dado de 10 faces', 10, 4, true, NOW(), NOW()),
(5, 1, 'd12', 'Dado de 12 faces', 12, 5, true, NOW(), NOW()),
(6, 1, 'd20', 'Dado de 20 faces', 20, 6, true, NOW(), NOW()),
(7, 1, 'd100', 'Dado de 100 faces', 100, 7, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- BONUS
-- ========================================
INSERT INTO bonus_config (id, jogo_id, nome, descricao, formula_base, ordem_exibicao, ativo, created_at, updated_at) VALUES
(1, 1, 'BBA', 'Bonus Base de Ataque', 'NIVEL', 1, true, NOW(), NOW()),
(2, 1, 'BBD', 'Bonus Base de Defesa', 'DES / 2', 2, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- VANTAGENS
-- ========================================
INSERT INTO vantagem_config (id, jogo_id, nome, descricao, nivel_maximo, formula_custo, descricao_efeito, ordem_exibicao, ativo, created_at, updated_at) VALUES
(1, 1, 'PV Extras', 'Aumenta PV', 10, 'NIVEL * 2', '+2 PV/nivel', 1, true, NOW(), NOW()),
(2, 1, 'Ataque Poderoso', 'Bonus ataque', 5, 'NIVEL * NIVEL', '+1 ataque/nivel', 2, true, NOW(), NOW()),
(3, 1, 'Defesa Aprimorada', 'Bonus defesa', 5, 'NIVEL * 3', '+1 defesa/nivel', 3, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
