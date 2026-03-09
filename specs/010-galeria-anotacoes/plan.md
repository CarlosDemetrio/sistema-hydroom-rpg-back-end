# Plano de Implementação — Spec 010 (Galeria de Imagens e Anotações)

> Data: Março 2026
> Baseado em: spec.md, EPICS-BACKLOG.md, Spec 006

## Phase 0 — Descoberta

**Fontes consultadas:**
- `docs/EPICS-BACKLOG.md` — EPIC 9
- `specs/006-ficha-personagem/spec.md` — Ficha entity (FK principal)
- `application.properties` — verificar configuração de multipart e storage existente
- AWS config existente no projeto (se houver)

**Estado atual:**
- Sem galeria de imagens
- Sem anotações
- Sem ImageStorageService
- Spring Boot Multipart configurável via properties

## Phase 1 — ImageStorageService

**Objetivo:** Abstrair o storage de imagens para permitir local e S3.

**Tarefas:**
- P1-T1: ImageStorageService interface + LocalImageStorageService + S3ImageStorageService (stub)

## Phase 2 — Galeria de Imagens

**Objetivo:** CRUD de imagens com upload e storage.

**Tarefas:**
- P2-T1: FichaImagem entity + repository + FichaImagemService + FichaImagemController

## Phase 3 — Anotações do Jogador

**Objetivo:** CRUD de anotações com controle de visibilidade.

**Tarefas:**
- P3-T1: FichaAnotacao entity + enum VisibilidadeAnotacao + CRUD completo + regras de visibilidade

## Phase 4 — Anotações do Mestre

**Objetivo:** CRUD de notas privadas do Mestre.

**Tarefas:**
- P4-T1: AnotacaoMestre entity + CRUD restrito a MESTRE

## Phase 5 — Testes

- P5-T1: Testes de integração da galeria (mock ImageStorageService)
- P5-T2: Testes de integração das anotações (visibilidade)

## Ordem de execução

Phase 1 → Phase 2 (depende do storage).
Phases 3 e 4 são independentes entre si.
Phase 5 ao final.

## Riscos

- S3ImageStorageService é um stub — documentar claramente que não está funcional em produção sem configurar credenciais
- Validação de content-type: alguns clientes podem enviar content-type incorreto — validar pelo magic bytes se necessário
- LocalImageStorageService: path deve existir no filesystem antes de usar — criar o diretório se não existir
- Upload de arquivos grandes: configurar spring.servlet.multipart.max-file-size=5MB no application.properties
