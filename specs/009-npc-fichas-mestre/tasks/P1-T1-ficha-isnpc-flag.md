# P1-T1 — Flag isNpc + jogadorId Nullable na Ficha

**Fase:** 1 — Entity
**Complexidade:** 🟢 Baixa
**Depende de:** Spec 006 (Ficha entity)
**Bloqueia:** P2-T1

## Objetivo

Garantir que a Ficha entity tem suporte a NPCs. Esta task pode já estar implementada pela Spec 006 — verificar antes de agir.

## Checklist

### 1. Verificar em Ficha entity

- [ ] Campo `boolean isNpc` com `@Column(nullable=false, columnDefinition="boolean default false")`
- [ ] Campo `String jogadorId` é nullable (sem @NotNull)

### 2. FichaRepository — queries por isNpc

- [ ] `findByJogoIdAndIsNpcFalse(Long jogoId)` — fichas de jogadores
- [ ] `findByJogoIdAndIsNpcTrue(Long jogoId)` — NPCs
- [ ] Atualizar `findByJogoIdAndJogadorId()` para filtrar apenas isNpc=false se necessário

### 3. FichaResponse

- [ ] Incluir campo `isNpc` no FichaResponse
- [ ] Incluir `jogadorId` (nullable) no FichaResponse

## Arquivos afetados

- `model/Ficha.java` (VERIFICAR/MODIFICAR)
- `repository/FichaRepository.java` (VERIFICAR/MODIFICAR)
- `dto/response/FichaResponse.java` (VERIFICAR/MODIFICAR)

## Verificações de aceitação

- [ ] Ficha criada sem jogadorId compila e persiste com isNpc=true, jogadorId=null
- [ ] Ficha criada com jogadorId tem isNpc=false por default
- [ ] `./mvnw test` passa
