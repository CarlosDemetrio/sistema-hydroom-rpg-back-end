# T7 — Testes de Integracao Completos: Sistema de Itens

> Fase: Backend — Testes | Prioridade: P1
> Dependencias: T1, T2, T3, T4, T5, T6 todos concluidos
> Bloqueia: nada (task final do backend desta spec)
> Estimativa: 2-3 dias

---

## Objetivo

Garantir cobertura de testes de integracao para todas as entidades e servicos da Spec 016. O padrao e o mesmo das demais specs: testes de integracao com H2 in-memory, `@Transactional`, rollback automatico, `@DisplayName` descritivo, padrão Arrange-Act-Assert.

---

## Classes de Teste a Criar

### 1. RaridadeItemConfigServiceIntegrationTest

```java
// Estende BaseConfiguracaoServiceIntegrationTest<RaridadeItemConfig, RaridadeItemConfigService, RaridadeItemConfigResponse>
// Cobre automaticamente ~10 cenarios base do BaseConfiguracaoServiceIntegrationTest
```

**Cenarios adicionais especificos:**

| ID | Cenario | Resultado esperado |
|----|---------|-------------------|
| RAR-01 | Criar raridade com cor invalida ("#GGHHII") | HTTP 400 |
| RAR-02 | Criar raridade com ordemExibicao duplicado no mesmo jogo | HTTP 409 |
| RAR-03 | Deletar raridade usada em ItemConfig | HTTP 409 |
| RAR-04 | Criar raridade com podeJogadorAdicionar=true | Criada corretamente |
| RAR-05 | Criar raridade com ranges bonusAtributoMin > bonusAtributoMax | HTTP 400 |

---

### 2. TipoItemConfigServiceIntegrationTest

```java
// Estende BaseConfiguracaoServiceIntegrationTest<TipoItemConfig, TipoItemConfigService, TipoItemConfigResponse>
```

**Cenarios adicionais:**

| ID | Cenario | Resultado esperado |
|----|---------|-------------------|
| TIPO-01 | Criar tipo com categoria=ARMA e subcategoria=ESPADA | Criado corretamente |
| TIPO-02 | Criar tipo sem subcategoria (nullable) | Criado corretamente |
| TIPO-03 | Deletar tipo usado em ItemConfig | HTTP 409 |
| TIPO-04 | Criar tipo com requerDuasMaos=true | Criado corretamente |

---

### 3. ItemConfigServiceIntegrationTest

**Cenarios:**

| ID | Cenario | Resultado esperado |
|----|---------|-------------------|
| ITEM-01 | Criar item com raridade e tipo do mesmo jogo | 201 |
| ITEM-02 | Criar item com raridade de outro jogo | 422 |
| ITEM-03 | Criar item com tipo de outro jogo | 422 |
| ITEM-04 | Criar item com nome duplicado no mesmo jogo | 409 |
| ITEM-05 | Criar ItemEfeito BONUS_DERIVADO com bonusAlvo do mesmo jogo | 201 |
| ITEM-06 | Criar ItemEfeito BONUS_DERIVADO com bonusAlvo de outro jogo | 422 |
| ITEM-07 | Criar ItemEfeito FORMULA_CUSTOMIZADA com formula invalida | 422 |
| ITEM-08 | Criar ItemEfeito FORMULA_CUSTOMIZADA com formula valida | 201 |
| ITEM-09 | Criar ItemRequisito NIVEL com valorMinimo=5 | 201 |
| ITEM-10 | Criar ItemRequisito ATRIBUTO com alvo="FOR" e valorMinimo=10 | 201 |
| ITEM-11 | Soft delete de ItemConfig com FichaItem existente | 204, FichaItem preservado |
| ITEM-12 | Listar itens com filtro por raridade | Retorna apenas itens da raridade |
| ITEM-13 | Listar itens com filtro por nome parcial | Retorna matches case-insensitive |
| ITEM-14 | GET detalhe de item inclui efeitos e requisitos sem N+1 | 200 com listas |

---

### 4. ClasseEquipamentoInicialServiceIntegrationTest

| ID | Cenario | Resultado esperado |
|----|---------|-------------------|
| CEI-01 | Criar ClasseEquipamentoInicial com item e classe do mesmo jogo | 201 |
| CEI-02 | Criar ClasseEquipamentoInicial com item de jogo diferente | 422 |
| CEI-03 | Criar 3 itens no grupo 1 de uma classe | 3 criados corretamente |
| CEI-04 | Listar equipamentos iniciais de classe retorna obrigatorios primeiro | 200, ordenacao correta |
| CEI-05 | Soft delete de ClasseEquipamentoInicial | 204 |

---

### 5. FichaItemServiceIntegrationTest

| ID | Cenario | Resultado esperado |
|----|---------|-------------------|
| FI-01 | Mestre adiciona item de raridade Incomum a ficha | 201 |
| FI-02 | Jogador adiciona item de raridade Comum (podeJogadorAdicionar=true) | 201 |
| FI-03 | Jogador tenta adicionar item de raridade Incomum (podeJogadorAdicionar=false) | 403 |
| FI-04 | Mestre adiciona item com requisito NIVEL nao atendido, forcarAdicao=false | 422 com descricao do requisito |
| FI-05 | Mestre adiciona item com requisito NIVEL nao atendido, forcarAdicao=true | 201 |
| FI-06 | Equipar item com duracaoAtual=10 | 200, equipado=true |
| FI-07 | Equipar item com duracaoAtual=0 | 422 "Item quebrado" |
| FI-08 | Desequipar item equipado | 200, equipado=false |
| FI-09 | Decrementar durabilidade de 5 para 3 | 200, duracaoAtual=3 |
| FI-10 | Decrementar durabilidade para 0: item desequipado | 200, equipado=false |
| FI-11 | Restaurar durabilidade | 200, duracaoAtual=duracaoPadrao |
| FI-12 | Listar inventario: separado equipados de inventario | 200, listas corretas |
| FI-13 | Listar inventario inclui pesoTotal | 200, pesoTotal = soma dos pesos |
| FI-14 | Jogador remove item nao-obrigatorio | 204 |
| FI-15 | Jogador tenta remover item obrigatorio | 403 |
| FI-16 | Mestre remove qualquer item | 204 |
| FI-17 | Adicionar item customizado (sem itemConfig) como Mestre | 201 |

---

### 6. FichaCalculationItemEfeitoIntegrationTest

| ID | Cenario | Resultado esperado |
|----|---------|-------------------|
| CALC-01 | Equipar item com BONUS_DERIVADO(B.B.A, +1): FichaBonus.BBA.itens == 1 | Correto |
| CALC-02 | Equipar item com BONUS_ATRIBUTO(FOR, +2): FichaAtributo.FOR.itens == 2 | Correto |
| CALC-03 | Equipar item com BONUS_VIDA(+5): FichaVida.itens == 5 | Correto |
| CALC-04 | Equipar item com BONUS_ESSENCIA(+3): FichaEssencia.itens == 3 | Correto |
| CALC-05 | Dois itens BONUS_DERIVADO(B.B.A, +1) equipados: FichaBonus.BBA.itens == 2 | Correto |
| CALC-06 | Desequipar item BONUS_DERIVADO(B.B.A, +1): FichaBonus.BBA.itens == 0 | Correto |
| CALC-07 | Recalcular 2x nao acumula | Idempotente |
| CALC-08 | Item quebrado (duracaoAtual=0) ignorado no calculo | itens == 0 |
| CALC-09 | Item customizado (itemConfig=null) nao lanca excecao | Calcula normalmente |
| CALC-10 | FORMULA_CUSTOMIZADA com formula "nivel_personagem * 2" em nivel 5: bonus=10 | Correto |
| CALC-11 | FichaBonus.total inclui campo itens | total correto |
| CALC-12 | FichaAtributo.total inclui campo itens | total correto |

---

## Criterios de Aceitacao Globais

- [ ] Todos os 12+ cenarios de FI-* passam
- [ ] Todos os 12+ cenarios de CALC-* passam
- [ ] `BaseConfiguracaoServiceIntegrationTest` aplicado a RaridadeItemConfig e TipoItemConfig
- [ ] Sem N+1 nos testes que verificam listagem de inventario (usando query log ou counter)
- [ ] `./mvnw test` passa com numero total de testes >= 457 + novos testes desta spec

---

*Produzido por: Business Analyst/PO | 2026-04-04*
