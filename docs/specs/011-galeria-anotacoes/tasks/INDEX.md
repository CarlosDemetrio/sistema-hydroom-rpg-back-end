# Tasks — Spec 011: Galeria de Imagens e Anotacoes

> Total: 9 tasks | Estimativa total: 8 dias
> Revisado em: 2026-04-03 (decisoes do PO: Cloudinary, Markdown, pastas hierarquicas)
> Sequencia recomendada: T0 → T1 → T2 → T3 → T4 → T5 → T6 → T7 → T8

---

## Resumo das Tasks

| Task | Titulo | Tipo | Estimativa | Depende de |
|------|--------|------|-----------|-----------|
| [T0](P0-T0-backend-anotacao-pasta-entity.md) | Backend: Entity AnotacaoPasta + CRUD de Pastas | Backend | 1 dia | — |
| [T1](P1-T1-backend-put-anotacao.md) | Backend: PUT Anotacao + pastaPaiId + Markdown | Backend | 1 dia | T0 |
| [T2](P1-T2-backend-ficha-imagem-entity.md) | Backend: Entity FichaImagem + Cloudinary Config | Backend | 0.5 dia | — (paralela a T0) |
| [T3](P1-T3-backend-ficha-imagem-service-controller.md) | Backend: Service + Controller de Galeria (Upload) | Backend | 1.5 dias | T2 |
| [T4](P1-T4-backend-testes-galeria.md) | Backend: Testes de integracao Galeria + Pastas | Backend | 1 dia | T0, T3 |
| [T5](P2-T5-frontend-edicao-anotacao.md) | Frontend: Edicao Markdown + Arvore de Pastas | Frontend | 1 dia | T0, T1 |
| [T6](P2-T6-frontend-galeria-model-api.md) | Frontend: Model + API Service de Galeria (Cloudinary) | Frontend | 0.5 dia | T3 |
| [T7](P2-T7-frontend-galeria-componentes.md) | Frontend: Componentes e Aba Galeria (Upload) | Frontend | 2 dias | T6 |
| [T8](P2-T8-frontend-testes.md) | Frontend: Testes de Galeria, Anotacao e Pastas | Frontend | 1.5 dias | T5, T7 |

---

## Dependencias Visuais

```
T0 ──────────────────────────────────────────── T1 ──── T5 ──── T8
  └──────────────────────────────────────────────────── T4       ↑
                                                          ↑       |
T2 ──→ T3 ──→ T4                                          |       |
         └──→ T6 ──→ T7 ─────────────────────────────────────── T8
```

Fluxo simplificado:
```
T0 (pastas entity)    → T1 (PUT anotacao) → T5 (frontend pastas/markdown) ──┐
T2 (imagem entity)    → T3 (service)      → T6 (model frontend)             ├→ T8 (testes)
                         T3 + T0 → T4 (testes backend)                      │
                                            T6 → T7 (componentes galeria)  ──┘
```

---

## O que Mudou (revisao 2026-04-03)

### Decisoes do PO incorporadas

| Decisao | Tasks afetadas |
|---------|---------------|
| Usar Cloudinary para upload real (nao URL externa) | T2 (renovada), T3 (renovada), T4 (renovada), T6 (renovada), T7 (renovada), T8 (renovada) |
| Editor Markdown (ngx-markdown + textarea) | T5 (renovada), T8 (renovada) |
| Pastas hierarquicas para anotacoes (max 3 niveis) | T0 (nova), T1 (renovada), T5 (renovada), T8 (renovada) |
| NPCs: Jogadores veem apenas nome, titulo e imagemPrincipalUrl | Spec.md atualizada; impacto na Spec 009 (NPC listing endpoint) |

### Tasks novas
- **T0** — `P0-T0-backend-anotacao-pasta-entity.md`: Criada (entidade AnotacaoPasta, hierarquia 3 niveis)

### Tasks removidas
- Nenhuma task foi removida — todas foram renovadas para refletir as novas decisoes

### Mudancas por task

| Task | Status | Principais mudancas |
|------|--------|---------------------|
| T0 | Nova | Entity AnotacaoPasta + CRUD + hierarquia auto-referencial |
| T1 | Renovada | Adicionados: campos pastaPaiId, visivelParaTodos; listar com filtro por pasta |
| T2 | Renovada | SDK Cloudinary adicionado; modelo usa urlCloudinary + publicId (sem url externa, sem descricao) |
| T3 | Renovada | Upload via multipart/form-data; CloudinaryUploadService separado; delete remove do Cloudinary |
| T4 | Renovada | @MockBean CloudinaryUploadService; cenarios de falha de Cloudinary; cenarios de pasta integrados |
| T5 | Renovada | ngx-markdown instalado; textarea para edicao; arvore de pastas com p-tree; visivelParaTodos |
| T6 | Renovada | Model usa UploadImagemDto (File + tipoImagem); FormData em vez de JSON no upload |
| T7 | Renovada | p-fileupload para upload; indicador de progresso; logica de upload via FormData |
| T8 | Renovada | Novos cenarios: upload, Markdown, pastas, visivelParaTodos; estimativa aumentada para 1.5 dias |

---

## Contexto Rapido

- **Anotacoes no backend**: JA IMPLEMENTADAS (listar, criar, deletar). Falta: PUT, campo pastaPaiId, visivelParaTodos.
- **Pastas no backend**: NAO EXISTE. Entity, CRUD e endpoints a criar do zero (T0).
- **Galeria no backend**: NAO EXISTE. Tudo a criar com integracAo Cloudinary (T2, T3, T4).
- **Frontend anotacoes**: JA IMPLEMENTADO (FichaAnotacoesTabComponent). Falta: Markdown, pastas, edicao inline.
- **Frontend galeria**: NAO EXISTE. Tudo a criar com upload via FormData (T6, T7).
- **Armazenamento de imagens**: Cloudinary (tier gratuito). Credenciais via variaveis de ambiente.

---

## Arquivos Principais Afetados

### Backend (novo ou alterado)

```
pom.xml                                              — adicionar cloudinary-http5
application.properties                               — adicionar cloudinary.*
application-test.properties                          — valores ficticios para testes
config/CloudinaryConfig.java                         — novo bean @Bean Cloudinary
model/AnotacaoPasta.java                             — novo
model/FichaImagem.java                               — novo (com urlCloudinary e publicId)
model/enums/TipoImagem.java                          — novo
model/FichaAnotacao.java                             — adicionar pastaPai, visivelParaTodos
repository/AnotacaoPastaRepository.java              — novo
repository/FichaImagemRepository.java                — novo
service/AnotacaoPastaService.java                    — novo (CRUD de pastas + hierarquia)
service/CloudinaryUploadService.java                 — novo (upload/destroy isolados)
service/FichaImagemService.java                      — novo
service/FichaAnotacaoService.java                    — adicionar atualizar(), filtro pasta
controller/AnotacaoPastaController.java              — novo
controller/FichaImagemController.java                — novo (multipart/form-data)
controller/FichaAnotacaoController.java              — adicionar PUT handler, query param pastaPaiId
dto/request/CriarPastaRequest.java                   — novo
dto/request/AtualizarPastaRequest.java               — novo
dto/request/AtualizarAnotacaoRequest.java            — novo
dto/request/UploadImagemRequest.java                 — novo
dto/request/AtualizarImagemRequest.java              — novo
dto/request/CriarAnotacaoRequest.java                — adicionar pastaPaiId, visivelParaTodos
dto/response/AnotacaoPastaResponse.java              — novo
dto/response/FichaImagemResponse.java                — novo (urlCloudinary, publicId)
dto/response/AnotacaoResponse.java                   — adicionar pastaPaiId, visivelParaTodos
mapper/AnotacaoPastaMapper.java                      — novo
mapper/FichaImagemMapper.java                        — novo
mapper/FichaAnotacaoMapper.java                      — adicionar metodo de update
test/AnotacaoPastaServiceIntegrationTest.java        — novo
test/FichaImagemServiceIntegrationTest.java          — novo
test/FichaAnotacaoServiceIntegrationTest.java        — adicionar cenarios de PUT e pasta
```

### Frontend (novo ou alterado)

```
package.json                                          — adicionar ngx-markdown, marked
app.config.ts                                         — provideMarkdown()
core/models/anotacao-pasta.model.ts                  — novo
core/models/ficha-imagem.model.ts                    — novo (UploadImagemDto com File)
core/models/anotacao.model.ts                        — adicionar visivelParaTodos, pastaPaiId
core/models/index.ts                                 — exportar novos models
core/services/api/fichas-api.service.ts              — metodos pasta + editarAnotacao + upload FormData
core/services/business/ficha-business.service.ts     — delegar pasta, edicao, upload
features/.../components/anotacao-card/...            — modo edicao Markdown + visivelParaTodos
features/.../components/ficha-anotacoes-tab/...      — arvore p-tree + filtro por pasta
features/.../components/imagem-card/...              — novo (usa urlCloudinary)
features/.../components/ficha-galeria-tab/...        — novo (upload via FormData + p-fileupload)
features/.../ficha-detail.component.ts               — adicionar aba Galeria
```
