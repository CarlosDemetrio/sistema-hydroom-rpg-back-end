# Spec 025 — Tasks Index: GAPs UX do Jogador

**Data de criação:** 2026-04-15  
**Spec:** `docs/specs/025-ux-gaps-jogador/spec.md`

---

## P0 — Bloqueadores (implementar imediatamente)

| Task | Título | Tipo | Esforço |
|---|---|---|---|
| T-025-01-BE | Adicionar ATIVA/MORTA/ABANDONADA ao enum FichaStatus (backend) | Backend | 1 dia |
| T-025-01-FE | Exibir badges de status na fichas-list e remover botão Excluir do Jogador | Frontend | 1 dia |
| T-025-01-EP | Endpoint PATCH /fichas/{id}/status (marcar MORTA/ABANDONADA) | Backend | 0.5 dia |
| T-025-02 | Fix: corrigir rota de edição em ficha-detail.component.ts | Frontend | 30 min |

---

## P1 — Alta Prioridade (próximo sprint)

| Task | Título | Tipo | Esforço |
|---|---|---|---|
| T-025-03 | Criar aba "Sessão/Combate" em ficha-detail com controles de vida/essência | Frontend | 2 dias |
| T-025-04 | Implementar polling de 30s na ficha-detail (modo sessão) | Frontend | 1 dia |
| T-025-05-BE | Endpoint GET /jogos/publicos (jogos descobríveis) | Backend | 1 dia |
| T-025-05-FE | Exibir seção de jogos disponíveis para descoberta | Frontend | 1 dia |
| T-025-06 | Polling na tela "Jogos Disponíveis" + notificação de mudança de status | Frontend | 1 dia |
| T-025-07 | Criar sub-componente de membros do corpo na aba Sessão | Frontend | 1 dia |

---

## P2 — Média Prioridade

| Task | Título | Tipo | Esforço |
|---|---|---|---|
| T-025-08 | Fix: wizard lê fichaId do path param (:id) além de queryParam | Frontend | 1 dia |
| T-025-09 | Exibir badges de status MORTA/ABANDONADA na fichas-list | Frontend | 0.5 dia |
| T-025-10 | canEdit() retorna false para fichas MORTA/ABANDONADA | Frontend | 0.5 dia |
| T-025-11 | Exibir badge de status no FichaHeaderComponent | Frontend | 0.5 dia |
| T-025-12 | Fix: labels do wizard ("Vantagens" e "Revisão" nos passos 5 e 6) | Frontend | 15 min |

---

## P3 — Baixa Prioridade / Aguarda Decisão PO

| Task | Título | Tipo | Esforço |
|---|---|---|---|
| T-025-13 | Remover item "Criar Ficha" do sidebar do Jogador | Frontend | 15 min |
| T-025-14 | Definir permissões corretas para HabilidadeConfig (Jogador vs Mestre) | Backend+FE | Aguarda PA-025-05 |

---

## Pontos em Aberto (bloqueiam algumas tasks)

- **PA-025-01**: COMPLETA → ATIVA no backend? (bloqueia T-025-01-BE)
- **PA-025-02**: Fluxo de renascimento? (bloqueia escopo de T-025-01)
- **PA-025-03**: Jogo público vs privado? (bloqueia T-025-05-BE)
- **PA-025-04**: Endpoint essencia/gastar em escopo? (afeta T-025-03)
- **PA-025-05**: HabilidadeConfig permissões? (bloqueia T-025-14)
- **PA-025-06**: Mestre deleta ou marca como abandonada? (afeta T-025-01-BE e T-025-01-FE)
