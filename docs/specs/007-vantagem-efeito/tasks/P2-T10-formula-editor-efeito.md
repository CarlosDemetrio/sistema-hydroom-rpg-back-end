# T10 — FormulaEditor Integrado para FORMULA_CUSTOMIZADA

> Fase: Frontend | Dependencias: T9 (EfeitoFormComponent criado) | Bloqueia: nada
> Estimativa: 2–3 horas

---

## Objetivo

Integrar o `FormulaEditorComponent` existente no formulario de efeito para o tipo `FORMULA_CUSTOMIZADA`, permitindo ao Mestre testar a formula antes de salvar e ver quais variaveis estao disponíveis.

---

## Contexto

O `FormulaEditorComponent` ja existe em `src/app/shared/components/formula-editor/`. Ele foi criado na sessao frontend anterior. Esta task conecta esse componente ao formulario de efeito, com o conjunto de variaveis especifico de FORMULA_CUSTOMIZADA (que e mais rico do que o editor usa para BonusConfig).

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `shared/components/formula-editor/formula-editor.component.ts` | Verificar se aceita variaveis customizadas como input |
| `features/configuracoes/vantagens/efeito-form/efeito-form.component.ts` | Integrar FormulaEditor |

---

## Passos de Implementacao

### Passo 1 — Verificar inputs do FormulaEditorComponent

Verificar se o componente tem `@Input variaveisDisponiveis: string[]`. Se nao tiver, adicionar.

```typescript
// formula-editor.component.ts (adicionar se nao existir)
variaveisDisponiveis = input<string[]>([]);
```

### Passo 2 — Construir lista de variaveis para FORMULA_CUSTOMIZADA

No `EfeitoFormComponent`, computar a lista de variaveis disponíveis:

```typescript
variaveisFormulaEfeito = computed((): VariavelFormula[] => {
  const vars: VariavelFormula[] = [
    { nome: 'nivel_vantagem', descricao: 'Nivel atual da vantagem na ficha', exemplo: '3' },
    { nome: 'nivel_personagem', descricao: 'Nivel atual do personagem', exemplo: '5' },
    { nome: 'valor_fixo', descricao: 'Valor fixo configurado neste efeito', exemplo: '2' },
    { nome: 'valor_por_nivel', descricao: 'Valor por nivel configurado neste efeito', exemplo: '1' },
  ];
  // Adicionar siglas de atributos do jogo
  this.atributosDisponiveis().forEach(a => {
    vars.push({
      nome: a.abreviacao,
      descricao: `Total do atributo ${a.nome}`,
      exemplo: '20'
    });
  });
  return vars;
});
```

### Passo 3 — Integrar no template de EfeitoFormComponent

```html
@if (mostrarFormula()) {
  <app-formula-editor
    [(formula)]="form.formula"
    [variaveisDisponiveis]="variaveisFormulaEfeito()"
    [jogoId]="jogoId()" />

  <!-- Link de ajuda: variaveis disponiveis -->
  <p-button label="Ver variaveis disponíveis" icon="pi pi-list"
    severity="info" text (onClick)="mostrarModalVariaveis.set(true)" />

  <p-dialog header="Variaveis disponíveis" [visible]="mostrarModalVariaveis()"
    (onHide)="mostrarModalVariaveis.set(false)">
    <p-table [value]="variaveisFormulaEfeito()">
      <ng-template pTemplate="header">
        <tr>
          <th>Variavel</th>
          <th>Descricao</th>
          <th>Exemplo</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-v>
        <tr>
          <td><code>{{ v.nome }}</code></td>
          <td>{{ v.descricao }}</td>
          <td>{{ v.exemplo }}</td>
        </tr>
      </ng-template>
    </p-table>
  </p-dialog>
}
```

---

## Comportamento do FormulaEditor no contexto de efeito

- **Teste de formula:** Botao "Testar" chama `POST /api/v1/jogos/{id}/formulas/preview` com as variaveis de exemplo populadas
- **Validacao inline:** Se o usuario digitar uma formula com variavel inexistente, exibir aviso em tempo real (antes de salvar)
- **Sem o editor de variaveis completo:** So o campo de formula + botao de teste + link "ver variaveis"

---

## Testes (Vitest)

```typescript
describe('EfeitoForm — FORMULA_CUSTOMIZADA', () => {
  it('deve exibir FormulaEditor quando tipo FORMULA_CUSTOMIZADA selecionado', () => {
    // Assert: app-formula-editor presente no DOM
  });

  it('deve ocultar FormulaEditor para outros tipos', () => {
    // Assert: app-formula-editor ausente no DOM
  });

  it('deve incluir variaveis de atributo na lista passada ao FormulaEditor', () => {
    // Mock atributosDisponiveis com FOR, AGI
    // Assert: variaveisFormulaEfeito() inclui { nome: 'FOR', ... } e { nome: 'AGI', ... }
  });
});
```

---

## Criterios de Aceitacao

- [ ] FormulaEditor exibido apenas para tipo FORMULA_CUSTOMIZADA
- [ ] Lista de variaveis inclui nivel_vantagem, nivel_personagem, valor_fixo, valor_por_nivel e siglas de atributos
- [ ] Modal "Ver variaveis" exibe tabela formatada
- [ ] Botao "Testar formula" funciona (chama /formulas/preview)
- [ ] Campo formula obrigatorio — botao salvar desabilitado se vazio
- [ ] Testes Vitest passando
