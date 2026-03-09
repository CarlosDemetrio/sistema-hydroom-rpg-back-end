# P6-T1 — Resumo de Ficha

**Fase:** 6 — Resumo
**Complexidade:** 🟢 Baixa
**Depende de:** Spec 007 (cálculos devem estar feitos)
**Bloqueia:** nada

## Objetivo

Endpoint compacto de leitura da ficha — apenas dados calculados principais para cards e listagens.

## Checklist

### 1. FichaResumoResponse DTO
- [ ] Record: `Long id`, `String nome`, `int nivel`, `int xp`, `String racaNome` (nullable), `String classeNome` (nullable)
- [ ] `Map<String, Integer> atributosTotais` — sigla → total (ex: {"FOR":8, "AGI":6})
- [ ] `Map<String, Double> bonusTotais` — nome → total
- [ ] `int vidaTotal`, `int essenciaTotal`, `int ameacaTotal`

### 2. FichaService ou FichaResumoService
- [ ] Método `getResumo(Long fichaId, String usuarioId)` → FichaResumoResponse:
  - Carregar ficha com sub-entities necessárias (JOIN FETCH para evitar N+1)
  - Montar response apenas com campos calculados

### 3. Endpoint
- [ ] `GET /api/fichas/{id}/resumo`
- [ ] `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

## Arquivos afetados
- `dto/response/FichaResumoResponse.java` (NOVO)
- `service/FichaService.java` (MODIFICAR — método getResumo)
- `controller/FichaController.java` (MODIFICAR — endpoint)

## Verificações de aceitação
- [ ] GET /resumo retorna FichaResumoResponse sem sub-entidades detalhadas
- [ ] Mais rápido que GET /ficha/{id} completo (apenas joins necessários)
- [ ] `./mvnw test` passa
