---
name: Estado atual da feature Ficha (backend + frontend)
description: Inventário do que está implementado, alinhado ou desalinhado entre backend e frontend no fluxo de Ficha — auditado em 2026-04-02
type: project
---

Estado auditado em 2026-04-02 no branch `feature/009-npc-fichas-mestre`.

**Why:** O FichaForm foi construído antes do backend existir; vários campos foram inventados. O FichaDetail foi reconstruído corretamente depois.

**How to apply:** Ao propor qualquer nova story de Ficha, partir desta tabela de verdade — não do BA-FICHA.md anterior, que pode ter ficado desatualizado.

## Backend — O que está implementado e exposto

| Endpoint | Status |
|---|---|
| GET/POST /jogos/{id}/fichas | OK |
| GET /jogos/{id}/fichas/minhas | OK |
| GET /fichas/{id} | OK |
| PUT /fichas/{id} | OK (nome, racaId, classeId, generoId, indoleId, presencaId, xp, renascimentos) |
| DELETE /fichas/{id} | OK (apenas MESTRE) |
| GET /jogos/{id}/npcs | OK (apenas MESTRE) |
| POST /jogos/{id}/npcs | OK (apenas MESTRE) — NpcCreateRequest inclui campo `descricao` |
| GET /fichas/{id}/resumo | OK — retorna FichaResumoResponse |
| POST /fichas/{id}/preview | OK — retorna FichaPreviewResponse rico (atributos, bonus, vida, essência, ameaça) |
| PUT /fichas/{id}/atributos | OK (lote) |
| PUT /fichas/{id}/aptidoes | OK (lote) |
| GET /fichas/{id}/vantagens | OK |
| POST /fichas/{id}/vantagens | OK (comprar) |
| PUT /fichas/{id}/vantagens/{vid} | OK (aumentar nível) |
| GET/POST/DELETE /fichas/{id}/anotacoes/{id} | OK |
| PUT /fichas/{id}/vida | OK — retorna FichaResumoResponse |
| PUT /fichas/{id}/prospeccao | OK — retorna FichaResumoResponse |
| POST /fichas/{id}/duplicar | OK |

**Gaps no backend:**
- Não existe GET /fichas/{id}/atributos (listagem individual sem update)
- Não existe GET /fichas/{id}/aptidoes (listagem individual sem update)
- FichaResumoResponse NÃO inclui vidaAtual/essenciaAtual (apenas totais)
- FichaVantagemResponse NÃO inclui categoriaNome
- NpcCreateRequest inclui `descricao`, mas FichaResponse inclui `descricao` — isso é exclusivo do NPC

## Frontend — Estado por componente

| Componente | Status | Problema |
|---|---|---|
| FichasListComponent | Funcional | Busca client-side por nome apenas; sem filtro por classe/raça/nível |
| FichaDetailComponent | Funcional — estrutura correta | Aba Atributos usa mock de FichaResumo.atributosTotais; não chama endpoint real |
| ficha-header | Implementado | Stats bar mostra vida/essência como 100% sempre (sem vidaAtual) |
| ficha-resumo-tab | Implementado | Atributos vêm do mock no Detail, não de endpoint próprio |
| ficha-atributos-tab | Implementado | Exibe tabela mas dados são mockados pelo Detail |
| ficha-aptidoes-tab | Implementado mas vazio | Detail passa lista vazia; sem endpoint GET /aptidoes |
| ficha-vantagens-tab | Funcional | categoriaNome não vem do backend — agrupa tudo como "Vantagens" |
| ficha-anotacoes-tab | Funcional e completo | Mais completo de todos — CRUD inline funciona |
| FichaFormComponent | QUEBRADO | buildForm() usa campos inexistentes: origem, linhagem, insolitus, nvs, descricaoFisica, pericias, equipamentos, titulosRunas. Atributos hardcoded. |
| Telas de NPC (Mestre) | NÃO EXISTE | Nenhuma tela NPC no features/mestre/ |
| XP / renascimentos | NÃO EXISTE | Não há tela para o Mestre conceder XP ou incrementar renascimentos |
