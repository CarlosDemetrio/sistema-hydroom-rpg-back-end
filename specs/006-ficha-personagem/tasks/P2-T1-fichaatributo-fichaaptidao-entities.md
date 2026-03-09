# P2-T1 — FichaAtributo + FichaAptidao Entities

**Fase:** 2 — Atributos/Aptidões
**Complexidade:** 🟢 Baixa
**Depende de:** P1-T2
**Bloqueia:** P2-T2

## Objetivo

Criar as entities de atributos e aptidões da Ficha com todos os campos calculáveis.

## Checklist

### 1. FichaAtributo entity
- [ ] Campos: `@ManyToOne Ficha ficha`, `@ManyToOne AtributoConfig atributoConfig`
- [ ] Campos numéricos: `int base`, `int nivel`, `int outros`, `int total`, `double impeto`
- [ ] `total` e `impeto` são calculados pelo FichaCalculationService (Spec 007) — armazenados para leitura rápida
- [ ] Unique constraint: `(ficha_id, atributo_config_id)`
- [ ] Lombok padrão

### 2. FichaAptidao entity
- [ ] Campos: `@ManyToOne Ficha ficha`, `@ManyToOne AptidaoConfig aptidaoConfig`
- [ ] Campos numéricos: `int base`, `int sorte`, `int classe`, `int total`
- [ ] `total` calculado: base + sorte + classe
- [ ] Unique constraint: `(ficha_id, aptidao_config_id)`

### 3. Repositories
- [ ] `FichaAtributoRepository.findByFichaId(Long fichaId)` → List<FichaAtributo>
- [ ] `FichaAptidaoRepository.findByFichaId(Long fichaId)` → List<FichaAptidao>

### 4. DTOs
- [ ] `FichaAtributoResponse` record: atributoConfigId, nome, sigla, base, nivel, outros, total, impeto
- [ ] `FichaAptidaoResponse` record: aptidaoConfigId, nome, base, sorte, classe, total

## Arquivos afetados
- `model/FichaAtributo.java` (NOVO)
- `model/FichaAptidao.java` (NOVO)
- `repository/FichaAtributoRepository.java` (NOVO)
- `repository/FichaAptidaoRepository.java` (NOVO)
- `dto/response/FichaAtributoResponse.java` (NOVO)
- `dto/response/FichaAptidaoResponse.java` (NOVO)

## Verificações de aceitação
- [ ] Entities compilam sem erros Lombok
- [ ] Unique constraints corretos no banco
- [ ] `./mvnw test` passa
