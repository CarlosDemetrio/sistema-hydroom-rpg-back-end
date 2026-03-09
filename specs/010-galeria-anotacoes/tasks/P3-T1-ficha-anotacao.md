# P3-T1 — FichaAnotacao Entity + Enum Visibilidade + CRUD

**Fase:** 3 — Anotações
**Complexidade:** 🟡 Média
**Depende de:** Spec 006
**Bloqueia:** P5-T2

## Objetivo

CRUD de anotações do personagem com controle de visibilidade (PUBLICA/PRIVADA).

## Checklist

### 1. VisibilidadeAnotacao enum

- [ ] `model/enums/VisibilidadeAnotacao.java`: PUBLICA, PRIVADA

### 2. FichaAnotacao entity

- [ ] `@Entity @Table(name = "ficha_anotacoes")` estendendo BaseEntity
- [ ] `@ManyToOne Ficha ficha`
- [ ] `String titulo` (@NotBlank, max 200)
- [ ] `@Column(columnDefinition = "TEXT") String conteudo` (@NotBlank)
- [ ] `LocalDate dataAnotacao` (nullable — data narrativa da anotação)
- [ ] `@Enumerated(EnumType.STRING) VisibilidadeAnotacao visibilidade` (@NotNull, default PRIVADA)
- [ ] Lombok padrão

### 3. FichaAnotacaoRepository

- [ ] `findByFichaIdOrderByCreatedAtDesc(Long fichaId)` → List<FichaAnotacao>
- [ ] `findByFichaIdAndVisibilidade(Long fichaId, VisibilidadeAnotacao v)` → List<FichaAnotacao>

### 4. FichaAnotacaoService — regras de visibilidade

- [ ] `listar(Long fichaId, String usuarioId)` → List<FichaAnotacao>:
  - Se usuário é Mestre do jogo → retornar apenas PUBLICA
  - Se usuário é dono da ficha (jogadorId) → retornar PUBLICA + PRIVADA
  - Outros → AccessDeniedException

- [ ] `buscar(Long fichaId, Long anotacaoId, String usuarioId)`:
  - Se PRIVADA e usuário não é dono → AccessDeniedException

- [ ] `criar(Long fichaId, FichaAnotacaoRequest, String usuarioId)` → FichaAnotacao:
  - Apenas o dono da ficha (ou Mestre) pode criar anotações

- [ ] `atualizar(Long fichaId, Long anotacaoId, FichaAnotacaoRequest, String usuarioId)`:
  - Apenas o criador pode atualizar

- [ ] `deletar(Long fichaId, Long anotacaoId, String usuarioId)`:
  - Apenas criador pode deletar

### 5. DTOs

- [ ] `FichaAnotacaoRequest` record: titulo, conteudo, dataAnotacao (nullable), visibilidade (@NotNull)
- [ ] `FichaAnotacaoResponse` record: id, titulo, conteudo, dataAnotacao, visibilidade, dataCriacao, dataUltimaAtualizacao

### 6. FichaAnotacaoController

- [ ] `POST /api/fichas/{id}/anotacoes`
- [ ] `GET /api/fichas/{id}/anotacoes`
- [ ] `GET /api/fichas/{id}/anotacoes/{aid}`
- [ ] `PUT /api/fichas/{id}/anotacoes/{aid}`
- [ ] `DELETE /api/fichas/{id}/anotacoes/{aid}`
- [ ] Todos: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

## Arquivos afetados

- `model/enums/VisibilidadeAnotacao.java` (NOVO)
- `model/FichaAnotacao.java` (NOVO)
- `repository/FichaAnotacaoRepository.java` (NOVO)
- `service/FichaAnotacaoService.java` (NOVO)
- `dto/request/FichaAnotacaoRequest.java` (NOVO)
- `dto/response/FichaAnotacaoResponse.java` (NOVO)
- `controller/FichaAnotacaoController.java` (NOVO)

## Verificações de aceitação

- [ ] Mestre vê apenas anotações PUBLICA no GET lista
- [ ] Jogador dono vê PUBLICA + PRIVADA no GET lista
- [ ] Mestre recebe 403 ao tentar buscar anotação PRIVADA de outro jogador
- [ ] `./mvnw test` passa
