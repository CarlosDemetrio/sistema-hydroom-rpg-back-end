# P5-T1 — Export/Import de Configurações

**Fase:** 5 — Export/Import
**Complexidade:** 🔴 Alta
**Depende de:** nada
**Bloqueia:** nada

## Objetivo

Serializar e deserializar todas as configurações de um jogo em JSON para compartilhamento entre mestres.

## Checklist

### 1. DTOs de Export
- [ ] `ConfigExportResponse` record: `String versao` ("1.0"), `String jogoNome`, `Map<String, List<?>> configuracoes`
  - Chaves: "atributos", "bonus", "vantagens", "classes", "racas", "aptidoes", "tiposAptidao", "niveis", "generos", "indoles", "presencas", "membrosCorpo", "dadosProspeccao"
  - Valores: listas de DTOs de cada config (sem IDs, sem jogoId — apenas campos de negócio)

### 2. DTOs de Import
- [ ] `ConfigImportRequest` record: mesma estrutura de `ConfigExportResponse`

### 3. ConfigExportImportService
- [ ] `exportar(Long jogoId)` → ConfigExportResponse:
  - Carregar todas as 13 configs do jogo
  - Montar mapa com nomes como chaves

- [ ] `importar(Long jogoId, ConfigImportRequest request, String usuarioId)`:
  - Validar que usuário é Mestre do jogo
  - Para cada tipo de config: verificar se nomes duplicam configs existentes
  - Se qualquer duplicata → lançar BusinessException com lista de nomes duplicados
  - Se OK → criar todas as configs em @Transactional único (uma nova entity por item)

### 4. Endpoints
- [ ] `GET /api/jogos/{id}/config/export` → retorna JSON com ConfigExportResponse
- [ ] `POST /api/jogos/{id}/config/import` → importa ConfigImportRequest
- [ ] Ambos: `@PreAuthorize("hasRole('MESTRE')")`

## Arquivos afetados
- `dto/response/ConfigExportResponse.java` (NOVO)
- `dto/request/ConfigImportRequest.java` (NOVO)
- `service/ConfigExportImportService.java` (NOVO)
- `controller/JogoController.java` (MODIFICAR — ou novo ConfigController)

## Verificações de aceitação
- [ ] GET export retorna JSON com todas as configs
- [ ] POST import em jogo vazio cria todas as configs
- [ ] POST import com nome duplicado retorna 400 com lista de duplicatas
- [ ] POST import é atômico (se falha no meio, rollback total)
- [ ] `./mvnw test` passa
