> **Status: VALIDO** | Ultima revisao: 2026-04-02
> NivelConfig com permitirRenascimento adicionado. CategoriaVantagem e PontosVantagemConfig CRUDs completos no backend.
> O que mudou desde a escrita: PontosVantagemConfig tem CRUD completo mas ZERO cobertura frontend (M1 pendente). GAP-02 define que XP e read-only para jogador (PO decidiu). GAP-06 confirma que pontosDisponiveis ausentes do FichaResumoResponse.

# BA-NIVEIS-PROGRESSAO.md — Análise Completa: Níveis e Progressão

> Documento de análise de negócio para as entidades `NivelConfig`, `CategoriaVantagem` e `PontosVantagemConfig`, com referências a `VantagemConfig` (detalhada no BA-VANTAGEM-CONFIG.md).
> Destina-se a guiar o desenvolvimento frontend, testes de integração e alinhamento de equipe.

---

## 1. Visão Geral

O sistema de progressão define **como o personagem cresce ao longo do jogo**. Três entidades colaboram para isso:

- **NivelConfig** — a tabela de progressão: quanto XP é necessário para cada nível, quantos pontos o personagem ganha, qual o teto dos atributos e se aquele nível permite Renascimento.
- **CategoriaVantagem** — agrupamento organizacional das vantagens por tema (Treinamento Físico, Ação, Reação, etc.). Não tem impacto mecânico direto, mas organiza a UI e permite que o Mestre descreva o "flavor" de cada grupo.
- **PontosVantagemConfig** — tabela complementar: quantos pontos de vantagem o personagem ganha ao atingir cada nível. Separado do NivelConfig para flexibilidade (o Mestre pode dar 3 pontos no nível 10 como marco especial).

Juntas, essas entidades controlam o arco de progressão de um personagem do nível 0 (iniciante) ao nível 35 (transcendente com múltiplos renascimentos).

---

## 2. Entidades e Relacionamentos

```
Jogo
 |
 +-- NivelConfig (N)               ← tabela de XP e limites
 |         unique (jogo_id, nivel)
 |         nivel: 0 a 35
 |         xpNecessaria, pontosAtributo, pontosAptidao
 |         limitadorAtributo, permitirRenascimento
 |
 +-- PontosVantagemConfig (N)      ← pontos de vantagem por nível
 |         unique (jogo_id, nivel)
 |         nivel, pontosGanhos
 |
 +-- CategoriaVantagem (N)         ← agrupamento de vantagens
 |         unique (jogo_id, nome)
 |         nome, descricao, cor (#RRGGBB)
 |         |
 |         +-- VantagemConfig (N) ← FK: categoriaVantagemId (opcional)
 |                   (ver BA-VANTAGEM-CONFIG.md)

Ficha
 |
 +-- xpAtual       → Integer (XP acumulado)
 +-- nivelAtual    → Integer (calculado a partir de xpAtual + NivelConfig)
 +-- renascimentos → Integer (contador de renascimentos realizados)
 +-- FichaVantagem (N) → referencia VantagemConfig (via CategoriaVantagem indiretamente)
```

### Cardinalidades

- `Jogo → NivelConfig`: 1:N, unique `(jogo_id, nivel)` — exatamente um registro de configuração por número de nível por jogo
- `Jogo → PontosVantagemConfig`: 1:N, unique `(jogo_id, nivel)` — idem, mas pode ser esparso (só níveis com pontos especiais precisam de registro)
- `Jogo → CategoriaVantagem`: 1:N, unique `(jogo_id, nome)`
- `CategoriaVantagem → VantagemConfig`: 1:N (FK opcional — vantagem pode não ter categoria)

---

## 3. Campos e Validações

### NivelConfig

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK, gerado | 5 |
| `jogo` | FK → Jogo | Sim | NOT NULL | — |
| `nivel` | Integer | Sim | min 0, unique por jogo | 5 (Nível 5 = Veterano Iniciante) |
| `xpNecessaria` | Long | Sim | min 0 | 15000 (15.000 XP para nível 5) |
| `pontosAtributo` | Integer | Sim | min 0, default 3 | 3 (ganhos AO ATINGIR este nível) |
| `pontosAptidao` | Integer | Não | min 0, default 3 | 5 (aptidões ganhas neste nível) |
| `limitadorAtributo` | Integer | Sim | min 1 | 50 (atributos não podem passar de 50 no nível 5) |
| `permitirRenascimento` | Boolean | Sim | default false | true (apenas níveis >= 31) |
| `dataCriacao` | LocalDateTime | — | auditoria | — |
| `dataUltimaAtualizacao` | LocalDateTime | — | auditoria | — |

**Regras dos campos:**

- `nivel = 0`: representa o personagem recém-criado, antes de qualquer XP. `xpNecessaria = 0`.
- `pontosAtributo` e `pontosAptidao`: são ganhos cumulativamente ao longo dos níveis, não por sessão. Ao subir do nível 4 para o nível 5, o personagem ganha os pontos configurados para o nível 5.
- `limitadorAtributo`: teto global — nenhum atributo do personagem naquele nível pode ter Total acima deste valor. Impede personagens de baixo nível com atributos desproporcionalmente altos.
- `permitirRenascimento`: no Klayrah Padrão, apenas níveis 31-35 permitem renascimento. O frontend deve exibir este campo com toggle e contextualizar que é raro.

### PontosVantagemConfig

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK, gerado | 10 |
| `jogo` | FK → Jogo | Sim | NOT NULL | — |
| `nivel` | Integer | Sim | min 1, unique por jogo | 10 (marcos especiais: nível 10) |
| `pontosGanhos` | Integer | Sim | min 0, default 1 | 3 (3 pontos de vantagem no nível 10) |

**Interpretação do sistema:** se o Mestre configura apenas os registros `nivel=10, pontos=3` e `nivel=20, pontos=3`, então:
- Níveis 1-9 sem registro: o personagem NÃO ganha pontos nesses níveis (a menos que haja um registro global)
- Nível 10: +3 pontos
- Níveis 11-19: nada
- Nível 20: +3 pontos

Na prática, o template Klayrah Padrão cria um registro para cada nível (nível 1 a 35) com `pontosGanhos = 1`, resultando em 1 ponto por nível. O Mestre pode substituir valores individuais.

### CategoriaVantagem

| Campo | Tipo | Obrigatório | Constraints | Exemplo real |
|---|---|---|---|---|
| `id` | Long | — | PK, gerado | 1 |
| `jogo` | FK → Jogo | Sim | NOT NULL | — |
| `nome` | String | Sim | max 100, unique por jogo | "Treinamento Físico" |
| `descricao` | String | Não | TEXT | "Vantagens relacionadas ao aprimoramento do combate físico" |
| `cor` | String | Não | max 7, formato #RRGGBB | "#E74C3C" (vermelho para combate) |
| `ordemExibicao` | Integer | Sim | default 0 | 1 |

**Nota sobre `cor`**: usada exclusivamente na UI para colorir badges/chips de categoria. Não tem impacto mecânico. Validação do formato `#RRGGBB` deve ser feita no frontend (color picker).

---

## 4. Regras de Negócio Críticas

1. **XP acumulada determina o nível**: o nível do personagem é o maior `NivelConfig.nivel` cujo `xpNecessaria <= xpAtual`. O backend não armazena o nível diretamente — ele é calculado dinamicamente. A tabela deve estar completa e consistente para que o cálculo funcione.

2. **XP dos níveis deve ser crescente**: `NivelConfig.xpNecessaria` para o nível N deve ser >= ao XP do nível N-1. O Mestre não pode configurar nível 5 com menos XP que nível 4. O backend ou frontend deve validar a ordenação.

3. **Lacunas na tabela de níveis**: se o Mestre não cadastrar, por exemplo, o nível 3, o personagem com XP suficiente "pulará" do nível 2 para o nível 4. Isso é um erro de configuração, não uma feature. O UI deve alertar quando há "buracos" na sequência de níveis.

4. **PontosVantagemConfig independente de NivelConfig**: a tabela de pontos de vantagem é separada da tabela de XP/limitador. Pode haver um nível sem registro em `PontosVantagemConfig` — nesse caso, o personagem não ganha pontos de vantagem naquele nível. Não é um erro, é uma escolha do Mestre.

5. **Renascimento apenas em níveis >= 31**: regra do Klayrah Padrão. O Mestre pode configurar `permitirRenascimento = true` em qualquer nível, mas o template segue esta convenção. Um personagem só pode renascer se estiver em um nível com `permitirRenascimento = true`.

6. **Renascimento reseta o nível**: ao renascer, o personagem volta ao nível 1 mas mantém bônus permanentes de renascimentos anteriores. O contador `Ficha.renascimentos` é incrementado. Os pontos de vantagem, vantagens e bônus de renascimento são mantidos.

7. **Deleção de nível em uso**: deletar um `NivelConfig` pode causar inconsistências em fichas que estejam naquele nível. O backend não bloqueia (soft delete), mas o frontend deve avisar.

8. **CategoriaVantagem com vantagens ativas**: ao deletar uma categoria, as `VantagemConfig` com essa categoria passam a ter `categoriaVantagemId = null` (ou FK quebrada). O sistema deve lidar graciosamente com esse caso na UI.

---

## 5. Fluxo do Mestre

### Configurar Progressão de Níveis:

```
1. Acessar /mestre/config/niveis
2. A tabela mostra todos os NivelConfig do jogo (0 a 35 se template aplicado)
3. Editar inline cada linha: XP, pontos de atributo/aptidão, limitador, renascimento
4. Salvar individualmente ou em lote (recomendado: PUT individual por linha editada)
5. Verificar consistência: XP deve ser crescente; sem buracos na sequência
```

### Configurar Pontos de Vantagem:

```
1. Acessar /mestre/config/pontos-vantagem (ou seção dentro de NivelConfig)
2. Visualizar a tabela de pontos por nível
3. Editar o campo pontosGanhos para cada nível
4. Para marcos especiais (nível 10, 20, 30): aumentar para 2 ou 3 pontos
```

### Configurar Categorias de Vantagem:

```
1. Acessar /mestre/config/vantagens → seção "Categorias"
2. Criar categorias: nome + descrição + cor
3. Ao criar/editar VantagemConfig, selecionar a categoria
```

---

## 6. Impacto na Ficha

**NivelConfig na Ficha:**
- `Ficha.nivelAtual` é calculado automaticamente: `MAX(nivel WHERE xpNecessaria <= fichaXpAtual)`
- Ao subir de nível: o jogador recebe `pontosAtributo` pontos para distribuir nos atributos
- O `limitadorAtributo` do nível atual limita o Total máximo de qualquer atributo
- `pontosAptidao` são usados para distribuir pontos em aptidões
- Se `permitirRenascimento = true` e o Mestre decidir: `Ficha.renascimentos++`, nível resetado para 1

**PontosVantagemConfig na Ficha:**
- Ao atingir o nível N: verificar se existe registro `PontosVantagemConfig` para esse nível
- Se sim: adicionar `pontosGanhos` ao pool de pontos de vantagem disponíveis
- O total de pontos disponíveis é: `SUM(pontosGanhos dos PontosVantagemConfig de todos os níveis atingidos)`
- Pontos gastos: `SUM(FichaVantagem.custoPago)` de todas as vantagens compradas

**CategoriaVantagem na Ficha:**
- Sem impacto mecânico direto — é organizacional
- Na tela de compra de vantagens: filtragem e agrupamento por categoria facilita a navegação

---

## 7. Design de Tela (UX)

### NivelConfig — Tela de Configuração

**Componente recomendado:** Tabela editável inline com 36 linhas (níveis 0-35). Não usar drawer — a natureza tabular dos dados pede edição in-place.

**Layout da tabela:**
```
┌───────┬────────────┬───────────────┬───────────────┬──────────────┬────────────────┐
│ Nível │ XP Necessária │ Pts Atributo │ Pts Aptidão │ Limitador   │ Renascimento  │
├───────┼────────────┼───────────────┼───────────────┼──────────────┼────────────────┤
│ 0     │ 0          │ 0             │ 0             │ 10           │ ○             │
│ 1     │ 1.000      │ 3             │ 3             │ 15           │ ○             │
│ ...   │            │               │               │              │               │
│ 31    │ 1.800.000  │ 5             │ 5             │ 300          │ ●             │
└───────┴────────────┴───────────────┴───────────────┴──────────────┴────────────────┘
```

- Cada linha editável inline (click para editar)
- `xpNecessaria`: formatação com separador de milhar para legibilidade (1.000.000 não 1000000)
- `permitirRenascimento`: toggle/checkbox — destaque visual em amarelo/dourado para os níveis com renascimento
- Validação inline: se XP de um nível for menor que o nível anterior, realçar em vermelho
- Botão "Verificar Consistência" que valida toda a tabela de uma vez

**Aviso de lacuna:**
```
⚠️ Atenção: faltam os níveis 15 e 17. Personagens com XP suficiente pulam diretamente.
```

### PontosVantagemConfig — Seção dentro de NivelConfig

**Componente recomendado:** Coluna adicional na tabela de NivelConfig (ou seção separada com tabela similar).

```
┌───────┬──────────────────────────┐
│ Nível │ Pontos de Vantagem       │
├───────┼──────────────────────────┤
│ 1-9   │ 1 por nível              │
│ 10    │ 3 ★ (marco especial)     │
│ 11-19 │ 1 por nível              │
│ 20    │ 3 ★                      │
└───────┴──────────────────────────┘
```

- Destacar níveis com pontos acima de 1 (marcos especiais)
- Exibir total acumulado ao atingir cada nível (soma dos pontos até aquele nível)

### CategoriaVantagem — Tela de Configuração

**Componente recomendado:** Tabela simples com color picker embutido. As categorias são criadas uma vez e raramente alteradas.

**Formulário:**
```
[Nome*]
[Descrição]  (textarea)
[Cor]  ← color picker visual (#RRGGBB)
        Preview: [  Treinamento Físico  ] (chip colorido)
[Ordem de Exibição]
```

- Color picker com paleta pré-definida + campo hex manual
- Preview em tempo real do chip colorido com o nome da categoria
- Aviso ao deletar categoria com vantagens vinculadas: "X vantagens perderão sua categoria"

**States:**
- Vazio: "Nenhuma categoria criada. Crie categorias para organizar suas vantagens."
- Lista com chips coloridos na coluna "Cor" da tabela principal

---

## 8. Contrato de API

### NivelConfig

| Método | Path | Roles | Descrição |
|---|---|---|---|
| GET | `/api/v1/configuracoes/niveis?jogoId={id}` | MESTRE, JOGADOR | Listar todos os níveis |
| GET | `/api/v1/configuracoes/niveis/{id}` | MESTRE, JOGADOR | Buscar nível por ID |
| POST | `/api/v1/configuracoes/niveis` | MESTRE | Criar nível |
| PUT | `/api/v1/configuracoes/niveis/{id}` | MESTRE | Atualizar nível |
| DELETE | `/api/v1/configuracoes/niveis/{id}` | MESTRE | Soft delete |

**Body POST/PUT:**
```json
{
  "jogoId": 5,
  "nivel": 5,
  "xpNecessaria": 15000,
  "pontosAtributo": 3,
  "pontosAptidao": 5,
  "limitadorAtributo": 50,
  "permitirRenascimento": false
}
```

**Response:**
```json
{
  "id": 5,
  "jogoId": 5,
  "nivel": 5,
  "xpNecessaria": 15000,
  "pontosAtributo": 3,
  "pontosAptidao": 5,
  "limitadorAtributo": 50,
  "permitirRenascimento": false,
  "dataCriacao": "2026-01-01T00:00:00",
  "dataUltimaAtualizacao": "2026-01-01T00:00:00"
}
```

### PontosVantagemConfig

> Nota: endpoint segue o padrão `/api/v1/configuracoes/{tipo}`, mas o tipo exato precisa ser confirmado na implementação (possivelmente `pontos-vantagem`).

| Método | Path | Roles |
|---|---|---|
| GET | `/api/v1/configuracoes/pontos-vantagem?jogoId={id}` | MESTRE, JOGADOR |
| POST | `/api/v1/configuracoes/pontos-vantagem` | MESTRE |
| PUT | `/api/v1/configuracoes/pontos-vantagem/{id}` | MESTRE |
| DELETE | `/api/v1/configuracoes/pontos-vantagem/{id}` | MESTRE |

**Body POST:**
```json
{ "jogoId": 5, "nivel": 10, "pontosGanhos": 3 }
```

### CategoriaVantagem

> Nota: URL diferente do padrão — sem `/v1/` e com `jogoId` no path.

| Método | Path | Roles |
|---|---|---|
| GET | `/api/jogos/{jogoId}/config/categorias-vantagem` | MESTRE, JOGADOR |
| GET | `/api/jogos/{jogoId}/config/categorias-vantagem/{id}` | MESTRE, JOGADOR |
| POST | `/api/jogos/{jogoId}/config/categorias-vantagem` | MESTRE |
| PUT | `/api/jogos/{jogoId}/config/categorias-vantagem/{id}` | MESTRE |
| DELETE | `/api/jogos/{jogoId}/config/categorias-vantagem/{id}` | MESTRE |

**Body POST/PUT:**
```json
{ "nome": "Treinamento Físico", "descricao": "Aprimoramentos de combate físico", "cor": "#E74C3C", "ordemExibicao": 1 }
```

**Response:**
```json
{
  "id": 1,
  "jogoId": 5,
  "nome": "Treinamento Físico",
  "descricao": "Aprimoramentos de combate físico",
  "cor": "#E74C3C",
  "ordemExibicao": 1,
  "dataCriacao": "2026-01-01T00:00:00",
  "dataUltimaAtualizacao": "2026-01-01T00:00:00"
}
```

---

## 9. O Que Já Existe

### Backend
- `NivelConfig`, `PontosVantagemConfig`, `CategoriaVantagem`: entidades JPA completas
- `NivelConfig.permitirRenascimento`: campo implementado (adicionado na Spec 009 — ver API-CONTRACT.md nota)
- Controllers, Services, Repositories, Mappers para os 3 tipos
- `GameConfigInitializerService`: cria os 36 registros de NivelConfig (0-35) e os PontosVantagemConfig padrão ao criar um Jogo
- Testes de integração para NivelConfig e CategoriaVantagem

### Frontend
- Componentes existentes:
  - `niveis-config/` — componente funcional (CRUD básico)
  - Categoria de Vantagem: gerenciado dentro de `vantagens-config/` (sem página separada)
- Modelo TypeScript: `NivelConfig` com `permitirRenascimento` em `config.models.ts`
- `PontosVantagemConfig`: sem componente dedicado identificado — provavelmente gerenciado via vantagens ou ausente

---

## 10. O Que Falta

### Backend
- Validação de consistência da tabela de níveis (XP crescente): atualmente não validado ao criar/editar
- Validação de lacunas na sequência de níveis: aviso quando o nível N existe mas N-1 não
- Endpoint de cálculo de nível atual por XP: útil para a ficha calcular o nível sem carregar toda a tabela
- Verificação de renascimento: lógica de aplicar o renascimento (reset de nível, manutenção de bônus) ainda não implementada no FichaService

### Frontend
- **Tabela editável inline para NivelConfig**: o componente atual provavelmente usa formulário padrão por linha; a UX ideal é edição inline de toda a tabela de uma vez
- **Formatação de XP com separador de milhar**: valores como 1800000 precisam aparecer como 1.800.000
- **Destaque para `permitirRenascimento`**: campo provavelmente ausente ou sem destaque visual especial no formulário atual
- **Verificação de consistência (XP crescente + sem lacunas)**: sem validação no frontend
- **PontosVantagemConfig**: sem componente de configuração identificado — a tela de NivelConfig deveria integrar esta configuração
- **CategoriaVantagem com color picker**: o campo `cor` existe no modelo mas provavelmente sem color picker visual no formulário
- **Total acumulado de pontos de vantagem por nível**: display informativo ausente
- **Aviso de deleção de categoria com vantagens vinculadas**: sem modal de confirmação com contagem de dependências
