# P4-T1 — Testes de Integração do Fluxo de Participação

**Fase:** 4 — Testes
**Complexidade:** 🟡 Média
**Depende de:** P3-T1
**Bloqueia:** nada

## Objetivo

Garantir cobertura completa do fluxo de participação com testes de integração.

## Checklist

### Cenários a cobrir em JogoParticipanteServiceIntegrationTest

- [ ] devePermitirSolicitacaoDeEntrada()
- [ ] deveRejeitarSolicitacaoDuplicada()
- [ ] deveMestreAprovarSolicitacao()
- [ ] deveMestreRejeitarSolicitacao()
- [ ] deveMestreBanirParticipante()
- [ ] naoDevePermitirSolicitacaoDoProprioMestre()
- [ ] naoDevePermitirAprovacaoPorNaoMestre()
- [ ] deveListarTodosParticipantesSendoMestre()
- [ ] deveListarApenaAprovedosSendoJogador()

### Cenários a cobrir em ParticipanteSecurityServiceTest (unitário)

- [ ] devePermitirAcessoParaMestre()
- [ ] devePermitirAcessoParaParticipanteAprovado()
- [ ] deveNegarAcessoParaParticipantePendente()
- [ ] deveNegarAcessoParaUsuarioSemParticipacao()

## Arquivos afetados

- `test/.../JogoParticipanteServiceIntegrationTest.java` (NOVO)
- `test/.../ParticipanteSecurityServiceTest.java` (NOVO)

## Verificações de aceitação

- [ ] Todos os testes passam
- [ ] `./mvnw test` passa sem erros
