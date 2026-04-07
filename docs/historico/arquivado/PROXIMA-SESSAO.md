# Proxima Sessao -- Ponto de Retomada

> Atualizado: 2026-04-05 [14:01] (pos-rodada 6, sessao 12)
> Branch backend: `main`
> Branch frontend: `main`

---

## Estado Atual (resumo executivo)

| Metrica | Valor |
|---------|-------|
| Backend testes | **571 passando**, 0 falhas |
| Frontend testes | **359 passando**, 0 falhas |
| Sprint 2 progresso | **19/35 concluidas** (54%) |
| Rodada mais recente | Rodada 6 — S007-T8, S005-P1T1/P1T2, S006-T4 |
| Proximo foco | S015-T4, S005-P1T3, S007-T9..T12 (frontend), S006-T6..T13 (wizard) |

---

## Comando de Retomada Rapida

### Passo 1: Ler contexto

```
docs/HANDOFF-SESSAO.md   -- estado completo pos-rodada 6, proxima rodada planejada
docs/SPRINT-ATUAL.md     -- sprint tracking com 19/35 concluidas
```

### Passo 2: Verificar testes (sanidade)

```bash
cd /Users/carlosdemetrio/IdeaProjects/ficha-controlador
./mvnw test 2>&1 | tail -5        # deve mostrar 571 testes, 0 falhas

cd /Users/carlosdemetrio/IdeaProjects/ficha-controlador-front-end/ficha-controlador-front-end
npx vitest run 2>&1 | tail -10    # deve mostrar 359 testes, 0 falhas
```

### Passo 3: Lancar Rodada 7

---

## Rodada 7 — Tasks Planejadas

### AGENTE 1 — S015-T4: Auto-concessao de Vantagens Pre-Definidas (Backend)

**Spec:** `docs/specs/015-config-pontos-classe-raca/tasks/P1-T4-auto-concessao-vantagens.md`
**Dependencia:** S015-T1 (CONCLUIDA) + S007-T7 (CONCLUIDA)
**Estimativa:** 3-4h
**Resumo:** Quando uma ficha e criada ou muda de classe/raca, o sistema deve conceder automaticamente as vantagens definidas em `ClasseVantagemPreDefinida` e `RacaVantagemPreDefinida`. Custo 0, campo `concedidoPeloMestre=false` (origem SISTEMA).

---

### AGENTE 2 — S005-P1T3: Testes de Integracao Participantes (Backend)

**Spec:** `docs/specs/005-participantes/tasks/P1-T3-testes-integracao.md`
**Dependencia:** S005-P1T1 (CONCLUIDA) + S005-P1T2 (CONCLUIDA)
**Estimativa:** 2-3h
**Resumo:** Testes de integracao cobrindo o fluxo completo de participantes (solicitar, aprovar, rejeitar, banir, desbanir, remover, re-solicitar). Incluir cenarios de seguranca (Jogador nao pode aprovar, etc).

---

### AGENTE 3 — S007-T9: Frontend VantagemEfeito — VantagensConfig UI com Tipos de Efeito

**Spec:** `docs/specs/007-vantagem-efeito/tasks/P2-T9-vantagens-config-efeitos-ui.md`
**Dependencia:** S007-T8 (CONCLUIDA)
**Estimativa:** 4-6h
**Repo:** `/Users/carlosdemetrio/IdeaProjects/ficha-controlador-front-end/ficha-controlador-front-end`

---

### AGENTE 4 — S006-T6: Frontend Wizard Passo 1 — Identificacao

**Spec:** `docs/specs/006-ficha-wizard/tasks/F6-T6-passo1-identificacao.md`
**Dependencia:** S006-T1/T4/T5 backend (CONCLUIDAS)
**Estimativa:** 3-4h
**Repo:** `/Users/carlosdemetrio/IdeaProjects/ficha-controlador-front-end/ficha-controlador-front-end`

---

## Decisoes Pendentes (para o PO)

| ID | Pergunta | Bloqueia |
|----|----------|---------|
| PA-004 | FORMULA_CUSTOMIZADA — qual e o alvo do resultado? | S007-T5alt |
| PA-015-04 | Campo `origem` em FichaVantagem: enum JOGADOR/MESTRE/SISTEMA? | S015-T4 |

---

## Arquivos-chave para ler antes de comecar

| Arquivo | Por que ler |
|---------|-------------|
| `docs/HANDOFF-SESSAO.md` | **LEIA PRIMEIRO** — estado completo pos-rodada 6 |
| `docs/MASTER.md` | Indice mestre, 19/35 Sprint 2 |
| `docs/SPRINT-ATUAL.md` | Tracking detalhado de todas as tasks |
| `docs/specs/015-config-pontos-classe-raca/tasks/P1-T4-*.md` | Task do Agente 1 |
| `docs/specs/005-participantes/tasks/P1-T3-*.md` | Task do Agente 2 |

---

*Atualizado: 2026-04-05 [14:01] | PM/Scrum Master*
