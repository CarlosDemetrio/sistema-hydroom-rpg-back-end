# T16 — Frontend: Seletor de jogo no `HeaderComponent`

> Fase: Frontend | Prioridade: P3 (POS-HOMOLOGACAO)
> Dependencias: nenhuma
> Bloqueia: nenhuma
> Estimativa: 4h
> Agente sugerido: primeng-ux-architect → angular-frontend-dev

---

## Contexto

Multiplos componentes (configs, NPCs, ficha-wizard) mostram o aviso "Selecione um jogo no cabecalho..." mas o `HeaderComponent` NAO tem dropdown de selecao de jogo. Os avisos ficam sem resposta visual.

O `CurrentGameService` existe e provavelmente tem `availableGames` e `setCurrentGame()`. A task e integrar um `p-select` ou `p-menu` no `HeaderComponent` conectado a esse service, com persistencia em localStorage.

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `ficha-controlador-front-end/src/app/shared/components/header/header.component.ts` | Adicionar seletor de jogo |
| `ficha-controlador-front-end/src/app/services/current-game.service.ts` | Confirmar API; adicionar persistencia se faltar |

---

## Passos (alto nivel)

1. Confirmar API do `CurrentGameService`
2. Adicionar `<p-select [options]="jogos()" [ngModel]="jogoAtual()" ...>` no header
3. Conectar ao service com `signal()` + `effect()` para persistencia
4. Atualizar componentes que mostram o aviso "Selecione um jogo" para remover o texto ou transformar em link para abrir o select
5. Testes de integracao do seletor
6. Validacao manual: trocar jogo e verificar que todas as configs respeitam a escolha

---

## Criterios de Aceite

- [ ] Seletor de jogo no header funciona
- [ ] Selecao persiste entre refreshes (localStorage)
- [ ] Avisos "Selecione um jogo" atualizam em tempo real apos selecao
- [ ] Testes do `header.component.spec.ts` cobrem o novo seletor

---

## Notas

- Esta e uma task MAIOR (~4h) — considerar virar spec propria se precisar UX customizado
- Interage com muitos componentes — coordenar com PM para evitar conflitos

---

## Referencias

- `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md` § EXTRA-09 (linha 275-280)
