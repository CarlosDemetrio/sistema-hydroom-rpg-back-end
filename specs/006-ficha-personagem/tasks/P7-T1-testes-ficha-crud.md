# P7-T1 — Testes de Integração: Ficha CRUD

**Fase:** 7 — Testes
**Complexidade:** 🟡 Média
**Depende de:** P6-T1

## Objetivo

Cobertura completa do CRUD de Ficha e inicialização automática dos sub-registros.

## Checklist

### Cenários em FichaServiceIntegrationTest

- [ ] deveCriarFichaComSubRegistrosInicializados()
  - Verificar que FichaAtributo, FichaAptidao, FichaBonus, FichaVida, FichaEssencia, FichaAmeaca, FichaProspeccao são criados
  - Verificar count de sub-registros = qtd de configs do jogo
- [ ] deveBuscarFichaComTodosSubComponentes()
- [ ] deveListarFichasDoJogador()
- [ ] deveMestreVerTodasFichasDoJogo()
- [ ] deveAtualizarCamposIdentidadeDaFicha()
- [ ] deveDeletarFichaComSoftDelete()
- [ ] deveRejeitarCriacaoSemParticipacaoAprovada()
- [ ] deveRejeitarConfigDeOutroJogo() — raça/classe de jogo diferente → 400

## Arquivos afetados
- `test/.../FichaServiceIntegrationTest.java` (NOVO)

## Verificações de aceitação
- [ ] Todos os testes passam
- [ ] `./mvnw test` passa
