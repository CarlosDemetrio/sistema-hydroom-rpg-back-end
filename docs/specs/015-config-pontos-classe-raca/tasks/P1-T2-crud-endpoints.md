# T2 — CRUD Endpoints Sub-recurso (14 endpoints) + Testes de Integracao

> Fase: Backend | Prioridade: P1
> Dependencias: T1 (entidades, repositories, DTOs, mappers)
> Bloqueia: T6, T7 (frontend precisa dos endpoints)
> Estimativa: 6–8 horas

---

## Objetivo

Criar os services CRUD e endpoints REST para as 4 novas entidades como sub-recursos de `ClassePersonagem` e `Raca`. Sao 14 endpoints no total (7 para Classe, 7 para Raca). Inclui testes de integracao para cada endpoint.

---

## Contexto

O padrao de sub-recurso ja existe no projeto:
- `ClasseBonus` como sub-recurso de `ClassePersonagem` — endpoints em `ClassePersonagemController`
- `RacaBonusAtributo` como sub-recurso de `Raca` — endpoints em `RacaController`

Seguir exatamente o mesmo padrao para os novos sub-recursos.

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `service/ClassePontosConfigService.java` | CRIAR |
| `service/ClasseVantagemPreDefinidaService.java` | CRIAR |
| `service/RacaPontosConfigService.java` | CRIAR |
| `service/RacaVantagemPreDefinidaService.java` | CRIAR |
| `controller/configuracao/ClassePersonagemController.java` | EDITAR — adicionar 7 endpoints |
| `controller/configuracao/RacaController.java` | EDITAR — adicionar 7 endpoints |
| Testes de integracao (novos) | CRIAR |

---

## Passos de Implementacao

### Passo 1 — Services

**`ClassePontosConfigService.java`**

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClassePontosConfigService {

    private final ClassePontosConfigRepository repository;
    private final ClassePersonagemRepository classeRepository;

    public List<ClassePontosConfig> listarPorClasse(Long classeId) {
        return repository.findByClassePersonagemIdOrderByNivel(classeId);
    }

    @Transactional
    public ClassePontosConfig criar(Long classeId, ClassePontosConfig pontosConfig) {
        ClassePersonagem classe = classeRepository.findById(classeId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("ClassePersonagem", classeId));

        // Validar duplicata de nivel
        if (repository.existsByClassePersonagemIdAndNivel(classeId, pontosConfig.getNivel())) {
            throw new RegraNegocioException(
                "Ja existe configuracao de pontos para o nivel " + pontosConfig.getNivel()
                + " na classe " + classe.getNome());
        }

        pontosConfig.setClassePersonagem(classe);
        return repository.save(pontosConfig);
    }

    @Transactional
    public ClassePontosConfig atualizar(Long classeId, Long pontosConfigId,
                                         ClassePontosConfig atualizado) {
        ClassePontosConfig existente = repository.findById(pontosConfigId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("ClassePontosConfig", pontosConfigId));

        // Validar que pertence a classe informada
        if (!existente.getClassePersonagem().getId().equals(classeId)) {
            throw new RegraNegocioException("ClassePontosConfig nao pertence a classe informada");
        }

        // Validar duplicata de nivel se mudou
        if (!existente.getNivel().equals(atualizado.getNivel())
            && repository.existsByClassePersonagemIdAndNivel(classeId, atualizado.getNivel())) {
            throw new RegraNegocioException(
                "Ja existe configuracao de pontos para o nivel " + atualizado.getNivel());
        }

        existente.setNivel(atualizado.getNivel());
        existente.setPontosAtributo(atualizado.getPontosAtributo());
        existente.setPontosAptidao(atualizado.getPontosAptidao());
        existente.setPontosVantagem(atualizado.getPontosVantagem());
        return repository.save(existente);
    }

    @Transactional
    public void deletar(Long classeId, Long pontosConfigId) {
        ClassePontosConfig existente = repository.findById(pontosConfigId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("ClassePontosConfig", pontosConfigId));

        if (!existente.getClassePersonagem().getId().equals(classeId)) {
            throw new RegraNegocioException("ClassePontosConfig nao pertence a classe informada");
        }

        existente.delete(); // soft delete
        repository.save(existente);
    }
}
```

Seguir padrao identico para:
- `ClasseVantagemPreDefinidaService` — com validacao adicional: `vantagemConfig` deve pertencer ao mesmo jogo da classe (RN-015-06)
- `RacaPontosConfigService` — identico ao ClassePontosConfigService mas com `Raca` como pai
- `RacaVantagemPreDefinidaService` — identico ao ClasseVantagemPreDefinidaService mas com `Raca` como pai

**Validacao especial para VantagemPreDefinida (ambos services):**

```java
@Transactional
public ClasseVantagemPreDefinida criar(Long classeId, ClasseVantagemPreDefinidaRequest request) {
    ClassePersonagem classe = classeRepository.findById(classeId)
        .orElseThrow(() -> new RecursoNaoEncontradoException("ClassePersonagem", classeId));

    VantagemConfig vantagem = vantagemConfigRepository.findById(request.vantagemConfigId())
        .orElseThrow(() -> new RecursoNaoEncontradoException("VantagemConfig", request.vantagemConfigId()));

    // RN-015-06: Validar mesmo jogo
    if (!vantagem.getJogo().getId().equals(classe.getJogo().getId())) {
        throw new RegraNegocioException("VantagemConfig deve pertencer ao mesmo jogo da classe");
    }

    // Validar duplicata
    if (repository.existsByClassePersonagemIdAndNivelAndVantagemConfigId(
            classeId, request.nivel(), request.vantagemConfigId())) {
        throw new RegraNegocioException(
            "Vantagem '" + vantagem.getNome() + "' ja esta pre-definida para o nivel " + request.nivel());
    }

    ClasseVantagemPreDefinida predefinida = ClasseVantagemPreDefinida.builder()
        .classePersonagem(classe)
        .nivel(request.nivel())
        .vantagemConfig(vantagem)
        .build();

    return repository.save(predefinida);
}
```

---

### Passo 2 — Endpoints em ClassePersonagemController

Adicionar ao controller existente (`ClassePersonagemController.java`):

```java
// ===== Sub-recurso: Pontos por Nivel =====

@GetMapping("/{id}/pontos-config")
@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
public ResponseEntity<List<ClassePontosConfigResponse>> listarPontosConfig(@PathVariable Long id) {
    List<ClassePontosConfig> pontos = classePontosConfigService.listarPorClasse(id);
    return ResponseEntity.ok(classePontosConfigMapper.toResponseList(pontos));
}

@PostMapping("/{id}/pontos-config")
@PreAuthorize("hasRole('MESTRE')")
public ResponseEntity<ClassePontosConfigResponse> criarPontosConfig(
        @PathVariable Long id,
        @Valid @RequestBody ClassePontosConfigRequest request) {
    ClassePontosConfig entity = classePontosConfigMapper.toEntity(request);
    ClassePontosConfig salvo = classePontosConfigService.criar(id, entity);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(classePontosConfigMapper.toResponse(salvo));
}

@PutMapping("/{id}/pontos-config/{pontosConfigId}")
@PreAuthorize("hasRole('MESTRE')")
public ResponseEntity<ClassePontosConfigResponse> atualizarPontosConfig(
        @PathVariable Long id,
        @PathVariable Long pontosConfigId,
        @Valid @RequestBody ClassePontosConfigRequest request) {
    ClassePontosConfig entity = classePontosConfigMapper.toEntity(request);
    ClassePontosConfig atualizado = classePontosConfigService.atualizar(id, pontosConfigId, entity);
    return ResponseEntity.ok(classePontosConfigMapper.toResponse(atualizado));
}

@DeleteMapping("/{id}/pontos-config/{pontosConfigId}")
@PreAuthorize("hasRole('MESTRE')")
public ResponseEntity<Void> deletarPontosConfig(
        @PathVariable Long id,
        @PathVariable Long pontosConfigId) {
    classePontosConfigService.deletar(id, pontosConfigId);
    return ResponseEntity.noContent().build();
}

// ===== Sub-recurso: Vantagens Pre-definidas =====

@GetMapping("/{id}/vantagens-predefinidas")
@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
public ResponseEntity<List<ClasseVantagemPreDefinidaResponse>> listarVantagensPreDefinidas(
        @PathVariable Long id) {
    List<ClasseVantagemPreDefinida> vantagens = classeVantagemPreDefinidaService.listarPorClasse(id);
    return ResponseEntity.ok(classeVantagemPreDefinidaMapper.toResponseList(vantagens));
}

@PostMapping("/{id}/vantagens-predefinidas")
@PreAuthorize("hasRole('MESTRE')")
public ResponseEntity<ClasseVantagemPreDefinidaResponse> criarVantagemPreDefinida(
        @PathVariable Long id,
        @Valid @RequestBody ClasseVantagemPreDefinidaRequest request) {
    ClasseVantagemPreDefinida salvo = classeVantagemPreDefinidaService.criar(id, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(classeVantagemPreDefinidaMapper.toResponse(salvo));
}

@DeleteMapping("/{id}/vantagens-predefinidas/{predefinidaId}")
@PreAuthorize("hasRole('MESTRE')")
public ResponseEntity<Void> deletarVantagemPreDefinida(
        @PathVariable Long id,
        @PathVariable Long predefinidaId) {
    classeVantagemPreDefinidaService.deletar(id, predefinidaId);
    return ResponseEntity.noContent().build();
}
```

### Passo 3 — Endpoints em RacaController

Adicionar endpoints identicos ao `RacaController`, substituindo:
- `ClassePersonagem` → `Raca`
- `classeId` → `racaId`
- URLs: `/api/v1/racas/{id}/pontos-config` e `/api/v1/racas/{id}/vantagens-predefinidas`

---

## Testes de Integracao

### Cenario T2-01 — CRUD ClassePontosConfig

```
Dado: ClassePersonagem "Guerreiro" existente
Quando: POST /classes/{id}/pontos-config com {nivel: 1, pontosAtributo: 2, pontosAptidao: 0, pontosVantagem: 1}
Entao: Status 201, response contem dados corretos
E: GET /classes/{id}/pontos-config retorna 1 item
```

### Cenario T2-02 — Duplicata de nivel rejeitada

```
Dado: ClassePontosConfig para nivel 1 ja existe na classe "Guerreiro"
Quando: POST /classes/{id}/pontos-config com {nivel: 1, ...}
Entao: Status 409 (Conflict) com mensagem de erro
```

### Cenario T2-03 — VantagemPreDefinida validacao de jogo

```
Dado: ClassePersonagem "Guerreiro" no Jogo A
E: VantagemConfig "Fortitude" no Jogo B
Quando: POST /classes/{id}/vantagens-predefinidas com {nivel: 5, vantagemConfigId: fortitude.id}
Entao: Status 400 com mensagem "VantagemConfig deve pertencer ao mesmo jogo da classe"
```

### Cenario T2-04 — CRUD RacaPontosConfig

```
Dado: Raca "Elfo" existente
Quando: POST /racas/{id}/pontos-config com {nivel: 1, pontosAtributo: 1, pontosAptidao: 1, pontosVantagem: 0}
Entao: Status 201, response contem dados corretos
```

### Cenario T2-05 — Soft delete funciona

```
Dado: ClassePontosConfig para nivel 5 existente
Quando: DELETE /classes/{id}/pontos-config/{pontosConfigId}
Entao: Status 204
E: GET /classes/{id}/pontos-config NAO retorna o item deletado
```

### Cenario T2-06 — PUT atualiza corretamente

```
Dado: ClassePontosConfig para nivel 1 com pontosAtributo=2
Quando: PUT /classes/{id}/pontos-config/{pontosConfigId} com {nivel: 1, pontosAtributo: 5, pontosAptidao: 0, pontosVantagem: 0}
Entao: Status 200, pontosAtributo == 5
```

### Cenario T2-07 — Seguranca: JOGADOR nao pode criar

```
Dado: Usuario com role JOGADOR
Quando: POST /classes/{id}/pontos-config com dados validos
Entao: Status 403 (Forbidden)
```

---

## Criterios de Aceitacao

- [ ] 4 services CRUD criados seguindo padrao `@Transactional(readOnly = true)` na classe
- [ ] 14 endpoints REST funcionando (7 Classe + 7 Raca)
- [ ] Seguranca: `@PreAuthorize("hasRole('MESTRE')")` em POST/PUT/DELETE
- [ ] Seguranca: `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")` em GET
- [ ] Validacao de duplicata de nivel (409 Conflict)
- [ ] Validacao de mesmo jogo para VantagemPreDefinida (400 Bad Request)
- [ ] Soft delete via `.delete()` — nunca deletar fisicamente
- [ ] Validacao de pertencimento (ClassePontosConfig pertence a classe informada)
- [ ] Cenarios T2-01 a T2-07 passam como testes de integracao
- [ ] `./mvnw test` passa (testes existentes + novos)

---

*Produzido por: PM/Scrum Master | 2026-04-04*
