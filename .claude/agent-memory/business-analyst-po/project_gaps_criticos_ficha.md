---
name: Gaps críticos do fluxo Ficha — prioridade de implementação
description: Lista priorizada dos gaps entre backend e frontend no fluxo Ficha, auditada em 2026-04-02
type: project
---

Auditado em 2026-04-02. Branch: `feature/009-npc-fichas-mestre`.

**Why:** Sem essa lista, o frontend pode implementar na ordem errada, criando dívida técnica acumulada.

**How to apply:** Usar como referência de priorização ao escrever stories de frontend para o fluxo Ficha.

## CRÍTICOS (bloqueadores de uso real)

1. **FichaFormComponent está totalmente quebrado** — buildForm() usa campos que não existem no backend (origem, linhagem, insolitus, nvs, descricaoFisica, pericias, equipamentos, titulosRunas). O submit só envia nome. Requer reescrita completa como wizard de 4 passos.

2. **Atributos no FichaDetail são mockados** — carregarAtributos() usa FichaResumo.atributosTotais para montar FichaAtributoResponse[] com base=0, nivel=0, impeto=0. Não reflete os valores reais de base/nivel/outros. Necessita de endpoint GET /fichas/{id}/atributos ou que o resumo passe os dados detalhados.

3. **Aptidões nunca carregam** — carregarAptidoes() define lista vazia com TODO. Não existe GET /fichas/{id}/aptidoes no backend. Usuário vê aba vazia sempre.

4. **Stats bar de vida/essência hardcoded** — FichaHeader passa [value]="100" fixo nas barras de progresso. Não há vidaAtual/essenciaAtual no FichaResumoResponse. Para mostrar a barra real, precisaria de PUT /vida e retorno com valores atuais, mais persistência de vidaAtual na entidade FichaVida.

5. **Nenhuma tela de NPC para o Mestre** — POST /jogos/{id}/npcs e GET /jogos/{id}/npcs existem no backend, mas não há nenhuma página em features/mestre/ para NPCs. O Mestre não tem como criar ou listar NPCs pela UI.

6. **Nenhuma tela para conceder XP / renascimentos** — PUT /fichas/{id} aceita xp e renascimentos, mas não há UI para o Mestre acessar esse campo. É o fluxo de progressão central do jogo.

## ALTOS (funcionalidade incompleta)

7. **FichaVantagemResponse não inclui categoriaNome** — frontend agrupa tudo como "Vantagens". Backend precisa incluir o campo, ou frontend precisa fazer chamada adicional para VantagemConfig.

8. **pontosVantagemRestantes sempre é 0** — FichaDetailComponent passa [pontosVantagemRestantes]="0" hardcoded. Não há cálculo de pontos disponíveis vs gastos.

9. **FichasList filtra apenas client-side por nome** — Backend suporta filtros ?nome=&classeId=&racaId=&nivel= mas a tela não os usa.

10. **Botão "Editar" do FichaDetail navega para rota errada** — irParaEdicao() navega para /fichas/{id}/editar, mas a rota registrada na FichasList usa /jogador/fichas/{id}/edit.

## MÉDIOS (UX incompleta mas não bloqueante)

11. **FichaHeader não exibe generoNome, presencaNome de forma destacada** — exibe no meta-text mas não como tags separadas.

12. **FichaResumoTab não inclui membros do corpo** — FichaPreviewResponse inclui VidaPreview com membros, mas FichaResumoResponse não inclui dados de membros.

13. **Sem tela de compra de vantagens** — existe POST /fichas/{id}/vantagens, mas não há UI de marketplace de vantagens disponíveis para compra. Apenas listagem das já compradas.
