# Spec 016 -- Regras de Negocio do Sistema de Itens

> Referencia canonica para todas as regras de negocio do sistema de itens/equipamentos.
> Cada regra e referenciada nos contratos de API como `RN-ITEM-XX`.
> Ultima atualizacao: 2026-04-04

---

## Regras de Restricao de Acesso

### RN-ITEM-01 -- Restricao de raridade para adicao de item pelo Jogador

Apenas o Mestre pode adicionar itens de raridade com `podeJogadorAdicionar = false` a uma ficha.
O Jogador so pode adicionar itens cuja raridade tenha `podeJogadorAdicionar = true` (tipicamente: Comum).
Tentativa de JOGADOR adicionar item de raridade restrita retorna **HTTP 403**.

- **Validacao**: server-side no `FichaItemService.adicionarItem()`
- **Campo de controle**: `RaridadeItemConfig.podeJogadorAdicionar`
- **Excecao**: Mestre ignora esta restricao (pode adicionar qualquer raridade)

### RN-ITEM-02 -- Jogador so opera na propria ficha

Jogador so pode adicionar/equipar/desequipar/remover itens da propria ficha (`ficha.jogadorId == usuarioLogado.id`).
Mestre pode operar em qualquer ficha do jogo (incluindo NPCs).
Tentativa de JOGADOR operar em ficha alheia retorna **HTTP 403**.

### RN-ITEM-03 -- Mestre pode remover qualquer item de qualquer ficha

O Mestre pode soft-deletar qualquer `FichaItem` de qualquer ficha do jogo.
Jogador pode remover apenas itens proprios que NAO sejam obrigatorios de classe (ver RN-ITEM-12).
Tentativa de JOGADOR remover item obrigatorio retorna **HTTP 403**.

---

## Regras de Equipar/Desequipar

### RN-ITEM-04 -- Bonus so ativo se equipado

`FichaItem` com `equipado = false` NAO gera bonus no `FichaCalculationService`.
Apenas itens com `equipado = true` sao processados para calculo de bonus.

- **Excecao**: itens de categoria CONSUMIVEL nunca sao "equipados" -- sao consumidos via endpoint dedicado (pos-MVP).

### RN-ITEM-05 -- Validacao de requisitos ao equipar

Ao equipar um item (`PATCH /equipar`), o sistema valida todos os `ItemRequisito` do `ItemConfig` associado:

| TipoRequisito | Validacao |
|---------------|-----------|
| `NIVEL` | `ficha.nivel >= valorMinimo` |
| `ATRIBUTO` | `fichaAtributo[alvo].total >= valorMinimo` |
| `BONUS` | `fichaBonus[alvo].total >= valorMinimo` |
| `APTIDAO` | `fichaAptidao[alvo].total >= valorMinimo` |
| `VANTAGEM` | ficha possui FichaVantagem com nome=alvo e nivel >= valorMinimo |
| `CLASSE` | `ficha.classe.nome == alvo` |
| `RACA` | `ficha.raca.nome == alvo` |

Se algum requisito nao for atendido: **HTTP 422** com descricao de qual requisito falhou.

### RN-ITEM-06 -- Mestre pode bypassar requisitos ao adicionar

O request `POST /fichas/{fichaId}/itens` aceita campo `forcarAdicao: true` (somente Mestre).
Com `forcarAdicao = true`, os requisitos NAO sao validados ao adicionar.
Os requisitos continuam sendo validados ao EQUIPAR (nao ha bypass de equipar).

### RN-ITEM-07 -- Item quebrado nao pode ser equipado

Item com `duracaoAtual == 0` esta "quebrado" e NAO pode ser equipado.
Tentativa retorna **HTTP 422** com mensagem "Item quebrado nao pode ser equipado".

### RN-ITEM-08 -- Durabilidade zero forca desequipamento

Quando `duracaoAtual` chega a 0 (via endpoint de durabilidade), o sistema automaticamente:
1. Seta `equipado = false`
2. Dispara recalculo da ficha (bonus do item removidos)

O item permanece no inventario como "quebrado" (nao e removido).

---

## Regras de Durabilidade

### RN-ITEM-09 -- Durabilidade decrementada pelo Mestre

No MVP, `duracaoAtual` e decrementado manualmente pelo Mestre via `POST /fichas/{fichaId}/itens/{itemId}/durabilidade`.
Request aceita `decremento` (positivo) ou `restaurar` (seta para duracaoPadrao do ItemConfig).

- `duracaoAtual` nunca fica negativo (minimo = 0)
- `duracaoAtual` nunca excede `duracaoPadrao` do ItemConfig ao restaurar
- Itens indestrutiveis (`duracaoPadrao == null`) nao aceitam operacao de durabilidade: **HTTP 422**

### RN-ITEM-10 -- Durabilidade inicial

Ao adicionar um `FichaItem` baseado em `ItemConfig`:
- `duracaoAtual` = `ItemConfig.duracaoPadrao` (copiado)
- Se `ItemConfig.duracaoPadrao == null`, `FichaItem.duracaoAtual = null` (indestrutivel)

---

## Regras de ItemConfig (Catalogo)

### RN-ITEM-11 -- ItemConfig pertence ao jogo

`ItemConfig`, `RaridadeItemConfig` e `TipoItemConfig` tem `jogo_id`.
Um item do Jogo A nao pode ser adicionado a uma ficha do Jogo B.
**Validacao server-side** em `FichaItemService.adicionarItem()`.

### RN-ITEM-12 -- Soft delete de ItemConfig nao afeta FichaItem existentes

Deletar um `ItemConfig` do catalogo (soft delete) NAO remove os `FichaItem` das fichas.
Os `FichaItem` que referenciavam o item deletado continuam existindo com dados preservados.
O item apenas deixa de aparecer no catalogo para novas adicoes.

### RN-ITEM-13 -- Unique constraint de nome por jogo

`ItemConfig`, `RaridadeItemConfig` e `TipoItemConfig` tem unique constraint `(jogo_id, nome)`.
Tentativa de criar duplicata retorna **HTTP 409**.

### RN-ITEM-14 -- Siglas de ItemEfeito NAO competem no namespace cross-entity

`ItemEfeito` usa FKs diretas para AtributoConfig, BonusConfig, AptidaoConfig.
NAO usa siglas (abreviacoes) como identificadores.
Portanto, `ItemEfeito` NAO compete no namespace cross-entity de siglas gerenciado pelo `SiglaValidationService`.

---

## Regras de ClasseEquipamentoInicial

### RN-ITEM-15 -- Equipamento inicial aplicado na criacao da ficha

Quando o wizard de criacao de ficha completa a escolha de classe:
- Itens com `obrigatorio = true` sao adicionados automaticamente como `FichaItem` com `origem = INICIAL_CLASSE`
- Itens com `obrigatorio = false` e mesmo `grupoEscolha` sao apresentados como opcoes (Jogador escolhe um do grupo)

### RN-ITEM-16 -- Item obrigatorio nao pode ser removido pelo Jogador

`FichaItem` com `origem = INICIAL_CLASSE` e cujo `ClasseEquipamentoInicial.obrigatorio = true` NAO pode ser removido pelo Jogador.
Apenas o Mestre pode remover itens obrigatorios de classe.
Tentativa de Jogador retorna **HTTP 403**.

### RN-ITEM-17 -- Grupo de escolha mutuamente exclusivo

Itens com mesmo `grupoEscolha` (int) dentro da mesma `ClasseEquipamentoInicial` sao mutuamente exclusivos.
O Jogador deve escolher exatamente um item de cada grupo.
`grupoEscolha = null` implica que o item e obrigatorio e concedido automaticamente.

---

## Regras de FichaItem

### RN-ITEM-18 -- Item customizado (sem ItemConfig)

Mestre pode criar `FichaItem` sem vincular a um `ItemConfig` do catalogo (endpoint separado: `POST /itens/customizado`).
Nesse caso, `nome`, `peso` e `raridadeId` sao obrigatorios no request.
No MVP: itens customizados sao apenas informativos (sem efeitos automaticos de `ItemEfeito`).

### RN-ITEM-19 -- Origem do FichaItem

Cada `FichaItem` tem uma `origem` (enum):
- `CATALOGO` -- adicionado manualmente do catalogo
- `CUSTOMIZADO` -- criado sem ItemConfig pelo Mestre
- `INICIAL_CLASSE` -- concedido pela ClasseEquipamentoInicial
- `INICIAL_RACA` -- concedido por equipamento de raca (pos-MVP)

### RN-ITEM-20 -- Quantidade e empilhamento

`FichaItem.quantidade` controla multiplas unidades do mesmo item.
No MVP: adicionar o mesmo `ItemConfig` duas vezes cria dois `FichaItem` distintos (sem auto-stack).

**Excecao -- municoes**: itens com `TipoItemConfig.categoria = CONSUMIVEL` e `subcategoria = MUNICAO` empilham automaticamente. Adicionar flecha ja existente na ficha incrementa `quantidade` em vez de criar novo registro.

### RN-ITEM-21 -- Peso e capacidade de carga

O peso total de todos os `FichaItem` (equipados e no inventario) e somado e comparado com a capacidade de carga.
No MVP: **apenas exibicao** (sem penalidade automatica de sobrecarga).
A capacidade de carga e derivada de `AtributoConfig.formulaImpeto` de FOR = `total * 3` kg.

---

## Regras de Calculo

### RN-ITEM-22 -- Campo `itens` nas entidades de ficha (SCHEMA-016-01)

As entidades `FichaAtributo`, `FichaBonus`, `FichaVida` e `FichaEssencia` precisam de campo `itens` (int, default 0).
Este campo e **zerado e recalculado** a cada chamada de `FichaCalculationService.recalcular()`.
Separa bonus de itens equipados de outras fontes (vantagens, classe, raca, gloria).

### RN-ITEM-23 -- Recalculo e idempotente

Recalcular bonus de itens duas vezes consecutivas produz o mesmo resultado.
O campo `itens` e zerado ANTES de somar os bonus de todos os `FichaItem` equipados.

### RN-ITEM-24 -- FichaAptidao.itens (dependente de PA-016-03)

Se confirmado pelo PO: `FichaAptidao` tambem recebe campo `itens` para bonus de aptidao via itens.
Aguardando decisao do PO (PA-016-03).

### RN-ITEM-25 -- ItemEfeito nao tem valorPorNivel

Diferentemente de `VantagemEfeito`, `ItemEfeito` usa `valorFixo` (int) e nao `valorPorNivel`.
Itens nao tem "nivel" -- o bonus e fixo.
Excecao: `FORMULA_CUSTOMIZADA` pode referenciar `nivel` do personagem como variavel na formula exp4j.

---

## Regras de Integridade Referencial

### RN-ITEM-26 -- Nao deletar RaridadeItemConfig em uso

Tentativa de deletar `RaridadeItemConfig` que esteja referenciada por algum `ItemConfig` ativo retorna **HTTP 409**.
O Mestre deve primeiro reclassificar ou deletar os itens que usam aquela raridade.

### RN-ITEM-27 -- Nao deletar TipoItemConfig em uso

Tentativa de deletar `TipoItemConfig` que esteja referenciada por algum `ItemConfig` ativo retorna **HTTP 409**.

### RN-ITEM-28 -- ItemEfeito com FK consistente

Ao criar `ItemEfeito`, as FKs (`atributoAlvoId`, `aptidaoAlvoId`, `bonusAlvoId`) devem referenciar configs do mesmo jogo que o `ItemConfig`.
Cross-jogo retorna **HTTP 422**.

---

*Produzido por: Tech Lead Backend | 2026-04-04*
*Baseado em: spec.md (Sec 4), decisoes do PO, padroes existentes do projeto*
