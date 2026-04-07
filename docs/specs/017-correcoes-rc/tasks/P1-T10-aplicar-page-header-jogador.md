# T10 — Frontend: Aplicar `PageHeaderComponent` em telas Jogador/wizard

> Fase: Frontend | Prioridade: P1 (DESEJAVEL PRE-RC)
> Dependencias: T8
> Bloqueia: nenhuma
> Estimativa: 2h
> Agente sugerido: angular-frontend-dev

---

## Contexto

Apos T8, esta task aplica o `PageHeaderComponent` em 4 telas que afetam o Jogador e o wizard de criacao de ficha:

1. `fichas-list.component.ts` — `/jogador/fichas` → voltar para `/dashboard`
2. `ficha-detail.component.ts` — `/jogador/fichas/:id` → voltar para `/jogador/fichas`
3. `ficha-wizard.component.ts` — `/jogador/fichas/nova` ou `/mestre/fichas/criar` → voltar para origem
4. `jogos-disponiveis.component.ts` — `/jogador/jogos` → voltar para `/dashboard`

**ATENCAO — coordenacao com Spec 012**: o `ficha-detail.component.ts` tambem e tocado pela Spec 012 fase 2 T11 (LevelUpDialog integration). Esta task deve rodar **APOS Spec 012 T11** para evitar conflito de merge.

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `features/jogador/pages/fichas-list/fichas-list.component.ts` | Adicionar `app-page-header` |
| `features/jogador/pages/ficha-detail/ficha-detail.component.ts` | Adicionar `app-page-header` (cuidado com FichaHeaderComponent) |
| `features/wizard/ficha-wizard.component.ts` (ou path real) | Adicionar `app-page-header` |
| `features/jogador/pages/jogos-disponiveis/jogos-disponiveis.component.ts` | Adicionar `app-page-header` |

---

## Passos Sugeridos

### Passo 1 — `fichas-list.component.ts`

```html
<app-page-header
  titulo="Meus Personagens"
  subtitulo="Lista de fichas que voce criou ou esta jogando"
  backRoute="/dashboard"
  backLabel="Voltar ao Dashboard"
>
  <div actions>
    <!-- botao "Nova ficha" existente -->
  </div>
</app-page-header>
```

### Passo 2 — `ficha-detail.component.ts` — cuidado especial

O `ficha-detail` ja tem um `FichaHeaderComponent` interno (com botoes Editar/Duplicar/Deletar). NAO substituir o `FichaHeaderComponent`. Adicionar `app-page-header` ACIMA dele:

```html
<app-page-header
  [titulo]="ficha()?.nome ?? 'Ficha'"
  backRoute="/jogador/fichas"
  backLabel="Minhas Fichas"
/>

<app-ficha-header [ficha]="ficha()" />
<!-- abas e conteudo continuam como estao -->
```

Decisao alternativa: incorporar o botao Voltar dentro do proprio `FichaHeaderComponent` em vez de duplicar header. Recomendacao: NAO fazer isso na T10 — manter os dois separados (PageHeader e FichaHeader) para nao introduzir refactor maior. Pode ser unificado em iteracao futura.

### Passo 3 — `ficha-wizard.component.ts`

```html
<app-page-header
  titulo="Criar Personagem"
  subtitulo="Siga os passos para criar sua ficha"
  [backRoute]="rotaVoltar()"
  backLabel="Cancelar"
/>
```

Onde `rotaVoltar()` e um `computed` ou metodo que retorna:
- `/jogador/fichas` se o usuario e Jogador
- `/mestre/fichas` (ou `/mestre/jogos/:id`) se e Mestre

Adicionar tambem confirmacao via `ConfirmationService` se o wizard tem rascunho nao salvo:
```typescript
sairWizard(): void {
  if (this.temAlteracoesNaoSalvas()) {
    this.confirmationService.confirm({
      message: 'Voce tem alteracoes nao salvas. Deseja realmente sair?',
      accept: () => this.router.navigate([this.rotaVoltar()])
    });
  } else {
    this.router.navigate([this.rotaVoltar()]);
  }
}
```

Mas a confirmacao pode ser deixada para uma task futura. Para esta task, basta o botao Voltar simples.

### Passo 4 — `jogos-disponiveis.component.ts`

```html
<app-page-header
  titulo="Jogos Disponiveis"
  subtitulo="Solicite participacao em mesas abertas"
  backRoute="/dashboard"
  backLabel="Voltar ao Dashboard"
/>
```

### Passo 5 — Validacao

```
cd ficha-controlador-front-end
npx ng build --configuration development
npx vitest run src/app/features/jogador/ src/app/features/wizard/
```

### Passo 6 — Teste manual

Para cada tela: navegar, clicar Voltar, verificar destino correto.

---

## Criterios de Aceite

- [ ] 4 telas Jogador/wizard tem `<app-page-header>` no topo
- [ ] `ficha-detail` mantem `FichaHeaderComponent` separado (NAO unificar)
- [ ] `ficha-wizard` tem `backRoute` dinamico baseado na role do usuario
- [ ] Build passa
- [ ] Testes passam
- [ ] **Coordenacao**: confirmar que Spec 012 T11 (`ficha-detail.component.ts`) ja foi mergeada antes de rodar esta task

---

## Notas

- A coordenacao com Spec 012 T11 e CRITICA. Se ambas as tasks rodarem em paralelo, vai haver conflito de merge no `ficha-detail.component.ts`. PM deve sequenciar.
- O `jogos-disponiveis.component.ts` foi marcado pelo auditor como "a verificar" — confirmar que e a tela correta antes de aplicar.
- NAO adicionar logica de confirmacao no wizard nesta task (escopo creep). Fica para iteracao futura.

---

## Referencias

- `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md` § P1 — tabela de telas (linha 22-32)
- T8 (componente criado)
- Spec 012 fase 2 T11 (precisa ter rodado antes)
