---
name: Spec 005 — Estado e Gaps de Gestao de Participantes
description: Auditoria completa da Spec 005: o que existe no backend/frontend, os 11 gaps identificados e decisoes criticas do PO confirmadas em 2026-04-02
type: project
---

## Estado da Spec 005 (auditado 2026-04-02)

**Branch:** feature/005-participantes (a criar a partir de feature/009-npc-fichas-mestre)

## O que JA existe no backend

- `JogoParticipante` entity com `status` (PENDENTE/APROVADO/REJEITADO/BANIDO) e `role` (MESTRE/JOGADOR)
- `JogoParticipanteService` com: `solicitar`, `aprovar`, `rejeitar`, `banir`, `listar`
- `JogoParticipanteController` com: `POST /solicitar`, `GET /`, `PUT /{pid}/aprovar`, `PUT /{pid}/rejeitar`, `DELETE /{pid}` (ERRADO — veja gaps)
- `ParticipanteSecurityService`: `canAccessJogo`, `assertMestreDoJogo`
- Testes de integracao parciais (solicitar/aprovar/rejeitar/banir basico)

## O que JA existe no frontend

- `participante.model.ts` — alinhado com backend
- `jogos-api.service.ts` — tem solicitar, aprovar, rejeitar, banir (via DELETE — semantica errada)
- `participante-business.service.ts` — parcial
- `JogoDetailComponent` (Mestre) — tem UI parcial mas com bugs de dados e semantica
- `JogosDisponiveisComponent` (Jogador) — sem solicitar/status/cancelar

## 11 Gaps Identificados

- G-01 (CRITICO): `DELETE /{pid}` bane em vez de remover. Deve ser soft delete. Banir deve ser `PUT /{pid}/banir`
- G-02 (ALTO): `PUT /{pid}/desbanir` inexistente
- G-03 (ALTO): `GET /meu-status` inexistente
- G-04 (MEDIO): `DELETE /minha-solicitacao` inexistente
- G-05 (MEDIO): Filtro `?status=` na listagem inexistente
- G-06 (CRITICO): `solicitar()` bloqueia re-solicitacao mesmo para REJEITADO/REMOVIDO
- G-07 (CRITICO): unique constraint `uk_jogo_usuario` impede segundo registro apos soft delete
- G-08 (ALTO): Logica de `solicitar()` nao checa especificamente status BANIDO
- G-09 (MEDIO): Frontend chama `banirParticipante` para botao "Remover"
- G-10 (MEDIO): Frontend sem desbanir/meu-status/cancelar
- G-11 (ALTO): JogosDisponiveis sem fluxo de solicitacao do Jogador

## Decisao Critica: Solucao para G-06/G-07 (re-solicitacao)

Reutilizar registro existente (UPDATE) em vez de INSERT novo.
- REJEITADO ou soft-deleted: setar status=PENDENTE, deletedAt=null
- BANIDO: lancar 409
- APROVADO ativo: lancar 409
- Isso evita migracao de schema e mantem a unique constraint

**Why:** Constraint `uk_jogo_usuario` existe no banco e seria trabalhosa de migrar.
**How to apply:** Em qualquer discussao sobre re-solicitacao, assumir strategy "reactivate" (UPDATE, nao INSERT).

## Tasks Criadas

- P1-T1: Corrigir logica de re-solicitacao (🔴 grande)
- P1-T2: Adicionar endpoints faltantes (🟡 media)
- P1-T3: Testes de integracao completos (🟡 media)
- P2-T1: Alinhar API/Business service frontend (🟢 pequena)
- P2-T2: Corrigir JogoDetail Mestre (🟡 media)
- P2-T3: Completar JogosDisponiveis Jogador (🟡 media)

## Ponto Fora do Escopo

Descoberta de jogos publicos (endpoint GET /jogos/publicos para Jogador solicitar em jogo desconhecido) e escopo de spec futura. Jogador precisa de link direto compartilhado pelo Mestre.
