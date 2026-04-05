# T7 — Insolitus: campo tipoVantagem + endpoint de concessao

> Fase: Backend | Dependencias: T1 | Bloqueia: T8, T12
> Estimativa: 3–4 horas
> **BLOQUEADO por PA-001 e PA-002 — confirmar com PO e Tech Lead antes de iniciar**

---

## Objetivo

Implementar Insolitus como variante de `VantagemConfig` usando um campo discriminador `tipoVantagem`, e criar o endpoint de concessao que permite ao Mestre atribuir um Insolitus a uma ficha diretamente, sem custo de pontos.

---

## Contexto

Insolitus e um traco especial concedido pelo Mestre livremente. Ele e identico a uma vantagem mecanicamente (pode ter todos os 8 tipos de efeito), mas difere em:
1. Como e obtido: concessao do Mestre, nao compra com pontos
2. Custo: sempre 0, independente da `formulaCusto`
3. Pre-requisitos: ignorados — o Mestre concede livremente
4. Reversibilidade: pode ser removido pelo Mestre (PA-001: confirmar)

---

## Pontos em Aberto que BLOQUEIAM esta task

| ID | Questao | Necessario antes de |
|----|---------|-------------------|
| PA-001 | FichaInsolitus pode ser removida pelo Mestre? Ou segue a regra de FichaVantagem (nunca remove)? | Endpoint DELETE em FichaService/controller |
| PA-002 | Enum `TipoVantagem` (VANTAGEM/INSOLITUS) vs campo boolean `isInsolitus`? | Modelo de dados e migration |

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `model/enums/TipoVantagem.java` | Novo enum (se PA-002 decidir pelo enum) |
| `model/VantagemConfig.java` | Adicionar campo `tipoVantagem` (ou `isInsolitus`) |
| `dto/request/configuracao/CreateVantagemRequest.java` | Campo `tipoVantagem` opcional, default VANTAGEM |
| `dto/response/configuracao/VantagemResponse.java` | Incluir `tipoVantagem` na resposta |
| `service/configuracao/VantagemConfiguracaoService.java` | Filtros por tipoVantagem; formulaCusto = "0" para INSOLITUS |
| `controller/FichaController.java` | Novo endpoint `POST /fichas/{id}/insolitus` |
| `service/FichaService.java` | Metodo `concederInsolitus()` |

---

## Passos de Implementacao

### Passo 1 — Enum TipoVantagem (se PA-002 = enum)

```java
// model/enums/TipoVantagem.java
public enum TipoVantagem {
    VANTAGEM,   // comprada com pontos — comportamento padrao
    INSOLITUS   // concedida pelo Mestre — sem custo, sem pre-requisitos
}
```

### Passo 2 — Adicionar campo em VantagemConfig

```java
// Após campo formulaCusto em VantagemConfig:
@Enumerated(EnumType.STRING)
@Column(name = "tipo_vantagem", nullable = false, length = 20)
@Builder.Default
private TipoVantagem tipoVantagem = TipoVantagem.VANTAGEM;

// Campo de visibilidade: Insolitus pode ser oculto do jogador
@Builder.Default
@Column(name = "visivel_para_jogador", nullable = false)
private Boolean visivelParaJogador = true;
```

### Passo 3 — VantagemConfiguracaoService: validar Insolitus

Quando `tipoVantagem == INSOLITUS`, o service deve:
- Aceitar `formulaCusto` nulo/vazio e normalizar para `"0"`
- Nao validar sigla cross-entity (Insolitus nao usa sigla em formulas)
- Opcional: avisar se Mestre tentar definir pre-requisitos para um Insolitus (ignorados em runtime)

```java
@Override
protected void validarAntesCriar(VantagemConfig config) {
    super.validarAntesCriar(config); // unique nome+jogo
    if (config.getTipoVantagem() == TipoVantagem.INSOLITUS) {
        config.setFormulaCusto("0"); // normalizar
    }
}
```

### Passo 4 — DTO de concessao de Insolitus

```java
// dto/request/ConcederInsolitusRequest.java
public record ConcederInsolitusRequest(
    @NotNull Long insolitusConfigId,       // deve ser VantagemConfig com tipoVantagem=INSOLITUS
    @NotNull @Min(1) Integer nivel,        // nivel a conceder (default 1)
    @Size(max = 500) String observacao     // nota do Mestre sobre a concessao (opcional)
) {}
```

### Passo 5 — Endpoint de concessao

```java
// Em FichaController:
@PostMapping("/{fichaId}/insolitus")
@PreAuthorize("hasRole('MESTRE')")
@Operation(summary = "Mestre concede Insolitus a uma ficha")
public ResponseEntity<FichaVantagemResponse> concederInsolitus(
        @PathVariable Long fichaId,
        @Valid @RequestBody ConcederInsolitusRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(fichaService.concederInsolitus(fichaId, request));
}

// GET para listar:
@GetMapping("/{fichaId}/insolitus")
@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
@Operation(summary = "Listar Insolitus de uma ficha")
public ResponseEntity<List<FichaVantagemResponse>> listarInsolitus(
        @PathVariable Long fichaId) {
    return ResponseEntity.ok(fichaService.listarInsolitus(fichaId));
}

// DELETE condicionado a PA-001:
@DeleteMapping("/{fichaId}/insolitus/{insolitusId}")
@PreAuthorize("hasRole('MESTRE')")
@Operation(summary = "Remover Insolitus de uma ficha (apenas MESTRE)")
public ResponseEntity<Void> removerInsolitus(
        @PathVariable Long fichaId,
        @PathVariable Long insolitusId) {
    fichaService.removerInsolitus(fichaId, insolitusId);
    return ResponseEntity.noContent().build();
}
```

### Passo 6 — FichaService.concederInsolitus()

```java
@Transactional
public FichaVantagemResponse concederInsolitus(Long fichaId, ConcederInsolitusRequest request) {
    Ficha ficha = fichaRepository.findById(fichaId)
        .orElseThrow(() -> new ResourceNotFoundException("Ficha", fichaId));

    VantagemConfig insolitus = vantagemRepository.findById(request.insolitusConfigId())
        .orElseThrow(() -> new ResourceNotFoundException("VantagemConfig", request.insolitusConfigId()));

    // Validar que e realmente um Insolitus
    if (insolitus.getTipoVantagem() != TipoVantagem.INSOLITUS) {
        throw new ValidationException(
            "A configuracao informada nao e um Insolitus — use POST /fichas/{id}/vantagens para vantagens normais"
        );
    }

    // Verificar se ja existe (unico por ficha_id + vantagem_config_id)
    fichaVantagemRepository.findByFichaIdAndVantagemConfigId(fichaId, insolitus.getId())
        .ifPresent(fv -> {
            throw new ConflictException("Este Insolitus ja foi concedido a esta ficha");
        });

    FichaVantagem fichaVantagem = FichaVantagem.builder()
        .ficha(ficha)
        .vantagemConfig(insolitus)
        .nivelAtual(request.nivel())
        .custoPago(0) // Insolitus e sempre gratuito
        .build();

    return mapper.toResponse(fichaVantagemRepository.save(fichaVantagem));
}
```

---

## Regras de Negocio

- **INSOLITUS nao consome pontos de vantagem** — `custoPago` sempre 0
- **Pre-requisitos de INSOLITUS sao ignorados** — o Mestre concede livremente
- **Efeitos mecanicos de INSOLITUS sao calculados da mesma forma** — o `FichaCalculationService` nao diferencia VANTAGEM de INSOLITUS ao processar efeitos
- **`visivelParaJogador`**: se false, o Insolitus aparece na lista do Mestre mas nao na do Jogador
- **Reversibilidade (PA-001):** Implementar endpoint DELETE apenas apos confirmacao com PO

---

## Diferencas de FichaVantagem para FichaInsolitus

| Aspecto | FichaVantagem | FichaInsolitus |
|---------|--------------|----------------|
| Rota | POST /fichas/{id}/vantagens | POST /fichas/{id}/insolitus |
| Custo pago | Calculado via formulaCusto | Sempre 0 |
| Pre-requisitos verificados | Sim | Nao |
| Nivel pode subir | Sim | Sim |
| Pode ser removido | Nao (regra permanente) | Sim (PA-001: confirmar com PO) |
| Armazenado em | ficha_vantagens | ficha_vantagens (mesma tabela, diferenciado pela VantagemConfig.tipoVantagem) |

---

## Criterios de Aceitacao

- [ ] `VantagemConfig` tem campo `tipoVantagem` (VANTAGEM default, INSOLITUS para Insolitus)
- [ ] `VantagemConfig` tem campo `visivelParaJogador` (boolean, default true)
- [ ] POST /configuracoes/vantagens com `tipoVantagem=INSOLITUS` persiste com `formulaCusto="0"`
- [ ] POST /fichas/{id}/insolitus concede o Insolitus com custoPago=0
- [ ] POST /fichas/{id}/insolitus retorna 422 se a config nao e um INSOLITUS
- [ ] POST /fichas/{id}/insolitus retorna 409 se o Insolitus ja foi concedido
- [ ] GET /fichas/{id}/insolitus lista apenas registros com VantagemConfig.tipoVantagem=INSOLITUS
- [ ] FichaCalculationService processa efeitos de Insolitus da mesma forma que vantagens
- [ ] `./mvnw test` passa
