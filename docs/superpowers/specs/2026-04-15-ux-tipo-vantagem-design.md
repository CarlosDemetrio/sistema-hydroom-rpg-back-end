# Design: UX-TIPO-VANTAGEM

**Data:** 2026-04-15
**Escopo:** Frontend Angular — `vantagens-config.component.ts`
**Backlog ID:** UX-TIPO-VANTAGEM (Sprint 4, P1)

---

## Contexto

O backend já suporta `tipoVantagem` (enum `TipoVantagem`: `VANTAGEM` | `INSOLITUS`) em:
- `CreateVantagemRequest` — campo opcional, default `VANTAGEM`
- `UpdateVantagemRequest` — campo opcional
- `VantagemResponse` — campo presente na resposta

O frontend tem o tipo no model (`VantagemConfig.tipoVantagem?`) mas não expõe o campo no formulário de criação/edição nem na tabela. Resultado: todas as vantagens criadas pelo Mestre via UI ficam como `VANTAGEM`; não há como criar uma `INSOLITUS` pela interface.

---

## Solução

### 1. Checkbox no formulário (aba Dados Gerais)

Posição: após o campo "Categoria", antes de "Nível Máximo".

```
[ ] Esta vantagem é Insólitus
    Concedida gratuitamente pelo Mestre, sem custo de pontos.
```

**Comportamento:**
- Checkbox marcado → `tipoVantagem = 'INSOLITUS'`; campo `formulaCusto` recebe `disable()` e valor é limpo
- Checkbox desmarcado → `tipoVantagem = 'VANTAGEM'`; campo `formulaCusto` recebe `enable()`
- No mode edição, o checkbox é populado via `form.patchValue()` existente (com mapeamento booleano)

### 2. DTOs atualizados

`vantagem-config.model.ts`:
```typescript
export interface CreateVantagemDto {
  // ... campos existentes ...
  tipoVantagem?: 'VANTAGEM' | 'INSOLITUS';
}

export interface UpdateVantagemDto {
  // ... campos existentes ...
  tipoVantagem?: 'VANTAGEM' | 'INSOLITUS';
}
```

### 3. FormGroup atualizado

```typescript
protected buildForm(): FormGroup {
  return this.fb.group({
    // ... campos existentes ...
    tipoVantagem: ['VANTAGEM'],
  });
}
```

**Mapeamento checkbox ↔ form:**
O `p-checkbox` usa `[(ngModel)]` com um signal booleano `isInsolitus`. Um `effect()` sincroniza com `form.get('tipoVantagem')`:
- `isInsolitus = true` → `form.patchValue({ tipoVantagem: 'INSOLITUS' })` + `formulaCusto.disable()`
- `isInsolitus = false` → `form.patchValue({ tipoVantagem: 'VANTAGEM' })` + `formulaCusto.enable()`

No `openDialog()`: ao popular o form, derivar `isInsolitus` do valor de `tipoVantagem` do item.

### 4. Tabela — coluna "Tipo" com texto condicional

`BaseConfigTableComponent` renderiza células via `item[col.field]` sem suporte a templates customizados por coluna (somente boolean, numérico e sigla têm tratamento especial).

Solução: adicionar coluna simples com field virtual `tipoVantagemLabel` calculado em `filteredItems()`:

```typescript
protected filteredItems = computed(() => {
  const q = this.searchQuery().toLowerCase().trim();
  const items = !q ? this.items() : this.items().filter(...);
  return items.map(v => ({
    ...v,
    tipoVantagemLabel: v.tipoVantagem === 'INSOLITUS' ? 'Insólitus' : '—',
  }));
});
```

Coluna adicionada ao array `columns`:
```typescript
{ field: 'tipoVantagemLabel', header: 'Tipo', width: '7rem' },
```

Resultado: INSOLITUS mostra "Insólitus", VANTAGEM mostra "—" (padrão implícito).

---

## Arquivos afetados

| Arquivo | Mudança |
|---------|---------|
| `src/app/core/models/vantagem-config.model.ts` | Adicionar `tipoVantagem` em `CreateVantagemDto` e `UpdateVantagemDto` |
| `vantagens-config.component.ts` | `buildForm()`, signals `isInsolitus`, template checkbox, efeito de sync, populate no edit |

---

## Testes

- Criar vantagem INSOLITUS: checkbox marcado → `tipoVantagem: 'INSOLITUS'` enviado na request
- Criar vantagem VANTAGEM: checkbox desmarcado → `tipoVantagem: 'VANTAGEM'` (ou ausente)
- Editar INSOLITUS existente: checkbox pré-marcado, `formulaCusto` desabilitado
- Editar VANTAGEM existente: checkbox desmarcado, `formulaCusto` habilitado
- Tag INSÓLITUS visível na tabela para vantagens do tipo INSOLITUS
- Marcar/desmarcar checkbox: `formulaCusto` habilita/desabilita corretamente

---

## Tracking

- Marcar `UX-TIPO-VANTAGEM` como concluído no HANDOFF-SESSAO.md
- Marcar `UX-NIVEL-MIN-PREREQ` como concluído (já estava implementado via S023-FE)
- Atualizar contadores de testes no HANDOFF após implementação
