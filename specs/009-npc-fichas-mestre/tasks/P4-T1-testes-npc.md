# P4-T1 — Testes de Integração: NPC CRUD

**Fase:** 4 — Testes
**Complexidade:** 🟢 Baixa
**Depende de:** P2-T1

## Checklist

### NpcServiceIntegrationTest

- [ ] deveCriarNpcComSubRegistrosInicializados()
- [ ] deveListarNpcsSeparadosDasFichasDeJogadores()
- [ ] deveMestreEditarNpc()
- [ ] deveMestreDeletarNpc()
- [ ] naoDevePermitirJogadorCriarNpc() — 403
- [ ] naoDevePermitirJogadorListarNpcs() — 403
- [ ] naoDeveNpcAparecerEmGetFichas()

## Arquivos afetados

- `test/.../NpcServiceIntegrationTest.java` (NOVO)

## Verificações de aceitação

- [ ] Todos os testes passam
- [ ] `./mvnw test` passa
