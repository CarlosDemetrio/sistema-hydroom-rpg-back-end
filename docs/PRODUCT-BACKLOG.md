# Product Backlog — Klayrah RPG: Ficha Controlador

> Documento gerado em: 2026-03-31
> Baseado em: auditoria completa do domínio (docs/) + backend (feature/009-npc-fichas-mestre) + frontend (feature/009-npc-fichas-mestre)

---

## Legenda de Status

- ⬜ Nao iniciado
- 🔄 Em andamento / Parcialmente implementado
- ✅ Concluido
- 🔴 Bloqueado

## Legenda de Prioridade

- **Must Have** — indispensavel para o produto funcionar
- **Should Have** — importante para a experiencia completa
- **Could Have** — melhoria desejavel, nao critica

---

## Indice de Epicos

| Epico | Nome | Total USs | Concluidas | Nao Iniciadas |
|-------|------|-----------|------------|---------------|
| E1 | Autenticacao e Onboarding | 5 | 3 | 2 |
| E2 | Mestre: Gestao de Jogos | 8 | 5 | 3 |
| E3 | Mestre: Gestao de Participantes | 5 | 3 | 2 |
| E4 | Mestre: Configuracoes do Jogo | 20 | 14 | 6 |
| E5 | Jogador: Criacao de Ficha | 5 | 2 | 3 |
| E6 | Jogador: Ficha — Visualizacao e Edicao | 18 | 0 | 18 |
| E7 | Mestre: Visao de Fichas e NPCs | 8 | 2 | 6 |
| E8 | Modo Sessao (Jogo Ativo) | 6 | 0 | 6 |
| E9 | Flow de Level Up | 5 | 0 | 5 |
| E10 | NPC | 5 | 2 | 3 |
| E11 | Testes e Qualidade | 8 | 0 | 8 |
| **Total** | | **93** | **31** | **62** |

---

## EPICO 1 — Autenticacao e Onboarding

### US-001: Login com Google OAuth
**Como** usuario novo ou retornando **quero** autenticar com minha conta Google **para** acessar o sistema sem criar senha separada

**Prioridade:** Must Have
**Epico:** E1 — Autenticacao e Onboarding
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] Botao "Entrar com Google" na tela de login
- [x] Redirecionamento OAuth2 para Google e callback correto
- [x] Sessao HTTP (cookie JSESSIONID + XSRF-TOKEN) mantida apos login
- [x] Redirecionamento automatico para dashboard se ja autenticado
- [x] IdleService dispara logout automatico apos 30 min de inatividade

**Notas tecnicas:**
- Backend: `GET /api/public/health`, OAuth2 via Spring Security
- Frontend: `LoginComponent`, `OAuthCallbackComponent`, `AuthService`, `IdleService`
- Testes: mock do AuthService, verificar redirecionamento pos-login

---

### US-002: Logout seguro
**Como** usuario autenticado **quero** encerrar minha sessao **para** proteger minha conta em dispositivos compartilhados

**Prioridade:** Must Have
**Epico:** E1 — Autenticacao e Onboarding
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] Botao de logout acessivel no header/perfil
- [x] Chamada `POST /api/v1/auth/logout` ao clicar
- [x] Limpeza de estado local (SignalStore) apos logout
- [x] Redirecionamento para `/login`

**Notas tecnicas:**
- Backend: `POST /api/v1/auth/logout`
- Frontend: `AuthService.logout()`, `ProfileComponent`
- Testes: verificar limpeza de estado e redirecionamento

---

### US-003: Perfil do usuario
**Como** usuario autenticado **quero** visualizar meu perfil (nome, email, role, foto) **para** confirmar meus dados no sistema

**Prioridade:** Should Have
**Epico:** E1 — Autenticacao e Onboarding
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] Pagina `/profile` com nome, email, role (Mestre/Jogador), status e data de criacao
- [x] Avatar com iniciais se sem foto
- [x] Indicacao visual de role (tag colorida)

**Notas tecnicas:**
- Backend: `GET /api/v1/auth/me`
- Frontend: `ProfileComponent`
- Testes: mock de resposta HTTP, verificar exibicao dos campos

---

### US-004: Onboarding de primeiro acesso (selecao de role)
**Como** usuario novo **quero** ser guiado na configuracao inicial (escolher se sou Mestre ou Jogador) **para** ser direcionado para a experiencia correta desde o inicio

**Prioridade:** Must Have
**Epico:** E1 — Autenticacao e Onboarding
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Na primeira autenticacao, exibir wizard de onboarding
- [ ] Passo 1: escolher role (Mestre ou Jogador)
- [ ] Passo 2: se Jogador, listar jogos disponiveis para solicitar participacao
- [ ] Passo 3: confirmacao e redirecionamento para dashboard correto
- [ ] Nao exibir wizard em logins subsequentes

**Notas tecnicas:**
- Backend: role definida no cadastro, pode ser consultada via `GET /api/v1/auth/me`
- Frontend: novo componente `OnboardingWizardComponent`, flag no AuthService
- Testes: simulacao de primeiro login

---

### US-005: Selecao de jogo ativo no header
**Como** usuario autenticado **quero** selecionar o jogo ativo no menu superior **para** que todas as telas mostrem dados do jogo correto

**Prioridade:** Must Have
**Epico:** E1 — Autenticacao e Onboarding
**Status:** 🔄 Em andamento

**Criterios de aceite:**
- [x] Dropdown no header com lista de jogos do usuario
- [x] Jogo selecionado persistido no `CurrentGameService`
- [x] Todas as paginas que dependem de jogo reagem ao jogo selecionado
- [ ] Indicador visual claro de qual jogo esta ativo
- [ ] Se usuario nao tem jogos, mostrar CTA para criar ou solicitar participacao

**Notas tecnicas:**
- Backend: `GET /api/v1/jogos/meus`
- Frontend: `CurrentGameService`, `MainLayoutComponent`, `currentGameGuard`
- Testes: verificar que troca de jogo atualiza estado global

---

## EPICO 2 — Mestre: Gestao de Jogos

### US-006: Criar novo jogo
**Como** Mestre **quero** criar um novo jogo (campanha) **para** comecar a configurar um mundo para meus jogadores

**Prioridade:** Must Have
**Epico:** E2 — Mestre: Gestao de Jogos
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] Formulario com nome (obrigatorio) e descricao (opcional)
- [x] Apos criar, sistema aplica Template Klayrah Padrao (13 configs automaticamente)
- [x] Validacao: nome minimo 3 caracteres, maximo 100
- [x] Redirecionamento para lista de jogos apos criacao

**Notas tecnicas:**
- Backend: `POST /api/v1/jogos`
- Frontend: `JogoFormComponent`
- Testes: criacao com dados validos, erro de nome vazio

---

### US-007: Listar meus jogos com filtros
**Como** Mestre **quero** ver todos os meus jogos com filtros por nome e status **para** encontrar rapidamente a campanha que preciso gerenciar

**Prioridade:** Must Have
**Epico:** E2 — Mestre: Gestao de Jogos
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] Tabela paginada (10/pagina) com nome, participantes, status e data
- [x] Filtro por texto (nome/descricao)
- [x] Filtro por status (Ativo/Inativo)
- [x] Acoes: Ver, Editar, Excluir
- [x] Estado vazio com CTA para criar

**Notas tecnicas:**
- Backend: `GET /api/v1/jogos`
- Frontend: `JogosListComponent`, `JogoManagementFacadeService`
- Testes: filtros, estado vazio, paginacao

---

### US-008: Editar jogo
**Como** Mestre **quero** editar o nome, descricao e status de um jogo existente **para** manter as informacoes atualizadas

**Prioridade:** Must Have
**Epico:** E2 — Mestre: Gestao de Jogos
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] Pre-preenchimento dos campos com dados atuais
- [x] Validacoes identicas a criacao
- [x] Possibilidade de alterar status (Ativo/Pausado/Finalizado)
- [x] Toast de sucesso ou erro

**Notas tecnicas:**
- Backend: `PUT /api/v1/jogos/{id}`
- Frontend: `JogoFormComponent` (modo edicao)

---

### US-009: Excluir jogo (soft delete)
**Como** Mestre **quero** excluir um jogo que nao uso mais **para** manter minha lista organizada

**Prioridade:** Should Have
**Epico:** E2 — Mestre: Gestao de Jogos
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] Dialog de confirmacao antes de excluir
- [x] Soft delete (jogo nao aparece mais na lista)
- [x] Toast de confirmacao
- [ ] Avisar que fichas e configuracoes serao ocultadas

**Notas tecnicas:**
- Backend: `DELETE /api/v1/jogos/{id}`
- Frontend: `JogosListComponent`, `JogoDetailComponent`

---

### US-010: Ver detalhes do jogo (com abas)
**Como** Mestre **quero** ver uma tela detalhada do jogo com informacoes, participantes e fichas **para** ter uma visao completa da campanha

**Prioridade:** Must Have
**Epico:** E2 — Mestre: Gestao de Jogos
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] Aba "Informacoes": nome, descricao, status, data criacao, total de participantes/aprovados/pendentes
- [x] Aba "Participantes": tabela com nome, status, data, acoes (aprovar/rejeitar/remover)
- [x] Aba "Fichas": cards com fichas vinculadas ao jogo
- [ ] Aba "Configuracoes": atalho para gerenciar configs do jogo

**Notas tecnicas:**
- Backend: `GET /api/v1/jogos/{id}`, `GET /api/v1/jogos/{jogoId}/participantes`
- Frontend: `JogoDetailComponent`

---

### US-011: Duplicar jogo (configs, sem fichas)
**Como** Mestre **quero** duplicar um jogo existente (clonando todas as configuracoes) **para** criar variantes sem reconfigurar tudo do zero

**Prioridade:** Should Have
**Epico:** E2 — Mestre: Gestao de Jogos
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Botao "Duplicar" na lista/detalhe de jogos
- [ ] Modal para informar novo nome
- [ ] Todas as 13 configs sao copiadas, fichas NAO
- [ ] Toast de confirmacao com link para o novo jogo

**Notas tecnicas:**
- Backend: `POST /api/v1/jogos/{id}/duplicar` com `{ novoNome }` — ja implementado
- Frontend: botao e modal a implementar
- Testes: verificar que configs foram copiadas, fichas nao

---

### US-012: Exportar e importar configuracoes
**Como** Mestre **quero** exportar as configuracoes de um jogo e importa-las em outro **para** reutilizar regras personalizadas entre campanhas

**Prioridade:** Should Have
**Epico:** E2 — Mestre: Gestao de Jogos
**Status:** 🔄 Em andamento

**Criterios de aceite:**
- [x] Botoes "Exportar" e "Importar" visiveis no ConfigLayout (botoes existem, sem acao real)
- [ ] "Exportar" faz download de arquivo JSON com todas as 13 configs
- [ ] "Importar" aceita arquivo JSON, ignora nomes duplicados
- [ ] Feedback de quantas configs foram importadas com sucesso

**Notas tecnicas:**
- Backend: `GET /api/v1/jogos/{id}/config/export`, `POST /api/v1/jogos/{id}/config/import` — ja implementados
- Frontend: logica de download/upload no `ConfigLayoutComponent`
- Testes: exportar, modificar JSON, reimportar

---

### US-013: Dashboard do Mestre com metricas reais
**Como** Mestre **quero** ver no dashboard numeros reais (jogos, jogadores, fichas) **para** ter uma visao rapida da saude das minhas campanhas

**Prioridade:** Should Have
**Epico:** E2 — Mestre: Gestao de Jogos
**Status:** 🔄 Em andamento

**Criterios de aceite:**
- [x] Cards com "Jogos Criados", "Jogadores Ativos", "Fichas Criadas"
- [x] Lista de jogos recentes
- [ ] Metricas vindas do endpoint real `GET /api/v1/dashboard/mestre` (atualmente usa dados do store local)
- [ ] Numero de participantes pendentes de aprovacao com badge de alerta
- [ ] Quick action para ir direto ao jogo ativo

**Notas tecnicas:**
- Backend: `GET /api/v1/dashboard/mestre` — ja implementado
- Frontend: `MestreDashboardComponent` — consumir o endpoint em vez do store local
- Testes: mock do endpoint de dashboard

---

## EPICO 3 — Mestre: Gestao de Participantes

### US-014: Solicitar participacao em um jogo (Jogador)
**Como** Jogador **quero** encontrar jogos disponiveis e solicitar participacao **para** comecar a jogar com um Mestre

**Prioridade:** Must Have
**Epico:** E3 — Mestre: Gestao de Participantes
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Pagina `/jogador/jogos` com lista de jogos que aceita solicitacoes
- [ ] Botao "Solicitar Participacao" para cada jogo
- [ ] Indicacao visual de status da solicitacao (Pendente/Aprovado/Rejeitado)
- [ ] Nao permitir solicitar duas vezes no mesmo jogo

**Notas tecnicas:**
- Backend: `POST /api/v1/jogos/{jogoId}/participantes/solicitar` — ja implementado
- Frontend: `JogosDisponiveisComponent` — atualmente placeholder completo
- Testes: solicitar, tentar duplicar, verificar status

---

### US-015: Listar e filtrar participantes (Mestre)
**Como** Mestre **quero** ver todos os participantes do jogo com status e filtros **para** gerenciar quem faz parte da campanha

**Prioridade:** Must Have
**Epico:** E3 — Mestre: Gestao de Participantes
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] Tabela com jogador, ficha, status e data
- [x] Indicacao visual por status (Pendente=amarelo, Aprovado=verde, Rejeitado=vermelho, Banido=cinza)
- [x] Acoes contextuais: aprovar/rejeitar (se PENDENTE), remover (qualquer)
- [ ] Filtro por status na aba de participantes

**Notas tecnicas:**
- Backend: `GET /api/v1/jogos/{jogoId}/participantes`
- Frontend: `JogoDetailComponent` (aba Participantes)

---

### US-016: Aprovar participante
**Como** Mestre **quero** aprovar a solicitacao de um jogador **para** autoriza-lo a criar ficha e participar da sessao

**Prioridade:** Must Have
**Epico:** E3 — Mestre: Gestao de Participantes
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] Botao "Aprovar" visivel para participantes PENDENTES
- [x] Atualiza status para APROVADO na UI imediatamente
- [x] Toast de confirmacao
- [ ] Notificacao ao jogador (futura)

**Notas tecnicas:**
- Backend: `PUT /api/v1/jogos/{jogoId}/participantes/{id}/aprovar`
- Frontend: `JogoDetailComponent`, `ParticipanteBusinessService`

---

### US-017: Rejeitar participante
**Como** Mestre **quero** rejeitar uma solicitacao de participacao **para** manter o controle sobre quem entra na campanha

**Prioridade:** Must Have
**Epico:** E3 — Mestre: Gestao de Participantes
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] Botao "Rejeitar" visivel para participantes PENDENTES
- [x] Atualiza status para REJEITADO
- [ ] Campo opcional para motivo da rejeicao

**Notas tecnicas:**
- Backend: `PUT /api/v1/jogos/{jogoId}/participantes/{id}/rejeitar`

---

### US-018: Banir participante
**Como** Mestre **quero** banir um participante aprovado **para** remove-lo permanentemente da campanha por violacoes de conduta

**Prioridade:** Should Have
**Epico:** E3 — Mestre: Gestao de Participantes
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Botao "Banir" disponivel para participantes APROVADOS
- [ ] Dialog de confirmacao com aviso sobre o impacto
- [ ] Status alterado para BANIDO
- [ ] Jogador banido nao pode solicitar novamente

**Notas tecnicas:**
- Backend: `DELETE /api/v1/jogos/{jogoId}/participantes/{id}` (semantica de banimento)
- Frontend: acao adicional no `JogoDetailComponent`

---

## EPICO 4 — Mestre: Configuracoes do Jogo (13 tipos)

> Nota de auditoria: todos os 14 componentes de configuracao existem. AtributosConfig, VantagensConfig, RacasConfig, ClassesConfig estao com implementacao completa (CRUD + sub-recursos). Os demais seguem o mesmo padrao base mas precisam de auditoria individual de sub-recursos e reordenacao real.

### US-019: Gerenciar Atributos
**Como** Mestre **quero** criar, editar, reordenar e excluir atributos do jogo **para** definir as caracteristicas fundamentais dos personagens

**Prioridade:** Must Have
**Epico:** E4 — Configuracoes
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] Listar atributos com busca por nome/sigla
- [x] Criar com: nome, abreviacao (2-5 chars maiusculos, unica cross-entity), formula de impeto, descricao do impeto, valor min/max, ordem
- [x] Editar todos os campos
- [x] Excluir com confirmacao
- [x] Drag-and-drop de reordenacao (UI) — falta persistir via `PUT /atributos/reordenar`
- [ ] Reordenacao batch realmente persiste no backend

**Notas tecnicas:**
- Backend: `/api/v1/configuracoes/atributos` (todos os verbos)
- Frontend: `AtributosConfigComponent` — implementado
- Testes: criacao com sigla duplicada deve falhar, formula de impeto valida

---

### US-020: Gerenciar Aptidoes e Tipos de Aptidao
**Como** Mestre **quero** criar e organizar aptidoes por tipo (Fisica/Mental) **para** definir as habilidades treinaveis dos personagens

**Prioridade:** Must Have
**Epico:** E4 — Configuracoes
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] CRUD completo de aptidoes (nome, tipo de aptidao, ordem)
- [x] CRUD completo de tipos de aptidao
- [x] Busca por nome
- [x] Reordenacao com persistencia (pendente confirmacao)
- [ ] Reordenacao batch realmente persiste no backend

**Notas tecnicas:**
- Backend: `/api/v1/configuracoes/aptidoes`, `/api/v1/configuracoes/tipos-aptidao`
- Frontend: `AptidoesConfigComponent`, `TiposAptidaoConfigComponent`

---

### US-021: Gerenciar Bonus (BBA, BBM, Reflexo, etc.)
**Como** Mestre **quero** criar bonus derivados com formulas baseadas em atributos **para** automatizar calculos de combate e defesa

**Prioridade:** Must Have
**Epico:** E4 — Configuracoes
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] CRUD completo com: nome, sigla, formula base (max 200 chars), ordem
- [x] Campo formula com hint de variaveis disponiveis (abreviacoes dos atributos)
- [x] Busca por nome/sigla
- [ ] Validacao de formula ao salvar (chamar endpoint `/formulas/validar`)
- [ ] Preview do valor calculado com atributos hipoteticos

**Notas tecnicas:**
- Backend: `/api/v1/configuracoes/bonus`, `/api/v1/configuracoes/formulas/validar`
- Frontend: `BonusConfigComponent`
- Testes: formula invalida deve mostrar erro claro

---

### US-022: Gerenciar Classes (com sub-recursos)
**Como** Mestre **quero** criar classes com bonus em derivados e aptidoes **para** diferenciar o papel de cada personagem no jogo

**Prioridade:** Must Have
**Epico:** E4 — Configuracoes
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] CRUD de classe (nome, descricao, ordem)
- [x] Sub-recurso ClasseBonus: adicionar/remover bonus derivados que a classe concede
- [x] Sub-recurso ClasseAptidaoBonus: adicionar/remover aptidoes com bonus de classe
- [x] Abas no drawer: Dados Gerais, Bonus, Aptidoes com Bonus
- [ ] Exibir valor do bonus (quantos pontos por nivel) no sub-recurso ClasseBonus

**Notas tecnicas:**
- Backend: `/api/v1/configuracoes/classes/{id}/bonus`, `/api/v1/configuracoes/classes/{id}/aptidao-bonus`
- Frontend: `ClassesConfigComponent`

---

### US-023: Gerenciar Racas (com sub-recursos)
**Como** Mestre **quero** criar racas com bonus/penalidades de atributos e restricoes de classe **para** dar variedade racial ao jogo

**Prioridade:** Must Have
**Epico:** E4 — Configuracoes
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] CRUD de raca (nome, descricao, ordem)
- [x] Sub-recurso RacaBonusAtributo: adicionar/remover bonus (positivo/negativo) em atributos
- [x] Sub-recurso RacaClassePermitida: restringir quais classes essa raca pode escolher
- [x] Indicacao visual de bonus positivo (verde) vs negativo (vermelho)
- [x] Se sem classes permitidas, exibir "Todas as classes permitidas"

**Notas tecnicas:**
- Backend: `/api/v1/configuracoes/racas/{id}/bonus-atributos`, `/api/v1/configuracoes/racas/{id}/classes-permitidas`
- Frontend: `RacasConfigComponent`

---

### US-024: Gerenciar Niveis e Progressao
**Como** Mestre **quero** configurar a tabela de niveis (XP, pontos, limitador, renascimento) **para** controlar como os personagens progridem

**Prioridade:** Must Have
**Epico:** E4 — Configuracoes
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] CRUD de nivel com: numero do nivel, XP necessaria, pontos de atributo, pontos de aptidao, limitador de atributo
- [x] Campo `permitirRenascimento` (boolean)
- [x] Exibicao em tabela ordenada por numero de nivel
- [ ] Validacao: numero de nivel unico por jogo
- [ ] Import da tabela completa (0-35) de uma vez via CSV ou JSON

**Notas tecnicas:**
- Backend: `/api/v1/configuracoes/niveis`
- Frontend: `NiveisConfigComponent`, `LimitadoresConfigComponent` (verificar sobreposicao)

---

### US-025: Gerenciar Vantagens (com sub-recursos)
**Como** Mestre **quero** criar vantagens com formula de custo, pre-requisitos e efeitos **para** definir os poderes especiais disponíveis aos jogadores

**Prioridade:** Must Have
**Epico:** E4 — Configuracoes
**Status:** 🔄 Em andamento

**Criterios de aceite:**
- [x] CRUD de vantagem (nome, sigla, categoria, nivel maximo, formula de custo, descricao)
- [x] Sub-recurso PreRequisitos: adicionar/remover pre-requisitos (outras vantagens)
- [x] Categorias de vantagem (dropdown)
- [ ] Sub-recurso VantagemEfeito: CRUD completo dos 8 tipos de efeito (BONUS_ATRIBUTO, BONUS_APTIDAO, BONUS_DERIVADO, BONUS_VIDA, BONUS_VIDA_MEMBRO, BONUS_ESSENCIA, DADO_UP, FORMULA_CUSTOMIZADA)
- [ ] CRUD de CategoriaVantagem (interface dedicada, atualmente sem UI)

**Notas tecnicas:**
- Backend: `/api/v1/jogos/{jogoId}/configuracoes/vantagens/{id}/efeitos`, `/api/jogos/{jogoId}/config/categorias-vantagem`
- Frontend: `VantagensConfigComponent` — efeitos nao implementados
- Testes: cada tipo de efeito com campos corretos

---

### US-026: Gerenciar Membros do Corpo
**Como** Mestre **quero** configurar os membros do corpo com porcentagem de vida **para** habilitar dano localizado na campanha

**Prioridade:** Should Have
**Epico:** E4 — Configuracoes
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] CRUD com: nome, porcentagem de vida (0.01-1.00), ordem
- [x] Exibicao clara do percentual (ex: 75% para Cabeca)
- [x] Validacao: soma das porcentagens nao e obrigatoria ser 100% (cada membro e independente)

**Notas tecnicas:**
- Backend: `/api/v1/configuracoes/membros-corpo`
- Frontend: `MembrosCorpoConfigComponent`

---

### US-027: Gerenciar Dados de Prospeccao
**Como** Mestre **quero** configurar os tipos de dados de prospeccao disponiveis **para** controlar quais dados posso dar aos jogadores como recompensa

**Prioridade:** Should Have
**Epico:** E4 — Configuracoes
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] CRUD com: nome (ex: d6), numero de faces (1-100)
- [x] Lista ordenada

**Notas tecnicas:**
- Backend: `/api/v1/configuracoes/dados-prospeccao`
- Frontend: `ProspeccaoConfigComponent`

---

### US-028: Gerenciar Generos, Indices e Presencas
**Como** Mestre **quero** configurar as opcoes de genero, indole e presenca **para** definir as escolhas de alinhamento disponíveis para os jogadores

**Prioridade:** Should Have
**Epico:** E4 — Configuracoes
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] CRUD para Genero (nome, ordem)
- [x] CRUD para Indole (nome, ordem)
- [x] CRUD para Presenca (nome, ordem)
- [x] Busca por nome em cada um

**Notas tecnicas:**
- Backend: `/api/v1/configuracoes/generos`, `/api/v1/configuracoes/indoles`, `/api/v1/configuracoes/presencas`
- Frontend: `GenerosConfigComponent`, `IndolesConfigComponent`, `PresencasConfigComponent`

---

### US-029: Reordenacao batch com persistencia real
**Como** Mestre **quero** arrastar e soltar os itens de configuracao e ter a ordem salva no servidor **para** controlar a exibicao na ficha do jogador

**Prioridade:** Must Have
**Epico:** E4 — Configuracoes
**Status:** 🔄 Em andamento

**Criterios de aceite:**
- [x] UI de drag-and-drop implementada no BaseConfigTableComponent
- [ ] Evento de reordenacao chama `PUT /api/v1/configuracoes/{tipo}/reordenar?jogoId={id}`
- [ ] Feedback visual de "salvando..." e "salvo"
- [ ] Rollback visual em caso de erro

**Notas tecnicas:**
- Backend: `PUT /api/v1/configuracoes/{tipo}/reordenar` — todos implementados
- Frontend: `handleReorder()` em cada config — atualmente exibe toast mas nao chama API

---

### US-030: Validacao e preview de formulas
**Como** Mestre **quero** validar uma formula antes de salvar e ver um preview do valor calculado **para** ter certeza que a logica esta correta

**Prioridade:** Should Have
**Epico:** E4 — Configuracoes
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Botao "Validar Formula" ao lado do campo de formula (BonusConfig, AtributoConfig)
- [ ] Feedback inline: formula valida (verde) ou invalida (vermelho + mensagem de erro)
- [ ] Botao "Preview" abre modal com valores hipoteticos para simular o calculo
- [ ] Lista de variaveis disponiveis para o jogo atual

**Notas tecnicas:**
- Backend: `POST /api/v1/configuracoes/formulas/validar`, `POST /api/v1/configuracoes/formulas/preview`, `GET /api/v1/configuracoes/formulas/variaveis`
- Frontend: componente compartilhado `FormulaFieldComponent`

---

### US-031: Gerenciar siglas em uso (painel de siglas)
**Como** Mestre **quero** ver um painel com todas as siglas em uso no jogo **para** evitar conflitos ao criar novas configuracoes

**Prioridade:** Could Have
**Epico:** E4 — Configuracoes
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Painel acessivel no ConfigLayout mostrando todas as siglas ativas
- [ ] Agrupadas por tipo (Atributo, Bonus, Vantagem)
- [ ] Busca por sigla
- [ ] Clicar na sigla navega para a config correspondente

**Notas tecnicas:**
- Backend: `GET /api/v1/configuracoes/siglas?jogoId={id}`, `GET /api/v1/configuracoes/siglas/{sigla}?jogoId={id}`
- Frontend: novo componente `SiglasPanel` no ConfigLayout

---

### US-032: Gerenciar Pontos de Vantagem por Nivel
**Como** Mestre **quero** configurar quantos pontos de vantagem o personagem ganha por nivel **para** controlar o ritmo de progressao de poderes

**Prioridade:** Should Have
**Epico:** E4 — Configuracoes
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Interface para configurar `PontosVantagemConfig` (pontos por nivel, acumulado)
- [ ] Vinculado ao NivelConfig do jogo
- [ ] CRUD basico

**Notas tecnicas:**
- Backend: entidade existe (`PontosVantagemConfig`), verificar se ha endpoint
- Frontend: nao implementado, possivelmente integrar ao `NiveisConfigComponent`

---

## EPICO 5 — Jogador: Criacao de Ficha de Personagem

### US-033: Assistente de criacao de ficha (wizard)
**Como** Jogador **quero** criar minha ficha guiado por um wizard passo-a-passo **para** nao esquecer nenhum campo obrigatorio e entender o que estou fazendo

**Prioridade:** Must Have
**Epico:** E5 — Criacao de Ficha
**Status:** 🔄 Em andamento

**Criterios de aceite:**
- [ ] Passo 1: Dados basicos — nome (obrigatorio), jogadorId
- [ ] Passo 2: Identidade — raca (dropdown das racas do jogo), classe, genero, indole, presenca
- [ ] Passo 3: Confirmacao com resumo antes de criar
- [ ] Validacao: jogador deve estar APROVADO no jogo para criar ficha
- [x] Redirecionamento para ficha recem-criada apos sucesso
- [ ] Dropdowns carregam dados das configs do jogo ativo (nao hardcoded)

**Notas tecnicas:**
- Backend: `POST /api/v1/jogos/{jogoId}/fichas` com `CreateFichaRequest` correto (racaId, classeId, generoId, indoleId, presencaId)
- Frontend: `FichaFormComponent` — existe mas usa atributos hardcoded (FOR/DES/CON/INT/SAB/CAR) em vez das configs do jogo
- Testes: criacao com todos os campos obrigatorios, sem jogo selecionado

---

### US-034: Listar minhas fichas
**Como** Jogador **quero** ver todas as minhas fichas do jogo atual em cards **para** acessar rapidamente o personagem que quero jogar

**Prioridade:** Must Have
**Epico:** E5 — Criacao de Ficha
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] Cards com nome, nivel, origem
- [x] Busca por nome
- [x] Acoes: Ver, Editar, Excluir (com confirmacao)
- [x] Estado vazio com CTA para criar
- [x] Filtragem por jogo ativo

**Notas tecnicas:**
- Backend: `GET /api/v1/jogos/{jogoId}/fichas/minhas`
- Frontend: `FichasListComponent`

---

### US-035: Excluir ficha (soft delete)
**Como** Jogador **quero** excluir uma ficha que nao uso mais **para** manter minha lista organizada

**Prioridade:** Should Have
**Epico:** E5 — Criacao de Ficha
**Status:** ✅ Concluido

**Criterios de aceite:**
- [x] Dialog de confirmacao antes de excluir
- [x] Soft delete (ficha some da lista)
- [x] Toast de confirmacao

**Notas tecnicas:**
- Backend: `DELETE /api/v1/fichas/{id}` (apenas MESTRE pode excluir fichas de outros)

---

### US-036: Duplicar ficha
**Como** Jogador ou Mestre **quero** duplicar uma ficha existente **para** criar uma variacao sem redigitar todos os dados

**Prioridade:** Should Have
**Epico:** E5 — Criacao de Ficha
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Botao "Duplicar" na lista/detalhe de fichas
- [ ] Modal com campo "Novo Nome" e opcao "Manter Jogador" (MESTRE only)
- [ ] Toast com link para nova ficha
- [ ] Nova ficha aparece na lista imediatamente

**Notas tecnicas:**
- Backend: `POST /api/v1/fichas/{id}/duplicar` com `{ novoNome, manterJogador }` — ja implementado
- Frontend: botao e modal a implementar

---

### US-037: Selecionar jogo ao criar ficha
**Como** Jogador **quero** que a criacao de ficha use automaticamente o jogo ativo **para** nao precisar escolher o jogo toda vez

**Prioridade:** Must Have
**Epico:** E5 — Criacao de Ficha
**Status:** 🔄 Em andamento

**Criterios de aceite:**
- [x] Se jogo ativo selecionado, pre-preencher jogoId
- [x] Se sem jogo, exibir aviso e redirecionar
- [ ] Dropdowns (raca, classe, etc.) carregam opcoes do jogo ativo

**Notas tecnicas:**
- Frontend: `FichaFormComponent` — integrar `CurrentGameService` com carregamento de configs dinamicas

---

## EPICO 6 — Jogador: Ficha — Visualizacao e Edicao

> Este e o maior epico e o que tem mais gap atual. O componente `FichaDetailComponent` e completamente placeholder.

### US-038: Aba Identidade — dados pessoais
**Como** Jogador **quero** visualizar e editar nome, origem, genero, indole, presenca, insólitus, titulo heroico e arquetipo de referencia **para** dar vida e personalidade ao meu personagem

**Prioridade:** Must Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Exibir e editar: nome, origem, genero (label da config), indole (label), presenca (label)
- [ ] Campo "Insólitus" com texto livre (impacto mecanico definido pelo Mestre)
- [ ] Campo "Titulo Heroico" (alcunha conquistada)
- [ ] Campo "Arquetipo de Referencia" (inspiracao do personagem)
- [ ] Edicao inline com salvamento via `PUT /api/v1/fichas/{id}`

**Notas tecnicas:**
- Backend: `GET /api/v1/fichas/{id}`, `PUT /api/v1/fichas/{id}`
- Frontend: novo componente `FichaIdentidadeTabComponent`
- Testes: salvar identidade, verificar campos persistidos

---

### US-039: Aba Descricao Fisica
**Como** Jogador **quero** registrar a aparencia fisica do meu personagem (altura, peso, idade, olhos, cabelo) **para** descrever visualmente quem ele e

**Prioridade:** Should Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Campos: altura (cm), peso (kg), idade, cor dos olhos, cabelo, pele, descricao livre de aparencia
- [ ] Calcule IMC com preview se genero e altura/peso preenchidos

**Notas tecnicas:**
- Backend: `PUT /api/v1/fichas/{id}`
- Frontend: novo componente `FichaDescricaoFisicaTabComponent`

---

### US-040: Aba Progressao (nivel, XP, renascimentos)
**Como** Jogador **quero** ver meu nivel atual, XP acumulada e renascimentos **para** acompanhar minha progressao

**Prioridade:** Must Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Exibir: nivel (calculado da XP), XP acumulada, renascimentos, NVS (Nivel de Vida Superior)
- [ ] Barra de progresso XP (xp atual vs xp para proximo nivel)
- [ ] Campo "Pontos de Atributo disponiveis" (do nivel atual)
- [ ] Campo "Pontos de Vantagem disponiveis"
- [ ] Indicacao visual se pode subir de nivel ou renascer

**Notas tecnicas:**
- Backend: `GET /api/v1/fichas/{id}/resumo` retorna dados calculados
- Frontend: novo componente `FichaProgressaoTabComponent`

---

### US-041: Aba Atributos — distribuicao e calculo
**Como** Jogador **quero** visualizar e distribuir pontos nos atributos do jogo **para** definir as forças e fraquezas do meu personagem

**Prioridade:** Must Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Para cada atributo do jogo: exibir Base, Nivel (pontos de nivel), Outros, Total
- [ ] Editar Base e Nivel (pontos distribuidos ao subir de nivel)
- [ ] Total calculado automaticamente (Base + Nivel + Outros)
- [ ] Impeto calculado e exibido ao lado do Total (via formula da config)
- [ ] Validacao: Total nao pode exceder o Limitador do nivel atual
- [ ] Contador de pontos de atributo restantes a distribuir
- [ ] Salvar em lote via `PUT /api/v1/fichas/{id}/atributos`

**Notas tecnicas:**
- Backend: `PUT /api/v1/fichas/{id}/atributos`, `GET /api/v1/fichas/{id}/resumo`
- Frontend: `FichaCalculationService` para preview, backend para calculo oficial
- Testes: distribuicao acima do limitador deve falhar, calcular impeto corretamente

---

### US-042: Aba Aptidoes — base, sorte, classe, total
**Como** Jogador **quero** visualizar e editar meus valores de aptidao **para** saber o que meu personagem sabe fazer

**Prioridade:** Must Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Listar todas as aptidoes do jogo agrupadas por Tipo (Fisica/Mental)
- [ ] Para cada aptidao: Base (editavel), Sorte (editavel), Classe (somente leitura — vem da classe), Total (calculado)
- [ ] Salvar em lote via `PUT /api/v1/fichas/{id}/aptidoes`
- [ ] Preview do total enquanto edita

**Notas tecnicas:**
- Backend: `PUT /api/v1/fichas/{id}/aptidoes`, `GET /api/v1/fichas/{id}/resumo`
- Frontend: novo componente `FichaAptidoesTabComponent`

---

### US-043: Aba Bonus e Combate (BBA, BBM, Reflexo, etc.)
**Como** Jogador **quero** visualizar todos os meus bonus de combate decompostos por fonte **para** entender de onde vem cada ponto

**Prioridade:** Must Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Para cada bonus do jogo: exibir Base (calculado pela formula), Vantagens, Classe, Itens, Gloria, Outros, Total
- [ ] "Outros" editavel pelo jogador
- [ ] Base recalculado automaticamente quando atributos mudam
- [ ] RD e RDM exibidos como derivados do Impeto

**Notas tecnicas:**
- Backend: `GET /api/v1/fichas/{id}/resumo`
- Frontend: novo componente `FichaBonusTabComponent`

---

### US-044: Aba Vida e Essencia
**Como** Jogador **quero** visualizar minha vida total e vida por membro do corpo, alem da essencia **para** rastrear meu estado de saude durante o jogo

**Prioridade:** Must Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Vida Total: decomposicao em VG (Vigor), VT (Vantagens), Nivel, Renascimentos, Outros
- [ ] Vida por membro: barra de vida para cada membro, dano atual editavel
- [ ] RD e RDM exibidos
- [ ] Essencia Total e Essencia Restante (campo de gasto editavel)
- [ ] Ameaça: decomposicao em Nivel, Itens, Titulos, Renascimentos, Outros

**Notas tecnicas:**
- Backend: `GET /api/v1/fichas/{id}/resumo`
- Frontend: novos componentes `FichaVidaTabComponent`, `FichaEssenciaTabComponent`

---

### US-045: Aba Vantagens — compra e gerenciamento
**Como** Jogador **quero** visualizar as vantagens compradas e comprar novas **para** especializar meu personagem com poderes unioos

**Prioridade:** Must Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Listar vantagens compradas com nivel atual e custo acumulado
- [ ] Exibir pre-requisitos de cada vantagem (atendidos ou nao)
- [ ] Botao "Comprar/Melhorar Vantagem" (custo deduzido dos pontos disponiveis)
- [ ] Ao melhorar, nivel sobe (nao pode baixar, nao pode remover)
- [ ] Pontos de vantagem restantes exibidos com destaque
- [ ] Validar pre-requisitos antes de permitir compra

**Notas tecnicas:**
- Backend: `GET /api/v1/fichas/{id}/vantagens`, `POST /api/v1/fichas/{id}/vantagens`, `PUT /api/v1/fichas/{id}/vantagens/{vid}`
- Frontend: novo componente `FichaVantagensTabComponent`
- Testes: compra com pontos insuficientes, pre-requisito nao atendido

---

### US-046: Aba Prospeccao (contador de dados)
**Como** Jogador **quero** visualizar quantos dados de prospeccao tenho de cada tipo **para** saber quando posso usar esse recurso especial

**Prioridade:** Should Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Para cada tipo de dado (d3, d6, d8...) exibir contador atual
- [ ] Apenas o Mestre pode adicionar prospeccao (campo read-only para jogador)
- [ ] Interface clara que prospeccao e recurso raro

**Notas tecnicas:**
- Backend: incluir na ficha ou via endpoint dedicado
- Frontend: novo componente `FichaProspeccaoTabComponent`

---

### US-047: Aba Anotacoes (jogador + mestre)
**Como** Jogador ou Mestre **quero** criar e visualizar anotacoes na ficha **para** registrar informacoes importantes sobre o personagem

**Prioridade:** Should Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Jogador ve: suas proprias anotacoes + anotacoes do Mestre marcadas como visiveis
- [ ] Mestre ve: todas as anotacoes (proprias e do jogador)
- [ ] Criar anotacao com: titulo, conteudo, tipo (JOGADOR/MESTRE), visivelParaJogador
- [ ] Deletar anotacao (Mestre: qualquer; Jogador: so suas)
- [ ] Marcacao visual diferenciando anotacoes do jogador vs mestre

**Notas tecnicas:**
- Backend: `GET /api/v1/fichas/{fichaId}/anotacoes`, `POST`, `DELETE`
- Frontend: novo componente `FichaAnotacoesTabComponent`
- Testes: jogador nao ve anotacoes privadas do mestre

---

### US-048: Preview em tempo real dos calculos
**Como** Jogador **quero** ver os valores calculados em tempo real enquanto edito atributos **para** tomar decisoes informadas durante a distribuicao de pontos

**Prioridade:** Should Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] `FichaCalculationService` calcula preview client-side de: impeto, bonus (BBA, BBM...), vida total, essencia total
- [ ] Preview exibido com indicacao clara de "estimativa" antes de salvar
- [ ] Apos salvar, backend retorna valores oficiais que substituem o preview
- [ ] Endpoint `POST /fichas/{id}/preview` disponivel para simular sem persistir

**Notas tecnicas:**
- Backend: `POST /api/v1/fichas/{id}/preview` — ja implementado
- Frontend: `FichaCalculationService` — existente mas nao integrado na ficha real

---

### US-049: Tela de visualizacao da ficha (view mode vs edit mode)
**Como** Jogador **quero** alternar entre modo de visualizacao e edicao na ficha **para** consultar dados rapido sem ativar edicao acidentalmente

**Prioridade:** Must Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Tela `/jogador/fichas/{id}` exibe ficha em modo leitura (atualmente placeholder)
- [ ] Botao "Editar" ativa campos editaveis
- [ ] Botao "Salvar" / "Cancelar" no modo edicao
- [ ] Navigacao por abas: Identidade, Progressao, Atributos, Aptidoes, Bonus, Vida, Vantagens, Anotacoes

**Notas tecnicas:**
- Backend: `GET /api/v1/fichas/{id}`, `GET /api/v1/fichas/{id}/resumo`
- Frontend: `FichaDetailComponent` — completamente placeholder, precisa ser reimplementado
- Testes: modo leitura nao permite edicao, cancelar desfaz alteracoes

---

### US-050: Resumo calculado da ficha
**Como** Jogador **quero** ver um resumo rapido da ficha com todos os valores calculados (atributos totais, vida, essencia, bonus) **para** ter uma visao geral do estado do personagem

**Prioridade:** Must Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Painel de resumo sempre visivel (ou acessivel) na tela da ficha
- [ ] Exibir: nivel, atributos totais com impeto, vida total, essencia, ameaca, bonus principais
- [ ] Atualizado apos cada salvamento

**Notas tecnicas:**
- Backend: `GET /api/v1/fichas/{id}/resumo`
- Frontend: componente `FichaResumoComponent`

---

### US-051: Galeria de imagens da ficha
**Como** Jogador **quero** adicionar imagens ao meu personagem (retrato, referencias) **para** deixar a ficha mais imersiva

**Prioridade:** Could Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Upload de imagem principal (retrato)
- [ ] Galeria de imagens de referencia (minimo 3 fotos)
- [ ] Visualizacao em modal/lightbox

**Notas tecnicas:**
- Backend: endpoints de galeria a implementar (SPEC 010)
- Frontend: novo componente `FichaGaleriaTabComponent`

---

### US-052: Historico de edicoes (auditoria)
**Como** Mestre ou Jogador **quero** ver um historico de quando e o que foi alterado na ficha **para** rastrear mudancas e reversoes

**Prioridade:** Could Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Lista de alteracoes com: data, usuario, campo alterado, valor anterior, novo valor
- [ ] Filtro por data e por campo
- [ ] Somente MESTRE ve o historico completo; Jogador ve apenas suas proprias alteracoes

**Notas tecnicas:**
- Backend: Hibernate Envers ja auditando fichas
- Frontend: novo componente `FichaHistoricoTabComponent`

---

### US-053: Impressao/PDF da ficha
**Como** Jogador **quero** exportar minha ficha em PDF **para** ter uma copia impressa para sessoes offline

**Prioridade:** Could Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Botao "Imprimir / Exportar PDF"
- [ ] Layout de impressao formatado (sem menu, sem header de app)
- [ ] Inclui todos os dados relevantes da ficha

**Notas tecnicas:**
- Frontend: CSS `@media print` ou biblioteca de PDF
- Backend: nenhuma alteracao necessaria

---

### US-054: Formulario de criacao de ficha conectado com configs reais
**Como** Jogador **quero** que o formulario de criacao de ficha carregue as opcoes reais do jogo (racas, classes, generos do backend) **para** criar meu personagem dentro das regras definidas pelo Mestre

**Prioridade:** Must Have
**Epico:** E6 — Ficha Visualizacao/Edicao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Dropdowns de Raca, Classe, Genero, Indole, Presenca carregam dados das APIs de config do jogo ativo
- [ ] Atributos exibidos sao os do jogo, nao hardcoded (FOR/DES/CON/INT/SAB/CAR substituidos pelos atributos reais)
- [ ] Formulario atual (FichaFormComponent) tem estrutura desalinhada com API real — precisa refatoracao

**Notas tecnicas:**
- Backend: `/api/v1/configuracoes/atributos?jogoId=`, `/api/v1/configuracoes/racas?jogoId=`, etc.
- Frontend: `FichaFormComponent` — seção identificacao usa campos errados (indole/linhagem como texto livre em vez de FK)

---

## EPICO 7 — Mestre: Visao de Fichas e NPCs

### US-055: Listar todas as fichas do jogo (visao mestre)
**Como** Mestre **quero** ver todas as fichas de jogadores do meu jogo com filtros **para** monitorar o estado dos personagens

**Prioridade:** Must Have
**Epico:** E7 — Mestre: Fichas e NPCs
**Status:** 🔄 Em andamento

**Criterios de aceite:**
- [x] Aba "Fichas" no JogoDetail mostra fichas do jogo
- [ ] Filtros: nome, classe, raca, nivel
- [ ] Informacoes extras na listagem: classe, raca, nivel, ultima atualizacao
- [ ] Acoes: Ver ficha completa, Editar, Duplicar

**Notas tecnicas:**
- Backend: `GET /api/v1/jogos/{jogoId}/fichas?nome=&classeId=&racaId=&nivel=`
- Frontend: `JogoDetailComponent` (aba Fichas) — ampliar cards

---

### US-056: Ver e editar ficha de qualquer jogador (Mestre)
**Como** Mestre **quero** visualizar e editar a ficha de qualquer jogador **para** conceder XP, ajustar valores e corrigir inconsistencias

**Prioridade:** Must Have
**Epico:** E7 — Mestre: Fichas e NPCs
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Mestre acessa `/fichas/{id}` de qualquer ficha do seu jogo
- [ ] Pode editar XP (unico campo que jogador nao pode editar)
- [ ] Pode editar atributos com poderes de mestre (sem restricao de pontos)
- [ ] Indicacao visual de "editando como Mestre"
- [ ] Historico da edicao registrado com autor

**Notas tecnicas:**
- Backend: `PUT /api/v1/fichas/{id}` — MESTRE pode editar qualquer ficha do jogo
- Frontend: logica de permissao baseada em role no componente de ficha

---

### US-057: Dashboard de Mestre com dados reais da campanha
**Como** Mestre **quero** ver metricas em tempo real (fichas por nivel, participantes pendentes, progresso da campanha) **para** gerenciar melhor minha sessao

**Prioridade:** Should Have
**Epico:** E7 — Mestre: Fichas e NPCs
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Widget: "Fichas por nivel" (histograma ou lista)
- [ ] Widget: "Participantes pendentes" com botao de aprovacao rapida
- [ ] Widget: "Media de nivel do grupo"
- [ ] Todos os dados vindos de `GET /api/v1/dashboard/mestre`

**Notas tecnicas:**
- Backend: `GET /api/v1/dashboard/mestre` — implementado mas pode precisar expandir campos
- Frontend: refatorar `MestreDashboardComponent` para consumir endpoint real

---

### US-058: Crear e gerenciar NPCs
**Como** Mestre **quero** criar personagens nao-jogadores (NPCs) com fichas completas **para** representar viloes, aliados e comerciantes do mundo

**Prioridade:** Must Have
**Epico:** E7 — Mestre: Fichas e NPCs
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Endpoint dedicado `POST /api/v1/jogos/{jogoId}/npcs` (isNpc=true, jogadorId=null)
- [ ] NPCs aparecem em lista separada das fichas de jogadores
- [ ] NPC pode ter todos os mesmos atributos, aptidoes e vantagens de uma ficha normal
- [ ] Mestre pode marcar NPC como "visivelParaJogadores" para mostrar stats

**Notas tecnicas:**
- Backend: `POST /api/v1/jogos/{jogoId}/npcs`, `GET /api/v1/jogos/{jogoId}/npcs` — ja implementados
- Frontend: pagina de NPCs inexistente, nao ha rota em `app.routes.ts`
- Testes: criar NPC, verificar isNpc=true, nao aparece na lista de fichas

---

### US-059: Duplicar ficha (Mestre duplica NPC ou cria variante)
**Como** Mestre **quero** duplicar um NPC ou ficha de jogador **para** criar variantes rapidamente (ex: Goblin Arqueiro Chefe baseado em Goblin Arqueiro)

**Prioridade:** Should Have
**Epico:** E7 — Mestre: Fichas e NPCs
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Botao "Duplicar" na tela de NPC/ficha
- [ ] Modal com nome e opcao "manterJogador"
- [ ] NPCs duplicados continuam sendo NPCs
- [ ] Fichas duplicadas podem trocar o jogador dono

**Notas tecnicas:**
- Backend: `POST /api/v1/fichas/{id}/duplicar` — ja implementado
- Frontend: botao na tela de detalhes da ficha/NPC

---

### US-060: Banir participante com historico de justificativa
**Como** Mestre **quero** banir um participante registrando o motivo **para** ter rastreabilidade das decisoes de moderacao

**Prioridade:** Could Have
**Epico:** E7 — Mestre: Fichas e NPCs
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Campo opcional "motivo" no banimento
- [ ] Historico de banimentos acessivel ao Mestre

**Notas tecnicas:**
- Backend: ampliar endpoint de banimento com campo motivo

---

### US-061: Convidar jogador por email
**Como** Mestre **quero** convidar jogadores por email **para** trazer pessoas especificas para minha campanha

**Prioridade:** Could Have
**Epico:** E7 — Mestre: Fichas e NPCs
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Campo de email no gerenciamento de participantes
- [ ] Email enviado com link de convite
- [ ] Convite tem prazo de validade

**Notas tecnicas:**
- Backend: endpoint a implementar
- Frontend: modal de convite

---

## EPICO 8 — Modo Sessao (Jogo Ativo)

### US-062: Tela de modo sessao simplificada
**Como** Jogador **quero** uma tela simplificada durante a sessao com os dados mais importantes **para** jogar sem precisar navegar pela ficha completa

**Prioridade:** Should Have
**Epico:** E8 — Modo Sessao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Rota `/sessao` acessivel para jogadores com jogo ativo
- [ ] Exibir: nivel, vida total/membro com barras, essencia, bonus principais (BBA, BBM, Reflexo)
- [ ] Acoes rapidas: ver aptidoes e vantagens
- [ ] Layout responsivo para mobile (jogadores usam celular na mesa)

**Notas tecnicas:**
- Backend: `GET /api/v1/fichas/{id}/resumo`
- Frontend: novo modulo `/features/sessao/`

---

### US-063: Acao rapida de dano por membro
**Como** Jogador **quero** registrar dano em membros especificos com 1-2 cliques **para** manter o controle de vida durante o combate

**Prioridade:** Must Have
**Epico:** E8 — Modo Sessao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Cards de membros do corpo com vida atual/maxima
- [ ] Botoes "+Dano" e "+Cura" em cada membro
- [ ] Campo de quantidade de dano/cura
- [ ] Barra de vida que muda de cor (verde/amarelo/vermelho)
- [ ] Atualizacao imediata sem reload

**Notas tecnicas:**
- Backend: `PUT /api/v1/fichas/{id}` com dano do membro
- Frontend: componente `MembrosVidaComponent`

---

### US-064: Acao rapida de essencia
**Como** Jogador **quero** gastar e recuperar essencia com 1 clique **para** gerenciar meu recurso magico durante o jogo

**Prioridade:** Must Have
**Epico:** E8 — Modo Sessao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Barra/contador de essencia atual/maxima
- [ ] Botoes "+Gastar" e "+Recuperar" com campo de quantidade
- [ ] Nao permitir essencia negativa (limitar ao maximo)
- [ ] Feedback visual imediato

**Notas tecnicas:**
- Backend: `PUT /api/v1/fichas/{id}`
- Frontend: componente `EssenciaRapidaComponent`

---

### US-065: Acao rapida de prospeccao
**Como** Jogador **quero** ver e usar meus dados de prospeccao **para** ativar esse recurso raro quando necessario

**Prioridade:** Should Have
**Epico:** E8 — Modo Sessao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Exibir contador de cada tipo de dado disponivel
- [ ] Botao "Usar" decrementa o contador
- [ ] Confirmacao antes de usar (recurso raro)

**Notas tecnicas:**
- Backend: `PUT /api/v1/fichas/{id}` atualizando prospeccao
- Frontend: componente `ProspeccaoRapidaComponent`

---

### US-066: Modo sessao no celular (responsividade)
**Como** Jogador **quero** usar o modo sessao no meu celular durante a partida **para** nao precisar carregar laptop para a mesa

**Prioridade:** Must Have
**Epico:** E8 — Modo Sessao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Layout mobile-first no modo sessao
- [ ] Touch targets adequados (min 44px)
- [ ] Dados criticos visiveis sem scroll horizontal
- [ ] Carregamento rapido (< 2s)

**Notas tecnicas:**
- Frontend: PrimeFlex breakpoints, testar em viewport 375px

---

### US-067: Mestre distribui XP durante sessao
**Como** Mestre **quero** distribuir XP para fichas durante a sessao **para** registrar a progressao do grupo em tempo real

**Prioridade:** Should Have
**Epico:** E8 — Modo Sessao
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Mestre ve lista de fichas ativas do jogo
- [ ] Campo de XP a adicionar por ficha (ou lote para todas)
- [ ] Apos adicionar XP, sistema verifica se personagem pode subir de nivel e notifica
- [ ] Historico da concessao de XP (quem deu, quando, quanto)

**Notas tecnicas:**
- Backend: `PUT /api/v1/fichas/{id}` com novo campo XP
- Frontend: painel exclusivo do Mestre no modo sessao

---

## EPICO 9 — Flow de Level Up

### US-068: Notificacao de level up disponivel
**Como** Jogador **quero** ser notificado quando tenho XP suficiente para subir de nivel **para** nao perder a progressao do meu personagem

**Prioridade:** Must Have
**Epico:** E9 — Level Up
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Badge/indicador visivel no menu ou dashboard quando nivel disponivel
- [ ] Toast de notificacao na proxima vez que abrir a ficha apos ganhar XP suficiente
- [ ] Link direto para o wizard de level up

**Notas tecnicas:**
- Backend: `GET /api/v1/fichas/{id}/resumo` retorna se pode subir nivel
- Frontend: computed no FichasStore verificando `xp >= xpProximoNivel`

---

### US-069: Wizard de level up — distribuir pontos de atributo
**Como** Jogador **quero** ser guiado na distribuicao dos pontos de atributo ao subir de nivel **para** nao errar a alocacao

**Prioridade:** Must Have
**Epico:** E9 — Level Up
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Tela com todos os atributos e pontos a distribuir (ex: 3 pontos)
- [ ] Contador de pontos restantes
- [ ] Validacao: nao exceder o limitador do novo nivel
- [ ] Nao permitir avancar sem usar todos os pontos

**Notas tecnicas:**
- Backend: `PUT /api/v1/fichas/{id}/atributos`
- Frontend: novo componente `LevelUpWizardComponent`

---

### US-070: Wizard de level up — comprar vantagens
**Como** Jogador **quero** comprar vantagens com meus pontos de vantagem no level up **para** aprimorar meu personagem

**Prioridade:** Must Have
**Epico:** E9 — Level Up
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Listagem de vantagens disponiveis (pre-requisitos atendidos)
- [ ] Destaque para vantagens ja compradas (possibilidade de upgrade)
- [ ] Pontos de vantagem disponiveis com contador
- [ ] Validacao de pre-requisitos em tempo real

**Notas tecnicas:**
- Backend: `POST /api/v1/fichas/{id}/vantagens`, `PUT /api/v1/fichas/{id}/vantagens/{vid}`
- Frontend: reutilizar parte do `FichaVantagensTabComponent`

---

### US-071: Wizard de level up — confirmacao
**Como** Jogador **quero** revisar todas as escolhas do level up antes de confirmar **para** nao cometer erros irreversiveis

**Prioridade:** Must Have
**Epico:** E9 — Level Up
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Tela de resumo: novo nivel, atributos alocados, vantagens compradas, deltas de vida/essencia
- [ ] Botao "Confirmar" persistindo todas as mudancas
- [ ] Botao "Voltar" permite rever escolhas sem perder dados
- [ ] Apos confirmar, redirecionar para ficha atualizada

**Notas tecnicas:**
- Backend: salvar em batch ou em transacao
- Frontend: estado do wizard gerenciado por signal local

---

### US-072: Renascimento (nivel 31+)
**Como** Jogador no nivel 31+ **quero** iniciar o processo de renascimento **para** desbloquear poderes exclusivos de personagens transcendentes

**Prioridade:** Could Have
**Epico:** E9 — Level Up
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Opcao de renascimento disponivel apenas se nivel >= 31 e `permitirRenascimento=true` na config
- [ ] Confirmacao com aviso sobre o que muda (bônus em vida, essencia, ameaca)
- [ ] Contador de renascimentos incrementado
- [ ] Desbloqueio de vantagens exclusivas de renascimento na lista

**Notas tecnicas:**
- Backend: `PUT /api/v1/fichas/{id}` incrementando `renascimentos`
- Frontend: passo adicional no LevelUpWizard

---

## EPICO 10 — NPC

### US-073: CRUD completo de NPCs (Mestre)
**Como** Mestre **quero** criar e gerenciar NPCs com fichas completas **para** ter todos os personagens do mundo prontos para as sessoes

**Prioridade:** Must Have
**Epico:** E10 — NPC
**Status:** 🔄 Em andamento

**Criterios de aceite:**
- [x] Backend: `POST /api/v1/jogos/{jogoId}/npcs`, `GET /api/v1/jogos/{jogoId}/npcs` implementados
- [x] Backend: DTO `NpcCreateRequest` criado
- [ ] Frontend: pagina `/mestre/jogos/{id}/npcs` com listagem
- [ ] Frontend: formulario de criacao de NPC (pode reutilizar FichaFormComponent com isNpc=true)
- [ ] Frontend: rota registrada em `app.routes.ts`

**Notas tecnicas:**
- Backend: endpoints existem; NPCs nao aparecem na lista de fichas de jogadores
- Frontend: nada implementado ainda (nao ha rota nem componente)
- Testes: criar NPC, verificar que nao aparece em `/fichas`

---

### US-074: Editar ficha de NPC (atributos, aptidoes, vantagens)
**Como** Mestre **quero** editar todos os atributos, aptidoes e vantagens de um NPC **para** ter controle total dos stats dos inimigos e aliados

**Prioridade:** Must Have
**Epico:** E10 — NPC
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Mesma interface da ficha de jogador, mas sem restricoes de pontos de atributo
- [ ] Mestre pode setar qualquer valor em qualquer campo
- [ ] Salvar NPC usa os mesmos endpoints de ficha

**Notas tecnicas:**
- Backend: `PUT /api/v1/fichas/{id}`, `PUT /api/v1/fichas/{id}/atributos`, etc.
- Frontend: reutilizar componentes de edicao de ficha

---

### US-075: Listar e filtrar NPCs
**Como** Mestre **quero** ver todos os NPCs do meu jogo com filtros **para** encontrar rapidamente o personagem que preciso na sessao

**Prioridade:** Should Have
**Epico:** E10 — NPC
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Lista separada das fichas de jogadores
- [ ] Filtros: nome, classe, raca, nivel
- [ ] Acoes: Ver, Editar, Duplicar, Excluir

**Notas tecnicas:**
- Backend: `GET /api/v1/jogos/{jogoId}/npcs` com filtros de query param
- Frontend: `NpcsListComponent`

---

### US-076: Compartilhar stats de NPC com jogadores
**Como** Mestre **quero** opcionalmente compartilhar os stats de um NPC com os jogadores **para** permitir que eles vejam as estatisticas do inimigo

**Prioridade:** Could Have
**Epico:** E10 — NPC
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Toggle "Visivel para jogadores" no NPC
- [ ] Se visivel, jogadores aprovados podem ver a ficha do NPC
- [ ] Dados sensiveis (anotacoes do Mestre) nunca aparecem para jogadores

**Notas tecnicas:**
- Backend: endpoint `GET /api/v1/fichas/{id}` ja controla permissoes por role
- Frontend: indicador visual de NPCs visíveis vs secretos

---

### US-077: Duplicar NPC como template
**Como** Mestre **quero** duplicar um NPC existente para criar variantes rapidamente **para** poupar tempo criando inimigos semelhantes

**Prioridade:** Should Have
**Epico:** E10 — NPC
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Botao "Duplicar" na listagem e no detalhe do NPC
- [ ] Modal com novo nome
- [ ] NPC duplicado e um NPC (isNpc=true mantido)

**Notas tecnicas:**
- Backend: `POST /api/v1/fichas/{id}/duplicar` com `{ novoNome, manterJogador: false }` — ja implementado
- Frontend: botao na UI de NPCs

---

## EPICO 11 — Testes e Qualidade

> Nota: atualmente apenas 2 arquivos `.spec.ts` existem no frontend. A meta e atingir pelo menos 1 teste por componente principal.

### US-078: Testes de servicos de API (100% cobertura)
**Como** desenvolvedor **quero** testes unitarios para todos os servicos de API **para** garantir que os contratos com o backend estao corretos

**Prioridade:** Should Have
**Epico:** E11 — Testes
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] `ConfigApiService` — testes para cada metodo HTTP
- [ ] `FichasApiService` — testes para CRUD e endpoints especializados
- [ ] `JogosApiService` — testes para CRUD e duplicar/export/import
- [ ] Mock do `HttpClient` via `HttpClientTestingModule`
- [ ] Cobertura > 80% dos servicos de API

**Notas tecnicas:**
- Frontend: Vitest + `@testing-library/angular`
- Testes: `/src/core/services/api/*.spec.ts`

---

### US-079: Testes de servicos de negocio
**Como** desenvolvedor **quero** testes dos servicos de negocio (FichaBusinessService, FichaCalculationService) **para** garantir que as regras de dominio estao corretas

**Prioridade:** Should Have
**Epico:** E11 — Testes
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] `FichaCalculationService` — testes de cada formula (BBA, BBM, vida, essencia, impeto)
- [ ] `FichaBusinessService` — testes de criacao, atualizacao, validacoes
- [ ] Cenarios de erro cobertos (sem jogo ativo, jogador nao aprovado)

**Notas tecnicas:**
- Frontend: `/src/core/services/business/*.spec.ts`

---

### US-080: Testes de componentes de configuracao
**Como** desenvolvedor **quero** testes dos componentes de configuracao (Atributos, Vantagens, Racas, Classes) **para** garantir que o CRUD funciona corretamente

**Prioridade:** Should Have
**Epico:** E11 — Testes
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] `AtributosConfigComponent` — teste de criacao, edicao, exclusao, busca
- [ ] `VantagensConfigComponent` — teste de pre-requisitos, aba de dados
- [ ] `RacasConfigComponent` — teste de bonus/penalidades, classes permitidas
- [ ] `ClassesConfigComponent` — teste de bonus e aptidoes com bonus
- [ ] Mocks dos servicos de config

---

### US-081: Testes da ficha de personagem (quando implementada)
**Como** desenvolvedor **quero** testes das abas da ficha **para** garantir que o coracão do produto funciona sem regressoes

**Prioridade:** Must Have
**Epico:** E11 — Testes
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] `FichaAtributosTabComponent` — distribuicao de pontos, limitador, calculo de impeto
- [ ] `FichaVantagensTabComponent` — compra com pre-requisitos, pontos insuficientes
- [ ] `FichaAnotacoesTabComponent` — permissoes de leitura (jogador nao ve notas privadas do Mestre)
- [ ] Testes E2E criticos (Vitest + navegacao real)

---

### US-082: Testes de guards e interceptors
**Como** desenvolvedor **quero** testes dos guards de autenticacao e role **para** garantir que rotas protegidas nao sao acessadas sem permissao

**Prioridade:** Must Have
**Epico:** E11 — Testes
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] `authGuard` — redireciona para login se nao autenticado
- [ ] `roleGuard` — bloqueia JOGADOR de acessar rotas MESTRE (e vice-versa)
- [ ] `currentGameGuard` — bloqueia acesso a configs sem jogo selecionado
- [ ] `authInterceptor` — adiciona XSRF-TOKEN nos requests mutantes

---

### US-083: Testes de componentes de dashboard
**Como** desenvolvedor **quero** testes dos dashboards de Mestre e Jogador **para** garantir que metricas sao exibidas corretamente

**Prioridade:** Should Have
**Epico:** E11 — Testes
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] `MestreDashboardComponent` — exibe contadores corretos, acoes rapidas funcionam
- [ ] `JogadorDashboardComponent` — exibe fichas do jogo correto, empty state

---

### US-084: Testes de autenticacao e fluxo OAuth
**Como** desenvolvedor **quero** testes do fluxo de login/logout **para** garantir que a autenticacao e robusta

**Prioridade:** Must Have
**Epico:** E11 — Testes
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] `LoginComponent` — botao "Entrar com Google" chama `AuthService.login()`
- [ ] `OAuthCallbackComponent` — processa callback e redireciona para dashboard
- [ ] `IdleService` — logout apos 30 min de inatividade

---

### US-085: Setup de CI com execucao de testes automatizados
**Como** desenvolvedor **quero** testes executando automaticamente no CI **para** pegar regressoes antes do merge

**Prioridade:** Should Have
**Epico:** E11 — Testes
**Status:** ⬜ Nao iniciado

**Criterios de aceite:**
- [ ] Pipeline CI executa `npm test` (Vitest) no frontend
- [ ] Pipeline CI executa `./mvnw test` no backend
- [ ] Falha de teste bloqueia o merge
- [ ] Relatorio de cobertura publicado

---

## Resumo Executivo

### Contagem por Epico

| Epico | Total | Concluidas | Em andamento | Nao iniciadas |
|-------|-------|------------|--------------|---------------|
| E1 — Autenticacao e Onboarding | 5 | 3 | 1 | 1 |
| E2 — Mestre: Gestao de Jogos | 8 | 4 | 2 | 2 |
| E3 — Mestre: Participantes | 5 | 3 | 0 | 2 |
| E4 — Mestre: Configuracoes | 14 | 9 | 3 | 2 |
| E5 — Jogador: Criacao de Ficha | 5 | 2 | 2 | 1 |
| E6 — Jogador: Ficha Detalhada | 17 | 0 | 0 | 17 |
| E7 — Mestre: Fichas e NPCs | 7 | 0 | 1 | 6 |
| E8 — Modo Sessao | 6 | 0 | 0 | 6 |
| E9 — Level Up | 5 | 0 | 0 | 5 |
| E10 — NPC | 5 | 0 | 1 | 4 |
| E11 — Testes e Qualidade | 8 | 0 | 0 | 8 |
| **TOTAL** | **85** | **21** | **10** | **54** |

---

### Top 5 Itens de Maior Prioridade Imediata

1. **US-054 / US-033** — Formulario de criacao de ficha conectado com configs reais (racas, classes, atributos do backend, nao hardcoded). Bloqueador para qualquer uso real do produto.

2. **US-049** — Tela de visualizacao da ficha (`FichaDetailComponent` e placeholder completo). E o componente central do produto — sem ele, o produto nao funciona para o jogador.

3. **US-041** — Aba Atributos com distribuicao de pontos, calculo de impeto e validacao de limitador. Maior mecanica do jogo.

4. **US-029** — Reordenacao batch com persistencia real no backend. O drag-and-drop existe na UI mas nao salva — gera frustacao imediata no Mestre.

5. **US-025** — VantagemEfeito (sub-recurso dos 8 tipos de efeito). Sem isso, as vantagens nao tem impacto mecanico real no jogo.

---

### Estimativa Honesta de Percentual de Produto Entregue

| Dimensao | % Entregue | Observacao |
|----------|-----------|------------|
| Backend (APIs) | ~75% | 13 CRUDs completos, fichas, NPCs, duplicacao, export/import implementados. Faltam: VantagemEfeito (interface), galeria, convite por email |
| Frontend — Autenticacao | ~90% | Login, logout, perfil, guards funcionando. Falta onboarding de primeiro acesso |
| Frontend — Gestao de Jogos | ~70% | CRUD completo, mas export/import sem acao real, duplicar sem UI, dashboard com dados locais |
| Frontend — Configuracoes | ~65% | 14 componentes criados, mas reordenacao nao persiste, VantagemEfeito ausente, formulas sem validacao |
| Frontend — Ficha do Jogador | ~15% | Formulario de criacao existe mas desalinhado com API; FichaDetail e placeholder; todas as 9+ abas nao existem |
| Frontend — NPCs | ~5% | Backend pronto, zero frontend |
| Frontend — Modo Sessao | ~0% | Nao iniciado |
| Frontend — Level Up | ~0% | Nao iniciado |
| **Overall do Produto** | **~35%** | A espinha dorsal (auth, jogos, configs) esta solida. O coracao (ficha detalhada, modo sessao, level up) nao existe |

---

*Backlog gerado em 2026-03-31 por revisao completa do domínio e auditoria do codigo frontend e backend.*
