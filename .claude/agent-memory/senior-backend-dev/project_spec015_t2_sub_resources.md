---
name: Spec 015 T2 — Sub-recursos ClassePontosConfig/RacaPontosConfig + Vantagens Pré-definidas
description: Padrão de serviços standalone para sub-recursos de ClassePersonagem e Raca (T2 entregue)
type: project
---

Spec 015 T2 entregue em 2026-04-04.

**Decisão arquitetural:** T2 cria 4 services standalone (`ClassePontosConfigService`, `ClasseVantagemPreDefinidaService`, `RacaPontosConfigService`, `RacaVantagemPreDefinidaService`) em vez de embutir nos services pai. Isso difere do padrão anterior (ClasseBonus/RacaBonusAtributo embutidos em ClasseConfiguracaoService/RacaConfiguracaoService). Aceito pela spec.

**Por que:** Sub-recursos com lógica de CRUD completa (GET/POST/PUT/DELETE) justificam services próprios. ClasseBonus/RacaBonusAtributo têm só GET/POST/DELETE (sem PUT).

**Como aplicar:** Para novos sub-recursos com PUT (atualização), criar service standalone. Para sub-recursos simples (apenas add/remove), embutir no service pai.

**pontosAptidao AUSENTE em ClassePontosConfig/RacaPontosConfig:** Decisão PO 2026-04-04 — aptidões independentes de classe/raça. Pool vem SOMENTE de NivelConfig.pontosAptidao.

**RN-015-06:** VantagemConfig deve pertencer ao mesmo jogo de ClassePersonagem/Raca — validado no service antes de criar VantagemPreDefinida. Lança ValidationException (400).

**Duplicata de nível:** Lança ConflictException (409) — existsByClassePersonagemIdAndNivel / existsByRacaIdAndNivel.

**Worktree vs main:** T1 artifacts (4 models, 4 repos, 8 DTOs, 4 mappers) estavam no main mas não no worktree feature/009. T2 precisou incluir T1 no worktree também.

**Endpoints adicionados (14 total):**
- ClasseController: GET/POST/PUT/DELETE /{id}/pontos-config, GET/POST/DELETE /{id}/vantagens-predefinidas
- RacaController: mesmos 7 endpoints com /{id}/pontos-config e /{id}/vantagens-predefinidas

**Testes:** 26 novos testes, todos passando. Build final: 234 testes, 0 falhas (worktree branch feature/009).
