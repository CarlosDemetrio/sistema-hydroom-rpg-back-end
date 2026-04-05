# Spec 013 — Tasks Index

> Spec: `013-documentacao-tecnica`
> Total de tasks: 6
> Status geral: BACKLOG — executar apos specs funcionais implementadas

---

## Fase Backend (T1–T3)

| Task | Titulo | Dependencias | Status |
|------|--------|-------------|--------|
| [T1](P1-T1-javadoc-services.md) | Javadoc em services criticos do backend | — | Pendente |
| [T2](P1-T2-openapi-annotations.md) | Enriquecer OpenAPI annotations em todos os controllers | — | Pendente |
| [T3](P1-T3-inline-comments.md) | Inline comments nas regras de negocio complexas | — | Pendente |

## Fase Frontend (T4–T5)

| Task | Titulo | Dependencias | Status |
|------|--------|-------------|--------|
| [T4](P2-T4-tsdoc-services.md) | TSDoc nos services Angular e signal stores | — | Pendente |
| [T5](P2-T5-component-readme.md) | README por componente critico do frontend | — | Pendente |

## Fase Shared (T6)

| Task | Titulo | Dependencias | Status |
|------|--------|-------------|--------|
| [T6](P3-T6-swagger-json.md) | Exportar swagger.json versionado + script de geracao | T2 | Pendente |

---

## Grafo de dependencias

```
T1 (Javadoc services) ──────────┐
T3 (Inline comments) ───────────┤── PARALELO
T2 (OpenAPI annotations) ───────┤
T4 (TSDoc frontend) ────────────┤
T5 (Component README) ──────────┘
                                 |
T2 ────> T6 (swagger.json export)
```

**Paralelismo:** T1, T2, T3, T4, T5 sao todas independentes entre si. T6 depende apenas de T2.

---

*Produzido por: PM/Scrum Orchestrator | 2026-04-04*
