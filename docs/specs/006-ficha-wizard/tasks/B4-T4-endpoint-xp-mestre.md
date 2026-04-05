# T4 — Endpoint `PUT /fichas/{id}/xp` (MESTRE-only)

> Fase: Backend
> Complexidade: Media
> Prerequisito: Nenhum (independente, mas complementar a T3)
> Bloqueia: Frontend de concessao de XP (Spec 008)
> Estimativa: 3–4 horas

---

## Objetivo

Criar o endpoint dedicado `PUT /fichas/{id}/xp` para o Mestre conceder XP a uma ficha. A concessao e **aditiva** (soma ao XP atual), recalcula automaticamente o nivel, e retorna o `FichaResumoResponse` atualizado com os novos pontos disponiveis.

---

## Contexto

Atualmente nao existe um endpoint dedicado para concessao de XP. O fluxo desejado e:

1. Mestre concede XP com motivo opcional
2. Backend acumula XP (nunca substitui)
3. Backend recalcula o nivel usando `NivelConfig`
4. Backend recalcula pontos disponiveis (atributo, aptidao, vantagem)
5. Backend retorna `FichaResumoResponse` atualizado

**Regras criticas:**
- XP e acumulativo: `ficha.xp += request.xp`
- Nivel e recalculado: `MAX(nivelConfig.nivel WHERE xpNecessaria <= ficha.xp)`
- Nivel nunca desce (mesmo que haja bug de configuracao)
- Motivo e campo de texto livre opcional — **nao e persistido** no MVP (sem historico de XP)
- Level up automatico: se o nivel subiu, o `FichaResumoResponse` ja reflete os novos pontos disponiveis

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `dto/request/ConcessaoXpRequest.java` | Criar novo record |
| `service/FichaService.java` | Adicionar metodo `concederXp()` |
| `service/FichaCalculationService.java` | Verificar/adicionar metodo de calculo de nivel por XP |
| `controller/FichaController.java` | Adicionar endpoint PUT /fichas/{id}/xp |

---

## Passos de Implementacao

### 1. Criar `ConcessaoXpRequest`

```java
// dto/request/ConcessaoXpRequest.java
public record ConcessaoXpRequest(

    @NotNull(message = "XP e obrigatorio")
    @Min(value = 1, message = "XP deve ser pelo menos 1")
    Long xp,

    @Size(max = 500, message = "Motivo deve ter no maximo 500 caracteres")
    String motivo
) {}
```

### 2. Adicionar logica de calculo de nivel em `FichaCalculationService`

Verificar se ja existe metodo que calcula o nivel a partir do XP. Se nao:

```java
/**
 * Calcula o nivel de uma ficha baseado no XP acumulado e nos NivelConfigs do jogo.
 * Retorna o maior nivel cuja xpNecessaria <= xpAtual.
 * Se nao houver NivelConfig com xpNecessaria=0, retorna 1 como fallback.
 */
public int calcularNivel(Long xpAtual, List<NivelConfig> niveis) {
    return niveis.stream()
            .filter(n -> n.getXpNecessaria() <= xpAtual)
            .mapToInt(NivelConfig::getNivel)
            .max()
            .orElse(1);
}
```

### 3. Adicionar `concederXp()` em `FichaService`

```java
@Transactional
public FichaResumoResponse concederXp(Long fichaId, ConcessaoXpRequest request) {
    Ficha ficha = buscarPorId(fichaId);

    // 1. Verificar acesso (somente MESTRE)
    // (o endpoint ja tem @PreAuthorize, mas validar tambem no service)
    verificarAcessoMestre(ficha.getJogo().getId());

    // 2. Acumular XP
    long novoXp = ficha.getXp() + request.xp();
    ficha.setXp(novoXp);

    // 3. Recalcular nivel
    List<NivelConfig> niveis = nivelConfigRepository.findByJogoIdOrderByNivel(ficha.getJogo().getId());
    int novoNivel = fichaCalculationService.calcularNivel(novoXp, niveis);

    // Nivel nunca desce
    if (novoNivel > ficha.getNivel()) {
        ficha.setNivel(novoNivel);
        log.info("Ficha '{}' subiu para o nivel {} (XP: {})", ficha.getNome(), novoNivel, novoXp);
    }

    fichaRepository.save(ficha);

    // 4. Retornar resumo atualizado (inclui pontosDisponiveis — ver T5)
    return fichaResumoService.getResumo(fichaId);
}
```

### 4. Adicionar endpoint no `FichaController`

```java
@PutMapping("/api/v1/fichas/{id}/xp")
@PreAuthorize("hasRole('MESTRE')")
@Operation(
    summary = "Conceder XP a uma ficha (Apenas MESTRE)",
    description = "Adiciona XP ao total da ficha e recalcula o nivel automaticamente. " +
                  "Retorna o resumo atualizado com os novos pontos disponiveis."
)
public ResponseEntity<FichaResumoResponse> concederXp(
        @PathVariable Long id,
        @Valid @RequestBody ConcessaoXpRequest request) {
    var resumo = fichaService.concederXp(id, request);
    return ResponseEntity.ok(resumo);
}
```

---

## Testes Obrigatorios

| Cenario | Given | When | Then |
|---------|-------|------|------|
| Concessao basica de XP | Ficha com 0 XP, nivel 1 | `PUT /fichas/{id}/xp` com `{ xp: 1000 }` | XP = 1000, nivel recalculado, HTTP 200 |
| Level up automatico | Ficha com 4000 XP, nivel 4; NivelConfig nivel 5 = 5000 XP | `PUT /fichas/{id}/xp` com `{ xp: 1500 }` | XP = 5500, nivel = 5, pontosDisponiveis atualizados |
| Nivel nao desce | Ficha nivel 5; NivelConfig nivel 5 = 5000; XP atual = 5000 | `PUT /fichas/{id}/xp` com `{ xp: 100 }` | XP = 5100, nivel = 5 (nao desce para 4) |
| XP acumulativo (nao substitui) | Ficha com 3000 XP | `PUT /fichas/{id}/xp` com `{ xp: 2000 }` | XP total = 5000 |
| JOGADOR tenta conceder XP | JOGADOR logado | `PUT /fichas/{id}/xp` | HTTP 403 Forbidden |
| XP = 0 invalido | MESTRE | `PUT /fichas/{id}/xp` com `{ xp: 0 }` | HTTP 422 (min=1) |
| Motivo opcional | MESTRE | `PUT /fichas/{id}/xp` com `{ xp: 500 }` (sem motivo) | HTTP 200, xp adicionado |
| Motivo longo invalido | MESTRE | `PUT /fichas/{id}/xp` com motivo > 500 chars | HTTP 422 |

---

## Criterios de Aceitacao

- [ ] `PUT /fichas/{id}/xp` aceita apenas role MESTRE (HTTP 403 para JOGADOR)
- [ ] XP e acumulativo: envia 1000, ficha passa de 3000 para 4000
- [ ] Nivel e recalculado automaticamente apos cada concessao
- [ ] Nivel nunca desce mesmo com configuracoes inconsistentes
- [ ] Response e `FichaResumoResponse` com todos os campos (incluindo pontosDisponiveis — depende de T5)
- [ ] `motivo` e campo opcional — nao persistido
- [ ] `xp` minimo de 1 — XP=0 retorna 422
- [ ] Testes de integracao cobrindo todos os cenarios acima

---

## Observacoes

- O campo `motivo` nao e persistido no MVP. O PO confirmou que historico de XP e para uma spec futura. O campo existe apenas para log e contexto na resposta.
- Se o `FichaResumoResponse` ainda nao tiver `pontosDisponiveis` (T5 nao concluido), o endpoint ainda funciona mas retorna o resumo sem esses campos. Garantir que T5 seja implementado antes de T8/T9/T10 no frontend.
- O endpoint `PUT /jogos/{id}/fichas/xp-lote` (concessao em lote para todas as fichas) e futuramente — fora do escopo desta spec.
