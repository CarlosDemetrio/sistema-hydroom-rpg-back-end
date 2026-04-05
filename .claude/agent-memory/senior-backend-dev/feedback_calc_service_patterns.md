---
name: Padrões do FichaCalculationService
description: Decisões de design e armadilhas encontradas na Spec-007 T0 ao corrigir o motor de cálculos
type: feedback
---

FichaAptidao.classe não deve ser zerado no reset global.

**Why:** O campo pode ser definido manualmente pelo Mestre via AtualizarAptidaoRequest. Zerá-lo no reset quebraria testes e funcionalidade existente. O aplicarClasseAptidaoBonus usa setClasse() (sobrescreve) em vez de somar para garantir idempotência somente quando há ClasseAptidaoBonus configurado.

**How to apply:** No resetarCamposDerivaveis(), NÃO incluir aptidoes.forEach(a -> a.setClasse(0)). Campos "manual + calculado" exigem sobrescrever (setXxx) em vez de somar (setXxx(getXxx() + valor)) nos métodos de aplicação automática.

---

ClasseBonus.bonus é o campo BonusConfig (não bonusConfig) — a JoinColumn é bonus_id.

**Why:** Inconsistência de naming no modelo: ClasseBonus usa `bonus` (não `bonusConfig`) como nome do campo @ManyToOne para BonusConfig. Tentei usar cb.getBonusConfig() e falhou. O campo correto é cb.getBonus().

**How to apply:** Sempre verificar o nome exato do campo @ManyToOne no modelo antes de codificar. ClasseBonus.getBonus() retorna BonusConfig. ClasseBonus.getValorPorNivel() é BigDecimal, não Integer — use multiply(BigDecimal.valueOf(nivel)).

---

GameConfigInitializerService não cria BonusConfig por padrão.

**Why:** Os BonusConfigs (B.B.A., B.B.M., etc.) não são criados pelo GameConfigInitializerService ao inicializar um jogo. Apenas: atributos, tiposAptidao, aptidoes, niveis, classes, racas, bonusRaciais, prospeccoes, generos, indoles, presencas, membrosCorpo, vantagens.

**How to apply:** Em testes que precisam de BonusConfig (para criar ClasseBonus), crie manualmente via bonusConfigRepository.save(). BonusConfig exige campo `sigla` (NotNull, max 5 chars, unique por jogo).

---

FichaAptidao.recalcularTotal() não era chamado no fluxo principal antes da Spec-007 T0.

**Why:** O FichaCalculationService chamava recalcularAtributos() e recalcularBonus() mas não havia equivalente para aptidões. Adicionado `aptidoes.forEach(FichaAptidao::recalcularTotal)` no fluxo principal como PASSO 6.

**How to apply:** Ao adicionar novos campos calculados em entidades de ficha, verificar se o recálculo é invocado no FichaCalculationService.recalcular().

---

RacaBonusAtributo requer JOIN FETCH no repositório para evitar LazyInitializationException.

**Why:** A coleção Raca.bonusAtributos é LAZY. Ao acessar dentro do FichaCalculationService sem transação aberta, lança LazyInitializationException. A solução foi carregar via RacaBonusAtributoRepository.findByRacaIdWithAtributo() no FichaService.recalcular() e passar a lista diretamente para o FichaCalculationService.

**How to apply:** Nunca confiar em lazy load dentro de métodos de cálculo. Sempre carregar relações necessárias via JOIN FETCH no FichaService antes de chamar FichaCalculationService.recalcular().
