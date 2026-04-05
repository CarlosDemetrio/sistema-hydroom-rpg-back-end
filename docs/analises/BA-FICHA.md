> **Status: PARCIALMENTE SUPERADO** | Ultima revisao: 2026-04-02
> Backend da ficha ~90% pronto. FichaService, FichaCalculationService, FichaVantagemService, FichaPreviewService, FichaResumoService todos implementados.
> O que mudou desde a escrita: GAP-01 confirma que FichaForm precisa rewrite wizard (envia apenas {nome}). GAP-03 confirma que VantagemEfeito nao esta integrado ao motor. PO decidiu wizard 5-6 passos com auto-save. Ver `docs/gaps/BA-GAPS-2026-04-02.md` para decisoes atualizadas.
> Para requisitos de criacao de ficha, usar dossie de gaps como fonte mais recente.

# BA-FICHA.md — Análise Completa: Ficha de Personagem

> Documento de análise de negócio e UX para a feature central do produto.
> Destina-se ao Frontend Tech Lead e aos Desenvolvedores Frontend.
> Última atualização: 2026-04-01 | Branch backend: `feature/009-npc-fichas-mestre`

---

## 1. Visão Geral da Ficha

A **Ficha** é o artefato central do produto. Representa um personagem (jogador ou NPC) dentro de um Jogo. Tudo que o Mestre configura (atributos, classes, raças, vantagens, bônus) é instanciado e personalizado na Ficha.

### Quem usa, quando usa

| Ator | Quando usa | O que faz |
|------|-----------|-----------|
| Jogador | Sessão de jogo | Consulta atributos, vida, essência; registra anotações; distribui pontos ao subir de nível |
| Mestre | Antes/durante sessão | Cria fichas, concede XP, edita qualquer ficha, cria NPCs, duplica fichas |

### Ficha de Jogador vs NPC

| Aspecto | Jogador | NPC |
|---------|---------|-----|
| `jogadorId` | ID do jogador dono | `null` |
| `isNpc` | `false` | `true` |
| Endpoint de criação | `POST /jogos/{id}/fichas` | `POST /jogos/{id}/npcs` |
| Quem pode criar | MESTRE ou JOGADOR (para si) | Apenas MESTRE |
| Quem pode editar | MESTRE ou dono | Apenas MESTRE |
| Visibilidade na lista | MESTRE vê todas; Jogador vê as suas | Apenas MESTRE (endpoint `/npcs`) |
| Anotações visíveis | Jogador vê as próprias + Mestre visíveis | Apenas MESTRE |

### Estados da Ficha

| Estado | Descrição | Tela |
|--------|-----------|------|
| **Criação** | Mestre ou Jogador cria nova ficha com identificação básica | FichaFormPage (modo create) |
| **Edição** | Distribuição de atributos, aptidões, compra de vantagens | FichaFormPage (modo edit) / abas inline no Detail |
| **Visualização** | Consulta em jogo: atributos, vida, bônus, vantagens, anotações | FichaDetailPage |
| **Progressão de Nível** | Mestre concede XP; nível sobe automaticamente; jogador distribui pontos | FichaDetailPage (ação inline) |

---

## 2. Dados da Ficha (Completo)

### 2.1 Campos base da entidade Ficha

| Campo | Tipo | Origem | Editável por |
|-------|------|--------|-------------|
| `id` | Long | Backend (auto) | Nunca |
| `nome` | String (max 100) | Jogador/Mestre input | MESTRE + dono |
| `jogoId` | Long | Seleção de jogo | Nunca |
| `jogadorId` | Long ou null | Sistema (ao criar) | MESTRE |
| `racaId` / `racaNome` | FK + nome | Seleção de config | MESTRE + dono |
| `classeId` / `classeNome` | FK + nome | Seleção de config | MESTRE + dono |
| `generoId` / `generoNome` | FK + nome | Seleção de config | MESTRE + dono |
| `indoleId` / `indoleNome` | FK + nome | Seleção de config | MESTRE + dono |
| `presencaId` / `presencaNome` | FK + nome | Seleção de config | MESTRE + dono |
| `nivel` | Integer (min 1) | Calculado via XP | Nunca (direto) |
| `xp` | Long (min 0) | MESTRE concede | Apenas MESTRE |
| `renascimentos` | Integer (min 0) | MESTRE concede | Apenas MESTRE |
| `isNpc` | boolean | Definido na criação | Nunca após criar |

### 2.2 Sub-recursos da Ficha

| Sub-recurso | Endpoint | O que contém |
|-------------|----------|-------------|
| Atributos | `PUT /fichas/{id}/atributos` | base, nivel, outros, total (calculado), ímpeto (calculado) |
| Aptidões | `PUT /fichas/{id}/aptidoes` | base, sorte, classe, total (calculado) |
| Vantagens | `GET/POST/PUT /fichas/{id}/vantagens` | nivel_atual, custo_pago, config da vantagem |
| Anotações | `GET/POST/DELETE /fichas/{fichaId}/anotacoes` | titulo, conteudo, tipo, visibilidade |

### 2.3 Dados calculados pelo backend (via /resumo)

O endpoint `GET /api/v1/fichas/{id}/resumo` retorna:

| Campo | Como é calculado |
|-------|-----------------|
| `atributosTotais` | `Record<sigla, total>` — base + nivel + outros + bônus de raça |
| `bonusTotais` | `Record<nome, valor>` — fórmulas de bônus avaliadas pelo FormulaEvaluatorService |
| `vidaTotal` | Vigor + Nível + VT (vantagens) + Renascimentos + Outros |
| `essenciaTotal` | Calculado por fórmulas de vantagens de essência |
| `ameacaTotal` | Nível + itens + títulos + renascimentos + outros |

O frontend **nunca deve calcular esses valores definitivamente**. Use `FichaCalculationService` apenas para preview temporário durante edição.

---

## 3. Regras de Negócio Críticas

### 3.1 Permissões por campo

| Ação | MESTRE | JOGADOR (dono) | JOGADOR (outro) |
|------|--------|----------------|-----------------|
| Ver ficha | Sim | Sim | Não |
| Editar identificação | Sim | Sim | Não |
| Conceder XP | Sim | Não | Não |
| Distribuir atributos | Sim | Sim | Não |
| Distribuir aptidões | Sim | Sim | Não |
| Comprar vantagens | Sim | Sim | Não |
| Criar anotação JOGADOR | Sim | Sim | Não |
| Criar anotação MESTRE | Sim | Não | Não |
| Ver anotações MESTRE ocultas | Sim | Não | Não |
| Deletar ficha | Sim | Não | Não |
| Duplicar ficha | Sim | Sim (só as suas) | Não |

### 3.2 Regras de validação ao salvar

- Nome da ficha: obrigatório, 1-100 chars.
- Atributos: campo `base` não pode exceder o `limitadorAtributo` do nível atual (NivelConfig). Validação no backend; frontend deve exibir barra visual de progresso com o limite.
- Atributos total: `total = base + nivel + outros + bônus_raca`. O `nivel` (campo de atributo) acumula pontos distribuídos ao subir de nível — não pode exceder o total de `pontosAtributo` acumulados até o nível atual.
- Aptidões: `total = base + sorte + classe`. Sem limitador numérico, mas `sorte` e `classe` são controlados pelo Mestre.
- Vantagens: custo calculado pela `formulaCusto` da VantagemConfig. Pontos disponíveis = soma de `pontosVantagem` de todos os níveis até o nível atual.

### 3.3 Regras de progressão de nível

- `nivel` é sempre calculado pelo backend: backend encontra o `NivelConfig` cuja `xpNecessaria <= xp atual` e retorna o maior desses.
- Ao subir de nível, jogador ganha `pontosAtributo` e `pontosAptidao` do NivelConfig.
- `renascimentos` só são possíveis se o NivelConfig do nível atual tem `permitirRenascimento = true` (nivel >= 31 por padrão).
- Frontend deve buscar `/resumo` após qualquer alteração de XP para atualizar todos os valores calculados.

### 3.4 Regras de vantagens

- Uma vez comprada (nivel >= 1), a vantagem **nunca pode ser removida** da ficha.
- O `nivelAtual` só pode subir, nunca descer. Endpoint `PUT /fichas/{id}/vantagens/{vid}` sempre incrementa.
- Pré-requisitos devem ser verificados pelo backend antes de permitir a compra.
- Pontos de vantagem gastos = soma do custo de cada nível comprado, usando `formulaCusto` da VantagemConfig.

### 3.5 Regras de anotações

- `tipoAnotacao = 'JOGADOR'`: Jogadores só podem criar este tipo.
- `tipoAnotacao = 'MESTRE'`: Apenas MESTRE pode criar.
- `visivelParaJogador = true`: Anotações do Mestre marcadas assim ficam visíveis ao jogador dono.
- Mestre pode deletar qualquer anotação; Jogador só as próprias.

---

## 4. Arquitetura de Componentes Recomendada

### 4.1 FichaDetailPage (modo visualização)

```
ficha-detail/
  ficha-detail.page.ts                    [SMART] — carrega ficha + resumo, orquestra abas
  components/
    ficha-header/                         [DUMB] — nome, nível, raça, classe, badge NPC
      @Input ficha: Ficha
      @Input resumo: FichaResumo
      @Output editar: EventEmitter<void>
      @Output duplicar: EventEmitter<void>
    ficha-stats-bar/                      [DUMB] — vida/essência/ameaça em barras
      @Input vidaTotal: number
      @Input vidaAtual: number
      @Input essenciaTotal: number
      @Input essenciaAtual: number
      @Input ameacaTotal: number
    ficha-atributos-tab/                  [DUMB] — grid de atributos com total e ímpeto
      @Input atributos: FichaAtributoResponse[]
      @Input atributosTotais: Record<string, number>
    ficha-bonus-tab/                      [DUMB] — grid de bônus derivados
      @Input bonusTotais: Record<string, number>
    ficha-aptidoes-tab/                   [DUMB] — lista de aptidões agrupadas por tipo
      @Input aptidoes: FichaAptidaoResponse[]
    ficha-vantagens-tab/                  [DUMB] — cards de vantagens compradas
      @Input vantagens: FichaVantagemResponse[]
      @Output aumentarNivel: EventEmitter<number> (vantagemId)
    ficha-anotacoes-tab/                  [SMART] — anotações com CRUD inline
      @Input fichaId: number
      @Input userRole: 'MESTRE' | 'JOGADOR'
      @Input userId: number
      -- usa FichasApiService diretamente --
    anotacao-card/                        [DUMB] — exibe uma anotação
      @Input anotacao: Anotacao
      @Input podeDeletar: boolean
      @Output deletar: EventEmitter<number>
  ficha-detail.routes.ts
```

**FichaDetailPage responsabilidades:**
- Lê `fichaId` da rota.
- Chama `fichasApiService.getFicha(id)` e `getFichaResumo(id)` em paralelo (`forkJoin`).
- Expõe `ficha`, `resumo` como signals.
- Ao receber `aumentarNivel`, chama `fichasApiService.aumentarNivelVantagem(id, vid)` e atualiza lista.
- Botão "Editar" navega para `/fichas/{id}/editar`.
- Botão "Duplicar" (somente MESTRE): abre dialog com input de nome e chama `duplicarFicha`.

### 4.2 FichaFormPage (modo criação/edição)

O modelo atual tem 10 seções com campos desalinhados do backend. A arquitetura recomendada é **wizard de 4 passos** (não 10 seções em scroll):

```
ficha-form/
  ficha-form.page.ts                      [SMART] — wizard, orquestra, salva
  steps/
    step-identificacao/                   [DUMB] — nome, raça, classe, gênero, índole, presença
      @Input jogoId: number
      @Input form: FormGroup
      @Input racas: Raca[]
      @Input classes: ClassePersonagem[]
      @Input generos: GeneroConfig[]
      @Input indoles: IndoleConfig[]
      @Input presencas: PresencaConfig[]
    step-atributos/                       [SMART] — atributos com limitador visual
      @Input fichaId: number | null
      @Input nivelAtual: number
      @Input limitadorAtributo: number
      @Input pontosDisponiveis: number
      @Input atributos: AtualizarAtributoDto[]
      @Output atributosChanged: EventEmitter<AtualizarAtributoDto[]>
      -- exibe barra de progresso base vs limitador --
    step-vantagens/                       [SMART] — marketplace de vantagens
      @Input fichaId: number | null
      @Input pontosDisponiveis: number
      @Input vantagensCompradas: FichaVantagemResponse[]
      @Input vantagensDisponiveis: VantagemConfig[]
      @Output comprar: EventEmitter<number>
    step-revisao/                         [DUMB] — resumo de tudo antes de confirmar
      @Input fichaData: CreateFichaDto
      @Input atributos: AtualizarAtributoDto[]
      @Output confirmar: EventEmitter<void>
      @Output voltar: EventEmitter<void>
  ficha-form.routes.ts
```

**Notas críticas sobre o formulário atual:**
- Os campos `origem`, `linhagem`, `insolitus`, `nvs`, `descricaoFisica` (altura, peso, etc.) **não existem no backend**. O backend tem apenas: `nome`, `racaId`, `classeId`, `generoId`, `indoleId`, `presencaId`, `nivel`, `xp`, `renascimentos`, `isNpc`.
- Atributos não são hardcoded (FOR/DES/CON/INT/SAB/CAR). Devem ser carregados de `GET /api/v1/configuracoes/atributos?jogoId=`.
- Aptidões devem ser carregadas de `GET /api/v1/configuracoes/aptidoes?jogoId=`.

---

## 5. Business Service: FichaBusinessService

O service atual é básico (CRUD + Store). Deve ser expandido:

### Métodos a adicionar

```typescript
// Carrega ficha + resumo em paralelo para o FichaDetailPage
loadFichaCompleta(fichaId: number): Observable<{ ficha: Ficha; resumo: FichaResumo }>

// Atualiza atributos em lote e refaz resumo
atualizarAtributos(fichaId: number, dto: AtualizarAtributoDto[]): Observable<FichaAtributoResponse[]>

// Atualiza aptidões em lote
atualizarAptidoes(fichaId: number, dto: AtualizarAptidaoDto[]): Observable<FichaAptidaoResponse[]>

// Carrega vantagens da ficha (com config aninhada)
loadVantagens(fichaId: number): Observable<FichaVantagemResponse[]>

// Compra vantagem (nivel 1)
comprarVantagem(fichaId: number, vantagemConfigId: number): Observable<unknown>

// Aumenta nível de vantagem
aumentarNivelVantagem(fichaId: number, vid: number): Observable<unknown>

// Duplica ficha (MESTRE ou dono)
duplicarFicha(fichaId: number, dto: DuplicarFichaDto): Observable<DuplicarFichaResponse>

// Calcula pontos de atributo restantes (computed)
pontosAtributoRestantes: Signal<number>   // total ganhos - total distribuídos

// Calcula pontos de vantagem restantes (computed)
pontosVantagemRestantes: Signal<number>   // total ganhos - total gasto em vantagens
```

### O que cachear vs buscar ao vivo

| Dado | Estratégia |
|------|-----------|
| Lista de fichas | Cache no FichasStore; invalida ao criar/deletar |
| Ficha atual (`currentFicha`) | Cache no FichasStore; atualiza após PUT |
| Resumo calculado (`FichaResumo`) | Buscar ao vivo após qualquer save; nunca cachear (sempre calculado) |
| Atributos da ficha | Buscar ao abrir formulário de edição; substituir após PUT |
| Vantagens da ficha | Buscar ao abrir aba; atualizar após compra/upgrade |
| Anotações | Buscar ao abrir aba; gerenciado localmente no `ficha-anotacoes-tab` |
| Configs (raças, classes, etc.) | Cache no ConfigStore (já existe); não rebuscar |

---

## 6. UX: Ficha Detail (Modo Visualização)

### Layout principal

**Desktop**: header fixo (sticky) com nome/nível/raça/classe/NPC badge + stats-bar de vida/essência/ameaça. Abaixo: TabView com 5 abas horizontais.

**Mobile**: header colapsável. Abas viram menu dropdown ou swipe tabs.

### Header (sticky)

```
[Avatar/Inicial] Nome do Personagem          [Nível 5] [Nobre] [Guerreiro]
                 raça • classe                         [NPC badge se isNpc]
──────────────────────────────────────────────────────────────────────────
Vida [████████░░] 25/30   Essência [██████░░░░] 12/20   Ameaça: 16
──────────────────────────────────────────────────────────────────────────
[Editar]  [Duplicar (MESTRE)]  [Deletar (MESTRE)]
```

### Aba Resumo

- Grid 3-4 colunas de cards de atributo: nome, total em destaque, ímpeto abaixo.
- Grid 2-3 colunas de bônus derivados (BBA, BBM, Bloqueio, Reflexo, Percepção, etc.).
- Membros do corpo em lista com barra de HP por membro.

### Aba Aptidões

- Agrupadas por TipoAptidao (Físicas / Mentais).
- Linha por aptidão: nome | base | sorte | classe | total (em destaque).

### Aba Vantagens

- Cards por categoria com cor de fundo da CategoriaVantagem.
- Cada card: nome, sigla, nível atual, custo pago, descrição do efeito.
- Botão "Subir nível" se nível atual < nivelMaximo e pontos disponíveis suficientes.

### Aba Anotações

- Lista de anotações com badge de tipo (JOGADOR / MESTRE) e badge "Visível" se visivelParaJogador.
- Botão "Nova anotação" abre form inline (título + conteúdo + toggle visível para Mestre).
- Jogador não vê toggle de visivelParaJogador nem pode criar anotação MESTRE.
- Mestre vê anotações ocultas com destaque visual diferente (ex: fundo amarelo).

### Estados

| Estado | UX |
|--------|-----|
| Loading | Skeleton screens nas 3 áreas: header, stats-bar, conteúdo da aba |
| Erro ao carregar | Card de erro com botão "Tentar novamente" |
| Ficha sem atributos | Aba Resumo mostra empty state com CTA "Distribuir atributos" |
| Vantagens vazias | Empty state com CTA "Comprar vantagens" |
| Sem anotações | Empty state com CTA "Criar primeira anotação" |

---

## 7. UX: Ficha Form (Criação/Edição)

### Wizard de 4 passos (recomendado)

O scroll único com 10 seções é excessivamente longo e contém campos inexistentes no backend. O wizard garante progressão linear e validação por etapa.

**Passo 1 — Identificação**
- Campos: nome (required), raça (dropdown com config do jogo), classe (dropdown), gênero (dropdown), índole (dropdown), presença (dropdown).
- Raça e Classe são selecionados via config do Mestre — carregar `GET /configuracoes/racas?jogoId=` e similares.
- Validação inline: nome obrigatório. Botão "Próximo" só ativa com nome preenchido.
- Em criação de NPC (rota do Mestre): campo jogadorId some; badge NPC aparece.

**Passo 2 — Atributos**
- Carrega `GET /configuracoes/atributos?jogoId=` — lista dinâmica, não hardcoded.
- Para cada atributo: spinner de `base` + indicador de pontos restantes.
- Barra de progresso por atributo: "Base: 12 / Limite: 20" (limite vem de NivelConfig.limitadorAtributo).
- Contador global: "Pontos disponíveis: 3 / 15 utilizados". Impede distribuir mais do que o disponível.
- Validação em tempo real: não pode exceder limitador por atributo.

**Passo 3 — Aptidões**
- Carrega `GET /configuracoes/aptidoes?jogoId=` — lista dinâmica.
- Agrupadas por tipo. Spinner para `base` de cada aptidão.
- Contador de `pontosAptidao` disponíveis.

**Passo 4 — Revisão e Salvar**
- Tabela resumo de todos os campos preenchidos.
- Botão "Criar Ficha" / "Salvar Alterações" com confirmação.
- Após salvar com sucesso: navegar para FichaDetailPage da ficha criada/editada.
- Após falha: toast de erro, permanecer no passo 4.

### Validações em tempo real

| Validação | Onde mostrar |
|-----------|-------------|
| Atributo base > limitador | Badge vermelho no atributo + mensagem inline |
| Pontos de atributo esgotados | Spinner desabilitado + badge "Sem pontos" |
| Nome em branco | Erro inline padrão do Angular |

---

## 8. Contrato de API — Endpoints de Ficha

| Método + Path | Componente que usa | Quando usar | Dados principais retornados |
|-------------|-------------------|------------|----------------------------|
| `GET /jogos/{id}/fichas` | FichasListPage | Ao carregar lista de fichas do jogo | `Ficha[]` |
| `GET /jogos/{id}/fichas/minhas` | Jogador Dashboard | Carregar fichas do próprio usuário | `Ficha[]` |
| `POST /jogos/{id}/fichas` | FichaFormPage step 4 | Criar ficha ao confirmar wizard | `Ficha` |
| `GET /jogos/{id}/npcs` | MestreDashboard | Listar NPCs do jogo | `Ficha[]` |
| `POST /jogos/{id}/npcs` | FichaFormPage (rota mestre) | Criar NPC ao confirmar wizard | `Ficha` |
| `GET /fichas/{id}` | FichaDetailPage, FichaFormPage | Carregar ficha para edição/visualização | `Ficha` |
| `PUT /fichas/{id}` | FichaFormPage step 4 | Atualizar identificação | `Ficha` |
| `DELETE /fichas/{id}` | FichaDetailPage (MESTRE) | Soft delete após confirmação | `void` |
| `GET /fichas/{id}/resumo` | FichaDetailPage (init + após saves) | Carregar todos os valores calculados | `FichaResumo` |
| `POST /fichas/{id}/preview` | FichaFormPage step 2 | Simular distribuição de atributos | valores calculados |
| `POST /fichas/{id}/duplicar` | FichaDetailPage (MESTRE/dono) | Duplicar após dialog de nome | `DuplicarFichaResponse` |
| `GET /fichas/{id}/vantagens` | ficha-vantagens-tab | Carregar vantagens ao abrir aba | `FichaVantagemResponse[]` |
| `POST /fichas/{id}/vantagens` | step-vantagens | Comprar vantagem (nível 1) | `FichaVantagemResponse` |
| `PUT /fichas/{id}/vantagens/{vid}` | ficha-vantagens-tab | Aumentar nível de vantagem | `FichaVantagemResponse` |
| `PUT /fichas/{id}/atributos` | step-atributos (ao sair do passo) | Salvar distribuição de atributos | `FichaAtributoResponse[]` |
| `PUT /fichas/{id}/aptidoes` | step-aptidoes | Salvar distribuição de aptidões | `FichaAptidaoResponse[]` |
| `GET /fichas/{id}/anotacoes` | ficha-anotacoes-tab | Carregar anotações ao abrir aba | `Anotacao[]` |
| `POST /fichas/{id}/anotacoes` | ficha-anotacoes-tab | Criar anotação | `Anotacao` |
| `DELETE /fichas/{id}/anotacoes/{aid}` | anotacao-card | Deletar após confirmação | `void` |

### Endpoints auxiliares necessários no form

| Endpoint | Passo que usa |
|----------|--------------|
| `GET /configuracoes/racas?jogoId=` | Passo 1 |
| `GET /configuracoes/classes?jogoId=` | Passo 1 |
| `GET /configuracoes/generos?jogoId=` | Passo 1 |
| `GET /configuracoes/indoles?jogoId=` | Passo 1 |
| `GET /configuracoes/presencas?jogoId=` | Passo 1 |
| `GET /configuracoes/atributos?jogoId=` | Passo 2 |
| `GET /configuracoes/niveis?jogoId=` | Passo 2 (limitador) |
| `GET /configuracoes/aptidoes?jogoId=` | Passo 3 |
| `GET /configuracoes/vantagens?jogoId=` | Passo 3 (vantagens disponíveis) |

---

## 9. Estado Atual

### Backend (branch feature/009)

| Feature | Status |
|---------|--------|
| Entidade `Ficha` com campos base | Implementada |
| `POST/GET/PUT/DELETE /fichas` | Implementados |
| `GET /fichas/{id}/resumo` | Implementado |
| `PUT /fichas/{id}/atributos` (batch) | Implementado |
| `PUT /fichas/{id}/aptidoes` (batch) | Implementado |
| `GET/POST/PUT /fichas/{id}/vantagens` | Implementados |
| `GET/POST/DELETE /fichas/{id}/anotacoes` | Implementados |
| `POST /fichas/{id}/duplicar` | Implementado |
| `POST /fichas/{id}/preview` | Implementado |
| `GET/POST /jogos/{id}/npcs` | Implementados |

### Frontend

| Feature | Status | Prioridade |
|---------|--------|-----------|
| `FichasApiService` | Implementado e alinhado com backend | — |
| `FichaBusinessService` (básico) | Implementado | — |
| `FichasStore` (NgRx Signals) | Implementado | — |
| `FichaFormComponent` com 10 seções | Implementado, mas **desalinhado do backend** — campos inexistentes (origem, linhagem, descricaoFisica, equipamentos, pericias, titulosRunas) e atributos hardcoded | CRITICO |
| `FichaDetailComponent` | Placeholder vazio | CRITICO |
| Wizard de 4 passos alinhado | Não implementado | CRITICO |
| `ficha-header` component | Não implementado | CRITICO |
| `ficha-stats-bar` component | Não implementado | CRITICO |
| `ficha-atributos-tab` | Não implementado | CRITICO |
| `ficha-aptidoes-tab` | Não implementado | ALTO |
| `ficha-bonus-tab` | Não implementado | ALTO |
| `ficha-vantagens-tab` | Não implementado | ALTO |
| `ficha-anotacoes-tab` com CRUD inline | Não implementado | ALTO |
| `step-identificacao` alinhado com backend | Reescrever (campos errados) | CRITICO |
| `step-atributos` dinâmico (via config) | Reescrever (atributos hardcoded) | CRITICO |
| `step-vantagens` marketplace | Não implementado | MEDIO |
| Limitador visual de atributo | Não implementado | ALTO |
| Pontos de atributo/vantagem restantes | Não implementado | ALTO |
| `FichaBusinessService` completo (resumo, atributos, vantagens) | Parcial | ALTO |
| Skeleton loading nas telas de ficha | Não implementado | MEDIO |
| Suporte a NPC (rota dedicada MESTRE) | Não implementado no frontend | ALTO |

### Dívidas técnicas críticas no frontend atual

1. `FichaFormComponent.buildForm()` usa campos inexistentes no backend (`origem`, `linhagem`, `insolitus`, `nvs`, `descricaoFisica`). Todo o submit enviaria dados incorretos — **o formulário atual não funciona com o backend real**.
2. Atributos hardcoded (FOR/DES/CON/INT/SAB/CAR) devem ser substituídos por lista dinâmica da config do jogo.
3. `FichaBusinessService.getFicha()` retorna `Observable<Ficha>` mas o `FichaFormComponent` espera um objeto com estrutura aninhada (`ficha.identificacao.origem`, etc.) que não existe no modelo real.
4. `FichaDetailComponent` é 100% placeholder — precisa ser implementado do zero.
