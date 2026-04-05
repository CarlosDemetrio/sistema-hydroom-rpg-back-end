# Spec 008 — Plano de Implementacao

> **Status:** PLANEJADO | 2026-04-02
> **Prerequisito:** Backend 100% implementado. Ver `docs/analises/BA-CLASSES-RACAS.md`.

---

## O que ja existe no Frontend

### ClassesConfigComponent
`src/app/features/mestre/pages/config/configs/classes-config/classes-config.component.ts`

**O que esta implementado:**
- CRUD basico (create, update, delete, reorder) via `ClasseConfigService`
- Drawer com 3 abas: "Dados Gerais", "Bônus", "Aptidoes c/ Bônus"
- Aba "Bônus": lista os `bonusConfig` do objeto `ClassePersonagem` com nome e formula; botao remover funcional (`removeClasseBonus`); dropdown de BonusConfig disponiveis (filtra os ja vinculados); botao "Adicionar" chama `configApi.addClasseBonus`
- Aba "Aptidoes c/ Bônus": lista `aptidaoBonus` com nome; botao remover funcional (`removeClasseAptidaoBonus`); dropdown de AptidaoConfig disponiveis; botao "Adicionar" chama `configApi.addClasseAptidaoBonus`
- Sub-recursos carregados via `loadSubResources()` ao `ngOnInit`

**O que esta faltando / com defeito:**
- `ClasseBonusConfig.valorPorNivel` ausente na interface TypeScript — o campo existe no backend mas nao no modelo frontend
- `addClasseBonus` envia apenas `{ bonusConfigId: number }` — nao envia `valorPorNivel`
- A aba "Bônus" nao tem campo de input para `valorPorNivel`
- `ClasseBonusConfig.bonusNome` exibe o nome, mas `valorPorNivel` nao e exibido na lista
- Os metodos `listClasseBonus`, `addClasseBonus`, `listClasseAptidaoBonus`, `addClasseAptidaoBonus` no API service retornam/aceitam `unknown` em vez de tipos concretos
- `ClasseAptidaoBonus` nao tem campo `bonus` no modelo TypeScript (apenas `aptidaoNome`)
- `addClasseAptidaoBonus` envia apenas `{ aptidaoConfigId: number }` — nao envia `bonus`

### RacasConfigComponent
`src/app/features/mestre/pages/config/configs/racas-config/racas-config.component.ts`

**O que esta implementado:**
- CRUD basico (create, update, delete, reorder) via `RacaConfigService`
- Drawer com 3 abas: "Dados Gerais", "Bônus em Atributos", "Classes Permitidas"
- Aba "Bônus em Atributos": lista `bonusAtributos` com nome e valor (verde/vermelho); input de bonus numerico (min=-99, max=99); botao remover funcional; dropdown de AtributoConfig disponiveis
- Aba "Classes Permitidas": lista `classesPermitidas` com nome; estado vazio "Todas as classes sao permitidas"; dropdown de ClassePersonagem disponiveis; botao remover funcional
- Sub-recursos carregados via `ngOnInit`

**O que esta faltando / com defeito:**
- `listRacaBonusAtributos`, `addRacaBonusAtributo`, `listRacaClassesPermitidas`, `addRacaClassePermitida`, `removeRacaClassePermitida` retornam/aceitam `unknown` no API service
- Badge na tabela principal (coluna "Bônus" / "Classes") nao existe — a tabela so exibe Ordem, Nome, Descricao
- Coluna customizada de preview na tabela principal (ex: "2 bônus | 1 restricao") nao implementada
- Indicador textual "(penalidade)" para valores negativos nao esta presente (so a cor vermelha)

### ConfigApiService
`src/app/core/services/api/config-api.service.ts`

**Metodos de sub-recurso existentes (com problemas de tipagem):**

| Metodo | Problema atual |
|--------|---------------|
| `listClasseBonus(classeId)` | Retorna `Observable<unknown[]>` |
| `addClasseBonus(classeId, dto)` | Aceita `{ bonusConfigId: number }` — falta `valorPorNivel` |
| `removeClasseBonus(classeId, bonusId)` | OK — retorna `Observable<void>` |
| `listClasseAptidaoBonus(classeId)` | Retorna `Observable<unknown[]>` |
| `addClasseAptidaoBonus(classeId, dto)` | Aceita `{ aptidaoConfigId: number }` — falta `bonus` |
| `removeClasseAptidaoBonus(classeId, aptidaoBonusId)` | OK — retorna `Observable<void>` |
| `listRacaBonusAtributos(racaId)` | Retorna `Observable<unknown[]>` |
| `addRacaBonusAtributo(racaId, dto)` | OK — aceita `{ atributoConfigId: number; bonus: number }` |
| `removeRacaBonusAtributo(racaId, id)` | OK — retorna `Observable<void>` |
| `listRacaClassesPermitidas(racaId)` | Retorna `Observable<unknown[]>` |
| `addRacaClassePermitida(racaId, dto)` | OK — aceita `{ classeId: number }` |
| `removeRacaClassePermitida(racaId, id)` | OK — retorna `Observable<void>` |

### Modelos TypeScript existentes
`src/app/core/models/config.models.ts`

| Interface | Problema atual |
|-----------|---------------|
| `ClasseBonusConfig` | Falta `valorPorNivel: number` |
| `ClasseAptidaoBonus` | Falta `bonus: number` |
| `RacaBonusAtributo` | OK — tem `bonus: number` |
| `RacaClassePermitida` | OK — tem `classeNome: string` |
| `ClassePersonagem` | OK — tem `bonusConfig[]` e `aptidaoBonus[]` |
| `Raca` | OK — tem `bonusAtributos[]` e `classesPermitidas[]` |

---

## Estrategia de Implementacao

### Abordagem: Corrigir tipagem primeiro, depois componentes

A maioria dos flows de UI ja existe nos componentes. As mudancas necessarias sao:

1. **Corrigir modelos TypeScript** — adicionar `valorPorNivel` e `bonus` nas interfaces
2. **Corrigir API service** — tipar retornos `unknown` com interfaces corretas; atualizar DTOs de entrada
3. **Corrigir ClassesConfigComponent** — adicionar campo `valorPorNivel` na aba Bônus; adicionar campo `bonus` na aba Aptidoes
4. **Melhorar RacasConfigComponent** — adicionar badge de restricao de classe na tabela; adicionar label "(penalidade)" nos valores negativos
5. **Testes** — cobertura dos metodos de API service e dos novos campos

### Integracao nos componentes existentes

**Nao e necessario criar novos componentes** — as abas ja existem nos drawers de ClassesConfig e RacasConfig. As mudancas sao cirurgicas:

- `ClassesConfigComponent`: adicionar `p-input-number [step]="0.01"` para `valorPorNivel`; exibir valor na lista; passar ao POST
- `RacasConfigComponent`: adicionar coluna/badge na tabela; adicionar "(penalidade)" no display; nenhuma mudanca estrutural necessaria

### Estrutura de arquivos afetados

```
src/app/core/models/
  config.models.ts                          ← T1: corrigir ClasseBonusConfig, ClasseAptidaoBonus

src/app/core/services/api/
  config-api.service.ts                     ← T1: tipar unknown[], corrigir DTOs de POST

src/app/features/mestre/pages/config/configs/
  classes-config/
    classes-config.component.ts             ← T2: campo valorPorNivel na aba Bônus; campo bonus na aba Aptidoes
  racas-config/
    racas-config.component.ts               ← T3: badge restricao na tabela; label penalidade

src/app/core/services/api/
  config-api.service.spec.ts                ← T4: testes para metodos de sub-recurso
```

---

## Contrato de API (referencia)

Ver secao 8 de `docs/analises/BA-CLASSES-RACAS.md` para contrato completo.

**Resumo dos DTOs de POST (o que o frontend deve enviar):**

```typescript
// POST /classes/{id}/bonus
{ bonusConfigId: number; valorPorNivel: number }

// POST /classes/{id}/aptidao-bonus
{ aptidaoConfigId: number; bonus: number }

// POST /racas/{id}/bonus-atributos
{ atributoConfigId: number; bonus: number }

// POST /racas/{id}/classes-permitidas
{ classeId: number }
```

**Respostas esperadas (o que o backend retorna):**

```typescript
// ClasseBonusConfig (GET /classes/{id} embarca este objeto)
{ id: number; classeId: number; bonusConfigId: number; bonusNome: string; valorPorNivel: number }

// ClasseAptidaoBonus
{ id: number; classeId: number; aptidaoConfigId: number; aptidaoNome: string; bonus: number }
```

---

## Dependencias entre Tasks

```
T1 (modelos + API service tipagem)
  ↓
  T2 (ClassesConfigComponent — valorPorNivel + bonus)
  T3 (RacasConfigComponent — badge + penalidade)
  ↓
  T4 (testes — depende de T1 para ter tipos corretos)
```

T2 e T3 podem ser desenvolvidas em paralelo apos T1.
