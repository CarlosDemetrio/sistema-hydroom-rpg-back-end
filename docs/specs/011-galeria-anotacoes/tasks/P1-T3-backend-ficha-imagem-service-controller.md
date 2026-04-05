# T3 — Backend: Service + Controller de Galeria (Upload Cloudinary)

> Fase: P1 (Backend)
> Estimativa: 1.5 dias
> Depende de: T2 (Entity FichaImagem + Repository + CloudinaryConfig)
> Bloqueia: T4 (Testes de integracao), T6 (Frontend: Model + API Service)

---

## Objetivo

Implementar os DTOs, mapper, service e controller da galeria de imagens. Expor os 4 endpoints CRUD em `/api/v1/fichas/{fichaId}/imagens`. O upload e feito via `multipart/form-data` — o backend recebe o arquivo, faz o upload ao Cloudinary via SDK, armazena a URL e o `publicId` retornados. Ao deletar, o backend remove o arquivo do Cloudinary antes do soft delete local.

---

## Regras de Negocio Criticas

| Regra | Implementacao |
|-------|--------------|
| MESTRE acessa/opera em qualquer ficha | Verificar `isMestre` via `jogoParticipanteRepository` |
| JOGADOR acessa apenas propria ficha | `ficha.jogadorId == usuarioAtual.id` |
| JOGADOR nao acessa fichas de NPC | `ficha.isNpc == true && !isMestre` → HTTP 403 |
| Avatar unico: ao adicionar novo AVATAR, o anterior vira GALERIA | Buscar avatar atual → mudar `tipoImagem` para GALERIA → salvar |
| Limite de 20 imagens por ficha | `fichaImagemRepository.countByFichaId(fichaId) >= 20` → HTTP 422 |
| Tipos de arquivo aceitos | `image/jpeg`, `image/png`, `image/webp`, `image/gif` — outros: HTTP 400 |
| Tamanho maximo do arquivo | 10 MB — maior: HTTP 400 |
| Soft delete deleta do Cloudinary | `cloudinary.uploader().destroy(publicId)` ao deletar |
| `urlCloudinary` e `publicId` imutaveis apos upload | PUT nao aceita trocar o arquivo — apenas titulo e ordem |
| `tipoImagem` imutavel apos criacao | PUT nao aceita campo `tipoImagem` |

---

## Arquivos a Criar

### 1. DTOs

**`UploadImagemRequest.java`** — campos do multipart
```
src/main/java/.../dto/request/UploadImagemRequest.java
```
```java
/**
 * Request para upload de imagem de ficha via multipart/form-data.
 * Campos sao recebidos como @RequestParam (nao como @RequestBody JSON).
 */
public record UploadImagemRequest(
    MultipartFile arquivo,   // o arquivo binario da imagem

    @NotNull(message = "Tipo de imagem e obrigatorio")
    TipoImagem tipoImagem,

    @Size(max = 200, message = "Titulo deve ter no maximo 200 caracteres")
    String titulo            // opcional
) {}
```

> Nota: Como o endpoint usa `multipart/form-data`, os campos sao recebidos como `@RequestParam` separados no controller (nao via `@RequestBody`). O record acima e um objeto auxiliar de agrupamento — o controller coleta cada campo individualmente e monta o objeto, ou usa um `@ModelAttribute`.

**`AtualizarImagemRequest.java`** — JSON para PUT
```
src/main/java/.../dto/request/AtualizarImagemRequest.java
```
```java
public record AtualizarImagemRequest(
    @Size(max = 200)
    String titulo,

    Integer ordemExibicao
) {}
```

> Todos os campos opcionais. `NullValuePropertyMappingStrategy.IGNORE` no mapper.

**`FichaImagemResponse.java`**
```
src/main/java/.../dto/response/FichaImagemResponse.java
```
```java
public record FichaImagemResponse(
    Long id,
    Long fichaId,
    String urlCloudinary,
    String publicId,
    String titulo,
    TipoImagem tipoImagem,
    Integer ordemExibicao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
```

### 2. Mapper

**`FichaImagemMapper.java`**
```
src/main/java/.../mapper/FichaImagemMapper.java
```
```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FichaImagemMapper {

    @Mapping(target = "fichaId", source = "ficha.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    FichaImagemResponse toResponse(FichaImagem imagem);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void atualizarEntidade(AtualizarImagemRequest request, @MappingTarget FichaImagem imagem);
}
```

### 3. `CloudinaryUploadService.java` (novo — separar responsabilidade)

```
src/main/java/.../service/CloudinaryUploadService.java
```

Separar a logica de upload/delete do Cloudinary em servico proprio, facilitando mock nos testes.

```java
@Service
@RequiredArgsConstructor
public class CloudinaryUploadService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryUploadService.class);
    private final Cloudinary cloudinary;

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
        "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long TAMANHO_MAXIMO_BYTES = 10L * 1024 * 1024; // 10 MB

    /**
     * Faz upload de arquivo para o Cloudinary.
     * @param arquivo MultipartFile recebido no controller
     * @param folder  Pasta de destino no Cloudinary (ex: "rpg-fichas/1/fichas/42")
     * @return Map com "url" (String) e "public_id" (String) retornados pelo Cloudinary
     */
    public Map<String, String> upload(MultipartFile arquivo, String folder) {
        validarArquivo(arquivo);
        try {
            Map uploadResult = cloudinary.uploader().upload(
                arquivo.getBytes(),
                ObjectUtils.asMap("folder", folder, "use_filename", true, "unique_filename", true)
            );
            return Map.of(
                "url",       (String) uploadResult.get("secure_url"),
                "public_id", (String) uploadResult.get("public_id")
            );
        } catch (IOException e) {
            throw new RuntimeException("Falha ao fazer upload da imagem: " + e.getMessage(), e);
        }
    }

    /**
     * Deleta arquivo do Cloudinary pelo publicId.
     * Falha e logada mas nao lancada — soft delete local continua mesmo se Cloudinary falhar.
     */
    public void destroy(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.error("Falha ao deletar imagem do Cloudinary. publicId={}, erro={}", publicId, e.getMessage());
        }
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de imagem e obrigatorio");
        }
        String contentType = arquivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType)) {
            throw new IllegalArgumentException(
                "Tipo de arquivo nao permitido: " + contentType +
                ". Tipos aceitos: JPEG, PNG, WebP, GIF"
            );
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new IllegalArgumentException(
                "Arquivo excede o tamanho maximo de 10 MB"
            );
        }
    }
}
```

> Mapear `IllegalArgumentException` para HTTP 400 no `GlobalExceptionHandler` se ainda nao estiver mapeado.

### 4. `FichaImagemService.java`

```
src/main/java/.../service/FichaImagemService.java
```

**Metodos:**

#### `listar(fichaId, usuarioAtualId)`
1. Buscar ficha — se nao encontrar: `ResourceNotFoundException`
2. Verificar papel do usuario atual
3. Se nao isMestre:
   - Se `ficha.isNpc`: `ForbiddenException`
   - Se `ficha.jogadorId != usuarioAtual.id`: `ForbiddenException`
4. Buscar imagens ordenadas
5. Reordenar se necessario: AVATAR primeiro, depois GALERIA por `ordemExibicao`

#### `adicionar(fichaId, arquivo, tipoImagem, titulo, usuarioAtualId)`
1. Verificar ficha e acesso (mesma logica do listar)
2. Contar imagens ativas: se `>= 20` → lançar `BusinessException` (HTTP 422)
3. Montar folder: `"rpg-fichas/" + jogo.getId() + "/fichas/" + fichaId`
4. Chamar `cloudinaryUploadService.upload(arquivo, folder)` — retorna `url` e `publicId`
5. Se `tipoImagem == AVATAR`:
   - Buscar avatar atual: `findByFichaIdAndTipoImagem(fichaId, TipoImagem.AVATAR)`
   - Se existe: mudar `tipoImagem` para `GALERIA` + salvar
6. Criar nova `FichaImagem` com `urlCloudinary`, `publicId`, `titulo`, `tipoImagem`
7. Salvar e retornar

> Nota: buscar `jogo.getId()` a partir da ficha para montar o folder do Cloudinary. A entidade `Ficha` tem FK para `Jogo`.

#### `atualizar(fichaId, imagemId, request, usuarioAtualId)`
1. Buscar imagem — se nao encontrar: `ResourceNotFoundException`
2. Verificar que `imagem.ficha.id == fichaId` — se nao: `ForbiddenException`
3. Verificar acesso (isMestre ou dono da ficha)
4. Aplicar campos via mapper (IGNORE nulls)
5. Salvar e retornar

#### `deletar(fichaId, imagemId, usuarioAtualId)`
1. Buscar imagem — se nao encontrar: `ResourceNotFoundException`
2. Verificar que `imagem.ficha.id == fichaId` — se nao: `ForbiddenException`
3. Verificar acesso (isMestre ou dono da ficha)
4. Chamar `cloudinaryUploadService.destroy(imagem.getPublicId())` — falha e logada, nao lancada
5. `imagem.delete()` + salvar

### 5. `FichaImagemController.java`

```
src/main/java/.../controller/FichaImagemController.java
```

```java
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Galeria de Imagens", description = "Upload e gerenciamento de imagens em fichas")
@RequestMapping("/api/v1/fichas/{fichaId}/imagens")
```

| Metodo | Anotacao | Role | Content-Type | Retorno |
|--------|----------|------|-------------|---------|
| `listar` | `@GetMapping` | MESTRE, JOGADOR | — | `List<FichaImagemResponse>` 200 |
| `adicionar` | `@PostMapping` (multipart) | MESTRE, JOGADOR | multipart/form-data | `FichaImagemResponse` 201 |
| `atualizar` | `@PutMapping("/{id}")` | MESTRE, JOGADOR | application/json | `FichaImagemResponse` 200 |
| `deletar` | `@DeleteMapping("/{id}")` | MESTRE, JOGADOR | — | `void` 204 |

Exemplo do handler de upload:

```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
@Operation(summary = "Fazer upload de imagem",
           description = "Faz upload de imagem para o Cloudinary e vincula a ficha.")
public ResponseEntity<FichaImagemResponse> adicionar(
        @PathVariable Long fichaId,
        @RequestParam("arquivo") MultipartFile arquivo,
        @RequestParam("tipoImagem") TipoImagem tipoImagem,
        @RequestParam(value = "titulo", required = false) String titulo) {
    Long usuarioAtualId = getUsuarioAtualId();
    var imagem = fichaImagemService.adicionar(fichaId, arquivo, tipoImagem, titulo, usuarioAtualId);
    return ResponseEntity.status(HttpStatus.CREATED).body(fichaImagemMapper.toResponse(imagem));
}
```

> Padrao do controller: obter `usuarioAtualId` via `SecurityContextHolder` + `usuarioRepository.findByEmail` (mesmo padrao do `FichaAnotacaoController`). Mapper no controller, nunca no service.

---

## Tratamento de Erros

| Situacao | Exception / Comportamento | HTTP |
|----------|--------------------------|------|
| Ficha nao encontrada | `ResourceNotFoundException` | 404 |
| Imagem nao encontrada | `ResourceNotFoundException` | 404 |
| JOGADOR sem acesso a ficha | `ForbiddenException` | 403 |
| Limite de 20 imagens atingido | `BusinessException` (ou exception mapeada para 422) | 422 |
| Arquivo muito grande (> 10 MB) | `IllegalArgumentException` mapeado para 400 | 400 |
| Tipo de arquivo invalido | `IllegalArgumentException` mapeado para 400 | 400 |
| Falha no Cloudinary ao deletar | Logar e continuar — sem reversao do soft delete | — |
| Falha no Cloudinary ao fazer upload | `RuntimeException` mapeado para 502 (Bad Gateway) | 502 |

> Se `BusinessException` nao existe: verificar `docs/backend/03-exceptions.md` e criar ou reutilizar exception existente para HTTP 422.
> Falha de upload ao Cloudinary e diferente de falha na delecao: ao subir, a falha e critica (usuario nao tem imagem); ao deletar, a falha e secundaria (imagem ficara orfada no Cloudinary mas o estado local e correto).

---

## Criterios de Aceite

- [ ] `POST /fichas/{fichaId}/imagens` aceita `multipart/form-data` com `arquivo`, `tipoImagem`, `titulo`
- [ ] Upload e feito ao Cloudinary e `urlCloudinary` + `publicId` sao persistidos
- [ ] Ao adicionar AVATAR quando ja existe um: o anterior muda para GALERIA
- [ ] `POST` com 21a imagem retorna HTTP 422 com mensagem de limite
- [ ] `POST` com arquivo > 10 MB retorna HTTP 400
- [ ] `POST` com tipo de arquivo invalido retorna HTTP 400
- [ ] `GET /fichas/{fichaId}/imagens` retorna lista com AVATAR primeiro
- [ ] `PUT` atualiza titulo e ordem; campos null nao alteram o existente
- [ ] `DELETE` faz soft delete local E chama Cloudinary destroy(publicId)
- [ ] Falha no Cloudinary destroy e logada mas nao retorna erro ao cliente
- [ ] JOGADOR sem acesso a ficha recebe HTTP 403 em todos os endpoints
- [ ] JOGADOR tentando acessar ficha de NPC recebe HTTP 403
- [ ] Swagger documenta todos os 4 endpoints incluindo o schema multipart
- [ ] `CloudinaryUploadService` e componente separado (facilita mock em testes)
