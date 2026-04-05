# P1-T3 — Testes de integracao completos

> Fase: 1 — Backend
> Complexidade: 🟡 media
> Depende de: P1-T1, P1-T2
> Bloqueia: Fase 2 (nao comitar sem cobertura)

---

## Objetivo

Expandir `JogoParticipanteServiceIntegrationTest.java` para cobrir todos os cenarios da state machine definida na Spec 005, incluindo casos de borda e fluxos de excecao.

---

## Contexto — Arquivos a Ler Antes de Comecar

- `service/JogoParticipanteServiceIntegrationTest.java` — testes atuais (referencia de padrao)
- `docs/specs/005-participantes/spec.md` — secoes 3 (state machine) e 7 (user stories + criterios)
- `model/BaseEntity.java` — como funciona soft delete (`delete()`, `deletedAt`)

---

## Padroes dos Testes Existentes (manter)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")

// Autenticacao via SecurityContextHolder
private void setAuth(Usuario usuario) {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a")
    );
}

// Padrao: @DisplayName descritivo em portugues
// Padrao: Arrange-Act-Assert
// Padrao: assertThrows para exceptions esperadas
```

---

## Lista Completa de Testes a Adicionar

### Bloco: RE-SOLICITACAO (novos em P1-T1)

```java
@Test
@DisplayName("deve reativar registro ao re-solicitar apos rejeicao")
void deveReativarRegistroAposRejeicao() {
    // Arrange
    setAuth(jogador);
    JogoParticipante primeira = participanteService.solicitar(jogo.getId());
    setAuth(mestre);
    participanteService.rejeitar(jogo.getId(), primeira.getId());

    // Act
    setAuth(jogador);
    JogoParticipante reSolicitacao = participanteService.solicitar(jogo.getId());

    // Assert: mesmo ID (registro reativado), status PENDENTE
    assertThat(reSolicitacao.getId()).isEqualTo(primeira.getId());
    assertThat(reSolicitacao.getStatus()).isEqualTo(StatusParticipante.PENDENTE);
    assertThat(reSolicitacao.getDeletedAt()).isNull();
}

@Test
@DisplayName("deve reativar registro ao re-solicitar apos remocao provisoria")
void deveReativarRegistroAposRemocao() {
    // Arrange: criar aprovado, remover
    JogoParticipante aprovado = participanteRepository.save(JogoParticipante.builder()
        .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR)
        .status(StatusParticipante.APROVADO).build());
    setAuth(mestre);
    participanteService.remover(jogo.getId(), aprovado.getId());

    // Act
    setAuth(jogador);
    JogoParticipante reSolicitacao = participanteService.solicitar(jogo.getId());

    // Assert
    assertThat(reSolicitacao.getStatus()).isEqualTo(StatusParticipante.PENDENTE);
    assertThat(reSolicitacao.getDeletedAt()).isNull();
}

@Test
@DisplayName("nao deve permitir solicitacao se BANIDO")
void naoDevePermitirSolicitacaoSeBanido() {
    // Arrange: criar aprovado, banir
    JogoParticipante aprovado = participanteRepository.save(JogoParticipante.builder()
        .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR)
        .status(StatusParticipante.APROVADO).build());
    setAuth(mestre);
    participanteService.banir(jogo.getId(), aprovado.getId());

    // Act + Assert
    setAuth(jogador);
    ConflictException ex = assertThrows(ConflictException.class,
        () -> participanteService.solicitar(jogo.getId()));
    assertThat(ex.getMessage()).contains("banido");
}

@Test
@DisplayName("nao deve permitir solicitacao se PENDENTE ja existe")
void naoDevePermitirSolicitacaoDuplicadaPendente() {
    setAuth(jogador);
    participanteService.solicitar(jogo.getId());
    assertThrows(ConflictException.class, () -> participanteService.solicitar(jogo.getId()));
}
```

### Bloco: REMOVER (soft delete — distinto de BANIR)

```java
@Test
@DisplayName("deve fazer soft delete ao remover participante aprovado")
void deveFazerSoftDeleteAoRemover() {
    // Arrange
    JogoParticipante aprovado = participanteRepository.save(JogoParticipante.builder()
        .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR)
        .status(StatusParticipante.APROVADO).build());
    setAuth(mestre);

    // Act
    participanteService.remover(jogo.getId(), aprovado.getId());

    // Assert: registro nao aparece em queries normais (deletedAt preenchido)
    List<JogoParticipante> lista = participanteService.listar(jogo.getId(), null);
    assertThat(lista).noneMatch(p -> p.getId().equals(aprovado.getId()));
}

@Test
@DisplayName("nao deve remover participante nao aprovado")
void naoDeveRemoverParticipanteNaoAprovado() {
    // Arrange: criar pendente
    setAuth(jogador);
    JogoParticipante pendente = participanteService.solicitar(jogo.getId());

    // Act + Assert
    setAuth(mestre);
    assertThrows(BusinessException.class,
        () -> participanteService.remover(jogo.getId(), pendente.getId()));
}
```

### Bloco: BANIR (semantica correta — apenas APROVADO)

```java
@Test
@DisplayName("deve banir participante aprovado com status BANIDO")
void deveBanirParticipanteAprovado() {
    JogoParticipante aprovado = participanteRepository.save(JogoParticipante.builder()
        .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR)
        .status(StatusParticipante.APROVADO).build());
    setAuth(mestre);

    JogoParticipante banido = participanteService.banir(jogo.getId(), aprovado.getId());

    assertThat(banido.getStatus()).isEqualTo(StatusParticipante.BANIDO);
    assertThat(banido.getDeletedAt()).isNull(); // BANIDO permanece visivel
}

@Test
@DisplayName("nao deve banir participante pendente")
void naoDeveBanirParticipantePendente() {
    setAuth(jogador);
    JogoParticipante pendente = participanteService.solicitar(jogo.getId());
    setAuth(mestre);

    assertThrows(BusinessException.class,
        () -> participanteService.banir(jogo.getId(), pendente.getId()));
}
```

### Bloco: DESBANIR

```java
@Test
@DisplayName("deve desbanir participante banido com transicao direta para APROVADO")
void deveDesbanirParticipanteBanido() {
    // Arrange: criar banido
    JogoParticipante banido = participanteRepository.save(JogoParticipante.builder()
        .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR)
        .status(StatusParticipante.BANIDO).build());
    setAuth(mestre);

    // Act
    JogoParticipante aprovado = participanteService.desbanir(jogo.getId(), banido.getId());

    // Assert
    assertThat(aprovado.getStatus()).isEqualTo(StatusParticipante.APROVADO);
}

@Test
@DisplayName("nao deve desbanir participante nao banido")
void naoDeveDesbanirParticipanteNaoBanido() {
    setAuth(jogador);
    JogoParticipante pendente = participanteService.solicitar(jogo.getId());
    setAuth(mestre);

    assertThrows(BusinessException.class,
        () -> participanteService.desbanir(jogo.getId(), pendente.getId()));
}
```

### Bloco: MEU STATUS

```java
@Test
@DisplayName("deve retornar status de participacao do usuario autenticado")
void deveRetornarMeuStatus() {
    // Arrange
    setAuth(jogador);
    participanteService.solicitar(jogo.getId());

    // Act
    Optional<JogoParticipante> status = participanteService.meuStatus(jogo.getId());

    // Assert
    assertThat(status).isPresent();
    assertThat(status.get().getStatus()).isEqualTo(StatusParticipante.PENDENTE);
    assertThat(status.get().getUsuario().getId()).isEqualTo(jogador.getId());
}

@Test
@DisplayName("deve retornar vazio se usuario nao tem participacao no jogo")
void deveRetornarVazioSeSemParticipacao() {
    setAuth(jogador);
    Optional<JogoParticipante> status = participanteService.meuStatus(jogo.getId());
    assertThat(status).isEmpty();
}
```

### Bloco: CANCELAR SOLICITACAO

```java
@Test
@DisplayName("deve cancelar solicitacao pendente com soft delete")
void deveCancelarSolicitacaoPendente() {
    // Arrange
    setAuth(jogador);
    participanteService.solicitar(jogo.getId());

    // Act
    participanteService.cancelarSolicitacao(jogo.getId());

    // Assert: nao aparece na listagem
    List<JogoParticipante> lista = participanteService.listar(jogo.getId(), null);
    assertThat(lista).noneMatch(p -> p.getUsuario().getId().equals(jogador.getId()));
}

@Test
@DisplayName("nao deve cancelar solicitacao nao pendente")
void naoDeveCancelarSolicitacaoNaoPendente() {
    // Arrange: criar aprovado diretamente
    participanteRepository.save(JogoParticipante.builder()
        .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR)
        .status(StatusParticipante.APROVADO).build());
    setAuth(jogador);

    assertThrows(BusinessException.class,
        () -> participanteService.cancelarSolicitacao(jogo.getId()));
}

@Test
@DisplayName("nao deve cancelar solicitacao inexistente")
void naoDeveCancelarSolicitacaoInexistente() {
    setAuth(jogador);
    assertThrows(ResourceNotFoundException.class,
        () -> participanteService.cancelarSolicitacao(jogo.getId()));
}
```

### Bloco: LISTAR COM FILTRO

```java
@Test
@DisplayName("Mestre deve filtrar participantes por status PENDENTE")
void deveListarFiltradoPorStatusPendente() {
    // Arrange
    participanteRepository.save(JogoParticipante.builder()
        .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR)
        .status(StatusParticipante.PENDENTE).build());
    participanteRepository.save(JogoParticipante.builder()
        .jogo(jogo).usuario(outroJogador).role(RoleJogo.JOGADOR)
        .status(StatusParticipante.APROVADO).build());
    setAuth(mestre);

    // Act
    List<JogoParticipante> lista = participanteService.listar(jogo.getId(), StatusParticipante.PENDENTE);

    // Assert: apenas PENDENTES
    assertThat(lista).allMatch(p -> StatusParticipante.PENDENTE.equals(p.getStatus()));
    assertThat(lista).hasSize(1);
}

@Test
@DisplayName("Jogador sempre ve apenas APROVADOS independente de filtro")
void jogadorSempreVeApenasAprovados() {
    participanteRepository.save(JogoParticipante.builder()
        .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR)
        .status(StatusParticipante.APROVADO).build());
    participanteRepository.save(JogoParticipante.builder()
        .jogo(jogo).usuario(outroJogador).role(RoleJogo.JOGADOR)
        .status(StatusParticipante.PENDENTE).build());
    setAuth(jogador);

    List<JogoParticipante> lista = participanteService.listar(jogo.getId(), StatusParticipante.PENDENTE);

    assertThat(lista).allMatch(p -> StatusParticipante.APROVADO.equals(p.getStatus()));
}
```

---

## Meta de Cobertura

| Bloco | Testes novos | Testes existentes | Total |
|-------|-------------|------------------|-------|
| Re-solicitacao | 4 | 0 | 4 |
| Remover (soft delete) | 2 | 0 | 2 |
| Banir (semantica correta) | 2 | 1 (parcial) | 3 |
| Desbanir | 2 | 0 | 2 |
| Meu status | 2 | 0 | 2 |
| Cancelar solicitacao | 3 | 0 | 3 |
| Listar com filtro | 2 | 2 (basico) | 4 |
| **Total novos** | **17** | | |

Meta de testes apos esta task: 457 + 17 = ~474 testes.

---

## Criterios de Aceitacao

- [ ] Todos os 17 novos testes passando
- [ ] Nenhum dos 457 testes existentes quebrado
- [ ] `@DisplayName` descritivos em todos os novos testes
- [ ] Padrao Arrange-Act-Assert seguido
- [ ] Nao usar `Thread.sleep` nem mocks de banco
