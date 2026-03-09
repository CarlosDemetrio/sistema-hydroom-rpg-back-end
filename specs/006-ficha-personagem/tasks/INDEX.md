# Índice de Tasks — Spec 006 (Ficha de Personagem)

| Task | Fase | Descrição | Complexidade |
|------|------|-----------|-------------|
| [P1-T1](./P1-T1-ficha-entity.md) | Entity | Ficha entity com campos identidade, narrativos, FKs para configs | 🟡 |
| [P1-T2](./P1-T2-ficha-service-controller.md) | CRUD Base | FichaService (CRUD + createSubRecords) + FichaController | 🔴 |
| [P2-T1](./P2-T1-fichaatributo-fichaaptidao-entities.md) | Atributos | FichaAtributo + FichaAptidao entities + repositories | 🟢 |
| [P2-T2](./P2-T2-endpoints-atributos-aptidoes.md) | Atributos | Endpoints update atributos/aptidões + validação de pontos | 🟡 |
| [P3-T1](./P3-T1-fichabonus-entity-endpoint.md) | Bônus | FichaBonus entity + endpoint PUT /api/fichas/{id}/bonus | 🟡 |
| [P4-T1](./P4-T1-ficha-vida-essencia-ameaca-prospeccao.md) | Estado | FichaVida + FichaVidaMembro + FichaEssencia + FichaAmeaca + FichaProspeccao entities | 🟡 |
| [P4-T2](./P4-T2-endpoints-vida-essencia.md) | Estado | Endpoints PUT para vida/essencia/ameaca/prospeccao | 🟡 |
| [P5-T1](./P5-T1-ficha-vantagem.md) | Vantagens | FichaVantagem entity + compra + pré-requisitos + custo | 🔴 |
| [P6-T1](./P6-T1-ficha-descricao-fisica.md) | Descrição | FichaDescricaoFisica entity + endpoint | 🟢 |
| [P7-T1](./P7-T1-testes-ficha-crud.md) | Testes | Testes de integração Ficha CRUD + inicialização sub-registros | 🟡 |
| [P7-T2](./P7-T2-testes-ficha-vantagem.md) | Testes | Testes de integração FichaVantagem (compra, pré-requisitos) | 🟡 |

**Total**: 11 tasks, ~8-10 dias de implementação

## Legenda de Complexidade
- 🟢 Baixa — mudanças pontuais, sem lógica nova
- 🟡 Média — lógica nova mas padrão conhecido
- 🔴 Alta — algoritmo complexo ou múltiplas dependências
