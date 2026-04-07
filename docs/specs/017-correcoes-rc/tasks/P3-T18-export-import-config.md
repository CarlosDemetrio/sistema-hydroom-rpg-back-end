# T18 — Frontend: Botoes Exportar/Importar `config-layout` funcionais

> Fase: Frontend | Prioridade: P3 (POS-HOMOLOGACAO)
> Dependencias: decisao do PO sobre contrato (PA-017-04)
> Bloqueia: nenhuma
> Estimativa: 3h
> Agente sugerido: angular-frontend-dev

---

## Contexto

`config-layout.component.ts:48-57` tem botoes `p-button label="Exportar"` e `p-button label="Importar"` SEM handlers. Os endpoints backend existem (`GET /jogos/{id}/config/export`, `POST /jogos/{id}/config/import`) e estao documentados em `jogos-api.service.ts:127-137`, mas nao estao conectados.

O PO pode confundir isso como "feature funcionando" — clicar nos botoes nao faz nada.

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `features/mestre/pages/config/config-layout.component.ts` | Adicionar handlers `exportarConfig()` e `importarConfig()` |
| `services/api/jogos-api.service.ts` | Confirmar API existe |

---

## Passos

1. Resolver PA-017-04 com PO: formato JSON, CSV ou ZIP? Quais campos?
2. Implementar `exportarConfig()`:
   - Chamar endpoint backend
   - Download do blob com `<a download>`
3. Implementar `importarConfig()`:
   - Usar `p-fileUpload` (ou `<input type="file">`)
   - Upload para endpoint backend
   - Recarregar as configuracoes apos sucesso
4. Feedback visual: toast de sucesso/erro
5. Testes

---

## Criterios de Aceite

- [ ] PA-017-04 resolvido
- [ ] Exportar gera download do arquivo
- [ ] Importar processa arquivo e recarrega configs
- [ ] Validacao de arquivo no frontend (tamanho, extensao)
- [ ] Testes do component

---

## Referencias

- `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md` § EXTRA-02 (linha 216-222)
- PA-017-04
