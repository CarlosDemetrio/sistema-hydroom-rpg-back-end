# P1-T1 — ImageStorageService Interface + Implementações

**Fase:** 1 — Storage
**Complexidade:** 🟡 Média
**Depende de:** nada
**Bloqueia:** P2-T1

## Objetivo

Abstrair o storage de imagens para suportar local (dev) e S3 (produção).

## Checklist

### 1. ImageStorageService interface

- [ ] `service/storage/ImageStorageService.java` (interface)
- [ ] `String store(MultipartFile file, Long fichaId)` → retorna URL de acesso
- [ ] `void delete(String url)` → remove do storage

### 2. LocalImageStorageService

- [ ] `@Service @ConditionalOnProperty(name="app.storage.type", havingValue="local", matchIfMissing=true)`
- [ ] `@Value("${app.storage.local.path}") String basePath`
- [ ] `store()`:
  - Validar content-type (aceitar apenas image/jpeg, image/png, image/webp)
  - Criar diretório `{basePath}/fichas/{fichaId}/` se não existir
  - Gerar nome de arquivo: `{UUID}.{extensao}`
  - Salvar arquivo com `Files.write()`
  - Retornar URL relativa: `/storage/fichas/{fichaId}/{filename}`
- [ ] `delete()`: extrair path da URL, deletar arquivo com `Files.deleteIfExists()`

### 3. S3ImageStorageService (stub)

- [ ] `@Service @ConditionalOnProperty(name="app.storage.type", havingValue="s3")`
- [ ] Implementar interface com métodos que lançam `UnsupportedOperationException("S3 storage não configurado")`
- [ ] Documentar: requer configuração de bucket e credenciais AWS

### 4. Configuração application.properties

- [ ] Adicionar em `application.properties`:
  ```
  app.storage.type=local
  app.storage.local.path=./uploads
  ```

### 5. Utilitário de validação

- [ ] `ImageValidationUtil.validarTipo(MultipartFile file)`:
  - Verificar `file.getContentType()` contra lista de tipos aceitos
  - Lançar `BusinessException("Tipo de arquivo não suportado: ${contentType}. Aceito: image/jpeg, image/png, image/webp")` se inválido

## Arquivos afetados

- `service/storage/ImageStorageService.java` (NOVO)
- `service/storage/LocalImageStorageService.java` (NOVO)
- `service/storage/S3ImageStorageService.java` (NOVO)
- `util/ImageValidationUtil.java` (NOVO)
- `src/main/resources/application.properties` (MODIFICAR)

## Verificações de aceitação

- [ ] LocalImageStorageService salva arquivo no path configurado
- [ ] store() com image/pdf lança BusinessException
- [ ] delete() remove arquivo do filesystem
- [ ] `./mvnw test` passa
