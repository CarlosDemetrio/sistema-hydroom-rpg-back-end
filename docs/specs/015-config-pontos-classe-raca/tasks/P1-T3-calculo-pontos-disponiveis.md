# T3 — Calculo de pontosDisponiveis com 3 Fontes

> Fase: Backend | Prioridade: P1
> Dependencias: T1 (repositories ClassePontosConfig, RacaPontosConfig)
> Bloqueia: nenhuma diretamente (Spec 006 T5 e Spec 012 T5 usarao este calculo)
> Estimativa: 2–3 horas

---

## Objetivo

Atualizar o calculo de `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis` e `pontosVantagemDisponiveis` para incluir as 3 fontes de pontos: `NivelConfig` + `ClassePontosConfig` + `RacaPontosConfig`. Atualmente, apenas `NivelConfig` e considerado.

---

## Contexto

A Spec 006 T5 define que `FichaResumoResponse` deve incluir `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis` e `pontosVantagemDisponiveis`. O calculo referenciado usa apenas `NivelConfig`. Com as novas entidades de Spec 015, o calculo deve somar contribuicoes de 3 fontes.

**Formula atualizada (RN-015-01):**

```
pontosAtributoDisponiveis =
    SUM(NivelConfig.pontosAtributo WHERE jogo = ficha.jogo AND nivel <= ficha.nivel)
  + SUM(ClassePontosConfig.pontosAtributo WHERE classe = ficha.classe AND nivel <= ficha.nivel)
  + SUM(RacaPontosConfig.pontosAtributo WHERE raca = ficha.raca AND nivel <= ficha.nivel)
  - pontosAtributoGastos
```

Idem para `pontosAptidao` e `pontosVantagem`.

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `service/FichaCalculationService.java` (ou servico que calcula pontos) | EDITAR — novo metodo ou atualizar existente |
| `repository/ClassePontosConfigRepository.java` | JA EXISTE (T1) — query `findByClassePersonagemIdAndNivelLessThanEqual` |
| `repository/RacaPontosConfigRepository.java` | JA EXISTE (T1) — query `findByRacaIdAndNivelLessThanEqual` |
| `repository/NivelConfigRepository.java` | Verificar se query por nivel existe |
| Teste de integracao | CRIAR |

---

## Passos de Implementacao

### Passo 1 — Verificar queries existentes em NivelConfigRepository

Confirmar que existe:
```java
List<NivelConfig> findByJogoIdAndNivelLessThanEqual(Long jogoId, int nivel);
```

Se nao existir, adicionar.

---

### Passo 2 — Criar metodo de calculo de pontos totais

**Opcao A — Metodo dedicado no FichaCalculationService:**

```java
/**
 * Calcula os pontos totais disponiveis (atributo, aptidao, vantagem)
 * somando 3 fontes: NivelConfig + ClassePontosConfig + RacaPontosConfig.
 *
 * @param jogoId    ID do jogo
 * @param classeId  ID da classe do personagem
 * @param racaId    ID da raca do personagem
 * @param nivel     Nivel atual da ficha
 * @return PontosDisponiveis com os 3 totais brutos (antes de descontar gastos)
 */
public PontosDisponiveis calcularPontosTotais(Long jogoId, Long classeId, Long racaId, int nivel) {

    // Fonte 1: NivelConfig
    List<NivelConfig> niveis = nivelConfigRepository
        .findByJogoIdAndNivelLessThanEqual(jogoId, nivel);

    int pontosAtributoNivel = niveis.stream()
        .mapToInt(n -> n.getPontosAtributo() != null ? n.getPontosAtributo() : 0)
        .sum();
    int pontosAptidaoNivel = niveis.stream()
        .mapToInt(n -> n.getPontosAptidao() != null ? n.getPontosAptidao() : 0)
        .sum();
    // NivelConfig nao tem pontosVantagem direto — PontosVantagemConfig e separado
    // Se pontosVantagem vier de PontosVantagemConfig, somar aqui tambem

    // Fonte 2: ClassePontosConfig
    List<ClassePontosConfig> classePontos = classeId != null
        ? classePontosConfigRepository
            .findByClassePersonagemIdAndNivelLessThanEqual(classeId, nivel)
        : List.of();

    int pontosAtributoClasse = classePontos.stream()
        .mapToInt(ClassePontosConfig::getPontosAtributo)
        .sum();
    int pontosAptidaoClasse = classePontos.stream()
        .mapToInt(ClassePontosConfig::getPontosAptidao)
        .sum();
    int pontosVantagemClasse = classePontos.stream()
        .mapToInt(ClassePontosConfig::getPontosVantagem)
        .sum();

    // Fonte 3: RacaPontosConfig
    List<RacaPontosConfig> racaPontos = racaId != null
        ? racaPontosConfigRepository
            .findByRacaIdAndNivelLessThanEqual(racaId, nivel)
        : List.of();

    int pontosAtributoRaca = racaPontos.stream()
        .mapToInt(RacaPontosConfig::getPontosAtributo)
        .sum();
    int pontosAptidaoRaca = racaPontos.stream()
        .mapToInt(RacaPontosConfig::getPontosAptidao)
        .sum();
    int pontosVantagemRaca = racaPontos.stream()
        .mapToInt(RacaPontosConfig::getPontosVantagem)
        .sum();

    return new PontosDisponiveis(
        pontosAtributoNivel + pontosAtributoClasse + pontosAtributoRaca,
        pontosAptidaoNivel + pontosAptidaoClasse + pontosAptidaoRaca,
        pontosVantagemClasse + pontosVantagemRaca  // + pontosVantagemNivel se vier de PontosVantagemConfig
    );
}
```

---

### Passo 3 — Criar record PontosDisponiveis (se nao existir)

```java
/**
 * Pontos brutos totais disponiveis (antes de descontar gastos).
 * Fonte: NivelConfig + ClassePontosConfig + RacaPontosConfig.
 */
public record PontosDisponiveis(
    int pontosAtributoTotal,
    int pontosAptidaoTotal,
    int pontosVantagemTotal
) {}
```

---

### Passo 4 — Integrar com PontosVantagemConfig

Verificar se `PontosVantagemConfig` (ja existente) tambem contribui para `pontosVantagemDisponiveis`. Se sim, somar:

```java
// Fonte adicional: PontosVantagemConfig
List<PontosVantagemConfig> pontosVantagemConfig = pontosVantagemConfigRepository
    .findByJogoIdAndNivelLessThanEqual(jogoId, nivel);

int pontosVantagemNivel = pontosVantagemConfig.stream()
    .mapToInt(PontosVantagemConfig::getPontos)
    .sum();
```

> **Nota:** Confirmar com o codigo existente se `PontosVantagemConfig` ja e somado em algum lugar. Nao duplicar.

---

### Passo 5 — Calcular pontos GASTOS

O desconto de pontos gastos depende dos campos existentes:
- `pontosAtributoGastos = SUM(FichaAtributo.base)` — decisao PO Q15
- `pontosAptidaoGastos = SUM(FichaAptidao.base)` — decisao PO Q15
- `pontosVantagemGastos = SUM(FichaVantagem.custoPago)` — apenas vantagens compradas

O calculo final:
```
pontosAtributoDisponiveis = pontosAtributoTotal - pontosAtributoGastos
pontosAptidaoDisponiveis  = pontosAptidaoTotal  - pontosAptidaoGastos
pontosVantagemDisponiveis = pontosVantagemTotal  - pontosVantagemGastos
```

---

## Testes de Integracao

### Cenario T3-01 — Pontos somente de NivelConfig

```
Dado: Jogo com NivelConfig nivel 1 (pontosAtributo=10, pontosAptidao=5)
E: Ficha nivel 1, sem classe nem raca com ClassePontosConfig ou RacaPontosConfig
Quando: calcularPontosTotais() e chamado
Entao: pontosAtributoTotal == 10, pontosAptidaoTotal == 5
```

### Cenario T3-02 — Pontos com 3 fontes

```
Dado: Jogo com NivelConfig nivel 1 (pontosAtributo=10)
E: ClassePontosConfig nivel 1 da classe Guerreiro (pontosAtributo=2)
E: RacaPontosConfig nivel 1 da raca Elfo (pontosAtributo=1)
E: Ficha nivel 1, classe=Guerreiro, raca=Elfo
Quando: calcularPontosTotais() e chamado
Entao: pontosAtributoTotal == 13 (10 + 2 + 1)
```

### Cenario T3-03 — Acumulacao por nivel

```
Dado: NivelConfig nivel 1 (pontosAtributo=10), nivel 2 (pontosAtributo=3)
E: ClassePontosConfig nivel 1 (pontosAtributo=2), nivel 3 (pontosAtributo=5)
E: Ficha nivel 2
Quando: calcularPontosTotais() e chamado
Entao: pontosAtributoTotal == 15 (10+3 do NivelConfig, +2 do ClassePontosConfig nivel 1)
E: ClassePontosConfig nivel 3 NAO e incluido (nivel 3 > ficha nivel 2)
```

### Cenario T3-04 — Ficha sem classe/raca (nullable)

```
Dado: Ficha nivel 1 sem classe e sem raca definidas
Quando: calcularPontosTotais() e chamado
Entao: Apenas pontos de NivelConfig retornados, sem erro
```

---

## Regras de Negocio

- **RN-015-01:** Pontos acumulam — `nivel <= fichaLevel` (SUM de todos os niveis ate o atual)
- **RN-015-02:** Cada entrada e por nivel especifico (nao por range)
- Classe e raca nullable — ficha pode nao ter classe ou raca definida ainda (wizard incompleto)
- Pontos gastos calculados conforme decisao PO Q15: `SUM(FichaAtributo.base)` e `SUM(FichaAptidao.base)`

---

## Criterios de Aceitacao

- [ ] Calculo de pontosDisponiveis soma NivelConfig + ClassePontosConfig + RacaPontosConfig
- [ ] Classe e raca nullable tratados sem erro
- [ ] Pontos acumulam corretamente por nivel (nivel <= fichaLevel)
- [ ] ClassePontosConfig de nivel superior ao da ficha NAO e incluido
- [ ] Cenarios T3-01 a T3-04 passam como testes de integracao
- [ ] `./mvnw test` passa (testes existentes nao quebram)

---

*Produzido por: PM/Scrum Master | 2026-04-04*
