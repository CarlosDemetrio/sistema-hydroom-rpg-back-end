# T4 — Endpoint resetar-estado

> Tipo: Backend
> Dependencias: nenhuma (paralelo com T1/T2/T3)
> Desbloqueia: T6, T10

---

## Objetivo

Implementar o endpoint `POST /fichas/{id}/resetar-estado` que restaura o estado de combate de uma ficha ao estado base em uma unica operacao atomica. Apenas o Mestre pode executar esta acao.

---

## O Que e Resetado

| Campo | Valor apos reset | Origem |
|-------|-----------------|--------|
| `FichaVida.vidaAtual` | `FichaVida.vidaTotal` | Campo calculado ja persistido |
| `FichaEssencia.essenciaAtual` | `FichaEssencia.total` | Campo calculado ja persistido |
| `FichaVidaMembro.danoRecebido` (todos os membros) | `0` | Reset de todos os registros da ficha |

## O Que NAO e Resetado

- `FichaProspeccao.quantidade` — decisao do PO: prospecção nao e resetada
- `ProspeccaoUso` — historico nao e alterado
- Atributos, aptidoes, vantagens, XP, nivel, renascimentos

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `service/FichaVidaService.java` | Adicionar metodo `resetarEstado()` |
| `controller/FichaController.java` | Adicionar endpoint POST /fichas/{id}/resetar-estado |

---

## Passos

### Passo 1 — FichaVidaService.resetarEstado()

```java
@Transactional
public Ficha resetarEstado(Long fichaId) {
    Ficha ficha = fichaRepository.findById(fichaId)
        .orElseThrow(() -> new ResourceNotFoundException("Ficha nao encontrada: " + fichaId));

    // Verificar que o usuario atual e MESTRE do jogo
    verificarAcessoMestre(ficha);

    // Resetar FichaVida
    FichaVida fichaVida = fichaVidaRepository.findByFichaId(fichaId)
        .orElseThrow(() -> new ResourceNotFoundException("Dados de vida nao encontrados: " + fichaId));
    fichaVida.setVidaAtual(fichaVida.getVidaTotal());
    fichaVidaRepository.save(fichaVida);

    // Resetar FichaEssencia
    FichaEssencia fichaEssencia = fichaEssenciaRepository.findByFichaId(fichaId)
        .orElseThrow(() -> new ResourceNotFoundException("Dados de essencia nao encontrados: " + fichaId));
    fichaEssencia.setEssenciaAtual(fichaEssencia.getTotal());
    fichaEssenciaRepository.save(fichaEssencia);

    // Resetar dano em todos os membros
    List<FichaVidaMembro> membros = fichaVidaMembroRepository.findByFichaId(fichaId);
    membros.forEach(m -> m.setDanoRecebido(0));
    fichaVidaMembroRepository.saveAll(membros);

    log.info("Estado resetado para ficha {}: vidaAtual={}, essenciaAtual={}",
        fichaId, fichaVida.getVidaTotal(), fichaEssencia.getTotal());

    return ficha;
}
```

**Metodo auxiliar verificarAcessoMestre():**
```java
private void verificarAcessoMestre(Ficha ficha) {
    Usuario usuarioAtual = getUsuarioAtual();
    boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
        ficha.getJogo().getId(), usuarioAtual.getId(), RoleJogo.MESTRE);
    if (!isMestre) {
        throw new ForbiddenException("Apenas o Mestre pode resetar o estado de uma ficha.");
    }
}
```

### Passo 2 — FichaController.java

```java
@PostMapping("/api/v1/fichas/{id}/resetar-estado")
@PreAuthorize("hasRole('MESTRE')")
@Operation(
    summary = "Resetar estado de combate da ficha (Apenas MESTRE)",
    description = "Restaura vidaAtual, essenciaAtual e danoRecebido de todos os membros ao estado base. " +
                  "Nao altera atributos, nivel, xp ou prospeccao. Operacao irreversivel.")
public ResponseEntity<FichaResumoResponse> resetarEstado(@PathVariable Long id) {
    fichaVidaService.resetarEstado(id);
    var resumo = fichaResumoService.getResumo(id);
    return ResponseEntity.ok(resumo);
}
```

---

## Consideracoes de Seguranca

- `@PreAuthorize("hasRole('MESTRE')")` no controller e duplo-check no service via `verificarAcessoMestre()`
- O service verifica que o Mestre e do mesmo jogo da ficha — um Mestre de outro jogo nao pode resetar

---

## Criterios de Aceitacao

- [ ] `POST /fichas/{id}/resetar-estado` por MESTRE retorna HTTP 200 com FichaResumoResponse
- [ ] Apos reset: `FichaVida.vidaAtual == FichaVida.vidaTotal`
- [ ] Apos reset: `FichaEssencia.essenciaAtual == FichaEssencia.total`
- [ ] Apos reset: todos os `FichaVidaMembro.danoRecebido == 0`
- [ ] Apos reset: `FichaProspeccao.quantidade` permanece inalterado
- [ ] `POST /fichas/{id}/resetar-estado` por JOGADOR retorna HTTP 403
- [ ] Operacao e atomica: se qualquer passo falhar, nenhuma alteracao e persistida
- [ ] MESTRE de outro jogo tentando resetar ficha do jogo A retorna HTTP 403
