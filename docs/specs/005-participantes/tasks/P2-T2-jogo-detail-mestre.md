# P2-T2 — Corrigir JogoDetail do Mestre

> Fase: 2 — Frontend
> Complexidade: 🟡 media
> Depende de: P2-T1
> Bloqueia: nada

---

## Objetivo

Corrigir e completar a aba "Participantes" do `JogoDetailComponent` para:
1. Separar as acoes de "Remover" (provisorio) e "Banir" (definitivo) que hoje estao confundidas
2. Adicionar acao "Desbanir" para participantes BANIDO
3. Adicionar filtro por status
4. Adicionar badge de contagem de solicitacoes PENDENTES na tab
5. Ajustar modelo de dados que usa `participante.jogador?.name` (modelo errado)

---

## Contexto — Arquivos a Ler Antes de Comecar

- `features/mestre/pages/jogo-detail/jogo-detail.component.ts` — componente atual completo
- `core/services/business/participante-business.service.ts` — apos P2-T1
- `core/models/participante.model.ts` — campos corretos (`nomeUsuario`, nao `jogador.name`)

---

## Diagnostico de Bugs no Componente Atual

### Bug 1: Campo de dados errado

```typescript
// ATUAL — ERRADO
participante.jogador?.name
participante.dataParticipacao

// CORRETO — campos do modelo ParticipanteResponse
participante.nomeUsuario
participante.dataCriacao
```

### Bug 2: Botao "Remover" chama `banirParticipante`

```typescript
// ATUAL — ERRADO
removerParticipante(participanteId: number) {
  this.participanteService.banirParticipante(...) // semantica errada
}
```

### Bug 3: Sem opcao de Banir nem Desbanir separados

A UI atual tem apenas "Aprovar", "Rejeitar" e "Remover". Nao existe botao separado para "Banir" (definitivo) nem "Desbanir".

### Bug 4: Sem filtro por status

Nao ha como o Mestre ver apenas PENDENTES para aprovar/rejeitar com eficiencia.

---

## Passos de Implementacao

### Passo 1: Corrigir campos no template

No template da tabela de participantes, substituir:
- `participante.jogador?.name` por `participante.nomeUsuario`
- `participante.dataParticipacao` por `participante.dataCriacao | date:'dd/MM/yyyy'`
- `participante.ficha` (campo inexistente no modelo) — remover ou marcar como TODO

### Passo 2: Adicionar signal de filtroStatus e computed filtrado

```typescript
filtroStatus = signal<StatusParticipante | null>(null);

participantesFiltrados = computed(() => {
  const todos = this.participantes();
  const filtro = this.filtroStatus();
  if (!filtro) return todos;
  return todos.filter(p => p.status === filtro);
});
```

No `ngOnInit`, ao carregar participantes passar o filtro reativo. Ou simplesmente filtrar client-side ja que a lista e pequena.

**Recomendacao:** Filtrar client-side nos participantes ja carregados. Evita chamada extra ao backend para cada mudanca de filtro.

### Passo 3: Adicionar badge de PENDENTES na tab

```html
<p-tab value="1">
  Participantes
  @if (participantesPendentes().length > 0) {
    <p-badge
      [value]="participantesPendentes().length.toString()"
      severity="warn"
      class="ml-2"
    />
  }
</p-tab>
```

### Passo 4: Adicionar SelectButton de filtro no cabecalho da aba

```html
<!-- Acima da tabela, dentro do p-tabpanel -->
<div class="flex justify-content-between align-items-center mb-3">
  <span class="font-semibold text-lg">
    {{ participantesFiltrados().length }} participantes
  </span>
  <p-selectbutton
    [options]="filtroOptions"
    [(ngModel)]="filtroStatusModel"
    optionLabel="label"
    optionValue="value"
  />
</div>
```

```typescript
filtroOptions = [
  { label: 'Todos', value: null },
  { label: 'Pendentes', value: 'PENDENTE' },
  { label: 'Aprovados', value: 'APROVADO' },
  { label: 'Rejeitados', value: 'REJEITADO' },
  { label: 'Banidos', value: 'BANIDO' },
];

// Usar model() ou signal() com efeito para filtroStatus
filtroStatusModel = signal<StatusParticipante | null>(null);
```

**Nota:** SelectButton do PrimeNG requer `[(ngModel)]` ou input/output. Prefira signal + effect em vez de ngModel para manter padrao do projeto.

### Passo 5: Corrigir e expandir acoes por status

A logica de botoes de acao deve depender do status:

| Status | Acoes visiveis |
|--------|---------------|
| PENDENTE | Aprovar, Rejeitar |
| APROVADO | Remover (provisorio), Banir (definitivo) |
| REJEITADO | (nenhuma acao imediata — jogador pode re-solicitar) |
| BANIDO | Desbanir |

```html
<td class="text-center">
  <div class="flex gap-2 justify-content-center">
    <!-- PENDENTE: aprovar ou rejeitar -->
    @if (participante.status === 'PENDENTE') {
      <p-button icon="pi pi-check" rounded text severity="success"
        pTooltip="Aprovar" (onClick)="aprovarParticipante(participante.id)" />
      <p-button icon="pi pi-times" rounded text severity="danger"
        pTooltip="Rejeitar" (onClick)="rejeitarParticipante(participante.id)" />
    }

    <!-- APROVADO: remover (provisorio) ou banir (definitivo) -->
    @if (participante.status === 'APROVADO') {
      <p-button icon="pi pi-user-minus" rounded text severity="warn"
        pTooltip="Remover (pode re-solicitar)"
        (onClick)="removerParticipante(participante.id)" />
      <p-button icon="pi pi-ban" rounded text severity="danger"
        pTooltip="Banir (impede re-solicitacao)"
        (onClick)="banirParticipante(participante.id)" />
    }

    <!-- BANIDO: desbanir -->
    @if (participante.status === 'BANIDO') {
      <p-button icon="pi pi-user-plus" rounded text severity="success"
        pTooltip="Desbanir (reintegrar como aprovado)"
        (onClick)="desbanirParticipante(participante.id)" />
    }
  </div>
</td>
```

### Passo 6: Corrigir metodos do componente

```typescript
removerParticipante(participanteId: number): void {
  this.confirmationService.confirm({
    message: 'Remover participante? Ele podera re-solicitar entrada.',
    header: 'Remocao Provisoria',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Remover',
    rejectLabel: 'Cancelar',
    accept: () => {
      this.participanteService.removerParticipante(this.jogoId()!, participanteId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Sucesso',
              detail: 'Participante removido. Ele pode re-solicitar entrada.' });
            this.participanteService.loadParticipantes(this.jogoId()!).pipe(
              takeUntilDestroyed(this.destroyRef)).subscribe();
          },
          error: () => this.messageService.add({ severity: 'error',
            summary: 'Erro', detail: 'Erro ao remover participante' })
        });
    }
  });
}

banirParticipante(participanteId: number): void {
  this.confirmationService.confirm({
    message: 'Banir participante? Ele NAO podera re-solicitar enquanto banido.',
    header: 'Confirmar Banimento',
    icon: 'pi pi-ban',
    acceptLabel: 'Banir',
    rejectLabel: 'Cancelar',
    acceptButtonStyleClass: 'p-button-danger',
    accept: () => {
      this.participanteService.banirParticipante(this.jogoId()!, participanteId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => this.messageService.add({ severity: 'success',
            summary: 'Sucesso', detail: 'Participante banido.' }),
          error: () => this.messageService.add({ severity: 'error',
            summary: 'Erro', detail: 'Erro ao banir participante' })
        });
    }
  });
}

desbanirParticipante(participanteId: number): void {
  this.participanteService.desbanirParticipante(this.jogoId()!, participanteId)
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe({
      next: () => this.messageService.add({ severity: 'success',
        summary: 'Sucesso', detail: 'Participante reintegrado como aprovado.' }),
      error: () => this.messageService.add({ severity: 'error',
        summary: 'Erro', detail: 'Erro ao desbanir participante' })
    });
}
```

### Passo 7: Adicionar importacoes PrimeNG necessarias

Verificar se `SelectButtonModule`, `BadgeModule` estao nos `imports` do componente. Adicionar se necessario.

---

## Testes a Adicionar

Criar `jogo-detail.component.spec.ts` (se nao existir) com:

```typescript
describe('JogoDetailComponent — Participantes', () => {
  it('deve exibir nome correto do participante (nomeUsuario)', ...);
  it('deve exibir botoes Aprovar/Rejeitar para PENDENTE', ...);
  it('deve exibir botoes Remover/Banir para APROVADO', ...);
  it('deve exibir botao Desbanir para BANIDO', ...);
  it('deve filtrar por status PENDENTE ao selecionar filtro', ...);
  it('deve exibir badge com contagem de pendentes na tab', ...);
});
```

---

## Criterios de Aceitacao

- [ ] `participante.nomeUsuario` exibido corretamente (nao mais `jogador?.name`)
- [ ] `dataCriacao` exibida no formato `dd/MM/yyyy`
- [ ] Participante PENDENTE: apenas botoes Aprovar e Rejeitar
- [ ] Participante APROVADO: botoes Remover (provisorio) e Banir (definitivo), com confirmacao
- [ ] Participante BANIDO: botao Desbanir
- [ ] Confirmacao de banimento com aviso claro que impede re-solicitacao
- [ ] Filtro por status funcional (client-side)
- [ ] Badge de pendentes visivel na tab quando count > 0
- [ ] Build sem erros e testes passando
