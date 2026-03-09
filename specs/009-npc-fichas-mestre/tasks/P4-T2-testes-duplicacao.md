# P4-T2 — Testes de Integração: Duplicação de Ficha

**Fase:** 4 — Testes
**Complexidade:** 🟢 Baixa
**Depende de:** P3-T1

## Checklist

### FichaDuplicacaoServiceIntegrationTest

- [ ] deveDuplicarFichaComTodosOsSubComponentes()
  - Verificar que nova ficha tem mesmos valores de atributos, bônus, etc.
- [ ] devePreservarValoresAtuaisNaDuplicacao() — valores calculados copiados, não reinicializados
- [ ] deveDuplicarComManterJogadorFalse() — resultado é NPC com jogadorId=null
- [ ] deveDuplicarComManterJogadorTrue() — resultado mantém jogadorId
- [ ] deveAlterarSubEntityDaCopiaSeMAfetar Original() — independência de dados
- [ ] deveDuplicarNpc() — duplicata de NPC é sempre NPC
- [ ] naoDevePermitirJogadorDuplicar() — 403

## Arquivos afetados

- `test/.../FichaDuplicacaoServiceIntegrationTest.java` (NOVO)

## Verificações de aceitação

- [ ] Todos os testes passam
- [ ] `./mvnw test` passa
