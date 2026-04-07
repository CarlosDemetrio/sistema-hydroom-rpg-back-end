# T20 — Frontend: `racas-config` loading states em chamadas auxiliares

> Fase: Frontend | Prioridade: P3 (POS-HOMOLOGACAO)
> Dependencias: nenhuma
> Bloqueia: nenhuma
> Estimativa: 1h
> Agente sugerido: angular-frontend-dev

---

## Contexto

`racas-config.component.ts:655-665` sobrescreve `ngOnInit` e chama `listAtributos`, `listClasses`, `listVantagens`. Se essas chamadas falharem silenciosamente, os dropdowns dentro do dialog aparecem vazios sem nenhuma mensagem de erro. O `items()` signal principal permanece vazio e o usuario so ve empty state generico.

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `features/mestre/pages/config/configs/racas-config/racas-config.component.ts:655-665` | Adicionar loading states e tratamento de erro explicito |

---

## Passos

1. Criar signals de loading separados:
   ```typescript
   loadingAtributos = signal(false);
   loadingClasses = signal(false);
   loadingVantagens = signal(false);
   erroAuxiliares = signal<string | null>(null);
   ```
2. No `ngOnInit`, marcar loading antes de cada chamada e resetar no `finalize`
3. No `error:` de cada subscribe, setar `erroAuxiliares()` com mensagem
4. No template, mostrar aviso quando `erroAuxiliares()` nao for null
5. Desabilitar botao "Novo" enquanto `loadingAtributos() || loadingClasses() || loadingVantagens()`

---

## Criterios de Aceite

- [ ] Loading states indicados visualmente
- [ ] Erro em chamadas auxiliares mostra mensagem clara
- [ ] Botao Novo desabilitado durante loading
- [ ] Testes do component atualizados

---

## Referencias

- `docs/auditoria/AUDITORIA-UX-UI-2026-04-07.md` § P3 — Problema secundario (linha 160-168)
