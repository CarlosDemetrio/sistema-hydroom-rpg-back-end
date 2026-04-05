# Spec 016 — Sistema de Itens/Equipamentos: Coordenacao Multi-BA

> Documento de coordenacao para decomposicao do trabalho entre multiplos BAs.
> Criado: 2026-04-04 | PM: Scrum Orchestrator
> Status: EM ESPECIFICACAO (BA-016-01 ativo, BA-016-02 e BA-UX-016 planejados)

---

## Visao Geral

O Sistema de Itens/Equipamentos e o maior modulo funcional apos o motor de calculos.
Estimativa total: **~21 tasks** (15 backend + 4 frontend + 2 dataset).
Decomposto em 3 sub-dominios com BAs dedicados para evitar gargalo de especificacao.

---

## Mapa de Sub-Dominios

```
SUB-DOMINIO 1: CONFIGURACAO (Catalogo)        SUB-DOMINIO 3: DATASET & DEFAULTS
  RaridadeItemConfig                              Dataset ~40 itens D&D 5e SRD
  TipoItemConfig (hierarquia)                     DefaultProvider (raridade, tipo, itens)
  ItemConfig (catalogo completo)                  ClasseEquipamentoInicial defaults
  ItemRequisito (nivel/atrib/aptidao/vantagem)
  ItemEfeito (similar a VantagemEfeito)         [PODE RODAR EM PARALELO COM SD-1]
                                                  |
  ~5 tasks backend + ~2 tasks frontend            ~2 tasks backend
  |                                               |
  v (BLOQUEIA)                                    v
SUB-DOMINIO 2: FICHA (Inventario)
  FichaItem (item na ficha, equipado/inventario)
  ClasseEquipamentoInicial (wizard)
  FichaCalculationService (aplicar ItemEfeito)
  Endpoints: adicionar, equipar, desequipar, listar, remover

  ~4 tasks backend + ~2 tasks frontend
```

---

## Dependencias Entre Sub-Dominios

| De | Para | Tipo | Detalhe |
|----|------|------|---------|
| SD-1 (Configuracao) | SD-2 (Ficha) | **BLOQUEIA** | FichaItem referencia ItemConfig; ItemEfeito necessario para calculos |
| SD-1 (Configuracao) | SD-3 (Dataset) | **BLOQUEIA PARCIAL** | Dataset precisa dos models de RaridadeItemConfig, TipoItemConfig, ItemConfig para defaults |
| SD-3 (Dataset) | SD-2 (Ficha) | **BLOQUEIA PARCIAL** | ClasseEquipamentoInicial defaults precisam existir antes do wizard usar |
| Spec 007 (Motor) | SD-2 (Ficha) | **BLOQUEIA** | ItemEfeito usa mesma infraestrutura de VantagemEfeito no FichaCalculationService |
| Spec 007 (Motor) | SD-1 (Configuracao) | **BLOQUEIA PARCIAL** | ItemEfeito usa tipos de efeito definidos em Spec 007 (BONUS_ATRIBUTO, etc.) |
| Spec 006 (Wizard) | SD-2 (Ficha) | **INFLUENCIADO** | ClasseEquipamentoInicial aparece no wizard de criacao de ficha |

### Sequencia de Implementacao Recomendada

```
FASE 1 (paralelo):
  SD-1 backend (entities, repos, services, controllers, testes)  [5 tasks]
  SD-3 backend (dataset research + DefaultProvider)              [2 tasks]
    > SD-3 pode iniciar pesquisa enquanto SD-1 cria os models
    > SD-3 DefaultProvider so compila apos models de SD-1 existirem

FASE 2 (sequencial apos Fase 1 + Spec 007):
  SD-1 frontend (tela de configuracao de catalogo)               [2 tasks]
  SD-2 backend (FichaItem + FichaCalculationService)             [4 tasks]
    > Depende de: SD-1 completo + Spec 007 completa

FASE 3 (sequencial apos Fase 2):
  SD-2 frontend (aba inventario na ficha)                        [2 tasks]

TOTAL: ~15 tasks backend + ~4 tasks frontend + ~2 tasks dataset = 21 tasks
```

---

## Tabela de Tasks por Sub-Dominio

### SD-1: Configuracao (Catalogo) — ~7 tasks

| ID | Tipo | Descricao | Dependencia | Estimativa |
|----|------|-----------|-------------|------------|
| S016-T1 | Backend | Entity `RaridadeItemConfig` + CRUD completo | Nenhuma | 2-3h |
| S016-T2 | Backend | Entity `TipoItemConfig` com hierarquia pai/filho + CRUD | S016-T1 | 4-6h |
| S016-T3 | Backend | Entity `ItemConfig` + `ItemRequisito` + `ItemEfeito` + CRUD | S016-T1, S016-T2 | 6-8h |
| S016-T4 | Backend | Validacoes cross-entity (sigla, unicidade, requisitos validos) | S016-T3 | 3-4h |
| S016-T5 | Backend | Testes de integracao para SD-1 completo | S016-T1 a T4 | 4-6h |
| S016-T6 | Frontend | Tela de configuracao RaridadeItem + TipoItem | S016-T1, S016-T2 | 6-8h |
| S016-T7 | Frontend | Tela de configuracao ItemConfig (catalogo com requisitos e efeitos) | S016-T3 | 8-10h |

### SD-2: Ficha (Inventario) — ~6 tasks

| ID | Tipo | Descricao | Dependencia | Estimativa |
|----|------|-----------|-------------|------------|
| S016-T8 | Backend | Entity `FichaItem` + endpoints (adicionar, equipar, desequipar, listar, remover) | SD-1 completo | 6-8h |
| S016-T9 | Backend | Entity `ClasseEquipamentoInicial` + endpoint de consulta | S016-T3 | 3-4h |
| S016-T10 | Backend | FichaCalculationService — aplicar ItemEfeito de itens equipados | S016-T3, Spec 007 completa | 6-8h |
| S016-T11 | Backend | Testes de integracao para SD-2 completo | S016-T8 a T10 | 4-6h |
| S016-T12 | Frontend | Aba "Inventario" na FichaDetail (lista de itens, equipar/desequipar) | S016-T8 | 8-10h |
| S016-T13 | Frontend | Integracao do wizard com ClasseEquipamentoInicial (Spec 006) | S016-T9, Spec 006 | 4-6h |

### SD-3: Dataset & DefaultProvider — ~2 tasks

| ID | Tipo | Descricao | Dependencia | Estimativa |
|----|------|-----------|-------------|------------|
| S016-T14 | Backend | Dataset de ~40 itens (D&D 5e SRD adaptado) + DefaultProvider raridades/tipos | SD-1 models existem | 6-8h |
| S016-T15 | Backend | ClasseEquipamentoInicial defaults por classe (Guerreiro, Mago, etc.) | S016-T9, S016-T14 | 3-4h |

---

## Criterio de "Done" por Sub-Dominio

### SD-1: Configuracao (Catalogo) — DONE quando:
- [ ] Entities RaridadeItemConfig, TipoItemConfig, ItemConfig, ItemRequisito, ItemEfeito existem com unique constraints corretas
- [ ] CRUDs completos (POST/GET/PUT/DELETE) com @PreAuthorize adequado
- [ ] TipoItemConfig suporta hierarquia pai/filho
- [ ] ItemEfeito reutiliza os tipos de efeito da Spec 007 (BONUS_ATRIBUTO, BONUS_APTIDAO, etc.)
- [ ] Validacao cross-entity de siglas via SiglaValidationService
- [ ] Testes de integracao cobrindo todos os cenarios (>= 80% branch coverage no modulo)
- [ ] Telas frontend de configuracao funcionais (MESTRE pode criar/editar/listar/deletar)

### SD-2: Ficha (Inventario) — DONE quando:
- [ ] FichaItem com estados equipado/inventario funcionando
- [ ] Endpoints de adicionar, equipar, desequipar, listar, remover item na ficha
- [ ] FichaCalculationService aplica bonus de itens equipados corretamente
- [ ] ClasseEquipamentoInicial funciona no contexto do wizard
- [ ] Testes de integracao cobrindo calculos com itens equipados
- [ ] Aba "Inventario" na FichaDetail funcional

### SD-3: Dataset & DefaultProvider — DONE quando:
- [ ] GameConfigInitializerService cria raridades, tipos e ~40 itens default ao criar jogo
- [ ] ClasseEquipamentoInicial defaults existem para cada classe default
- [ ] Testes verificam que defaults sao criados corretamente

---

## Plano Anti-Conflito de Merge

Para evitar conflitos entre BAs trabalhando simultaneamente:

| BA | Arquivos/Pacotes EXCLUSIVOS | NAO TOCAR |
|----|---------------------------|-----------|
| BA-016-01 (Spec geral) | `docs/specs/016-sistema-itens/spec.md`, `plan.md`, `tasks/INDEX.md` | Nada fora de `016-sistema-itens/` |
| BA-016-02 (Dataset) | `docs/specs/016-sistema-itens/dataset/` | `spec.md`, `plan.md`, tasks individuais fora de dataset |
| BA-UX-016 (UX) | `docs/design/EQUIPAMENTOS-INVENTARIO.md` | Qualquer arquivo em `specs/016-sistema-itens/` exceto UX refs |

---

## Perguntas Ainda em Aberto

Decisoes pendentes do PO que os BAs seguintes devem considerar:

### Q-016-01: Limite de itens equipados simultaneamente
- **Opcao A:** Sem limite — jogador equipa quantos itens quiser
- **Opcao B:** Limite por tipo (ex: 1 armadura, 2 armas, 2 aneis, 1 elmo)
- **Opcao C:** Limite por "slots" (ex: 6 slots totais, qualquer combinacao)
- **Impacto:** Afeta S016-T8 (FichaItem) e S016-T12 (frontend inventario)
- **Recomendacao PM:** Opcao B (limite por tipo) e mais realista para RPG de mesa. Perguntar ao PO.

### Q-016-02: Stacking de bonus de itens
- **Opcao A:** Bonus acumulam (dois itens +2 FOR = +4 FOR total)
- **Opcao B:** Apenas o maior bonus vale (dois itens +2 FOR e +3 FOR = +3 FOR)
- **Opcao C:** Configuravel por tipo de efeito
- **Impacto:** Afeta S016-T10 (FichaCalculationService) diretamente
- **Recomendacao PM:** Opcao A (acumular) e mais simples para MVP. Perguntar ao PO.

### Q-016-03: Item quebrado (durabilidade 0)
- **Opcao A:** Item removido do inventario automaticamente
- **Opcao B:** Item fica no inventario mas sem bonus (marcado como "quebrado")
- **Opcao C:** Durabilidade e opcional — nem todo item tem
- **Impacto:** Afeta S016-T8 (FichaItem estados) e S016-T10 (calculos)
- **Recomendacao PM:** Opcao B + C (durabilidade opcional, quando chega a 0 perde bonus mas nao some). Perguntar ao PO.

### Q-016-04: Mecanica de reparo de itens
- **Opcao A:** Custo em moeda (ferreiro NPC) — requer sistema de moeda
- **Opcao B:** Mestre repara manualmente (sem custo mecanico)
- **Opcao C:** Item nao tem reparo — quando quebra, acabou
- **Opcao D:** Fora do MVP
- **Impacto:** Se incluso, adiciona ~2 tasks extras (endpoint + frontend)
- **Recomendacao PM:** Opcao D (fora do MVP) ou Opcao B (minimalista). Perguntar ao PO.

### Q-016-05: Item customizado pelo Mestre
- Mestre pode criar itens unicos que NAO estao no catalogo? (ex: "Espada do Rei Morto" com efeitos especiais)
- Se sim, como diferenciar de ItemConfig? Campo `isCustomizado` em FichaItem?
- **Impacto:** Afeta S016-T3 (ItemConfig) e S016-T8 (FichaItem)
- **Recomendacao PM:** Sim, o Mestre pode criar itens unicos como ItemConfig normais — ja sao configurados pelo Mestre por natureza do sistema. Nao precisa de flag especial. Dataset fornece apenas defaults iniciais.

### Q-016-06: Peso e capacidade de carga
- Sistema de peso existe? Se sim, quem define o peso maximo? (atributo FOR? fixo? configuravel?)
- Se nao, o campo `peso` em ItemConfig e apenas informativo?
- **Impacto:** Se peso for mecanico, adiciona ~2 tasks (calculo + validacao)
- **Recomendacao PM:** Campo `peso` informativo no MVP. Carga mecanica = pos-MVP. Perguntar ao PO.

---

## Riscos Identificados

| Risco | Impacto | Mitigacao |
|-------|---------|-----------|
| Spec 016 depende de Spec 007 (motor de calculos) para SD-2 | SD-2 bloqueado ate Spec 007 completa | SD-1 e SD-3 podem avancar independentemente |
| ItemEfeito replica logica de VantagemEfeito | Risco de duplicacao de codigo | Reutilizar infraestrutura de Spec 007 (mesmos enums, mesma logica no FichaCalculationService) |
| Escopo pode crescer com respostas do PO (Q-016-01 a Q-016-06) | +2 a +6 tasks extras | Respostas minimalistas para MVP; funcionalidades complexas para pos-MVP |
| 21 tasks adicionam ~25% ao backlog MVP | Atraso potencial no MVP | Priorizar SD-1 e SD-3 enquanto Sprint 2 roda; SD-2 entra no Sprint 3 |
| ClasseEquipamentoInicial afeta wizard (Spec 006) | Retrabalho se wizard ja implementado | Incluir hook no wizard para equipamentos iniciais desde o inicio |

---

## Historico de Revisoes

| Data | Revisao | Autor |
|------|---------|-------|
| 2026-04-04 | rev.1 — Criacao do documento de coordenacao | PM/Scrum Orchestrator |

---

*Este documento e a fonte de coordenacao entre BAs da Spec 016. Atualizar sempre que um BA concluir ou uma decisao do PO for tomada.*
