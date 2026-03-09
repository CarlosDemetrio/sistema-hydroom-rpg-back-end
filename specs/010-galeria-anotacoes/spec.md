# Spec 010 — Galeria de Imagens e Anotações

> Status: 📝 Planejado
> Épico: EPIC 9 do EPICS-BACKLOG.md
> Depende de: Spec 006 (Ficha de Personagem)
> Bloqueia: nada

## Contexto

Com o core da Ficha de Personagem pronto, este spec adiciona features de enriquecimento: galeria de imagens para retratos do personagem e um sistema de anotações/diário. Estas são features "nice-to-have" que melhoram significativamente a experiência de roleplay.

## User Stories

### Story 1 — Galeria de Imagens (P3)
**Como** jogador, **quero** fazer upload de imagens do meu personagem para enriquecer a ficha.

**Critérios:**
- POST /api/fichas/{id}/imagens → upload via multipart/form-data
- GET /api/fichas/{id}/imagens → lista imagens da ficha
- PUT /api/fichas/{id}/imagens/{imgId}/avatar → define como imagem principal
- DELETE /api/fichas/{id}/imagens/{imgId} → remove imagem
- Storage: S3 (produção) ou filesystem local (desenvolvimento), configurável por properties

### Story 2 — Anotações do Personagem (P3)
**Como** jogador, **quero** manter um diário das aventuras do meu personagem.

**Critérios:**
- CRUD de anotações vinculadas à ficha
- Campos: título, conteúdo, data da anotação, visibilidade (PUBLICA/PRIVADA)
- Anotações PUBLICAS visíveis para o Mestre; PRIVADAS apenas para o Jogador dono

### Story 3 — Anotações do Mestre (P3)
**Como** mestre, **quero** manter notas privadas sobre cada ficha/jogador.

**Critérios:**
- CRUD de notas do Mestre vinculadas a uma ficha
- Nunca visíveis para o Jogador
- Separadas das anotações do Jogador

## Requisitos Funcionais

| ID | Descrição |
|----|-----------|
| FR-001 | FichaImagem entity: id, fichaId (FK), urlImagem, isAvatar (boolean, default false), ordem (int), descricao (opcional, max 200), BaseEntity |
| FR-002 | POST /api/fichas/{id}/imagens → MultipartFile, cria FichaImagem; aceita image/jpeg, image/png, image/webp |
| FR-003 | GET /api/fichas/{id}/imagens → lista ordenada por ordem asc |
| FR-004 | PUT /api/fichas/{id}/imagens/{imgId}/avatar → isAvatar=true, outros false |
| FR-005 | DELETE /api/fichas/{id}/imagens/{imgId} → soft delete + remove do storage |
| FR-006 | ImageStorageService interface: store(MultipartFile, fichaId) → URL; delete(url) |
| FR-007 | LocalImageStorageService: salva em app.storage.local.path/fichas/{fichaId}/{uuid}.ext |
| FR-008 | S3ImageStorageService: stub (placeholder para produção) com @ConditionalOnProperty |
| FR-009 | Validação: tipo de arquivo deve ser image/jpeg, image/png ou image/webp (rejeitar com 400) |
| FR-010 | Tamanho máximo: 5MB por imagem (spring.servlet.multipart.max-file-size=5MB) |
| FR-011 | VisibilidadeAnotacao enum: PUBLICA, PRIVADA |
| FR-012 | FichaAnotacao entity: id, fichaId, titulo (max 200), conteudo (TEXT), dataAnotacao (LocalDate), visibilidade, BaseEntity |
| FR-013 | CRUD /api/fichas/{id}/anotacoes: POST, GET lista, GET /{aid}, PUT /{aid}, DELETE /{aid} |
| FR-014 | Visibilidade: Jogador dono vê PUBLICA + PRIVADA próprias; Mestre vê apenas PUBLICA; outros 403 |
| FR-015 | AnotacaoMestre entity: id, fichaId, conteudo (TEXT), BaseEntity |
| FR-016 | CRUD /api/fichas/{id}/anotacoes-mestre: POST, GET lista, GET /{amid}, PUT /{amid}, DELETE /{amid} — todos @PreAuthorize("hasRole('MESTRE')") |
| FR-017 | AnotacaoMestre nunca retornada para Jogador |
| FR-018 | FichaImagemResponse record: id, urlImagem, isAvatar, ordem, descricao |
| FR-019 | FichaAnotacaoRequest record: titulo, conteudo, dataAnotacao, visibilidade |
| FR-020 | FichaAnotacaoResponse record: id, titulo, conteudo, dataAnotacao, visibilidade, dataCriacao, dataUltimaAtualizacao |

## Requisitos Não-Funcionais

- ImageStorageService desacoplado via interface (facilitando testes com mock)
- Validação de content-type de imagens (não confiar apenas na extensão)
- LocalImageStorageService com path configurável (não hardcoded)
- S3ImageStorageService pode ser stub — não precisa estar 100% funcional neste spec

## Out of Scope

- OCR ou processamento de imagens
- Compressão/resize automático
- Comentários em anotações
- Versioning de anotações
- Galeria de NPCs (pode ser extensão futura)
