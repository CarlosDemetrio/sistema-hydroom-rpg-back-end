# Feature Specification: Ficha de Personagem do ZERO

**Feature Branch**: `006-ficha-personagem`
**Created**: 2026-03-08
**Status**: Draft
**Epics cobertos**: EPIC 4 — Ficha de Personagem
**Input**: As 13 configurações têm CRUD completo (incluindo siglas, fórmulas e relacionamentos via Spec 004). O FormulaEvaluatorService está integrado. Agora é possível criar a Ficha de Personagem — entidade central do sistema — com todos os seus sub-componentes calculados a partir das configs do Jogo.

---

## User Stories & Testing *(mandatory)*

### Story 1 — Criar e Gerenciar Ficha Básica (Priority: P1)

Como Jogador aprovado em um Jogo, consigo criar minha Ficha de Personagem informando identidade (nome, raça, classe, gênero, índole, presença) e dados narrativos opcionais, para que o Mestre e eu possamos acompanhar minha evolução no sistema Klayrah.

**Por que P1**: Sem Ficha não existe personagem. É a entidade central que tudo mais depende.

**Teste independente**: Criar um Jogo com configs básicas, aprovar um Jogador, criar uma Ficha — verificar que todos os sub-registros foram inicializados automaticamente.

**Acceptance Scenarios**:

1. **Given** um Jogo com AtributoConfig e BonusConfig configurados, e um Jogador com participação APROVADA, **When** o Jogador faz POST /api/jogos/{id}/fichas com dados válidos, **Then** a Ficha é criada e cada sub-registro (FichaAtributo, FichaBonus, etc.) é inicializado com zeros.
2. **Given** um Jogador sem participação aprovada no Jogo, **When** tenta criar uma Ficha, **Then** o sistema rejeita com erro de autorização.
3. **Given** uma Ficha criada com raça que não pertence ao Jogo, **When** o sistema tenta validar, **Then** a criação é rejeitada com ValidationException.
4. **Given** um Mestre autenticado, **When** faz GET /api/jogos/{id}/fichas, **Then** vê todas as fichas do jogo. **Given** um Jogador, **When** faz GET /api/jogos/{id}/fichas, **Then** vê apenas as próprias fichas.
5. **Given** uma Ficha existente, **When** o dono faz DELETE /api/fichas/{id}, **Then** soft delete é aplicado (deleted_at preenchido).

---

### Story 2 — Atributos e Aptidões (Priority: P1)

Como Mestre ou Jogador, consigo distribuir pontos de atributo e aptidão na Ficha, respeitando os limites definidos pelo NivelConfig atual do personagem, para que as capacidades do personagem reflitam as escolhas de evolução.

**Por que P1**: Atributos e Aptidões são o núcleo da ficha mecânica — sem eles não há base para cálculo de bônus, vida, essência nem nenhum outro sub-sistema.

**Teste independente**: Criar Ficha com NivelConfig definindo 10 pontos de atributo. Tentar distribuir 11 pontos → rejeitar. Distribuir 10 → aceitar. Verificar que limitador de atributo é respeitado.

**Acceptance Scenarios**:

1. **Given** uma Ficha recém-criada, **When** consultada, **Then** cada FichaAtributo aparece com `base=0`, `nivel=0`, `outros=0`, `total=0`, `impeto=0`.
2. **Given** um NivelConfig com `pontosAtributo=10` e `limitadorAtributo=5`, **When** o Jogador tenta atribuir 6 pontos em um único atributo, **Then** o sistema rejeita com ValidationException indicando o limitador.
3. **Given** distribuição de 10 pontos somando `base` de todos os FichaAtributo, **When** enviada, **Then** aceita. Ao tentar 11 pontos, **Then** rejeita.
4. **Given** uma FichaAptidao, **When** atualizada com `base+sorte+classe`, **Then** `total` é calculado como a soma dos três campos.
5. **Given** o total de pontos de aptidão distribuídos excede `pontosAptidao` do NivelConfig, **When** salvo, **Then** rejeita com erro descritivo.

---

### Story 3 — Bônus (Priority: P1)

Como sistema, calculo automaticamente o `base` de cada FichaBonus usando a `formulaBase` da BonusConfig com os valores atuais dos atributos do personagem, para que os bônus derivados sejam sempre consistentes.

**Por que P1**: Bônus derivados de atributos (como BBA, ARM, etc.) são referenciados em mecânicas centrais do sistema Klayrah.

**Teste independente**: Criar BonusConfig com `formulaBase = "FOR + AGI"`. Criar Ficha com atributos FOR=3, AGI=2. Verificar que FichaBonus.base é calculado como 5. Adicionar vantagens e verificar total.

**Acceptance Scenarios**:

1. **Given** um BonusConfig com `formulaBase = "FOR + AGI"` e Ficha com FOR=3, AGI=2, **When** FichaBonus é consultado, **Then** `base=5` (calculado pela fórmula).
2. **Given** um FichaBonus com `base=5, vantagens=2, classe=1, itens=0, gloria=0, outros=0`, **When** consultado, **Then** `total=8`.
3. **Given** PUT /api/fichas/{id}/bonus com novos valores de `vantagens`, `classe`, etc., **When** salvo, **Then** total é recalculado.

---

### Story 4 — Vida, Essência, Ameaça e Prospecção (Priority: P1)

Como Mestre, consigo ver e atualizar os pontos de vida, essência, ameaça e dados de prospecção de cada personagem, para controlar o estado de combate e progressão da sessão.

**Por que P1**: Estes sub-sistemas definem o estado de combate e sobrevivência do personagem — sem eles a sessão de jogo não pode ocorrer.

**Teste independente**: Criar Ficha com MembroCorpoConfig "Cabeça" (30% vida) e "Tronco" (50% vida). Definir vidaTotal=100. Verificar que FichaVidaMembro "Cabeça" tem vida=30 e "Tronco" tem vida=50.

**Acceptance Scenarios**:

1. **Given** uma Ficha com `vidaTotal=100` e dois MembroCorpoConfig (30% e 50%), **When** consultada, **Then** FichaVidaMembro "Cabeça" tem `vida=30` e "Tronco" `vida=50`.
2. **Given** FichaEssencia com Vigor=4, Sabedoria=2, nivel=3, renascimentos=1, vantagens=0, outros=0, **When** calculada, **Then** `total = floor((4+2)/2) + 3 + 1 + 0 + 0 = 7`.
3. **Given** FichaAmeaca com nivel=2, itens=1, titulos=0, renascimentos=1, outros=0, **When** consultada, **Then** `total=4`.
4. **Given** DadoProspeccaoConfig com 6 faces, **When** FichaProspeccao é criada, **Then** inicializa com `quantidade=0`.
5. **Given** PUT /api/fichas/{id}/prospeccao com nova quantidade, **When** salvo, **Then** atualizado corretamente.

---

### Story 5 — Vantagens Compradas (Priority: P1)

Como Jogador, consigo comprar vantagens para meu personagem respeitando pré-requisitos e aumentar o nível das vantagens já compradas, para que meu personagem evolua de acordo com as regras do sistema Klayrah.

**Por que P1**: O sistema de vantagens é parte central da progressão do personagem. Sem ele, a Ficha não representa a evolução do personagem.

**Teste independente**: Tentar comprar vantagem B que exige vantagem A nível 2. Sem ter A → rejeitar. Comprar A, elevar para nível 2, então comprar B → aceitar. Tentar remover A → rejeitar (vantagem comprada nunca é removida).

**Acceptance Scenarios**:

1. **Given** uma VantagemConfig B com pré-requisito A nível 2, **When** o Jogador tenta comprar B sem ter A, **Then** o sistema rejeita com ValidationException indicando o pré-requisito não atendido.
2. **Given** o Jogador tem a vantagem A no nível 2, **When** tenta comprar B, **Then** o sistema permite e cria FichaVantagem com `nivelAtual=1`.
3. **Given** uma FichaVantagem existente com `nivelAtual=1`, **When** o Jogador aumenta para nível 2, **Then** aceito.
4. **Given** uma FichaVantagem existente, **When** o Jogador tenta fazer DELETE, **Then** o sistema retorna 405 Method Not Allowed (rota não existe).
5. **Given** uma VantagemConfig com `formulaCusto = "custoBase * nivelVantagem"`, **When** o custo é calculado para nivelAtual=2, **Then** o valor correto é retornado no response.

---

### Story 6 — Descrição Física (Priority: P2)

Como Jogador, consigo preencher os dados físicos do meu personagem (altura, peso, idade, aparência), para enriquecer a narrativa e imersão no jogo.

**Por que P2**: Dados descritivos enriquecem a experiência, mas não bloqueiam o funcionamento mecânico da Ficha.

**Acceptance Scenarios**:

1. **Given** uma Ficha existente, **When** o Jogador faz PUT /api/fichas/{id}/descricao-fisica com altura=175, peso=70.5, idade=25, **Then** FichaDescricaoFisica é criada/atualizada com os valores.
2. **Given** FichaDescricaoFisica com campos opcionais nulos, **When** consultada, **Then** campos opcionais aparecem como null no response sem erros.
3. **Given** altura negativa (-10), **When** enviada, **Then** ValidationException é lançada.

---

### Edge Cases

- O que acontece ao criar Ficha em Jogo sem nenhum AtributoConfig? FichaAtributo simplesmente não é inicializado (lista vazia).
- O que acontece ao adicionar nova AtributoConfig ao Jogo depois que Fichas já existem? Sub-registros das fichas existentes NÃO são atualizados automaticamente (escopo de Spec 007).
- O que acontece ao deletar um AtributoConfig com FichaAtributo associado? Soft delete do config — FichaAtributo permanece com FK para o config deletado (dados históricos preservados).
- O que acontece ao tentar criar duas fichas com o mesmo nome no mesmo jogo? Aceitar ou rejeitar? (A ser decidido — fichas não têm unicidade de nome por padrão, apenas sugerido).
- O que acontece ao tentar aumentar FichaVantagem além do `nivelMaximo` da VantagemConfig? Deve ser rejeitado com ValidationException.
- O que acontece ao tentar distribuir pontos negativos em um atributo? ValidationException — valores mínimos de 0.

---

## Requirements

### Functional Requirements

**Ficha — Entidade e Identidade**

- **FR-001**: `Ficha` DEVE ter campos de identidade: `nome` (obrigatório, max 100), `jogadorId` (usuarioId do Jogador), `jogo` (FK), `raca` (FK → Raca, nullable), `classePersonagem` (FK → ClassePersonagem, nullable), `generoConfig` (FK → GeneroConfig, nullable), `indoleConfig` (FK → IndoleConfig, nullable), `presencaConfig` (FK → PresencaConfig, nullable).
- **FR-002**: `Ficha` DEVE ter campos de progressão: `nivel` (int, default 1, min 1), `xp` (int, default 0, min 0), `renascimentos` (int, default 0, min 0).
- **FR-003**: `Ficha` DEVE ter campos narrativos opcionais: `origem` (texto, max 500), `arquetipo` (texto, max 200), `insolitus` (texto, max 200), `tituloHeroico` (texto, max 200).
- **FR-004**: `Ficha` DEVE estender `BaseEntity` (soft delete, audit fields via `@SQLRestriction("deleted_at IS NULL")`).

**Ficha — CRUD e Inicialização**

- **FR-005**: POST /api/jogos/{jogoId}/fichas DEVE criar Ficha e inicializar automaticamente: um FichaAtributo por AtributoConfig ativo do jogo, um FichaAptidao por AptidaoConfig, um FichaBonus por BonusConfig, uma FichaVidaMembro por MembroCorpoConfig, uma FichaProspeccao por DadoProspeccaoConfig. Todos inicializados com zeros.
- **FR-006**: A criação de Ficha DEVE validar que raca, classePersonagem, generoConfig, indoleConfig e presencaConfig (quando informados) pertencem ao mesmo Jogo da Ficha.
- **FR-007**: A criação de Ficha DEVE validar que o usuário tem participação com status APROVADA no Jogo.
- **FR-008**: GET /api/jogos/{jogoId}/fichas DEVE retornar todas as fichas para MESTRE e apenas as do próprio usuário para JOGADOR.
- **FR-009**: GET /api/fichas/{id}, PUT /api/fichas/{id} e DELETE /api/fichas/{id} DEVEM seguir o padrão REST. DELETE aplica soft delete.
- **FR-010**: O response de Ficha DEVE incluir todos os sub-componentes calculados.

**FichaAtributo e FichaAptidao**

- **FR-011**: `FichaAtributo` DEVE ter campos: `ficha` (FK), `atributoConfig` (FK), `base` (int, min 0), `nivel` (int, min 0), `outros` (int, default 0), `total` (int, calculado = base + nivel + outros), `impeto` (double, calculado via formulaImpeto do AtributoConfig).
- **FR-012**: `FichaAptidao` DEVE ter campos: `ficha` (FK), `aptidaoConfig` (FK), `base` (int, min 0), `sorte` (int, default 0), `classe` (int, default 0), `total` (int, calculado = base + sorte + classe).
- **FR-013**: PUT /api/fichas/{id}/atributos DEVE receber lista de FichaAtributoUpdateRequest e validar que a soma dos campos `base` de todos os atributos não excede `pontosAtributo` do NivelConfig para o nível atual da Ficha.
- **FR-014**: PUT /api/fichas/{id}/atributos DEVE validar que nenhum atributo individual ultrapassa o `limitadorAtributo` do NivelConfig para o nível atual da Ficha.
- **FR-015**: PUT /api/fichas/{id}/aptidoes DEVE validar que a soma dos campos `base` não excede `pontosAptidao` do NivelConfig para o nível atual.

**FichaBonus**

- **FR-016**: `FichaBonus` DEVE ter campos: `ficha` (FK), `bonusConfig` (FK), `base` (double, calculado via formulaBase com totais dos atributos), `vantagens` (double, default 0), `classe` (double, default 0), `itens` (double, default 0), `gloria` (double, default 0), `outros` (double, default 0), `total` (double, calculado = base + vantagens + classe + itens + gloria + outros).
- **FR-017**: PUT /api/fichas/{id}/bonus DEVE receber lista de FichaBonusUpdateRequest atualizando campos parciais (sem recalcular `base` — recalculo é escopo de Spec 007).

**FichaVida, FichaVidaMembro, FichaEssencia, FichaAmeaca**

- **FR-018**: `FichaVida` DEVE ter campos: `ficha` (FK 1:1), `vt` (int, Vigor Total, default 0), `outros` (int, default 0), `vidaTotal` (int, calculado = vigor + nivel + vt + renascimentos + outros).
- **FR-019**: `FichaVidaMembro` DEVE ter campos: `fichaVida` (FK), `membroCorpoConfig` (FK), `vida` (int, calculado = vidaTotal × porcentagemVida do MembroCorpoConfig), `danoRecebido` (int, default 0).
- **FR-020**: `FichaEssencia` DEVE ter campos: `ficha` (FK 1:1), `renascimentos` (int, default 0), `vantagens` (int, default 0), `outros` (int, default 0), `total` (int, calculado = floor((vigor + sabedoria) / 2) + nivel + renascimentos + vantagens + outros), `essenciaRestante` (int, default = total).
- **FR-021**: `FichaAmeaca` DEVE ter campos: `ficha` (FK 1:1), `itens` (int, default 0), `titulos` (int, default 0), `renascimentos` (int, default 0), `outros` (int, default 0), `total` (int, calculado = nivel + itens + titulos + renascimentos + outros).
- **FR-022**: Endpoints PUT /api/fichas/{id}/vida, /essencia, /ameaca DEVEM permitir atualização manual dos campos não-calculados.

**FichaProspeccao**

- **FR-023**: `FichaProspeccao` DEVE ter campos: `ficha` (FK), `dadoProspeccaoConfig` (FK), `quantidade` (int, min 0, default 0).
- **FR-024**: PUT /api/fichas/{id}/prospeccao DEVE receber lista de FichaProspeccaoUpdateRequest atualizando quantidade de dados disponíveis.

**FichaVantagem**

- **FR-025**: `FichaVantagem` DEVE ter campos: `ficha` (FK), `vantagemConfig` (FK), `nivelAtual` (int, min 1).
- **FR-026**: POST /api/fichas/{id}/vantagens DEVE validar que os pré-requisitos da VantagemConfig (VantagemPreRequisito) estão atendidos na Ficha antes de criar FichaVantagem.
- **FR-027**: PUT /api/fichas/{id}/vantagens/{vid} DEVE permitir apenas aumentar `nivelAtual` (nunca diminuir). Validar que não ultrapassa `nivelMaximo` da VantagemConfig.
- **FR-028**: Não existe DELETE para FichaVantagem — uma vantagem comprada nunca pode ser removida (regra de negócio do sistema Klayrah).
- **FR-029**: O custo de uma FichaVantagem DEVE ser calculado via `formulaCusto` da VantagemConfig e incluído no response.

**FichaDescricaoFisica**

- **FR-030**: `FichaDescricaoFisica` DEVE ter campos: `ficha` (FK 1:1), `altura` (int cm, min 1, max 300, nullable), `peso` (BigDecimal kg, min 0, nullable), `idade` (int, min 0, nullable), `descricaoOlhos` (String, max 100, nullable), `descricaoCabelos` (String, max 100, nullable), `descricaoPele` (String, max 100, nullable).
- **FR-031**: PUT /api/fichas/{id}/descricao-fisica DEVE criar ou atualizar (upsert) FichaDescricaoFisica para a Ficha.

**Segurança e Autorização**

- **FR-032**: Todos os endpoints de escrita (POST, PUT, DELETE) em fichas DO PRÓPRIO JOGADOR requerem autenticação. MESTRE pode editar qualquer Ficha do jogo.
- **FR-033**: Leituras de Ficha requerem autenticação (`hasAnyRole('MESTRE', 'JOGADOR')`). JOGADOR só acessa fichas próprias.

---

### Key Entities

| Entidade | Status Atual | O que criar |
|---|---|---|
| `Ficha` | ❌ Não existe | Criar do zero |
| `FichaAtributo` | ❌ Não existe | Criar do zero |
| `FichaAptidao` | ❌ Não existe | Criar do zero |
| `FichaBonus` | ❌ Não existe | Criar do zero |
| `FichaVida` | ❌ Não existe | Criar do zero |
| `FichaVidaMembro` | ❌ Não existe | Criar do zero |
| `FichaEssencia` | ❌ Não existe | Criar do zero |
| `FichaAmeaca` | ❌ Não existe | Criar do zero |
| `FichaProspeccao` | ❌ Não existe | Criar do zero |
| `FichaVantagem` | ❌ Não existe | Criar do zero |
| `FichaDescricaoFisica` | ❌ Não existe | Criar do zero |

---

## Success Criteria

### Measurable Outcomes

- **SC-001**: POST /api/jogos/{id}/fichas com configs válidas cria Ficha e inicializa todos os sub-registros em 100% dos casos.
- **SC-002**: Distribuição de pontos acima do limite do NivelConfig é rejeitada com mensagem clara.
- **SC-003**: Tentativa de comprar vantagem sem pré-requisito atendido é rejeitada com mensagem indicando qual pré-requisito falta.
- **SC-004**: Tentativa de deletar FichaVantagem retorna 405 (endpoint não existe).
- **SC-005**: FichaVantagem com nivelAtual acima de `nivelMaximo` da VantagemConfig é rejeitada.
- **SC-006**: MESTRE vê todas as fichas do jogo; JOGADOR vê apenas as próprias.
- **SC-007**: Todos os endpoints têm `@Operation` Swagger e anotações de segurança corretas.
- **SC-008**: `./mvnw test` passa 100% após todas as tasks da spec.

---

## Out of Scope

- **Recálculo automático de bônus/vida/essência ao alterar atributos** — escopo de Spec 007 (Motor de Recálculo Integrado)
- **FichaVantagem DELETE** — por design, vantagens compradas nunca são removidas no sistema Klayrah
- **Notificações ao Mestre sobre mudanças na Ficha** — funcionalidade de colaboração em tempo real (Spec posterior)
- **Histórico de alterações na Ficha** — auditoria completa de mudanças (Spec posterior)
- **Validação de ClassePersonagem vs RacaClassePermitida ao criar Ficha** — depende de Spec 004 Phase 6 estar estável

---

## Assumptions

- `Participacao` entity com campo `status` (APROVADA, PENDENTE, etc.) já existe e é consultável.
- As 13 ConfiguracaoEntity têm CRUD completo conforme Spec 004.
- `VantagemPreRequisito` entity existe conforme Spec 004 Phase 4.
- O cálculo de `base` de FichaBonus via fórmula não é automático neste spec — é calculado na criação da Ficha e recalculado manualmente; recálculo reativo fica para Spec 007.
- O campo `sigla` dos atributos (abreviacao) é o mesmo identificador usado nas variáveis de fórmula de BonusConfig.

---

## Dependencies

- `VantagemPreRequisito` (Spec 004 Phase 4) — necessário para validação de pré-requisitos em FichaVantagem.
- `ClasseBonus` e `ClasseAptidaoBonus` (Spec 004 Phase 5) — referenciados nos campos `classe` de FichaAtributo e FichaAptidao.
- `FormulaEvaluatorService` — usado para calcular `base` de FichaBonus e `impeto` de FichaAtributo.
- `BaseEntity` — todas as entidades de Ficha estendem.
- `docs/glossario/02-configuracoes-jogo.md` — detalhes de cada config e seus relacionamentos com a Ficha.
- `docs/glossario/03-termos-dominio.md` — glossário de termos: vida, essência, ameaça, prospecção.
- `docs/backend/` (01 a 11) — guidelines de arquitetura a seguir em todos os novos artefatos.
