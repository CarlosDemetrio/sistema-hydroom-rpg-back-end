---
name: Auditoria de Segurança SP1-T18 — Endpoints de Ficha
description: Vulnerabilidades encontradas e corrigidas na auditoria de segurança dos endpoints de Ficha
type: project
---

Auditoria concluída em 2026-04-01. Dois CVEs de severidade encontrados e corrigidos.

**Vulnerabilidade 1 — CRÍTICA: Jogador podia criar NPC**
- Localização: `FichaService.criar()`
- Causa: flag `isNpc` não era bloqueada para jogadores — apenas `jogadorId` era forçado para o próprio usuário, mas `isNpc = Boolean.TRUE.equals(request.isNpc())` não tinha guarda
- Correção: adicionada verificação `if (!isMestre && Boolean.TRUE.equals(request.isNpc())) throw ForbiddenException` como passo 3 do método, antes de determinar jogadorId
- Teste quebrado que precisou ser corrigido: `NpcFichaMestreIntegrationTest.deveRejeitarCriacaoDeNpcSemRoleMestre` — o comentário original dizia que "a proteção real é no controller via @PreAuthorize"; o teste foi reescrito para `deveRejeitarCriacaoDeNpcPorJogador` verificando a rejeição no service

**Vulnerabilidade 2 — CRÍTICA: FichaPreviewService sem controle de acesso**
- Localização: `FichaPreviewService.simular()`
- Causa: o service não injetava `JogoParticipanteRepository` nem `UsuarioRepository`, portanto não havia verificação de quem era o dono da ficha
- Correção: adicionados `JogoParticipanteRepository` e `UsuarioRepository` como dependências; adicionado `verificarAcessoLeitura(ficha)` no início de `simular()` — padrão idêntico ao usado em `FichaResumoService`, `FichaVantagemService` e `FichaVidaService`

**O que estava correto:**
- `FichaService`: buscarPorId, atualizar, deletar, duplicar, atualizarAtributos, atualizarAptidoes — todos chamam `verificarAcessoLeitura/Escrita`
- `FichaVantagemService`: listar, comprar, aumentarNivel — todos verificam acesso
- `FichaVidaService`: atualizarVida, atualizarProspeccao — ambos verificam acesso
- `FichaResumoService`: getResumo — verifica acesso
- `FichaAnotacaoService`: regras de visibilidade granular por tipo de anotação
- `GET /jogos/{jogoId}/npcs` e `POST /jogos/{jogoId}/npcs` — `@PreAuthorize("hasRole('MESTRE')")` correto + dupla proteção no service

**Why:** O endpoint de preview foi adicionado tardiamente sem seguir o padrão estabelecido dos outros services. A criação de NPC por jogador era impossível via controller (PreAuthorize), mas possível via chamadas diretas ao service ou em futuras refatorações que removam a annotation.

**How to apply:** Ao criar novos services de ficha, sempre adicionar `JogoParticipanteRepository`, `UsuarioRepository` e os métodos `verificarAcessoLeitura`/`verificarAcessoEscrita`. A dupla proteção (controller + service) é obrigatória para operações de ficha.

**Novos testes adicionados:**
- `FichaServiceIntegrationTest`: 6 novos testes de segurança na seção "AUDITORIA SP1-T18"
- `FichaPreviewServiceSecurityTest`: novo arquivo com 5 testes de controle de acesso no preview
- `NpcFichaMestreIntegrationTest`: teste corrigido + novo teste separando as preocupações
