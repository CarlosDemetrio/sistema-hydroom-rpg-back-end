# P1-T2 — Adicionar endpoints faltantes no backend

> Fase: 1 — Backend
> Complexidade: 🟡 media
> Depende de: P1-T1
> Bloqueia: P1-T3, Fase 2

---

## Objetivo

Completar o contrato de API da Spec 005 adicionando os endpoints ausentes e corrigindo a semantica do `DELETE /{pid}` (que atualmente bane em vez de remover).

---

## Contexto — Arquivos a Ler Antes de Comecar

- `controller/JogoParticipanteController.java` — controller atual
- `service/JogoParticipanteService.java` — service atual (apos P1-T1)
- `dto/response/ParticipanteResponse.java` — DTO de resposta
- `docs/specs/005-participantes/spec.md` — secao 6 (tabela de endpoints)

---

## Diagnostico: Endpoints Atuais vs Esperados

| Endpoint spec | Status atual | Acao necessaria |
|--------------|-------------|----------------|
| `POST /solicitar` | OK (com bugs corrigidos em P1-T1) | Nenhuma apos P1-T1 |
| `GET /` (listar) | OK, sem filtro | Adicionar `?status` opcional |
| `GET /meu-status` | AUSENTE | Criar |
| `PUT /{pid}/aprovar` | OK | Nenhuma |
| `PUT /{pid}/rejeitar` | OK | Nenhuma |
| `PUT /{pid}/banir` | AUSENTE (atual DELETE faz banir) | Criar e migrar logica |
| `PUT /{pid}/desbanir` | AUSENTE | Criar |
| `DELETE /{pid}` (remover) | ERRADO (bane em vez de remover) | Corrigir para soft delete |
| `DELETE /minha-solicitacao` | AUSENTE | Criar |

---

## Passos de Implementacao

### Passo 1: Corrigir semantica do `DELETE /{pid}` no service

O metodo `banir()` atual deve ser renomeado para refletir o que faz. O DELETE deve fazer soft delete (remocao provisoria), nao banimento.

**Em `JogoParticipanteService.java`:**

Renomear `banir()` para algo mais especifico — ou manter o nome e criar metodo separado. A abordagem recomendada e manter `banir()` e adicionar `remover()`:

```java
/**
 * Mestre remove provisoriamente um participante APROVADO (soft delete).
 * O jogador pode re-solicitar.
 */
@Transactional
public void remover(Long jogoId, Long participanteId) {
    Usuario mestre = getUsuarioAtual();
    assertMestre(jogoId, mestre.getId());

    JogoParticipante participante = buscarParticipante(jogoId, participanteId);
    assertStatus(participante, StatusParticipante.APROVADO, "remover");

    participante.delete(); // BaseEntity.delete() seta deletedAt
    log.info("Mestre {} removeu participante {} do jogo {}", mestre.getId(), participanteId, jogoId);
    participanteRepository.save(participante);
}
```

### Passo 2: Criar `desbanir()` no service

```java
/**
 * Mestre desbane participante BANIDO — transicao direta para APROVADO.
 * Nao exige nova solicitacao.
 */
@Transactional
public JogoParticipante desbanir(Long jogoId, Long participanteId) {
    Usuario mestre = getUsuarioAtual();
    assertMestre(jogoId, mestre.getId());

    JogoParticipante participante = buscarParticipante(jogoId, participanteId);
    assertStatus(participante, StatusParticipante.BANIDO, "desbanir");

    participante.setStatus(StatusParticipante.APROVADO);
    log.info("Mestre {} desbaniu participante {} no jogo {}", mestre.getId(), participanteId, jogoId);
    return participanteRepository.save(participante);
}
```

### Passo 3: Criar `meuStatus()` no service

```java
/**
 * Retorna o status de participacao do usuario autenticado no jogo.
 * Retorna Optional.empty() se nunca solicitou.
 * Nota: nao retorna registros soft-deleted (apenas participacoes ativas).
 */
public Optional<JogoParticipante> meuStatus(Long jogoId) {
    Usuario usuario = getUsuarioAtual();
    return participanteRepository.findByJogoIdAndUsuarioId(jogoId, usuario.getId());
}
```

### Passo 4: Criar `cancelarSolicitacao()` no service

```java
/**
 * Jogador cancela propria solicitacao PENDENTE (soft delete do registro).
 */
@Transactional
public void cancelarSolicitacao(Long jogoId) {
    Usuario usuario = getUsuarioAtual();

    JogoParticipante participante = participanteRepository
        .findByJogoIdAndUsuarioId(jogoId, usuario.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Participacao", jogoId));

    if (!StatusParticipante.PENDENTE.equals(participante.getStatus())) {
        throw new BusinessException(
            "Nao e possivel cancelar solicitacao com status " + participante.getStatus() + ".");
    }

    participante.delete();
    participanteRepository.save(participante);
    log.info("Usuario {} cancelou solicitacao no jogo {}", usuario.getId(), jogoId);
}
```

### Passo 5: Adicionar filtro por status na listagem

**Em `JogoParticipanteRepository.java`**, a query `findByJogoId` existente retorna tudo. Adicionar sobrecarga com status opcional:

```java
@Query("""
    SELECT p FROM JogoParticipante p
    WHERE p.jogo.id = :jogoId
    AND p.deletedAt IS NULL
    AND (:status IS NULL OR p.status = :status)
    ORDER BY p.createdAt DESC
""")
List<JogoParticipante> findByJogoIdAndStatusOpcional(
    @Param("jogoId") Long jogoId,
    @Param("status") StatusParticipante status
);
```

**Em `JogoParticipanteService.java`**, modificar `listar()` para aceitar filtro:

```java
public List<JogoParticipante> listar(Long jogoId, StatusParticipante filtroStatus) {
    Usuario usuario = getUsuarioAtual();
    boolean ehMestre = participanteRepository.existsByJogoIdAndUsuarioIdAndRole(
        jogoId, usuario.getId(), RoleJogo.MESTRE);

    if (ehMestre) {
        return participanteRepository.findByJogoIdAndStatusOpcional(jogoId, filtroStatus);
    }
    // Jogador sempre ve apenas APROVADOS, independente do filtro
    return participanteRepository.findByJogoIdAndStatus(jogoId, StatusParticipante.APROVADO);
}
```

### Passo 6: Corrigir e expandir o controller

**Em `JogoParticipanteController.java`**, o controller deve ser reescrito para refletir todos os endpoints corretos:

```java
// Listar com filtro opcional
@GetMapping
@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
public ResponseEntity<List<ParticipanteResponse>> listar(
        @PathVariable Long jogoId,
        @RequestParam(required = false) StatusParticipante status) {
    return ResponseEntity.ok(
        participanteService.listar(jogoId, status).stream()
            .map(mapper::toResponse).toList()
    );
}

// Meu status (JOGADOR)
@GetMapping("/meu-status")
@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
@Operation(summary = "Ver proprio status de participacao")
public ResponseEntity<ParticipanteResponse> meuStatus(@PathVariable Long jogoId) {
    return participanteService.meuStatus(jogoId)
        .map(mapper::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}

// Banir (MESTRE) — novo endpoint PUT
@PutMapping("/{participanteId}/banir")
@PreAuthorize("hasRole('MESTRE')")
@Operation(summary = "Banir participante aprovado (Apenas MESTRE)")
public ResponseEntity<ParticipanteResponse> banir(
        @PathVariable Long jogoId,
        @PathVariable Long participanteId) {
    log.info("Banindo participante {} no jogo {}", participanteId, jogoId);
    return ResponseEntity.ok(mapper.toResponse(participanteService.banir(jogoId, participanteId)));
}

// Desbanir (MESTRE)
@PutMapping("/{participanteId}/desbanir")
@PreAuthorize("hasRole('MESTRE')")
@Operation(summary = "Desbanir participante banido (Apenas MESTRE)")
public ResponseEntity<ParticipanteResponse> desbanir(
        @PathVariable Long jogoId,
        @PathVariable Long participanteId) {
    log.info("Desbanindo participante {} no jogo {}", participanteId, jogoId);
    return ResponseEntity.ok(mapper.toResponse(participanteService.desbanir(jogoId, participanteId)));
}

// Remover provisoriamente (MESTRE) — agora faz soft delete, nao bane
@DeleteMapping("/{participanteId}")
@PreAuthorize("hasRole('MESTRE')")
@Operation(summary = "Remover participante provisoriamente (Apenas MESTRE)",
           description = "Soft delete. Jogador pode re-solicitar. Use /banir para restricao permanente.")
public ResponseEntity<Void> remover(
        @PathVariable Long jogoId,
        @PathVariable Long participanteId) {
    log.info("Removendo participante {} do jogo {}", participanteId, jogoId);
    participanteService.remover(jogoId, participanteId);
    return ResponseEntity.noContent().build();
}

// Cancelar propria solicitacao (JOGADOR)
@DeleteMapping("/minha-solicitacao")
@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
@Operation(summary = "Cancelar propria solicitacao pendente")
public ResponseEntity<Void> cancelarSolicitacao(@PathVariable Long jogoId) {
    log.info("Cancelando solicitacao no jogo {}", jogoId);
    participanteService.cancelarSolicitacao(jogoId);
    return ResponseEntity.noContent().build();
}
```

**Atencao:** Remover o metodo `banir()` antigo que usava `@DeleteMapping("/{participanteId}")` — ele sera substituido pelo novo `@DeleteMapping("/{participanteId}")` com semantica de remocao.

---

## Ajustes em `banir()` existente

O metodo `banir()` atual aceita qualquer status (nao BANIDO). A spec diz que so APROVADO pode ser banido. Adicionar restricao:

```java
// Em JogoParticipanteService.banir()
assertStatus(participante, StatusParticipante.APROVADO, "banir");
// Remover o check manual de StatusParticipante.BANIDO.equals(p.getStatus())
```

---

## Criterios de Aceitacao

- [ ] `GET /participantes?status=PENDENTE` retorna apenas participantes PENDENTES para Mestre
- [ ] `GET /participantes` sem filtro retorna todos (Mestre) ou apenas APROVADOS (Jogador)
- [ ] `GET /participantes/meu-status` retorna 200 + ParticipanteResponse ou 404
- [ ] `PUT /{pid}/banir` transiciona APROVADO para BANIDO (HTTP 200)
- [ ] `PUT /{pid}/banir` para PENDENTE retorna HTTP 422 (transicao invalida)
- [ ] `PUT /{pid}/desbanir` transiciona BANIDO para APROVADO (HTTP 200)
- [ ] `DELETE /{pid}` faz soft delete (deleted_at preenchido) e retorna HTTP 204
- [ ] `DELETE /{pid}` para nao-APROVADO retorna HTTP 422
- [ ] `DELETE /minha-solicitacao` faz soft delete da propria participacao PENDENTE
- [ ] `DELETE /minha-solicitacao` para nao-PENDENTE retorna HTTP 422
- [ ] Swagger atualizado com todos os endpoints novos
