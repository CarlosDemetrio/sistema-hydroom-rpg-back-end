# T4 — FichaCalculationService: BONUS_DERIVADO e BONUS_VIDA_MEMBRO

> Fase: Backend | Dependencias: T1, T2 | Bloqueia: T8
> Estimativa: 2 horas

---

## Objetivo

Implementar no `FichaCalculationService` o processamento dos efeitos `BONUS_DERIVADO` e `BONUS_VIDA_MEMBRO`, acumulando bonus nos campos `FichaBonus.vantagens` e `FichaVidaMembro.bonusVantagens` e atualizando o calculo de vida por membro.

---

## Contexto

- `FichaBonus.vantagens` ja existe no modelo e ja e somado em `recalcularTotal()` — mas nunca foi populado pelo motor
- `FichaVidaMembro.bonusVantagens` foi adicionado em T1 — precisa ser somado ao calculo de `calcularVidaMembro()`
- `BONUS_DERIVADO` e um dos casos de uso mais comuns no dominio Klayrah (ex: TCO adiciona +N em B.B.A)

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `service/FichaCalculationService.java` | Adicionar cases BONUS_DERIVADO e BONUS_VIDA_MEMBRO; atualizar `calcularVidaMembro()` |

---

## Passos de Implementacao

### Passo 1 — Adicionar mapa de FichaBonus no metodo aplicarEfeitosVantagens

O mapa de bonus deve ser construido junto com os outros mapas de lookup:

```java
Map<Long, FichaBonus> bonusMap = bonus.stream()
    .filter(b -> b.getBonusConfig() != null)
    .collect(Collectors.toMap(b -> b.getBonusConfig().getId(), b -> b));

Map<Long, FichaVidaMembro> membrosMap = membros.stream()
    .filter(m -> m.getMembroCorpoConfig() != null)
    .collect(Collectors.toMap(m -> m.getMembroCorpoConfig().getId(), m -> m));
```

### Passo 2 — Adicionar case BONUS_DERIVADO no switch

```java
case BONUS_DERIVADO -> {
    if (efeito.getBonusAlvo() == null) {
        log.warn("BONUS_DERIVADO sem bonusAlvo — efeito ID {}", efeito.getId());
        break;
    }
    FichaBonus alvo = bonusMap.get(efeito.getBonusAlvo().getId());
    if (alvo != null) {
        alvo.setVantagens(alvo.getVantagens() + calcularValorEfeito(efeito, nivel));
    } else {
        log.warn("BONUS_DERIVADO: FichaBonus nao encontrada para BonusConfig ID {}",
            efeito.getBonusAlvo().getId());
    }
}
```

### Passo 3 — Adicionar case BONUS_VIDA_MEMBRO no switch

```java
case BONUS_VIDA_MEMBRO -> {
    if (efeito.getMembroAlvo() == null) {
        log.warn("BONUS_VIDA_MEMBRO sem membroAlvo — efeito ID {}", efeito.getId());
        break;
    }
    FichaVidaMembro alvo = membrosMap.get(efeito.getMembroAlvo().getId());
    if (alvo != null) {
        alvo.setBonusVantagens(alvo.getBonusVantagens() + calcularValorEfeito(efeito, nivel));
    } else {
        log.warn("BONUS_VIDA_MEMBRO: FichaVidaMembro nao encontrada para MembroCorpoConfig ID {}",
            efeito.getMembroAlvo().getId());
    }
}
```

### Passo 4 — Atualizar `calcularVidaMembro()` para incluir `bonusVantagens`

```java
public int calcularVidaMembro(FichaVidaMembro membro, int vidaTotal, BigDecimal porcentagem) {
    if (porcentagem == null) {
        membro.setVida(0);
        return 0;
    }
    BigDecimal resultado = BigDecimal.valueOf(vidaTotal).multiply(porcentagem)
            .setScale(0, RoundingMode.FLOOR);
    int vidaProporcional = resultado.intValue();
    int bonusVantagens = membro.getBonusVantagens() != null ? membro.getBonusVantagens() : 0;
    int vida = vidaProporcional + bonusVantagens;
    membro.setVida(vida);
    return vida;
}
```

**Atencao de ordem:** `aplicarEfeitosVantagens()` popula `bonusVantagens`. Depois, `recalcularEstado()` chama `calcularVidaMembro()` que usa `bonusVantagens`. A ordem em `recalcular()` garante isso:
```
aplicarEfeitosVantagens() → ... → recalcularEstado()
```

---

## Regras de Negocio

- **BONUS_DERIVADO vs BONUS_VIDA_MEMBRO:** BONUS_DERIVADO afeta bonus de combate (B.B.A., B.B.M., etc.); BONUS_VIDA_MEMBRO afeta pontos de vida de um membro especifico
- **BONUS_VIDA_MEMBRO nao altera o pool global:** O pool (`FichaVida.vidaTotal`) nao muda — apenas o membro especifico tem vida extra
- **Multiplos efeitos:** Uma ficha pode ter varias vantagens com BONUS_DERIVADO para o mesmo bonus — todos acumulam em `vantagens`
- **BONUS_DERIVADO e resetado:** `FichaBonus.vantagens = 0` e feito em `zerarContribuicoesVantagens()` (T2) — sem dupla contagem

---

## Diferenca critica: BONUS_VIDA vs BONUS_VIDA_MEMBRO

| Efeito | Campo alterado | Propaga para membros? | Exemplo |
|--------|---------------|----------------------|---------|
| BONUS_VIDA | FichaVida.vt | Sim (vidaTotal cresce, membros recalculam) | +10 VT — 10% vai para braco esquerdo |
| BONUS_VIDA_MEMBRO | FichaVidaMembro.bonusVantagens | Nao (direto no membro) | +15 direto na cabeca, sem afetar pool |

---

## Criterios de Aceitacao

- [ ] BONUS_DERIVADO: `FichaBonus.vantagens` acumula corretamente para nivel 1, 5, 10
- [ ] BONUS_DERIVADO: `FichaBonus.total` inclui `vantagens` no calculo final
- [ ] BONUS_VIDA_MEMBRO: `FichaVidaMembro.bonusVantagens` acumula corretamente
- [ ] BONUS_VIDA_MEMBRO: `FichaVidaMembro.vida` = `floor(vidaTotal * porcentagem) + bonusVantagens`
- [ ] BONUS_VIDA_MEMBRO nao altera `FichaVida.vidaTotal`
- [ ] Efeitos com soft delete sao ignorados
- [ ] `./mvnw test` passa
