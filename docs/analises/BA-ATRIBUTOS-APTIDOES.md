# BA-ATRIBUTOS-APTIDOES.md — Análise Completa: Atributos, Aptidões, Tipos de Aptidão e Bônus

> Documento de análise de negócio para as entidades `AtributoConfig`, `AptidaoConfig`, `TipoAptidao` e `BonusConfig`.
> Destina-se a guiar o desenvolvimento frontend, testes de integração e alinhamento de equipe.

---

## 1. Visão Geral

Atributos, Aptidões e Bônus formam o **núcleo mecânico** do sistema Klayrah. Eles são configurados pelo Mestre e, uma vez que fichas existam, tornam-se a base de praticamente todo cálculo do jogo.

- **AtributoConfig** — características fundamentais do personagem (Força, Agilidade, Vigor, Sabedoria, Intuição, Inteligência, Astúcia). Tudo no jogo deriva deles. Cada atributo tem uma **fórmula de Ímpeto** que gera um efeito mecânico secundário calculado automaticamente.
- **TipoAptidao** — categorias que agrupam aptidões (ex: Física, Mental). Deve ser configurado antes das Aptidões.
- **AptidaoConfig** — habilidades treináveis do personagem (Furtividade, Diplomacia, Acrobacia). Cada aptidão pertence a um TipoAptidao.
- **BonusConfig** — valores derivados, calculados por fórmula a partir dos atributos (B.B.A, Bloqueio, Reflexo, B.B.M). São os "modificadores de combate" do sistema.

A relação de dependência é: `TipoAptidao → AptidaoConfig` e `AtributoConfig → BonusConfig (via fórmula)`.

---

## 2. Entidades e Relacionamentos

```
Jogo
 |
 +-- TipoAptidao (N)              ← categoria: "Física", "Mental"
 |         |
 |         +-- AptidaoConfig (N)  ← aptidão pertence a um tipo
 |
 +-- AtributoConfig (N)           ← Força, Agilidade, Vigor...
 |         abreviacao (única por jogo, cross-entity)
 |         formulaImpeto → avaliada pelo FormulaEvaluatorService
 |
 +-- BonusConfig (N)              ← B.B.A, Bloqueio, Reflexo...
           sigla (obrigatória, única por jogo, cross-entity)
           formulaBase → usa abreviações de AtributoConfig como variáveis

Ficha
 |
 +-- FichaAtributo (N)            ← base / nivel / outros / total / impeto
 +-- FichaAptidao (N)            ← base / sorte / classe / total
 +-- FichaBonus (N)              ← calculado + fontes (Vantagens, Classe, Itens, Glória, Outros)
```

### Cardinalidades

- `Jogo → AtributoConfig`: 1:N, unique `(jogo_id, nome)` e `(jogo_id, abreviacao)`
- `Jogo → TipoAptidao`: 1:N, unique `(jogo_id, nome)`
- `Jogo → AptidaoConfig`: 1:N, unique `(jogo_id, nome)`; `TipoAptidao` é FK obrigatória
- `Jogo → BonusConfig`: 1:N, unique `(jogo_id, nome)` e `(jogo_id, sigla)`
- `Ficha → FichaAtributo`: 1:N (um registro por atributo configurado no jogo)
- `Ficha → FichaBonus`: 1:N (um registro por bônus configurado no jogo)

---

## 3. Campos e Validações

### AtributoConfig

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK, gerado | 1 |
| `jogo` | FK → Jogo | Sim | NOT NULL | Jogo "Klayrah" |
| `nome` | String | Sim | max 50, unique por jogo | "Força" |
| `abreviacao` | String | Não | 2-5 chars, unique cross-entity por jogo | "FOR" |
| `descricao` | String | Não | max 500 | "Mede a força física bruta do personagem" |
| `formulaImpeto` | String | Não | max 100, validada por exp4j | "total * 3" |
| `descricaoImpeto` | String | Não | max 200 | "Capacidade de carga em kg" |
| `valorMinimo` | Integer | Não | default 0 | 0 |
| `valorMaximo` | Integer | Não | default 999 | 999 |
| `ordemExibicao` | Integer | Não | default 0 | 1 |
| `deletedAt` | LocalDateTime | — | soft delete | null = ativo |

**Regras dos campos:**
- `abreviacao`: quando preenchida, deve ser única no escopo do jogo inteiro (não apenas entre atributos, mas cruzado com BonusConfig.sigla e VantagemConfig.sigla). Validada pelo `SiglaValidationService`.
- `formulaImpeto`: variável disponível é `total` (Total do atributo na ficha). Exemplo: `"total / 10"` calcula 1 ponto de Ímpeto a cada 10 de atributo.

### TipoAptidao

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK, gerado | 1 |
| `jogo` | FK → Jogo | Sim | NOT NULL | — |
| `nome` | String | Sim | max 50, unique por jogo | "Física" |
| `descricao` | String | Não | max 500 | "Aptidões que envolvem o corpo e movimento" |
| `ordemExibicao` | Integer | Não | default 0 | 0 |

### AptidaoConfig

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK, gerado | 5 |
| `jogo` | FK → Jogo | Sim | NOT NULL | — |
| `tipoAptidao` | FK → TipoAptidao | Sim | NOT NULL | TipoAptidao "Física" |
| `nome` | String | Sim | max 50, unique por jogo | "Furtividade" |
| `descricao` | String | Não | max 500 | "Capacidade de mover-se sem ser notado" |
| `ordemExibicao` | Integer | Não | default 0 | 8 |

### BonusConfig

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK, gerado | 1 |
| `jogo` | FK → Jogo | Sim | NOT NULL | — |
| `nome` | String | Sim | max 50, unique por jogo | "B.B.A" |
| `sigla` | String | Sim | 2-5 chars, unique cross-entity por jogo | "BBA" |
| `descricao` | String | Não | max 500 | "Bônus Base de Ataque" |
| `formulaBase` | String | Não | max 200, validada por exp4j | "FLOOR((FOR + AGI) / 3)" |
| `ordemExibicao` | Integer | Não | default 0 | 0 |

**Regras do campo `formulaBase`:** variáveis permitidas são as abreviações dos `AtributoConfig` do jogo (ex: `FOR`, `AGI`, `VIG`). Qualquer variável não reconhecida gera erro 422. A fórmula é avaliada pelo `FormulaEvaluatorService` com os valores de atributo da ficha.

---

## 4. Regras de Negócio Críticas

1. **Sigla/abreviação única por jogo, cross-entity**: se `FOR` é abreviação de AtributoConfig "Força", nenhuma outra entidade do mesmo jogo pode usar `FOR` como sigla — incluindo BonusConfig, VantagemConfig, etc. Violação: HTTP 409 Conflict.

2. **TipoAptidao deve existir antes da AptidaoConfig**: não é possível criar uma Aptidão sem um Tipo de Aptidão cadastrado. A ordem de configuração importa.

3. **Fórmulas referenciando siglas**: a `formulaImpeto` de um AtributoConfig usa apenas `total` como variável. A `formulaBase` de um BonusConfig usa abreviações de AtributoConfig — portanto, remover um AtributoConfig cujas abreviações são usadas em fórmulas de BonusConfig quebraria os cálculos. Validar referências antes de excluir.

4. **Deleção não deve quebrar fórmulas**: ao deletar (soft delete) um AtributoConfig ou TipoAptidao, verificar se existem fichas ativas ou fórmulas de BonusConfig dependentes.

5. **Nomes são únicos por jogo (case-insensitive)**: "Força" e "força" conflitam dentro do mesmo jogo. Erro: HTTP 409.

6. **Limitador de atributo (NivelConfig) se aplica ao Total**: o limitador definido em `NivelConfig.limitadorAtributo` impede que o Total (Base + Nível + Outros) de qualquer atributo ultrapasse o valor configurado para o nível atual do personagem.

7. **AtributoConfig com fichas existentes**: alterar `valorMinimo`/`valorMaximo` ou `formulaImpeto` pode invalidar dados já salvos. O backend não bloqueia, mas a UX deve avisar o Mestre.

---

## 5. Fluxo do Mestre

### Sequência correta de configuração:

```
1. Criar TiposAptidao (Física, Mental)
2. Criar AtributosConfig (Força, Agilidade, Vigor, Sabedoria, Intuição, Inteligência, Astúcia)
   → Definir abreviação (FOR, AGI, VIG, SAB, INT, INTU, AST)
   → Definir formulaImpeto e descricaoImpeto
3. Criar BonusConfig (B.B.A, Bloqueio, Reflexo, B.B.M, Percepção, Raciocínio)
   → Usar abreviações de Atributos na formulaBase
4. Criar AptidoesConfig (Acrobacia, Furtividade, Diplomacia...)
   → Vincular ao TipoAptidao correto
5. Reordenar (drag-and-drop) para exibição na ficha
```

O template Klayrah Padrão já aplica as 7+2+24+6 configurações ao criar o jogo — o Mestre pode ajustar, não criar do zero.

---

## 6. Impacto na Ficha

**AtributoConfig → FichaAtributo:**
- Cada atributo gera um registro `FichaAtributo` na ficha com campos: `base`, `nivel` (pontos distribuídos ao subir de nível), `outros` (bônus diversos), `total` (soma automática)
- O `impeto` é calculado automaticamente: `FormulaEvaluatorService.calcularImpeto(formulaImpeto, total)`
- Exemplo: Vigor Total = 30, formulaImpeto = `"total / 10"` → Ímpeto do Vigor = 3 (que se torna a RD do personagem)

**AptidaoConfig → FichaAptidao:**
- Cada aptidão gera um `FichaAptidao` com campos: `base`, `sorte` (bônus de sorte), `classe` (bônus da classe), `total` (soma)
- O bônus de "Vantagens" em aptidões vem de `VantagemEfeito` do tipo `BONUS_APTIDAO`

**BonusConfig → FichaBonus:**
- Cada bônus gera um `FichaBonus` com 5 fontes de valor: `vantagens`, `classe`, `itens`, `gloria`, `outros`
- O valor base calculado pela fórmula é somado às 5 fontes para o total final
- Exibido na ficha como "B.B.A: 12 (Base 8 | Van 3 | Classe 1 | Itens 0 | Glória 0 | Outros 0)"

---

## 7. Design de Tela (UX)

### AtributoConfig

**Componente recomendado:** Tabela com formulário inline ou drawer lateral. Drag-and-drop para reordenar.

**Formulário de criação/edição:**
```
[Nome*]         [Abreviação]    (2-5 chars, verificação async de unicidade)
[Valor Mínimo]  [Valor Máximo]
[Fórmula de Ímpeto]     [Validar fórmula] → botão que chama endpoint de validação
[Descrição do Ímpeto]   (texto livre explicando o que o ímpeto significa)
[Descrição]
[Ordem de Exibição]
```

**Validações visuais:**
- Abreviação: indicador de "Disponível" / "Já em uso" (verificação async após digitar)
- Fórmula de Ímpeto: botão "Testar" que valida e mostra resultado com `total = 10`
- Valor mínimo deve ser <= valor máximo

**States:**
- Vazio: "Nenhum atributo configurado. O template Klayrah já inclui 7 atributos padrão."
- Carregando: skeleton loader na tabela
- Erro: banner vermelho com mensagem da API
- Sucesso: toast "Atributo salvo com sucesso"

**Caso especial:** aviso quando o Mestre tenta excluir um atributo cuja abreviação é referenciada em fórmulas de BonusConfig.

### TipoAptidao

**Componente recomendado:** Tabela simples (apenas nome e descrição). Raramente alterado após configuração inicial.

**Formulário:** `[Nome*]` + `[Descrição]` + `[Ordem]`. Inline na tabela (sem drawer necessário).

**Aviso de dependência:** ao deletar, mostrar quantas aptidões dependem deste tipo.

### AptidaoConfig

**Componente recomendado:** Tabela com filtro por TipoAptidao. Grouping visual por tipo (linha de cabeçalho separando "Físicas" das "Mentais").

**Formulário de criação/edição:**
```
[TipoAptidao*]  ← dropdown com os tipos cadastrados
[Nome*]
[Descrição]
[Ordem de Exibição]
```

**States:**
- Vazio com TipoAptidao existente: "Nenhuma aptidão criada. Adicione aptidões para este tipo."
- Aviso quando não existe nenhum TipoAptidao: "Crie um Tipo de Aptidão antes de adicionar aptidões."

### BonusConfig

**Componente recomendado:** Tabela com coluna de fórmula truncada + ícone de "ver fórmula".

**Formulário de criação/edição:**
```
[Nome*]     [Sigla*]     (2-5 chars, verificação async de unicidade cross-entity)
[Fórmula Base]  [Validar / Testar fórmula]
  → Autocomplete de variáveis disponíveis: lista as abreviações dos AtributoConfig do jogo
[Descrição]
[Ordem de Exibição]
```

**Editor de fórmula:** componente `EditorFormulaComponent` (listado no UX Backlog como P1). Deve mostrar autocomplete das variáveis disponíveis (abreviações dos atributos configurados) e calcular preview com valores de exemplo.

**Validações visuais:**
- Sigla: indicador "Disponível" / "Já em uso"
- Fórmula: resultado calculado com valores de exemplo (ex: FOR=10, AGI=8) exibido em tempo real

---

## 8. Contrato de API

### AtributoConfig

| Método | Path | Roles | Descrição |
|---|---|---|---|
| GET | `/api/v1/configuracoes/atributos?jogoId={id}` | MESTRE, JOGADOR | Listar atributos do jogo |
| GET | `/api/v1/configuracoes/atributos/{id}` | MESTRE, JOGADOR | Buscar por ID |
| POST | `/api/v1/configuracoes/atributos` | MESTRE | Criar atributo |
| PUT | `/api/v1/configuracoes/atributos/{id}` | MESTRE | Atualizar atributo |
| DELETE | `/api/v1/configuracoes/atributos/{id}` | MESTRE | Soft delete |
| PUT | `/api/v1/configuracoes/atributos/reordenar?jogoId={id}` | MESTRE | Reordenar em lote |

**Response de AtributoConfig:**
```json
{
  "id": 1,
  "jogoId": 5,
  "nome": "Força",
  "abreviacao": "FOR",
  "descricao": "Potência física bruta do personagem",
  "formulaImpeto": "total * 3",
  "descricaoImpeto": "Capacidade de carga em kg",
  "valorMinimo": 0,
  "valorMaximo": 999,
  "ordemExibicao": 1,
  "dataCriacao": "2026-01-01T00:00:00",
  "dataUltimaAtualizacao": "2026-01-01T00:00:00"
}
```

### TipoAptidao

| Método | Path | Roles |
|---|---|---|
| GET | `/api/v1/configuracoes/tipos-aptidao?jogoId={id}` | MESTRE, JOGADOR |
| GET | `/api/v1/configuracoes/tipos-aptidao/{id}` | MESTRE, JOGADOR |
| POST | `/api/v1/configuracoes/tipos-aptidao` | MESTRE |
| PUT | `/api/v1/configuracoes/tipos-aptidao/{id}` | MESTRE |
| DELETE | `/api/v1/configuracoes/tipos-aptidao/{id}` | MESTRE |
| PUT | `/api/v1/configuracoes/tipos-aptidao/reordenar?jogoId={id}` | MESTRE |

### AptidaoConfig

| Método | Path | Roles |
|---|---|---|
| GET | `/api/v1/configuracoes/aptidoes?jogoId={id}[&nome=]` | MESTRE, JOGADOR |
| GET | `/api/v1/configuracoes/aptidoes/{id}` | MESTRE, JOGADOR |
| POST | `/api/v1/configuracoes/aptidoes` | MESTRE |
| PUT | `/api/v1/configuracoes/aptidoes/{id}` | MESTRE |
| DELETE | `/api/v1/configuracoes/aptidoes/{id}` | MESTRE |
| PUT | `/api/v1/configuracoes/aptidoes/reordenar?jogoId={id}` | MESTRE |

### BonusConfig

| Método | Path | Roles |
|---|---|---|
| GET | `/api/v1/configuracoes/bonus?jogoId={id}[&nome=]` | MESTRE, JOGADOR |
| GET | `/api/v1/configuracoes/bonus/{id}` | MESTRE, JOGADOR |
| POST | `/api/v1/configuracoes/bonus` | MESTRE |
| PUT | `/api/v1/configuracoes/bonus/{id}` | MESTRE |
| DELETE | `/api/v1/configuracoes/bonus/{id}` | MESTRE |
| PUT | `/api/v1/configuracoes/bonus/reordenar?jogoId={id}` | MESTRE |

---

## 9. O Que Já Existe

### Backend
- `AtributoConfig`, `AptidaoConfig`, `TipoAptidao`, `BonusConfig`: entidades JPA completas com todas as constraints
- Controllers, Services, Repositories e Mappers para os 4 tipos
- `SiglaValidationService`: valida unicidade cross-entity de abreviações/siglas por jogo
- `FormulaEvaluatorService`: valida e avalia `formulaImpeto` e `formulaBase`
- Testes de integração via `BaseConfiguracaoServiceIntegrationTest`
- `FichaAtributo`, `FichaAptidao`, `FichaBonus`: entidades de instância na ficha (criadas mas aguardando Spec 006/007)

### Frontend
- Componentes existentes em `/mestre/config/configs/`:
  - `atributos-config/` — componente funcional (CRUD básico implementado)
  - `aptidoes-config/` — componente funcional
  - `tipos-aptidao-config/` — componente funcional
  - `bonus-config/` — componente funcional
- Modelos TypeScript: `AtributoConfig`, `AptidaoConfig`, `BonusConfig` em `config.models.ts` e arquivos dedicados
- `BaseConfigTableComponent` reutilizável para tabela/CRUD

---

## 10. O Que Falta

### Backend
- Validação de referências antes de excluir: bloquear soft delete de AtributoConfig cuja abreviação é usada em fórmulas de BonusConfig ativos
- Endpoint de validação de fórmula ao vivo (para o frontend testar antes de salvar)
- Lógica de recálculo dos FichaBonus e FichaAtributo quando configurações são alteradas (aguarda Spec 007)

### Frontend
- **Validação de sigla única assíncrona**: campo de abreviação/sigla sem verificação em tempo real (UX Backlog item 7)
- **Editor de fórmulas**: sem autocomplete de variáveis nem preview de resultado (UX Backlog item 6 — P1)
- **Agrupamento visual de aptidões por tipo**: lista atual é plana, sem separação visual
- **Aviso de dependência na deleção**: sem modal de confirmação mostrando dependentes
- **Filtro por tipo de aptidão**: sem dropdown de filtro na lista de aptidões
- Campo `descricaoImpeto` provavelmente ausente do formulário frontend (modelo existe no TypeScript mas verificar implementação do formulário)
- Campo `permitirRenascimento` não confirmado no formulário de NivelConfig (ver BA-NIVEIS-PROGRESSAO)
