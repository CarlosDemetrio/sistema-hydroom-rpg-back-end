# T9 — Frontend: Aplicar `PageHeaderComponent` em telas Mestre

> Fase: Frontend | Prioridade: P1 (DESEJAVEL PRE-RC)
> Dependencias: T8
> Bloqueia: nenhuma
> Estimativa: 2h
> Agente sugerido: angular-frontend-dev

---

## Contexto

Apos T8 criar o `PageHeaderComponent`, esta task aplica em 4 telas do Mestre que nao tem botao Voltar:

1. `config-layout.component.ts` — `/mestre/config/*` → voltar para `/dashboard`
2. `jogo-detail.component.ts` — `/mestre/jogos/:id` → voltar para `/mestre/jogos`
3. `jogo-form.component.ts` — `/mestre/jogos/novo`, `:id/edit` → voltar para `/mestre/jogos`
4. `npcs.component.ts` — `/mestre/npcs` → voltar para `/dashboard`

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `features/mestre/pages/config/config-layout.component.ts` | Adicionar `app-page-header` no template |
| `features/mestre/pages/jogo-detail/jogo-detail.component.ts` | Adicionar `app-page-header` |
| `features/mestre/pages/jogo-form/jogo-form.component.ts` | Adicionar `app-page-header` (substituir botao Cancelar atual) |
| `features/mestre/pages/npcs/npcs.component.ts` | Adicionar `app-page-header` |

---

## Passos Sugeridos

### Passo 1 — `config-layout.component.ts`

1. Importar `PageHeaderComponent`:
   ```typescript
   import { PageHeaderComponent } from '@shared/components/page-header/page-header.component';
   ```
2. Adicionar ao `imports: [...]` do `@Component`
3. Substituir o `<h1>` atual no topo do template por:
   ```html
   <app-page-header
     titulo="Configuracoes do Sistema"
     subtitulo="Configure as regras e mecanicas do seu jogo"
     backRoute="/dashboard"
     backLabel="Voltar ao Dashboard"
   >
     <div actions>
       <!-- botoes Exportar/Importar atuais (mesmo que decorativos) -->
     </div>
   </app-page-header>
   ```

### Passo 2 — `jogo-detail.component.ts`

```html
<app-page-header
  [titulo]="jogo()?.nome ?? 'Jogo'"
  [subtitulo]="jogo()?.descricao ?? ''"
  backRoute="/mestre/jogos"
  backLabel="Meus Jogos"
>
  <div actions>
    <!-- botoes Editar / Excluir existentes -->
  </div>
</app-page-header>
```

### Passo 3 — `jogo-form.component.ts`

```html
<app-page-header
  [titulo]="ehEdicao() ? 'Editar Jogo' : 'Novo Jogo'"
  backRoute="/mestre/jogos"
  backLabel="Cancelar"
/>
```

Importante: o botao "Cancelar" atual do form provavelmente fica REDUNDANTE. Decidir entre:
- Remover o botao Cancelar do form e usar so o do header
- Manter ambos (Cancelar inferior e Voltar superior)

Recomendacao: remover o Cancelar inferior — fica mais limpo.

### Passo 4 — `npcs.component.ts`

```html
<app-page-header
  titulo="NPCs"
  subtitulo="Personagens nao jogadores controlados pelo Mestre"
  backRoute="/dashboard"
  backLabel="Voltar ao Dashboard"
>
  <div actions>
    <!-- botao "Novo NPC" existente -->
  </div>
</app-page-header>
```

### Passo 5 — Validacao

```
cd ficha-controlador-front-end
npx ng build --configuration development
npx vitest run src/app/features/mestre/
```

Atualizar testes que renderizam essas telas (provavelmente algumas verificam estrutura do template).

### Passo 6 — Teste manual

Para cada tela:
1. Navegar para a tela
2. Clicar no botao Voltar
3. Verificar que volta para a rota esperada

---

## Criterios de Aceite

- [ ] 4 telas Mestre tem `<app-page-header>` no topo
- [ ] Cada tela tem `backRoute` correto (config-layout → `/dashboard`, jogo-detail → `/mestre/jogos`, jogo-form → `/mestre/jogos`, npcs → `/dashboard`)
- [ ] Botoes de acao existentes preservados via slot `[actions]`
- [ ] `jogo-form` Cancelar inferior decidido (recomendacao: remover)
- [ ] Build passa
- [ ] Testes passam
- [ ] Validacao manual em cada tela

---

## Notas

- NAO mexer no `header.component.ts` global do app — `PageHeaderComponent` e LOCAL por tela
- Verificar import path correto (`@shared/...` ou caminho relativo)
- Se a tela ja tem um `<h1>`, substituir por `app-page-header` (nao deixar ambos)

---

## Referencias

- `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md` § P1 — tabela de telas (linha 22-32)
- T8 (componente criado)
