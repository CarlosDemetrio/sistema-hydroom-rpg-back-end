# Plan 012 — Níveis, Progressão e Level Up Frontend

> Plano de implementação para a Spec 012.
> Data: 2026-04-02
> Branch base: `feature/012-niveis-progressao-frontend`

---

## 1. Estado Atual (Auditoria 2026-04-02)

### Frontend — O que existe

| Componente / Artefato | Localização | Estado |
|----------------------|-------------|--------|
| `NiveisConfigComponent` | `features/mestre/pages/config/configs/niveis-config/` | Funcional com CRUD via drawer; todos os campos inclusive `permitirRenascimento`; sem formatação de milhar, sem validação de consistência de XP, sem coluna `permitirRenascimento` na tabela |
| `NivelConfigService` | `core/services/business/config/nivel-config.service.ts` | Funcional — estende `BaseConfigService<NivelConfig>` |
| `ConfigApiService` — NivelConfig | `core/services/api/config-api.service.ts` | Completo: listNiveis, getNivel, createNivel, updateNivel, deleteNivel |
| `ConfigApiService` — CategoriaVantagem | `core/services/api/config-api.service.ts` | Completo: todos os 5 métodos HTTP existem |
| Modelo `CategoriaVantagem` | `core/models/categoria-vantagem.model.ts` | Existe com campos: id, jogoId, nome, descricao, cor, ordemExibicao |
| `VantagensConfigComponent` | `features/mestre/pages/config/configs/vantagens-config/` | Usa CategoriaVantagem apenas como select no drawer de Vantagem. CRUD de categorias não existe |
| `ConfigStore` | `core/stores/config.store.ts` | Tem `niveis()` signal; sem `pontosVantagem()` nem sinal dedicado para categorias de vantagem standalone |
| Rota `/mestre/config/niveis` | `app.routes.ts` linha 91 | Ativa no sidebar |
| Rota `/mestre/config/vantagens` | `app.routes.ts` linha 103 | Ativa no sidebar — inclui CategoriaVantagem implicitamente |
| `FichaVantagensTabComponent` | `ficha-detail/components/ficha-vantagens-tab/` | Existe; campo `pontosVantagemRestantes` mockado como 0 |
| `FichaResumo` model | `core/models/ficha.model.ts` | Sem campos de pontos disponíveis |
| `FichaResumoResponse` backend | `dto/response/FichaResumoResponse.java` | Sem `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis`, `pontosVantagemDisponiveis` |

### Frontend — O que NÃO existe (gaps)

| Gap | Tipo | Severidade |
|-----|------|-----------|
| `PontosVantagemConfig` — model TypeScript | Modelo | CRITICO |
| `PontosVantagemConfig` — API service (config-api.service) | API | CRITICO |
| `PontosVantagemConfig` — business service | Business | CRITICO |
| `PontosVantagemConfigComponent` | Componente | CRITICO |
| Rota `/mestre/config/pontos-vantagem` | Rota | CRITICO |
| Entrada no sidebar para PontosVantagem | UI | MEDIO |
| `CategoriaVantagemConfigComponent` dedicado (com color picker) | Componente | MEDIO |
| Rota `/mestre/config/categorias-vantagem` | Rota | MEDIO |
| Entrada no sidebar para CategoriaVantagem | UI | MEDIO |
| `pontosAtributoDisponiveis` no `FichaResumo` TypeScript | Modelo | CRITICO |
| `pontosAtributoDisponiveis` no `FichaResumoResponse` Java | Backend | CRITICO |
| Painel de concessão de XP pelo Mestre | UI | CRITICO |
| Detecção de level up no FichaDetailPage | Lógica | CRITICO |
| Toast especial de level up | UI | ALTO |
| Animação CSS no header ao subir nível | UI | BAIXO |
| Badge de pontos pendentes no FichaHeader | UI | CRITICO |
| `LevelUpDialogComponent` (container + 3 steps) | Componente | CRITICO |
| Compra de vantagens conectada ao saldo real | Lógica | ALTO |
| Botão e UI de Renascimento | UI + Backend | MEDIO |
| Endpoint backend `POST /fichas/{id}/renascer` | Backend | MEDIO |
| Coluna `permitirRenascimento` na tabela NivelConfig | UI | BAIXO |
| Formatação de milhar em xpNecessaria | UI | BAIXO |
| Validação de consistência de XP (aviso) | UI | BAIXO |

---

## 2. Dependências entre Tasks

```
T1 (PontosVantagem API service)
  → T2 (PontosVantagemConfigComponent)

T3 (CategoriaVantagem component dedicado) — independente

T4 (NivelConfig ajustes UX) — independente

T5 (FichaResumoResponse backend — pontos disponíveis)
  → T6 (FichaResumo model TypeScript — pontos disponíveis)
    → T7 (Painel XP + detecção level up)
      → T8 (LevelUpDialogComponent — Step 1 atributos)
      → T9 (LevelUpDialogComponent — Step 2 aptidões)
      → T10 (LevelUpDialogComponent — Step 3 vantagens)
        → T11 (Conectar saldo de vantagens em FichaVantagensTab)

T12 (Renascimento backend endpoint) — independente
  → T13 (Renascimento UI)

T14 (NivelConfig sidebar + PontosVantagem sidebar + CategoriaVantagem sidebar) — depende de T2 e T3
```

---

## 3. Sequência de Implementação

### Fase 1 — Infraestrutura (sem bloqueadores)

**T1:** Modelo TypeScript + API service para PontosVantagemConfig
**T3:** CategoriaVantagemConfigComponent (color picker, CRUD dedicado)
**T4:** Ajustes no NivelConfigComponent (UX: milhar, coluna renascimento, aviso consistência)
**T5:** Backend — adicionar pontos disponíveis ao FichaResumoResponse

Estas tasks são independentes e podem ser feitas em paralelo.

### Fase 2 — Configuração de Pontos de Vantagem

**T2:** PontosVantagemConfigComponent (depende de T1)

### Fase 3 — Frontend de Ficha (depende de T5)

**T6:** Atualizar modelo TypeScript `FichaResumo` com campos de pontos
**T7:** Painel de XP (concessão pelo Mestre) + detecção de level up
**T8:** LevelUpDialogComponent + Step 1 (atributos)
**T9:** Step 2 (aptidões)
**T10:** Step 3 (vantagens — informativo)
**T11:** Conectar saldo real de pontos de vantagem em FichaVantagensTab

### Fase 4 — Renascimento (depende de backend T12)

**T12:** Backend — endpoint POST /fichas/{id}/renascer
**T13:** UI de renascimento no FichaDetail

### Fase 5 — Navegação

**T14:** Adicionar rotas e entradas no sidebar para PontosVantagem e CategoriaVantagem

---

## 4. Arquivos Afetados (Resumo)

### Novos arquivos (frontend)

```
src/app/core/models/pontos-vantagem-config.model.ts
src/app/core/services/business/config/pontos-vantagem-config.service.ts
src/app/features/mestre/pages/config/configs/pontos-vantagem-config/
  pontos-vantagem-config.component.ts
  pontos-vantagem-config.component.spec.ts
src/app/features/mestre/pages/config/configs/categorias-vantagem-config/
  categorias-vantagem-config.component.ts
  categorias-vantagem-config.component.spec.ts
src/app/features/jogador/pages/ficha-detail/components/level-up-dialog/
  level-up-dialog.component.ts
  level-up-dialog.component.spec.ts
  steps/level-up-atributos-step/
    level-up-atributos-step.component.ts
  steps/level-up-aptidoes-step/
    level-up-aptidoes-step.component.ts
  steps/level-up-vantagens-step/
    level-up-vantagens-step.component.ts
```

### Arquivos modificados (frontend)

```
src/app/core/models/ficha.model.ts                              — adicionar 3 campos em FichaResumo
src/app/core/models/config.models.ts                            — adicionar PontosVantagemConfig (ou novo arquivo)
src/app/core/services/api/config-api.service.ts                 — adicionar métodos PontosVantagemConfig
src/app/core/services/business/config/index.ts                  — exportar PontosVantagemConfigService
src/app/core/stores/config.store.ts                             — adicionar signal pontosVantagem()
src/app/features/mestre/pages/config/config-sidebar.component.ts — adicionar 2 entradas
src/app/features/mestre/pages/config/configs/niveis-config/     — ajustes UX
src/app/features/jogador/pages/ficha-detail/ficha-detail.component.ts — detecção level up
src/app/features/jogador/pages/ficha-detail/components/ficha-header/   — badge pontos + animação
src/app/features/jogador/pages/ficha-detail/components/ficha-vantagens-tab/ — conectar saldo real
src/app/app.routes.ts                                           — 2 novas rotas de config
```

### Arquivos modificados (backend)

```
src/main/java/.../dto/response/FichaResumoResponse.java         — 3 novos campos
src/main/java/.../service/FichaService.java                     — calcular pontos disponíveis
src/main/java/.../controller/FichaController.java               — novo endpoint /renascer (T12)
```
