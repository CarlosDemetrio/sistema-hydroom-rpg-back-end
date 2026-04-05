# T7 — Frontend: Toggle de Visibilidade NPC por Jogador

> Tipo: Frontend (Angular)
> Dependencias: T2 (backend visibilidade pronto e testado)
> Design: `docs/design/NPC-VISIBILITY.md`

---

## Objetivo

Implementar o `NpcVisibilidadeComponent` e integrá-lo ao `FichaDetailPage`, conforme o design em `NPC-VISIBILITY.md`. O Mestre ve um painel lateral (desktop) ou bottom drawer (mobile) com toggle global e seletor multiselect de jogadores. Jogadores veem badge "Aliado" nos NPCs revelados para eles.

---

## Arquivos a Criar/Alterar

### Novos

| Arquivo | Tipo |
|---------|------|
| `ficha/components/npc-visibilidade/npc-visibilidade.component.ts` | Dumb component |
| `ficha/services/ficha-visibilidade.api.service.ts` | API service |

### Alterados

| Arquivo | Alteracao |
|---------|-----------|
| `ficha/pages/ficha-detail/ficha-detail.page.ts` | Adicionar computed `mostrarPainelNpc`, signals para `jogadoresComAcesso`, layout 2 colunas quando NPC + MESTRE |
| `ficha/components/ficha-header/ficha-header.component.ts` | Badge de visibilidade quando NPC e MESTRE |
| `ficha/pages/fichas-list/fichas-list.page.ts` | Card de NPC para Jogador com badge "Aliado" e cadeado |

---

## Passos

### Passo 1 — FichaVisibilidadeApiService

```typescript
@Injectable({ providedIn: 'root' })
export class FichaVisibilidadeApiService {
  private readonly http = inject(HttpClient);
  private readonly BASE = '/api/v1/fichas';

  listarVisibilidade(fichaId: number): Observable<FichaVisibilidadeResponse> {
    return this.http.get<FichaVisibilidadeResponse>(`${this.BASE}/${fichaId}/visibilidade`);
  }

  atualizarVisibilidade(fichaId: number, dto: AtualizarVisibilidadeDto): Observable<FichaVisibilidadeResponse> {
    return this.http.post<FichaVisibilidadeResponse>(`${this.BASE}/${fichaId}/visibilidade`, dto);
  }

  revogarAcesso(fichaId: number, jogadorId: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/${fichaId}/visibilidade/${jogadorId}`);
  }

  atualizarGlobal(fichaId: number, visivelGlobalmente: boolean): Observable<FichaResponse> {
    return this.http.patch<FichaResponse>(`${this.BASE}/${fichaId}/visibilidade/global`, { visivelGlobalmente });
  }
}
```

### Passo 2 — Interfaces TypeScript

```typescript
// em ficha.model.ts ou visibilidade.model.ts
export interface FichaVisibilidadeResponse {
  fichaId: number;
  visivelGlobalmente: boolean;
  jogadoresComAcesso: JogadorAcessoItem[];
}

export interface JogadorAcessoItem {
  jogadorId: number;
  jogadorNome: string;
  nomePersonagem: string;
}

export interface NpcVisibilidadeUpdate {
  visivelParaJogadores: boolean;
  jogadoresComAcesso: number[];
}
```

Adicionar em `FichaResponse`:
```typescript
visivelGlobalmente?: boolean;
jogadorTemAcessoStats?: boolean;
```

### Passo 3 — NpcVisibilidadeComponent

Props (`input()`):
- `fichaId = input.required<number>()`
- `jogoId = input.required<number>()`
- `visivelGlobalmente = input<boolean>(false)`
- `jogadoresComAcesso = input<number[]>([])`

Outputs (`output()`):
- `visibilidadeAtualizada = output<NpcVisibilidadeUpdate>()`

Signals internos:
- `visivelGlobalmenteLocal = signal<boolean>(false)` — inicializado do input
- `jogadoresSelecionados = signal<number[]>([])`
- `salvando = signal<boolean>(false)`
- `houveAlteracao = computed(() => ...)`
- `participantesAprovados = signal<JogadorAcessoItem[]>([])` — carregado via `ParticipantesApiService`

Template: conforme wireframe em `NPC-VISIBILITY.md` secao 4.

### Passo 4 — FichaDetailPage

Adicionar:
```typescript
mostrarPainelNpc = computed(() => this.ficha()?.isNpc && this.isMestre());
jogadoresComAcesso = signal<number[]>([]);
drawerVisibilidadeAberto = signal<boolean>(false);
```

Carregar `jogadoresComAcesso` ao inicializar se `mostrarPainelNpc() === true`:
```typescript
if (this.mostrarPainelNpc()) {
  this.fichaVisibilidadeApiService.listarVisibilidade(fichaId).subscribe(v => {
    this.jogadoresComAcesso.set(v.jogadoresComAcesso.map(j => j.jogadorId));
  });
}
```

Layout: usar `[class.lg:grid-cols-3]="mostrarPainelNpc()"` conforme design.

### Passo 5 — FichaHeaderComponent

Adicionar badge de visibilidade quando NPC + Mestre (conforme secao 8 do design).

### Passo 6 — Card de NPC na lista (Jogador)

Na `FichasListPage` (ou componente de card de ficha):
- Se `npc.jogadorTemAcessoStats === true`: exibir badge "Aliado" (severity="success") e botao "Ver ficha completa"
- Se `npc.jogadorTemAcessoStats === false` (mas aparece na lista): exibir cadeado + "Estatísticas ocultadas"

---

## Testes (Vitest)

- `NpcVisibilidadeComponent`: testar render do toggle, multiselect, botao salvar desabilitado sem alteracoes, botao salvar habilitado apos alterar
- `FichaDetailPage`: testar `mostrarPainelNpc` computed (true quando isNpc+MESTRE, false contrario)
- `FichaVisibilidadeApiService`: testar chamadas HTTP com `vi.fn()`

---

## Criterios de Aceitacao

- [ ] Painel lateral aparece em desktop quando `isNpc=true` e `role=MESTRE`
- [ ] Drawer bottom aparece em mobile quando `isNpc=true` e `role=MESTRE`, acionado por botao na toolbar
- [ ] Toggle "visivel globalmente" atualiza via PATCH e exibe toast de sucesso/erro
- [ ] Multiselect lista participantes aprovados do jogo
- [ ] Botao "Salvar visibilidade" desabilitado se nao houve alteracao
- [ ] Botao "Salvar visibilidade" com loading state durante request
- [ ] Badge "Vee stats" / "Nao ve" por participante na lista de status
- [ ] Badge "Aliado" aparece para Jogador com acesso granular no card do NPC
- [ ] Cadeado aparece para Jogador sem acesso granular no card de NPC visivel globalmente
- [ ] NPCs com `visivelGlobalmente=false` nao aparecem na lista do Jogador
- [ ] Toast de sucesso ao salvar visibilidade
- [ ] Sem erros TypeScript, sem warnings de build
