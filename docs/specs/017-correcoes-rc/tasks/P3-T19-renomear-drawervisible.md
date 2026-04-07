# T19 — Frontend: Renomear `drawerVisible` → `dialogVisible`

> Fase: Frontend | Prioridade: P3 (POS-HOMOLOGACAO)
> Dependencias: nenhuma
> Bloqueia: nenhuma
> Estimativa: 2h
> Agente sugerido: angular-frontend-dev

---

## Contexto

A migracao drawer → dialog esta 100% concluida em produto, mas sobrou divida nominal:

1. **Signal `drawerVisible`** em todos os 13 configs + `npcs.component.ts` — controla abertura de `<p-dialog>`. Exemplo: `npcs.component.ts:316`, `racas-config.component.ts:577`
2. **`describe('drawer de criacao')`** em varios `.spec.ts` — exemplo: `npcs.component.spec.ts:200`, `pontos-vantagem-config.component.spec.ts:269`

Renomeacao e puramente cosmetica (sem impacto runtime) mas reduz confusao ao ler o codigo.

---

## Arquivos Envolvidos

Todos os 13 config components + `npcs.component.ts` + respectivos `.spec.ts`. Estimativa: ~20-30 arquivos.

---

## Passos

1. Grep por `drawerVisible`:
   ```
   grep -rn "drawerVisible" src/app --include="*.ts"
   ```
2. Usar `Edit replace_all` em cada arquivo para `drawerVisible` → `dialogVisible`
3. Grep por `describe('drawer`:
   ```
   grep -rn "describe(['\"].*drawer" src/app --include="*.spec.ts"
   ```
4. Renomear manualmente cada `describe` para `describe('dialog`
5. Rodar suite completa

---

## Criterios de Aceite

- [ ] Zero ocorrencias de `drawerVisible` em `.ts` de producao
- [ ] `describe('drawer...')` renomeado nos testes
- [ ] Suite de testes passa
- [ ] Build OK

---

## Notas

- Task tediosa mas mecanica. Pode ser feita em um unico commit grande (refactor/rename).
- Usar `Edit` com `replace_all: true` em cada arquivo para garantir consistencia.

---

## Referencias

- `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md` § P2 (linha 123-138)
