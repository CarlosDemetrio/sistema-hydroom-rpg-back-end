# T1 — Campo `status` na Ficha + Endpoint `/completar`

> Fase: Backend
> Complexidade: Media
> Prerequisito: Nenhum
> Bloqueia: T6 (Passo 1), T11 (Revisao), T13 (Badge incompleta)
> Estimativa: 3–4 horas

---

## Objetivo

Adicionar o campo `status` (RASCUNHO/COMPLETA) na entidade `Ficha` e implementar o endpoint `PUT /fichas/{id}/completar` que transiciona a ficha para COMPLETA apos validar todos os campos obrigatorios. Este e o pre-requisito central de toda a Spec 006.

---

## Contexto

A entidade `Ficha` atual (em `model/Ficha.java`) nao tem campo de status. Toda ficha criada existe em estado implicito de "completa", o que nao reflete a realidade do wizard de criacao em etapas. Com este campo, o frontend pode:
- Exibir badge "Incompleta" na listagem
- Impedir que fichas incompletas entrem em sessao
- Retomar rascunhos de onde pararam

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `model/Ficha.java` | Adicionar campo `status` |
| `model/enums/FichaStatus.java` | Criar enum novo |
| `dto/response/FichaResponse.java` | Adicionar campo `status` |
| `mapper/FichaMapper.java` | Mapear campo `status` |
| `service/FichaService.java` | Inicializar status=RASCUNHO no criar(); novo metodo completar() |
| `service/FichaValidationService.java` | Novo metodo validarCompletude() |
| `controller/FichaController.java` | Novo endpoint PUT /fichas/{id}/completar |
| `resources/db/migration/` | Nova migration Flyway (se producao) ou ajuste de test schema |

---

## Passos de Implementacao

### 1. Criar o enum `FichaStatus`

```java
// src/main/java/.../model/enums/FichaStatus.java
public enum FichaStatus {
    RASCUNHO,
    COMPLETA
}
```

### 2. Adicionar campo `status` na entidade `Ficha`

Em `model/Ficha.java`, apos o campo `descricao`:

```java
@Builder.Default
@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false, length = 20)
private FichaStatus status = FichaStatus.RASCUNHO;
```

### 3. Atualizar `FichaResponse`

Em `dto/response/FichaResponse.java`, adicionar o campo `status`:

```java
// Adicionar ao record
String status
// (mapeado da enum .name())
```

Manter compatibilidade: o campo e adicionado ao final do record para nao quebrar construtores existentes em testes.

### 4. Atualizar `FichaMapper`

Em `mapper/FichaMapper.java`, garantir que o campo `status` seja mapeado para `entity.getStatus().name()`.

### 5. Garantir que `criar()` inicializa com RASCUNHO

Em `FichaService.criar()`, o builder ja usa `@Builder.Default` entao o valor padrao `RASCUNHO` sera usado automaticamente. Verificar que nao ha sobrescrita acidental.

### 6. Adicionar `validarCompletude()` no `FichaValidationService`

```java
public void validarCompletude(Ficha ficha) {
    if (ficha.getRaca() == null) {
        throw new ValidationException("Raca e obrigatoria para completar a ficha.");
    }
    if (ficha.getClasse() == null) {
        throw new ValidationException("Classe e obrigatoria para completar a ficha.");
    }
    if (ficha.getGenero() == null) {
        throw new ValidationException("Genero e obrigatorio para completar a ficha.");
    }
    if (ficha.getIndole() == null) {
        throw new ValidationException("Indole e obrigatoria para completar a ficha.");
    }
    if (ficha.getPresenca() == null) {
        throw new ValidationException("Presenca e obrigatoria para completar a ficha.");
    }
    // Validar RacaClassePermitida (ver T2 — mas incluir aqui se T2 nao estiver pronto)
}
```

### 7. Adicionar `completar()` no `FichaService`

```java
@Transactional
public Ficha completar(Long fichaId) {
    Ficha ficha = buscarPorId(fichaId);
    verificarAcessoEscrita(ficha);

    // Idempotente: ja COMPLETA retorna sem erro
    if (ficha.getStatus() == FichaStatus.COMPLETA) {
        return ficha;
    }

    fichaValidationService.validarCompletude(ficha);
    ficha.setStatus(FichaStatus.COMPLETA);
    return fichaRepository.save(ficha);
}
```

### 8. Adicionar endpoint no `FichaController`

```java
@PutMapping("/api/v1/fichas/{id}/completar")
@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
@Operation(summary = "Completar ficha", description = "Transiciona a ficha de RASCUNHO para COMPLETA. Valida todos os campos obrigatorios.")
public ResponseEntity<FichaResponse> completar(@PathVariable Long id) {
    var ficha = fichaService.completar(id);
    var response = fichaMapper.toResponse(ficha);
    return ResponseEntity.ok(response);
}
```

---

## Testes Obrigatorios

### Testes de integracao em `FichaServiceIntegrationTest`

| Cenario | Given | When | Then |
|---------|-------|------|------|
| Ficha criada comeca como RASCUNHO | Jogo configurado, usuario JOGADOR aprovado | `fichaService.criar(request)` | `ficha.getStatus() == RASCUNHO` |
| Completar com todos os campos | Ficha com raca, classe, genero, indole, presenca | `fichaService.completar(fichaId)` | `status == COMPLETA`, HTTP 200 |
| Completar sem raca | Ficha sem raca | `fichaService.completar(fichaId)` | `ValidationException` com mensagem clara |
| Completar sem classe | Ficha sem classe | `fichaService.completar(fichaId)` | `ValidationException` |
| Idempotencia: completar ja completa | Ficha COMPLETA | `fichaService.completar(fichaId)` | HTTP 200, sem erro |
| JOGADOR outro nao pode completar | Ficha de outro jogador | `fichaService.completar(fichaId)` | `ForbiddenException` |
| FichaResponse inclui campo status | Ficha criada | `fichaMapper.toResponse(ficha)` | `response.status() == "RASCUNHO"` |

---

## Criterios de Aceitacao

- [ ] Enum `FichaStatus` criado com `RASCUNHO` e `COMPLETA`
- [ ] Campo `status` na entity `Ficha` com default `RASCUNHO`
- [ ] `FichaResponse` inclui campo `status` como String
- [ ] `POST /fichas` sempre retorna `status: "RASCUNHO"`
- [ ] `PUT /fichas/{id}/completar` retorna 200 e `status: "COMPLETA"` quando todos os campos estao preenchidos
- [ ] `PUT /fichas/{id}/completar` retorna 422 com mensagem clara quando campos obrigatorios estao faltando
- [ ] `PUT /fichas/{id}/completar` retorna 200 idempotente quando ficha ja esta COMPLETA
- [ ] Testes de integracao cobrindo todos os cenarios acima passando
- [ ] Nenhuma regressao nos 457 testes existentes

---

## Observacoes

- A migration de banco nao e necessaria para testes (H2 in-memory, ddl-auto=create-drop). Para producao, adicionar: `ALTER TABLE fichas ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO';`
- Fichas NPCs tambem comecam como RASCUNHO e podem ser completadas pelo Mestre. A logica de `isNpc` nao altera o comportamento do status.
- Nao implementar restricao de sessao com RASCUNHO nesta task — isso e responsabilidade da Spec 008 (modo sessao).
