# Briefing Copilot R07 — Finalizar DefaultGameConfigProvider + Spec Habilidades

> Criado: 2026-04-09 pelo Copilot CLI
> Para execução: próxima sessão Copilot
> Pré-requisito: branch `main` limpa, HEAD `b707d90`

---

## Contexto

R06 ficou parcial. Dois itens pendentes chegam para R07:

1. **Finalizar `getDefaultVantagens()`** — ainda tem 33 placeholders errados no provider
2. **Spec + implementação de Habilidades** — sistema ainda não existe no backend

---

## Task 1 — Finalizar getDefaultVantagens() [WIP de R06]

**Tipo:** Backend
**Arquivo:** `src/main/java/.../config/DefaultGameConfigProviderImpl.java`
**CSV fonte:** `docs/revisao-game-default/csv/17-vantagem-config.csv`

### Problema atual

`getDefaultVantagens()` (linha ~313) tem 33 entradas placeholder com:
- Nomes errados ("Treinamento de Combate Ofensivo" em vez de "Treinamento em Combate Ofensivo")
- `formulaCusto = "custo_base * nivel_vantagem"` — variável `custo_base` **não existe** no `FormulaEvaluatorService` (válidos: `nivel`, `base`, `total`, `FOR`, `AGI`, `VIG`, `SAB`, `INT`, `INTU`, `AST`)
- Sem `sigla`, sem `tipoVantagem`, sem `categoriaNome`

### O que fazer

Substituir **todo o corpo** de `getDefaultVantagens()` pelos 64 vantagens canônicas do CSV.

**Campos do builder a usar:**

```java
VantagemConfigDTO.builder()
    .sigla(...)                  // coluna sigla do CSV (2-5 chars)
    .nome(...)                   // coluna nome
    .descricao(...)              // coluna descricao
    .nivelMaximoVantagem(...)    // coluna nivel_maximo (Integer, null se vazio)
    .formulaCusto(...)           // coluna formula_custo (ex: "4", "nivel * 5", "0")
    .valorBonusFormula(...)      // coluna descricao_efeito (texto do efeito)
    .tipoVantagem(...)           // coluna tipo_vantagem: "VANTAGEM" ou "INSOLITUS"
    .categoriaNome(...)          // coluna categoria_nome (deve bater com getDefaultCategoriasVantagem())
    .nivelMinimoPersonagem(1)    // padrão 1 para todas
    .podeEvoluir(true)           // padrão true para todas
    .ordemExibicao(...)          // coluna ordem_exibicao
    .build()
```

> ⚠️ **Campos a NÃO usar:** `custoBase`, `tipoBonus` — o initializer não lê esses campos

**Categorias válidas** (devem bater exatamente):
- `Treinamento Físico`
- `Treinamento Mental`
- `Ação`
- `Reação`
- `Vantagem de Atributo`
- `Vantagem Geral`
- `Vantagem Histórica`
- `Vantagem de Renascimento`
- `Vantagem Racial`

**Total: 64 vantagens** (47 VANTAGEM + 17 INSOLITUS)

### Validação

- `./mvnw compile` — deve compilar sem erros
- `./mvnw test` — manter 743 testes passando

---

## Task 2 — Spec + Implementação de Habilidades (backend simplificado)

**Tipo:** Backend (spec + entidade + CRUD)
**Escopo SIMPLIFICADO** (definido pelo PO em 2026-04-09):

> "Apenas nome, descrição, dano/efeito (texto mesmo), e libere para o próprio usuário também criar habilidades, caso não queira nenhuma dali. Assim o mestre apenas escreve o dano/efeito. Deixa pra depois toda essa parte de requisitos, por classe, raça, vantagem e etc..."

### O que implementar

**Entidade: `HabilidadeConfig`**
- `nome` — String, obrigatório
- `descricao` — String, opcional
- `danoEfeito` — String (texto livre, ex: "2D6 de dano de fogo", "Paralisa o alvo por 1 turno")
- `criadorPadrao` — enum: `MESTRE` | `USUARIO` (indica se é padrão do sistema ou criada pelo usuário/Mestre)
- Herda de `BaseEntity` (`id`, `createdAt`, `updatedAt`, `deletedAt`, `jogo`)
- Implementa `ConfiguracaoEntity`
- Unique constraint: `(jogo_id, nome)`

**Fluxo padrão:** seguir exatamente o padrão das 13 entidades de configuração existentes:
1. `HabilidadeConfig` entity (extends `BaseEntity`, implements `ConfiguracaoEntity`)
2. `HabilidadeConfigRepository` (extends `JpaRepository`)
3. `HabilidadeConfigService` (extends `AbstractConfiguracaoService`)
4. DTOs: `CreateHabilidadeConfigDTO` (record), `UpdateHabilidadeConfigDTO` (record), `HabilidadeConfigResponseDTO` (record)
5. `HabilidadeConfigMapper` (MapStruct)
6. `HabilidadeConfigController` (thin layer, segue `AtributoController`)
7. Migration Flyway: `V{next}__create_habilidade_config.sql`
8. Teste: extends `BaseConfiguracaoServiceIntegrationTest`

**Permissões:**
- `POST`, `PUT`, `DELETE` → `hasAnyRole('MESTRE', 'JOGADOR')` ← Diferente das outras configs! Jogador pode criar suas próprias.
- `GET` → `hasAnyRole('MESTRE', 'JOGADOR')`

**Não implementar agora:**
- Requisitos por classe, raça ou vantagem
- Vinculação de habilidade a ficha de personagem
- Cálculo automático de dano

### Critério de aceite

- Compilação limpa
- `./mvnw test` — todos os testes passando + novos testes da habilidade (mínimo: `BaseConfiguracaoServiceIntegrationTest` herdado)
- CRUD funcional via Swagger (`/swagger-ui.html`)

---

## Sequenciamento

```
Task 1 — finalizar getDefaultVantagens() (sem dependências, pode fazer primeiro)
Task 2 — Spec + impl HabilidadeConfig (independente)
```

As duas tasks podem rodar em paralelo com agentes diferentes.

---

## Referências úteis

- CSV vantagens: `docs/revisao-game-default/csv/17-vantagem-config.csv`
- Padrão de entidade: `AtributoConfig`, `BonusConfig` (mais simples)
- Padrão de controller: `AtributoController`
- Padrão de teste: `AtributoConfiguracaoServiceIntegrationTest`
- Segurança MESTRE+JOGADOR (escrita): sem precedente direto — definir explicitamente no controller

---

*Briefing R07 criado em 2026-04-09.*
