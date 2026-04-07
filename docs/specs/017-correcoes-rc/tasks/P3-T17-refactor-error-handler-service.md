# T17 — Frontend: Refactor/remover `ErrorHandlerService`

> Fase: Frontend | Prioridade: P3 (POS-HOMOLOGACAO)
> Dependencias: T4 (interceptor refatorado) — consumidor principal removido
> Bloqueia: nenhuma
> Estimativa: 2h
> Agente sugerido: angular-tech-lead

---

## Contexto

`services/error-handler.service.ts:18-25` e um wrapper raso de `ToastService.error()`. Apos T4 remover o consumidor principal, este servico fica praticamente orfao. Duas estrategias:

### Opcao A — Remover totalmente
- Grep por todos os consumidores
- Substituir chamadas por `ToastService` direto
- Deletar o arquivo

### Opcao B — Evoluir para algo util
- Adicionar logica de severidade por status HTTP
- Integrar com logging/monitoring (Sentry, LogRocket)
- Centralizar formatacao de mensagens de erro backend

Decisao: **Opcao A** (simplicidade) no MVP. Se precisar de algo mais sofisticado, criar um `LoggingService` separado.

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `ficha-controlador-front-end/src/app/services/error-handler.service.ts` | DELETAR (apos substituicao dos consumidores) |
| Consumidores do `ErrorHandlerService` | ATUALIZAR para usar `ToastService` direto |
| `error-handler.service.spec.ts` | DELETAR |

---

## Passos

1. `grep -rn "errorHandler\|ErrorHandlerService" src/app --include="*.ts"`
2. Para cada consumidor, substituir `errorHandler.handleError(msg)` por `toastService.error(msg)`
3. Remover imports orfaos
4. Deletar o service e seu teste
5. Rodar suite completa

---

## Criterios de Aceite

- [ ] Grep por `ErrorHandlerService` retorna zero resultados
- [ ] Testes passam
- [ ] Build OK

---

## Referencias

- `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` § Recomendacoes Adicionais item 4 (linha 163)
