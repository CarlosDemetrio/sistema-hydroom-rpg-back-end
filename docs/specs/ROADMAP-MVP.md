# Roadmap MVP — ficha-controlador

> Sequencia de implementacao para fechar o MVP.
> Baseado em: decisoes do PO (2026-04-03), GLOSSARIO-GAPS.md, TECH-LEAD-BACKEND-REVIEW.md
> Fonte de verdade: `docs/MASTER.md`

---

## Ponto Zero: Correcao Urgente (pre-qualquer-fase)

| Item | Spec/Task | Esforco | Justificativa |
|------|-----------|---------|---------------|
| Bug XP: remover xp/renascimentos do UpdateFichaRequest | Spec 006 T3 | P1 (pequeno) | Vulnerabilidade de seguranca ativa — jogador altera propria XP |

**Criterio de saida:** `PUT /fichas/{id}` nao aceita mais campos `xp` e `renascimentos`. Testes de regressao passando. Deploy imediato.

---

## Fase 0.5: Fundacao de Configuracao

> Sem as entidades de pontos por classe/raca e sem o DefaultProvider correto, o wizard de ficha nao pode calcular pontos disponiveis corretamente e todo jogo novo sera criado com dados incorretos.

**Spec 015: ConfigPontos Classe/Raca + DefaultProvider** — 7 tasks

| Task | Tipo | Descricao | Complexidade |
|------|------|-----------|-------------|
| T1 | Backend | Entidades, Repositories, DTOs e Mappers (4 novas entidades) | Media |
| T2 | Backend | CRUD Endpoints sub-recurso (14 endpoints) + Testes de integracao | Alta |
| T3 | Backend | Calculo de pontosDisponiveis com 3 fontes | Media |
| T4 | Backend | Auto-concessao de vantagens pre-definidas na criacao e level up | Alta |
| T5 | Backend | Corrigir DefaultProvider (BUG-DC-02..09) + adicionar defaults ausentes | Alta |
| T6 | Frontend | UI ClassePersonagem — abas Pontos por Nivel e Vantagens Pre-definidas | Media |
| T7 | Frontend | UI Raca — abas Pontos por Nivel e Vantagens Pre-definidas | Media |

**Estimativa:** 5 tasks backend + 2 tasks frontend = 7 total
**T5 (DefaultProvider) e independente:** pode ser paralelizada com Spec 007 ou qualquer outra task
**Deve ser concluida antes de:** Spec 006 T5 (pontosDisponiveis) e Spec 006 wizard completo

---

## Fase 1: Motor Correto (pre-requisito de tudo)

> Sem o motor correto, TODA ficha criada tera valores matematicamente errados.

**Spec 007: VantagemEfeito + Motor de Calculos** — 12 tasks

| Task | Tipo | Descricao | Complexidade |
|------|------|-----------|-------------|
| T1 | Backend | Adaptar modelo de dados para efeitos de vantagem | Media |
| T2 | Backend | FichaCalculationService — BONUS_ATRIBUTO e BONUS_APTIDAO | Alta |
| T3 | Backend | FichaCalculationService — BONUS_VIDA e BONUS_ESSENCIA | Media |
| T4 | Backend | FichaCalculationService — BONUS_DERIVADO e BONUS_VIDA_MEMBRO | Alta |
| T5 | Backend | FichaCalculationService — DADO_UP | Media |
| T6 | Backend | FichaCalculationService — FORMULA_CUSTOMIZADA | Alta |
| T7 | Backend | Insolitus — campo tipoVantagem + endpoint de concessao | Media |
| T8 | Backend | Testes de integracao para todos os tipos de efeito | Alta |
| T9 | Frontend | VantagensConfigComponent — secao de efeitos | Alta |
| T10 | Frontend | FormulaEditor integrado para FORMULA_CUSTOMIZADA | Media |
| T11 | Frontend | Seletor de dado para DADO_UP | Baixa |
| T12 | Frontend | UI de concessao de Insolitus pelo Mestre | Media |

**Estimativa:** 8 tasks backend + 4 tasks frontend = 12 total
**Pontos em aberto antes de iniciar T7:** PA-001 (remocao de Insolitus), PA-002 (enum vs boolean), PA-004 (alvo de FORMULA_CUSTOMIZADA)
**Impacto estimado pelo Tech Lead:** ~20-30 arquivos impactados, risco de regressao nos calculos existentes

---

## Fase 2: Ficha Funcional

> Tres specs que juntas entregam: ficha criada corretamente via wizard + controle de acesso por jogo.

### Spec 006: Wizard de Criacao de Ficha — 13 tasks

| Task | Tipo | Descricao | Complexidade | Depende de |
|------|------|-----------|-------------|-----------|
| T1 | Backend | Campo status + endpoint /completar | Media | — |
| T2 | Backend | Validacao RacaClassePermitida na criacao | Baixa | — |
| T3 | Backend | Bloquear XP no PUT para JOGADOR | Baixa | — |
| T4 | Backend | Endpoint PUT /fichas/{id}/xp (MESTRE-only) | Media | — |
| T5 | Backend | pontosDisponiveis no FichaResumoResponse | Alta | — |
| T6 | Frontend | Passo 1: Identificacao (rewrite do wizard) | Alta | T1 |
| T7 | Frontend | Passo 2: Descricao fisica | Baixa | T6 |
| T8 | Frontend | Passo 3: Distribuicao de atributos | Alta | T5, T6 |
| T9 | Frontend | Passo 4: Distribuicao de aptidoes | Media | T5, T6 |
| T10 | Frontend | Passo 5: Compra de vantagens iniciais | Alta | T5, T6 |
| T11 | Frontend | Passo 6: Revisao e confirmacao | Media | T1, T6 |
| T12 | Frontend | Auto-save visual (indicador de salvamento) | Baixa | T6 |
| T13 | Frontend | Badge "incompleta" na listagem de fichas | Baixa | T1 |

**Estimativa:** 5 tasks backend + 8 tasks frontend = 13 total

### Spec 005: Gestao de Participantes — 6 tasks

| Task | Tipo | Descricao | Complexidade | Depende de |
|------|------|-----------|-------------|-----------|
| P1-T1 | Backend | Corrigir logica de re-solicitacao e constraint | Alta | — |
| P1-T2 | Backend | Endpoints faltantes (banir, desbanir, remover, meu-status, cancelar, filtro) | Media | P1-T1 |
| P1-T3 | Backend | Testes de integracao completos | Media | P1-T1, P1-T2 |
| P2-T1 | Frontend | Alinhar API service e Business service | Baixa | P1-T2 |
| P2-T2 | Frontend | JogoDetail do Mestre (semantica remover/banir/desbanir) | Media | P2-T1 |
| P2-T3 | Frontend | JogosDisponiveis do Jogador (solicitar, status, cancelar) | Media | P2-T1 |

**Estimativa:** 3 tasks backend + 3 tasks frontend = 6 total

### Spec 008: Sub-recursos Classes/Racas — 4 tasks

| Info | Detalhe |
|------|---------|
| Escopo | Frontend: conectar ClasseBonus, ClasseAptidaoBonus, RacaBonusAtributo, RacaClassePermitida ao backend |
| Backend | 100% implementado (Spec 004) — 0 tasks backend |
| Status | spec+plan+tasks PRONTOS (T1-T4) |

**Estimativa:** 0 tasks backend + 4 tasks frontend = 4 total

**Subtotal Fase 2:** 8 tasks backend + 15 tasks frontend = 23 total

---

## Fase 3: Gestao e Progressao

### Spec 009-ext: NPC Visibility + Prospeccao + Essencia + Reset — 10 tasks

| Task | Tipo | Descricao | Complexidade |
|------|------|-----------|-------------|
| T1 | Backend | Campo visivelGlobalmente em Ficha | Baixa |
| T2 | Backend | FichaVisibilidade entity + endpoints | Alta |
| T3 | Backend | ProspeccaoUso entity + endpoints (usar/conceder/reverter) | Alta |
| T4 | Backend | Endpoint resetar-estado | Media |
| T5 | Backend | Verificar essenciaAtual no FichaResumoResponse | Baixa |
| T6 | Backend | Testes de integracao | Alta |
| T7 | Frontend | Toggle de visibilidade NPC | Alta |
| T8 | Frontend | Barra de essencia reativa | Baixa |
| T9 | Frontend | Prospeccao (usar + reverter) | Alta |
| T10 | Frontend | Painel de reset do Mestre | Media |

**Estimativa:** 6 tasks backend + 4 tasks frontend = 10 total

### Spec 012: Niveis e Level Up (frontend) — 12 tasks ativas

| Info | Detalhe |
|------|---------|
| Escopo | Frontend: PontosVantagemConfig CRUD, level up UI, progressao visual |
| Backend | Parcialmente implementado (PontosVantagemConfig CRUD existe) |
| Status | spec+plan+tasks PRONTOS (T1-T11, T14 ativas; T12/T13 fora MVP) |

**Estimativa:** 1 task backend (T5) + 11 tasks frontend = 12 total

**Subtotal Fase 3:** 7 tasks backend + 15 tasks frontend = 22 total

---

## Fase 4: Infraestrutura de Seguranca

> IMPLEMENTAR POR ULTIMO — impacto transversal em ~50+ @PreAuthorize em ~15 controllers.

### Spec 010: Roles ADMIN/MESTRE/JOGADOR — 9 tasks

| Task | Tipo | Descricao | Complexidade |
|------|------|-----------|-------------|
| T1 | Backend | Adicionar ADMIN ao model Usuario e enum | Media |
| T2 | Backend | Endpoint POST /me/role — onboarding | Media |
| T3 | Backend | Endpoints admin: listar usuarios e alterar role | Media |
| T4 | Backend | Atualizar todos os @PreAuthorize para incluir ADMIN | Alta |
| T5 | Banco | Seed SQL do primeiro ADMIN | Baixa |
| T6 | Frontend | Guards e redirect para onboarding | Media |
| T7 | Frontend | Wizard de onboarding | Media |
| T8 | Frontend | Tela de administracao de usuarios | Media |
| T9 | Teste | Testes de integracao para ADMIN em endpoints criticos | Alta |

**Estimativa:** 5 tasks backend + 3 tasks frontend + 1 teste = 9 total

**DECISAO RESOLVIDA (P-03):** ADMIN = apenas gestao de usuarios no MVP. SEM bypass de canAccessJogo. T3/T4 simplificadas.

---

## Fase 5: Enriquecimento (pos-MVP core)

### Spec 011: Galeria de Imagens e Anotacoes — 8 tasks

| Task | Tipo | Descricao | Estimativa |
|------|------|-----------|-----------|
| T1 | Backend | PUT Anotacao (edicao) | 0.5 dia |
| T2 | Backend | Entity FichaImagem + Repository | 0.5 dia |
| T3 | Backend | Service + Controller de Galeria | 1 dia |
| T4 | Backend | Testes de integracao de Galeria | 0.5 dia |
| T5 | Frontend | Edicao inline de Anotacao | 0.5 dia |
| T6 | Frontend | Model + API Service de Galeria | 0.5 dia |
| T7 | Frontend | Componentes e aba Galeria | 1.5 dias |
| T8 | Frontend | Testes de Galeria e Anotacao | 1 dia |

**Estimativa:** 4 tasks backend + 4 tasks frontend = 8 total (~6 dias de trabalho)

---

## Totais Consolidados

| Fase | Specs | Tasks Backend | Tasks Frontend | Total |
|------|-------|---------------|----------------|-------|
| 0. Correcao Urgente | 006 T3 | 1 | 0 | 1 |
| 0.5 Fundacao Config | 015 | 5 | 2 | 7 |
| 1. Motor Correto | 007 | 8 | 4 | 12 |
| 2. Ficha Funcional | 006 + 005 + 008 | 8 | 15 | 23 |
| 3. Gestao/Progressao | 009-ext + 012 | 7 | 15 | 22 |
| 4. Infraestrutura | 010 | 5 + 1T | 3 | 9 |
| 5. Enriquecimento | 011 | 4 | 4 | 8 |
| **TOTAL** | **9 specs** | **38** | **44** | **82** |

> Todas as specs agora tem tasks concretas. Total real: **82 tasks** (38 backend + 44 frontend).
> Spec 015 adicionada como Fase 0.5 — resolve GAP-PONTOS-CONFIG e bugs do DefaultProvider.

---

## Caminho Critico

```
[URGENTE] Bug XP (1 task)
    |
    v
Spec 007 Motor (12 tasks) ← GARGALO PRINCIPAL
    |                   \
    |                    Spec 015 (7 tasks) — T5 paralelizavel com 007
    |                   /
    v                  v
Spec 006 Wizard (13 tasks) ←→ Spec 008 Sub-recursos (paralelo, frontend-only)
    |
    v
Spec 005 Participantes (6 tasks)
    |
    v
Spec 009-ext NPC (10 tasks) ←→ Spec 012 Niveis (paralelo)
    |
    v
Spec 010 Roles (9 tasks) ← IMPLEMENTAR POR ULTIMO
    |
    v
Spec 011 Galeria (8 tasks) ← pos-MVP core
```

---

## Conceitos Fora do MVP (por decisao do PO ou design)

| Conceito | Razao | Referencia |
|----------|-------|-----------|
| Renascimento como mecanica completa | Fim de jogo (nivel 31+), pos-MVP | GLOSSARIO-GAPS GAP-DOMAIN-01 |
| XP em lote (toda a mesa) | Endpoint individual suficiente para MVP | GLOSSARIO-GAPS GAP-DOMAIN-10 |
| Modo Sessao formal (entidade) | Sistema funciona sem sessao formal | GLOSSARIO-GAPS GAP-DOMAIN-06 |
| Sistema de Itens/Equipamentos | Campos manuais sao suficientes | GLOSSARIO-GAPS GAP-DOMAIN-07 |
| Upload de imagens (binario/S3) | MVP usa URLs externas | Spec 011 decisao |
| Notificacoes (email/push) | Nao e MVP | Spec 005 PA-004 |
| Transferencia de propriedade do Jogo | Nao e MVP | Spec 005 PA-002 |
| Cooldown entre REJEITADO → PENDENTE | Sem cooldown (decisao PO) | Spec 005 |

---

*Produzido por: PM/Scrum Master | 2026-04-03 | Revisado: 2026-04-04 — adicionada Fase 0.5 (Spec 015)*
*Base: GLOSSARIO-GAPS.md, TECH-LEAD-BACKEND-REVIEW.md, INDEX.md de todas as specs, DEFAULT-CONFIG-AUDITORIA.md*
