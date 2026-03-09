# P2-T2 — Filtros nas Fichas

**Fase:** 2 — Filtros
**Complexidade:** 🟢 Baixa
**Depende de:** Spec 006
**Bloqueia:** nada

## Objetivo

Adicionar parâmetros de busca ao endpoint de listagem de fichas.

## Checklist

### 1. FichaRepository — query com filtros
- [ ] Usar JPA Specification ou query method com Optional params
- [ ] Filtros: nome (LIKE), classeId, racaId, nivel (exato)
- [ ] Apenas fichas com isNpc=false

### 2. FichaController
- [ ] `GET /api/jogos/{id}/fichas` recebe: `?nome=`, `?classeId=`, `?racaId=`, `?nivel=` (todos opcionais)
- [ ] Passar para FichaService.listar(jogoId, filtros, usuarioId)

## Arquivos afetados
- `repository/FichaRepository.java` (MODIFICAR)
- `service/FichaService.java` (MODIFICAR)
- `controller/FichaController.java` (MODIFICAR)

## Verificações de aceitação
- [ ] GET /fichas?nivel=3 retorna apenas fichas de nível 3
- [ ] GET /fichas?nome=Arthas retorna fichas com "Arthas" no nome (case-insensitive)
- [ ] `./mvnw test` passa
