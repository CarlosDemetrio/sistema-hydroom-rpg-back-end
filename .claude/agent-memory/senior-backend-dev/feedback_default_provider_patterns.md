---
name: Padrões e Armadilhas do DefaultGameConfigProvider
description: Decisões de design e armadilhas encontradas na Spec-015 T5 ao corrigir e expandir o GameConfigInitializerService
type: feedback
---

GameConfigInitializerService agora cria BonusConfig (BBA, BBM, etc.) durante a inicialização do jogo.

**Why:** Antes da Spec-015 T5, o initializer não criava BonusConfig. Após a correção, testes que manualmente criavam BonusConfig com sigla 'BBA' começaram a falhar com DataIntegrityViolation (unique constraint). A solução foi usar `orElseGet` para buscar BBA existente antes de criar.

**How to apply:** Em qualquer teste que cria BonusConfig manualmente após criar um jogo via `jogoService.criarJogo()`, sempre buscar o BonusConfig existente primeiro usando `bonusConfigRepository.findByJogoIdOrderByOrdemExibicao()` com filter, e só criar via `orElseGet` como fallback.

---

Defaults do Klayrah RPG corrigidos na Spec-015 T5 (valores canônicos):

- Índole: 3 valores (Bom, Mau, Neutro) — NÃO 9 alinhamentos D&D
- Presença: 4 valores éticos (Bom, Leal, Caótico, Neutro) — NÃO escala de intensidade
- Gênero: 3 valores (Masculino, Feminino, Outro) — NÃO 4 com "Prefiro não informar"
- Cabeça: porcentagemVida = 0.75 — NÃO 0.25
- Membro "Sangue": porcentagemVida = 1.00 (representa vida total, pode exceder 1.00 na soma dos membros)
- Classe "Necromante" — NÃO "Necromance"

**Why:** A auditoria DEFAULT-CONFIG-AUDITORIA.md (2026-04-04) revelou que todos os jogos novos eram criados com dados inconsistentes com o sistema Klayrah.

**How to apply:** Ao verificar defaults de jogo em testes, usar os valores corrigidos. A soma das porcentagens de membros do corpo NÃO precisa ser 1.00 — Sangue tem 100% e os outros membros individuais também têm suas porcentagens. Não escrever assertions de soma total.

---

Ordem de inicialização do GameConfigInitializerService importa para FK constraints.

**Why:** ClasseBonus tem FK para BonusConfig. Se BonusConfig for criado depois de ClasseBonus, o initializer falha. O mesmo se aplica a CategoriaVantagem antes de VantagemConfig.

**How to apply:** Ordem correta: AtributoConfig → TipoAptidao → AptidaoConfig → BonusConfig → NivelConfig → PontosVantagemConfig → CategoriaVantagem → ClassePersonagem → Raca → RacaBonusAtributo → DadoProspeccao → Genero → Indole → Presenca → MembroCorpo → VantagemConfig.

---

NivelConfigDTO.of() agora tem dois overloads:

- 6-param: `of(nivel, xp, pontosAtributo, pontosVantagem, pontosAptidao, limitadorAtributo)` — usa limitador do DTO
- 5-param: `of(nivel, xp, pontosAtributo, pontosVantagem, pontosAptidao)` — limitador null, o service usa default 50

**Why:** BUG-DC-02: o GameConfigInitializerService hardcodava limitadorAtributo=50 ignorando o DTO.

**How to apply:** Sempre usar o overload de 6 parâmetros no DefaultGameConfigProviderImpl para que o limitador correto seja aplicado por faixa de nível.
