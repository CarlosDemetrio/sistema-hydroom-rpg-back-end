# Plano de Implementacao — Spec 009-ext

> Branch base: `feature/009-npc-fichas-mestre`
> Ultima atualizacao: 2026-04-02

---

## 1. O Que Ja Existe

### Backend (confirmado por leitura de codigo)

| O que existe | Onde | Notas |
|-------------|------|-------|
| `Ficha.descricao` campo TEXT | `model/Ficha.java` linha 96 | Ja implementado com `@Size(max=2000)` |
| `FichaEssencia.essenciaAtual` | `model/FichaEssencia.java` linha 72 | Ja existe como campo persistido |
| `PUT /fichas/{id}/vida` | `FichaController.java` linha 285 | Atualiza `vidaAtual` + `essenciaAtual` + membros |
| `PUT /fichas/{id}/prospeccao` | `FichaController.java` linha 299 | Atualiza quantidade diretamente (semantica genérica) |
| `FichaProspeccao` entity | `model/FichaProspeccao.java` | `fichaId`, `dadoProspeccaoConfigId`, `quantidade` |
| `FichaVida` e `FichaVidaMembro` | model/ | `vidaAtual`, `danoRecebido` ja existem |
| `FichaVidaService.atualizarVida()` | service/ | Ja persiste `essenciaAtual` |
| `GET /jogos/{id}/npcs` (apenas MESTRE) | `FichaController.java` linha 134 | Endpoint protegido por `hasRole('MESTRE')` |
| `POST /jogos/{id}/npcs` | `FichaController.java` linha 143 | Cria NPC com `isNpc=true`, `jogadorId=null` |

### O Que NAO Existe (confirmado como ausente)

| O que falta | Impacto |
|------------|---------|
| `Ficha.visivelGlobalmente` campo | Sem controle de visibilidade global de NPC |
| Entidade `FichaVisibilidade` | Sem visibilidade granular por jogador |
| Entidade `ProspeccaoUso` | Sem semantica de uso/confirmacao/reversao |
| `POST /fichas/{id}/prospeccao/usar` | Jogador nao tem endpoint de uso com semantica correta |
| `PATCH /fichas/{id}/prospeccao/usos/{id}/confirmar` | Mestre nao pode confirmar uso |
| `PATCH /fichas/{id}/prospeccao/usos/{id}/reverter` | Mestre nao pode reverter uso |
| `POST /fichas/{id}/prospeccao/conceder` | Mestre tem apenas o PUT legado |
| `POST /fichas/{id}/resetar-estado` | Sem endpoint de reset |
| `GET /fichas/{id}/visibilidade` | Sem listagem de acesso granular |
| `POST /fichas/{id}/visibilidade` | Sem endpoint para revelar NPC |
| Filtro de NPCs por visibilidade no GET /fichas | Jogador ve todos ou nenhum, sem filtro por FichaVisibilidade |
| `FichaResumoResponse.essenciaAtual` | Verificar se ja esta exposto (provavel que sim via FichaVidaService) |

---

## 2. Sequencia de Implementacao

```
T1 (Backend) — campo visivelGlobalmente em Ficha
     |
     v
T2 (Backend) — FichaVisibilidade entity + endpoints GET/POST/DELETE/PATCH
     |
     v
T3 (Backend) — ProspeccaoUso entity + endpoints usar/confirmar/reverter/conceder
     |
     v
T4 (Backend) — endpoint resetar-estado
     |
     v
T5 (Backend) — verificar FichaResumoResponse expoe essenciaAtual
     |
     v
T6 (Backend) — Testes de integracao para T1 a T5
     |
     v
T7 (Frontend) — toggle visibilidade NPC + painel NpcVisibilidadeComponent
T8 (Frontend) — barra de essencia reativa (independente de T7)
T9 (Frontend) — prospeccao (ProspeccaoJogadorComponent + ProspeccaoMestreComponent)
T10 (Frontend) — painel de reset do Mestre
```

**T7, T8, T9, T10 sao independentes entre si e podem ser paralelizados apos T1-T6.**

---

## 3. Entidade FichaVisibilidade — Detalhes

```java
@Entity
@Table(name = "ficha_visibilidades",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_ficha_visibilidade",
        columnNames = {"ficha_id", "jogador_id"}
    ),
    indexes = {
        @Index(name = "idx_ficha_vis_ficha", columnList = "ficha_id"),
        @Index(name = "idx_ficha_vis_jogador", columnList = "jogador_id")
    })
@SQLRestriction("deleted_at IS NULL")
public class FichaVisibilidade extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @Column(name = "jogador_id", nullable = false)
    private Long jogadorId;
}
```

**Validacao no service:** antes de criar, verificar que `ficha.isNpc() == true`. Se nao for NPC, lancar `ValidationException`.

---

## 4. Entidade ProspeccaoUso — Detalhes

```java
@Entity
@Table(name = "prospeccao_usos",
    indexes = {
        @Index(name = "idx_pros_uso_ficha_pros", columnList = "ficha_prospeccao_id"),
        @Index(name = "idx_pros_uso_status", columnList = "status")
    })
public class ProspeccaoUso extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_prospeccao_id", nullable = false)
    private FichaProspeccao fichaProspeccao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProspeccaoUsoStatus status;
}

enum ProspeccaoUsoStatus { PENDENTE, CONFIRMADO, REVERTIDO }
```

---

## 5. Mudancas em Entidades Existentes

### Ficha — adicionar campo

```java
@Builder.Default
@Column(name = "visivel_globalmente", nullable = false)
private boolean visivelGlobalmente = false;
```

Relevante apenas quando `isNpc = true`. Para fichas de jogadores, manter `false` sempre.

### FichaEssencia — sem mudancas

O campo `essenciaAtual` ja existe. Verificar apenas se `FichaResumoResponse` o expoe.

---

## 6. Mudancas no FichaController

Adicionar ao `FichaController` (ou criar `FichaVisibilidadeController` separado):

```
GET    /api/v1/fichas/{id}/visibilidade              → FichaVisibilidadeService.listar()
POST   /api/v1/fichas/{id}/visibilidade              → FichaVisibilidadeService.atualizar()
DELETE /api/v1/fichas/{id}/visibilidade/{jogadorId}  → FichaVisibilidadeService.revogar()
PATCH  /api/v1/fichas/{id}/visibilidade/global       → FichaVisibilidadeService.atualizarGlobal()
```

Adicionar ao `FichaController` (ou criar `ProspeccaoController` separado):

```
POST   /api/v1/fichas/{id}/prospeccao/conceder            → ProspeccaoService.conceder()
POST   /api/v1/fichas/{id}/prospeccao/usar                → ProspeccaoService.usar()
PATCH  /api/v1/fichas/{id}/prospeccao/usos/{uid}/confirmar → ProspeccaoService.confirmar()
PATCH  /api/v1/fichas/{id}/prospeccao/usos/{uid}/reverter  → ProspeccaoService.reverter()
GET    /api/v1/fichas/{id}/prospeccao/usos                → ProspeccaoService.listarUsos()
GET    /api/v1/jogos/{jogoId}/prospeccao/pendentes        → ProspeccaoService.listarPendentesJogo()
```

Adicionar ao `FichaController`:

```
POST   /api/v1/fichas/{id}/resetar-estado  → FichaVidaService.resetarEstado()
```

---

## 7. Impacto em Endpoints Existentes

### GET /jogos/{jogoId}/fichas — mudanca de comportamento

Atualmente retorna todas as fichas (Mestre) ou apenas as do Jogador (Jogador). Jogadores nao veem NPCs.

**Mudanca:** Jogadores devem ver NPCs com `visivelGlobalmente=true` na listagem, com o campo `jogadorTemAcessoStats` indicando se tem `FichaVisibilidade` ativa.

**Implementacao:** `FichaService.listarComFiltros()` precisa incluir NPCs visiveis para o Jogador solicitante + fazer JOIN com `FichaVisibilidade` para calcular `jogadorTemAcessoStats`.

**Campo novo em FichaResponse:**
```java
Boolean jogadorTemAcessoStats  // null para fichas de jogadores; true/false para NPCs
```

### GET /fichas/{id} — mudanca de comportamento

Atualmente NPCs lancam `ForbiddenException` para Jogadores (conforme `FichaVidaService.verificarAcessoLeitura()`).

**Mudanca:** Verificar `FichaVisibilidade` antes de lancar 403. Se Jogador tem acesso granular ao NPC, permitir acesso.

---

## 8. Estrutura de Arquivos Novos

### Backend

```
model/
  FichaVisibilidade.java
  ProspeccaoUso.java
  enums/
    ProspeccaoUsoStatus.java

repository/
  FichaVisibilidadeRepository.java
  ProspeccaoUsoRepository.java

service/
  FichaVisibilidadeService.java
  ProspeccaoService.java           -- novo service para semantica de uso/confirmacao

dto/request/
  AtualizarVisibilidadeRequest.java   -- { jogadoresIds, substituir }
  AtualizarVisibilidadeGlobalRequest.java  -- { visivelGlobalmente }
  ConcederProspeccaoRequest.java      -- { dadoProspeccaoConfigId, quantidade }
  UsarProspeccaoRequest.java          -- { dadoProspeccaoConfigId }

dto/response/
  FichaVisibilidadeResponse.java      -- { fichaId, visivelGlobalmente, jogadoresComAcesso[] }
  ProspeccaoUsoResponse.java          -- { usoId, dadoNome, status, criadoEm }

mapper/
  FichaVisibilidadeMapper.java
  ProspeccaoUsoMapper.java
```

### Frontend

```
ficha/
  components/
    npc-visibilidade/
      npc-visibilidade.component.ts    [DUMB]
      npc-visibilidade.component.html
    prospeccao-jogador/
      prospeccao-jogador.component.ts  [DUMB]
    prospeccao-mestre/
      prospeccao-mestre.component.ts   [DUMB]
    ficha-reset-estado/
      ficha-reset-estado.component.ts  [DUMB]
  services/
    ficha-visibilidade.api.service.ts
    prospeccao.api.service.ts
```

---

## 9. Riscos e Decisoes Tecnicas

| Risco | Mitigacao |
|-------|-----------|
| N+1 no GET /fichas com JOIN em FichaVisibilidade | Usar projecao (DTO projetado diretamente via JPQL) em vez de carregar entidades completas |
| ProspeccaoUso cresce sem limite em jogos longos | Adicionar indice em `status` + eventual paginacao em listagens. Soft delete nao apaga historico (intencional) |
| Concorrencia: dois Jogadores usam o mesmo tipo de dado simultaneamente | `@Version` ou `SELECT FOR UPDATE` no FichaProspeccao para evitar quantidade negativa |
| Compatibilidade com endpoint PUT /fichas/{id}/prospeccao legado | Manter o endpoint legado funcionando. O novo `POST /prospeccao/conceder` e para concessão com semantica explicita. Documentar no Swagger que o PUT legado sera depreciado |
