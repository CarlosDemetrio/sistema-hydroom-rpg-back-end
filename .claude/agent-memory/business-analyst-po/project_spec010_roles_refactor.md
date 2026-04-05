---
name: Spec 010 — Roles ADMIN / MESTRE / JOGADOR
description: Decisoes de produto e regras criticas da spec 010 de refatoracao de roles
type: project
---

Spec 010 criada em 2026-04-02. Adiciona role ADMIN como super-admin, preservando MESTRE e JOGADOR.

**Por:** Decisao do PO — "perfil admin com acesso a tudo, mestre com acesso a jogos que criou, jogador com acesso a jogos que participa".

**Estrutura de arquivos:**
- `docs/specs/010-roles-refactor/spec.md` — spec completo com US, BDD e dossie de regras
- `docs/specs/010-roles-refactor/plan.md` — estrategia de migracao zero-downtime
- `docs/specs/010-roles-refactor/tasks/INDEX.md` — 9 tasks indexadas
- Tasks T1-T9 criadas com arquivos individuais

**Regras criticas descobertas:**

1. Role global (Usuario.role) e diferente da role no jogo (JogoParticipante.roleJogo). Um JOGADOR pode ser Mestre de um jogo especifico via JogoParticipante.

2. Usuario novo recebe role = null apos OAuth2. Deve ser redirecionado para onboarding antes de acessar qualquer endpoint de negocio.

3. ADMIN nao pode autodefinir sua role — apenas via seed SQL ou promocao por outro ADMIN.

4. Prevencao de lock-out: nao pode revogar o ultimo ADMIN do sistema (validacao por count).

5. ParticipanteSecurityService.isMestreDoJogo() opera sobre RoleJogo e NAO deve ser alterado por esta spec.

**Ponto em aberto critico (P-03):** ADMIN bypassa validacao de participante em canAccessJogo()? Nao decidido pelo PO. Enquanto nao definido, implementar bypass com metodo isAdmin() no ParticipanteSecurityService.

**Padrao de @PreAuthorize apos esta spec:**
- `hasRole('MESTRE')` → `hasAnyRole('ADMIN', 'MESTRE')`
- `hasAnyRole('MESTRE', 'JOGADOR')` → `hasAnyRole('ADMIN', 'MESTRE', 'JOGADOR')`
- ~80 ocorrencias em 25 controllers

**Estado atual do campo role em Usuario.java:**
- Campo e String (nao enum), @Builder.Default = "JOGADOR"
- T1 remove o default para permitir null no onboarding
- Nenhuma migracao de schema necessaria (VARCHAR(20) ja suporta "ADMIN")

**How to apply:** Ao escrever novos endpoints ou specs, sempre usar o novo padrao com ADMIN incluido. Perguntar ao PO sobre P-03 antes de implementar T2/T3.
