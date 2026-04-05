---
name: Agent Roster and Allocation Rules
description: Agent allocation rules (1 task/agent max), roster history, and gargalo patterns
type: project
---

**Regra de agentes (decisao PM 2026-04-04 rodada 2):**
- **1 task por agente** (maximo)
- Tasks triviais (<30min): podem agrupar 2-3
- Tasks >2h: agente proprio obrigatorio
- Specs grandes: dividir em sub-agentes

**Why:** Agentes com multiplas tasks geram conflitos de escopo e reduzem throughput. Modelo 1:1 maximiza paralelismo e minimiza conflitos de merge.

**How to apply:** Ao planejar rodadas, cada task de 2h+ recebe um agente dedicado com system prompt, escopo de arquivos e plano anti-conflito. Tasks <30min podem ser agrupadas no mesmo agente (ex: QW-Bug1 + QW-Bug2).

**Roster historico (sessao 10):**
- java-spring-tech-lead -- backend architecture, tech decisions
- senior-backend-dev -- backend implementation
- angular-tech-lead -- frontend architecture
- angular-frontend-dev -- frontend implementation
- primeng-ux-architect -- design system, UX
- business-analyst-po -- specs, domain analysis

**Gargalo History:**
- 2026-04-01 Sprint 1: Frontend gargalo (10 tasks para 1 dev). RESOLVIDO com 3 agents.
- 2026-04-02 Sprint 1 Session 4: 23 frontend-backend gaps. Frontend gargalo IMMINENT.
- 2026-04-04 Rodada 2: Modelo 1 task/agente adotado. 3 tracks paralelos sem conflitos.
