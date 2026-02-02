-- ============================================================================
-- Migration: Seed Configuration Data - Part 1 (Atributos e Aptidões)
-- Versão: V20260201100300
-- Descrição: Popula dados padrão de atributos e aptidões
-- Autor: Backend Team
-- Data: 2026-02-01
-- ============================================================================

-- ===========================================================================
-- SEED: configuracao_atributos (7 atributos padrão)
-- ===========================================================================
INSERT INTO configuracao_atributos (nome, descricao, formula_impeto, unidade_impeto, ordem_exibicao, ativo) VALUES
('Força', 'Capacidade física bruta, determina capacidade de carga', 'total * 3', 'kg', 1, TRUE),
('Agilidade', 'Velocidade e reflexos, determina deslocamento', 'total / 3', 'metros', 2, TRUE),
('Vigor', 'Resistência física, redução de dano físico', 'total / 10', 'RD', 3, TRUE),
('Sabedoria', 'Resistência mágica, redução de dano mágico', 'total / 10', 'RDM', 4, TRUE),
('Intuição', 'Sorte e percepção instintiva, pontos de sorte', 'MIN(total / 20, 3)', 'pontos', 5, TRUE),
('Inteligência', 'Capacidade de comando e raciocínio', 'total / 20', 'comando', 6, TRUE),
('Astúcia', 'Pensamento estratégico e tático', 'total / 10', 'estratégia', 7, TRUE);

-- ===========================================================================
-- SEED: configuracao_aptidoes (24 aptidões - 12 físicas + 12 mentais)
-- ===========================================================================

-- Aptidões Físicas
INSERT INTO configuracao_aptidoes (nome, tipo, descricao, ordem_exibicao, ativo) VALUES
('Acrobacia', 'FISICA', 'Habilidade de realizar manobras acrobáticas', 1, TRUE),
('Guarda', 'FISICA', 'Capacidade de defesa e bloqueio', 2, TRUE),
('Aparar', 'FISICA', 'Habilidade de desviar ataques', 3, TRUE),
('Atletismo', 'FISICA', 'Força física e condicionamento', 4, TRUE),
('Resvalar', 'FISICA', 'Capacidade de esquiva e movimento ágil', 5, TRUE),
('Resistência', 'FISICA', 'Capacidade de resistir a condições adversas', 6, TRUE),
('Perseguição', 'FISICA', 'Habilidade de perseguir ou fugir', 7, TRUE),
('Natação', 'FISICA', 'Capacidade de nadar e manobras aquáticas', 8, TRUE),
('Furtividade', 'FISICA', 'Capacidade de se mover sem ser detectado', 9, TRUE),
('Prestidigitação', 'FISICA', 'Destreza manual e truques', 10, TRUE),
('Conduzir', 'FISICA', 'Habilidade de pilotar veículos e montarias', 11, TRUE),
('Arte da Fuga', 'FISICA', 'Capacidade de escapar de restrições', 12, TRUE);

-- Aptidões Mentais
INSERT INTO configuracao_aptidoes (nome, tipo, descricao, ordem_exibicao, ativo) VALUES
('Idiomas', 'MENTAL', 'Conhecimento de línguas', 13, TRUE),
('Observação', 'MENTAL', 'Capacidade de notar detalhes', 14, TRUE),
('Falsificar', 'MENTAL', 'Habilidade de criar falsificações', 15, TRUE),
('Prontidão', 'MENTAL', 'Capacidade de reagir rapidamente', 16, TRUE),
('Auto Controle', 'MENTAL', 'Controle emocional e mental', 17, TRUE),
('Sentir Motivação', 'MENTAL', 'Capacidade de ler intenções', 18, TRUE),
('Sobrevivência', 'MENTAL', 'Conhecimento de técnicas de sobrevivência', 19, TRUE),
('Investigar', 'MENTAL', 'Habilidade de coletar e analisar informações', 20, TRUE),
('Blefar', 'MENTAL', 'Capacidade de enganar e mentir', 21, TRUE),
('Atuação', 'MENTAL', 'Habilidade de interpretar personagens', 22, TRUE),
('Diplomacia', 'MENTAL', 'Capacidade de negociação e persuasão', 23, TRUE),
('Operação de Mecanismos', 'MENTAL', 'Conhecimento de dispositivos mecânicos', 24, TRUE);

-- ===========================================================================
-- VERIFICAÇÃO (Opcional - comentar em produção)
-- ===========================================================================
-- SELECT COUNT(*) as total_atributos FROM configuracao_atributos WHERE ativo = TRUE;
-- Expected: 7
-- SELECT COUNT(*) as total_aptidoes FROM configuracao_aptidoes WHERE ativo = TRUE;
-- Expected: 24
