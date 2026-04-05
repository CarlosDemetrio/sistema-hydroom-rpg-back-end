---
name: Análise completa de gaps — todos os domínios (2026-04-02)
description: Dossiê de gaps por domínio (ficha, progressão, participantes, NPC, VantagemEfeito, wizard) com perguntas para PO
type: project
---

Produzido em 2026-04-02. Branch: `feature/009-npc-fichas-mestre`.

**Why:** Consolidação de todos os gaps identificados nos docs (PRODUCT-BACKLOG, EPICS-BACKLOG, UX-BACKLOG, API-CONTRACT, glossário) para alimentar refinamento de sprint.

**How to apply:** Usar como base para sessões de refinamento — cada gap tem pergunta específica para decisão do PO.

## GAP-01: Wizard de criação de ficha — fluxo indefinido
- FichaFormComponent existe com 10 seções em scroll único, mas submete apenas {nome}
- Glossário define campos obrigatórios: racaId, classeId, generoId, indoleId, presencaId
- Campos narrativos (Insólitus, Título Heróico, Arquétipo) são opcionais mas sem critério de quando pedir
- Pergunta PO: wizard de quantos passos? O que é obrigatório no passo 1 vs opcional depois?

## GAP-02: XP — quem pode conceder, quando, como
- Backend aceita xp via PUT /fichas/{id} mas qualquer role MESTRE ou JOGADOR pode editar
- Glossário: "XP concedida pelo Mestre por completar desafios, derrotar inimigos e progredir na história"
- Sem endpoint dedicado `POST /fichas/{id}/xp` com validação de role
- Sem histórico de concessão de XP (auditoria)
- Pergunta PO: Jogador pode editar sua própria XP? Precisa de endpoint separado para concessão em lote?

## GAP-03: VantagemEfeito — tipos implementados mas sem integração na ficha
- Backend tem 8 tipos implementados (BONUS_ATRIBUTO, BONUS_APTIDAO, BONUS_DERIVADO, BONUS_VIDA, BONUS_VIDA_MEMBRO, BONUS_ESSENCIA, DADO_UP, FORMULA_CUSTOMIZADA)
- FichaCalculationService não consome VantagemEfeito ao recalcular (usa VT hardcoded)
- DADO_UP não tem semântica clara: o dado evolui na progressão da vantagem, mas como o frontend mostra isso?
- Pergunta PO: FORMULA_CUSTOMIZADA precisa de qual motor de avaliação? As variáveis disponíveis são as mesmas do FormulaEvaluator?

## GAP-04: Participantes — transições de estado ambíguas
- Status definidos: PENDENTE, APROVADO, REJEITADO, BANIDO
- Falta: jogador REJEITADO pode solicitar novamente? Após quanto tempo? Quantas vezes?
- Falta: jogador BANIDO é diferente de REJEITADO em termos de regra — mas o endpoint DELETE usa a mesma semântica para ambos
- Pergunta PO: BANIDO = bloqueio permanente? REJEITADO = pode tentar de novo?

## GAP-05: NPC vs Ficha de Jogador — diferenças de negócio não documentadas
- isNpc=true e jogadorId=null são os únicos diferenciadores no modelo
- Questões abertas: NPC tem progressão (XP/nível)? Jogador pode ver stats de NPC? NPC pode ter vantagens?
- Campo `descricao` existe no NpcCreateRequest mas não em CreateFichaRequest — diferença intencional?
- Pergunta PO: Mestre pode "revelar" um NPC para os jogadores? NPC tem todas as mecânicas de ficha ou é simplificado?

## GAP-06: Level Up — fluxo exato indefinido
- Glossário: ao subir de nível, personagem ganha pontosAtributo + pontosAptidao + pontosVantagem
- Sem wizard de level up implementado — nem no backend (validação) nem no frontend
- Pergunta PO: Level up é automático ao ganhar XP suficiente ou o jogador "confirma" o level up? Pontos de atributo devem ser distribuídos imediatamente ou podem acumular?

## GAP-07: Insólitus — impacto mecânico sem modelagem
- Glossário: "Insólitus tem impacto mecânico real: pode desbloquear bônus extras em atributos, liberar vantagens exclusivas"
- No modelo atual, Insólitus é apenas texto livre na Ficha — sem nenhuma mecânica associada
- Pergunta PO: Insólitus é modelado mecanicamente agora ou fica como narrativa livre para o Mestre resolver na mesa?

## GAP-08: Essência restante — persistência não modelada
- FichaResumoResponse tem essenciaTotal mas não essenciaAtual (gasta)
- Para gastar essência (lançar magia), precisa de endpoint PUT /fichas/{id}/essencia ou similar
- Analogia: PUT /fichas/{id}/vida já existe para dano nos membros
- Pergunta PO: essenciaGasta persiste no banco? O jogador pode "recarregar" essência manualmente?

## GAP-09: Pontos de Vantagem — cálculo inconsistente
- PontosVantagemConfig existe mas o frontend passa pontosVantagemRestantes=0 hardcoded
- Cálculo correto: somar (PontosVantagem por nível de 1 até nível atual) - (custo de todas as FichaVantagens)
- FichaResumoResponse não inclui esse cálculo
- Pergunta PO: PontosVantagem acumulam ao subir de nível (todos os níveis somados) ou apenas os do nível atual são usáveis?

## GAP-10: Prospecção — quem concede, como decrementa
- Backend tem PUT /fichas/{id}/prospeccao mas não está claro se incrementa OU decrementa
- Apenas Mestre pode dar prospecção (glossário: "recurso que o Mestre concede")
- Jogador usa (decrementa) durante partida — precisa de endpoint separado ou é o mesmo?
- Pergunta PO: usar prospecção é uma ação do jogador sem validação ou o Mestre precisa confirmar?
