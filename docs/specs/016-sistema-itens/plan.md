# Spec 016 — Plano de Implementacao: Sistema de Itens e Equipamentos

> Spec: `016-sistema-itens`
> Baseado em: spec.md v1.0 | 2026-04-04
> Estimativa total: ~15-18 dias de trabalho
> Depende de: Spec 007 (FichaCalculationService corrigido), Spec 006 (Ficha funcional)

---

## Fases e Dependencias

```
FASE 1 (Backend — Configuracao)
  T1: RaridadeItemConfig + TipoItemConfig + CRUD
  T2: ItemConfig + ItemEfeito + ItemRequisito + CRUD
  T3: ClasseEquipamentoInicial (sub-recurso de ClassePersonagem)
  [T1 e T2 paralelos; T3 depende de T2]

FASE 1 (Backend — Ficha)
  T4: FichaItem entity + endpoints (add, equipar, remover, listar)
  T5: FichaCalculationService — aplicar ItemEfeito de itens equipados
  [T4 depende de T1+T2; T5 depende de T4 + Spec 007 T0]

FASE 1 (Backend — Dataset e Testes)
  T6: DefaultGameConfigProviderImpl — dataset completo de 40 itens
  T7: Testes de integracao completos
  [T6 depende de T1+T2+T3; T7 depende de T4+T5+T6]

FASE 3 (Frontend)
  T8: UI Raridades e Tipos de Item
  T9: UI Catalogo de Itens (ItemConfig + ItemEfeito + ItemRequisito)
  T10: UI ClasseEquipamentoInicial
  T11: UI Inventario na ficha (FichaItem)
  [T8 primeiro; T9 depende de T8; T10 depende de T9; T11 depende de T8+T9]
```

---

## Fase 1 — Backend: Configuracao de Catalogo

### T1 — RaridadeItemConfig + TipoItemConfig
**Estimativa:** 1-2 dias
**Arquivos novos:**
- `model/RaridadeItemConfig.java` — entidade JPA
- `model/TipoItemConfig.java` — entidade JPA
- `repository/RaridadeItemConfigRepository.java`
- `repository/TipoItemConfigRepository.java`
- `service/configuracao/RaridadeItemConfigService.java`
- `service/configuracao/TipoItemConfigService.java`
- `dto/request/configuracao/RaridadeItemConfigRequest.java`
- `dto/request/configuracao/TipoItemConfigRequest.java`
- `dto/response/configuracao/RaridadeItemConfigResponse.java`
- `dto/response/configuracao/TipoItemConfigResponse.java`
- `mapper/configuracao/RaridadeItemConfigMapper.java`
- `mapper/configuracao/TipoItemConfigMapper.java`
- `controller/configuracao/RaridadeItemConfigController.java`
- `controller/configuracao/TipoItemConfigController.java`

**Padrao:** Seguir exatamente o padrao de `AtributoConfig` (AbstractConfiguracaoService, BaseEntity, ConfiguracaoEntity).

---

### T2 — ItemConfig + ItemEfeito + ItemRequisito
**Estimativa:** 2-3 dias
**Dependencias:** T1 concluido
**Arquivos novos:**
- `model/ItemConfig.java` — entidade com FKs para Raridade + Tipo + List<ItemEfeito> + List<ItemRequisito>
- `model/ItemEfeito.java` — sub-entidade (sem BaseEntity — embedded no ItemConfig)
- `model/ItemRequisito.java` — sub-entidade
- `model/enums/TipoItemEfeito.java` — enum com 7 valores
- `model/enums/TipoRequisito.java` — enum com 7 valores
- Repositories, Services, DTOs, Mappers, Controllers para ItemConfig
- Controllers de sub-recurso para ItemEfeito e ItemRequisito

**Atencao:**
- `ItemEfeito` e `ItemRequisito` sao criados/deletados junto ao ItemConfig ou via sub-endpoints dedicados
- Validar que `atributoAlvo`, `bonusAlvo`, `aptidaoAlvo` pertencem ao mesmo jogo do ItemConfig
- Validar formula em `ItemEfeito.formula` via `FormulaEvaluatorService.isValid()`

---

### T3 — ClasseEquipamentoInicial
**Estimativa:** 1 dia
**Dependencias:** T2 concluido
**Arquivos novos:**
- `model/ClasseEquipamentoInicial.java`
- `repository/ClasseEquipamentoInicialRepository.java`
- `service/ClasseEquipamentoInicialService.java`
- DTOs + Mapper + Controller (sub-recurso de ClassePersonagem)

**Atencao:**
- Validar que `itemConfig.jogo == classe.jogo` (itens e classes devem ser do mesmo jogo)
- Sub-recurso: `GET/POST/PUT/DELETE /api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais`

---

## Fase 1 (cont.) — Backend: Ficha

### T4 — FichaItem Entity + Endpoints
**Estimativa:** 2-3 dias
**Dependencias:** T1, T2
**Arquivos novos/editados:**
- `model/FichaItem.java` — entidade com FK para Ficha, ItemConfig (nullable), RaridadeItemConfig
- `repository/FichaItemRepository.java` — incluindo query com JOIN FETCH para evitar N+1
- `service/FichaItemService.java` — logica de adicionar (validar raridade, requisitos), equipar, remover
- DTOs de request e response
- `controller/FichaItemController.java`

**Logica critica em FichaItemService:**
1. `adicionarItem()`: checar role do usuario vs `podeJogadorAdicionar`; validar requisitos (ou bypassar se Mestre + forcarAdicao=true)
2. `equiparItem()`: checar `duracaoAtual != 0`; persistir; disparar recalculo da ficha
3. `desequiparItem()`: persistir; disparar recalculo
4. `decrementarDurabilidade()`: se chegar a 0, forcar `equipado=false` + disparar recalculo

---

### T5 — FichaCalculationService: Aplicar ItemEfeito
**Estimativa:** 2 dias
**Dependencias:** T4, Spec 007 T0 (motor corrigido)
**Arquivos editados:**
- `model/FichaAtributo.java` — adicionar campo `itens` (int, default 0)
- `model/FichaBonus.java` — adicionar campo `itens` (int, default 0)
- `model/FichaVida.java` — adicionar campo `itens` (int, default 0)
- `model/FichaEssencia.java` — adicionar campo `itens` (int, default 0)
- `service/FichaCalculationService.java` — adicionar Passo 5: aplicar ItemEfeito

**Sequencia de calculo apos T5 (ordem completa):**
```
Passo 1: reset (zeros) — existente apos Spec 007 T0
Passo 2: bonus racial (RacaBonusAtributo) — existente apos Spec 007 T0
Passo 3: bonus de classe em BonusConfig — existente apos Spec 007 T0
Passo 4: bonus de classe em aptidoes — existente apos Spec 007 T0
Passo 5: VantagemEfeito — existente apos Spec 007 T1-T6
Passo 6: ItemEfeito de itens equipados (NOVO — esta task)
Passo 7: calcular totais de atributos
Passo 8: calcular formulas de Impeto
Passo 9: calcular BonusConfig (formulas)
Passo 10: calcular Vida Total, Essencia Total, Ameaca
```

**Metodo novo em FichaCalculationService:**
```java
private void aplicarItemEfeito(
    List<FichaItem> itensEquipados,
    List<FichaAtributo> atributos,
    List<FichaAptidao> aptidoes,
    List<FichaBonus> bonus,
    FichaVida vida,
    FichaEssencia essencia) {

    for (FichaItem fichaItem : itensEquipados) {
        if (!fichaItem.isEquipado()) continue;
        if (fichaItem.getDuracaoAtual() != null && fichaItem.getDuracaoAtual() <= 0) continue;

        List<ItemEfeito> efeitos = fichaItem.getItemConfig() != null
            ? fichaItem.getItemConfig().getEfeitos()
            : Collections.emptyList();

        for (ItemEfeito efeito : efeitos) {
            switch (efeito.getTipoEfeito()) {
                case BONUS_ATRIBUTO -> aplicarBonusAtributoItem(efeito, atributos);
                case BONUS_APTIDAO -> aplicarBonusAptidaoItem(efeito, aptidoes);
                case BONUS_DERIVADO -> aplicarBonusDerivadoItem(efeito, bonus);
                case BONUS_VIDA -> vida.setItens(vida.getItens() + valorEfeito(efeito));
                case BONUS_ESSENCIA -> essencia.setItens(essencia.getItens() + valorEfeito(efeito));
                case FORMULA_CUSTOMIZADA -> aplicarFormulaCustomizadaItem(efeito, atributos, bonus, aptidoes);
                case EFEITO_DADO -> aplicarEfeitoDadoItem(efeito, ficha); // ficha passada como param
            }
        }
    }
}
```

**Reset no Passo 1 (adicionar ao metodo existente `resetarCamposDerivaveis`):**
```java
atributos.forEach(a -> a.setItens(0));  // NOVO
bonus.forEach(b -> b.setItens(0));       // NOVO
vida.setItens(0);                        // NOVO
essencia.setItens(0);                    // NOVO
// campos existentes: outros, vantagens, classe (inalterados)
```

---

## Fase 2 — Backend: Dataset e Testes

### T6 — DefaultGameConfigProviderImpl: Dataset Completo
**Estimativa:** 1-2 dias
**Dependencias:** T1, T2, T3
**Arquivos editados:**
- `service/GameConfigInitializerService.java` — chamar metodo de inicializacao de itens
- `config/defaults/DefaultGameConfigProviderImpl.java` — adicionar metodos de dataset

**Dataset completo:** Ver `dataset/dataset-itens-default.md` para a lista de 40 itens com todos os campos.

**Ordem de criacao no DefaultProvider:**
1. RaridadeItemConfig (7 raridades)
2. TipoItemConfig (20 tipos)
3. ItemConfig (40 itens) — referencias por nome aos tipos e raridades criados acima
4. ClasseEquipamentoInicial (para as 12 classes)

---

### T7 — Testes de Integracao Completos
**Estimativa:** 2-3 dias
**Dependencias:** T1, T2, T3, T4, T5, T6
**Arquivos novos:**
- `RaridadeItemConfigServiceIntegrationTest.java` — estende `BaseConfiguracaoServiceIntegrationTest`
- `TipoItemConfigServiceIntegrationTest.java` — estende `BaseConfiguracaoServiceIntegrationTest`
- `ItemConfigServiceIntegrationTest.java` — testes CRUD + sub-recursos
- `FichaItemServiceIntegrationTest.java` — testes de adicionar, equipar, requisitos, raridade, durabilidade
- `FichaCalculationItemEfeitoIntegrationTest.java` — testes de todos os 7 tipos de ItemEfeito no motor

**Cenarios criticos a cobrir:**
- Jogador adiciona item Comum: OK
- Jogador adiciona item Incomum: 403
- Equipar item com duracao=0: 422
- Desequipar: bonus removido no proximo calculo
- Idempotencia: recalcular 2x nao acumula
- ClasseEquipamentoInicial aplicado na criacao de ficha (integrado com Spec 006 T5)
- Soft delete de ItemConfig nao remove FichaItem

---

## Fase 3 — Frontend

### T8 — UI Raridades e Tipos de Item
**Estimativa:** 1 dia
**Arquivos novos (Angular):**
- `features/mestre/pages/raridades-item/raridades-item.component.ts`
- `features/mestre/pages/tipos-item/tipos-item.component.ts`
- Services e models correspondentes

**Pattern:** Seguir padrao dos outros components de config (signal stores, PrimeNG DataTable, reordenacao drag-and-drop).

---

### T9 — UI Catalogo de Itens
**Estimativa:** 3 dias
**Dependencias:** T8
**Arquivos novos:**
- `features/mestre/pages/itens-config/itens-config.component.ts` — listagem + filtros
- `features/mestre/pages/itens-config/item-form/item-form.component.ts` — formulario com abas: Dados Basicos, Efeitos, Requisitos
- `features/mestre/pages/itens-config/item-efeito-form/item-efeito-form.component.ts`
- Signal stores, services, models

**UX critico:**
- Formulario de ItemEfeito deve ter seletor dinamico de alvo (atributo, bonus ou aptidao) baseado no tipo escolhido
- Lista de itens com filtro por tipo (arvore categoria/subcategoria), raridade e nome
- Chip colorido de raridade baseado em `RaridadeItemConfig.cor`

---

### T10 — UI ClasseEquipamentoInicial
**Estimativa:** 1 dia
**Dependencias:** T9
**Arquivos novos/editados:**
- `features/mestre/pages/classes-personagem/classe-detail/` — adicionar aba "Equipamentos Iniciais"
- Componente de configuracao de grupos de escolha (drag-and-drop para ordenar grupos)

---

### T11 — UI Inventario na Ficha
**Estimativa:** 3 dias
**Dependencias:** T8, T9
**Arquivos novos/editados:**
- `features/ficha/components/ficha-inventario-tab/ficha-inventario-tab.component.ts` — aba de inventario
- `features/ficha/components/ficha-item-card/ficha-item-card.component.ts` — card de item (cor de raridade, badge equipado, barra de durabilidade)
- `features/ficha/components/adicionar-item-dialog/adicionar-item-dialog.component.ts` — busca no catalogo + filtros

**UX critico:**
- Separar visualmente itens equipados (topo) dos itens no inventario (abaixo)
- Peso total com barra de progresso vs capacidade de carga
- Chip de raridade colorido (hex da RaridadeItemConfig)
- Badge "Quebrado" em vermelho quando duracao=0
- Mestre ve botao de decrementar durabilidade

---

## Estimativa Consolidada

| Task | Tipo | Estimativa | Dependencias |
|------|------|-----------|-------------|
| T1 | Backend | 1-2 dias | Spec 007 T0 concluido |
| T2 | Backend | 2-3 dias | T1 |
| T3 | Backend | 1 dia | T2 |
| T4 | Backend | 2-3 dias | T1, T2 |
| T5 | Backend | 2 dias | T4, Spec 007 T0 |
| T6 | Backend | 1-2 dias | T1, T2, T3 |
| T7 | Backend | 2-3 dias | T1-T6 |
| T8 | Frontend | 1 dia | T1 (backend) |
| T9 | Frontend | 3 dias | T2, T8 |
| T10 | Frontend | 1 dia | T9 |
| T11 | Frontend | 3 dias | T4, T8, T9 |
| **TOTAL** | | **~20-24 dias** | |

> Nota: Paralelo possivel entre T1+T2+T3 (backend) e T8 (frontend se T1 backend estiver disponivel via mock/contrato). Estimativas conservadoras — desenvolvedor familiarizado com o projeto pode executar mais rapido.

---

## Decisoes de Produto Pendentes (resolucoes necessarias antes das tasks)

| Decisao | Task bloqueada | Responsavel |
|---------|---------------|------------|
| PA-016-01: Penalidade de sobrecarga e MVP? | T4 (logica de validacao), T11 (UX) | PO |
| PA-016-02: Passo de equipamentos no wizard? | T7 (integracao Spec 006), T11 | PO |
| PA-016-03: `FichaAptidao.itens` necessario? | T5 (SCHEMA-016-02) | PO + Tech Lead |
| PA-016-04: Item customizado com efeitos no MVP? | T4, T11 | PO |
| PA-016-05: Municao stackeia automaticamente? | T4 (servico) | PO |
| PA-016-06: Jogador ve catalogo completo? | T11 (RF-07) | PO |

---

*Produzido por: Business Analyst/PO | 2026-04-04*
