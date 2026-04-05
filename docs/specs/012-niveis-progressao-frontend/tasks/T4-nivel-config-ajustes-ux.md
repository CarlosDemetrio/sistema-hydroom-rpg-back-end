# T4 — Ajustes UX no NiveisConfigComponent

> Spec: 012 | Fase: 1 | Tipo: Frontend | Prioridade: BAIXO
> Depende de: nada
> Bloqueia: nada

---

## Objetivo

Melhorar a usabilidade do componente existente `NiveisConfigComponent` com três ajustes: (1) adicionar coluna `permitirRenascimento` na tabela, (2) formatar `xpNecessaria` com separador de milhar, (3) exibir avisos de inconsistência na tabela de XP.

## Contexto

O componente já funciona corretamente para CRUD. Os ajustes são puramente de UX e não afetam a lógica de negócio. São de baixa prioridade mas melhoram significativamente a usabilidade para o Mestre ao configurar 36 níveis.

## Arquivo Afetado

`src/app/features/mestre/pages/config/configs/niveis-config/niveis-config.component.ts`

## Passos

### 1. Coluna `permitirRenascimento` na tabela

Verificar se `BaseConfigTableComponent` suporta colunas booleanas/template. Adicionar ao array `columns`:

```typescript
{ field: 'permitirRenascimento', header: 'Renascer', width: '8rem' }
```

Se `BaseConfigTableComponent` não renderizar booleanos visualmente, usar template customizado com `<p-tag>` ou ícone:
- `permitirRenascimento = true`: `<p-tag severity="warn" value="Sim" icon="pi pi-refresh" />`
- `permitirRenascimento = false`: (vazio ou `-`)

Destacar visualmente as linhas com renascimento permitido (background sutilmente diferente ou cor na tag).

### 2. Formatação de milhar em xpNecessaria

Verificar se o `p-inputnumber` no drawer já tem `[useGrouping]="true"`. Se não, adicionar:

```html
<p-input-number
  inputId="xpNecessaria"
  formControlName="xpNecessaria"
  [useGrouping]="true"
  [min]="0"
/>
```

Para a tabela: verificar se `BaseConfigTableComponent` formata números. Se não, usar pipe `number:'1.0-0':'pt-BR'` no template da coluna ou criar pipe customizado.

### 3. Validação de consistência — computed

Criar dois signals computados para os avisos:

```typescript
protected niveisComXpInvalida = computed(() => {
  const sorted = [...this.items()].sort((a, b) => a.nivel - b.nivel);
  const invalidos = new Set<number>();
  for (let i = 1; i < sorted.length; i++) {
    if (sorted[i].xpNecessaria < sorted[i - 1].xpNecessaria) {
      invalidos.add(sorted[i].nivel);
    }
  }
  return invalidos;
});

protected lacunasNaSequencia = computed(() => {
  const niveis = new Set(this.items().map(n => n.nivel));
  const lacunas: number[] = [];
  const min = Math.min(...niveis);
  const max = Math.max(...niveis);
  for (let i = min + 1; i < max; i++) {
    if (!niveis.has(i)) lacunas.push(i);
  }
  return lacunas;
});
```

**Exibição:**
- Linha com XP inválida: badge de aviso inline na coluna XP (`p-tag severity="danger" value="XP inv."`)
- Lacunas: `<p-message severity="warn">` acima da tabela com texto "Faltam os níveis: 15, 17. Personagens com XP suficiente pulam diretamente."
- Ambos são apenas avisos — não bloqueiam criação/edição.

## Critérios de Aceitação

- [ ] Coluna `permitirRenascimento` exibida na tabela com destaque visual
- [ ] Linhas com permitirRenascimento=true visualmente diferenciadas (tag ou ícone)
- [ ] Campo xpNecessaria formatado com separador de milhar no drawer
- [ ] Aviso inline na coluna XP quando xpNecessaria < xp do nível anterior
- [ ] Aviso global quando há lacunas na sequência de níveis
- [ ] Build sem erros, sem regressões nos testes existentes do componente

## Premissas

- Os avisos de inconsistência são apenas informativos — não bloqueiam o Mestre
- A formatação de milhar na tabela pode exigir template slot customizado dependendo da implementação de `BaseConfigTableComponent`
