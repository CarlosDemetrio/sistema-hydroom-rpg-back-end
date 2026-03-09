# P7-T2 — Testes de Integração: Export/Import

**Fase:** 7 — Testes
**Complexidade:** 🟡 Média
**Depende de:** P5-T1

## Checklist

### ConfigExportImportServiceIntegrationTest

- [ ] deveExportarTodasAsConfigs()
  - Criar jogo com configs variadas → exportar → verificar JSON completo
- [ ] deveImportarConfigsEmJogoVazio()
  - Exportar de jogo A → importar em jogo B vazio → verificar configs iguais
- [ ] deveRejeitarImportComNomesDuplicados()
  - Jogo B já tem atributo "Força" → import tenta criar "Força" → 400 com lista de duplicatas
- [ ] deveImportarAtomica()
  - Import com 5 itens onde o 3º tem nome duplicado → rollback → jogo B não tem nenhum dos outros 4 também
- [ ] deveRejeitarExportPorNaoMestre() — 403

## Arquivos afetados
- `test/.../ConfigExportImportServiceIntegrationTest.java` (NOVO)

## Verificações de aceitação
- [ ] Todos os testes passam
- [ ] `./mvnw test` passa
