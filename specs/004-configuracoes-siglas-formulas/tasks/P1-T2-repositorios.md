# P1-T2 — Repositórios para sigla

## Objetivo
Adicionar métodos de repositório necessários para validação de unicidade de sigla e listagem.

## Depende de
P1-T1 (campos sigla nos models)

## Steps

### AtributoConfigRepository
Adicionar (se não existir):
```java
boolean existsByJogoIdAndAbreviacaoIgnoreCaseAndIdNot(Long jogoId, String abreviacao, Long id);
boolean existsByJogoIdAndAbreviacaoIgnoreCase(Long jogoId, String abreviacao);

// Para listagem de siglas do jogo
@Query("SELECT a.abreviacao FROM AtributoConfig a WHERE a.jogo.id = :jogoId AND a.abreviacao IS NOT NULL")
List<String> findAbreviacoesByJogoId(@Param("jogoId") Long jogoId);

// Para SiglaValidationService — retorna sigla + id + nome
@Query("SELECT new br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.SiglaEmUsoResponse(" +
       "'ATRIBUTO', a.abreviacao, a.id, a.nome) " +
       "FROM AtributoConfig a WHERE a.jogo.id = :jogoId AND a.abreviacao IS NOT NULL")
List<SiglaEmUsoResponse> findSiglasComInfoByJogoId(@Param("jogoId") Long jogoId);
```

### BonusConfigRepository
```java
boolean existsByJogoIdAndSiglaIgnoreCaseAndIdNot(Long jogoId, String sigla, Long id);
boolean existsByJogoIdAndSiglaIgnoreCase(Long jogoId, String sigla);

@Query("SELECT new br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.SiglaEmUsoResponse(" +
       "'BONUS', b.sigla, b.id, b.nome) " +
       "FROM BonusConfig b WHERE b.jogo.id = :jogoId AND b.sigla IS NOT NULL")
List<SiglaEmUsoResponse> findSiglasComInfoByJogoId(@Param("jogoId") Long jogoId);
```

### VantagemConfigRepository
```java
boolean existsByJogoIdAndSiglaIgnoreCaseAndIdNot(Long jogoId, String sigla, Long id);
boolean existsByJogoIdAndSiglaIgnoreCase(Long jogoId, String sigla);

@Query("SELECT new br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.SiglaEmUsoResponse(" +
       "'VANTAGEM', v.sigla, v.id, v.nome) " +
       "FROM VantagemConfig v WHERE v.jogo.id = :jogoId AND v.sigla IS NOT NULL")
List<SiglaEmUsoResponse> findSiglasComInfoByJogoId(@Param("jogoId") Long jogoId);
```

## Nota sobre SiglaEmUsoResponse
O record `SiglaEmUsoResponse` será criado na task P1-T5. Para compilar, pode-se criar o record primeiro (sem o controller):

```java
// dto/response/configuracao/SiglaEmUsoResponse.java
public record SiglaEmUsoResponse(String tipo, String sigla, Long entityId, String nome) {}
```

## Acceptance Checks
- [ ] `existsByJogoIdAndAbreviacaoIgnoreCaseAndIdNot` funciona em teste isolado
- [ ] `existsByJogoIdAndSiglaIgnoreCaseAndIdNot` funciona nos dois repositórios
- [ ] `findAbreviacoesByJogoId` retorna apenas abreviações não nulas
- [ ] `findSiglasComInfoByJogoId` retorna lista tipada corretamente

## File Checklist
- `repository/ConfiguracaoAtributoRepository.java`
- `repository/BonusConfigRepository.java`
- `repository/VantagemConfigRepository.java`
- `dto/response/configuracao/SiglaEmUsoResponse.java` (criar agora para compilar as queries)

## References
- `docs/backend/04-repositories.md`
- `repository/ConfiguracaoAtributoRepository.java` — padrão existente de `existsByJogoIdAndNomeIgnoreCase`
