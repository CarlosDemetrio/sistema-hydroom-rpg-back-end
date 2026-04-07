# T22 — Frontend: Fix overlay clipping de selects/dropdowns dentro de dialogs

> Fase: Frontend | Prioridade: P0 (BLOQUEANTE PRE-RC)
> Dependencias: nenhuma
> Bloqueia: nenhuma
> Estimativa: 2h (config global + auditoria + testes)
> Agente sugerido: angular-frontend-dev

---

## Contexto

**Bug reportado pelo PO (2026-04-07, apos a auditoria UX):**

> "Os selects e coisas que abrem estao sendo cortados e nao listados ate o final."

**Sintoma observado:**
Quando o usuario abre um formulario dentro de um `p-dialog` e clica em qualquer
componente de overlay (`p-select`, `p-multiselect`, `p-autocomplete`, `p-datepicker`,
etc.), o painel/lista que abre e cortado pelo limite do dialog. Em listas longas
o usuario nao consegue rolar ate o final ou ate o item desejado, tornando o
formulario praticamente inutilizavel para selecoes nao triviais (raca, classe,
genero, atributos, etc.).

**Causa-raiz tecnica (problema classico do PrimeNG):**
Componentes de overlay do PrimeNG, por padrao, sao renderizados como filhos
diretos do componente onde estao declarados. Quando esse componente esta dentro
de um container com `overflow: hidden` (caso do `p-dialog`), o overlay e clipado
pelo limite do container.

**Suspeita de causa-raiz historica:**
Efeito colateral da migracao `p-drawer` → `p-dialog` que o PO realizou em
sessoes anteriores. O `p-drawer` tem `overflow` mais permissivo, entao o
problema nao se manifestava antes. Apos trocar para `p-dialog` (mais restritivo),
todos os formularios herdaram o clipping.

**Por que a auditoria UX nao capturou:**
A auditoria do `primeng-ux-architect` (2026-04-07) inspecionou apenas a estrutura
visual estatica das telas — nao abriu nenhum dialog para validar interacoes de
overlay. E um achado adicional, posterior a auditoria, reportado diretamente
pelo PO durante uso da aplicacao.

**Por que e P0:**
Sem este fix, o usuario nao consegue selecionar valores em qualquer dropdown
dentro de qualquer dialog do sistema. Isso afeta praticamente TODAS as telas de
formulario do Mestre (configuracoes, NPCs, classes, racas, vantagens) e do
Jogador (wizard de criacao de ficha, edicao). E arguably mais critico que o
PageHeader (P1) ou ate alguns P0 logicos como `hasBothRoles`, porque bloqueia
o fluxo principal de criacao/edicao de dados.

---

## Solucao Recomendada

A solucao oficial do PrimeNG 21 e fazer com que o overlay seja apendado ao
`<body>` em vez de ao container pai. Isso pode ser feito de duas formas:

### Estrategia A (RECOMENDADA) — Configuracao global em `app.config.ts`

Adicionar `overlayOptions: { appendTo: 'body' }` no `providePrimeNG(...)`. Isso
faz com que TODOS os overlays do PrimeNG (em todo o app) sejam renderizados
como filhos do `<body>`, escapando de qualquer container com `overflow: hidden`.

**Vantagens:**
- Um unico ponto de mudanca
- Resolve o problema em 100% das telas de uma vez
- Sem risco de esquecer um componente novo no futuro
- Padrao recomendado pela documentacao oficial PrimeNG 21

**Riscos:**
- Pode afetar componentes que dependem de posicionamento relativo ao container
  (raro, mas possivel)
- Z-index global pode precisar ajuste se houver dialogs aninhados

### Estrategia B (FALLBACK) — `[appendTo]="'body'"` por componente

Adicionar o atributo `[appendTo]="'body'"` em cada componente de overlay
individualmente. Mais verboso e propenso a esquecimento, mas com escopo mais
controlado.

**Quando usar:** caso a Estrategia A cause regressao em algum componente
especifico — usar B como fallback localizado para os componentes problematicos.

**Recomendacao do PM:** comecar pela Estrategia A. Se aparecer regressao em algum
componente especifico durante validacao manual, manter A globalmente e adicionar
override local nesse componente.

---

## Arquivos Envolvidos

### Arquivo de configuracao global

| Arquivo | Mudanca |
|---------|---------|
| `ficha-controlador-front-end/src/app/app.config.ts` | Adicionar `overlayOptions: { appendTo: 'body' }` no `providePrimeNG(...)` |

### Arquivos provavelmente afetados pelo bug (5-10 candidatos identificados)

A intersecao de "templates com overlay components" + "templates com `p-dialog`"
mostra que pelo menos os arquivos abaixo provavelmente disparam o bug. Quem
implementar deve fazer uma rodada rapida de validacao manual em cada um (abrir
o dialog, abrir o select, conferir se o painel aparece sem clipping):

| # | Arquivo | Telas afetadas |
|---|---------|----------------|
| 1 | `features/mestre/pages/config/configs/racas-config/racas-config.component.ts` | Form de Raca (atributos bonus + classes permitidas — multiselect dentro de dialog) |
| 2 | `features/mestre/pages/config/configs/classes-config/classes-config.component.ts` | Form de Classe (atributo principal, aptidoes — selects dentro de dialog) |
| 3 | `features/mestre/pages/config/configs/vantagens-config/vantagens-config.component.ts` | Form de Vantagem (categoria, pre-requisitos, formula — multiplos selects) |
| 4 | `features/mestre/pages/config/configs/vantagens-config/efeito-form/efeito-form.component.ts` | Sub-form de Efeito de Vantagem (tipo, alvo) |
| 5 | `features/mestre/pages/config/configs/aptidoes-config/aptidoes-config.component.ts` | Form de Aptidao (tipo de aptidao via select) |
| 6 | `features/mestre/pages/config/configs/prospeccao-config/prospeccao-config.component.ts` | Form de Dado de Prospeccao |
| 7 | `features/mestre/pages/npcs/npcs.component.ts` | Form de NPC (raca, classe, genero, indole, presenca — varios selects) |
| 8 | `features/jogador/pages/ficha-form/sections/vantagens-section.component.ts` | Wizard step de Vantagens (autocomplete + multiselect) |
| 9 | `features/jogador/pages/ficha-form/sections/equipamentos-section.component.ts` | Wizard step de Equipamentos (selects de equipamento) |
| 10 | `features/jogador/pages/ficha-form/steps/step-identificacao/step-identificacao.component.ts` | Wizard step de Identificacao (raca, classe, genero — selects) |
| 11 | `features/jogador/pages/ficha-detail/components/npc-visibilidade/npc-visibilidade.component.ts` | Dialog de visibilidade NPC (multiselect de jogadores) |
| 12 | `features/jogador/pages/ficha-detail/components/prospeccao/prospeccao.component.ts` | Dialog de prospeccao (selects) |

A lista nao e exaustiva — quem implementa deve confirmar via grep:

```bash
cd ficha-controlador-front-end
grep -rEl "p-(select|multiselect|autocomplete|datepicker|cascadeselect|dropdown)" src/app
grep -rEl "p-dialog" src/app
```

A intersecao desses dois conjuntos e o universo a auditar.

---

## Passos Sugeridos

### Passo 1 — Aplicar fix global em `app.config.ts`

Localizar o bloco `providePrimeNG(...)` (atualmente nas linhas ~31-42 do arquivo)
e adicionar `overlayOptions`:

**Antes:**
```typescript
providePrimeNG({
  theme: {
    preset: CustomPreset,
    options: {
      darkModeSelector: '.app-dark',
      cssLayer: false
    }
  },
  ripple: true,
  inputStyle: 'outlined'
}),
```

**Depois:**
```typescript
providePrimeNG({
  theme: {
    preset: CustomPreset,
    options: {
      darkModeSelector: '.app-dark',
      cssLayer: false
    }
  },
  ripple: true,
  inputStyle: 'outlined',
  overlayOptions: {
    appendTo: 'body'
  }
}),
```

Nota: a propriedade exata pode variar entre versoes do PrimeNG 21. Se
`overlayOptions.appendTo` nao funcionar, tentar `appendTo: 'body'` no topo do
objeto `providePrimeNG`. Validar com a documentacao MCP do PrimeNG via
`@primeng/mcp`.

### Passo 2 — Build + smoke test

```bash
cd ficha-controlador-front-end
npx ng build --configuration development
npx vitest run
```

Esperado: build OK, 848+ testes passando (sem regressao).

### Passo 3 — Validacao manual em pelo menos 5 telas representativas

Abrir cada uma e confirmar que o overlay nao e mais clipado:

1. **Mestre → Configuracoes → Racas**: criar nova raca, abrir multiselect "atributos bonus"
2. **Mestre → Configuracoes → Classes**: criar nova classe, abrir select "atributo principal"
3. **Mestre → Configuracoes → Vantagens**: criar nova vantagem, abrir select "categoria"
4. **Mestre → NPCs**: criar novo NPC, abrir todos os selects (raca, classe, genero, indole, presenca)
5. **Jogador → Wizard de ficha**: passo Identificacao, abrir os selects principais

Para cada um: o painel do overlay deve abrir com altura completa (ate o limite
da viewport, nao do dialog), permitindo scroll do conteudo todo.

### Passo 4 — Verificacao de regressao em selects FORA de dialogs

Validar que selects que NAO estao dentro de dialogs continuam funcionando
corretamente (nao foram afetados negativamente):

1. Header → switch de tema (se existir como select)
2. Header → seletor "Visualizar como" (Mestre/Jogador, quando aplicavel)
3. Lista de jogos (filtros, ordenacao se houver selects inline)

### Passo 5 — Verificacao de z-index com dialogs aninhados

Caso exista cenario de dialog aninhado (dialog dentro de dialog), validar que
o overlay do dialog mais interno aparece ACIMA do dialog externo. Caso negativo,
adicionar `style="z-index: 99999"` ou ajustar via CSS layer.

### Passo 6 — Caso a Estrategia A nao funcione globalmente

Se algum componente especifico apresentar regressao (overlay com posicionamento
errado, z-index abaixo do dialog, etc.), aplicar fallback local nesse componente
adicionando `[appendTo]="null"` (mantendo o append no parent) APENAS nele, e
manter a config global para os demais. Documentar a excecao no commit.

---

## Criterios de Aceite

- [ ] `app.config.ts` tem `overlayOptions: { appendTo: 'body' }` no `providePrimeNG`
- [ ] Build (`ng build`) compila sem erro
- [ ] `npx vitest run` continua 848+ testes passando (sem regressao)
- [ ] Validacao manual em pelo menos 5 telas representativas (lista acima): overlays abrem sem clipping
- [ ] Validacao manual em pelo menos 2 selects FORA de dialogs: continuam funcionando
- [ ] Caso haja regressao localizada, documentada com fallback no commit
- [ ] Commit com mensagem explicativa referenciando o bug do PO e a Spec 017 T22

---

## Notas

- Esta task NAO renomeia `drawerVisible` → `dialogVisible` (isso e P3 T19, divida nominal).
- Esta task NAO altera os templates dos componentes individuais — apenas o
  arquivo de configuracao global. Caso algum override local seja necessario,
  documentar no commit como excecao.
- O PageHeaderComponent (T8) NAO tem relacao com este bug. Sao melhorias
  independentes.
- Caso o agente identifique que `overlayOptions.appendTo` nao existe na API do
  PrimeNG 21 instalado no projeto, usar a abordagem B (atributo `[appendTo]="'body'"`
  por componente) — neste caso a estimativa sobe para ~4h e a task pode precisar
  ser dividida.
- Atencao especial ao **MCP PrimeNG** (`@primeng/mcp`) configurado em `.mcp.json`:
  consultar a documentacao oficial da versao 21.1.1 para a API correta de
  `providePrimeNG`.

---

## Referencias

- Bug reportado pelo PO em 2026-04-07 (apos auditoria UX, achado adicional)
- PrimeNG docs: https://primeng.org/configuration (secao `overlayOptions`)
- Suspeita historica: migracao `p-drawer` → `p-dialog` realizada em sessoes anteriores
- Auditoria UX (`AUDITORIA-UX-UI-2026-04-07.md`) NAO capturou este bug —
  inspecao apenas estatica, sem abrir dialogs
