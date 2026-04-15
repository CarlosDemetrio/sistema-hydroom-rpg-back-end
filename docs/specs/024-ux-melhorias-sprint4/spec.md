# Spec 024 — UX Melhorias Sprint 4

> Spec: `024-ux-melhorias-sprint4`
> Epic: Sprint 4 P1 — UX fixes
> Status: Pendente
> Depende de: Spec 023 (pre-requisitos polimorficos — concluido)
> Bloqueia: nenhuma spec

---

## 1. Visao Geral do Negocio

**Problema resolvido:** Duas lacunas de UX identificadas pos-Sprint 4 Wave P0:

1. **UX-TIPO-VANTAGEM** — O formulario de criacao/edicao de `VantagemConfig` nao expoe o campo `tipoVantagem` (VANTAGEM | INSOLITUS). O backend ja suporta o campo em `CreateVantagemRequest`, `UpdateVantagemRequest` e `VantagemResponse`, mas o frontend nao tem o campo no form, nos DTOs nem na tabela. Resultado: todas as vantagens criadas via UI ficam com tipo `VANTAGEM`; nao e possivel criar uma `INSOLITUS` pela interface.

2. **UX-NIVEL-MIN-PREREQ** — Pre-requisito tipo VANTAGEM ja exibe `nivelMinimo` corretamente no chip e no input (implementado como parte da Spec 023 FE). Task considerada concluida.

**Objetivo da Spec 024:** Implementar UX-TIPO-VANTAGEM no frontend.

---

## 2. Atores Envolvidos

| Ator | Role | Acoes permitidas |
|------|------|-----------------|
| Mestre | MESTRE | Criar/editar vantagens marcando como Insolitus |

---

## 3. Regras de Negocio

- `tipoVantagem` tem dois valores: `VANTAGEM` (padrao, comprada com pontos) e `INSOLITUS` (concedida pelo Mestre gratuitamente)
- Vantagens `INSOLITUS` nao tem custo de pontos — `formulaCusto` deve ser desabilitado quando o tipo for INSOLITUS
- O campo `tipoVantagem` e enviado no create e no update; no update, o backend aceita mudanca de tipo
- Backend default: `VANTAGEM`

---

## 4. Criterios de Aceite

- [ ] Formulario de criacao exibe checkbox "Esta vantagem e Insolitus"
- [ ] Ao marcar o checkbox: campo `formulaCusto` e desabilitado e limpo
- [ ] Ao desmarcar: `formulaCusto` volta a ser editavel
- [ ] Ao salvar, o DTO enviado ao backend inclui `tipoVantagem: 'INSOLITUS'` ou `'VANTAGEM'`
- [ ] Formulario de edicao pre-preenche o checkbox com base no `tipoVantagem` do item
- [ ] Tabela de vantagens exibe coluna "Tipo" com texto "Insolitus" para tipo INSOLITUS e "—" para VANTAGEM
- [ ] Testes Vitest cobrindo criacao INSOLITUS, edicao, e comportamento do formulaCusto

---

## 5. Decisoes de Produto

| Decisao | Escolha |
|---------|---------|
| UI do campo tipoVantagem | Checkbox (binario, INSOLITUS e excecao) |
| Exibicao na tabela | Coluna "Tipo" com campo virtual `tipoVantagemLabel` |
| formulaCusto ao selecionar INSOLITUS | Desabilitado + valor limpo |

---

*Produzido por: Business Analyst/PO | 2026-04-15*
