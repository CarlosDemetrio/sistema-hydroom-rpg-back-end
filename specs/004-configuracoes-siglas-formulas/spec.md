# Feature Specification: Aprofundamento das Configurações — Siglas, Fórmulas e Relacionamentos

**Feature Branch**: `004-configuracoes-siglas-formulas`
**Created**: 2026-03-08
**Status**: Draft
**Epics cobertos**: EPIC 1 (Aprofundamento das Configurações Existentes) + EPIC 2 (Motor de Fórmulas e Siglas)
**Input**: As 13 configurações têm CRUD básico, mas faltam os relacionamentos, campos e validações que o glossário exige. O FormulaEvaluatorService existe, mas não está integrado ao ciclo de vida das configurações.

---

## User Stories & Testing *(mandatory)*

### Story 1 — Unicidade de Siglas por Jogo (Priority: P1)

Como Mestre, não consigo criar duas configurações no mesmo jogo com a mesma sigla/abreviação, independente do tipo de configuração, para que as fórmulas do motor de cálculo funcionem corretamente.

**Por que P1**: Siglas são as variáveis do motor de fórmulas. Duplicidade torna o sistema de cálculo imprevisível e quebra as fichas dos jogadores.

**Teste independente**: Criar um atributo com abreviação "FOR", depois tentar criar um bônus, vantagem ou outro atributo no mesmo jogo com sigla "FOR" — todos devem ser rejeitados com erro de conflito.

**Acceptance Scenarios**:

1. **Given** um jogo com um atributo de abreviação "FOR", **When** o Mestre tenta criar um BonusConfig com sigla "FOR" no mesmo jogo, **Then** o sistema rejeita com erro de conflito indicando que a sigla já está em uso.
2. **Given** um jogo com um BonusConfig de sigla "BBA", **When** o Mestre edita uma VantagemConfig para usar sigla "BBA", **Then** o sistema rejeita com erro de conflito.
3. **Given** um jogo com sigla "FOR" em uso, **When** o Mestre consulta `GET /api/jogos/{id}/siglas`, **Then** o sistema retorna a lista de todas as siglas em uso com tipo e entidade de origem.
4. **Given** um Mestre remove uma config com sigla "FOR", **When** consulta a lista de siglas, **Then** "FOR" não aparece mais.

---

### Story 2 — Validação de Fórmulas ao Salvar (Priority: P1)

Como Mestre, o sistema me avisa imediatamente se digitar uma fórmula inválida ou que usa variáveis não existentes no jogo, para que eu não salve configurações quebradas que só vão falhar no momento de calcular a ficha.

**Por que P1**: Fórmulas inválidas só são detectadas na hora do cálculo, que acontece na ficha do jogador — erro custoso e de difícil rastreamento.

**Teste independente**: Salvar um BonusConfig com formulaBase usando variável "XYZ" (não registrada como sigla) → deve ser rejeitado. Depois registrar "XYZ" como sigla de atributo → deve ser aceito.

**Acceptance Scenarios**:

1. **Given** uma fórmula com variáveis inexistentes no jogo, **When** o Mestre tenta salvar o BonusConfig, **Then** o sistema rejeita com erro indicando quais variáveis não foram reconhecidas.
2. **Given** uma fórmula com sintaxe inválida (parênteses abertos, operadores inválidos), **When** o Mestre salva, **Then** o sistema rejeita com erro de sintaxe.
3. **Given** um AtributoConfig com formulaImpeto usando `total`, **When** salvo, **Then** aceito (variável fixa permitida).
4. **Given** um VantagemConfig com formulaCusto usando `custoBase` e `nivelVantagem`, **When** salvo, **Then** aceito (variáveis fixas de custo de vantagem).
5. **Given** uma fórmula válida com variáveis existentes, **When** o Mestre usa `POST /api/jogos/{id}/formulas/preview` com valores de teste, **Then** o sistema retorna o resultado calculado sem persistir nada.

---

### Story 3 — CategoriaVantagem e PontosVantagemConfig com CRUD (Priority: P1)

Como Mestre, consigo gerenciar categorias de vantagens e definir pontos de vantagem por nível, para organizar o sistema de vantagens do meu jogo.

**Por que P1**: Sem CategoriaVantagem, as vantagens não podem ser organizadas. Sem PontosVantagemConfig, o sistema de progressão fica incompleto.

**Acceptance Scenarios**:

1. **Given** um jogo, **When** o Mestre cria uma CategoriaVantagem com nome, descrição e cor, **Then** ela é persistida e retornada.
2. **Given** uma CategoriaVantagem existente, **When** o Mestre tenta criar outra com o mesmo nome no mesmo jogo, **Then** o sistema rejeita com conflito.
3. **Given** um jogo, **When** o Mestre define pontos de vantagem para os níveis 1 a 10, **Then** cada nível é persistido com seu valor e pode ser consultado.
4. **Given** um PontosVantagemConfig existente, **When** o Mestre tenta criar outro para o mesmo nível no mesmo jogo, **Then** o sistema rejeita com conflito (unicidade por jogo+nível).

---

### Story 4 — VantagemConfig: Sigla, Categoria e Pré-Requisitos (Priority: P2)

Como Mestre, consigo associar uma sigla, uma categoria e pré-requisitos a cada VantagemConfig, para que o sistema de vantagens seja completo e coerente com o glossário.

**Por que P2**: Sigla é necessária para fórmulas. Categoria organiza a UI. Pré-requisitos são regra de negócio, mas a ficha pode ser usada sem eles em um primeiro momento.

**Acceptance Scenarios**:

1. **Given** um VantagemConfig criado sem sigla, **When** o Mestre tenta criar uma VantagemConfig sem sigla, **Then** o sistema aceita (sigla é opcional inicialmente) [ou rejeita se for obrigatório — a ser decidido na task].
2. **Given** uma VantagemConfig com categoria definida, **When** o Mestre lista as vantagens, **Then** a categoria aparece na resposta.
3. **Given** uma VantagemConfig A, **When** o Mestre define VantagemConfig B como pré-requisito de A, **Then** a relação é persistida e retornada no GET de A.
4. **Given** uma relação de pré-requisito circular (A → B → A), **When** o Mestre tenta criar a relação, **Then** o sistema detecta e rejeita o ciclo.

---

### Story 5 — ClassePersonagem: Bônus e Aptidões da Classe (Priority: P2)

Como Mestre, consigo configurar quais bônus e aptidões uma Classe concede ao personagem, para que as fichas sejam geradas com os bônus corretos de acordo com a classe escolhida.

**Por que P2**: Bloqueia a lógica de cálculo da Ficha, mas não é necessário para o CRUD básico de Ficha.

**Acceptance Scenarios**:

1. **Given** uma ClassePersonagem, **When** o Mestre associa um BonusConfig com valor +1 por nível, **Then** a relação é persistida (ClasseBonus).
2. **Given** uma ClassePersonagem, **When** o Mestre associa uma AptidaoConfig com bônus fixo +2, **Then** a relação é persistida (ClasseAptidaoBonus).
3. **Given** uma ClassePersonagem com ClasseBonus, **When** a classe é consultada via GET, **Then** os bônus associados aparecem na response.

---

### Story 6 — Raca: Classes Permitidas (Priority: P2)

Como Mestre, consigo configurar quais classes cada Raça pode escolher, para que o sistema imponha restrições de raça no criador de fichas.

**Por que P2**: Regra de negócio da Ficha, mas o CRUD pode funcionar sem ela.

**Acceptance Scenarios**:

1. **Given** uma Raça e uma ClassePersonagem, **When** o Mestre associa a classe como permitida para a raça, **Then** a relação é persistida (RacaClassePermitida).
2. **Given** uma Raça com classes permitidas, **When** consultada via GET, **Then** as classes aparecem na response.

---

### Edge Cases

- O que acontece ao deletar uma config que tem sigla usada em fórmulas de outras configs?
- O que acontece ao renomear/trocar a sigla de uma config que já está em uso em fórmulas?
- O que acontece ao deletar uma CategoriaVantagem que já está associada a vantagens?
- O que acontece ao criar um pré-requisito circular (A pré-requisita B, B pré-requisita A)?
- O que acontece se uma fórmula usa funções matemáticas desconhecidas (ex: `tan`, `log`)?

---

## Requirements

### Functional Requirements

**Sistema de Siglas**

- **FR-001**: O sistema DEVE adicionar campo `sigla` (obrigatório, @Size(min=2, max=5)) em `BonusConfig`.
- **FR-002**: O sistema DEVE adicionar campo `sigla` (opcional, @Size(min=2, max=5)) em `VantagemConfig`.
- **FR-003**: O sistema DEVE validar no service layer que a sigla/abreviação é única por jogo cross-entity (AtributoConfig.abreviacao, BonusConfig.sigla, VantagemConfig.sigla).
- **FR-004**: O sistema DEVE retornar ConflictException ao tentar criar/atualizar uma sigla já existente no jogo em qualquer entidade.
- **FR-005**: O sistema DEVE expor `GET /api/jogos/{id}/siglas` retornando todas as siglas em uso no jogo, com tipo (ATRIBUTO, BONUS, VANTAGEM) e entityId.
- **FR-006**: O sistema DEVE alertar (ou rejeitar) ao tentar editar/excluir uma config cuja sigla é referenciada em fórmulas de outras configs.

**Validação de Fórmulas**

- **FR-007**: O sistema DEVE validar `formulaImpeto` (AtributoConfig) no service durante criar e atualizar — aceita: `total`, funções matemáticas (floor, ceil, min, max, abs, sqrt), operadores.
- **FR-008**: O sistema DEVE validar `formulaBase` (BonusConfig) no service durante criar e atualizar — aceita: siglas de atributos do jogo (dinâmico), variáveis `nivel`, `base`, funções e operadores.
- **FR-009**: O sistema DEVE validar `formulaCusto` (VantagemConfig) no service durante criar e atualizar — aceita: `custoBase`, `nivelVantagem`, funções e operadores.
- **FR-010**: O sistema DEVE retornar erro descritivo indicando quais variáveis da fórmula não foram reconhecidas.
- **FR-011**: O sistema DEVE expor `POST /api/jogos/{id}/formulas/preview` — recebe `{formula, tipo, valores}`, retorna resultado sem persistir.
- **FR-012**: O sistema DEVE expor `GET /api/jogos/{id}/formulas/variaveis` — lista todas as variáveis disponíveis agrupadas por tipo.

**CategoriaVantagem e PontosVantagemConfig**

- **FR-013**: `CategoriaVantagem` DEVE ter CRUD completo (controller, service, mapper, DTOs) sob `/api/jogos/{id}/config/categorias-vantagem`.
- **FR-014**: `CategoriaVantagem` DEVE implementar `ConfiguracaoEntity` e usar o padrão Lombok (`@Data @Builder`).
- **FR-015**: `CategoriaVantagem` DEVE ter unicidade por `(jogo_id, nome)`.
- **FR-016**: `PontosVantagemConfig` DEVE ter CRUD completo sob `/api/jogos/{id}/config/pontos-vantagem`.
- **FR-017**: `PontosVantagemConfig` DEVE ter unicidade por `(jogo_id, nivel)`.
- **FR-018**: `VantagemConfig` DEVE ter FK para `CategoriaVantagem` (nullable — categoria é opcional inicialmente).

**VantagemPreRequisito**

- **FR-019**: O sistema DEVE criar entity `VantagemPreRequisito` representando pré-requisito de uma vantagem sobre outra.
- **FR-020**: O sistema DEVE validar e rejeitar pré-requisitos circulares.
- **FR-021**: O CRUD de VantagemConfig DEVE incluir pré-requisitos no create/update/response.

**ClasseBonus e ClasseAptidaoBonus**

- **FR-022**: O sistema DEVE criar entity `ClasseBonus` (ClassePersonagem + BonusConfig + valorPorNivel).
- **FR-023**: O sistema DEVE criar entity `ClasseAptidaoBonus` (ClassePersonagem + AptidaoConfig + bonus).
- **FR-024**: O CRUD de ClassePersonagem DEVE incluir ClasseBonus e ClasseAptidaoBonus no response e permitir gerenciá-los.

**RacaClassePermitida**

- **FR-025**: O sistema DEVE criar entity `RacaClassePermitida` (Raca + ClassePersonagem).
- **FR-026**: O CRUD de Raca DEVE incluir classes permitidas no response e permitir associar/desassociar.

---

### Key Entities

| Entidade | Status Atual | O que muda |
|---|---|---|
| `AtributoConfig` | ✅ CRUD completo | + cross-entity sigla validation, + formulaImpeto validation |
| `BonusConfig` | ✅ CRUD completo | + campo `sigla`, + cross-entity validation, + formulaBase validation |
| `VantagemConfig` | ✅ CRUD completo | + campo `sigla`, + campo `categoriaVantagem`, + formulaCusto validation |
| `CategoriaVantagem` | ❌ Entity existe, sem CRUD | + CRUD completo, + Lombok fix, + ConfiguracaoEntity |
| `PontosVantagemConfig` | ❌ Entity existe, sem CRUD | + CRUD completo, + Lombok fix |
| `VantagemPreRequisito` | ❌ Não existe | Criar do zero |
| `ClasseBonus` | ❌ Não existe | Criar do zero |
| `ClasseAptidaoBonus` | ❌ Não existe | Criar do zero |
| `RacaClassePermitida` | ❌ Não existe | Criar do zero |

---

## Success Criteria

### Measurable Outcomes

- **SC-001**: Qualquer tentativa de criar sigla duplicada no mesmo jogo é rejeitada em 100% dos casos, independente da entidade.
- **SC-002**: Fórmulas com variáveis não registradas no jogo são rejeitadas com mensagem identificando as variáveis inválidas.
- **SC-003**: Fórmulas com sintaxe inválida são rejeitadas com mensagem de erro de sintaxe.
- **SC-004**: `GET /api/jogos/{id}/siglas` retorna lista completa e precisa das siglas em uso.
- **SC-005**: CategoriaVantagem e PontosVantagemConfig têm testes de integração cobrindo create, list, update, delete e duplicate prevention.
- **SC-006**: VantagemPreRequisito rejeita ciclos e persiste relações válidas.
- **SC-007**: ClasseBonus, ClasseAptidaoBonus e RacaClassePermitida são retornados nos responses das entidades pai.

---

## Assumptions

- O campo `sigla` em VantagemConfig é opcional (nem todas as vantagens precisam ser variáveis de fórmula). Se futuro uso exigir, a constraint pode ser adicionada depois.
- A validação de impacto de siglas em fórmulas existentes (FR-006) pode ser um warning no response em vez de erro bloqueante na primeira iteração.
- `VantagemEfeito` (efeitos detalhados por tipo/alvo/valor) é desconsiderado neste spec — é complexidade de fórmula para a Ficha e pode vir em spec posterior.
- As entities de Ficha existentes (FichaVantagem, etc.) não são alteradas neste spec — serão refeitas do zero no EPIC 4.
- A detecção de dependências circulares de fórmulas (EPIC 2.4) é desconsiderada neste spec — complexidade para fase posterior.

---

## Dependencies

- `FormulaEvaluatorService` existente (evaluate, isValid) — base para a validação de fórmulas.
- `AbstractConfiguracaoService` — ponto de extensão para injeção da sigla validation.
- `BaseConfiguracaoServiceIntegrationTest` — base para testes de CategoriaVantagem e PontosVantagemConfig.
- Padrão de controller/mapper/DTO de `AtributoController` — referência para novos controllers.
- `docs/backend/` (01 a 11) — guidelines de arquitetura a seguir em todos os novos artefatos.
