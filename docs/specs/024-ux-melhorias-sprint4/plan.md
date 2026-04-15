# Spec 024 — Plano de Implementacao: UX Melhorias Sprint 4

> Spec: `024-ux-melhorias-sprint4`
> Status: Pendente
> Dependencias: Spec 023 (concluido)
> Estimativa total: ~1–2 horas (apenas Frontend)

---

## 1. Visao Geral

Uma unica task frontend: adicionar o campo `tipoVantagem` (checkbox Insolitus) ao formulario de `VantagemConfig`.

```
T1 — Frontend
  vantagens-config.component.ts
  vantagem-config.model.ts (DTOs)
  vantagens-config.component.spec.ts (testes)
```

---

## 2. Estado Atual

- Backend: `tipoVantagem` suportado em Create/Update/Response. Nenhuma mudanca necessaria.
- Frontend: campo ausente no form, DTOs e tabela.
- `UX-NIVEL-MIN-PREREQ`: considerado concluido (implementado na Spec 023 FE).

---

## 3. T1 — tipoVantagem no formulario

**Escopo:**
1. `CreateVantagemDto` + `UpdateVantagemDto` — adicionar `tipoVantagem?`
2. `buildForm()` — adicionar campo `tipoVantagem: ['VANTAGEM']`
3. Signal `isInsolitus` + `effect` de sincronizacao (form + disable de formulaCusto)
4. Populate no `openDrawer()` para edit mode
5. Template: checkbox "Esta vantagem e Insolitus" apos campo Categoria
6. `filteredItems()` — adicionar campo virtual `tipoVantagemLabel`
7. `columns` — adicionar coluna "Tipo"
8. Testes: 8 cenarios (ver task T1)

**Estimativa:** 1–2 horas

---

## 4. Criterios de Pronto (Definition of Done)

- [ ] DTOs atualizados
- [ ] Checkbox no form com comportamento correto (disable/enable formulaCusto)
- [ ] Edicao pre-preenche checkbox
- [ ] Coluna "Tipo" na tabela
- [ ] Testes passando
- [ ] `npm test` verde (exceto falhas pre-existentes conhecidas)
- [ ] HANDOFF-SESSAO.md atualizado (UX-TIPO-VANTAGEM e UX-NIVEL-MIN-PREREQ marcados como CONCLUIDO)

---

*Produzido por: Business Analyst/PO | 2026-04-15*
