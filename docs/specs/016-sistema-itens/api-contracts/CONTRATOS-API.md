# Spec 016 -- Contratos de API: Sistema de Itens e Equipamentos

> Contratos REST completos para o sistema de itens/equipamentos.
> Padrao do projeto: Spring Boot 4, Java 25, records como DTOs, MapStruct, soft delete.
> Regras de negocio: ver `REGRAS-NEGOCIO.md` (referencias `RN-ITEM-XX`).
> Ultima atualizacao: 2026-04-04

---

## Indice de Endpoints

| # | Metodo | Path | Secao |
|---|--------|------|-------|
| 1 | GET | `/api/v1/configuracoes/raridades-item` | [1.1](#11-listar-raridades) |
| 2 | GET | `/api/v1/configuracoes/raridades-item/{id}` | [1.2](#12-buscar-raridade-por-id) |
| 3 | POST | `/api/v1/configuracoes/raridades-item` | [1.3](#13-criar-raridade) |
| 4 | PUT | `/api/v1/configuracoes/raridades-item/{id}` | [1.4](#14-atualizar-raridade) |
| 5 | DELETE | `/api/v1/configuracoes/raridades-item/{id}` | [1.5](#15-deletar-raridade) |
| 6 | PUT | `/api/v1/configuracoes/raridades-item/reordenar` | [1.6](#16-reordenar-raridades) |
| 7 | GET | `/api/v1/configuracoes/tipos-item` | [2.1](#21-listar-tipos-item) |
| 8 | GET | `/api/v1/configuracoes/tipos-item/{id}` | [2.2](#22-buscar-tipo-item-por-id) |
| 9 | POST | `/api/v1/configuracoes/tipos-item` | [2.3](#23-criar-tipo-item) |
| 10 | PUT | `/api/v1/configuracoes/tipos-item/{id}` | [2.4](#24-atualizar-tipo-item) |
| 11 | DELETE | `/api/v1/configuracoes/tipos-item/{id}` | [2.5](#25-deletar-tipo-item) |
| 12 | PUT | `/api/v1/configuracoes/tipos-item/reordenar` | [2.6](#26-reordenar-tipos-item) |
| 13 | GET | `/api/v1/configuracoes/itens` | [3.1](#31-listar-itens-do-catalogo) |
| 14 | GET | `/api/v1/configuracoes/itens/{id}` | [3.2](#32-buscar-item-por-id) |
| 15 | POST | `/api/v1/configuracoes/itens` | [3.3](#33-criar-item) |
| 16 | PUT | `/api/v1/configuracoes/itens/{id}` | [3.4](#34-atualizar-item) |
| 17 | DELETE | `/api/v1/configuracoes/itens/{id}` | [3.5](#35-deletar-item) |
| 18 | PUT | `/api/v1/configuracoes/itens/reordenar` | [3.6](#36-reordenar-itens) |
| 19 | GET | `/api/v1/configuracoes/itens/{id}/efeitos` | [4.1](#41-listar-efeitos-de-item) |
| 20 | POST | `/api/v1/configuracoes/itens/{id}/efeitos` | [4.2](#42-adicionar-efeito-a-item) |
| 21 | PUT | `/api/v1/configuracoes/itens/{id}/efeitos/{efeitoId}` | [4.3](#43-atualizar-efeito-de-item) |
| 22 | DELETE | `/api/v1/configuracoes/itens/{id}/efeitos/{efeitoId}` | [4.4](#44-remover-efeito-de-item) |
| 23 | GET | `/api/v1/configuracoes/itens/{id}/requisitos` | [5.1](#51-listar-requisitos-de-item) |
| 24 | POST | `/api/v1/configuracoes/itens/{id}/requisitos` | [5.2](#52-adicionar-requisito-a-item) |
| 25 | DELETE | `/api/v1/configuracoes/itens/{id}/requisitos/{reqId}` | [5.3](#53-remover-requisito-de-item) |
| 26 | GET | `/api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais` | [6.1](#61-listar-equipamentos-iniciais) |
| 27 | POST | `/api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais` | [6.2](#62-adicionar-equipamento-inicial) |
| 28 | PUT | `/api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais/{id}` | [6.3](#63-atualizar-equipamento-inicial) |
| 29 | DELETE | `/api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais/{id}` | [6.4](#64-remover-equipamento-inicial) |
| 30 | GET | `/api/v1/fichas/{fichaId}/itens` | [7.1](#71-listar-inventario-da-ficha) |
| 31 | POST | `/api/v1/fichas/{fichaId}/itens` | [7.2](#72-adicionar-item-do-catalogo) |
| 32 | POST | `/api/v1/fichas/{fichaId}/itens/customizado` | [7.3](#73-adicionar-item-customizado) |
| 33 | PATCH | `/api/v1/fichas/{fichaId}/itens/{itemId}/equipar` | [7.4](#74-equipar-item) |
| 34 | PATCH | `/api/v1/fichas/{fichaId}/itens/{itemId}/desequipar` | [7.5](#75-desequipar-item) |
| 35 | POST | `/api/v1/fichas/{fichaId}/itens/{itemId}/durabilidade` | [7.6](#76-alterar-durabilidade) |
| 36 | DELETE | `/api/v1/fichas/{fichaId}/itens/{itemId}` | [7.7](#77-remover-item-da-ficha) |

---

## 1. RaridadeItemConfig

**Controller**: `RaridadeItemController`
**Base path**: `/api/v1/configuracoes/raridades-item`
**Tag Swagger**: `Configuracoes - Raridades de Item`
**Service**: `RaridadeItemConfiguracaoService extends AbstractConfiguracaoService`

### 1.1 Listar Raridades

```
GET /api/v1/configuracoes/raridades-item?jogoId={jogoId}&nome={nome}
```

**Autorizacao**: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

| Param | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| `jogoId` | Long | Sim | ID do jogo |
| `nome` | String | Nao | Filtro parcial case-insensitive |

**Response 200**:
```json
[
  {
    "id": 1,
    "jogoId": 10,
    "nome": "Comum",
    "cor": "#9d9d9d",
    "ordemExibicao": 1,
    "podeJogadorAdicionar": true,
    "bonusAtributoMin": null,
    "bonusAtributoMax": null,
    "bonusDerivadoMin": null,
    "bonusDerivadoMax": null,
    "descricao": "Itens mundanos encontrados em qualquer loja",
    "dataCriacao": "2026-04-04T10:00:00",
    "dataUltimaAtualizacao": "2026-04-04T10:00:00"
  },
  {
    "id": 2,
    "jogoId": 10,
    "nome": "Incomum",
    "cor": "#1eff00",
    "ordemExibicao": 2,
    "podeJogadorAdicionar": false,
    "bonusAtributoMin": 1,
    "bonusAtributoMax": 2,
    "bonusDerivadoMin": 1,
    "bonusDerivadoMax": 1,
    "descricao": "Itens com propriedades magicas leves",
    "dataCriacao": "2026-04-04T10:00:00",
    "dataUltimaAtualizacao": "2026-04-04T10:00:00"
  }
]
```

**Erros**: 404 (jogo nao encontrado)

---

### 1.2 Buscar Raridade por ID

```
GET /api/v1/configuracoes/raridades-item/{id}
```

**Autorizacao**: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

**Response 200**: mesmo schema de item individual do array acima.

**Erros**: 404 (raridade nao encontrada)

---

### 1.3 Criar Raridade

```
POST /api/v1/configuracoes/raridades-item
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Request Body** (`CreateRaridadeItemRequest`):
```json
{
  "jogoId": 10,
  "nome": "Raro",
  "cor": "#0070dd",
  "ordemExibicao": 3,
  "podeJogadorAdicionar": false,
  "bonusAtributoMin": 1,
  "bonusAtributoMax": 3,
  "bonusDerivadoMin": 1,
  "bonusDerivadoMax": 2,
  "descricao": "Itens com propriedades magicas significativas"
}
```

| Campo | Tipo | Obrigatorio | Validacao |
|-------|------|-------------|-----------|
| `jogoId` | Long | Sim | `@NotNull` |
| `nome` | String | Sim | `@NotBlank @Size(max=50)` |
| `cor` | String | Sim | `@NotBlank @Size(min=4, max=7)` `@Pattern(regexp="^#[0-9a-fA-F]{3,6}$")` |
| `ordemExibicao` | Integer | Nao | Auto-calculado se null |
| `podeJogadorAdicionar` | Boolean | Sim | `@NotNull` |
| `bonusAtributoMin` | Integer | Nao | `@Min(0)` |
| `bonusAtributoMax` | Integer | Nao | `@Min(0)`, deve ser >= bonusAtributoMin |
| `bonusDerivadoMin` | Integer | Nao | `@Min(0)` |
| `bonusDerivadoMax` | Integer | Nao | `@Min(0)`, deve ser >= bonusDerivadoMin |
| `descricao` | String | Nao | `@Size(max=500)` |

**Response 201**: mesmo schema de RaridadeItemResponse

**Erros**:
- 400: validacao falhou
- 403: usuario nao e MESTRE
- 404: jogo nao encontrado
- 409: nome duplicado no jogo (`RN-ITEM-13`)

---

### 1.4 Atualizar Raridade

```
PUT /api/v1/configuracoes/raridades-item/{id}
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Request Body** (`UpdateRaridadeItemRequest`):
```json
{
  "nome": "Raro (atualizado)",
  "cor": "#0070ff",
  "podeJogadorAdicionar": false,
  "bonusAtributoMin": 2,
  "bonusAtributoMax": 4,
  "bonusDerivadoMin": 1,
  "bonusDerivadoMax": 3,
  "descricao": "Descricao atualizada"
}
```

Todos os campos sao opcionais (null = nao altera). MapStruct `NullValuePropertyMappingStrategy.IGNORE`.

**Response 200**: RaridadeItemResponse atualizado

**Erros**: 400, 403, 404, 409 (nome duplicado)

---

### 1.5 Deletar Raridade

```
DELETE /api/v1/configuracoes/raridades-item/{id}
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Response 204**: sem body

**Erros**:
- 403: usuario nao e MESTRE
- 404: raridade nao encontrada
- 409: raridade em uso por algum `ItemConfig` ativo (`RN-ITEM-26`)

---

### 1.6 Reordenar Raridades

```
PUT /api/v1/configuracoes/raridades-item/reordenar?jogoId={jogoId}
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Request Body** (`ReordenarRequest`):
```json
{
  "itens": [
    { "id": 1, "ordemExibicao": 1 },
    { "id": 2, "ordemExibicao": 2 },
    { "id": 3, "ordemExibicao": 3 }
  ]
}
```

**Response 204**: sem body

**Erros**: 400, 403

---

## 2. TipoItemConfig

**Controller**: `TipoItemController`
**Base path**: `/api/v1/configuracoes/tipos-item`
**Tag Swagger**: `Configuracoes - Tipos de Item`
**Service**: `TipoItemConfiguracaoService extends AbstractConfiguracaoService`

### 2.1 Listar Tipos de Item

```
GET /api/v1/configuracoes/tipos-item?jogoId={jogoId}&nome={nome}&categoria={cat}
```

**Autorizacao**: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

| Param | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| `jogoId` | Long | Sim | ID do jogo |
| `nome` | String | Nao | Filtro parcial case-insensitive |
| `categoria` | String | Nao | Filtro por categoria (ARMA, ARMADURA, ACESSORIO, CONSUMIVEL, FERRAMENTA, AVENTURA) |

**Response 200**:
```json
[
  {
    "id": 1,
    "jogoId": 10,
    "nome": "Espada Longa",
    "categoria": "ARMA",
    "subcategoria": "ESPADA",
    "requerDuasMaos": false,
    "ordemExibicao": 1,
    "descricao": "Espada de lamina longa, versatil",
    "dataCriacao": "2026-04-04T10:00:00",
    "dataUltimaAtualizacao": "2026-04-04T10:00:00"
  },
  {
    "id": 2,
    "jogoId": 10,
    "nome": "Arco Longo",
    "categoria": "ARMA",
    "subcategoria": "ARCO",
    "requerDuasMaos": true,
    "ordemExibicao": 2,
    "descricao": null,
    "dataCriacao": "2026-04-04T10:00:00",
    "dataUltimaAtualizacao": "2026-04-04T10:00:00"
  }
]
```

**Erros**: 404 (jogo nao encontrado)

---

### 2.2 Buscar Tipo Item por ID

```
GET /api/v1/configuracoes/tipos-item/{id}
```

**Autorizacao**: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

**Response 200**: TipoItemResponse individual

**Erros**: 404

---

### 2.3 Criar Tipo Item

```
POST /api/v1/configuracoes/tipos-item
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Request Body** (`CreateTipoItemRequest`):
```json
{
  "jogoId": 10,
  "nome": "Espada Curta",
  "categoria": "ARMA",
  "subcategoria": "ESPADA",
  "requerDuasMaos": false,
  "ordemExibicao": 3,
  "descricao": "Espada leve e rapida"
}
```

| Campo | Tipo | Obrigatorio | Validacao |
|-------|------|-------------|-----------|
| `jogoId` | Long | Sim | `@NotNull` |
| `nome` | String | Sim | `@NotBlank @Size(max=100)` |
| `categoria` | String | Sim | `@NotBlank @Size(max=50)` |
| `subcategoria` | String | Nao | `@Size(max=50)` |
| `requerDuasMaos` | Boolean | Nao | default false |
| `ordemExibicao` | Integer | Nao | Auto-calculado se null |
| `descricao` | String | Nao | `@Size(max=300)` |

**Response 201**: TipoItemResponse

**Erros**: 400, 403, 404, 409 (nome duplicado)

---

### 2.4 Atualizar Tipo Item

```
PUT /api/v1/configuracoes/tipos-item/{id}
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Request Body** (`UpdateTipoItemRequest`): todos os campos opcionais (null = nao altera)

**Response 200**: TipoItemResponse

**Erros**: 400, 403, 404, 409

---

### 2.5 Deletar Tipo Item

```
DELETE /api/v1/configuracoes/tipos-item/{id}
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Response 204**

**Erros**: 403, 404, 409 (tipo em uso por ItemConfig ativo -- `RN-ITEM-27`)

---

### 2.6 Reordenar Tipos Item

```
PUT /api/v1/configuracoes/tipos-item/reordenar?jogoId={jogoId}
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Request/Response**: identico ao padrao de reordenacao (ver 1.6)

---

## 3. ItemConfig (Catalogo de Itens)

**Controller**: `ItemConfigController`
**Base path**: `/api/v1/configuracoes/itens`
**Tag Swagger**: `Configuracoes - Catalogo de Itens`
**Service**: `ItemConfiguracaoService extends AbstractConfiguracaoService`

### 3.1 Listar Itens do Catalogo

```
GET /api/v1/configuracoes/itens?jogoId={jogoId}&nome={nome}&tipoId={tipoId}&raridadeId={raridadeId}&nivelMinimo={nivel}
```

**Autorizacao**: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

| Param | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| `jogoId` | Long | Sim | ID do jogo |
| `nome` | String | Nao | Filtro parcial case-insensitive |
| `tipoId` | Long | Nao | Filtrar por TipoItemConfig |
| `raridadeId` | Long | Nao | Filtrar por RaridadeItemConfig |
| `nivelMinimo` | Integer | Nao | Filtrar itens com nivelMinimo <= valor informado |

**Response 200**:
```json
[
  {
    "id": 42,
    "jogoId": 10,
    "nome": "Espada Longa +1",
    "raridade": {
      "id": 2,
      "nome": "Incomum",
      "cor": "#1eff00"
    },
    "tipo": {
      "id": 1,
      "nome": "Espada Longa",
      "categoria": "ARMA",
      "subcategoria": "ESPADA",
      "requerDuasMaos": false
    },
    "peso": 1.50,
    "valor": 350,
    "duracaoPadrao": 10,
    "nivelMinimo": 3,
    "propriedades": "Versatil, Magico",
    "descricao": "Uma espada longa forjada com encantamento de combate",
    "ordemExibicao": 1,
    "quantidadeEfeitos": 1,
    "quantidadeRequisitos": 2,
    "dataCriacao": "2026-04-04T10:00:00",
    "dataUltimaAtualizacao": "2026-04-04T10:00:00"
  }
]
```

> **Nota**: a listagem retorna `quantidadeEfeitos` e `quantidadeRequisitos` (contagens) em vez de carregar os sub-recursos completos. Para detalhes, usar o GET por ID ou os endpoints de sub-recurso.

**Erros**: 404 (jogo nao encontrado)

---

### 3.2 Buscar Item por ID

```
GET /api/v1/configuracoes/itens/{id}
```

**Autorizacao**: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

**Response 200** (detalhado, inclui efeitos e requisitos inline):
```json
{
  "id": 42,
  "jogoId": 10,
  "nome": "Espada Longa +1",
  "raridade": {
    "id": 2,
    "nome": "Incomum",
    "cor": "#1eff00"
  },
  "tipo": {
    "id": 1,
    "nome": "Espada Longa",
    "categoria": "ARMA",
    "subcategoria": "ESPADA",
    "requerDuasMaos": false
  },
  "peso": 1.50,
  "valor": 350,
  "duracaoPadrao": 10,
  "nivelMinimo": 3,
  "propriedades": "Versatil, Magico",
  "descricao": "Uma espada longa forjada com encantamento de combate",
  "ordemExibicao": 1,
  "efeitos": [
    {
      "id": 100,
      "tipoEfeito": "BONUS_DERIVADO",
      "atributoAlvoId": null,
      "atributoAlvoNome": null,
      "aptidaoAlvoId": null,
      "aptidaoAlvoNome": null,
      "bonusAlvoId": 5,
      "bonusAlvoNome": "B.B.A",
      "valorFixo": 1,
      "formula": null,
      "descricaoEfeito": "+1 em Bonus Base de Ataque",
      "dataCriacao": "2026-04-04T10:30:00"
    }
  ],
  "requisitos": [
    {
      "id": 200,
      "tipo": "NIVEL",
      "alvo": null,
      "valorMinimo": 3,
      "descricaoLegivel": "Nivel minimo: 3"
    },
    {
      "id": 201,
      "tipo": "ATRIBUTO",
      "alvo": "FOR",
      "valorMinimo": 12,
      "descricaoLegivel": "Forca minima: 12"
    }
  ],
  "dataCriacao": "2026-04-04T10:00:00",
  "dataUltimaAtualizacao": "2026-04-04T10:00:00"
}
```

**Erros**: 404

---

### 3.3 Criar Item

```
POST /api/v1/configuracoes/itens
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Request Body** (`CreateItemConfigRequest`):
```json
{
  "jogoId": 10,
  "nome": "Espada Longa +1",
  "raridadeId": 2,
  "tipoId": 1,
  "peso": 1.50,
  "valor": 350,
  "duracaoPadrao": 10,
  "nivelMinimo": 3,
  "propriedades": "Versatil, Magico",
  "descricao": "Uma espada longa forjada com encantamento de combate",
  "ordemExibicao": 1
}
```

| Campo | Tipo | Obrigatorio | Validacao |
|-------|------|-------------|-----------|
| `jogoId` | Long | Sim | `@NotNull` |
| `nome` | String | Sim | `@NotBlank @Size(max=100)` |
| `raridadeId` | Long | Sim | `@NotNull`, FK valida no mesmo jogo |
| `tipoId` | Long | Sim | `@NotNull`, FK valida no mesmo jogo |
| `peso` | BigDecimal | Sim | `@NotNull @DecimalMin("0.00") @Digits(integer=3, fraction=2)` |
| `valor` | Integer | Nao | `@Min(0)` |
| `duracaoPadrao` | Integer | Nao | `@Min(1)` (null = indestrutivel) |
| `nivelMinimo` | Integer | Nao | `@Min(1)` (default 1) |
| `propriedades` | String | Nao | `@Size(max=1000)` |
| `descricao` | String | Nao | `@Size(max=2000)` |
| `ordemExibicao` | Integer | Nao | Auto-calculado se null |

**Response 201**: ItemConfigResponse (mesmo schema do GET por ID, mas `efeitos: []` e `requisitos: []`)

**Erros**: 400, 403, 404 (jogo/raridade/tipo), 409 (nome duplicado -- `RN-ITEM-13`)

---

### 3.4 Atualizar Item

```
PUT /api/v1/configuracoes/itens/{id}
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Request Body** (`UpdateItemConfigRequest`):
```json
{
  "nome": "Espada Longa +2",
  "raridadeId": 3,
  "tipoId": null,
  "peso": 1.60,
  "valor": 800,
  "duracaoPadrao": 12,
  "nivelMinimo": 5,
  "propriedades": "Versatil, Magico, Resistente",
  "descricao": "Versao aprimorada da espada encantada"
}
```

Todos os campos opcionais (null = nao altera).

**Response 200**: ItemConfigResponse atualizado

**Erros**: 400, 403, 404, 409

> **Nota**: alterar raridade/tipo de um ItemConfig NAO afeta `FichaItem` existentes que ja referenciam o item (os dados do FichaItem sao snapshots -- `RN-ITEM-12`).

---

### 3.5 Deletar Item

```
DELETE /api/v1/configuracoes/itens/{id}
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Response 204**

**Erros**: 403, 404

> **Nota**: soft delete. `FichaItem` existentes que referenciam este ItemConfig continuam funcionando (`RN-ITEM-12`). O item apenas sai do catalogo para novas adicoes.

---

### 3.6 Reordenar Itens

```
PUT /api/v1/configuracoes/itens/reordenar?jogoId={jogoId}
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Request/Response**: identico ao padrao de reordenacao (ver 1.6)

---

## 4. ItemEfeito (Sub-recurso de ItemConfig)

**Controller**: `ItemEfeitoController`
**Base path**: `/api/v1/configuracoes/itens/{itemId}/efeitos`
**Tag Swagger**: `Configuracoes - Efeitos de Item`
**Service**: `ItemEfeitoService`

> Segue padrao identico a `VantagemEfeitoController` -- sub-recurso de entidade pai.

### 4.1 Listar Efeitos de Item

```
GET /api/v1/configuracoes/itens/{itemId}/efeitos
```

**Autorizacao**: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

**Response 200**:
```json
[
  {
    "id": 100,
    "itemConfigId": 42,
    "tipoEfeito": "BONUS_DERIVADO",
    "atributoAlvoId": null,
    "atributoAlvoNome": null,
    "aptidaoAlvoId": null,
    "aptidaoAlvoNome": null,
    "bonusAlvoId": 5,
    "bonusAlvoNome": "B.B.A",
    "valorFixo": 1,
    "formula": null,
    "descricaoEfeito": "+1 em Bonus Base de Ataque",
    "dataCriacao": "2026-04-04T10:30:00"
  },
  {
    "id": 101,
    "itemConfigId": 42,
    "tipoEfeito": "BONUS_ATRIBUTO",
    "atributoAlvoId": 1,
    "atributoAlvoNome": "Forca",
    "aptidaoAlvoId": null,
    "aptidaoAlvoNome": null,
    "bonusAlvoId": null,
    "bonusAlvoNome": null,
    "valorFixo": 2,
    "formula": null,
    "descricaoEfeito": "+2 em Forca",
    "dataCriacao": "2026-04-04T10:31:00"
  }
]
```

**Erros**: 404 (item nao encontrado)

---

### 4.2 Adicionar Efeito a Item

```
POST /api/v1/configuracoes/itens/{itemId}/efeitos
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Request Body** (`CriarItemEfeitoRequest`):
```json
{
  "tipoEfeito": "BONUS_ATRIBUTO",
  "atributoAlvoId": 1,
  "aptidaoAlvoId": null,
  "bonusAlvoId": null,
  "valorFixo": 2,
  "formula": null,
  "descricaoEfeito": "+2 em Forca"
}
```

| Campo | Tipo | Obrigatorio | Validacao |
|-------|------|-------------|-----------|
| `tipoEfeito` | TipoItemEfeito (enum) | Sim | `@NotNull` |
| `atributoAlvoId` | Long | Condicional | Obrigatorio se tipoEfeito = BONUS_ATRIBUTO |
| `aptidaoAlvoId` | Long | Condicional | Obrigatorio se tipoEfeito = BONUS_APTIDAO |
| `bonusAlvoId` | Long | Condicional | Obrigatorio se tipoEfeito = BONUS_DERIVADO |
| `valorFixo` | Integer | Condicional | Obrigatorio exceto FORMULA_CUSTOMIZADA e EFEITO_DADO |
| `formula` | String | Condicional | `@Size(max=200)` Obrigatorio se FORMULA_CUSTOMIZADA |
| `descricaoEfeito` | String | Nao | `@Size(max=300)` |

**Validacao condicional por tipo de efeito**:

| TipoItemEfeito | Campos obrigatorios | Campos ignorados |
|----------------|---------------------|------------------|
| `BONUS_ATRIBUTO` | `atributoAlvoId`, `valorFixo` | `aptidaoAlvoId`, `bonusAlvoId`, `formula` |
| `BONUS_APTIDAO` | `aptidaoAlvoId`, `valorFixo` | `atributoAlvoId`, `bonusAlvoId`, `formula` |
| `BONUS_DERIVADO` | `bonusAlvoId`, `valorFixo` | `atributoAlvoId`, `aptidaoAlvoId`, `formula` |
| `BONUS_VIDA` | `valorFixo` | todos os alvos FK, `formula` |
| `BONUS_ESSENCIA` | `valorFixo` | todos os alvos FK, `formula` |
| `FORMULA_CUSTOMIZADA` | `formula` | todos os alvos FK, `valorFixo` |
| `EFEITO_DADO` | nenhum obrigatorio | `formula`, `valorFixo` |

**Response 201**: ItemEfeitoResponse

**Erros**:
- 400: validacao falhou
- 403: usuario nao e MESTRE
- 404: item nao encontrado, ou FK alvo nao encontrada
- 422: FK alvo pertence a outro jogo (`RN-ITEM-28`); formula invalida

---

### 4.3 Atualizar Efeito de Item

```
PUT /api/v1/configuracoes/itens/{itemId}/efeitos/{efeitoId}
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Request Body**: mesmo que `CriarItemEfeitoRequest`

**Response 200**: ItemEfeitoResponse atualizado

**Erros**: 400, 403, 404, 422

---

### 4.4 Remover Efeito de Item

```
DELETE /api/v1/configuracoes/itens/{itemId}/efeitos/{efeitoId}
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Response 204**

**Erros**: 403, 404

---

## 5. ItemRequisito (Sub-recurso de ItemConfig)

**Controller**: `ItemRequisitoController`
**Base path**: `/api/v1/configuracoes/itens/{itemId}/requisitos`
**Tag Swagger**: `Configuracoes - Requisitos de Item`
**Service**: `ItemRequisitoService`

### 5.1 Listar Requisitos de Item

```
GET /api/v1/configuracoes/itens/{itemId}/requisitos
```

**Autorizacao**: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

**Response 200**:
```json
[
  {
    "id": 200,
    "itemConfigId": 42,
    "tipo": "NIVEL",
    "alvo": null,
    "valorMinimo": 3,
    "descricaoLegivel": "Nivel minimo: 3"
  },
  {
    "id": 201,
    "itemConfigId": 42,
    "tipo": "ATRIBUTO",
    "alvo": "FOR",
    "valorMinimo": 12,
    "descricaoLegivel": "Forca minima: 12"
  },
  {
    "id": 202,
    "itemConfigId": 42,
    "tipo": "CLASSE",
    "alvo": "Guerreiro",
    "valorMinimo": null,
    "descricaoLegivel": "Classe: Guerreiro"
  }
]
```

> **`descricaoLegivel`**: campo calculado pelo mapper com base no tipo e alvo, para exibicao no frontend sem logica de formatacao.

**Erros**: 404

---

### 5.2 Adicionar Requisito a Item

```
POST /api/v1/configuracoes/itens/{itemId}/requisitos
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Request Body** (`CriarItemRequisitoRequest`):
```json
{
  "tipo": "ATRIBUTO",
  "alvo": "FOR",
  "valorMinimo": 12
}
```

| Campo | Tipo | Obrigatorio | Validacao |
|-------|------|-------------|-----------|
| `tipo` | TipoRequisito (enum) | Sim | `@NotNull` |
| `alvo` | String | Condicional | `@Size(max=50)` Obrigatorio exceto para tipo NIVEL |
| `valorMinimo` | Integer | Condicional | `@Min(1)` Obrigatorio para NIVEL, ATRIBUTO, BONUS, APTIDAO |

**Validacao condicional por tipo**:

| TipoRequisito | `alvo` | `valorMinimo` |
|---------------|--------|---------------|
| `NIVEL` | ignorado | obrigatorio |
| `ATRIBUTO` | sigla do atributo (ex: "FOR") | obrigatorio |
| `BONUS` | sigla do bonus (ex: "BBA") | obrigatorio |
| `APTIDAO` | nome da aptidao | obrigatorio |
| `VANTAGEM` | nome da vantagem | nivel minimo da vantagem (default 1) |
| `CLASSE` | nome da classe | ignorado |
| `RACA` | nome da raca | ignorado |

**Response 201**: ItemRequisitoResponse

**Erros**: 400, 403, 404

---

### 5.3 Remover Requisito de Item

```
DELETE /api/v1/configuracoes/itens/{itemId}/requisitos/{reqId}
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Response 204**

**Erros**: 403, 404

---

## 6. ClasseEquipamentoInicial (Sub-recurso de ClassePersonagem)

**Controller**: endpoint adicionado ao `ClasseController` existente (segue padrao de bonus/aptidao-bonus)
**Base path**: `/api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais`
**Tag Swagger**: `Configuracoes - Classes` (tag existente)
**Service**: `ClasseConfiguracaoService` (metodos adicionais)

### 6.1 Listar Equipamentos Iniciais

```
GET /api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais
```

**Autorizacao**: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

**Response 200**:
```json
[
  {
    "id": 300,
    "classeId": 5,
    "itemConfig": {
      "id": 42,
      "nome": "Espada Longa",
      "raridade": { "id": 1, "nome": "Comum", "cor": "#9d9d9d" },
      "tipo": { "id": 1, "nome": "Espada Longa", "categoria": "ARMA" }
    },
    "obrigatorio": true,
    "grupoEscolha": null,
    "quantidade": 1
  },
  {
    "id": 301,
    "classeId": 5,
    "itemConfig": {
      "id": 50,
      "nome": "Arco Curto",
      "raridade": { "id": 1, "nome": "Comum", "cor": "#9d9d9d" },
      "tipo": { "id": 3, "nome": "Arco Curto", "categoria": "ARMA" }
    },
    "obrigatorio": false,
    "grupoEscolha": 1,
    "quantidade": 1
  },
  {
    "id": 302,
    "classeId": 5,
    "itemConfig": {
      "id": 55,
      "nome": "Besta Leve",
      "raridade": { "id": 1, "nome": "Comum", "cor": "#9d9d9d" },
      "tipo": { "id": 7, "nome": "Besta Leve", "categoria": "ARMA" }
    },
    "obrigatorio": false,
    "grupoEscolha": 1,
    "quantidade": 1
  }
]
```

> **Interpretacao de `grupoEscolha`**: items 301 e 302 tem `grupoEscolha: 1` -- o Jogador escolhe um entre eles. Item 300 tem `grupoEscolha: null` e `obrigatorio: true` -- concedido automaticamente (`RN-ITEM-15`, `RN-ITEM-17`).

**Erros**: 404 (classe nao encontrada)

---

### 6.2 Adicionar Equipamento Inicial

```
POST /api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Request Body** (`ClasseEquipamentoInicialRequest`):
```json
{
  "itemConfigId": 42,
  "obrigatorio": true,
  "grupoEscolha": null,
  "quantidade": 1
}
```

| Campo | Tipo | Obrigatorio | Validacao |
|-------|------|-------------|-----------|
| `itemConfigId` | Long | Sim | `@NotNull`, FK valida no mesmo jogo da classe |
| `obrigatorio` | Boolean | Sim | `@NotNull` |
| `grupoEscolha` | Integer | Nao | `@Min(1)` null se obrigatorio=true |
| `quantidade` | Integer | Nao | `@Min(1)` default 1 |

**Regras de validacao**:
- Se `obrigatorio = true`, `grupoEscolha` deve ser null (ignorado se enviado)
- Se `obrigatorio = false`, `grupoEscolha` deve ser informado (identifica o grupo de escolha)
- `itemConfigId` deve referenciar um item do MESMO jogo que a classe

**Response 201**: ClasseEquipamentoInicialResponse

**Erros**: 400, 403, 404 (classe ou itemConfig nao encontrado), 422 (itemConfig de outro jogo)

---

### 6.3 Atualizar Equipamento Inicial

```
PUT /api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais/{id}
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Request Body**: mesmo que `ClasseEquipamentoInicialRequest`

**Response 200**: ClasseEquipamentoInicialResponse atualizado

**Erros**: 400, 403, 404

---

### 6.4 Remover Equipamento Inicial

```
DELETE /api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais/{id}
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")`

**Response 204**

**Erros**: 403, 404

> **Nota**: remover um `ClasseEquipamentoInicial` NAO afeta fichas ja criadas -- apenas impacta novas criacoes de ficha.

---

## 7. FichaItem (Inventario do Personagem)

**Controller**: `FichaItemController`
**Base path**: `/api/v1/fichas/{fichaId}/itens`
**Tag Swagger**: `Fichas - Inventario`
**Service**: `FichaItemService`

> Este controller NAO segue o padrao de AbstractConfiguracaoService (nao e entidade de configuracao). Segue padrao semelhante ao FichaVantagemService / FichaVidaService.

### 7.1 Listar Inventario da Ficha

```
GET /api/v1/fichas/{fichaId}/itens?equipado={true|false}
```

**Autorizacao**: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

| Param | Tipo | Obrigatorio | Descricao |
|-------|------|-------------|-----------|
| `equipado` | Boolean | Nao | true = apenas equipados, false = apenas inventario, null = todos |

**Validacao de acesso** (`RN-ITEM-02`):
- JOGADOR: so pode ver itens da propria ficha
- MESTRE: pode ver itens de qualquer ficha do jogo

**Response 200**:
```json
{
  "fichaId": 1,
  "nomePersonagem": "Theron Valden",
  "pesoTotalEquipado": 5.30,
  "pesoTotalInventario": 12.80,
  "pesoTotal": 18.10,
  "capacidadeCarga": 45.00,
  "itens": [
    {
      "id": 500,
      "itemConfigId": 42,
      "nome": "Espada Longa +1",
      "equipado": true,
      "duracaoAtual": 8,
      "duracaoPadrao": 10,
      "quantidade": 1,
      "peso": 1.50,
      "notas": "Encontrada nas ruinas de Verath",
      "origem": "CATALOGO",
      "raridade": {
        "id": 2,
        "nome": "Incomum",
        "cor": "#1eff00"
      },
      "tipo": {
        "id": 1,
        "nome": "Espada Longa",
        "categoria": "ARMA",
        "subcategoria": "ESPADA",
        "requerDuasMaos": false
      },
      "efeitos": [
        {
          "tipoEfeito": "BONUS_DERIVADO",
          "bonusAlvoNome": "B.B.A",
          "valorFixo": 1,
          "descricaoEfeito": "+1 em B.B.A"
        }
      ],
      "adicionadoPor": "john.doe",
      "dataCriacao": "2026-04-04T10:30:00"
    },
    {
      "id": 501,
      "itemConfigId": null,
      "nome": "Amuleto Misterioso",
      "equipado": false,
      "duracaoAtual": null,
      "duracaoPadrao": null,
      "quantidade": 1,
      "peso": 0.10,
      "notas": "Presente do velho ermitao. Efeito desconhecido.",
      "origem": "CUSTOMIZADO",
      "raridade": {
        "id": 4,
        "nome": "Muito Raro",
        "cor": "#a335ee"
      },
      "tipo": null,
      "efeitos": [],
      "adicionadoPor": "mestre@game.com",
      "dataCriacao": "2026-04-04T11:00:00"
    },
    {
      "id": 502,
      "itemConfigId": 60,
      "nome": "Flecha",
      "equipado": false,
      "duracaoAtual": null,
      "duracaoPadrao": null,
      "quantidade": 20,
      "peso": 0.05,
      "notas": null,
      "origem": "CATALOGO",
      "raridade": {
        "id": 1,
        "nome": "Comum",
        "cor": "#9d9d9d"
      },
      "tipo": {
        "id": 15,
        "nome": "Flecha",
        "categoria": "CONSUMIVEL",
        "subcategoria": "MUNICAO",
        "requerDuasMaos": false
      },
      "efeitos": [],
      "adicionadoPor": "john.doe",
      "dataCriacao": "2026-04-04T12:00:00"
    }
  ]
}
```

> **JOIN FETCH obrigatorio** na `FichaItemRepository` para evitar N+1 (`RNF-01`).
> Campos `pesoTotalEquipado`, `pesoTotalInventario`, `pesoTotal` e `capacidadeCarga` sao calculados pelo service.

**Erros**: 403 (jogador tentando ver ficha alheia), 404 (ficha nao encontrada)

---

### 7.2 Adicionar Item do Catalogo

```
POST /api/v1/fichas/{fichaId}/itens
```

**Autorizacao**: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

**Request Body** (`AdicionarFichaItemRequest`):
```json
{
  "itemConfigId": 42,
  "quantidade": 1,
  "notas": "Encontrada nas ruinas de Verath",
  "forcarAdicao": false
}
```

| Campo | Tipo | Obrigatorio | Validacao |
|-------|------|-------------|-----------|
| `itemConfigId` | Long | Sim | `@NotNull`, item do mesmo jogo da ficha (`RN-ITEM-11`) |
| `quantidade` | Integer | Nao | `@Min(1)` default 1 |
| `notas` | String | Nao | `@Size(max=500)` |
| `forcarAdicao` | Boolean | Nao | default false; se true, ignora requisitos (`RN-ITEM-06`, somente MESTRE) |

**Regras de negocio aplicadas**:
1. `RN-ITEM-01`: JOGADOR so pode adicionar itens com `raridade.podeJogadorAdicionar = true` -- senao 403
2. `RN-ITEM-02`: JOGADOR so pode adicionar a propria ficha -- senao 403
3. `RN-ITEM-05`: valida requisitos do item (se `forcarAdicao = false`)
4. `RN-ITEM-06`: MESTRE pode usar `forcarAdicao = true` para ignorar requisitos; JOGADOR nao pode (campo ignorado)
5. `RN-ITEM-10`: `duracaoAtual` copiado do `ItemConfig.duracaoPadrao`
6. `RN-ITEM-11`: `ItemConfig` deve pertencer ao mesmo jogo da ficha
7. `RN-ITEM-19`: `origem = CATALOGO`
8. `RN-ITEM-20`: municoes empilham (incrementa quantidade se ja existe)

**Response 201**:
```json
{
  "id": 503,
  "itemConfigId": 42,
  "nome": "Espada Longa +1",
  "equipado": false,
  "duracaoAtual": 10,
  "duracaoPadrao": 10,
  "quantidade": 1,
  "peso": 1.50,
  "notas": "Encontrada nas ruinas de Verath",
  "origem": "CATALOGO",
  "raridade": {
    "id": 2,
    "nome": "Incomum",
    "cor": "#1eff00"
  },
  "tipo": {
    "id": 1,
    "nome": "Espada Longa",
    "categoria": "ARMA",
    "subcategoria": "ESPADA",
    "requerDuasMaos": false
  },
  "efeitos": [
    {
      "tipoEfeito": "BONUS_DERIVADO",
      "bonusAlvoNome": "B.B.A",
      "valorFixo": 1,
      "descricaoEfeito": "+1 em B.B.A"
    }
  ],
  "adicionadoPor": "john.doe",
  "dataCriacao": "2026-04-04T15:00:00"
}
```

**Erros**:
- 400: validacao falhou
- 403: JOGADOR tentando adicionar item de raridade restrita (`RN-ITEM-01`) ou em ficha alheia (`RN-ITEM-02`)
- 404: ficha ou itemConfig nao encontrado
- 422: requisito nao atendido (`RN-ITEM-05`) -- body inclui detalhes:

```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Requisitos nao atendidos para equipar este item",
  "detalhes": [
    "Atributo FOR atual (10) menor que o minimo (12)",
    "Nivel atual (2) menor que o minimo (3)"
  ]
}
```

---

### 7.3 Adicionar Item Customizado

```
POST /api/v1/fichas/{fichaId}/itens/customizado
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")` (somente Mestre -- `RN-ITEM-18`)

**Request Body** (`AdicionarFichaItemCustomizadoRequest`):
```json
{
  "nome": "Espada Amaldicoada de Zathur",
  "raridadeId": 5,
  "peso": 1.50,
  "quantidade": 1,
  "notas": "Criada pelo feiticeiro Zathur -- efeito unico"
}
```

| Campo | Tipo | Obrigatorio | Validacao |
|-------|------|-------------|-----------|
| `nome` | String | Sim | `@NotBlank @Size(max=100)` |
| `raridadeId` | Long | Sim | `@NotNull`, FK do mesmo jogo |
| `peso` | BigDecimal | Sim | `@NotNull @DecimalMin("0.00") @Digits(integer=3, fraction=2)` |
| `quantidade` | Integer | Nao | `@Min(1)` default 1 |
| `notas` | String | Nao | `@Size(max=500)` |

**Response 201**: FichaItemResponse com `itemConfigId: null`, `origem: "CUSTOMIZADO"`, `tipo: null`, `efeitos: []`

**Erros**: 400, 403, 404

> **Nota MVP**: itens customizados nao tem efeitos automaticos (`RN-ITEM-18`). Sao apenas informativos no calculo.

---

### 7.4 Equipar Item

```
PATCH /api/v1/fichas/{fichaId}/itens/{itemId}/equipar
```

**Autorizacao**: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

**Request Body**: nenhum (endpoint de acao)

**Regras de negocio aplicadas**:
1. `RN-ITEM-02`: JOGADOR so pode equipar itens da propria ficha
2. `RN-ITEM-05`: valida requisitos do ItemConfig (nivel, atributo, bonus, aptidao, vantagem, classe, raca)
3. `RN-ITEM-07`: item com `duracaoAtual == 0` retorna 422
4. `RN-ITEM-04`: seta `equipado = true` e dispara recalculo da ficha

**Response 200**: FichaItemResponse com `equipado: true`

**Erros**:
- 403: JOGADOR em ficha alheia
- 404: ficha ou item nao encontrado
- 422: item quebrado (`RN-ITEM-07`) ou requisito nao atendido (`RN-ITEM-05`)

> **Apos sucesso**: `FichaCalculationService.recalcular()` e chamado automaticamente. Os campos `itens` de `FichaAtributo`, `FichaBonus`, `FichaVida`, `FichaEssencia` sao atualizados (`RN-ITEM-22`).

---

### 7.5 Desequipar Item

```
PATCH /api/v1/fichas/{fichaId}/itens/{itemId}/desequipar
```

**Autorizacao**: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

**Request Body**: nenhum

**Regras**:
1. `RN-ITEM-02`: JOGADOR so na propria ficha
2. Seta `equipado = false` e dispara recalculo

**Response 200**: FichaItemResponse com `equipado: false`

**Erros**: 403, 404

> **Apos sucesso**: recalculo da ficha (bonus do item removidos).

---

### 7.6 Alterar Durabilidade

```
POST /api/v1/fichas/{fichaId}/itens/{itemId}/durabilidade
```

**Autorizacao**: `@PreAuthorize("hasRole('MESTRE')")` (somente Mestre -- `RN-ITEM-09`)

**Request Body** (`AlterarDurabilidadeRequest`):
```json
{
  "decremento": 2
}
```

**OU** (restaurar):
```json
{
  "restaurar": true
}
```

| Campo | Tipo | Obrigatorio | Validacao |
|-------|------|-------------|-----------|
| `decremento` | Integer | Condicional | `@Min(1)` Obrigatorio se `restaurar` nao informado |
| `restaurar` | Boolean | Condicional | Se true, restaura `duracaoAtual` para `duracaoPadrao` |

**Regras**:
- `decremento` e `restaurar` sao mutuamente exclusivos. Enviar ambos retorna 400.
- `duracaoAtual` nunca fica negativo (minimo 0).
- `restaurar` seta `duracaoAtual = ItemConfig.duracaoPadrao` (para itens de catalogo) ou o valor original (para customizados).
- Se item e indestrutivel (`duracaoPadrao == null`): **HTTP 422** "Item indestrutivel nao tem durabilidade"
- Se `duracaoAtual` chega a 0: `RN-ITEM-08` (desequipa automaticamente + recalculo)

**Response 200**:
```json
{
  "id": 500,
  "nome": "Espada Longa +1",
  "duracaoAtual": 6,
  "duracaoPadrao": 10,
  "equipado": true,
  "quebrado": false
}
```

**OU** (se quebrou):
```json
{
  "id": 500,
  "nome": "Espada Longa +1",
  "duracaoAtual": 0,
  "duracaoPadrao": 10,
  "equipado": false,
  "quebrado": true
}
```

**Erros**: 400, 403, 404, 422 (item indestrutivel)

---

### 7.7 Remover Item da Ficha

```
DELETE /api/v1/fichas/{fichaId}/itens/{itemId}
```

**Autorizacao**: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`

**Regras**:
1. `RN-ITEM-03`: MESTRE pode remover qualquer item de qualquer ficha
2. `RN-ITEM-02`: JOGADOR so pode remover da propria ficha
3. `RN-ITEM-16`: JOGADOR nao pode remover itens com `origem = INICIAL_CLASSE` e `obrigatorio = true`
4. `RN-ITEM-01`: JOGADOR nao pode remover itens de raridade com `podeJogadorAdicionar = false`
5. Se item estava equipado: desequipa automaticamente antes de remover (recalculo da ficha)

**Response 204**

**Erros**:
- 403: JOGADOR tentando remover item restrito (`RN-ITEM-03`, `RN-ITEM-16`) ou de ficha alheia
- 404: ficha ou item nao encontrado

---

## DTOs -- Records Java

### Records de Request (Configuracao)

```java
// CreateRaridadeItemRequest.java
public record CreateRaridadeItemRequest(
    @NotNull Long jogoId,
    @NotBlank @Size(max = 50) String nome,
    @NotBlank @Size(min = 4, max = 7) @Pattern(regexp = "^#[0-9a-fA-F]{3,6}$") String cor,
    Integer ordemExibicao,
    @NotNull Boolean podeJogadorAdicionar,
    @Min(0) Integer bonusAtributoMin,
    @Min(0) Integer bonusAtributoMax,
    @Min(0) Integer bonusDerivadoMin,
    @Min(0) Integer bonusDerivadoMax,
    @Size(max = 500) String descricao
) {}

// UpdateRaridadeItemRequest.java
public record UpdateRaridadeItemRequest(
    @Size(max = 50) String nome,
    @Size(min = 4, max = 7) @Pattern(regexp = "^#[0-9a-fA-F]{3,6}$") String cor,
    Boolean podeJogadorAdicionar,
    @Min(0) Integer bonusAtributoMin,
    @Min(0) Integer bonusAtributoMax,
    @Min(0) Integer bonusDerivadoMin,
    @Min(0) Integer bonusDerivadoMax,
    @Size(max = 500) String descricao
) {}

// CreateTipoItemRequest.java
public record CreateTipoItemRequest(
    @NotNull Long jogoId,
    @NotBlank @Size(max = 100) String nome,
    @NotBlank @Size(max = 50) String categoria,
    @Size(max = 50) String subcategoria,
    Boolean requerDuasMaos,
    Integer ordemExibicao,
    @Size(max = 300) String descricao
) {}

// UpdateTipoItemRequest.java
public record UpdateTipoItemRequest(
    @Size(max = 100) String nome,
    @Size(max = 50) String categoria,
    @Size(max = 50) String subcategoria,
    Boolean requerDuasMaos,
    @Size(max = 300) String descricao
) {}

// CreateItemConfigRequest.java
public record CreateItemConfigRequest(
    @NotNull Long jogoId,
    @NotBlank @Size(max = 100) String nome,
    @NotNull Long raridadeId,
    @NotNull Long tipoId,
    @NotNull @DecimalMin("0.00") @Digits(integer = 3, fraction = 2) BigDecimal peso,
    @Min(0) Integer valor,
    @Min(1) Integer duracaoPadrao,
    @Min(1) Integer nivelMinimo,
    @Size(max = 1000) String propriedades,
    @Size(max = 2000) String descricao,
    Integer ordemExibicao
) {}

// UpdateItemConfigRequest.java
public record UpdateItemConfigRequest(
    @Size(max = 100) String nome,
    Long raridadeId,
    Long tipoId,
    @DecimalMin("0.00") @Digits(integer = 3, fraction = 2) BigDecimal peso,
    @Min(0) Integer valor,
    @Min(1) Integer duracaoPadrao,
    @Min(1) Integer nivelMinimo,
    @Size(max = 1000) String propriedades,
    @Size(max = 2000) String descricao
) {}

// CriarItemEfeitoRequest.java
public record CriarItemEfeitoRequest(
    @NotNull TipoItemEfeito tipoEfeito,
    Long atributoAlvoId,
    Long aptidaoAlvoId,
    Long bonusAlvoId,
    Integer valorFixo,
    @Size(max = 200) String formula,
    @Size(max = 300) String descricaoEfeito
) {}

// CriarItemRequisitoRequest.java
public record CriarItemRequisitoRequest(
    @NotNull TipoRequisito tipo,
    @Size(max = 50) String alvo,
    @Min(1) Integer valorMinimo
) {}

// ClasseEquipamentoInicialRequest.java
public record ClasseEquipamentoInicialRequest(
    @NotNull Long itemConfigId,
    @NotNull Boolean obrigatorio,
    @Min(1) Integer grupoEscolha,
    @Min(1) Integer quantidade
) {}
```

### Records de Request (Ficha)

```java
// AdicionarFichaItemRequest.java
public record AdicionarFichaItemRequest(
    @NotNull Long itemConfigId,
    @Min(1) Integer quantidade,
    @Size(max = 500) String notas,
    Boolean forcarAdicao
) {}

// AdicionarFichaItemCustomizadoRequest.java
public record AdicionarFichaItemCustomizadoRequest(
    @NotBlank @Size(max = 100) String nome,
    @NotNull Long raridadeId,
    @NotNull @DecimalMin("0.00") @Digits(integer = 3, fraction = 2) BigDecimal peso,
    @Min(1) Integer quantidade,
    @Size(max = 500) String notas
) {}

// AlterarDurabilidadeRequest.java
public record AlterarDurabilidadeRequest(
    @Min(1) Integer decremento,
    Boolean restaurar
) {}
```

### Records de Response

```java
// RaridadeItemResponse.java
public record RaridadeItemResponse(
    Long id,
    Long jogoId,
    String nome,
    String cor,
    Integer ordemExibicao,
    Boolean podeJogadorAdicionar,
    Integer bonusAtributoMin,
    Integer bonusAtributoMax,
    Integer bonusDerivadoMin,
    Integer bonusDerivadoMax,
    String descricao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}

// TipoItemResponse.java
public record TipoItemResponse(
    Long id,
    Long jogoId,
    String nome,
    String categoria,
    String subcategoria,
    Boolean requerDuasMaos,
    Integer ordemExibicao,
    String descricao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}

// RaridadeItemResumoResponse.java (para uso inline em outros responses)
public record RaridadeItemResumoResponse(
    Long id,
    String nome,
    String cor
) {}

// TipoItemResumoResponse.java (para uso inline em outros responses)
public record TipoItemResumoResponse(
    Long id,
    String nome,
    String categoria,
    String subcategoria,
    Boolean requerDuasMaos
) {}

// ItemConfigResponse.java (GET por ID -- completo)
public record ItemConfigResponse(
    Long id,
    Long jogoId,
    String nome,
    RaridadeItemResumoResponse raridade,
    TipoItemResumoResponse tipo,
    BigDecimal peso,
    Integer valor,
    Integer duracaoPadrao,
    Integer nivelMinimo,
    String propriedades,
    String descricao,
    Integer ordemExibicao,
    List<ItemEfeitoResponse> efeitos,
    List<ItemRequisitoResponse> requisitos,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}

// ItemConfigListaResponse.java (GET listagem -- sem sub-recursos)
public record ItemConfigListaResponse(
    Long id,
    Long jogoId,
    String nome,
    RaridadeItemResumoResponse raridade,
    TipoItemResumoResponse tipo,
    BigDecimal peso,
    Integer valor,
    Integer duracaoPadrao,
    Integer nivelMinimo,
    String propriedades,
    String descricao,
    Integer ordemExibicao,
    Integer quantidadeEfeitos,
    Integer quantidadeRequisitos,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}

// ItemEfeitoResponse.java
public record ItemEfeitoResponse(
    Long id,
    Long itemConfigId,
    TipoItemEfeito tipoEfeito,
    Long atributoAlvoId,
    String atributoAlvoNome,
    Long aptidaoAlvoId,
    String aptidaoAlvoNome,
    Long bonusAlvoId,
    String bonusAlvoNome,
    Integer valorFixo,
    String formula,
    String descricaoEfeito,
    LocalDateTime dataCriacao
) {}

// ItemRequisitoResponse.java
public record ItemRequisitoResponse(
    Long id,
    Long itemConfigId,
    TipoRequisito tipo,
    String alvo,
    Integer valorMinimo,
    String descricaoLegivel
) {}

// ClasseEquipamentoInicialResponse.java
public record ClasseEquipamentoInicialResponse(
    Long id,
    Long classeId,
    ItemConfigResumoResponse itemConfig,
    Boolean obrigatorio,
    Integer grupoEscolha,
    Integer quantidade
) {}

// ItemConfigResumoResponse.java (resumo para uso em equipamentos iniciais e fichaItem)
public record ItemConfigResumoResponse(
    Long id,
    String nome,
    RaridadeItemResumoResponse raridade,
    TipoItemResumoResponse tipo
) {}

// FichaItemResponse.java (item individual no inventario)
public record FichaItemResponse(
    Long id,
    Long itemConfigId,
    String nome,
    Boolean equipado,
    Integer duracaoAtual,
    Integer duracaoPadrao,
    Integer quantidade,
    BigDecimal peso,
    String notas,
    OrigemFichaItem origem,
    RaridadeItemResumoResponse raridade,
    TipoItemResumoResponse tipo,
    List<FichaItemEfeitoResponse> efeitos,
    String adicionadoPor,
    LocalDateTime dataCriacao
) {}

// FichaItemEfeitoResponse.java (efeito simplificado para exibicao no inventario)
public record FichaItemEfeitoResponse(
    TipoItemEfeito tipoEfeito,
    String alvoNome,
    Integer valorFixo,
    String descricaoEfeito
) {}

// FichaInventarioResponse.java (wrapper do GET /fichas/{fichaId}/itens)
public record FichaInventarioResponse(
    Long fichaId,
    String nomePersonagem,
    BigDecimal pesoTotalEquipado,
    BigDecimal pesoTotalInventario,
    BigDecimal pesoTotal,
    BigDecimal capacidadeCarga,
    List<FichaItemResponse> itens
) {}

// DurabilidadeResponse.java (resposta do POST durabilidade)
public record DurabilidadeResponse(
    Long id,
    String nome,
    Integer duracaoAtual,
    Integer duracaoPadrao,
    Boolean equipado,
    Boolean quebrado
) {}
```

### Enums

```java
// TipoItemEfeito.java
public enum TipoItemEfeito {
    BONUS_ATRIBUTO,
    BONUS_APTIDAO,
    BONUS_DERIVADO,
    BONUS_VIDA,
    BONUS_ESSENCIA,
    FORMULA_CUSTOMIZADA,
    EFEITO_DADO
}

// TipoRequisito.java
public enum TipoRequisito {
    NIVEL,
    ATRIBUTO,
    BONUS,
    APTIDAO,
    VANTAGEM,
    CLASSE,
    RACA
}

// OrigemFichaItem.java
public enum OrigemFichaItem {
    CATALOGO,
    CUSTOMIZADO,
    INICIAL_CLASSE,
    INICIAL_RACA
}
```

---

## Resumo de Controllers e Services

| Controller | Base Path | Service | Estende AbstractConfiguracaoService? |
|------------|-----------|---------|-------------------------------------|
| `RaridadeItemController` | `/api/v1/configuracoes/raridades-item` | `RaridadeItemConfiguracaoService` | Sim |
| `TipoItemController` | `/api/v1/configuracoes/tipos-item` | `TipoItemConfiguracaoService` | Sim |
| `ItemConfigController` | `/api/v1/configuracoes/itens` | `ItemConfiguracaoService` | Sim |
| `ItemEfeitoController` | `.../itens/{itemId}/efeitos` | `ItemEfeitoService` | Nao (sub-recurso) |
| `ItemRequisitoController` | `.../itens/{itemId}/requisitos` | `ItemRequisitoService` | Nao (sub-recurso) |
| `ClasseController` (existente) | `.../classes/{classeId}/equipamentos-iniciais` | `ClasseConfiguracaoService` | Sim (ja existe) |
| `FichaItemController` | `/api/v1/fichas/{fichaId}/itens` | `FichaItemService` | Nao (entidade de ficha) |

---

## Decisoes de Design Tomadas pelo Tech Lead

### D-016-01: URL pattern para configuracoes de item

Seguimos o padrao existente do projeto: `/api/v1/configuracoes/{recurso}` com `jogoId` como query param.
Isto e consistente com todos os 13 CRUDs de configuracao existentes (atributos, aptidoes, bonus, classes, etc.).

Alternativa rejeitada: `/api/v1/jogos/{jogoId}/raridades-item` -- nao segue o padrao existente.

### D-016-02: Efeitos e Requisitos como sub-recurso, nao inline no ItemConfig

Efeitos e Requisitos tem controllers separados (como VantagemEfeitoController).
Motivo: permite CRUD granular sem reenviar o item inteiro; consistente com o padrao VantagemEfeito.

### D-016-03: FichaItem response inclui efeitos inline (nao em sub-recurso separado)

No inventario da ficha, os efeitos do item SAO retornados inline (lista simplificada `FichaItemEfeitoResponse`).
Motivo: o frontend precisa exibir os bonus de cada item sem request extra; sao poucos efeitos por item.

### D-016-04: ClasseEquipamentoInicial no ClasseController existente

Seguimos o padrao de `bonus` e `aptidao-bonus`: sub-recurso no mesmo controller.
NAO criamos controller separado.
Motivo: consistencia com ClasseBonus e ClasseAptidaoBonus.

### D-016-05: Dois responses para ItemConfig (lista vs detalhe)

`ItemConfigListaResponse` (listagem) retorna contagens de efeitos/requisitos.
`ItemConfigResponse` (detalhe/GET por ID) retorna efeitos e requisitos completos.
Motivo: performance na listagem (evita carregar sub-recursos de todos os itens).

### D-016-06: FichaInventarioResponse como wrapper

O GET do inventario retorna um wrapper com metadados (peso total, capacidade) e a lista de itens.
Motivo: frontend precisa desses calculos sem request separado.

### D-016-07: Enum TipoItemEfeito separado de TipoEfeito existente

Apesar de similar ao `TipoEfeito` de `VantagemEfeito`, criamos enum separado `TipoItemEfeito`.
Motivo: tipos diferentes (item nao tem BONUS_VIDA_MEMBRO nem valorPorNivel), e sealed interface seria overengineering neste ponto.

---

*Produzido por: Tech Lead Backend | 2026-04-04*
*Revisado contra: padroes existentes de AtributoController, VantagemEfeitoController, ClasseController, FichaController*
