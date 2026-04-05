# Auditoria: DefaultGameConfigProviderImpl + GameConfigInitializerService

> Data: 2026-04-04 | Branch: feature/009-npc-fichas-mestre
> Arquivos auditados:
> - `src/.../config/DefaultGameConfigProviderImpl.java`
> - `src/.../config/GameDefaultConfigProvider.java` (interface)
> - `src/.../service/GameConfigInitializerService.java`
>
> **Corrigir APÓS conclusão das outras specs (007, 006, 005).**

---

## 1. Tabela de Cobertura

| Config | Na interface? | No provider? | No initializer? | Problema |
|--------|:---:|:---:|:---:|---------|
| AtributoConfig | ✅ | ✅ | ✅ | OK |
| TipoAptidao | ❌ | ❌ | ✅ hardcoded | FISICA/MENTAL criados direto no service — não configurável pelo provider |
| AptidaoConfig | ✅ | ✅ | ✅ | OK |
| NivelConfig | ✅ | ✅ | ✅ (com bugs) | limitadorAtributo ignorado; ver bugs |
| **BonusConfig** | ❌ | ❌ | ❌ | **AUSENTE — nenhum jogo novo tem B.B.A, B.B.M, Esquiva etc.** |
| ClassePersonagem | ✅ | ✅ | ✅ | OK — mas sem ClasseBonus e ClasseAptidaoBonus |
| **ClasseBonus** | ❌ | ❌ | ❌ | **AUSENTE — Guerreiro criado sem nenhum bônus de B.B.A por nível** |
| **ClasseAptidaoBonus** | ❌ | ❌ | ❌ | **AUSENTE — Ladrão criado sem bônus em Furtividade** |
| Raca | ✅ | ✅ | ✅ | OK |
| RacaBonusAtributo | ✅ (via getDefaultBonusRaciais) | ✅ | ✅ | OK |
| RacaClassePermitida | ❌ | ❌ | ❌ | Ausente — nenhuma restrição raça/classe configurada |
| DadoProspeccaoConfig | ✅ | ✅ | ✅ | OK |
| GeneroConfig | ✅ | ✅ | ✅ | OK |
| IndoleConfig | ✅ | ✅ | ✅ | OK |
| PresencaConfig | ✅ | ✅ | ✅ | OK |
| MembroCorpoConfig | ✅ | ✅ | ✅ | OK |
| VantagemConfig | ✅ | ✅ | ✅ (com perda) | Campos do DTO ignorados; ver bugs |
| **CategoriaVantagem** | ❌ | ❌ | ❌ | **AUSENTE — todas as vantagens criadas sem categoria** |
| **PontosVantagemConfig** | ❌ | ❌ | ❌ | **AUSENTE — pontos de vantagem por nível = 0 para todo jogo novo** |
| VantagemEfeito | ❌ | ❌ | ❌ | Aguarda Spec 007 — ver seção 5 |
| LimitadorConfig | ✅ | ✅ | ❌ comentado | `createLimitadores` comentado nas linhas 98-101 do service |

---

## 2. Bugs Identificados

### BUG-DC-06 — Cabeça: 25% → **75%** da vida total (CRÍTICO)

**Localização:** `DefaultGameConfigProviderImpl.java`, linha 277
```java
// ERRADO:
MembroCorpoConfigDTO.of("Cabeça", new BigDecimal("0.25"), 1)
// CORRETO (decisão PO 2026-04-04):
MembroCorpoConfigDTO.of("Cabeça", new BigDecimal("0.75"), 1)
```
Bloqueia Spec 006 — golpes na Cabeça precisam ser 3× mais letais do que o código atual implementa.

---

### BUG-DC-07 — Índole: 9 alinhamentos → **3 valores simples** (decisão PO 2026-04-04)

**Localização:** `DefaultGameConfigProviderImpl.java`, linhas 249-258
```java
// REMOVER os 9 alinhamentos D&D. Substituir por:
IndoleConfigDTO.of("Bom",   1)
IndoleConfigDTO.of("Mau",   2)
IndoleConfigDTO.of("Neutro",3)
```

---

### BUG-DC-08 — Presença: escala de intensidade → **postura ética** (decisão PO 2026-04-04)

**Localização:** `DefaultGameConfigProviderImpl.java`, linhas 263-270
```java
// REMOVER Insignificante/Fraco/Normal/Notável/Impressionante/Dominante. Substituir por:
PresencaConfigDTO.of("Bom",    1)
PresencaConfigDTO.of("Leal",   2)
PresencaConfigDTO.of("Caótico",3)
PresencaConfigDTO.of("Neutro", 4)
```

---

### BUG-DC-09 — Gênero: 4 valores → **3 valores** (decisão PO 2026-04-04)

**Localização:** `DefaultGameConfigProviderImpl.java`, linhas 240-244
```java
// REMOVER "Não-Binário" e "Prefiro não informar". Manter:
GeneroConfigDTO.of("Masculino", 1)
GeneroConfigDTO.of("Feminino",  2)
GeneroConfigDTO.of("Outro",     3)
```

---

### ~~BUG-DC-01~~ — Nível 35 com XP igual ao nível 34 — **INTENCIONAL**
Decisão do PO (2026-04-04): nível 35 é o teto máximo. XP 595.000 = igual ao nível 34 por design.
Adicionar comentário no código para deixar explícito:
```java
NivelConfigDTO.of(35, 595000L, 3, 1, 3)  // Nível máximo — XP = teto, não cresce mais
```

---

### BUG-DC-02 — `limitadorAtributo` do DTO completamente ignorado
**Localização:** `GameConfigInitializerService.java`, linha 235
```java
nivel.setLimitadorAtributo(50); // Default limitador (will be configurable later)
```
O 5º parâmetro de `NivelConfigDTO.of(...)` é ignorado. Todos os 36 níveis ficam com limitador = 50.

---

### BUG-DC-03 — Limitadores comentados no initializer
**Localização:** `GameConfigInitializerService.java`, linhas 98-101
```java
// log.debug("Criando limitadores...");
// List<LimitadorConfig> limitadores = createLimitadores(jogo, defaultProvider.getDefaultLimitadores());
```
`getDefaultLimitadores()` retorna 5 faixas no provider mas nunca é chamado.

---

### BUG-DC-04 — Campos do `VantagemConfigDTO` silenciosamente descartados
**Localização:** `GameConfigInitializerService.java`, método `createVantagens()` (linha 424)

| Campo do DTO | Mapeamento atual | Status |
|---|---|---|
| `valorBonusFormula` | ❌ descartado | Fórmula do efeito perdida |
| `custoBase` | ❌ descartado | Não persiste |
| `nivelMinimoPersonagem` | ❌ descartado | Não persiste |
| `podeEvoluir` | ❌ descartado | Não persiste |
| `tipoBonus` | Mapeado para `descricaoEfeito` | Lossy — string livre em vez de enum |

**Causa:** `VantagemConfig` atual não tem esses campos. Pertencem ao modelo de `VantagemEfeito` (Spec 007). O DTO foi escrito antecipando Spec 007.

---

### BUG-DC-05 — TipoAptidao hardcoded no service (não configurável via provider)
**Localização:** `GameConfigInitializerService.java`, método `createTiposAptidao()` (linha 178)

FISICA e MENTAL criados diretamente no service. Mestre não consegue customizar tipos de aptidão padrão via implementação de `GameDefaultConfigProvider`.

---

## 3. O Que Precisa Ser Adicionado

### PRIORIDADE ALTA — Impactam cálculos de ficha

#### 3.1 BonusConfig defaults
Sem BonusConfig, todo jogo novo começa sem B.B.A, B.B.M e derivados. `FichaCalculationService` não tem nada para calcular nesses campos.

Adicionar à interface: `List<BonusConfigDTO> getDefaultBonus()`

Fórmulas confirmadas pelo PO (Q-DC-01, Q-DC-11 — 2026-04-04):
```java
BonusConfigDTO.of("B.B.A",        "BBA",  "(FOR + AGI) / 3",  1)  // glossário 03-termos-dominio.md linha 25
BonusConfigDTO.of("B.B.M",        "BBM",  "(SAB + INT) / 3",  2)  // glossário 03-termos-dominio.md linha 26
BonusConfigDTO.of("Defesa",       "DEF",  "VIG / 5",          3)  // Q-DC-01
BonusConfigDTO.of("Esquiva",      "ESQ",  "AGI / 5",          4)  // Q-DC-01
BonusConfigDTO.of("Iniciativa",   "INI",  "INTU / 5",         5)  // Q-DC-01
BonusConfigDTO.of("Percepção",    "PER",  "INTU / 3",         6)  // glossário 02-configuracoes-jogo.md: "6 bônus"
BonusConfigDTO.of("Raciocínio",   "RAC",  "INT / 3",          7)  // glossário 02-configuracoes-jogo.md: "6 bônus"
BonusConfigDTO.of("Bloqueio",     "BLO",  "VIG / 3",          8)  // glossário 02-configuracoes-jogo.md: "6 bônus"
BonusConfigDTO.of("Reflexo",      "REF",  "AGI / 3",          9)  // glossário 02-configuracoes-jogo.md: "6 bônus"
```
> **Nota:** Glossário menciona "6 bônus calculados no template" — B.B.A, Bloqueio, Reflexo, B.B.M, Percepção, Raciocínio. Fórmulas de Bloqueio/Reflexo/Percepção/Raciocínio não detalhadas no glossário — usando derivações por atributo relevante. **Confirmar com PO antes de implementar.**

---

#### 3.2 PontosVantagemConfig defaults
Sem isso, `pontosVantagemDisponiveis = 0` para todos os jogadores de jogos novos.

Adicionar à interface: `List<PontosVantagemConfigDTO> getDefaultPontosVantagem()`

**Tabela confirmada pelo PO (2026-04-04):**
```java
PontosVantagemConfigDTO.of(1,  6)   // Nível 1: 6 pontos
PontosVantagemConfigDTO.of(5,  3)   // A cada 5 níveis: +3
PontosVantagemConfigDTO.of(10, 10)  // Marco nível 10: +10
PontosVantagemConfigDTO.of(15, 3)
PontosVantagemConfigDTO.of(20, 10)  // Marco nível 20: +10
PontosVantagemConfigDTO.of(25, 3)
PontosVantagemConfigDTO.of(30, 15)  // Marco nível 30: +15
PontosVantagemConfigDTO.of(35, 3)
```
Mestre pode editar livremente após criação do jogo.

---

#### 3.3 ClasseBonus — DECISÃO DO PO (2026-04-04)
**Classes NÃO dão bônus diretos em BonusConfig.** Classes dão:
1. **Pontos extras de atributo** — distribuíveis pelo jogador (mecanismo = GAP-PONTOS-CONFIG)
2. **Vantagens pré-definidas** — FichaVantagem criadas automaticamente ao escolher a classe

**Impacto em GAP-CALC-01:** `ClasseBonus.valorPorNivel` aplicado ao `FichaBonus.classe` só faz sentido se o Mestre configurar manualmente na tela de classes. No default provider, `getDefaultClasseBonus()` deve retornar mapa vazio (sem bônus por padrão).

**Implicação para Spec 007 T0:** Corrigir o serviço para buscar `ClasseBonus` e aplicar quando existir, mas não esperar que exista por padrão.

---

#### 3.4 CategoriaVantagem defaults
Adicionar à interface: `List<CategoriaVantagemDTO> getDefaultCategoriasVantagem()`

**8 categorias canônicas do Klayrah** (glossário `02-configuracoes-jogo.md`, linhas 204-212 — confirmado como padrão):
```java
CategoriaVantagemDTO.of("Treinamento Físico",      "#e74c3c", 1)  // TCO, TCD, TCE
CategoriaVantagemDTO.of("Treinamento Mental",       "#8e44ad", 2)  // TM, TPM, TL, T.M
CategoriaVantagemDTO.of("Ação",                     "#e67e22", 3)  // Ataque Adicional, Ataque Sentai
CategoriaVantagemDTO.of("Reação",                   "#27ae60", 4)  // Contra-Ataque, Intercepção
CategoriaVantagemDTO.of("Vantagem de Atributo",     "#2980b9", 5)  // CFM, DM, DF, DV, SG, IN
CategoriaVantagemDTO.of("Vantagem Geral",           "#95a5a6", 6)  // Saúde de Ferro, Ambidestria
CategoriaVantagemDTO.of("Vantagem Histórica",       "#f39c12", 7)  // Riqueza, Capangas
CategoriaVantagemDTO.of("Vantagem de Renascimento", "#1abc9c", 8)  // Último Sigilo, Pensamento Bifurcado
```

---

### PRIORIDADE MÉDIA

#### 3.5 ClasseAptidaoBonus defaults
Ladrão → bônus em Furtividade e Prestidigitação. Confirmar tabela completa com PO.

#### 3.6 TipoAptidao via provider
Mover FISICA/MENTAL para `getDefaultTiposAptidao()` e chamar provider no initializer.

#### 3.7 RacaClassePermitida defaults
Depende de decisão do PO (Q-DC-05) — o sistema Klayrah é restritivo por raça?

---

## 4. Correções de Bugs (código)

```java
// BUG-DC-01: Nível 35 XP
NivelConfigDTO.of(35, 630000L, 3, 1, 3)  // confirmar valor com PO

// BUG-DC-02: Usar limitador do DTO
nivel.setLimitadorAtributo(dto.getLimitadorAtributo());  // não hardcoded

// BUG-DC-03: Descomentar limitadores
log.debug("Criando limitadores...");
createLimitadores(jogo, defaultProvider.getDefaultLimitadores());
// + implementar createLimitadores() no service
```

---

## 5. Pós-Spec 007 — VantagemEfeito no initializer

Quando Spec 007 for implementada:

1. Novo DTO: `VantagemEfeitoDefaultDTO(tipoEfeito, alvo, valor, formula)`
2. Nova interface: `Map<String, List<VantagemEfeitoDefaultDTO>> getDefaultVantagemEfeitos()` (chave = nome da vantagem)
3. Novo método no service: `createVantagemEfeitos(Map<String, VantagemConfig>, provider)`
4. Atualizar `createVantagens()`: remover mapeamento `tipoBonus → descricaoEfeito`

Exemplo de mapeamento pós-Spec 007:
```
"Fortitude"        → TipoEfeito.BONUS_ATRIBUTO, alvo="VIG", formula="nivel_vantagem * 2"
"Ataque Aprimorado"→ TipoEfeito.BONUS_BBA_BBM,  alvo="BBA", formula="nivel_vantagem * 1"
"Vida Extra"       → TipoEfeito.BONUS_VIDA,      alvo=null,  formula="nivel_vantagem * 5"
"Essência Ampliada"→ TipoEfeito.BONUS_ESSENCIA,  alvo=null,  formula="nivel_vantagem * 10"
```

---

## 6. Ordem de Inicialização Correta (com adições)

```
1.  AtributoConfig          (independente)
2.  TipoAptidao             (independente) ← mover para provider
3.  AptidaoConfig           (depende de TipoAptidao)
4.  BonusConfig             (independente) ← ADICIONAR
5.  NivelConfig             (independente)
6.  PontosVantagemConfig    (independente) ← ADICIONAR
7.  CategoriaVantagem       (independente) ← ADICIONAR
8.  ClassePersonagem        (independente)
9.  ClasseBonus             (depende de ClassePersonagem + BonusConfig) ← ADICIONAR
10. ClasseAptidaoBonus      (depende de ClassePersonagem + AptidaoConfig) ← ADICIONAR
11. Raca                    (independente)
12. RacaBonusAtributo       (depende de Raca + AtributoConfig) ✅ já existe
13. RacaClassePermitida     (depende de Raca + ClassePersonagem) ← ADICIONAR se necessário
14. DadoProspeccaoConfig    (independente)
15. GeneroConfig            (independente)
16. IndoleConfig            (independente)
17. PresencaConfig          (independente)
18. MembroCorpoConfig       (independente)
19. VantagemConfig          (depende de CategoriaVantagem)
20. VantagemEfeito          (depende de VantagemConfig) ← PÓS-SPEC 007
```

---

## 7. Perguntas para o PO (adicionar ao PERGUNTAS-PENDENTES-PO.md)

| # | Pergunta | Impacto |
|---|---------|---------|
| Q-DC-01 | Quais BonusConfig padrão e suas fórmulas? (B.B.A = `nivel/5`? Defesa = `VIG/5`?) | BonusConfig defaults |
| Q-DC-02 | Tabela de marcos de pontos de vantagem por nível? | PontosVantagemConfig defaults |
| Q-DC-03 | Quais ClasseBonus por classe? Guerreiro +1 BBA/nível? | ClasseBonus defaults |
| Q-DC-04 | Nível 35 XP = nível 34 foi intencional ou typo? | BUG-DC-01 |
| Q-DC-05 | RacaClassePermitida é restritivo no Klayrah? (ex: Elfo não pode ser Berserker?) | RacaClassePermitida |

---

*Produzido por: análise direta do código-fonte | 2026-04-04*
*Corrigir após: Spec 007, 006, 005 implementadas*

---

## 8. Coerência com o Glossário

> Revisão realizada em 2026-04-03.
> Fontes consultadas: `docs/glossario/01-contexto-geral.md`, `02-configuracoes-jogo.md`, `03-termos-dominio.md`, `04-siglas-formulas.md`.
> Arquivo auditado: `src/main/java/.../config/DefaultGameConfigProviderImpl.java`.

---

### 8.1 Conformidades — o que está correto

| Item | Detalhe |
|------|---------|
| **Atributos (7) — nomes** | Força, Agilidade, Vigor, Sabedoria, Intuição, Inteligência, Astúcia correspondem exatamente ao glossário (`02-configuracoes-jogo.md`, linha 16). |
| **Atributos (7) — siglas** | FOR, AGI, VIG, SAB, INTU, INT, AST estão idênticas à tabela de siglas padrão (`04-siglas-formulas.md`, linhas 44-51). |
| **Ímpeto de Força** | `total * 3` (kg) — correto. Glossário `03-termos-dominio.md` linha 16: "Ímpeto de Força = Total × 3 (capacidade de carga em kg)". |
| **Ímpeto de Vigor** | `total / 10` com unidade "RD" — correto. Glossário `03-termos-dominio.md` linha 42: "RD = Redução de Dano (físico) — Ímpeto do Vigor". |
| **Ímpeto de Sabedoria** | `total / 10` com unidade "RDM" — correto. Glossário `03-termos-dominio.md` linha 43: "RDM = Redução de Dano Mágico — Ímpeto da Sabedoria". |
| **Aptidões físicas (12)** | Acrobacia, Guarda, Aparar, Atletismo, Resvalar, Resistência, Perseguição, Natação, Furtividade, Prestidigitação, Conduzir, Arte da Fuga — lista idêntica ao glossário (`03-termos-dominio.md`, linha 124). |
| **Aptidões mentais (12)** | Idiomas, Observação, Falsificar, Prontidão, Auto Controle, Sentir Motivação, Sobrevivência, Investigar, Blefar, Atuação, Diplomacia, Operação de Mecanismos — lista idêntica ao glossário (`03-termos-dominio.md`, linha 125). |
| **Tipos de aptidão** | FISICA e MENTAL — corretos (`02-configuracoes-jogo.md`, linha 65: "2 tipos — Física e Mental"). |
| **Raças (4) — nomes** | Humano, Elfo, Anão, Meio-Elfo — corretos (`02-configuracoes-jogo.md`, seção Raça). |
| **Bônus raciais Elfo** | +2 AGI, -1 VIG — corretos. Glossário `02-configuracoes-jogo.md` linha 104: "Elfo pode ter +2 em Agilidade mas -1 em Vigor". |
| **Bônus raciais Anão** | +2 VIG, -1 AGI — corretos. Glossário `02-configuracoes-jogo.md` linha 104: "Anão pode ter +2 em Vigor mas -1 em Agilidade". |
| **Humano sem bônus** | Correto (`DefaultGameConfigProviderImpl.java`, linha 205: `"Humano", List.of()`). O glossário descreve Humano como "versátil e adaptável", sem bônus numéricos. |
| **Dados de prospecção (6)** | d3, d4, d6, d8, d10, d12 — corretos. Glossário `02-configuracoes-jogo.md` linha 139: "6 tipos de dados (d3 a d12)". |
| **Classes (11 de 12)** | Guerreiro, Arqueiro, Monge, Berserker, Assassino, Fauno (Herdeiro), Mago, Feiticeiro, Sacerdote, Ladrão, Negociante — nomes corretos. |
| **Ambidestria** | Vantagem presente no glossário `02-configuracoes-jogo.md` linha 211 (categoria Vantagem Geral) e no provider. |

---

### 8.2 Divergências — nomes ou valores que não batem com o glossário

#### DIV-01 — Classe "Necromance" vs "Necromante" ✅ CORRIGIR

| Onde | Valor no código | Valor no glossário | Referência |
|------|-----------------|--------------------|------------|
| `DefaultGameConfigProviderImpl.java`, linha 185 | `"Necromance"` | `"Necromante"` | `02-configuracoes-jogo.md`, linha 91 |

**Correção:** Alterar `"Necromance"` para `"Necromante"` no provider. Documentado como BUG simples — corrigir no mesmo PR dos outros bugs do provider.

---

#### DIV-02 — Índole: 9 valores no código vs 3 no glossário — ✅ RESOLVIDO (PO 2026-04-04)

| Onde | Valor no código | Valor no glossário | Referência |
|------|-----------------|--------------------|------------|
| `DefaultGameConfigProviderImpl.java`, linhas 249-258 | 9 valores: Ordeiro Bondoso, Neutro Bondoso, Caótico Bondoso, Ordeiro Neutro, Neutro, Caótico Neutro, Ordeiro Maligno, Neutro Maligno, Caótico Maligno | 3 valores: Bom, Mau, Neutro | `02-configuracoes-jogo.md`, linha 161; `03-termos-dominio.md`, linha 92 |

**Análise:** O código implementa o sistema clássico de alinhamento D&D (3x3 = 9 combinações), enquanto o glossário define apenas 3 valores simples para Índole. O glossário também define "Presença" com 4 valores (Bom, Leal, Caótico, Neutro) que parecem ser a segunda dimensão do alinhamento D&D. O provider inverte essa lógica — expande Índole para 9 e redefine Presença com escala de intensidade.

**Decisão PO (Q-DC-06):** Opção A — 3 valores simples. Provider precisa ser simplificado. Ver BUG-DC-07.

---

#### DIV-03 — Presença: escala de intensidade vs alinhamento ético — ✅ RESOLVIDO (PO 2026-04-04)

| Onde | Valor no código | Valor no glossário | Referência |
|------|-----------------|--------------------|------------|
| `DefaultGameConfigProviderImpl.java`, linhas 263-270 | 6 valores: Insignificante, Fraco, Normal, Notável, Impressionante, Dominante | 4 valores: Bom, Leal, Caótico, Neutro | `02-configuracoes-jogo.md`, linha 171; `03-termos-dominio.md`, linha 93 |

**Análise:** São conceitos completamente diferentes. O glossário define Presença como "alinhamento ético/comportamental — como o personagem se relaciona com regras e estruturas sociais", com valores de postura (Leal, Caótico, Neutro, Bom). O código implementa Presença como uma **escala de intensidade/carisma** (Insignificante → Dominante), que não tem correspondência no glossário.

**Decisão PO (Q-DC-07):** Opção A — Postura ética (Bom/Leal/Caótico/Neutro). Provider precisa ser corrigido. Ver BUG-DC-08.

---

#### DIV-04 — Gênero: "Outro" no glossário vs "Não-Binário" no código — ✅ RESOLVIDO (PO 2026-04-04)

| Onde | Valor no código | Valor no glossário | Referência |
|------|-----------------|--------------------|------------|
| `DefaultGameConfigProviderImpl.java`, linha 241 | `"Não-Binário"` | `"Outro"` | `02-configuracoes-jogo.md`, linha 151 |

O glossário lista 3 opções (Masculino, Feminino, Outro). O provider tem 4 (Masculino, Feminino, Não-Binário, Prefiro não informar), com "Não-Binário" substituindo "Outro" e acrescentando "Prefiro não informar".

**Decisão PO (Q-DC-08):** Opção A — 3 valores (Masculino, Feminino, Outro). Provider deve remover "Não-Binário" e "Prefiro não informar". Ver BUG-DC-09.

---

#### DIV-05 — Membros do corpo: 6 no código vs 7 no glossário — ✅ CORRIGIR (membro "Sangue" faltando)

| Onde | Valor no código | Valor no glossário | Referência |
|------|-----------------|--------------------|------------|
| `DefaultGameConfigProviderImpl.java`, linhas 277-283 | 6 membros (sem "Sangue") | 7 membros — inclui "Sangue" | `02-configuracoes-jogo.md`, linha 121; `03-termos-dominio.md`, linha 45 |

O glossário descreve "Sangue" como membro especial (100% da vida total, representa o sistema circulatório, dano de venenos e hemorragias). O provider omite esse membro.

**Correção:** Adicionar `MembroCorpoConfigDTO.of("Sangue", new BigDecimal("1.00"), 7)` ao provider.

---

#### DIV-06 — Porcentagem da Cabeça: 25% no código vs 75% no glossário — ✅ RESOLVIDO BUG-DC-06 (PO 2026-04-04)

| Onde | Valor no código | Valor no glossário | Referência |
|------|-----------------|--------------------|------------|
| `DefaultGameConfigProviderImpl.java`, linha 277 | `0.25` (25%) | `75%` | `03-termos-dominio.md`, linha 44 |

O glossário (`03-termos-dominio.md`, linha 44) afirma explicitamente: "Cabeça = 75% da vida total. Se vida total = 100, cabeça aguenta 75 de dano." O código usa 0.25 (25%).

**Decisão PO (Q-DC-09):** Opção B — **75% (0.75)**. Golpes na cabeça são mais letais e estratégicos. Ver BUG-DC-06.

---

#### DIV-07 — Vantagens: 11 genéricas no código vs vantagens canônicas no glossário — ✅ RESOLVIDO (PO 2026-04-04)

O glossário documenta vantagens canônicas do Klayrah (TCO, TCD, TCE, TM, CFM, Saúde de Ferro, Contra-Ataque) organizadas em 8 categorias. O provider tem 11 vantagens genéricas (Fortitude, Força Aprimorada, Agilidade Aprimorada, Ataque Aprimorado, Defesa Mágica, Golpe Crítico, Vida Extra, Essência Ampliada, Visão no Escuro, Resistência a Veneno, Ambidestria) que não correspondem às vantagens nominadas no glossário.

Referências do glossário não encontradas no provider:
- `TCO` (Treinamento em Combate Ofensivo) — `03-termos-dominio.md`, linha 108
- `TCD` (Treinamento em Combate Defensivo) — `03-termos-dominio.md`, linha 109
- `TCE` (Treinamento em Combate Evasivo) — `03-termos-dominio.md`, linha 110
- `TM` (Treinamento Mágico) — `03-termos-dominio.md`, linha 111
- `CFM` (Capacidade de Força Máxima) — `03-termos-dominio.md`, linha 112
- `TPM`, `TL`, `T.M`, `DM`, `DF`, `DV`, `SG`, `IN` — `04-siglas-formulas.md`, linhas 53-64

**Decisão PO (Q-DC-10):** Opção C — Ambas. Manter as 11 genéricas + adicionar as vantagens canônicas do Klayrah. Ver Seção 9 para a lista completa de vantagens canônicas a adicionar.

---

### 8.3 Ausências — o que o glossário menciona mas não está no provider

| Item ausente | Referência no glossário | Impacto |
|---|---|---|
| **BonusConfig (6 bônus)** | `02-configuracoes-jogo.md`, linha 79: "6 bônus calculados no template" — B.B.A, Bloqueio, Reflexo, B.B.M, Percepção, Raciocínio | CRITICO — sem B.B.A e B.B.M, cálculos de combate ficam zerados. Já documentado em seção 3.1. |
| **Membro "Sangue"** | `02-configuracoes-jogo.md`, linha 118; `03-termos-dominio.md`, linha 45 | ALTA — sistema de dano por veneno/hemorragia sem suporte. Ver DIV-05 acima. |
| **CategoriaVantagem (8 categorias)** | `02-configuracoes-jogo.md`, linhas 204-212: Treinamento Físico, Treinamento Mental, Ação, Reação, Vantagem de Atributo, Vantagem Geral, Vantagem Histórica, Vantagem de Renascimento | ALTA — vantagens criadas sem categoria. Já documentado em seção 3.4. |
| **Vantagens canônicas do Klayrah** | `03-termos-dominio.md`, linhas 108-113; `04-siglas-formulas.md`, linhas 52-64 | ALTA — TCO, TCD, TCE, TM, CFM são o núcleo mecânico do sistema. Ver DIV-07 e Q-DC-10. |
| **PontosVantagemConfig** | `03-termos-dominio.md`, linha 66: "padrão: 3 pontos por nível" | ALTA — pontos de vantagem = 0 em todo jogo novo. Já documentado em seção 3.2. |
| **Fórmulas de B.B.A e B.B.M** | `03-termos-dominio.md`, linhas 25-26: B.B.A = `(FOR + AGI) / 3`, B.B.M = `(SAB + INT) / 3` | MEDIA — o provider propõe `nivel/5` para ambos (seção 3.1), mas o glossário define fórmulas diferentes baseadas em atributos. Confirmar com PO. |

---

### 8.4 Itens no provider não documentados no glossário

Esses itens existem no código mas não têm correspondência explícita no glossário. Podem ser adições válidas que precisam ser incorporadas ao glossário, ou podem ser inconsistências de design.

| Item | Localização no código | Observação |
|------|----------------------|------------|
| **Fórmula de Ímpeto de Agilidade** | linha 40: `total / 3` com unidade "metros" | Glossário não documenta o ímpeto de Agilidade. A descrição "velocidade e reflexos, determina deslocamento" sugere que Agilidade determina movimentação. |
| **Fórmula de Ímpeto de Intuição** | linha 64: `min(total / 20, 3)` com unidade "pontos" | Glossário não documenta o ímpeto de Intuição. A descrição "sorte e percepção instintiva, pontos de sorte" sugere um sistema de pontos de sorte (máx 3). |
| **Fórmula de Ímpeto de Inteligência** | linha 74: `total / 20` com unidade "comando" | Glossário não documenta o ímpeto de Inteligência. |
| **Fórmula de Ímpeto de Astúcia** | linha 79: `total / 10` com unidade "estratégia" | Glossário não documenta o ímpeto de Astúcia. |
| **Gênero "Prefiro não informar"** | linha 243 | Não está no glossário. Pode ser adição legítima de inclusão. |
| **Índole com 9 alinhamentos** | linhas 249-258 | Não está no glossário. O sistema D&D (3x3) pode ser o design pretendido, mas diverge do glossário atual. |
| **Presença como escala de intensidade** | linhas 263-270 | Não está no glossário. Pode ser redesign intencional do conceito. Ver DIV-03. |
| **LimitadorConfig com 5 faixas** | linhas 165-171 | Glossário menciona Limitador mas não documenta as faixas numéricas. O código define 5 faixas com tetos de 10, 50, 75, 100, 120. |
| **Vantagens genéricas (11)** | linhas 289-424 | Fortitude, Força Aprimorada, Agilidade Aprimorada, Golpe Crítico, Visão no Escuro, Resistência a Veneno não constam no glossário de vantagens. Podem ser substituídas pelas canônicas, ou podem coexistir como vantagens acessórias. |

---

### 8.5 Resumo das Decisões do PO — Bloco 11 (todas respondidas 2026-04-04)

| # | Decisão | Ação |
|---|---------|------|
| Q-DC-06 | Índole = 3 valores simples (Bom/Mau/Neutro) | Corrigir provider → BUG-DC-07 |
| Q-DC-07 | Presença = postura ética (Bom/Leal/Caótico/Neutro) | Corrigir provider → BUG-DC-08 |
| Q-DC-08 | Gênero = 3 valores (Masculino/Feminino/Outro) | Corrigir provider → BUG-DC-09 |
| Q-DC-09 | Cabeça = **75%** (0.75) da vida total | Corrigir provider → BUG-DC-06 |
| Q-DC-10 | Vantagens = Opção C (manter genéricas + adicionar canônicas) | Ver Seção 9 |
| Q-DC-11 | B.B.A = `(FOR+AGI)/3`, B.B.M = `(SAB+INT)/3` | Corrigir seção 3.1 ✅ |

---

*Seção 8 adicionada por: análise BA cruzando glossário e código-fonte | 2026-04-03*
*Decisões do PO registradas: 2026-04-04*

---

## 9. Vantagens Canônicas a Adicionar (Q-DC-10 Opção C)

> Fonte: `03-termos-dominio.md` linhas 100-113, `04-siglas-formulas.md` linhas 52-64, `02-configuracoes-jogo.md` linhas 204-212.
> As 11 vantagens genéricas existentes ficam. As seguintes 19 canônicas devem ser ADICIONADAS.
> **Detalhes de fórmulas de custo e efeito requerem confirmação com PO antes de implementar.**

### 9.1 Treinamento Físico (Categoria 1)

| Nome | Sigla | Efeito (glossário) | Nivel Máx |
|------|-------|-------------------|-----------|
| Treinamento em Combate Ofensivo | TCO | +1 B.B.A por nível de vantagem | 10 |
| Treinamento em Combate Defensivo | TCD | +1 Bloqueio por nível + RD natural | 10 |
| Treinamento em Combate Evasivo | TCE | +2 Reflexo por nível (dobrado) | 10 |

### 9.2 Treinamento Mental (Categoria 2)

| Nome | Sigla | Efeito (glossário) | Nivel Máx |
|------|-------|-------------------|-----------|
| Treinamento Mágico | TM | +1 B.B.M por nível de vantagem | 10 |
| Treinamento em Percepção Mágica | TPM | não detalhado no glossário | ? |
| Treinamento Lógico | TL | não detalhado no glossário | ? |
| Treinamento em Manipulação | T.M | não detalhado no glossário | ? |

### 9.3 Ação (Categoria 3)

| Nome | Sigla | Efeito |
|------|-------|--------|
| Ataque Adicional | — | não detalhado no glossário |
| Ataque Sentai | — | não detalhado no glossário |

### 9.4 Reação (Categoria 4)

| Nome | Sigla | Efeito |
|------|-------|--------|
| Contra-Ataque | — | não detalhado no glossário |
| Intercepção | — | não detalhado no glossário |

### 9.5 Vantagem de Atributo (Categoria 5)

| Nome | Sigla | Efeito |
|------|-------|--------|
| Capacidade de Força Máxima | CFM | 1D3 em danos por contusão |
| Domínio de Força | DM | não detalhado no glossário |
| Destreza Felina | DF | não detalhado no glossário |
| Domínio de Vigor | DV | não detalhado no glossário |
| Sabedoria de Gamaiel | SG | não detalhado no glossário |
| Inteligência de Nyck | IN | não detalhado no glossário |

### 9.6 Vantagem Geral (Categoria 6)

| Nome | Sigla | Efeito |
|------|-------|--------|
| Saúde de Ferro | — | +5 Vida por nível de vantagem |

> **Nota:** `Ambidestria` já existe no provider (11 genéricas) — manter, mover para categoria Vantagem Geral.

### 9.7 Vantagem Histórica (Categoria 7)

| Nome | Efeito |
|------|--------|
| Riqueza | não detalhado — contexto socioeconômico |
| Capangas | não detalhado — aliados NPCs |

### 9.8 Vantagem de Renascimento (Categoria 8)

| Nome | Pré-requisito | Efeito |
|------|---------------|--------|
| Último Sigilo | 1 Renascimento | não detalhado |
| Pensamento Bifurcado | 1 Renascimento | não detalhado |
| Previsão em Combate | 2 Renascimentos | não detalhado |

> **Nota:** Vantagens de Renascimento ficam fora do MVP (Renascimento pós-MVP, T12/T13 cancelados). Incluir no provider como placeholder sem VantagemEfeito.

---

### 9.9 Pergunta ao PO — Detalhes das Vantagens Canônicas

Várias vantagens canônicas acima não têm detalhes de efeito, custo, ou nível máximo no glossário. Antes de implementar o provider definitivo, o PO precisa definir:

- Formulas de custo (formulaCusto) para cada vantagem canônica
- Nível máximo (nivelMaximo) para TPM, TL, T.M, DM, DF, DV, SG, IN, Ação, Reação
- VantagemEfeito para cada vantagem (aguarda Spec 007)

**Adicionar ao `PERGUNTAS-PENDENTES-PO.md` — Bloco 12** quando pronto para implementar.
