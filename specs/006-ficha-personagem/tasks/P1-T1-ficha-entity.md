# P1-T1 — Ficha Entity

**Fase:** 1 — Entity
**Complexidade:** 🟡 Média
**Depende de:** Specs 004 e 005 concluídos
**Bloqueia:** P1-T2

## Objetivo

Criar a entity Ficha com todos os campos de identidade e narrativos, relacionamentos com as configs do jogo.

## Checklist

### 1. Ficha entity
- [ ] `@Entity @Table(name = "fichas")` estendendo `BaseEntity`
- [ ] Campos de identidade:
  - `String nome` (@NotBlank, @Size(max=100))
  - `String jogadorId` (nullable — null para NPCs)
  - `@ManyToOne Jogo jogo` (@NotNull)
  - `@ManyToOne Raca raca` (nullable)
  - `@ManyToOne ClassePersonagem classe` (nullable)
  - `@ManyToOne GeneroConfig genero` (nullable)
  - `@ManyToOne IndoleConfig indole` (nullable)
  - `@ManyToOne PresencaConfig presenca` (nullable)
  - `int nivel` (default 1)
  - `int xp` (default 0)
  - `int renascimentos` (default 0)
  - `boolean isNpc` (default false)
- [ ] Campos narrativos (todos opcionais, String):
  - `origem`, `arquetipo`, `insolitus`, `tituloHeroico`
- [ ] Lombok: @Data @Builder @EqualsAndHashCode(callSuper=true) @NoArgsConstructor @AllArgsConstructor
- [ ] Validação: raça, classe, gênero, índole, presença devem pertencer ao mesmo jogo

### 2. DTOs
- [ ] `FichaCreateRequest` record: nome, jogadorId (nullable), racaId, classeId, generoId, indoleId, presencaId, origem, arquetipo, insolitus, tituloHeroico
- [ ] `FichaUpdateRequest` record: mesmos campos, todos com @Valid e nullability para patch semântico
- [ ] `FichaResponse` record: todos os campos + dataCriacao + dataUltimaAtualizacao + jogoId + isNpc

### 3. Mapper
- [ ] `FichaMapper` com toEntity(request, Jogo, Raca, ClassePersonagem, etc.) e toResponse(Ficha)

## Arquivos afetados
- `model/Ficha.java` (NOVO ou REESCREVER do zero)
- `dto/request/FichaCreateRequest.java` (NOVO)
- `dto/request/FichaUpdateRequest.java` (NOVO)
- `dto/response/FichaResponse.java` (NOVO)
- `mapper/FichaMapper.java` (NOVO)

## Verificações de aceitação
- [ ] Entity compila sem erros Lombok
- [ ] DTOs são records imutáveis
- [ ] `./mvnw test` passa
