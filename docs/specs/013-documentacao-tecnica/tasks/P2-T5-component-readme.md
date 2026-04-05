# T5 — README por Componente Critico do Frontend

> Fase: Frontend | Dependencias: Nenhuma | Bloqueia: Nenhuma
> Estimativa: 2–3 horas

---

## Objetivo

Criar um `README.md` para cada componente critico do frontend. O README deve servir como guia rapido para desenvolvedores que precisam modificar ou estender o componente.

---

## Componentes Afetados

| Componente | Diretorio | Complexidade | Prioridade |
|-----------|-----------|-------------|-----------|
| `ficha-header` | `features/shared/components/ficha-header/` | Alta (barras, resumo, estado) | **P0** |
| `ficha-vantagens-tab` | `features/shared/components/ficha-vantagens-tab/` | Alta (pontos, categorias, pre-req) | **P0** |
| Wizard steps (1-6) | `features/jogador/pages/ficha-create/` | Alta (multi-step form) | **P0** |
| `level-up-dialog` | `features/shared/components/level-up-dialog/` | Media (distribuicao pontos) | **P1** |
| `formula-editor` | `shared/components/formula-editor/` | Media (exp4j preview) | **P1** |
| `npcs` (mestre) | `features/mestre/pages/npcs/` | Media (lista + detalhe NPC) | **P2** |

---

## Padrao de README

Cada README deve conter no maximo 50-80 linhas:

```markdown
# ComponentName

> Breve descricao do que o componente faz (1-2 linhas)

## Inputs/Outputs

| Nome | Tipo | Direcao | Descricao |
|------|------|---------|-----------|
| fichaId | number | input() | ID da ficha a exibir |
| onSave | EventEmitter | output() | Emitido apos save com sucesso |

## Estado Interno

- `fichaDetail` — signal derivado de fichaStore, contem dados completos
- `isEditing` — signal local, controla modo edicao

## Dependencias

- FichaBusinessService — calculos de pontos e validacao
- FichaApiService — chamadas HTTP
- PrimeNG: p-progressBar, p-card, p-dialog

## Regras de Negocio Neste Componente

1. Barras de vida/essencia: percentual = (atual / total) * 100
2. Pontos disponiveis: vem de FichaResumoResponse.pontosXxxDisponiveis
3. NPC: se ficha.isNpc, esconde botao de level up

## Bugs Conhecidos

- [BUG-001] Barras hardcoded em 100% (corrigido em T-QW Bug 1)
```

---

## O que NAO incluir nos READMEs

- Documentacao de API (coberto por TSDoc em T4)
- Instrucoes de setup/build (coberto pelo README raiz)
- Screenshots (mudam frequentemente)
- Codigo de exemplo extenso (o codigo-fonte e a referencia)

---

## Criterios de Aceitacao

- [ ] `ficha-header/README.md`: inputs, estado interno, regras de barras
- [ ] `ficha-vantagens-tab/README.md`: inputs, logica de pontos e categorias
- [ ] Wizard: 1 README geral cobrindo o fluxo de 6 passos + 1 README por step complexo
- [ ] `level-up-dialog/README.md`: regras de distribuicao de pontos
- [ ] `formula-editor/README.md`: variaveis disponiveis, integracao com exp4j
- [ ] Todos os READMEs seguem o padrao definido (max 80 linhas)
- [ ] Build frontend passa (`ng build`)
