# Spec 005 — Plano de Implementacao: Gestao de Participantes

> Branch alvo: `feature/005-participantes`
> Data: 2026-04-02
> Base de testes antes de iniciar: 457 testes passando (branch `feature/009-npc-fichas-mestre`)

---

## 1. Estado Atual do Codigo (auditado em 2026-04-02)

### 1.1 O que JA EXISTE e esta alinhado com a Spec

| Artefato | Arquivo | Status |
|----------|---------|--------|
| Entity `JogoParticipante` | `model/JogoParticipante.java` | OK — tem `status`, `role`, `BaseEntity` (soft delete) |
| Enum `StatusParticipante` | `model/enums/StatusParticipante.java` | OK — PENDENTE, APROVADO, REJEITADO, BANIDO |
| Enum `RoleJogo` | `model/enums/RoleJogo.java` | OK — MESTRE, JOGADOR |
| Repository | `repository/JogoParticipanteRepository.java` | OK — queries existem mas faltam algumas |
| Service | `service/JogoParticipanteService.java` | PARCIAL — falta desbanir, remover (soft delete), meu-status |
| Controller | `controller/JogoParticipanteController.java` | PARCIAL — falta desbanir, meu-status, cancelar-solicitacao |
| Mapper | `mapper/JogoParticipanteMapper.java` | OK — mapeia corretamente |
| Response DTO | `dto/response/ParticipanteResponse.java` | OK — alinhado com spec |
| Security service | `service/ParticipanteSecurityService.java` | OK — canAccessJogo, assertMestreDoJogo |
| Testes de integracao | `service/JogoParticipanteServiceIntegrationTest.java` | PARCIAL — cobre solicitar/aprovar/rejeitar/banir/listar basico |

### 1.2 O que DIVERGE da Spec (gaps a corrigir)

| Gap | Descricao | Impacto |
|-----|-----------|---------|
| G-01 | `DELETE /{pid}` implementado como BANIR (status=BANIDO). A spec define DELETE como REMOVER (soft delete). BANIR deve ser `PUT /{pid}/banir`. | CRITICO — semantica errada |
| G-02 | Endpoint `PUT /{pid}/desbanir` nao existe no controller nem no service | ALTO — fluxo incompleto |
| G-03 | Endpoint `GET /meu-status` nao existe | ALTO — jogador nao consegue verificar proprio status |
| G-04 | Endpoint `DELETE /minha-solicitacao` nao existe | MEDIO — jogador nao pode cancelar propria solicitacao |
| G-05 | Filtro por status na listagem nao existe (`?status=PENDENTE`) | MEDIO — mestre nao consegue filtrar |
| G-06 | `solicitar()` usa `existsByJogoIdAndUsuarioId` que bloqueia re-solicitacao mesmo apos REJEITADO/REMOVIDO | CRITICO — impede re-solicitacao |
| G-07 | unique constraint `uk_jogo_usuario` impede re-solicitacao (segundo registro apos soft delete) | CRITICO — constraint no banco |
| G-08 | Logica de banimento (`solicitar()`) nao checa especificamente status BANIDO | ALTO — jogador banido consegue solicitar |
| G-09 | Frontend usa `banirParticipante` para o botao "Remover" | MEDIO — confusao semantica (mas vai ser corrigido com G-01) |
| G-10 | Frontend nao tem endpoint para `desbanir`, `meu-status`, `cancelar-solicitacao`, filtro | MEDIO — funcionalidades ausentes |
| G-11 | Tela de Jogador para solicitar entrada em jogos publicos/disponiveis nao esta conectada | ALTO — fluxo do Jogador incompleto |

### 1.3 O que EXISTE no Frontend

| Artefato | Arquivo | Status |
|----------|---------|--------|
| Model `Participante` | `core/models/participante.model.ts` | OK — alinhado com backend |
| API service (parcial) | `core/services/api/jogos-api.service.ts` | PARCIAL — falta desbanir, meuStatus, cancelarSolicitacao |
| Business service | `core/services/business/participante-business.service.ts` | PARCIAL — falta desbanir, meuStatus, cancelar |
| JogoDetail (Mestre) | `features/mestre/pages/jogo-detail/jogo-detail.component.ts` | PARCIAL — tem aprovar/rejeitar/remover (chama banir errado) |
| JogosDisponiveis (Jogador) | `features/jogador/pages/jogos-disponiveis/jogos-disponiveis.component.ts` | INCOMPLETO — nao tem botao solicitar, sem status de participacao |
| JogosStore | `core/stores/jogos.store.ts` | OK — tem setParticipantes, updateParticipanteInState |

---

## 2. O que Precisa Ser Criado ou Modificado

### Backend

#### Modificacoes em arquivos existentes

| Arquivo | O que modificar |
|---------|----------------|
| `JogoParticipanteController.java` | Renomear `DELETE /{pid}` para `PUT /{pid}/banir`; adicionar `DELETE /{pid}` (remover/soft delete); adicionar `PUT /{pid}/desbanir`; adicionar `GET /meu-status`; adicionar `DELETE /minha-solicitacao`; adicionar `?status` no GET |
| `JogoParticipanteService.java` | Corrigir `banir()` (status=BANIDO nao soft delete); criar `remover()`; criar `desbanir()`; criar `meuStatus()`; criar `cancelarSolicitacao()`; corrigir `solicitar()` (checar BANIDO ativo, permitir re-solicitar apos REJEITADO/REMOVIDO com novo registro) |
| `JogoParticipanteRepository.java` | Adicionar `findByJogoIdAndUsuarioIdIncluindoBanidos()` para checar BANIDO mesmo em soft-deleted; adicionar `findByJogoIdAndStatusOptional()` para filtro |
| `JogoParticipanteServiceIntegrationTest.java` | Adicionar testes para desbanir, remover (soft delete), meu-status, re-solicitacao apos rejeicao, bloqueio de banido |

#### Artefatos novos no backend

Nenhum novo arquivo de entidade, DTO ou mapper e necessario. Tudo ja existe — apenas metodos e endpoints adicionais.

### Frontend

#### Modificacoes em arquivos existentes

| Arquivo | O que modificar |
|---------|----------------|
| `jogos-api.service.ts` | Adicionar `desbanirParticipante()`, `meuStatusParticipacao()`, `cancelarSolicitacao()`, `listarParticipantes(jogoId, status?)` com filtro |
| `participante-business.service.ts` | Adicionar `desbanirParticipante()`, `meuStatus()`, `cancelarSolicitacao()` |
| `jogo-detail.component.ts` | Corrigir semantica do botao "Remover" (chamar removerParticipante em vez de banirParticipante); adicionar botao "Banir"; adicionar botao "Desbanir" para BANIDO; adicionar filtro por status; adicionar badge de pendentes |
| `jogos-disponiveis.component.ts` | Adicionar botao "Solicitar Entrada" para jogos sem participacao; exibir status atual de participacao (PENDENTE, REJEITADO); adicionar botao "Cancelar Solicitacao" para PENDENTE |

#### Artefatos novos no frontend

Nenhum componente novo e critico — tudo entra nas paginas existentes.

---

## 3. Sequencia de Implementacao

```
Fase 1 (Backend) — deve ser concluida antes da Fase 2

  P1-T1: Corrigir constraint unique + logica de re-solicitacao (CRITICO)
       Depende de: nada
       Bloqueia: todos os demais

  P1-T2: Corrigir semantica DELETE/BANIR + adicionar endpoints faltantes
       Depende de: P1-T1
       Bloqueia: P1-T3, Fase 2

  P1-T3: Testes de integracao completos
       Depende de: P1-T1, P1-T2
       Bloqueia: Fase 2 (nao pode comitar sem testes)

Fase 2 (Frontend) — pode comecar apos P1-T2 estar em review

  P2-T1: Corrigir API service + Business service (alinhamento com novos endpoints)
       Depende de: P1-T2
       Bloqueia: P2-T2, P2-T3

  P2-T2: Corrigir JogoDetail (Mestre) — semantica de remover/banir/desbanir + filtro
       Depende de: P2-T1
       Bloqueia: nada

  P2-T3: Corrigir JogosDisponiveis (Jogador) — solicitar, cancelar, ver status
       Depende de: P2-T1
       Bloqueia: nada
```

---

## 4. Riscos e Dependencias

### Risco CRITICO: unique constraint `uk_jogo_usuario`

A entity atual tem:
```java
@UniqueConstraint(name = "uk_jogo_usuario", columnNames = {"jogo_id", "usuario_id"})
```

Esta constraint impede criar um segundo registro de participacao para o mesmo par (jogo, usuario), mesmo apos soft delete. A spec exige que apos REJEITADO ou REMOVIDO o jogador possa criar um novo registro PENDENTE.

**Solucao:** Remover a unique constraint global e adicionar uma constraint condicional (parcial index no banco) que so aplica unicidade para registros ativos (deleted_at IS NULL) com status APROVADO ou BANIDO. Alternativamente: reusar o registro existente atualizando o status em vez de criar novo.

**Decisao recomendada:** Reutilizar o registro existente (UPDATE status=PENDENTE, deletedAt=null) em vez de INSERT novo. Isso evita migracao de schema e e mais simples de implementar com os tests existentes.

**Impacto:** A logica de `solicitar()` precisa: (1) checar se existe registro BANIDO (rejeitar com 409); (2) checar se existe registro APROVADO ativo (rejeitar com 409); (3) se existe registro REJEITADO ou REMOVIDO (soft-deleted), reativar o registro com status PENDENTE; (4) caso contrario, criar novo registro.

### Risco MEDIO: Autenticacao em testes

Os testes existentes usam `SecurityContextHolder` diretamente. Os novos testes devem seguir o mesmo padrao (`setAuth(usuario)`).

### Dependencia: Spec 006 (Ficha)

A Spec 006 requer que o `ParticipanteSecurityService.assertCanAccessJogo()` esteja correto antes de implementar o acesso a fichas. O `ParticipanteSecurityService` ja esta implementado e correto — sem dependencia de codigo novo.

### Dependencia: Frontend — rota de jogos publicos

O backend atual nao tem endpoint `GET /api/v1/jogos/publicos` para o Jogador descobrir jogos onde pode solicitar entrada. O `JogosDisponiveisComponent` ja tem um TODO documentado sobre isso. **Esta feature nao e parte da Spec 005** — Jogador precisa ter o ID do jogo de alguma forma (ex: link compartilhado pelo Mestre). O fluxo de descoberta de jogos e escopo de spec futura.

---

## 5. Mapeamento de Testes Necessarios

### Novos testes de integracao (backend)

| Cenario | Metodo de teste |
|---------|----------------|
| Re-solicitacao apos REJEITADO | `devePermitirReSolicitacaoAposRejeicao` |
| Re-solicitacao apos REMOVIDO | `devePermitirReSolicitacaoAposRemocao` |
| BANIDO nao pode solicitar | `naoDevePermitirSolicitacaoSeBanido` |
| Desbanir transiciona para APROVADO | `deveMestreDesbanirParticipante` |
| Remover faz soft delete (nao banir) | `deveMestreRemoverParticipanteComSoftDelete` |
| Meu status encontrado | `deveRetornarMeuStatusDeParticipacao` |
| Meu status nao encontrado | `deveRetornarVazioSeNaoHaParticipacao` |
| Cancelar solicitacao PENDENTE | `deveJogadorCancelarSolicitacaoPendente` |
| Cancelar solicitacao nao-PENDENTE | `naoDeveCancelarSolicitacaoNaoPendente` |
| Filtrar por status PENDENTE | `deveListarApenasParticipantesComStatusPendente` |
| Aprovar nao pode BANIDO | `naoDeveAprovarParticipanteBanido` |
| Banir apenas APROVADO | `naoDeveBanirParticipantePendente` |

### Novos testes de frontend (Vitest)

| Cenario | Arquivo |
|---------|---------|
| `desbanirParticipante` chama endpoint correto | `jogos-api.service.spec.ts` |
| `meuStatus` chama endpoint correto | `jogos-api.service.spec.ts` |
| JogoDetail exibe botao "Banir" para APROVADO | `jogo-detail.component.spec.ts` |
| JogoDetail exibe botao "Desbanir" para BANIDO | `jogo-detail.component.spec.ts` |
| JogosDisponiveis exibe botao "Solicitar" | `jogos-disponiveis.component.spec.ts` |

---

*Produzido por: Business Analyst/PO | 2026-04-02*
