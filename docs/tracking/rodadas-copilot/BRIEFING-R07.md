# Briefing Copilot R07 — Finalizar DefaultGameConfigProvider + Spec Habilidades

> Criado: 2026-04-09 pelo Copilot CLI (revisado)
> Para execução: próxima sessão Copilot
> Pré-requisito: branch `main` limpa, HEAD `6f5c54d`

---

## Contexto

R06 ficou parcial. Dois itens pendentes chegam para R07:

1. **Finalizar `getDefaultVantagens()`** — ainda tem 33 placeholders errados no provider
2. **Spec + implementação de Habilidades** — sistema ainda não existe no backend

---

## ⚠️ Testes que JÁ ESTÃO QUEBRADOS (corrigir na T-TESTES)

O arquivo `src/test/.../config/DefaultGameConfigProviderImplTest.java` tem dois testes com assertions desatualizadas **desde R06**:

| Teste | Problema | Correção necessária |
|-------|----------|---------------------|
| `T5-02` (`deveRetornarOitoPontosVantagemDefaults`) | `hasSize(8)` — provider agora tem 35 entradas | `hasSize(35)` + atualizar assertions |
| `T5-09` (`deveRetornarOitoCategoriasVantagemDefaults`) | `hasSize(8)` + lista sem "Vantagem Racial" | `hasSize(9)` + incluir "Vantagem Racial" |

---

## Estratégia de paralelismo para getDefaultVantagens()

O método tem 64 vantagens divididas em 9 categorias. Para permitir múltiplos agentes em paralelo **sem conflito de merge**, o fluxo é:

```
Wave 0 (1 agente, blocking):
  T-SCAFFOLD → refatora getDefaultVantagens() em 9 private helpers vazios

Wave 1 (9 agentes em paralelo, após T-SCAFFOLD):
  T-FIS → buildVantagensTreinamentoFisico()   (3 vantagens)
  T-MEN → buildVantagensTreinamentoMental()   (4 vantagens)
  T-ACA → buildVantagensAcao()                (2 vantagens)
  T-REA → buildVantagensReacao()              (7 vantagens)
  T-ATR → buildVantagensAtributo()            (8 vantagens)
  T-GER → buildVantagensGeral()               (5 vantagens)
  T-HIS → buildVantagensHistorica()           (7 vantagens)
  T-REN → buildVantagensRenascimento()        (11 vantagens)
  T-RAC → buildVantagensRaciais()             (17 vantagens INSOLITUS)

Wave 2 (após todas da Wave 1):
  T-TESTES → corrigir testes quebrados + adicionar testes de vantagens
```

---

## Task T-SCAFFOLD — Refatorar getDefaultVantagens() em helpers

**Tipo:** Backend · **Depende de:** nada · **Bloqueia:** todas T-FIS...T-RAC

**Arquivo:** `src/main/java/.../config/DefaultGameConfigProviderImpl.java`

### O que fazer

1. Substituir o corpo completo de `getDefaultVantagens()` por:

```java
@Override
public List<VantagemConfigDTO> getDefaultVantagens() {
    List<VantagemConfigDTO> result = new java.util.ArrayList<>();
    result.addAll(buildVantagensTreinamentoFisico());
    result.addAll(buildVantagensTreinamentoMental());
    result.addAll(buildVantagensAcao());
    result.addAll(buildVantagensReacao());
    result.addAll(buildVantagensAtributo());
    result.addAll(buildVantagensGeral());
    result.addAll(buildVantagensHistorica());
    result.addAll(buildVantagensRenascimento());
    result.addAll(buildVantagensRaciais());
    return java.util.Collections.unmodifiableList(result);
}
```

2. Adicionar 9 private methods **vazios** logo abaixo, cada um retornando `List.of()`:

```java
private List<VantagemConfigDTO> buildVantagensTreinamentoFisico()  { return List.of(); }
private List<VantagemConfigDTO> buildVantagensTreinamentoMental()  { return List.of(); }
private List<VantagemConfigDTO> buildVantagensAcao()               { return List.of(); }
private List<VantagemConfigDTO> buildVantagensReacao()             { return List.of(); }
private List<VantagemConfigDTO> buildVantagensAtributo()           { return List.of(); }
private List<VantagemConfigDTO> buildVantagensGeral()              { return List.of(); }
private List<VantagemConfigDTO> buildVantagensHistorica()          { return List.of(); }
private List<VantagemConfigDTO> buildVantagensRenascimento()       { return List.of(); }
private List<VantagemConfigDTO> buildVantagensRaciais()            { return List.of(); }
```

**Validação:** `./mvnw compile` — deve compilar. Testes de vantagens vão retornar 0/errado temporariamente (aceitável, T-TESTES vai corrigir depois).

**Commit message:**
```
refactor(defaults): extrai getDefaultVantagens em 9 private helpers por categoria [Copilot R07 T-SCAFFOLD]
```

---

## Tasks Wave 1 — Preencher helpers (paralelas entre si, após T-SCAFFOLD)

**CSV fonte de todas:** `docs/revisao-game-default/csv/17-vantagem-config.csv`

**Campos do builder a usar em TODAS as tasks:**

```java
VantagemConfigDTO.builder()
    .sigla(...)                  // coluna sigla (2-5 chars, única por jogo)
    .nome(...)
    .descricao(...)
    .nivelMaximoVantagem(...)    // Integer do CSV
    .formulaCusto(...)           // ex: "4", "0", "nivel * 5"
    .valorBonusFormula(...)      // coluna descricao_efeito
    .tipoVantagem(...)           // "VANTAGEM" ou "INSOLITUS"
    .categoriaNome(...)          // deve bater EXATAMENTE com getDefaultCategoriasVantagem()
    .nivelMinimoPersonagem(1)
    .podeEvoluir(true)
    .ordemExibicao(...)
    .build()
```

> ⚠️ **NÃO usar:** `custoBase`, `tipoBonus` — o initializer ignora esses campos

---

### Task T-FIS — buildVantagensTreinamentoFisico() · 3 vantagens

**Método alvo:** `buildVantagensTreinamentoFisico()`
**Commit message:** `feat(defaults): vantagens Treinamento Físico (TCO, TCD, TCE) [Copilot R07 T-FIS]`

| sigla | nome | nivel_maximo | formula_custo |
|-------|------|-------------|---------------|
| TCO | Treinamento em Combate Ofensivo | 10 | 4 |
| TCD | Treinamento em Combate Defensivo | 10 | 4 |
| TCE | Treinamento em Combate Evasivo | 10 | 2 |

---

### Task T-MEN — buildVantagensTreinamentoMental() · 4 vantagens

**Método alvo:** `buildVantagensTreinamentoMental()`
**Commit message:** `feat(defaults): vantagens Treinamento Mental (TM, TPM, TL, TMA) [Copilot R07 T-MEN]`

| sigla | nome | nivel_maximo | formula_custo |
|-------|------|-------------|---------------|
| TM   | Treinamento Mágico | 10 | 4 |
| TPM  | Treinamento em Percepção Mágica | 10 | 2 |
| TL   | Treinamento Lógico | 5 | 4 |
| TMA  | Treinamento em Manipulação | 3 | 3 |

---

### Task T-ACA — buildVantagensAcao() · 2 vantagens

**Método alvo:** `buildVantagensAcao()`
**Commit message:** `feat(defaults): vantagens Ação (AA, AS) [Copilot R07 T-ACA]`

| sigla | nome | nivel_maximo | formula_custo |
|-------|------|-------------|---------------|
| AA | Ataque Adicional | 1 | 10 |
| AS | Ataque Sentai | 1 | 10 |

---

### Task T-REA — buildVantagensReacao() · 7 vantagens

**Método alvo:** `buildVantagensReacao()`
**Commit message:** `feat(defaults): vantagens Reação (CA, ITC, RE, IH, DH, ISB, RA) [Copilot R07 T-REA]`

| sigla | nome | nivel_maximo | formula_custo |
|-------|------|-------------|---------------|
| CA  | Contra-Ataque | 1 | 5 |
| ITC | Interceptação | 1 | 5 |
| RE  | Reflexos Especiais | 3 | 5 |
| IH  | Instinto Heroico | 1 | 5 |
| DH  | Deflexão Heroica | 1 | 5 |
| ISB | Instinto de Sobrevivência | 3 | 3 |
| RA  | Reflexos Aprimorados | 3 | 3 |

---

### Task T-ATR — buildVantagensAtributo() · 8 vantagens

**Método alvo:** `buildVantagensAtributo()`
**Commit message:** `feat(defaults): vantagens Atributo (CFM, DM, TEN, DV, DF, SG, SAG, IN) [Copilot R07 T-ATR]`

| sigla | nome | nivel_maximo | formula_custo |
|-------|------|-------------|---------------|
| CFM | Capacidade de Força Máxima | 1 | 6 |
| DM  | Domínio de Força | 6 | 2 |
| TEN | Tenacidade | 1 | 6 |
| DV  | Domínio de Vigor | 2 | 2 |
| DF  | Destreza Felina | 1 | 5 |
| SG  | Sabedoria de Gamaiel | 3 | 3 |
| SAG | Sentidos Aguçados | 5 | 3 |
| IN  | Inteligência de Nyck | 3 | 2 |

---

### Task T-GER — buildVantagensGeral() · 5 vantagens

**Método alvo:** `buildVantagensGeral()`
**Commit message:** `feat(defaults): vantagens Geral (SFE, CONC, SRQ, AMB, MF) [Copilot R07 T-GER]`

| sigla | nome | nivel_maximo | formula_custo |
|-------|------|-------------|---------------|
| SFE  | Saúde de Ferro | 4 | 3 |
| CONC | Concentração | 4 | 3 |
| SRQ  | Saque Rápido | 2 | 3 |
| AMB  | Ambidestria | 1 | 5 |
| MF   | Memória Fotográfica | 1 | 10 |

---

### Task T-HIS — buildVantagensHistorica() · 7 vantagens

**Método alvo:** `buildVantagensHistorica()`
**Commit message:** `feat(defaults): vantagens Histórica (HER, RIQ, IA, OFI, TOF, VO, CAP) [Copilot R07 T-HIS]`

| sigla | nome | nivel_maximo | formula_custo |
|-------|------|-------------|---------------|
| HER | Herança | 1 | 5 |
| RIQ | Riqueza | 3 | `nivel * 5` ← especial, não é número fixo |
| IA  | Índole Aplicada | 5 | 2 |
| OFI | Ofícios | 2 | 2 |
| TOF | Treino de Ofício | 10 | 4 |
| VO  | Vínculo com Organização | 3 | 7 |
| CAP | Capangas | 5 | 5 |

---

### Task T-REN — buildVantagensRenascimento() · 11 vantagens

**Método alvo:** `buildVantagensRenascimento()`
**Commit message:** `feat(defaults): vantagens Renascimento (CDA, USI, ESC, PCO, AI, DNL, AEC, ATD, SNM, PBF, MEI) [Copilot R07 T-REN]`

| sigla | nome | nivel_maximo | formula_custo |
|-------|------|-------------|---------------|
| CDA | Controle de Dano | 1 | 5 |
| USI | Último Sigilo | 1 | 5 |
| ESC | Escaramuça | 3 | 3 |
| PCO | Previsão em Combate | 3 | 15 |
| AI  | Armas Improvisadas | 10 | 4 |
| DNL | Dano Não Letal | 1 | 5 |
| AEC | Ação em Cadeia | 1 | 10 |
| ATD | Atenção Difusa | 10 | 5 |
| SNM | Senso Numérico | 1 | 10 |
| PBF | Pensamento Bifurcado | 1 | 10 |
| MEI | Memória Eidética | 1 | 10 |

---

### Task T-RAC — buildVantagensRaciais() · 17 vantagens INSOLITUS

**Método alvo:** `buildVantagensRaciais()`
**Commit message:** `feat(defaults): vantagens Raciais INSOLITUS (Karzarcryer, Ikaruz, Hankraz, Humano, Anakarys) [Copilot R07 T-RAC]`

**Todas com:** `tipoVantagem = "INSOLITUS"`, `categoriaNome = "Vantagem Racial"`, `formulaCusto = "0"`, `nivelMaximoVantagem = 1`

| sigla | nome | raça |
|-------|------|------|
| ENF | Elemento Natural: Fogo | Karzarcryer |
| IEF | Imunidade Elemental: Fogo | Karzarcryer |
| ESD | Estômago de Dragão | Karzarcryer |
| ASA | Membro Adicional: Asas | Ikarúz |
| ADA | Adaptação Atmosférica | Ikarúz |
| CAL | Combate Alado | Ikarúz (nivelMaximo = 3) |
| PIR | Piercings Raciais | Hankráz |
| CEG | Corpo Esguio | Hankráz |
| VEM | Vagante entre Mundos | Hankráz |
| AHU | Adaptabilidade Humana | Humano |
| RHU | Resiliência Humana | Humano |
| VHU | Versatilidade Humana | Humano |
| EIN | Espírito Inabalável | Humano |
| LCI | Legado de Civilização | Humano |
| ANA | Armas Naturais Aprimoradas | Anakarys |
| DES | Deslocamento Especial | Anakarys |
| AAR | Ataque Adicional Racial | Anakarys |

> ⚠️ `CAL` (Combate Alado) tem `nivelMaximoVantagem = 3` — única exceção da tabela acima.

---

## Task T-TESTES — Corrigir e ampliar testes · após todas Wave 1

**Arquivo:** `src/test/.../config/DefaultGameConfigProviderImplTest.java`
**Depende de:** todas T-FIS...T-RAC concluídas

### Correções obrigatórias (testes JÁ QUEBRADOS desde R06)

**T5-02:** Mudar `hasSize(8)` → `hasSize(35)`. Manter/ajustar assertions de nível 1 (6 pts), 10 (10 pts), 30 (15 pts).

**T5-09:** Mudar `hasSize(8)` → `hasSize(9)`. Adicionar `"Vantagem Racial"` ao `containsExactlyInAnyOrder`.

### Novos testes a adicionar

```
T5-11: getDefaultVantagens() retorna exatamente 64 vantagens
T5-12: siglas das vantagens são únicas e têm 2-5 caracteres
T5-13: todos os INSOLITUS têm formulaCusto = "0"
T5-14: todas as categoriaNome existem em getDefaultCategoriasVantagem()
T5-15: nenhuma formulaCusto usa "custo_base" (variável inválida no FormulaEvaluator)
```

**Validação final:** `./mvnw test` — deve passar com ≥ 743 testes (+ novos de T5-11 a T5-15).

**Commit message:**
```
test(defaults): corrige T5-02/T5-09 + testes T5-11..T5-15 para 64 vantagens [Copilot R07 T-TESTES]
```

---

## Task T-HAB — Spec + Implementação de HabilidadeConfig

**Tipo:** Backend · **Independente das demais (pode rodar em paralelo com Wave 1)**

**Escopo SIMPLIFICADO** (definido pelo PO em 2026-04-09):

> "Apenas nome, descrição, dano/efeito (texto mesmo), e libere para o próprio usuário também criar habilidades, caso não queira nenhuma dali. Assim o mestre apenas escreve o dano/efeito. Deixa pra depois toda essa parte de requisitos, por classe, raça, vantagem e etc..."

### Entidade: `HabilidadeConfig`

| Campo | Tipo | Regras |
|-------|------|--------|
| `nome` | String | obrigatório, unique `(jogo_id, nome)` |
| `descricao` | String | opcional |
| `danoEfeito` | String | texto livre, ex: "2D6 de fogo", "Paralisa por 1 turno" |
| `ordemExibicao` | Integer | obrigatório |

Herda de `BaseEntity` + implementa `ConfiguracaoEntity`.

### Checklist de implementação (padrão das 13 entidades)

1. `HabilidadeConfig` entity
2. `HabilidadeConfigRepository`
3. `HabilidadeConfigService` extends `AbstractConfiguracaoService`
4. DTOs: `CreateHabilidadeConfigDTO` (record), `UpdateHabilidadeConfigDTO` (record), `HabilidadeConfigResponseDTO` (record)
5. `HabilidadeConfigMapper` (MapStruct)
6. `HabilidadeConfigController` — seguir padrão de `AtributoController`
7. Migration Flyway: `V{next}__create_habilidade_config.sql`
8. `HabilidadeConfigServiceIntegrationTest` extends `BaseConfiguracaoServiceIntegrationTest`

### Permissões (diferente das outras configs!)

- `POST`, `PUT`, `DELETE` → `hasAnyRole('MESTRE', 'JOGADOR')` — Jogador pode criar suas próprias
- `GET` → `hasAnyRole('MESTRE', 'JOGADOR')`

### Não implementar agora

- Requisitos por classe, raça, vantagem
- Vinculação a ficha de personagem
- Cálculo automático de dano

### Validação

- `./mvnw compile` limpo
- `./mvnw test` — todos os testes passando + testes da habilidade herdados de `BaseConfiguracaoServiceIntegrationTest`

**Commit message:**
```
feat(config): HabilidadeConfig entity + CRUD + testes [Copilot R07 T-HAB]
```

---

## Sequenciamento completo

```
Wave 0 (1 agente, blocking):
  T-SCAFFOLD

Wave 1 (10 agentes em paralelo, após T-SCAFFOLD):
  T-FIS, T-MEN, T-ACA, T-REA, T-ATR, T-GER, T-HIS, T-REN, T-RAC
  T-HAB ← independente, pode rodar desde Wave 0

Wave 2 (1 agente, após Wave 1):
  T-TESTES
```

---

## Referências úteis

- CSV vantagens: `docs/revisao-game-default/csv/17-vantagem-config.csv`
- Testes atuais: `src/test/.../config/DefaultGameConfigProviderImplTest.java`
- Padrão entity: `AtributoConfig`, `BonusConfig`
- Padrão controller: `AtributoController`
- Padrão teste: `AtributoConfiguracaoServiceIntegrationTest`

---

*Briefing R07 revisado em 2026-04-09.*
