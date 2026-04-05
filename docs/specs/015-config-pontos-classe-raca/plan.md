# Spec 015 — Plano de Implementacao: ConfigPontos Raca/Classe + DefaultProvider

> Spec: `015-config-pontos-classe-raca`
> Status: PLANEJADO
> Dependencias: Spec 004 (ClassePersonagem, Raca, VantagemConfig — todos implementados)
> Bloqueia: Spec 006 (wizard precisa de pontos corretos), Spec 007 (motor precisa de pontos)

---

## 1. Estado Atual

### O que ja existe

| Artefato | Status | Localizacao |
|----------|--------|-------------|
| ClassePersonagem (entity + CRUD) | Implementado | `model/ClassePersonagem.java` |
| Raca (entity + CRUD) | Implementado | `model/Raca.java` |
| ClasseBonus (sub-recurso) | Implementado | `model/ClasseBonus.java` |
| ClasseAptidaoBonus (sub-recurso) | Implementado | `model/ClasseAptidaoBonus.java` |
| RacaBonusAtributo (sub-recurso) | Implementado | `model/RacaBonusAtributo.java` |
| RacaClassePermitida (sub-recurso) | Implementado | `model/RacaClassePermitida.java` |
| NivelConfig (entity + CRUD) | Implementado | `model/NivelConfig.java` |
| FichaVantagem (entity) | Implementado | `model/FichaVantagem.java` |
| DefaultGameConfigProviderImpl | Implementado (com bugs) | `config/DefaultGameConfigProviderImpl.java` |
| GameConfigInitializerService | Implementado (com bugs) | `service/GameConfigInitializerService.java` |

### O que NAO existe (sera criado nesta spec)

| Artefato | Task |
|----------|------|
| ClassePontosConfig | T1 |
| ClasseVantagemPreDefinida | T1 |
| RacaPontosConfig | T1 |
| RacaVantagemPreDefinida | T1 |
| CRUD endpoints para as 4 novas entidades | T2 |
| Calculo de pontos com 3 fontes | T3 |
| Auto-concessao de vantagens | T4 |
| DefaultProvider corrigido e completo | T5 |
| Frontend: abas em ClassePersonagem | T6 |
| Frontend: abas em Raca | T7 |

---

## 2. Sequencia de Implementacao

### Fase 1 — Backend Core (T1-T4)

```
T1: Entidades + Repositories + DTOs + Mappers (4 novas entidades)
    |
    +---> T2: CRUD Endpoints (14 endpoints) + Testes de integracao
    |
    +---> T3: Calculo de pontosDisponiveis (atualizar FichaCalculationService)
    |          Depende de T1 (repositories)
    |
    +---> T4: Auto-concessao de vantagens (triggers em FichaService)
              Depende de T1 (repositories) + FichaVantagem existente
```

T2, T3 e T4 podem ser paralelizadas apos T1 (trabalham em arquivos diferentes).

### Fase 2 — Backend Infra + Frontend (T5-T7)

```
T5: Corrigir DefaultProvider + adicionar defaults novos
    Independente de T1-T4 (nao usa as novas entidades no provider imediatamente,
    mas adiciona BonusConfig, PontosVantagemConfig, CategoriaVantagem, vantagens)

T6: Frontend ClassePersonagem — abas Pontos e Vantagens Pre-definidas
    Depende de T2 (endpoints disponiveis)

T7: Frontend Raca — abas Pontos e Vantagens Pre-definidas
    Depende de T2 (endpoints disponiveis)
```

T5 e independente e pode ser paralelizada com qualquer task.
T6 e T7 podem ser paralelizadas entre si.

---

## 3. Dependencias entre Tasks

```
T1 (entidades + repos + DTOs)
 ├── T2 (CRUD endpoints + testes)
 │    ├── T6 (frontend Classe)
 │    └── T7 (frontend Raca)
 ├── T3 (calculo pontos)
 └── T4 (auto-concessao vantagens)

T5 (DefaultProvider) — INDEPENDENTE (paralelizavel com T1-T4)
```

### Dependencias externas

| Task | Depende de (externo) | Natureza |
|------|---------------------|----------|
| T3 | FichaCalculationService existente | Adicionar ao servico existente |
| T4 | FichaService existente (criarFicha, recalcularNivel) | Adicionar triggers |
| T4 | FichaVantagem entity existente | Adicionar campo `origem` |
| T5 | DefaultGameConfigProviderImpl + GameConfigInitializerService | Correcao de bugs |

---

## 4. Arquivos Impactados por Task

### T1 — Novas entidades

| Arquivo | Operacao |
|---------|----------|
| `model/ClassePontosConfig.java` | CRIAR |
| `model/ClasseVantagemPreDefinida.java` | CRIAR |
| `model/RacaPontosConfig.java` | CRIAR |
| `model/RacaVantagemPreDefinida.java` | CRIAR |
| `model/ClassePersonagem.java` | EDITAR — adicionar `Set<ClassePontosConfig>` e `Set<ClasseVantagemPreDefinida>` |
| `model/Raca.java` | EDITAR — adicionar `Set<RacaPontosConfig>` e `Set<RacaVantagemPreDefinida>` |
| `repository/ClassePontosConfigRepository.java` | CRIAR |
| `repository/ClasseVantagemPreDefinidaRepository.java` | CRIAR |
| `repository/RacaPontosConfigRepository.java` | CRIAR |
| `repository/RacaVantagemPreDefinidaRepository.java` | CRIAR |
| `dto/request/configuracao/ClassePontosConfigRequest.java` | CRIAR |
| `dto/request/configuracao/ClasseVantagemPreDefinidaRequest.java` | CRIAR |
| `dto/request/configuracao/RacaPontosConfigRequest.java` | CRIAR |
| `dto/request/configuracao/RacaVantagemPreDefinidaRequest.java` | CRIAR |
| `dto/response/configuracao/ClassePontosConfigResponse.java` | CRIAR |
| `dto/response/configuracao/ClasseVantagemPreDefinidaResponse.java` | CRIAR |
| `dto/response/configuracao/RacaPontosConfigResponse.java` | CRIAR |
| `dto/response/configuracao/RacaVantagemPreDefinidaResponse.java` | CRIAR |
| `mapper/configuracao/ClassePontosConfigMapper.java` | CRIAR |
| `mapper/configuracao/ClasseVantagemPreDefinidaMapper.java` | CRIAR |
| `mapper/configuracao/RacaPontosConfigMapper.java` | CRIAR |
| `mapper/configuracao/RacaVantagemPreDefinidaMapper.java` | CRIAR |

### T2 — Controllers + Services + Testes

| Arquivo | Operacao |
|---------|----------|
| `service/ClassePontosConfigService.java` | CRIAR |
| `service/ClasseVantagemPreDefinidaService.java` | CRIAR |
| `service/RacaPontosConfigService.java` | CRIAR |
| `service/RacaVantagemPreDefinidaService.java` | CRIAR |
| `controller/configuracao/ClassePersonagemController.java` | EDITAR — adicionar endpoints sub-recurso |
| `controller/configuracao/RacaController.java` | EDITAR — adicionar endpoints sub-recurso |
| Testes de integracao (4 novos arquivos) | CRIAR |

### T3 — Calculo de pontos

| Arquivo | Operacao |
|---------|----------|
| `service/FichaCalculationService.java` | EDITAR — metodo de calculo de pontos |
| Teste de integracao | CRIAR ou EDITAR |

### T4 — Auto-concessao

| Arquivo | Operacao |
|---------|----------|
| `model/FichaVantagem.java` | EDITAR — adicionar campo `origem` (enum) |
| `model/OrigemVantagem.java` (enum) | CRIAR |
| `service/FichaService.java` | EDITAR — adicionar triggers |
| `service/VantagemAutoConcessaoService.java` | CRIAR |
| Teste de integracao | CRIAR |

### T5 — DefaultProvider

| Arquivo | Operacao |
|---------|----------|
| `config/DefaultGameConfigProviderImpl.java` | EDITAR — corrigir bugs + adicionar defaults |
| `config/GameDefaultConfigProvider.java` | EDITAR — adicionar metodos na interface |
| `service/GameConfigInitializerService.java` | EDITAR — descomentar limitadores, fix limitadorAtributo |
| `dto/defaults/BonusConfigDTO.java` | CRIAR |
| `dto/defaults/PontosVantagemConfigDTO.java` | CRIAR (se nao existir) |
| `dto/defaults/CategoriaVantagemDTO.java` | CRIAR (se nao existir) |
| Teste do provider | CRIAR |

### T6 / T7 — Frontend

| Arquivo | Operacao |
|---------|----------|
| `classes-config/` componentes | EDITAR — adicionar abas |
| `racas-config/` componentes | EDITAR — adicionar abas |
| Models e services Angular | CRIAR |

---

## 5. Plano Anti-Conflito

Para evitar conflitos de merge se T2/T3/T4 forem paralelizadas:

| Agente | Arquivos exclusivos | NAO tocar |
|--------|-------------------|-----------|
| Agente T2 | Controllers, Services CRUD, testes CRUD | FichaCalculationService, FichaService |
| Agente T3 | FichaCalculationService (pontosDisponiveis) | Controllers, FichaService |
| Agente T4 | FichaService, VantagemAutoConcessaoService, FichaVantagem | Controllers, FichaCalculationService |
| Agente T5 | DefaultGameConfigProviderImpl, GameConfigInitializerService | Tudo acima |

---

## 6. Estimativas

| Task | Complexidade | Estimativa |
|------|-------------|-----------|
| T1 | Media (4 entidades padrao) | 4-6 horas |
| T2 | Alta (14 endpoints + testes) | 6-8 horas |
| T3 | Media (atualizar calculo existente) | 2-3 horas |
| T4 | Alta (triggers + nova entidade + testes) | 4-6 horas |
| T5 | Alta (muitas correcoes + defaults) | 6-8 horas |
| T6 | Media (abas em componente existente) | 3-4 horas |
| T7 | Media (similar a T6) | 3-4 horas |
| **Total** | | **28-39 horas** |

---

## 7. Riscos e Mitigacoes

| Risco | Impacto | Mitigacao |
|-------|---------|-----------|
| PA-015-01/02/03 nao respondidos pelo PO | T5 defaults incompletos | Implementar provider com placeholder; defaults confirmados podem ser adicionados depois |
| FichaVantagem.origem impacta dados existentes | Migration pode falhar | Campo nullable com DEFAULT 'JOGADOR' para retrocompatibilidade |
| 14 endpoints novos aumentam superficie de ataque | Seguranca | Seguir padrao @PreAuthorize existente; validar jogo_id em todas as operacoes |
| Conflito com Spec 007 T0 no FichaCalculationService | Merge conflicts | T3 deve ser feita ANTES ou DEPOIS de 007-T0, nunca em paralelo |

---

*Produzido por: PM/Scrum Master | 2026-04-04*
