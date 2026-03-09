# P3-T1 — FichaValidationService

**Fase:** 3 — Validações
**Complexidade:** 🟡 Média
**Depende de:** P2-T1
**Bloqueia:** nada

## Objetivo

Centralizar validações de negócio da Ficha antes de persistir.

## Checklist

### 1. FichaValidationService

- [ ] `validarPontosAtributo(Ficha ficha, List<FichaAtributo> atributos, NivelConfig nivel)`:
  - Soma de (base+nivel) de todos os atributos ≤ nivel.pontosAtributo
  - Lança BusinessException: "Pontos de atributo excedidos: distribuídos X, disponíveis Y"

- [ ] `validarLimitadorAtributo(List<FichaAtributo> atributos, NivelConfig nivel)`:
  - Para cada atributo: (base+nivel) ≤ nivel.limitadorAtributo
  - Lança BusinessException listando atributos que excedem o limite

- [ ] `validarPontosAptidao(Ficha ficha, List<FichaAptidao> aptidoes, NivelConfig nivel)`:
  - Soma de base de todas as aptidões ≤ nivel.pontosAptidao

- [ ] `validarClassePermitidaPorRaca(Ficha ficha)`:
  - Se ficha.raca != null e ficha.classe != null
  - Verificar que existe RacaClassePermitida para (raca, classe) — ou que a tabela está vazia para aquela raça (sem restrições)
  - Lança BusinessException: "Classe {X} não permitida para a raça {Y}"

- [ ] `validarPreRequisitosVantagens(Ficha ficha, List<FichaVantagem> vantagens)`:
  - Para cada FichaVantagem: verificar que todos os VantagemPreRequisito têm FichaVantagem correspondente com nivelAtual >= nivelMinimo

- [ ] `validarTudo(Ficha ficha, ...)` — chama todos os métodos acima em sequência

### 2. Integrar no FichaService

- [ ] Chamar `fichaValidationService.validarTudo()` em FichaService.atualizar() ANTES dos cálculos

## Arquivos afetados
- `service/FichaValidationService.java` (NOVO)
- `service/FichaService.java` (MODIFICAR — adicionar validação)

## Verificações de aceitação
- [ ] Atualizar atributos acima do limite → 400 com mensagem clara
- [ ] Atributo acima do limitador → 400 com nome do atributo
- [ ] Classe não permitida pela raça → 400 com nomes de classe e raça
- [ ] `./mvnw test` passa
