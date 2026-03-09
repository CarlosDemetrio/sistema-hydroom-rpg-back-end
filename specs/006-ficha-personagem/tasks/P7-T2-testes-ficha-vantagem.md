# P7-T2 — Testes de Integração: FichaVantagem

**Fase:** 7 — Testes
**Complexidade:** 🟡 Média
**Depende de:** P7-T1

## Objetivo

Cobertura do fluxo de compra de vantagens incluindo validação de pré-requisitos.

## Checklist

### Cenários em FichaVantagemServiceIntegrationTest

- [ ] deveComprarVantagemSemPreRequisitos()
- [ ] deveComprarVantagemComPreRequisitosAtendidos()
- [ ] deveRejeitarCompraComPreRequisitosNaoAtendidos() — 400 com lista de pré-requisitos faltando
- [ ] deveRejeitarCompraDeVantagemJaComprada() — 409
- [ ] deveAumentarNivelDeVantagem()
- [ ] deveRejeitarAumentoAlemDoNivelMaximo() — 400
- [ ] naoDevePermitirRemocaoDeVantagem() — sem endpoint DELETE

## Arquivos afetados
- `test/.../FichaVantagemServiceIntegrationTest.java` (NOVO)

## Verificações de aceitação
- [ ] Todos os testes passam
- [ ] `./mvnw test` passa
