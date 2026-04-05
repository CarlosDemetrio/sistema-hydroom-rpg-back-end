---
name: Auditoria UX Fichas — Modo Sessao e Criacao
description: 20 gaps UX identificados em auditoria de 2026-04-03; bugs criticos no FichaHeaderComponent e NpcsComponent; 8 quick wins; 8 pontos em aberto para PO
type: project
---

Auditoria completa de UX feita em 2026-04-03 cobrindo modo sessao (jogador e mestre), criacao de ficha (wizard), edicao (level up) e NPCs.

Entregavel: `docs/analises/UX-FICHAS-AUDITORIA.md`

**Why:** O FichaDetailComponent e o FichaHeaderComponent existem mas tem lacunas criticas que impedem o uso em sessao real de jogo. Identificadas antes de iniciar o desenvolvimento do modo sessao.

**How to apply:** Antes de qualquer task de modo sessao ou wizard, consultar este documento para priorizar pelo impacto real no jogador.

---

## Bugs Criticos Confirmados no Codigo Existente

- `FichaHeaderComponent`: barras de vida e essencia com `[value]="100"` hardcoded — sempre aparecem cheias, independente do estado real de combate
- `FichaVantagensTabComponent`: `[pontosVantagemRestantes]="0"` hardcoded no FichaDetailComponent — jogadores nunca sabem quantos pontos tem disponíveis
- `NpcsComponent` linha 432: `router.navigate(['/jogador/fichas', fichaId])` — Mestre e redirecionado para rota de Jogador ao ver ficha de NPC
- `FichaVantagemResponse` TypeScript: sem campo `categoriaNome` — agrupa tudo como "Vantagens" mesmo o backend ja retornando a categoria
- Modelo `Ficha` TypeScript: sem campo `status` (RASCUNHO/COMPLETA) — impossivel exibir badge de ficha incompleta ou bloquear RASCUNHO de entrar em sessao
- Modelo `FichaResumo` TypeScript: sem campos `vidaAtual`, `essenciaAtual`, `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis`, `pontosVantagemDisponiveis`

---

## Lacunas Totais (sem spec ou sem implementacao)

- `FichaStatsCombateComponent`: especificado em MODO-SESSAO.md, nao existe no codigo
- `PainelSessaoComponent` (`/jogos/:jogoId/sessao`): especificado, nao existe — Mestre nao tem visao centralizada de sessao
- `ProspeccaoJogadorComponent` e `ProspeccaoMestreComponent`: especificados, nao existem
- `NpcVisibilidadeComponent`: especificado em NPC-VISIBILITY.md, nao existe
- `LevelUpDialogComponent`: especificado em LEVEL-UP.md, nao existe
- Toggle de modo sessao no `FichaHeaderComponent`: especificado, nao implementado

---

## Inconsistencias entre Specs

- MODO-SESSAO.md (sec.12) cita `POST /fichas/{id}/essencia/gastar` e `POST /fichas/{id}/essencia/curar`, mas Spec 009-ext diz para usar `PUT /fichas/{id}/vida` passando `essenciaAtual`. Endpoint semantico separado nao confirmado no backend.
- WIZARD-CRIACAO-FICHA.md tem 5 passos (sem aptidoes), Spec 006 original tem passo de aptidoes. Ha divergencia entre spec e design do wizard.
- Spec 012 diz "pontos de aptidao controlados pelo Mestre" mas o wizard de level up permite ao Jogador distribuir livremente. Contradicao a resolver com PO.

---

## Pontos em Aberto Criticos para PO (2026-04-03)

- PA-UX-01: Atualizacoes em tempo real durante sessao (polling vs SSE vs manual) — decisao de arquitetura
- PA-UX-02: Aptidoes no wizard de criacao OU no primeiro level up — afeta estrutura do wizard
- PA-UX-03: Pontos que excedem o limitador sao perdidos ou acumulam para o proximo nivel
- PA-UX-05: Estados ativa/morta/abandonada — Mestre pode reativar ficha morta?
- PA-UX-06: Compra de vantagens pos-criacao: livre ou precisa de aprovacao do Mestre?

---

## Acoes sem Spec Alguma

- Edicao de vida membro a membro (dano por regiao corporal) — nenhum spec descreve esta UI
- Edicao do campo `outros` do atributo (modificadores temporarios)
- Edicao do campo `sorte` das aptidoes pelo Mestre
- Concessao de Insolitus pelo Mestre (endpoint mencionado mas sem spec de UI)
- Marcacao de ficha como morta ou abandonada
