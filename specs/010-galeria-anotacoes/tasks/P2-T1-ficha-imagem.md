# P2-T1 — FichaImagem Entity + Service + Controller

**Fase:** 2 — Galeria
**Complexidade:** 🟡 Média
**Depende de:** P1-T1
**Bloqueia:** P5-T1

## Objetivo

CRUD completo de imagens da ficha com upload via multipart.

## Checklist

### 1. FichaImagem entity

- [ ] `@Entity @Table(name = "ficha_imagens")` estendendo BaseEntity
- [ ] `@ManyToOne Ficha ficha` (@NotNull)
- [ ] `String urlImagem` (@NotBlank) — URL retornada pelo ImageStorageService
- [ ] `boolean isAvatar` (default false)
- [ ] `int ordem` (default: count atual + 1)
- [ ] `String descricao` (nullable, max 200)
- [ ] Lombok padrão

### 2. FichaImagemRepository

- [ ] `findByFichaIdOrderByOrdemAsc(Long fichaId)` → List<FichaImagem>
- [ ] `findByFichaIdAndIsAvatarTrue(Long fichaId)` → Optional<FichaImagem>

### 3. FichaImagemService

- [ ] `upload(Long fichaId, MultipartFile file, String descricao, String usuarioId)` → FichaImagem:
  - Validar acesso (usuário é dono da ficha ou Mestre)
  - Validar tipo via ImageValidationUtil
  - Chamar imageStorageService.store()
  - Criar FichaImagem com URL retornada
  - ordem = count atual + 1

- [ ] `listar(Long fichaId, String usuarioId)` → List<FichaImagem>

- [ ] `setAvatar(Long fichaId, Long imgId, String usuarioId)`:
  - Buscar imagem atual avatar (se existir) → setar isAvatar=false
  - Setar nova como isAvatar=true

- [ ] `deletar(Long fichaId, Long imgId, String usuarioId)`:
  - Soft delete na entity
  - Chamar imageStorageService.delete(urlImagem)

### 4. DTOs

- [ ] `FichaImagemResponse` record: id, urlImagem, isAvatar, ordem, descricao
- [ ] `UploadImagemRequest` pode ser recebido via @RequestParam (não record — multipart)

### 5. FichaImagemController

- [ ] `POST /api/fichas/{id}/imagens` (@RequestParam MultipartFile arquivo, @RequestParam(required=false) String descricao)
- [ ] `GET /api/fichas/{id}/imagens`
- [ ] `PUT /api/fichas/{id}/imagens/{imgId}/avatar`
- [ ] `DELETE /api/fichas/{id}/imagens/{imgId}`
- [ ] Todos: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

## Arquivos afetados

- `model/FichaImagem.java` (NOVO)
- `repository/FichaImagemRepository.java` (NOVO)
- `service/FichaImagemService.java` (NOVO)
- `dto/response/FichaImagemResponse.java` (NOVO)
- `controller/FichaImagemController.java` (NOVO)

## Verificações de aceitação

- [ ] POST /imagens com JPEG válido retorna 201 com FichaImagemResponse
- [ ] POST /imagens com PDF retorna 400
- [ ] PUT /avatar seta isAvatar=true e remove de outra imagem
- [ ] DELETE remove do storage e soft-deleta entity
- [ ] `./mvnw test` passa
