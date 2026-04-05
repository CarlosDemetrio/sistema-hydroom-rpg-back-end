# T7 — UI Raca: Abas Pontos por Nivel e Vantagens Pre-definidas

> Fase: Frontend | Prioridade: P2
> Dependencias: T2 (endpoints CRUD disponiveis)
> Bloqueia: nenhuma
> Estimativa: 3–4 horas

---

## Objetivo

Na tela de configuracao de `Raca` (componente ja existente), adicionar duas novas abas:
1. **"Pontos por Nivel"** — tabela editavel de `RacaPontosConfig` (CRUD inline)
2. **"Vantagens Pre-definidas"** — lista editavel de `RacaVantagemPreDefinida` (add/remove)

---

## Contexto

O componente de configuracao de Raca ja existe no frontend e segue o padrao de tabs para sub-recursos (similar a como RacaBonusAtributo e RacaClassePermitida ja estao implementados). As novas abas devem seguir exatamente o mesmo padrao visual e de interacao.

Esta task e simetrica a T6 (ClassePersonagem). A logica, layout e padroes sao identicos — apenas os endpoints e models referenciam `Raca` em vez de `ClassePersonagem`.

**Endpoints backend (criados em T2):**
- `GET /api/v1/racas/{id}/pontos-config` → listar
- `POST /api/v1/racas/{id}/pontos-config` → criar
- `PUT /api/v1/racas/{id}/pontos-config/{pontosConfigId}` → atualizar
- `DELETE /api/v1/racas/{id}/pontos-config/{pontosConfigId}` → deletar
- `GET /api/v1/racas/{id}/vantagens-predefinidas` → listar
- `POST /api/v1/racas/{id}/vantagens-predefinidas` → criar
- `DELETE /api/v1/racas/{id}/vantagens-predefinidas/{predefinidaId}` → deletar

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `models/raca-pontos-config.model.ts` | CRIAR |
| `models/raca-vantagem-predefinida.model.ts` | CRIAR |
| `services/api/raca-pontos-config-api.service.ts` | CRIAR |
| `services/api/raca-vantagem-predefinida-api.service.ts` | CRIAR |
| `features/mestre/pages/racas-config/` componentes | EDITAR — adicionar abas |
| Testes | CRIAR ou EDITAR |

---

## Passos de Implementacao

### Passo 1 — Criar models TypeScript

**`raca-pontos-config.model.ts`:**
```typescript
export interface RacaPontosConfig {
  id: number;
  racaId: number;
  nivel: number;
  pontosAtributo: number;
  pontosAptidao: number;
  pontosVantagem: number;
  dataCriacao: string;
  dataUltimaAtualizacao: string;
}

export interface RacaPontosConfigRequest {
  nivel: number;
  pontosAtributo: number;
  pontosAptidao: number;
  pontosVantagem: number;
}
```

**`raca-vantagem-predefinida.model.ts`:**
```typescript
export interface RacaVantagemPreDefinida {
  id: number;
  racaId: number;
  nivel: number;
  vantagemConfigId: number;
  vantagemConfigNome: string;
  dataCriacao: string;
  dataUltimaAtualizacao: string;
}

export interface RacaVantagemPreDefinidaRequest {
  nivel: number;
  vantagemConfigId: number;
}
```

---

### Passo 2 — Criar API services

Seguir padrao identico dos API services existentes no projeto. Usar `inject(HttpClient)`.

---

### Passo 3 — Aba "Pontos por Nivel"

**Layout identico a T6:**
- Tabela PrimeNG (`p-table`) com colunas: Nivel, Pontos Atributo, Pontos Aptidao, Pontos Vantagem, Acoes
- Botao "Adicionar nivel" com formulario: nivel (spinner), pontosAtributo, pontosAptidao, pontosVantagem
- Botao de delete por linha com confirmacao
- Ordenado por nivel ASC

**Validacoes:**
- Nivel >= 1
- Todos os pontos >= 0
- Nivel duplicado: erro 409 do backend

---

### Passo 4 — Aba "Vantagens Pre-definidas"

**Layout identico a T6:**
- Tabela PrimeNG com colunas: Nivel, Vantagem, Acoes
- Botao "Adicionar vantagem": nivel (spinner) + dropdown de VantagemConfig
- Delete por linha com confirmacao
- Ordenado por nivel ASC

---

## Convencoes Angular (CLAUDE.md frontend)

- Usar `inject()` para DI
- Usar `signal()`, `computed()`, `model()`, `input()`, `output()`
- Usar `@if` / `@for`
- Sem `CommonModule`
- Testes com Vitest (`vi.fn()`)

---

## Oportunidade de Reutilizacao

Se T6 e T7 forem desenvolvidas em sequencia, considerar extrair componentes compartilhados:
- `PontosConfigTableComponent` — tabela generica de pontos por nivel (input: lista de pontos, output: create/update/delete)
- `VantagemPreDefinidaTableComponent` — tabela generica de vantagens pre-definidas

Isso evitaria duplicacao de codigo entre ClassePersonagem e Raca. A decisao de extrair componentes genéricos vs copiar fica com o desenvolvedor — nao e obrigatorio.

---

## Testes

### Cenario T7-01 — Listar pontos por nivel da raca

```
Dado: Raca "Elfo" com 1 entrada de RacaPontosConfig (nivel 1, pontosAptidao=1)
Quando: Aba "Pontos por Nivel" e aberta
Entao: Tabela mostra 1 linha: Nivel=1, Pontos Aptidao=1
```

### Cenario T7-02 — Adicionar vantagem pre-definida a raca

```
Dado: Aba "Vantagens Pre-definidas" aberta para raca "Elfo"
Quando: Usuario clica "Adicionar", seleciona nivel=1 e vantagem "Visao Noturna", confirma
Entao: Nova linha aparece na tabela: Nivel=1, Vantagem="Visao Noturna"
```

### Cenario T7-03 — Deletar pontos config

```
Dado: Raca com RacaPontosConfig para nivel 5
Quando: Usuario clica delete na linha de nivel 5 e confirma
Entao: Linha removida da tabela
E: GET /racas/{id}/pontos-config nao retorna mais o item
```

---

## Criterios de Aceitacao

- [ ] Aba "Pontos por Nivel" exibe tabela com CRUD de RacaPontosConfig
- [ ] Aba "Vantagens Pre-definidas" exibe tabela com add/remove de RacaVantagemPreDefinida
- [ ] Dropdown de VantagemConfig carrega vantagens do jogo atual
- [ ] Validacoes identicas a T6 (nivel >= 1, pontos >= 0, vantagem obrigatoria)
- [ ] Erros do backend exibidos ao usuario
- [ ] Apenas MESTRE ve botoes de add/edit/delete
- [ ] Testes Vitest passam
- [ ] Build Angular 0 erros

---

*Produzido por: PM/Scrum Master | 2026-04-04*
