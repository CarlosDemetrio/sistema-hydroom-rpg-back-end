# T5 — Frontend: Edicao Inline de Anotacao com Markdown + Pastas

> Fase: P2 (Frontend)
> Estimativa: 1 dia
> Depende de: T1 (Backend PUT Anotacao + pastaPaiId), T0 (Backend AnotacaoPasta)
> Bloqueia: T8 (Testes)

---

## Objetivo

Adicionar modo de edicao inline com editor Markdown ao `AnotacaoCardComponent`. Integrar `ngx-markdown` para renderizacao de Markdown nas anotacoes. Adicionar suporte a pastas no `FichaAnotacoesTabComponent` — lista de anotacoes organizada por pasta usando `p-tree` do PrimeNG. Conectar ao novo endpoint `PUT /fichas/{fichaId}/anotacoes/{id}` e ao endpoint de pastas `/fichas/{fichaId}/anotacao-pastas`.

---

## Dependencia: ngx-markdown

Instalar antes de comecar:

```bash
npm install ngx-markdown marked
```

Registrar no `app.config.ts`:

```typescript
import { provideMarkdown } from 'ngx-markdown';

export const appConfig: ApplicationConfig = {
  providers: [
    // ...providers existentes...
    provideMarkdown()
  ]
};
```

> Ponto de atencao PA-004: verificar compatibilidade de versao com Angular 21 antes de instalar. Se houver problema, usar `marked` diretamente com pipe customizado + `DomSanitizer.bypassSecurityTrustHtml()` como alternativa.

---

## Regras de Negocio Relevantes

| Regra | Implementacao no frontend |
|-------|--------------------------|
| MESTRE pode editar qualquer anotacao | Botao "Editar" visivel para `userRole === 'MESTRE'` |
| JOGADOR pode editar apenas as proprias | Botao "Editar" visivel se `userRole === 'JOGADOR' && anotacao.autorId === userId` |
| JOGADOR nao pode alterar `visivelParaJogador` | Toggle so aparece no form de edicao se `userRole === 'MESTRE'` |
| Qualquer autor pode marcar `visivelParaTodos` | Toggle `visivelParaTodos` visivel para quem pode editar |
| `tipoAnotacao` e imutavel | Nao exibir campo `tipoAnotacao` no form de edicao |
| Conteudo e Markdown | Exibir com `<markdown>` no modo visualizacao; editar com `<textarea>` no modo edicao |

---

## Arquivos Afetados

### Criar
1. `src/app/core/models/anotacao-pasta.model.ts` — model de pastas

### Modificar
1. `src/app/core/models/anotacao.model.ts` — adicionar `AtualizarAnotacaoDto`, `pastaPaiId`, `visivelParaTodos`
2. `src/app/core/models/index.ts` — exportar `AnotacaoPasta`
3. `src/app/core/services/api/fichas-api.service.ts` — `editarAnotacao()` + endpoints de pasta
4. `src/app/core/services/business/ficha-business.service.ts` — delegar pasta e edicao
5. `src/app/features/jogador/pages/ficha-detail/components/anotacao-card/anotacao-card.component.ts` — modo edicao com Markdown
6. `src/app/features/jogador/pages/ficha-detail/components/ficha-anotacoes-tab/ficha-anotacoes-tab.component.ts` — arvore de pastas + filtro por pasta

---

## Passos de Implementacao

### 1. Criar `anotacao-pasta.model.ts`

```typescript
export interface AnotacaoPasta {
  id: number;
  fichaId: number;
  nome: string;
  pastaPaiId: number | null;
  ordemExibicao: number;
  subPastas: AnotacaoPasta[];
  dataCriacao: string;
  dataUltimaAtualizacao: string;
}

export interface CriarPastaDto {
  nome: string;
  pastaPaiId?: number;
  ordemExibicao?: number;
}

export interface AtualizarPastaDto {
  nome?: string;
  ordemExibicao?: number;
}
```

### 2. Atualizar `anotacao.model.ts`

```typescript
export interface Anotacao {
  id: number;
  fichaId: number;
  autorId: number;
  autorNome: string;
  titulo: string;
  conteudo: string;           // texto Markdown — campo existente, novo nome semantico
  tipoAnotacao: TipoAnotacao;
  visivelParaJogador: boolean;
  visivelParaTodos: boolean;  // novo campo
  pastaPaiId: number | null;  // novo campo
  dataCriacao: string;
  dataUltimaAtualizacao: string;
}

export interface AtualizarAnotacaoDto {
  titulo?: string;
  conteudo?: string;           // aceita Markdown
  visivelParaJogador?: boolean;
  visivelParaTodos?: boolean;  // novo
  pastaPaiId?: number | null;  // novo — null = mover para raiz
}
```

### 3. FichasApiService — adicionar metodos

```typescript
// Edicao de anotacao
editarAnotacao(fichaId: number, anotacaoId: number, dto: AtualizarAnotacaoDto): Observable<Anotacao> {
  return this.http.put<Anotacao>(`${this.baseUrl}/${fichaId}/anotacoes/${anotacaoId}`, dto);
}

// Listagem de anotacoes filtrada por pasta (pastaPaiId null = sem filtro)
listarAnotacoes(fichaId: number, pastaPaiId?: number): Observable<Anotacao[]> {
  const params = pastaPaiId != null ? { pastaPaiId: pastaPaiId.toString() } : {};
  return this.http.get<Anotacao[]>(`${this.baseUrl}/${fichaId}/anotacoes`, { params });
}

// Pastas
listarPastas(fichaId: number): Observable<AnotacaoPasta[]> {
  return this.http.get<AnotacaoPasta[]>(`${this.baseUrl}/${fichaId}/anotacao-pastas`);
}

criarPasta(fichaId: number, dto: CriarPastaDto): Observable<AnotacaoPasta> {
  return this.http.post<AnotacaoPasta>(`${this.baseUrl}/${fichaId}/anotacao-pastas`, dto);
}

deletarPasta(fichaId: number, pastaId: number): Observable<void> {
  return this.http.delete<void>(`${this.baseUrl}/${fichaId}/anotacao-pastas/${pastaId}`);
}
```

### 4. AnotacaoCardComponent — modo edicao com Markdown

**Estado do componente:**

```typescript
// Inputs existentes
anotacao = input.required<Anotacao>();
podeDeletar = input.required<boolean>();
userRole = input.required<'MESTRE' | 'JOGADOR'>();
userId = input.required<number>();

// Outputs
editar = output<Anotacao>();  // emite anotacao atualizada
deletar = output<number>();   // emite anotacao.id

// Estado de edicao
modoEdicao = signal(false);
tituloEdit = signal('');
conteudoEdit = signal('');      // Markdown bruto
visivelEdit = signal(false);
visivelParaTodosEdit = signal(false);
salvandoEdicao = signal(false);

// Computed
podeEditar = computed(() =>
  this.userRole() === 'MESTRE' ||
  this.anotacao().autorId === this.userId()
);
```

**Template — modo visualizacao com renderizacao Markdown:**

```html
@if (!modoEdicao()) {
  <p-card>
    <ng-template pTemplate="header">
      <div class="flex justify-between items-center px-3 pt-3">
        <span class="font-semibold">{{ anotacao().titulo }}</span>
        <div class="flex gap-1">
          @if (podeEditar()) {
            <p-button icon="pi pi-pencil" size="small" text (onClick)="iniciarEdicao()" />
          }
          @if (podeDeletar()) {
            <p-button icon="pi pi-trash" size="small" text severity="danger"
              (onClick)="deletar.emit(anotacao().id)" />
          }
        </div>
      </div>
    </ng-template>
    <!-- Renderizacao Markdown via ngx-markdown -->
    <markdown [data]="anotacao().conteudo" class="prose prose-sm" />
    <div class="flex gap-2 mt-2 text-xs text-color-secondary">
      <span>{{ anotacao().autorNome }}</span>
      @if (anotacao().tipoAnotacao === 'MESTRE' && userRole() === 'MESTRE') {
        @if (!anotacao().visivelParaJogador) {
          <p-badge value="Privado" severity="warning" />
        } @else {
          <p-badge value="Visivel ao Jogador" severity="success" />
        }
      }
      @if (anotacao().visivelParaTodos) {
        <p-badge value="Compartilhado" severity="info" />
      }
    </div>
  </p-card>
}
```

**Template — modo edicao com textarea Markdown:**

```html
@if (modoEdicao()) {
  <p-card>
    <div class="flex flex-col gap-3">
      <div>
        <label class="text-sm font-medium mb-1 block">Titulo</label>
        <input pInputText class="w-full" [ngModel]="tituloEdit()"
          (ngModelChange)="tituloEdit.set($event)" />
      </div>
      <div>
        <label class="text-sm font-medium mb-1 block">Conteudo (Markdown)</label>
        <textarea pTextarea class="w-full font-mono text-sm" rows="6"
          [ngModel]="conteudoEdit()"
          (ngModelChange)="conteudoEdit.set($event)"
          placeholder="Escreva em Markdown... **negrito**, _italico_, # titulo"></textarea>
      </div>
      @if (userRole() === 'MESTRE' && anotacao().tipoAnotacao === 'MESTRE') {
        <div class="flex items-center gap-2">
          <p-togglebutton
            [ngModel]="visivelEdit()"
            (ngModelChange)="visivelEdit.set($event)"
            onLabel="Visivel ao Jogador"
            offLabel="Privado" />
        </div>
      }
      <div class="flex items-center gap-2">
        <p-checkbox
          [ngModel]="visivelParaTodosEdit()"
          (ngModelChange)="visivelParaTodosEdit.set($event)"
          [binary]="true"
          inputId="visivelParaTodos" />
        <label for="visivelParaTodos" class="text-sm">Compartilhar com todos</label>
      </div>
      <div class="flex gap-2 justify-end">
        <p-button label="Cancelar" text (onClick)="cancelarEdicao()" />
        <p-button label="Salvar" [loading]="salvandoEdicao()" (onClick)="confirmarEdicao()" />
      </div>
    </div>
  </p-card>
}
```

**Metodos:**

```typescript
iniciarEdicao(): void {
  this.tituloEdit.set(this.anotacao().titulo);
  this.conteudoEdit.set(this.anotacao().conteudo);
  this.visivelEdit.set(this.anotacao().visivelParaJogador);
  this.visivelParaTodosEdit.set(this.anotacao().visivelParaTodos);
  this.modoEdicao.set(true);
}

cancelarEdicao(): void {
  this.modoEdicao.set(false);
}

confirmarEdicao(): void {
  this.editar.emit({
    ...this.anotacao(),
    titulo: this.tituloEdit(),
    conteudo: this.conteudoEdit(),
    visivelParaJogador: this.visivelEdit(),
    visivelParaTodos: this.visivelParaTodosEdit(),
  });
}
```

> AnotacaoCardComponent permanece [DUMB] — o evento `editar` e tratado pelo `FichaAnotacoesTabComponent` (SMART) que faz a chamada HTTP.

### 5. FichaAnotacoesTabComponent — arvore de pastas

O componente SMART precisa ser estendido para:

- Carregar `pastas: signal<AnotacaoPasta[]>([])` ao montar
- Exibir arvore de pastas usando `p-tree` do PrimeNG (converter `AnotacaoPasta[]` para `TreeNode[]`)
- Filtrar lista de anotacoes pela pasta selecionada (`pastaSelecionada: signal<AnotacaoPasta | null>(null)`)
- Ao clicar em pasta: buscar `GET /anotacoes?pastaPaiId=X` e atualizar lista
- Ao clicar em "Todas": buscar sem filtro de pasta

Conversao `AnotacaoPasta[]` → `TreeNode[]`:

```typescript
converterParaTreeNode(pastas: AnotacaoPasta[]): TreeNode[] {
  return pastas.map(p => ({
    key:      p.id.toString(),
    label:    p.nome,
    data:     p,
    children: p.subPastas?.length ? this.converterParaTreeNode(p.subPastas) : []
  }));
}
```

Template do seletor de pasta (sidebar lateral ou dropdown):

```html
<p-tree
  [value]="pastaTreeNodes()"
  selectionMode="single"
  [(selection)]="pastaSelecionadaNode"
  (onNodeSelect)="onPastaSelect($event)"
  styleClass="text-sm" />
```

---

## Imports necessarios

```typescript
// No componente que usa ngx-markdown:
import { MarkdownModule } from 'ngx-markdown';

// No componente que usa p-tree:
import { TreeModule } from 'primeng/tree';
import { TreeNode } from 'primeng/api';
```

---

## Criterios de Aceite

- [ ] `ngx-markdown` instalado e configurado no `app.config.ts`
- [ ] `AnotacaoCardComponent` renderiza conteudo como Markdown no modo visualizacao
- [ ] `AnotacaoCardComponent` exibe `<textarea>` com Markdown bruto no modo edicao
- [ ] Botao "Editar" aparece apenas para usuarios com permissao
- [ ] Toggle `visivelParaJogador` so aparece para MESTRE em modo edicao
- [ ] Toggle `visivelParaTodos` aparece para qualquer usuario com permissao de edicao
- [ ] Ao salvar: output `editar` emitido com DTO correto
- [ ] `FichaAnotacoesTabComponent` exibe arvore de pastas com `p-tree`
- [ ] Selecionar pasta filtra a lista de anotacoes
- [ ] `AnotacaoPasta` model criado e exportado via `index.ts`
- [ ] `FichasApiService` tem metodos de pasta e `editarAnotacao`
- [ ] `npm run build` sem erros de compilacao
