# P4-T1 — AnotacaoMestre Entity + CRUD

**Fase:** 4 — Anotações do Mestre
**Complexidade:** 🟢 Baixa
**Depende de:** Spec 006
**Bloqueia:** P5-T2

## Objetivo

CRUD de notas privadas do Mestre sobre uma ficha — nunca visíveis para o Jogador.

## Checklist

### 1. AnotacaoMestre entity

- [ ] `@Entity @Table(name = "anotacoes_mestre")` estendendo BaseEntity
- [ ] `@ManyToOne Ficha ficha`
- [ ] `@Column(columnDefinition = "TEXT") String conteudo` (@NotBlank)
- [ ] Lombok padrão
- [ ] **Sem campo visibilidade** — sempre privado para o Mestre

### 2. AnotacaoMestreRepository

- [ ] `findByFichaIdOrderByCreatedAtDesc(Long fichaId)` → List<AnotacaoMestre>

### 3. AnotacaoMestreService

- [ ] `criar(Long fichaId, AnotacaoMestreRequest, String mestreId)`:
  - Validar que mestreId é Mestre do jogo ao qual a ficha pertence
- [ ] `listar(Long fichaId, String mestreId)` → List
- [ ] `buscar(Long fichaId, Long anotacaoId, String mestreId)` → AnotacaoMestre
- [ ] `atualizar(Long fichaId, Long anotacaoId, AnotacaoMestreRequest, String mestreId)`
- [ ] `deletar(Long fichaId, Long anotacaoId, String mestreId)`

### 4. DTOs

- [ ] `AnotacaoMestreRequest` record: conteudo (@NotBlank)
- [ ] `AnotacaoMestreResponse` record: id, conteudo, dataCriacao, dataUltimaAtualizacao

### 5. AnotacaoMestreController

- [ ] `POST /api/fichas/{id}/anotacoes-mestre` — @PreAuthorize("hasRole('MESTRE')")
- [ ] `GET /api/fichas/{id}/anotacoes-mestre` — @PreAuthorize("hasRole('MESTRE')")
- [ ] `GET /api/fichas/{id}/anotacoes-mestre/{amid}` — @PreAuthorize("hasRole('MESTRE')")
- [ ] `PUT /api/fichas/{id}/anotacoes-mestre/{amid}` — @PreAuthorize("hasRole('MESTRE')")
- [ ] `DELETE /api/fichas/{id}/anotacoes-mestre/{amid}` — @PreAuthorize("hasRole('MESTRE')")

## Arquivos afetados

- `model/AnotacaoMestre.java` (NOVO)
- `repository/AnotacaoMestreRepository.java` (NOVO)
- `service/AnotacaoMestreService.java` (NOVO)
- `dto/request/AnotacaoMestreRequest.java` (NOVO)
- `dto/response/AnotacaoMestreResponse.java` (NOVO)
- `controller/AnotacaoMestreController.java` (NOVO)

## Verificações de aceitação

- [ ] Jogador recebe 403 em todos os endpoints de anotações-mestre
- [ ] Mestre de outro jogo recebe 403
- [ ] `./mvnw test` passa
