> **Status: VALIDO** | Ultima revisao: 2026-04-02
> Sub-recursos implementados conforme esta analise: ClasseBonus, ClasseAptidaoBonus, RacaBonusAtributo, RacaClassePermitida. Endpoints dedicados com JOIN FETCH.
> O que mudou desde a escrita: todos os sub-recursos tem endpoints REST dedicados. Frontend ainda nao consome (SP2-T02, SP2-T03 pendentes).

# BA-CLASSES-RACAS.md — Análise Completa: Classes de Personagem e Raças

> Documento de análise de negócio para as entidades `ClassePersonagem` (com `ClasseBonus` e `ClasseAptidaoBonus`) e `Raca` (com `RacaBonusAtributo` e `RacaClassePermitida`).
> Destina-se a guiar o desenvolvimento frontend, testes de integração e alinhamento de equipe.

---

## 1. Visão Geral

Classe e Raça definem a **identidade mecânica** do personagem no mundo de Klayrah. São escolhidas no momento da criação da ficha e, em regra, não mudam depois.

- **ClassePersonagem** — o "papel" do personagem (Guerreiro, Mago, Ladrão...). Cada classe concede bônus automáticos em combate (`ClasseBonus`) e em aptidões específicas (`ClasseAptidaoBonus`). Um Guerreiro ganha +1 em B.B.A por nível; um Ladrão ganha bônus fixo em Furtividade.
- **Raca** — a espécie do personagem (Humano, Elfo, Anão...). Cada raça pode dar modificadores positivos ou negativos em atributos (`RacaBonusAtributo`) e restringir quais classes o personagem pode escolher (`RacaClassePermitida`).

Ambas são configuradas pelo Mestre. O template Klayrah Padrão já inclui 12 classes e múltiplas raças com todas as relações.

---

## 2. Entidades e Relacionamentos

```
Jogo
 |
 +-- ClassePersonagem (N)          ← Guerreiro, Mago, Ladrão...
 |         |
 |         +-- ClasseBonus (N)     ← bônus em BonusConfig, crescente por nível
 |         |       classe_id  → ClassePersonagem
 |         |       bonus_id   → BonusConfig
 |         |       valorPorNivel → BigDecimal (pode ser fracionário: 0.5/nível)
 |         |
 |         +-- ClasseAptidaoBonus (N)   ← bônus fixo em AptidaoConfig
 |                 classe_id  → ClassePersonagem
 |                 aptidao_id → AptidaoConfig
 |                 bonus      → Integer (min 0, fixo, não crescente)
 |
 +-- Raca (N)                      ← Humano, Elfo, Anão...
           |
           +-- RacaBonusAtributo (N)    ← bônus (+ ou -) em AtributoConfig
           |       raca_id     → Raca
           |       atributo_id → AtributoConfig
           |       bonus       → Integer (pode ser negativo: penalidade)
           |
           +-- RacaClassePermitida (N)  ← restrição: quais classes esta raça pode ter
                   raca_id  → Raca
                   classe_id → ClassePersonagem

Ficha
 |
 +-- classeId    → ClassePersonagem (FK na ficha)
 +-- racaId      → Raca (FK na ficha)
```

### Cardinalidades

- `Jogo → ClassePersonagem`: 1:N, unique `(jogo_id, nome)`
- `ClassePersonagem → ClasseBonus`: 1:N, unique `(classe_id, bonus_id)` — cada par classe+bônus só uma vez
- `ClassePersonagem → ClasseAptidaoBonus`: 1:N, unique `(classe_id, aptidao_id)` — cada par classe+aptidão só uma vez
- `Jogo → Raca`: 1:N, unique `(jogo_id, nome)`
- `Raca → RacaBonusAtributo`: 1:N, unique `(raca_id, atributo_id)` — cada atributo só uma vez por raça
- `Raca → RacaClassePermitida`: 1:N, unique `(raca_id, classe_id)` — cada classe só uma vez por raça
- `Ficha → ClassePersonagem`: N:1 (uma ficha tem uma classe)
- `Ficha → Raca`: N:1 (uma ficha tem uma raça)

---

## 3. Campos e Validações

### ClassePersonagem

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK, gerado | 3 |
| `jogo` | FK → Jogo | Sim | NOT NULL | — |
| `nome` | String | Sim | min 1, max 100, unique por jogo | "Guerreiro" |
| `descricao` | String | Não | TEXT (sem limite rígido) | "Especialista em combate corpo a corpo. Recebe bônus crescentes em B.B.A por nível." |
| `bonusConfig` | Set<ClasseBonus> | — | Gerenciado via sub-endpoint | [+1 B.B.A por nível] |
| `aptidaoBonus` | Set<ClasseAptidaoBonus> | — | Gerenciado via sub-endpoint | [+2 em Intimidação] |
| `ordemExibicao` | Integer | Sim | default 0 | 1 |

### ClasseBonus

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK | — |
| `classe` | FK → ClassePersonagem | Sim | NOT NULL | Guerreiro |
| `bonus` | FK → BonusConfig | Sim | NOT NULL | B.B.A |
| `valorPorNivel` | BigDecimal | Sim | precision 10, scale 2; qualquer valor | 1.00 (ou 0.50 para crescimento lento) |

**Regra crítica:** `valorPorNivel` pode ser fracionário (ex: 0.5). Isso permite que a classe conceda +1 B.B.A a cada 2 níveis. O sistema acumula os valores e pode arredondar ao calcular a ficha.

### ClasseAptidaoBonus

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK | — |
| `classe` | FK → ClassePersonagem | Sim | NOT NULL | Ladrão |
| `aptidao` | FK → AptidaoConfig | Sim | NOT NULL | Furtividade |
| `bonus` | Integer | Sim | min 0, NOT NULL | 2 |

**Nota:** `bonus` é fixo (não cresce por nível) e nunca negativo. Representa um treinamento específico da classe na aptidão.

### Raca

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK | 2 |
| `jogo` | FK → Jogo | Sim | NOT NULL | — |
| `nome` | String | Sim | min 1, max 100, unique por jogo | "Elfo" |
| `descricao` | String | Não | TEXT | "Povo eterno das florestas. +2 Agilidade, -1 Vigor." |
| `bonusAtributos` | Set<RacaBonusAtributo> | — | Sub-endpoint | [AGI+2, VIG-1] |
| `classesPermitidas` | Set<RacaClassePermitida> | — | Sub-endpoint (vazio = todas permitidas) | [Mago, Arqueiro] |
| `ordemExibicao` | Integer | Sim | default 0 | 2 |

### RacaBonusAtributo

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK | — |
| `raca` | FK → Raca | Sim | NOT NULL | Elfo |
| `atributo` | FK → AtributoConfig | Sim | NOT NULL | Agilidade |
| `bonus` | Integer | Sim | NOT NULL, pode ser negativo | +2 ou -1 |

**Regra crítica:** `bonus` pode ser negativo — é uma **penalidade racial** (ex: Anão -1 Agilidade). O frontend deve deixar claro que valores negativos são aceitos e representam desvantagens.

### RacaClassePermitida

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK | — |
| `raca` | FK → Raca | Sim | NOT NULL | Elfo |
| `classe` | FK → ClassePersonagem | Sim | NOT NULL | Mago |

---

## 4. Regras de Negócio Críticas

1. **Classes permitidas vazia = todas as classes disponíveis**: se `Raca.classesPermitidas` estiver vazio, o personagem pode escolher qualquer classe do jogo. A restrição só entra em vigor quando pelo menos uma classe está cadastrada na lista. O frontend deve exibir "Todas as classes disponíveis" quando a lista estiver vazia.

2. **Uma classe por ficha**: cada `Ficha` tem exatamente uma `ClassePersonagem`. Não existe multiclasse — a classe é escolhida uma vez e define os bônus permanentes.

3. **Uma raça por ficha**: cada `Ficha` tem exatamente uma `Raca`. Imutável após criação (regra de negócio, não constraint — mas a UX deve impedir alteração ou avisar sobre consequências).

4. **Bônus de classe são aplicados por nível**: `ClasseBonus.valorPorNivel` é multiplicado pelo nível atual do personagem para calcular o bônus total da classe em determinado BonusConfig.

5. **Bônus de aptidão são fixos**: `ClasseAptidaoBonus.bonus` não escala com o nível — é um bônus permanente e fixo que vai para o campo "Classe" de cada `FichaAptidao`.

6. **Bônus racial vai para "Outros" do atributo**: `RacaBonusAtributo.bonus` é somado ao campo `outros` de `FichaAtributo`. Pode ser negativo (penalidade).

7. **Unique constraint nos sub-recursos**: não é possível adicionar o mesmo `BonusConfig` duas vezes a uma classe, nem o mesmo `AtributoConfig` duas vezes a uma raça. O backend retorna HTTP 409 se tentado.

8. **Soft delete dos sub-recursos**: `ClasseBonus`, `ClasseAptidaoBonus` e `RacaClassePermitida` têm `@SQLRestriction("deleted_at IS NULL")` — são invisíveis após deleção. `RacaBonusAtributo` não tem esta annotation mas segue o padrão `BaseEntity`.

9. **Fichas existentes**: ao deletar uma `ClassePersonagem` ou `Raca` que esteja referenciada em fichas ativas, o backend deve impedir (FK constraint) ou o frontend deve avisar. Validar antes de permitir a deleção.

---

## 5. Fluxo do Mestre

### Configurar Classes:

```
1. Criar ClassePersonagem (nome, descrição, ordem)
2. Abrir detalhe da classe → aba "Bônus em Bônus"
   → Adicionar ClasseBonus: selecionar BonusConfig + definir valorPorNivel
   → Exemplo: Guerreiro → B.B.A → 1.0 por nível
3. Abrir detalhe → aba "Bônus em Aptidões"
   → Adicionar ClasseAptidaoBonus: selecionar AptidaoConfig + bônus fixo
   → Exemplo: Ladrão → Furtividade → +2
4. Repetir para as 12 classes do template
```

### Configurar Raças:

```
1. Criar Raca (nome, descrição, ordem)
2. Abrir detalhe → aba "Bônus de Atributos"
   → Adicionar RacaBonusAtributo: selecionar AtributoConfig + valor (positivo ou negativo)
   → Exemplo: Elfo → Agilidade → +2; Elfo → Vigor → -1
3. Abrir detalhe → aba "Classes Permitidas" (opcional — vazio = todas)
   → Adicionar RacaClassePermitida: selecionar ClassePersonagem
   → Exemplo: Elfo → Mago; Elfo → Arqueiro (se Elfos não puderem ser Guerreiros)
4. Repetir para cada raça
```

**Pré-requisito**: `AtributoConfig`, `BonusConfig` e `AptidaoConfig` devem existir antes de configurar bônus de classe/raça.

---

## 6. Impacto na Ficha

**ClassePersonagem na Ficha:**
- Campo `classeId` na `Ficha` armazena a escolha
- `FichaBonus.classe` = `ClasseBonus.valorPorNivel * nivel_atual` (calculado automaticamente)
- `FichaAptidao.classe` = `ClasseAptidaoBonus.bonus` (fixo, copiado direto)
- Exibição na ficha: no detalhamento de cada Bônus, uma linha "Classe: +X"

**Raca na Ficha:**
- Campo `racaId` na `Ficha`
- `FichaAtributo.outros` recebe o `RacaBonusAtributo.bonus` (pode ser negativo)
- Se `classesPermitidas` não vazio: a ficha só pode ser criada com uma classe da lista

---

## 7. Design de Tela (UX)

### ClassePersonagem — Tela Principal

**Componente recomendado:** Tabela com linha expansível (accordion) mostrando bônus e aptidões inline. Alternativa: drawer lateral com abas ao clicar na linha.

**Layout da listagem:**
```
[ + Nova Classe ]
┌─────────────────┬────────────────────────┬──────────────────────┐
│ Nome            │ Bônus (preview)         │ Aptidões (preview)   │
├─────────────────┼────────────────────────┼──────────────────────┤
│ Guerreiro    ▼  │ B.B.A +1/nível          │ Intimidação +2        │
│   [expandido]   │ [+ Adicionar Bônus]     │ [+ Adicionar Aptidão] │
│ Mago         ▶  │ B.B.M +1/nível          │ —                    │
└─────────────────┴────────────────────────┴──────────────────────┘
```

**Formulário da classe (drawer ou modal):**
```
[Nome*]
[Descrição]  (textarea)
[Ordem de Exibição]

--- Bônus em Bônus de Combate ---
┌─────────────────┬─────────────────┬────────┐
│ Bônus           │ Valor por nível │ Ação   │
├─────────────────┼─────────────────┼────────┤
│ B.B.A           │ 1.00            │ [✕]   │
│ [Dropdown]      │ [Number input]  │ [+]   │
└─────────────────┴─────────────────┴────────┘

--- Bônus em Aptidões ---
┌──────────────────┬──────────┬────────┐
│ Aptidão          │ Bônus    │ Ação   │
├──────────────────┼──────────┼────────┤
│ Furtividade      │ 2        │ [✕]   │
│ [Dropdown]       │ [Number] │ [+]   │
└──────────────────┴──────────┴────────┘
```

**Validações visuais:**
- `valorPorNivel`: aceitar decimais; exibir preview "No nível 5: +X em B.B.A"
- Não permitir adicionar o mesmo bônus/aptidão duas vezes (disable opção já selecionada no dropdown)
- Confirmar antes de deletar classe referenciada em fichas

**States:**
- Vazio: "Nenhuma classe configurada. O template Klayrah Padrão inclui 12 classes."
- Carregando sub-recursos (bônus/aptidões): skeleton nas listas internas

### Raca — Tela Principal

**Componente recomendado:** Tabela com drawer lateral detalhado. As raças têm mais informação visual (bônus positivos/negativos + restrições de classe).

**Formulário da raça:**
```
[Nome*]
[Descrição]  (textarea)
[Ordem de Exibição]

--- Bônus de Atributos ---
┌──────────────────┬────────────┬────────┐
│ Atributo         │ Bônus      │ Ação   │
├──────────────────┼────────────┼────────┤
│ Agilidade        │ +2         │ [✕]   │
│ Vigor            │ -1   ⚠️    │ [✕]   │
│ [Dropdown]       │ [±Number]  │ [+]   │
└──────────────────┴────────────┴────────┘
⚠️ Valores negativos são penalidades raciais

--- Classes Permitidas (vazio = todas) ---
[Chip list de classes selecionadas]
[ + Adicionar classe permitida ]
Nota: Deixar vazio para permitir todas as classes
```

**Validações visuais:**
- Bônus de atributo: campo numérico com sinal (aceitar -5 a +10); valores negativos exibidos em vermelho com ícone de aviso
- Classes permitidas: se ao menos 1 classe adicionada, destacar aviso "Esta raça tem restrição de classe"
- Confirmar antes de deletar raça usada em fichas ativas

**States:**
- `classesPermitidas` vazio: exibir badge "Sem restrições" na tabela principal
- `classesPermitidas` com itens: exibir badge "X classes permitidas" em amarelo

---

## 8. Contrato de API

### ClassePersonagem — CRUD Principal

| Método | Path | Roles | Descrição |
|---|---|---|---|
| GET | `/api/v1/configuracoes/classes?jogoId={id}[&nome=]` | MESTRE, JOGADOR | Listar classes |
| GET | `/api/v1/configuracoes/classes/{id}` | MESTRE, JOGADOR | Buscar por ID |
| POST | `/api/v1/configuracoes/classes` | MESTRE | Criar classe |
| PUT | `/api/v1/configuracoes/classes/{id}` | MESTRE | Atualizar classe |
| DELETE | `/api/v1/configuracoes/classes/{id}` | MESTRE | Soft delete |
| PUT | `/api/v1/configuracoes/classes/reordenar?jogoId={id}` | MESTRE | Reordenar |

### ClassePersonagem — Sub-recursos

| Método | Path | Roles | Descrição |
|---|---|---|---|
| GET | `/api/v1/configuracoes/classes/{classeId}/bonus` | MESTRE, JOGADOR | Listar bônus da classe |
| POST | `/api/v1/configuracoes/classes/{classeId}/bonus` | MESTRE | Adicionar bônus |
| DELETE | `/api/v1/configuracoes/classes/{classeId}/bonus/{bonusId}` | MESTRE | Remover bônus |
| GET | `/api/v1/configuracoes/classes/{classeId}/aptidao-bonus` | MESTRE, JOGADOR | Listar bônus de aptidão |
| POST | `/api/v1/configuracoes/classes/{classeId}/aptidao-bonus` | MESTRE | Adicionar bônus de aptidão |
| DELETE | `/api/v1/configuracoes/classes/{classeId}/aptidao-bonus/{id}` | MESTRE | Remover bônus de aptidão |

**Body POST /bonus:**
```json
{ "bonusConfigId": 1, "valorPorNivel": 1.0 }
```

**Body POST /aptidao-bonus:**
```json
{ "aptidaoConfigId": 5, "bonus": 2 }
```

### Raca — CRUD Principal

| Método | Path | Roles | Descrição |
|---|---|---|---|
| GET | `/api/v1/configuracoes/racas?jogoId={id}[&nome=]` | MESTRE, JOGADOR | Listar raças |
| GET | `/api/v1/configuracoes/racas/{id}` | MESTRE, JOGADOR | Buscar por ID |
| POST | `/api/v1/configuracoes/racas` | MESTRE | Criar raça |
| PUT | `/api/v1/configuracoes/racas/{id}` | MESTRE | Atualizar raça |
| DELETE | `/api/v1/configuracoes/racas/{id}` | MESTRE | Soft delete |
| PUT | `/api/v1/configuracoes/racas/reordenar?jogoId={id}` | MESTRE | Reordenar |

### Raca — Sub-recursos

| Método | Path | Roles | Descrição |
|---|---|---|---|
| GET | `/api/v1/configuracoes/racas/{racaId}/bonus-atributos` | MESTRE, JOGADOR | Listar bônus de atributo |
| POST | `/api/v1/configuracoes/racas/{racaId}/bonus-atributos` | MESTRE | Adicionar bônus |
| DELETE | `/api/v1/configuracoes/racas/{racaId}/bonus-atributos/{id}` | MESTRE | Remover bônus |
| GET | `/api/v1/configuracoes/racas/{racaId}/classes-permitidas` | MESTRE, JOGADOR | Listar classes permitidas |
| POST | `/api/v1/configuracoes/racas/{racaId}/classes-permitidas` | MESTRE | Adicionar classe permitida |
| DELETE | `/api/v1/configuracoes/racas/{racaId}/classes-permitidas/{id}` | MESTRE | Remover classe permitida |

**Body POST /bonus-atributos:**
```json
{ "atributoConfigId": 2, "bonus": -1 }
```

**Body POST /classes-permitidas:**
```json
{ "classeId": 3 }
```

**Response Raca (com sub-recursos):**
```json
{
  "id": 2,
  "jogoId": 5,
  "nome": "Elfo",
  "descricao": "Povo eterno das florestas.",
  "ordemExibicao": 2,
  "bonusAtributos": [
    { "id": 1, "racaId": 2, "atributoConfigId": 2, "atributoNome": "Agilidade", "bonus": 2 },
    { "id": 2, "racaId": 2, "atributoConfigId": 3, "atributoNome": "Vigor", "bonus": -1 }
  ],
  "classesPermitidas": [
    { "id": 1, "racaId": 2, "classeId": 7, "classeNome": "Mago" }
  ],
  "dataCriacao": "2026-01-01T00:00:00",
  "dataUltimaAtualizacao": "2026-01-01T00:00:00"
}
```

---

## 9. O Que Já Existe

### Backend
- `ClassePersonagem`, `ClasseBonus`, `ClasseAptidaoBonus`: entidades JPA completas, incluindo `@SQLRestriction` nos sub-recursos
- `Raca`, `RacaBonusAtributo`, `RacaClassePermitida`: entidades JPA completas
- Controllers e sub-resource controllers para bônus e aptidão-bônus de classe
- Controllers e sub-resource controllers para bônus-atributos e classes-permitidas de raça
- Services, Repositories, Mappers para todos os 6 tipos
- Testes de integração para `ClassePersonagem` e `Raca`

### Frontend
- Componentes existentes:
  - `classes-config/` — CRUD básico implementado
  - `racas-config/` — CRUD básico implementado
- Modelos TypeScript: `ClassePersonagem`, `ClasseBonusConfig`, `ClasseAptidaoBonus`, `Raca`, `RacaBonusAtributo`, `RacaClassePermitida` em `config.models.ts`

---

## 10. O Que Falta

### Backend
- Validação de FK antes de soft delete: impedir deleção de Classe/Raça referenciada em fichas ativas (backend pode retornar 409 com mensagem clara)
- Validação de restrição de classe no momento de criação da ficha: se `Raca.classesPermitidas` não vazio, rejeitar `classeId` não presente na lista

### Frontend
- **Gestão de sub-recursos no UI**: o CRUD básico existe, mas a interface para adicionar/remover ClasseBonus, ClasseAptidaoBonus, RacaBonusAtributo e RacaClassePermitida provavelmente está incompleta (componentes existem mas sub-recursos são gerenciados via endpoints separados)
- **Valor por nível fracionário**: o formulário de ClasseBonus deve aceitar decimais (0.5) com input type="number" step="0.01"
- **Valores negativos para bônus racial**: input numérico sem mínimo restrito a 0; sinalização visual de penalidade
- **Preview do impacto na ficha**: ao configurar ClasseBonus, mostrar "No nível 10: +10 em B.B.A" em tempo real
- **Validação de restrição de classe na criação de ficha**: formulário de nova ficha deve filtrar classes disponíveis pela raça escolhida
- **Badge "Sem restrições" vs "X classes" na lista de raças**: visibilidade imediata do impacto da configuração
- **Confirmação de deleção com impacto**: modal informando quantas fichas serão afetadas antes de deletar
