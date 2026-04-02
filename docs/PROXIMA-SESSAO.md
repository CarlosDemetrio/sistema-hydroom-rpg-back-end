# Proxima Sessao — Ponto de Retomada

> Atualizado: 2026-04-02 (fim de sessao 5)
> Branch frontend: `feature/009-npc-fichas-mestre` (ou main — verificar)
> Branch backend: `feature/009-npc-fichas-mestre`

---

## Estado Atual do Build e Testes

### Backend
- **457 testes passando** (era 422 — +35 novos testes de integracao)
- GET /fichas/{id}/atributos: **JA IMPLEMENTADO** (estava no codigo, agente adicionou 8 testes)
- GET /fichas/{id}/aptidoes: **JA IMPLEMENTADO** (estava no codigo, agente adicionou 8 testes)
- categoriaNome em FichaVantagemResponse: **JA IMPLEMENTADO** (estava no codigo, agente adicionou 2 testes)
- **Security fixes SP1-T18: CODIFICADOS MAS NAO COMMITADOS** (6 arquivos em service/)

### Frontend
- **Build Angular**: 0 erros, 0 warnings
- **Testes Vitest**: parcialmente corrigidos — 32 passando / 34 falhando (sessao 5 corrigi inputBinding → componentInputs)
- Commits pendentes (mudancas nao commitadas desta sessao)

---

## ACAO IMEDIATA — Commits Pendentes

### Backend (repo ficha-controlador, branch feature/009-npc-fichas-mestre)

```bash
# SP1-T18: security fixes
git add src/main/java/.../service/FichaAnotacaoService.java \
        src/main/java/.../service/FichaPreviewService.java \
        src/main/java/.../service/FichaResumoService.java \
        src/main/java/.../service/FichaService.java \
        src/main/java/.../service/FichaVantagemService.java \
        src/main/java/.../service/FichaVidaService.java
git commit -m "fix(security): bloquear NPCs para Jogadores + fixes N+1 — SP1-T18"

# Novos testes de integracao (GET atributos/aptidoes + categoriaNome)
git add src/test/java/.../service/FichaServiceIntegrationTest.java
git add src/test/java/.../service/FichaVantagemServiceIntegrationTest.java
git commit -m "test(ficha): testes de integracao GET atributos, aptidoes e categoriaNome"
```

### Frontend (repo ficha-controlador-front-end)

```bash
# Fix testes — inputBinding → componentInputs (NG0315 corrigido)
git add src/app/shared/components/formula-editor/formula-editor.component.spec.ts
git add src/app/shared/components/base-config/base-config-table.component.spec.ts
git add src/app/features/mestre/pages/config/configs/atributos-config/atributos-config.component.spec.ts
git commit -m "fix(tests): corrigir specs — substituir inputBinding por componentInputs"

# FormulaEditorComponent + Quick Wins + handleReorder wiring
git add src/app/shared/components/formula-editor/
git add src/app/features/mestre/pages/config/configs/  # handleReorder wired
git commit -m "feat(formula-editor): FormulaEditorComponent com chips, preview e validacao"
git commit -m "feat(config): conectar handleReorder ao ConfigApiService em 12 componentes"
git commit -m "feat(ux): quick wins QW-2 badges sidebar, QW-3 aviso aptidoes, QW-5 tooltips"
```

---

## Testes Frontend — O que Falta Corrigir

**Status apos sessao 5**: 32/66 passando com 2 spec files ainda falhando

### formula-editor.component.spec.ts
- **Fix aplicado**: inputBinding → componentInputs + NO_ERRORS_SCHEMA + componentImports sem InputNumberModule
- **Falhas restantes**: chips de variaveis (p-tag: 0 vs 3 esperado)
  - Causa provavel: TagModule nao funciona via componentImports override em JIT
  - Solucao: testar via `(component as any).todasVariaveis().length` em vez de querySelector('p-tag')
  - Ou: incluir TagModule normalmente e verificar se PrimeNG Tag renderiza como `p-tag` no DOM

### base-config-table.component.spec.ts
- **Fix aplicado**: inputBinding → componentInputs
- **Status**: nao confirmado (sessao interrompida antes de rodar)
- **Risco**: required inputs (`titulo`, `items`, `columns` sao `input.required<>()`) — se componentInputs nao funcionar, vai falhar com "required input not set"

### atributos-config.component.spec.ts
- **Fix aplicado**: ConfigApiService mock adicionado ao providers
- **Status**: deve estar passando (fix simples e correto)

---

## Proximos Passos Prioritarios

### P0 — Fix testes frontend (continuar de onde parou)

1. **Rodar npm test** para ver estado atual dos testes com as correcoes aplicadas
2. **Corrigir chips tests** em formula-editor: substituir querySelector('p-tag') por contagem via `todasVariaveis().length`
3. **Confirmar base-config-table** com componentInputs
4. **Meta**: 0 testes falhando antes de qualquer nova feature

### P1 — Commits pendentes

5. Commitar security fixes backend (SP1-T18)
6. Commitar testes de integracao novos backend
7. Commitar fixes frontend (spec files + formula editor + QW)

### P2 — Sprint 2 Features (em ordem de prioridade)

Ver secao de repriorization abaixo — mantida da sessao 4.

---

## Repriorization Sprint 2 (inalterada da sessao 4)

### P0 — Critico (proxima implementacao)

- [ ] **US-FICHA-01** — Reescrita FichaForm wizard (envia apenas {nome} atualmente)
- [ ] **US-FICHA-05** — Tela de NPCs para o Mestre (ausente totalmente)
- [ ] **US-FICHA-04** — Concessao de XP pelo Mestre (progressao bloqueada)
- [ ] **US-FICHA-02 + US-FICHA-03** — Atributos e aptidoes reais no FichaDetail

### P1 — Alta prioridade

- [ ] **SP2-T01** — FormulaEditorComponent (CRIADO nesta sessao, integrar nos forms de config)
- [ ] **M1** — PontosVantagem CRUD no frontend (zero cobertura)
- [ ] **US-FICHA-07** — Marketplace de vantagens
- [ ] **M2 + M3** — FormulaController + SiglaController API services no frontend
- [ ] **SP2-T02** — Sub-recursos Classe (ClasseBonus + ClasseAptidaoBonus UI)
- [ ] **SP2-T03** — Sub-recursos Raca (RacaBonusAtributo + RacaClassePermitida UI)

### P2 — Media prioridade

- [ ] US-FICHA-06 — Barras de Vida e Essencia reativas
- [ ] C2 — Dashboard Mestre com dados reais
- [ ] M7 — Edicao de perfil UI
- [ ] SP2-T04 — Color picker CategoriaVantagem
- [ ] SP2-T05 — Validacao async unicidade de sigla

### Divida Tecnica

- [ ] C1 — handleReorder wiring para o 13o componente (12/13 feito)
- [ ] DT-FE-01 — atualizarAnotacao() fantasma
- [ ] DT-FE-02 — CategoriaVantagem URL corrigida
- [ ] SP1-T23 — Testes de integracao FichaController
- [ ] SP1-T27 — DDL de producao (3 ALTER TABLE)
- [ ] SP1-T13, T14, T17 — Barras HP, Participantes UI, Skeletons

---

## Endpoints Backend Disponiveis (prontos para frontend consumir)

| Endpoint | Status | Usado no frontend? |
|----------|--------|--------------------|
| GET /api/v1/fichas/{id}/atributos | Implementado + testado | NAO — atributos mockados no FichaDetail |
| GET /api/v1/fichas/{id}/aptidoes | Implementado + testado | NAO — aptidoes vazias no FichaDetail |
| FichaVantagemResponse.categoriaNome | Implementado | NAO — frontend ignora esse campo |
| GET /api/v1/fichas?isNpc=true | Existe | NAO — tela NPC ausente |
| POST /api/v1/fichas (isNpc=true) | Existe | NAO |
| PUT /api/v1/fichas/{id}/vida | Existe | Parcialmente |
| GET /api/v1/usuarios/me | Existe | NAO — tela perfil ausente |
| PUT /api/v1/usuarios/me | Existe | NAO |
| POST /api/v1/formulas/preview | Existe | NAO |
| GET /api/v1/formulas/variaveis | Existe | NAO |
| GET /api/v1/siglas/{jogoId} | Existe | NAO |

---

## Causa Raiz: Falha nos Testes (para referencia futura)

**Problema**: `inputBinding` de `@angular/core` em `render({ bindings })` do @testing-library/angular aplica os bindings no wrapper component, nao no componente real — NG0315.

**Solucao**: Usar `componentInputs` que chama `fixture.componentRef.setInput()` diretamente.

**InputNumber CD loop**: Remover InputNumberModule via `componentImports` + adicionar `NO_ERRORS_SCHEMA` nos testes que usam FormulaEditorComponent.

**p-tag chips**: `p-tag` com `TagModule` via `componentImports` pode nao renderizar como elemento `p-tag` no DOM (PrimeNG pode criar shadow DOM). Testar via logica do componente em vez do DOM.
