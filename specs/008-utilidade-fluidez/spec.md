# Spec 008 — Endpoints de Utilidade e Fluidez

> Status: 📝 Planejado
> Épico: EPIC 7 do EPICS-BACKLOG.md
> Depende de: Specs 006 + 007 (Ficha + Cálculos)
> Bloqueia: nada

## Contexto

Com o core do sistema completo (configs, ficha, cálculos), este spec adiciona endpoints auxiliares que melhoram a UX e fluidez do frontend. Nenhum destes é estritamente necessário para o sistema funcionar, mas melhoram significativamente a usabilidade.

## User Stories

### Story 1 — Dashboard do Mestre (P2)
**Como** mestre, **quero** ver um resumo do meu jogo para acompanhar rapidamente o progresso das fichas.

**Critérios:**
- GET /api/jogos/{id}/dashboard → totalFichas, totalParticipantes, fichasPorNivel, ultimasAlteracoes
- Apenas Mestre pode acessar o dashboard completo

### Story 2 — Dashboard do Jogador (P2)
**Como** jogador, **quero** acessar rapidamente as minhas fichas e jogos onde participo.

**Critérios:**
- GET /api/jogos/{id}/fichas/minhas → fichas do usuário logado
- GET /api/jogos/meus → jogos onde participa como aprovado ou é Mestre

### Story 3 — Busca e Filtros (P2)
**Como** mestre/jogador, **quero** filtrar configurações e fichas por nome e outros critérios.

**Critérios:**
- Filtro `?nome=` em todos os endpoints de configuração (busca por LIKE, case-insensitive)
- Filtros `?nome=&classeId=&racaId=&nivel=` no endpoint de fichas

### Story 4 — Reordenação em Batch (P2)
**Como** mestre, **quero** reordenar os itens de configuração arrastando e soltando.

**Critérios:**
- PUT /api/jogos/{id}/config/{tipo}/reordenar → recebe lista de {id, ordemExibicao}
- Disponível para todos os 13 tipos de config
- Executa em batch numa única transação

### Story 5 — Duplicação de Jogo (P2)
**Como** mestre, **quero** duplicar as configurações de um jogo para criar variações de campanha.

**Critérios:**
- POST /api/jogos/{id}/duplicar → cria novo jogo com cópia de todas as configs (sem fichas/participantes)
- Usuário que duplica torna-se Mestre do novo jogo

### Story 6 — Export/Import de Configurações (P3)
**Como** mestre, **quero** exportar/importar configurações para compartilhar presets com outros mestres.

**Critérios:**
- GET /api/jogos/{id}/config/export → JSON com todas as configs
- POST /api/jogos/{id}/config/import → importa configs de JSON, valida duplicatas

### Story 7 — Resumo de Ficha (P3)
**Como** jogador/mestre, **quero** ver uma versão compacta da ficha para listagens e cards.

**Critérios:**
- GET /api/fichas/{id}/resumo → dados calculados principais (sem sub-entidades detalhadas)

## Requisitos Funcionais

| ID | Descrição |
|----|-----------|
| FR-001 | GET /api/jogos/{id}/dashboard → DashboardMestreResponse (totalFichas, totalParticipantes, fichasPorNivel: Map<Integer,Long>, ultimasAlteracoes: List<{fichaId, nome, dataAlteracao}>) |
| FR-002 | GET /api/jogos/{id}/fichas/minhas → lista fichas do usuário logado no jogo |
| FR-003 | GET /api/jogos/meus → lista jogos com participação APROVADA ou onde é Mestre |
| FR-004 | Filtro `?nome=` (LIKE %nome%, case-insensitive) em todos os 13 GET de configurações |
| FR-005 | Resultado de configurações sempre ordenado por ordemExibicao por default |
| FR-006 | Filtros em GET /fichas: `?nome=`, `?classeId=`, `?racaId=`, `?nivel=` (todos opcionais) |
| FR-007 | PUT /api/jogos/{id}/config/atributos/reordenar → List<ReordenarItemRequest {id, ordemExibicao}> |
| FR-008 | Endpoint de reordenação disponível para todos os 13 tipos de config |
| FR-009 | POST /api/jogos/{id}/duplicar → DuplicarJogoRequest {novoNome}, cria jogo + copia 13 configs |
| FR-010 | Duplicação de jogo: usuário torna-se Mestre; ordemExibicao preservada; novos IDs gerados |
| FR-011 | GET /api/jogos/{id}/config/export → ConfigExportResponse com mapa por tipo + versão "1.0" |
| FR-012 | POST /api/jogos/{id}/config/import → valida duplicatas antes de importar; falha atomicamente |
| FR-013 | GET /api/fichas/{id}/resumo → FichaResumoResponse (id, nome, nivel, xp, racaNome, classeNome, atributosTotais, bonusTotais, vidaTotal, essenciaTotal, ameacaTotal) |
| FR-014 | ReordenarItemRequest: record com id (Long) e ordemExibicao (int, @Min(1)) |
| FR-015 | DuplicarJogoRequest: record com novoNome (@NotBlank) |
| FR-016 | DuplicarJogoResponse: jogoId, nome do novo jogo |
| FR-017 | Import falha atomicamente (transação única) — ou importa tudo ou nada |
| FR-018 | Dashboard: ultimasAlteracoes limitado a 5 entradas, ordenado por dataUltimaAtualizacao desc |
| FR-019 | GET /jogos/meus → retorna JogoResumoResponse com id, nome, totalFichas, isMestre |
| FR-020 | Duplicação copia apenas configs base — não copia sub-entidades de relacionamento (RacaBonusAtributo, ClasseBonus, etc.) — esses precisam ser reconfigurados no novo jogo |

## Requisitos Não-Funcionais

- Reordenação em batch em transação única (@Transactional)
- Duplicação de jogo em transação única
- Import em transação única (rollback completo se qualquer item falhar)
- Dashboard: queries otimizadas (evitar N+1 com GROUP BY e COUNT)

## Out of Scope

- Export/Import de fichas (apenas configs)
- Notificações push/email
- Dashboard com gráficos ou histórico temporal
- Duplicação com sub-entidades de relacionamento (ClasseBonus, RacaClassePermitida, etc.)
