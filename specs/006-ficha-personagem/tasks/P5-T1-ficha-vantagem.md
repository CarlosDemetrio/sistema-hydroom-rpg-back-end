# P5-T1 — FichaVantagem: Compra + Pré-requisitos + Custo

**Fase:** 5 — Vantagens
**Complexidade:** 🔴 Alta
**Depende de:** P1-T2 + Spec 004 Phase 4 (VantagemPreRequisito)
**Bloqueia:** nada

## Objetivo

Implementar a compra de vantagens com validação de pré-requisitos (DFS de VantagemPreRequisito) e cálculo de custo.

## Checklist

### 1. FichaVantagem entity
- [ ] Campos: `@ManyToOne Ficha ficha`, `@ManyToOne VantagemConfig vantagemConfig`, `int nivelAtual`
- [ ] Unique constraint: `(ficha_id, vantagem_config_id)`
- [ ] nivelAtual mínimo: 1 (não pode ser zerado)

### 2. FichaVantagemRepository
- [ ] `findByFichaId(Long fichaId)` → List<FichaVantagem>
- [ ] `findByFichaIdAndVantagemConfigId(Long fichaId, Long vantagemConfigId)` → Optional

### 3. FichaVantagemService

- [ ] `comprar(Long fichaId, Long vantagemConfigId, String usuarioId)`:
  - Validar que vantagem não foi comprada ainda
  - Validar pré-requisitos: para cada VantagemPreRequisito da vantagem desejada, verificar que FichaVantagem correspondente existe com nivelAtual >= nivelMinimo
  - Calcular custo via FormulaEvaluatorService.calcularCustoVantagem(formulaCusto, 1, custoBase)
  - Criar FichaVantagem com nivelAtual=1
- [ ] `aumentarNivel(Long fichaId, Long fichaVantagemId, String usuarioId)`:
  - Validar que nivelAtual < vantagemConfig.nivelMaximo
  - Incrementar nivelAtual
  - Recalcular custo para novo nível
- [ ] Sem método `remover` — vantagens não podem ser removidas
- [ ] `listar(Long fichaId)` → List<FichaVantagem>

### 4. DTOs
- [ ] `ComprarVantagemRequest` record: vantagemConfigId
- [ ] `FichaVantagemResponse` record: id, vantagemConfigId, nome, nivelAtual, nivelMaximo, custoTotal

### 5. Endpoints no FichaController
- [ ] `POST /api/fichas/{id}/vantagens` — comprar vantagem
- [ ] `PUT /api/fichas/{id}/vantagens/{vid}` — aumentar nível
- [ ] `GET /api/fichas/{id}/vantagens` — listar vantagens da ficha
- [ ] SEM DELETE (regra de negócio: nunca remove)

## Arquivos afetados
- `model/FichaVantagem.java` (NOVO)
- `repository/FichaVantagemRepository.java` (NOVO)
- `service/FichaVantagemService.java` (NOVO)
- `dto/request/ComprarVantagemRequest.java` (NOVO)
- `dto/response/FichaVantagemResponse.java` (NOVO)
- `controller/FichaController.java` (MODIFICAR — adicionar endpoints de vantagem)

## Verificações de aceitação
- [ ] POST comprar vantagem sem pré-requisitos → 201
- [ ] POST comprar vantagem com pré-requisitos não atendidos → 400 com mensagem descritiva
- [ ] POST comprar vantagem já comprada → 409
- [ ] PUT aumentar nível além do máximo → 400
- [ ] GET listar retorna vantagens com custos calculados
- [ ] `./mvnw test` passa
