# Design Spec: Reset de Estado pelo Mestre

> Documento de design UI/UX para o painel de ações administrativas do Mestre em fichas.
> Destina-se ao angular-frontend-dev e angular-tech-lead.
> Versao: 1.0 | Gerado em: 2026-04-02
> Visivel apenas para: role === 'MESTRE'

---

## 1. Visao Geral e Contexto

O Mestre precisa de um ponto centralizado para executar acoes de reset em fichas durante ou apos uma sessao. Essas acoes sao **irreversiveis** (com excecao de vida, que e recalculada a partir dos danoRecebido dos membros), e devem ser protegidas por confirmacao explicita.

**Casos de uso reais:**
- Inicio de nova sessao: resetar vida de todos os personagens (curar completamente)
- Fim de combate: resetar essencia de um ou todos os personagens
- Resolucao de erro de calculo: forcar recalculo de prospeccao
- Encerramento de arco: reset em lote de varios campos ao mesmo tempo

**O que pode ser resetado:**

| Campo | O que faz | Endpoint (a confirmar) |
|-------|-----------|------------------------|
| Vida | `danoRecebido = 0` em todos os `MembroCorpo` da ficha | `POST /fichas/{id}/reset/vida` |
| Essencia | Essencia atual volta ao total calculado | `POST /fichas/{id}/reset/essencia` |
| Prospeccao | Remove usos pendentes e zera contador de usos (NAO remove o estoque de dados) | `POST /fichas/{id}/reset/prospeccao` |

**Vida em lote** (reset de todos os personagens do jogo em uma unica acao) e um caso de uso frequente no inicio de cada sessao — deve ter acao de alto nivel na tela de sessao do Mestre, nao apenas dentro de cada ficha individual.

---

## 2. Localizacao na UI

### Decisao de layout: Menu "Acoes do Mestre" no FichaHeaderComponent

As acoes de reset NAO ficam em uma aba separada. Ficam em um menu dropdown de "Acoes do Mestre" no canto superior direito do `FichaHeaderComponent`, visivel apenas para o Mestre.

**Racional:** Acoes destrutivas nao merecem uma aba permanente que o Mestre veria toda vez que abrisse a ficha. Um menu contextual discreto comunica melhor a natureza administrativa e excepcional dessas acoes.

```
┌─────────────────────────────────────────────────────────────────────┐
│  [p-avatar]  Aldric, Filho da Névoa          [p-tag "Nv. 5" filled] │
│  letra A     Humano • Guerreiro                                      │
│              Índole: Caótico Bom • Presença: Imponente               │
├─────────────────────────────────────────────────────────────────────┤
│  Vida [████████░░░░] 25/30   Essência [██████░░] 12/20              │
│  Ameaça: 16  XP: 4200/6000 [p-progressBar XP]                       │
├─────────────────────────────────────────────────────────────────────┤
│  [Editar]  [Duplicar]  [Deletar]  [•••] ← menu Acoes do Mestre      │
└─────────────────────────────────────────────────────────────────────┘
```

O botao `[•••]` (icone `pi pi-ellipsis-v`) abre um `p-menu` com as opcoes de reset.

---

## 3. Wireframes

### 3.1 Desktop — Menu "Acoes do Mestre"

```
┌──────────────────────────────────────────────────────────────────────────┐
│ [p-toolbar] [<] Fichas   Nome do Jogo   [Avatar]                         │
├──────────────────────────────────────────────────────────────────────────┤
│  [FichaHeaderComponent]                                                  │
│   Aldric, Filho da Névoa     [Nv. 5]                                     │
│   Humano • Guerreiro                                                     │
│   Vida [████████░░] 25/30    Essência [█████░░] 12/20                   │
│                                                                          │
│   [Editar]  [Duplicar]  [Deletar]  [... Ações do Mestre ▾]              │
│                                     ┌──────────────────────┐            │
│                                     │ ↩ Resetar Vida       │            │
│                                     │ ↩ Resetar Essência   │            │
│                                     │ ↩ Resetar Prospecção │            │
│                                     │ ─────────────────── │            │
│                                     │ + Conceder XP        │            │
│                                     │ + Conceder Dado      │            │
│                                     └──────────────────────┘            │
├──────────────────────────────────────────────────────────────────────────┤
│  [p-tabs] Resumo | Atributos | Aptidoes | Vantagens | Anotacoes          │
│  [Conteudo da aba ativa]                                                 │
└──────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Mobile — Menu de Acoes via p-drawer

Em mobile, o botao `[...]` abre um `p-drawer` pelo rodape (position="bottom") com as mesmas opcoes em lista vertical, com touch targets maiores (min 48px de altura por item).

```
┌─────────────────────────────┐
│ [<] Fichas       [...] [Ed] │  ← p-toolbar compacto
├─────────────────────────────┤
│ [FichaHeaderComponent]      │
│ Aldric, Filho da Névoa      │
│ Vida [████] 25/30           │
│ Essência [███] 12/20        │
├─────────────────────────────┤
│ [p-tabs scrollavel]         │
│ [Conteudo da aba ativa]     │
└─────────────────────────────┘

    ↕ ao clicar em [...]:

┌─────────────────────────────┐
│  Ações do Mestre            │  ← p-drawer position="bottom"
│  ──────────────             │    h="auto"
│                             │
│  ↩ Resetar Vida             │  ← item com icone + label + descricao
│    Cura toda a vida do pers.│
│  ──────────────────         │
│  ↩ Resetar Essência         │
│    Restaura essência total  │
│  ──────────────────         │
│  ↩ Resetar Prospecção       │
│    Remove usos pendentes    │
│  ──────────────────         │
│  + Conceder XP              │
│  + Conceder Dado            │
│                             │
│  [Fechar]                   │
└─────────────────────────────┘
```

---

## 4. Componente: MestreAcoesMenuComponent

**Arquivo**: `ficha/components/mestre-acoes-menu/mestre-acoes-menu.component.ts`
**Selector**: `app-mestre-acoes-menu`
**Tipo**: Dumb (recebe fichaId e emite eventos para o pai orquestrar os requests)
**Renderizado apenas quando**: `role === 'MESTRE'`

### Props

```typescript
fichaId = input.required<number>();
fichaNome = input.required<string>();
loading = input<boolean>(false);

resetarVidaClick = output<void>();
resetarEssenciaClick = output<void>();
resetarProspeccaoClick = output<void>();
concederXpClick = output<void>();
concederDadoClick = output<void>();
```

### Template (Desktop — p-menu)

```html
<p-button
  icon="pi pi-ellipsis-v"
  text rounded
  severity="secondary"
  (onClick)="menu.toggle($event)"
  aria-label="Acoes administrativas do Mestre"
  pTooltip="Acoes do Mestre"
  tooltipPosition="bottom" />

<p-menu #menu [popup]="true" [model]="menuItems()" />
```

### Itens do Menu (computed signal)

```typescript
protected menuItems = computed((): MenuItem[] => [
  {
    label: 'Resets',
    items: [
      {
        label: 'Resetar Vida',
        icon: 'pi pi-heart',
        command: () => this.resetarVidaClick.emit(),
      },
      {
        label: 'Resetar Essência',
        icon: 'pi pi-bolt',
        command: () => this.resetarEssenciaClick.emit(),
      },
      {
        label: 'Resetar Prospecção',
        icon: 'pi pi-refresh',
        command: () => this.resetarProspeccaoClick.emit(),
      },
    ],
  },
  { separator: true },
  {
    label: 'Conceder',
    items: [
      {
        label: 'Conceder XP',
        icon: 'pi pi-star',
        command: () => this.concederXpClick.emit(),
      },
      {
        label: 'Conceder Dado de Prospecção',
        icon: 'pi pi-gift',
        command: () => this.concederDadoClick.emit(),
      },
    ],
  },
]);
```

---

## 5. Fluxo de Reset com Confirmacao Obrigatoria

Cada acao de reset segue o mesmo padrao de 3 etapas:

```
[Clique no menu] → [p-confirmDialog com impacto] → [Request API] → [Toast sucesso/erro]
```

### 5.1 Reset de Vida

```typescript
// No FichaDetailPage (Smart), ao receber o evento de resetarVida:
protected onResetarVida(): void {
  this.confirmationService.confirm({
    header: 'Resetar vida de ' + this.ficha()!.nome + '?',
    message: `O dano recebido em todos os membros do corpo de <strong>${this.ficha()!.nome}</strong>
              sera zerado. O personagem voltara com vida completa (${this.resumo()!.vidaTotal} PV).
              <br><br>Esta acao nao pode ser desfeita.`,
    acceptLabel: 'Sim, resetar vida',
    rejectLabel: 'Cancelar',
    acceptIcon: 'pi pi-heart',
    acceptButtonStyleClass: 'p-button-success',
    accept: () => this.executarReset('vida'),
  });
}
```

```html
<!-- p-confirmDialog com template customizado para exibir impacto -->
<p-confirmDialog>
  <ng-template #message let-msg>
    <div class="flex flex-col gap-3">
      <div class="flex items-start gap-3">
        <div class="flex items-center justify-center w-12 h-12 rounded-full bg-green-100 flex-shrink-0">
          <i class="pi pi-heart text-green-600 text-xl"></i>
        </div>
        <p class="m-0 text-sm leading-relaxed" [innerHTML]="msg.message"></p>
      </div>
      <p-message severity="warn">
        <ng-template #messageicon><i class="pi pi-exclamation-triangle mr-2"></i></ng-template>
        Esta acao afeta todos os membros do corpo do personagem simultaneamente.
      </p-message>
    </div>
  </ng-template>
</p-confirmDialog>
```

### 5.2 Reset de Essencia

Mesmo padrao. Mensagem especifica:

```
"A essência de [nome] voltará ao total calculado ([X] pontos).
 Usos de essência durante a sessão serao descartados."
```

`acceptButtonStyleClass`: `p-button-info`
Icone do dialog: `pi pi-bolt` com fundo azul claro.

### 5.3 Reset de Prospeccao

Mais cuidadoso — este reset afeta usos pendentes. Mensagem especifica:

```
"Os usos pendentes de prospecção de [nome] serão removidos.
 O estoque de dados NÃO será alterado.
 Esta acao e util para corrigir erros de registro."
```

`acceptButtonStyleClass`: `p-button-warning`
Icone: `pi pi-refresh` com fundo amarelo claro.

---

## 6. Execucao do Request e Feedback

```typescript
protected async executarReset(tipo: 'vida' | 'essencia' | 'prospeccao'): Promise<void> {
  this.loading.set(true);

  this.fichasApiService.resetarEstado(this.fichaId(), tipo).subscribe({
    next: () => {
      const mensagens = {
        vida: { summary: 'Vida resetada', detail: `${this.ficha()!.nome} voltou com vida completa.` },
        essencia: { summary: 'Essência restaurada', detail: `Essência de ${this.ficha()!.nome} foi restaurada.` },
        prospeccao: { summary: 'Prospecção limpa', detail: `Usos pendentes de prospecção removidos.` },
      };
      this.toastService.add({ severity: 'success', ...mensagens[tipo], life: 4000 });
      // Recarregar resumo para atualizar barras de vida/essencia
      this.carregarResumo();
      this.loading.set(false);
    },
    error: (err) => {
      this.toastService.add({
        severity: 'error',
        summary: 'Erro ao resetar',
        detail: 'Nao foi possivel executar o reset. Tente novamente.',
        life: 5000,
      });
      this.loading.set(false);
    },
  });
}
```

---

## 7. Reset em Lote — Tela de Sessao do Mestre

Para resetar a vida de **todos** os personagens de um jogo de uma vez, o Mestre usa a tela de sessao (ver `MODO-SESSAO.md`). Esta funcionalidade NAO esta dentro da ficha individual.

O botao de reset em lote na tela de sessao usa a mesma logica de confirmacao, mas com impacto descrito para todos os personagens:

```
"A vida de TODOS os personagens do jogo sera resetada.
 [N] fichas serao afetadas.
 Confirmar antes de iniciar a sessao?"
```

Endpoint sugerido: `POST /jogos/{jogoId}/reset/vida-todos`

---

## 8. Conceder XP (inline no menu)

Ao clicar em "Conceder XP", abre um `p-dialog` simples (nao um confirm, pois nao e irreversivel da mesma forma):

```
┌─────────────────────────────────┐
│ Conceder XP                     │
│                                 │
│ Personagem: Aldric, Filho da    │
│             Névoa (Nv. 5)       │
│                                 │
│ XP atual: 4.200                 │
│ Proximo nível em: 1.800 XP      │
│                                 │
│ Quantidade de XP:               │
│ [p-inputNumber min=1 max=99999] │
│                                 │
│ [Cancelar]  [Conceder XP]       │
└─────────────────────────────────┘
```

Apos conceder XP, se o nivel do personagem subir, exibir `p-toast` com severidade `success` e mensagem especial de level up (ver `LEVEL-UP.md`).

---

## 9. Estados da UI

### Carregando (durante request de reset)

- Botao do menu fica desabilitado com spinner enquanto request esta ativo
- Sobreposicao sutil de loading no FichaHeaderComponent (opcao: skeleton sobre as barras de vida/essencia)

### Sucesso

- `p-toast` com severidade `success`, duracao 4000ms
- Barras de vida/essencia atualizam imediatamente via recarga do resumo

### Erro

- `p-toast` com severidade `error`, duracao 5000ms
- Mensagem generica + sugestao de tentar novamente
- Estado local NAO e atualizado otimisticamente para resets (risco de inconsistencia)

### Estado vazio (ficha sem membros de corpo para reset de vida)

```html
@if (membrosCorpo().length === 0) {
  <p-message severity="info">
    <ng-template #messageicon><i class="pi pi-info-circle mr-2"></i></ng-template>
    Esta ficha nao possui membros do corpo configurados. O reset de vida nao tera efeito.
  </p-message>
}
```

---

## 10. Componentes PrimeNG Utilizados

| Componente | Modulo | Uso |
|-----------|--------|-----|
| `p-menu` | `MenuModule` | Menu popup de acoes do Mestre |
| `p-button` | `ButtonModule` | Botao de abertura do menu, confirmacao |
| `p-confirmDialog` | `ConfirmDialogModule` | Confirmacao obrigatoria antes de cada reset |
| `p-dialog` | `DialogModule` | Dialog de concessao de XP |
| `p-toast` | `ToastModule` | Feedback de sucesso/erro |
| `p-message` | `MessageModule` | Avisos contextuais no confirmDialog |
| `p-inputNumber` | `InputNumberModule` | Quantidade de XP a conceder |
| `p-drawer` | `DrawerModule` | Menu de acoes em mobile (position="bottom") |

---

## 11. Acessibilidade

- Botao `[...]`: `aria-label="Acoes administrativas do Mestre"` — nunca rotular apenas como "..."
- `p-menu`: navega com teclas de seta; items com `role="menuitem"`
- `p-confirmDialog`: foco preso no dialog quando aberto; `aria-modal="true"`; botao de aceitar recebe foco inicial no botao MENOS destrutivo (cancelar, nao confirmar)
- `p-drawer` mobile: foco movido para o drawer ao abrir; `aria-label="Menu de acoes do Mestre"` no drawer
- Icones com `pi-*` decorativos nunca transmitem a unica informacao — sempre acompanhados de label textual

---

## 12. Controle de Visibilidade por Role

```typescript
// No FichaDetailPage ou FichaHeaderComponent:
protected isMestre = computed(() =>
  this.authService.currentUser()?.role === 'MESTRE'
);
```

```html
<!-- Renderizado APENAS se Mestre -->
@if (isMestre()) {
  <app-mestre-acoes-menu
    [fichaId]="ficha()!.id"
    [fichaNome]="ficha()!.nome"
    (resetarVidaClick)="onResetarVida()"
    (resetarEssenciaClick)="onResetarEssencia()"
    (resetarProspeccaoClick)="onResetarProspeccao()"
    (concederXpClick)="onAbrirConcederXp()"
    (concederDadoClick)="onAbrirConcederDado()" />
}
```

Jogadores **nunca** veem o menu de acoes do Mestre, mesmo que a rota seja acessivel.

---

## 13. Estrutura de Arquivos

```
ficha/
  components/
    mestre-acoes-menu/
      mestre-acoes-menu.component.ts    [DUMB] — menu popup de acoes
      mestre-acoes-menu.component.html
    conceder-xp-dialog/
      conceder-xp-dialog.component.ts   [DUMB] — dialog de concessao de XP
      conceder-xp-dialog.component.html
```

---

## 14. API Contract

| Metodo | Endpoint | Payload | Resposta | Role |
|--------|----------|---------|----------|------|
| `POST` | `/api/v1/fichas/{id}/reset/vida` | — | `FichaResumoResponse` atualizado | MESTRE |
| `POST` | `/api/v1/fichas/{id}/reset/essencia` | — | `FichaResumoResponse` atualizado | MESTRE |
| `POST` | `/api/v1/fichas/{id}/reset/prospeccao` | — | — | MESTRE |
| `POST` | `/api/v1/fichas/{id}/xp` | `{ quantidade: number }` | `Ficha` atualizada (com novo nivel) | MESTRE |
| `POST` | `/api/v1/jogos/{id}/reset/vida-todos` | — | `{ fichasAfetadas: number }` | MESTRE |

> Nota: Todos esses endpoints podem nao existir ainda. Verificar com backend antes de implementar o service Angular.

---

## 15. Checklist de Implementacao

- [ ] `MestreAcoesMenuComponent` com `p-menu` popup, visivel apenas para Mestre
- [ ] Versao mobile: `p-drawer` position="bottom" com mesmos itens em lista
- [ ] `p-confirmDialog` customizado para reset de vida (icone verde, mensagem de impacto)
- [ ] `p-confirmDialog` customizado para reset de essencia (icone azul)
- [ ] `p-confirmDialog` customizado para reset de prospeccao (icone amarelo, aviso sobre estoque)
- [ ] Dialog de concessao de XP com `p-inputNumber` e texto de "proximo nivel em X XP"
- [ ] Toast de sucesso com mensagem especifica por tipo de reset
- [ ] Toast de level up quando XP concedido resulta em mudanca de nivel
- [ ] Recarregar resumo apos qualquer reset bem-sucedido
- [ ] Loading state no botao do menu durante request
- [ ] Endpoint `POST /fichas/{id}/xp` confirmado com backend
- [ ] Endpoints de reset (`/reset/vida`, `/reset/essencia`, `/reset/prospeccao`) confirmados
- [ ] Endpoint de reset em lote (`/jogos/{id}/reset/vida-todos`) para a tela de sessao
