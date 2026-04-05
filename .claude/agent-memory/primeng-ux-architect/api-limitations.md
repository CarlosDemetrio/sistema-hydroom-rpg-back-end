---
name: Limitações de API conhecidas que impactam o frontend
description: Campos ausentes no backend atual que forçam decisões de design provisórias no frontend
type: project
---

## Limitações identificadas em 2026-04-01

### 1. statusParticipacao ausente em JogoResumo

**Endpoint**: `GET /api/v1/jogos`
**Problema**: `JogoResumo` retorna `meuRole` mas não `statusParticipacao` (PENDENTE/REJEITADO/BANIDO).
**Impacto**: `JogosDisponiveisComponent` não consegue exibir corretamente os estados PENDENTE, REJEITADO, BANIDO.
**Workaround atual**: Se `meuRole === 'JOGADOR'`, assumir APROVADO. Outros estados ficam inacessíveis.
**Solução recomendada**: Backend deve incluir `statusParticipacao: StatusParticipante | null` no `JogoResumo`.

### 2. mestreNome ausente em JogoResumo

**Endpoint**: `GET /api/v1/jogos`
**Problema**: `JogoResumo` não inclui nome do Mestre criador do jogo.
**Impacto**: Card de jogo não pode exibir "Mestre: Carlos".
**Workaround atual**: Omitir campo no card ou fazer N+1 requests (não recomendado).
**Solução recomendada**: Incluir `mestreNome: string` no `JogoResumo`.

### 3. vidaAtual / essenciaAtual ausentes na Ficha e FichaResumo

**Problema**: Backend não persiste vida atual e essência atual — apenas calcula os totais.
**Impacto**: Barras de vida/essência no header da ficha não mostram HP atual, apenas máximo (barra cheia).
**Workaround MVP**: Exibir barras cheias com tooltip "HP atual não rastreado nesta versão".
**Solução futura**: Adicionar campos `vidaAtual` e `essenciaAtual` à entidade Ficha com update direto.

### 4. FichaVantagemResponse desalinhado

**Arquivo afetado**: `/src/app/core/models/ficha-vantagem.model.ts`
**Problema**: `FichaVantagem` usa campos `nivel`, `bonus`, `dano` e aninha `VantagemConfig` — não corresponde ao response real do backend.
**Response real esperado**: `{ id, fichaId, vantagemConfigId, vantagemNome, vantagemSigla, categoriaNome, nivelAtual, nivelMaximo, custoPago, descricaoEfeito }`
**Ação necessária**: Reescrever interface `FichaVantagemResponse` no model antes de implementar `FichaVantagensTabComponent`.

### 5. tipoAptidaoNome ausente em FichaAptidaoResponse

**Endpoint**: `PUT /api/v1/fichas/{id}/aptidoes` response
**Problema**: `FichaAptidaoResponse` não inclui `tipoAptidaoNome` — apenas `aptidaoConfigId` e `aptidaoNome`.
**Impacto**: `FichaAptidoesTabComponent` não consegue agrupar por tipo sem join adicional.
**Workaround**: Carregar também `GET /configuracoes/aptidoes?jogoId=` para fazer o join no frontend, ou exibir lista plana sem agrupamento na Fase 1.
**Solução recomendada**: Backend incluir `tipoAptidaoNome: string` no `FichaAptidaoResponse`.
