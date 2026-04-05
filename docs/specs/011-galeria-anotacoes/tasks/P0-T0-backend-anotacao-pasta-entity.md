# T0 — Backend: Entity AnotacaoPasta + Repository + CRUD Basico

> Fase: P0 (Pre-requisito — deve ser implementada antes de T1)
> Estimativa: 1 dia
> Depende de: nenhuma
> Bloqueia: T1 (Backend: PUT Anotacao + pastaPaiId)

---

## Objetivo

Criar a entidade `AnotacaoPasta`, seu repository e o CRUD completo de pastas de anotacoes. Pastas suportam hierarquia auto-referencial de ate 3 niveis de profundidade, vinculadas a uma ficha.

Esta task e pre-requisito de T1 porque `FichaAnotacao` tera FK para `AnotacaoPasta`. A entity e os endpoints de pasta precisam existir antes de adicionar o campo `pastaPaiId` nas anotacoes.

---

## Regras de Negocio

| Regra | Detalhe |
|-------|---------|
| Pastas sao vinculadas a uma ficha | `ficha_id NOT NULL` |
| Nivel maximo de hierarquia: 3 | Raiz (nivel 0) > Nivel 1 > Nivel 2 > Nivel 3. Criar pasta filha de nivel 3: HTTP 422 |
| Nivel calculado em runtime | Nao armazenar `nivel` no banco — calcular via traversal ao validar criacao |
| Nomes unicos por pai dentro da mesma ficha | Unique constraint `(ficha_id, pasta_pai_id, nome)` |
| Soft delete nao deleta anotacoes filhas | Anotacoes com `pasta_pai_id` apontando para a pasta deletada ficam com `pasta_pai_id = null` (raiz) |
| Sub-pastas de pasta deletada | Decisao MVP: sub-pastas tambem ficam na raiz ao deletar a pasta pai (desaninhamento em cascata — confirmar com PO via PA-008) |
| MESTRE opera em qualquer ficha | Incluindo fichas de NPC |
| JOGADOR opera apenas na propria ficha | Igual a regra de anotacoes |

---

## Arquivos a Criar

### 1. `AnotacaoPasta.java`

```
src/main/java/br/com/hydroom/rpg/fichacontrolador/model/AnotacaoPasta.java
```

```java
@Entity
@Table(name = "anotacao_pastas",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_anotacao_pasta_ficha_pai_nome",
        columnNames = {"ficha_id", "pasta_pai_id", "nome"}
    ),
    indexes = {
        @Index(name = "idx_anotacao_pasta_ficha", columnList = "ficha_id"),
        @Index(name = "idx_anotacao_pasta_pai",   columnList = "pasta_pai_id")
    }
)
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnotacaoPasta extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @Column(nullable = false, length = 100)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pasta_pai_id")
    private AnotacaoPasta pastaPai;     // null = pasta de nivel raiz

    @Column(name = "ordem_exibicao", nullable = false)
    @Builder.Default
    private Integer ordemExibicao = 0;
}
```

### 2. `AnotacaoPastaRepository.java`

```
src/main/java/.../repository/AnotacaoPastaRepository.java
```

```java
public interface AnotacaoPastaRepository extends JpaRepository<AnotacaoPasta, Long> {

    // Lista pastas raiz de uma ficha (sem pai)
    List<AnotacaoPasta> findByFichaIdAndPastaPaiIsNullOrderByOrdemExibicaoAsc(Long fichaId);

    // Lista sub-pastas diretas de uma pasta
    List<AnotacaoPasta> findByPastaPaiIdOrderByOrdemExibicaoAsc(Long pastaPaiId);

    // Verifica existencia por nome (para unique constraint manual se necessario)
    boolean existsByFichaIdAndPastaPaiIdAndNome(Long fichaId, Long pastaPaiId, String nome);

    // Lista todas as pastas de uma ficha (para construir arvore no service)
    List<AnotacaoPasta> findByFichaIdOrderByOrdemExibicaoAsc(Long fichaId);
}
```

### 3. DTOs

**`CriarPastaRequest.java`**
```java
public record CriarPastaRequest(
    @NotBlank(message = "Nome da pasta e obrigatorio")
    @Size(max = 100, message = "Nome deve ter no maximo 100 caracteres")
    String nome,

    Long pastaPaiId,   // null = criar na raiz

    Integer ordemExibicao
) {}
```

**`AtualizarPastaRequest.java`**
```java
public record AtualizarPastaRequest(
    @Size(max = 100)
    String nome,

    Integer ordemExibicao
) {}
```

**`AnotacaoPastaResponse.java`**
```java
public record AnotacaoPastaResponse(
    Long id,
    Long fichaId,
    String nome,
    Long pastaPaiId,
    Integer ordemExibicao,
    List<AnotacaoPastaResponse> subPastas,  // null ou vazio em contextos sem carga de arvore
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
```

### 4. `AnotacaoPastaMapper.java`

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AnotacaoPastaMapper {

    @Mapping(target = "fichaId", source = "ficha.id")
    @Mapping(target = "pastaPaiId", source = "pastaPai.id")
    @Mapping(target = "dataCriacao", source = "createdAt")
    @Mapping(target = "dataUltimaAtualizacao", source = "updatedAt")
    @Mapping(target = "subPastas", ignore = true)   // carregado separadamente no service
    AnotacaoPastaResponse toResponse(AnotacaoPasta pasta);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void atualizarEntidade(AtualizarPastaRequest request, @MappingTarget AnotacaoPasta pasta);
}
```

### 5. `AnotacaoPastaService.java`

Metodos:

**`listarArvore(fichaId, usuarioAtualId)`**
1. Verificar acesso (isMestre ou dono da ficha)
2. Buscar todas as pastas da ficha: `findByFichaIdOrderByOrdemExibicaoAsc(fichaId)`
3. Montar arvore em memoria: pastas com `pastaPaiId=null` sao raiz; sub-pastas aninhadas recursivamente
4. Retornar lista de `AnotacaoPastaResponse` com `subPastas` preenchido

**`criar(fichaId, request, usuarioAtualId)`**
1. Verificar acesso
2. Se `request.pastaPaiId` nao e null:
   - Buscar `pastaPai` — se nao encontrar ou se `pastaPai.ficha.id != fichaId`: `ForbiddenException`
   - Calcular nivel do pai: traversal (pai.pastaPai?.pastaPai != null → nivel 3 → HTTP 422)
3. Verificar unique: `(fichaId, pastaPaiId, nome)` — se ja existe: `DuplicateResourceException`
4. Criar e salvar

**`atualizar(fichaId, pastaId, request, usuarioAtualId)`**
1. Verificar acesso e pertencia a ficha
2. Aplicar campos via mapper (IGNORE nulls)
3. Se novo `nome` conflita com irmaos: `DuplicateResourceException`
4. Salvar

**`deletar(fichaId, pastaId, usuarioAtualId)`**
1. Verificar acesso
2. Soft delete da pasta: `pasta.delete()` + save
3. Desanestrar sub-pastas diretas: buscar `findByPastaPaiIdOrderByOrdemExibicaoAsc(pastaId)` e setar `pastaPai = null` + save em cada uma
4. Desanestrar anotacoes vinculadas: buscar `FichaAnotacao` com `pastaPai.id == pastaId` e setar `pastaPai = null` + save em cada uma

### 6. `AnotacaoPastaController.java`

```
@RestController
@RequestMapping("/api/v1/fichas/{fichaId}/anotacao-pastas")
@Tag(name = "Pastas de Anotacoes", description = "Hierarquia de pastas para organizacao de anotacoes")
```

| Metodo | Endpoint | Role | Retorno |
|--------|----------|------|---------|
| GET | `/` | MESTRE, JOGADOR | `List<AnotacaoPastaResponse>` (arvore) 200 |
| POST | `/` | MESTRE, JOGADOR | `AnotacaoPastaResponse` 201 |
| PUT | `/{id}` | MESTRE, JOGADOR | `AnotacaoPastaResponse` 200 |
| DELETE | `/{id}` | MESTRE, JOGADOR | `void` 204 |

---

## Testes de Integracao a Criar

Arquivo: `AnotacaoPastaServiceIntegrationTest.java`

| Cenario | Descricao |
|---------|-----------|
| `deveCriarPastaRaiz` | Pasta sem pai criada com sucesso |
| `deveCriarSubPastaDeNivel2` | Sub-pasta de pasta raiz criada |
| `deveCriarSubPastaDeNivel3` | Sub-pasta de sub-pasta criada |
| `deveImpedirCriacaoDeNivel4` | Tentar criar filho de pasta nivel 3: BusinessException |
| `deveImpedirNomeDuplicadoNoPai` | Dois pastas irmas com mesmo nome: DuplicateResourceException |
| `deveDeletarPastaDesaninhando` | Deletar pasta pai: sub-pastas ficam na raiz |
| `deveDeletarPastaDesaninhando Anotacoes` | Deletar pasta: anotacoes filhas ficam com pastaPai=null |
| `deveListarArvoreCompleta` | listarArvore retorna estrutura com subPastas aninhadas |
| `deveImpedirJogadorEmFichaAlheia` | Jogador tenta operar em pasta de ficha alheia: ForbiddenException |
| `devePermitirMestreEmQualquerFicha` | Mestre cria pasta em ficha de NPC |

---

## Criterios de Aceite

- [ ] `AnotacaoPasta.java` estende `BaseEntity`, tem auto-referencia `pastaPai`, soft delete
- [ ] Unique constraint `(ficha_id, pasta_pai_id, nome)` implementada e testada
- [ ] Tentativa de criar sub-pasta de nivel 4 retorna HTTP 422
- [ ] Ao deletar pasta, sub-pastas e anotacoes filhas ficam na raiz (desaninhamento)
- [ ] `GET /fichas/{fichaId}/anotacao-pastas` retorna arvore com subPastas aninhadas
- [ ] `POST`, `PUT`, `DELETE` funcionam com controle de acesso correto por role
- [ ] Todos os testes de integracao passam
- [ ] Schema H2 sobe sem erros (auto-referencia em H2 suportada)
