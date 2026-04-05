# T13 — UI de Renascimento no FichaDetail

> Spec: 012 | Fase: 4 | Tipo: Frontend | Prioridade: MEDIO
> Depende de: T12 (endpoint backend), T7 (FichaDetail atualizado)
> Bloqueia: nada

---

## Objetivo

Adicionar a UI de renascimento no FichaDetail: botão visível apenas para MESTRE quando o personagem está em um nível com `permitirRenascimento = true`, com modal de confirmação em duas etapas.

## Arquivos Afetados

- `src/app/features/jogador/pages/ficha-detail/components/ficha-header/ficha-header.component.ts` — ou `ficha-resumo-tab`
- `src/app/core/services/api/fichas-api.service.ts` — adicionar método `renascer()`

## Passos

### 1. Adicionar método `renascer()` em FichasApiService

```typescript
/**
 * POST /api/v1/fichas/{id}/renascer
 * Executa o renascimento do personagem (apenas MESTRE).
 */
renascer(fichaId: number): Observable<Ficha> {
  return this.http.post<Ficha>(
    `${this.baseUrl}/fichas/${fichaId}/renascer`,
    { confirmado: true }
  );
}
```

### 2. Computed para verificar disponibilidade de renascimento

No FichaDetailPage ou FichaHeaderComponent:

```typescript
protected podeRenascer = computed(() => {
  const ficha = this.ficha();
  const niveis = this.configStore.niveis();
  const nivelConfig = niveis.find(n => n.nivel === ficha.nivel);
  return nivelConfig?.permitirRenascimento ?? false;
});
```

### 3. Botão de Renascimento (visível apenas para MESTRE)

Localização: FichaHeaderComponent ou seção de ações no FichaResumoTab.

```html
@if (isMestre() && podeRenascer()) {
  <p-button
    label="Iniciar Renascimento"
    icon="pi pi-refresh"
    severity="warn"
    [outlined]="true"
    size="small"
    (onClick)="abrirConfirmacaoRenascimento()"
    pTooltip="O personagem está apto para renascer"
    tooltipPosition="bottom" />
}
```

### 4. Modal de confirmação em duas etapas

Etapa 1 — Aviso de consequências:

```typescript
protected abrirConfirmacaoRenascimento(): void {
  this.confirmationService.confirm({
    header: 'Renascimento de ' + this.ficha().nome,
    message: `
      Ao renascer, ${this.ficha().nome} voltará ao Nível 1.
      O XP será resetado.
      Vantagens compradas e bônus de renascimentos anteriores são mantidos.
      Esta ação NÃO PODE ser desfeita.
    `,
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Confirmar Renascimento',
    rejectLabel: 'Cancelar',
    acceptButtonStyleClass: 'p-button-danger',
    accept: () => this.confirmarRenascimento(),
  });
}
```

Etapa 2 — Executar:

```typescript
protected confirmarRenascimento(): void {
  this.renascendo.set(true);
  this.fichasApi.renascer(this.fichaId()).subscribe({
    next: (fichaAtualizada) => {
      this.renascendo.set(false);
      this.toastService.add({
        severity: 'success',
        summary: 'Renascimento realizado!',
        detail: `${fichaAtualizada.nome} renasceu. Renascimento #${fichaAtualizada.renascimentos} completado.`,
        life: 8000,
      });
      this.carregarFicha();  // recarregar ficha completa
    },
    error: (err) => {
      this.renascendo.set(false);
      // Tratar erro 400 "nível não permite renascimento" especificamente
    },
  });
}
```

### 5. Display do contador de renascimentos no header

Adicionar no FichaHeaderComponent um badge com o número de renascimentos, visível quando `ficha.renascimentos > 0`:

```html
@if (ficha().renascimentos > 0) {
  <p-badge
    [value]="ficha().renascimentos"
    severity="warn"
    pTooltip="{{ ficha().renascimentos }} renascimento(s) realizados"
    class="ml-2" />
}
```

## Critérios de Aceitação

- [ ] Método `renascer()` adicionado em `FichasApiService`
- [ ] Botão "Iniciar Renascimento" visível APENAS para MESTRE E APENAS quando `permitirRenascimento=true` no nível atual
- [ ] Botão NÃO visível para MESTRE quando nível não permite renascimento
- [ ] Botão NÃO visível para JOGADOR em nenhum caso
- [ ] Modal de confirmação com aviso de consequências e botão danger
- [ ] Após confirmar: ficha recarregada com nível e renascimentos atualizados
- [ ] Toast de sucesso com número do renascimento
- [ ] Badge de renascimentos no header quando contador > 0
- [ ] Erro HTTP 400 tratado com mensagem amigável

## Premissas

- T12 (endpoint backend) deve estar concluído antes desta task
- O `ConfigStore` já tem os NivelConfig carregados ao abrir o FichaDetail
- `isMestre()` está disponível no FichaDetailPage via AuthStore ou serviço de autenticação
