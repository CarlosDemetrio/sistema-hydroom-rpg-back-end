# P1-T3 — SiglaValidationService (cross-entity)

## Objetivo
Criar o serviço central que valida unicidade de siglas cross-entity por jogo e lista todas as siglas em uso.

## Depende de
P1-T2 (repositórios com métodos de sigla)

## Contexto
A regra crítica do glossário (`docs/glossario/04-siglas-formulas.md`): siglas devem ser **únicas por jogo, cross-entity**. Se `FOR` existe como atributo, nenhum bônus, vantagem ou outro atributo do mesmo jogo pode usar `FOR`.

A validação no service layer (não no DB via constraint cross-table) é a abordagem correta.

## Steps

### 1. Criar enum TipoSigla
```java
// No próprio SiglaValidationService ou em arquivo separado em dto/enums/
public enum TipoSigla {
    ATRIBUTO, BONUS, VANTAGEM
}
```

### 2. Criar SiglaValidationService

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class SiglaValidationService {

    private final ConfiguracaoAtributoRepository atributoRepository;
    private final BonusConfigRepository bonusRepository;
    private final VantagemConfigRepository vantagemRepository;

    /**
     * Valida que a sigla não está em uso em NENHUMA entidade do jogo,
     * exceto a entidade sendo editada (excludeId + tipo).
     *
     * @param sigla      sigla a validar (case-insensitive)
     * @param jogoId     ID do jogo
     * @param excludeId  ID da entidade atual (null se create)
     * @param tipo       tipo da entidade chamadora (para excluir ela mesma corretamente)
     * @throws ConflictException se sigla já estiver em uso
     */
    public void validarSiglaDisponivel(String sigla, Long jogoId, Long excludeId, TipoSigla tipo) {
        if (sigla == null || sigla.isBlank()) return;

        String siglaUpper = sigla.toUpperCase();

        // Verificar atributos (exceto o próprio se tipo == ATRIBUTO)
        Long atributoExclude = tipo == TipoSigla.ATRIBUTO ? excludeId : null;
        if (atributoExclude != null) {
            if (atributoRepository.existsByJogoIdAndAbreviacaoIgnoreCaseAndIdNot(jogoId, sigla, atributoExclude)) {
                lançarConflito(siglaUpper, "atributo");
            }
        } else {
            if (atributoRepository.existsByJogoIdAndAbreviacaoIgnoreCase(jogoId, sigla)) {
                lançarConflito(siglaUpper, "atributo");
            }
        }

        // Verificar bônus (exceto o próprio se tipo == BONUS)
        Long bonusExclude = tipo == TipoSigla.BONUS ? excludeId : null;
        if (bonusExclude != null) {
            if (bonusRepository.existsByJogoIdAndSiglaIgnoreCaseAndIdNot(jogoId, sigla, bonusExclude)) {
                lançarConflito(siglaUpper, "bônus");
            }
        } else {
            if (bonusRepository.existsByJogoIdAndSiglaIgnoreCase(jogoId, sigla)) {
                lançarConflito(siglaUpper, "bônus");
            }
        }

        // Verificar vantagens (exceto a própria se tipo == VANTAGEM)
        Long vantagemExclude = tipo == TipoSigla.VANTAGEM ? excludeId : null;
        if (vantagemExclude != null) {
            if (vantagemRepository.existsByJogoIdAndSiglaIgnoreCaseAndIdNot(jogoId, sigla, vantagemExclude)) {
                lançarConflito(siglaUpper, "vantagem");
            }
        } else {
            if (vantagemRepository.existsByJogoIdAndSiglaIgnoreCase(jogoId, sigla)) {
                lançarConflito(siglaUpper, "vantagem");
            }
        }
    }

    /**
     * Lista todas as siglas em uso no jogo, com tipo e entityId.
     */
    public List<SiglaEmUsoResponse> listarSiglasDoJogo(Long jogoId) {
        List<SiglaEmUsoResponse> todas = new ArrayList<>();
        todas.addAll(atributoRepository.findSiglasComInfoByJogoId(jogoId));
        todas.addAll(bonusRepository.findSiglasComInfoByJogoId(jogoId));
        todas.addAll(vantagemRepository.findSiglasComInfoByJogoId(jogoId));
        return todas.stream()
                .sorted(Comparator.comparing(SiglaEmUsoResponse::sigla))
                .toList();
    }

    private void lançarConflito(String sigla, String entidade) {
        throw new ConflictException(
            ValidationMessages.Sigla.SIGLA_JA_EM_USO.formatted(sigla, entidade)
        );
    }
}
```

### 3. Adicionar mensagem em ValidationMessages
```java
public static final class Sigla {
    public static final String SIGLA_JA_EM_USO =
        "Sigla '%s' já está em uso em %s neste jogo. Siglas devem ser únicas por jogo.";
}
```

## Casos de uso que o service cobre

| Cenário | excludeId | tipo | Resultado esperado |
|---|---|---|---|
| Criar atributo com sigla "FOR" | null | ATRIBUTO | Verifica bônus e vantagens (sem excluir nada) |
| Editar atributo ID=5, mudar sigla | 5L | ATRIBUTO | Exclui o próprio atributo 5 da busca em atributos, verifica bônus e vantagens |
| Criar bônus com sigla "FOR" (atributo já tem) | null | BONUS | Rejeita — atributo já usa "FOR" |
| Criar vantagem sem sigla | null | VANTAGEM | Retorna imediatamente (sigla == null) |

## Acceptance Checks
- [ ] Criar atributo com sigla existente em bônus → ConflictException com mensagem descritiva
- [ ] Criar bônus com sigla existente em vantagem → ConflictException
- [ ] Editar atributo mantendo mesma sigla → não rejeita (excludeId correto)
- [ ] Vantagem sem sigla não dispara validação
- [ ] `listarSiglasDoJogo` retorna todas as siglas ordenadas por sigla

## File Checklist
- `service/configuracao/SiglaValidationService.java`
- `exception/ValidationMessages.java` (adicionar constante Sigla.SIGLA_JA_EM_USO)

## References
- `docs/glossario/04-siglas-formulas.md` — regra de unicidade
- `docs/backend/05-services.md`
- `docs/backend/03-exceptions.md`
- `service/configuracao/BonusConfiguracaoService.java` — padrão de lançar ConflictException
