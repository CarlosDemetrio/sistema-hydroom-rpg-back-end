# P2-T2 — Endpoints Update Atributos/Aptidões

**Fase:** 2 — Atributos/Aptidões
**Complexidade:** 🟡 Média
**Depende de:** P2-T1
**Bloqueia:** nada (mas P7-T1 cobre esses endpoints)

## Objetivo

Endpoints para atualizar os valores de atributos e aptidões de uma ficha.

## Checklist

### 1. Request DTOs
- [ ] `AtualizarFichaAtributosRequest` record: `List<AtualizarAtributoItem> atributos` onde `AtualizarAtributoItem` tem `atributoConfigId`, `base`, `nivel`, `outros`
- [ ] `AtualizarFichaAptidoesRequest` record: similar para aptidões

### 2. FichaAtributoService / FichaService (atualizar atributos)
- [ ] `atualizarAtributos(Long fichaId, AtualizarFichaAtributosRequest, String usuarioId)`:
  - Validar que o usuário é dono da ficha (ou Mestre)
  - Validar que total de pontos distribuídos ≤ NivelConfig.pontosAtributo do nível atual
  - Validar que nenhum atributo (base+nivel) > NivelConfig.limitadorAtributo
  - Atualizar cada FichaAtributo
- [ ] `atualizarAptidoes(Long fichaId, AtualizarFichaAptidoesRequest, String usuarioId)`:
  - Validar total ≤ NivelConfig.pontosAptidao

### 3. Endpoints no FichaController
- [ ] `PUT /api/fichas/{id}/atributos` — @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
- [ ] `PUT /api/fichas/{id}/aptidoes` — @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")

## Arquivos afetados
- `dto/request/AtualizarFichaAtributosRequest.java` (NOVO)
- `dto/request/AtualizarFichaAptidoesRequest.java` (NOVO)
- `service/FichaService.java` (MODIFICAR — adicionar métodos)
- `controller/FichaController.java` (MODIFICAR — adicionar endpoints)

## Verificações de aceitação
- [ ] PUT /fichas/{id}/atributos com pontos acima do limite retorna 400 com mensagem descritiva
- [ ] PUT /fichas/{id}/atributos com atributo acima do limitador retorna 400
- [ ] `./mvnw test` passa
