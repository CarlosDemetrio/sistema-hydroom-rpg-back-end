# T9 — Passo 4: Distribuicao de Aptidoes

> Fase: Frontend
> Complexidade: Media
> Prerequisito: T5 (pontosAptidaoDisponiveis no FichaResumoResponse), T6 (Passo 1)
> Bloqueia: T11 (Revisao)
> Estimativa: 4–5 horas

---

## Objetivo

Implementar o Passo 4 do wizard onde o jogador distribui seus pontos de aptidao entre as aptidoes configuradas pelo Mestre, agrupadas por `TipoAptidao`. Nenhuma aptidao e obrigatoria — o jogador pode deixar todas em 0 e avancar. Auto-save via `PUT /fichas/{id}/aptidoes`.

---

## Contexto

Aptidoes sao carregadas de `GET /fichas/{id}/aptidoes` (ja inicializadas na criacao da ficha). A lista e dinamica — agrupada por `TipoAptidao`. Os campos `sorte` e `classe` sao controlados pelo Mestre e aparecem como somente leitura neste passo (ambos sao 0 na criacao).

**Regras criticas:**
- Nenhum minimo — o jogador pode avançar sem distribuir nenhum ponto
- `base` e o unico campo editavel pelo jogador
- `sorte` e `classe`: exibidos, somente leitura (gerenciados pelo Mestre)
- Bonus de classe (`ClasseAptidaoBonus`): aplicado automaticamente pelo backend ao campo `classe` — nao editavel
- A soma de todos os `base` nao pode exceder `pontosAptidaoDisponiveis`
- Nao ha limitador por aptidao (diferente dos atributos)

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `ficha-form/ficha-wizard.component.ts` | Adicionar formPasso4 e logica de auto-save |
| `ficha-form/steps/step-aptidoes/step-aptidoes.component.ts` | Criar componente dumb |
| `ficha-form/steps/step-aptidoes/step-aptidoes.component.html` | Template agrupado por tipo |

---

## Wireframe do Passo 4

```
┌────────────────────────────────────────────────────────────────┐
│  H2: Aptidoes                                                  │
│  Distribua seus pontos entre as aptidoes do personagem.        │
│                                                                │
│  Pontos disponiveis: 12    Utilizados: 8                       │
│  [████████░░░░░░░░]                                            │
│                                                                │
│  ── Fisicas ────────────────────────────────────────────────   │
│  Acrobacia         [  -  ]  [ 3 ]  [  +  ]   Total: 3         │
│  Atletismo         [  -  ]  [ 5 ]  [  +  ]   Total: 5         │
│  Furtividade       [  -  ]  [ 0 ]  [  +  ]   Total: 0         │
│                                                                │
│  ── Mentais ────────────────────────────────────────────────   │
│  Percepcao         [  -  ]  [ 0 ]  [  +  ]   Total: 0         │
│  Diplomacia        [  -  ]  [ 0 ]  [  +  ]   Total: 0         │
│  ...                                                           │
└────────────────────────────────────────────────────────────────┘
```

---

## Estrutura do Componente

### `StepAptidoesComponent` (Dumb)

```typescript
// Inputs
aptidoesAgrupadas = input.required<TipoAptidaoComAptidoes[]>();
pontosDisponiveis = input.required<number>();

// Outputs
aptidoesChanged = output<FichaAptidaoEditavel[]>();
```

**Tipos auxiliares:**
```typescript
interface TipoAptidaoComAptidoes {
  tipoNome: string;
  aptidoes: FichaAptidaoEditavel[];
}

interface FichaAptidaoEditavel {
  fichaAptidaoId: number;
  aptidaoNome: string;
  base: number;
  sorte: number;   // somente leitura
  classe: number;  // somente leitura
}
```

**Calculo de pontos:**
```typescript
readonly pontosUtilizados = computed(() =>
  this.aptidoesAgrupadas().flatMap(g => g.aptidoes)
    .reduce((sum, a) => sum + a.base, 0)
);

readonly pontosRestantes = computed(() =>
  Math.max(0, this.pontosDisponiveis() - this.pontosUtilizados())
);
```

**Agrupamento no wizard:**

O `FichaWizardComponent` recebe `FichaAptidaoResponse[]` do backend e agrupa por `tipoNome` antes de passar para o `StepAptidoesComponent`:

```typescript
readonly aptidoesAgrupadas = computed(() => {
  const grupos = new Map<string, FichaAptidaoEditavel[]>();
  for (const a of this.formPasso4()) {
    if (!grupos.has(a.tipoAptidaoNome)) {
      grupos.set(a.tipoAptidaoNome, []);
    }
    grupos.get(a.tipoAptidaoNome)!.push(a);
  }
  return Array.from(grupos.entries()).map(([tipoNome, aptidoes]) => ({ tipoNome, aptidoes }));
});
```

### Auto-save no Wizard

```typescript
salvarPasso4(): Observable<FichaAptidaoResponse[]> {
  const requests = this.formPasso4().map(a => ({
    fichaAptidaoId: a.fichaAptidaoId,
    base: a.base
  }));
  return this.fichasApiService.atualizarAptidoes(this.fichaId()!, requests);
}
```

---

## Testes Obrigatorios

| Cenario | Descricao |
|---------|-----------|
| Renderizacao agrupada | Aptidoes exibidas agrupadas por TipoAptidao |
| Aptidoes dinamicas | Nenhuma aptidao hardcoded; todas vem do backend |
| Contador global | "X / Y pontos utilizados" atualizado em tempo real |
| Incrementar aptidao | Clicar [+] aumenta base e atualiza contador |
| Decrementar aptidao | Clicar [-] diminui base (minimo 0) |
| Sem pontos disponiveis | Botao [+] de todas desabilitado quando pontosRestantes == 0 |
| Botao "Proximo" sempre habilitado | Passo e opcional — pode avancar com 0 pontos |
| sorte e classe readonly | Campos nao editaveis exibidos como texto |
| Auto-save | Ao clicar "Proximo", PUT /fichas/{id}/aptidoes chamado |
| Pre-preenchimento (rascunho) | Aptidoes ja distribuidas pre-carregadas |

---

## Criterios de Aceitacao

- [ ] Aptidoes carregadas dinamicamente e agrupadas por TipoAptidao
- [ ] Contador de pontos disponiveis vs utilizados visivel
- [ ] Spinner [-][base][+] por aptidao
- [ ] Botao [+] desabilitado quando pontosRestantes == 0
- [ ] Botao "Proximo" sempre habilitado (passo opcional)
- [ ] Campos `sorte` e `classe` exibidos como somente leitura
- [ ] Ao avancar, PUT /fichas/{id}/aptidoes chamado com todos os valores
- [ ] Rascunho retomado: aptidoes ja distribuidas pre-carregadas

---

## Observacoes

- `FichaAptidaoResponse` deve incluir `tipoAptidaoNome` para o agrupamento. Verificar se este campo ja existe na resposta do backend. Se nao, e um bug do mapeamento a corrigir no backend (fora do escopo desta task — abrir ponto de atencao).
- Nao ha limitador por aptidao — o unico limite e o total de pontos disponiveis.
- O bonus de classe (`classe` no `FichaAptidao`) e preenchido pelo backend automaticamente ao criar a ficha baseado em `ClasseAptidaoBonus`. Exibir como badge "+N (bonus de classe)".
