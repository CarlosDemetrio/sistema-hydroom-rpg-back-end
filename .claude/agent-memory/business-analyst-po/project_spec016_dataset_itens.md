---
name: Spec 016 — Dataset de Itens SRD (dataset-itens-srd.md)
description: Dataset BA completo para T6 do sistema de itens: 7 raridades, 20 tipos, 40 itens, equipamentos iniciais por classe e mapeamento FichaCalculationService
type: project
---

Dataset de referencia BA criado em 2026-04-04 em `docs/specs/016-sistema-itens/dataset/dataset-itens-srd.md`.

**Fato/Decisao:** Dataset expande o conteudo da task P2-T6-default-dataset.md com flavour text, mapeamento de FKs, mapeamento campo-a-campo para o FichaCalculationService e 4 pontos em aberto para o PO.

**Why:** A task T6 ja tinha o dataset em forma de tabela, mas nao tinha flavour text para descricoes, nao tinha mapeamento explicito de FKs (resolucao por nome no mesmo jogo), e nao tinha o mapeamento campo→entidade para o servico de calculo. O documento BA preenche essas lacunas para o agente implementador.

**How to apply:** Ao trabalhar em qualquer task da Spec 016 relacionada a itens ou calculo:
- 7 raridades: Comum (#9d9d9d) ate Unico (#e268a8); so Comum tem podeJogadorAdicionar=true
- 20 tipos: 11 de ARMA, 4 de ARMADURA, 2 de ACESSORIO, 2 de CONSUMIVEL, 1 de AVENTURA
- 40 itens: 15 armas (11 comuns + 4 magicas), 10 armaduras/escudos, 5 acessorios magicos, 5 consumiveis, 5 aventura
- Armas Comuns: sem ItemEfeito. Armas magicas (+1/+2): BONUS_DERIVADO em B.B.A ou B.B.M
- Armaduras: BONUS_DERIVADO em Defesa. Escudos: BONUS_DERIVADO em Bloqueio
- Pocoes: duracaoPadrao=1, nunca equipadas, sem ItemEfeito no MVP (informativas)
- Requisitos formais: apenas Cota de Malha (FOR>=10) e Placa Completa (FOR>=12)
- Equipamentos iniciais: 40 registros em 12 classes; Feiticeiro e o unico sem item obrigatorio

**Pontos em aberto (PA-016-DS-01..04):**
- PA-016-DS-01: pocoes com ItemEfeito no MVP ou apenas informativas?
- PA-016-DS-02: penalidade de Furtividade da Cota de Escamas como ItemEfeito negativo?
- PA-016-DS-03: decremento de durabilidade do Kit de Curandeiro por uso ou manual?
- PA-016-DS-04: Manto de Elvenkind como Amuleto ou tipo Capa (subcategoria nao esta no dataset de tipos)?
