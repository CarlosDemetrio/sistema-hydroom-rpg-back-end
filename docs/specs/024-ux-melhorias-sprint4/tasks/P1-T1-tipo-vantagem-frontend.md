# T1 — tipoVantagem no formulario de VantagemConfig

> Fase: Frontend | Dependencias: nenhuma | Bloqueia: nenhuma
> Estimativa: 1–2 horas
> Backlog ID: UX-TIPO-VANTAGEM (Sprint 4, P1)

---

## Objetivo

Expor o campo `tipoVantagem` (VANTAGEM | INSOLITUS) no formulario de criacao e edicao de `VantagemConfig`. O backend ja suporta o campo; o frontend precisa de: campo no form, DTOs atualizados e coluna na tabela.

---

## Contexto

- Angular 21, PrimeNG 21.1.1, Vitest, @testing-library/angular
- Usar `inject()` para DI — nunca constructor injection
- Usar `signal()`, `computed()`, `effect()` para estado reativo
- Usar `@if` / `@for` — nunca `*ngIf` / `*ngFor`
- Componentes standalone, sem `CommonModule`
- Testes com `vi.fn()` (Vitest)

**Arquivo principal:** `src/app/features/mestre/pages/config/configs/vantagens-config/vantagens-config.component.ts`

**Backend suporta:**
- `CreateVantagemRequest.tipoVantagem` — campo opcional, default `VANTAGEM`
- `UpdateVantagemRequest.tipoVantagem` — campo opcional
- `VantagemResponse.tipoVantagem` — presente na resposta

---

## Arquivos a Modificar

| Arquivo | Mudanca |
|---------|---------|
| `src/app/core/models/vantagem-config.model.ts` | Adicionar `tipoVantagem` em `CreateVantagemDto` e `UpdateVantagemDto` |
| `vantagens-config.component.ts` | `buildForm()`, signal `isInsolitus`, template checkbox, efeito de sync, populate no edit, `filteredItems()`, `columns` |
| `vantagens-config.component.spec.ts` | Testes novos para o campo |

---

## Implementacao

### 1. DTOs — `vantagem-config.model.ts`

```typescript
export interface CreateVantagemDto {
  jogoId: number;
  nome: string;
  sigla?: string;
  descricao?: string;
  categoriaVantagemId: number;
  nivelMaximo: number;
  formulaCusto?: string;
  descricaoEfeito?: string;
  ordemExibicao?: number;
  tipoVantagem?: 'VANTAGEM' | 'INSOLITUS'; // NOVO
}

export interface UpdateVantagemDto {
  nome?: string;
  sigla?: string;
  descricao?: string;
  categoriaVantagemId?: number;
  nivelMaximo?: number;
  formulaCusto?: string;
  descricaoEfeito?: string;
  ordemExibicao?: number;
  tipoVantagem?: 'VANTAGEM' | 'INSOLITUS'; // NOVO
}
```

### 2. FormGroup — `buildForm()`

Adicionar campo `tipoVantagem` com default `'VANTAGEM'`:

```typescript
protected buildForm(): FormGroup {
  return this.fb.group({
    nome:                ['', [...]],
    sigla:               ['', [...]],
    categoriaVantagemId: [null, [...]],
    nivelMaximo:         [1,    [...]],
    formulaCusto:        ['',   [...]],
    descricaoEfeito:     ['',   [...]],
    descricao:           ['',   [...]],
    ordemExibicao:       [1,    [...]],
    tipoVantagem:        ['VANTAGEM'], // NOVO
  });
}
```

### 3. Signal + efeito de sincronizacao

Adicionar signal booleano e `effect` que sincroniza com o form e controla `formulaCusto`:

```typescript
protected isInsolitus = signal(false);

// No construtor ou ngOnInit — apos buildForm():
private setupInsolitusEffect(): void {
  effect(() => {
    const insolitus = this.isInsolitus();
    this.form.patchValue({ tipoVantagem: insolitus ? 'INSOLITUS' : 'VANTAGEM' });
    const formulaCusto = this.form.get('formulaCusto');
    if (insolitus) {
      formulaCusto?.disable();
      formulaCusto?.setValue('');
    } else {
      formulaCusto?.enable();
    }
  }, { allowSignalWrites: true });
}
```

### 4. Populate no edit mode — `openDrawer()`

Ao abrir o dialog em modo edicao, derivar `isInsolitus` do item:

```typescript
openDrawer(item?: VantagemConfig): void {
  this.openDialog(item);
  this.selectedVantagem.set(item ?? null);
  this.isInsolitus.set(item?.tipoVantagem === 'INSOLITUS'); // NOVO
  // ... resto igual
}
```

### 5. Template — checkbox na aba Dados Gerais

Inserir apos o campo "Categoria", antes de "Nivel Maximo":

```html
<!-- Tipo Vantagem (Insolitus) -->
<div class="flex flex-column gap-2">
  <div class="flex align-items-center gap-2">
    <p-checkbox
      inputId="isInsolitus"
      [(ngModel)]="isInsolitus"
      [binary]="true"
      data-testid="checkbox-insolitus"
    />
    <label for="isInsolitus" class="font-semibold cursor-pointer">
      Esta vantagem e Insolitus
    </label>
  </div>
  <small class="text-color-secondary">
    Concedida gratuitamente pelo Mestre, sem custo de pontos.
  </small>
</div>
```

**Nota:** O campo `formulaCusto` recebe `[disabled]="isInsolitus()"` como indicacao visual adicional (o `effect` ja controla o estado do form).

### 6. Tabela — campo virtual em `filteredItems()`

Adicionar mapeamento para exibicao na tabela:

```typescript
protected filteredItems = computed(() => {
  const q = this.searchQuery().toLowerCase().trim();
  const base = !q
    ? this.items()
    : this.items().filter(
        (v) =>
          v.nome?.toLowerCase().includes(q) ||
          (v.sigla ?? '').toLowerCase().includes(q) ||
          (v.categoriaNome ?? '').toLowerCase().includes(q),
      );
  return base.map((v) => ({
    ...v,
    tipoVantagemLabel: v.tipoVantagem === 'INSOLITUS' ? 'Insolitus' : '—',
  }));
});
```

Adicionar coluna ao array `columns`:

```typescript
readonly columns: ConfigTableColumn[] = [
  { field: 'ordemExibicao',    header: 'Ordem',    width: '5rem' },
  { field: 'nome',             header: 'Nome' },
  { field: 'sigla',            header: 'Sigla',    width: '6rem' },
  { field: 'categoriaNome',    header: 'Categoria', width: '12rem' },
  { field: 'nivelMaximo',      header: 'Nivel Max.', width: '8rem' },
  { field: 'tipoVantagemLabel', header: 'Tipo',    width: '7rem' }, // NOVO
];
```

---

## Testes a Escrever

| Cenario | Descricao |
|---------|-----------|
| Criar INSOLITUS | Checkbox marcado -> DTO enviado com `tipoVantagem: 'INSOLITUS'` |
| Criar VANTAGEM | Checkbox desmarcado (default) -> DTO com `tipoVantagem: 'VANTAGEM'` |
| Editar INSOLITUS existente | Checkbox pre-marcado, `formulaCusto` desabilitado |
| Editar VANTAGEM existente | Checkbox desmarcado, `formulaCusto` habilitado |
| Marcar checkbox | `formulaCusto` desabilita e limpa |
| Desmarcar checkbox | `formulaCusto` volta a ser editavel |
| Tabela INSOLITUS | Coluna "Tipo" exibe "Insolitus" para item INSOLITUS |
| Tabela VANTAGEM | Coluna "Tipo" exibe "—" para item VANTAGEM |

---

## Criterios de Pronto

- [ ] DTOs atualizados com `tipoVantagem`
- [ ] `buildForm()` inclui campo `tipoVantagem`
- [ ] Checkbox visivel no formulario, posicionado apos Categoria
- [ ] Marcar checkbox desabilita/limpa `formulaCusto`; desmarcar reabilita
- [ ] Edicao de item existente pre-preenche o checkbox corretamente
- [ ] Coluna "Tipo" aparece na tabela (Insolitus / —)
- [ ] Todos os testes Vitest novos passando
- [ ] `npm test` 100% verde (ou apenas falhas pre-existentes conhecidas)

---

*Produzido por: Business Analyst/PO | 2026-04-15*
