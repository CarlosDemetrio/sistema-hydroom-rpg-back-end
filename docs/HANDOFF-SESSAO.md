# Handoff de Sessao — 2026-04-13 (sessao 19 — Waves 1+2+3 completas)

> Branch atual: `main`
> Backend: **796 testes** passando, 0 falhas
> Frontend: **~1208 testes** passando (2 falhas pre-existentes ficha-vantagens-tab)
> Sprint 3: RC desbloqueado — homologacao em andamento
> Ultima atualizacao: 2026-04-13

---

## Resumo Executivo

**Sessao 2026-04-13** entregou em tres waves paralelas:
1. **Spec 016 T5 BE** — FichaCalculationService Passo 6 (recalculo com itens equipados), +10 testes backend
2. **Spec 016 T8+T9+T10 FE** — Telas de raridades, tipos de item, catalogo e classe de equipamento, +89 testes frontend
3. **Spec 021 T1 BE** — HabilidadeConfig CRUD completo (entity + service + controller + testes), +15 testes backend
4. **Spec 016 T11 FE** — Inventario na FichaDetail (aba equipamentos), +40 testes frontend
5. **Spec 021 T2 FE** — HabilidadeConfig frontend Mestre + Jogador, +30 testes frontend
6. **Spec 017 P2 T13+T14+T-DOC1** — Encontrados pre-implementados (OAuth interceptor, doc)
7. **Spec 007 T9+T11** — Encontrados pre-implementados (efeitos FE + DadoUp, 53 testes)
8. **Spec 021 BA** — Spec HabilidadeConfig escrita (commit anterior b8c3c3d)

Backend saltou de 771 para **796 testes** (+25). Frontend saltou de 1006 para **~1208 testes** (+202).

---

## Wave 1 — 2026-04-13 (5 entregas)

| Spec/Task | Status | Resultado | Commits |
|-----------|--------|-----------|---------|
| Spec 007 T9+T11 (FE efeitos + DadoUp) | Pre-implementado | 53 testes, ja existia | — |
| Spec 017 P2 T13+T14+T-DOC1 (OAuth+interceptor+doc) | Pre-implementado | ja existia | `83f17ac` |
| Spec 021 BA (spec HabilidadeConfig) | Concluido | spec escrita | `b8c3c3d` |
| Spec 016 T5 BE (FichaCalculationService Passo 6 itens) | Concluido | 781 testes (+10) | `c2b4522`, `74c2d36` |
| Spec 016 T8+T9+T10 FE (raridades, tipos, catalogo, classe equip) | Concluido | 1138 testes (+89) | `944739e`, `95a00a9`, `f4f9736` |

## Wave 2 — 2026-04-13 (2 entregas) — CONCLUIDA

| Spec/Task | Status | Resultado | Commits |
|-----------|--------|-----------|---------|
| Spec 021 T1 BE (HabilidadeConfig CRUD) | Concluido | 796 testes (+15) | `b0e84c4`, `39f39ed` |
| Spec 016 T11 FE (Inventario FichaDetail) | Concluido | +40 testes frontend | `6b88997` |

## Wave 3 — 2026-04-13 (1 entrega) — CONCLUIDA

| Spec/Task | Status | Resultado | Commits |
|-----------|--------|-----------|---------|
| Spec 021 T2 FE (HabilidadeConfig Mestre + Jogador) | Concluido | +30 testes frontend | `caa0d2c` |

---

## Decisoes e Pontos de Atencao

### PA-021-03 — Resolvido

Decisao do PO: a tela de habilidades para JOGADOR ficara em **secao separada** (nao no painel de configuracoes do Mestre).

### Ponto tecnico: path HabilidadeConfig — RESOLVIDO

`HabilidadeConfigController` usa path `/api/jogos/{jogoId}/config/habilidades` (padrao de `CategoriaVantagemController`). Frontend T2 (commit `caa0d2c`) ja implementado com esse path. Mestre em `/mestre/config/habilidades`, Jogador em `/jogador/habilidades`.

---

## Estado das Specs (atualizado pos-Waves 1+2+3)

| Spec | Titulo | Status | Nota |
|------|--------|--------|------|
| 004 | Siglas, formulas, relacionamentos | CONCLUIDO | — |
| 005 | Participantes, aprovacao, permissoes | CONCLUIDO | — |
| 006 | Wizard de criacao de ficha | CONCLUIDO | — |
| 007 | VantagemEfeito + Motor de calculos | 12/13 | T10 DESBLOQUEADO (PA-004 resolvido) |
| 008 | Sub-recursos Classes/Racas (FE) | CONCLUIDO | — |
| 009-ext | NPC Visibility + Prospeccao + Essencia | CONCLUIDO | — |
| 010 | Roles ADMIN refactor | STAND-BY | pos-homologacao |
| 011 | Galeria + Anotacoes | CONCLUIDO 9/9 | — |
| 012 | Niveis e progressao (FE) | CONCLUIDO | — |
| 013 | Documentacao tecnica | STAND-BY | pos-homologacao |
| 014 | Cobertura de testes | Parcial | T1+T5 OK; T2-T4+T6 pendentes |
| 015 | ConfigPontos Classe/Raca | Parcial | T4 DESBLOQUEADO (PA-015-04 resolvido) |
| 016 | Sistema de Itens | **CONCLUIDO** | T1-T11 todos concluidos (T5 Wave 1, T8-T10 Wave 1, T11 Wave 2) |
| 017 | Correcoes Pre-RC | **CONCLUIDO** | P0+P1+P2+P3 — T15-T21 pre-implementados |
| 018 | Deploy Backend GCP | CONCLUIDO | +hotfixes infra |
| 019 | Deploy Frontend Firebase | CONCLUIDO | — |
| 021 | Sistema de Habilidades | **CONCLUIDO** | BA + T1 BE (Wave 2) + T2 FE (Wave 3) |
| 022 | DefaultGameConfigProvider refactor | CONCLUIDO | 11 providers + facade + 64 vantagens |

---

## Bloqueados / Pontos em Aberto

### PAs acumulados

| ID | Descricao | Bloqueia | Proxima acao |
|----|-----------|----------|--------------|
| PA-R05-01 | FichaPreviewResponse incompleto (sem aptidoes/dado prospeccao) | Nao (decisao PO) | PO decide se amplia resposta |
| PA-R05-02 | FichaPreviewService sem testes avancados | Nao | Pos-RC |
| PA-R02-01 | Spec 016 T5 — FichaItemService 4x TODO recalcularStats | **RESOLVIDO** (Wave 1) | — |
| PA-004 | FormulaEditorEfeito (S007-T10) | **RESOLVIDO** | Editor permite selecionar campo-alvo (atributo/bonus); 1 formula por campo |
| PA-015-04 | Enum origem FichaVantagem (S015-T4) | **RESOLVIDO** | Enum OrigemFichaVantagem: JOGADOR, MESTRE, SISTEMA |
| PA-017-03 | Reativar SidebarComponent (T15, P3) | Nao | Pos-RC |
| PA-017-04 | Exportar/Importar config — formato | Nao | Pos-RC |
| PA-021-03 | Tela habilidades JOGADOR em secao separada | **RESOLVIDO** | Decisao PO registrada |

### Outros bloqueios

- Nenhum bloqueio ativo. S007-T10 e S015-T4 agora DESBLOQUEADOS.

---

## Proxima Sessao — Homologacao / RC

Todas as waves da sessao 19 foram concluidas. Spec 016, Spec 021 e Spec 017 estao 100%. PA-004 e PA-015-04 resolvidos pelo PO — desbloqueiam S007-T10 e S015-T4. O foco agora e **homologacao e validacao para RC** + implementacao das tasks desbloqueadas.

### Decisoes de PO registradas (sessao 19)

**PA-004 RESOLVIDO — FormulaEditor (Spec 007 T10):**
- O editor de formula customizada permite selecionar **para qual campo** (atributo ou bonus) o resultado da formula ira
- Pode-se adicionar uma formula por campo de atributo/bonus na vantagem
- Desbloqueia task T10 (FormulaEditor)

**PA-015-04 RESOLVIDO — enum `origem` em FichaVantagem (Spec 015 T4):**
- Criar enum `OrigemFichaVantagem` com valores: `JOGADOR`, `MESTRE`, `SISTEMA`
- Desbloqueia task T4 (auto-concessao de vantagens pre-definidas no level up)

### Prioridade 1: Validacao em producao

| ID | Cenario | Criticidade |
|----|---------|-------------|
| BUG-PROD-01 | Validar CSS em hydrooon.com.br | Alta |
| BUG-PROD-02 | Validar login OAuth sem "Erro 0" | Alta |
| PA-R04-02 | Smoke test overlay clipping em 5 telas | Media |
| PA-R04-03 | Decidir badge severity ficha-vantagens-tab (fix ou known issue) | Baixa |
| PA-R04-04 | Decidir OOM ficha-wizard-passo4 (fix ou known issue) | Baixa |

### Prioridade 2: Verificacoes pendentes

1. **S007-T12** — Insolitus UI — possivelmente pre-implementado (sem arquivo de task)
2. **Path HabilidadeConfig** — frontend T2 ja implementado com `/api/jogos/{jogoId}/config/habilidades` — confirmar se funciona em producao

### Prioridade 3: Tasks desbloqueadas

- **S007-T10** — FormulaEditorEfeito (PA-004 resolvido) — frontend
- **S015-T4** — VantagemAutoConcessao com enum OrigemFichaVantagem (PA-015-04 resolvido) — backend

---

## Pos-RC (ordem de prioridade)

1. **Spec 014 T2-T4+T6** — cobertura testes (JaCoCo 50% para 75%)
2. **Spec 013** — Documentacao tecnica
3. **Spec 010** — Roles ADMIN refactor

---

## Observacoes Tecnicas

- Frontend budget warning pre-existente: bundle 1.14MB vs limite 1MB (nao bloqueia)
- ficha-wizard OOM pre-existente: 2 timeouts (nao bloqueia)
- `application.properties` e `application-dev.properties` com linhas concatenadas: follow-up pos-RC
- Telas sem PageHeader por decisao tecnica: `fichas-list`, `jogos-disponiveis` (telas-destino)
- Toast com `key` isolado mantido: `npc-visibilidade`, `prospeccao`
- MarkdownPipe usa fallback basico sem `marked` instalado (negrito, italico, headers, code inline)
- `tipoImagem` imutavel apos upload — para promover GALERIA para AVATAR: novo upload com tipo AVATAR
- GraalVM native image funcional com distroless/cc-debian12 (apos ~9 fixes de reflection)
- Structured logging GCP com formato ECS (nao GCP nativo — melhor compatibilidade)
- Micrometer Prometheus configurado para observability
- OAuth2 com timeouts de 15s no token exchange e userinfo
- vpc-egress configurado como private-ranges-only (nao all-traffic)
- HabilidadeConfigController usa `/api/jogos/{jogoId}/config/habilidades` (nao `/api/v1/`) — frontend T2 ja implementado com esse path
