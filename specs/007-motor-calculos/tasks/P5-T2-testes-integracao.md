# P5-T2 — Testes de Integração: Fluxo Completo de Cálculos

**Fase:** 5 — Testes
**Complexidade:** 🟡 Média
**Depende de:** P5-T1

## Objetivo

Testes de integração que verificam que os cálculos são executados ao salvar a Ficha.

## Checklist

### FichaCalculationIntegrationTest

- [ ] `deveCalcularAtributosAoAtualizarFicha()` — atualizar base de atributo → verificar total e impeto no banco
- [ ] `deveCalcularBonusAoAtualizarAtributos()` — atualizar atributos → verificar base de bonus com formula
- [ ] `deveCalcularVidaAoAtualizar()` — verificar vidaTotal após update
- [ ] `deveAtualizarNivelAoMudarXp()` — xp → lookup NivelConfig → nivel atualizado
- [ ] `deveRetornarPreviewSemPersistir()` — POST /preview com novos valores → GET original não mudou
- [ ] `deveRejeitarAtributoAcimaDoLimitador()` — 400 com mensagem
- [ ] `deveRejeitarPontosDeAtributoExcedidos()` — 400 com mensagem

## Arquivos afetados
- `test/.../FichaCalculationIntegrationTest.java` (NOVO)

## Verificações de aceitação
- [ ] Todos os testes passam
- [ ] `./mvnw test` passa
