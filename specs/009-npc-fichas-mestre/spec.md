# Spec 009 — NPC e Fichas do Mestre

> Status: 📝 Planejado
> Épico: EPIC 8 do EPICS-BACKLOG.md
> Depende de: Spec 006 (Ficha de Personagem) + Spec 007 (Motor de Cálculos)
> Bloqueia: nada

## Contexto

Com o core da Ficha de Personagem pronto, o Mestre precisa de fichas sem jogador dono — NPCs. NPCs compartilham a mesma estrutura da Ficha normal (FichaAtributo, FichaBonus, etc.) mas são criados e gerenciados exclusivamente pelo Mestre. A duplicação de fichas (para criar variantes de NPCs rapidamente) também faz parte deste spec.

## User Stories

### Story 1 — Criar e gerenciar NPCs (P2)
**Como** mestre, **quero** criar fichas sem jogador dono para representar inimigos, aliados e figurantes.

**Critérios:**
- POST /api/jogos/{id}/npcs → cria ficha com isNpc=true e jogadorId=null
- GET /api/jogos/{id}/npcs → lista apenas NPCs (não mistura com fichas de jogadores)
- CRUD completo: GET/PUT/DELETE /api/npcs/{id}
- Apenas Mestre pode criar/editar/deletar NPCs
- NPC inicializa os mesmos sub-componentes que uma Ficha normal

### Story 2 — NPCs separados das fichas de jogadores (P2)
**Como** mestre, **quero** que NPCs não apareçam na listagem de fichas de jogadores.

**Critérios:**
- GET /api/jogos/{id}/fichas retorna APENAS fichas com isNpc=false
- GET /api/jogos/{id}/npcs retorna APENAS fichas com isNpc=true

### Story 3 — Duplicar Ficha/NPC (P2)
**Como** mestre, **quero** duplicar uma ficha existente para criar variantes rapidamente.

**Critérios:**
- POST /api/fichas/{id}/duplicar → cria cópia com todos os sub-componentes com valores atuais
- Opção `manterJogador` (boolean): se false ou original é NPC → duplicata é NPC
- Apenas Mestre pode duplicar

## Requisitos Funcionais

| ID | Descrição |
|----|-----------|
| FR-001 | Ficha entity tem `isNpc` (boolean, default false) e `jogadorId` nullable (Spec 006 deve implementar) |
| FR-002 | POST /api/jogos/{id}/npcs → isNpc=true, jogadorId=null, requer MESTRE |
| FR-003 | GET /api/jogos/{id}/npcs → apenas fichas com isNpc=true, requer MESTRE |
| FR-004 | GET /api/jogos/{id}/fichas → apenas fichas com isNpc=false |
| FR-005 | GET /api/npcs/{id} → requer MESTRE |
| FR-006 | PUT /api/npcs/{id} → requer MESTRE |
| FR-007 | DELETE /api/npcs/{id} → soft delete, requer MESTRE |
| FR-008 | NPC usa mesmas sub-entities de Ficha (FichaAtributo, FichaBonus, etc.) |
| FR-009 | FichaCalculationService.recalcular() funciona para NPC da mesma forma |
| FR-010 | NpcCreateRequest: mesmo que FichaCreateRequest mas sem jogadorId |
| FR-011 | NpcResponse: FichaResponse com isNpc=true e jogadorId=null |
| FR-012 | POST /api/fichas/{id}/duplicar → DuplicarFichaRequest {novoNome, manterJogador (boolean)} |
| FR-013 | Duplicação copia todos os sub-componentes com valores atuais (não reinicializa em zero) |
| FR-014 | Se manterJogador=false ou original é NPC → duplicata tem isNpc=true e jogadorId=null |
| FR-015 | Apenas Mestre pode duplicar fichas (POST /duplicar) |

## Requisitos Não-Funcionais

- Duplicação em @Transactional único
- Sem endpoints de listagem de NPCs para Jogadores (403)
- NPC não aparece nas fichas do Jogador

## Out of Scope

- NPCs com estados especiais (HP atual, condições de combate)
- Exportação de NPC em formato statblock
- Compartilhamento de NPCs entre jogos diferentes
