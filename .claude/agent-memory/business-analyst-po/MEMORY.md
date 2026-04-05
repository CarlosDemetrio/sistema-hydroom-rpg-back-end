# Memória do Agente BA/PO — ficha-controlador

- [Estado atual da feature Ficha](project_ficha_estado_atual.md) — Inventário backend/frontend auditado em 2026-04-02: o que está ok, o que está mockado, o que está quebrado
- [Gaps críticos do fluxo Ficha](project_gaps_criticos_ficha.md) — Lista priorizada de 13 gaps entre backend e frontend, com severidade
- [Análise completa de gaps — todos os domínios](project_gaps_analise_completa.md) — 10 gaps de requisitos (wizard, XP, VantagemEfeito, NPC, progressão) com perguntas para PO
- [Spec 005 — Participantes: estado e gaps](project_spec005_participantes.md) — 11 gaps auditados, decisão de re-solicitação (strategy reactivate), 6 tasks criadas
- [Spec 008 — Sub-recursos de Classes e Racas](project_spec008_classes_racas.md) — 4 tasks frontend-only, problemas de tipagem criticos, pontos em aberto P-01 a P-05
- [Spec 010 — Roles ADMIN/MESTRE/JOGADOR](project_spec010_roles_refactor.md) — decisões de produto, regras críticas, ponto P-03 pendente (bypass ADMIN em canAccessJogo)
- [Spec 011 — Galeria e Anotações (revisado)](project_spec011_galeria_anotacoes.md) — Cloudinary, Markdown, pastas 3 níveis, visão NPC restrita; 9 tasks ~9 dias; PA-008 aguarda PO
- [Spec 012 — Níveis, Progressão e Level Up Frontend](project_spec012_niveis_progressao.md) — 14 tasks, 3 gaps críticos backend, P-01/P-02 aguardam PO
- [Auditoria Integração Config → Ficha](project_integracao_config_ficha.md) — 9 gaps críticos, ordem de 10 passos de cálculo, schema changes necessárias, pontos em aberto PA-PONTOS-01 a PA-CALC-01
- [Auditoria UX Fichas — Modo Sessao e Criacao](project_ux_fichas_auditoria.md) — 20 gaps UX, 6 bugs no codigo existente, 8 quick wins, 8 PA para PO; entregavel: docs/analises/UX-FICHAS-AUDITORIA.md
- [Spec 009-ext — Atualizações 2026-04-03](project_spec009ext_atualizacoes.md) — endpoints essencia padronizados, PA-UX-01 resolvido (polling), task T-QW com 3 bugs frontend criticos
- [Spec 007 — Revisão 2026-04-03: Bugs Calc Base](project_spec007_revisao_bugs_calc.md) — P0-T0 criada (6 bugs), SCHEMA-01/02, sequência de 10 passos, 13 tasks totais, PA-006 (VIG/SAB hardcoded) aguarda PO
- [Auditoria DefaultGameConfigProvider vs Glossário](project_default_config_auditoria_glossario.md) — 7 divergências (DIV-06 CRITICA: Cabeça 25%/75%), 6 perguntas Q-DC-06 a Q-DC-11 para PO
- [Análise de Cobertura de MVP — Gaps 2026-04-04](project_gaps_cobertura_mvp.md) — 8 gaps (3 bloqueadores, 4 sem task, 1 validação PO); recomenda Spec 015 e tasks adicionais em 006/007/009-ext
- [Spec 015 — Dataset defaults ClassePontosConfig e RacaPontosConfig](project_spec015_dataset_defaults.md) — PA-015-01/02/03 respondidos; 5 pontos abertos (PA-DV-01..05) aguardam PO
- [Spec 016 — Dataset de Itens SRD](project_spec016_dataset_itens.md) — 7 raridades, 20 tipos, 40 itens, 12 classes; mapeamento FK+calculo; 4 pontos abertos PA-016-DS-01..04
