# P4-T2 — Endpoints Update Vida/Essência/Ameaça/Prospecção

**Fase:** 4 — Estado
**Complexidade:** 🟡 Média
**Depende de:** P4-T1
**Bloqueia:** nada

## Objetivo

Endpoints para atualizar manualmente os campos de estado do personagem.

## Checklist

### 1. Request DTOs
- [ ] `AtualizarFichaVidaRequest`: vt, outros, danosPorMembro (Map<membroConfigId, danoRecebido>)
- [ ] `AtualizarFichaEssenciaRequest`: vantagens, outros, essenciaRestante
- [ ] `AtualizarFichaAmeacaRequest`: itens, titulos, outros
- [ ] `AtualizarFichaProspeccaoRequest`: List de {dadoConfigId, quantidade}

### 2. Endpoints no FichaController
- [ ] `PUT /api/fichas/{id}/vida`
- [ ] `PUT /api/fichas/{id}/essencia`
- [ ] `PUT /api/fichas/{id}/ameaca`
- [ ] `PUT /api/fichas/{id}/prospeccao`
- [ ] Todos: @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")

## Arquivos afetados
- 4 Request DTOs (NOVOS)
- `service/FichaService.java` (MODIFICAR)
- `controller/FichaController.java` (MODIFICAR)

## Verificações de aceitação
- [ ] PUT /fichas/{id}/vida atualiza campos manuais
- [ ] `./mvnw test` passa
