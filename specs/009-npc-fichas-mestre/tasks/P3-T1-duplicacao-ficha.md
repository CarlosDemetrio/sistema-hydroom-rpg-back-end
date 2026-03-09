# P3-T1 — FichaDuplicacaoService + Endpoint Duplicar

**Fase:** 3 — Duplicação
**Complexidade:** 🟡 Média
**Depende de:** P2-T1
**Bloqueia:** P4-T2

## Objetivo

Duplicar uma Ficha (ou NPC) com todos os sub-componentes copiando valores atuais.

## Checklist

### 1. DuplicarFichaRequest + DuplicarFichaResponse DTOs

- [ ] Request record: `@NotBlank String novoNome`, `boolean manterJogador` (default false)
- [ ] Response record: `Long fichaId`, `String nome`, `boolean isNpc`

### 2. FichaDuplicacaoService

- [ ] `duplicar(Long fichaId, DuplicarFichaRequest request, String mestreId)` → Ficha:
  - Validar que mestreId é Mestre do jogo
  - Carregar Ficha original com TODOS os sub-componentes (JOIN FETCH ou batch load):
    - FichaAtributo, FichaAptidao, FichaBonus
    - FichaVida + FichaVidaMembro
    - FichaEssencia, FichaAmeaca, FichaProspeccao
    - FichaVantagem, FichaDescricaoFisica
  - Criar nova Ficha:
    - nome = request.novoNome
    - jogo = original.jogo
    - isNpc = !request.manterJogador || original.isNpc
    - jogadorId = (isNpc) ? null : original.jogadorId
    - Demais campos de identidade (raca, classe, etc.) copiados
  - Para cada sub-entity: criar nova com novos IDs e mesmos valores
  - Tudo em @Transactional único

### 3. Endpoint

- [ ] `POST /api/fichas/{id}/duplicar`
- [ ] `@PreAuthorize("hasRole('MESTRE')")`
- [ ] Retorna 201 com DuplicarFichaResponse

## Arquivos afetados

- `dto/request/DuplicarFichaRequest.java` (NOVO)
- `dto/response/DuplicarFichaResponse.java` (NOVO)
- `service/FichaDuplicacaoService.java` (NOVO)
- `controller/FichaController.java` (MODIFICAR — novo endpoint)

## Verificações de aceitação

- [ ] POST /duplicar retorna 201 com nova fichaId
- [ ] Nova ficha tem mesmos valores de sub-entities que a original
- [ ] Alterar sub-entity da nova ficha não afeta a original
- [ ] manterJogador=false → duplicata é NPC
- [ ] manterJogador=true e original não é NPC → duplicata mantém jogadorId
- [ ] Jogador recebe 403
- [ ] `./mvnw test` passa
