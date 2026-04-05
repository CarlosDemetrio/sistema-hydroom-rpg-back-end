# T3 — FichaCalculationService: BONUS_VIDA e BONUS_ESSENCIA

> Fase: Backend | Dependencias: T1, T2 (assinatura de `aplicarEfeitosVantagens` definida) | Bloqueia: T8
> Estimativa: 1–2 horas

---

## Objetivo

Implementar no `FichaCalculationService` o processamento dos efeitos `BONUS_VIDA` e `BONUS_ESSENCIA`, acumulando os bonus nos campos `FichaVida.vt` e `FichaEssencia.vantagens` antes dos calculos de total.

---

## Contexto

Os campos `FichaVida.vt` e `FichaEssencia.vantagens` ja existem no modelo e ja sao somados nas formulas de calculo:

- `calcularVidaTotal()`: `vigorTotal + nivel + **vt** + renascimentos + outros`
- `calcularEssenciaTotal()`: `floor((VIG+SAB)/2) + nivel + renascimentos + **vantagens** + outros`

O problema: esses campos nao sao populados pelo motor — ficam como 0 ou valor manual. Esta task preenche a lacuna para as contribuicoes de vantagem.

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `service/FichaCalculationService.java` | Adicionar cases BONUS_VIDA e BONUS_ESSENCIA em `aplicarEfeitosVantagens()` |

---

## Passos de Implementacao

### Adicionar cases no switch de `aplicarEfeitosVantagens()`

```java
case BONUS_VIDA -> {
    int bonus = calcularValorEfeito(efeito, nivel);
    vida.setVt(vida.getVt() + bonus);
}

case BONUS_ESSENCIA -> {
    int bonus = calcularValorEfeito(efeito, nivel);
    essencia.setVantagens(essencia.getVantagens() + bonus);
}
```

Esses casos sao os mais simples: nao ha FK de alvo para resolver, o valor vai diretamente para o campo correspondente.

---

## Regras de Negocio

- **RN-007:** Os campos `vt` e `vantagens` sao zerados em `zerarContribuicoesVantagens()` antes de recalcular — ja implementado em T2
- **BONUS_VIDA:** Afeta o pool global de vida, que por sua vez distribui para membros proporcionalmente (o `calcularVidaMembro()` usa `vidaTotal` que inclui `vt`)
- **BONUS_ESSENCIA:** Nao existe FK de alvo — nenhuma validacao de alvo necessaria
- Efeitos com `deleted_at != null` ja sao ignorados pelo filtro implementado em T2

---

## Exemplo de Calculo

Cenario: Vantagem "Saude de Ferro" com efeito BONUS_VIDA, valorPorNivel=5
- FichaVantagem nivelAtual=4
- Antes: `FichaVida.vt = 0` (apos zerarContribuicoes)
- `calcularValorEfeito(efeito, 4) = 0 + 5 * 4 = 20`
- Apos BONUS_VIDA: `FichaVida.vt = 20`
- Apos `calcularVidaTotal(ficha, vida, vigorTotal=30)`:
  - `vidaTotal = 30 (VIG) + 5 (nivel) + 20 (vt) + 0 (renascimentos) + 0 (outros) = 55`

---

## Criterios de Aceitacao

- [ ] BONUS_VIDA: `FichaVida.vt` acumula corretamente multiplas vantagens com efeito de vida
- [ ] BONUS_ESSENCIA: `FichaEssencia.vantagens` acumula corretamente
- [ ] Efeitos com soft delete sao ignorados
- [ ] BONUS_VIDA propaga corretamente para o calculo de vida total (vidaTotal aumenta)
- [ ] BONUS_ESSENCIA propaga corretamente para o calculo de essencia total
- [ ] `./mvnw test` passa
