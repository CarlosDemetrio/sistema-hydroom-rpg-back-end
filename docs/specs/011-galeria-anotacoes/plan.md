# Plan 011 — Galeria de Imagens e Anotacoes

> Branch sugerido: `feature/011-galeria-anotacoes`
> Baseado em: `feature/009-npc-fichas-mestre` (ou main se mergeado)

---

## 1. O que ja existe

### Backend — Anotacoes (COMPLETO)

Toda a infra de anotacoes esta implementada no branch `feature/009-npc-fichas-mestre`:

- `FichaAnotacao` entity, `TipoAnotacao` enum
- `FichaAnotacaoRepository` com queries de filtragem por tipo, visibilidade e autor
- `FichaAnotacaoService`: `listar()`, `criar()`, `deletar()` — com controle de acesso completo
- `FichaAnotacaoController`: GET, POST, DELETE em `/api/v1/fichas/{fichaId}/anotacoes`
- `FichaAnotacaoMapper`, `CriarAnotacaoRequest`, `AnotacaoResponse`
- `FichaAnotacaoServiceIntegrationTest` com 9 cenarios

**Lacuna unica no backend de anotacoes:** endpoint `PUT` (edicao) nao implementado.

### Frontend — Anotacoes (EXISTENTE, FUNCIONAL)

- `anotacao.model.ts` — `Anotacao`, `CriarAnotacaoDto`, `TipoAnotacao` (alinhados com backend)
- `FichaAnotacoesTabComponent` — lista, form de criacao, delecao, controle por userRole
- `AnotacaoCardComponent` — card com botao de deletar
- `FichaBusinessService` — `loadAnotacoes()`, `criarAnotacao()`, `deletarAnotacao()`

**Lacuna no frontend de anotacoes:** sem edicao inline (botao "Editar" + form in-place no card).

### Backend — Galeria (NAO EXISTE)

Nenhum arquivo relacionado a imagens de ficha existe. Tudo a criar:
- Entity `FichaImagem` + enum `TipoImagem`
- `FichaImagemRepository`
- `FichaImagemService`
- `FichaImagemController`
- DTOs + Mapper
- Testes de integracao

### Frontend — Galeria (NAO EXISTE)

Nenhum componente de galeria existe. Tudo a criar:
- `ficha-imagem.model.ts`
- `FichaGaleriaTabComponent`
- `ImagemCardComponent`
- Extensao do `FichasApiService` (novos endpoints)
- Extensao do `FichaBusinessService` (loadImagens, adicionarImagem, etc.)
- Aba "Galeria" no `FichaDetailPage`

---

## 2. Decisao de Armazenamento de Imagens

**MVP: URL externa**

O backend armazena a URL fornecida pelo usuario. Nenhum arquivo e transferido para o servidor. O frontend exibe a imagem via tag `<img src="url">`.

**Vantagens:**
- Zero infraestrutura de storage no MVP
- Nenhum risco de seguranca de upload
- Implementacao rapida

**Limitacoes conhecidas (aceitaveis para MVP):**
- Imagens externas podem ficar indisponiveis (link quebrado)
- Sem controle de formato ou tamanho

**Validacao do MVP:** `@URL` no DTO (Jakarta Validation). URLs `http://` e `https://` aceitas.

**Fase posterior (fora desta spec):** Upload para S3-compatible storage com pre-signed URLs. Sera uma nova spec (012 ou similar) adicionando `multipart/form-data` endpoint.

---

## 3. Sequencia de Implementacao

A sequencia abaixo minimiza dependencias e permite testes incrementais.

### Fase 1 — Backend: PUT Anotacao (gap pequeno, alto valor)

Implementar o endpoint `PUT /api/v1/fichas/{fichaId}/anotacoes/{id}`.

- Adicionar `AtualizarAnotacaoRequest` DTO
- Adicionar metodo `atualizar()` no `FichaAnotacaoService`
- Adicionar handler `PUT` no `FichaAnotacaoController`
- Adicionar `@Mapping` no `FichaAnotacaoMapper` para update (NullValuePropertyMappingStrategy.IGNORE)
- Adicionar cenarios de teste em `FichaAnotacaoServiceIntegrationTest`

Estimativa: 0.5 dia.

### Fase 2 — Backend: Galeria de Imagens (novo modulo)

2.1. Entity e Repository:
  - Criar `TipoImagem.java` enum (`AVATAR`, `GALERIA`)
  - Criar `FichaImagem.java` entity (extends BaseEntity)
  - Criar `FichaImagemRepository.java`

2.2. DTOs:
  - `AdicionarImagemRequest.java` (url, titulo, descricao, tipoImagem)
  - `AtualizarImagemRequest.java` (titulo, descricao, ordemExibicao)
  - `FichaImagemResponse.java`

2.3. Mapper:
  - `FichaImagemMapper.java` (MapStruct, IGNORE nulls no update)

2.4. Service:
  - `FichaImagemService.java`:
    - `listar(fichaId)` — controle de acesso + ordem (AVATAR primeiro)
    - `adicionar(fichaId, request, autorId)` — promove avatar anterior para GALERIA
    - `atualizar(fichaId, imagemId, request, autorId)` — edita campos opcionais
    - `deletar(fichaId, imagemId, autorId)` — soft delete

2.5. Controller:
  - `FichaImagemController.java` (GET, POST, PUT, DELETE)

Estimativa: 1.5 dias.

### Fase 3 — Testes de Integracao do Backend

- `FichaImagemServiceIntegrationTest.java`:
  - Listar imagens (MESTRE, JOGADOR proprio, JOGADOR alheio — 403)
  - Adicionar AVATAR (promove anterior para GALERIA)
  - Limite de 20 imagens (retorna 422)
  - URL invalida (retorna 400)
  - Edicao de titulo/descricao
  - Soft delete
  - NPC: somente MESTRE

Estimativa: 0.5 dia.

### Fase 4 — Frontend: Edicao de Anotacao

- Adicionar `AtualizarAnotacaoDto` em `anotacao.model.ts`
- Adicionar `editarAnotacao(fichaId, anotacaoId, dto)` no `FichasApiService`
- Adicionar `editarAnotacao(fichaId, anotacaoId, dto)` no `FichaBusinessService`
- Adicionar modo de edicao inline no `AnotacaoCardComponent`:
  - Botao "Editar" visivel para: MESTRE ou (JOGADOR e autor == userId)
  - Ao clicar "Editar": campos de titulo e conteudo ficam editaveis in-place
  - MESTRE: ver toggle adicional para `visivelParaJogador`
  - Botao "Salvar edicao" + "Cancelar"

Estimativa: 0.5 dia.

### Fase 5 — Frontend: Galeria de Imagens

5.1. Model:
  - `ficha-imagem.model.ts` (`FichaImagem`, `AdicionarImagemDto`, `AtualizarImagemDto`, `TipoImagem`)

5.2. API Service:
  - Adicionar metodos em `FichasApiService`:
    - `getImagens(fichaId)`
    - `adicionarImagem(fichaId, dto)`
    - `atualizarImagem(fichaId, imagemId, dto)`
    - `deletarImagem(fichaId, imagemId)`

5.3. Business Service:
  - Adicionar metodos em `FichaBusinessService`:
    - `loadImagens(fichaId)`
    - `adicionarImagem(fichaId, dto)`
    - `atualizarImagem(fichaId, imagemId, dto)`
    - `deletarImagem(fichaId, imagemId)`

5.4. Componentes:
  - `ImagemCardComponent` [DUMB]:
    - Exibe imagem (tag `<img>`)
    - Titulo abaixo
    - Badge "Avatar" se tipoImagem=AVATAR
    - Botoes: "Definir como avatar" (se GALERIA), "Editar", "Deletar"
  - `FichaGaleriaTabComponent` [SMART]:
    - Input: `fichaId`, `userRole`, `userId`
    - Carrega imagens ao abrir
    - Exibe avatar em destaque (maior) + grade de galeria
    - Form inline "Adicionar imagem" (URL, titulo, tipo)
    - Contador "X/20 imagens"
    - Empty state com CTA
    - Loading skeleton

5.5. Integracao no FichaDetailPage:
  - Adicionar aba "Galeria" ao TabView do `FichaDetailPage`
  - Passar `fichaId`, `userRole`, `userId` para `FichaGaleriaTabComponent`

Estimativa: 2 dias.

### Fase 6 — Testes do Frontend

- `ficha-imagem.model.spec.ts` (se houver logica no model)
- `imagem-card.component.spec.ts` — renderizacao, botoes por role
- `ficha-galeria-tab.component.spec.ts` — carregamento, adicao, delecao, empty state, limite 20
- `anotacao-card.component.spec.ts` — modo edicao inline
- Extensao de `ficha-business.service.spec.ts` com metodos de imagem

Estimativa: 1 dia.

---

## 4. Total de Esforco Estimado

| Fase | Descricao | Estimativa |
|------|-----------|-----------|
| 1 | Backend: PUT Anotacao | 0.5 dia |
| 2 | Backend: Galeria (entity → controller) | 1.5 dias |
| 3 | Backend: Testes de integracao Galeria | 0.5 dia |
| 4 | Frontend: Edicao inline de Anotacao | 0.5 dia |
| 5 | Frontend: Galeria completa | 2 dias |
| 6 | Frontend: Testes | 1 dia |
| **Total** | | **6 dias** |

---

## 5. Ordem de Tasks

```
T1 → T2 → T3       (backend de anotacoes completo)
T4 → T5 → T6       (backend de galeria completo)
T7 (frontend)      (pode comecar em paralelo com T4/T5/T6 se backend mockado)
T8 (testes)        (apos T1-T7)
```

> T7 pode comecar com mocks antes do backend estar pronto (Vitest), mas integracao real depende de T1-T6 deployados.

---

*Produzido por: Business Analyst/PO | 2026-04-02 | Plan 011*
