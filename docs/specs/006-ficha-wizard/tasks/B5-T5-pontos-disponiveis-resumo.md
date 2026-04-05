# T5 — `pontosDisponiveis` no `FichaResumoResponse`

> Fase: Backend
> Complexidade: Alta
> Prerequisito: Nenhum (independente)
> Bloqueia: T8 (Atributos), T9 (Aptidoes), T10 (Vantagens)
> Estimativa: 4–6 horas

---

## Objetivo

Adicionar ao `FichaResumoResponse` os campos `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis` e `pontosVantagemDisponiveis`, calculados como a diferenca entre pontos ganhos ao longo dos niveis e pontos ja gastos na ficha. Esta informacao e critica para o wizard de criacao e para a tela de distribuicao de pontos pos-nivel.

---

## Contexto

O `FichaResumoResponse` atual (`dto/response/FichaResumoResponse.java`) nao inclui informacoes sobre pontos disponiveis. O frontend precisa saber:

- Quantos pontos de atributo o jogador ainda pode distribuir
- Quantos pontos de aptidao o jogador ainda pode distribuir
- Quantos pontos de vantagem o jogador ainda pode gastar

Estes valores sao calculados a partir de:
- `NivelConfig` — pontos ganhos por nivel de atributo e aptidao
- `PontosVantagemConfig` — pontos de vantagem ganhos por nivel
- `FichaAtributo.base` — pontos ja distribuidos em atributos
- `FichaAptidao.base` — pontos ja distribuidos em aptidoes
- `FichaVantagem.custoPago` — pontos de vantagem ja gastos

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `dto/response/FichaResumoResponse.java` | Adicionar 3 campos de pontos disponiveis |
| `service/FichaResumoService.java` | Implementar calculo dos pontos disponiveis |
| `repository/ConfiguracaoNivelRepository.java` | Adicionar query para soma de pontos por nivel |
| `repository/PontosVantagemConfigRepository.java` | Adicionar query para soma de pontos de vantagem |

---

## Formulas de Calculo

### `pontosAtributoDisponiveis`

```
pontosAtributoDisponiveis =
    SUM(NivelConfig.pontosAtributo WHERE nivel >= 1 AND nivel <= ficha.nivel)
    - SUM(FichaAtributo.base WHERE fichaId = ficha.id)
```

**Nota:** O nivel 0 nao concede pontos de atributo (xpNecessaria=0, e o estado inicial antes do nivel 1). Apenas a partir do nivel 1 o jogador ganha pontos.

**Restricao:** O valor nunca pode ser negativo. Se for negativo (configuracao errada do Mestre), retornar 0.

### `pontosAptidaoDisponiveis`

```
pontosAptidaoDisponiveis =
    SUM(NivelConfig.pontosAptidao WHERE nivel >= 1 AND nivel <= ficha.nivel)
    - SUM(FichaAptidao.base WHERE fichaId = ficha.id)
```

**Mesma restricao:** minimo 0.

### `pontosVantagemDisponiveis`

```
pontosVantagemDisponiveis =
    SUM(PontosVantagemConfig.pontosGanhos WHERE nivel >= 1 AND nivel <= ficha.nivel)
    - SUM(FichaVantagem.custoPago WHERE fichaId = ficha.id)
```

**Nota:** `PontosVantagemConfig` e esparso — pode nao haver registro para todos os niveis. `SUM` com LEFT JOIN retorna 0 para niveis sem registro.

**Mesma restricao:** minimo 0.

---

## Passos de Implementacao

### 1. Atualizar `FichaResumoResponse`

```java
// dto/response/FichaResumoResponse.java
public record FichaResumoResponse(
        Long id,
        String nome,
        int nivel,
        long xp,
        String racaNome,
        String classeNome,
        Map<String, Integer> atributosTotais,
        Map<String, Integer> bonusTotais,
        int vidaTotal,
        int essenciaTotal,
        int ameacaTotal,
        // NOVOS CAMPOS:
        int pontosAtributoDisponiveis,
        int pontosAptidaoDisponiveis,
        int pontosVantagemDisponiveis
) {}
```

**Atencao:** Adicionar ao final do record para nao quebrar construtores em testes existentes. Verificar testes que constroem `FichaResumoResponse` diretamente e atualizar.

### 2. Adicionar queries em `ConfiguracaoNivelRepository`

```java
// Soma de pontosAtributo de niveis 1 ate nivelAtual
@Query("SELECT COALESCE(SUM(n.pontosAtributo), 0) FROM NivelConfig n WHERE n.jogo.id = :jogoId AND n.nivel >= 1 AND n.nivel <= :nivelAtual AND n.deletedAt IS NULL")
int somarPontosAtributo(@Param("jogoId") Long jogoId, @Param("nivelAtual") int nivelAtual);

// Soma de pontosAptidao de niveis 1 ate nivelAtual
@Query("SELECT COALESCE(SUM(n.pontosAptidao), 0) FROM NivelConfig n WHERE n.jogo.id = :jogoId AND n.nivel >= 1 AND n.nivel <= :nivelAtual AND n.deletedAt IS NULL")
int somarPontosAptidao(@Param("jogoId") Long jogoId, @Param("nivelAtual") int nivelAtual);
```

### 3. Adicionar query em `PontosVantagemConfigRepository`

```java
// Soma de pontosGanhos de niveis 1 ate nivelAtual
@Query("SELECT COALESCE(SUM(p.pontosGanhos), 0) FROM PontosVantagemConfig p WHERE p.jogo.id = :jogoId AND p.nivel >= 1 AND p.nivel <= :nivelAtual AND p.deletedAt IS NULL")
int somarPontosVantagem(@Param("jogoId") Long jogoId, @Param("nivelAtual") int nivelAtual);
```

### 4. Adicionar queries de pontos gastos

```java
// Em FichaAtributoRepository
@Query("SELECT COALESCE(SUM(fa.base), 0) FROM FichaAtributo fa WHERE fa.ficha.id = :fichaId")
int somarBaseAtributos(@Param("fichaId") Long fichaId);

// Em FichaAptidaoRepository
@Query("SELECT COALESCE(SUM(fa.base), 0) FROM FichaAptidao fa WHERE fa.ficha.id = :fichaId")
int somarBaseAptidoes(@Param("fichaId") Long fichaId);

// Em FichaVantagemRepository
@Query("SELECT COALESCE(SUM(fv.custoPago), 0) FROM FichaVantagem fv WHERE fv.ficha.id = :fichaId")
int somarCustoPagoVantagens(@Param("fichaId") Long fichaId);
```

### 5. Calcular em `FichaResumoService`

No metodo `getResumo()` do `FichaResumoService`, adicionar o calculo dos pontos disponiveis antes de construir o response:

```java
// Pontos ganhos
int pontosAtributoGanhos = nivelConfigRepository.somarPontosAtributo(jogoId, ficha.getNivel());
int pontosAptidaoGanhos = nivelConfigRepository.somarPontosAptidao(jogoId, ficha.getNivel());
int pontosVantagemGanhos = pontosVantagemConfigRepository.somarPontosVantagem(jogoId, ficha.getNivel());

// Pontos gastos
int pontosAtributoGastos = fichaAtributoRepository.somarBaseAtributos(fichaId);
int pontosAptidaoGastos = fichaAptidaoRepository.somarBaseAptidoes(fichaId);
int pontosVantagemGastos = fichaVantagemRepository.somarCustoPagoVantagens(fichaId);

// Disponiveis (minimo 0)
int pontosAtributoDisponiveis = Math.max(0, pontosAtributoGanhos - pontosAtributoGastos);
int pontosAptidaoDisponiveis = Math.max(0, pontosAptidaoGanhos - pontosAptidaoGastos);
int pontosVantagemDisponiveis = Math.max(0, pontosVantagemGanhos - pontosVantagemGastos);
```

---

## Testes Obrigatorios

| Cenario | Given | When | Then |
|---------|-------|------|------|
| Nivel 1 sem distribuicao | Ficha nivel 1, NivelConfig nivel 1: pontosAtributo=15 | `getResumo()` | `pontosAtributoDisponiveis=15` |
| Nivel 1 com distribuicao parcial | Ficha nivel 1, 15 pontos ganhos, 10 distribuidos | `getResumo()` | `pontosAtributoDisponiveis=5` |
| Nivel 1 todos distribuidos | Ficha nivel 1, 15 pontos ganhos, 15 distribuidos | `getResumo()` | `pontosAtributoDisponiveis=0` |
| Nivel 5, acumulo de niveis | Ficha nivel 5, 3 pts/nivel = 15 ganhos, 12 gastos | `getResumo()` | `pontosAtributoDisponiveis=3` |
| Pontos de vantagem zero quando sem PontosVantagemConfig | Ficha nivel 1, sem PontosVantagemConfig cadastrado | `getResumo()` | `pontosVantagemDisponiveis=0` |
| Pontos nunca negativos | Ficha com mais gasto que ganho (config errada) | `getResumo()` | `pontosAtributoDisponiveis=0` (nao negativo) |
| Resposta inclui novos campos | Qualquer ficha | `GET /fichas/{id}/resumo` | JSON inclui os 3 novos campos |

---

## Criterios de Aceitacao

- [ ] `FichaResumoResponse` inclui `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis`, `pontosVantagemDisponiveis`
- [ ] Calculos corretos para nivel inicial (nivel 1, sem XP anterior)
- [ ] Calculos corretos para nivel 5 (acumulo de pontos dos niveis anteriores)
- [ ] Pontos disponiveis nunca negativos
- [ ] `GET /fichas/{id}/resumo` retorna os novos campos no JSON
- [ ] Testes existentes que constroem `FichaResumoResponse` atualizados para incluir os novos campos
- [ ] Nenhuma regressao em testes existentes

---

## Pontos de Atencao

- Verificar se `FichaAptidao.base` e o campo correto a ser somado (nao `sorte` nem `classe`). O `base` e a parte editavel pelo jogador.
- `FichaVantagem.custoPago` deve existir como campo na entidade. Verificar antes de implementar a query.
- O campo `pontosAptidao` pode ser nulo em alguns `NivelConfig` (conforme o model, esta como `@Min(0), default 3`, mas verificar se e `@NotNull`). Usar `COALESCE` nas queries para tratar nulos.
