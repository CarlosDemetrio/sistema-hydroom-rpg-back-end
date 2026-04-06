# T0 — Replanejamento da Spec 013

> Fase: Pre-execucao (Backlog — fazer antes de qualquer task de documentacao)
> Responsavel: PM
> Status: PENDENTE

---

## O que precisa ser feito

Antes de executar qualquer documentacao, o PM deve criar 1 arquivo de task por modulo listado abaixo.
Cada task cobre exatamente 1 arquivo/componente. Estimativa maxima por task: 1h.

Alem dos modulos existentes, incluir o bloco GitHub (Pages, Wiki, READMEs com badges, Releases).

---

## Dois tipos de documentacao (nao misturar)

| Tipo | Publico-alvo | Formato | Onde publica |
|------|-------------|---------|--------------|
| **Tecnica** | Desenvolvedores | Javadoc, TSDoc, OpenAPI, README componente | GitHub Pages (API), repositorio |
| **Nao-tecnica / Dominio** | Mestres, Jogadores, futuros devs | Prosa clara, regras, exemplos | GitHub Wiki |

As tasks abaixo cobrem **os dois tipos**. Nao misturar numa mesma task.

---

## Modulos a Documentar

### Backend — Javadoc (1 task por service)

**Services criticos (P0):**
- `FichaCalculationService`
- `FichaService`
- `FormulaEvaluatorService`
- `FichaVantagemService`
- `VantagemAutoConcessaoService`
- `JogoParticipanteService`

**Services de configuracao (1 task por service):**
- `AbstractConfiguracaoService`
- `AtributoConfigService`
- `AptidaoConfigService`
- `BonusConfigService`
- `ClassePersonagemService`
- `DadoProspeccaoConfigService`
- `GeneroConfigService`
- `IndoleConfigService`
- `MembroCorpoConfigService`
- `NivelConfigService`
- `PresencaConfigService`
- `RacaService`
- `TipoAptidaoService`
- `VantagemConfigService`

**Services de suporte:**
- `SiglaValidationService`
- `GameConfigInitializerService`

---

### Backend — OpenAPI/Swagger (1 task por controller)

- `FichaController`
- `JogoController`
- `JogoParticipanteController`
- `VantagemEfeitoController`
- `AtributoConfigController`
- `AptidaoConfigController`
- `BonusConfigController`
- `ClassePersonagemController`
- `DadoProspeccaoConfigController`
- `GeneroConfigController`
- `IndoleConfigController`
- `MembroCorpoConfigController`
- `NivelConfigController`
- `PresencaConfigController`
- `RacaController`
- `TipoAptidaoController`
- `VantagemConfigController`
- DTOs de request/response (`@Schema` — 1 task cobrindo todos)

---

### Backend — Inline Comments (1 task por arquivo complexo)

- `FichaCalculationService` (algoritmo de calculo, ordem critica)
- `FormulaEvaluatorService` (variaveis disponiveis, exemplos)
- `SiglaValidationService` (regra cross-entity)
- `VantagemAutoConcessaoService` (logica de auto-concessao)

---

### Frontend — TSDoc (1 task por service)

- `fichas-api.service.ts`
- `ficha-business.service.ts`
- `jogos-api.service.ts`
- `config-api.service.ts`
- Fichas store / signals

---

### Frontend — README por componente (1 task por componente)

- `FichaWizardComponent` (orquestrador, sinais principais, fluxo de 6 passos)
- `StepIdentificacaoComponent`
- `StepDescricaoComponent`
- `StepAtributosComponent`
- `StepAptidoesComponent`
- `StepVantagensComponent`
- `StepRevisaoComponent`
- `WizardRodapeComponent`
- `FichaHeaderComponent`
- `FichaVantagensTabComponent`
- `FormulaEditorComponent`
- `EfeitoFormComponent`
- `FichaDetailComponent`

---

### Documentacao Nao-Tecnica — Dominio e Regras (1 task por topico)

> Publica no GitHub Wiki. Escrito em prosa, sem codigo. Explica o "o que" e o "por que",
> nao o "como implementado". Deve ser verificavel contra o codigo — sem inventar regras.

**Sistema geral (1 task cada):**
- Visao geral do sistema: o que e o Klayrah RPG, quem usa (Mestre, Jogador), fluxo basico Jogo → Ficha
- Como funciona um Jogo: criacao, configuracao, participantes, ciclo de vida
- Como funciona uma Ficha: criacao pelo wizard, campos, status (RASCUNHO → COMPLETA), evolucao (XP → nivel)
- Como funciona o sistema de fórmulas: variaveis disponiveis (FOR, AGI, VIG...), exemplos reais, onde sao usadas

**Configuracoes (1 task por configuracao — explicar o que faz, regras, campos):**
- `AtributoConfig`: o que e um atributo, o que e uma sigla, o que e formulaImpeto, limites valorMinimo/Maximo
- `AptidaoConfig`: o que e uma aptidao, relacao com TipoAptidao, como afeta o personagem
- `BonusConfig`: o que e um bonus, o que e formulaBase, quando e aplicado ao personagem
- `ClassePersonagem`: o que e uma classe, ClasseBonus (como funciona), ClasseAptidaoBonus, ClassePontosConfig, RacaClassePermitida
- `DadoProspeccaoConfig`: o que e um dado de prospeccao, o que e numeroFaces, como e usado em sessao
- `GeneroConfig`: o que e, como influencia o personagem
- `IndoleConfig`: o que e, como influencia o personagem
- `MembroCorpoConfig`: o que e um membro do corpo, porcentagemVida, como afeta o calculo de vida
- `NivelConfig`: o que e um nivel, xpNecessaria, pontosAtributo, pontosAptidao, limitadorAtributo — como tudo isso interage
- `PresencaConfig`: o que e, como influencia o personagem
- `Raca`: o que e uma raca, RacaBonusAtributo (como funciona), RacaPontosConfig, regras de RacaClassePermitida
- `TipoAptidao`: o que e, relacao com AptidaoConfig, como agrupa aptidoes
- `VantagemConfig`: o que e uma vantagem, VANTAGEM vs INSOLITUS, nivelMaximo, formulaCusto, pre-requisitos, efeitos (8 tipos)

**Ficha e motor de calculos (1 task cada):**
- Ficha — campos e significado: cada campo explicado (atributos base/total, aptidoes, vida, essencia, ameaca, prospeccao)
- Ficha — ciclo de vida: criacao pelo wizard (6 passos), completar, ganhar XP, subir de nivel, level up
- Motor de calculos: ordem de calculo, como vantagens afetam atributos/bonus/vida, o que e recalculado quando
- Sistema de vantagens: como comprar, como custo e calculado, Insolitus (concessao pelo Mestre), revogacao
- Sistema de participantes: estados (PENDENTE, APROVADO, REJEITADO, REMOVIDO, BANIDO), quem pode fazer o que

---

### GitHub Documentation (1 task por entregavel)

- README raiz — backend (badges build/cobertura/versao, setup local, arquitetura resumida)
- README raiz — frontend (badges, estrutura de features, scripts)
- GitHub Wiki — Dominio Klayrah (migrar `docs/glossario/`)
- GitHub Wiki — Guia de desenvolvimento backend
- GitHub Wiki — Guia de desenvolvimento frontend
- GitHub Pages — publicar Swagger UI automaticamente (GitHub Action)
- Template de Release + `CHANGELOG.md` + tag `v0.0.1-RC`

---

## Criterio de Aceitacao desta T0

- [ ] 1 arquivo `.md` de task criado por modulo listado acima
- [ ] Cada task tem: objetivo, arquivo-alvo, criterios de aceitacao objetivos
- [ ] INDEX.md atualizado com todas as tasks
- [ ] Estimativa de cada task <= 1h

*Criado: 2026-04-06*
