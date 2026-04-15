# Spec 026 — Tasks por Prioridade

**Spec:** UX Gaps: Fluxo do Mestre e Perfil de Usuário
**Total de tasks:** 12
**Data:** 2026-04-15

---

## Prioridade P0 — Bloqueadores de Usabilidade

Nenhuma task P0 — nenhum gap bloqueia completamente o uso do sistema.

---

## Prioridade P1 — Alto Impacto

| ID | Título | Tipo | Escopo | Depende de |
|---|---|---|---|---|
| S026-T01 | Adicionar PageHeaderComponent (botão Voltar) nas 19 telas de config | FE | `features/mestre/pages/config/configs/**/` | — |
| S026-T02 | Tornar campo Nome editável no Perfil com PUT /api/v1/usuarios/me | FE | `pages/profile/profile.component.ts` | — |
| S026-T03 | Adicionar botão Excluir NPC na listagem com confirm dialog | FE | `features/mestre/pages/npcs/npcs.component.ts` | PA-026-05 |
| S026-T04 | Guard de troca de jogo com drawer aberto no ConfigLayout | FE | `features/mestre/pages/config/config-layout.component.ts` | PA-026-01 |
| S026-T05 | Badge de participantes pendentes na navegação | FE | `shared/layout/` + endpoint backend | PA-026-06 |

---

## Prioridade P2 — Médio Impacto

| ID | Título | Tipo | Escopo | Depende de |
|---|---|---|---|---|
| S026-T06 | Remover / ocultar botões Exportar e Importar até funcionalidade implementada | FE | `config-layout.component.ts` | PA-026-02 |
| S026-T07 | Implementar onMenuToggle com p-sidebar para mobile | FE | `shared/layout/header.component.ts` | PA-026-04 |
| S026-T08 | Adicionar data de criação ao JogoResumoResponse e exibir na lista | BE+FE | `JogoController`, `JogoResumoResponse`, `jogos-list.component.ts` | PA-026-03 |
| S026-T09 | Criar layout de área do Mestre com sidebar contextual (fora das configs) | FE | `features/mestre/` | Relacionado S026-T07 |

---

## Prioridade P3 — Qualidade e Limpeza

| ID | Título | Tipo | Escopo |
|---|---|---|---|
| S026-T10 | Remover console.log/error do ProfileComponent | FE | `pages/profile/profile.component.ts` |
| S026-T11 | Remover setTimeout de navegação no JogoFormComponent | FE | `features/mestre/pages/jogo-form/jogo-form.component.ts` |
| S026-T12 | Deletar dead code config.component.ts | FE | `features/mestre/pages/config/config.component.ts` |

---

## Notas de Implementação

### S026-T01 — Botão Voltar nas Configs (XS)
Adicionar `PageHeaderComponent` no topo de cada `*-config.component.ts` dentro de `configs/`. Já existe em `JogoDetailComponent` e `JogoFormComponent` como referência. Usar `backRoute="/mestre/config"` em todos. Total: 19 arquivos afetados. Trabalho mecânico, sem lógica nova.

### S026-T02 — Edição de Nome no Perfil (S)
Backend já tem `PUT /api/v1/usuarios/me` com `AtualizarUsuarioRequest { String nome }`. Frontend precisa: campo de input editável, botão Salvar, chamada HTTP, toast de sucesso, atualização do estado do `AuthService` após salvar. Remover os 3 `console.log/error` pode ser feito junto (S026-T10).

### S026-T03 — Excluir NPC (S)
Aguarda PA-026-05 (NPCs seguem soft delete como fichas ou podem ser deletados?). Se soft delete: backend retorna a ficha com status alterado. Se hard delete: verificar endpoint existente.

### S026-T04 — Guard de Troca de Jogo (M)
Exige estado compartilhado entre `ConfigLayoutComponent` e os 19 filhos. Estratégia sugerida: um signal `hasOpenDrawer` no nível do layout, passado como input ou via service. Aguarda PA-026-01.

### S026-T05 — Badge Participantes Pendentes (M)
Exige endpoint `GET /api/v1/jogos/pendentes/contagem` (ou equivalente) ou chamada ao endpoint existente de listagem de participantes. Aguarda PA-026-06.

### S026-T06 — Botões Exportar/Importar (XS)
Aguarda PA-026-02. Se não há roadmap, remover as linhas 63-80 do `config-layout.component.ts`. Se há roadmap, manter desabilitado com tooltip "Em breve".
