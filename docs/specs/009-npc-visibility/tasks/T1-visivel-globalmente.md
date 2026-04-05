# T1 — Campo visivelGlobalmente em Ficha

> Tipo: Backend
> Dependencias: nenhuma
> Desbloqueia: T2

---

## Objetivo

Adicionar o campo `visivelGlobalmente` na entidade `Ficha` e expor nos DTOs correspondentes. Este campo controla se um NPC aparece ou nao na listagem de fichas do Jogador.

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `model/Ficha.java` | Adicionar campo `visivelGlobalmente` |
| `dto/request/NpcCreateRequest.java` | Adicionar campo opcional `visivelGlobalmente` |
| `dto/request/UpdateFichaRequest.java` | Verificar se precisa incluir o campo |
| `dto/response/FichaResponse.java` | Adicionar campo `visivelGlobalmente` e `jogadorTemAcessoStats` |
| `mapper/FichaMapper.java` | Mapear os novos campos |
| `service/FichaService.java` | Atualizar logica de listagem para Jogadores (incluir NPCs visivelGlobalmente) |

---

## Passos

### Passo 1 — model/Ficha.java

Adicionar abaixo do campo `descricao`:

```java
/**
 * Indica se este NPC esta visivel na listagem de fichas para os Jogadores.
 * Relevante apenas quando isNpc=true.
 * Visibilidade de stats completos e controlada por FichaVisibilidade.
 */
@Builder.Default
@Column(name = "visivel_globalmente", nullable = false)
private boolean visivelGlobalmente = false;
```

### Passo 2 — dto/response/FichaResponse.java

Adicionar campos:
- `Boolean visivelGlobalmente` — null para fichas de jogadores; true/false para NPCs
- `Boolean jogadorTemAcessoStats` — null para fichas de jogadores; calculado por FichaVisibilidade para NPCs (preenchido no service, nao no mapper)

### Passo 3 — dto/request/NpcCreateRequest.java

Adicionar campo opcional:
```java
Boolean visivelGlobalmente  // default null → interpretado como false no service
```

### Passo 4 — FichaService.listarComFiltros()

A logica atual para Jogadores retorna apenas fichas onde `jogadorId = usuarioAtual.getId()`.

**Adicionar ao resultado:** NPCs com `visivelGlobalmente = true` do jogo, populando `jogadorTemAcessoStats` via consulta a `FichaVisibilidadeRepository` (que sera criado na T2 — mas a query pode ser preparada retornando `false` por padrao ate T2 estar completa).

**Query adicional para Jogadores:**
```sql
SELECT f FROM Ficha f
WHERE f.jogo.id = :jogoId
  AND f.isNpc = true
  AND f.visivelGlobalmente = true
  AND f.deletedAt IS NULL
```

### Passo 5 — GET /fichas/{id} — verificarAcessoLeitura()

Em `FichaVidaService.verificarAcessoLeitura()`, a logica atual lanca 403 para qualquer NPC acessado por Jogador. Adicionar verificacao: se `FichaVisibilidade` existe para (fichaId, usuarioId), permitir acesso.

> Esta verificacao completa sera implementada na T2. Na T1, apenas preparar o campo e os DTOs.

---

## Criterios de Aceitacao

- [ ] Campo `visivelGlobalmente` existe em `Ficha` com `@Builder.Default = false`
- [ ] `FichaResponse` inclui `visivelGlobalmente`
- [ ] `NpcCreateRequest` aceita `visivelGlobalmente` como opcional
- [ ] Ao criar NPC sem `visivelGlobalmente`, o valor padrao e `false`
- [ ] `GET /jogos/{id}/fichas` para Jogador retorna NPCs com `visivelGlobalmente=true` (com `jogadorTemAcessoStats=false` ate T2)
- [ ] `GET /jogos/{id}/fichas` para Jogador NAO retorna NPCs com `visivelGlobalmente=false`
- [ ] Testes unitarios no FichaService para o novo filtro de NPCs visiveis
