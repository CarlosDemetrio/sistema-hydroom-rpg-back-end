# T15 — Frontend: Reativar `SidebarComponent` no `MainLayoutComponent`

> Fase: Frontend | Prioridade: P3 (POS-HOMOLOGACAO)
> Dependencias: decisao de produto PA-017-03
> Bloqueia: nenhuma
> Estimativa: 3h
> Agente sugerido: primeng-ux-architect → angular-frontend-dev

---

## Contexto

`SidebarComponent` existe em `shared/layout/sidebar.component.ts` com menu por role, mas nao esta importado em `main-layout.component.ts:14` (so ha `HeaderComponent` + `router-outlet`). Isso deixa o usuario sem navegacao global persistente.

Esta task e **opcional pos-RC** porque o `PageHeaderComponent` (P1) ja resolve o problema imediato de "nao consigo voltar das telas". Reativar sidebar e decisao de produto (PA-017-03):
- Coexistencia: sidebar + PageHeader (duas fontes de navegacao)
- Substituicao: sidebar substitui PageHeader (reverter T8-T10)
- Apenas mobile: sidebar drawer em telas pequenas, PageHeader em telas grandes

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `ficha-controlador-front-end/src/app/shared/layout/main-layout.component.ts` | Importar e renderizar `SidebarComponent` |
| `ficha-controlador-front-end/src/app/shared/layout/sidebar.component.ts` | Revisar e possivelmente atualizar rotas |

---

## Passos (alto nivel)

1. Resolver PA-017-03 com o PO
2. Adicionar `SidebarComponent` ao `imports` do `MainLayoutComponent`
3. Renderizar `<app-sidebar>` no template (layout flexbox side-by-side com `<router-outlet>`)
4. Revisar rotas do sidebar — podem estar desatualizadas
5. Testar colapso/expansao, acessibilidade, mobile
6. Se coexistir com `PageHeaderComponent`, avaliar redundancia visual

---

## Criterios de Aceite

- [ ] PA-017-03 resolvido
- [ ] Sidebar renderizado no layout principal
- [ ] Menu por role funcional (MESTRE ve itens de Mestre, JOGADOR ve itens de Jogador)
- [ ] Testes passam
- [ ] Mobile testado

---

## Referencias

- `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md` § EXTRA-01 (linha 206-214)
- PA-017-03
