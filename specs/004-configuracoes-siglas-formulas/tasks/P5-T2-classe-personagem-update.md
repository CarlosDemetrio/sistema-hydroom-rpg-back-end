# P5-T2 — ClassePersonagem CRUD: incluir bônus e aptidões

## Objetivo
Atualizar o CRUD de ClassePersonagem para expor e gerenciar ClasseBonus e ClasseAptidaoBonus.

## Depende de
P5-T1 (entities ClasseBonus e ClasseAptidaoBonus criadas)

## Estratégia de gerenciamento das listas

Mesma decisão de VantagemPreRequisito: **endpoints dedicados** em vez de inline no create/update.
- Mais simples de implementar
- Permite adicionar/remover bônus individuais sem reenviar a lista completa
- Evita problemas de substituição de lista no mapper

## Steps

### 1. ClasseResponse — adicionar listas

```java
public record ClasseResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    Integer ordemExibicao,
    List<ClasseBonusResponse> bonusConfig,       // NOVO
    List<ClasseAptidaoBonusResponse> aptidaoBonus, // NOVO
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
```

### 2. ClassePersonagemMapper — mapear listas

```java
@Mapping(target = "jogoId", source = "jogo.id")
@Mapping(target = "dataCriacao", source = "createdAt")
@Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
@Mapping(target = "bonusConfig", source = "bonusConfig")
@Mapping(target = "aptidaoBonus", source = "aptidaoBonus")
ClasseResponse toResponse(ClassePersonagem entity);

@Mapping(target = "bonusId", source = "bonus.id")
@Mapping(target = "bonusNome", source = "bonus.nome")
ClasseBonusResponse toBonusResponse(ClasseBonus cb);

@Mapping(target = "aptidaoId", source = "aptidao.id")
@Mapping(target = "aptidaoNome", source = "aptidao.nome")
ClasseAptidaoBonusResponse toAptidaoResponse(ClasseAptidaoBonus cab);
```

> **N+1 prevention**: usar JOIN FETCH no buscarPorId do ClassePersonagemRepository quando as listas forem necessárias.

```java
// ClassePersonagemRepository
@Query("SELECT c FROM ClassePersonagem c " +
       "LEFT JOIN FETCH c.bonusConfig cb LEFT JOIN FETCH cb.bonus " +
       "LEFT JOIN FETCH c.aptidaoBonus ab LEFT JOIN FETCH ab.aptidao " +
       "WHERE c.id = :id")
Optional<ClassePersonagem> findByIdWithBonuses(@Param("id") Long id);
```

### 3. Endpoints de ClasseBonus no ClassePersonagemController

```java
// Listar bônus de uma classe
GET /api/jogos/{jogoId}/config/classes/{id}/bonus

// Adicionar bônus
POST /api/jogos/{jogoId}/config/classes/{id}/bonus
Body: ClasseBonusRequest { bonusId, valorPorNivel }

// Remover bônus
DELETE /api/jogos/{jogoId}/config/classes/{id}/bonus/{bonusId}
```

### 4. Endpoints de ClasseAptidaoBonus no ClassePersonagemController

```java
// Listar bônus de aptidão da classe
GET /api/jogos/{jogoId}/config/classes/{id}/aptidao-bonus

// Adicionar
POST /api/jogos/{jogoId}/config/classes/{id}/aptidao-bonus
Body: ClasseAptidaoBonusRequest { aptidaoId, bonus }

// Remover
DELETE /api/jogos/{jogoId}/config/classes/{id}/aptidao-bonus/{aptidaoId}
```

### 5. ClassePersonagemConfiguracaoService — novos métodos

```java
@Transactional
public ClasseBonus adicionarBonus(Long classeId, Long bonusId, BigDecimal valorPorNivel) {
    ClassePersonagem classe = buscarPorId(classeId);
    BonusConfig bonus = bonusRepository.findById(bonusId)
        .orElseThrow(() -> new ResourceNotFoundException("BonusConfig", bonusId));

    // Validar mesmo jogo
    if (!bonus.getJogo().getId().equals(classe.getJogo().getId())) {
        throw new ValidationException(ValidationMessages.ClasseBonus.JOGO_DIFERENTE);
    }

    if (classeBonusRepository.existsByClasseIdAndBonusId(classeId, bonusId)) {
        throw new ConflictException(ValidationMessages.ClasseBonus.JA_EXISTE);
    }

    return classeBonusRepository.save(
        ClasseBonus.builder().classe(classe).bonus(bonus).valorPorNivel(valorPorNivel).build()
    );
}

@Transactional
public void removerBonus(Long classeId, Long classeBonusId) {
    ClasseBonus cb = classeBonusRepository.findById(classeBonusId)
        .orElseThrow(() -> new ResourceNotFoundException("ClasseBonus", classeBonusId));
    if (!cb.getClasse().getId().equals(classeId)) {
        throw new ValidationException("Bônus não pertence à classe informada.");
    }
    classeBonusRepository.delete(cb); // hard delete ou soft delete — decidir na implementação
}

// Idem para aptidaoBonus
```

### 6. Atualizar GET de ClassePersonagem para carregar listas

No controller, buscar usando `findByIdWithBonuses` para o endpoint `GET /{id}`. Para listagem (`GET /`), retornar sem listas (para performance) — ou criar resposta resumida `ClasseResumoResponse`.

## Acceptance Checks
- [ ] GET `/classes/{id}` retorna listas `bonusConfig` e `aptidaoBonus` preenchidas
- [ ] POST `/classes/{id}/bonus` adiciona ClasseBonus com validação de mesmo jogo
- [ ] Duplicata de BonusConfig na mesma classe é rejeitada
- [ ] DELETE remove o bônus da classe
- [ ] GET lista de classes (sem id) não dispara N+1 (listas não carregadas)
- [ ] BonusConfig de outro jogo é rejeitado na adição

## File Checklist
- `dto/response/configuracao/ClasseResponse.java`
- `mapper/configuracao/ClassePersonagemMapper.java`
- `controller/configuracao/ClassePersonagemController.java`
- `service/configuracao/ClassePersonagemConfiguracaoService.java`
- `repository/ClassePersonagemRepository.java` (query JOIN FETCH)
- `repository/ClasseBonusRepository.java`
- `repository/ClasseAptidaoBonusRepository.java`
- `exception/ValidationMessages.java`

## References
- `controller/configuracao/AtributoController.java` — padrão thin controller
- `docs/backend/06-mappers.md` — N+1 prevention
- `docs/backend/07-controllers.md`
