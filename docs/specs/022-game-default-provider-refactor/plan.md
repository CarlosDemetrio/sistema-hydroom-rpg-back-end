# Spec 022 — Plano de Implementacao: Refatoracao do GameDefaultConfigProvider

> Spec: `022-game-default-provider-refactor`
> Baseado em: spec.md v1.0 | 2026-04-09
> Estimativa total: ~4-6 dias de trabalho
> Depende de: nada (refatoracao interna)

---

## Fases e Dependencias

```
FASE 1 (Scaffold — sequencial, blocking)
  T1: Criar pacote defaults/ + 10 providers (exceto vantagens) com dados migrados
  T2: Criar DefaultVantagensProvider com categorias + 9 build helpers vazios
  T3: Refatorar DefaultGameConfigProviderImpl como thin facade
  [T1 e T2 paralelos; T3 depende de T1+T2]

FASE 2 (Vantagens — 9 tasks paralelas, apos T3)
  T4: buildTreinamentoFisico()    (3 vantagens)
  T5: buildTreinamentoMental()    (4 vantagens)
  T6: buildAcao()                 (2 vantagens)
  T7: buildReacao()               (7 vantagens)
  T8: buildAtributo()             (8 vantagens)
  T9: buildGeral()                (5 vantagens)
  T10: buildHistorica()           (7 vantagens)
  T11: buildRenascimento()        (11 vantagens)
  T12: buildRaciais()             (17 vantagens INSOLITUS)
  [T4-T12 totalmente paralelas entre si]

FASE 3 (Testes — apos FASE 2)
  T13: Corrigir testes quebrados T5-02/T5-09 + novos T5-11..T5-19
  [T13 depende de T4-T12 todas concluidas]
```

---

## Fase 1 — Scaffold e Migracao

### T1 — Providers de configs (exceto vantagens)
**Estimativa:** 1-2 dias
**Arquivos novos (pacote `config/defaults/`):**

| Provider | Metodos | Qtd dados | Dados fonte (spec 5.x) |
|----------|---------|-----------|------------------------|
| `DefaultAtributosProvider` | `get()` | 7 atributos | spec 5.10 / `08-atributo-config.csv` |
| `DefaultAptidoesProvider` | `get()` | 24 aptidoes (12F+12M) | spec 5.12 / `13-aptidao-config.csv` |
| `DefaultNiveisProvider` | `getNiveis()`, `getLimitadores()` | 36 niveis + 5 tiers | spec 5.13 / `11-nivel-config.csv` |
| `DefaultClassesProvider` | `get()` | 12 classes | spec 5.15 / `15-classe-personagem.csv` |
| `DefaultRacasProvider` | `getRacas()`, `getBonusRaciais()`, `getVantagensPreDefinidas()` | 6 racas + 12 bonus + 20 vantagens | spec 5.16 / `16-raca.csv` + `16b` + `16e` |
| `DefaultProspeccoesProvider` | `get()` | 6 dados | spec 5.17 / `05-dado-prospeccao-config.csv` |
| `DefaultConfigSimpleProvider` | `getGeneros()`, `getIndoles()`, `getPresencas()`, `getMembrosCorpo()` | 3+3+4+7 = 17 | spec 5.18 / `02,03,04,10-*.csv` |
| `DefaultBonusProvider` | `get()` | 9 bonus | spec 5.11 / `09-bonus-config.csv` |
| `DefaultPontosVantagemProvider` | `get()` | 35 entradas | spec 5.14 / `12-pontos-vantagem-config.csv` |
| `DefaultItensProvider` | `getRaridades()`, `getTipos()`, `getItens()`, `getEfeitos()`, `getRequisitos()` | 7+20+40+29+7 = 103 | spec 5.20-5.22 / `07,14,18,18b,18c-*.csv` |

**Padrao de cada provider:**
```java
@Component
public class DefaultAtributosProvider {
    public List<AtributoConfigDTO> get() {
        return List.of(
            // dados copiados integralmente do metodo original
        );
    }
}
```

**Validacao:** `./mvnw compile`

---

### T2 — DefaultVantagensProvider (scaffold)
**Estimativa:** 0.5 dia
**Arquivo novo:** `config/defaults/DefaultVantagensProvider.java`

Criar com:
- `getCategorias()` — 9 categorias (copiadas de `getDefaultCategoriasVantagem()`)
- `getVantagens()` — agrega 9 metodos build, inicialmente retornando `List.of()` (vazios)
- 9 metodos privados `build{Categoria}()` retornando `List.of()`
- Helper privado `vantagem(...)` com 9 parametros

**Validacao:** `./mvnw compile`

---

### T3 — Facade
**Estimativa:** 0.5 dia
**Dependencias:** T1 + T2
**Arquivo modificado:** `DefaultGameConfigProviderImpl.java`

1. Adicionar `@RequiredArgsConstructor`
2. Injetar 11 providers via `private final`
3. Substituir corpo de cada `getDefault*()` por delegacao
4. Manter `getDefaultClassePontos()` e `getDefaultRacaPontos()` como stubs `Map.of()`
5. Remover imports e dados inline

**Resultado:** ~80 linhas

**Validacao:** `./mvnw test -Dtest=DefaultGameConfigProviderImplTest` (testes T5-01, T5-03..T5-08, T5-10 devem passar; T5-02 e T5-09 continuam quebrados ate T13)

---

## Fase 2 — Preenchimento das 64 Vantagens

**Arquivo alvo de todas as tasks:** `config/defaults/DefaultVantagensProvider.java`
**CSV fonte:** `docs/revisao-game-default/csv/17-vantagem-config.csv`

Cada task preenche um metodo `build{Categoria}()` com os dados do CSV usando o helper `vantagem()`.

### T4 — buildTreinamentoFisico() · 3 vantagens
**Estimativa:** 15 min
**Siglas:** VTCO, VTCD, VTCE

### T5 — buildTreinamentoMental() · 4 vantagens
**Estimativa:** 15 min
**Siglas:** VTM, VTPM, VTL, VTMA

### T6 — buildAcao() · 2 vantagens
**Estimativa:** 10 min
**Siglas:** VAA, VAS

### T7 — buildReacao() · 7 vantagens
**Estimativa:** 20 min
**Siglas:** VCA, VITC, VRE, VIH, VDH, VISB, VRA

### T8 — buildAtributo() · 8 vantagens
**Estimativa:** 20 min
**Siglas:** VCFM, VDM, VTEN, VDV, VDF, VSG, VSAG, VIN

### T9 — buildGeral() · 5 vantagens
**Estimativa:** 15 min
**Siglas:** VSFE, VCON, VSRQ, VAMB, VMF

### T10 — buildHistorica() · 7 vantagens
**Estimativa:** 20 min
**Siglas:** VHER, VRIQ, VIA, VOFI, VTOF, VVO, VCAP
**Atencao:** VRIQ tem `formulaCusto = "nivel * 5"` (nao e numero fixo)

### T11 — buildRenascimento() · 11 vantagens
**Estimativa:** 25 min
**Siglas:** VCDA, VUSI, VESC, VPCO, VAI, VDNL, VAEC, VATD, VSNM, VPBF, VMEI

### T12 — buildRaciais() · 17 vantagens INSOLITUS
**Estimativa:** 30 min
**Siglas:** VENF, VIEF, VESD, VASA, VADA, VCAL, VPIR, VCEG, VVEM, VAHU, VRHU, VVHU, VEIN, VLCI, VANA, VDES, VAAR
**Atencao:** Todas `tipoVantagem = "INSOLITUS"`, `formulaCusto = "0"`. VCAL tem `nivelMaximoVantagem = 3` (unica excecao).

---

## Fase 3 — Testes

### T13 — Correcao de testes + novos testes de invariantes
**Estimativa:** 1 dia
**Dependencias:** T4-T12 todas concluidas
**Arquivo modificado:** `DefaultGameConfigProviderImplTest.java`

**Correcoes obrigatorias:**
- T5-02: `hasSize(8)` → `hasSize(35)`. Manter assertions nivel 1 (6pts), 10 (10pts), 30 (15pts)
- T5-09: `hasSize(8)` → `hasSize(9)`. Incluir "Vantagem Racial"

**Novos testes:**
- T5-11: 64 vantagens total
- T5-12: siglas unicas, 2-5 chars e prefixo V
- T5-13: INSOLITUS → formulaCusto = "0"
- T5-14: categoriaNome valida
- T5-15: sem "custo_base" em formulaCusto
- T5-16: 7 atributos com abreviacoes unicas
- T5-17: 6 racas com nomes unicos
- T5-18: 36 niveis (0-35) com XP crescente
- T5-19: 40 itens com raridade e tipo existentes

**Validacao final:** `./mvnw test` — >= 743 testes passando

---

## Dependencias Visuais

```
[T1: 10 providers] ──┐
                      ├──> [T3: Facade] ──> [T4-T12: 9 vantagens em paralelo] ──> [T13: Testes]
[T2: Vantagens scaffold] ┘
```

---

## Checklist Final

- [ ] `./mvnw compile` — limpo
- [ ] `./mvnw test` — 100% verde (>= 743)
- [ ] `DefaultGameConfigProviderImpl` <= 100 linhas
- [ ] Cada provider em `defaults/` <= 400 linhas
- [ ] **Atributos:** 7 atributos com siglas unicas (FOR, AGI, VIG, SAB, INTU, INT, AST)
- [ ] **Bonus:** 9 bonus calculados com formulas validas
- [ ] **Aptidoes:** 24 aptidoes (12 FISICA + 12 MENTAL)
- [ ] **Niveis:** 36 niveis (0-35) com XP crescente
- [ ] **Pontos Vantagem:** 35 entradas, total acumulado = 53 pontos
- [ ] **Classes:** 12 classes com nomes unicos
- [ ] **Racas:** 6 racas + 12 bonus raciais + 20 vantagens pre-definidas
- [ ] **Prospeccoes:** 6 dados (d3 a d12)
- [ ] **Generos:** 3, **Indoles:** 3, **Presencas:** 4, **Membros:** 7
- [ ] **Categorias Vantagem:** 9 categorias incluindo "Vantagem Racial"
- [ ] **Vantagens:** 64 vantagens (47 VANTAGEM + 17 INSOLITUS)
- [ ] 17 INSOLITUS com `formulaCusto = "0"`
- [ ] Nenhuma vantagem usa `custoBase` ou `tipoBonus`
- [ ] **Raridades:** 7, **Tipos Item:** 20, **Itens:** 40
- [ ] **Efeitos Item:** 29 entradas, **Requisitos Item:** 7 entradas
- [ ] `JogoServiceIntegrationTest` continua passando

---

*Produzido por: Tech Lead / Copilot | 2026-04-09*
