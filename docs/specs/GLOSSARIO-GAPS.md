# GLOSSARIO-GAPS.md — Inventario de Conceitos do Dominio vs. Specs e Implementacao

> Produzido por: Business Analyst/PO | 2026-04-02
> Base: glossario completo (01 a 05), specs 005/006/007/009/010/011, analises BA e auditoria do codigo.
> Objetivo: mapear o que existe, o que falta e o que diverge, para guiar o planejamento das proximas specs.

---

## 1. Conceitos Implementados E Especificados

Lista rapida — detalhes nos documentos de referencia indicados.

| Conceito | Entidade(s) de codigo | Spec(s) | Analise(s) |
|---|---|---|---|
| Jogo (Campanha) | `Jogo`, `JogoService`, `JogoController` | (base) | — |
| Ficha de Personagem | `Ficha`, `FichaService`, `FichaController` | 006 | BA-FICHA |
| Ficha de NPC | `Ficha.isNpc`, endpoints `/npcs` | 009 | BA-FICHA |
| Participante / JogoParticipante | `JogoParticipante`, `JogoParticipanteController` | 005 | — |
| Atributo (AtributoConfig) | `AtributoConfig` CRUD completo | 004 | BA-ATRIBUTOS-APTIDOES |
| Ímpeto | `FichaAtributo.impeto`, `FichaCalculationService` | 007 | BA-ATRIBUTOS-APTIDOES |
| Limitador de Atributo | `NivelConfig.limitadorAtributo`, `FichaValidationService` | 006 | BA-NIVEIS-PROGRESSAO |
| Aptidão (AptidaoConfig) | `AptidaoConfig`, `FichaAptidao` CRUD completo | 004 | BA-ATRIBUTOS-APTIDOES |
| TipoAptidao | `TipoAptidao` CRUD completo | 004 | BA-ATRIBUTOS-APTIDOES |
| Bônus (BonusConfig) | `BonusConfig`, `FichaBonus` CRUD completo | 004 | BA-ATRIBUTOS-APTIDOES |
| Glória (campo de FichaBonus) | `FichaBonus.gloria` — campo persistido | 007 | BA-FICHA |
| Classe de Personagem | `ClassePersonagem`, `ClasseBonus`, `ClasseAptidaoBonus` | 004 | BA-CLASSES-RACAS |
| Raça | `Raca`, `RacaBonusAtributo`, `RacaClassePermitida` | 004 | BA-CLASSES-RACAS |
| Nível (NivelConfig) | `NivelConfig` CRUD completo | 004 | BA-NIVEIS-PROGRESSAO |
| XP / Progressão de Nível | `Ficha.xp`, `Ficha.nivel`, cálculo em `FichaService` | 006 | BA-NIVEIS-PROGRESSAO |
| Renascimento (campo) | `Ficha.renascimentos`, `NivelConfig.permitirRenascimento` | 006 | BA-NIVEIS-PROGRESSAO |
| Pontos de Vantagem | `PontosVantagemConfig` CRUD completo | 004 | BA-NIVEIS-PROGRESSAO |
| Vantagem (VantagemConfig) | `VantagemConfig`, `CategoriaVantagem`, pré-requisitos | 004 | BA-VANTAGEM-CONFIG |
| VantagemEfeito (8 tipos) | `VantagemEfeito` entidade + CRUD — mas NAO integrado ao FichaCalculationService | 007 | BA-VANTAGEM-CONFIG |
| FichaVantagem | `FichaVantagem`, nunca removivel | 006 | BA-VANTAGEM-CONFIG |
| Membro do Corpo (MembroCorpoConfig) | `MembroCorpoConfig`, `FichaVidaMembro` | 004 | — |
| Dado de Prospecção (DadoProspeccaoConfig) | `DadoProspeccaoConfig`, `FichaProspeccao` | 004 | — |
| Gênero (GeneroConfig) | `GeneroConfig` CRUD completo | (base) | BA-CONFIGURACOES-SIMPLES |
| Índole (IndoleConfig) | `IndoleConfig` CRUD completo | (base) | BA-CONFIGURACOES-SIMPLES |
| Presença (PresencaConfig) | `PresencaConfig` CRUD completo | (base) | BA-CONFIGURACOES-SIMPLES |
| Vida Total (FichaVida) | `FichaVida`, `FichaCalculationService.calcularVidaTotal` | 007 | BA-FICHA |
| VG / VT (componentes de vida) | `FichaVida.vt`, cálculo via `FichaCalculationService` | 007 | BA-FICHA |
| Membros do Corpo com dano | `FichaVidaMembro`, `calcularVidaMembro` | 007 | — |
| Essência | `FichaEssencia`, `calcularEssenciaTotal` | 007 | BA-FICHA |
| Ameaça | `FichaAmeaca`, `calcularAmeacaTotal` | 007 | BA-FICHA |
| Prospecção (dado na ficha) | `FichaProspeccao` com DADO_UP | 007 | — |
| Anotações da Ficha | `FichaAnotacao`, `FichaAnotacaoController` | 011 | BA-FICHA |
| Descrição Física | `FichaDescricaoFisica` — entidade e repositório, sem endpoint dedicado | 011 (parcial) | BA-FICHA |
| Siglas/Abreviações cross-entity | `SiglaValidationService`, validação por jogo | 004 | — |
| Motor de Fórmulas | `FormulaEvaluatorService` (exp4j) | 004 | — |
| Template Klayrah Padrão | `GameConfigInitializerService` — popula 13 configs ao criar Jogo | (base) | — |
| Duplicação de Ficha | `POST /fichas/{id}/duplicar`, `FichaService.duplicarFicha` | 009 | BA-FICHA |
| Duplicação de Jogo | `JogoDuplicacaoService` | (base) | — |
| Export/Import de configs | `ConfigExportImportService`, endpoints em JogoController | (base) | — |
| Dashboard do Mestre | `DashboardService`, `DashboardController` — estatísticas básicas | 008 (parcial) | — |
| Roles (MESTRE, JOGADOR) | `@PreAuthorize`, `SecurityConfig` | 010 | — |
| Soft Delete / Auditoria | `BaseEntity.deletedAt`, `@SQLRestriction` | (base) | — |

---

## 2. Conceitos Especificados Mas NAO (Completamente) Implementados

Estes conceitos têm spec escrita mas o código não está completo.

### 2.1 Status da Ficha (RASCUNHO / COMPLETA) — Spec 006

**O que a spec diz:** A ficha deve ter um enum `FichaStatus` com os valores `RASCUNHO`, `COMPLETA` e `ARQUIVADA`. O wizard auto-salva como RASCUNHO; o endpoint `PUT /fichas/{id}/completar` transiciona para COMPLETA.

**O que o código tem:** A entidade `Ficha.java` não possui o campo `status`. Não existe enum `FichaStatus`. Não existe endpoint `PUT /fichas/{id}/completar`. A ficha é criada diretamente como se fosse completa.

**Impacto:** Fichas incompletas (wizard abandonado) não podem ser diferenciadas de fichas completas. O sistema não pode impedir uso de ficha incompleta em sessão.

**Spec de referência:** Spec 006, seção 3 (Estados da Ficha) e RF-003/RF-005/RF-006.

---

### 2.2 Concessão de XP (endpoint dedicado) — Spec 006

**O que a spec diz:** `PUT /api/v1/fichas/{id}/xp` com `ConcessaoXpRequest { xp, motivo }`. XP é acumulativa, recalcula nível automaticamente, retorna `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis`, `pontosVantagemDisponiveis`.

**O que o código tem:** `Ficha.xp` existe como campo. O `PUT /api/v1/fichas/{id}` aceita `UpdateFichaRequest` que inclui `xp` e `renascimentos` — mas sem controle de role separado (qualquer UPDATE pode alterar XP), e sem os pontos disponíveis na resposta.

**Impacto:** Jogador pode, tecnicamente, alterar o próprio XP via PUT geral. `FichaResumoResponse` não inclui `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis`, `pontosVantagemDisponiveis` (campos ausentes do record).

**Spec de referência:** Spec 006, seção 7 (Campo XP — Somente Mestre) e seção 8 (FichaResumoResponse).

---

### 2.3 Pontos Disponíveis no FichaResumoResponse — Spec 006

**O que a spec diz:** `FichaResumoResponse` deve incluir `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis`, `pontosVantagemDisponiveis`.

**O que o código tem:** O record atual tem apenas `id`, `nome`, `nivel`, `xp`, `racaNome`, `classeNome`, `atributosTotais`, `bonusTotais`, `vidaTotal`, `essenciaTotal`, `ameacaTotal`. Os campos de pontos disponíveis estão ausentes.

**Impacto:** O frontend não consegue exibir quantos pontos o jogador ainda pode distribuir sem fazer cálculos no cliente, o que é explicitamente proibido (backend é fonte oficial).

---

### 2.4 Integração do VantagemEfeito ao FichaCalculationService — Spec 007

**O que a spec diz:** Os 8 tipos de efeito de `VantagemEfeito` devem ser processados pelo `FichaCalculationService` ao calcular a ficha.

**O que o código tem:** `VantagemEfeito` existe como entidade com CRUD. O `FichaCalculationService` existe, mas os comentários na spec indicam que os efeitos NÃO são consumidos — `VT` está hardcoded como 0.

**Impacto:** Todas as fichas com vantagens de bônus têm valores matematicamente errados. Saúde de Ferro não adiciona VT. TCO não adiciona ao B.B.A via efeito.

---

### 2.5 Endpoint de Descrição Física — Spec 011 (implícito)

**O que o código tem:** A entidade `FichaDescricaoFisica` existe com campos `altura`, `peso`, `idade`, `descricaoOlhos`, `descricaoCabelos`, `descricaoPele`. Há repositório e referências nos testes. A ficha é criada com uma `FichaDescricaoFisica` em branco.

**O que falta:** Nenhum endpoint dedicado para ler ou atualizar `FichaDescricaoFisica`. Não há `GET /fichas/{id}/descricao-fisica` nem `PUT /fichas/{id}/descricao-fisica`. Os campos `ValidationMessages.Ficha.TITULO_TAMANHO`, `INSOLITUS_TAMANHO`, `ORIGEM_TAMANHO` e `ARQUETIPO_TAMANHO` existem nas constantes mas não há campos correspondentes na entidade `Ficha.java`.

---

### 2.6 Galeria de Imagens — Spec 011

**O que a spec diz:** Avatar principal e galeria secundária referenciados por URL. Campo `imagemUrl` na ficha.

**O que o código tem:** Nada. A spec 011 confirma explicitamente: "Nenhuma entidade, service ou controller de imagem existe no backend."

---

### 2.7 Insolitus como Entidade Configurável — Spec 007

**O que a spec diz:** `InsolitusCo` como variação de `VantagemConfig` (Opção A: campo discriminador `tipoVantagem: VANTAGEM | INSOLITUS`). Endpoints `POST/GET/DELETE /fichas/{id}/insolitus`.

**O que o código tem:** `ValidationMessages.Ficha.INSOLITUS_TAMANHO = "Insolitus deve ter no máximo 200 caracteres"` e `FICHA_INSOLITUS_MAX = 200` — sugerem que em algum momento houve uma abordagem de campo texto livre na ficha (não entidade configurável). Não existe campo `insolitus` na entidade `Ficha` atual, nem entidade `InsolitusCo`, nem endpoints relacionados.

---

## 3. Conceitos do Glossario SEM Spec E SEM Implementacao

Estes conceitos aparecem no glossário mas não têm spec escrita nem código correspondente.

---

### GAP-DOMAIN-01: Renascimento como Mecânica (não apenas campo)

**O que o glossário diz:** "Ciclo de transcendência a partir do nível 31. Ao renascer, o personagem ganha poderes exclusivos e bônus em vida, essência e ameaça. Representa transcender a mortalidade. O personagem volta ao nível 1 mas mantém bônus permanentes."

**O que existe:** `Ficha.renascimentos` (contador Integer), `NivelConfig.permitirRenascimento` (flag), `FichaAmeaca.renascimentos` e `FichaEssencia.renascimentos` (componentes de cálculo). O contador incrementa como parte de `UpdateFichaRequest`.

**O que falta:** Nenhum endpoint dedicado para executar o renascimento. Nenhuma lógica de: (1) verificar se `NivelConfig.permitirRenascimento = true` para o nível atual; (2) resetar o nível para 1; (3) preservar vantagens e bônus de renascimentos anteriores; (4) incrementar `Ficha.renascimentos`; (5) conceder acesso a vantagens da categoria "Vantagem de Renascimento".

**Por que é importante (impacto no MVP):** Renascimento é uma mecânica de fim de jogo (níveis 31-35). Personagens de campanha longa não conseguem progredir além do nível 30 sem essa funcionalidade. O glossário define explicitamente que é uma das mecânicas mais impactantes do Klayrah.

**Sugestão:** Incluir em spec nova `012-renascimento-xp-lote`, junto com a concessão de XP em lote. Ou incluir no escopo expandido da Spec 006 (campo XP).

**Prioridade: pós-MVP** — relevante apenas quando personagens atingirem nível 31.

---

### GAP-DOMAIN-02: Título Heróico

**O que o glossário diz:** "Alcunha ou título conquistado pelo personagem através de feitos no jogo. Exemplos: 'O Matador de Dragões', 'A Sombra de Eldoria'."

**Contribui para o cálculo de Ameaça:** `FichaAmeaca.titulos` (campo Integer) já existe e está no cálculo — mas `titulos` é um contador numérico, não uma lista de títulos textuais.

**O que existe:** `ValidationMessages.Ficha.TITULO_TAMANHO = "Título heroico deve ter no máximo 200 caracteres"` — sugere que houve intenção de campo texto na ficha, mas o campo não existe na entidade `Ficha.java`. `FichaAmeaca.titulos` armazena um integer (quantidade/bônus de ameaça de títulos), não os títulos em si.

**O que falta:** Não está claro se o design intende títulos como: (a) lista de strings textuais (narrativo), (b) campo texto livre na ficha, ou (c) entidade configurável similar a vantagem. A constante de validação (`TITULO_TAMANHO = 200`) sugere (b) — campo texto.

**Por que é importante:** Títulos contribuem para a Ameaça (via `FichaAmeaca.titulos`). Sem modelo claro de títulos, não é possível calcular o bônus de ameaça por titulo especifico.

**Sugestão:** Incluir como campo texto livre na ficha (`tituloHeroico: String`) em Spec 006 (expansão dos campos da ficha). Mestre concede via `PUT /fichas/{id}`.

**Prioridade: MVP** — impacta o cálculo de Ameaça (que já existe como valor calculado).

**Pergunta para PO:** `FichaAmeaca.titulos` é um counter manual (Mestre digita número) ou deve ser calculado automaticamente pela quantidade de títulos concedidos?

---

### GAP-DOMAIN-03: Origem do Personagem

**O que o glossário diz:** "De onde o personagem veio, sua terra natal ou background."

**O que existe:** `ValidationMessages.Ficha.ORIGEM_TAMANHO = "Origem deve ter no máximo 200 caracteres"` — constante de validação existe, mas o campo não existe na entidade `Ficha.java`.

**O que falta:** Campo `origem` na ficha ou tabela separada `FichaIdentidade`.

**Por que é importante:** Campo narrativo — sem impacto mecânico. Complementa a identidade do personagem junto a Arquétipo, Insolitus e Título Heróico.

**Sugestão:** Adicionar como campo texto na ficha junto com os outros campos de identidade (Titulo Heróico, Insolitus, Arquétipo) em uma expansão de Spec 006 ou spec nova de "campos de identidade".

**Prioridade: nice-to-have** — sem impacto mecânico.

---

### GAP-DOMAIN-04: Arquétipo de Referência

**O que o glossário diz:** "Referência fictícia ou real que inspira a interpretação do personagem. Exemplos: 'Aragorn de Senhor dos Anéis', 'Geralt de Rívia'."

**O que existe:** `ValidationMessages.Ficha.ARQUETIPO_TAMANHO = "Arquétipo de referência deve ter no máximo 200 caracteres"` e `FICHA_ARQUETIPO_MAX = 200` — constante existe, campo ausente na entidade.

**O que falta:** Campo `arquetipoReferencia` na ficha.

**Por que é importante:** Campo puramente narrativo — sem impacto mecânico. Útil para roleplay e narrativa.

**Sugestão:** Agrupar com Origem e Título Heróico em expansão de campos de identidade da Spec 006.

**Prioridade: nice-to-have** — sem impacto mecânico.

---

### GAP-DOMAIN-05: FichaDescricaoFisica — Endpoints

**O que o glossário diz:** Implicitamente, a descrição física (altura, peso, idade, aparência) faz parte da identidade do personagem. O campo `gênero` é mencionado como influenciando o cálculo de BMI/peso.

**O que existe:** Entidade `FichaDescricaoFisica` com campos `altura`, `peso`, `idade`, `descricaoOlhos`, `descricaoCabelos`, `descricaoPele`. Criada em branco junto com a ficha. Repositório e testes de limpeza existem.

**O que falta:**
- `GET /api/v1/fichas/{id}/descricao-fisica` — para o frontend exibir os dados
- `PUT /api/v1/fichas/{id}/descricao-fisica` — para edição

**Por que é importante:** A entidade existe mas é inacessível via API. Os dados são criados mas não podem ser lidos nem editados. É uma feature incompleta, não um gap de design.

**Sugestão:** Incluir estes dois endpoints no escopo da Spec 006 (campos da ficha) ou da Spec 011 (galeria/anotações).

**Prioridade: MVP** — entidade existe, faltam apenas 2 endpoints simples (GET + PUT).

---

### GAP-DOMAIN-06: Modo Sessão

**O que o glossário diz (implícito na Spec 006):** "Fichas com status RASCUNHO não podem entrar em sessão de jogo." — mas o conceito de "modo sessão" não está definido nem no glossário detalhado nem em nenhuma spec.

**O que o glossário menciona:** A fórmula de Ameaça inclui `ITENS` — o que pressupõe um sistema de inventário/itens que não existe. A fórmula de Vida menciona "estado de combate" (vida atual vs vida total).

**O que existe:** `FichaVida.vidaAtual` e `FichaEssencia.essenciaAtual` — campos de estado em combate, atualizados via `PUT /fichas/{id}/vida`. Endpoints `PUT /fichas/{id}/vida` e `PUT /fichas/{id}/prospeccao` existem.

**O que não existe:** Nenhuma definição formal de "sessão de jogo" como entidade ou estado. Não existe `Sessao`, `ModoSessao`, `SessaoJogo` nem qualquer conceito de "ficha está em sessão vs não está em sessão".

**Por que é importante:** Se o sistema vai bloquear fichas RASCUNHO de entrar em sessão (Spec 006), precisa existir uma definição do que é "sessão". Caso contrário, a restrição é inoperante.

**Sugestão:** Criar spec nova `012-modo-sessao` que define: o que é uma sessão, como a ficha entra em modo de sessão, como vida/essência atuais são gerenciadas, e como o status RASCUNHO bloqueia essa entrada.

**Prioridade: pós-MVP** — o sistema funciona sem sessão formal; a restrição de RASCUNHO pode ser uma validação simples no frontend por enquanto.

---

### GAP-DOMAIN-07: Sistema de Itens (Equipamentos)

**O que o glossário diz:** `FichaAmeaca` tem o componente `ITENS` (nível de ameaça vindo de equipamentos). A fórmula de Bônus menciona "Itens" como uma das 5 fontes de `FichaBonus` (base + vantagens + classe + **itens** + glória + outros).

**O que existe:** `FichaAmeaca.itens` (Integer) e `FichaBonus.itens` (Integer) — campos numéricos que representam o bônus de itens como um número direto, sem modelagem de itens individuais.

**O que falta:** Nenhuma entidade `Item`, `Equipamento`, `InventarioFicha` ou similar. Os campos `itens` são preenchidos manualmente pelo Mestre como um número.

**Por que é importante:** Itens são uma fonte de bônus para Ameaça e para Bônus de Combate. Sem modelagem de itens, esses valores são inseridos manualmente — o que é uma decisão de design válida para MVP mas diverge de um RPG completo.

**Sugestão:** Manter como campo numérico manual (Mestre define o valor total de bônus de itens). Documentar explicitamente que "inventário e sistema de itens" é fora do escopo do sistema. Criar spec futura se necessário.

**Prioridade: fora do escopo MVP** — abordagem de campo manual é a decisão correta para MVP.

---

### GAP-DOMAIN-08: Glória como Mecânica

**O que o glossário diz:** "Glória — Fonte de bônus vinda de conquistas e feitos heróicos no jogo. Exemplos: +2 Glória em B.B.A por matar um dragão."

**O que existe:** `FichaBonus.gloria` (Integer) — campo numérico persistido e incluído no cálculo de `FichaBonus.total = base + vantagens + classe + itens + gloria + outros`.

**O que falta:** Nenhum endpoint específico para conceder Glória. Nenhum histórico de conquistas. O valor é atualizável via o endpoint geral de PUT ficha/bonus indiretamente, mas não há um `PUT /fichas/{id}/gloria` dedicado com controle de role.

**Por que é importante:** Sem endpoint dedicado, o Mestre não tem como conceder Glória de forma controlada (com auditoria, com role check). Fica dependente de uma atualização manual do campo genérico.

**Sugestão:** Incluir endpoint `PUT /fichas/{id}/gloria` no escopo da Spec 006 expandida, com semântica análoga à concessão de XP (MESTRE only, acumulativo).

**Prioridade: MVP** — impacto mecânico real (afeta cálculo de Bônus).

---

### GAP-DOMAIN-09: Renascimento e Vantagens de Renascimento

**O que o glossário diz:** A categoria "Vantagem de Renascimento" existe no template Klayrah com vantagens exclusivas para quem renasceu (Último Sigilo, Pensamento Bifurcado). O pré-requisito "1 Renascimento" deve ser verificável.

**O que existe:** `VantagemPreRequisito` com detecção de ciclos DFS. Mas os tipos de pré-requisito suportados não estão completamente mapeados — não está claro se "1 Renascimento" é um tipo de pré-requisito suportado na entidade.

**O que falta:** Verificação de `Ficha.renascimentos >= N` como tipo de pré-requisito de vantagem.

**Sugestão:** Incluir `MINIMO_RENASCIMENTOS` como tipo de pré-requisito em `VantagemPreRequisito` — parte do escopo da Spec 007 (motor de cálculos).

**Prioridade: pós-MVP** — relevante apenas após a mecânica de renascimento estar implementada.

---

### GAP-DOMAIN-10: XP em Lote (para toda a mesa)

**O que o glossário diz (implícito):** O Mestre concede XP a todos os personagens da mesa após uma sessão — é um caso de uso natural que o BA menciona em Spec 006 (`PUT /jogos/{id}/fichas/xp-lote`).

**O que existe:** Apenas `PUT /fichas/{id}/xp` (não implementado ainda — ver GAP 2.2). Nenhum endpoint de lote.

**Sugestão:** Incluir `PUT /api/v1/jogos/{jogoId}/fichas/xp-lote` em spec nova ou na Spec 006 expandida.

**Prioridade: pós-MVP** — o endpoint individual é suficiente para MVP.

---

### GAP-DOMAIN-11: Concessão de Renascimento (endpoint dedicado)

**O que falta:** Endpoint `PUT /fichas/{id}/renascimento` com:
- Validação: `NivelConfig.permitirRenascimento = true` para nível atual
- Ação: `Ficha.renascimentos++`, `Ficha.nivel = 1`, `Ficha.xp = 0`
- Regra: vantagens e bônus de renascimentos anteriores são mantidos
- Role: MESTRE only (o Mestre autoriza o renascimento)

**Sugestão:** Spec nova `012-renascimento`.

**Prioridade: pós-MVP.**

---

## 4. Inconsistencias Entre Glossario e Codigo

### INC-01: FichaVida não mapeia a formula do glossário

**Glossário diz:** `Vida Total = Vigor Total (VG) + Nível + VT + Renascimentos + Outros`

**Código tem:** `FichaVida` com campos `vt`, `outros`, `vidaTotal`, `vidaAtual`. O `FichaCalculationService.calcularVidaTotal` faz: `vigorTotal + nivelFicha + vt + renascimentos + outros` — **o campo `renascimentos` não existe em `FichaVida`**. O `renascimentos` é lido diretamente de `Ficha.renascimentos`, o que está correto. Mas o glossário define VG (Vigor Total) como componente separado, enquanto o código usa `vigorTotal` como variável local calculada. Não é uma inconsistência crítica, mas o modelo de dados não expõe VG explicitamente.

**Impacto:** Menor — o cálculo está correto, mas o frontend não consegue exibir "VG = X" separadamente sem recalcular no cliente.

**Recomendação:** Incluir `vg` (vigor total, componente de vida) no `FichaResumoResponse` ou em um response expandido de vida.

---

### INC-02: FichaEssencia não inclui VIG e SAB na formula

**Glossário diz:** `Essência Total = FLOOR((VIG + SAB) / 2) + Nível + Renascimentos + Vantagens + Outros`

**Código tem:** `FichaEssencia` com `renascimentos`, `vantagens`, `outros`, `total`, `essenciaAtual`. O `FichaCalculationService.calcularEssenciaTotal` recebe `vigorTotal` e `sabedoriaTotal` como parâmetros e calcula corretamente. No entanto, `FichaEssencia` não armazena o componente base (FLOOR((VIG+SAB)/2)) separadamente. O `essenciaTotal` é `renascimentos + vantagens + outros`, e o componente base é calculado on-the-fly.

**Impacto:** O frontend não consegue exibir a decomposição da essência (base = X, vantagens = Y, etc.) sem uma resposta mais detalhada.

**Recomendação:** Incluir `baseEssencia` no response ou no objeto `FichaEssencia`.

---

### INC-03: Campos de Identidade em ValidationMessages sem campos na entidade Ficha

**O que o código tem:** `ValidationMessages.Ficha` define constantes para `TITULO_TAMANHO`, `INSOLITUS_TAMANHO`, `ORIGEM_TAMANHO`, `ARQUETIPO_TAMANHO` — mas a entidade `Ficha.java` não tem campos `tituloHeroico`, `insolitus`, `origem`, `arquetipoReferencia`.

**Hipótese:** Esses campos foram planejados em alguma versão anterior e as constantes ficaram como artefato. A decisão do PO (Spec 006, seção 9) é: Insolitus = entidade configurável concedida pelo Mestre; Título Heróico e Arquétipo = campos texto livres concedidos pelo Mestre.

**Recomendação:** Implementar `tituloHeroico` e `arquetipoReferencia` como campos String na entidade `Ficha`. `Insolitus` como entidade configurável (Spec 007). Remover ou adaptar as constantes de validação ao escopo correto.

---

### INC-04: FichaAmeaca.titulos é um Integer, não uma lista de títulos

**Glossário diz:** Título Heróico é uma alcunha textual conquistada. A Ameaça inclui bônus de títulos.

**Código tem:** `FichaAmeaca.titulos` = Integer (valor de bônus de ameaça vindo de títulos).

**Interpretação correta:** O `titulos` em `FichaAmeaca` representa o bônus numérico de ameaça derivado de títulos, não os títulos em si. Os títulos textuais seriam armazenados em `Ficha.tituloHeroico` (campo texto livre). O Mestre atualiza manualmente o `FichaAmeaca.titulos` como um número que reflete quantos/quais títulos contribuem para a ameaça.

**Status:** Design válido para MVP, mas precisa ser documentado explicitamente para evitar confusão.

---

### INC-05: Modo Sessao mencionado como restricao mas nao definido como entidade

**Spec 006 RN-001 diz:** "Ficha com status RASCUNHO não pode ser usada em sessão de jogo. O endpoint de modo sessão deve verificar o status."

**Código tem:** Nenhuma entidade ou conceito de "sessão de jogo". `FichaVida.vidaAtual` e `FichaEssencia.essenciaAtual` são os estados de combate.

**Inconsistência:** A spec referencia um "endpoint de modo sessão" que não existe e não está especificado. A restrição é inoperante até que o campo `FichaStatus` e o conceito de sessão sejam implementados.

---

### INC-06: Formula de Vida Total no codigo diverge do glossario

**Glossário diz (04-siglas-formulas.md):** `Vida total = VIGOR + NIVEL + VANTAGENS + RENASCIMENTOS + OUTROS`

**Codigo confirma:** `calcularVidaTotal = vigorTotal + nivelFicha + vt + renascimentos + outros` — onde `vt` = "Vida de Vantagens" (bônus de vantagens). O campo é `vt` (abreviação) em vez de `vantagens` (nome explícito), mas o cálculo está alinhado.

**Status:** Não é uma inconsistência funcional, apenas nomenclatura. Documentar que `FichaVida.vt` == "VANTAGENS" na fórmula do glossário.

---

## 5. Recomendacao de Specs a Criar — Lista Priorizada

### PRIORIDADE 1 — Bloqueia MVP ou tem campo/entidade já pronta mas sem endpoint

| # | Spec Recomendada | Conceito(s) cobertos | Depende de |
|---|---|---|---|
| 1 | **Spec 006 expandida — Campos de Identidade** | `tituloHeroico`, `arquetipoReferencia`, `origem` na ficha; endpoints para `FichaDescricaoFisica` | Spec 006 base |
| 2 | **Spec 006 expandida — XP Controlado + FichaStatus** | Endpoint `PUT /fichas/{id}/xp` MESTRE-only; enum `FichaStatus`; `PUT /fichas/{id}/completar`; pontos disponíveis no `FichaResumoResponse` | Spec 006 base, NivelConfig |
| 3 | **Spec 006 expandida — Glória** | Endpoint `PUT /fichas/{id}/gloria` MESTRE-only para conceder bônus de conquistas | Spec 006 base |
| 4 | **Spec 007 implementação** | Integrar VantagemEfeito ao FichaCalculationService (8 tipos); validar que VT, Essência, B.B.A etc. são calculados com efeitos | Spec 007 spec escrita |

### PRIORIDADE 2 — Mecânicas do Domínio Relevantes para Campanhas Longas

| # | Spec Recomendada | Conceito(s) cobertos | Depende de |
|---|---|---|---|
| 5 | **Spec 012 — Renascimento** | Mecânica completa de renascimento (verificação, reset de nível, manutenção de vantagens); endpoint `PUT /fichas/{id}/renascimento`; tipo de pré-requisito `MINIMO_RENASCIMENTOS` | Spec 006, 007 |
| 6 | **Spec 013 — Concessão em Lote** | `PUT /jogos/{id}/fichas/xp-lote`; opcionalmente `PUT /jogos/{id}/fichas/xp-sessao` para concessão por sessão com motivo | Spec 012 |

### PRIORIDADE 3 — Nice-to-Have / Fora de MVP

| # | Spec Recomendada | Conceito(s) cobertos | Nota |
|---|---|---|---|
| 7 | **Spec 014 — Modo Sessão** | Definição formal de SessaoJogo; estado "em sessão" da ficha; restricao de RASCUNHO; gestão de vida/essência atual em sessão | Complexidade alta |
| 8 | **Futuro — Itens/Equipamentos** | Entidade `Item`, `InventarioFicha`; bônus de itens calculados automaticamente | Escopo grande, fora de MVP |

---

## 6. Conceitos Explicitamente Fora do Escopo (por decisao do PO ou design)

| Conceito | Razão |
|---|---|
| Upload de imagens (binário/S3) | Spec 011 decidiu MVP com URLs externas apenas |
| Histórico de transições de participação | Spec 005 RN-006: Envers descartado para participantes |
| Cooldown entre REJEITADO → PENDENTE | Spec 005: sem cooldown (decisão PO 2026-04-02) |
| Redistribuição de atributos após nível inicial | Spec 006 PA-001: ponto em aberto, não é MVP |
| Sistema de inventário/itens com entidade própria | FichaAmeaca.itens e FichaBonus.itens são valores manuais |
| Notificações (email/push) ao jogador | Spec 005 PA-004: não é MVP |
| Transferência de propriedade do Jogo | Spec 005 PA-002: não é MVP |

---

## 7. Glossario vs. Codigo — Tabela Resumo Final

| Conceito do Glossário | Status | Lacuna Principal |
|---|---|---|
| Ficha / NPC | Implementado | FichaStatus ausente |
| Atributos / Ímpeto | Implementado | — |
| Limitador | Implementado | — |
| Bônus (calculado) | Implementado | Glória sem endpoint dedicado |
| Vida / VG / VT / Membros | Implementado | VG não exposto no response |
| Essência | Implementado | Componente base não exposto |
| Ameaça | Implementado | Títulos = contador manual (ok por ora) |
| Prospecção / DADO_UP | Implementado | — |
| Progressão / XP / Nível | Parcial | XP sem endpoint dedicado MESTRE-only; pontos disponíveis ausentes do resumo |
| Renascimento (mecânica) | Parcial | Apenas campo contador; sem endpoint/lógica de execução |
| Título Heróico | Parcial | Constante existe; campo na entidade ausente |
| Origem | Parcial | Constante existe; campo na entidade ausente |
| Arquétipo de Referência | Parcial | Constante existe; campo na entidade ausente |
| Insolitus | Especificado (Spec 007) | Entidade não implementada; pendente decisão A vs B |
| Descrição Física | Parcial | Entidade existe; endpoints GET/PUT ausentes |
| Galeria de Imagens | Sem spec impl. | Nada implementado |
| Modo Sessão | Sem spec | Conceito indefinido no domínio |
| Sistema de Itens | Fora de MVP | Campos manuais são suficientes |
| Glória (mecânica) | Parcial | Campo existe; endpoint dedicado ausente |

---

*Produzido por: Business Analyst/PO — ficha-controlador | 2026-04-02*
*Revisao: auditoria completa de glossario + specs + grep no codigo*
