# P1-T4 — Integração do SiglaValidationService nos services

## Objetivo
Injetar e chamar `SiglaValidationService` em `AtributoConfiguracaoService`, `BonusConfiguracaoService` e `VantagemConfiguracaoService`.

## Depende de
P1-T3 (SiglaValidationService criado)

## Steps

### AtributoConfiguracaoService

**Injetar**: `SiglaValidationService siglaValidationService`

**validarAntesCriar(AtributoConfig configuracao)**:
```java
validateUniqueNome(configuracao.getNome(), configuracao.getJogo().getId());
siglaValidationService.validarSiglaDisponivel(
    configuracao.getAbreviacao(),
    configuracao.getJogo().getId(),
    null,             // create: sem excludeId
    TipoSigla.ATRIBUTO
);
```

**validarAntesAtualizar(AtributoConfig existente, AtributoConfig atualizado)**:
```java
// Valida nome se mudou
if (!existente.getNome().equalsIgnoreCase(atualizado.getNome())) {
    validateUniqueNome(atualizado.getNome(), existente.getJogo().getId());
}
// Valida sigla se mudou
if (atualizado.getAbreviacao() != null
    && !existente.getAbreviacao().equalsIgnoreCase(atualizado.getAbreviacao())) {
    siglaValidationService.validarSiglaDisponivel(
        atualizado.getAbreviacao(),
        existente.getJogo().getId(),
        existente.getId(),       // update: excluir o próprio
        TipoSigla.ATRIBUTO
    );
}
```

**atualizarCampos(AtributoConfig existente, AtributoConfig atualizado)** — incluir campos ignorados atualmente:
```java
existente.setNome(atualizado.getNome());
existente.setDescricao(atualizado.getDescricao());
existente.setOrdemExibicao(atualizado.getOrdemExibicao());
existente.setFormulaImpeto(atualizado.getFormulaImpeto());
// Adicionar:
if (atualizado.getAbreviacao() != null) existente.setAbreviacao(atualizado.getAbreviacao());
if (atualizado.getValorMinimo() != null) existente.setValorMinimo(atualizado.getValorMinimo());
if (atualizado.getValorMaximo() != null) existente.setValorMaximo(atualizado.getValorMaximo());
```

---

### BonusConfiguracaoService

**Injetar**: `SiglaValidationService siglaValidationService`

**validarAntesCriar(BonusConfig configuracao)**:
```java
validateUniqueNome(configuracao.getNome(), configuracao.getJogo().getId());
siglaValidationService.validarSiglaDisponivel(
    configuracao.getSigla(),
    configuracao.getJogo().getId(),
    null,
    TipoSigla.BONUS
);
```

**validarAntesAtualizar(BonusConfig existente, BonusConfig atualizado)**:
```java
if (!existente.getNome().equalsIgnoreCase(atualizado.getNome())) {
    validateUniqueNome(atualizado.getNome(), existente.getJogo().getId());
}
String siglaNova = atualizado.getSigla();
if (siglaNova != null && !siglaNova.equalsIgnoreCase(existente.getSigla())) {
    siglaValidationService.validarSiglaDisponivel(
        siglaNova, existente.getJogo().getId(), existente.getId(), TipoSigla.BONUS
    );
}
```

---

### VantagemConfiguracaoService

Mesmo padrão do Bonus, mas sigla é opcional — a validação já trata `null` internamente (`if (sigla == null) return`).

---

## Acceptance Checks
- [ ] Criar atributo com sigla já usada em bônus → ConflictException
- [ ] Editar atributo mantendo mesma sigla → aceito
- [ ] Editar atributo para uma sigla livre → aceito
- [ ] `atualizarCampos` de AtributoConfig agora atualiza abreviacao, valorMinimo, valorMaximo
- [ ] Criar bônus com sigla nula → ValidationException (sigla obrigatória em Bonus)
- [ ] Criar vantagem sem sigla → aceito
- [ ] Criar vantagem com sigla já em uso → ConflictException

## File Checklist
- `service/configuracao/AtributoConfiguracaoService.java`
- `service/configuracao/BonusConfiguracaoService.java`
- `service/configuracao/VantagemConfiguracaoService.java`

## References
- `docs/backend/05-services.md`
- `service/configuracao/AbstractConfiguracaoService.java` — hooks validarAntesCriar/Atualizar
