> **Status: VALIDO** | Ultima revisao: 2026-04-02
> VantagemConfig CRUD completo com pre-requisitos (deteccao ciclos DFS), CategoriaVantagem FK, e VantagemEfeito (8 tipos).
> O que mudou desde a escrita: VantagemEfeito implementado com 8 tipos (BONUS_ATRIBUTO, BONUS_APTIDAO, BONUS_DERIVADO, BONUS_VIDA, BONUS_VIDA_MEMBRO, BONUS_ESSENCIA, DADO_UP, FORMULA_CUSTOMIZADA). GAP-03 confirma que VantagemEfeito NAO esta integrado ao FichaCalculationService (VT=0 hardcoded). Frontend sem UI para efeitos (SP2-T06 pendente).

# BA-VANTAGEM-CONFIG.md — Análise Completa: Configuração de Vantagem

> Documento de análise de negócio para a entidade `VantagemConfig` e seus relacionamentos.
> Destina-se a guiar o desenvolvimento frontend, os testes de integração e o alinhamento de equipe.

---

## 1. Visão Geral

**Vantagens** são habilidades especiais, talentos e poderes que um personagem pode adquirir permanentemente, gastando **pontos de vantagem** acumulados ao subir de nível. São o principal mecanismo de especialização e diferenciação entre personagens.

### Por que vantagens existem

Dois Guerreiros com os mesmos atributos e mesma classe podem ser radicalmente diferentes pelas vantagens que escolheram. Um pode ter "Ataque Adicional" (age duas vezes por rodada), outro "Contra-Ataque" (reage ao ser atacado). As vantagens expressam o *caminho de desenvolvimento único* de cada personagem.

### Princípio central

Tudo configurável pelo Mestre. O Mestre define quantas vantagens existem, o que elas fazem (efeitos), quanto custam (fórmula), quando podem ser compradas (pré-requisitos), e como evoluem (nível máximo). Nada é hardcoded — até a progressão de dados é configurável via tipo de efeito.

### Regra fundamental

Uma vez comprada, **uma vantagem não pode ser removida da ficha**. O nível só pode subir, nunca descer. Isso representa um investimento permanente no desenvolvimento do personagem.

### Entidades envolvidas

| Entidade | Responsabilidade |
|---|---|
| `VantagemConfig` | Configuração master da vantagem (nome, sigla, custo, níveis) |
| `CategoriaVantagem` | Agrupamento organizacional (ex: Treinamento Físico, Ação) |
| `VantagemEfeito` | O que a vantagem *faz* mecanicamente (bônus, vida, etc.) |
| `VantagemPreRequisito` | Condições que a ficha deve satisfazer para comprar |
| `FichaVantagem` | Instância da vantagem na ficha (nível atual, custo pago) |
| `PontosVantagemConfig` | Tabela de pontos ganhos por nível de personagem |

---

## 2. Entidades e Relacionamentos (diagrama textual)

```
Jogo
 |
 +-- CategoriaVantagem (N) ← agrupamento com nome, descrição e cor
 |
 +-- PontosVantagemConfig (N) ← pontos ganhos por nível (1 registro por nível por jogo)
 |
 +-- VantagemConfig (N) ← a vantagem em si
       |
       +-- categoriaVantagem (FK, opcional) → CategoriaVantagem
       |
       +-- preRequisitos (OneToMany) → VantagemPreRequisito
       |         vantagem_id  → VantagemConfig (quem exige)
       |         requisito_id → VantagemConfig (quem é exigido)
       |         nivel_minimo → Integer
       |
       +-- efeitos (OneToMany, cascade ALL) → VantagemEfeito
                 tipoEfeito     → TipoEfeito (enum, 8 valores)
                 atributoAlvo   → AtributoConfig (FK, opcional)
                 aptidaoAlvo    → AptidaoConfig (FK, opcional)
                 bonusAlvo      → BonusConfig (FK, opcional)
                 membroAlvo     → MembroCorpoConfig (FK, opcional)
                 valorFixo      → BigDecimal (opcional)
                 valorPorNivel  → BigDecimal (opcional)
                 formula        → String (opcional, exp4j)
                 descricaoEfeito → String

Ficha
 |
 +-- FichaVantagem (N)
       ficha_id          → Ficha
       vantagem_config_id → VantagemConfig
       nivel_atual       → Integer (mínimo 1, nunca decresce)
       custo_pago        → Integer (total já gasto)
```

### Cardinalidades importantes

- `Jogo → VantagemConfig`: 1:N (unique por `jogo_id + nome` e `jogo_id + sigla`)
- `VantagemConfig → VantagemEfeito`: 1:N (cascade ALL, orphanRemoval — efeitos pertencem exclusivamente à vantagem)
- `VantagemConfig → VantagemPreRequisito`: 1:N (sem cascade — pré-requisitos são gerenciados separadamente via endpoint)
- `Ficha → FichaVantagem`: 1:N (unique por `ficha_id + vantagem_config_id` — cada vantagem só pode existir uma vez na ficha)
- `PontosVantagemConfig → Jogo`: unique por `jogo_id + nivel` — um registro por nível por jogo

---

## 3. Campos e Regras de Negócio (por campo, com exemplos reais)

### VantagemConfig

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK, gerado | 42 |
| `jogo` | FK → Jogo | Sim | NOT NULL | Jogo "Klayrah Campanha 1" |
| `nome` | String | Sim | max 100, unique por jogo (case-insensitive) | "Treinamento em Combate Ofensivo" |
| `sigla` | String | Não | 2-5 chars, unique por jogo cross-entity (case-insensitive) | "TCO" |
| `descricao` | String | Não | max 1000 | "Treinamento intensivo em técnicas ofensivas de combate corpo a corpo." |
| `nivelMaximo` | Integer | Sim | min 1, default 10 | 10 (TCO vai de nível 1 a 10) |
| `formulaCusto` | String | Sim | max 100, validada por exp4j, variáveis: `custo_base` e `nivel_vantagem` | "custo_base * nivel_vantagem" |
| `descricaoEfeito` | String | Não | max 500 | "+1 em B.B.A por nível. A partir do nível 5: D.UP ativo." |
| `categoriaVantagem` | FK → CategoriaVantagem | Não | opcional | Categoria "Treinamento Físico" |
| `ordemExibicao` | Integer | Não | default 0 | 1 (aparece primeiro na lista) |
| `preRequisitos` | List<VantagemPreRequisito> | — | gerenciados via endpoint separado | [TCO nível 3 para Ataque Sentai] |
| `efeitos` | List<VantagemEfeito> | — | cascade ALL, gerenciados via endpoint separado | [BONUS_DERIVADO → BBA, valorPorNivel=1] |
| `deletedAt` | LocalDateTime | — | soft delete (herdado de BaseEntity) | null = ativo |

#### Regras de negócio dos campos

**nome**: unicidade case-insensitive por jogo. "TCO" e "tco" conflitam. Erro: HTTP 409 Conflict.

**sigla**: completamente opcional. Quando preenchida, deve ser única no escopo do jogo inteiro — não apenas entre vantagens, mas também entre atributos, bônus, e qualquer outra entidade com sigla. Validada pelo `SiglaValidationService`. Se `FOR` já é abreviação do atributo Força, nenhuma vantagem pode usar `FOR`. Erro: HTTP 409 Conflict com mensagem indicando onde a sigla já está em uso.

**formulaCusto**: validada via `FormulaEvaluatorService`. Variáveis permitidas: `custo_base` e `nivel_vantagem`. Qualquer outra variável gera HTTP 422 Unprocessable Entity. Exemplos válidos:
- `"custo_base * nivel_vantagem"` — custo cresce linearmente
- `"custo_base * nivel_vantagem * nivel_vantagem"` — custo cresce quadraticamente
- `"custo_base"` — custo fixo, independente do nível
- `"ceil(custo_base * nivel_vantagem / 2)"` — custo com arredondamento

**nivelMaximo**: define o teto da vantagem. Uma vantagem com `nivelMaximo = 1` não pode ser evoluída (vantagem "one-shot"). Uma com `nivelMaximo = 10` pode ser melhorada 10 vezes. O campo `FichaVantagem.podeSubirNivel()` retorna `false` quando `nivelAtual >= nivelMaximo`.

**descricaoEfeito**: texto livre para descrever o efeito ao jogador. É o texto que aparece na interface da ficha. Não tem impacto mecânico — o impacto mecânico real fica em `VantagemEfeito`.

### VantagemEfeito

| Campo | Tipo | Obrigatório | Quando |
|---|---|---|---|
| `tipoEfeito` | TipoEfeito (enum) | Sim | Sempre |
| `atributoAlvo` | FK → AtributoConfig | Condicional | BONUS_ATRIBUTO |
| `aptidaoAlvo` | FK → AptidaoConfig | Condicional | BONUS_APTIDAO |
| `bonusAlvo` | FK → BonusConfig | Condicional | BONUS_DERIVADO |
| `membroAlvo` | FK → MembroCorpoConfig | Condicional | BONUS_VIDA_MEMBRO |
| `valorFixo` | BigDecimal | Opcional | A maioria dos tipos |
| `valorPorNivel` | BigDecimal | Opcional | A maioria dos tipos |
| `formula` | String | Condicional | FORMULA_CUSTOMIZADA |
| `descricaoEfeito` | String | Não | Qualquer tipo |

**Regra crítica dos alvos**: exatamente um FK de alvo deve ser preenchido por efeito, de acordo com o tipo. Os demais devem ser `null`. O sistema não valida isso automaticamente no banco — a responsabilidade é da camada de serviço/frontend ao criar o efeito.

**Cálculo do valor do efeito no nível N**: `valorFixo + (valorPorNivel * N)`

Exemplos:
- TCO nível 3, `valorFixo = null`, `valorPorNivel = 1` → bônus em BBA = 1 * 3 = +3
- "Saúde de Ferro" nível 2, `valorFixo = null`, `valorPorNivel = 5` → +10 VT
- Vantagem com bônus base + crescimento, `valorFixo = 2`, `valorPorNivel = 1`, nível 4 → 2 + 1*4 = +6

### FichaVantagem

| Campo | Tipo | Regras |
|---|---|---|
| `ficha` | FK → Ficha | NOT NULL |
| `vantagemConfig` | FK → VantagemConfig | NOT NULL |
| `nivelAtual` | Integer | min 1, nunca decresce |
| `custoPago` | Integer | min 0, acumulado total |

`custoPago` registra o total já gasto pelo personagem naquela vantagem ao longo de toda a vida da ficha (soma de todos os upgrades). Usado para histórico e exibição.

### PontosVantagemConfig

| Campo | Tipo | Regras |
|---|---|---|
| `nivel` | Integer | min 1, unique por jogo |
| `pontosGanhos` | Integer | min 0, default 1 |

Funciona como uma tabela de pontos: ao atingir o nível X, o personagem ganha `pontosGanhos` pontos de vantagem. O sistema default concede 1 ponto por nível. O Mestre pode configurar para dar mais pontos em níveis específicos (ex: 3 pontos no nível 10 como marco especial).

---

## 4. Os 8 Tipos de Efeito — Detalhamento Completo

### 4.1 BONUS_ATRIBUTO

**O que é**: Adiciona um bônus permanente ao valor "Outros" de um atributo específico da ficha. Aumenta diretamente um dos 7 atributos (Força, Agilidade, Vigor, Sabedoria, Intuição, Inteligência, Astúcia).

**Campos obrigatórios**: `atributoAlvo` (FK para o atributo-alvo)

**Campos opcionais**: `valorFixo`, `valorPorNivel`, `descricaoEfeito`

**Alvos nulos**: `aptidaoAlvo`, `bonusAlvo`, `membroAlvo`, `formula`

**Como o valor é aplicado**: o total de bônus do efeito (`valorFixo + valorPorNivel * nivelAtual`) é somado ao campo "Outros" do atributo na ficha.

**Exemplos reais Klayrah**:

| Vantagem | Atributo-alvo | valorFixo | valorPorNivel | Efeito em nível 3 |
|---|---|---|---|---|
| Fortitude | Vigor | null | 2 | +6 em Vigor |
| Força Aprimorada | Força | null | 2 | +6 em Força |
| Agilidade Aprimorada | Agilidade | null | 2 | +6 em Agilidade |
| Capacidade de Força Máxima (CFM) | Força | null | 1 | +3 em Força |

**Como o frontend deve apresentar o formulário**:
- Exibir dropdown obrigatório: "Atributo alvo" listando todos os `AtributoConfig` do jogo
- Exibir campo numérico "Valor por nível" (mais comum)
- Exibir campo numérico "Valor fixo" (opcional, bônus base independente do nível)
- Preview calculado: "No nível 3: +X em [Nome do Atributo]"

---

### 4.2 BONUS_APTIDAO

**O que é**: Adiciona um bônus permanente a uma aptidão específica da ficha. Complementa o bônus de classe e o bônus de sorte.

**Campos obrigatórios**: `aptidaoAlvo` (FK para a aptidão-alvo)

**Campos opcionais**: `valorFixo`, `valorPorNivel`, `descricaoEfeito`

**Alvos nulos**: `atributoAlvo`, `bonusAlvo`, `membroAlvo`, `formula`

**Impacto na ficha**: o bônus aparece como uma linha separada no campo "Outros" da aptidão (não é "Classe" nem "Sorte").

**Exemplo real Klayrah**:

| Vantagem | Aptidão-alvo | valorPorNivel | Efeito em nível 2 |
|---|---|---|---|
| Treinamento em Furtividade | Furtividade | 2 | +4 em Furtividade |
| Mestre Diplomata | Diplomacia | 3 | +6 em Diplomacia |

**Como o frontend deve apresentar o formulário**:
- Exibir dropdown obrigatório: "Aptidão alvo" listando todos os `AptidaoConfig` do jogo (agrupados por tipo se possível)
- Exibir campos numéricos "Valor fixo" e "Valor por nível"
- Preview: "No nível 2: +X em [Nome da Aptidão]"

---

### 4.3 BONUS_DERIVADO

**O que é**: Adiciona um bônus permanente a um dos bônus calculados (BonusConfig) da ficha. Esses bônus incluem B.B.A, Bloqueio, Reflexo, B.B.M, Percepção, Raciocínio. O bônus vai para a linha "Vantagens" no detalhamento do bônus na ficha.

**Campos obrigatórios**: `bonusAlvo` (FK para o BonusConfig-alvo)

**Campos opcionais**: `valorFixo`, `valorPorNivel`, `descricaoEfeito`

**Alvos nulos**: `atributoAlvo`, `aptidaoAlvo`, `membroAlvo`, `formula`

**Impacto na ficha**: os bônus de combate têm 5 fontes — Vantagens, Classe, Itens, Glória, Outros. Este efeito alimenta a fonte "Vantagens".

**Exemplos reais Klayrah** (os mais comuns):

| Vantagem | Bônus-alvo | valorFixo | valorPorNivel | Efeito em nível 5 |
|---|---|---|---|---|
| TCO (Treinamento em Combate Ofensivo) | B.B.A | null | 1 | +5 em B.B.A |
| TCD (Treinamento em Combate Defensivo) | Bloqueio | null | 1 | +5 em Bloqueio |
| TCE (Treinamento em Combate Evasivo) | Reflexo | null | 2 | +10 em Reflexo |
| TM (Treinamento Mágico) | B.B.M | null | 1 | +5 em B.B.M |
| Ataque Aprimorado | B.B.A | null | 1 | +5 em B.B.A |
| Defesa Mágica | B.B.M | null | 1 | +5 em B.B.M |

**Como o frontend deve apresentar o formulário**:
- Exibir dropdown obrigatório: "Bônus alvo" listando todos os `BonusConfig` do jogo
- Exibir campos numéricos "Valor fixo" e "Valor por nível"
- Preview: "No nível 5: +X em [Nome do Bônus]"

---

### 4.4 BONUS_VIDA

**O que é**: Adiciona pontos de vida total ao personagem. Os pontos adicionados alimentam o componente **VT** (Vida de Vantagens) na fórmula de Vida Total.

**Campos obrigatórios**: nenhum FK de alvo

**Campos opcionais**: `valorFixo`, `valorPorNivel`, `descricaoEfeito`

**Alvos nulos**: `atributoAlvo`, `aptidaoAlvo`, `bonusAlvo`, `membroAlvo`, `formula`

**Impacto na ficha**: Vida Total = VG (Vigor) + Nível + VT (Vantagens) + Renascimentos + Outros. Este efeito incrementa VT.

**Exemplo real Klayrah**:

| Vantagem | valorPorNivel | Efeito em nível 4 |
|---|---|---|
| Saúde de Ferro | 5 | +20 VT |
| Vida Extra | 5 | +20 VT |

**Como o frontend deve apresentar o formulário**:
- Sem dropdown de alvo (não há FK)
- Exibir campos numéricos "Valor fixo" e "Valor por nível"
- Label contextual: "Bônus em Vida Total (VT)"
- Preview: "No nível 4: +X de Vida Total"

---

### 4.5 BONUS_VIDA_MEMBRO

**O que é**: Adiciona pontos de vida a um membro do corpo específico (ex: Cabeça, Tronco, Braço). Aumenta a capacidade de absorção de dano localizado daquele membro, *além* do cálculo padrão por porcentagem.

**Campos obrigatórios**: `membroAlvo` (FK para o MembroCorpoConfig-alvo)

**Campos opcionais**: `valorFixo`, `valorPorNivel`, `descricaoEfeito`

**Alvos nulos**: `atributoAlvo`, `aptidaoAlvo`, `bonusAlvo`, `formula`

**Diferença de BONUS_VIDA**: BONUS_VIDA aumenta a vida total do personagem (o "pool" global), que depois é distribuído pelos membros via porcentagem. BONUS_VIDA_MEMBRO aumenta diretamente a vida de um membro específico sem alterar o pool global. Útil para vantagens como "Cabeça de Ferro" (mais HP específico na cabeça).

**Exemplo real Klayrah**:

| Vantagem | Membro-alvo | valorPorNivel | Efeito em nível 2 |
|---|---|---|---|
| Cabeça de Ferro | Cabeça | 10 | +20 HP na Cabeça |
| Tronco Blindado | Tronco | 15 | +30 HP no Tronco |

**Como o frontend deve apresentar o formulário**:
- Exibir dropdown obrigatório: "Membro alvo" listando todos os `MembroCorpoConfig` do jogo
- Exibir campos numéricos "Valor fixo" e "Valor por nível"
- Preview: "No nível 2: +X de HP em [Nome do Membro]"

---

### 4.6 BONUS_ESSENCIA

**O que é**: Adiciona pontos de essência (recurso mágico/espiritual) ao personagem. A essência é usada para lançar magias e ativar poderes sobrenaturais.

**Campos obrigatórios**: nenhum FK de alvo

**Campos opcionais**: `valorFixo`, `valorPorNivel`, `descricaoEfeito`

**Alvos nulos**: `atributoAlvo`, `aptidaoAlvo`, `bonusAlvo`, `membroAlvo`, `formula`

**Impacto na ficha**: alimenta o componente "Vantagens" da fórmula de Essência Total: `FLOOR((VIG + SAB) / 2) + Nível + Renascimentos + Vantagens + Outros`.

**Exemplo real Klayrah**:

| Vantagem | valorPorNivel | Efeito em nível 3 |
|---|---|---|
| Essência Ampliada | 10 | +30 de Essência |
| Reserva Mágica | 15 | +45 de Essência |

**Como o frontend deve apresentar o formulário**:
- Sem dropdown de alvo
- Exibir campos numéricos "Valor fixo" e "Valor por nível"
- Label contextual: "Bônus em Essência Total"
- Preview: "No nível 3: +X de Essência"

---

### 4.7 DADO_UP

**O que é**: Mecanismo de progressão de dados de prospecção. A cada nível da vantagem com este efeito, o dado de prospecção "evolui" uma vez na sequência: d3 → d4 → d6 → d8 → d10 → d12.

**Campos obrigatórios**: nenhum FK de alvo, nenhum valor numérico

**Todos os campos de valor/alvo**: null (o incremento é sempre exatamente +1 face na sequência acima)

**Impacto na ficha**: o personagem ganha acesso a um dado de prospecção de maior potência ao usar sua vantagem D.UP em combate ou situações especiais. Não é um bônus numérico fixo — é uma evolução de recurso.

**Contexto Klayrah**: A sequência d3→d4→d6→d8→d10 foi confirmada no glossário. TCO nível 1 confere 1D3, nível 2 confere 1D4, nível 5 confere 1D10.

**Exemplo real Klayrah**:

| Vantagem | Nível 1 | Nível 2 | Nível 5 |
|---|---|---|---|
| D.UP (Dado Up) | 1d3 | 1d4 | 1d10 |

**Como o frontend deve apresentar o formulário**:
- Sem dropdown de alvo
- Sem campos de valor numérico (esconder ou desabilitar)
- Texto informativo fixo: "Este efeito evolui o dado de prospecção (+1 face por nível: d3 → d4 → d6 → d8 → d10 → d12)"
- Preview: "Nível 1: 1d3 / Nível 2: 1d4 / Nível 5: 1d10"

---

### 4.8 FORMULA_CUSTOMIZADA

**O que é**: Tipo de efeito avançado que permite ao Mestre definir um efeito completamente personalizado via fórmula exp4j. Aceita `nivel_vantagem` como variável, além das abreviações de atributos do jogo.

**Campos obrigatórios**: `formula` (string exp4j, max 200 chars)

**Campos opcionais**: `valorFixo`, `valorPorNivel`, `descricaoEfeito`

**Alvos nulos**: todos os FKs de alvo (o alvo é implícito na descrição textual)

**Variáveis disponíveis na fórmula**:
- `nivel_vantagem` — nível atual da vantagem (ex: 1, 2, 3...)
- Abreviações de todos os atributos do jogo: `FOR`, `AGI`, `VIG`, `SAB`, `INT`, `INTU`, `AST`

**Funções disponíveis**: `FLOOR()`, `CEIL()`, `MIN()`, `MAX()`, `ABS()`, `SQRT()`, operadores `+`, `-`, `*`, `/`, `^`, `%`

**Quando usar**: quando nenhum dos outros 7 tipos atende. Ex: vantagem que concede bônus baseado em múltiplos atributos, ou com progressão não linear.

**Exemplos reais Klayrah**:

| Vantagem | Fórmula | Descrição |
|---|---|---|
| Poder da Mente | `FLOOR((INT + SAB) / 4) * nivel_vantagem` | Bônus em B.B.M baseado em dois atributos |
| Golpe Crítico | `nivel_vantagem * 5` | Aumenta chance de crítico em % |

**Como o frontend deve apresentar o formulário**:
- Exibir campo de texto "Fórmula" com sugestão de variáveis disponíveis
- Botão "Validar fórmula" que chama o endpoint de validação antes de salvar
- Exibir todas as variáveis disponíveis como chips clicáveis (nível_vantagem, FOR, AGI, VIG, SAB, INT, INTU, AST)
- Preview calculado mostrando o resultado da fórmula para os níveis 1, 3, 5 e 10

### Tabela-resumo dos 8 tipos

| Tipo | FK alvo obrigatório | Valor numérico | Fórmula |
|---|---|---|---|
| BONUS_ATRIBUTO | `atributoAlvo` | valorFixo / valorPorNivel | — |
| BONUS_APTIDAO | `aptidaoAlvo` | valorFixo / valorPorNivel | — |
| BONUS_DERIVADO | `bonusAlvo` | valorFixo / valorPorNivel | — |
| BONUS_VIDA | nenhum | valorFixo / valorPorNivel | — |
| BONUS_VIDA_MEMBRO | `membroAlvo` | valorFixo / valorPorNivel | — |
| BONUS_ESSENCIA | nenhum | valorFixo / valorPorNivel | — |
| DADO_UP | nenhum | nenhum | — |
| FORMULA_CUSTOMIZADA | nenhum | opcional | `formula` obrigatório |

---

## 5. Sistema de Pré-Requisitos

### O que são

Pré-requisitos são condições que a ficha do personagem deve satisfazer **antes** de poder comprar uma vantagem. São sempre baseados em outras vantagens: "para comprar A, você precisa ter B no nível mínimo X".

### Estrutura do VantagemPreRequisito

```
VantagemPreRequisito {
  vantagem (quem EXIGE o pré-requisito)
  requisito (quem É EXIGIDO)
  nivelMinimo (nível mínimo que o requisito deve estar)
}
```

**Exemplo**: Ataque Sentai exige TCO nível 5.
- `vantagem` = Ataque Sentai
- `requisito` = TCO
- `nivelMinimo` = 5

### Endpoints de gestão

Os pré-requisitos são gerenciados via endpoints dedicados no `VantagemController`:

| Operação | Endpoint | Permissão |
|---|---|---|
| Listar pré-requisitos de uma vantagem | `GET /api/v1/configuracoes/vantagens/{id}/prerequisitos` | MESTRE, JOGADOR |
| Adicionar pré-requisito | `POST /api/v1/configuracoes/vantagens/{id}/prerequisitos` | MESTRE |
| Remover pré-requisito | `DELETE /api/v1/configuracoes/vantagens/{id}/prerequisitos/{prId}` | MESTRE |

### Validações ao adicionar pré-requisito

1. **Vantagem existente**: ambas as vantagens (a que exige e a exigida) devem existir
2. **Mesmo jogo**: ambas devem pertencer ao mesmo jogo
3. **Não duplicado**: o par `(vantagem_id, requisito_id)` deve ser único
4. **Sem auto-referência**: uma vantagem não pode ser pré-requisito de si mesma
5. **Sem ciclo**: verificação DFS (busca em profundidade) para detectar dependências circulares

### Detecção de ciclos — algoritmo DFS

O serviço implementa uma busca em profundidade para evitar grafos cíclicos de pré-requisitos.

**Exemplo de ciclo proibido**:
```
A exige B (nível 1)
B exige C (nível 1)
C exige A (nível 1)  ← PROIBIDO — criaria ciclo A→B→C→A
```

**Como funciona**:
1. Ao adicionar o pré-requisito `(vantagem = X, requisito = Y)`, o sistema percorre *transitivamente* o que `Y` exige
2. Se durante esse percurso encontrar `X`, o sistema detecta ciclo e lança `ConflictException`
3. O algoritmo usa um `Set<Long>` para rastrear visitados (evita loops infinitos em grafos complexos)

**Mensagem de erro**: `"Adicionar este pré-requisito criaria uma dependência circular entre vantagens."`

### Apresentação visual na interface

Na tela de configuração de uma vantagem, a seção de pré-requisitos deve exibir:

```
PRE-REQUISITOS
+--------------------------------------------------+
| Vantagem exigida          | Nível mínimo | Acao  |
|---------------------------|--------------|-------|
| TCO (Treinamento Combat.) | Nível 5      | [X]   |
| TCD                       | Nível 3      | [X]   |
+--------------------------------------------------+
[ + Adicionar pré-requisito ]
```

O modal de adição deve mostrar:
- Dropdown "Vantagem exigida" (todas as vantagens do jogo, exceto a própria)
- Campo numérico "Nível mínimo" (default 1, min 1, max = nivelMaximo da vantagem exigida)
- Aviso visual se houver risco de ciclo (opcional, a validação real é backend)

---

## 6. Sistema de Pontos de Vantagem

### Como os pontos são gerados

A tabela `PontosVantagemConfig` define quantos pontos de vantagem o personagem ganha ao atingir cada nível. A configuração padrão é 1 ponto por nível. O Mestre pode customizar isso — por exemplo, conceder 3 pontos em marcos especiais como nível 10, 20 e 30.

```
NivelPersonagem → PontosVantagemConfig.nivel
                  PontosVantagemConfig.pontosGanhos (ex: 1)

Total de pontos acumulados = soma de pontosGanhos de todos os níveis atingidos
Pontos disponíveis = total acumulado - total já gasto em vantagens
```

### formulaCusto — cálculo do custo ao evoluir

O campo `VantagemConfig.formulaCusto` define quanto custa **um nível específico** da vantagem.

**Variáveis da fórmula**: `custo_base` e `nivel_vantagem`

**Atenção**: `nivel_vantagem` na fórmula de custo refere-se ao *nível que está sendo comprado*. Se o personagem está comprando o nível 3 de TCO, `nivel_vantagem = 3`.

**Exemplos de fórmulas e seus custos progressivos**:

| Fórmula | Nível 1 | Nível 2 | Nível 3 | Nível 5 | Total até nível 5 |
|---|---|---|---|---|---|
| `custo_base * nivel_vantagem` (base=2) | 2 | 4 | 6 | 10 | 30 |
| `custo_base` (base=4) | 4 | 4 | 4 | 4 | 20 |
| `custo_base * nivel_vantagem * nivel_vantagem` (base=1) | 1 | 4 | 9 | 25 | 55 |
| `ceil(custo_base * nivel_vantagem / 2)` (base=3) | 2 | 3 | 5 | 8 | 23 |

### Validação de pontos ao comprar

Ao comprar/evoluir uma vantagem na ficha, o sistema deve verificar:
1. Calcular o custo do nível sendo comprado via `formulaCusto` substituindo `nivel_vantagem` pelo novo nível
2. Calcular os pontos disponíveis do personagem (acumulados - gastos)
3. Verificar se os pontos disponíveis cobrem o custo
4. Verificar se todos os pré-requisitos estão satisfeitos na ficha
5. Verificar que `nivelAtual < nivelMaximo` (`FichaVantagem.podeSubirNivel()`)

### Regra de não remoção

**Uma vez comprada, a FichaVantagem nunca pode ser deletada.** Só pode:
- Ter seu `nivelAtual` incrementado (`nivelAtual < nivelMaximo`)
- Ter `custoPago` atualizado ao subir de nível

Tentativas de remoção devem ser bloqueadas pelo backend (sem endpoint de delete para FichaVantagem). O frontend não deve exibir botão de remover vantagem da ficha.

### Exemplo completo de cálculo

Personagem nível 7, PontosVantagemConfig padrão (1 por nível):
- Pontos acumulados: 7
- TCO já no nível 3, custo pago até agora: 2 + 4 + 6 = 12 — espera, isso excede 7?

Observação: os pontos são acumulados ao longo da progressão, não todos de uma vez. A cada nível subido, ganham-se novos pontos para gastar. A conta real é sequencial no tempo de jogo, não instantânea.

---

## 7. Fluxo do Mestre — Criar Vantagem (passo a passo)

### Pré-condição

O Mestre deve ter criado previamente:
- O Jogo
- As `CategoriaVantagem` que deseja usar (opcional, mas necessário se quiser categorizar)
- Os `AtributoConfig`, `AptidaoConfig`, `BonusConfig`, `MembroCorpoConfig` relevantes (necessários conforme os tipos de efeito que deseja usar)

### Passo 1: Definir metadados da vantagem

O Mestre preenche o formulário principal (`POST /api/v1/configuracoes/vantagens`):

```json
{
  "jogoId": 1,
  "nome": "Treinamento em Combate Ofensivo",
  "sigla": "TCO",
  "descricao": "Treinamento intensivo em técnicas ofensivas de combate corpo a corpo. Aumenta o B.B.A progressivamente.",
  "nivelMaximo": 10,
  "formulaCusto": "custo_base * nivel_vantagem",
  "descricaoEfeito": "+1 B.B.A por nível. A partir do nível 5: D.UP ativo.",
  "categoriaVantagemId": 1,
  "ordemExibicao": 1
}
```

O backend valida:
- Nome único no jogo
- Sigla única cross-entity no jogo (se informada)
- Fórmula de custo válida com variáveis `custo_base` e `nivel_vantagem`

Resposta: `201 Created` com `VantagemResponse` incluindo o `id` gerado.

### Passo 2: Adicionar efeitos (via endpoint de efeitos)

Após criar a vantagem, o Mestre adiciona um ou mais `VantagemEfeito`. Cada efeito é criado separadamente:

**Efeito 1 — BONUS_DERIVADO em B.B.A**:
```json
POST /api/v1/configuracoes/vantagens/{id}/efeitos
{
  "tipoEfeito": "BONUS_DERIVADO",
  "bonusAlvoId": 1,
  "valorPorNivel": 1,
  "descricaoEfeito": "+1 em B.B.A por nível de TCO"
}
```

**Efeito 2 — DADO_UP (D.UP a partir do nível 5)**:
```json
POST /api/v1/configuracoes/vantagens/{id}/efeitos
{
  "tipoEfeito": "DADO_UP",
  "descricaoEfeito": "D.UP ativo a partir do nível 5 de TCO"
}
```

### Passo 3: Adicionar pré-requisitos (opcional)

Se a vantagem exigir condições:

```json
POST /api/v1/configuracoes/vantagens/{id}/prerequisitos
{
  "requisitoId": 42,
  "nivelMinimo": 3
}
```

### Passo 4: Revisar e ajustar

O Mestre pode:
- Atualizar metadados via `PUT /api/v1/configuracoes/vantagens/{id}`
- Reordenar vantagens via `PUT /api/v1/configuracoes/vantagens/reordenar?jogoId=1`
- Soft-deletar via `DELETE /api/v1/configuracoes/vantagens/{id}`

**Atenção**: deletar uma vantagem que já está em fichas de personagens pode gerar inconsistências. O sistema usa soft delete (`deletedAt`), mas o frontend deve avisar o Mestre caso a vantagem esteja em uso antes de deletar.

---

## 8. Fluxo do Jogador — Comprar Vantagem (passo a passo)

### Pré-condição

O Jogador tem uma ficha no sistema com nível >= 1 e pontos de vantagem disponíveis.

### Passo 1: Visualizar vantagens disponíveis

O Jogador acessa a lista de vantagens do jogo:
- `GET /api/v1/configuracoes/vantagens?jogoId=1` — lista todas as vantagens configuradas pelo Mestre

O frontend deve exibir:
- Vantagens organizadas por categoria (com cor da categoria, se configurada)
- Para cada vantagem: nome, sigla, nivelMaximo, custo calculado para o próximo nível, descrição do efeito
- Indicador visual: vantagem já comprada / disponível para compra / bloqueada por pré-requisito
- Filtro por nome e categoria

### Passo 2: Verificar elegibilidade

Antes de mostrar "Comprar", o frontend verifica (dados já disponíveis na ficha):

1. **Pré-requisitos satisfeitos?** — Todos os `VantagemPreRequisito` da vantagem devem estar na `FichaVantagem` com `nivelAtual >= nivelMinimo`
2. **Pode evoluir?** — Se já tem a vantagem, `nivelAtual < nivelMaximo`
3. **Tem pontos suficientes?** — `pontosDisponiveis >= custoDoPróximoNivel`

Se alguma condição não for atendida, o botão de compra fica desabilitado com tooltip explicativo.

### Passo 3: Confirmar a compra

Ao confirmar, o frontend chama o endpoint de compra de vantagem na ficha:

```json
POST /api/v1/fichas/{fichaId}/vantagens
{
  "vantagemConfigId": 15,
  "nivelParaComprar": 1
}
```

O backend:
1. Verifica os pré-requisitos (todos devem estar satisfeitos)
2. Calcula o custo via `formulaCusto` (substitui `custo_base` e `nivel_vantagem`)
3. Verifica se há pontos disponíveis
4. Cria o `FichaVantagem` com `nivelAtual = 1` e `custoPago = custoCalculado`
5. Deduz os pontos gastos do saldo do personagem

### Passo 4: Evoluir vantagem já comprada

Se o personagem já tem a vantagem, pode subir de nível:

```json
PUT /api/v1/fichas/{fichaId}/vantagens/{fichaVantagemId}/evoluir
```

O backend:
1. Verifica `podeSubirNivel()` — `nivelAtual < nivelMaximo`
2. Calcula custo do próximo nível
3. Verifica pontos disponíveis
4. Incrementa `nivelAtual`
5. Soma ao `custoPago` o novo custo

### Passo 5: Visualizar efeitos ativos

Após comprar, o Jogador vê na ficha os efeitos calculados:
- "TCO nível 3: +3 em B.B.A" (BONUS_DERIVADO, valorPorNivel=1, nível=3)
- Os campos calculados da ficha (B.B.A, vida, essência) são atualizados automaticamente

---

## 9. Edge Cases e Validações Críticas (lista completa)

### Validações de entrada (HTTP 400 / 422)

1. **nome em branco**: `@NotBlank` — "Nome é obrigatório"
2. **nome > 100 chars**: `@Size(max=100)` — "Nome deve ter no máximo 100 caracteres"
3. **sigla < 2 chars**: `@Size(min=2)` — "Sigla deve ter entre 2 e 5 caracteres"
4. **sigla > 5 chars**: `@Size(max=5)` — "Sigla deve ter entre 2 e 5 caracteres"
5. **nivelMaximo null**: `@NotNull` — "Nível máximo é obrigatório"
6. **nivelMaximo < 1**: `@Min(1)` — "Nível máximo deve ser no mínimo 1"
7. **formulaCusto em branco**: `@NotBlank` — "Fórmula de custo é obrigatória"
8. **formulaCusto > 100 chars**: `@Size(max=100)` — "Fórmula de custo deve ter no máximo 100 caracteres"
9. **formulaCusto com variáveis inválidas**: `formulaEvaluatorService.validarFormula()` — HTTP 422, lista as variáveis inválidas
10. **formulaCusto com sintaxe inválida**: HTTP 422, detalha o erro de sintaxe exp4j
11. **descricaoEfeito > 500 chars**: `@Size(max=500)` — "Descrição do efeito deve ter no máximo 500 caracteres"
12. **descricao > 1000 chars**: `@Size(max=1000)` — "Descrição deve ter no máximo 1000 caracteres"
13. **jogoId null**: `@NotNull` — "Jogo é obrigatório"

### Validações de conflito (HTTP 409)

14. **nome duplicado no mesmo jogo**: `existsByJogoIdAndNomeIgnoreCase` — "Já existe uma vantagem com o nome '{nome}' neste jogo"
15. **sigla duplicada no mesmo jogo (cross-entity)**: `SiglaValidationService` — "Sigla '{sigla}' já está em uso em {entidade} neste jogo. Siglas devem ser únicas por jogo."
16. **pré-requisito duplicado**: `prerequisitoRepository.existsByVantagemIdAndRequisitoId` — "Este pré-requisito já está registrado para esta vantagem."
17. **pré-requisito auto-referência**: `vantagemId.equals(requisitoId)` — "Uma vantagem não pode ser pré-requisito de si mesma."
18. **pré-requisito cria ciclo**: algoritmo DFS — "Adicionar este pré-requisito criaria uma dependência circular entre vantagens."
19. **pré-requisito de jogos diferentes**: `vantagem.getJogo().getId().equals(requisito.getJogo().getId())` — "O pré-requisito deve pertencer ao mesmo jogo da vantagem."

### Edge cases de negócio

20. **Vantagem com nivelMaximo=1**: é uma vantagem "one-shot" (ex: "Visão no Escuro"). `podeSubirNivel()` retorna `false` imediatamente após a primeira compra. O frontend não deve exibir opção de evolução.
21. **formulaCusto = "custo_base"**: custo flat, não cresce com o nível. O campo `custo_base` não existe no banco — é uma variável calculada? Sim, `custo_base` é um valor literal que o Mestre define na fórmula. Na prática, o Mestre digita `"3"` como fórmula para custo fixo de 3, ou usa `custo_base` como variável cujo valor é passado ao avaliar (implementação depende do frontend).
22. **Vantagem sem efeitos**: tecnicamente válida no backend. O Mestre pode criar uma vantagem "narrativa" sem efeitos mecânicos. O frontend deve mostrar aviso mas não bloquear.
23. **Múltiplos efeitos em uma vantagem**: uma vantagem pode ter vários `VantagemEfeito` (ex: "+2 FOR e +1 BBA ao mesmo tempo"). O frontend deve exibir todos e permitir adicionar/remover individualmente.
24. **Efeito com valorFixo e valorPorNivel ambos nulos**: tecnicamente válido no banco (ex: DADO_UP não usa valores numéricos). Para outros tipos, o frontend deve validar que ao menos um dos dois está preenchido.
25. **Soft delete de vantagem em uso**: o soft delete (`deletedAt`) oculta a vantagem do `@SQLRestriction("deleted_at IS NULL")`. Fichas que já possuem essa vantagem terão `FichaVantagem` com FK para uma entidade soft-deletada — o motor de cálculo deve lidar com isso graciosamente (ignorar efeitos de vantagens deletadas).
26. **Update de sigla para valor em uso**: ao atualizar, só valida unicidade da nova sigla se ela mudou (`!siglaNova.equalsIgnoreCase(siglaExistente)`). Caso contrário, não valida (evitar que o update da própria vantagem falhe por causa da sigla dela mesma).
27. **Pré-requisito com nivelMinimo > nivelMaximo do requisito**: o backend não valida isso explicitamente (o min é apenas >= 1). O frontend deve validar: `nivelMinimo <= nivelMaximo da vantagem-requisito`.
28. **FichaVantagem com nivelAtual = nivelMaximo**: `podeSubirNivel()` retorna `false`. O botão de evolução não deve aparecer. O personagem não pode ultrapassar o teto.
29. **Vantagem deletada que ainda é pré-requisito de outra**: soft-delete não verifica dependências. O sistema deve, no mínimo, avisar. Idealmente, bloquear soft-delete de vantagens que são pré-requisito de outras vantagens ativas.
30. **formulaCusto com divisão por zero**: se a fórmula for `"nivel_vantagem / (nivel_vantagem - 1)"`, para nível 1 ocorre divisão por zero. O `FormulaEvaluatorService` deve tratar esse caso. O frontend deve testar fórmulas antes de salvar (botão "Validar").

---

## 10. Wireframe Textual — Tela de Config de Vantagens

### 10.1 Listagem de Vantagens

```
+===========================================================================+
| VANTAGENS DO JOGO                        [MESTRE: Filtros] [+ Nova Vantagem] |
+===========================================================================+

Buscar: [________________________] [Categoria: Todas v]

TREINAMENTO FISICO  (cor: #E74C3C) ─────────────────────────────────────────
  #  | Nome                             | Sigla | Níveis | Fórmula custo | Ações
 ────+──────────────────────────────────+───────+────────+───────────────+─────
  1  | Treinamento em Combate Ofensivo  | TCO   | 1-10   | custo_base*nv | ✏ 🗑
  2  | Treinamento em Combate Defensivo | TCD   | 1-10   | custo_base*nv | ✏ 🗑
  3  | Treinamento em Combate Evasivo   | TCE   | 1-10   | custo_base*nv | ✏ 🗑

VANTAGEM GERAL  (cor: #27AE60) ─────────────────────────────────────────────
  4  | Saúde de Ferro                   | —     | 1-10   | custo_base*nv | ✏ 🗑
  5  | Ambidestria                      | —     | 1-1    | custo_base    | ✏ 🗑

(SEM CATEGORIA) ─────────────────────────────────────────────────────────────
  6  | Visão no Escuro                  | —     | 1-1    | custo_base    | ✏ 🗑

[Reordenar vantagens]

Total: 6 vantagens configuradas
```

**Notas de UX**:
- Cada categoria tem cor configurável (campo `cor` em `CategoriaVantagem`)
- "Reordenar" abre drag-and-drop ou campos de ordem numérica por categoria
- Colunas ordenáveis por nome e ordem
- Paginação ou scroll infinito para jogos com muitas vantagens
- Badge de quantos personagens já possuem a vantagem (informativo)

---

### 10.2 Drawer de Criação/Edição de Vantagem

```
╔═════════════════════════════════════════════════════╗
║  NOVA VANTAGEM                              [✕]     ║
╠═════════════════════════════════════════════════════╣
║                                                     ║
║  IDENTIFICAÇÃO                                      ║
║  ┌─────────────────────────────────────────────┐   ║
║  │ Nome *                                      │   ║
║  │ [Treinamento em Combate Ofensivo           ] │   ║
║  └─────────────────────────────────────────────┘   ║
║  ┌──────────────────────┐  ┌─────────────────────┐ ║
║  │ Sigla (2-5 chars)    │  │ Categoria            │ ║
║  │ [TCO               ] │  │ [Treinamento Físico v]│ ║
║  └──────────────────────┘  └─────────────────────┘ ║
║  ┌─────────────────────────────────────────────┐   ║
║  │ Descrição                                   │   ║
║  │ [Treinamento intensivo em técnicas...      ] │   ║
║  │                                             │   ║
║  └─────────────────────────────────────────────┘   ║
║                                                     ║
║  CUSTOS E NIVEIS                                    ║
║  ┌───────────────────┐  ┌──────────────────────┐   ║
║  │ Nível máximo *    │  │ Ordem de exibição    │   ║
║  │ [10             ] │  │ [1                 ] │   ║
║  └───────────────────┘  └──────────────────────┘   ║
║  ┌─────────────────────────────────────────────┐   ║
║  │ Fórmula de custo * [Validar fórmula]        │   ║
║  │ [custo_base * nivel_vantagem               ] │   ║
║  │ Variáveis: custo_base, nivel_vantagem       │   ║
║  │ Preview: nv1=?  nv2=?  nv5=?  nv10=?       │   ║
║  └─────────────────────────────────────────────┘   ║
║  ┌─────────────────────────────────────────────┐   ║
║  │ Descrição do efeito (texto livre para       │   ║
║  │ exibição na ficha do jogador)               │   ║
║  │ [+1 B.B.A por nível                       ] │   ║
║  └─────────────────────────────────────────────┘   ║
║                                                     ║
║  ────────────────────────────────────────────────  ║
║           [Cancelar]    [Salvar Vantagem]           ║
╚═════════════════════════════════════════════════════╝
```

---

### 10.3 Seção de Efeitos (dentro do detalhe da vantagem, após criação)

```
EFEITOS  [ + Adicionar efeito ]
+─────────────────────────────────────────────────────+
| Tipo             | Alvo       | Fixo | Por Nível | ✕ |
|──────────────────+────────────+──────+───────────+───|
| BONUS_DERIVADO   | B.B.A      | —    | +1        | ✕ |
| DADO_UP          | —          | —    | —         | ✕ |
+─────────────────────────────────────────────────────+
```

**Modal de adição de efeito — dinâmico por tipo selecionado**:

```
Tipo de efeito *: [ BONUS_DERIVADO                    v ]

  ↓ Quando BONUS_ATRIBUTO:
    Atributo alvo *: [ Força                          v ]
    Valor fixo:      [   ]   Valor por nível: [   ]

  ↓ Quando BONUS_APTIDAO:
    Aptidão alvo *: [ Furtividade                     v ]
    Valor fixo:     [   ]   Valor por nível: [   ]

  ↓ Quando BONUS_DERIVADO:
    Bônus alvo *: [ B.B.A                             v ]
    Valor fixo:   [   ]   Valor por nível: [ 1 ]

  ↓ Quando BONUS_VIDA:
    (sem alvo)
    Valor fixo:   [   ]   Valor por nível: [ 5 ]
    Label: "Bônus em Vida Total (VT)"

  ↓ Quando BONUS_VIDA_MEMBRO:
    Membro alvo *: [ Cabeça                           v ]
    Valor fixo:    [   ]   Valor por nível: [   ]

  ↓ Quando BONUS_ESSENCIA:
    (sem alvo)
    Valor fixo:   [   ]   Valor por nível: [ 10 ]
    Label: "Bônus em Essência Total"

  ↓ Quando DADO_UP:
    (sem alvo, sem valores numéricos)
    Informativo: "Evolui o dado: d3→d4→d6→d8→d10→d12"

  ↓ Quando FORMULA_CUSTOMIZADA:
    (sem alvo)
    Fórmula *: [ FLOOR(nivel_vantagem * FOR / 3) ] [Validar]
    Variáveis: [nivel_vantagem] [FOR] [AGI] [VIG] [SAB] [INT] [INTU] [AST]

Descrição do efeito: [texto livre para o jogador ver na ficha       ]

Preview — nível 1: ?   nível 3: ?   nível 5: ?   nível 10: ?

                [Cancelar]   [Adicionar Efeito]
```

---

### 10.4 Seção de Pré-Requisitos (dentro do detalhe da vantagem)

```
PRE-REQUISITOS  [ + Adicionar pré-requisito ]
+──────────────────────────────────────┬──────────────┬────+
| Vantagem exigida                     | Nível mínimo | ✕  |
|──────────────────────────────────────+──────────────+────|
| TCO (Treinamento em Combate Ofensivo)| 5            | ✕  |
+──────────────────────────────────────┴──────────────┴────+

  ← Sem pré-requisitos configurados
```

**Atenção ao remover**: pré-requisito é removido com hard-delete (não soft-delete).

---

## 11. Critérios de Aceite (checklist para desenvolvedores e QA)

### Configuração — MESTRE

- [ ] MESTRE cria vantagem com todos os campos obrigatórios → HTTP 201 + VantagemResponse
- [ ] MESTRE cria vantagem com nome duplicado no jogo → HTTP 409 com mensagem clara
- [ ] MESTRE cria vantagem com sigla duplicada na mesma entidade → HTTP 409
- [ ] MESTRE cria vantagem com sigla usada por atributo do mesmo jogo → HTTP 409
- [ ] MESTRE cria vantagem com sigla usada por BonusConfig do mesmo jogo → HTTP 409
- [ ] MESTRE cria vantagem com sigla em jogo diferente → HTTP 201 (siglas são por jogo)
- [ ] MESTRE cria vantagem com formulaCusto válida → salva e valida via exp4j
- [ ] MESTRE cria vantagem com formulaCusto usando variável inválida → HTTP 422
- [ ] MESTRE cria vantagem com formulaCusto com sintaxe inválida → HTTP 422
- [ ] MESTRE cria vantagem com nivelMaximo=1 → vantagem one-shot criada
- [ ] MESTRE atualiza vantagem sem mudar sigla → não re-valida sigla
- [ ] MESTRE atualiza vantagem com nova sigla disponível → HTTP 200
- [ ] MESTRE atualiza vantagem com nova sigla em uso → HTTP 409
- [ ] MESTRE soft-deleta vantagem → aparece com `deletedAt` preenchido
- [ ] MESTRE soft-deleta vantagem → não aparece mais na listagem
- [ ] MESTRE reordena vantagens via batch → ordemExibicao atualizada em todas
- [ ] JOGADOR tenta criar vantagem → HTTP 403

### Efeitos

- [ ] MESTRE adiciona efeito BONUS_ATRIBUTO com atributoAlvo → criado
- [ ] MESTRE adiciona efeito BONUS_APTIDAO com aptidaoAlvo → criado
- [ ] MESTRE adiciona efeito BONUS_DERIVADO com bonusAlvo → criado
- [ ] MESTRE adiciona efeito BONUS_VIDA sem alvo → criado
- [ ] MESTRE adiciona efeito BONUS_VIDA_MEMBRO com membroAlvo → criado
- [ ] MESTRE adiciona efeito BONUS_ESSENCIA sem alvo → criado
- [ ] MESTRE adiciona efeito DADO_UP sem alvo e sem valores → criado
- [ ] MESTRE adiciona efeito FORMULA_CUSTOMIZADA com fórmula válida → criado
- [ ] MESTRE adiciona efeito FORMULA_CUSTOMIZADA com fórmula inválida → HTTP 422
- [ ] Vantagem pode ter múltiplos efeitos → todos retornados no VantagemResponse
- [ ] Efeito deletado → vantagem atualizada sem o efeito (orphanRemoval)

### Pré-Requisitos

- [ ] MESTRE adiciona pré-requisito de outra vantagem do mesmo jogo → HTTP 201
- [ ] MESTRE adiciona pré-requisito duplicado → HTTP 409
- [ ] MESTRE adiciona pré-requisito auto-referência → HTTP 409
- [ ] MESTRE adiciona pré-requisito que cria ciclo direto (A exige B, B exige A) → HTTP 409
- [ ] MESTRE adiciona pré-requisito que cria ciclo indireto (A→B→C, C→A) → HTTP 409
- [ ] MESTRE adiciona pré-requisito de vantagem de outro jogo → HTTP 422
- [ ] MESTRE remove pré-requisito → HTTP 204, pré-requisito não existe mais
- [ ] Listagem de vantagem por ID inclui preRequisitos populados

### Ficha (compra de vantagem)

- [ ] JOGADOR compra vantagem sem pré-requisitos → FichaVantagem criada, pontos deduzidos
- [ ] JOGADOR tenta comprar vantagem sem pré-requisitos satisfeitos → HTTP 422
- [ ] JOGADOR tenta comprar vantagem sem pontos suficientes → HTTP 422
- [ ] JOGADOR compra vantagem one-shot (nivelMaximo=1) → nível fica em 1
- [ ] JOGADOR tenta evoluir vantagem one-shot → HTTP 422
- [ ] JOGADOR evolui vantagem nível 2→3 → nivelAtual=3, custoPago acumulado
- [ ] JOGADOR tenta deletar FichaVantagem → HTTP 405 / 404 (endpoint não existe)
- [ ] Ficha com vantagem deletada (soft-delete) → sistema não quebra

### Segurança

- [ ] Endpoints de escrita sem autenticação → HTTP 401
- [ ] Endpoints de escrita com role JOGADOR → HTTP 403
- [ ] Endpoints de leitura com role JOGADOR → HTTP 200
- [ ] Acesso a vantagem de jogo que o usuário não participa → HTTP 403

### Integração

- [ ] Buscar vantagem por ID retorna preRequisitos e efeitos populados (sem N+1)
- [ ] Listagem de vantagens não dispara N+1 queries
- [ ] Criar jogo novo → vantagens default criadas pelo `GameConfigInitializerService`
- [ ] Filtro por nome funciona case-insensitive
- [ ] Fórmula `custo_base * nivel_vantagem` com custo_base=2: nível 5 custa 10 pontos

---

## 12. Dependências com Outras Configs (o que precisa existir antes)

### Dependências obrigatórias

| Config necessária | Por que | Quando |
|---|---|---|
| `Jogo` | VantagemConfig pertence a um Jogo | Sempre |

### Dependências condicionais (por tipo de efeito)

| Efeito | Config necessária | Por que |
|---|---|---|
| `BONUS_ATRIBUTO` | `AtributoConfig` | FK `atributoAlvo` |
| `BONUS_APTIDAO` | `AptidaoConfig` | FK `aptidaoAlvo` |
| `BONUS_DERIVADO` | `BonusConfig` | FK `bonusAlvo` |
| `BONUS_VIDA_MEMBRO` | `MembroCorpoConfig` | FK `membroAlvo` |
| `BONUS_VIDA` | nenhuma | sem FK de alvo |
| `BONUS_ESSENCIA` | nenhuma | sem FK de alvo |
| `DADO_UP` | nenhuma | sem FK de alvo |
| `FORMULA_CUSTOMIZADA` | nenhuma (mas usa siglas) | variáveis referenciam abreviações de atributos |

### Dependências opcionais

| Config | Por que | Impacto se ausente |
|---|---|---|
| `CategoriaVantagem` | categorização organizacional | Vantagem fica sem categoria (aceito) |
| `PontosVantagemConfig` | pontosGanhos por nível | Sistema sem configuração de pontos (bloqueante para ficha) |

### Ordem recomendada de criação ao configurar um jogo do zero

```
1. Jogo
2. AtributoConfig (7 atributos)
3. TipoAptidao (2 tipos: FISICA e MENTAL)
4. AptidaoConfig (24 aptidões)
5. BonusConfig (6 bônus derivados)
6. MembroCorpoConfig (7 membros)
7. NivelConfig (36 níveis)
8. PontosVantagemConfig (1 por nível, padrão: 1 ponto cada)
9. CategoriaVantagem (8 categorias — opcional mas recomendado)
10. VantagemConfig (N vantagens)
    10a. VantagemEfeito (por vantagem)
    10b. VantagemPreRequisito (por vantagem, quando aplicável)
```

**Nota**: o `GameConfigInitializerService` executa esse fluxo automaticamente ao criar um novo jogo, gerando todas as configs padrão do template Klayrah transacionalmente. Se qualquer etapa falhar, toda a criação é revertida.

---

*Documento gerado em: Março 2026*
*Baseado em: `VantagemConfig`, `VantagemEfeito`, `VantagemPreRequisito`, `FichaVantagem`, `PontosVantagemConfig`, `TipoEfeito`, `VantagemConfiguracaoService`, `GameDefaultConfigProvider`*
