---
name: Sprint 1 Tasks T19/T20/T21
description: Implementação de PUT vida/prospecção, NpcCreateRequest com descricao, GET/PUT /usuarios/me
type: project
---

## SP1-T19 — PUT /fichas/{id}/vida e PUT /fichas/{id}/prospeccao

Implementado em 2026-04-01.

- Novos DTOs: `AtualizarVidaRequest` (com inner record `MembroVidaRequest`) e `AtualizarProspeccaoRequest`
- Novo service: `FichaVidaService` — gerencia estado de combate sem recalcular atributos derivados
- Endpoints adicionados em `FichaController`: `PUT /api/v1/fichas/{id}/vida` e `PUT /api/v1/fichas/{id}/prospeccao`
- Campos adicionados às entidades: `FichaVida.vidaAtual` e `FichaEssencia.essenciaAtual`
- `FichaCalculationService.calcularVidaTotal` inicializa `vidaAtual` com `vidaTotal` quando zero (nova ficha)
- Ambos retornam `FichaResumoResponse` (chamando `FichaResumoService.getResumo`)
- Autorização: Mestre pode editar qualquer ficha; Jogador só as próprias (mesmo padrão do restante do sistema)

**Why:** Estado de combate (vida/essência restante, dano nos membros, dados de prospecção) é separado dos valores calculados — não deve recalcular tudo, apenas persiste valores informados pelo frontend.

## SP1-T20 — NpcCreateRequest com descricao

- Campo `descricao` (String, max 2000, opcional) adicionado a `NpcCreateRequest`
- Campo `descricao` (columnDefinition="TEXT", max 2000, opcional) adicionado à entidade `Ficha`
- `FichaResponse` agora inclui campo `descricao`
- `FichaMapper` atualizado: `toEntity` e `updateEntity` ignoram `descricao`
- `FichaService.atualizarDescricao(fichaId, descricao)` método novo para persistir descrição
- `FichaController.criarNpc` chama `atualizarDescricao` após criar o NPC se `descricao != null`
- `NpcFichaMestreIntegrationTest` já existia; adicionado novo teste para `atualizarDescricao`

**Why:** NPC precisa de descrição textual para o Mestre documentar características narrativas do personagem.

## SP1-T21 — GET/PUT /api/v1/usuarios/me

- Novo service: `UsuarioService` — `buscarAtual()` e `atualizarNome()`
- Novo controller: `UsuarioController` em `/api/v1/usuarios`
- `UsuarioMapper` atualizado: mapeamento de `ativo` via `isActive()` e `jogoAtivo` ignorado
- `@PreAuthorize("isAuthenticated()")` nos dois endpoints
- Apenas `nome` é editável — email e foto são gerenciados pelo Google OAuth2

**Why:** Frontend precisa de endpoint dedicado para perfil do usuário separado de `/api/v1/auth/me` (que é de autenticação).

## Resultado

- 422 testes passando (17 novos adicionados), 0 falhas
- Sem quebra de testes existentes
