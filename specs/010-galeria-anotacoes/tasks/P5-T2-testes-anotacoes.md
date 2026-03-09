# P5-T2 — Testes de Integração: Anotações

**Fase:** 5 — Testes
**Complexidade:** 🟡 Média
**Depende de:** P3-T1, P4-T1

## Checklist

### FichaAnotacaoServiceIntegrationTest

- [ ] deveCriarAnotacaoPublica()
- [ ] deveCriarAnotacaoPrivada()
- [ ] deveMestreVerApenasAnotacoesPublicas()
- [ ] deveJogadorDonoVerPublicasEPrivadas()
- [ ] deveMestreNaoVerAnotacaoPrivadaDetalhe() — 403
- [ ] deveOutroJogadorNaoVerAnotacoes() — 403

### AnotacaoMestreServiceIntegrationTest

- [ ] deveMestreCriarAnotacaoMestre()
- [ ] deveMestreListarAnotacoesMestre()
- [ ] deveJogadorNaoVerAnotacoesMestre() — 403
- [ ] deveMestreDeOutroJogoNaoVer() — 403

## Arquivos afetados

- `test/.../FichaAnotacaoServiceIntegrationTest.java` (NOVO)
- `test/.../AnotacaoMestreServiceIntegrationTest.java` (NOVO)

## Verificações de aceitação

- [ ] Todos os testes passam
- [ ] `./mvnw test` passa
