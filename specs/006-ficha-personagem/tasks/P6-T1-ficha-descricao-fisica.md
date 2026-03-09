# P6-T1 — FichaDescricaoFisica

**Fase:** 6 — Descrição Física
**Complexidade:** 🟢 Baixa
**Depende de:** P1-T2
**Bloqueia:** nada

## Objetivo

Entity e endpoint para dados físicos do personagem.

## Checklist

### 1. FichaDescricaoFisica entity (1:1 com Ficha)
- [ ] `@OneToOne Ficha ficha`
- [ ] Campos: `Integer alturaCm`, `BigDecimal pesoKg`, `Integer idade`
- [ ] Descrições: `String descricaoOlhos`, `String descricaoCabelos`, `String descricaoPele` (todos nullable, max 100)
- [ ] Criada automaticamente junto com a Ficha (valores null)

### 2. DTOs
- [ ] `FichaDescricaoFisicaRequest` record: todos os campos opcionais
- [ ] `FichaDescricaoFisicaResponse` record: todos os campos

### 3. Endpoint
- [ ] `PUT /api/fichas/{id}/descricao-fisica` — atualiza todos os campos (patch semântico)

## Arquivos afetados
- `model/FichaDescricaoFisica.java` (NOVO)
- `repository/FichaDescricaoFisicaRepository.java` (NOVO)
- `dto/request/FichaDescricaoFisicaRequest.java` (NOVO)
- `dto/response/FichaDescricaoFisicaResponse.java` (NOVO)
- `service/FichaService.java` (MODIFICAR — inicializar e atualizar)
- `controller/FichaController.java` (MODIFICAR — adicionar endpoint)

## Verificações de aceitação
- [ ] FichaDescricaoFisica criada ao criar Ficha (com campos null)
- [ ] PUT /fichas/{id}/descricao-fisica atualiza campos
- [ ] `./mvnw test` passa
