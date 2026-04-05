# T0 — Corrigir Bugs Pre-Existentes no FichaCalculationService

> Fase: Backend | Prioridade: P0 — BLOQUEIA TODAS AS OUTRAS TASKS DESTA SPEC
> Dependencias: nenhuma
> Bloqueia: T1, T2, T3, T4, T5, T6, T7, T8
> Estimativa: 4–6 horas

---

## Objetivo

Corrigir bugs ativos no motor de calculos (`FichaCalculationService`, `FichaAmeaca`, `FichaVida`, `FichaService`) que existem independentemente da integracao de `VantagemEfeito`. Estes bugs tornam os calculos de qualquer ficha matematicamente incorretos — bônus de classe zerados, bonus de raca ignorados, nivel nunca atualizado ao ganhar XP.

Esta task deve ser executada e validada (testes passando) antes de iniciar qualquer outra task da Spec 007.

---

## Contexto

A auditoria `docs/analises/INTEGRACAO-CONFIG-FICHA.md` identificou os seguintes bugs ativos:

| Bug | Entidade afetada | Campo zerado incorretamente |
|-----|------------------|-----------------------------|
| GAP-CALC-01 | `FichaBonus.classe` | `ClasseBonus.valorPorNivel * nivel` nunca aplicado — **ver nota abaixo** |
| GAP-CALC-02 | `FichaAptidao.classe` | `ClasseAptidaoBonus.bonus` nunca aplicado — **ver nota abaixo** |
| GAP-CALC-03 | `FichaAtributo.outros` | `RacaBonusAtributo.bonus` nunca aplicado |
| GAP-CALC-06 | `Ficha.nivel` | Nivel nunca recalculado ao ganhar XP |
| GAP-CALC-07 | `FichaAmeaca.recalcularTotal()` | `nivel` ausente do calculo da entidade |
| GAP-CALC-08 | `FichaVida.recalcularTotal()` | `vigorTotal` e `nivel` ausentes do calculo da entidade |

> **GAP-CALC-09** (VIG/SAB hardcoded): NAO esta no escopo desta task. Aguarda decisao do PO (PA-006 na spec.md).

> **REGRA DE NEGOCIO CONFIRMADA (2026-04-04):** Apenas vantagens, insólitus e equipamentos geram bônus diretamente. Todos os demais bônus são CALCULADOS a partir de atributos + somatório de vantagens/equipamentos. Isso implica:
> - **GAP-CALC-01 / ClasseBonus:** Classes NÃO dão bônus diretos em BonusConfig por padrão. No `DefaultGameConfigProvider`, `getDefaultClasseBonus()` retorna mapa vazio. A mecânica de `ClasseBonus` existe para que o **Mestre configure manualmente** classes com bônus customizados. O `FichaCalculationService` deve aplicar `ClasseBonus.valorPorNivel × nivel` quando existir, mas não esperar que exista.
> - **GAP-CALC-02 / ClasseAptidaoBonus:** Mesma lógica — opcional, configurado pelo Mestre.
> - **GAP-CALC-03 / RacaBonusAtributo:** Este continua sendo bug real — raças têm bônus configurados por padrão e nunca são aplicados.

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `service/FichaCalculationService.java` | Adicionar Passos 1–4 da nova sequencia de calculo (reset + raca + classeBonus + classeAptidaoBonus) |
| `service/FichaService.java` | Recalcular nivel apos concessao de XP |
| `model/FichaAmeaca.java` | Corrigir `recalcularTotal()` para incluir `nivel` |
| `model/FichaVida.java` | Corrigir `recalcularTotal()` ou remover metodo inconsistente |
| `repository/ClasseBonusRepository.java` | Adicionar query para buscar ClasseBonus por classe |
| `repository/ClasseAptidaoBonusRepository.java` | Adicionar query para buscar ClasseAptidaoBonus por classe |
| `repository/NivelConfigRepository.java` | Adicionar query para buscar nivel por XP |

---

## Passos de Implementacao

### Passo 1 — Corrigir FichaAmeaca.recalcularTotal() (GAP-CALC-07)

O metodo atual ignora o `nivel`. Como `FichaAmeaca` nao armazena `nivel` (ele pertence a `Ficha`), a opcao mais limpa e remover `recalcularTotal()` da entidade e garantir que apenas o `FichaCalculationService.calcularAmeacaTotal()` execute o calculo completo.

**Opcao A (remover da entidade — recomendada):**
```java
// Remover de FichaAmeaca.java o metodo recalcularTotal()
// O calculo correto ja esta em FichaCalculationService.calcularAmeacaTotal():
// total = nivelFicha + itens + titulos + renascimentos + outros
```

**Opcao B (manter e corrigir):**
Se o metodo for necessario para uso isolado (ex: testes de entidade), adicionar `nivel` como parametro:
```java
public void recalcularTotal(int nivel) {
    this.total = nivel +
                 (itens != null ? itens : 0) +
                 (titulos != null ? titulos : 0) +
                 (renascimentos != null ? renascimentos : 0) +
                 (outros != null ? outros : 0);
}
```

> Confirmar com Tech Lead qual opcao adotar. Registrar decisao como comentario no codigo.

---

### Passo 2 — Corrigir FichaVida.recalcularTotal() (GAP-CALC-08)

O metodo atual calcula apenas `vt + outros`. A formula completa requer `vigorTotal + nivel + vt + renascimentos + outros`. Identicamente ao GAP-CALC-07, `vigorTotal` e `nivel` sao externos a entidade.

**Opcao A (remover da entidade — recomendada):**
```java
// Remover de FichaVida.java o metodo recalcularTotal()
// O calculo correto ja esta em FichaCalculationService.calcularVidaTotal()
```

**Opcao B (manter com parametros):**
```java
public void recalcularTotal(int vigorTotal, int nivel) {
    this.vidaTotal = vigorTotal + nivel +
                     (vt != null ? vt : 0) +
                     (renascimentos != null ? renascimentos : 0) +
                     (outros != null ? outros : 0);
}
```

---

### Passo 3 — Adicionar queries nos repositorios

**ClasseBonusRepository** — buscar ClasseBonus por classe:

```java
/**
 * Busca todos os ClasseBonus de uma classe especifica.
 * Carrega o BonusConfig associado para lookup por ID.
 */
@Query("""
    SELECT cb FROM ClasseBonus cb
    JOIN FETCH cb.bonusConfig
    WHERE cb.classe.id = :classeId
    AND cb.deletedAt IS NULL
    """)
List<ClasseBonus> findByClasseIdWithBonusConfig(@Param("classeId") Long classeId);
```

**ClasseAptidaoBonusRepository** — buscar ClasseAptidaoBonus por classe:

```java
/**
 * Busca todos os ClasseAptidaoBonus de uma classe especifica.
 * Carrega a AptidaoConfig associada para lookup por ID.
 */
@Query("""
    SELECT cab FROM ClasseAptidaoBonus cab
    JOIN FETCH cab.aptidao
    WHERE cab.classe.id = :classeId
    AND cab.deletedAt IS NULL
    """)
List<ClasseAptidaoBonus> findByClasseIdWithAptidao(@Param("classeId") Long classeId);
```

**NivelConfigRepository** — buscar nivel maximo por XP:

```java
/**
 * Retorna o maior nivel onde xpNecessaria <= xpAtual para o jogo informado.
 * Usado para recalcular o nivel da ficha ao ganhar XP.
 */
@Query("""
    SELECT n FROM NivelConfig n
    WHERE n.jogo.id = :jogoId
    AND n.xpNecessaria <= :xpAtual
    AND n.deletedAt IS NULL
    ORDER BY n.nivel DESC
    LIMIT 1
    """)
Optional<NivelConfig> findMaxNivelAlcancado(
    @Param("jogoId") Long jogoId,
    @Param("xpAtual") Long xpAtual);
```

---

### Passo 4 — Implementar Passos 1–4 da sequencia de calculo no FichaCalculationService

Adicionar metodos privados `resetarCamposDerivaveis()`, `aplicarRacaBonusAtributo()`, `aplicarClasseBonus()`, `aplicarClasseAptidaoBonus()` e chama-los no inicio de `recalcular()`, antes dos calculos existentes.

**4.1 resetarCamposDerivaveis:**

```java
/**
 * Passo 1 da sequencia: zera todos os campos que serao recalculados.
 * Chamado no inicio de recalcular() para garantir idempotencia.
 * NAO zera campos de entrada manual do Mestre (itens, gloria, outros, etc.).
 */
private void resetarCamposDerivaveis(
        List<FichaAtributo> atributos,
        List<FichaAptidao> aptidoes,
        List<FichaBonus> bonus,
        FichaVida vida,
        List<FichaVidaMembro> membros,
        FichaEssencia essencia) {

    atributos.forEach(a -> a.setOutros(0));
    // aptidoes.outros sera zerado apos SCHEMA-01 (task T1)
    bonus.forEach(b -> {
        b.setVantagens(0);
        b.setClasse(0);
    });
    aptidoes.forEach(a -> a.setClasse(0));
    vida.setVt(0);
    // membros.bonusVantagens sera zerado apos SCHEMA-02 (task T1)
    essencia.setVantagens(0);
}
```

**4.2 aplicarRacaBonusAtributo (GAP-CALC-03):**

```java
/**
 * Passo 2 da sequencia: aplica bonus raciais em FichaAtributo.outros.
 * Bonus de raca e fixo (nao escala com nivel) e pode ser negativo.
 * O campo outros ja foi zerado no Passo 1 — este metodo soma em cima de zero.
 *
 * @param ficha    ficha com getRaca() carregado (Raca.bonusAtributos via JOIN FETCH)
 * @param atributos lista de FichaAtributo da ficha
 */
private void aplicarRacaBonusAtributo(Ficha ficha, List<FichaAtributo> atributos) {
    if (ficha.getRaca() == null) return;

    Map<Long, FichaAtributo> atributosMap = atributos.stream()
        .filter(a -> a.getAtributoConfig() != null)
        .collect(Collectors.toMap(a -> a.getAtributoConfig().getId(), a -> a));

    for (RacaBonusAtributo racaBonus : ficha.getRaca().getBonusAtributos()) {
        if (racaBonus.getAtributo() == null) continue;
        FichaAtributo alvo = atributosMap.get(racaBonus.getAtributo().getId());
        if (alvo != null) {
            alvo.setOutros(alvo.getOutros() + racaBonus.getBonus()); // pode ser negativo
        } else {
            log.warn("RacaBonusAtributo: FichaAtributo nao encontrado para AtributoConfig ID {} (raca: {})",
                racaBonus.getAtributo().getId(), ficha.getRaca().getNome());
        }
    }
}
```

**4.3 aplicarClasseBonus (GAP-CALC-01):**

```java
/**
 * Passo 3 da sequencia: aplica bonus de classe em FichaBonus.classe.
 * Formula: classeBonus.valorPorNivel * ficha.nivel (multiplica pelo nivel da FICHA).
 * O campo classe ja foi zerado no Passo 1 — este metodo sobrescreve com o valor correto.
 *
 * @param ficha  ficha com getClasse() carregado
 * @param bonus  lista de FichaBonus da ficha
 * @param classeBonus lista de ClasseBonus da classe (carregados previamente para evitar N+1)
 */
private void aplicarClasseBonus(Ficha ficha, List<FichaBonus> bonus, List<ClasseBonus> classeBonus) {
    if (ficha.getClasse() == null || classeBonus.isEmpty()) return;

    Map<Long, FichaBonus> bonusMap = bonus.stream()
        .filter(b -> b.getBonusConfig() != null)
        .collect(Collectors.toMap(b -> b.getBonusConfig().getId(), b -> b));

    for (ClasseBonus cb : classeBonus) {
        if (cb.getBonusConfig() == null) continue;
        FichaBonus alvo = bonusMap.get(cb.getBonusConfig().getId());
        if (alvo != null) {
            int valorClasse = cb.getValorPorNivel() != null
                ? cb.getValorPorNivel() * ficha.getNivel()
                : 0;
            alvo.setClasse(alvo.getClasse() + valorClasse);
        } else {
            log.warn("ClasseBonus: FichaBonus nao encontrado para BonusConfig ID {} (classe: {})",
                cb.getBonusConfig().getId(), ficha.getClasse().getNome());
        }
    }
}
```

**4.4 aplicarClasseAptidaoBonus (GAP-CALC-02):**

```java
/**
 * Passo 4 da sequencia: aplica bonus fixo de classe em FichaAptidao.classe.
 * ClasseAptidaoBonus.bonus e FIXO — nao multiplica pelo nivel.
 * O campo classe ja foi zerado no Passo 1 — este metodo sobrescreve com o valor correto.
 *
 * @param ficha           ficha com getClasse() carregado
 * @param aptidoes        lista de FichaAptidao da ficha
 * @param classeAptidaoBonus lista de ClasseAptidaoBonus (carregados previamente)
 */
private void aplicarClasseAptidaoBonus(
        Ficha ficha,
        List<FichaAptidao> aptidoes,
        List<ClasseAptidaoBonus> classeAptidaoBonus) {

    if (ficha.getClasse() == null || classeAptidaoBonus.isEmpty()) return;

    Map<Long, FichaAptidao> aptidoesMap = aptidoes.stream()
        .filter(a -> a.getAptidaoConfig() != null)
        .collect(Collectors.toMap(a -> a.getAptidaoConfig().getId(), a -> a));

    for (ClasseAptidaoBonus cab : classeAptidaoBonus) {
        if (cab.getAptidao() == null) continue;
        FichaAptidao alvo = aptidoesMap.get(cab.getAptidao().getId());
        if (alvo != null) {
            alvo.setClasse(alvo.getClasse() + (cab.getBonus() != null ? cab.getBonus() : 0));
        } else {
            log.warn("ClasseAptidaoBonus: FichaAptidao nao encontrada para AptidaoConfig ID {} (classe: {})",
                cab.getAptidao().getId(), ficha.getClasse().getNome());
        }
    }
}
```

**4.5 Atualizar recalcular() para invocar os novos metodos:**

```java
// Os parametros existentes sao mantidos. Adicionar os novos no inicio do metodo:
public void recalcular(
        Ficha ficha,                        // ja existia
        List<FichaAtributo> atributos,      // ja existia
        List<FichaAptidao> aptidoes,        // JA ADICIONADO em T2 (ou adicionar aqui se T2 nao esta feita)
        List<FichaBonus> bonus,             // ja existia
        FichaVida vida,                     // ja existia
        List<FichaVidaMembro> membros,      // ja existia
        FichaEssencia essencia,             // ja existia
        FichaAmeaca ameaca,                 // ja existia
        List<ClasseBonus> classeBonus,      // NOVO — carregado pelo FichaService antes de chamar
        List<ClasseAptidaoBonus> classeAptidaoBonus) { // NOVO

    // PASSO 1: reset
    resetarCamposDerivaveis(atributos, aptidoes, bonus, vida, membros, essencia);

    // PASSO 2: bônus racial
    aplicarRacaBonusAtributo(ficha, atributos);

    // PASSO 3: bônus de classe em bônus derivados
    aplicarClasseBonus(ficha, bonus, classeBonus);

    // PASSO 4: bônus de classe em aptidoes
    aplicarClasseAptidaoBonus(ficha, aptidoes, classeAptidaoBonus);

    // PASSOS 5-10: calculos existentes (VantagemEfeito sera Passo 5 apos T2-T6)
    // ... metodos existentes inalterados ...
}
```

> **Nota sobre carregamento sem N+1:** O `FichaService` deve buscar `classeBonus` e `classeAptidaoBonus` usando as queries dos repositorios (Passo 3 desta task) antes de chamar `recalcular()`. O `FichaCalculationService` nao deve fazer queries adicionais dentro dos metodos de calculo.

---

### Passo 5 — Recalcular nivel ao ganhar XP no FichaService (GAP-CALC-06)

No `FichaService`, no metodo que processa `PUT /fichas/{id}/xp` (ou onde XP e atualizado), adicionar o algoritmo de recalculo de nivel:

```java
/**
 * Recalcula o nivel da ficha com base no XP atual.
 * Retorna true se houve level up (nivel aumentou).
 */
private boolean recalcularNivel(Ficha ficha) {
    int nivelAnterior = ficha.getNivel();

    Optional<NivelConfig> nivelAlcancado = nivelConfigRepository
        .findMaxNivelAlcancado(ficha.getJogo().getId(), ficha.getXp());

    int novoNivel = nivelAlcancado
        .map(NivelConfig::getNivel)
        .orElse(1); // nivel minimo = 1

    ficha.setNivel(novoNivel);
    return novoNivel > nivelAnterior;
}
```

Integrar no fluxo de concessao de XP:

```java
// Apos atualizar ficha.xp:
boolean levelUp = recalcularNivel(ficha);
ficha = fichaRepository.save(ficha);

// Se houve level up, recalcular a ficha completa:
if (levelUp) {
    // chamar recalcular() com os dados atualizados
    // incluir flag levelUp no response
}
```

---

## Regras de Negocio

- **RN-RESET:** O reset no Passo 1 e obrigatorio para idempotencia. Sem ele, cada chamada a `recalcular()` acumula bonus em cima dos anteriores.
- **RN-RACA-NEGATIVO:** `RacaBonusAtributo.bonus` pode ser negativo. O codigo deve tratar isso — `a.setOutros(a.getOutros() + racaBonus.getBonus())` ja funciona para valores negativos.
- **RN-CLASSE-NIVEL:** `ClasseBonus.valorPorNivel` multiplica pelo nivel da FICHA, nao da classe. Se o sistema suportar multi-classe no futuro, esta regra precisara revisao (PA-CALC-01).
- **RN-CLASSE-FIXO:** `ClasseAptidaoBonus.bonus` e fixo e nao escala com nivel. Contrasta deliberadamente com `ClasseBonus`.
- **RN-NIVEL-MINIMO:** Se nenhum `NivelConfig` tiver `xpNecessaria <= ficha.xp` (ex: ficha com xp=0 e NivelConfig.nivel=1 requer xp=100), o nivel fica em 1 (nivel inicial).

---

## Testes de Integracao

Criar classe `FichaCalculationServiceBugsIntegrationTest` (ou adicionar nos testes existentes de FichaCalculationService).

### Cenario T0-01 — ClasseBonus aplicado corretamente

```
Dado: Jogo com Guerreiro tendo ClasseBonus.valorPorNivel=2 em BonusConfig "B.B.A"
E: Ficha com classe=Guerreiro, nivel=5
Quando: FichaService.recalcular() e chamado
Entao: FichaBonus.BBA.classe == 10  (2 * 5)
E: FichaBonus.BBA.total inclui os 10 pontos de classe
```

### Cenario T0-02 — ClasseAptidaoBonus aplicado corretamente

```
Dado: Jogo com Ladrao tendo ClasseAptidaoBonus.bonus=3 em AptidaoConfig "Furtividade"
E: Ficha com classe=Ladrao
Quando: FichaService.recalcular() e chamado
Entao: FichaAptidao.Furtividade.classe == 3
E: FichaAptidao.Furtividade.total inclui os 3 pontos de classe
```

### Cenario T0-03 — RacaBonusAtributo positivo aplicado

```
Dado: Jogo com Elfo tendo RacaBonusAtributo.bonus=2 em AtributoConfig "AGI"
E: Ficha com raca=Elfo
Quando: FichaService.recalcular() e chamado
Entao: FichaAtributo.AGI.outros == 2
E: FichaAtributo.AGI.total inclui os 2 pontos raciais
```

### Cenario T0-04 — RacaBonusAtributo negativo aplicado (penalidade)

```
Dado: Jogo com Orc tendo RacaBonusAtributo.bonus=-2 em AtributoConfig "SAB"
E: Ficha com raca=Orc
Quando: FichaService.recalcular() e chamado
Entao: FichaAtributo.SAB.outros == -2
E: FichaAtributo.SAB.total e reduzido em 2
```

### Cenario T0-05 — Idempotencia: recalcular() duas vezes nao acumula

```
Dado: Ficha com Guerreiro ClasseBonus.valorPorNivel=2 em B.B.A, nivel=5
Quando: recalcular() e chamado DUAS vezes
Entao: FichaBonus.BBA.classe == 10 (nao 20)
```

### Cenario T0-06 — Nivel recalculado ao ganhar XP

```
Dado: Jogo com NivelConfig: nivel=1(xp=0), nivel=2(xp=100), nivel=3(xp=300)
E: Ficha com xp=50, nivel=1
Quando: Mestre concede 200 XP (total=250)
Entao: ficha.nivel == 2
E: response inclui flag levelUp=true
```

### Cenario T0-07 — Nivel nao regride com XP insuficiente

```
Dado: Ficha com xp=350, nivel=3
Quando: recalcularNivel() e chamado
Entao: ficha.nivel == 3 (nivel 3 requer xp=300 <= 350)
```

---

## Criterios de Aceitacao

- [ ] `FichaBonus.classe` e calculado como `ClasseBonus.valorPorNivel * ficha.nivel` para cada BonusConfig da classe
- [ ] `FichaAptidao.classe` e preenchido com `ClasseAptidaoBonus.bonus` (fixo) para cada AptidaoConfig da classe
- [ ] `FichaAtributo.outros` e inicializado com `RacaBonusAtributo.bonus` (pode ser negativo)
- [ ] `recalcular()` chamado duas vezes nao acumula bonus (idempotente)
- [ ] `FichaAmeaca.recalcularTotal()` corrigido (inclui nivel ou metodo removido da entidade)
- [ ] `FichaVida.recalcularTotal()` corrigido (inclui vigorTotal e nivel ou metodo removido da entidade)
- [ ] XP concedido via `PUT /fichas/{id}/xp` dispara recalculo de nivel
- [ ] Level up retorna flag `levelUp: true` no response
- [ ] Nivel nao regride se XP for concedido mas nao atingir o proximo nivel
- [ ] Todos os cenarios T0-01 a T0-07 passam como testes de integracao
- [ ] `./mvnw test` passa (457 testes existentes nao quebram)

---

## Nota sobre GAP-CALC-09 (fora do escopo)

O GAP-CALC-09 (VIG e SAB hardcoded por abreviacao) NAO e corrigido nesta task. O problema existe mas a solucao requer decisao do PO sobre como o sistema identificara qual atributo alimenta a formula de vida e qual alimenta a formula de essencia (ver PA-006 na spec.md). A correcao sera feita em task separada apos decisao do PO.

---

*Produzido por: Business Analyst/PO | 2026-04-03*
