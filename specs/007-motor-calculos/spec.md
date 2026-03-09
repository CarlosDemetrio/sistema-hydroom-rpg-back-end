# Feature Specification: Motor de Cálculos da Ficha

**Feature Branch**: `007-motor-calculos`
**Created**: 2026-03-08
**Status**: Draft
**Epics cobertos**: EPIC 5 (Motor de Cálculos da Ficha)
**Input**: Spec 006 criou as entidades da Ficha de Personagem (FichaAtributo, FichaBonus, FichaVida, FichaEssencia, FichaAmeaca, etc.). O FormulaEvaluatorService existe com exp4j. Este spec integra os dois: todos os valores derivados devem ser calculados server-side antes de cada persistência.

---

## User Stories & Testing *(mandatory)*

### Story 1 — Cálculo automático ao salvar Ficha (Priority: P1)

Como jogador, quando atualizo os atributos da minha ficha, todos os valores derivados (ímpetos, bônus, vida por membro, essência, ameaça) são recalculados automaticamente antes de salvar, sem que eu precise calcular nada manualmente.

**Por que P1**: É a razão de existir do motor de cálculos. Sem isso, a ficha exibe dados inconsistentes e o jogo é inviável.

**Teste independente**: Criar ficha com vigor=10, atualizar vigor para 15 → verificar que vidaTotal, vida de cada membro e essência foram recalculados automaticamente no response.

**Acceptance Scenarios**:

1. **Given** uma ficha com FichaAtributo de força com base=5, nivel=2, **When** o jogador salva a ficha, **Then** FichaAtributo.total = 7 e FichaAtributo.impeto é calculado via formulaImpeto do AtributoConfig correspondente.
2. **Given** uma ficha com atributos calculados, **When** um BonusConfig tem formulaBase usando siglas de atributos, **Then** FichaBonus.base é calculado com os totais dos atributos da ficha.
3. **Given** uma ficha com vidaTotal calculado, **When** há MembroCorpoConfig com porcentagemVida=0.30, **Then** FichaVidaMembro.vida = FLOOR(vidaTotal × 0.30).
4. **Given** uma ficha com vigor.total=15 e sabedoria.total=10, nivel=3, **When** salva, **Then** essenciaTotal = FLOOR((15+10)/2) + 3 + outros modificadores.
5. **Given** FichaCalculationService.recalcular(ficha) chamado, **When** completa, **Then** todos os sub-valores da ficha estão atualizados antes do persist.

---

### Story 2 — Validações de negócio ao salvar (Priority: P1)

Como sistema, quando um jogador tenta salvar uma ficha com distribuição de pontos inválida (mais pontos de atributo do que permite o nível, atributo acima do limitador, classe não permitida pela raça), o sistema rejeita com mensagem descritiva indicando cada violação.

**Por que P1**: Sem validações, o sistema permite fichas em estado ilegal. As regras de negócio do RPG dependem dessas restrições.

**Teste independente**: Criar ficha de nível 1 (NivelConfig com pontosAtributo=10, limitadorAtributo=5), tentar salvar com soma de pontos de atributo=12 → deve ser rejeitado com mensagem indicando excesso de pontos.

**Acceptance Scenarios**:

1. **Given** NivelConfig de nivel=1 com pontosAtributo=10, **When** jogador tenta salvar ficha com soma de (base+nivel) dos atributos > 10, **Then** sistema rejeita com BusinessException indicando excesso de pontos.
2. **Given** NivelConfig de nivel=1 com limitadorAtributo=5, **When** jogador tenta salvar ficha com qualquer FichaAtributo.total > 5, **Then** sistema rejeita com BusinessException indicando qual atributo ultrapassou o limite.
3. **Given** uma raça com classes permitidas definidas, **When** jogador seleciona uma classe não permitida pela sua raça, **Then** sistema rejeita com BusinessException indicando a restrição de raça.
4. **Given** uma VantagemConfig com pré-requisito de vantagem X, **When** jogador tenta adicionar a vantagem sem ter vantagem X, **Then** sistema rejeita com BusinessException listando os pré-requisitos não atendidos.
5. **Given** múltiplas violações simultâneas, **When** jogador salva ficha inválida, **Then** sistema retorna todas as violações em uma única resposta (não apenas a primeira).

---

### Story 3 — Preview de cálculos sem persistir (Priority: P2)

Como jogador, consigo simular alterações na ficha e ver todos os valores calculados sem salvar permanentemente, para planejar como distribuir meus pontos antes de confirmar.

**Por que P2**: Melhora a UX significativamente, mas a ficha funciona sem preview. Os cálculos devem existir antes.

**Teste independente**: POST /api/fichas/{id}/preview com atributos alterados → response deve conter ficha com todos os valores recalculados, mas GET /api/fichas/{id} deve retornar a ficha original inalterada.

**Acceptance Scenarios**:

1. **Given** uma ficha salva, **When** o jogador faz POST /api/fichas/{id}/preview com vigor=20, **Then** o response retorna FichaResponse com vidaTotal e essência recalculados para vigor=20.
2. **Given** POST /api/fichas/{id}/preview executado com sucesso, **When** o jogador faz GET /api/fichas/{id}, **Then** os valores originais da ficha estão inalterados no banco.
3. **Given** dados de preview com distribuição inválida (pontos acima do limite), **When** o jogador solicita preview, **Then** o sistema retorna as violações de validação sem persistir nada.
4. **Given** preview com formulaBase de bônus, **When** retornado, **Then** FichaBonus.base é calculado com os valores do preview, não os valores atuais do banco.

---

### Story 4 — Nível automático por XP (Priority: P2)

Como jogador, quando minha ficha ganha experiência (XP), o nível é atualizado automaticamente para o NivelConfig correspondente, e todos os limitadores são recalculados sem que eu precise informar o nível manualmente.

**Por que P2**: Automação de progressão — melhora UX mas a ficha pode funcionar com nível manual inicialmente.

**Teste independente**: Criar ficha com xp=0, atualizar para xp=1500 (XP que corresponde a nivel=3 no NivelConfig) → verificar que ficha.nivel foi atualizado para 3 e limitadores recalculados.

**Acceptance Scenarios**:

1. **Given** NivelConfig com nivel=2 e xpNecessaria=500, **When** ficha.xp é atualizado para 500, **Then** ficha.nivel é automaticamente atualizado para 2.
2. **Given** ficha com nivel=3, **When** NivelConfig de nivel=3 tem limitadorAtributo=8, **Then** FichaValidationService usa limitadorAtributo=8 para validações.
3. **Given** ficha com xp=999 e NivelConfig com nivel=2/xp=500 e nivel=3/xp=1000, **When** consulta nivel atual, **Then** retorna nivel=2 (maior nivel onde xpNecessaria <= xp).
4. **Given** ficha com nivel atualizado automaticamente, **When** o novo nivel tem limitadores diferentes, **Then** recalcular() é chamado novamente com os novos parâmetros de nivel.

---

### Edge Cases

- O que acontece se AtributoConfig não tem formulaImpeto definida (null)? FichaAtributo.impeto deve ser null ou 0?
- O que acontece se a sigla "VIG" (vigor) não está registrada no jogo mas a fórmula de essência precisa dela?
- O que acontece se BonusConfig.formulaBase referencia uma sigla de atributo que foi deletada (soft delete)?
- O que acontece se MembroCorpoConfig.porcentagemVida * vidaTotal = 0 (resultado zero)?
- O que acontece se não existe nenhum NivelConfig onde xpNecessaria <= ficha.xp (ficha com xp abaixo do nivel 1)?
- O que acontece em preview quando a ficha referencia um Jogo com configurações deletadas?

---

## Requirements

### Functional Requirements

**FichaCalculationService — Core**

- **FR-001**: O sistema DEVE criar `FichaCalculationService.recalcular(Ficha ficha)` que recalcula todos os valores derivados da ficha em sequência correta.
- **FR-002**: O sistema DEVE calcular `FichaAtributo.total = base + nivel + outros` para cada atributo.
- **FR-003**: O sistema DEVE calcular `FichaAtributo.impeto` via `FormulaEvaluatorService.calcularImpeto(formulaImpeto, total)` — se formulaImpeto for null, impeto permanece null.
- **FR-004**: O sistema DEVE construir mapa de variáveis `{sigla → total}` de todos os FichaAtributo para uso nas fórmulas de bônus.
- **FR-005**: O sistema DEVE calcular `FichaBonus.base` via `FormulaEvaluatorService.calcularDerivado(formulaBase, atributos)` — se formulaBase for null, base permanece inalterado.
- **FR-006**: O sistema DEVE calcular `FichaBonus.total = base + vantagens + classe + itens + gloria + outros`.
- **FR-007**: O sistema DEVE calcular `vidaTotal = vigor.total + ficha.nivel + ficha.vtBase + ficha.renascimentos + ficha.outrosVida`.
- **FR-008**: O sistema DEVE calcular `FichaVidaMembro.vida = FLOOR(vidaTotal × membroCorpoConfig.porcentagemVida)` para cada membro.
- **FR-009**: O sistema DEVE calcular `essenciaTotal = FLOOR((vigor.total + sabedoria.total) / 2) + nivel + renascimentos + vantagens + outrosEssencia`.
- **FR-010**: O sistema DEVE calcular `ameacaTotal = nivel + itens + titulos + renascimentos + outrosAmeaca`.
- **FR-011**: O sistema DEVE determinar o nível atual da ficha buscando o maior NivelConfig onde `xpNecessaria <= ficha.xp`.
- **FR-012**: O sistema DEVE identificar vigor e sabedoria pelos campos de sigla do AtributoConfig correspondente (configurável por jogo, não hardcoded).

**Integração no Fluxo de Save**

- **FR-013**: `FichaCalculationService.recalcular()` DEVE ser chamado em `FichaService.criar()` e `FichaService.atualizar()` ANTES do `repository.save()`.
- **FR-014**: Quando `ficha.xp` é atualizado, o sistema DEVE recalcular `ficha.nivel` automaticamente via lookup em NivelConfig antes de chamar `recalcular()`.
- **FR-015**: Se lookup de NivelConfig não encontrar nenhum nível compatível com o XP, o sistema DEVE usar o primeiro nível disponível (nivel=1 ou o mais baixo cadastrado).

**FichaValidationService**

- **FR-016**: O sistema DEVE criar `FichaValidationService.validar(Ficha ficha, NivelConfig nivelAtual)` que executa todas as validações de negócio.
- **FR-017**: O sistema DEVE validar que a soma de pontos de atributo distribuídos `(sum de FichaAtributo.base + FichaAtributo.nivel)` não excede `NivelConfig.pontosAtributo`.
- **FR-018**: O sistema DEVE validar que nenhum `FichaAtributo.total` excede `NivelConfig.limitadorAtributo`.
- **FR-019**: O sistema DEVE validar que a classe da ficha está na lista de classes permitidas pela raça da ficha (se raça tiver classes permitidas definidas).
- **FR-020**: O sistema DEVE validar que todos os pré-requisitos de cada VantagemConfig na ficha estão satisfeitos (a ficha possui a vantagem pré-requisito no nível mínimo).
- **FR-021**: O sistema DEVE coletar TODAS as violações antes de lançar a exceção (não falhar na primeira).
- **FR-022**: O sistema DEVE lançar `BusinessException` com lista descritiva de todas as violações encontradas.

**FichaPreviewService**

- **FR-023**: O sistema DEVE criar `FichaPreviewService.simular(Long fichaId, FichaUpdateRequest request)` → `FichaResponse`.
- **FR-024**: O preview DEVE carregar a ficha do banco, aplicar as alterações do request em memória (sem chamar `repository.save()`), executar `recalcular()` e retornar o `FichaResponse`.
- **FR-025**: O sistema DEVE expor `POST /api/fichas/{id}/preview` com `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`.
- **FR-026**: O preview DEVE executar as validações de negócio e retornar as violações no response (sem lançar exceção que aborte a operação — retornar 200 com warnings ou 422 com lista).

**Erros e Resiliência**

- **FR-027**: Erros de cálculo de fórmula (variável não encontrada, divisão por zero) DEVEM lançar `BusinessException` com mensagem identificando a fórmula problemática.
- **FR-028**: `FichaCalculationService` DEVE ser stateless e injetável via Spring (sem estado de instância).
- **FR-029**: `FichaCalculationService` DEVE ser testável unitariamente com dados mockados (sem necessidade de banco).

---

### Key Services

| Serviço | Status | O que criar |
|---|---|---|
| `FormulaEvaluatorService` | ✅ Existe | — (usado como dependência) |
| `FichaCalculationService` | ❌ Não existe | Criar do zero |
| `FichaValidationService` | ❌ Não existe | Criar do zero |
| `FichaPreviewService` | ❌ Não existe | Criar do zero |
| `FichaService` | ✅ Existe (Spec 006) | Integrar cálculo + validação no criar/atualizar |

---

## Success Criteria

### Measurable Outcomes

- **SC-001**: Após atualizar qualquer atributo base, todos os valores derivados (impeto, bonus base, vida, essencia) são recalculados e persistidos corretamente em 100% dos casos.
- **SC-002**: Ficha com pontos acima do limite do nivel é rejeitada com mensagem descritiva em 100% dos casos.
- **SC-003**: Ficha com atributo acima do limitadorAtributo é rejeitada com mensagem identificando o atributo violador.
- **SC-004**: `POST /api/fichas/{id}/preview` retorna ficha com valores calculados sem alterar o estado persistido.
- **SC-005**: Atualização de XP atualiza nivel automaticamente para o NivelConfig correto.
- **SC-006**: `./mvnw test` passa 100% com testes cobrindo FichaCalculationService, FichaValidationService e FichaPreviewService.

---

## Assumptions

- Vigor e sabedoria são identificados pela sigla do AtributoConfig configurada por jogo — o Mestre define quais siglas representam vigor e sabedoria nas configurações do jogo. Se não configurado, essência não pode ser calculada e deve retornar null.
- `FichaAtributo.outros` (modificadores temporários de outros) é somado no total mas não conta para validação de pontos distribuídos.
- O campo `renascimentos` da ficha incrementa tanto vida quanto essência — é um modificador global, não por atributo.
- Pontos de vantagem gastos versus disponíveis são validados em Spec 006 (ao comprar a vantagem) — este spec foca em validações de atributos, limitador e pré-requisitos.
- Recálculo em batch (recalcular todas as fichas quando uma configuração muda) é desconsiderado — complexidade para fase posterior (Spec 008).
- `FichaCalculationService` trata valores null em campos opcionais (vtBase, outrosVida, etc.) como zero.

---

## Dependencies

- `FormulaEvaluatorService` existente — `calcularImpeto()`, `calcularDerivado()` são os pontos de integração principais.
- Entidades da Ficha de Spec 006: `Ficha`, `FichaAtributo`, `FichaBonus`, `FichaVida`, `FichaVidaMembro`, `FichaEssencia`, `FichaAmeaca`.
- `NivelConfig`, `AtributoConfig`, `BonusConfig`, `MembroCorpoConfig`, `VantagemConfig` — configurações do jogo usadas nos cálculos.
- `RacaClassePermitida`, `VantagemPreRequisito` de Spec 004 — usados nas validações.
- `FichaService` de Spec 006 — ponto de injeção do cálculo/validação.
- `docs/glossario/04-siglas-formulas.md` — especificação das fórmulas e variáveis.
- `docs/glossario/03-termos-dominio.md` — definição de vida, essência, ameaça.
