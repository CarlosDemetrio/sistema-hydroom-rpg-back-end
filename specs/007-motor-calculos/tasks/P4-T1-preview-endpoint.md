# P4-T1 — FichaPreviewService + Endpoint Preview

**Fase:** 4 — Preview
**Complexidade:** 🟡 Média
**Depende de:** P3-T1
**Bloqueia:** nada

## Objetivo

Endpoint que simula cálculos com dados editados sem persistir — útil para UX em tempo real.

## Checklist

### 1. FichaPreviewService

- [ ] `simular(Long fichaId, FichaPreviewRequest request)` → FichaResponse:
  - Carregar Ficha do DB (read-only)
  - Aplicar mudanças do request em memória (sem persistir)
  - Executar FichaCalculationService.recalcular() em memória
  - Montar e retornar FichaResponse com valores calculados
  - Não chamar save() em nenhum momento

### 2. FichaPreviewRequest DTO

- [ ] Record com: atributos (opcional), aptidoes (opcional), bonus (opcional), xp (opcional)
- [ ] Apenas os campos enviados são aplicados; o restante mantém valores atuais do DB

### 3. Endpoint

- [ ] `POST /api/fichas/{id}/preview`
- [ ] @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
- [ ] Retorna 200 com FichaResponse calculado (sem persistir)

## Arquivos afetados
- `service/FichaPreviewService.java` (NOVO)
- `dto/request/FichaPreviewRequest.java` (NOVO)
- `controller/FichaController.java` (MODIFICAR — adicionar endpoint)

## Verificações de aceitação
- [ ] POST /preview com novos valores de atributo → retorna FichaResponse com totais recalculados
- [ ] Após POST /preview, GET /api/fichas/{id} retorna valores ORIGINAIS (não persistiu)
- [ ] `./mvnw test` passa
