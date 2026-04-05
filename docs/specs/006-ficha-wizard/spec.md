# Spec 006 — Wizard de Criacao de Ficha

> Spec: `006-ficha-personagem` / `006-ficha-wizard`
> Epic: EPIC 4
> Status: Desbloqueado — decisoes do PO confirmadas em 2026-04-02
> Depende de: Spec 005 (participantes aprovados), Spec 007 (VantagemEfeito — motor de calculos)
> Bloqueia: Spec 008 (utilidade/fluidez), Spec 009 (NPC visibility)

---

## 1. Visao Geral do Negocio

**Problema resolvido:** O `FichaFormComponent` atual envia apenas `{ nome }` e usa campos inexistentes no backend (`origem`, `linhagem`, `descricaoFisica`, atributos hardcoded FOR/DES/CON...). A ficha criada fica sem raca, classe, genero, indole e presenca — todos os calculos derivados ficam errados ou ausentes.

**Objetivo:** Implementar um wizard de 5 passos com auto-save progressivo que garanta que todos os dados obrigatorios sejam coletados na criacao, os atributos sejam distribuidos na criacao (nao depois), e a ficha so seja marcada como "completa" quando todos os campos obrigatorios estiverem preenchidos. Fichas incompletas nao podem ser usadas em sessao.

**Decisao do PO (2026-04-02):**
- Wizard de 5-6 passos (Opcao B) — experiencia completa
- Auto-save a cada passo: salva versao incompleta no backend; bloqueia uso em sessao ate completar
- Todos os campos sao obrigatorios para completar a ficha
- Atributos SAO preenchidos na criacao
- Insolitus, Titulo Heroico e Arquetipo: concedidos pelo Mestre depois, nao obrigatorios no wizard

---

## 2. Atores Envolvidos

| Ator | Role | Acoes no wizard |
|------|------|----------------|
| Jogador | JOGADOR | Cria propria ficha no wizard (atores aprovados no jogo) |
| Mestre | MESTRE | Cria ficha para qualquer jogador; cria NPC (rota dedicada); pode pular restricoes de validacao de pontos |
| Sistema | — | Auto-save a cada passo; calcula nivel, limitador e pontos disponíveis via NivelConfig |

---

## 3. Estados da Ficha

| Estado | Descricao | Restricao |
|--------|-----------|-----------|
| RASCUNHO | Ficha criada pelo wizard, campos obrigatorios ainda incompletos | Nao pode entrar em sessao; editavel |
| COMPLETA | Todos os campos obrigatorios preenchidos e validados | Pode entrar em sessao; editavel |
| ARQUIVADA | Soft-deleted | Invisivel para o Jogador; visivel para o Mestre |

> Campo `status` na entidade Ficha: `RASCUNHO` ou `COMPLETA`. O sistema transiciona automaticamente quando todos os passos estao preenchidos.

---

## 4. Estrutura do Wizard (5 Passos)

### Passo 1 — Identificacao

**Obrigatorios:** nome, racaId, classeId, generoId, indoleId, presencaId

**Validacoes:**
- `nome`: min 1 char, max 100 chars; obrigatorio
- `racaId`: deve ser uma Raca ativa do jogo
- `classeId`: deve ser uma ClassePersonagem ativa do jogo; a classe deve ser permitida pela Raca selecionada (RacaClassePermitida)
- `generoId`, `indoleId`, `presencaId`: configs ativas do jogo

**Carrega:**
- `GET /api/v1/configuracoes/racas?jogoId={id}` — lista de racas disponiveis
- `GET /api/v1/configuracoes/classes?jogoId={id}` — filtrado pelas classes permitidas pela raca escolhida
- `GET /api/v1/configuracoes/generos?jogoId={id}`
- `GET /api/v1/configuracoes/indoles?jogoId={id}`
- `GET /api/v1/configuracoes/presencas?jogoId={id}`

**Auto-save ao avancar:** POST /jogos/{jogoId}/fichas com status RASCUNHO se primeira vez; PUT /fichas/{id} nas subsequentes.

---

### Passo 2 — Atributos

**Obrigatorio:** distribuir TODOS os pontos de atributo disponiveis para o nivel inicial (NivelConfig.nivel = 0 ou 1).

**Regras:**
- Carrega `GET /api/v1/configuracoes/atributos?jogoId={id}` — lista dinamica, nunca hardcoded
- Carrega `GET /api/v1/configuracoes/niveis?jogoId={id}` para obter `pontosAtributo` e `limitadorAtributo` do nivel inicial
- Para cada atributo: campo `base` editavel pelo usuario; campos `nivel` e `outros` iniciam em 0
- Restricao por atributo: `base` nao pode ultrapassar `NivelConfig.limitadorAtributo` do nivel atual
- Restricao global: soma de todos os `base` nao pode ultrapassar `NivelConfig.pontosAtributo` do nivel inicial
- Os bonus de raca (RacaBonusAtributo) SAO aplicados automaticamente ao campo `outros` — nao consomem pontos do jogador
- Pontos nao precisam ser todos alocados para avancar (jogador pode reservar); porem todos os atributos devem ter ao menos 0

**Exibicao:**
- Contador global: "X pontos disponiveis / Y utilizados"
- Por atributo: barra visual "Base: X / Limite: Y"
- Badge de bonus de raca: "+Z (bonus de raca)" em destaque

**Auto-save ao avancar:** PUT /fichas/{id}/atributos com valores atuais.

---

### Passo 3 — Aptidoes

**Obrigatorio:** nenhum valor minimo — jogador pode deixar todas em 0 e avancar.

**Regras:**
- Carrega `GET /api/v1/configuracoes/aptidoes?jogoId={id}` — agrupadas por TipoAptidao
- Para cada aptidao: campo `base` editavel; `sorte` e `classe` sao 0 na criacao (Mestre pode ajustar depois)
- Contador de `pontosAptidao` disponiveis vs utilizados
- Bonus de classe (ClasseAptidaoBonus) aplicados automaticamente — nao consomem pontos

**Auto-save ao avancar:** PUT /fichas/{id}/aptidoes.

---

### Passo 4 — Vantagens (opcional na criacao)

**Obrigatorio:** nenhuma vantagem e obrigatoria na criacao.

**Regras:**
- Carrega `GET /api/v1/configuracoes/vantagens?jogoId={id}` agrupadas por CategoriaVantagem
- Pontos de vantagem disponiveis: soma de `PontosVantagemConfig.pontosGanhos` para os niveis 0 e 1
- Jogador pode comprar 0 ou mais vantagens; qualquer ponto nao gasto e mantido para uso futuro
- Pré-requisitos de vantagem SAO validados (backend valida antes de confirmar)
- Uma vantagem comprada NUNCA pode ser removida

**Auto-save ao avancar:** POST /fichas/{id}/vantagens para cada vantagem comprada.

---

### Passo 5 — Revisao e Confirmacao

**O que exibe:**
- Resumo de todos os campos preenchidos nos passos 1-4
- Atributos com totais calculados (base + bonus de raca)
- Aptidoes com totais (base + bonus de classe)
- Vantagens compradas com custo
- Pontos nao utilizados (atributo, aptidao, vantagem)
- Preview calculado via `POST /fichas/{id}/preview` (sem persistir)

**Acoes:**
- "Voltar" — retorna ao passo anterior para corrigir
- "Criar Ficha" — transiciona status de RASCUNHO para COMPLETA; redireciona para FichaDetailPage

**Validacao final (backend):**
- Todos os campos obrigatorios do Passo 1 preenchidos
- Nenhum atributo ultrapassou o limitador
- Total de pontos de atributo distribuidos nao excede o disponivel
- Classe permitida pela raca selecionada

---

## 5. Contrato de API — Wizard

| Endpoint | Passo | Metodo | Descricao |
|----------|-------|--------|-----------|
| POST /api/v1/jogos/{jogoId}/fichas | Passo 1 (1a vez) | MESTRE/JOGADOR | Cria ficha com status RASCUNHO |
| PUT /api/v1/fichas/{id} | Passo 1 (edicao) | MESTRE/dono | Atualiza identificacao |
| PUT /api/v1/fichas/{id}/atributos | Passo 2 | MESTRE/dono | Salva distribuicao de atributos |
| PUT /api/v1/fichas/{id}/aptidoes | Passo 3 | MESTRE/dono | Salva distribuicao de aptidoes |
| POST /api/v1/fichas/{id}/vantagens | Passo 4 | MESTRE/dono | Compra vantagem (nivel 1) |
| POST /api/v1/fichas/{id}/preview | Passo 5 | MESTRE/dono | Calcula valores sem persistir |
| PUT /api/v1/fichas/{id}/completar | Passo 5 | MESTRE/dono | Transiciona RASCUNHO → COMPLETA |

### CreateFichaRequest (Passo 1 — criacao)

```json
{
  "nome": "Aldric, o Guardiao",
  "jogoId": 5,
  "racaId": 2,
  "classeId": 1,
  "generoId": 3,
  "indoleId": 1,
  "presencaId": 2,
  "isNpc": false
}
```

> `jogadorId`: preenchido automaticamente pelo backend com o ID do usuario logado (se JOGADOR). Para o Mestre criar ficha de outro jogador, aceita `jogadorId` explicito no request.

### FichaResponse (retorno apos criacao)

```json
{
  "id": 101,
  "jogoId": 5,
  "jogadorId": 18,
  "nome": "Aldric, o Guardiao",
  "racaId": 2,
  "racaNome": "Humano",
  "classeId": 1,
  "classeNome": "Guerreiro",
  "generoId": 3,
  "generoNome": "Masculino",
  "indoleId": 1,
  "indoleNome": "Leal",
  "presencaId": 2,
  "presencaNome": "Austero",
  "nivel": 1,
  "xp": 0,
  "renascimentos": 0,
  "isNpc": false,
  "status": "RASCUNHO",
  "descricao": null,
  "dataCriacao": "2026-04-02T10:00:00",
  "dataUltimaAtualizacao": "2026-04-02T10:00:00"
}
```

---

## 6. Campo `descricao`

**Decisao do PO (2026-04-02):** O campo `descricao` existe em fichas de jogadores (nao apenas NPCs), mas e opcional.

- Tipo: TEXT (sem limite rigido, max recomendado 2000 chars para UX)
- Editavel por: MESTRE e dono da ficha
- Exibicao: aba ou secao "Sobre o personagem" no FichaDetailPage
- Nao entra no wizard obrigatorio — editavel apos a criacao na tela de edicao

---

## 7. Campo XP — Somente Mestre

**Decisao do PO (2026-04-02):** XP e read-only para o Jogador. Apenas o Mestre concede XP.

| Operacao | Endpoint | Role |
|----------|----------|------|
| Concessao de XP para uma ficha | PUT /fichas/{id}/xp | MESTRE |
| Concessao em lote (todas as fichas do jogo) | PUT /jogos/{id}/fichas/xp-lote (futuramente) | MESTRE |

### ConcessaoXpRequest

```json
{
  "xp": 1500,
  "motivo": "Sessao 12 — Derrota do Bosso Final"
}
```

> Campo `motivo`: texto livre opcional. Sem persistencia de historico no MVP (decisao do PO).

**Regras:**
- XP e acumulativo — cada concessao ADICIONA ao xp atual (nao substitui)
- Nao existe reducao de XP via API publica (apenas o Mestre pode, e nao e use case padrao)
- Nivel e recalculado automaticamente pelo backend apos cada concessao de XP
- Level up automatico: ao atingir XP suficiente para o proximo nivel, o backend atualiza o nivel da ficha e libera os pontos para o Jogador distribuir

### Level Up Automatico

Ao receber XP que ultrapassa o threshold do proximo nivel (NivelConfig.xpNecessaria):
1. Backend recalcula o nivel atual
2. Backend calcula pontos ganhos: `SUM(pontosAtributo)` de todos os niveis atingidos - total ja distribuido
3. Backend retorna `FichaResumoResponse` com `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis`, `pontosVantagemDisponiveis` atualizados
4. Frontend exibe notificacao de level up e prompt para distribuir pontos

---

## 8. FichaResumoResponse — Campos de Pontos

**Decisao do PO (2026-04-02):** Pontos acumulam ao longo dos niveis (saldo = ganhos totais - gastos totais). Gastos sao irreversiveis.

O `FichaResumoResponse` DEVE incluir:

```json
{
  "fichaId": 101,
  "nivel": 5,
  "xp": 15000,
  "pontosAtributoDisponiveis": 3,
  "pontosAptidaoDisponiveis": 5,
  "pontosVantagemDisponiveis": 2,
  "atributosTotais": { "FOR": 45, "AGI": 38, "VIG": 50 },
  "bonusTotais": { "BBA": 22, "Bloqueio": 18 },
  "vidaTotal": 120,
  "essenciaTotal": 60,
  "ameacaTotal": 25
}
```

**Calculo dos pontos disponiveis:**
- `pontosAtributoDisponiveis` = SUM(NivelConfig.pontosAtributo para niveis 1 ate nivelAtual) - SUM(base de todos os FichaAtributo)
- `pontosAptidaoDisponiveis` = SUM(NivelConfig.pontosAptidao para niveis 1 ate nivelAtual) - SUM(base de todas as FichaAptidao)
- `pontosVantagemDisponiveis` = SUM(PontosVantagemConfig.pontosGanhos para niveis atingidos) - SUM(FichaVantagem.custoPago)

---

## 9. Insólitus, Titulo Heroico e Arquetipo

**Decisao do PO (2026-04-02):** Estes tres elementos sao concedidos pelo Mestre — nao sao obrigatorios na criacao e nao entram no wizard.

- **Insolitus**: modelado como entidade configuravel similar a VantagemConfig (ver Spec 007-vantagem-efeito para detalhes). O Mestre concede via endpoint dedicado.
- **Titulo Heroico**: campo texto livre na ficha — concedido pelo Mestre via edicao direta
- **Arquetipo**: campo texto livre (ou FK para futura entidade ArquetipoConfig) — concedido pelo Mestre

> Estes campos estao fora do wizard de criacao e serao editaveis pelo Mestre na tela de detalhe da ficha.

---

## 10. Requisitos Funcionais

**RF-001** Jogador aprovado no jogo pode criar uma ficha para si mesmo.

**RF-002** Mestre pode criar ficha para qualquer jogador aprovado no jogo, informando `jogadorId` no request.

**RF-003** A criacao cria uma ficha com status RASCUNHO.

**RF-004** Auto-save ocorre ao avancar cada passo — dados parciais sao persistidos.

**RF-005** A ficha com status RASCUNHO nao pode entrar em sessao de jogo.

**RF-006** A transicao RASCUNHO → COMPLETA so ocorre quando todos os campos obrigatorios do Passo 1 estao preenchidos e validos.

**RF-007** Atributos sao distribuidos na criacao (Passo 2) — nao depois como edicao independente.

**RF-008** XP e lido via FichaResumoResponse; concessao de XP e exclusiva do Mestre.

**RF-009** Level up e automatico — o backend recalcula nivel ao receber XP e libera pontos para distribuicao.

**RF-010** Jogador nao pode editar o campo XP diretamente — qualquer PUT /fichas/{id} que inclua XP e rejeitado se o ator for JOGADOR.

**RF-011** Fichas com status RASCUNHO aparecem na lista para o dono com badge "incompleta".

**RF-012** A classe escolhida deve ser permitida pela raca — validacao RacaClassePermitida obrigatoria.

---

## 11. Requisitos Nao Funcionais

- **Consistencia:** O backend e o unico responsavel pelos calculos de nivel, pontos e totais. Frontend usa apenas para preview.
- **Auto-save seguro:** Se o usuario fechar o browser no Passo 3, os dados dos Passos 1 e 2 estao salvos como RASCUNHO.
- **Validacao dupla:** Client-side (imediata, UX) + Server-side (definitiva, seguranca).
- **Atributos dinamicos:** Nunca hardcodados. Sempre carregados de GET /configuracoes/atributos.

---

## 12. Epico e User Stories

### Epic 4 — Ficha de Personagem

---

**US-006-01: Criar ficha via wizard**
Como Jogador aprovado em um jogo,
Quero criar minha ficha de personagem passo a passo,
Para garantir que todos os dados estejam corretos sem perder informacao.

Criterios de Aceite:

Cenario 1: Passo 1 completo — identificacao criada
  Dado que sou Jogador aprovado no jogo
  Quando preencho nome, raca, classe, genero, indole e presenca e clico "Proximo"
  Entao a ficha e criada com status RASCUNHO via POST /fichas
  E avanço para o Passo 2

Cenario 2: Classe incompativel com a Raca
  Dado que seleciono Raca "Elfo" que nao permite Classe "Guerreiro"
  Quando clico "Proximo"
  Entao recebo erro inline "Esta classe nao e permitida para a raca selecionada"
  E o botao "Proximo" permanece desabilitado

Cenario 3: Fechei o browser no Passo 3
  Dado que salvei o Passo 1 e o Passo 2 com sucesso
  Quando abro o wizard novamente
  Entao retorno ao Passo 3 com os dados anteriores pre-preenchidos

---

**US-006-02: Distribuir atributos no wizard**
Como Jogador criando minha ficha,
Quero distribuir meus pontos de atributo no Passo 2 do wizard,
Para que meu personagem comece o jogo com a distribuicao que eu quero.

Criterios de Aceite:

Cenario 1: Distribuicao valida
  Dado que tenho 15 pontos disponiveis e o limitador e 20
  Quando distribuo 10 em FOR e 5 em AGI
  Entao o contador exibe "0 pontos restantes"
  E ao avancar, PUT /fichas/{id}/atributos e chamado com os valores

Cenario 2: Tentativa de exceder o limitador
  Dado que o limitador do nivel 1 e 20
  Quando tento colocar 21 pontos em FOR
  Entao o campo e bloqueado em 20 e exibe badge "Limite atingido"

Cenario 3: Tentativa de exceder os pontos totais
  Dado que tenho 15 pontos disponiveis
  Quando a soma de todos os atributos atingiria 16
  Entao o spinner do ultimo campo e desabilitado com mensagem "Sem pontos disponiveis"

---

**US-006-03: Mestre concede XP a uma ficha**
Como Mestre do jogo,
Quero conceder XP a uma ficha especifica,
Para que o personagem evolua e o nivel seja atualizado automaticamente.

Criterios de Aceite:

Cenario 1: Concessao bem-sucedida
  Dado que a ficha tem 10.000 XP e esta no nivel 4
  Quando envio PUT /fichas/{id}/xp com { "xp": 5000 }
  Entao o XP total passa para 15.000
  E o backend recalcula e retorna nivel 5 (se NivelConfig.xpNecessaria para nivel 5 e <= 15000)
  E `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis` e `pontosVantagemDisponiveis` sao recalculados

Cenario 2: Tentativa de Jogador conceder XP
  Dado que sou um Jogador autenticado
  Quando envio PUT /fichas/{id}/xp
  Entao recebo HTTP 403 Forbidden

---

## 13. Regras de Negocio Criticas do Dominio

**RN-001:** Ficha com status RASCUNHO nao pode ser usada em sessao de jogo. O endpoint de modo sessao deve verificar o status.

**RN-002:** Atributos sao inicializados como registros em `FichaAtributo` (um por AtributoConfig do jogo) no momento da criacao da ficha — todos com base=0, nivel=0, outros=0.

**RN-003:** Os bonus de raca (RacaBonusAtributo) vao para o campo `outros` do FichaAtributo — nao sao editaveis pelo Jogador.

**RN-004:** Nivel e sempre calculado pelo backend. Nunca aceitar nivel vindo do frontend como entrada definitiva.

**RN-005:** Gastos de pontos sao irreversiveis. Redistribuicao de atributos apos o nivel inicial requer decisao futura do PO (nao e requisito MVP).

**RN-006:** Insolitus, Titulo Heroico e Arquetipo sao concedidos pelo Mestre — nao estao no wizard e nao sao necessarios para a ficha ficar COMPLETA.

---

## 14. Pontos em Aberto

| ID | Questao | Impacto |
|----|---------|---------|
| PA-001 | Redistribuicao de atributos e possivel depois do nivel inicial? (ex: o Jogador errou a distribuicao) | Afeta regras de edicao pos-criacao |
| PA-002 | O Jogador pode apagar uma ficha RASCUNHO que criou (se desistiu)? | Afeta o DELETE /fichas/{id} |
| PA-003 | Multiplas fichas por jogador no mesmo jogo e permitido? PO confirmou que sim (morte frequente) | Confirmado: sem restricao |
| PA-004 | O wizard de criacao de NPC difere do wizard de ficha de jogador? | Rota diferente (/npcs), campo jogadorId ausente, badge NPC |

---

## 15. Checklist de Validacao UX

- [ ] Indicador de progresso do wizard (Passo X de 5) visivel em todas as etapas
- [ ] Auto-save: indicador visual de "Salvo automaticamente" apos cada passo
- [ ] Passo 2 (atributos): barra visual por atributo mostrando base vs limitador
- [ ] Passo 2: contador global de pontos em destaque (nao pode passar despercebido)
- [ ] Passo 4 (vantagens): search/filtro por categoria para facilitar selecao em jogos com muitas vantagens
- [ ] Passo 5 (revisao): exibir calculos finais (totais de atributos com bonus de raca inclusos)
- [ ] Notificacao de level up apos concessao de XP pelo Mestre (ex: toast ou modal)
- [ ] Badge "Incompleta" na listagem para fichas RASCUNHO

---

## 16. Dependencias

- **Depende de:** Spec 005 (Jogador precisa ser APROVADO para criar ficha)
- **Depende de:** Spec 007 (VantagemEfeito — motor de calculos deve estar funcional antes de FichaDetail ser considerado "pronto")
- **Bloqueia:** Spec 008 (dashboards, filtros), Spec 009 (NPC visibility)

---

*Produzido por: Business Analyst/PO | 2026-04-02 | Decisoes do PO incorporadas*
