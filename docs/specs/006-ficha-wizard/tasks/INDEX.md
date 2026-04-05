# Spec 006 — Indice de Tasks

> Spec: `006-ficha-wizard`
> Total de tasks: 13
> Fase Backend: T1–T5
> Fase Frontend: T6–T13

---

## Visao Geral

| ID | Fase | Titulo | Complexidade | Prerequisito | Status |
|----|------|--------|--------------|--------------|--------|
| [T1](B1-T1-status-completar.md) | Backend | Campo status + endpoint /completar | Media | — | Pendente |
| [T2](B2-T2-validacao-raca-classe.md) | Backend | Validacao RacaClassePermitida na criacao | Baixa | — | Pendente |
| [T3](B3-T3-bloquear-xp-jogador.md) | Backend | Bloquear XP no PUT /fichas/{id} para JOGADOR | Baixa | — | Pendente |
| [T4](B4-T4-endpoint-xp-mestre.md) | Backend | Endpoint PUT /fichas/{id}/xp (MESTRE-only) | Media | — | Pendente |
| [T5](B5-T5-pontos-disponiveis-resumo.md) | Backend | pontosDisponiveis no FichaResumoResponse | Alta | — | Pendente |
| [T6](F6-T6-passo1-identificacao.md) | Frontend | Passo 1: Identificacao (rewrite do wizard) | Alta | T1 | Pendente |
| [T7](F7-T7-passo2-descricao-fisica.md) | Frontend | Passo 2: Descricao fisica (campo opcional) | Baixa | T6 | Pendente |
| [T8](F8-T8-passo3-atributos.md) | Frontend | Passo 3: Distribuicao de atributos | Alta | T5, T6 | Pendente |
| [T9](F9-T9-passo4-aptidoes.md) | Frontend | Passo 4: Distribuicao de aptidoes | Media | T5, T6 | Pendente |
| [T10](F10-T10-passo5-vantagens.md) | Frontend | Passo 5: Compra de vantagens iniciais | Alta | T5, T6 | Pendente |
| [T11](F11-T11-passo6-revisao.md) | Frontend | Passo 6: Revisao e confirmacao | Media | T1, T6 | Pendente |
| [T12](F12-T12-autosave-visual.md) | Frontend | Auto-save visual (indicador de salvamento) | Baixa | T6 | Pendente |
| [T13](F13-T13-badge-incompleta.md) | Frontend | Badge "incompleta" na listagem de fichas | Baixa | T1 | Pendente |

---

## Ordem de Execucao Recomendada

### Sprint 1 — Backend (T1 a T5, podem ser feitas em paralelo por 2 devs)

```
Dev 1: T1 → T4 (fluxo de status e XP)
Dev 2: T2 → T3 → T5 (validacoes e pontos)
```

### Sprint 2 — Frontend Foundation (T6, T12, T13)

```
T6 primeiro (wizard base + Passo 1) — tudo o mais depende desta estrutura
T12 e T13 podem ser feitas em paralelo apos T6
```

### Sprint 3 — Frontend Passos (T7 a T11)

```
T7 (simples, pode ser feito junto com T8)
T8 e T9 em paralelo (estrutura similar)
T10 apos T8 e T9 (precisa de pontos do resumo funcionando)
T11 apos T10 (revisao depende de todos os passos anteriores)
```

---

## Dependencias entre Tasks (grafo)

```
T1 ──────────────────────────────────────> T6 ──> T7
T2 (independente, enriquece T1)                   T8
T3 (independente)                                 T9
T4 (independente)                                 T10
T5 ──────────────────────────────────────> T8, T9, T10
T6 ──> T7, T8, T9, T10, T11, T12
T1 + T6 ──> T11
T1 ──> T13
```

---

## Criterio de Done da Spec 006

A Spec 006 e considerada **CONCLUIDA** quando:
- [ ] Todos os 5 endpoints de backend (T1–T5) passando em testes de integracao
- [ ] Wizard de 6 passos navegavel no frontend com auto-save funcional
- [ ] Ficha criada com status RASCUNHO via Passo 1
- [ ] Atributos salvos via Passo 3 com validacao de pontos
- [ ] Aptidoes salvas via Passo 4
- [ ] Botao "Criar Personagem" transiciona para COMPLETA com sucesso
- [ ] Badge "Incompleta" visivel na lista de fichas
- [ ] XP editavel apenas pelo Mestre (HTTP 403 para JOGADOR)
- [ ] 0 regressoes nos 457 testes existentes
