> **Status: VALIDO** | Ultima revisao: 2026-04-02
> Todos os CRUDs completos e testados. Frontend consome todos.
> O que mudou desde a escrita: PUT /fichas/{id}/vida e PUT /fichas/{id}/prospeccao implementados (commit 4702887). MembroCorpoConfig com porcentagemVida funcional.

# BA-CONFIGURACOES-SIMPLES.md — Análise Completa: Configurações de Identidade e Mecânicas Especiais

> Documento de análise de negócio para as entidades `GeneroConfig`, `IndoleConfig`, `PresencaConfig`, `DadoProspeccaoConfig` e `MembroCorpoConfig`.
> Destina-se a guiar o desenvolvimento frontend, testes de integração e alinhamento de equipe.

---

## 1. Visão Geral

Este grupo reúne configurações que, individualmente, são simples (poucos campos, sem sub-recursos), mas cobrem domínios distintos e têm impactos específicos na ficha:

- **GeneroConfig** — opções de gênero para o personagem. No Klayrah, o gênero pode influenciar o cálculo de peso do personagem via BMI. É uma lista de opções que aparece como dropdown na criação da ficha.
- **IndoleConfig** — alinhamento moral do personagem (Bom, Mau, Neutro). Define a tendência ética e pode ser pré-requisito para certas vantagens.
- **PresencaConfig** — alinhamento comportamental (Leal, Caótico, Neutro, Bom). Complementa a Índole para dar profundidade à personalidade e pode ser requisito para vantagens ou restrições de classe.
- **DadoProspeccaoConfig** — tipos de dados de prospecção disponíveis no jogo (d3, d4, d6, d8, d10, d12). A prospecção é um recurso raro e poderoso que o Mestre concede aos jogadores.
- **MembroCorpoConfig** — partes do corpo do personagem com seus pesos de vida. Define o sistema de dano localizado — cada membro aguenta um percentual da vida total do personagem.

Todas estas entidades seguem o padrão `ConfiguracaoEntity` e têm CRUD completo. Nenhuma delas tem sub-recursos ou relacionamentos complexos.

---

## 2. Entidades e Relacionamentos

```
Jogo
 |
 +-- GeneroConfig (N)              ← Masculino, Feminino, Outro
 |         unique (jogo_id, nome)
 |
 +-- IndoleConfig (N)              ← Bom, Mau, Neutro
 |         unique (jogo_id, nome)
 |
 +-- PresencaConfig (N)            ← Leal, Caótico, Neutro, Bom
 |         unique (jogo_id, nome)
 |
 +-- DadoProspeccaoConfig (N)      ← d3, d4, d6, d8, d10, d12
 |         unique (jogo_id, nome)
 |         numeroFaces: 1-100
 |
 +-- MembroCorpoConfig (N)         ← Cabeça, Tronco, Braço, Perna, Sangue
          unique (jogo_id, nome)
          porcentagemVida: 0.01-1.00

Ficha
 |
 +-- generoId   → GeneroConfig (FK)
 +-- indoleId   → IndoleConfig (FK)
 +-- presencaId → PresencaConfig (FK)
 +-- FichaProspeccao (N)    ← contador de dados por tipo
 |       dado_prospeccao_id → DadoProspeccaoConfig
 |       quantidade         → Integer
 |
 +-- FichaVidaMembro (N)    ← vida por membro do corpo
         membro_corpo_id    → MembroCorpoConfig
         danoRecebido       → Integer
```

### Cardinalidades

- Todas: `Jogo → Entidade`: 1:N, unique `(jogo_id, nome)`
- `Ficha → GeneroConfig`: N:1 (opcional em NPC)
- `Ficha → IndoleConfig`: N:1 (opcional em NPC)
- `Ficha → PresencaConfig`: N:1 (opcional em NPC)
- `Ficha → FichaProspeccao`: 1:N (um registro por tipo de dado)
- `Ficha → FichaVidaMembro`: 1:N (um registro por membro configurado)

---

## 3. Campos e Validações

### GeneroConfig

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK, gerado | 1 |
| `jogo` | FK → Jogo | Sim | NOT NULL | — |
| `nome` | String | Sim | max 50, unique por jogo | "Masculino" |
| `descricao` | String | Não | max 200 | "Personagem com características masculinas" |
| `ordemExibicao` | Integer | Não | default 0 | 1 |

**Nota de negócio:** o gênero pode influenciar cálculo de peso via BMI. A lógica exata de BMI (se houver diferença entre gêneros) é definida pelo sistema de cálculos de ficha (Spec 007) — o Mestre apenas define quais opções existem.

### IndoleConfig

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK, gerado | 1 |
| `jogo` | FK → Jogo | Sim | NOT NULL | — |
| `nome` | String | Sim | max 50, unique por jogo | "Bom" |
| `descricao` | String | Não | max 200 | "O personagem age em prol do bem comum" |
| `ordemExibicao` | Integer | Não | default 0 | 1 |

**Nota de negócio:** a Índole pode ser pré-requisito de `VantagemConfig`. Por exemplo, a vantagem "Pureza de Alma" pode exigir Índole "Bom". Esse vínculo é feito no `VantagemPreRequisito`, mas o `IndoleConfig.id` é referenciado lá.

### PresencaConfig

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK, gerado | 2 |
| `jogo` | FK → Jogo | Sim | NOT NULL | — |
| `nome` | String | Sim | max 50, unique por jogo | "Caótico" |
| `descricao` | String | Não | max 200 | "Desrespeita regras e estruturas sociais, age por impulso" |
| `ordemExibicao` | Integer | Não | default 0 | 2 |

**Nota de negócio:** Presença + Índole juntos formam o "alinhamento" do personagem (como nos sistemas clássicos de RPG). "Bom e Leal" é diferente de "Bom e Caótico". Podem ser pré-requisitos de vantagens ou restrições de classes (ex: uma classe de Paladino pode exigir Índole "Bom" E Presença "Leal").

### DadoProspeccaoConfig

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK, gerado | 3 |
| `jogo` | FK → Jogo | Sim | NOT NULL | — |
| `nome` | String | Sim | max 20, unique por jogo | "d6" |
| `descricao` | String | Não | max 200 | "Dado de 6 faces — padrão para prospecções de dificuldade média" |
| `numeroFaces` | Integer | Sim | min 1, max 100 | 6 |
| `ordemExibicao` | Integer | Não | default 0 | 3 |

**Regras dos campos:**
- `nome`: convenção "d" + número de faces (d3, d4, d6, d8, d10, d12). Não é obrigado seguir esta convenção — o Mestre pode criar "dado especial" com 7 faces.
- `numeroFaces`: determina o valor máximo que o dado pode rolar. Validar que `min 1` e `max 100` são respeitados. Fora desse intervalo: HTTP 400.
- O Klayrah Padrão define 6 dados: d3 (3 faces) até d12 (12 faces).

### MembroCorpoConfig

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK, gerado | 1 |
| `jogo` | FK → Jogo | Sim | NOT NULL | — |
| `nome` | String | Sim | max 50, unique por jogo | "Cabeça" |
| `porcentagemVida` | BigDecimal | Sim | min 0.01, max 1.00, precision 3 scale 2 | 0.75 (= 75% da vida total) |
| `ordemExibicao` | Integer | Não | default 0 | 1 |

**Regras dos campos:**
- `porcentagemVida`: valor entre 0.01 e 1.00 (1% a 100%). Representa qual fração da vida total aquele membro aguenta.
  - Cabeça: 0.75 (75%) — ferimento grave
  - Tronco: 1.00 (100%) — representa o núcleo vital
  - Braço: 0.25 (25%)
  - Perna: 0.25 (25%)
  - Sangue: 1.00 (100%) — hemorragia/envenenamento
- A soma das porcentagens pode ser maior que 1.00 (cada membro é independente, não dividem a mesma vida). Isso é proposital: o personagem tem X de vida por membro, calculado individualmente.

---

## 4. Regras de Negócio Críticas

1. **Gênero, Índole e Presença são opcionais para NPCs**: ao criar um NPC via `POST /api/v1/jogos/{jogoId}/npcs`, os campos `generoId`, `indoleId` e `presencaId` são opcionais (podem ser null). Para fichas de jogadores, os três são obrigatórios.

2. **Prospecção é recurso do Mestre, não auto-adquirida**: o jogador não acumula dados de prospecção automaticamente ao subir de nível. O Mestre decide manualmente quando conceder prospecção. O sistema apenas armazena o contador atual em `FichaProspeccao.quantidade`.

3. **Dado de prospecção está vinculado à ficha, não ao nível**: `FichaProspeccao` registra quantos dados de cada tipo o personagem tem no momento. O Mestre pode adicionar ou remover via interface dedicada (não é uma config de nível, é uma atribuição manual).

4. **Membros do corpo criam `FichaVidaMembro` automaticamente**: ao criar uma ficha, o sistema deve gerar um `FichaVidaMembro` para cada `MembroCorpoConfig` ativo no jogo. Se o Mestre criar um novo membro depois que fichas já existem, essas fichas antigas não terão o novo membro — a reconciliação deve ser tratada.

5. **Vida do membro = porcentagem × vida total**: `FichaVidaMembro.vidaMaxima = MembroCorpoConfig.porcentagemVida × Ficha.vidaTotal`. Recalculado automaticamente quando a vida total muda.

6. **Deleção de membro com fichas existentes**: deletar um `MembroCorpoConfig` cria `FichaVidaMembro` órfão. O sistema de soft delete oculta o config, mas os dados de dano na ficha persistem. O backend não bloqueia — o frontend deve avisar.

7. **Nomes case-insensitive por jogo**: "Masculino" e "masculino" conflitam dentro do mesmo jogo. Erro: HTTP 409.

8. **Índole e Presença como pré-requisitos de vantagens**: esses valores são usados em `VantagemPreRequisito`. Ao deletar uma Índole/Presença, vantagens com esse pré-requisito ficam com referência inválida. Validar dependências antes de deletar.

---

## 5. Fluxo do Mestre

### Configurar Gênero, Índole, Presença (fluxo similar para os três):

```
1. Acessar /mestre/config/{generos|indoles|presencas}
2. Tabela simples com os valores existentes (template já criou 3 opções)
3. Adicionar nova opção: preencher nome + descrição opcional
4. Reordenar via drag-and-drop para controlar a ordem no dropdown da ficha
5. Deletar com confirmação (checar dependências em fichas/vantagens)
```

### Configurar Dados de Prospecção:

```
1. Acessar /mestre/config/prospeccao (ou /dados-prospeccao)
2. Tabela mostra dados existentes (d3 a d12 no template)
3. Criar novo dado: nome + número de faces
4. Reordenar por número de faces (do menor para o maior)
5. Para distribuir dados a um personagem: ir à ficha específica (não config)
```

### Configurar Membros do Corpo:

```
1. Acessar /mestre/config/membros-corpo
2. Tabela com nome e porcentagem de vida
3. Criar/editar membro: nome + porcentagemVida (slider 1%-100% ou campo numérico)
4. Verificar que os 7 membros padrão estão configurados (Cabeça, Tronco, Braços x2, Pernas x2, Sangue)
5. AVISO: novos membros criados após fichas existentes não são adicionados automaticamente
```

---

## 6. Impacto na Ficha

**GeneroConfig, IndoleConfig, PresencaConfig:**
- São atributos descritivos da identidade do personagem, armazenados como FKs na `Ficha`
- Exibidos na seção "Identificação" da ficha
- Índole e Presença: o front deve exibir ambos como "alinhamento" combinado (ex: "Bom / Leal")
- Podem restringir ou habilitar certas `VantagemConfig` via `VantagemPreRequisito`

**DadoProspeccaoConfig → FichaProspeccao:**
- Cada tipo de dado configura um "slot" de prospecção na ficha
- `FichaProspeccao.quantidade` inicia em 0 e é incrementado pelo Mestre
- O jogador usa os dados declarando ao Mestre — o sistema registra o consumo decrementando a quantidade
- Exibido na ficha como contadores por tipo: "d6: 2 | d8: 1 | d12: 0"

**MembroCorpoConfig → FichaVidaMembro:**
- Cada membro configura uma "barra de vida" independente na ficha
- `FichaVidaMembro.danoRecebido` registra o dano acumulado naquele membro
- Se `danoRecebido >= vidaMaxima` do membro: efeito específico (incapacitação do membro, KO, etc. — regra narrativa, não automatizada no backend)
- A ficha exibe cada membro com barra de vida visual: `[Cabeça: 60/75 HP]`

---

## 7. Design de Tela (UX)

### GeneroConfig / IndoleConfig / PresencaConfig — Padrão Comum

**Componente recomendado:** Tabela simples com edição inline. São as configurações mais simples do sistema — listas de opções de texto.

**Layout:**
```
[ + Adicionar ]                              [ Reordenar ]
┌──────────────────────┬──────────────────────┬────────┐
│ Nome                 │ Descrição             │ Ação   │
├──────────────────────┼──────────────────────┼────────┤
│ Masculino            │ —                    │ ✏ ✕   │
│ Feminino             │ —                    │ ✏ ✕   │
│ Outro                │ —                    │ ✏ ✕   │
└──────────────────────┴──────────────────────┴────────┘
```

**Formulário (modal ou inline):**
```
[Nome*]          (max 50 chars)
[Descrição]      (max 200 chars, opcional)
[Ordem]
```

**States:**
- Vazio: "Nenhuma opção configurada. O template Klayrah já inclui valores padrão."
- Aviso de deleção: se a opção está em uso em fichas, exibir contagem antes de confirmar

**Casos especiais de UX:**
- `IndoleConfig`: ao deletar um valor usado como pré-requisito de vantagem, listar as vantagens afetadas
- `PresencaConfig`: idem
- A ordem de exibição controla diretamente a ordem do dropdown na tela de criação de ficha — o Mestre deve poder ver isso contextualizado

### DadoProspeccaoConfig

**Componente recomendado:** Tabela simples, mas com campo numérico para `numeroFaces`. Ordenação por `numeroFaces` por padrão.

**Layout:**
```
[ + Adicionar Dado ]
┌──────────┬───────────────┬──────────────────────────┬────────┐
│ Nome     │ Faces         │ Descrição                │ Ação   │
├──────────┼───────────────┼──────────────────────────┼────────┤
│ d3       │ 3             │ —                        │ ✏ ✕   │
│ d6       │ 6             │ —                        │ ✏ ✕   │
│ d12      │ 12            │ Dado poderoso e raro     │ ✏ ✕   │
└──────────┴───────────────┴──────────────────────────┴────────┘
```

**Formulário:**
```
[Nome*]          (max 20 chars, ex: "d6")
[Número de Faces*]  (input numérico, min 1, max 100)
[Descrição]      (max 200 chars)
[Ordem]
```

**Validações visuais:**
- `numeroFaces` < 1 ou > 100: mensagem de erro abaixo do campo
- Sugestão automática de nome ao digitar faces: "6 faces → sugestão: d6"

**States:**
- Vazio: "Nenhum dado de prospecção configurado. O template inclui d3 a d12."
- Nota informativa: "Prospecção é distribuída pelo Mestre em cada sessão — esta tela apenas define os tipos disponíveis."

### MembroCorpoConfig

**Componente recomendado:** Tabela com coluna de porcentagem exibida como barra visual (mini progress bar) para indicar o peso relativo de cada membro.

**Layout:**
```
[ + Adicionar Membro ]
┌──────────────────┬──────────────────────────────┬────────┐
│ Membro           │ % de Vida                    │ Ação   │
├──────────────────┼──────────────────────────────┼────────┤
│ Cabeça           │ [████████░░] 75%              │ ✏ ✕   │
│ Tronco           │ [██████████] 100%             │ ✏ ✕   │
│ Braço Direito    │ [██░░░░░░░░] 25%              │ ✏ ✕   │
│ Braço Esquerdo   │ [██░░░░░░░░] 25%              │ ✏ ✕   │
│ Perna Direita    │ [██░░░░░░░░] 25%              │ ✏ ✕   │
│ Perna Esquerda   │ [██░░░░░░░░] 25%              │ ✏ ✕   │
│ Sangue           │ [██████████] 100%             │ ✏ ✕   │
└──────────────────┴──────────────────────────────┴────────┘
Exemplo: Personagem com 100 HP → Cabeça aguenta 75 HP de dano localizado
```

**Formulário:**
```
[Nome*]               (max 50 chars)
[Porcentagem de Vida*]
  Slider: [0%  ──────●──────────────  100%]
  OU campo numérico: [75] %
  (min 1%, max 100%, passo 1%)
[Ordem]
```

**Validações visuais:**
- `porcentagemVida` fora de 1%-100%: mensagem de erro e slider bloqueado
- Preview ao editar: "Com 100 HP de vida total, este membro aguenta [75 HP] de dano"
- Aviso ao adicionar novo membro se fichas já existem: "Este membro não será adicionado automaticamente às fichas existentes. Contate o desenvolvedor para reconciliação."

**States:**
- Vazio: "Nenhum membro configurado. O template Klayrah inclui 7 membros."
- Aviso de deleção: listar quantas fichas ativas têm dados de dano registrados neste membro

---

## 8. Contrato de API

### GeneroConfig

| Método | Path | Roles | Descrição |
|---|---|---|---|
| GET | `/api/v1/configuracoes/generos?jogoId={id}[&nome=]` | MESTRE, JOGADOR | Listar gêneros |
| GET | `/api/v1/configuracoes/generos/{id}` | MESTRE, JOGADOR | Buscar por ID |
| POST | `/api/v1/configuracoes/generos` | MESTRE | Criar gênero |
| PUT | `/api/v1/configuracoes/generos/{id}` | MESTRE | Atualizar |
| DELETE | `/api/v1/configuracoes/generos/{id}` | MESTRE | Soft delete |
| PUT | `/api/v1/configuracoes/generos/reordenar?jogoId={id}` | MESTRE | Reordenar |

**Body POST/PUT:**
```json
{ "jogoId": 5, "nome": "Feminino", "descricao": null, "ordemExibicao": 2 }
```

**Response (padrão para GeneroConfig, IndoleConfig, PresencaConfig):**
```json
{
  "id": 2,
  "jogoId": 5,
  "nome": "Feminino",
  "descricao": null,
  "ordemExibicao": 2,
  "dataCriacao": "2026-01-01T00:00:00",
  "dataUltimaAtualizacao": "2026-01-01T00:00:00"
}
```

### IndoleConfig

| Método | Path | Roles |
|---|---|---|
| GET | `/api/v1/configuracoes/indoles?jogoId={id}[&nome=]` | MESTRE, JOGADOR |
| GET | `/api/v1/configuracoes/indoles/{id}` | MESTRE, JOGADOR |
| POST | `/api/v1/configuracoes/indoles` | MESTRE |
| PUT | `/api/v1/configuracoes/indoles/{id}` | MESTRE |
| DELETE | `/api/v1/configuracoes/indoles/{id}` | MESTRE |
| PUT | `/api/v1/configuracoes/indoles/reordenar?jogoId={id}` | MESTRE |

### PresencaConfig

| Método | Path | Roles |
|---|---|---|
| GET | `/api/v1/configuracoes/presencas?jogoId={id}[&nome=]` | MESTRE, JOGADOR |
| GET | `/api/v1/configuracoes/presencas/{id}` | MESTRE, JOGADOR |
| POST | `/api/v1/configuracoes/presencas` | MESTRE |
| PUT | `/api/v1/configuracoes/presencas/{id}` | MESTRE |
| DELETE | `/api/v1/configuracoes/presencas/{id}` | MESTRE |
| PUT | `/api/v1/configuracoes/presencas/reordenar?jogoId={id}` | MESTRE |

### DadoProspeccaoConfig

| Método | Path | Roles | Descrição |
|---|---|---|---|
| GET | `/api/v1/configuracoes/dados-prospeccao?jogoId={id}` | MESTRE, JOGADOR | Listar dados |
| GET | `/api/v1/configuracoes/dados-prospeccao/{id}` | MESTRE, JOGADOR | Buscar por ID |
| POST | `/api/v1/configuracoes/dados-prospeccao` | MESTRE | Criar dado |
| PUT | `/api/v1/configuracoes/dados-prospeccao/{id}` | MESTRE | Atualizar |
| DELETE | `/api/v1/configuracoes/dados-prospeccao/{id}` | MESTRE | Soft delete |
| PUT | `/api/v1/configuracoes/dados-prospeccao/reordenar?jogoId={id}` | MESTRE | Reordenar |

**Body POST/PUT:**
```json
{ "jogoId": 5, "nome": "d6", "numeroFaces": 6, "descricao": null, "ordemExibicao": 3 }
```

**Response:**
```json
{
  "id": 3,
  "jogoId": 5,
  "nome": "d6",
  "numeroFaces": 6,
  "descricao": null,
  "ordemExibicao": 3,
  "dataCriacao": "2026-01-01T00:00:00",
  "dataUltimaAtualizacao": "2026-01-01T00:00:00"
}
```

### MembroCorpoConfig

| Método | Path | Roles | Descrição |
|---|---|---|---|
| GET | `/api/v1/configuracoes/membros-corpo?jogoId={id}[&nome=]` | MESTRE, JOGADOR | Listar membros |
| GET | `/api/v1/configuracoes/membros-corpo/{id}` | MESTRE, JOGADOR | Buscar por ID |
| POST | `/api/v1/configuracoes/membros-corpo` | MESTRE | Criar membro |
| PUT | `/api/v1/configuracoes/membros-corpo/{id}` | MESTRE | Atualizar |
| DELETE | `/api/v1/configuracoes/membros-corpo/{id}` | MESTRE | Soft delete |
| PUT | `/api/v1/configuracoes/membros-corpo/reordenar?jogoId={id}` | MESTRE | Reordenar |

**Body POST/PUT:**
```json
{ "jogoId": 5, "nome": "Cabeça", "porcentagemVida": 0.75, "ordemExibicao": 1 }
```

**Response:**
```json
{
  "id": 1,
  "jogoId": 5,
  "nome": "Cabeça",
  "porcentagemVida": 0.75,
  "ordemExibicao": 1,
  "dataCriacao": "2026-01-01T00:00:00",
  "dataUltimaAtualizacao": "2026-01-01T00:00:00"
}
```

---

## 9. O Que Já Existe

### Backend
- Todas as 5 entidades: JPA completas com constraints, soft delete, auditoria
- Controllers, Services, Repositories e Mappers para todos os 5 tipos
- `GameConfigInitializerService`: cria os valores padrão (Masculino/Feminino/Outro para gênero; Bom/Mau/Neutro para índole; d3-d12 para prospecção; 7 membros do corpo) ao criar um jogo
- Testes de integração via `BaseConfiguracaoServiceIntegrationTest`
- `FichaProspeccao` e `FichaVidaMembro`: entidades de instância na ficha (criadas — aguardam Spec 006/007 para uso pleno)

### Frontend
- Componentes existentes:
  - `generos-config/` — CRUD básico implementado
  - `indoles-config/` — CRUD básico implementado
  - `presencas-config/` — CRUD básico implementado
  - `prospeccao-config/` — componente funcional (nome no filesystem usa "prospeccao" sem "dado")
  - `membros-corpo-config/` — CRUD básico implementado
- Modelos TypeScript completos em `config.models.ts`: `GeneroConfig`, `IndoleConfig`, `PresencaConfig`, `DadoProspeccaoConfig`, `MembroCorpoConfig`
- `ProspeccaoConfig` como alias de `DadoProspeccaoConfig` (retrocompatibilidade)

---

## 10. O Que Falta

### Backend
- Validação de dependências antes de deleção (para todos os 5 tipos):
  - GeneroConfig, IndoleConfig, PresencaConfig: verificar fichas ativas referenciando o valor
  - IndoleConfig, PresencaConfig: verificar `VantagemPreRequisito` referenciando o valor
  - DadoProspeccaoConfig: verificar `FichaProspeccao` com quantidade > 0
  - MembroCorpoConfig: verificar `FichaVidaMembro` com dano registrado
- Reconciliação de `FichaVidaMembro` ao adicionar novo `MembroCorpoConfig` após fichas existentes (complexidade alta — pode ser feature futura)

### Frontend
- **Barra visual de porcentagem na tabela de MembroCorpoConfig**: coluna de porcentagem exibida como número puro, sem representação visual
- **Slider para `porcentagemVida`**: campo numérico simples — slider seria mais intuitivo
- **Ordenação por `numeroFaces` em DadoProspeccaoConfig**: tabela provavelmente não ordena automaticamente por faces
- **Aviso de deleção com impacto**: nenhum dos 5 componentes provavelmente tem modal de confirmação que conta fichas afetadas
- **Nota contextual sobre prospecção**: sem explicação no UI sobre o que é prospecção e como funciona (confuso para Mestres novatos)
- **Alinhamento combinado (Índole + Presença)**: na tela de ficha, os dois campos devem ser exibidos juntos como "Bom / Caótico" — verificar implementação em `FichaFormComponent`
- **`LimitadorConfig` como interface vazia**: existe `limitadores-config/` como componente e `LimitadorConfig` como interface vazia em `config.models.ts` (comentário indica que foi removido do backend) — componente deve ser removido ou escondido do menu de navegação para evitar confusão
