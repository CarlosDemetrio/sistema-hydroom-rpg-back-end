# T5 — Backend: FichaResumoResponse com Pontos Disponíveis

> Spec: 012 | Fase: 1 | Tipo: Backend | Prioridade: CRITICO
> Depende de: nada
> Bloqueia: T6 (modelo TypeScript), T7, T8, T9, T10, T11

---

## Objetivo

Adicionar 3 campos ao `FichaResumoResponse` para que o frontend saiba quantos pontos o personagem tem disponíveis para distribuir. Esses campos são a base de todo o fluxo de level up no frontend.

## Problema

`FichaResumoResponse.java` atualmente não inclui:
- `pontosAtributoDisponiveis`
- `pontosAptidaoDisponiveis`
- `pontosVantagemDisponiveis`

Sem esses campos, o frontend não pode exibir o badge de pontos pendentes, habilitar o wizard de level up, nem controlar o saldo de vantagens. O campo `pontosVantagemRestantes` em `FichaVantagensTabComponent` está mockado como 0 por esta razão.

## Regra de Cálculo (RESOLVIDO pelo PO — 2026-04-03)

`pontosAtributoDisponiveis` = **soma de TODAS as fontes configuradas** menos pontos já gastos.

**Fontes de pontos de atributo (MVP):**
- `NivelConfig.pontosAtributo` para cada nível alcançado até o nível atual da ficha
- ~~`ClassePersonagem` — pontos extras por classe~~ Ausente no modelo atual. Documentado como GAP-PONTOS-CONFIG (pós-MVP). Q14 2026-04-03.
- ~~`Raca` — pontos extras por raça~~ Ausente no modelo atual. Mesmo gap.
- ~~`VantagemEfeito` BONUS_ATRIBUTO~~ Não integrado no MVP. Segunda passagem em Spec 007.

**Fontes de pontos de aptidão (MVP):**
- `NivelConfig.pontosAptidao` para cada nível alcançado até o nível atual da ficha

**Pontos de atributo gastos:** `SUM(FichaAtributo.nivel)` — inclui edições diretas do Mestre.

**Pontos de aptidão gastos:** `SUM(FichaAptidao.base)` — decisão do PO 2026-04-03 (Q15). O sistema não distingue valor inicial de criação vs incrementos de level up.

**Para pontos de vantagem (MVP):**
- Fontes: `PontosVantagemConfig` por nível alcançado (vinculado ao Jogo)
- Gastos: `SUM(FichaVantagem.custoPago)` — excluindo Insólitus onde `custoPago = 0`
- ~~Bonus de raça/classe em pontos de vantagem~~ Ausente no modelo atual. Mesmo gap pós-MVP.

## Arquivos Afetados (Backend)

- `src/main/java/.../dto/response/FichaResumoResponse.java` — adicionar 3 campos
- `src/main/java/.../service/FichaService.java` — implementar cálculo
- `src/main/java/.../service/FichaResumoService.java` (se existir separado) — idem

## Passos

### 1. Atualizar FichaResumoResponse.java

```java
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

### 2. Implementar cálculo em FichaService

Ao montar `FichaResumoResponse`:

```java
// Pontos de atributo ganhos por nível
int pontosAtributoGanhos = nivelConfigRepository
    .findByJogoIdAndNivelLessThanEqual(ficha.getJogo().getId(), fichaAtual.getNivel())
    .stream()
    .mapToInt(NivelConfig::getPontosAtributo)
    .sum();

// Pontos de atributo gastos (campo nivel de cada FichaAtributo)
int pontosAtributoGastos = fichaAtributoRepository
    .findByFichaId(fichaId)
    .stream()
    .mapToInt(FichaAtributo::getNivel)
    .sum();

int pontosAtributoDisponiveis = Math.max(0, pontosAtributoGanhos - pontosAtributoGastos);

// Pontos de aptidão ganhos por nível
int pontosAptidaoGanhos = nivelConfigRepository
    .findByJogoIdAndNivelLessThanEqual(ficha.getJogo().getId(), fichaAtual.getNivel())
    .stream()
    .mapToInt(NivelConfig::getPontosAptidao)
    .sum();

// Pontos de aptidão gastos = SUM(FichaAptidao.base) — decisão do PO 2026-04-03 (Q15)
// O sistema não distingue valor inicial de criação vs incrementos de level up.
// SUM(FichaAptidao.base) é o total gasto, ponto final.
int pontosAptidaoGastos = fichaAptidaoRepository
    .findByFichaId(fichaId)
    .stream()
    .mapToInt(FichaAptidao::getBase)
    .sum();

int pontosAptidaoDisponiveis = Math.max(0, pontosAptidaoGanhos - pontosAptidaoGastos);

// Pontos de vantagem
int pontosVantagemGanhos = pontosVantagemConfigRepository
    .findByJogoIdAndNivelLessThanEqual(ficha.getJogo().getId(), fichaAtual.getNivel())
    .stream()
    .mapToInt(PontosVantagemConfig::getPontosGanhos)
    .sum();

int pontosVantagemGastos = fichaVantagemRepository
    .findByFichaId(fichaId)
    .stream()
    .mapToInt(FichaVantagem::getCustoPago)
    .sum();

int pontosVantagemDisponiveis = Math.max(0, pontosVantagemGanhos - pontosVantagemGastos);
```

### 3. Atualizar testes de integração

Verificar testes de `FichaService` ou `FichaController` que testam o endpoint `GET /fichas/{id}/resumo` e atualizar para incluir os 3 novos campos.

## Critérios de Aceitação

- [ ] `FichaResumoResponse` tem os 3 novos campos
- [ ] `GET /api/v1/fichas/{id}/resumo` retorna os 3 campos calculados corretamente
- [ ] Ficha sem nenhum nível configurado: todos os campos retornam 0 (sem NPE)
- [ ] Ficha no nível 5 com NivelConfig configurado: `pontosAtributoDisponiveis` = ganhos - gastos
- [ ] Pontos de vantagem: `pontosVantagemDisponiveis` = saldo correto após compras
- [ ] Testes existentes de FichaResumo ainda passam (sem regressão)
- [ ] `pontosAptidaoDisponiveis` = `SUM(NivelConfig.pontosAptidao até nível atual)` - `SUM(FichaAptidao.base)` (decisão PO 2026-04-03)
- [ ] Ficha no nível 5 com aptidões distribuídas: `pontosAptidaoDisponiveis` = ganhos - `SUM(base)` correto

## Pontos em Aberto

- Verificar se `NivelConfigRepository` precisa de novo método `findByJogoIdAndNivelLessThanEqual` ou se o existente já cobre.
- `ClassePersonagem` e `Raca` **não têm** campos de pontos de atributo/aptidão/vantagem extras por nível no modelo atual. A funcionalidade descrita pelo PO na Q14 ("liberar pontos extras ao atingir certo nível por classe/raça") foi documentada como **GAP-PONTOS-CONFIG** em `docs/analises/INTEGRACAO-CONFIG-FICHA.md`. Na T5 MVP, as fontes de pontos são apenas `NivelConfig`. Pontos de classe/raça por nível serão tratados quando o GAP-PONTOS-CONFIG for resolvido.
