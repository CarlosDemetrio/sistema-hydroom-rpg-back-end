# P7-T1 — Testes de Integração: Duplicação de Jogo

**Fase:** 7 — Testes
**Complexidade:** 🟡 Média
**Depende de:** P4-T1

## Checklist

### JogoDuplicacaoServiceIntegrationTest

- [ ] deveDuplicarJogoComTodasAsConfigs()
  - Criar jogo com 3 atributos + 2 bônus → duplicar → verificar que novo jogo tem os mesmos
- [ ] devePreservarOrdemExibicaoNaDuplicacao()
- [ ] deveGerarNovosMestreNoJogoDuplicado()
- [ ] naoDeveCopiarSubEntidadesDeRelacionamento()
- [ ] devePermitirAlterarConfigDuplicadaSemAfetar Original()
- [ ] deveRejeitarDuplicacaoPorNaoMestre() — 403

## Arquivos afetados
- `test/.../JogoDuplicacaoServiceIntegrationTest.java` (NOVO)

## Verificações de aceitação
- [ ] Todos os testes passam
- [ ] `./mvnw test` passa
