---
name: Spec 008 — Sub-recursos de Classes e Racas
description: Estado, decisões e gaps da Spec 008 (sub-recursos de ClassePersonagem e Raca no frontend)
type: project
---

Spec 008 criada em 2026-04-02. Backend 100% implementado. Frontend tem estrutura basica mas com
defeitos criticos de tipagem e campos ausentes.

**Why:** O CRUD basico de Classes e Racas existe no frontend, mas os sub-recursos nao estao
corretamente conectados — faltam campos no modelo TypeScript e os DTOs de POST estao incompletos.
Isso bloqueia a criacao de fichas com dados corretos de classe/raca.

**How to apply:** Ao trabalhar na Spec 008, priorizar T1 (tipagem) antes de qualquer mudanca
nos componentes. As abas dos drawers ja existem — as mudancas sao cirurgicas, nao estruturais.

## Problemas Criticos Identificados (auditoria 2026-04-02)

- `ClasseBonusConfig` no frontend NAO tem campo `valorPorNivel` — campo existe no backend
- `ClasseAptidaoBonus` no frontend NAO tem campo `bonus` — campo existe no backend
- `addClasseBonus` envia apenas `{ bonusConfigId }`, falta `valorPorNivel`
- `addClasseAptidaoBonus` envia apenas `{ aptidaoConfigId }`, falta `bonus`
- 6 metodos de sub-recurso no ConfigApiService retornam/aceitam `unknown`

## Decisoes de Produto

- NAO criar novos componentes — as abas dos drawers ja existem
- RacasConfigComponent: adicionar coluna "Classes" na tabela usando computed `racasComInfo`
- Badge colorido (verde/amarelo) e nice-to-have para iteracao futura se BaseConfigTable nao suportar
- Bonus negativo em RacaBonusAtributo deve ter indicador TEXTUAL "(penalidade)" alem da cor

## Pontos em Aberto (perguntas para stakeholder)

- P-01: Backend tem PUT para editar `valorPorNivel` de ClasseBonus existente?
- P-02: Backend tem PUT para editar `bonus` de ClasseAptidaoBonus existente?
- P-03: Confirmacao de remocao de sub-recurso quando classe/raca esta em uso em fichas?
- P-04: Coluna preview na tabela de classes (ex: "B.B.A +1/niv")?
- P-05: Backend retorna 409 ou 500 ao deletar Classe/Raca com fichas vinculadas?

## Tasks

- T1: Tipagem modelos + API service (prerequisito para tudo)
- T2: ClassesConfigComponent — valorPorNivel + bonus (depende T1)
- T3: RacasConfigComponent — badge restricao + label penalidade (depende T1)
- T4: Testes (depende T1, T2, T3)
