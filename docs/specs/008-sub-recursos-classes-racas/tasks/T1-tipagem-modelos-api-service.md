# T1 — Corrigir tipagem: modelos TypeScript + metodos do API service

> **Complexidade:** pequena
> **Depende de:** —
> **Bloqueia:** T2, T3, T4
> **Arquivo principal:** `config.models.ts` + `config-api.service.ts`

---

## Objetivo

Eliminar os `unknown` nos metodos de sub-recurso do `ConfigApiService` e adicionar os campos
ausentes nas interfaces `ClasseBonusConfig` e `ClasseAptidaoBonus`. Esta task e pre-requisito
para T2 e T3 — sem tipagem correta, o TypeScript impedira a compilacao das mudancas nos componentes.

---

## Contexto

### Problema 1: campos ausentes nos modelos

`ClasseBonusConfig` (em `config.models.ts`) nao tem `valorPorNivel`.
`ClasseAptidaoBonus` nao tem `bonus`.

O backend retorna esses campos na resposta do `GET /classes/{id}`, mas o frontend os descarta
por falta de mapeamento no modelo TypeScript.

### Problema 2: DTOs de POST incompletos no API service

`addClasseBonus` envia `{ bonusConfigId: number }` — falta `valorPorNivel`.
`addClasseAptidaoBonus` envia `{ aptidaoConfigId: number }` — falta `bonus`.

### Problema 3: retornos `unknown` nos metodos de lista

`listClasseBonus`, `listClasseAptidaoBonus`, `listRacaBonusAtributos`, `listRacaClassesPermitidas`
retornam `Observable<unknown[]>`. Esses metodos nao sao chamados diretamente nos componentes
(os componentes usam `refreshSelectedClasse/Raca` que chama `getClasse/getRaca`), mas a tipagem
`unknown` e um debito tecnico que deve ser corrigido.

---

## Arquivos Afetados

1. `src/app/core/models/config.models.ts`
2. `src/app/core/services/api/config-api.service.ts`

---

## Passos de Implementacao

### Passo 1 — Corrigir `config.models.ts`

**Interface `ClasseBonusConfig` (linha ~19):**
Adicionar campo `valorPorNivel: number`.

Estado atual:
```typescript
export interface ClasseBonusConfig {
  id: number;
  classeId: number;
  bonusConfigId: number;
  bonusNome: string;
}
```

Estado alvo:
```typescript
export interface ClasseBonusConfig {
  id: number;
  classeId: number;
  bonusConfigId: number;
  bonusNome: string;
  valorPorNivel: number;
}
```

**Interface `ClasseAptidaoBonus` (linha ~29):**
Adicionar campo `bonus: number`.

Estado atual:
```typescript
export interface ClasseAptidaoBonus {
  id: number;
  classeId: number;
  aptidaoConfigId: number;
  aptidaoNome: string;
}
```

Estado alvo:
```typescript
export interface ClasseAptidaoBonus {
  id: number;
  classeId: number;
  aptidaoConfigId: number;
  aptidaoNome: string;
  bonus: number;
}
```

### Passo 2 — Corrigir `config-api.service.ts`

**Metodo `listClasseBonus` (linha ~192):**
Alterar retorno de `Observable<unknown[]>` para `Observable<ClasseBonusConfig[]>`.
Importar `ClasseBonusConfig` se necessario.

**Metodo `addClasseBonus` (linha ~196):**
Alterar assinatura do DTO: `dto: { bonusConfigId: number }` -> `dto: { bonusConfigId: number; valorPorNivel: number }`.
Alterar retorno de `Observable<unknown>` para `Observable<ClasseBonusConfig>`.

**Metodo `listClasseAptidaoBonus` (linha ~204):**
Alterar retorno de `Observable<unknown[]>` para `Observable<ClasseAptidaoBonus[]>`.

**Metodo `addClasseAptidaoBonus` (linha ~208):**
Alterar assinatura: `dto: { aptidaoConfigId: number }` -> `dto: { aptidaoConfigId: number; bonus: number }`.
Alterar retorno de `Observable<unknown>` para `Observable<ClasseAptidaoBonus>`.

**Metodo `listRacaBonusAtributos` (linha ~316):**
Alterar retorno de `Observable<unknown[]>` para `Observable<RacaBonusAtributo[]>`.

**Metodo `addRacaBonusAtributo` (linha ~320):**
Alterar retorno de `Observable<unknown>` para `Observable<RacaBonusAtributo>`.

**Metodo `listRacaClassesPermitidas` (linha ~328):**
Alterar retorno de `Observable<unknown[]>` para `Observable<RacaClassePermitida[]>`.

**Metodo `addRacaClassePermitida` (linha ~332):**
Alterar retorno de `Observable<unknown>` para `Observable<RacaClassePermitida>`.

Verificar que `ClasseBonusConfig`, `ClasseAptidaoBonus`, `RacaBonusAtributo`, `RacaClassePermitida`
estao importados no topo do arquivo (provavelmente via `@core/models`).

---

## Criterios de Aceitacao

- [ ] `ClasseBonusConfig` tem campo `valorPorNivel: number` em `config.models.ts`
- [ ] `ClasseAptidaoBonus` tem campo `bonus: number` em `config.models.ts`
- [ ] `addClasseBonus` aceita `{ bonusConfigId: number; valorPorNivel: number }` no tipo
- [ ] `addClasseAptidaoBonus` aceita `{ aptidaoConfigId: number; bonus: number }` no tipo
- [ ] Todos os metodos `list*` de sub-recurso retornam tipos concretos (sem `unknown`)
- [ ] `npm run build` sem erros de TypeScript
- [ ] `npx vitest run` sem regressao (baseline: 271 testes passando)

---

## Observacoes

- Nao alterar a logica dos componentes nesta task — apenas tipagem.
- Apos esta task, o TypeScript pode indicar erros de compilacao em `ClassesConfigComponent`
  onde `addClasseBonus` e chamado sem `valorPorNivel` — esses erros serao corrigidos em T2.
- Verificar se `ClasseBonusConfig` e `ClasseAptidaoBonus` sao exportados pelo barrel `index.ts`
  de models — se nao, adicionar as exportacoes necessarias.
