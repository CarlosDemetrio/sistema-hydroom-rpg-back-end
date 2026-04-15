# Copilot R07 — Spec 022: DefaultGameConfigProvider Refactor + T-HAB pendente

> Data: 2026-04-09/10
> Branch: `main`
> Base: 743 testes backend (pre-rodada)
> Status: PARCIAL (Spec 022 CONCLUIDA; T-HAB NAO IMPLEMENTADA)

---

## Contexto

Rodada focada em duas entregas: (1) refatorar o `DefaultGameConfigProviderImpl` monolitico em providers focados conforme Spec 022, e (2) implementar HabilidadeConfig entity + CRUD (T-HAB). A Spec 022 foi concluida com sucesso. T-HAB nao foi executada.

Briefing: `docs/tracking/rodadas-copilot/BRIEFING-R07.md`
Spec 022: `docs/specs/022-game-default-provider-refactor/`

---

## Tasks Executadas

### Fase 1 — Scaffold (T1-T3) + Fase 2 — Vantagens (T4-T12)

**Agente:** GitHub Copilot CLI (direto)
**Commit:** `a243459`

Refatoracao completa do `DefaultGameConfigProviderImpl`:
- Extraidos 11 providers focados + facade `DefaultGameConfigProviderImpl` que delega
- 64 vantagens completas com sigla, tipoVantagem, categoriaNome, formulaCusto
- Todos os placeholders errados da R06 (PA-R06-01) substituidos

### Tech Lead Review Fixes

**Commit:** `05510a1`

Correcoes identificadas em code review automatico aplicadas.

### UTF-8 Fixes

**Commit:** `e6b60bf`

Caracteres especiais corrigidos nos providers de aptidoes, classes e itens.

### ClassePontosConfig + RacaPontosConfig Defaults

**Commit:** `832410b`

Defaults implementados para as entidades de pontos por classe e raca (criadas na Spec 015).

### Dado de Prospeccao D20

**Commit:** `dd81325`

Adicionado dado de 20 faces ao provedor de prospeccoes.

---

## T-HAB — HabilidadeConfig (NAO EXECUTADA)

A task estava no briefing como independente (paralela a Spec 022). Nao foi implementada. Nenhum arquivo HabilidadeConfig.java existe no projeto. O escopo PO esta definido no briefing: nome, descricao, dano/efeito (texto), jogador pode criar/editar/deletar.

Proxima acao: escrever Spec 021 e implementar em rodada dedicada.

---

## Commits

| Hash | Mensagem | Task |
|------|----------|------|
| `a243459` | refactor(defaults): spec 022 — extract DefaultGameConfigProviderImpl into 11 focused providers | F1-T1..T3 + F2-T4..T12 |
| `05510a1` | fix(defaults): apply tech lead findings from spec 022 review | Code review fixes |
| `e6b60bf` | chore(defaults): fix UTF-8 special characters in aptidoes, classes and itens providers | UTF-8 fix |
| `832410b` | feat(defaults): implement ClassePontosConfig and RacaPontosConfig defaults | ClassePontos + RacaPontos |
| `dd81325` | feat(prospeccoes): adiciona dado de 20 faces ao provedor de prospeccoes | D20 prospeccao |

---

## Estado Final

### Backend

| Metrica | Valor |
|---------|-------|
| Testes totais | 771 (0 falhas, 3 skipped) |
| Delta vs base (743) | +28 |
| HEAD (pos-R07) | `dd81325` |

Nota: contagem de 771 verificada em 2026-04-12 apos todos os hotfixes de infra subsequentes. Os hotfixes nao adicionaram testes.

---

## Pendencias / PAs

| ID | Descricao | Bloqueia | Proxima acao |
|----|-----------|---------|--------------|
| PA-R06-01 | getDefaultVantagens() com placeholders | RESOLVIDO | 64 vantagens completas |
| PA-R06-02 | Spec de Habilidades (T-HAB) | Nao (RC nao depende) | Escrever Spec 021, implementar em rodada dedicada |

---

*Rodada Copilot R07 encerrada em 2026-04-10. Tracking formalizado em 2026-04-12.*
