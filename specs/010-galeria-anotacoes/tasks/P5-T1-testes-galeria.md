# P5-T1 — Testes de Integração: Galeria de Imagens

**Fase:** 5 — Testes
**Complexidade:** 🟡 Média
**Depende de:** P2-T1

## Objetivo

Testes de integração com ImageStorageService mockado.

## Checklist

### FichaImagemServiceIntegrationTest

- [ ] Usar `@MockBean ImageStorageService imageStorageService` (evitar filesystem real nos testes)
- [ ] Setup: `when(imageStorageService.store(any(), any())).thenReturn("http://fake-url/image.jpg")`

### Cenários

- [ ] deveFazerUploadDeImagemValida() — MockMultipartFile com image/jpeg → 201
- [ ] deveRejeitarUploadComTipoInvalido() — MockMultipartFile com application/pdf → 400
- [ ] deveListarImagensOrdenadasPorOrdem()
- [ ] deveDefinirAvatar() — verifica que anterior perde avatar e nova ganha
- [ ] deveDeletarImagem() — verifica soft delete + delete do storage chamado

## Arquivos afetados

- `test/.../FichaImagemServiceIntegrationTest.java` (NOVO)

## Verificações de aceitação

- [ ] Todos os testes passam sem acesso ao filesystem real
- [ ] `./mvnw test` passa
