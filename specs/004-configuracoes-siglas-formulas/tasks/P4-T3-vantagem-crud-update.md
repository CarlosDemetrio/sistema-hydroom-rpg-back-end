# P4-T3 — VantagemConfig CRUD: incluir pré-requisitos no response e gestão

## Objetivo
Atualizar os DTOs, mapper e controller de VantagemConfig para incluir pré-requisitos. Definir como o Mestre gerencia pré-requisitos (via endpoints dedicados ou inline no CRUD da vantagem).

## Depende de
P4-T2 (detecção de ciclos implementada)

## Decisão de design: como gerenciar pré-requisitos

**Opção A**: Inline no CRUD de VantagemConfig — `CreateVantagemRequest` inclui lista de pré-requisitos.
**Opção B**: Endpoints dedicados — `POST /vantagens/{id}/prerequisitos`, `DELETE /vantagens/{id}/prerequisitos/{prId}`.

**Decisão**: **Opção B** — endpoints dedicados. Motivo: pré-requisitos podem ser adicionados/removidos independentemente da vantagem, e a lógica de ciclo é complexa para embutir no create. O response ainda lista os pré-requisitos.

## Steps

### 1. VantagemResponse — adicionar lista

```java
public record VantagemResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    String sigla,
    Long categoriaVantagemId,
    String categoriaNome,
    Integer nivelMaximo,
    String formulaCusto,
    String descricaoEfeito,
    Integer ordemExibicao,
    List<VantagemPreRequisitoResponse> preRequisitos,  // NOVO
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
```

### 2. VantagemConfigMapper — mapear preRequisitos

```java
@Mapping(target = "preRequisitos", source = "preRequisitos")
VantagemResponse toResponse(VantagemConfig entity);

@Mapping(target = "requisitoId", source = "requisito.id")
@Mapping(target = "requisitoNome", source = "requisito.nome")
VantagemPreRequisitoResponse toPreRequisitoResponse(VantagemPreRequisito pr);
```

> **Atenção**: a lista `preRequisitos` em `VantagemConfig` é `@OneToMany` lazy. O mapper vai disparar N+1 queries se não cuidar. Solução: no service, chamar `findByVantagemId(id)` antes de mapear, ou usar `@EntityGraph` no repository de VantagemConfig.

**Abordagem recomendada**: No `VantagemController.buscarPorId()`, após buscar a vantagem, popular os pré-requisitos explicitamente:
```java
List<VantagemPreRequisito> preReqs = prerequisitoService.listar(vantagem.getId());
// Setar no objeto ou passar junto ao mapper
```

Ou adicionar método no repositório com JOIN FETCH:
```java
@Query("SELECT v FROM VantagemConfig v LEFT JOIN FETCH v.preRequisitos WHERE v.id = :id")
Optional<VantagemConfig> findByIdWithPreRequisitos(@Param("id") Long id);
```

### 3. Endpoints de pré-requisitos no VantagemController

```java
// Listar pré-requisitos de uma vantagem
GET /api/jogos/{jogoId}/config/vantagens/{id}/prerequisitos

// Adicionar pré-requisito
POST /api/jogos/{jogoId}/config/vantagens/{id}/prerequisitos
Body: VantagemPreRequisitoRequest { requisitoId, nivelMinimo }

// Remover pré-requisito
DELETE /api/jogos/{jogoId}/config/vantagens/{id}/prerequisitos/{prId}
```

Security: POST e DELETE com `hasRole('MESTRE')`, GET com `hasAnyRole`.

### 4. Remoção de pré-requisito

```java
@Transactional
public void removerPreRequisito(Long prId) {
    VantagemPreRequisito pr = prerequisitoRepository.findById(prId)
        .orElseThrow(() -> new ResourceNotFoundException("VantagemPreRequisito", prId));
    pr.delete(); // soft delete
    prerequisitoRepository.save(pr);
}
```

> **Nota**: soft delete em VantagemPreRequisito é opcional — pode-se fazer hard delete já que não há auditoria de pré-requisitos. Usar hard delete simplifica. Decidir na implementação.

## Acceptance Checks
- [ ] GET VantagemConfig por ID retorna lista `preRequisitos` (vazia se nenhum)
- [ ] POST prerequisitos adiciona e detecta ciclos (via P4-T2)
- [ ] DELETE prerequisitos remove a relação
- [ ] GET lista todos os pré-requisitos de uma vantagem
- [ ] Mapper não dispara N+1 (verificar com logs do Hibernate)

## File Checklist
- `dto/response/configuracao/VantagemResponse.java`
- `mapper/configuracao/VantagemConfigMapper.java`
- `controller/configuracao/VantagemController.java`
- `service/configuracao/VantagemConfiguracaoService.java`
- `repository/VantagemPreRequisitoRepository.java` (query com JOIN FETCH se necessário)

## References
- `controller/configuracao/AtributoController.java` — padrão thin controller
- `docs/backend/06-mappers.md` — N+1 prevention
