# P1-T1 — JogoParticipante: Status + DTOs + Repository

**Fase:** 1 — Entity/DTOs
**Complexidade:** 🟡 Média
**Depende de:** nada
**Bloqueia:** P2-T1

## Objetivo

Evoluir a entity JogoParticipante para suportar o fluxo de aprovação: adicionar enum StatusParticipante, campo status, queries necessárias e DTOs.

## Checklist

### 1. Enum StatusParticipante
- [ ] Criar `model/enums/StatusParticipante.java` com valores: PENDENTE, APROVADO, REJEITADO, BANIDO

### 2. JogoParticipante entity
- [ ] Adicionar campo `StatusParticipante status` com `@Enumerated(EnumType.STRING)` e `@Column(nullable = false)`
- [ ] Default no banco: APROVADO (para compatibilidade com registros existentes via `columnDefinition`)
- [ ] Manter Lombok: @Data @Builder @EqualsAndHashCode(callSuper=true) @NoArgsConstructor @AllArgsConstructor

### 3. JogoParticipanteRepository
- [ ] `findByJogoIdAndUsuarioId(Long jogoId, String usuarioId)` → Optional
- [ ] `findByJogoIdOrderByCreatedAtDesc(Long jogoId)` → List
- [ ] `findByJogoIdAndStatus(Long jogoId, StatusParticipante status)` → List
- [ ] `existsByJogoIdAndUsuarioId(Long jogoId, String usuarioId)` → boolean

### 4. DTOs
- [ ] `dto/request/SolicitarParticipacaoRequest.java` — record vazio (sem campos obrigatórios; usa usuário logado)
- [ ] `dto/response/ParticipanteResponse.java` — record com: id, jogoId, usuarioId, nomeUsuario (se disponível), status, dataCriacao, dataUltimaAtualizacao

### 5. Mapper
- [ ] `mapper/JogoParticipanteMapper.java` — toResponse(JogoParticipante entity)

## Arquivos afetados

- `model/enums/StatusParticipante.java` (NOVO)
- `model/JogoParticipante.java` (MODIFICAR — adicionar status)
- `repository/JogoParticipanteRepository.java` (MODIFICAR — novos métodos)
- `dto/request/SolicitarParticipacaoRequest.java` (NOVO)
- `dto/response/ParticipanteResponse.java` (NOVO)
- `mapper/JogoParticipanteMapper.java` (NOVO)

## Verificações de aceitação

- [ ] Enum tem os 4 valores esperados
- [ ] Entity compila sem erros Lombok
- [ ] Repository queries estão corretas (JPQL ou Spring Data Naming)
- [ ] DTOs são records imutáveis
- [ ] `./mvnw test` passa
