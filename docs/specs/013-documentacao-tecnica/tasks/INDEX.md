# Spec 013 — Tasks Index

> Spec: `013-documentacao-tecnica`
> Total de tasks: **1 (replanejamento pendente)** — tasks T1-T6 originais sao referencias, serao substituidas
> Status geral: **REPLANEJAMENTO NECESSARIO** — executar T0 antes de qualquer implementacao
> Atualizado: 2026-04-06

---

## ⚠️ Atencao — Replanejamento Necessario

As tasks T1-T6 originais cobrem escopo muito amplo (4-5h cada). Antes de executar qualquer task
de documentacao, o PM deve executar **T0** para quebrar em tasks menores (max 1.5h) e incluir
o bloco de documentacao GitHub (Pages, Wiki, READMEs de repo, Releases/changelog).

---

## Fase 0 — Pre-execucao (fazer PRIMEIRO)

| Task | Titulo | Dependencias | Status |
|------|--------|-------------|--------|
| [T0](P0-T0-replanejamento.md) | Replanejamento: quebrar tasks + incluir GitHub strategy | — | **PENDENTE** |

---

## Modulos inventariados (ver T0 para lista completa)

> As tasks individuais (1 por modulo) serao criadas pelo PM ao executar T0.
> NAO executar T1-T6 originais — sao muito amplas e serao descartadas.

**Contagem estimada de tasks apos replanejamento:**

*Documentacao Tecnica:*
- Backend Javadoc: ~16 tasks (1 por service)
- Backend OpenAPI: ~18 tasks (1 por controller + DTOs)
- Backend Inline Comments: 4 tasks
- Frontend TSDoc: 5 tasks
- Frontend Component README: 13 tasks (1 por componente)
- GitHub Documentation: 7 tasks

*Documentacao Nao-Tecnica / Dominio (GitHub Wiki):*
- Sistema geral: 4 tasks (visao geral, jogo, ficha, formulas)
- Configuracoes: 13 tasks (1 por configuracao — o que faz, regras, campos)
- Ficha e motor: 5 tasks (campos, ciclo de vida, calculos, vantagens, participantes)

**Total estimado: ~85 tasks** — todas com escopo unitario, max 1h cada, sem inventar regras

---

## Tasks originais (referencia de escopo — NAO executar)

| Task | Titulo | Status |
|------|--------|--------|
| [T1](P1-T1-javadoc-services.md) | Javadoc services backend | SUBSTITUIR (muito ampla) |
| [T2](P1-T2-openapi-annotations.md) | OpenAPI controllers | SUBSTITUIR (muito ampla) |
| [T3](P1-T3-inline-comments.md) | Inline comments negocio | SUBSTITUIR |
| [T4](P2-T4-tsdoc-services.md) | TSDoc services Angular | SUBSTITUIR (muito ampla) |
| [T5](P2-T5-component-readme.md) | README por componente | SUBSTITUIR (muito ampla) |
| [T6](P3-T6-swagger-json.md) | swagger.json export | SUBSTITUIR (incorporar em GitHub Pages task) |

---

*Produzido por: PM/Scrum Orchestrator | 2026-04-06 — aguarda execucao de T0*
