# Cenários de Teste — ficha-controlador

## Como usar este documento

Este documento é a fonte de verdade para a escrita de testes do projeto. Cada seção mapeia uma camada diferente:

- **Seção 1** — Jornadas de Usuário: fluxos completos narrados do ponto de vista do usuário. Use para identificar o que testar e para rastrear cobertura funcional.
- **Seção 2** — Cenários Unitários (Frontend Angular): cenários no formato GIVEN/WHEN/THEN para services e componentes Angular. Use com Vitest + `@testing-library/angular`.
- **Seção 3** — Cenários de Integração (Backend): cenários para os services e endpoints não cobertos pelos testes existentes. Use com `BaseConfiguracaoServiceIntegrationTest` como referência de estrutura.
- **Seção 4** — Matriz de Cobertura: visão consolidada de prioridade por funcionalidade.

**Convenções:**
- `P0` — crítico: quebra o produto se falhar, deve ser testado antes de qualquer merge
- `P1` — importante: cobre regra de negócio core, deve existir antes do release
- `P2` — desejável: cobre edge cases e UX, pode ser feito incrementalmente
- `(backend)` / `(frontend)` — indica onde o teste deve ser escrito

---

## 1. Jornadas de Usuário

### 1.1 MESTRE — Criar jogo novo e configurar aptidões

**Contexto:** O Mestre acabou de criar uma conta e quer configurar seu primeiro jogo de Klayrah.

#### Jornada crítica (happy path)

1. Mestre acessa `/mestre/jogos/novo`, preenche nome e descrição, clica em "Criar".
2. Sistema cria o jogo e aplica o **Template Klayrah Padrão** automaticamente (7 atributos, 24 aptidões, 12 classes, 35 níveis, 6 bônus, etc.).
3. Mestre navega até Configurações > Aptidões do jogo recém-criado.
4. Mestre vê a lista de 24 aptidões pré-configuradas.
5. Mestre clica em "Nova Aptidão", preenche nome "Intimidação", seleciona tipo "Mental", define sigla "INT2" e clica em "Criar Aptidão".
6. Aptidão aparece na tabela. Mestre clica em "Editar" na aptidão "Furtividade" e muda a descrição.
7. Mestre reordena aptidões arrastando linhas da tabela.
8. Mestre clica em "Excluir" em uma aptidão não usada em nenhuma ficha e confirma o dialog.

**Resultado esperado:** Jogo criado com todas as 13 configurações inicializadas. Aptidão "Intimidação" salva com tipo correto. Aptidão editada reflete nova descrição. Aptidão excluída some da lista (soft delete).

#### Cenários de borda

- Tentar criar aptidão com nome que já existe no mesmo jogo → sistema exibe erro "Este nome já está em uso" no campo (validação frontend via `uniqueNameValidator`).
- Tentar criar aptidão sem selecionar tipo → botão "Criar" desabilitado ou mensagem "Campo obrigatório".
- Tentar criar aptidão com sigla que já é usada por um atributo do mesmo jogo (ex: `FOR`) → backend retorna 409 Conflict; frontend exibe toast de erro.
- Tentar criar aptidão com sigla com 6+ caracteres → frontend bloqueia antes de enviar (maxLength 5).
- Tentar criar aptidão sem jogo selecionado no cabeçalho → componente exibe banner "Nenhum jogo selecionado" e oculta tabela.
- Fechar drawer sem salvar → form descartado, lista inalterada.

#### Cenários de segurança

- Usuário com role JOGADOR acessa `GET /api/v1/jogos/{jogoId}/aptidoes` → 200 OK (leitura permitida).
- Usuário com role JOGADOR faz `POST /api/v1/jogos/{jogoId}/aptidoes` → 403 Forbidden.
- Usuário não autenticado faz qualquer requisição → 401 Unauthorized.
- Mestre tenta criar aptidão em jogo que não é seu → 403 Forbidden (verificação de ownership).

---

### 1.2 MESTRE — Criar NPC e configurar seus atributos

**Contexto:** O Mestre quer criar um vilão chamado "Lorde Malthar" para uma sessão.

#### Jornada crítica (happy path)

1. Mestre acessa a página de NPCs do jogo (`/mestre/jogos/{jogoId}/npcs`).
2. Clica em "Novo NPC", preenche nome "Lorde Malthar", seleciona raça "Humano" e classe "Necromante".
3. Sistema cria NPC via `POST /api/v1/jogos/{jogoId}/npcs` com `isNpc=true` e `jogadorId=null`.
4. Sistema inicializa automaticamente todos os sub-registros (FichaAtributo, FichaBonus, FichaVida, etc.) com valores zero.
5. Mestre acessa a ficha do NPC e vai para a aba "Atributos".
6. Mestre distribui pontos: Força=20, Agilidade=12, Vigor=25, Sabedoria=18, Intuição=10, Inteligência=22, Astúcia=15.
7. Mestre clica em "Salvar" → `PUT /api/v1/fichas/{id}/atributos` recalcula valores derivados.
8. Aba "Resumo" exibe vida total, bônus (BBA, BBM, etc.) calculados pelo backend.

**Resultado esperado:** NPC criado com `isNpc=true` e sem jogadorId. Atributos salvos dentro dos limites do nível. Resumo exibe cálculos corretos.

#### Cenários de borda

- Mestre tenta setar Força=999 para NPC de nível 1 cujo limitador é 10 → backend retorna 422 com mensagem "Atributo 'Força' excede o limitador do nível atual (10)".
- Mestre tenta criar NPC com raça de outro jogo → backend retorna 403 "Raça não pertence a este jogo".
- NPC não aparece nas fichas do Jogador (`GET /api/v1/jogos/{jogoId}/fichas`) → lista exclui NPCs quando o solicitante é JOGADOR.
- NPC aparece em `GET /api/v1/jogos/{jogoId}/npcs` para MESTRE → lista inclui o NPC.
- Duplicar NPC: `POST /api/v1/fichas/{id}/duplicar` com `novoNome="Lorde Malthar Cópia"` → nova ficha criada com `isNpc=true`.

#### Cenários de segurança

- JOGADOR tenta `POST /api/v1/jogos/{jogoId}/npcs` → 403 Forbidden ("Apenas o Mestre pode criar NPCs").
- JOGADOR tenta `GET /api/v1/jogos/{jogoId}/npcs` → 403 Forbidden.
- JOGADOR tenta acessar `GET /api/v1/fichas/{npcId}` → 403 Forbidden ("NPCs só são acessíveis pelo Mestre").
- JOGADOR tenta `PUT /api/v1/fichas/{npcId}/atributos` → 403 Forbidden ("NPCs só podem ser modificados pelo Mestre").

---

### 1.3 MESTRE — Aprovar jogador no jogo

**Contexto:** Um Jogador solicitou participação no jogo. O Mestre quer aprová-lo.

#### Jornada crítica (happy path)

1. Jogador acessa `POST /api/v1/jogos/{jogoId}/participantes/solicitar` e envia pedido de participação.
2. Mestre acessa lista de participantes pendentes (`GET /api/v1/jogos/{jogoId}/participantes?status=PENDENTE`).
3. Mestre aprova o Jogador via `PUT /api/v1/jogos/{jogoId}/participantes/{id}/aprovar`.
4. Jogador agora aparece na lista com status APROVADO.
5. Jogador pode criar fichas no jogo (`POST /api/v1/jogos/{jogoId}/fichas`).

**Resultado esperado:** Status do participante muda de PENDENTE para APROVADO. Jogador recebe acesso aos endpoints de criação.

#### Cenários de borda

- Mestre tenta aprovar usuário que não é participante do jogo → 404 Not Found.
- Mestre tenta aprovar participante já APROVADO → idempotente ou retorna aviso.
- Mestre rejeita participante via `PUT .../rejeitar` → status muda para REJEITADO, Jogador não pode criar fichas.
- Jogador não aprovado tenta `POST /api/v1/jogos/{jogoId}/fichas` → 403 "você não é participante aprovado deste jogo".

#### Cenários de segurança

- JOGADOR tenta aprovar outro jogador → 403 Forbidden.
- Mestre de outro jogo tenta aprovar participante deste jogo → 403 Forbidden.

---

### 1.4 MESTRE — Editar vantagem com pré-requisitos

**Contexto:** O Mestre quer criar a vantagem "Último Sigilo" (de Renascimento) que exige "TCO nível 5".

#### Jornada crítica (happy path)

1. Mestre acessa Configurações > Vantagens e cria "TCO" (Treinamento em Combate Ofensivo) com nivelMaximo=10.
2. Mestre cria "Último Sigilo" com categoria "Renascimento" e define pré-requisito: TCO com nivelMinimo=5.
3. Mestre edita "Último Sigilo" para adicionar segundo pré-requisito: "1 Renascimento".
4. Sistema salva pré-requisitos sem ciclo (A exige B, B exige A seria rejeitado).

**Resultado esperado:** Vantagem criada com pré-requisitos encadeados. Tentativa de comprar "Último Sigilo" por personagem sem TCO nível 5 retorna erro de pré-requisito.

#### Cenários de borda

- Criar pré-requisito que forma ciclo direto (A exige A) → backend retorna 422 "Ciclo detectado nos pré-requisitos".
- Criar pré-requisito que forma ciclo indireto (A → B → A) → backend retorna 422 "Ciclo detectado".
- Deletar vantagem que é pré-requisito de outra → backend retorna 409 "Vantagem é pré-requisito de [nome]" (verificar se há proteção, se não, adicionar ao backlog).
- Editar sigla de vantagem para sigla existente em atributo → backend retorna 409 "Sigla já utilizada neste jogo".

#### Cenários de segurança

- JOGADOR tenta `POST /api/v1/jogos/{jogoId}/vantagens` → 403 Forbidden.
- JOGADOR tenta `DELETE /api/v1/jogos/{jogoId}/vantagens/{id}` → 403 Forbidden.

---

### 1.5 JOGADOR — Entrar em um jogo disponível

**Contexto:** Jogador autenticado quer ver seus jogos e entrar em um.

#### Jornada crítica (happy path)

1. Jogador acessa `/jogador/jogos` → `JogosDisponiveisComponent` carrega via `GET /api/v1/jogos`.
2. Grid exibe os jogos com role do usuário (JOGADOR ou MESTRE), número de participantes e status (ativo/inativo).
3. Jogador clica em "Entrar" em um jogo ativo → `CurrentGameService.selectGame(id)` persiste ID no `localStorage`.
4. Toast exibe "Jogo X selecionado!". Router navega para `/jogador/fichas`.
5. Em qualquer outra página, `CurrentGameService.currentGame()` retorna o jogo selecionado.

**Resultado esperado:** Jogo salvo no localStorage. Computed `currentGame` retorna objeto correto. Navegação funciona.

#### Cenários de borda

- Jogador acessa página sem nenhum jogo disponível → estado "Nenhum jogo encontrado" exibido com mensagem explicativa.
- API retorna erro de rede → estado de erro exibido com botão "Tentar novamente".
- Jogador já tem jogo selecionado na sessão anterior (localStorage com ID válido) → jogo é restaurado automaticamente ao abrir a aplicação.
- Jogo no localStorage foi deletado → `currentGame` retorna null (jogo não existe na lista); `CurrentGameService` deve lidar com graciosamente.
- Botão "Entrar" não aparece para jogo inativo (`ativo=false`) → comportamento correto por inspeção do template.
- Mestre vê botão "Gerenciar" em vez de "Entrar" → comportamento correto por inspeção do template.

#### Cenários de segurança

- Usuário não autenticado acessa `/jogador/jogos` → `authGuard` redireciona para `/login`.
- Usuário com role MESTRE acessa `/jogador/jogos` → `roleGuard` redireciona para área do Mestre (verificar configuração da rota).

---

### 1.6 JOGADOR — Criar ficha de personagem

**Contexto:** Jogador aprovado no jogo quer criar seu personagem.

#### Jornada crítica (happy path)

1. Jogador acessa `/jogador/fichas/novo` com jogo selecionado.
2. Preenche nome "Aldric", seleciona raça "Humano", classe "Guerreiro", gênero "Masculino".
3. Clica em "Criar" → `POST /api/v1/jogos/{jogoId}/fichas` com `jogadorId` do usuário atual.
4. Backend inicializa automaticamente todos os sub-registros (FichaAtributo para cada AtributoConfig, FichaBonus para cada BonusConfig, FichaVida, FichaEssencia, FichaAmeaca, FichaProspeccao para cada DadoProspeccaoConfig).
5. Backend recalcula valores derivados logo após a criação.
6. Jogador é redirecionado para `/fichas/{id}` (FichaDetailComponent).
7. Aba "Resumo" exibe vida, essência, bônus com valores corretos.

**Resultado esperado:** Ficha criada com `isNpc=false` e `jogadorId` do usuário. Sub-registros inicializados. Resumo calculado.

#### Cenários de borda

- Criar ficha sem nome → validação frontend impede submissão; backend retorna 400 se bypassado.
- Criar ficha com raça de outro jogo → backend retorna 403 "Raça não pertence a este jogo".
- Jogador tenta criar ficha com `isNpc=true` → backend retorna 403 "Apenas o Mestre pode criar NPCs".
- Jogador tenta criar ficha com `jogadorId` de outro usuário → backend ignora e usa o ID do usuário autenticado.
- Jogador não aprovado tenta criar ficha → 403 "você não é participante aprovado deste jogo".

#### Cenários de segurança

- Jogador tenta criar ficha em jogo de outro Mestre onde não é participante → 403.
- Jogador tenta criar segunda ficha com mesmo nome → verificar se há restrição de nome único por jogo+jogador (documentar comportamento atual e testar).

---

### 1.7 JOGADOR — Visualizar ficha com valores calculados

**Contexto:** Jogador quer ver o estado atual de sua ficha.

#### Jornada crítica (happy path)

1. Jogador navega para `/fichas/{id}`.
2. `FichaDetailComponent.ngOnInit()` lê `id` da rota e chama `loadFichaCompleta(id)`.
3. `forkJoin` dispara `GET /api/v1/fichas/{id}` e `GET /api/v1/fichas/{id}/resumo` em paralelo.
4. Loading skeletons são exibidos durante a requisição.
5. Após resposta, cabeçalho da ficha exibe nome, nível, raça, classe.
6. Aba "Resumo" (padrão) exibe: atributos totais (FOR=15, AGI=12, etc.), bônus calculados (BBA=9, BBM=8, etc.), vida total, essência.
7. Jogador troca para aba "Atributos" → dados carregados do `resumo.atributosTotais`.
8. Jogador troca para aba "Vantagens" → `GET /api/v1/fichas/{id}/vantagens` disparado; lista de vantagens exibida.
9. Jogador troca para aba "Anotações" → `GET /api/v1/fichas/{fichaId}/anotacoes` disparado; apenas anotações visíveis ao jogador exibidas.

**Resultado esperado:** Dados carregados e exibidos sem erro. Valores calculados pelo backend refletidos corretamente na UI.

#### Cenários de borda

- API retorna 404 (ficha deletada ou ID inválido) → estado de erro exibido com mensagem "Ficha não encontrada ou foi removida."
- API retorna erro de rede (timeout) → estado de erro com botão "Tentar novamente".
- ID na URL é texto não-numérico (`/fichas/abc`) → `ngOnInit` define `erro="ID de ficha invalido."` sem chamada HTTP.
- Jogador acessa ficha de outro jogador → backend retorna 403; frontend exibe estado de erro adequado.
- Forkjoin: ficha carrega com sucesso mas resumo falha → estado de erro parcial; verificar comportamento atual e documentar expectativa.

#### Cenários de segurança

- Jogador tenta acessar ficha de NPC → backend retorna 403; frontend exibe erro.
- Jogador não autenticado tenta acessar qualquer ficha → `authGuard` redireciona para `/login`.

---

### 1.8 JOGADOR — Comprar vantagem para o personagem

**Contexto:** Jogador tem pontos de vantagem disponíveis e quer comprar "TCO nível 1".

#### Jornada crítica (happy path)

1. Jogador está na aba "Vantagens" da ficha.
2. Lista de vantagens disponíveis carregada via `GET /api/v1/jogos/{jogoId}/vantagens` (endpoint de config).
3. Jogador seleciona "TCO" e clica em "Comprar".
4. Frontend chama `POST /api/v1/fichas/{id}/vantagens` com `vantagemConfigId`.
5. Backend verifica pré-requisitos (nenhum neste caso), calcula custo (ex: 2 pontos), cria FichaVantagem com `nivelAtual=1`.
6. Vantagem aparece na lista com nível 1 e custo pago.
7. Jogador clica em "Aumentar Nível" → `PUT /api/v1/fichas/{id}/vantagens/{vid}` → nível sobe para 2.

**Resultado esperado:** Vantagem comprada e persistida. Nível aumentado corretamente. Lista atualizada na UI.

#### Cenários de borda

- Tentar comprar vantagem que já foi comprada → backend retorna 409 "A ficha já possui esta vantagem".
- Tentar comprar vantagem com pré-requisito não atendido (ex: "Último Sigilo" sem TCO nível 5) → backend retorna 422 com lista de pré-requisitos faltantes.
- Tentar aumentar nível de vantagem já no nível máximo (ex: TCO nível 10) → backend retorna 422 "já está no nível máximo".
- Jogador tenta comprar vantagem em ficha de outro jogador → backend retorna 403.
- Vantagem com `nivelMaximo=1` — botão "Aumentar Nível" deve estar desabilitado no frontend (verificar `podeAumentarNivel` + condição de nível máximo).

#### Cenários de segurança

- Jogador tenta comprar vantagem em ficha de NPC → 403 Forbidden.
- Usuário não participante do jogo tenta comprar vantagem → 403 Forbidden.

---

### 1.9 JOGADOR — Adicionar anotação na ficha

**Contexto:** Jogador quer registrar uma nota pessoal sobre sua ficha ("Preciso comprar TCO nível 3 no próximo nível").

#### Jornada crítica (happy path)

1. Jogador está na aba "Anotações" da ficha.
2. Clica em "Nova Anotação", preenche título e conteúdo.
3. Frontend chama `POST /api/v1/fichas/{fichaId}/anotacoes` com `tipoAnotacao: "JOGADOR"`.
4. Anotação aparece na lista com data de criação.
5. Jogador clica em "Deletar" na anotação → dialog de confirmação → `DELETE /api/v1/fichas/{fichaId}/anotacoes/{id}`.
6. Anotação some da lista.

**Resultado esperado:** Anotação criada com tipo JOGADOR e autoria correta. Deletada apenas pelo seu autor.

#### Cenários de borda

- Jogador tenta criar anotação com `tipoAnotacao: "MESTRE"` → backend retorna 403 "Jogadores não podem criar anotações do tipo MESTRE".
- Mestre cria anotação com `visivelParaJogador: true` na ficha do Jogador → Jogador vê a anotação na sua lista.
- Mestre cria anotação com `visivelParaJogador: false` na ficha do Jogador → Jogador NÃO vê a anotação.
- Mestre vê TODAS as anotações (tipo MESTRE e JOGADOR) da ficha.
- Jogador tenta deletar anotação do Mestre → backend retorna 403 "você só pode deletar suas próprias anotações".
- Jogador tenta criar anotação em ficha de NPC → backend retorna 403.

#### Cenários de segurança

- Jogador A tenta deletar anotação do Jogador B na mesma ficha → 403 Forbidden.
- Anotação não pertence à ficha indicada na URL → backend retorna 403 "Esta anotação não pertence à ficha informada".

---

## 2. Cenários Unitários — Frontend Angular

> Tecnologia: Vitest + `@testing-library/angular`. Mock com `vi.fn()`. Seguir padrão Arrange-Act-Assert.

### 2.1 FichasApiService

**Arquivo sugerido:** `src/app/core/services/api/fichas-api.service.spec.ts`

#### `listFichas`

```
GIVEN: HttpClient mockado, jogoId=42, sem filtros
WHEN: listFichas(42) é chamado
THEN: dispara GET /api/v1/jogos/42/fichas sem query params
      retorna Observable<Ficha[]>
```

```
GIVEN: HttpClient mockado, jogoId=42, filtros { nome: "Aldric", classeId: 5 }
WHEN: listFichas(42, { nome: "Aldric", classeId: 5 }) é chamado
THEN: dispara GET /api/v1/jogos/42/fichas?nome=Aldric&classeId=5
```

```
GIVEN: filtros com racaId=null e nivel=undefined
WHEN: listFichas(42, { racaId: null, nivel: undefined }) é chamado
THEN: esses params NÃO aparecem na URL (params opcionais omitidos)
```

#### `createFicha`

```
GIVEN: HttpClient mockado, jogoId=42, dto com nome="Aldric"
WHEN: createFicha(42, dto) é chamado
THEN: dispara POST /api/v1/jogos/42/fichas com body=dto
      retorna Observable<Ficha>
```

#### `atualizarAtributos`

```
GIVEN: HttpClient mockado, fichaId=10, lista de AtualizarAtributoDto
WHEN: atualizarAtributos(10, dtoList) é chamado
THEN: dispara PUT /api/v1/fichas/10/atributos com body=dtoList
      retorna Observable<FichaAtributoResponse[]>
```

#### `comprarVantagem`

```
GIVEN: HttpClient mockado, fichaId=10, dto com vantagemConfigId=5
WHEN: comprarVantagem(10, { vantagemConfigId: 5 }) é chamado
THEN: dispara POST /api/v1/fichas/10/vantagens com body correto
      retorna Observable<FichaVantagemResponse>
```

#### `criarNpc`

```
GIVEN: HttpClient mockado, jogoId=42, NpcCreateDto
WHEN: criarNpc(42, dto) é chamado
THEN: dispara POST /api/v1/jogos/42/npcs (endpoint dedicado de NPC)
      NÃO dispara POST /api/v1/jogos/42/fichas
```

#### `getAnotacoes` / `criarAnotacao` / `deletarAnotacao`

```
GIVEN: fichaId=10
WHEN: getAnotacoes(10) é chamado
THEN: dispara GET /api/v1/fichas/10/anotacoes

GIVEN: fichaId=10, anotacaoId=3
WHEN: deletarAnotacao(10, 3) é chamado
THEN: dispara DELETE /api/v1/fichas/10/anotacoes/3
      retorna Observable<void>
```

---

### 2.2 FichaBusinessService

**Arquivo sugerido:** `src/app/core/services/business/ficha-business.service.spec.ts`

#### `loadFichaCompleta`

```
GIVEN: FichasApiService mockado com getFicha() e getFichaResumo() retornando mocks
       FichasStore mockado com updateFichaInState(), setCurrentFicha()
WHEN: loadFichaCompleta(10) é chamado e se subscreve
THEN: getFicha(10) e getFichaResumo(10) são chamados em paralelo (forkJoin)
      FichasStore.updateFichaInState(10, fichaRetornada) é chamado
      FichasStore.setCurrentFicha(fichaRetornada) é chamado
      Observable emite { ficha, resumo }
```

```
GIVEN: getFicha() retorna dados mas getFichaResumo() retorna erro
WHEN: loadFichaCompleta(10) é chamado e se subscreve
THEN: Observable emite erro (forkJoin propaga erro de qualquer observable)
      FichasStore.setCurrentFicha NÃO é chamado
```

#### `canEdit`

```
GIVEN: AuthService.isMestre() = true
       ficha com jogadorId=99
WHEN: canEdit(ficha) é chamado
THEN: retorna true (Mestre pode editar qualquer ficha)
```

```
GIVEN: AuthService.isMestre() = false
       AuthService.currentUser() = { id: "42", ... }
       ficha com jogadorId=42
WHEN: canEdit(ficha) é chamado
THEN: retorna true (Jogador pode editar sua própria ficha)
```

```
GIVEN: AuthService.isMestre() = false
       AuthService.currentUser() = { id: "42", ... }
       ficha com jogadorId=99 (ficha de outro jogador)
WHEN: canEdit(ficha) é chamado
THEN: retorna false
```

```
GIVEN: AuthService.isMestre() = false
       AuthService.currentUser() = null
WHEN: canEdit(ficha) é chamado
THEN: retorna false (usuário sem id não pode editar)
```

#### `minhasFichas` (computed)

```
GIVEN: AuthService.isMestre() = true
       FichasStore com 5 fichas (3 de um jogador, 2 de outro)
WHEN: minhasFichas() é lido
THEN: retorna todas as 5 fichas (Mestre vê tudo)
```

```
GIVEN: AuthService.isMestre() = false, currentUser().id = "7"
       FichasStore com fichas: [jogadorId=7, jogadorId=7, jogadorId=99]
WHEN: minhasFichas() é lido
THEN: retorna apenas as 2 fichas com jogadorId=7
```

#### `deleteFicha`

```
GIVEN: FichasApiService.deleteFicha mockado para retornar void
       FichasStore com currentFicha().id = 10
WHEN: deleteFicha(10) é chamado e se subscreve
THEN: FichasStore.removeFicha(10) é chamado
      FichasStore.clearCurrentFicha() é chamado (pois era a ficha atual)
```

```
GIVEN: FichasStore com currentFicha().id = 99 (diferente)
WHEN: deleteFicha(10) é chamado
THEN: FichasStore.removeFicha(10) é chamado
      FichasStore.clearCurrentFicha() NÃO é chamado
```

---

### 2.3 CurrentGameService

**Arquivo sugerido:** `src/app/core/services/current-game.service.spec.ts`

#### `selectGame`

```
GIVEN: localStorage vazio, JogoBusinessService com jogos carregados
WHEN: selectGame(42) é chamado
THEN: currentGameId() retorna 42
      localStorage.getItem("currentGameId") retorna "42" (persistência)
```

#### `currentGame` (computed)

```
GIVEN: currentGameId = 42, JogoBusinessService.jogos() inclui { id: 42, nome: "Eldoria" }
WHEN: currentGame() é lido
THEN: retorna o objeto do jogo com id=42
```

```
GIVEN: currentGameId = 42, mas JogoBusinessService.jogos() não inclui jogo com id=42
WHEN: currentGame() é lido
THEN: retorna null (jogo não encontrado na lista)
```

#### `clearGame`

```
GIVEN: currentGameId = 42
WHEN: clearGame() é chamado
THEN: currentGameId() retorna null
      localStorage.getItem("currentGameId") retorna null
```

#### Auto-seleção na inicialização

```
GIVEN: localStorage vazio, JogoBusinessService carrega [{ id:1, ativo:true }, { id:2, ativo:true }]
WHEN: CurrentGameService é instanciado
THEN: após effect rodar, currentGameId() é 1 (primeiro jogo ativo)
```

```
GIVEN: localStorage com "currentGameId" = "5", jogos disponíveis incluem id=5
WHEN: CurrentGameService é instanciado
THEN: currentGameId() é 5 (restaurado do localStorage)
      auto-seleção NÃO sobrescreve o valor existente
```

---

### 2.4 AuthService

**Arquivo sugerido:** `src/app/services/auth.service.spec.ts`

#### `getUserInfo`

```
GIVEN: HttpClient mockado retorna { id: "1", name: "João", email: "j@j.com", role: "JOGADOR" }
WHEN: getUserInfo() é chamado e se subscreve
THEN: GET /api/auth/me é disparado com withCredentials: true
      currentUser() retorna o objeto do usuário
      isAuthenticated() retorna true
      isJogador() retorna true
      isMestre() retorna false
```

```
GIVEN: HttpClient mockado retorna { role: "MESTRE" }
WHEN: getUserInfo() é chamado e se subscreve
THEN: isMestre() retorna true
      isJogador() retorna false
```

#### `logout`

```
GIVEN: currentUser() não-nulo, HttpClient mockado para POST /api/auth/logout
WHEN: logout() é chamado e se subscreve
THEN: POST /api/auth/logout é disparado
      currentUser() retorna null após a resposta
      isAuthenticated() retorna false
```

#### `login` (redirecionamento)

```
GIVEN: window.location mockado
WHEN: login() é chamado
THEN: URL atual é salva em sessionStorage ("REDIRECT_URL")
      window.location.href aponta para backendUrl + "/oauth2/authorization/google"
      NÃO usa location do proxy (/oauth2) — usa URL absoluta do backendUrl
```

---

### 2.5 FichaDetailComponent

**Arquivo sugerido:** `src/app/features/jogador/pages/ficha-detail/ficha-detail.component.spec.ts`

#### Carregamento inicial com sucesso

```
GIVEN: ActivatedRoute com params.id = "10"
       FichaBusinessService.loadFichaCompleta mockado retornando { ficha, resumo }
WHEN: componente é criado
THEN: loading() inicia como true
      loadFichaCompleta(10) é chamado
      após emissão: loading() = false, ficha() e resumo() não-nulos
      template exibe conteúdo principal (não skeleton, não erro)
      abaAtiva() = 0 (aba Resumo por padrão)
```

#### Erro 404

```
GIVEN: ActivatedRoute com params.id = "10"
       FichaBusinessService.loadFichaCompleta emite erro com status=404
WHEN: componente inicializa
THEN: loading() = false
      erro() = "Ficha nao encontrada ou foi removida."
      template exibe estado de erro com ícone e mensagem
      botão "Tentar novamente" é exibido
```

#### Erro de rede (status != 404)

```
GIVEN: FichaBusinessService.loadFichaCompleta emite erro com status=503
WHEN: componente inicializa
THEN: erro() = "Nao foi possivel carregar a ficha. Verifique sua conexao."
```

#### ID inválido na URL

```
GIVEN: ActivatedRoute com params.id = "abc"
WHEN: componente inicializa
THEN: fichaId() = null
      erro() = "ID de ficha invalido."
      loadFichaCompleta NÃO é chamado
      loading() = false
```

#### Troca de aba — lazy loading de vantagens

```
GIVEN: componente carregado com ficha, vantagens() = []
       FichaBusinessService.loadVantagens mockado retornando [vantagem1, vantagem2]
WHEN: usuário troca para aba 3 (Vantagens)
THEN: loadVantagens(fichaId) é chamado
      vantagens() passa a ter 2 itens
      loadingVantagens() vai true e depois false
```

```
GIVEN: vantagens() já tem itens (aba visitada antes)
WHEN: usuário volta para aba 3 novamente
THEN: loadVantagens NÃO é chamado novamente (cache local)
```

#### Dialog de duplicar

```
GIVEN: componente carregado, showDuplicarDialog() = false
WHEN: botão "Duplicar" é clicado no cabeçalho
THEN: showDuplicarDialog() = true
      dialog é exibido
      botão "Duplicar" dentro do dialog está desabilitado (novoNome vazio)
```

```
GIVEN: novoNomeDuplicar() = "Aldric Cópia"
       FichaBusinessService.duplicarFicha mockado retornando { fichaId: 11, nome: "Aldric Cópia" }
WHEN: botão "Duplicar" dentro do dialog é clicado
THEN: duplicando() = true durante a operação
      após sucesso: toast "Ficha Aldric Cópia criada com sucesso!"
      router navega para /fichas/11
      showDuplicarDialog() = false, novoNomeDuplicar() = ""
```

#### Permissões de UI

```
GIVEN: FichaBusinessService.canEdit(ficha) = false
WHEN: componente renderiza
THEN: podeEditar() = false
      botão "Editar" não aparece no header (ou está desabilitado)
```

```
GIVEN: AuthService.isMestre() = false
WHEN: componente renderiza
THEN: podeDeletar() = false
      botão "Deletar" não aparece
```

---

### 2.6 JogosDisponiveisComponent

**Arquivo sugerido:** `src/app/features/jogador/pages/jogos-disponiveis/jogos-disponiveis.component.spec.ts`

#### Estado de carregamento

```
GIVEN: JogosApiService.listJogos mockado com delay (não resolvido ainda)
WHEN: componente é criado e ngOnInit() executa
THEN: loading() = true
      template exibe 6 skeletons de card (loop `@for (_ of [1,2,3,4,5,6]`)
```

#### Carregamento com sucesso

```
GIVEN: JogosApiService.listJogos retorna [jogo1 (JOGADOR, ativo), jogo2 (MESTRE)]
WHEN: componente carrega
THEN: loading() = false, erro() = null
      2 cards exibidos
      jogo1 mostra botão "Entrar"
      jogo2 mostra botão "Gerenciar"
```

#### Estado vazio

```
GIVEN: JogosApiService.listJogos retorna []
WHEN: componente carrega
THEN: empty state exibido com ícone pi-search
      mensagem "Nenhum jogo encontrado"
      nenhum card de jogo
```

#### Estado de erro

```
GIVEN: JogosApiService.listJogos retorna erro de rede
WHEN: componente carrega
THEN: erro() = "Não foi possível carregar os jogos. Verifique sua conexão."
      p-message de severity="error" exibido
      botão "Tentar novamente" exibido
```

#### Selecionar jogo

```
GIVEN: jogo { id: 5, nome: "Queda de Eldoria", meuRole: "JOGADOR", ativo: true } na lista
       CurrentGameService.selectGame mockado
       Router mockado
WHEN: botão "Entrar" do jogo é clicado
THEN: CurrentGameService.selectGame(5) é chamado
      toast "Jogo 'Queda de Eldoria' selecionado!" exibido
      router.navigate(["/jogador/fichas"]) é chamado
```

#### Jogo já selecionado

```
GIVEN: CurrentGameService.currentGame() retorna jogo com id=5
       lista inclui jogo com id=5
WHEN: componente renderiza
THEN: card do jogo id=5 exibe tag "Jogo atual" (severity="success")
      botão "Entrar" não aparece para este jogo
```

#### Botão "Atualizar"

```
GIVEN: componente já carregado
WHEN: botão "Atualizar" é clicado
THEN: loading() = true
      listJogos() é chamado novamente
```

---

### 2.7 AptidoesConfigComponent

**Arquivo sugerido:** `src/app/features/mestre/pages/config/configs/aptidoes-config/aptidoes-config.component.spec.ts`

#### Sem jogo selecionado

```
GIVEN: AptidaoConfigService.currentGameId() = null
WHEN: componente renderiza
THEN: banner "Nenhum jogo selecionado" exibido
      tabela de aptidões NÃO exibida (ou ocultada via @if)
```

#### Carregamento inicial com jogo

```
GIVEN: AptidaoConfigService.currentGameId() = 42
       AptidaoConfigService.loadItems() retorna 24 aptidões
       ConfigApiService.listTiposAptidao(42) retorna [{ id:1, nome:"Física" }, { id:2, nome:"Mental" }]
WHEN: ngOnInit() executa
THEN: items() tem 24 aptidões
      tiposAptidao() tem 2 itens
      tabela exibe 24 linhas (com paginação: 15 por página)
```

#### Filtro de busca

```
GIVEN: 24 aptidões carregadas
WHEN: searchQuery recebe "furt"
THEN: filteredItems() retorna apenas aptidões cujo nome ou descrição contenha "furt" (case-insensitive)
      tabela exibe apenas as aptidões filtradas
```

#### Validação de nome único ao abrir drawer para nova aptidão

```
GIVEN: items() inclui aptidão com nome="Furtividade"
WHEN: openDrawer() é chamado (modo criação)
THEN: form tem validator uniqueNameValidator com a lista atual
      form.get("nome").setValue("Furtividade")
      form.get("nome").markAsTouched()
      form.get("nome").errors contem { uniqueName: true }
      template exibe "Este nome já está em uso"
```

#### Validação de nome único ao abrir drawer para edição (não conflita consigo mesmo)

```
GIVEN: items() inclui aptidão id=5 nome="Furtividade"
WHEN: openDrawer({ id: 5, nome: "Furtividade", ... }) é chamado (modo edição)
THEN: uniqueNameValidator é inicializado com currentId=5
      form.get("nome").setValue("Furtividade") → sem erro de uniqueName
      form.get("nome").setValue("Acrobacia") → sem erro (nome diferente, não existe)
      form.get("nome").setValue("Diplomacia") → erro uniqueName se "Diplomacia" existe em outro id
```

#### Save (criação)

```
GIVEN: form preenchido válido (nome="Intimidação", tipoAptidaoId=2, ordemExibicao=25)
       AptidaoConfigService.createItem mockado retornando nova aptidão
WHEN: save() é chamado
THEN: loading() = true
      AptidaoConfigService.createItem(formValue) é chamado
      após sucesso: toast "Aptidão criada com sucesso"
      closeDrawer() é chamado
      loadData() é chamado (recarrega lista)
      loading() = false
```

#### Save com form inválido

```
GIVEN: form inválido (nome vazio)
WHEN: save() é chamado
THEN: AptidaoConfigService.createItem NÃO é chamado
      form fica visível com erros de validação
```

#### confirmDelete

```
GIVEN: ConfirmationService mockado
WHEN: confirmDelete(7) é chamado
THEN: confirmationService.confirm() é chamado com mensagem de confirmação
      ao aceitar: AptidaoConfigService.delete(7) é chamado
```

---

## 3. Cenários de Integração — Backend

> Tecnologia: Spring Boot Test + H2 + `@ActiveProfiles("test")`. Seguir padrão de `BaseConfiguracaoServiceIntegrationTest`.

### 3.1 GET /api/v1/jogos/{jogoId}/fichas

**Arquivo sugerido:** `FichaServiceIntegrationTest.java`

```
DADO: jogo criado com Mestre A e Jogador B (aprovado) e Jogador C (aprovado)
      fichaB1 e fichaB2 pertencem ao Jogador B
      fichaC1 pertence ao Jogador C
      npc1 é NPC do jogo
QUANDO: Mestre A chama listarComFiltros(jogoId, null, null, null, null)
ENTÃO: retorna [fichaB1, fichaB2, fichaC1, npc1] (todas, incluindo NPCs)

QUANDO: Jogador B chama listarComFiltros(jogoId, null, null, null, null)
ENTÃO: retorna [fichaB1, fichaB2] (apenas as próprias, sem NPC, sem fichas de outros)

QUANDO: Jogador B chama listarComFiltros(jogoId, null, null, null, null)
ENTÃO: NPC NÃO está na lista do Jogador B
```

```
DADO: fichaB1.nome = "Aldric", fichaB2.nome = "Brenna"
QUANDO: Mestre A chama listarComFiltros(jogoId, "Ald", null, null, null)
ENTÃO: retorna apenas [fichaB1] (filtro de nome, case-insensitive)
```

```
DADO: fichaB1.classe = Guerreiro (classeId=10), fichaB2.classe = Mago (classeId=11)
QUANDO: Mestre A chama listarComFiltros(jogoId, null, 10L, null, null)
ENTÃO: retorna apenas [fichaB1]
```

```
DADO: jogo ativo, Jogador B não é participante aprovado
QUANDO: Jogador B tenta chamar listarComFiltros(jogoId, ...)
ENTÃO: lança ForbiddenException
```

---

### 3.2 POST /api/v1/jogos/{jogoId}/fichas

```
DADO: Jogador aprovado no jogo, jogo tem AtributoConfig, BonusConfig, MembroCorpoConfig, DadoProspeccaoConfig configurados
QUANDO: Jogador chama criar(CreateFichaRequest com nome="Aldric", isNpc=false)
ENTÃO: ficha criada com isNpc=false e jogadorId=userId do autenticado
       fichaAtributos inicializados (um para cada AtributoConfig do jogo)
       fichaBonus inicializados (um para cada BonusConfig do jogo)
       fichaVida criada com vidaTotal calculado
       fichaEssencia criada
       fichaAmeaca criada
       fichaProspeccao criada (um para cada DadoProspeccaoConfig)
       log "Ficha 'Aldric' criada com sucesso" emitido
```

```
DADO: Jogador aprovado
QUANDO: Jogador chama criar com isNpc=true
ENTÃO: lança ForbiddenException("Apenas o Mestre pode criar NPCs.")
```

```
DADO: Jogador aprovado, racaId aponta para raça de OUTRO jogo
QUANDO: criar() é chamado
ENTÃO: lança ForbiddenException("Raça não pertence a este jogo.")
```

```
DADO: Jogador NÃO aprovado (status PENDENTE)
QUANDO: criar() é chamado
ENTÃO: lança ForbiddenException("você não é participante aprovado deste jogo")
```

---

### 3.3 PUT /api/v1/fichas/{id}/atributos

```
DADO: ficha de Jogador B criada, nível 1 com limitador=10
      AtributoConfig FOR com valorMaximo=100, valorMinimo=1
QUANDO: Jogador B atualiza atributos com FOR.base=5 (dentro do limite)
ENTÃO: fichaAtributo.base = 5, total calculado corretamente
       retorna lista de FichaAtributoResponse com novos valores
```

```
DADO: nível 1 com limitador=10
QUANDO: Jogador B tenta atualizar FOR.base=15 (acima do limitador)
ENTÃO: lança ValidationException("Atributo 'Força' excede o limitador do nível atual")
       nenhuma alteração persistida (transação revertida)
```

```
DADO: AtributoConfig com valorMinimo=1
QUANDO: Jogador B tenta atualizar FOR.base=0 (abaixo do mínimo)
ENTÃO: lança ValidationException ou @Valid captura antes (verificar implementação atual)
```

```
DADO: fichaC pertence ao Jogador C
QUANDO: Jogador B (diferente) tenta atualizar atributos de fichaC
ENTÃO: lança ForbiddenException
```

```
DADO: fichaC pertence ao Jogador C
QUANDO: Mestre tenta atualizar atributos de fichaC
ENTÃO: atualização realizada com sucesso (Mestre pode editar qualquer ficha)
```

---

### 3.4 GET /api/v1/fichas/{id}/resumo

```
DADO: ficha criada com FOR=15, AGI=12, VIG=18
      BonusConfig BBA com formula="FLOOR((FOR+AGI)/3)"
      AtributoConfig VIG com formulaImpeto="TOTAL*3" (para RD)
QUANDO: FichaResumoService.getResumo(fichaId) é chamado
ENTÃO: atributosTotais["FOR"] = 15
       atributosTotais["AGI"] = 12
       atributosTotais["VIG"] = 18
       bonusTotais["B.B.A"] = FLOOR((15+12)/3) = 9
       vidaTotal = VIG (18) + nivel + VT + renascimentos (valores configurados)
```

```
DADO: ficha com renascimento ativado
QUANDO: getResumo() é chamado
ENTÃO: ameaca inclui componente de renascimentos
```

```
DADO: fichaC pertence ao Jogador C
QUANDO: Jogador B chama getResumo(fichaC.id)
ENTÃO: lança ForbiddenException
```

```
DADO: fichaC pertence ao Jogador C
QUANDO: Mestre chama getResumo(fichaC.id)
ENTÃO: retorna resumo completo da ficha
```

---

### 3.5 POST /api/v1/fichas/{id}/vantagens

```
DADO: ficha criada, VantagemConfig "TCO" sem pré-requisitos, formulaCusto="CUSTO_BASE * NIVEL"
QUANDO: FichaVantagemService.comprar(fichaId, tcoId) é chamado
ENTÃO: FichaVantagem criada com nivelAtual=1, custoPago calculado pela fórmula
       retorna FichaVantagem com id não-nulo
```

```
DADO: ficha já tem TCO comprada
QUANDO: comprar(fichaId, tcoId) é chamado novamente
ENTÃO: lança ConflictException("A ficha já possui esta vantagem")
```

```
DADO: VantagemConfig "Último Sigilo" com pré-requisito "TCO nível 5"
      ficha SEM TCO comprada
QUANDO: comprar(fichaId, ultimoSigiloId) é chamado
ENTÃO: lança ValidationException("Pré-requisitos não atendidos para comprar 'Último Sigilo'")
       mensagem menciona "'TCO' no nível 5"
```

```
DADO: ficha tem TCO com nivelAtual=3 (não atende nível mínimo de 5)
QUANDO: comprar(fichaId, ultimoSigiloId) é chamado
ENTÃO: lança ValidationException (pré-requisito com nível insuficiente)
```

```
DADO: ficha tem TCO com nivelAtual=5 (atende)
QUANDO: comprar(fichaId, ultimoSigiloId) é chamado
ENTÃO: vantagem comprada com sucesso
```

```
DADO: FichaVantagem de TCO com nivelAtual=10, VantagemConfig.nivelMaximo=10
QUANDO: FichaVantagemService.aumentarNivel(fichaId, fichaVantagemId) é chamado
ENTÃO: lança ValidationException("já está no nível máximo (10)")
```

---

### 3.6 Anotações: visibilidade MESTRE vs JOGADOR

```
DADO: ficha de Jogador B
      anotacaoJogador: tipo=JOGADOR, autor=B, visivelParaJogador=false
      anotacaoMestreVisivel: tipo=MESTRE, autor=Mestre, visivelParaJogador=true
      anotacaoMestreOculta: tipo=MESTRE, autor=Mestre, visivelParaJogador=false
QUANDO: FichaAnotacaoService.listar(fichaId) chamado pelo Mestre
ENTÃO: retorna [anotacaoJogador, anotacaoMestreVisivel, anotacaoMestreOculta] (todas as 3)
```

```
QUANDO: listar(fichaId) chamado pelo Jogador B
ENTÃO: retorna [anotacaoJogador, anotacaoMestreVisivel] (própria + Mestre visíveis)
       anotacaoMestreOculta NÃO está na lista
```

```
DADO: Jogador B é autor, autenticado como Jogador B
QUANDO: criar(fichaId, { tipoAnotacao: "MESTRE", ... }, jogadorBId)
ENTÃO: lança ForbiddenException("Jogadores não podem criar anotações do tipo MESTRE")
```

```
DADO: anotacao pertence ao Jogador B (autorId=B)
      Jogador C tenta deletar
QUANDO: deletar(fichaId, anotacaoId) chamado por Jogador C
ENTÃO: lança ForbiddenException("você só pode deletar suas próprias anotações")
```

```
DADO: anotacao pertence ao Jogador B
QUANDO: deletar(fichaId, anotacaoId) chamado pelo Mestre
ENTÃO: anotação soft-deletada com sucesso
```

```
DADO: anotacao pertence à ficha A, fichaId passado é da ficha B
QUANDO: deletar(fichaB.id, anotacaoA.id) chamado
ENTÃO: lança ForbiddenException("Esta anotação não pertence à ficha informada")
```

---

### 3.7 Duplicação de Ficha

```
DADO: fichaOriginal com atributos, aptidões e vantagens configuradas
QUANDO: FichaService.duplicar(fichaOriginal.id, "Cópia de Aldric", false) chamado pelo Mestre
ENTÃO: nova ficha criada com nome="Cópia de Aldric"
       atributos copiados com mesmos valores
       vantagens copiadas com mesmos níveis
       jogadorId da cópia é null (manterJogador=false)
       fichaOriginal NÃO é alterada
```

```
DADO: fichaOriginal com jogadorId=42
QUANDO: duplicar(fichaOriginal.id, "Cópia", true) chamado
ENTÃO: nova ficha tem jogadorId=42 (manterJogador=true)
```

```
DADO: fichaB pertence ao Jogador B, Jogador C tenta duplicar
QUANDO: duplicar(fichaB.id, ...) chamado pelo Jogador C
ENTÃO: lança ForbiddenException
```

---

## 4. Matriz de Cobertura Mínima

| Funcionalidade | Tipo de Teste Necessário | Onde | Prioridade |
|---|---|---|---|
| Criar Jogo + Template Klayrah Padrão (13 configs inicializadas) | Integração — `JogoServiceIntegrationTest` | backend | P0 |
| CRUD de AtributoConfig, AptidaoConfig, BonusConfig, VantagemConfig | Integração — `BaseConfiguracaoServiceIntegrationTest` | backend | P0 (já coberto) |
| Validação de sigla única cross-entity | Integração — `SiglaValidationServiceIntegrationTest` | backend | P0 (já coberto) |
| Criar Ficha (Jogador) com inicialização de sub-registros | Integração — `FichaServiceIntegrationTest` | backend | P0 |
| Criar NPC pelo Mestre | Integração — `NpcFichaMestreIntegrationTest` | backend | P0 (já coberto) |
| Regra: Jogador NÃO pode criar NPC | Integração — `FichaServiceIntegrationTest` | backend | P0 |
| Regra: Jogador só vê próprias fichas (não NPCs) | Integração — `FichaServiceIntegrationTest` | backend | P0 |
| PUT /fichas/{id}/atributos — dentro do limitador | Integração — `FichaAtributoIntegrationTest` | backend | P0 |
| PUT /fichas/{id}/atributos — acima do limitador (422) | Integração — `FichaAtributoIntegrationTest` | backend | P0 |
| GET /fichas/{id}/resumo — cálculos corretos (BBA, vida) | Integração — `FichaResumoServiceIntegrationTest` | backend | P0 |
| POST /fichas/{id}/vantagens — compra simples | Integração — `FichaVantagemIntegrationTest` | backend | P0 |
| POST /fichas/{id}/vantagens — pré-requisito não atendido | Integração — `FichaVantagemIntegrationTest` | backend | P0 |
| POST /fichas/{id}/vantagens — vantagem já comprada (409) | Integração — `FichaVantagemIntegrationTest` | backend | P0 |
| PUT /fichas/{id}/vantagens/{vid} — nível máximo (422) | Integração — `FichaVantagemIntegrationTest` | backend | P0 |
| Anotações: visibilidade Mestre vê tudo | Integração — `FichaAnotacaoIntegrationTest` | backend | P0 |
| Anotações: Jogador só vê próprias + Mestre visível | Integração — `FichaAnotacaoIntegrationTest` | backend | P0 |
| Anotações: Jogador não pode criar tipo MESTRE | Integração — `FichaAnotacaoIntegrationTest` | backend | P0 |
| `FichasApiService` — URLs e params corretos | Unitário | frontend | P0 |
| `AuthService` — currentUser, isMestre, isJogador | Unitário | frontend | P0 |
| `FichaBusinessService.canEdit` — todos os casos | Unitário | frontend | P0 |
| `FichaDetailComponent` — loading, erro 404, erro rede | Unitário (componente) | frontend | P0 |
| `JogosDisponiveisComponent` — empty state, erro, seleção | Unitário (componente) | frontend | P0 |
| `CurrentGameService` — selectGame, persistência, auto-seleção | Unitário | frontend | P1 |
| `FichaBusinessService.loadFichaCompleta` — forkJoin, erro parcial | Unitário | frontend | P1 |
| `FichaBusinessService.minhasFichas` — filtro por userId | Unitário | frontend | P1 |
| `FichaDetailComponent` — lazy loading por aba (vantagens, anotações) | Unitário (componente) | frontend | P1 |
| `FichaDetailComponent` — dialog duplicar (fluxo completo) | Unitário (componente) | frontend | P1 |
| `AptidoesConfigComponent` — CRUD completo, validações | Unitário (componente) | frontend | P1 |
| `AptidoesConfigComponent` — uniqueNameValidator (edição vs criação) | Unitário | frontend | P1 |
| Aprovação de participante pelo Mestre | Integração — `JogoParticipanteServiceIntegrationTest` | backend | P1 |
| GET /fichas com filtros (nome, classeId, racaId) | Integração — `FichaServiceIntegrationTest` | backend | P1 |
| Duplicação de ficha — copia atributos e vantagens | Integração — `JogoDuplicacaoServiceIntegrationTest` | backend | P1 (já parcialmente coberto) |
| `AuthService.login` — redirecionamento OAuth2 com URL absoluta | Unitário | frontend | P1 |
| Pré-requisito de vantagem com ciclo (DFS) | Integração — `VantagemPreRequisitoIntegrationTest` | backend | P1 (já coberto) |
| VantagemConfig com pré-requisito nível parcial (nível insuficiente) | Integração — `FichaVantagemIntegrationTest` | backend | P1 |
| FichaResumoService — acesso negado Jogador a ficha alheia | Integração — `FichaResumoServiceIntegrationTest` | backend | P1 |
| Anotações: deletar pela pessoa errada (403) | Integração — `FichaAnotacaoIntegrationTest` | backend | P1 |
| Anotações: anotação não pertence à ficha (403) | Integração — `FichaAnotacaoIntegrationTest` | backend | P1 |
| `FichasApiService` — criarNpc usa endpoint /npcs (não /fichas) | Unitário | frontend | P1 |
| `CurrentGameService` — clearGame remove localStorage | Unitário | frontend | P2 |
| `FichaDetailComponent` — permissões podeEditar/podeDeletar/podeDuplicar | Unitário (componente) | frontend | P2 |
| `JogosDisponiveisComponent` — jogo atual marcado com tag "Jogo atual" | Unitário (componente) | frontend | P2 |
| `JogosDisponiveisComponent` — botão "Gerenciar" para role MESTRE | Unitário (componente) | frontend | P2 |
| POST /fichas/{id}/vantagens — Jogador tenta comprar vantagem de NPC (403) | Integração | backend | P2 |
| GET /fichas/{id}/resumo — Mestre pode ver resumo de qualquer ficha | Integração | backend | P2 |
| `AptidoesConfigComponent` — sem jogo selecionado exibe banner | Unitário (componente) | frontend | P2 |
| `AptidoesConfigComponent` — reordenação chama handleReorder | Unitário (componente) | frontend | P2 |
| IdleService — logout automático após 30min | Unitário | frontend | P2 |
| Rate limiting — 100 req/min geral | Teste E2E / carga | backend | P2 |

---

*Última atualização: Abril 2026*
*Baseado na spec 009 (branch `feature/009-npc-fichas-mestre`).*
