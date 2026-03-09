# P3-T1 — FichaBonus Entity + Endpoint

**Fase:** 3 — Bônus
**Complexidade:** 🟡 Média
**Depende de:** P1-T2
**Bloqueia:** nada

## Objetivo

Criar FichaBonus com todos os campos de bônus e endpoint de atualização.

## Checklist

### 1. FichaBonus entity
- [ ] Campos: `@ManyToOne Ficha ficha`, `@ManyToOne BonusConfig bonusConfig`
- [ ] Campos numéricos: `double base`, `double vantagens`, `double classe`, `double itens`, `double gloria`, `double outros`, `double total`
- [ ] `base` e `total` calculados pelo FichaCalculationService (Spec 007)
- [ ] Unique constraint: `(ficha_id, bonus_config_id)`

### 2. Repository
- [ ] `FichaBonusRepository.findByFichaId(Long fichaId)` → List<FichaBonus>

### 3. DTOs
- [ ] `FichaBonusResponse` record: bonusConfigId, nome, sigla, base, vantagens, classe, itens, gloria, outros, total
- [ ] `AtualizarFichaBonusRequest` record: List de items com bonusConfigId, vantagens, classe, itens, gloria, outros (base é calculado)

### 4. Endpoint
- [ ] `PUT /api/fichas/{id}/bonus` — atualiza os campos manuais (não o base, que é calculado)

## Arquivos afetados
- `model/FichaBonus.java` (NOVO)
- `repository/FichaBonusRepository.java` (NOVO)
- `dto/response/FichaBonusResponse.java` (NOVO)
- `dto/request/AtualizarFichaBonusRequest.java` (NOVO)
- `service/FichaService.java` (MODIFICAR)
- `controller/FichaController.java` (MODIFICAR)

## Verificações de aceitação
- [ ] FichaBonus criado ao criar Ficha (um por BonusConfig do jogo)
- [ ] PUT /fichas/{id}/bonus atualiza campos manuais
- [ ] `./mvnw test` passa
