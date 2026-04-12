# Spec 021 — Tasks Index

> Spec: `021-sistema-habilidades`
> Total de tasks: 2
> Status geral: Pendente

---

## Fase Backend (T1)

| Task | Titulo | Dependencias | Status | Estimativa |
|------|--------|-------------|--------|-----------|
| [T1](P1-T1-habilidade-config-backend.md) | HabilidadeConfig — entity, CRUD, testes | nenhuma | Pendente | 2–3h |

---

## Fase Frontend (T2)

| Task | Titulo | Dependencias | Status | Estimativa |
|------|--------|-------------|--------|-----------|
| [T2](P2-T2-habilidade-config-frontend.md) | HabilidadeConfig — tela CRUD, store, testes | T1 | Pendente | 3–4h |

---

## Grafo de dependencias

```
T1 (Backend)
  └── T2 (Frontend)
```

---

## Pontos em Aberto (resolver antes de iniciar T2)

- **PA-021-03:** Onde a tela de habilidades aparece para o Jogador? No painel de configuracoes do jogo (junto ao Mestre) ou em caminho separado (ex: dentro da ficha do personagem)?

---

*Produzido por: Business Analyst/PO | 2026-04-12*
