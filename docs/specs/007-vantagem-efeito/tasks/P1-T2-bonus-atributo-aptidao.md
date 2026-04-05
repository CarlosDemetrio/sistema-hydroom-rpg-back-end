# T2 — FichaCalculationService: BONUS_ATRIBUTO e BONUS_APTIDAO

> Fase: Backend | Dependencias: T1 | Bloqueia: T8
> Estimativa: 2–3 horas

---

## Objetivo

Implementar no `FichaCalculationService` o processamento dos efeitos `BONUS_ATRIBUTO` e `BONUS_APTIDAO`, garantindo que os campos `outros` de `FichaAtributo` e `FichaAptidao` recebam os bonus corretos antes dos calculos de total.

---

## Contexto

Apos T1:
- `FichaAtributo.outros` existe mas nao recebe bonus de vantagem
- `FichaAptidao.outros` existe mas nao recebe bonus de vantagem
- `FichaCalculationService.aplicarEfeitosVantagens()` e um stub vazio

O metodo `recalcular()` ja chama `aplicarEfeitosVantagens()` na etapa 0, antes de `recalcularAtributos()`. A ordem e critica: os efeitos de vantagem nos atributos devem ser populados ANTES de calcular os totais dos atributos.

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `service/FichaCalculationService.java` | Implementar BONUS_ATRIBUTO e BONUS_APTIDAO em `aplicarEfeitosVantagens()` |

---

## Passos de Implementacao

### Passo 1 — Metodo auxiliar `calcularValorEfeito`

```java
/**
 * Calcula o valor numerico de um efeito para um dado nivel de vantagem.
 * Formula: (valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelVantagem
 */
private int calcularValorEfeito(VantagemEfeito efeito, int nivelVantagem) {
    double valorFixo = efeito.getValorFixo() != null
        ? efeito.getValorFixo().doubleValue() : 0.0;
    double valorPorNivel = efeito.getValorPorNivel() != null
        ? efeito.getValorPorNivel().doubleValue() : 0.0;
    return (int) Math.round(valorFixo + valorPorNivel * nivelVantagem);
}
```

### Passo 2 — Metodo `zerarContribuicoesVantagens`

Antes de somar os novos bonus, zerar os campos que sao de responsabilidade exclusiva do motor de vantagens:

```java
/**
 * Zera todos os campos que serao recalculados a partir de efeitos de vantagem.
 * Chamado no inicio de aplicarEfeitosVantagens() para garantir idempotencia.
 */
private void zerarContribuicoesVantagens(
        List<FichaAtributo> atributos,
        List<FichaBonus> bonus,
        FichaVida vida,
        List<FichaVidaMembro> membros,
        FichaEssencia essencia) {

    atributos.forEach(a -> a.setOutros(0));
    bonus.forEach(b -> b.setVantagens(0));
    vida.setVt(0);
    membros.forEach(m -> m.setBonusVantagens(0));
    essencia.setVantagens(0);
}
```

### Passo 3 — Implementar BONUS_ATRIBUTO em `aplicarEfeitosVantagens`

```java
private void aplicarEfeitosVantagens(
        List<FichaVantagem> vantagens,
        List<FichaAtributo> atributos,
        List<FichaBonus> bonus,
        FichaVida vida,
        List<FichaVidaMembro> membros,
        FichaEssencia essencia) {

    zerarContribuicoesVantagens(atributos, bonus, vida, membros, essencia);

    // Mapas de lookup para evitar buscas O(n) dentro do loop
    Map<Long, FichaAtributo> atributosMap = atributos.stream()
        .filter(a -> a.getAtributoConfig() != null)
        .collect(Collectors.toMap(a -> a.getAtributoConfig().getId(), a -> a));

    Map<Long, FichaAptidao> aptidoesMap = /* passado via parametro — ver Passo 4 */;

    for (FichaVantagem fichaVantagem : vantagens) {
        if (fichaVantagem.getVantagemConfig() == null) continue;
        int nivel = fichaVantagem.getNivelAtual() != null ? fichaVantagem.getNivelAtual() : 1;

        for (VantagemEfeito efeito : fichaVantagem.getVantagemConfig().getEfeitos()) {
            if (efeito.getDeletedAt() != null) continue; // soft delete

            switch (efeito.getTipoEfeito()) {
                case BONUS_ATRIBUTO -> {
                    if (efeito.getAtributoAlvo() == null) {
                        log.warn("BONUS_ATRIBUTO sem atributoAlvo — efeito ID {}", efeito.getId());
                        break;
                    }
                    FichaAtributo alvo = atributosMap.get(efeito.getAtributoAlvo().getId());
                    if (alvo != null) {
                        alvo.setOutros(alvo.getOutros() + calcularValorEfeito(efeito, nivel));
                    } else {
                        log.warn("BONUS_ATRIBUTO: FichaAtributo nao encontrado para AtributoConfig ID {}",
                            efeito.getAtributoAlvo().getId());
                    }
                }
                case BONUS_APTIDAO -> {
                    // implementado abaixo
                }
                // outros tipos: T3, T4, T5, T6
                default -> { /* ignorado nesta task */ }
            }
        }
    }
}
```

### Passo 4 — Adicionar `List<FichaAptidao>` ao metodo

Para processar BONUS_APTIDAO, o metodo precisa receber aptidoes:

```java
// Assinatura atualizada:
private void aplicarEfeitosVantagens(
        List<FichaVantagem> vantagens,
        List<FichaAtributo> atributos,
        List<FichaAptidao> aptidoes,       // NOVO
        List<FichaBonus> bonus,
        FichaVida vida,
        List<FichaVidaMembro> membros,
        FichaEssencia essencia) { ... }

// Atualizar recalcular() para passar aptidoes:
public void recalcular(
        Ficha ficha,
        List<FichaAtributo> atributos,
        List<FichaAptidao> aptidoes,       // NOVO
        List<FichaBonus> bonus,
        FichaVida vida,
        List<FichaVidaMembro> membros,
        FichaEssencia essencia,
        FichaAmeaca ameaca,
        List<FichaVantagem> vantagens) { ... }
```

Implementar BONUS_APTIDAO:

```java
case BONUS_APTIDAO -> {
    if (efeito.getAptidaoAlvo() == null) {
        log.warn("BONUS_APTIDAO sem aptidaoAlvo — efeito ID {}", efeito.getId());
        break;
    }
    FichaAptidao alvo = aptidoesMap.get(efeito.getAptidaoAlvo().getId());
    if (alvo != null) {
        alvo.setOutros(alvo.getOutros() + calcularValorEfeito(efeito, nivel));
    } else {
        log.warn("BONUS_APTIDAO: FichaAptidao nao encontrada para AptidaoConfig ID {}",
            efeito.getAptidaoAlvo().getId());
    }
}
```

Tambem atualizar `zerarContribuicoesVantagens()` para resetar `aptidoes`:
```java
aptidoes.forEach(a -> a.setOutros(0));
```

---

## Regras de Negocio

- **RN-001:** Efeitos com `deleted_at != null` sao ignorados no calculo
- **RN-007:** O motor zera e recalcula TODOS os campos a cada passagem (idempotente)
- Se a `FichaVantagem` nao tem `VantagemConfig` carregado: `log.warn` e pular (nao lancar excecao)
- Se o atributo/aptidao alvo nao existe na ficha (config adicionada apos criacao da ficha): `log.warn` e pular
- Lookup por `atributoConfig.id` deve usar mapa construido antes do loop (nao iterar a lista toda dentro do loop)

---

## Testes

Os testes de integracao sao escritos em T8. Para verificacao local:

Cenario mental: Vantagem TCO com efeito BONUS_ATRIBUTO em FOR com valorPorNivel=2.
- FichaVantagem nivelAtual=3
- FichaAtributo.FOR: base=10, nivel=2, outros=0 antes do calculo
- Apos `aplicarEfeitosVantagens()`: FichaAtributo.FOR.outros = 6
- Apos `recalcularAtributos()`: FichaAtributo.FOR.total = 10 + 2 + 6 = 18

---

## Criterios de Aceitacao

- [ ] BONUS_ATRIBUTO: `FichaAtributo.outros` acumulado corretamente para nivel 1, 3, 10
- [ ] BONUS_APTIDAO: `FichaAptidao.outros` acumulado corretamente
- [ ] `FichaAptidao.total` inclui `outros` no calculo
- [ ] Efeitos com soft delete sao ignorados
- [ ] FichaAtributo/Aptidao sem correspondencia na ficha: apenas log.warn, sem excecao
- [ ] Lookup por ID usa mapa — sem iteracao O(n) dentro do loop de vantagens
- [ ] `./mvnw test` passa
