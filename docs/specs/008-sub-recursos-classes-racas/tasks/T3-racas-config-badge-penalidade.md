# T3 — RacasConfigComponent: badge de restricao na tabela + indicador textual de penalidade

> **Complexidade:** pequena
> **Depende de:** T1 (tipagem corrigida)
> **Bloqueia:** T4
> **Arquivo principal:** `racas-config.component.ts`

---

## Objetivo

Melhorar o `RacasConfigComponent` em dois pontos:

1. **Badge na tabela principal:** adicionar coluna ou indicador visual de "Sem restricoes" vs "X classes" para `classesPermitidas`, tornando imediatamente visivel quais racas tem restricao de classe.

2. **Indicador textual de penalidade:** valores negativos em `RacaBonusAtributo` ja sao exibidos em vermelho, mas apenas a cor nao e suficiente para acessibilidade — adicionar texto "(penalidade)" ou "(-)".

---

## Contexto

### Estado atual

A tabela principal de racas tem apenas 3 colunas: Ordem, Nome, Descricao.
Nao ha nenhuma indicacao visual de restricoes de classe ou de bônus de atributo.
Um Mestre que acabou de configurar "Elfo: apenas Mago e Arqueiro" nao consegue ver
isso na listagem sem abrir o drawer.

Na aba "Bônus em Atributos", valores negativos sao exibidos em vermelho (classe `text-red-400`),
mas o Mestre precisaria saber que "vermelho = penalidade" — nao ha texto explicativo.

---

## Arquivos Afetados

1. `src/app/features/mestre/pages/config/configs/racas-config/racas-config.component.ts`

---

## Passos de Implementacao

### Passo 1 — Adicionar badge de restricao de classe na tabela

A `BaseConfigTable` aceita colunas via o array `columns: ConfigTableColumn[]`. Verificar se
`ConfigTableColumn` suporta template customizado ou apenas fields simples.

**Opcao A — Se BaseConfigTable suporta renderizacao customizada:**
Adicionar coluna com template que exibe o badge.

**Opcao B — Abordagem mais simples (recomendada se A nao for suportado):**
Adicionar uma propriedade computada `racasComBadge` que mapeia cada `Raca` para um objeto
com campo adicional `restricaoLabel: string` e usar esse campo numa coluna simples.

**Implementacao recomendada (Opcao B):**

Adicionar computed na classe:
```typescript
protected racasComInfo = computed(() =>
  this.filteredItems().map((r) => ({
    ...r,
    restricaoLabel: r.classesPermitidas?.length
      ? `${r.classesPermitidas.length} classe(s)`
      : 'Sem restrições',
    temRestricao: (r.classesPermitidas?.length ?? 0) > 0,
  }))
);
```

Adicionar campo `restricaoLabel` na definicao de colunas:
```typescript
readonly columns: ConfigTableColumn[] = [
  { field: 'ordemExibicao', header: 'Ordem', width: '5rem' },
  { field: 'nome',          header: 'Nome' },
  { field: 'descricao',     header: 'Descrição' },
  { field: 'restricaoLabel', header: 'Classes' },
];
```

Alterar `[items]="filteredItems()"` para `[items]="racasComInfo()"` no template.

**Nota:** Se `BaseConfigTable` nao renderiza o campo com estilos condicionais (apenas texto),
a coluna mostrara "Sem restricoes" ou "2 classe(s)" como texto simples. Isso ja cumpre o
requisito de visibilidade — melhoria visual com badge colorido pode ser feita depois.

### Passo 2 — Adicionar label textual de penalidade

Na secao de lista da aba "Bônus em Atributos", alterar o display de cada item:

Estado atual:
```html
<span class="valor-numerico--sm" [class.text-green-400]="bonus.bonus > 0" [class.text-red-400]="bonus.bonus < 0">
  {{ bonus.bonus > 0 ? '+' : '' }}{{ bonus.bonus }}
</span>
```

Estado alvo (adicionar label "(penalidade)" para valores negativos):
```html
<div class="flex align-items-center gap-2">
  <span class="valor-numerico--sm"
        [class.text-green-400]="bonus.bonus > 0"
        [class.text-red-400]="bonus.bonus < 0">
    {{ bonus.bonus > 0 ? '+' : '' }}{{ bonus.bonus }}
  </span>
  @if (bonus.bonus < 0) {
    <span class="text-xs text-red-400">(penalidade)</span>
  }
</div>
```

### Passo 3 — Verificar hint informativo sobre valores negativos

Na aba "Bônus em Atributos", ja existe o texto:
```html
<small class="text-color-secondary">
  Valores positivos = bônus. Negativos = penalidade.
</small>
```
Verificar que este texto esta visivel e nao foi removido. Se ausente, re-adicionar.

---

## Criterios de Aceitacao

- [ ] A tabela principal de racas exibe uma coluna "Classes" com valor "Sem restricoes" ou "N classe(s)"
- [ ] Ao adicionar uma classe permitida a uma raca e fechar o drawer, a tabela principal atualiza o valor da coluna
- [ ] Valores negativos em RacaBonusAtributo exibem o texto "(penalidade)" alem da cor vermelha
- [ ] A hint "Valores positivos = bônus. Negativos = penalidade." continua visivel na aba
- [ ] `npm run build` sem erros
- [ ] `npx vitest run` sem regressao

---

## Observacoes

- Esta task nao altera a logica de adicao/remocao de sub-recursos de raca — essas partes ja
  funcionam corretamente.
- Se `BaseConfigTable` nao suportar a coluna de restricao sem modificacao, escalar para o
  time de frontend antes de prosseguir — nao modificar `BaseConfigTable` nesta task
  (risco de regressao em outros 12 componentes que a usam).
- A coluna "Classes" com badge colorido (verde/amarelo) e uma melhoria nice-to-have para
  uma iteracao futura se a tabela base nao suportar templates de celula.
