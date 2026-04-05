# T13 — Badge "Incompleta" na Listagem de Fichas

> Fase: Frontend
> Complexidade: Baixa
> Prerequisito: T1 (campo `status` na FichaResponse)
> Bloqueia: Nenhum
> Estimativa: 1–2 horas

---

## Objetivo

Exibir um badge visual "Incompleta" nas fichas com status `RASCUNHO` na tela de listagem de fichas (`FichasListPage`). O badge deve ser clicavel para retomar o wizard de criacao de onde parou.

---

## Contexto

Apos T1, o campo `status` esta disponivel na `FichaResponse`. Esta task e simples: consiste em ler esse campo e exibir um badge condicional na listagem. A principal decisao de UX e que o badge deve ser um link que retoma o wizard.

**Decisao:** Badge "Incompleta" abre o wizard na rota `/jogos/{jogoId}/fichas/criar?fichaId={id}`, que retoma o rascunho de onde parou (logica de retomada implementada em T6).

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `fichas-list/fichas-list.page.ts` | Verificar se exibe campo status |
| `fichas-list/fichas-list.page.html` | Adicionar badge condicional |
| `fichas-list/components/ficha-card/ficha-card.component.html` | Adicionar badge no card (se existir componente de card) |

---

## Wireframe do Badge

### Na listagem (view de tabela ou cards)

```
┌───────────────────────────────────────────────────────────────┐
│ Aldric, o Guardiao    [RASCUNHO]    Nivel 1  Guerreiro        │
│                        ↑ badge laranja clicavel               │
│                                                               │
│ Thorin Escudo de Ferro               Nivel 5  Paladino       │
│ (ficha COMPLETA — sem badge)                                  │
└───────────────────────────────────────────────────────────────┘
```

---

## Implementacao

### Badge condicional no template da listagem

```html
<!-- Dentro do card ou linha da ficha -->
@if (ficha.status === 'RASCUNHO') {
  <p-tag
    value="Incompleta"
    severity="warn"
    [rounded]="true"
    styleClass="cursor-pointer"
    [pTooltip]="'Clique para continuar criando este personagem'"
    tooltipPosition="top"
    (click)="retomar(ficha)"
    role="button"
    aria-label="Continuar criando {{ ficha.nome }}" />
}
```

### Metodo `retomar()` no componente

```typescript
retomar(ficha: FichaResponse): void {
  this.router.navigate(
    ['/jogos', ficha.jogoId, 'fichas', 'criar'],
    { queryParams: { fichaId: ficha.id } }
  );
}
```

---

## Tipo `FichaResponse` no Frontend

Verificar se o model TypeScript `Ficha` (ou `FichaResponse`) ja inclui o campo `status`. Se nao, adicionar:

```typescript
// Em ficha.models.ts ou similar
export interface FichaResponse {
  id: number;
  jogoId: number;
  nome: string;
  // ... campos existentes ...
  status: 'RASCUNHO' | 'COMPLETA';
}
```

---

## Testes Obrigatorios

| Cenario | Descricao |
|---------|-----------|
| Badge visivel | Ficha com status='RASCUNHO' exibe badge "Incompleta" |
| Badge ausente | Ficha com status='COMPLETA' nao exibe badge |
| Click retoma wizard | Clicar no badge navega para o wizard com fichaId na query |
| Tooltip | Hover exibe tooltip "Clique para continuar criando este personagem" |
| Multiplas fichas | Apenas fichas RASCUNHO tem badge (nao contamina outras) |

---

## Criterios de Aceitacao

- [ ] Badge `p-tag` com severity="warn" visivel apenas em fichas com status='RASCUNHO'
- [ ] Badge nao aparece em fichas com status='COMPLETA'
- [ ] Clicar no badge navega para o wizard na rota correta com `?fichaId=`
- [ ] Tooltip no badge com instrucao clara
- [ ] Model TypeScript `FichaResponse` inclui campo `status`
- [ ] Testes cobrindo exibicao condicional e navegacao

---

## Observacoes

- Esta task e intencionalemnte pequena — a complexidade esta em T1 (backend) e T6 (retomada do wizard), que sao os pre-requisitos.
- O badge deve ser visivel apenas para o dono da ficha e para o Mestre. O backend ja garante isso — o endpoint `/fichas` so retorna fichas acessiveis ao usuario logado. Nao ha filtro adicional necessario no frontend.
- Se a `FichasListPage` nao existir ainda (apenas `FichaFormComponent` existe), esta task precisa que a listagem basica seja implementada antes. Verificar estado atual do componente de listagem.
