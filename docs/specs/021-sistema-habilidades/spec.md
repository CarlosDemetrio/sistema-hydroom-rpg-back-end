# Spec 021 — Sistema de Habilidades (HabilidadeConfig)

> Spec: `021-sistema-habilidades`
> Epic: EPIC 2 (Configuracoes do Jogo)
> Status: Pendente
> Depende de: Spec 001 (Jogo entity — implementado)
> Bloqueia: nenhuma spec

---

## 1. Visao Geral do Negocio

**Problema resolvido:** O sistema nao tem um mecanismo para que Mestres ou Jogadores registrem habilidades de personagem (ataques, tecnicas, magias, manobras) vinculadas a um Jogo. Atualmente nao ha entidade de configuracao para descrever o nome de uma habilidade, seu efeito narrativo e o dano que ela causa.

**Decisao do PO (2026-04-09):**
> "Apenas nome, descricao, dano/efeito (texto), e libere para jogador tambem criar."

**Objetivo:** Implementar a entidade `HabilidadeConfig` como uma configuracao do jogo, permitindo que Mestres e Jogadores cadastrem, editem e removam habilidades com nome, descricao narrativa e descricao de dano/efeito em texto livre.

**Diferenca critica em relacao as 13 configs existentes:**
- Nas 13 entidades de configuracao existentes: apenas MESTRE pode criar, editar e deletar.
- `HabilidadeConfig`: **MESTRE e JOGADOR** tem acesso total de escrita (GET, POST, PUT, DELETE).

**Valor entregue:**
- Mestre pode catalogar habilidades padrao do mundo do jogo (ex: "Golpe Brutal — 2D6+FOR de dano fisico").
- Jogador pode registrar habilidades proprias do personagem sem depender do Mestre para cada cadastro.
- Habilidades ficam disponiveis como referencia consultavel dentro do contexto do jogo.

---

## 2. Atores Envolvidos

| Ator | Role | Acoes permitidas |
|------|------|-----------------|
| Mestre | MESTRE | Criar, listar, buscar por ID, editar, deletar habilidades do jogo |
| Jogador | JOGADOR | Criar, listar, buscar por ID, editar, deletar habilidades do jogo |
| Sistema | — | Garantir unicidade de nome por jogo; aplicar soft delete |

---

## 3. Modelo de Dados

### Entidade: `HabilidadeConfig`

Herda de `BaseEntity` (soft delete via `deleted_at`, campos de auditoria `created_at`, `updated_at`, `created_by`, `updated_by`, `@SQLRestriction("deleted_at IS NULL")`).
Implementa `ConfiguracaoEntity` (exige `getId()` e `getJogo()`).

| Campo | Tipo Java | Coluna SQL | Nullable | Restricoes |
|-------|-----------|-----------|----------|-----------|
| `id` | `Long` | `id` | nao | PK, auto-increment (herdado de BaseEntity) |
| `jogo` | `Jogo` | `jogo_id` | nao | FK → jogos; parte do unique constraint |
| `nome` | `String` | `nome` | nao | `@NotBlank`, `@Size(max=100)`, unique `(jogo_id, nome)` |
| `descricao` | `String` | `descricao` | sim | `@Size(max=1000)` |
| `danoEfeito` | `String` | `dano_efeito` | sim | `@Size(max=500)`, texto livre (ex: "2D6 de fogo") |
| `ordemExibicao` | `Integer` | `ordem_exibicao` | nao | `@NotNull`, `@Min(0)` |

**Constraint unica:** `UNIQUE (jogo_id, nome)` — duas habilidades no mesmo jogo nao podem ter o mesmo nome.

**Sem sigla/abreviacao:** `HabilidadeConfig` nao participa do namespace de siglas cross-entity (valido apenas para AtributoConfig, BonusConfig, VantagemConfig).

---

## 4. Requisitos Funcionais

**RF-001** Um usuario autenticado com role MESTRE ou JOGADOR pode criar uma `HabilidadeConfig` em um Jogo existente ao qual tenha acesso.

**RF-002** O campo `nome` e obrigatorio, nao pode ser vazio e deve ser unico dentro do jogo (case-sensitive). Tentativa de criar com nome duplicado deve retornar HTTP 409.

**RF-003** Os campos `descricao` e `danoEfeito` sao opcionais e aceitam texto livre. `danoEfeito` e semanticamente destinado a descrever dano ou efeito mecanico da habilidade (ex: "2D6+FOR de dano fisico"), mas o sistema nao valida seu conteudo — e apenas texto.

**RF-004** O campo `ordemExibicao` e obrigatorio e controla a ordem de listagem.

**RF-005** Um usuario com role MESTRE ou JOGADOR pode listar todas as `HabilidadeConfig` de um Jogo, ordenadas por `ordemExibicao`.

**RF-006** Um usuario com role MESTRE ou JOGADOR pode buscar uma `HabilidadeConfig` especifica por ID.

**RF-007** Um usuario com role MESTRE ou JOGADOR pode editar qualquer campo de uma `HabilidadeConfig` existente no jogo. A edicao de nome deve revalidar a unicidade.

**RF-008** Um usuario com role MESTRE ou JOGADOR pode deletar (soft delete) uma `HabilidadeConfig`. O registro fica invisivel para todas as listagens mas permanece no banco.

**RF-009** `HabilidadeConfig` deletada nao aparece em listagens nem em buscas por ID (comportamento padrao do `@SQLRestriction`).

**RF-010** Tentativa de acesso a um jogo ao qual o usuario nao tem acesso deve retornar HTTP 403 (regra de acesso ao jogo — tratada pela camada de seguranca existente).

---

## 5. Requisitos Nao Funcionais

**RNF-001 — Seguranca:** Todos os endpoints exigem autenticacao. Role minima para qualquer operacao e `JOGADOR`. Nao ha operacao exclusiva de MESTRE nesta entidade.

**RNF-002 — Consistencia:** Soft delete — registros nunca sao fisicamente removidos. `@SQLRestriction("deleted_at IS NULL")` garante invisibilidade automatica.

**RNF-003 — Padroes do projeto:** Seguir o padrao das 13 configs existentes: `AbstractConfiguracaoService`, MapStruct, records de DTO, `@Transactional(readOnly=true)` na classe + `@Transactional` em escritas.

**RNF-004 — Testes:** Cobertura via `BaseConfiguracaoServiceIntegrationTest` (~10 cenarios automaticos) + cenarios adicionais especificos da regra de nome duplicado.

---

## 6. Endpoints REST Propostos

Base path: `/api/v1/jogos/{jogoId}/habilidades`

| Metodo | Path | Descricao | Role |
|--------|------|-----------|------|
| `GET` | `/api/v1/jogos/{jogoId}/habilidades` | Listar todas as habilidades do jogo | MESTRE ou JOGADOR |
| `GET` | `/api/v1/jogos/{jogoId}/habilidades/{id}` | Buscar habilidade por ID | MESTRE ou JOGADOR |
| `POST` | `/api/v1/jogos/{jogoId}/habilidades` | Criar nova habilidade | MESTRE ou JOGADOR |
| `PUT` | `/api/v1/jogos/{jogoId}/habilidades/{id}` | Atualizar habilidade existente | MESTRE ou JOGADOR |
| `DELETE` | `/api/v1/jogos/{jogoId}/habilidades/{id}` | Soft delete da habilidade | MESTRE ou JOGADOR |

**Codigos HTTP:**

| Situacao | Status |
|----------|--------|
| Criacao com sucesso | 201 Created |
| Leitura/Listagem com sucesso | 200 OK |
| Atualizacao com sucesso | 200 OK |
| Delecao com sucesso | 204 No Content |
| Nome duplicado no jogo | 409 Conflict |
| Habilidade nao encontrada | 404 Not Found |
| Jogo nao encontrado | 404 Not Found |
| Sem autenticacao | 401 Unauthorized |
| Sem permissao (role incorreta) | 403 Forbidden |
| Campos invalidos | 422 Unprocessable Entity |

---

## 7. DTOs

### CreateHabilidadeConfigDTO (request)

```
nome          String   obrigatorio   @NotBlank, @Size(max=100)
descricao     String   opcional      @Size(max=1000)
danoEfeito    String   opcional      @Size(max=500)
ordemExibicao Integer  obrigatorio   @NotNull, @Min(0)
```

### UpdateHabilidadeConfigDTO (request)

```
nome          String   opcional   @Size(max=100), se presente: @NotBlank
descricao     String   opcional   @Size(max=1000)
danoEfeito    String   opcional   @Size(max=500)
ordemExibicao Integer  opcional   @Min(0)
```

Campos null no update sao ignorados (`NullValuePropertyMappingStrategy.IGNORE` no MapStruct).

### HabilidadeConfigResponseDTO (response)

```
id                    Long
jogoId                Long
nome                  String
descricao             String   (null se nao preenchido)
danoEfeito            String   (null se nao preenchido)
ordemExibicao         Integer
dataCriacao           LocalDateTime
dataUltimaAtualizacao LocalDateTime
```

---

## 8. Regras de Negocio Criticas

**RN-001 — Nome unico por jogo:** Duas `HabilidadeConfig` no mesmo jogo nao podem ter o mesmo nome. A validacao deve ocorrer tanto na criacao (POST) quanto na atualizacao (PUT). O servico deve lancar `ConfiguracaoDuplicadaException` com mensagem clara, resultando em HTTP 409.

**RN-002 — Permissoes simetricas:** Diferente de todas as outras configs do sistema, JOGADOR tem as mesmas permissoes que MESTRE nesta entidade. `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")` deve ser aplicado a todos os endpoints (inclusive POST, PUT, DELETE).

**RN-003 — Sem namespace de siglas:** `HabilidadeConfig` nao tem campo `sigla` ou `abreviacao`. A validacao cross-entity de siglas por jogo NAO se aplica a esta entidade.

**RN-004 — Soft delete padrao:** Deletar uma habilidade aplica `deleted_at = now()`. O registro permanece no banco. `@SQLRestriction("deleted_at IS NULL")` torna o registro invisivel para todas as queries JPA automaticamente.

**RN-005 — Escopo de jogo:** Habilidades sao sempre criadas dentro do contexto de um `Jogo`. O `jogo_id` e obrigatorio e deve existir. Nao ha habilidades "globais" desvinculadas de jogo.

**RN-006 — danoEfeito e texto livre:** O sistema nao interpreta, valida nem processa o conteudo de `danoEfeito`. Nao ha integracao com `FormulaEvaluatorService`. O campo e puramente descritivo/narrativo.

---

## 9. Criterios de Aceitacao

### Cenario 1: Criar habilidade com sucesso (MESTRE)
Dado que um usuario com role MESTRE esta autenticado
E o jogo `{jogoId}` existe
Quando envia POST `/api/v1/jogos/{jogoId}/habilidades` com `{ nome: "Golpe Brutal", danoEfeito: "2D6+FOR", ordemExibicao: 1 }`
Entao recebe HTTP 201
E o corpo da resposta contem `id`, `nome: "Golpe Brutal"`, `danoEfeito: "2D6+FOR"`, `jogoId`

### Cenario 2: Criar habilidade com sucesso (JOGADOR)
Dado que um usuario com role JOGADOR esta autenticado
E o jogo `{jogoId}` existe
Quando envia POST `/api/v1/jogos/{jogoId}/habilidades` com `{ nome: "Chute Giratório", ordemExibicao: 2 }`
Entao recebe HTTP 201
E a habilidade e criada normalmente (mesmo comportamento que MESTRE)

### Cenario 3: Nome duplicado
Dado que ja existe uma `HabilidadeConfig` com nome "Golpe Brutal" no jogo `{jogoId}`
Quando envia POST com `{ nome: "Golpe Brutal", ordemExibicao: 3 }`
Entao recebe HTTP 409 Conflict

### Cenario 4: Listar habilidades
Dado que existem 3 habilidades no jogo `{jogoId}`
Quando envia GET `/api/v1/jogos/{jogoId}/habilidades`
Entao recebe HTTP 200
E o corpo e uma lista com 3 habilidades ordenadas por `ordemExibicao`

### Cenario 5: Soft delete
Dado que existe uma `HabilidadeConfig` com `id=42` no jogo `{jogoId}`
Quando envia DELETE `/api/v1/jogos/{jogoId}/habilidades/42`
Entao recebe HTTP 204
E GET `/api/v1/jogos/{jogoId}/habilidades/42` retorna HTTP 404
E GET `/api/v1/jogos/{jogoId}/habilidades` nao inclui a habilidade deletada

### Cenario 6: Campo descricao e danoEfeito opcionais
Dado que um usuario autenticado envia POST apenas com `nome` e `ordemExibicao`
Entao recebe HTTP 201
E `descricao` e `danoEfeito` sao null na resposta

### Cenario 7: Acesso sem autenticacao
Dado que a requisicao nao contem sessao autenticada
Quando envia GET `/api/v1/jogos/{jogoId}/habilidades`
Entao recebe HTTP 401

---

## 10. Pontos em Aberto

**PA-021-01 — Vinculo com Ficha:** Habilidades sao apenas descricoes textuais de configuracao, ou em specs futuras serao vinculadas a fichas (ex: `FichaHabilidade`)? Esta spec nao cria vinculo com ficha — apenas a entidade de configuracao. Confirmar se ha plano de vinculacao.

**PA-021-02 — Edicao cruzada:** Um Jogador pode editar ou deletar uma habilidade criada pelo Mestre (ou por outro Jogador)? A spec atual nao restringe — qualquer MESTRE ou JOGADOR pode editar qualquer habilidade do jogo. Se o PO quiser restricao de "apenas o criador pode editar", a entidade precisaria de campo `criadorId`.

**PA-021-03 — Frontend — contexto de acesso:** A tela de gerenciamento de habilidades deve aparecer no painel de configuracoes do Mestre (junto com as 13 outras configs) e tambem para o Jogador? Ou o Jogador acessa por um caminho diferente (ex: na propria ficha do personagem)?

---

*Produzido por: Business Analyst/PO | 2026-04-12*
