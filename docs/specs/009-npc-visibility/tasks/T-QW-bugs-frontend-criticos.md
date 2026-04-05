# T-QW — Quick Wins: Bugs Criticos de Frontend (Spec 009-ext)

> Task: T-QW-bugs-frontend-criticos
> Tipo: Frontend
> Prioridade: ALTA — pode ser feita agora, sem depender de outras tasks desta spec
> Dependencias: nenhuma (T5 desbloqueia a correcao completa do Bug 1, mas a task pode comecar antes)
> Estimativa: ~2h total (Bug 1: 1h30 | Bug 2: 15min | Bug 3: 5min)
> Branch sugerido: pode ir no branch atual `feature/009-npc-fichas-mestre` ou ser um hotfix

---

## Contexto

A auditoria UX `docs/analises/UX-FICHAS-AUDITORIA.md` identificou tres bugs no frontend que causam experiencia incorreta ou navegacao errada. Nenhum deles exige novos endpoints — dois requerem ajuste de modelo/template e um e uma linha de codigo errada.

---

## Bug 1 — Barras de vida e essencia sempre cheias (valor hardcoded)

**Severidade:** BLOQUEADOR (UX-01, UX-02 na auditoria)

**Arquivo afetado:**
`src/app/features/jogador/pages/ficha-detail/components/ficha-header/ficha-header.component.ts`

**Problema atual:**

```typescript
// Linha 82 — barra de vida
<p-progressBar [value]="100" ... />

// Linha 95 — barra de essencia
<p-progressBar [value]="100" ... />
```

As barras estao hardcoded com `[value]="100"`, exibindo sempre 100% independente do estado real de combate. O label do aria tambem exibe apenas o total (`resumo().vidaTotal`), nao o valor atual.

**Causa raiz:** O modelo TypeScript `FichaResumo` (em `src/app/core/models/ficha.model.ts`) nao possui os campos `vidaAtual` e `essenciaAtual`, embora esses campos ja existam no backend (`FichaVida.vidaAtual` e `FichaEssencia.essenciaAtual`). O `FichaResumoResponse` do backend precisa expor esses campos.

**Correcao — Passo 1 (Backend):**
Verificar se `FichaResumoResponse.java` ja inclui `vidaAtual` e `essenciaAtual`.
- Se nao incluir: adicionar os dois campos ao record `FichaResumoResponse` e garantir que o mapper preenche corretamente a partir de `ficha.getFichaVida().getVidaAtual()` e `ficha.getFichaEssencia().getEssenciaAtual()`.
- Ver T5 (`T5-essencia-resumo.md`) desta spec — essa task faz exatamente essa verificacao.

**Correcao — Passo 2 (Frontend — modelo):**
Arquivo: `src/app/core/models/ficha.model.ts`

Adicionar ao interface `FichaResumo`:
```typescript
vidaAtual: number;
essenciaAtual: number;
```

**Correcao — Passo 3 (Frontend — template):**
Arquivo: `src/app/features/jogador/pages/ficha-detail/components/ficha-header/ficha-header.component.ts`

Substituir os dois bindings hardcoded:

```typescript
// Vida — ANTES
<p-progressBar [value]="100" class="vida-bar" [showValue]="false"
  [attr.aria-label]="'Vida total: ' + resumo().vidaTotal" />

// Vida — DEPOIS
<p-progressBar
  [value]="(resumo().vidaAtual / resumo().vidaTotal) * 100"
  class="vida-bar"
  [showValue]="false"
  [attr.aria-label]="'Vida: ' + resumo().vidaAtual + ' de ' + resumo().vidaTotal"
/>

// Essencia — ANTES
<p-progressBar [value]="100" class="essencia-bar" [showValue]="false"
  [attr.aria-label]="'Essencia total: ' + resumo().essenciaTotal" />

// Essencia — DEPOIS
<p-progressBar
  [value]="(resumo().essenciaAtual / resumo().essenciaTotal) * 100"
  class="essencia-bar"
  [showValue]="false"
  [attr.aria-label]="'Essencia: ' + resumo().essenciaAtual + ' de ' + resumo().essenciaTotal"
/>
```

Tambem atualizar o label de texto acima de cada barra para exibir `X / Y` em vez de apenas o total:

```typescript
// ANTES
<span class="text-color-secondary">{{ resumo().vidaTotal }}</span>

// DEPOIS
<span class="text-color-secondary font-mono">
  {{ resumo().vidaAtual }} / {{ resumo().vidaTotal }}
</span>
```

**Criterio de aceite:**
- Dado que uma ficha tem `vidaAtual = 15` e `vidaTotal = 30`, a barra de vida exibe 50% preenchida
- Dado que uma ficha tem `essenciaAtual = 0` e `essenciaTotal = 20`, a barra de essencia exibe 0% preenchida
- O aria-label exibe "Vida: 15 de 30" (nao apenas "Vida total: 30")
- Barras nao exibem NaN quando `vidaTotal = 0` (guardar com `|| 0` no divisor)

**Observacao sobre divisao por zero:**
Se `vidaTotal` ou `essenciaTotal` puder ser 0 (personagem recém criado sem calculos), adicionar protecao:
```typescript
[value]="resumo().vidaTotal > 0 ? (resumo().vidaAtual / resumo().vidaTotal) * 100 : 0"
```

---

## Bug 2 — Pontos de vantagem disponveis hardcoded como 0

**Severidade:** ALTO (UX-14 na auditoria)

**Arquivo afetado:**
`src/app/features/jogador/pages/ficha-detail/components/ficha-vantagens-tab/ficha-vantagens-tab.component.ts`

**Problema atual:**

O componente `FichaVantagensTabComponent` recebe `pontosVantagemRestantes` como `input<number>(0)` — defaultando para zero. Na chamada do componente pai (`FichaDetailComponent`), esse input e passado com o valor estatico `[pontosVantagemRestantes]="0"`, ou simplesmente omitido (assumindo o default).

```typescript
// Linha 107 — declaracao do input
pontosVantagemRestantes = input<number>(0);
```

Isso causa o badge "0 pontos de vantagem disponíveis" para todos os jogadores, mesmo aqueles com pontos acumulados apos level up.

**Causa raiz:**
O campo `pontosVantagemDisponiveis` nao existe no `FichaResumoResponse` do backend nem no modelo TypeScript `FichaResumo`. Sua implementacao e parte da Spec 012 (nivel e progressao). Portanto, a correcao completa so e possivel apos a Spec 012 T5.

**Correcao imediata (sem backend):**
Substituir o badge estatico por um estado neutro que nao engana o usuario:

Arquivo: `src/app/features/jogador/pages/ficha-detail/components/ficha-vantagens-tab/ficha-vantagens-tab.component.ts`

```typescript
// ANTES — exibe "0" para todos (enganoso)
<span class="text-sm font-medium">Pontos de vantagem disponiveis:</span>
<p-badge [value]="pontosVantagemRestantes().toString()" severity="info" />

// DEPOIS — exibe "-" enquanto o campo nao existe no backend
<span class="text-sm font-medium">Pontos de vantagem disponiveis:</span>
@if (pontosVantagemRestantes() !== null && pontosVantagemRestantes() !== undefined) {
  <p-badge [value]="pontosVantagemRestantes()!.toString()" severity="info" />
} @else {
  <span class="text-sm text-color-secondary font-mono">—</span>
}
```

Atualizar o tipo do input para aceitar null:
```typescript
pontosVantagemRestantes = input<number | null>(null);
```

**Correcao futura (apos Spec 012 T5):**
Quando `FichaResumo.pontosVantagemDisponiveis` for adicionado ao backend e ao modelo TypeScript, passar o valor real:
```typescript
// No FichaDetailComponent (componente pai)
[pontosVantagemRestantes]="resumo()?.pontosVantagemDisponiveis ?? null"
```

**Criterio de aceite:**
- Badge exibe `—` (traco) enquanto `pontosVantagemDisponiveis` nao esta disponivel no backend
- Badge NAO exibe "0" por padrao (nao confundir "sem dados" com "zero pontos disponíveis")
- Quando Spec 012 T5 for concluida e o campo existir no response, o badge exibe o numero real

---

## Bug 3 — Mestre redirecionado para rota de Jogador ao ver ficha de NPC

**Severidade:** MEDIO (UX-17 na auditoria)

**Arquivo afetado:**
`src/app/features/mestre/pages/npcs/npcs.component.ts`

**Problema atual:**

```typescript
// Linha 432 — metodo verFicha
verFicha(fichaId: number): void {
  this.router.navigate(['/jogador/fichas', fichaId]);
}
```

O Mestre ao clicar em "Ver Ficha" de um NPC e redirecionado para `/jogador/fichas/:id`. Esta rota e protegida pelo `roleGuard` para o papel JOGADOR, o que pode resultar em:
- Acesso negado (403 / redirect para login) se o guard rejeitar MESTRE
- Ou exibicao de uma tela sem os controles de Mestre (editar, deletar, revelar NPC)

**Causa raiz:**
O metodo `verFicha` usa a rota de jogador hardcoded. O Mestre deveria ser direcionado para uma rota que exibe a ficha com permissoes de Mestre.

**Correcao:**

Verificar qual rota o Mestre usa para ver uma ficha individual. Opcoes conforme a estrutura de rotas do projeto:

Opcao A — rota generica (preferida se existir):
```typescript
verFicha(fichaId: number): void {
  this.router.navigate(['/fichas', fichaId]);
}
```

Opcao B — rota dedicada do Mestre (se existir):
```typescript
verFicha(fichaId: number): void {
  this.router.navigate(['/mestre/fichas', fichaId]);
}
```

Opcao C — verificar role dinamicamente (apenas se as opcoes A e B nao existirem):
```typescript
verFicha(fichaId: number): void {
  const isMestre = this.authStore.currentUser()?.role === 'MESTRE';
  const basePath = isMestre ? '/mestre/fichas' : '/jogador/fichas';
  this.router.navigate([basePath, fichaId]);
}
```

**Acao necessaria antes de implementar:**
Verificar `src/app/app.routes.ts` para confirmar qual rota de detalhe de ficha existe para o Mestre. Se nao existir rota `/mestre/fichas/:id`, usar a rota generica ou criar a rota antes de corrigir a navegacao.

**Criterio de aceite:**
- Mestre clica em "Ver Ficha" de um NPC e e direcionado para uma rota valida para a role MESTRE
- A tela exibida mostra os controles de Mestre (editar, deletar, controles de NPC)
- Mestre nao recebe erro 403 ou redirect inesperado

---

## Relacao com outras Tasks desta Spec

| Bug | Relacao |
|-----|---------|
| Bug 1 | Parcialmente coberto pela T5 (verificar `essenciaAtual` no FichaResumoResponse) e T8 (frontend barra de essencia reativa). Esta task antecipa e detalha a correcao completa. |
| Bug 2 | Independente. A correcao completa depende da Spec 012 T5 (adicionar `pontosVantagemDisponiveis` ao backend). Esta task implementa o estado intermediario correto. |
| Bug 3 | Totalmente independente. Correcao de uma linha. |

---

## Checklist de Implementacao

- [ ] Bug 1: Verificar `FichaResumoResponse.java` — campos `vidaAtual` e `essenciaAtual` presentes
- [ ] Bug 1: Adicionar `vidaAtual: number` e `essenciaAtual: number` ao interface `FichaResumo` em `ficha.model.ts`
- [ ] Bug 1: Substituir `[value]="100"` por expressao calculada nas duas barras do `FichaHeaderComponent`
- [ ] Bug 1: Atualizar labels de texto para exibir `X / Y` em vez de apenas `Y`
- [ ] Bug 1: Adicionar protecao contra divisao por zero
- [ ] Bug 1: Atualizar aria-labels para refletir o valor atual
- [ ] Bug 2: Mudar o tipo do input `pontosVantagemRestantes` para `number | null` com default `null`
- [ ] Bug 2: Substituir o badge estatico por logica condicional (numero se disponivel, `—` se null)
- [ ] Bug 3: Verificar rotas existentes em `app.routes.ts` para o Mestre
- [ ] Bug 3: Corrigir `verFicha()` no `NpcsComponent` para usar rota correta do Mestre
- [ ] Testes: atualizar ou criar testes para os tres componentes afetados
