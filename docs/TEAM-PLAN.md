# Klayrah RPG — Team Plan (Backend + Frontend)

> Gerado em: 2026-03-31  
> Branch de trabalho: `feature/009-npc-fichas-mestre` (backend) / `main` (frontend)

## Time

| Papel | Responsabilidade |
|-------|-----------------|
| **Tech Lead** | Arquitetura, revisão de PRs, padrões de código, decisões técnicas |
| **Backend Dev 1** | Segurança, role checks, endpoints faltantes em configurações |
| **Backend Dev 2** | Features novas: anotações, histórico, updates diretos de ficha |
| **Backend Dev 3** | Spec 009 (NPC, duplicação de ficha), participants, user profile |
| **Frontend Dev** | Camada de API, integração com backend, state management |
| **UI/UX Specialist** | Design system, componentes PrimeNG, páginas de configuração |
| **QA** | Testes de integração backend, testes de componente frontend |

---

## Portas Docker (sem conflito)

| Serviço | Porta Original | Nova Porta |
|---------|---------------|------------|
| PostgreSQL | 5432 | 5453 |
| Backend Spring Boot | 8080 | 8181 |
| Frontend Angular | 4200 | 4201 |
| Frontend Live Reload | 49153 | 49173 |

---

## FASE 0 — Infraestrutura (imediato)

| Issue | Descrição | Responsável |
|-------|-----------|-------------|
| INFRA-001 | Atualizar portas no compose.yaml | Tech Lead |
| INFRA-002 | Criar proxy.conf.local.json para dev local (localhost:8181) | Frontend Dev |
| INFRA-003 | Atualizar script npm start:local no package.json | Frontend Dev |
| INFRA-004 | Atualizar CORS no backend para aceitar 4201 | Backend Dev 1 |

---

## FASE 1 — Backend: Segurança & Correções Críticas

### ISSUE-B001: Role checks em todos os controllers de configuração
**Descrição:** 17 controllers de configuração não têm `@PreAuthorize` consistente. Criação/atualização/deleção deve ser MESTRE, leitura MESTRE ou JOGADOR.  
**Arquivos:** `src/main/java/.../controller/configuracao/*Controller.java` (17 arquivos)  
**Responsável:** Backend Dev 1  
**Prioridade:** CRÍTICA  

### ISSUE-B002: Restaurar role check em JogoController.criar()
**Descrição:** `@PreAuthorize` comentado em `JogoController.criar()` (linha 81). Risco de segurança.  
**Arquivo:** `controller/JogoController.java`  
**Responsável:** Backend Dev 1  
**Prioridade:** ALTA  

### ISSUE-B003: NpcCreateRequest DTO e endpoint dedicado para NPC
**Descrição:** `NpcCreateRequest.java` existe mas não está completo. Criar endpoint `POST /jogos/{jogoId}/npcs` separado de fichas de jogadores.  
**Arquivos:** `dto/request/NpcCreateRequest.java`, `controller/FichaController.java`  
**Responsável:** Backend Dev 3  
**Prioridade:** ALTA  

### ISSUE-B004: Endpoint de duplicação de ficha
**Descrição:** `DuplicarFichaRequest.java` e `DuplicarFichaResponse.java` existem mas sem endpoint/service implementado.  
**Arquivos:** `dto/request/DuplicarFichaRequest.java`, `dto/response/DuplicarFichaResponse.java`, `controller/FichaController.java`  
**Responsável:** Backend Dev 3  
**Prioridade:** ALTA  

---

## FASE 2 — Backend: Endpoints Faltantes

### ISSUE-B005: Endpoints de update direto da ficha
**Descrição:** Atualizar atributos, aptidões, vida e prospecção individualmente sem enviar a ficha inteira.  
- `PUT /fichas/{id}/atributos` — lista de `{atributoConfigId, base, outros}`
- `PUT /fichas/{id}/aptidoes` — lista de `{aptidaoConfigId, base, sorte, outros}`
- `PUT /fichas/{id}/vida` — `{vida, vidaMembros[]}`
- `PUT /fichas/{id}/prospeccao` — `{dadoProspeccaoConfigId, quantidade}`  
**Responsável:** Backend Dev 2  
**Prioridade:** ALTA  

### ISSUE-B006: Endpoint de template Klayrah
**Descrição:** `POST /jogos/{jogoId}/config/template/klayrah` — aplica template padrão mesmo depois da criação.  
**Responsável:** Backend Dev 2  
**Prioridade:** MÉDIA  

### ISSUE-B007: Gerenciamento de perfil do usuário
**Descrição:** `GET /api/v1/usuarios/me`, `PUT /api/v1/usuarios/me` para editar nome e foto.  
**Responsável:** Backend Dev 3  
**Prioridade:** MÉDIA  

### ISSUE-B008: Endpoint de restauração de soft-delete
**Descrição:** `POST /fichas/{id}/restaurar` e `POST /jogos/{id}/restaurar` para recuperar entidades deletadas.  
**Responsável:** Backend Dev 1  
**Prioridade:** BAIXA  

---

## FASE 3 — Backend: Novas Features

### ISSUE-B009: Sistema de anotações da ficha
**Descrição:** Criar entidade `FichaAnotacao` e CRUD completo.  
- `GET /fichas/{id}/anotacoes` — lista anotações (filtrar por jogador/mestre conforme role)
- `POST /fichas/{id}/anotacoes` — criar anotação
- `PUT /fichas/{id}/anotacoes/{anotacaoId}` — editar
- `DELETE /fichas/{id}/anotacoes/{anotacaoId}` — deletar

**Campos:** `titulo`, `conteudo`, `visivel` (jogador vê? flag), `tipoAnotacao` (JOGADOR | MESTRE)  
**Responsável:** Backend Dev 2  
**Prioridade:** MÉDIA  

### ISSUE-B010: Endpoint de histórico de alterações (Envers)
**Descrição:** Expor o audit trail do Hibernate Envers via endpoint.  
- `GET /fichas/{id}/historico?page=0&size=20` — paginado, MESTRE only  
- Deve mostrar: campo alterado, valor anterior, valor novo, quem alterou, quando  
**Responsável:** Backend Dev 2  
**Prioridade:** MÉDIA  

### ISSUE-B011: Convidar jogador por email
**Descrição:** `POST /jogos/{jogoId}/participantes/convidar` com `{email}`. Backend busca usuário por email ou gera token de convite.  
**Responsável:** Backend Dev 3  
**Prioridade:** BAIXA  

---

## FASE 4 — Frontend: Correção da Camada de API

### ISSUE-F001: Corrigir URLs dos serviços de API
**Descrição:** Os serviços do frontend têm URLs erradas. Exemplos:  
- ❌ `GET /api/v1/fichas` → ✅ `GET /api/v1/jogos/{jogoId}/fichas`  
- ❌ `GET /api/v1/config/{tipo}` → ✅ `GET /api/v1/jogos/{jogoId}/configuracoes/{tipo}`  
- Todos os endpoints de configuração usam `jogoId` como path param  
**Arquivos:** `core/services/api/*.service.ts` (todos)  
**Responsável:** Frontend Dev  
**Prioridade:** CRÍTICA  

### ISSUE-F002: Proxy para desenvolvimento local
**Descrição:** `proxy.conf.json` aponta para `http://backend:8080` (Docker only). Criar `proxy.conf.local.json` → `http://localhost:8181` e script `start:local`.  
**Responsável:** Frontend Dev  
**Prioridade:** CRÍTICA  

### ISSUE-F003: Integrar CurrentGameService com todas as páginas
**Descrição:** Todas as páginas protegidas por `currentGameGuard` devem usar `CurrentGameService.jogoAtivo()` para passar o `jogoId` nas chamadas de API.  
**Responsável:** Frontend Dev  
**Prioridade:** ALTA  

### ISSUE-F004: Adicionar chamadas de API faltantes
**Descrição:** Adicionar nos serviços: dashboard, duplicação de jogo, export/import de config, participantes (aprovar/rejeitar/banir), NPC.  
**Responsável:** Frontend Dev  
**Prioridade:** ALTA  

---

## FASE 5 — Frontend: Completar Features

### ISSUE-F005: Seção de Membros do Corpo na ficha
**Descrição:** Seção `VidaSectionComponent` incompleta — falta a parte de `FichaVidaMembro` (localização de danos por membro). Integrar com `MembroCorpoConfig` do jogo.  
**Responsável:** Frontend Dev + UI/UX  
**Prioridade:** ALTA  

### ISSUE-F006: Exibir valores calculados na ficha
**Descrição:** Mostrar valores calculados pelo backend em tempo real:
- Ímpeto de cada atributo
- Total de cada aptidão
- BBA, BBM, demais bônus
- Vida Total, Essência, Ameaça, RD, RDM  
**Responsável:** Frontend Dev  
**Prioridade:** ALTA  

### ISSUE-F007: UI de gerenciamento de participantes
**Descrição:** Tela para mestre ver lista de participantes, com ações: aprovar, rejeitar, banir. Mostrar status atual (PENDENTE, APROVADO, REJEITADO, BANIDO).  
**Responsável:** Frontend Dev + UI/UX  
**Prioridade:** ALTA  

### ISSUE-F008: UI de gerenciamento de NPCs
**Descrição:** Seção exclusiva do mestre para criar/editar/listar NPCs. Separado da lista de fichas dos jogadores.  
**Responsável:** Frontend Dev + UI/UX  
**Prioridade:** MÉDIA  

---

## FASE 6 — UI/UX: Redesenho Completo

### ISSUE-U001: Design System — tema Klayrah
**Descrição:** Customizar o tema Aura do PrimeNG com identidade visual do Klayrah RPG:
- Paleta de cores: tons escuros com accent vermelho/âmbar (estética RPG de mesa)
- Tipografia: fonte serif para títulos, monospace para valores numéricos
- Componentes customizados: cards de ficha, badges de atributo, barras de vida
- Dark mode por padrão (RPG = noite)  
**Arquivos:** `src/styles.scss`, `app.config.ts` (PrimeNG theme)  
**Responsável:** UI/UX Specialist  
**Prioridade:** ALTA  

### ISSUE-U002: Redesenho das 13 páginas de configuração
**Descrição:** Cada página de config deve ter:
- Header com nome da config + botão "+ Novo"
- Tabela/lista com reordenação drag-and-drop (usando `ordemExibicao`)
- Formulário lateral (drawer/sidebar) para criar/editar
- Campo de busca por nome
- Confirmação de exclusão
- Para configs com sub-recursos (Classe, Raça, Vantagem): tabs ou seções expansíveis  
**Componentes:** Todo `features/mestre/config/*`  
**Responsável:** UI/UX Specialist  
**Prioridade:** ALTA  

### ISSUE-U003: Redesenho da ficha de personagem
**Descrição:** A ficha deve ter visual de "documento oficial do Klayrah" — não um simples formulário. Proposta:
- Layout em abas (Identidade | Atributos | Aptidões | Bônus | Vida/Essência | Vantagens | Anotações)
- Header fixo com nome do personagem, nível, raça e classe
- Valores calculados em destaque com indicadores visuais (barra de vida, contadores de essência)
- Preview em tempo real de cálculos ao editar atributos
- Responsivo: mobile tem as abas em bottom nav  
**Componentes:** `features/jogador/ficha-form/*`, `features/jogador/ficha-detail/*`  
**Responsável:** UI/UX Specialist  
**Prioridade:** ALTA  

### ISSUE-U004: Estados de loading, erro e vazio
**Descrição:** Todos os componentes que fazem chamadas async devem ter:
- Skeleton loaders durante carregamento (não spinner genérico)
- Mensagens de erro contextuais por campo nos formulários
- Empty state com ilustração e CTA quando lista vazia
- Toast de sucesso/erro após operações  
**Responsável:** UI/UX Specialist  
**Prioridade:** MÉDIA  

### ISSUE-U005: Mestre Dashboard melhorado
**Descrição:** Dashboard do mestre deve ter:
- Cards de métricas (fichas ativas, participantes aprovados, sessões)
- Gráfico de distribuição de fichas por nível (PrimeNG Chart)
- Lista de atividade recente
- Acesso rápido às configurações mais usadas  
**Responsável:** UI/UX Specialist  
**Prioridade:** MÉDIA  

### ISSUE-U006: Mobile responsiveness
**Descrição:** Testar e corrigir responsividade em breakpoints mobile (< 768px):
- Sidebar colapsa para bottom sheet no mobile
- Tabela de config vira lista de cards
- Ficha em abas vira accordion  
**Responsável:** UI/UX Specialist  
**Prioridade:** BAIXA  

---

## FASE 7 — QA: Testes e Revisão

### ISSUE-Q001: Testes de integração — controllers de configuração
**Descrição:** Verificar que TODOS os 17 controllers de config têm testes cobrindo:
- Criar (com e sem permissão)
- Listar com filtro nome
- Reordenar batch
- Atualizar
- Deletar  
**Responsável:** QA  

### ISSUE-Q002: Testes de integração — FichaController
**Descrição:** Cobrir todos os endpoints novos:
- Duplicar ficha
- Update direto de atributos/aptidões/vida
- Anotações CRUD
- NPC CRUD  
**Responsável:** QA  

### ISSUE-Q003: Testes de componente — Angular
**Descrição:** Testes com Vitest + @testing-library/angular para:
- ConfigApiService (mock HTTP)
- FichasApiService
- AuthService
- BaseConfigComponent  
**Responsável:** QA  

### ISSUE-Q004: Tech Lead Review — Backend
**Descrição:** Revisar arquitetura dos novos endpoints, verificar:
- Consistência com padrão AbstractConfiguracaoService
- Transações corretas
- Segurança (PreAuthorize em tudo)
- Performance (N+1 queries)  
**Responsável:** Tech Lead  

### ISSUE-Q005: Tech Lead Review — Frontend
**Descrição:** Revisar:
- Uso correto de Signals (sem BehaviorSubject)
- Componentes standalone
- Injeção com inject() não constructor
- Sem localStorage  
**Responsável:** Tech Lead  

---

## Ordem de Execução

```
FASE 0 (Infra)     ━━━━▶ IMEDIATO
FASE 1 (BE Seg)    ━━━━▶ Paralelo com FASE 4 (FE API)
FASE 2 (BE Ends)   ━━━━▶ Após FASE 1
FASE 3 (BE Novo)   ━━━━▶ Após FASE 2
FASE 5 (FE Feat)   ━━━━▶ Após FASE 4
FASE 6 (UX)        ━━━━▶ Paralelo com FASE 5
FASE 7 (QA)        ━━━━▶ Ao final de cada fase
```

---

## Checklist de Conclusão por Fase

- [ ] **FASE 0** — Portas atualizadas, proxy local funcionando
- [ ] **FASE 1** — Todos os role checks adicionados, testes passando
- [ ] **FASE 2** — Endpoints novos com testes de integração
- [ ] **FASE 3** — Anotações e histórico funcionando
- [ ] **FASE 4** — Frontend conectando corretamente ao backend
- [ ] **FASE 5** — Membros, cálculos e participantes OK
- [ ] **FASE 6** — Design system aplicado, config pages redesenhadas
- [ ] **FASE 7** — 80%+ cobertura nos testes críticos, Tech Lead aprovado
