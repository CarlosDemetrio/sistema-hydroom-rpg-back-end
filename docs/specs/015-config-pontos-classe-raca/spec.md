# Spec 015 — ConfigPontos Raca/Classe + DefaultProvider

> Spec: `015-config-pontos-classe-raca`
> Epic: GAP-PONTOS-CONFIG (decisao PO 2026-04-03) + BUG-DC-02..09 (auditoria DefaultProvider)
> Status: PLANEJADO
> Depende de: Spec 004 (ClassePersonagem, Raca, VantagemConfig — todos implementados)
> Bloqueia: Spec 006 (wizard precisa de pontos corretos), Spec 007 (motor precisa de pontos para calculo)

---

## 1. Visao Geral do Negocio

**Problema 1 — GAP-PONTOS-CONFIG:** O PO confirmou (Q14, 2026-04-03) que Raca e Classe devem liberar pontos configuraveis por nivel do personagem, alem de `NivelConfig`. Atualmente, APENAS `NivelConfig` libera `pontosAtributo`, `pontosAptidao` e `pontosVantagem`. O Mestre precisa configurar que "Guerreiro nivel 5 libera +2 pontos de atributo" ou "Elfo nivel 1 libera +1 ponto de aptidao". Alem disso, classes e racas podem conceder vantagens pre-definidas automaticamente ao atingir determinado nivel.

**Problema 2 — BUG-DC-02..09:** A auditoria `docs/analises/DEFAULT-CONFIG-AUDITORIA.md` revelou 8+ bugs no `DefaultGameConfigProviderImpl` e `GameConfigInitializerService`. Todo jogo novo e criado com dados incorretos ou incompletos: limitadores ignorados, membros com porcentagem errada, genero/indole/presenca com valores divergentes do glossario, e configuracoes essenciais ausentes (BonusConfig, PontosVantagemConfig, CategoriaVantagem, vantagens canonicas).

**Problema 3 — pontosDisponiveis incompleto:** O calculo de `pontosAtributoDisponiveis` (referenciado em Spec 006 T5 / Spec 012 T5) usa APENAS `NivelConfig`. Com as novas entidades, deve somar tambem `ClassePontosConfig` e `RacaPontosConfig`.

**Problema 4 — Auto-concessao de vantagens:** Quando um personagem atinge um nivel, o sistema deve verificar `ClasseVantagemPreDefinida` e `RacaVantagemPreDefinida` e auto-criar `FichaVantagem` sem custo de pontos.

**Decisao do PO (2026-04-03):**
> "Classe e Raca devem poder liberar pontos configuraveis por nivel, alem de NivelConfig. Pontos acumulam entre niveis."

---

## 2. Atores Envolvidos

| Ator | Role | Acoes |
|------|------|-------|
| Mestre | MESTRE | Configura pontos por nivel para Classe e Raca; define vantagens pre-definidas |
| Sistema | — | Soma pontos de NivelConfig + ClassePontosConfig + RacaPontosConfig; auto-concede vantagens |
| Jogador | JOGADOR | Visualiza pontos disponiveis (leitura); recebe vantagens automaticas |
| Backend | — | Cria entidades, CRUD endpoints, atualiza motor de calculos e default provider |

---

## 3. Novas Entidades de Configuracao

### 3.1 ClassePontosConfig (sub-recurso de ClassePersonagem)

| Campo | Tipo | Constraint | Descricao |
|-------|------|-----------|-----------|
| classePersonagem | FK → ClassePersonagem | NOT NULL | Classe pai |
| nivel | int | NOT NULL, >= 1 | Nivel do personagem que aciona este bonus |
| pontosAtributo | int | NOT NULL, DEFAULT 0, >= 0 | Pontos de atributo liberados nesse nivel |
| pontosAptidao | int | NOT NULL, DEFAULT 0, >= 0 | Pontos de aptidao liberados nesse nivel |
| pontosVantagem | int | NOT NULL, DEFAULT 0, >= 0 | Pontos de vantagem liberados nesse nivel |

**Unique constraint:** `(classe_personagem_id, nivel)` — impede duplicatas de nivel por classe.
**Heranca:** `extends BaseEntity` (soft delete, audit fields).
**Semantica:** "Ao atingir nivel X com essa classe, o personagem ganha Y pontos adicionais."

### 3.2 ClasseVantagemPreDefinida (sub-recurso de ClassePersonagem)

| Campo | Tipo | Constraint | Descricao |
|-------|------|-----------|-----------|
| classePersonagem | FK → ClassePersonagem | NOT NULL | Classe pai |
| nivel | int | NOT NULL, >= 1 | Nivel em que a vantagem e auto-concedida |
| vantagemConfig | FK → VantagemConfig | NOT NULL | Vantagem a ser concedida |

**Unique constraint:** `(classe_personagem_id, nivel, vantagem_config_id)` — impede duplicatas.
**Heranca:** `extends BaseEntity` (soft delete, audit fields).
**Semantica:** "Ao atingir nivel X com essa classe, o personagem recebe automaticamente a vantagem Y sem custo de pontos."

### 3.3 RacaPontosConfig (sub-recurso de Raca)

| Campo | Tipo | Constraint | Descricao |
|-------|------|-----------|-----------|
| raca | FK → Raca | NOT NULL | Raca pai |
| nivel | int | NOT NULL, >= 1 | Nivel do personagem que aciona este bonus |
| pontosAtributo | int | NOT NULL, DEFAULT 0, >= 0 | Pontos de atributo liberados nesse nivel |
| pontosAptidao | int | NOT NULL, DEFAULT 0, >= 0 | Pontos de aptidao liberados nesse nivel |
| pontosVantagem | int | NOT NULL, DEFAULT 0, >= 0 | Pontos de vantagem liberados nesse nivel |

**Unique constraint:** `(raca_id, nivel)` — impede duplicatas de nivel por raca.
**Heranca:** `extends BaseEntity` (soft delete, audit fields).

### 3.4 RacaVantagemPreDefinida (sub-recurso de Raca)

| Campo | Tipo | Constraint | Descricao |
|-------|------|-----------|-----------|
| raca | FK → Raca | NOT NULL | Raca pai |
| nivel | int | NOT NULL, >= 1 | Nivel em que a vantagem e auto-concedida |
| vantagemConfig | FK → VantagemConfig | NOT NULL | Vantagem a ser concedida |

**Unique constraint:** `(raca_id, nivel, vantagem_config_id)` — impede duplicatas.
**Heranca:** `extends BaseEntity` (soft delete, audit fields).

---

## 4. Regras de Negocio

### RN-015-01 — Acumulacao de Pontos
Pontos de Classe e Raca ACUMULAM com pontos de NivelConfig. O calculo total e:
```
pontosAtributoDisponiveis =
    SUM(NivelConfig.pontosAtributo WHERE nivel <= fichaLevel)
  + SUM(ClassePontosConfig.pontosAtributo WHERE classeId = ficha.classe AND nivel <= fichaLevel)
  + SUM(RacaPontosConfig.pontosAtributo WHERE racaId = ficha.raca AND nivel <= fichaLevel)
  - pontosAtributoGastos
```
Idem para `pontosAptidao` e `pontosVantagem`.

### RN-015-02 — Pontos por Nivel (nao por range)
Cada entrada de `ClassePontosConfig` / `RacaPontosConfig` refere-se a um nivel especifico. Se um personagem e nivel 5, somam-se os pontos de entradas com `nivel <= 5`.

### RN-015-03 — Vantagens Pre-definidas: Auto-concessao
Quando um personagem atinge um nivel (via criacao nivel 1 ou level up), o sistema deve:
1. Verificar `ClasseVantagemPreDefinida` para a classe do personagem no nivel atingido
2. Verificar `RacaVantagemPreDefinida` para a raca do personagem no nivel atingido
3. Para cada vantagem encontrada: criar `FichaVantagem` com `custoPago = 0` e `origem = SISTEMA`
4. NAO duplicar: se a `FichaVantagem` para aquela `vantagemConfig` ja existe, ignorar

### RN-015-04 — Vantagens Pre-definidas: Custo Zero
Vantagens concedidas pelo sistema via pre-definicao NAO consomem pontos de vantagem do jogador. O campo `custoPago = 0` diferencia de vantagens compradas pelo jogador.

### RN-015-05 — Campo Origem em FichaVantagem
Adicionar campo `origem` em `FichaVantagem`:
- `JOGADOR` — comprada pelo jogador com pontos
- `MESTRE` — concedida pelo Mestre (Insolitus ou manual)
- `SISTEMA` — auto-concedida por ClasseVantagemPreDefinida ou RacaVantagemPreDefinida

### RN-015-06 — Validacao de VantagemConfig no Mesmo Jogo
Ao criar `ClasseVantagemPreDefinida` ou `RacaVantagemPreDefinida`, a `vantagemConfig` referenciada deve pertencer ao mesmo jogo da classe/raca.

### RN-015-07 — Cascata de Exclusao
Ao deletar (soft delete) uma `ClassePersonagem`, suas `ClassePontosConfig` e `ClasseVantagemPreDefinida` devem ser soft-deleted em cascata. Idem para `Raca`.

### RN-015-08 — DefaultProvider: Coerencia com Glossario
Todos os valores default devem seguir o glossario Klayrah (`docs/glossario/`). Divergencias identificadas na auditoria devem ser corrigidas.

---

## 5. Bugs do DefaultProvider (BUG-DC-02..09)

Referencia completa: `docs/analises/DEFAULT-CONFIG-AUDITORIA.md`

| Bug | Arquivo | Problema | Correcao |
|-----|---------|---------|---------|
| BUG-DC-02 | GameConfigInitializerService | `limitadorAtributo` hardcoded = 50 | Usar valor do DTO: `nivel.setLimitadorAtributo(dto.getLimitadorAtributo())` |
| BUG-DC-03 | GameConfigInitializerService | `createLimitadores()` comentado | Descomentar e implementar o metodo |
| BUG-DC-06 | DefaultGameConfigProviderImpl | Cabeca porcentagem = 0.25 | Corrigir para **0.75** (75% da vida — decisao PO) |
| BUG-DC-07 | DefaultGameConfigProviderImpl | Indole: 9 alinhamentos D&D | Substituir por 3 valores: Bom, Mau, Neutro |
| BUG-DC-08 | DefaultGameConfigProviderImpl | Presenca: escala intensidade | Substituir por 4 valores: Bom, Leal, Caotico, Neutro |
| BUG-DC-09 | DefaultGameConfigProviderImpl | Genero: 4 valores | Substituir por 3 valores: Masculino, Feminino, Outro |
| DIV-01 | DefaultGameConfigProviderImpl | "Necromance" | Corrigir para "Necromante" |
| DIV-05 | DefaultGameConfigProviderImpl | Membro "Sangue" ausente | Adicionar Sangue com porcentagem 1.00 (100% da vida) |

### Defaults Ausentes (adicionar ao provider)

| Config | Dados |
|--------|-------|
| BonusConfig (9) | B.B.A, B.B.M, Defesa, Esquiva, Iniciativa, Percepcao, Raciocinio, Bloqueio, Reflexo — com formulas |
| PontosVantagemConfig (8) | nivel 1→6pts, 5→3pts, 10→10pts, 15→3pts, 20→10pts, 25→3pts, 30→15pts, 35→3pts |
| CategoriaVantagem (8) | Treinamento Fisico, Treinamento Mental, Acao, Reacao, Vantagem de Atributo, Vantagem Geral, Vantagem Historica, Vantagem de Renascimento |
| Vantagens canonicas (~22) | TCO, TCD, TCE, TM, TPM, TL, T.M, CFM, DM, DF, DV, SG, IN, Saude de Ferro, Ataque Adicional, Ataque Sentai, Contra-Ataque, Intercepcao, Riqueza, Capangas, Ultimo Sigilo, Pensamento Bifurcado |

---

## 6. Impacto no FichaCalculationService

### Calculo de Pontos Disponiveis (Spec 006 T5 / Spec 012 T5)

**Antes (incompleto):**
```
pontosAtributoDisponiveis = SUM(NivelConfig.pontosAtributo) - gastos
```

**Depois (completo):**
```
pontosAtributoDisponiveis =
    SUM(NivelConfig.pontosAtributo WHERE nivel <= fichaLevel)
  + SUM(ClassePontosConfig.pontosAtributo WHERE classe = ficha.classe AND nivel <= fichaLevel)
  + SUM(RacaPontosConfig.pontosAtributo WHERE raca = ficha.raca AND nivel <= fichaLevel)
  - SUM(FichaAtributo.base)  // pontos gastos
```

### Auto-concessao de Vantagens

**Triggers:**
1. `FichaService.criarFicha()` → verificar vantagens pre-definidas para nivel = 1
2. `FichaService.recalcularNivel()` → verificar vantagens pre-definidas para o novo nivel alcancado

---

## 7. Endpoints Novos

### Sub-recursos de ClassePersonagem

| Metodo | URL | Role | Descricao |
|--------|-----|------|-----------|
| GET | `/api/v1/classes/{id}/pontos-config` | MESTRE, JOGADOR | Listar pontos por nivel da classe |
| POST | `/api/v1/classes/{id}/pontos-config` | MESTRE | Criar entrada de pontos para um nivel |
| PUT | `/api/v1/classes/{id}/pontos-config/{pontosConfigId}` | MESTRE | Atualizar entrada |
| DELETE | `/api/v1/classes/{id}/pontos-config/{pontosConfigId}` | MESTRE | Soft delete |
| GET | `/api/v1/classes/{id}/vantagens-predefinidas` | MESTRE, JOGADOR | Listar vantagens pre-definidas da classe |
| POST | `/api/v1/classes/{id}/vantagens-predefinidas` | MESTRE | Adicionar vantagem pre-definida |
| DELETE | `/api/v1/classes/{id}/vantagens-predefinidas/{predefinidaId}` | MESTRE | Soft delete |

### Sub-recursos de Raca

| Metodo | URL | Role | Descricao |
|--------|-----|------|-----------|
| GET | `/api/v1/racas/{id}/pontos-config` | MESTRE, JOGADOR | Listar pontos por nivel da raca |
| POST | `/api/v1/racas/{id}/pontos-config` | MESTRE | Criar entrada de pontos para um nivel |
| PUT | `/api/v1/racas/{id}/pontos-config/{pontosConfigId}` | MESTRE | Atualizar entrada |
| DELETE | `/api/v1/racas/{id}/pontos-config/{pontosConfigId}` | MESTRE | Soft delete |
| GET | `/api/v1/racas/{id}/vantagens-predefinidas` | MESTRE, JOGADOR | Listar vantagens pre-definidas da raca |
| POST | `/api/v1/racas/{id}/vantagens-predefinidas` | MESTRE | Adicionar vantagem pre-definida |
| DELETE | `/api/v1/racas/{id}/vantagens-predefinidas/{predefinidaId}` | MESTRE | Soft delete |

---

## 8. Pontos em Aberto

| ID | Questao | Impacto | Status |
|----|---------|---------|--------|
| PA-015-01 | Dados default de ClassePontosConfig por classe (ex: Guerreiro nivel 1 = +2 atributo?) | T5 defaults | Confirmar com PO |
| PA-015-02 | Dados default de RacaPontosConfig por raca (ex: Elfo nivel 1 = +1 aptidao?) | T5 defaults | Confirmar com PO |
| PA-015-03 | Quais vantagens pre-definidas por classe/raca no default? | T5 defaults | Confirmar com PO |
| PA-015-04 | Campo `origem` em FichaVantagem: enum com JOGADOR/MESTRE/SISTEMA ou string livre? | T4 | Recomendar enum (mais seguro) |

---

## 9. Criterios de Aceitacao Globais

- [ ] 4 novas entidades criadas com BaseEntity, soft delete, audit fields
- [ ] 14 novos endpoints (7 para Classe, 7 para Raca) funcionando com seguranca
- [ ] Calculo de pontosDisponiveis soma NivelConfig + ClassePontosConfig + RacaPontosConfig
- [ ] Vantagens pre-definidas auto-concedidas na criacao (nivel 1) e level up
- [ ] Todos os bugs BUG-DC-02..09 corrigidos no DefaultProvider
- [ ] BonusConfig, PontosVantagemConfig, CategoriaVantagem adicionados ao provider
- [ ] Vantagens canonicas adicionadas ao provider com categorias corretas
- [ ] Frontend: abas de pontos e vantagens pre-definidas em ClassePersonagem e Raca
- [ ] `./mvnw test` passa (457+ testes existentes nao quebram + novos testes)
- [ ] Coerencia completa entre DefaultProvider e glossario Klayrah

---

*Produzido por: PM/Scrum Master | 2026-04-04*
*Base: DEFAULT-CONFIG-AUDITORIA.md, BA-GAPS-2026-04-02.md (Q14, Q16), decisoes PO 2026-04-03/04*
