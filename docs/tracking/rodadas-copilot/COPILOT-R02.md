# Copilot R02 — Spec 016 (continuação T3+T4+T7) + Spec 011 (backend T0+T1+T2+T4)

> Data: 2026-04-07
> Branch: `main`
> Base: 653 testes backend (pós R01)
> Status: **CONCLUIDA**

---

## Contexto

Lote 2 de execucao paralela. Pre-requisito: R01 mergeado (Spec 014 T1+T5,
Spec 016 T1+T2+T6 existentes). Enquanto Claude Code trabalhava na Spec 017
(RC bloqueante), este lote foi escolhido por nao tocar em `ficha-detail` nem
em layout/header — zero risco de conflito de merge.

7 tasks de 2 specs executadas com paralelismo em waves encadeadas por
dependencia (4 agentes wave 1, depois F e G em wave 2+3).

---

## Tasks Executadas

### [016 T3] ClasseEquipamentoInicial — Sub-recurso de ClassePersonagem

**Agente:** `senior-backend-dev` (Agent A)
**Commit:** `ac41d29`
**Arquivos criados:** 7

Implementado sub-recurso para vincular itens do catalogo como equipamentos
iniciais de uma ClassePersonagem (grupos de escolha + obrigatoriedade).

**Decisao tecnica:** NAO implementa `ConfiguracaoEntity` nem estende
`AbstractConfiguracaoService` — e sub-recurso, nao configuracao standalone.
Service e standalone com validacao de jogo cruzado (RN-T3-01).

**Endpoints:**
- `GET /api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais`
- `POST /api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais`
- `PUT /api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais/{id}`
- `DELETE /api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais/{id}`

---

### [016 T4] FichaItem — Inventario de Ficha

**Agente:** `senior-backend-dev` (Agent B)
**Commit:** `383416b` ⚠️ (merge acidental com 011 T1 — ver Incidentes)
**Arquivos criados:** 9 + 1 teste (18 cenarios)

Vinculo Ficha ↔ ItemConfig com controle de durabilidade, equipamento,
quantidade e peso. Mestre pode adicionar itens de qualquer raridade; Jogador
so pode adicionar se `podeJogadorAdicionar=true`.

**Endpoints (7):**
- `POST /api/v1/fichas/{fichaId}/itens` — adicionar do catalogo
- `POST /api/v1/fichas/{fichaId}/itens/customizado` — item sem ItemConfig (Mestre only)
- `GET /api/v1/fichas/{fichaId}/itens` — listar inventario (separado equipados)
- `PATCH /api/v1/fichas/{fichaId}/itens/{id}/equipar`
- `PATCH /api/v1/fichas/{fichaId}/itens/{id}/desequipar`
- `PATCH /api/v1/fichas/{fichaId}/itens/{id}/durabilidade`
- `DELETE /api/v1/fichas/{fichaId}/itens/{id}`

**TODOs para T5 (bloqueada):** 4 pontos no service onde `recalcularStats()`
devera ser chamado ao equipar/desequipar.

**Testes:** `FichaItemServiceIntegrationTest` — 18 cenarios cobrindo
permissoes, durabilidade, equipamento, inventario com pesoTotal.

---

### [011 T0] AnotacaoPasta entity + CRUD

**Agente:** `senior-backend-dev` (Agent C)
**Commit:** `8595831`
**Arquivos criados:** 7 + 1 teste (10 cenarios)

Hierarquia de pastas para anotacoes (max 3 niveis). Auto-referencial
com `pastaPai` nullable.

**Endpoints:**
- `GET/POST/PUT/DELETE /api/v1/fichas/{fichaId}/pastas`
- `GET /api/v1/fichas/{fichaId}/pastas/arvore`

**Decisao tecnica:** Unique constraint `(ficha_id, pasta_pai_id, nome)` nao
bloqueia duas pastas raiz com mesmo nome no PostgreSQL (NULL ≠ NULL). Validacao
feita no service. Para producao: criar indice parcial `WHERE pasta_pai_id IS NULL`.

**Ajuste no `AnotacaoPastaService.deletar()`:** TODO de T0 completado em T1 —
desvincula `FichaAnotacao` antes de deletar a pasta.

---

### [011 T2] FichaImagem entity + repository

**Agente:** `senior-backend-dev` (Agent D)
**Commit:** `a31067d`
**Arquivos criados:** 4 + modificacoes em pom.xml e properties

Apenas entity + repository (sem service/controller — aguarda T3).

**Enums criados:** `TipoImagem` (AVATAR, GALERIA)

**Dependencia adicionada ao pom.xml:** `cloudinary-http5` v2.3.0
(task spec refencia v1.39.0 que nao existe).

**Campos nao-nulos:** `urlCloudinary`, `publicId` — preenchidos pelo
`CloudinaryUploadService` (T3).

---

### [011 T1] PUT /api/v1/anotacoes/{id}

**Agente:** `senior-backend-dev` (Agent F)
**Commit:** `383416b` (junto com T4 — ver Incidentes)
**Testes:** 20 cenarios (`FichaAnotacaoServiceIntegrationTest`)

Adicionado endpoint PUT para atualizar anotacoes. Adicionados campos ao
modelo: `pastaPai` (ManyToOne AnotacaoPasta, nullable) e `visivelParaTodos`
(Boolean, default false).

**Arquivos modificados:** `FichaAnotacao`, `FichaAnotacaoService`,
`FichaAnotacaoController`, `FichaAnotacaoMapper`, `FichaAnotacaoRepository`

**Adicionado:** `AtualizarAnotacaoRequest` (record com campos opcionais,
NullValuePropertyMappingStrategy.IGNORE no mapper)

---

### [016 T7] Testes de integracao — Sistema de Itens

**Agente:** `senior-backend-dev` (Agent E)
**Commit:** `23138f9`

Complementos + novas classes de teste para cobrir o sistema de itens.

**Adicionados a classes existentes:**
- `RaridadeItemConfigServiceIntegrationTest` — RAR-01 a RAR-05 (+5 cenarios)
- `TipoItemConfigServiceIntegrationTest` — TIPO-01 a TIPO-04 (+4 cenarios)
- `ItemConfigServiceIntegrationTest` — ITEM-05 a ITEM-14 (+9 cenarios, dos 4 pre-existentes + 10 herdados)

**Classes criadas:**
- `ClasseEquipamentoInicialServiceIntegrationTest` — CEI-01 a CEI-05 (5 cenarios)
- `FichaCalculationItemEfeitoIntegrationTest` — skeleton `@Disabled` (aguarda T5)

**Adicoes ao service layer (necessarias para RAR-03 / TIPO-03):**
- `RaridadeItemConfigService.deletar()` — FK guard contra ItemConfig
- `TipoItemConfigService.deletar()` — FK guard contra ItemConfig
- `RaridadeItemConfigRepository` — `existsByJogoIdAndOrdemExibicao`
- `ItemConfigRepository` — `existsByRaridadeId`, `existsByTipoId`

---

### [011 T4] Testes de integracao — Galeria + AnotacaoPasta

**Agente:** `senior-backend-dev` (Agent G)
**Commit:** `d25c56c`

**Adicionados ao `AnotacaoPastaServiceIntegrationTest`** — 4 novos cenarios:
- `deveCriarAnotacaoNaPasta`
- `deveMoverAnotacaoEntrePastas`
- `deveListarAnotacoesFiltrandoPorPasta`
- `deveListarAnotacoesRaiz`

**Criado:** `FichaImagemServiceIntegrationTest` — skeleton `@Disabled` com
Javadoc documentando 21 cenarios pendentes para quando T3 for desbloqueada.

---

## Commits (backend)

| Hash | Mensagem | Task |
|------|----------|------|
| `ac41d29` | `feat(itens): ClasseEquipamentoInicial sub-resource [Spec 016 T3]` | 016 T3 |
| `a31067d` | `feat(galeria): FichaImagem entity + repository [Spec 011 T2]` | 011 T2 |
| `8595831` | `feat(galeria): AnotacaoPasta entity + CRUD [Spec 011 T0]` | 011 T0 |
| `383416b` | `feat(galeria): PUT /api/v1/anotacoes/{id} endpoint [Spec 011 T1]` | 011 T1 + 016 T4 ⚠️ |
| `9d29190` | `chore: remove .claude/ from tracking + add to .gitignore` | corretivo |
| `d25c56c` | `test(galeria): testes integracao backend [Spec 011 T4]` | 011 T4 |
| `23138f9` | `test(itens): testes integração Spec 016 backend [Spec 016 T7]` | 016 T7 |

---

## Estado Final

### Backend (`ficha-controlador`)

| Metrica | Valor |
|---------|-------|
| Testes totais | **719** (0 falhas, 4 skipped) |
| Delta vs R01 | **+66 testes** |
| Working tree | Limpo |
| HEAD | `23138f9` |

---

## Incidentes

### INC-R02-01 — Commit merge acidental T4 + T1

Agentes B (016 T4) e F (011 T1) rodaram em paralelo. Agent F executou
`git add .` capturando os arquivos de FichaItem que Agent B tinha escrito
mas ainda nao commitado. Resultado: 016 T4 e 011 T1 estao no mesmo commit
`383416b`. Codigo 100% correto e funcional; apenas o historico nao e atomico
por task.

**Mitigacao implementada:** Todos os agentes subsequentes passaram a receber
instrucao explicita de usar `git add <arquivos-especificos>` (nunca `git add .`).

### INC-R02-02 — Arquivos .claude/ commitados acidentalmente

No mesmo commit `383416b`, arquivos do agent-memory (`.claude/`) foram
incluidos. Removidos por commit corretivo `9d29190` + `.claude/` adicionado
ao `.gitignore`.

---

## Pendencias / PAs

| ID | Descricao | Bloqueia | Proxima acao |
|----|-----------|---------|--------------|
| PA-R02-01 | **016 T5 bloqueada** — `FichaItemService` tem 4x TODO para `recalcularStats()`; `FichaCalculationItemEfeitoIntegrationTest` @Disabled | Calculo automatico ao equipar | Task T5 (reservada) |
| PA-R02-02 | **011 T3 bloqueada** — `FichaImagemService`/Controller nao criados; `FichaImagemServiceIntegrationTest` @Disabled com 21 cenarios documentados | Upload de galeria | Task T3 (aguarda security review) |
| PA-R02-03 | **Cloudinary versao** — spec referencia v1.39.0 (inexistente); usada v2.3.0 | Nao — sem impacto atual | Verificar API compat ao implementar T3 |
| PA-R02-04 | **Unique constraint pasta raiz** — PostgreSQL NULL != NULL; validacao so no service | Nao — H2 ok; prod sem deploy | Criar indice parcial quando for para producao |
| PA-R02-05 | **`listar(fichaId, null)` retorna tudo** — spec T4 esperava so raiz; service retorna todas sem filtro de pasta | Nao | Ajustar FichaAnotacaoService ao implementar UI |

---

## Paralelismo Utilizado

```
Wave 1 (paralelo):
  Agent A → 016 T3 (ClasseEquipamentoInicial)
  Agent B → 016 T4 (FichaItem)           ← race condition com F (ver INC-R02-01)
  Agent C → 011 T0 (AnotacaoPasta)
  Agent D → 011 T2 (FichaImagem entity)

Wave 2 (apos C):
  Agent F → 011 T1 (PUT anotacao)

Wave 3 (apos D + T1):
  Agent G → 011 T4 (testes galeria)

Wave 3 (apos A + B):
  Agent E → 016 T7 (testes integracao)
```

Total de agentes: 6 ativos + 1 orquestrador

---

*Rodada Copilot R02 encerrada em 2026-04-07.*
