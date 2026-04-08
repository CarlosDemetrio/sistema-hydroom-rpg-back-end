# CSVs de Revisão — GameDefaultProvider

## Objetivo

Estes arquivos CSV servem como **fonte editável** para revisar e completar os dados do `DefaultGameConfigProviderImpl`.
Após preencher e validar, o conteúdo será convertido de volta para código Java no provider.

---

## Formato CSV

| convenção | significado |
|---|---|
| Separador: `;` | evita conflito com fórmulas matemáticas que usam vírgulas |
| Campo vazio | campo opcional não preenchido (será `null` na entidade) |
| `TODO` | campo **obrigatório** que ainda precisa ser preenchido |
| Linhas começando com `#` | comentários — ignorados pelo parser |
| `true` / `false` | booleanos |
| Referência por nome | chaves estrangeiras são resolvidas pelo nome exato (case-sensitive) |

---

## Ordem de preenchimento (respeitar dependências)

```
01-tipo-aptidao.csv                   ← base, sem deps
02-genero-config.csv                  ← base
03-indole-config.csv                  ← base
04-presenca-config.csv                ← base
05-dado-prospeccao-config.csv         ← base
06-categoria-vantagem.csv             ← base (necessário ANTES de 17)
07-raridade-item-config.csv           ← base (necessário ANTES de 18)
08-atributo-config.csv                ← base (necessário antes de 09, 17b, 16b, 18b)
09-bonus-config.csv                   ← refs abreviações de 08 nas fórmulas
10-membro-corpo-config.csv            ← base (necessário ANTES de 17b)
11-nivel-config.csv                   ← base
12-pontos-vantagem-config.csv         ← base
13-aptidao-config.csv                 ← refs tipo_aptidao_nome → 01
14-tipo-item-config.csv               ← enums apenas, sem deps de outros CSVs
15-classe-personagem.csv              ← base
  15b-classe-bonus.csv                ← refs classe_nome → 15, bonus_nome → 09
  15c-classe-aptidao-bonus.csv        ← refs classe_nome → 15, aptidao_nome → 13
  15d-classe-pontos-config.csv        ← refs classe_nome → 15
16-raca.csv                           ← base
  16b-raca-bonus-atributo.csv         ← refs raca_nome → 16, atributo_sigla → 08
  16c-raca-classe-permitida.csv       ← refs raca_nome → 16, classe_nome → 15
  16d-raca-pontos-config.csv          ← refs raca_nome → 16
17-vantagem-config.csv                ← refs categoria_nome → 06
  17b-vantagem-efeito.csv             ← refs vantagem_nome → 17, atributo_sigla → 08,
                                          aptidao_nome → 13, bonus_nome → 09, membro_nome → 10
  17c-vantagem-prerequisito.csv       ← refs vantagem_nome + requisito_nome → 17
15e-classe-vantagem-predefinida.csv   ← refs classe_nome → 15, vantagem_nome → 17
16e-raca-vantagem-predefinida.csv     ← refs raca_nome → 16, vantagem_nome → 17
18-item-config.csv                    ← refs raridade_nome → 07, tipo_nome → 14
  18b-item-efeito.csv                 ← refs item_nome → 18, bonus_nome → 09, atributo_sigla → 08
  18c-item-requisito.csv              ← refs item_nome → 18
```

---

## Problemas conhecidos (a corrigir ao preencher)

| arquivo | problema |
|---|---|
| `15-*`, `16-*` | sub-entidades todas vazias (TODO PA-015-01 / PA-015-02) |
| `17-vantagem-config.csv` | `formula_custo` usa `custo_base` que não é variável válida no FormulaEvaluatorService |
| `17-vantagem-config.csv` | `categoria_nome` não é atualmente setada no initializer — precisará de fix no código |
| `17b-vantagem-efeito.csv` | **nenhuma vantagem tem efeito definido** — preencher completamente |
| `07-raridade`, `14-tipo-item`, `17-vantagem`, `18-item` | acentuação faltando em vários nomes |
| `18-item-config.csv` | nenhum item tem `descricao` |
| `18c-item-requisito.csv` | nenhum item tem requisitos formais |
