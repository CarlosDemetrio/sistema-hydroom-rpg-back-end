# P3-T1 — Reordenação em Batch

**Fase:** 3 — Reordenação
**Complexidade:** 🟡 Média
**Depende de:** nada
**Bloqueia:** nada

## Objetivo

Endpoint para reordenar itens de configuração em batch (drag & drop no frontend).

## Checklist

### 1. DTOs
- [ ] `ReordenarItemRequest` record: `Long id`, `@Min(1) int ordemExibicao`
- [ ] `ReordenarRequest` record: `@NotEmpty List<ReordenarItemRequest> itens`

### 2. ReordenacaoService
- [ ] `reordenar(Long jogoId, List<ReordenarItemRequest> itens, Class<?> entityClass)` ou método tipado por entidade:
  - Validar que todos os IDs existem e pertencem ao jogoId (falhar se algum não pertencer)
  - Para cada item: buscar entity por ID, atualizar ordemExibicao
  - @Transactional — todos em uma transação

### 3. Endpoints — para cada um dos 13 tipos:
- [ ] `PUT /api/jogos/{id}/config/atributos/reordenar`
- [ ] `PUT /api/jogos/{id}/config/bonus/reordenar`
- [ ] `PUT /api/jogos/{id}/config/vantagens/reordenar`
- [ ] `PUT /api/jogos/{id}/config/classes/reordenar`
- [ ] `PUT /api/jogos/{id}/config/racas/reordenar`
- [ ] (+ outros 8 tipos)
- [ ] Todos: `@PreAuthorize("hasRole('MESTRE')")`

### 4. Estratégia de implementação
- [ ] Reutilizar AbstractConfiguracaoService ou criar ReordenacaoService genérico
- [ ] Evitar duplicação: um único método que funciona para qualquer entity com ordemExibicao

## Arquivos afetados
- `dto/request/ReordenarItemRequest.java` (NOVO)
- `dto/request/ReordenarRequest.java` (NOVO)
- `service/ReordenacaoService.java` (NOVO)
- 13 controllers (MODIFICAR — adicionar endpoint reordenar)

## Verificações de aceitação
- [ ] PUT reordenar com 3 itens → ordemExibicao atualizada em batch
- [ ] PUT reordenar com ID de outro jogo → 400 ou 404
- [ ] `./mvnw test` passa
