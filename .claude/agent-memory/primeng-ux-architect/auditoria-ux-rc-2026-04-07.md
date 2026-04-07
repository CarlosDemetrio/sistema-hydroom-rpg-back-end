---
name: Auditoria UX/UI para RC (2026-04-07)
description: Gaps de navegação, estado dos drawers, GameDefault e achados extras auditados antes da RC
type: project
---

Auditoria UX/UI realizada antes da RC do projeto. Resultado consolidado em `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md`.

**Achados principais:**

1. Botão "Voltar ao Dashboard" ausente em: config-layout, jogo-detail, jogo-form, npcs, ficha-wizard, fichas-list, ficha-detail. Recomendação: criar `PageHeaderComponent` com `backRoute` input.

2. Migração drawer→dialog CONCLUÍDA. Nenhum componente de produção usa DrawerModule. Apenas nomenclatura `drawerVisible` (signal name) e `describe('drawer...')` (tests) sobrevivem como dívida nominal.

3. Não existe componente "GameDefaultConfig" no frontend. O PO se refere aos dados iniciais do `DefaultGameConfigProviderImpl.java` — bugs estão no backend (BUG-DC-06..08), já documentados em `docs/analises/DEFAULT-CONFIG-AUDITORIA.md`. Spec 015 cobre alguns.

4. `SidebarComponent` existe mas NÃO está no `MainLayoutComponent` — o layout é apenas header + router-outlet, sem sidebar ativa.

5. Seletor de jogo no header não existe — todos os warnings "selecione um jogo no cabeçalho" ficam sem resposta visual.

6. Botões Exportar/Importar no config-layout não têm handler (onClick).

7. `hasBothRoles()` no header usa `||` em vez de `&&` — bug lógico, todo usuário vê o switcher Mestre/Jogador.

8. `jogo-detail.verFicha()` navega para `/jogador/fichas/:id` protegida por role JOGADOR — Mestre recebe 403.

9. `jogo-form.component.ts` usa MessageService direto sem `<p-toast>` no template — toasts silenciosos.

**Why:** Auditoria pré-RC solicitada pelo PO após relatar problemas de navegação e bug no GameDefault.

**How to apply:** Spec corretiva para RC deve priorizar T1 (PageHeader + Voltar), T3 (hasBothRoles fix), T4 (seletor de jogo) e T5 (verFicha Mestre).
