# UX-FICHAS-AUDITORIA — Auditoria de Experiencia de Uso das Fichas

> Produzido por: Business Analyst/PO | 2026-04-03
> Baseado em: specs 006, 009-ext, 012; designs MODO-SESSAO.md, FICHA-DETAIL-DESIGN.md, LEVEL-UP.md, PROSPECCAO-SESSAO.md, NPC-VISIBILITY.md, WIZARD-CRIACAO-FICHA.md
> Auditoria de codigo: FichaDetailComponent, FichaHeaderComponent, FichaResumoTabComponent, FichaVantagensTabComponent, NpcsComponent

---

## 1. Jornada do Jogador — Modo Sessao

### Cenario tipico: sessao ativa, combate em andamento

**Passo 1: Jogador abre a ficha**
- Acessa `/fichas/:id` (rota atual)
- O `FichaDetailComponent` carrega ficha + resumo em paralelo via `FichaBusinessService.loadFichaCompleta()`
- O `FichaHeaderComponent` exibe nome, nivel, vida total e essencia total
- FRICAO IDENTIFICADA: a barra de vida exibe `[value]="100"` hardcoded — sempre cheia, independente do `vidaAtual` real. O mesmo para essencia. O header mostra apenas os totais calculados, nao o estado atual de combate.
- FRICAO IDENTIFICADA: o `FichaResumo` nao possui `vidaAtual` nem `essenciaAtual` — os campos existem no backend em `FichaVida.vidaAtual` e `FichaEssencia.essenciaAtual`, mas o `FichaResumoResponse` nao os expoe no frontend (modelo TypeScript `FichaResumo` em `ficha.model.ts` nao tem esses campos).

**Passo 2: Jogador precisa subtrair vida no combate**
- NAO HA BOTAO para subtrair vida na tela atual
- O design `MODO-SESSAO.md` especifica `FichaStatsCombateComponent` com botoes de acao rapida, mas esse componente nao existe no codigo
- O endpoint `/api/v1/fichas/{id}/vida` existe no backend (implementado na Spec 009), mas nao ha chamada no frontend
- LACUNA CRITICA: jogador depende de anotacoes fisicas ate o componente ser implementado

**Passo 3: Jogador quer gastar essencia**
- Mesmo problema: nenhum controle de essencia na UI atual
- O design especifica botoes `-1 Essencia`, `-5 Essencia`, `+1` — nao existem
- O endpoint `POST /fichas/{id}/essencia/gastar` referenciado no `MODO-SESSAO.md` (secao 12) nao esta confirmado no backend — a Spec 009-ext menciona que o endpoint de vida ja inclui `essenciaAtual` via `PUT /fichas/{id}/vida`, mas o design de modo sessao descreve um endpoint semantico separado `/essencia/gastar` que provavelmente nao existe
- INCONSISTENCIA DE SPEC: o design MODO-SESSAO.md cita `POST /fichas/{id}/essencia/gastar`, mas a Spec 009-ext diz que o frontend deve usar o endpoint existente `PUT /fichas/{id}/vida` passando `essenciaAtual`

**Passo 4: Jogador quer usar dado de prospeccao**
- A aba "Resumo" existe, mas `FichaResumoTabComponent` nao exibe dados de prospeccao
- O design `PROSPECCAO-SESSAO.md` especifica `ProspeccaoJogadorComponent` na aba Resumo — nao existe no codigo
- Para usar um dado: o jogador precisaria de um botao "Usar" que chame `POST /fichas/{id}/prospeccao/usar` — endpoint nao confirmado como implementado

**Passo 5: Jogador quer verificar BBA, RD, Impeto antes de atacar**
- A aba "Resumo" (`FichaResumoTabComponent`) exibe `bonusTotais` como grid de cards — isso funciona
- Mas em modo sessao, esses dados ficam abaixo do fold, exigindo scroll ou troca de aba
- O `FichaHeaderComponent` nao exibe dados de combate (BBA, RD, Impeto) — apenas vida/essencia/ameaca

**Passo 6: Modo sessao**
- O `p-toggleButton` de "Modo Sessao" especificado no design nao existe no `FichaHeaderComponent`
- A query param `?mode=sessao` nao e tratada pelo `FichaDetailComponent`
- Sem modo sessao, as abas nao se reorganizam com "Combate" como primeira aba
- Persistencia no `localStorage` nao implementada

### Pontos de friccao criticos (Jogador — Modo Sessao)

| Passo | Friccao | Severidade |
|-------|---------|------------|
| Visualizar vida atual | Barra sempre 100% — nao reflete estado real | BLOQUEADOR |
| Subtrair vida | Nenhum controle na UI | BLOQUEADOR |
| Gastar essencia | Nenhum controle na UI | BLOQUEADOR |
| Usar prospeccao | Bloco de prospeccao ausente na aba Resumo | ALTO |
| Ver BBA/Impeto acima do fold | Exige scroll ou troca de aba | MEDIO |
| Ativar modo sessao | Botao nao existe | MEDIO |

---

## 2. Jornada do Mestre — Modo Sessao

### Cenario tipico: Mestre gerencia sessao com 3 jogadores

**Passo 1: Mestre quer ver status de todos os jogadores de uma vez**
- Nao existe rota `/jogos/:jogoId/sessao` no frontend
- O `PainelSessaoComponent` especificado em `MODO-SESSAO.md` nao existe
- O Mestre precisa abrir cada ficha individualmente para ver status de vida
- LACUNA TOTAL: o painel centralizado de sessao nao esta implementado nem planejado em tasks existentes

**Passo 2: Mestre quer conceder XP apos sessao**
- O `NpcsComponent` exibe NPCs mas nao ha tela de fichas de jogadores para o Mestre
- O Spec 012 identifica como gap: nao ha endpoint dedicado de concessao de XP acessivel na UI do Mestre
- `FichaDetailComponent` existe mas esta na rota `/jogador/fichas/:id` — o Mestre e redirecionado para `/jogador/fichas/:id` ao clicar em "Ver Ficha" no `NpcsComponent` (linha 432: `this.router.navigate(['/jogador/fichas', fichaId])`) — rota potencialmente errada para o Mestre

**Passo 3: Mestre quer resetar vida de todos os jogadores no inicio da proxima sessao**
- O endpoint `POST /api/v1/fichas/{id}/resetar-estado` foi especificado na Spec 009-ext
- O endpoint `POST /api/v1/jogos/{jogoId}/reset/vida-todos` foi especificado no design MODO-SESSAO.md
- Nenhum desses endpoints tem implementacao frontend — nao existe botao de reset em nenhuma tela
- Nao e possivel confirmar se o endpoint backend `/jogos/{jogoId}/reset/vida-todos` foi implementado (nao mencionado nos commits recentes da Spec 009)

**Passo 4: Mestre quer confirmar uso de prospeccao**
- O `ProspeccaoPainelMestreComponent` especificado nao existe
- Sem painel de sessao, o Mestre nao tem visao global de usos pendentes
- Precisaria abrir a ficha de cada jogador individualmente

**Passo 5: Mestre quer revelar NPC para jogador especifico**
- `NpcsComponent` lista NPCs mas nao tem painel de visibilidade
- O `NpcVisibilidadeComponent` especificado em `NPC-VISIBILITY.md` nao existe
- O endpoint `POST /fichas/{id}/visibilidade` (Spec 009-ext) pode nao estar implementado no backend — nao esta nos commits recentes

**Passo 6: Mestre quer conceder dado de prospeccao a um jogador**
- Nenhuma tela do Mestre tem controle de concessao de prospeccao
- O endpoint `POST /fichas/{id}/prospeccao/conceder` (Spec 009-ext) e requisito de backend a implementar

### Pontos de friccao criticos (Mestre — Modo Sessao)

| Passo | Friccao | Severidade |
|-------|---------|------------|
| Ver status de todos os jogadores | Painel centralizado nao existe | BLOQUEADOR |
| Conceder XP via UI | Nenhum botao/tela dedicada | ALTO |
| Resetar vida em lote | Endpoint e botao ausentes | ALTO |
| Confirmar prospeccao | Painel global ausente | ALTO |
| Revelar NPC para jogador | NpcVisibilidadeComponent ausente | MEDIO |
| Conceder dados de prospeccao | Nenhuma tela do Mestre tem controle | MEDIO |

---

## 3. Gaps de UX por Contexto

| ID | Gap | Contexto | Impacto | Spec que deveria cobrir | Status |
|----|-----|----------|---------|------------------------|--------|
| UX-01 | Vida atual nao exibida — barra hardcoded em 100% | Sessao (Jogador) | ALTO | MODO-SESSAO.md / Spec 009-ext RF-ESS-004 | Nao coberto na UI |
| UX-02 | Essencia atual nao exibida | Sessao (Jogador) | ALTO | Spec 009-ext RF-ESS-003/004 | Nao coberto na UI |
| UX-03 | Sem botoes de gasto de vida/essencia | Sessao (Jogador) | BLOQUEADOR | MODO-SESSAO.md sec.2.4 | Componente nao existe |
| UX-04 | Bloco de prospeccao ausente da aba Resumo | Sessao (Jogador) | ALTO | PROSPECCAO-SESSAO.md sec.3.1 | Componente nao existe |
| UX-05 | Modo Sessao sem toggle de ativacao | Sessao (Jogador) | MEDIO | MODO-SESSAO.md sec.6 | Nao implementado |
| UX-06 | Abas nao se reorganizam em modo sessao | Sessao (Jogador) | MEDIO | MODO-SESSAO.md sec.2.1 | Nao implementado |
| UX-07 | Painel centralizado de sessao do Mestre ausente | Sessao (Mestre) | BLOQUEADOR | MODO-SESSAO.md sec.3 | Componente nao existe |
| UX-08 | Sem tela de conceder XP para o Mestre | Sessao (Mestre) | ALTO | Spec 012 gap + Spec 006 sec.7 | Nao implementado |
| UX-09 | Reset de vida em lote ausente | Sessao (Mestre) | ALTO | MODO-SESSAO.md sec.4 + Spec 009-ext RF-RESET | Sem frontend e endpoint em duvida |
| UX-10 | Painel global de prospeccao pendente ausente | Sessao (Mestre) | ALTO | PROSPECCAO-SESSAO.md sec.7 | Componente nao existe |
| UX-11 | NpcVisibilidadeComponent ausente | NPCs (Mestre) | MEDIO | NPC-VISIBILITY.md | Nao existe |
| UX-12 | Ficha com status nao exibido (RASCUNHO vs COMPLETA) | Criacao | MEDIO | Spec 006 RF-011 | Campo status ausente do modelo TypeScript |
| UX-13 | Wizard de criacao nao implementado | Criacao | BLOQUEADOR | Spec 006 | FichaFormComponent atual envia apenas {nome} |
| UX-14 | pontosVantagemRestantes hardcoded como 0 | Edicao | ALTO | Spec 012 + Spec 006 sec.8 | `[pontosVantagemRestantes]="0"` no template |
| UX-15 | Badge de pontos pendentes para level up ausente | Edicao | ALTO | LEVEL-UP.md sec.3.2 | Nao implementado |
| UX-16 | LevelUpDialogComponent ausente | Edicao | BLOQUEADOR | LEVEL-UP.md sec.4 | Nao existe |
| UX-17 | NpcsComponent redireciona para rota de Jogador | NPCs (Mestre) | MEDIO | N/A | Bug: router.navigate(['/jogador/fichas', ...]) |
| UX-18 | categoriaNome ausente de FichaVantagemResponse | Edicao | MEDIO | Spec 009 (implementado no backend) | Frontend agrupa todas como "Vantagens" |
| UX-19 | FichaResumo sem pontosDisponiveis | Edicao | ALTO | Spec 012 + Spec 006 sec.8 | Campos ausentes do modelo TypeScript e backend |
| UX-20 | Descricao do personagem sem tela de edicao | Criacao/Edicao | BAIXO | Spec 006 sec.6 | Campo existe no backend, sem UI |

---

## 4. Acoes Criticas Sem Spec ou Sem Implementacao

### 4.1 Acoes sem nenhum spec cobrindo

**ACAO-01: Editar vida membro a membro**
O sistema tem `FichaVidaMembro` com `danoRecebido` por regiao corporal (cabeca, torax, brcao esquerdo, etc.). Nenhum spec descreve como o Jogador ou Mestre registra dano por membro. O reset de estado zera todos os membros, mas o fluxo de entrada de dano por membro nao tem UI especificada.

**ACAO-02: Editar campo `outros` do atributo**
O `FichaAtributo` tem campo `outros` (modificadores temporarios, magias, etc.). Nenhum spec descreve quando/como esse campo e editado apos a criacao da ficha.

**ACAO-03: Campo `sorte` das aptidoes**
O `FichaAptidao.sorte` e descrito como controlado pelo Mestre, mas nenhum spec detalha a UI de edicao desse campo especificamente — o Spec 012 menciona que e readonly no wizard de level up, mas nao define onde o Mestre edita.

**ACAO-04: Conceder Insolitus pelo Mestre**
O Insolitus e concedido pelo Mestre via "endpoint dedicado" (Spec 006 sec.9), mas esse endpoint nao esta descrito em nenhum spec com payload, rota ou comportamento frontend detalhados.

**ACAO-05: Ficha "morta" ou "abandonada"**
A decisao do PO de 2026-04-03 e que fichas nunca sao deletadas (states: ativa, morta, abandonada). Nenhum spec descreve como o Mestre marca uma ficha como morta/abandonada, nem como fichas nesses estados aparecem na listagem.

### 4.2 Acoes especificadas mas sem implementacao frontend confirmada

**ACAO-06: Confirmar transicao RASCUNHO -> COMPLETA**
`PUT /fichas/{id}/completar` (Spec 006) — sem implementacao frontend.

**ACAO-07: Auto-save a cada passo do wizard**
A logica de retomada de rascunho ("abriu o wizard novamente e retornou ao Passo 3") nao tem especificacao de como o frontend detecta a existencia de um rascunho ao iniciar a criacao.

**ACAO-08: XP em lote**
`PUT /jogos/{id}/fichas/xp-lote` (Spec 006 sec.7) descrito como "futuramente" — sem spec detalhada, sem implementacao.

---

## 5. Recomendacoes de Melhoria por Spec

### 5.1 Spec 006 — Wizard de Criacao

**REC-006-01: Especificar deteccao de rascunho existente**
O Passo 1 (Identidade) deve verificar antes de criar se ja existe uma ficha RASCUNHO do usuario naquele jogo. Se sim: perguntar "Voce tem um personagem incompleto. Continuar de onde parou?" com opcoes [Continuar] e [Comecar do zero]. "Comecar do zero" nao deleta o rascunho (fichas nao sao deletadas) — cria um novo.

**REC-006-02: Adicionar passo de Aptidoes ao wizard**
O design `WIZARD-CRIACAO-FICHA.md` tem 5 passos: Identidade / Raca e Classe / Personalidade / Atributos / Revisao. O Spec 006 original tem 5 passos que incluem Aptidoes (Passo 3). O design atual pula aptidoes — o jogador chegaria ao nivel 1 sem ter distribuido pontos de aptidao. Decidir: aptidoes ficam no wizard (alinhado ao Spec 006) ou sao distribuidas no primeiro level up?

**REC-006-03: Campo status nao esta no modelo TypeScript**
A interface `Ficha` em `ficha.model.ts` nao tem o campo `status` (RASCUNHO/COMPLETA). Isso impede que o frontend exiba o badge "Incompleta" (RF-011) ou bloqueie fichas RASCUNHO de entrar em sessao (RN-001).

**REC-006-04: Especificar fluxo de retomada do wizard em rota**
Qual e a rota do wizard? `/fichas/nova` ou `/jogos/:jogoId/fichas/nova`? Se o wizard esta em `/fichas/nova`, como o frontend sabe para qual jogo criar (o Jogador pode estar em varios jogos)? Isso e lacuna critica de fluxo — o jogo precisa estar selecionado antes do wizard comecar.

### 5.2 Spec 009-ext — Visibilidade, Essencia, Prospeccao

**REC-009-01: Resolver inconsistencia do endpoint de essencia**
O design `MODO-SESSAO.md` (sec.12) referencia `POST /fichas/{id}/essencia/gastar` e `POST /fichas/{id}/essencia/curar`. A Spec 009-ext diz que o frontend deve usar `PUT /fichas/{id}/vida` (endpoint existente) passando `essenciaAtual`. Escolher UM padrao e documentar nos dois lugares. Recomendacao: usar o endpoint semantico separado para clareza de intencao.

**REC-009-02: Confirmar quais endpoints da Spec 009-ext estao no backend**
Os commits recentes (branch `feature/009-npc-fichas-mestre`) nao mencionam explicitamente: `POST /fichas/{id}/prospeccao/usar`, `PATCH /fichas/{id}/prospeccao/usos/{usoId}/confirmar`, `POST /fichas/{id}/visibilidade`, `GET /fichas/{id}/visibilidade`, `POST /fichas/{id}/resetar-estado`. Esses precisam de auditoria no backend antes de criar tasks frontend para eles.

**REC-009-03: Especificar polling vs WebSocket para atualizacoes em tempo real**
Durante uma sessao ativa, quando o Mestre concede XP ou reseta vida, como o Jogador ve a mudanca na propria ficha? O design atual nao especifica. Opcoes: (a) Polling a cada 30s, (b) Server-Sent Events, (c) atualiza manualmente ao clicar em "Recarregar". Esta decisao impacta a experiencia de sessao e precisa de decisao do PO.

**REC-009-04: Especificar o que o Jogador ve ao acessar NPC com visivelGlobalmente=true mas sem acesso granular**
O design `NPC-VISIBILITY.md` descreve o card com cadeado e "Estatisticas ocultadas pelo Mestre". A Spec 009-ext RF-VIS-005 confirma. Mas: o card do NPC exibe nome, raca, classe — e isso suficiente para o Jogador entender que e um NPC inimigo vs aliado vs desconhecido? Considerar adicionar um tipo visual (icone de inimigo, aliado, neutro) controlado pelo Mestre.

### 5.3 Spec 012 — Level Up

**REC-012-01: Especificar quem distribui pontos de aptidao**
O design `LEVEL-UP.md` diz que "pontos de aptidao sao controlados pelo Mestre" na linha do Spec 012, mas o step 2 do dialog de level up permite ao Jogador distribuir aptidoes livremente (campo `base` editavel). Ha uma contradicao. Decisao necessaria do PO: Jogador distribui livremente OU precisa de aprovacao do Mestre?

**REC-012-02: Pontos perdidos ao atingir limitador**
O design `LEVEL-UP.md` (sec.11) mostra aviso "Os X pontos restantes nao podem ser distribuidos neste nivel. Eles serao perdidos ao confirmar." — esta e uma regra de negocio critica que impacta diretamente a satisfacao do jogador. Verificar se isso e o comportamento desejado ou se pontos devem acumular para o proximo nivel.

**REC-012-03: Especificar level up multiplo em uma unica concessao de XP**
Se o Mestre concede XP suficiente para subir 3 niveis de uma vez, o dialog de level up deve aparecer 3 vezes ou consolidar os pontos dos 3 niveis em um unico dialog? O spec diz "Backend recalcula o nivel atual" mas nao detalha a UI para multiplos niveis.

---

## 6. Quick Wins de UX

Quick wins sao melhorias que podem ser feitas no codigo existente sem spec novo e sem novos endpoints de backend.

### QW-UX-01: Exibir essenciaAtual e vidaAtual no header

**Arquivo:** `FichaHeaderComponent`
**Problema:** `[value]="100"` hardcoded nas barras de vida e essencia
**Solucao:** O `FichaResumo` retornado pelo backend ja inclui `vidaTotal` e `essenciaTotal`. O backend em `FichaVida` ja tem `vidaAtual` e em `FichaEssencia` ja tem `essenciaAtual`. Adicionar esses dois campos ao `FichaResumoResponse` backend e ao modelo TypeScript `FichaResumo`, depois conectar as barras.
**Impacto:** Elimina o maior bug visual da ficha — as barras sempre cheias enganam o jogador
**Esforco estimado:** Backend: 1h (adicionar 2 campos ao DTO). Frontend: 30min (usar os campos).

### QW-UX-02: Corrigir rota de navegacao do NpcsComponent

**Arquivo:** `NpcsComponent`, linha 432
**Problema:** `this.router.navigate(['/jogador/fichas', fichaId])` envia o Mestre para a rota de Jogador
**Solucao:** Usar `/fichas/:id` (rota generica) ou `/mestre/fichas/:id` se existir
**Impacto:** Evita que o Mestre seja tratado como Jogador ao ver ficha de NPC
**Esforco estimado:** 5 minutos

### QW-UX-03: Exibir categoriaNome nas vantagens

**Arquivo:** `FichaVantagensTabComponent`
**Problema:** O backend ja retorna `categoriaNome` (implementado na Spec 009 — ver commits recentes). O frontend agrupa tudo como "Vantagens" por fallback
**Solucao:** Adicionar `categoriaNome?: string` ao `FichaVantagemResponse` no TypeScript e remover o fallback
**Impacto:** Vantagens ficam organizadas por categoria (Combate, Magia, Sobrevivencia, etc.)
**Esforco estimado:** 15 minutos

### QW-UX-04: Adicionar campo status ao modelo Ficha

**Arquivo:** `ficha.model.ts`
**Problema:** Interface `Ficha` nao tem campo `status` — impossivel exibir badge "Incompleta" para fichas RASCUNHO
**Solucao:** Adicionar `status: 'RASCUNHO' | 'COMPLETA' | null` ao modelo TypeScript
**Impacto:** Desbloqueia o RF-011 (badge de incompleta) e o RN-001 (bloquear ficha RASCUNHO de entrar em sessao)
**Esforco estimado:** 5 minutos de modelo + 30 minutos de UI

### QW-UX-05: Exibir XP como barra de progresso ate o proximo nivel

**Arquivo:** `FichaHeaderComponent`
**Problema:** XP e exibido apenas como numero (`XP: 4200`). O usuario nao sabe quantos XP faltam para o proximo nivel.
**Solucao:** Calcular percentual usando `resumo().xp / proximo nivel xpNecessaria`. O NivelConfig do jogo pode ser lido do `ConfigStore` (ja existe no frontend).
**Impacto:** Jogador tem feedback visual de progressao — mecanica importante para engajamento em RPG
**Esforco estimado:** 1-2h (requer acesso ao NivelConfig no contexto do FichaHeader)

### QW-UX-06: Badge de pontos pendentes para distribuicao

**Arquivo:** `FichaHeaderComponent`, `FichaVantagensTabComponent`
**Problema:** `pontosVantagemRestantes` esta hardcoded como `0`. Jogadores com pontos nao distribuidos nao recebem nenhum aviso.
**Solucao (parcial sem backend):** Quando `FichaResumo.pontosVantagemDisponiveis` for adicionado ao backend, conectar ao badge de aviso no header. A logica do badge ja esta especificada em `LEVEL-UP.md` sec.3.2.
**Impacto:** Evita que jogadores esquecam de gastar pontos acumulados
**Esforco estimado (backend):** Media — requer adicionar campos ao FichaResumoResponse

### QW-UX-07: Adicionar campo descricao a ficha de jogador

**Arquivo:** `FichaDetailComponent` — nova aba ou secao na aba Resumo
**Problema:** Campo `descricao` existe no backend (Spec 006 sec.6) mas nao tem UI para visualizar nem editar
**Solucao:** Adicionar secao "Sobre o personagem" na aba Resumo com o campo `descricao` em modo leitura + botao editar para Mestre e dono da ficha
**Impacto:** Fecha gap de funcionalidade documentado no Spec 006
**Esforco estimado:** 1-2h (endpoint ja existe, so precisa de UI)

### QW-UX-08: Adicionar label semantico aos dados de combate na aba Resumo

**Arquivo:** `FichaResumoTabComponent`
**Problema:** Os bonus (BBA, BBM, RD) aparecem como grid de cards sem hierarquia visual. Em sessao de jogo, o jogador precisa de acesso visual rapido a BBA e Impeto — que sao os mais usados.
**Solucao (sem novo componente):** Destacar os bonus de combate com icones distintos e tamanho de fonte maior. Adicionar tooltip com descricao de cada bonus.
**Impacto:** Reduz o tempo de consulta durante combate
**Esforco estimado:** 30 minutos

---

## 7. Analise dos Contextos por Pergunta

### Contexto 1: Modo Sessao

**P1 — Acoes criticas durante o jogo**
O jogador PRECISA fazer rapidamente: subtrair vida (dano), subtrair essencia (uso de habilidades), usar dado de prospeccao. Nenhuma dessas acoes esta implementada na UI atual.

**P2 — Acessibilidade de acoes criticas**
NAO. Vida e essencia exigem codigo backend (endpoint existente, falta UI). Prospeccao exige novo componente.

**P3 — Atalhos ausentes**
Faltam: botao de gasto customizado (digitar quantidade de dano), indicador de status MORTO (vida = 0), atalho para prospeccao no modo sessao.

**P4 — Mestre ve todas as fichas**
NAO. Nao existe painel centralizado. O Mestre precisa navegar ficha por ficha.

**P5 — Reset em lote vs individual**
Nao implementado em nenhum dos dois contextos. O endpoint de reset individual (Spec 009-ext) pode existir no backend, mas sem frontend. O reset em lote precisa de novo endpoint.

**P6 — Lacunas nao cobertas pelos specs**
A sessao em tempo real (atualizacoes reativas quando o Mestre age) nao tem especificacao de mecanismo de comunicacao (polling vs SSE vs manual). Essa e uma lacuna de spec critica para a experiencia de sessao ao vivo.

### Contexto 2: Criacao de Ficha

**P1 — Wizard cobre campos obrigatorios?**
O Spec 006 cobre: nome, raca, classe, genero, indole, presenca, atributos, vantagens, revisao. O design `WIZARD-CRIACAO-FICHA.md` tem 5 passos mas pula aptidoes (Passo 3 do Spec 006). Ha divergencia entre spec e design que precisa de alinhamento.

**P2 — Auto-save bem especificado?**
A logica de auto-save esta especificada (PUT a cada passo), mas nao esta o fluxo de retomada. Se o jogador fecha o browser no Passo 2, ao voltar ele precisa: (a) reconhecer que ja tem rascunho, (b) ser direcionado ao passo correto. Isso nao esta especificado.

**P3 — Jogador vai entender os campos?**
Os campos de identidade (nome, genero, indole, presenca) sao claros. Os campos de atributos sao o maior risco — jogadores novos nao sabem o que e "Força", "Vigor" ou como o limitador funciona. O Spec 006 especifica barra visual e contador, mas nao especifica tooltips com descricao de cada atributo.

**P4 — Campos que precisam de tooltip/ajuda**
Identificados: `limitadorAtributo` (o que significa ter teto de 20?), `indole` vs `presenca` (diferenca nao e obvia), `pontosAtributo disponiveis` (quanto e suficiente para um personagem equilibrado?), `bonus de raca` no campo `outros` (por que aparece automaticamente?).

**P5 — Distribuicao de atributos esta clara?**
O design e razoavel (contador regressivo + barra por atributo). O risco e o usuario tentar colocar mais pontos do que tem e o botao simplesmente ser desabilitado sem explicacao contextual suficiente.

### Contexto 3: Edicao e Level Up

**P1 — Wizard de Level Up bem especificado?**
O `LEVEL-UP.md` esta muito bem especificado: dialog com p-stepper, 3 steps, animacao, toast, badge persistente. A dependencia de `pontosAtributoDisponiveis` no backend e o unico bloqueador.

**P2 — Pontos acumulados claros?**
O design especifica o badge persistente no header com texto como "3 atrib. + 5 apt. para distribuir" — isso e claro. O risco e o jogador fechar o dialog sem distribuir e esquecer (o badge resolve isso).

**P3 — Compra de vantagens especificada?**
O Passo 4 (Vantagens) do wizard de criacao especifica a compra inicial. A compra pos-criacao esta implicitamente na aba de Vantagens, mas sem spec detalhada de como o jogador navega pelo catalogo de vantagens, filtra por categoria, ve pre-requisitos e confirma a compra. Esse fluxo precisa de spec proprio.

**P4 — Acoes exclusivas do Mestre misturadas com Jogador?**
No `FichaDetailComponent` atual, o `podeEditar` computed mistura Mestre e Jogador para edicao. O `podeDeletar` e exclusivo do Mestre. O problema e que acoes como "Conceder Insolitus", "Editar aptidao (campo sorte)", "Conceder XP" nao aparecem em nenhuma UI mesmo para o Mestre — estao em specs mas sem implementacao.

### Contexto 4: NPCs

**P1 — O que o Jogador ve de um NPC**
Bem definido na Spec 009-ext e no design `NPC-VISIBILITY.md`: sem acesso granular, ve nome/raca/classe + cadeado. Com acesso granular, ve ficha completa. Implementado no backend, nao no frontend.

**P2 — Mestre revela stats para jogador especifico (nao para todos)**
Especificado no Spec 009-ext RF-VIS-002 e no design `NPC-VISIBILITY.md` via `p-multiselect`. O componente nao existe ainda.

**P3 — Criacao/edicao de NPC vs ficha de jogador**
Atualmente sao identicas: o `NpcsComponent` usa o mesmo formulario de campos da ficha. Isso e correto — NPC e mecanicamente identico. A diferenca e: (a) Jogador nao cria NPC, (b) NPC nao tem jogadorId, (c) NPC tem descricao exclusiva (campo mais relevante para NPCs). O formulario de criacao de NPC no `NpcsComponent` nao inclui o campo `descricao` — isso e uma lacuna menor.

---

## 8. Pontos em Aberto para o PO

| ID | Questao | Impacto de Negocio |
|----|---------|-------------------|
| PA-UX-01 | Atualizacoes em tempo real durante sessao: polling, SSE ou manual? | Define a arquitetura de sessao — impacta todos os componentes de modo sessao |
| PA-UX-02 | Aptidoes no wizard de criacao ou apenas no primeiro level up? | Afeta estrutura do wizard e fluxo de criacao |
| PA-UX-03 | Pontos que excedem o limitador sao perdidos ou acumulam? | Regra de negocio critica — afeta satisfacao do jogador |
| PA-UX-04 | Mestre pode editar sorte de aptidao direto na ficha (sem wizard)? | Afeta design do tab de aptidoes e permissoes |
| PA-UX-05 | Estados ativa/morta/abandonada: Mestre pode reativar uma ficha morta? | Afeta modelo de dados e UI de historico |
| PA-UX-06 | Compra de vantagens pos-criacao: Jogador compra livremente ou precisa de aprovacao do Mestre? | Afeta o design do catalogo de vantagens |
| PA-UX-07 | Level up multiplo em uma concessao de XP: um dialog ou multiplos? | Afeta design do LevelUpDialogComponent |
| PA-UX-08 | O campo `descricao` de NPC deve aparecer no drawer de criacao de NPC? | Pequeno gap de usabilidade no NpcsComponent |

---

## 9. Checklist de Implementacao Prioritizada

### Prioridade 1 — Bloqueadores (sem isso, ficha nao funciona em sessao)

- [ ] Adicionar `vidaAtual` e `essenciaAtual` ao `FichaResumoResponse` (backend)
- [ ] Adicionar `vidaAtual` e `essenciaAtual` ao modelo `FichaResumo` TypeScript
- [ ] Conectar barras de vida/essencia no `FichaHeaderComponent` a valores reais
- [ ] Implementar `FichaStatsCombateComponent` com botoes de gasto
- [ ] Adicionar campo `status` ao modelo `Ficha` TypeScript

### Prioridade 2 — Alto impacto (sessao parcialmente funcional)

- [ ] Implementar `ProspeccaoJogadorComponent` na aba Resumo
- [ ] Adicionar `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis`, `pontosVantagemDisponiveis` ao `FichaResumoResponse` (backend)
- [ ] Remover hardcode `[pontosVantagemRestantes]="0"` no `FichaDetailComponent`
- [ ] Corrigir rota do `NpcsComponent` (`/jogador/fichas` -> rota correta)
- [ ] Adicionar `categoriaNome` ao modelo `FichaVantagemResponse` TypeScript

### Prioridade 3 — Medio impacto (UX melhora significativamente)

- [ ] Implementar `LevelUpDialogComponent` com p-stepper (depende da Prioridade 2)
- [ ] Implementar badge de pontos pendentes no `FichaHeaderComponent`
- [ ] Implementar modo sessao toggle no `FichaHeaderComponent`
- [ ] Adicionar campo `descricao` na aba Resumo do `FichaDetailComponent`

### Prioridade 4 — Quick wins (30-60 minutos cada)

- [ ] Adicionar `categoriaNome` ao modelo TypeScript (QW-UX-03)
- [ ] Adicionar `status` ao modelo Ficha (QW-UX-04)
- [ ] Corrigir rota NpcsComponent (QW-UX-02)
- [ ] Adicionar tooltips nos bonus da aba Resumo (QW-UX-08)

---

*Produzido por: Business Analyst/PO | 2026-04-03*
*Fontes auditadas: specs 006, 009-ext, 012; designs MODO-SESSAO, FICHA-DETAIL-DESIGN, LEVEL-UP, PROSPECCAO-SESSAO, NPC-VISIBILITY, WIZARD-CRIACAO-FICHA; componentes FichaDetailComponent, FichaHeaderComponent, FichaResumoTabComponent, FichaVantagensTabComponent, NpcsComponent, ficha.model.ts*
