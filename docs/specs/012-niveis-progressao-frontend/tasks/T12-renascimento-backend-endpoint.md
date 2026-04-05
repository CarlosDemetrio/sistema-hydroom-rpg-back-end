# T12 — Backend: Endpoint POST /fichas/{id}/renascer

> Spec: 012 | Fase: 4 | Tipo: Backend | Prioridade: MEDIO
> Depende de: nada
> Bloqueia: T13 (UI de renascimento)

---

## Objetivo

Criar o endpoint dedicado `POST /api/v1/fichas/{id}/renascer` que executa a lógica completa de renascimento: reset de nível e XP, incremento do contador de renascimentos, manutenção das vantagens e bônus permanentes.

## Contexto

Atualmente `PUT /api/v1/fichas/{id}` aceita `renascimentos` no body via `UpdateFichaRequest`, mas isso apenas atualiza o contador — não executa o reset de nível e XP. O renascimento precisa de um endpoint dedicado para encapsular a transação completa.

## Questão Arquitetural Crítica (P-01)

Antes de implementar, o PO precisa confirmar:
- Após renascer, o nível vai para **0** ou para **1**?
- O XP vai para **0** ou para o XP mínimo do nível 1?
- Quais dados são mantidos: apenas `renascimentos++`, vantagens compradas (`FichaVantagem`), e valores de `FichaEssencia.renascimentos`/`FichaAmeaca.renascimentos`?
- Os pontos de atributo e aptidão (campos `nivel` de `FichaAtributo` e `base` de `FichaAptidao`) são resetados?

**Hipótese padrão baseada no Klayrah RPG:**
- Nível resetado para 1 (não 0)
- XP resetado para XP mínimo do nível 1 (ou 0 se nível 1 tem xpNecessaria=0)
- `Ficha.renascimentos++`
- `FichaEssencia.renascimentos++` e `FichaAmeaca.renascimentos++`
- `FichaVantagem`: mantidos (vantagens compradas não são perdidas)
- `FichaAtributo.nivel`: resetado para 0 (o Jogador redistribui no próximo level up)
- `FichaAptidao.base`: resetado para 0

## Arquivos Afetados (Backend)

- `src/main/java/.../controller/FichaController.java` — novo endpoint
- `src/main/java/.../service/FichaService.java` — método `renascer(fichaId)`
- `src/main/java/.../dto/request/RenascerRequest.java` — criar (se necessário)

## Passos

### 1. RenascerRequest.java (opcional)

```java
public record RenascerRequest(
    @NotNull Boolean confirmado   // campo obrigatório para confirmar intenção explícita
) {}
```

### 2. FichaController.java — endpoint

```java
@PostMapping("/api/v1/fichas/{id}/renascer")
@PreAuthorize("hasRole('MESTRE')")
@Operation(summary = "Executa renascimento do personagem (apenas MESTRE)")
public ResponseEntity<FichaResponse> renascer(
        @PathVariable Long id,
        @RequestBody @Valid RenascerRequest request,
        Principal principal) {
    if (!Boolean.TRUE.equals(request.confirmado())) {
        return ResponseEntity.badRequest().build();
    }
    FichaResponse response = mapper.toResponse(fichaService.renascer(id));
    return ResponseEntity.ok(response);
}
```

### 3. FichaService.java — método renascer()

```java
@Transactional
public Ficha renascer(Long fichaId) {
    Ficha ficha = fichaRepository.findById(fichaId)
        .orElseThrow(() -> new EntidadeNaoEncontradaException("Ficha", fichaId));

    // Validação: nível atual deve ter permitirRenascimento = true
    NivelConfig nivelAtualConfig = nivelConfigRepository
        .findByJogoIdAndNivel(ficha.getJogo().getId(), ficha.getNivel())
        .orElseThrow(() -> new RegraDeNegocioException("Configuração de nível não encontrada para renascimento"));
    if (!nivelAtualConfig.isPermitirRenascimento()) {
        throw new RegraDeNegocioException("O personagem não está em um nível que permite renascimento");
    }

    // Reset de nível e XP
    NivelConfig nivel1 = nivelConfigRepository
        .findByJogoIdAndNivel(ficha.getJogo().getId(), 1)
        .orElseThrow(() -> new RegraDeNegocioException("Nível 1 não configurado — impossível renascer"));
    ficha.setNivel(1);
    ficha.setXp(nivel1.getXpNecessaria());  // ou 0 — confirmar com PO

    // Incrementar contadores de renascimento
    ficha.setRenascimentos(ficha.getRenascimentos() + 1);
    // FichaEssencia e FichaAmeaca têm campos renascimentos próprios — incrementar também

    // Reset de atributos de nível (campo nivel de cada FichaAtributo)
    fichaAtributoRepository.findByFichaId(fichaId).forEach(attr -> {
        attr.setNivel(0);
        fichaAtributoRepository.save(attr);
    });

    // Reset de aptidões de base (campo base de cada FichaAptidao) — CONFIRMAR COM PO
    // fichaAptidaoRepository.findByFichaId(fichaId).forEach(apt -> {
    //     apt.setBase(0);
    // });

    return fichaRepository.save(ficha);
}
```

### 4. Testes de integração

Criar teste em `FichaServiceIntegrationTest`:
- Cenário feliz: ficha no nível 31 com `permitirRenascimento=true` → nível resetado, contador incrementado
- Cenário exceção: ficha no nível 20 com `permitirRenascimento=false` → erro `RegraDeNegocioException`
- Cenário exceção: nível 1 não configurado → erro

## Critérios de Aceitação

- [ ] Endpoint `POST /api/v1/fichas/{id}/renascer` criado com role MESTRE
- [ ] Validação de `permitirRenascimento` no nível atual
- [ ] Reset de nível e XP executado na transação
- [ ] `Ficha.renascimentos` incrementado
- [ ] Comportamento de FichaAtributo, FichaAptidao e FichaEssencia/FichaAmeaca documentado e implementado conforme decisão do PO
- [ ] Endpoint retorna `FichaResponse` atualizada
- [ ] Testes de integração cobrindo cenário feliz e exceções
- [ ] Swagger documenta o endpoint

## Pontos em Aberto

- **P-01:** Comportamento exato do reset de nível/XP/atributos/aptidões — **aguarda confirmação do PO antes de implementar**
