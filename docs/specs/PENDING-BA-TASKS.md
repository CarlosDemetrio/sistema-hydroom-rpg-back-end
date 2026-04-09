# Tasks BA Pendentes (pré-spec)

Briefing estruturado para Business Analysts. Estas três frentes precisam de trabalho de análise/decisão antes de virarem specs técnicas executáveis. Criado em 2026-04-08 pelo PO.

**Ordem sugerida de execução:** BA-01 → BA-03 → BA-02 (BA-02 é a maior e depende de BA-01 estar validado para saber o que já existe no catálogo default).

---

## BA-01 — Revisão da massa do GameDefaultProvider

**Spec number sugerida:** SPEC-020-revisao-game-default
**Prioridade:** P0 (bloqueia qualquer ajuste futuro no provider)
**Dependências:** nenhuma
**Esforço estimado BA:** 1-2 sessões

### Escopo
O PO preencheu CSVs com a massa de dados padrão do sistema Klayrah (raças, vantagens, atributos, pontos, etc.) que alimentará o `GameDefaultProvider` ao criar um novo Jogo. Um BA precisa auditar, corrigir e validar essa massa antes de virar código.

### O que o BA deve investigar/decidir
- Ler todos os CSVs em `docs/revisao-game-default/` e os markdowns de revisão (`configs 00-17`).
- Conferir consistência matemática:
  - Somas de pontos de atributo por nível batem com `NivelConfig.pontosAtributo`?
  - Vantagens raciais somam corretamente nos bônus de atributo das raças?
  - Custos de vantagens (fórmulas) produzem valores coerentes com a economia de pontos?
  - Fórmulas de Ímpeto por atributo usam apenas siglas válidas e únicas (regra crítica: sigla única por jogo, cross-entity)?
- Validar balanceamento entre raças: nenhuma raça deve ser obviamente superior/inferior. Produzir tabela comparativa.
- Conferir se todas as 6 raças Klayrah têm: nome, descrição, bônus de atributos, vantagens raciais, classes permitidas.
- Verificar categorias de vantagens e se cada vantagem está na categoria correta.
- Checar se há lacunas (ex: configs 00-17 mencionadas mas CSV ausente ou vazio).
- Decidir valores faltantes consultando o PO quando necessário (registrar perguntas em dossiê).

### Artefatos de entrada
- `docs/revisao-game-default/` — CSVs preenchidos pelo PO + markdowns de revisão das 18 configs
- `docs/glossario/02-configuracoes-jogo.md` — definição das 13+1 configurações
- `docs/glossario/04-siglas-formulas.md` — regras de siglas/fórmulas
- Código atual: `src/main/java/.../service/GameConfigInitializerService.java` e `GameDefaultProvider` (valores hoje usados)

### Artefato de saída
- **Dossiê de auditoria** em `docs/specs/SPEC-020-revisao-game-default/dossier-auditoria.md`:
  - Erros encontrados (lista numerada com CSV/linha e correção proposta)
  - Tabela comparativa de balanceamento entre raças
  - Lista de perguntas ao PO (se houver)
  - Decisões tomadas durante a revisão
- **CSVs corrigidos** (versionados em `docs/specs/SPEC-020-revisao-game-default/csv-final/`)
- **spec.md + plan.md + tasks/INDEX.md** descrevendo a task técnica: "migrar CSVs validados para código do GameDefaultProvider" (implementação é trivial após auditoria — provavelmente 1-3 tasks de backend)

### Critérios de aceitação da fase BA
- Todos os CSVs têm status "validado" ou "pendente PO" explícito
- Zero inconsistência matemática nos valores validados
- PO aprovou o dossiê antes da spec virar implementável

---

## BA-02 — Sistema de Habilidades (Skills)

**Spec number sugerida:** SPEC-021-sistema-habilidades
**Prioridade:** P1 (feature fundamental esquecida no backlog inicial)
**Dependências:** BA-01 concluído (para saber quais habilidades já vêm no catálogo default); Spec 007 (VantagemEfeito) e Spec 016 (Itens/Equipamentos) como referências estruturais
**Esforço estimado BA:** 2-3 sessões (feature grande, toca modelo, backend, UX)

### Escopo
Habilidades são ações/poderes que um personagem pode executar durante o jogo (ex: "Bola de Fogo", "Ataque Giratório", "Cura Menor"). Elas têm custo, efeito, duração, dados de dano. Hoje o sistema tem Vantagens (passivas/buffs) e Insólitus (poderes especiais), mas não tem um catálogo estruturado de "habilidades ativas" configurável pelo Mestre. Esta é uma lacuna fundamental do MVP.

### O que o BA deve investigar/decidir

#### Modelo conceitual
- Definir formalmente o que é uma Habilidade no domínio Klayrah (diferenciar de Vantagem, Insólitus, Efeito de Item).
- Tipos de habilidade a cobrir:
  - Concedida por Insólitus (faz parte de pacote)
  - Concedida por Vantagem
  - Exclusiva de Classe
  - Exclusiva de Raça
  - Concedida por Arma/Equipamento (ex: "Golpe Poderoso" que só existe enquanto arma X estiver equipada)
  - Criada ad-hoc pelo Mestre (campanha-específica)
- Atributos da habilidade:
  - nome, descrição, sigla (unicidade cross-entity?)
  - custo (essência? pontos de ação? usos/dia? a decidir)
  - alcance / área de efeito
  - dados de dano (múltiplos? tipo de dado, quantidade, modificador)
  - é buff? se sim, duração (turnos, rodadas, cenas)
  - bônus aplicados enquanto ativa
  - requisitos (atributo mínimo, nível mínimo, classe, raça)
  - fórmula de efeito (reuso do FormulaEvaluatorService?)

#### Integração com sistemas existentes
- Como Habilidade se relaciona com `VantagemEfeito` (Spec 007)? São exclusivas ou Habilidade pode ter efeitos reutilizando a mesma estrutura?
- Como se relaciona com `FichaItem`/Equipamentos (Spec 016)? Quando equipar uma arma que concede habilidade, ela aparece no painel de habilidades ativas da ficha.
- Integração com Insólitus: hoje Insólitus é `TipoVantagem=INSOLITUS`. Insólitus deve ter lista de habilidades concedidas?
- Ficha: nova aba "Habilidades" ou integrada em outra?

#### Backend
- Entidades novas: `HabilidadeConfig`, `HabilidadeEfeito`?, `FichaHabilidade`?
- Endpoints CRUD configuração + endpoints de ativação (gastar recursos, aplicar buff temporário)
- Como persistir buffs ativos (com duração)? Nova tabela `FichaBuffAtivo`?

#### UX (frontend)
- Tela de catálogo do Mestre: listagem + busca por tipo/classe/raça/arma
- **Busca facilitada para Mestre encontrar habilidades e armas** (requisito explícito do PO): filtro por nome, tipo, origem (classe/raça/vantagem/item)
- Na ficha (Jogador): aba de habilidades disponíveis + botão "usar"
- Painel do Mestre em sessão: visualização rápida das habilidades do NPC e dos jogadores
- Wizard de criação de habilidade: UX guiada (tipo → efeito → custo → dano)

### Artefatos de entrada
- `docs/specs/SPEC-007-vantagens-efeitos/` — modelo de VantagemEfeito
- `docs/specs/SPEC-016-sistema-itens/` — modelo de equipamentos
- `docs/glossario/03-termos-dominio.md` — termos do domínio
- Dossiê BA-01 — catálogo default de vantagens/raças/classes (para identificar quais já descrevem "habilidades" implicitamente)
- Sessão de entrevista com PO para responder perguntas pendentes

### Artefato de saída
- **Dossiê de decisões** `docs/specs/SPEC-021-sistema-habilidades/dossier-decisoes.md` — perguntas ao PO, respostas, decisões de modelagem
- **spec.md** — descrição completa da feature (domínio, fluxos, regras de negócio)
- **plan.md** — plano técnico (entidades, endpoints, telas, fases)
- **tasks/INDEX.md** + tasks individuais (estimar 15-25 tasks entre backend e frontend, divididas em fases)
- **Mockups de UX** (baixa fidelidade ou descrição textual) para as telas principais: catálogo, wizard de criação, aba da ficha, busca

### Critérios de aceitação da fase BA
- PO aprovou o modelo conceitual (o que é Habilidade vs Vantagem vs Efeito de Item)
- Todas as perguntas do dossiê respondidas
- Integração com Specs 007 e 016 definida sem retrabalho
- Spec pronta para um tech-lead estimar e quebrar em sprints

---

## BA-03 — Categorização e tags de NPCs

**Spec number sugerida:** SPEC-022-npc-categorizacao
**Prioridade:** P2 (quality-of-life do Mestre, não bloqueia nada)
**Dependências:** Spec 009-ext (NPC Visibility) já concluída
**Esforço estimado BA:** 0.5-1 sessão (escopo pequeno)

### Escopo
Mestres gerenciam dezenas/centenas de NPCs ao longo de uma campanha. Hoje a listagem é plana e não há como agrupar ou filtrar NPCs por contexto (cidade, facção, loja, situação). BA precisa definir sistema leve de tags/categorias para facilitar a organização.

### O que o BA deve investigar/decidir
- **Tags livres vs categorias fixas vs híbrido**: decidir com PO. Recomendação inicial: tags livres (Mestre cria livremente) + categorias "sugeridas" opcionais (cidade, raça, facção, loja, situação).
- Modelo: tag é entidade própria com escopo de jogo? Ou string simples em array?
- NPC pode ter múltiplas tags? (sim, muito provavelmente)
- Busca/filtro: UX no listagem de NPCs — chips de tag, autocomplete, filtro múltiplo (AND/OR?).
- Tags podem ser visíveis para jogadores? (provavelmente não — tag é ferramenta de organização do Mestre)
- Migração: NPCs existentes ficam sem tag; não é breaking change.
- Integração: aproveitar alguma estrutura existente (ex: se já houver sistema genérico de tags para outras entidades) ou criar do zero?

### Artefatos de entrada
- `docs/specs/SPEC-009-ext-npc-visibility/` — modelo atual de NPC
- Código: entity `Ficha` com `isNpc=true`, `JogoParticipante`
- Conversa com PO para confirmar lista inicial de categorias sugeridas

### Artefato de saída
- **spec.md** — descrição da feature + decisões de modelagem
- **plan.md** — plano técnico (entidade `NpcTag` ou array de string, endpoints, UX)
- **tasks/INDEX.md** + tasks (estimar 4-8 tasks: 2-3 backend + 2-5 frontend)
- Não precisa de dossiê separado — cabe tudo no spec.md pela simplicidade

### Critérios de aceitação da fase BA
- PO confirmou abordagem (tags livres vs fixas vs híbrido)
- UX de busca/filtro definida
- Spec pronta para execução direta

---

## Observações gerais

- Todas as três tasks produzem artefatos em `docs/specs/SPEC-0NN-nome/` seguindo o padrão existente do projeto.
- BA deve registrar perguntas pendentes em dossiê dedicado e aguardar resposta do PO antes de finalizar spec.
- Nenhuma dessas tasks bloqueia o RC atual (Rodada 17). Podem ser executadas em paralelo com a homologação, desde que o PO tenha tempo para responder perguntas.
- BA-02 é a mais pesada e provavelmente vai gerar várias sessões de ida-e-volta com o PO — agendar com antecedência.
