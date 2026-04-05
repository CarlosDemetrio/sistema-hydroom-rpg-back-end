
# UX BACKLOG — Klayrah RPG Sistema de Fichas

**Data da Auditoria:** 31 de Março de 2026
**Status:** Auditoria Completa

---

## EXECUTIVE SUMMARY


A aplicação está **~75% funcional** (Phase 1), mas tem **2 bloqueadores críticos** e vários problemas de UX que degradam a experiência. O sistema é tecnicamente sólido (bom padrão de componentes, signals, reactive forms), mas precisa de melhorias nas **jornadas do usuário** e **feedback visual**.

- ✅ 26 componentes completos ou bem implementados
- ❌ 2 componentes placeholder críticos (FichaDetail, JogosDisponiveis)
- ⚠️ Formulário de ficha muito longo (1 page, scroll infinito)
- ⚠️ Sem sistema de notificações
- ⚠️ Sem editor de fórmulas (customização bloqueada)
- ⚠️ Apenas 2 arquivos de teste em todo o frontend
- ✅ Design system bem estruturado (CSS variables, theme tokens)
- ✅ BaseConfigTable é um excelente componente reutilizável

---

## SEÇÃO 1: AUDIT DE COMPONENTES

| Componente | Rota | Status | Problemas UX identificados |
|---|---|---|---|
| LoginComponent | `/login` | ✅ OK | Sem feedback de erro de auth; sem loading state no botão |
| ProfileComponent | `/profile` | ✅ Completo | — |
| DashboardComponent | `/dashboard` | ✅ Funcional | Roteamento por role OK |
| MestreDashboardComponent | `/mestre/dashboard` | ✅ Funcional | Falta jogo ativo visível |
| JogadorDashboardComponent | `/jogador/dashboard` | ✅ Funcional | Falta destaque do jogo selecionado |
| FichasListComponent | `/jogador/fichas` | ✅ Excelente | Busca, filtros, empty states OK |
| **FichaDetailComponent** | `/jogador/fichas/:id` | ❌ PLACEHOLDER | **CRÍTICO**: placeholder "Em Desenvolvimento" |
| FichaFormComponent | `/jogador/fichas/nova` | ✅ Funcional (10 seções) | Muito longo; sem wizard; sem validação visual |
| IdentificacaoSectionComponent | (embarcado) | ✅ Completo | — |
| ProgressaoSectionComponent | (embarcado) | ✅ Completo | — |
| DescricaoFisicaSectionComponent | (embarcado) | ✅ Completo | — |
| AtributosSectionComponent | (embarcado) | ✅ Funcional | Falta visual de range do limitador |
| VidaSectionComponent | (embarcado) | ✅ Funcional | Falta membros do corpo |
| PericiasSectionComponent | (embarcado) | ✅ Funcional | Falta lista de perícias do jogo |
| VantagensSectionComponent | (embarcado) | ✅ Funcional | Falta efeitos estruturados (VantagemEfeito) |
| ObservacoesSectionComponent | (embarcado) | ✅ Completo | — |
| **JogosDisponiveisComponent** | `/jogador/jogos` | ❌ PLACEHOLDER | **CRÍTICO**: placeholder "Em Desenvolvimento" |
| JogosListComponent (Mestre) | `/mestre/jogos` | ✅ Excelente | Tabela com filtros, busca perfeitos |
| JogoFormComponent | `/mestre/jogos/novo` | ✅ Funcional | — |
| JogoDetailComponent | `/mestre/jogos/:id` | ✅ Funcional | 3 tabs; participantes OK; falta ficha view |
| ConfigLayoutComponent | `/mestre/config` | ✅ Funcional | Sidebar OK; falta contexto visual |
| 13x ConfigComponents | `/mestre/config/*` | ✅ Funcional | CRUD básico; sem validação de sigla única; sem editor fórmulas |
| BaseConfigTableComponent | (compartilhado) | ✅ Excelente | Componente reutilizável bem feito |

---

## SEÇÃO 2: JORNADAS CRÍTICAS DE USUÁRIO

### Jornada 1: Novo Jogador → Primeira Ficha

**Fluxo Ideal:**
```
Login → Dashboard → Selecionar Jogo → Wizard Ficha (5 passos) → Preview → Ficha criada e visualizada
```

**Fluxo Atual:**
```
Login ✅ → Dashboard ✅ → Form 10 seções (muito scroll) → Salva → Lista
→ Clica na ficha → TELA BRANCA ❌
```

**Problemas:** FichaDetail não implementado; form muito longo; sem celebração de sucesso.

---

### Jornada 2: Jogador → Buscar/Entrar em um Jogo

**Fluxo Ideal:**
```
Dashboard → "Buscar Jogos" → Cards de jogos → Solicitar Acesso → Status Pendente
→ Mestre aprova → Notificação → Jogo selecionável
```

**Fluxo Atual:**
```
Menu → Jogos Disponíveis → TELA EM BRANCO ❌
```

**Impacto:** CRÍTICO — jogador isolado, não consegue entrar em jogos.

---

### Jornada 3: Mestre → Configurar Novo Jogo

**Fluxo Atual:**
```
Criar jogo ✅ → Detail OK ✅ → Config com 13 opções ✅
→ Sem validação de sigla única ⚠️ → Sem editor de fórmulas ⚠️
```

---

### Jornada 4: Jogador → Level Up / Progressão

**Status:** ❌ Não implementado — sem sistema de XP/level no frontend.

---

### Jornada 5: Modo Sessão Ativa

**Status:** ❌ Não existe — jogador precisa de interface simplificada durante partida.

---

## SEÇÃO 3: PROBLEMAS DE UX POR SEVERIDADE

### 🔴 CRÍTICOS (Bloqueiam Uso)

#### 1. FichaDetailComponent não implementado
- **Rota:** `/jogador/fichas/:id`
- **Impacto:** Jogador cria ficha mas não consegue visualizá-la
- **Solução:** Tela com abas (Resumo | Stats | Competências | Anotações) + botão editar
- **Esforço:** ~8h

#### 2. JogosDisponiveisComponent não implementado
- **Rota:** `/jogador/jogos`
- **Impacto:** Jogador não consegue buscar ou solicitar entrada em jogos
- **Solução:** Cards de jogos com filtros, busca, botão "Solicitar Acesso"
- **Esforço:** ~6h

#### 3. Zero testes de componente
- **Impacto:** Nenhuma rede de segurança para regressões
- **Solução:** Cobertura mínima: 1 teste por componente crítico (Vitest + Testing Library)
- **Esforço:** ~16h para cobertura básica

---

### 🟠 IMPORTANTES (Degradam Experiência)

#### 4. Formulário de ficha muito longo
- **Rota:** `/jogador/fichas/nova`
- **Solução:** Converter para Form Wizard (5-6 passos com stepper visual)
- **Esforço:** ~12h

#### 5. Sem validação visual de campos
- **Solução:** Borda vermelha + ícone + mensagem abaixo do campo inválido
- **Esforço:** ~4h

#### 6. Sem editor de fórmulas
- **Rota:** `/mestre/config/bonus`, `/mestre/config/vantagens`
- **Solução:** Modal com editor + validação de sintaxe + autocomplete de variáveis
- **Esforço:** ~8h

#### 7. Sem validação de sigla única no frontend
- **Rota:** `/mestre/config/*` (todos os configs com sigla)
- **Impacto:** Viola regra crítica do domínio
- **Solução:** Verificação assíncrona + mensagem de erro clara
- **Esforço:** ~2h

#### 8. Sem indicador de jogo selecionado
- **Impacto:** Usuário não sabe em qual jogo está
- **Solução:** Header com "Jogo Atual: {nome}" selecionável via dropdown
- **Esforço:** ~4h

#### 9. Sem feedback de carregamento no login
- **Solução:** Loading state no botão "Entrar com Google"
- **Esforço:** ~1h

#### 10. FichaFormComponent sem sub-seções de VantagemEfeito
- **Impacto:** Efeitos estruturados (novo backend) não aparecem na ficha
- **Solução:** VantagensSectionComponent exibir efeitos de cada vantagem comprada

---

### 🟡 MELHORIAS (Nice to Have)

- Modo "Sessão de Jogo" (vida grande, rolar dados)
- Histórico de alterações de ficha (auditoria visual)
- Export para PDF
- Auto-save do formulário
- Convite por link personalizado
- Notificações em tempo real

---

## SEÇÃO 4: COMPONENTES FALTANTES (Prioritizados)

| # | Componente | Rota | Prioridade | Esforço |
|---|---|---|---|---|
| 1 | FichaDetailComponent | `/jogador/fichas/:id` | 🔴 P0 | 8h |
| 2 | JogosDisponiveisComponent | `/jogador/jogos` | 🔴 P0 | 6h |
| 3 | FichaWizardComponent | `/jogador/fichas/nova` | 🟠 P1 | 12h |
| 4 | EditorFormulaComponent | `/mestre/config/*` | 🟠 P1 | 8h |
| 5 | NotificationCenterComponent | Header global | 🟠 P1 | 10h |
| 6 | ConviteJogadoresModal | `/mestre/jogos/:id` | 🟠 P1 | 6h |
| 7 | SessaoModoComponent | `/jogador/sessao` | 🟡 P2 | 10h |
| 8 | LevelUpWizardComponent | `/jogador/fichas/:id/level-up` | 🟡 P2 | 8h |
| 9 | HistoricoAlteracoesComponent | Modal em FichaDetail | 🟡 P2 | 6h |

---

## SEÇÃO 5: DESIGN SYSTEM — O QUE FALTA

### Componentes Genéricos Não Implementados
- `NotificationBell` (bell icon com badge)
- `BreadcrumbsComponent` (navegação contextual)
- `StepperComponent` (form wizard)
- `FormulaEditorComponent` (editor de fórmulas exp4j)
- `TimelineComponent` (histórico de alterações)
- `DiceRollerComponent` (rolar dados)
- `BarraVidaComponent` (barra de vida com cor dinâmica)

### Design Tokens Faltantes
- `--rpg-success-color` (verde sucesso)
- `--rpg-failure-color` (vermelho falha/crítico)
- `--rpg-crit-color` (dourado crítico)

### Padrões de Interação Faltantes
1. **Inline Editing** — não existe em nenhum lugar
2. **Keyboard Shortcuts** — Ctrl+S, Escape, Tab não mapeadas
3. **Auto-save** — forms perdem dados ao navegar
4. **Empty states com CTA** — inconsistente

---

## SEÇÃO 6: ARQUITETURA DE INFORMAÇÃO RECOMENDADA

### Header Global
```
Logo | Jogo Atual: {nome} ▼ | 🔔 Notificações | Avatar do usuário
```

### Breadcrumbs
```
Dashboard > Meus Jogos > {Jogo XYZ} > Configurações > Atributos
```

### Sidebar Contextual (por role)
```
JOGADOR:
  Dashboard
  Minhas Fichas → [lista de fichas]
  Buscar Jogos
  Perfil

MESTRE:
  Dashboard
  Meus Jogos → [lista de jogos]
    └─ {Jogo Atual}
       ├─ Informações
       ├─ Participantes
       ├─ Fichas (jogadores + NPCs)
       └─ Configurações (13 tipos)
  Perfil
```

---

## TOP 10 PRIORIDADES

| # | Item | Impacto | Esforço | Prioridade |
|---|---|---|---|---|
| 1 | FichaDetailComponent | 🔴 Bloqueador | 8h | P0 |
| 2 | JogosDisponiveisComponent | 🔴 Bloqueador | 6h | P0 |
| 3 | Testes de componente (cobertura básica) | 🔴 Qualidade | 16h | P0 |
| 4 | Refatorar FichaForm para Wizard | 🟠 UX | 12h | P1 |
| 5 | NotificationCenterComponent | 🟠 Transparência | 10h | P1 |
| 6 | Editor de Fórmulas | 🟠 Feature | 8h | P1 |
| 7 | Validação de Sigla Única | 🟠 Integridade | 2h | P1 |
| 8 | Indicador de Jogo no Header | 🟠 Contexto | 4h | P1 |
| 9 | Modo Sessão Ativa | 🟡 Feature | 10h | P2 |
| 10 | Level Up Wizard | 🟡 Feature | 8h | P2 |

---

## ROADMAP RECOMENDADO

### Sprint 1 (1 semana) — Resolver Bloqueadores
- FichaDetailComponent (8h)
- JogosDisponiveisComponent (6h)
- Validação sigla única (2h)
- Indicador jogo no header (4h)
- Testes P0 (8h)
**Total: ~28h**

### Sprint 2 (2 semanas) — Melhorias Essenciais
- FichaForm → Wizard (12h)
- NotificationCenter (10h)
- Editor de Fórmulas (8h)
- Testes P1 (8h)
**Total: ~38h**

### Sprint 3 (2 semanas) — Features Completas
- Modo Sessão (10h)
- Level Up Wizard (8h)
- Histórico de Alterações (6h)
- Polishing visual + responsividade (8h)
- Testes P2 (8h)
**Total: ~40h**
