# T2 — HabilidadeConfig: Tela CRUD, Service HTTP, Signal Store e Testes

> Fase: Frontend | Dependencias: T1 (backend funcionando) | Bloqueia: nenhuma
> Estimativa: 3–4 horas

---

## Objetivo

Implementar a interface de gerenciamento de `HabilidadeConfig` no frontend Angular, permitindo a Mestres e Jogadores listar, criar, editar e remover habilidades de um jogo. A tela deve refletir a regra de negocio central desta entidade: **ambas as roles tem acesso completo**.

---

## Contexto

- Angular 21, PrimeNG 21.1.1, @ngrx/signals 21, Vitest, @testing-library/angular
- Usar `inject()` para DI — nunca constructor injection nos componentes
- Usar `signal()`, `computed()`, `model()`, `input()`, `output()` para estado reativo
- Usar `@if` / `@for` — nunca `*ngIf` / `*ngFor`
- Componentes standalone, sem `CommonModule`
- Testes com `vi.fn()` (Vitest)

**Contexto de acesso do Jogador:**
`HabilidadeConfig` e vinculada a um `Jogo` (campo `jogo_id`). As habilidades sao globais do jogo — nao sao por ficha nem por personagem. Um Jogador que acessa o jogo pode ver e gerenciar todas as habilidades do jogo, independentemente de quem as criou. Esta e uma decisao do PO. O ponto PA-021-03 (onde a tela aparece para o Jogador) permanece em aberto — verificar com UX Architect antes de integrar ao roteamento.

---

## Arquivos a Criar

| Arquivo | Descricao |
|---------|-----------|
| `core/models/habilidade-config.model.ts` | Interfaces TypeScript |
| `core/services/habilidade-config.service.ts` | Service HTTP |
| `features/configuracoes/habilidades/habilidade-config.store.ts` | Signal Store (@ngrx/signals) |
| `features/configuracoes/habilidades/habilidade-config-list.component.ts` | Componente de listagem + acoes |
| `features/configuracoes/habilidades/habilidade-config-list.component.html` | Template da listagem |
| `features/configuracoes/habilidades/habilidade-config-form.component.ts` | Formulario modal (criar/editar) |
| `features/configuracoes/habilidades/habilidade-config-form.component.html` | Template do formulario |
| `features/configuracoes/habilidades/habilidade-config.service.spec.ts` | Testes do service |
| `features/configuracoes/habilidades/habilidade-config.store.spec.ts` | Testes do store |

---

## Modelo TypeScript

```typescript
// core/models/habilidade-config.model.ts

export interface HabilidadeConfig {
  id: number;
  jogoId: number;
  nome: string;
  descricao: string | null;
  danoEfeito: string | null;
  ordemExibicao: number;
  dataCriacao: string;
  dataUltimaAtualizacao: string;
}

export interface CreateHabilidadeConfigRequest {
  nome: string;
  descricao?: string | null;
  danoEfeito?: string | null;
  ordemExibicao: number;
}

export interface UpdateHabilidadeConfigRequest {
  nome?: string;
  descricao?: string | null;
  danoEfeito?: string | null;
  ordemExibicao?: number;
}
```

---

## Service HTTP

```typescript
// core/services/habilidade-config.service.ts

import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  HabilidadeConfig,
  CreateHabilidadeConfigRequest,
  UpdateHabilidadeConfigRequest
} from '../models/habilidade-config.model';

@Injectable({ providedIn: 'root' })
export class HabilidadeConfigService {
  private readonly http = inject(HttpClient);

  private url(jogoId: number): string {
    return `/api/v1/jogos/${jogoId}/habilidades`;
  }

  listar(jogoId: number): Observable<HabilidadeConfig[]> {
    return this.http.get<HabilidadeConfig[]>(this.url(jogoId));
  }

  buscarPorId(jogoId: number, id: number): Observable<HabilidadeConfig> {
    return this.http.get<HabilidadeConfig>(`${this.url(jogoId)}/${id}`);
  }

  criar(jogoId: number, dto: CreateHabilidadeConfigRequest): Observable<HabilidadeConfig> {
    return this.http.post<HabilidadeConfig>(this.url(jogoId), dto);
  }

  atualizar(jogoId: number, id: number, dto: UpdateHabilidadeConfigRequest): Observable<HabilidadeConfig> {
    return this.http.put<HabilidadeConfig>(`${this.url(jogoId)}/${id}`, dto);
  }

  deletar(jogoId: number, id: number): Observable<void> {
    return this.http.delete<void>(`${this.url(jogoId)}/${id}`);
  }
}
```

---

## Signal Store

```typescript
// features/configuracoes/habilidades/habilidade-config.store.ts

import { signalStore, withState, withMethods, patchState } from '@ngrx/signals';
import { inject } from '@angular/core';
import { HabilidadeConfigService } from '../../../core/services/habilidade-config.service';
import {
  HabilidadeConfig,
  CreateHabilidadeConfigRequest,
  UpdateHabilidadeConfigRequest
} from '../../../core/models/habilidade-config.model';

interface HabilidadeConfigState {
  habilidades: HabilidadeConfig[];
  loading: boolean;
  error: string | null;
}

const initialState: HabilidadeConfigState = {
  habilidades: [],
  loading: false,
  error: null,
};

export const HabilidadeConfigStore = signalStore(
  withState(initialState),
  withMethods((store, service = inject(HabilidadeConfigService)) => ({
    async carregarHabilidades(jogoId: number): Promise<void> {
      patchState(store, { loading: true, error: null });
      try {
        const habilidades = await firstValueFrom(service.listar(jogoId));
        patchState(store, { habilidades, loading: false });
      } catch {
        patchState(store, { error: 'Erro ao carregar habilidades.', loading: false });
      }
    },

    async criarHabilidade(jogoId: number, dto: CreateHabilidadeConfigRequest): Promise<void> {
      patchState(store, { loading: true, error: null });
      try {
        const nova = await firstValueFrom(service.criar(jogoId, dto));
        patchState(store, {
          habilidades: [...store.habilidades(), nova]
            .sort((a, b) => a.ordemExibicao - b.ordemExibicao),
          loading: false,
        });
      } catch {
        patchState(store, { error: 'Erro ao criar habilidade.', loading: false });
        throw new Error('Erro ao criar habilidade.');
      }
    },

    async atualizarHabilidade(
      jogoId: number, id: number, dto: UpdateHabilidadeConfigRequest
    ): Promise<void> {
      patchState(store, { loading: true, error: null });
      try {
        const atualizada = await firstValueFrom(service.atualizar(jogoId, id, dto));
        patchState(store, {
          habilidades: store.habilidades().map(h => h.id === id ? atualizada : h),
          loading: false,
        });
      } catch {
        patchState(store, { error: 'Erro ao atualizar habilidade.', loading: false });
        throw new Error('Erro ao atualizar habilidade.');
      }
    },

    async deletarHabilidade(jogoId: number, id: number): Promise<void> {
      patchState(store, { loading: true, error: null });
      try {
        await firstValueFrom(service.deletar(jogoId, id));
        patchState(store, {
          habilidades: store.habilidades().filter(h => h.id !== id),
          loading: false,
        });
      } catch {
        patchState(store, { error: 'Erro ao deletar habilidade.', loading: false });
        throw new Error('Erro ao deletar habilidade.');
      }
    },
  }))
);
```

**Nota:** importar `firstValueFrom` de `rxjs`.

---

## Componente de Listagem: `HabilidadeConfigListComponent`

### Comportamento esperado

- Tabela PrimeNG (`p-table`) com colunas: Ordem, Nome, Dano/Efeito (truncado a 60 chars), Acoes
- Coluna Acoes: botoes Editar e Excluir — visíveis para MESTRE e JOGADOR
- Botao "Nova Habilidade" no cabecalho — visivel para MESTRE e JOGADOR
- Ao clicar Editar: abre `HabilidadeConfigFormComponent` em modal com dados preenchidos
- Ao clicar Excluir: confirmacao via `ConfirmDialog` do PrimeNG antes de deletar
- Estado de loading: skeleton ou spinner na tabela enquanto `store.loading()` for true
- Estado de erro: `p-message` com severidade "error" quando `store.error()` for nao-null

### Template (resumido)

```html
<!-- habilidade-config-list.component.html -->

<div class="flex justify-between items-center mb-4">
  <h2 class="text-xl font-semibold">Habilidades</h2>
  <p-button
    label="Nova Habilidade"
    icon="pi pi-plus"
    (onClick)="abrirFormulario(null)"
  />
</div>

@if (store.error()) {
  <p-message severity="error" [text]="store.error()!" />
}

<p-table
  [value]="store.habilidades()"
  [loading]="store.loading()"
  [tableStyle]="{ 'min-width': '50rem' }"
  stripedRows
>
  <ng-template pTemplate="header">
    <tr>
      <th style="width: 4rem">Ordem</th>
      <th>Nome</th>
      <th>Dano / Efeito</th>
      <th style="width: 8rem">Acoes</th>
    </tr>
  </ng-template>
  <ng-template pTemplate="body" let-habilidade>
    <tr>
      <td>{{ habilidade.ordemExibicao }}</td>
      <td>{{ habilidade.nome }}</td>
      <td>{{ habilidade.danoEfeito | truncate:60 }}</td>
      <td>
        <p-button
          icon="pi pi-pencil"
          [text]="true"
          severity="secondary"
          (onClick)="abrirFormulario(habilidade)"
        />
        <p-button
          icon="pi pi-trash"
          [text]="true"
          severity="danger"
          (onClick)="confirmarDelecao(habilidade)"
        />
      </td>
    </tr>
  </ng-template>
  <ng-template pTemplate="emptymessage">
    <tr>
      <td colspan="4" class="text-center text-muted-color">
        Nenhuma habilidade cadastrada.
      </td>
    </tr>
  </ng-template>
</p-table>

<p-confirmDialog />

<habilidade-config-form
  [visible]="formVisivel()"
  [habilidade]="habilidadeSelecionada()"
  [jogoId]="jogoId()"
  (salvo)="aoSalvar($event)"
  (fechado)="fecharFormulario()"
/>
```

**Nota sobre o pipe `truncate`:** Verificar se o projeto ja tem um pipe de truncamento. Se nao, usar uma funcao no componente ou implementar pipe simples.

---

## Componente de Formulario: `HabilidadeConfigFormComponent`

### Inputs/Outputs

```typescript
// Inputs
jogoId = input.required<number>();
habilidade = input<HabilidadeConfig | null>(null);  // null = modo criacao
visible = input<boolean>(false);

// Outputs
salvo = output<HabilidadeConfig>();
fechado = output<void>();
```

### Campos do formulario

| Campo | Componente PrimeNG | Validacao |
|-------|-------------------|-----------|
| Nome | `p-inputtext` | Obrigatorio, max 100 chars |
| Descricao | `p-textarea` | Opcional, max 1000 chars |
| Dano / Efeito | `p-inputtext` | Opcional, max 500 chars, placeholder: "Ex: 2D6+FOR de dano fisico" |
| Ordem de Exibicao | `p-inputnumber` | Obrigatorio, minimo 0 |

### Comportamento

- Titulo do dialog: "Nova Habilidade" (criacao) ou "Editar Habilidade" (edicao)
- Ao abrir em modo edicao: preencher campos com dados de `habilidade`
- Botao "Salvar": desabilitado se `nome` vazio ou `ordemExibicao` nulo
- Ao salvar com sucesso: emite evento `salvo` com a entidade retornada da API e fecha o dialog
- Ao salvar com erro 409 (nome duplicado): exibe mensagem "Ja existe uma habilidade com este nome."
- Ao cancelar ou fechar: emite `fechado` sem salvar

---

## Testes

### Service (`habilidade-config.service.spec.ts`)

Cobrir:
- `listar()` — GET correto, retorno mapeado
- `criar()` — POST com body correto
- `atualizar()` — PUT com body correto
- `deletar()` — DELETE sem body

```typescript
// Padrao de teste com HttpTestingController (verificar padrao do projeto)
// Ou com vi.fn() mockando o HttpClient
```

### Store (`habilidade-config.store.spec.ts`)

Cobrir:
- `carregarHabilidades()` — atualiza `habilidades`, reseta `loading`
- `criarHabilidade()` — adiciona ao array e ordena
- `atualizarHabilidade()` — substitui no array
- `deletarHabilidade()` — remove do array
- Erros: `store.error()` preenchido quando service falha

---

## Checklist de Implementacao

- [ ] Interfaces TypeScript criadas em `core/models/habilidade-config.model.ts`
- [ ] Service HTTP com os 5 metodos (listar, buscarPorId, criar, atualizar, deletar)
- [ ] Signal Store com estado `habilidades`, `loading`, `error`
- [ ] Componente de listagem com tabela PrimeNG
- [ ] Coluna Dano/Efeito truncada a 60 caracteres
- [ ] Botao "Nova Habilidade" visivel para MESTRE e JOGADOR (sem restricao de role no template)
- [ ] Botoes Editar e Excluir visiveis para MESTRE e JOGADOR
- [ ] Dialog de confirmacao antes de deletar
- [ ] Componente de formulario modal (criar/editar)
- [ ] Validacao client-side: nome obrigatorio, ordemExibicao obrigatorio e >= 0
- [ ] Tratamento de erro 409 com mensagem especifica
- [ ] Testes Vitest: service (4 casos) + store (5 casos)
- [ ] `npx vitest run` passa 100% verde

---

## Pontos em Aberto

**PA-021-03 — Onde a tela aparece para o Jogador:**
A tela de habilidades existe no painel de configuracoes (visivel apenas para MESTRE) ou deve tambem aparecer para o JOGADOR? Como JOGADOR pode criar/editar/deletar, a tela precisa de um caminho de acesso para ele. Opcoes:
1. A pagina de configuracoes do jogo passa a ser acessivel para MESTRE e JOGADOR
2. Existe uma secao separada para o Jogador com as entidades que ele pode gerenciar

Verificar com UX Architect antes de integrar ao roteamento. A task pode ser implementada sem rota definida (componente pronto, roteamento resolvido depois).

---

## Commit

```
feat(frontend): HabilidadeConfig tela CRUD — list, form, store, testes [S021 T2]
```

---

*Produzido por: Business Analyst/PO | 2026-04-12*
