---
name: Spec 011 — Galeria e Anotações: decisões do PO e estado atual
description: Decisões do PO para Cloudinary, Markdown, pastas hierárquicas e visão de NPC para jogadores (revisado 2026-04-03)
type: project
---

Spec 011 criada em 2026-04-02 e revisada em 2026-04-03 com decisões do PO que mudaram o escopo radicalmente.

**Why:** PO forneceu decisões técnicas que substituem o modelo original de URL externa por upload real (Cloudinary), adicionam editor Markdown nas anotações e introduzem hierarquia de pastas (nova entidade AnotacaoPasta).

**How to apply:** Ao trabalhar em tasks da Spec 011, usar sempre Cloudinary (não URL externa), multipart/form-data (não JSON para upload), e considerar AnotacaoPasta (T0) como pré-requisito de T1 e T5.

## O que já existe (backend)

### Anotações: PARCIALMENTE IMPLEMENTADO
- `FichaAnotacao`, `TipoAnotacao`, `FichaAnotacaoRepository`, `FichaAnotacaoService`, `FichaAnotacaoController`, `FichaAnotacaoMapper`, DTOs, 9 testes
- Endpoints: GET, POST, DELETE em `/api/v1/fichas/{fichaId}/anotacoes`
- Lacunas: falta PUT (edição), falta campo `pastaPaiId`, falta campo `visivelParaTodos`

### Galeria: NÃO EXISTE — tudo a criar

## Decisões do PO (2026-04-03)

### Galeria de Imagens
- **Cloudinary** — tier gratuito (25GB storage, 25GB bandwidth, 25 créditos)
- Upload via `multipart/form-data` — backend recebe arquivo e envia ao Cloudinary via SDK
- SDK Java: `com.cloudinary:cloudinary-http5` (versão 1.39.0+)
- Credenciais: `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`
- Pasta upload: `rpg-fichas/{jogoId}/fichas/{fichaId}/`
- Soft delete + destroy no Cloudinary (`cloudinary.uploader().destroy(publicId)`); falha logada mas não reverte
- Modelo: campos `urlCloudinary` + `publicId`. Campo `descricao` removido do MVP

### NPCs — visão restrita para Jogadores
- Listagem de NPCs para Jogadores: apenas `id`, `nome`, `titulo`, `imagemPrincipalUrl`
- `imagemPrincipalUrl` = URL do avatar (FichaImagem com tipoImagem=AVATAR) do NPC
- Nada mais exposto ao Jogador sobre NPCs

### Anotações — Editor Markdown
- Conteúdo em Markdown (TEXT no banco — sem renomear coluna no MVP)
- Renderização frontend: `ngx-markdown` (`npm install ngx-markdown marked`)
- Edição frontend: `<textarea>` nativo — sem split view no MVP
- Alternativa: `marked` direto com DomSanitizer se ngx-markdown incompatível com Angular 21

### Anotações — Pastas Hierárquicas
- Nova entidade `AnotacaoPasta` (auto-referencial, max 3 níveis)
- Unique constraint: `(ficha_id, pasta_pai_id, nome)`
- Deletar pasta: anotações filhas ficam na raiz (pastaPaiId = null) — não são deletadas
- Sub-pastas de pasta deletada também ficam na raiz (MVP — confirmar PA-008)
- Frontend: `p-tree` do PrimeNG

### Novo campo em FichaAnotacao
- `visivelParaTodos: boolean` — qualquer autor pode marcar a própria como compartilhada
- MESTRE pode alterar em qualquer anotação

## Tasks (9 no total, ~9 dias)

| Task | Tipo | Estimativa |
|------|------|-----------|
| T0: Backend AnotacaoPasta entity + CRUD | Backend | 1 dia |
| T1: Backend PUT Anotacao + pastaPaiId | Backend | 1 dia |
| T2: Backend FichaImagem + Cloudinary config | Backend | 0.5 dia |
| T3: Backend service/controller galeria (upload) | Backend | 1.5 dias |
| T4: Backend testes integração (com @MockBean Cloudinary) | Backend | 1 dia |
| T5: Frontend edição Markdown + árv. pastas | Frontend | 1 dia |
| T6: Frontend model + API service (FormData) | Frontend | 0.5 dia |
| T7: Frontend componentes galeria (upload) | Frontend | 2 dias |
| T8: Frontend testes (+40 testes novos) | Frontend | 1.5 dias |

## Pontos em Aberto (aguardam PO)

- **PA-008** CRÍTICO: Sub-pastas ao deletar pasta pai — ficam na raiz ou deletadas em cascata?
- **PA-009**: Limite de 20 imagens é adequado para Mestres com muitos NPCs?
- **PA-010**: Drag-and-drop na galeria é necessário no MVP?
- **PA-011**: Endpoint separado para avatar ou sempre buscar via lista?
- **PA-012**: `visivelParaTodos` também se aplica a pastas ou só a anotações?
