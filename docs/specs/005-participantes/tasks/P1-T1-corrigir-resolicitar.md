# P1-T1 — Corrigir logica de re-solicitacao e constraint

> Fase: 1 — Backend
> Complexidade: 🔴 grande
> Depende de: nada
> Bloqueia: P1-T2, P1-T3

---

## Objetivo

Corrigir o problema critico que impede jogadores de re-solicitar entrada apos rejeicao ou remocao. Atualmente a unique constraint `uk_jogo_usuario` e a logica de `solicitar()` bloqueiam qualquer segunda tentativa do mesmo usuario no mesmo jogo.

---

## Contexto — Arquivos a Ler Antes de Comecar

- `model/JogoParticipante.java` — constraint `uk_jogo_usuario` e campo `status`
- `service/JogoParticipanteService.java` — metodo `solicitar()`, linhas 42-63
- `repository/JogoParticipanteRepository.java` — `existsByJogoIdAndUsuarioId()`, `findByJogoIdAndUsuarioId()`
- `model/enums/StatusParticipante.java` — enum com os 4 valores

---

## Diagnostico do Problema

### Problema 1: unique constraint impede segundo registro

```java
// JogoParticipante.java (atual)
@UniqueConstraint(name = "uk_jogo_usuario", columnNames = {"jogo_id", "usuario_id"})
```

Esta constraint e global — bloqueia INSERT de novo registro mesmo quando o registro anterior foi soft-deleted (deleted_at != null).

### Problema 2: `solicitar()` checa existencia sem distinguir status

```java
// JogoParticipanteService.java (atual) — ERRADO
if (participanteRepository.existsByJogoIdAndUsuarioId(jogoId, usuario.getId())) {
    throw new ConflictException("Voce ja possui uma participacao neste jogo.");
}
```

Este check usa `deleted_at IS NULL`, entao bloqueia REJEITADO ativo. Mas nao detecta BANIDO em registros soft-deleted.

---

## Solucao: Reutilizar Registro Existente (Strategy "Reactivate")

Em vez de criar um novo registro a cada solicitacao, a logica de `solicitar()` deve:

1. Buscar qualquer registro existente (incluindo soft-deleted) para o par (jogo, usuario)
2. Se BANIDO (ativo ou soft-deleted): lancar ConflictException 409
3. Se APROVADO (ativo): lancar ConflictException 409
4. Se PENDENTE (ativo): lancar ConflictException 409 (ja tem solicitacao aberta)
5. Se REJEITADO (ativo): reativar — setar `status=PENDENTE`, `deletedAt=null`, salvar
6. Se REMOVIDO (soft-deleted, status qualquer exceto BANIDO): reativar — setar `status=PENDENTE`, `deletedAt=null`, salvar
7. Se nenhum registro: criar novo

Esta abordagem mantem a unique constraint (sem migracao de schema), resolve todos os casos e e testavel.

---

## Passos de Implementacao

### Passo 1: Adicionar query que busca incluindo soft-deleted

Em `JogoParticipanteRepository.java`, adicionar:

```java
/**
 * Busca participacao incluindo registros soft-deleted.
 * Usado para verificar banimentos e re-solicitacoes.
 */
@Query("""
    SELECT p FROM JogoParticipante p
    WHERE p.jogo.id = :jogoId
    AND p.usuario.id = :usuarioId
""")
Optional<JogoParticipante> findByJogoIdAndUsuarioIdIncluindoRemovidos(
    @Param("jogoId") Long jogoId,
    @Param("usuarioId") Long usuarioId
);
```

**Atencao:** Esta query NAO deve ter o filtro `deleted_at IS NULL` do `@SQLRestriction`. Para contornar o `@SQLRestriction` da `BaseEntity`, usar a anotacao `@FilterDef` ou `@Query` com JPQL nativo com subquery, OU usar `EntityManager` com `@SQLRestriction` desabilitado via `Session.disableFilter()`. A forma mais simples no Spring Data JPA e usar `@Query` com `nativeQuery = true`:

```java
@Query(value = """
    SELECT * FROM jogo_participantes
    WHERE jogo_id = :jogoId AND usuario_id = :usuarioId
    ORDER BY created_at DESC LIMIT 1
""", nativeQuery = true)
Optional<JogoParticipante> findByJogoIdAndUsuarioIdIncluindoRemovidos(
    @Param("jogoId") Long jogoId,
    @Param("usuarioId") Long usuarioId
);
```

**Verificar** se `BaseEntity` usa `@SQLRestriction("deleted_at IS NULL")` — se sim, JPQL sem native=true vai aplicar o filtro automaticamente. Confirmar em `model/BaseEntity.java` antes de implementar.

### Passo 2: Reescrever `solicitar()` no service

```java
@Transactional
public JogoParticipante solicitar(Long jogoId) {
    Usuario usuario = getUsuarioAtual();
    Jogo jogo = jogoRepository.findById(jogoId)
        .orElseThrow(() -> new ResourceNotFoundException("Jogo", jogoId));

    // RN-005: Mestre nao pode solicitar no proprio jogo
    if (participanteRepository.existsByJogoIdAndUsuarioIdAndRole(
            jogoId, usuario.getId(), RoleJogo.MESTRE)) {
        throw new BusinessException("O Mestre nao pode solicitar entrada no proprio jogo.");
    }

    // Buscar registro existente (incluindo soft-deleted)
    Optional<JogoParticipante> existente = participanteRepository
        .findByJogoIdAndUsuarioIdIncluindoRemovidos(jogoId, usuario.getId());

    if (existente.isPresent()) {
        JogoParticipante p = existente.get();

        // RF-002: BANIDO nao pode re-solicitar
        if (StatusParticipante.BANIDO.equals(p.getStatus())) {
            throw new ConflictException("Voce foi banido deste jogo pelo Mestre.");
        }

        // RF-003: APROVADO nao precisa solicitar novamente
        if (StatusParticipante.APROVADO.equals(p.getStatus()) && p.getDeletedAt() == null) {
            throw new ConflictException("Voce ja e participante aprovado deste jogo.");
        }

        // PENDENTE ja aberto
        if (StatusParticipante.PENDENTE.equals(p.getStatus()) && p.getDeletedAt() == null) {
            throw new ConflictException("Voce ja possui uma solicitacao pendente neste jogo.");
        }

        // RF-004/RF-007: REJEITADO ou REMOVIDO — reativar registro
        p.setStatus(StatusParticipante.PENDENTE);
        p.setDeletedAt(null);  // reativar soft delete se estava removido
        log.info("Reativando participacao {} para usuario {} no jogo {}",
            p.getId(), usuario.getId(), jogoId);
        return participanteRepository.save(p);
    }

    // Primeiro registro
    JogoParticipante participante = JogoParticipante.builder()
        .jogo(jogo)
        .usuario(usuario)
        .role(RoleJogo.JOGADOR)
        .status(StatusParticipante.PENDENTE)
        .build();

    log.info("Usuario {} solicitou entrada no jogo {}", usuario.getId(), jogoId);
    return participanteRepository.save(participante);
}
```

### Passo 3: Verificar metodo `delete()` em BaseEntity

Confirmar que `BaseEntity.delete()` seta `deletedAt = LocalDateTime.now()`. O metodo `setDeletedAt(null)` deve funcionar para reativar — verificar se ha restricao.

---

## Testes Obrigatorios para Esta Task

Adicionar em `JogoParticipanteServiceIntegrationTest.java`:

```java
@Test
@DisplayName("deve permitir re-solicitacao apos rejeicao")
void devePermitirReSolicitacaoAposRejeicao() {
    // Arrange: solicitar e rejeitar
    setAuth(jogador);
    JogoParticipante pendente = participanteService.solicitar(jogo.getId());
    setAuth(mestre);
    participanteService.rejeitar(jogo.getId(), pendente.getId());

    // Act: re-solicitar
    setAuth(jogador);
    JogoParticipante novaPendente = participanteService.solicitar(jogo.getId());

    // Assert
    assertThat(novaPendente.getStatus()).isEqualTo(StatusParticipante.PENDENTE);
    assertThat(novaPendente.getId()).isEqualTo(pendente.getId()); // mesmo registro
}

@Test
@DisplayName("nao deve permitir solicitacao se BANIDO")
void naoDevePermitirSolicitacaoSeBanido() {
    // Arrange: criar aprovado e banir
    JogoParticipante aprovado = participanteRepository.save(JogoParticipante.builder()
        .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR).status(StatusParticipante.APROVADO).build());
    setAuth(mestre);
    participanteService.banir(jogo.getId(), aprovado.getId()); // banir via endpoint futuro

    // Act + Assert
    setAuth(jogador);
    assertThrows(ConflictException.class, () -> participanteService.solicitar(jogo.getId()));
}
```

---

## Criterios de Aceitacao

- [ ] Jogador com participacao REJEITADO consegue chamar `solicitar()` sem erro
- [ ] Jogador com participacao REMOVIDO (soft-deleted) consegue chamar `solicitar()` sem erro
- [ ] Ambos os casos acima reutilizam o mesmo registro (mesmo ID), apenas atualizam status
- [ ] Jogador BANIDO recebe ConflictException ao tentar solicitar
- [ ] Jogador APROVADO recebe ConflictException ao tentar solicitar novamente
- [ ] Jogador sem nenhuma participacao cria novo registro normalmente
- [ ] Testes passando sem regressao
