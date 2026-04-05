# T4 — FichaItem: Entity, Endpoints e Servico de Inventario

> Fase: Backend — Ficha | Prioridade: P1
> Dependencias: T1 (raridade), T2 (ItemConfig)
> Bloqueia: T5 (calculo), T7 (testes), T11 (frontend)
> Estimativa: 2-3 dias

---

## Objetivo

Implementar `FichaItem` — a instancia de um item no inventario de um personagem. Inclui os endpoints para adicionar, equipar, desequipar, remover itens e decrementar durabilidade. O servico deve validar restricoes de raridade (role JOGADOR) e requisitos do item, e disparar recalculo da ficha ao equipar/desequipar.

---

## Entidade

```java
@Entity
@Table(name = "ficha_itens")
@Data @EqualsAndHashCode(callSuper = true)
@Builder @NoArgsConstructor @AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class FichaItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_config_id") // nullable — item customizado
    private ItemConfig itemConfig;

    @Column(nullable = false, length = 100)
    private String nome; // pode sobrepor itemConfig.nome

    @Column(nullable = false)
    private boolean equipado; // default false

    @Column
    private Integer duracaoAtual; // null = indestrutivel

    @Column(nullable = false)
    private int quantidade; // default 1

    @Column(precision = 5, scale = 2)
    private BigDecimal peso; // nullable — herda de itemConfig se null

    @Column(length = 500)
    private String notas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raridade_id") // nullable — herda de itemConfig se null
    private RaridadeItemConfig raridade;

    @Column(length = 100)
    private String adicionadoPor; // username do usuario que adicionou
}
```

---

## Arquivos a Criar

| Arquivo | Descricao |
|---------|-----------|
| `model/FichaItem.java` | Entidade |
| `repository/FichaItemRepository.java` | JPA repository com JOIN FETCH |
| `service/FichaItemService.java` | Logica completa de inventario |
| `dto/request/FichaItemAdicionarRequest.java` | Adicionar item do catalogo |
| `dto/request/FichaItemCustomizadoRequest.java` | Adicionar item sem itemConfig |
| `dto/request/FichaItemDuracaoRequest.java` | Decrementar/restaurar durabilidade |
| `dto/response/FichaItemResponse.java` | Response completo com efeitos |
| `dto/response/FichaInventarioResponse.java` | Response de listagem com peso total |
| `mapper/FichaItemMapper.java` | MapStruct |
| `controller/FichaItemController.java` | Controller REST |

---

## DTOs

### FichaItemAdicionarRequest

```java
public record FichaItemAdicionarRequest(
    @NotNull Long itemConfigId,
    @Min(1) int quantidade,        // default 1
    @Size(max = 500) String notas,
    boolean forcarAdicao           // se true e Mestre: pula validacao de requisitos
) {}
```

### FichaItemCustomizadoRequest

```java
public record FichaItemCustomizadoRequest(
    @NotBlank @Size(max = 100) String nome,
    @NotNull Long raridadeId,
    @NotNull @DecimalMin("0.00") BigDecimal peso,
    @Min(1) int quantidade,
    @Size(max = 500) String notas
) {}
```

### FichaItemDuracaoRequest

```java
public record FichaItemDuracaoRequest(
    @NotNull @Min(1) Integer decremento,  // valor a decrementar
    boolean restaurar                      // se true: restaura ao duracaoPadrao do ItemConfig
) {}
```

### FichaInventarioResponse

```java
public record FichaInventarioResponse(
    List<FichaItemResponse> equipados,    // itens com equipado=true
    List<FichaItemResponse> inventario,   // itens com equipado=false
    BigDecimal pesoTotal,                 // soma de todos os itens * quantidade
    BigDecimal capacidadeCarga,           // FOR.total * 3 (calculado)
    boolean sobrecarregado                // pesoTotal > capacidadeCarga
) {}
```

---

## Logica do FichaItemService

### adicionarItem()

```
1. Verificar que ficha pertence ao jogo do itemConfig
2. Verificar role do usuario:
   a. Se JOGADOR: verificar raridade.podeJogadorAdicionar == true → se false: lanca ForbiddenException
   b. Se MESTRE: sem restricao de raridade
3. Validar requisitos (se !forcarAdicao ou se JOGADOR):
   a. Para cada ItemRequisito do itemConfig: verificar contra ficha atual
   b. Se qualquer requisito falhar: lanca ValidationException com lista de requisitos faltantes
4. Criar FichaItem com:
   - nome = itemConfig.nome
   - equipado = false
   - duracaoAtual = itemConfig.duracaoPadrao
   - raridade = itemConfig.raridade
   - peso = itemConfig.peso
   - adicionadoPor = username do usuario autenticado
5. Salvar e retornar FichaItemResponse
```

### equiparItem()

```
1. Verificar ownership (JOGADOR so equipa propria ficha)
2. Verificar duracaoAtual != 0 → se 0: HTTP 422 "Item quebrado nao pode ser equipado"
3. Setar equipado = true
4. Salvar
5. Disparar fichaService.recalcular(fichaId) → sincronamente para resposta imediata
6. Retornar FichaItemResponse atualizado
```

### desequiparItem()

```
1. Verificar ownership
2. Setar equipado = false
3. Salvar
4. Disparar fichaService.recalcular(fichaId)
5. Retornar FichaItemResponse
```

### decrementarDurabilidade()

```
1. Somente MESTRE pode chamar
2. Se request.restaurar == true: duracaoAtual = itemConfig.duracaoPadrao (ou null se indestrutivel)
3. Se request.restaurar == false:
   a. duracaoAtual = max(0, duracaoAtual - decremento)
   b. Se duracaoAtual == 0 e item estava equipado: setar equipado = false
4. Salvar
5. Se duracaoAtual chegou a 0 e estava equipado: disparar fichaService.recalcular(fichaId)
6. Retornar FichaItemResponse
```

### removerItem()

```
1. Se JOGADOR:
   a. Verificar ownership da ficha
   b. Se item tem ClasseEquipamentoInicial.obrigatorio=true: HTTP 403 "Item obrigatorio nao pode ser removido pelo Jogador"
2. Se MESTRE: sem restricao
3. Soft delete (ficha.delete())
4. Se item estava equipado: disparar fichaService.recalcular(fichaId)
5. Retornar 204 No Content
```

---

## Endpoints

```java
@RestController
@RequestMapping("/api/v1/fichas/{fichaId}/itens")
public class FichaItemController {

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    ResponseEntity<FichaInventarioResponse> listarInventario(@PathVariable Long fichaId);

    @PostMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    ResponseEntity<FichaItemResponse> adicionarItem(
        @PathVariable Long fichaId,
        @Valid @RequestBody FichaItemAdicionarRequest request);

    @PostMapping("/customizado")
    @PreAuthorize("hasRole('MESTRE')")
    ResponseEntity<FichaItemResponse> adicionarItemCustomizado(
        @PathVariable Long fichaId,
        @Valid @RequestBody FichaItemCustomizadoRequest request);

    @PatchMapping("/{itemId}/equipar")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    ResponseEntity<FichaItemResponse> equiparItem(
        @PathVariable Long fichaId,
        @PathVariable Long itemId);

    @PatchMapping("/{itemId}/desequipar")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    ResponseEntity<FichaItemResponse> desequiparItem(
        @PathVariable Long fichaId,
        @PathVariable Long itemId);

    @PostMapping("/{itemId}/durabilidade")
    @PreAuthorize("hasRole('MESTRE')")
    ResponseEntity<FichaItemResponse> atualizarDurabilidade(
        @PathVariable Long fichaId,
        @PathVariable Long itemId,
        @Valid @RequestBody FichaItemDuracaoRequest request);

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    ResponseEntity<Void> removerItem(
        @PathVariable Long fichaId,
        @PathVariable Long itemId);
}
```

---

## Query sem N+1

```java
// FichaItemRepository
@Query("""
    SELECT fi FROM FichaItem fi
    LEFT JOIN FETCH fi.itemConfig ic
    LEFT JOIN FETCH ic.raridade
    LEFT JOIN FETCH ic.tipo
    LEFT JOIN FETCH ic.efeitos e
    LEFT JOIN FETCH e.atributoAlvo
    LEFT JOIN FETCH e.bonusAlvo
    LEFT JOIN FETCH fi.raridade
    WHERE fi.ficha.id = :fichaId
    AND fi.deletedAt IS NULL
    ORDER BY fi.equipado DESC, fi.nome
    """)
List<FichaItem> findByFichaIdWithDetails(@Param("fichaId") Long fichaId);
```

---

## Regras de Negocio

- **RN-T4-01:** JOGADOR so pode adicionar itens com `raridade.podeJogadorAdicionar = true` → HTTP 403
- **RN-T4-02:** JOGADOR so pode adicionar/equipar/desequipar itens da propria ficha → HTTP 403
- **RN-T4-03:** Item com `duracaoAtual = 0` nao pode ser equipado → HTTP 422
- **RN-T4-04:** Decremento de durabilidade que chega a 0: `equipado` forcado para `false` + recalculo da ficha
- **RN-T4-05:** JOGADOR nao pode remover itens `obrigatorio=true` (vindos de ClasseEquipamentoInicial) → HTTP 403
- **RN-T4-06:** `peso` efetivo = `fichaItem.peso ?? fichaItem.itemConfig?.peso ?? 0`
- **RN-T4-07:** `capacidadeCarga` = `FichaAtributo[FOR].total * 3` (formula de Impeto de Forca)
- **RN-T4-08:** Sobrecarga e apenas informativa no MVP — sem penalidade automatica (ver PA-016-01)
- **RN-T4-09:** Item de ItemConfig deletado (soft delete) pode ser equipado normalmente — o FichaItem existente e independente

---

## Criterios de Aceitacao

- [ ] JOGADOR adiciona item Comum: 201
- [ ] JOGADOR tenta adicionar item Incomum (podeJogadorAdicionar=false): 403
- [ ] Mestre adiciona qualquer raridade: 201
- [ ] Equipar item com duracaoAtual=0: 422
- [ ] Equipar item com duracaoAtual>0: 200, equipado=true
- [ ] GET inventario retorna equipados separados de inventario
- [ ] GET inventario inclui pesoTotal e capacidadeCarga corretamente calculados
- [ ] Decrementar durabilidade para 0: item desequipado automaticamente
- [ ] Restaurar durabilidade via request.restaurar=true: duracaoAtual volta ao duracaoPadrao
- [ ] JOGADOR nao remove item obrigatorio: 403
- [ ] MESTRE remove qualquer item: 204
- [ ] `./mvnw test` passa sem regressao

---

*Produzido por: Business Analyst/PO | 2026-04-04*
