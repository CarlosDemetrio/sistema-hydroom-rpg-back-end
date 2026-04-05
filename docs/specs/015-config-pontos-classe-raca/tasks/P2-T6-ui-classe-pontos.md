# T6 — UI ClassePersonagem: Abas Pontos por Nivel e Vantagens Pre-definidas

> Fase: Frontend | Prioridade: P2
> Dependencias: T2 (endpoints CRUD disponiveis)
> Bloqueia: nenhuma
> Estimativa: 3–4 horas

---

## Objetivo

Na tela de configuracao de `ClassePersonagem` (componente ja existente), adicionar duas novas abas:
1. **"Pontos por Nivel"** — tabela editavel de `ClassePontosConfig` (CRUD inline)
2. **"Vantagens Pre-definidas"** — lista editavel de `ClasseVantagemPreDefinida` (add/remove)

---

## Contexto

O componente de configuracao de ClassePersonagem ja existe no frontend e segue o padrao de tabs para sub-recursos (similar a como ClasseBonus e ClasseAptidaoBonus ja estao implementados na Spec 008). As novas abas devem seguir exatamente o mesmo padrao visual e de interacao.

**Endpoints backend (criados em T2):**
- `GET /api/v1/classes/{id}/pontos-config` → listar
- `POST /api/v1/classes/{id}/pontos-config` → criar
- `PUT /api/v1/classes/{id}/pontos-config/{pontosConfigId}` → atualizar
- `DELETE /api/v1/classes/{id}/pontos-config/{pontosConfigId}` → deletar
- `GET /api/v1/classes/{id}/vantagens-predefinidas` → listar
- `POST /api/v1/classes/{id}/vantagens-predefinidas` → criar
- `DELETE /api/v1/classes/{id}/vantagens-predefinidas/{predefinidaId}` → deletar

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `models/classe-pontos-config.model.ts` | CRIAR |
| `models/classe-vantagem-predefinida.model.ts` | CRIAR |
| `services/api/classe-pontos-config-api.service.ts` | CRIAR |
| `services/api/classe-vantagem-predefinida-api.service.ts` | CRIAR |
| `features/mestre/pages/classes-config/` componentes | EDITAR — adicionar abas |
| Testes | CRIAR ou EDITAR |

---

## Passos de Implementacao

### Passo 1 — Criar models TypeScript

**`classe-pontos-config.model.ts`:**
```typescript
export interface ClassePontosConfig {
  id: number;
  classePersonagemId: number;
  nivel: number;
  pontosAtributo: number;
  pontosAptidao: number;
  pontosVantagem: number;
  dataCriacao: string;
  dataUltimaAtualizacao: string;
}

export interface ClassePontosConfigRequest {
  nivel: number;
  pontosAtributo: number;
  pontosAptidao: number;
  pontosVantagem: number;
}
```

**`classe-vantagem-predefinida.model.ts`:**
```typescript
export interface ClasseVantagemPreDefinida {
  id: number;
  classePersonagemId: number;
  nivel: number;
  vantagemConfigId: number;
  vantagemConfigNome: string;
  dataCriacao: string;
  dataUltimaAtualizacao: string;
}

export interface ClasseVantagemPreDefinidaRequest {
  nivel: number;
  vantagemConfigId: number;
}
```

---

### Passo 2 — Criar API services

Seguir o padrao existente dos outros API services do projeto. Usar `inject(HttpClient)` (nao constructor injection).

---

### Passo 3 — Aba "Pontos por Nivel"

**Layout:**
- Tabela PrimeNG (`p-table`) com colunas: Nivel, Pontos Atributo, Pontos Aptidao, Pontos Vantagem, Acoes
- Edicao inline (celulas editaveis) ou dialog para add/edit
- Botao "Adicionar nivel" abre formulario com campos: nivel (spinner), pontosAtributo (spinner), pontosAptidao (spinner), pontosVantagem (spinner)
- Botao de delete por linha (com confirmacao)
- Ordenado por nivel ASC

**Validacoes frontend:**
- Nivel >= 1
- Todos os pontos >= 0
- Nivel duplicado: exibir erro do backend (409)

---

### Passo 4 — Aba "Vantagens Pre-definidas"

**Layout:**
- Tabela PrimeNG com colunas: Nivel, Vantagem, Acoes
- Botao "Adicionar vantagem" abre formulario com:
  - Campo nivel (spinner, >= 1)
  - Dropdown de VantagemConfig (carregar do endpoint existente `/api/v1/jogos/{jogoId}/vantagens`)
- Botao de delete por linha (com confirmacao)
- Ordenado por nivel ASC, depois por nome da vantagem

**Validacoes frontend:**
- Nivel >= 1
- VantagemConfig obrigatoria (dropdown)
- Duplicata nivel+vantagem: exibir erro do backend

---

## Convencoes Angular (CLAUDE.md frontend)

- Usar `inject()` para DI (nao constructor injection)
- Usar `signal()`, `computed()`, `model()`, `input()`, `output()`
- Usar `@if` / `@for` (nunca `*ngIf` / `*ngFor`)
- Sem `CommonModule`
- Testes com Vitest (`vi.fn()`)
- `@testing-library/angular` para testes de componente

---

## Testes

### Cenario T6-01 — Listar pontos por nivel

```
Dado: ClassePersonagem com 2 entradas de ClassePontosConfig
Quando: Aba "Pontos por Nivel" e aberta
Entao: Tabela mostra 2 linhas ordenadas por nivel
```

### Cenario T6-02 — Adicionar pontos para nivel

```
Dado: Aba "Pontos por Nivel" aberta
Quando: Usuario clica "Adicionar", preenche nivel=5, pontosAtributo=3, confirma
Entao: Nova linha aparece na tabela com nivel=5, pontosAtributo=3
```

### Cenario T6-03 — Listar vantagens pre-definidas

```
Dado: ClassePersonagem com 1 ClasseVantagemPreDefinida (nivel 1, vantagem "TCO")
Quando: Aba "Vantagens Pre-definidas" e aberta
Entao: Tabela mostra 1 linha: Nivel=1, Vantagem="TCO"
```

---

## Criterios de Aceitacao

- [ ] Aba "Pontos por Nivel" exibe tabela com CRUD de ClassePontosConfig
- [ ] Aba "Vantagens Pre-definidas" exibe tabela com add/remove de ClasseVantagemPreDefinida
- [ ] Dropdown de VantagemConfig carrega vantagens do jogo atual
- [ ] Validacoes de formulario (nivel >= 1, pontos >= 0, vantagem obrigatoria)
- [ ] Erros do backend (409 duplicata, 400 jogo diferente) exibidos ao usuario
- [ ] Apenas MESTRE vê botoes de add/edit/delete
- [ ] Testes Vitest passam
- [ ] Build Angular 0 erros

---

*Produzido por: PM/Scrum Master | 2026-04-04*
