# Spec 016 — Sistema de Itens e Equipamentos

> Spec: `016-sistema-itens`
> Epic: EPIC 10 — Inventario e Equipamentos
> Status: PLANEJADO — spec+plan+tasks PRONTOS, implementacao PENDENTE
> Depende de: Spec 007 (FichaCalculationService corrigido), Spec 006 (Ficha com status funcional)
> Bloqueia: nada (feature aditiva)
> Prioridade: P1 — MVP

---

## 1. Visao Geral do Negocio

**Problema resolvido:** O sistema Klayrah nao possui modelagem de equipamentos. Atualmente, itens equipados sao registrados apenas como texto livre na ficha, sem calculo automatico de bonus, sem controle de durabilidade, sem catalogo reutilizavel entre personagens, e sem restricao de raridade por papel (Jogador vs Mestre). O resultado: o Mestre precisa gerenciar bonus de itens manualmente e o Jogador nao tem visibilidade clara dos efeitos dos seus equipamentos.

**Objetivo:** Criar um sistema completo de catalogo de itens (`ItemConfig`) configuravel pelo Mestre, com suporte a equipamento em fichas (`FichaItem`), calculo automatico de bonus de itens equipados no `FichaCalculationService`, e dataset inicial baseado no D&D 5e SRD adaptado ao Klayrah.

**Principio central (invariante do projeto):** Tudo configuravel pelo Mestre, nada hardcoded. Raridades, tipos e itens sao todos configurados via CRUD, nao enumerados no codigo.

**Valor entregue:**
- Mestre configura catalogo de itens uma vez; todos os personagens do jogo reutilizam
- Bonus de itens equipados calculados automaticamente (sem calculo manual pelo Mestre)
- Controle de raridade garante que apenas Mestre adiciona itens poderosos
- Dataset inicial acelera onboarding de novos jogos (40 itens prontos)
- Jogador ve claramente o que esta equipado e quais bonus estao ativos

---

## 2. Atores Envolvidos

| Ator | Role | Acoes |
|------|------|-------|
| Mestre | MESTRE | Configura raridades, tipos, catalogo de itens; adiciona qualquer raridade de item a fichas; revoga itens de fichas |
| Jogador | JOGADOR | Adiciona apenas itens Comuns ao proprio inventario; equipa/desequipa itens proprios; visualiza inventario |
| Sistema | — | Calcula bonus de itens equipados no FichaCalculationService; valida requisitos de uso; controla durabilidade |

---

## 3. Entidades de Configuracao

### 3.1 RaridadeItemConfig

Configuracao de raridade de itens. Cada raridade define cor de exibicao e ranges de bonus esperados para auxiliar o Mestre a balancear itens customizados.

**Campos:**

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| `id` | Long | Auto | PK |
| `jogo` | FK → Jogo | Sim | Jogo ao qual pertence |
| `nome` | String (50) | Sim | Ex: Comum, Incomum, Raro |
| `cor` | String (7) | Sim | Hex color ex: #9d9d9d |
| `ordemExibicao` | int | Sim | Ordem crescente de poder |
| `podeJogadorAdicionar` | boolean | Sim | Se Jogador pode adicionar itens desta raridade sem aprovacao do Mestre |
| `bonusAtributoMin` | int | Nao | Range esperado de bonus de atributo (minimo) |
| `bonusAtributoMax` | int | Nao | Range esperado de bonus de atributo (maximo) |
| `bonusDerivadoMin` | int | Nao | Range esperado de bonus em BonusConfig (minimo) |
| `bonusDerivadoMax` | int | Nao | Range esperado de bonus em BonusConfig (maximo) |
| `descricao` | String (500) | Nao | Descricao narrativa da raridade |
| `deletedAt` | Timestamp | Auto | Soft delete |

**Constraints:** unique `(jogo_id, nome)`, unique `(jogo_id, ordemExibicao)`

---

### 3.2 TipoItemConfig

Categorias hierarquicas de itens. Suporta dois niveis: categoria (Arma, Armadura, Acessorio) e subcategoria (Espada, Arco, Armadura Leve etc.).

**Campos:**

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| `id` | Long | Auto | PK |
| `jogo` | FK → Jogo | Sim | Jogo ao qual pertence |
| `nome` | String (100) | Sim | Ex: Arma, Espada Curta |
| `categoria` | String (50) | Sim | Categoria pai: ARMA, ARMADURA, ACESSORIO, CONSUMIVEL, FERRAMENTA, AVENTURA |
| `subcategoria` | String (50) | Nao | Subcategoria: ESPADA, ARCO, LANCA, CAJADO, ARMADURA_LEVE, ARMADURA_MEDIA, ARMADURA_PESADA, ESCUDO, ANEL, AMULETO, POCAO, KIT |
| `requerDuasMaos` | boolean | Nao (default false) | Se o item ocupa as duas maos ao ser equipado |
| `ordemExibicao` | int | Sim | Ordem de exibicao na UI |
| `descricao` | String (300) | Nao | Descricao do tipo |
| `deletedAt` | Timestamp | Auto | Soft delete |

**Hierarquia sugerida de tipos:**

| Categoria | Subcategoria | requerDuasMaos |
|-----------|-------------|----------------|
| ARMA | ESPADA (Espada Curta, Espada Longa, Espada Dupla) | false / true |
| ARMA | ARCO (Arco Curto, Arco Longo) | true |
| ARMA | LANCA (Lanca, Alabarda) | false / true |
| ARMA | MACHADO (Machadinha, Machado de Batalha, Machado Grande) | false / true |
| ARMA | MARTELO (Martelo Leve, Martelo de Guerra) | false / true |
| ARMA | CAJADO (Cajado de Madeira, Cajado Arcano) | true |
| ARMA | ADAGA (Adaga, Faca de Combate) | false |
| ARMA | ARREMESSO (Dardo, Javeline) | false |
| ARMA | BESTA (Besta Leve, Besta de Mao) | false / true |
| ARMADURA | ARMADURA_LEVE (Gibao de Couro, Couro Batido) | — |
| ARMADURA | ARMADURA_MEDIA (Camisao de Malha, Cota de Escamas) | — |
| ARMADURA | ARMADURA_PESADA (Cota de Malha, Meia Placa, Placa Completa) | — |
| ARMADURA | ESCUDO (Escudo de Madeira, Escudo de Aco) | — |
| ACESSORIO | ANEL (Anel de Poder, Anel de Protecao) | — |
| ACESSORIO | AMULETO (Amuleto, Colar, Pendente) | — |
| ACESSORIO | BOTAS (Botas de Velocidade, Botas Tacas) | — |
| ACESSORIO | CAPA (Capa de Invisibilidade, Capa Protetora) | — |
| ACESSORIO | LUVAS (Luvas de Forcamento, Luvas de Precisao) | — |
| CONSUMIVEL | POCAO (Pocao de Cura, Pocao de Forca) | — |
| CONSUMIVEL | MUNICAO (Flecha, Virote, Dardo) | — |
| FERRAMENTA | KIT (Kit de Ladroa, Kit de Curandeiro, Kit de Ferreiro) | — |
| AVENTURA | OUTROS (Corda, Archote, Escada) | — |

**Constraints:** unique `(jogo_id, nome)`

---

### 3.3 ItemConfig

O item em si. Pertence ao catalogo do jogo. Mestre configura; personagens instanciam via `FichaItem`.

**Campos principais:**

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| `id` | Long | Auto | PK |
| `jogo` | FK → Jogo | Sim | Jogo ao qual pertence |
| `nome` | String (100) | Sim | Nome do item |
| `raridade` | FK → RaridadeItemConfig | Sim | Raridade do item |
| `tipo` | FK → TipoItemConfig | Sim | Tipo/subcategoria |
| `peso` | BigDecimal (5,2) | Sim | Peso em kg (impacta Impeto de Forca) |
| `valor` | int | Nao | Custo em moedas de ouro (nullable para itens sem preco) |
| `duracaoPadrao` | int | Nao | Durabilidade inicial (null = indestrutivel) |
| `nivelMinimo` | int | Nao (default 1) | Nivel minimo para usar o item |
| `propriedades` | String (1000) | Nao | Texto livre: versatil, finura, perfurante, etc. |
| `descricao` | String (2000) | Nao | Descricao narrativa/lore |
| `ordemExibicao` | int | Sim | Ordem no catalogo |
| `deletedAt` | Timestamp | Auto | Soft delete |

**Sub-entidades:**
- `List<ItemRequisito>` — requisitos para usar/equipar o item
- `List<ItemEfeito>` — efeitos/bonus concedidos quando equipado

**Constraints:** unique `(jogo_id, nome)`

---

### 3.4 ItemRequisito

Requisito para que o personagem possa usar/equipar um item. Validado no momento de adicionar o item a ficha.

**Campos:**

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| `id` | Long | Auto | PK |
| `itemConfig` | FK → ItemConfig | Sim | Item ao qual pertence |
| `tipo` | Enum (TipoRequisito) | Sim | Tipo do requisito |
| `alvo` | String (50) | Nao | Sigla ou nome do alvo (ex: "FOR", "Furtividade", "Guerreiro") |
| `valorMinimo` | int | Nao | Valor minimo necessario |

**Enum TipoRequisito:**
- `NIVEL` — personagem deve estar no nivel minimo (`valorMinimo`)
- `ATRIBUTO` — atributo cujo total deve ser >= `valorMinimo` (`alvo` = sigla ex: "FOR")
- `BONUS` — bonus derivado >= `valorMinimo` (`alvo` = sigla ex: "BBA")
- `APTIDAO` — aptidao >= `valorMinimo` (`alvo` = nome da aptidao)
- `VANTAGEM` — deve possuir a vantagem (`alvo` = nome da vantagem, `valorMinimo` = nivel minimo)
- `CLASSE` — deve ser da classe informada (`alvo` = nome da classe)
- `RACA` — deve ser da raca informada (`alvo` = nome da raca)

---

### 3.5 ItemEfeito

Efeito/bonus concedido pelo item quando equipado. Estrutura similar a `VantagemEfeito` (mesmos tipos base).

**Campos:**

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| `id` | Long | Auto | PK |
| `itemConfig` | FK → ItemConfig | Sim | Item ao qual pertence |
| `tipoEfeito` | Enum (TipoItemEfeito) | Sim | Tipo do efeito |
| `atributoAlvo` | FK → AtributoConfig | Nao | Alvo para BONUS_ATRIBUTO |
| `aptidaoAlvo` | FK → AptidaoConfig | Nao | Alvo para BONUS_APTIDAO |
| `bonusAlvo` | FK → BonusConfig | Nao | Alvo para BONUS_DERIVADO |
| `valorFixo` | int | Nao | Valor fixo do bonus |
| `formula` | String (200) | Nao | Formula exp4j para FORMULA_CUSTOMIZADA |
| `descricaoEfeito` | String (300) | Nao | Descricao legivel do efeito |

**Enum TipoItemEfeito:**
- `BONUS_ATRIBUTO` — adiciona `valorFixo` ao campo `itens` de um FichaAtributo
- `BONUS_APTIDAO` — adiciona `valorFixo` ao campo `itens` de um FichaAptidao (campo novo, ver RN-11)
- `BONUS_DERIVADO` — adiciona `valorFixo` ao campo `itens` de um FichaBonus
- `BONUS_VIDA` — adiciona `valorFixo` ao componente `itens` de FichaVida
- `BONUS_ESSENCIA` — adiciona `valorFixo` ao componente `itens` de FichaEssencia
- `FORMULA_CUSTOMIZADA` — formula exp4j avaliada pelo FormulaEvaluatorService
- `EFEITO_DADO` — modifica o dado de prospeccao (similar a DADO_UP de VantagemEfeito)

> **Diferenca critica de VantagemEfeito:** ItemEfeito usa `valorFixo` simples (nao `valorPorNivel`) pois itens nao tem "nivel" — o bonus e fixo pelo item em si. Excecao: FORMULA_CUSTOMIZADA pode referenciar nivel do personagem como variavel.

> **Nota de campo `itens`:** FichaAtributo, FichaAptidao, FichaBonus, FichaVida e FichaEssencia precisam de novo campo `itens` (int, default 0) para separar bonus de itens de outros bonus. Ver RN-11 e SCHEMA-016-01 na secao de riscos.

---

### 3.6 ClasseEquipamentoInicial

Define equipamentos iniciais que um personagem recebe ao criar uma ficha com determinada classe. Suporta itens obrigatorios e grupos de escolha (Jogador escolhe um item dentre os do mesmo grupo).

**Campos:**

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| `id` | Long | Auto | PK |
| `classe` | FK → ClassePersonagem | Sim | Classe que recebe o equipamento |
| `itemConfig` | FK → ItemConfig | Sim | Item concedido |
| `obrigatorio` | boolean | Sim | true = sempre concedido; false = parte de um grupo de escolha |
| `grupoEscolha` | Integer | Nao | Itens com mesmo grupoEscolha = escolher apenas um. Null = obrigatorio |
| `quantidade` | int | Sim (default 1) | Quantidade do item concedido |

> Sub-recurso de ClassePersonagem. Endpoint: `GET/POST/PUT/DELETE /api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais`

---

### 3.7 FichaItem

Instancia de um item em uma ficha especifica. Pode ser baseado no catalogo (`itemConfig`) ou customizado do zero pelo Mestre (itemConfig null).

**Campos:**

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| `id` | Long | Auto | PK |
| `ficha` | FK → Ficha | Sim | Ficha dona do item |
| `itemConfig` | FK → ItemConfig | Nao | Null para itens customizados |
| `nome` | String (100) | Sim | Nome (sobreponivel ao do itemConfig) |
| `equipado` | boolean | Sim (default false) | Se o item esta equipado (ativa bonus) |
| `duracaoAtual` | Integer | Nao | Durabilidade restante (null = indestrutivel) |
| `quantidade` | int | Sim (default 1) | Quantidade |
| `peso` | BigDecimal (5,2) | Nao | Peso sobreponivel (herda de itemConfig se null) |
| `notas` | String (500) | Nao | Anotacoes do jogador sobre o item |
| `raridade` | FK → RaridadeItemConfig | Nao | Raridade sobreponivel (herda de itemConfig se null) |
| `adicionadoPor` | String | Auto | Username de quem adicionou (audit) |
| `deletedAt` | Timestamp | Auto | Soft delete |

---

## 4. Regras de Negocio

### RN-01 — Restricao de raridade para adicao de item

Apenas o Mestre pode adicionar itens de raridade com `podeJogadorAdicionar = false`. Jogador so pode adicionar itens de raridades com `podeJogadorAdicionar = true` (ex: Comum). Tentativa de JOGADOR adicionar item de raridade restrita = HTTP 403.

### RN-02 — Bonus so ativo se equipado

Um `FichaItem` com `equipado = false` NAO gera bonus no calculo da ficha. Apenas itens com `equipado = true` sao processados pelo `FichaCalculationService`. Excecao: itens de categoria CONSUMIVEL nunca sao "equipados" — sao consumidos diretamente.

### RN-03 — Durabilidade e quebra

Quando `duracaoAtual` chega a 0, o item esta "quebrado". Item quebrado: `equipado` forcado para `false` pelo sistema (bonus removidos). O Mestre pode restaurar a durabilidade. Nao ha remocao automatica do item — ele fica no inventario como quebrado.

### RN-04 — Peso impacta Impeto de Forca

O peso total de todos os itens em `FichaItem` (equipados e no inventario) e somado e comparado com a capacidade de carga da ficha (`AtributoConfig.formulaImpeto` de FOR = `total * 3` kg). Sobrecarga pode impactar calculos futuros (pos-MVP). No MVP: exibir peso total e capacidade, sem penalidade automatica.

### RN-05 — ItemConfig pertence ao jogo

`ItemConfig`, `RaridadeItemConfig` e `TipoItemConfig` tem `jogo_id`. Um item do Jogo A nao pode ser adicionado a uma ficha do Jogo B. Validacao server-side.

### RN-06 — Requisitos sao validados ao adicionar

Ao adicionar um `FichaItem` a uma ficha, o sistema valida `ItemRequisito`. Se algum requisito nao for atendido, HTTP 422 com descricao de qual requisito falhou. Mestre pode bypassar requisitos ao adicionar itens (flag `forcarAdicao: true` no request).

### RN-07 — ClasseEquipamentoInicial aplicado na criacao da ficha

Quando o wizard de criacao de ficha (Spec 006) completa o Passo 1 (escolha de classe), o sistema cria automaticamente os `FichaItem` correspondentes aos equipamentos obrigatorios e registra as opcoes de grupo de escolha para o Passo de Equipamentos.

### RN-08 — Item customizado (sem itemConfig)

Mestre pode criar `FichaItem` sem vincular a um `ItemConfig` do catalogo. Nesses casos, `nome`, `peso`, `raridade` e `notas` sao obrigatorios e os efeitos sao calculados via a lista de efeitos customizados do FichaItem (nao de ItemEfeito). No MVP: itens customizados sem efeito automatico — apenas informativos.

### RN-09 — Soft delete de ItemConfig

Deletar um `ItemConfig` do catalogo nao remove os `FichaItem` das fichas existentes. Os `FichaItem` que referenciavam o item deletado continuam existindo com o nome e bonus do momento da adicao. O item apenas deixa de aparecer no catalogo para novas adicoes.

### RN-10 — Siglas de ItemEfeito nao entram no namespace de siglas

`ItemEfeito` usa FKs diretas para AtributoConfig, BonusConfig, AptidaoConfig — nao usa siglas (abreviacoes) como identificadores. Portanto, `ItemEfeito` NAO compete no namespace cross-entity de siglas. Apenas `AtributoConfig`, `BonusConfig` e `VantagemConfig` competem neste namespace.

### RN-11 — Campo `itens` nos FichaBonus, FichaAtributo e FichaVida (SCHEMA-016-01)

O `FichaCalculationService` separa bonus por fonte: Vantagens, Classe, Itens, Gloria, Outros. Para aplicar bonus de itens separadamente, os campos de origem precisam existir nas entidades. `FichaAtributo.itens`, `FichaBonus.itens`, `FichaVida.itens`, `FichaEssencia.itens` devem ser adicionados (int, default 0). Estes campos sao zerados e recalculados a cada chamada de `recalcular()`, similar ao campo `vantagens`.

### RN-12 — Mestre pode remover item de qualquer ficha

O Mestre pode remover (soft delete) qualquer `FichaItem` de qualquer ficha do jogo. Jogador pode remover apenas itens proprios nao-obrigatorios. Itens marcados como obrigatorios na ClasseEquipamentoInicial nao podem ser removidos pelo Jogador (apenas Mestre).

### RN-13 — Quantidade e estaque

`FichaItem.quantidade` controla multiplas unidades do mesmo item. Adicionar o mesmo `ItemConfig` duas vezes com `quantidade=1` cria dois `FichaItem` distintos (sem auto-stackeamento no MVP). Municoes (CONSUMIVEL/MUNICAO) empilham automaticamente: adicionar flecha ja existente incrementa `quantidade`.

### RN-14 — Durabilidade decrementada pelo Mestre

No MVP, `duracaoAtual` e decrementada manualmente pelo Mestre via endpoint dedicado (`POST /fichas/{id}/itens/{itemId}/durabilidade`). Decremento automatico por combate e pos-MVP.

### RN-15 — Insolitus e Vantagens nao sao itens

`VantagemConfig` e `InsolitusCo` (Spec 007) nao sao processados como `ItemEfeito`. Sao entidades distintas com seu proprio fluxo de calculo. A fonte "Itens" nos campos de bonus das entidades de ficha e exclusivamente para `FichaItem` equipados.

---

## 5. Requisitos Funcionais

| # | Requisito | Ator | Endpoint |
|---|-----------|------|----------|
| RF-01 | CRUD completo de RaridadeItemConfig por jogo | MESTRE | `/api/v1/configuracoes/raridades-item` |
| RF-02 | CRUD completo de TipoItemConfig por jogo | MESTRE | `/api/v1/configuracoes/tipos-item` |
| RF-03 | CRUD completo de ItemConfig por jogo | MESTRE | `/api/v1/configuracoes/itens` |
| RF-04 | CRUD de ItemEfeito como sub-recurso de ItemConfig | MESTRE | `/api/v1/configuracoes/itens/{itemId}/efeitos` |
| RF-05 | CRUD de ItemRequisito como sub-recurso de ItemConfig | MESTRE | `/api/v1/configuracoes/itens/{itemId}/requisitos` |
| RF-06 | CRUD de ClasseEquipamentoInicial como sub-recurso de ClassePersonagem | MESTRE | `/api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais` |
| RF-07 | Listar itens do catalogo com filtros (tipo, raridade, nome) | MESTRE, JOGADOR | `GET /api/v1/jogos/{jogoId}/catalogo-itens` |
| RF-08 | Adicionar item do catalogo a uma ficha | MESTRE (qualquer), JOGADOR (so Comuns) | `POST /api/v1/fichas/{fichaId}/itens` |
| RF-09 | Adicionar item customizado a uma ficha | MESTRE | `POST /api/v1/fichas/{fichaId}/itens/customizado` |
| RF-10 | Equipar/desequipar item | MESTRE, JOGADOR (proprio) | `PATCH /api/v1/fichas/{fichaId}/itens/{itemId}/equipar` |
| RF-11 | Remover item da ficha (soft delete) | MESTRE, JOGADOR (restrito) | `DELETE /api/v1/fichas/{fichaId}/itens/{itemId}` |
| RF-12 | Decrementar durabilidade | MESTRE | `POST /api/v1/fichas/{fichaId}/itens/{itemId}/durabilidade` |
| RF-13 | Listar inventario de uma ficha | MESTRE, JOGADOR (proprio) | `GET /api/v1/fichas/{fichaId}/itens` |
| RF-14 | Aplicar bonus de itens equipados no FichaCalculationService | Sistema | Automatico em toda chamada `recalcular()` |
| RF-15 | Aplicar ClasseEquipamentoInicial na criacao de ficha | Sistema | Chamado pelo wizard (Spec 006 T5) |
| RF-16 | Dataset inicial de 40 itens no DefaultGameConfigProviderImpl | Sistema | Chamado ao criar Jogo |

---

## 6. Requisitos Nao Funcionais

| # | Requisito | Justificativa |
|---|-----------|--------------|
| RNF-01 | Listar inventario: N+1 resolvido via JOIN FETCH | Fichas com 20+ itens nao devem gerar 20+ queries |
| RNF-02 | Calcular bonus de itens: zerado e recalculado a cada chamada | Idempotencia obrigatoria (igual ao padrao existente) |
| RNF-03 | Validar requisitos de item: sem consultas extras no path critico | Carregar requisitos com JOIN FETCH junto ao ItemConfig |
| RNF-04 | Dataset inicial: sem hardcode — todos os itens via CRUD configuravel | Principio central do Klayrah |
| RNF-05 | Soft delete preserva historico de FichaItem | Auditoria e conformidade com INCONS-02 (fichas preservadas) |
| RNF-06 | Formato de peso: BigDecimal(5,2) | Ex: 1.36 kg — precisao de decimos de quilo |

---

## 7. Contrato de API

### RaridadeItemConfig

| Metodo | Path | Role | Status |
|--------|------|------|--------|
| GET | `/api/v1/configuracoes/raridades-item?jogoId={id}` | MESTRE, JOGADOR | 200 |
| GET | `/api/v1/configuracoes/raridades-item/{id}` | MESTRE, JOGADOR | 200 / 404 |
| POST | `/api/v1/configuracoes/raridades-item` | MESTRE | 201 |
| PUT | `/api/v1/configuracoes/raridades-item/{id}` | MESTRE | 200 / 404 / 409 |
| DELETE | `/api/v1/configuracoes/raridades-item/{id}` | MESTRE | 204 / 404 / 409 |
| PATCH | `/api/v1/configuracoes/raridades-item/reordenar` | MESTRE | 200 |

### TipoItemConfig

| Metodo | Path | Role | Status |
|--------|------|------|--------|
| GET | `/api/v1/configuracoes/tipos-item?jogoId={id}` | MESTRE, JOGADOR | 200 |
| POST | `/api/v1/configuracoes/tipos-item` | MESTRE | 201 |
| PUT | `/api/v1/configuracoes/tipos-item/{id}` | MESTRE | 200 / 409 |
| DELETE | `/api/v1/configuracoes/tipos-item/{id}` | MESTRE | 204 / 409 |

### ItemConfig

| Metodo | Path | Role | Status |
|--------|------|------|--------|
| GET | `/api/v1/configuracoes/itens?jogoId={id}&tipo={tipo}&raridade={rar}&nome={q}` | MESTRE, JOGADOR | 200 |
| GET | `/api/v1/configuracoes/itens/{id}` | MESTRE, JOGADOR | 200 / 404 |
| POST | `/api/v1/configuracoes/itens` | MESTRE | 201 |
| PUT | `/api/v1/configuracoes/itens/{id}` | MESTRE | 200 / 409 |
| DELETE | `/api/v1/configuracoes/itens/{id}` | MESTRE | 204 / 409 |
| GET | `/api/v1/configuracoes/itens/{id}/efeitos` | MESTRE, JOGADOR | 200 |
| POST | `/api/v1/configuracoes/itens/{id}/efeitos` | MESTRE | 201 |
| PUT | `/api/v1/configuracoes/itens/{id}/efeitos/{efeitoId}` | MESTRE | 200 |
| DELETE | `/api/v1/configuracoes/itens/{id}/efeitos/{efeitoId}` | MESTRE | 204 |
| GET | `/api/v1/configuracoes/itens/{id}/requisitos` | MESTRE, JOGADOR | 200 |
| POST | `/api/v1/configuracoes/itens/{id}/requisitos` | MESTRE | 201 |
| DELETE | `/api/v1/configuracoes/itens/{id}/requisitos/{reqId}` | MESTRE | 204 |

### ClasseEquipamentoInicial

| Metodo | Path | Role | Status |
|--------|------|------|--------|
| GET | `/api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais` | MESTRE, JOGADOR | 200 |
| POST | `/api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais` | MESTRE | 201 |
| PUT | `/api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais/{id}` | MESTRE | 200 |
| DELETE | `/api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais/{id}` | MESTRE | 204 |

### FichaItem

| Metodo | Path | Role | Status |
|--------|------|------|--------|
| GET | `/api/v1/fichas/{fichaId}/itens` | MESTRE, JOGADOR (proprio) | 200 |
| POST | `/api/v1/fichas/{fichaId}/itens` | MESTRE, JOGADOR (Comuns) | 201 / 403 / 422 |
| POST | `/api/v1/fichas/{fichaId}/itens/customizado` | MESTRE | 201 |
| PATCH | `/api/v1/fichas/{fichaId}/itens/{itemId}/equipar` | MESTRE, JOGADOR | 200 |
| PATCH | `/api/v1/fichas/{fichaId}/itens/{itemId}/desequipar` | MESTRE, JOGADOR | 200 |
| POST | `/api/v1/fichas/{fichaId}/itens/{itemId}/durabilidade` | MESTRE | 200 |
| DELETE | `/api/v1/fichas/{fichaId}/itens/{itemId}` | MESTRE, JOGADOR (restrito) | 204 / 403 / 409 |

### FichaItemRequest (POST /itens)

```json
{
  "itemConfigId": 42,
  "quantidade": 1,
  "notas": "Encontrado nas ruinas de Verath",
  "forcarAdicao": false
}
```

### FichaItemCustomizadoRequest (POST /itens/customizado)

```json
{
  "nome": "Espada Amaldicoada de Zathur",
  "raridadeId": 5,
  "peso": 1.5,
  "quantidade": 1,
  "notas": "Criada pelo feiticeiro Zathur — efeito unico"
}
```

### FichaItemResponse

```json
{
  "id": 101,
  "itemConfigId": 42,
  "nome": "Espada Longa +1",
  "equipado": true,
  "duracaoAtual": 8,
  "duracaoPadrao": 10,
  "quantidade": 1,
  "peso": 1.5,
  "notas": null,
  "raridade": { "id": 2, "nome": "Incomum", "cor": "#1eff00" },
  "tipo": { "id": 3, "nome": "Espada Longa", "categoria": "ARMA" },
  "efeitos": [
    { "tipoEfeito": "BONUS_DERIVADO", "bonusAlvo": "B.B.A", "valorFixo": 1, "descricaoEfeito": "+1 em B.B.A" }
  ],
  "pesoBruto": 1.5,
  "adicionadoPor": "john.doe",
  "dataCriacao": "2026-04-04T10:30:00"
}
```

---

## 8. Epico e User Stories

**EPIC-10: Sistema de Itens e Equipamentos**
Objetivo: Permitir que Mestre configure catalogo de itens e que personagens adquiram, equipem e beneficiem-se mecanicamente dos equipamentos.

---

**US-016-01: Configurar Raridades de Itens**
Como Mestre,
Quero configurar as raridades de itens do meu jogo (nome, cor, restricao de adicao),
Para que eu possa balancear quais tipos de itens Jogadores podem adicionar autonomamente.

Cenario 1: Criar raridade Comum permitida para Jogador
  Dado que sou Mestre de um Jogo
  Quando crio RaridadeItemConfig com nome="Comum", podeJogadorAdicionar=true, cor="#9d9d9d"
  Entao a raridade e criada com status 201
  E aparece no catalogo de raridades do jogo

Cenario 2: Nome duplicado no mesmo jogo
  Dado que ja existe raridade "Comum" no Jogo
  Quando tento criar outra raridade "Comum" no mesmo Jogo
  Entao recebo HTTP 409 com mensagem de conflito

---

**US-016-02: Configurar Catalogo de Itens**
Como Mestre,
Quero criar itens no catalogo do jogo com nome, tipo, raridade, peso, efeitos e requisitos,
Para que personagens possam adquirir e se beneficiar dos equipamentos mecanicamente.

Cenario 1: Criar item com efeito de bonus em B.B.A
  Dado que tenho RaridadeItemConfig "Incomum" e TipoItemConfig "Espada Longa"
  E tenho BonusConfig "B.B.A" configurado no jogo
  Quando crio ItemConfig "Espada Longa +1" com ItemEfeito BONUS_DERIVADO bonusAlvo=BBA valorFixo=1
  Entao o item e criado com status 201
  E o efeito esta associado ao item

Cenario 2: Deletar ItemConfig usado em ficha ativa
  Dado que "Espada Longa +1" esta em um FichaItem de uma ficha ativa
  Quando Mestre deleta "Espada Longa +1" do catalogo
  Entao o soft delete acontece (status 204)
  E o FichaItem existente permanece na ficha com os dados preservados

---

**US-016-03: Adicionar Item ao Inventario**
Como Jogador,
Quero adicionar itens Comuns ao meu inventario sem precisar de aprovacao do Mestre,
Para que eu possa gerenciar itens de baixo poder de forma autonoma.

Cenario 1: Jogador adiciona item Comum
  Dado que sou Jogador com ficha propria no Jogo
  E existe FichaItem "Corda" de raridade Comum (podeJogadorAdicionar=true)
  Quando envio POST /fichas/{id}/itens com itemConfigId do item Comum
  Entao o item e adicionado com status 201 e equipado=false

Cenario 2: Jogador tenta adicionar item Raro
  Dado que sou Jogador com ficha propria
  E "Espada Longa +1" tem raridade Incomum (podeJogadorAdicionar=false)
  Quando envio POST /fichas/{id}/itens com itemConfigId da Espada Longa +1
  Entao recebo HTTP 403 com mensagem "Apenas o Mestre pode adicionar itens desta raridade"

---

**US-016-04: Equipar Item e Ativar Bonus**
Como Jogador,
Quero equipar um item do meu inventario,
Para que seus bonus sejam aplicados automaticamente nas minhas fichas de calculo.

Cenario 1: Equipar item e verificar bonus aplicado
  Dado que tenho "Espada Longa +1" no inventario com equipado=false
  Quando envio PATCH /fichas/{id}/itens/{itemId}/equipar
  Entao o item muda para equipado=true
  E o proximo calculo da ficha inclui +1 em B.B.A na fonte "Itens"

Cenario 2: Item quebrado nao pode ser equipado
  Dado que tenho item com duracaoAtual=0
  Quando tento equipar o item
  Entao recebo HTTP 422 com mensagem "Item quebrado nao pode ser equipado"

---

**US-016-05: Controlar Durabilidade**
Como Mestre,
Quero decrementar a durabilidade de itens equipados em combate,
Para que itens poderosos se desgastem e mantenham tensao no jogo.

Cenario 1: Decrementar durabilidade
  Dado que FichaItem "Escudo de Aco" tem duracaoAtual=5
  Quando Mestre envia POST /fichas/{id}/itens/{itemId}/durabilidade com {"decremento": 2}
  Entao duracaoAtual fica 3

Cenario 2: Durabilidade chega a zero (item quebra)
  Dado que FichaItem tem duracaoAtual=1
  Quando Mestre envia POST com {"decremento": 1}
  Entao duracaoAtual fica 0
  E equipado e forcado para false automaticamente
  E o proximo calculo da ficha nao inclui os bonus do item

---

## 9. Criterios de Aceitacao Gerais

- [ ] CRUD de RaridadeItemConfig com unique `(jogo_id, nome)` e reordenacao
- [ ] CRUD de TipoItemConfig com hierarquia categoria/subcategoria
- [ ] CRUD de ItemConfig com sub-recursos ItemEfeito e ItemRequisito
- [ ] CRUD de ClasseEquipamentoInicial como sub-recurso de ClassePersonagem
- [ ] `FichaCalculationService.recalcular()` aplica ItemEfeito de itens equipados nos campos `itens` das entidades afetadas
- [ ] Idempotencia: recalcular duas vezes nao acumula bonus de itens
- [ ] JOGADOR nao consegue adicionar itens com `podeJogadorAdicionar=false` (HTTP 403)
- [ ] Item com `duracaoAtual=0` e forcado para `equipado=false`
- [ ] Soft delete de ItemConfig nao afeta FichaItem existentes
- [ ] Dataset de 40 itens criado ao inicializar novo Jogo (via DefaultGameConfigProviderImpl)
- [ ] Peso total do inventario exibido no inventario (soma de todos FichaItem)
- [ ] `./mvnw test` passa com todos os testes existentes (sem regressao)

---

## 10. Pontos em Aberto / Perguntas para Stakeholder

| ID | Pergunta | Impacto |
|----|----------|---------|
| PA-016-01 | Penalidade automatica de sobrecarga (peso > capacidade) e MVP ou pos-MVP? MVP atual so exibe o peso. | Impacta RF-04 e FichaCalculationService |
| PA-016-02 | Wizard de Spec 006 deve ter Passo de Equipamentos Iniciais ou os itens sao apenas aplicados automaticamente sem interacao do Jogador? | Impacta Spec 006 T6/T11 |
| PA-016-03 | `FichaAptidao` deve ter campo `itens` para bonus de itens em aptidoes? Ou aptidoes nao sao afetadas por itens? | Impacta SCHEMA-016-01 |
| PA-016-04 | Item customizado (sem ItemConfig) pode ter efeitos automaticos no MVP, ou apenas informativos? | Impacta T4 e T5 |
| PA-016-05 | Municao (Flecha, Virote): stackea automaticamente ao adicionar ou cria novo FichaItem? | Impacta RN-13 e T4 |
| PA-016-06 | Jogador pode ver o catalogo completo do jogo para escolher quais itens quer comprar/adquirir, ou ve apenas o seu inventario? | Impacta RF-07 e T11 |

---

## 11. Riscos Tecnicos

| ID | Risco | Mitigacao |
|----|-------|-----------|
| SCHEMA-016-01 | FichaAtributo, FichaBonus, FichaVida, FichaEssencia precisam de campo `itens` (novo) — migration necessaria | Task T5 inclui migration + adapter no FichaCalculationService |
| SCHEMA-016-02 | `FichaAptidao.itens` pode ser necessario dependendo de PA-016-03 | Aguardar resposta antes de T5 |
| PERF-016-01 | Listar inventario de ficha com muitos itens pode gerar N+1 se ItemConfig, RaridadeItemConfig e TipoItemConfig forem carregados separadamente | JOIN FETCH obrigatorio na FichaItemRepository |
| COMPAT-016-01 | Adicionar campo `itens` as entidades de ficha pode quebrar testes existentes que verificam `total` via formula sem considerar `itens` | Revisar todos os testes de FichaCalculationService na T7 |

---

*Produzido por: Business Analyst/PO | 2026-04-04*
*Baseado em: decisoes do PO, Spec 007 (VantagemEfeito pattern), GLOSSARIO.md, D&D 5e SRD*
