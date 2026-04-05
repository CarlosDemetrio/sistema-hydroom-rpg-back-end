# T9 — VantagensConfigComponent: Secao de Efeitos (Lista + Add/Edit/Delete)

> Fase: Frontend | Dependencias: T8 (backend funcionando) | Bloqueia: T10, T11
> Estimativa: 4–6 horas

---

## Objetivo

Adicionar uma secao "Efeitos" ao formulario de configuracao de vantagem, permitindo ao Mestre listar, adicionar e remover efeitos de uma vantagem. O formulario de efeito deve exibir campos dinamicamente conforme o tipo selecionado.

---

## Contexto Frontend

- Angular 21, PrimeNG 21.1.1, @ngrx/signals, Vitest
- Usar `inject()` para DI, `signal()`/`computed()` para estado reativo
- Usar `@if`/`@for` (nunca `*ngIf`/`*ngFor`)
- Componentes em `src/app/features/configuracoes/vantagens/`

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `features/configuracoes/vantagens/vantagens-config.component.ts` | Adicionar secao de efeitos |
| `features/configuracoes/vantagens/vantagens-config.component.html` | Template da secao de efeitos |
| `core/services/vantagem-efeito.service.ts` | CRUD de efeitos via API |
| `core/models/vantagem-efeito.model.ts` | Interfaces TypeScript |
| `features/configuracoes/vantagens/efeito-form/efeito-form.component.ts` | Novo componente de formulario de efeito |

---

## Modelo TypeScript

```typescript
// core/models/vantagem-efeito.model.ts

export type TipoEfeito =
  | 'BONUS_ATRIBUTO'
  | 'BONUS_APTIDAO'
  | 'BONUS_DERIVADO'
  | 'BONUS_VIDA'
  | 'BONUS_VIDA_MEMBRO'
  | 'BONUS_ESSENCIA'
  | 'DADO_UP'
  | 'FORMULA_CUSTOMIZADA';

export interface VantagemEfeito {
  id: number;
  tipoEfeito: TipoEfeito;
  atributoAlvoId?: number;
  atributoAlvoNome?: string;
  aptidaoAlvoId?: number;
  aptidaoAlvoNome?: string;
  bonusAlvoId?: number;
  bonusAlvoNome?: string;
  membroAlvoId?: number;
  membroAlvoNome?: string;
  valorFixo?: number;
  valorPorNivel?: number;
  formula?: string;
  descricaoEfeito?: string;
}

export interface CriarVantagemEfeitoRequest {
  tipoEfeito: TipoEfeito;
  atributoAlvoId?: number;
  aptidaoAlvoId?: number;
  bonusAlvoId?: number;
  membroAlvoId?: number;
  valorFixo?: number;
  valorPorNivel?: number;
  formula?: string;
  descricaoEfeito?: string;
}
```

---

## VantagemEfeitoService

```typescript
// core/services/vantagem-efeito.service.ts

@Injectable({ providedIn: 'root' })
export class VantagemEfeitoService {
  private http = inject(HttpClient);
  private jogoId = inject(JogoContextService).jogoId;

  listar(vantagemId: number): Observable<VantagemEfeito[]> {
    return this.http.get<VantagemEfeito[]>(
      `/api/v1/jogos/${this.jogoId()}/configuracoes/vantagens/${vantagemId}/efeitos`
    );
  }

  criar(vantagemId: number, request: CriarVantagemEfeitoRequest): Observable<VantagemEfeito> {
    return this.http.post<VantagemEfeito>(
      `/api/v1/jogos/${this.jogoId()}/configuracoes/vantagens/${vantagemId}/efeitos`,
      request
    );
  }

  deletar(vantagemId: number, efeitoId: number): Observable<void> {
    return this.http.delete<void>(
      `/api/v1/jogos/${this.jogoId()}/configuracoes/vantagens/${vantagemId}/efeitos/${efeitoId}`
    );
  }
}
```

---

## EfeitoFormComponent

Componente standalone que gerencia o formulario de criacao de um efeito.

### Estado reativo (signals)

```typescript
tipoSelecionado = signal<TipoEfeito | null>(null);

// Computed: quais campos mostrar
mostrarAlvoAtributo = computed(() => this.tipoSelecionado() === 'BONUS_ATRIBUTO');
mostrarAlvoAptidao  = computed(() => this.tipoSelecionado() === 'BONUS_APTIDAO');
mostrarAlvoBonus    = computed(() => this.tipoSelecionado() === 'BONUS_DERIVADO');
mostrarAlvoMembro   = computed(() => this.tipoSelecionado() === 'BONUS_VIDA_MEMBRO');
mostrarValorNumerico = computed(() =>
  ['BONUS_ATRIBUTO', 'BONUS_APTIDAO', 'BONUS_DERIVADO',
   'BONUS_VIDA', 'BONUS_VIDA_MEMBRO', 'BONUS_ESSENCIA'].includes(this.tipoSelecionado() ?? '')
);
mostrarFormula  = computed(() => this.tipoSelecionado() === 'FORMULA_CUSTOMIZADA');
isDadoUp        = computed(() => this.tipoSelecionado() === 'DADO_UP');
```

### Template — logica de exibicao dinamica

```html
<!-- Secao de efeitos na tela de vantagem -->
<p-panel header="Efeitos Mecanicos">
  <!-- Lista de efeitos existentes -->
  @for (efeito of efeitos(); track efeito.id) {
    <div class="efeito-item">
      <p-tag [value]="efeito.tipoEfeito" />
      <span>{{ descricaoEfeito(efeito) }}</span>
      @if (isMestre()) {
        <p-button icon="pi pi-trash" severity="danger" text
          (onClick)="confirmarDelecaoEfeito(efeito)" />
      }
    </div>
  }

  <!-- Formulario para novo efeito -->
  @if (isMestre() && mostrarFormAdicionarEfeito()) {
    <app-efeito-form
      [vantagemId]="vantagemId()"
      [atributosDisponiveis]="atributos()"
      [aptidoesDisponiveis]="aptidoes()"
      [bonusDisponiveis]="bonus()"
      [membrosDisponiveis]="membros()"
      (efeitoSalvo)="onEfeitoSalvo($event)"
      (cancelar)="mostrarFormAdicionarEfeito.set(false)" />
  }

  @if (isMestre()) {
    <p-button label="+ Adicionar Efeito"
      (onClick)="mostrarFormAdicionarEfeito.set(true)" />
  }
</p-panel>
```

### Formulario dinamico por tipo

```html
<!-- EfeitoFormComponent template -->
<div class="efeito-form">
  <!-- Selector de tipo -->
  <p-dropdown [options]="tiposEfeito" optionLabel="label" optionValue="value"
    placeholder="Selecionar tipo de efeito"
    [(ngModel)]="tipoSelecionado" />

  <!-- Preview descritivo do tipo selecionado -->
  @if (tipoSelecionado()) {
    <p class="tipo-descricao">{{ descricaoTipo(tipoSelecionado()) }}</p>
  }

  <!-- Dropdown de alvo (condicional por tipo) -->
  @if (mostrarAlvoAtributo()) {
    <p-dropdown label="Atributo alvo *"
      [options]="atributosDisponiveis()" optionLabel="nome" optionValue="id"
      [(ngModel)]="form.atributoAlvoId" />
  }
  @if (mostrarAlvoAptidao()) {
    <p-dropdown label="Aptidao alvo *"
      [options]="aptidoesDisponiveis()" optionLabel="nome" optionValue="id"
      [(ngModel)]="form.aptidaoAlvoId" />
  }
  @if (mostrarAlvoBonus()) {
    <p-dropdown label="Bonus alvo *"
      [options]="bonusDisponiveis()" optionLabel="nome" optionValue="id"
      [(ngModel)]="form.bonusAlvoId" />
  }
  @if (mostrarAlvoMembro()) {
    <p-dropdown label="Membro do corpo alvo *"
      [options]="membrosDisponiveis()" optionLabel="nome" optionValue="id"
      [(ngModel)]="form.membroAlvoId" />
  }

  <!-- Campos numericos (todos exceto DADO_UP e FORMULA_CUSTOMIZADA) -->
  @if (mostrarValorNumerico()) {
    <p-inputNumber label="Valor fixo" [(ngModel)]="form.valorFixo" />
    <p-inputNumber label="Valor por nivel" [(ngModel)]="form.valorPorNivel" />
    <!-- Preview calculado -->
    @if (podeMostrarPreview()) {
      <p class="preview">
        Previsao: No nivel {{ nivelPreview }}: +{{ calcularPreview() }}
        @if (form.atributoAlvoId || form.aptidaoAlvoId || form.bonusAlvoId || form.membroAlvoId) {
          em {{ nomeAlvo() }}
        }
      </p>
    }
  }

  <!-- Formula customizada (T10) -->
  @if (mostrarFormula()) {
    <app-formula-editor
      [(formula)]="form.formula"
      [variaveisDisponiveis]="variaveisFormula()" />
  }

  <!-- DADO_UP: apenas descricao informativa -->
  @if (isDadoUp()) {
    <div class="dado-up-info">
      <i class="pi pi-info-circle"></i>
      Cada nivel desta vantagem avanca o dado de prospeccao uma posicao na sequencia.
      <!-- T11: seletor visual de dados -->
    </div>
  }

  <!-- Descricao opcional -->
  <p-inputText label="Descricao do efeito (opcional)"
    [(ngModel)]="form.descricaoEfeito" />

  <div class="actions">
    <p-button label="Salvar efeito" (onClick)="salvar()" [disabled]="!podeSubmeter()" />
    <p-button label="Cancelar" severity="secondary" (onClick)="cancelar.emit()" />
  </div>
</div>
```

---

## Preview Calculado

Computar preview em tempo real para tipos numericos:

```typescript
nivelPreview = signal<number>(1);

calcularPreview = computed(() => {
  const fixo = this.form.valorFixo ?? 0;
  const porNivel = this.form.valorPorNivel ?? 0;
  return fixo + porNivel * this.nivelPreview();
});

// Input para ajustar o nivel do preview:
// <p-slider [(ngModel)]="nivelPreview" [min]="1" [max]="nivelMaximoVantagem()" />
```

---

## Validacoes Client-Side

| Campo | Regra |
|-------|-------|
| tipoEfeito | Obrigatorio sempre |
| atributoAlvoId | Obrigatorio se tipo == BONUS_ATRIBUTO |
| aptidaoAlvoId | Obrigatorio se tipo == BONUS_APTIDAO |
| bonusAlvoId | Obrigatorio se tipo == BONUS_DERIVADO |
| membroAlvoId | Obrigatorio se tipo == BONUS_VIDA_MEMBRO |
| valorFixo ou valorPorNivel | Ao menos um obrigatorio para tipos numericos |
| formula | Obrigatorio se tipo == FORMULA_CUSTOMIZADA |

```typescript
podeSubmeter = computed(() => {
  const tipo = this.tipoSelecionado();
  if (!tipo) return false;
  if (tipo === 'BONUS_ATRIBUTO' && !this.form.atributoAlvoId) return false;
  if (tipo === 'BONUS_APTIDAO' && !this.form.aptidaoAlvoId) return false;
  if (tipo === 'BONUS_DERIVADO' && !this.form.bonusAlvoId) return false;
  if (tipo === 'BONUS_VIDA_MEMBRO' && !this.form.membroAlvoId) return false;
  if (tipo === 'FORMULA_CUSTOMIZADA' && !this.form.formula) return false;
  const ehNumerico = !['DADO_UP', 'FORMULA_CUSTOMIZADA'].includes(tipo);
  if (ehNumerico && !this.form.valorFixo && !this.form.valorPorNivel) return false;
  return true;
});
```

---

## Testes (Vitest)

```typescript
describe('EfeitoFormComponent', () => {
  it('deve exibir dropdown de atributo apenas para BONUS_ATRIBUTO', () => {
    // Selecionar BONUS_ATRIBUTO → exibe dropdown de atributo
    // Selecionar BONUS_VIDA → oculta dropdown de atributo
  });

  it('deve calcular preview corretamente', () => {
    // valorFixo=2, valorPorNivel=3, nivelPreview=4 → preview = 14
  });

  it('deve desabilitar botao salvar sem campos obrigatorios', () => {
    // BONUS_ATRIBUTO sem atributoAlvoId → podeSubmeter = false
  });

  it('deve ocultar todos os campos numericos para DADO_UP', () => {
    // Selecionar DADO_UP → valorFixo, valorPorNivel, dropdowns ocultos
  });
});
```

---

## Criterios de Aceitacao

- [ ] Lista de efeitos exibida na tela de vantagem
- [ ] Formulario de novo efeito exibe campos corretos por tipo (tabela da spec)
- [ ] Preview calculado em tempo real para tipos numericos
- [ ] Botao salvar desabilitado se campos obrigatorios ausentes
- [ ] Confirmacao de delecao com dialogo
- [ ] Dropdowns de alvo exibem apenas configs do mesmo jogo
- [ ] Testes Vitest passando (EfeitoFormComponent)
- [ ] `npx vitest run` passa sem novos erros
