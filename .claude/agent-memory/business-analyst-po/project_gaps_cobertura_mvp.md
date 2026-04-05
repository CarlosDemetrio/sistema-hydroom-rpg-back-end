---
name: Análise de Cobertura de MVP — Gaps 2026-04-04
description: 8 gaps identificados entre specs 005-014 e o MVP necessário; 3 bloqueadores de qualidade, 4 funcionalidades sem task, 1 ponto de validação PO
type: project
---

Análise realizada em 2026-04-04 cruzando specs 005-014 com jornadas de usuário completas.
Resultado completo no output da conversa (não tem arquivo doc — foi entregue diretamente).

## Resultado: Specs 005-014 NÃO fecham o MVP

Cobertura: 3.5/5 jornadas. Jornadas 1 (Criação de Jogo) e 5 (Ciclo de Vida da Ficha) parcialmente descobertas.

## 3 Bloqueadores de qualidade

**GAP-MVP-01 — DefaultGameConfigProvider sem Spec**
- Problema: 9+ bugs e ausências documentados em `docs/analises/DEFAULT-CONFIG-AUDITORIA.md`, todos com respostas do PO em `docs/gaps/PERGUNTAS-PENDENTES-PO.md`, mas sem spec ou task para implementar.
- Itens críticos: BUG-DC-06 (Cabeça 25%→75%), BUG-DC-07 (Índole), BUG-DC-08 (Presença), BUG-DC-09 (Gênero), ausência de BonusConfig defaults, PontosVantagemConfig defaults, CategoriaVantagem defaults, Membro "Sangue", vantagens canônicas (opção C).
- Ação: Nova **Spec 015 — Correção DefaultGameConfigProvider**. Executar antes ou em paralelo com Spec 007.
- **Why:** Todo jogo criado hoje tem configuração defeituosa (Cabeça 3x mais fraca, sem bônus B.B.A/B.B.M, pontos de vantagem = 0).

**GAP-MVP-02 — FichaStatus MORTA/ABANDONADA sem task**
- Problema: PO decidiu (INCONS-02) que fichas nunca são deletadas — devem ter status MORTA/ABANDONADA. Spec 006 define enum FichaStatus com apenas RASCUNHO e COMPLETA. DELETE /fichas/{id} retornando 405 não está em nenhuma task. PA-002 (Spec 006) sem resposta.
- Ação: Task adicional na Spec 006 (T1-complemento): enum FichaStatus completo + endpoint POST /fichas/{id}/arquivar + DELETE → 405.
- **Why:** Sem isso, endpoint DELETE remove fichas permanentemente, violando decisão do PO.

**GAP-MVP-03 — FichaVantagem auto-criação por Classe sem entidade e sem task**
- Problema: PO confirmou (Q20) que classes dão "vantagens pré-definidas" auto-criadas ao escolher classe. Entidade ClasseVantagemPreDefinida não existe. Nenhuma task em nenhuma spec cobre isso.
- Perguntas abertas: PA-CLASSE-01 (vantagens têm custo deduzido ou são gratuitas?), PA-CLASSE-02 (fichas ativas são afetadas ao reconfigurar vantagens da classe?).
- Ação: Task nova (Spec 006 backend ou Spec 015) + resposta dos PAs antes de implementar.
- **Why:** Wizard de criação completa sem criar vantagens da classe — ficha fica mecanicamente incorreta.

## 4 Funcionalidades documentadas sem task implementadora

**GAP-MVP-04 — Polling 30s documentado mas sem task**
- Spec 009-ext spec.md diz "nenhuma task adicional necessária" mas implementação de setInterval + ciclo de vida + tratamento de erros não é trivial. Nenhuma task na Spec 009-ext cobre explicitamente o polling.
- Ação: Task T11 na Spec 009-ext — FichaPollingService + integração nos componentes Smart.

**GAP-MVP-05 — Revogação de FichaVantagem pelo Mestre sem task**
- Regra documentada na memória: MESTRE pode revogar qualquer vantagem. Spec 007 T7 cobre concessão de Insólitus, mas endpoint DELETE /fichas/{id}/vantagens/{fichaVantagemId} (MESTRE-only) não está em nenhuma task.
- Ação: Task entre T7 e T8 na Spec 007.

**GAP-MVP-07 — Inconsistências de URL sem responsável (INCONS-03 e INCONS-04)**
- CategoriaVantagem sem /api/v1/ e VantagemEfeito com jogoId duplicado. Marcados "próximo refinamento" sem task.
- Ação: Corrigir em Spec 007 T1 (VantagemEfeito) + task avulsa ou Spec 015 (CategoriaVantagem).

**GAP-MVP-08 — Wizard de criação de NPC frontend sem task**
- Endpoint POST /npcs existe. UI de listagem existe. Wizard de criação não está em nenhuma task.
- Ação: Task T14 na Spec 006 ou T12 na Spec 009-ext.

## 1 Ponto de validação PO

**GAP-MVP-06 — GAP-PONTOS-CONFIG: PO precisa confirmar ciência**
- MVP calcula pontos apenas de NivelConfig (Spec 012 T5 recorta isso). Pontos de Raça e Classe como fontes = futuro.
- PO respondeu Q11/Q14 que todas as fontes liberam pontos — mas Spec 012 T5 recortou por escopo de MVP. PO precisa confirmar ciência antes de fechar Spec 012.

## Ordem de priorização

```
Antes de Spec 006: Spec 015 (DefaultGameConfigProvider)
Durante Spec 006: GAP-MVP-02 (FichaStatus), GAP-MVP-08 (NPC wizard), responder PA-CLASSE-01/02
Durante Spec 007: GAP-MVP-05 (revogação vantagem), GAP-MVP-07 (URLs)
Durante Spec 009-ext: GAP-MVP-04 (polling)
Antes de fechar Spec 012: validar GAP-MVP-06 com PO
```

**Why:** Gaps identificados cruzando jornadas de usuário com tasks das specs — specs focam em funcionalidades individuais mas não cobrem todas as conexões entre elas.
**How to apply:** Ao planejar próxima sprint, verificar se Spec 015 foi criada antes de começar Spec 006. Ao revisar Spec 006, verificar se tasks de FichaStatus e NPC wizard foram adicionadas.
