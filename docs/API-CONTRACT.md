# API Contract — Backend ↔ Frontend

Documento de referência dos endpoints REST do projeto Klayrah RPG.
Atualizado: 2026-03-31 | Branch backend: `feature/009-npc-fichas-mestre`

---

## Convenções

- Base URL: `/api/v1` (exceto `CategoriaVantagem` que usa `/api/jogos/{jogoId}/config/` sem `/v1/`)
- Auth: sessão HTTP via OAuth2 Google (cookie `JSESSIONID` + `XSRF-TOKEN`)
- Roles: `MESTRE`, `JOGADOR`
- Soft delete em todas as entidades (campo `deleted_at`)
- Datas retornadas como `LocalDateTime` serializado pelo Jackson (ISO-8601)

---

## 1. Auth

| Método | Path | Roles | Descrição |
|--------|------|-------|-----------|
| GET | `/api/v1/auth/me` | qualquer autenticado | Dados completos do usuário + jogo ativo (se MESTRE) |
| POST | `/api/v1/auth/logout` | qualquer autenticado | Encerra sessão |
| GET | `/api/public/health` | público | Health check |

### GET /api/v1/auth/me — Response
```json
{
  "id": 1,
  "nome": "Carlos",
  "email": "carlos@example.com",
  "role": "MESTRE",
  "jogoAtivo": { "id": 5, "nome": "Klayrah" }
}
```

---

## 2. Jogos

| Método | Path | Roles | Descrição |
|--------|------|-------|-----------|
| GET | `/api/v1/jogos` | MESTRE, JOGADOR | Listar jogos do usuário |
| GET | `/api/v1/jogos/meus` | MESTRE, JOGADOR | Meus jogos com role e qtd personagens |
| GET | `/api/v1/jogos/ativo` | MESTRE, JOGADOR | Jogo ativo do Mestre |
| GET | `/api/v1/jogos/{id}` | MESTRE, JOGADOR | Detalhes do jogo |
| POST | `/api/v1/jogos` | MESTRE | Criar jogo |
| PUT | `/api/v1/jogos/{id}` | MESTRE | Atualizar jogo |
| DELETE | `/api/v1/jogos/{id}` | MESTRE | Soft delete do jogo |
| POST | `/api/v1/jogos/{id}/ativar` | MESTRE | Reativar jogo inativo |
| POST | `/api/v1/jogos/{id}/duplicar` | MESTRE | Duplicar jogo (configs, sem fichas) |
| GET | `/api/v1/jogos/{id}/config/export` | MESTRE | Exportar todas as configs do jogo |
| POST | `/api/v1/jogos/{id}/config/import` | MESTRE | Importar configs (nomes duplicados ignorados) |

### POST /api/v1/jogos — Body
```json
{ "nome": "Klayrah", "descricao": "Campanha principal" }
```

### POST /api/v1/jogos/{id}/duplicar — Body
```json
{ "novoNome": "Klayrah v2" }
```

### Response: DuplicarJogoResponse
```json
{ "jogoId": 10, "nome": "Klayrah v2" }
```

---

## 3. Participantes

| Método | Path | Roles | Descrição |
|--------|------|-------|-----------|
| POST | `/api/v1/jogos/{jogoId}/participantes/solicitar` | JOGADOR | Solicitar participação |
| GET | `/api/v1/jogos/{jogoId}/participantes` | MESTRE | Listar participantes |
| PUT | `/api/v1/jogos/{jogoId}/participantes/{id}/aprovar` | MESTRE | Aprovar participante |
| PUT | `/api/v1/jogos/{jogoId}/participantes/{id}/rejeitar` | MESTRE | Rejeitar participante |
| DELETE | `/api/v1/jogos/{jogoId}/participantes/{id}` | MESTRE | Remover participante |

---

## 4. Fichas

| Método | Path | Roles | Descrição |
|--------|------|-------|-----------|
| GET | `/api/v1/jogos/{jogoId}/fichas` | MESTRE, JOGADOR | Listar fichas (Mestre vê todas; Jogador só as suas) |
| GET | `/api/v1/jogos/{jogoId}/fichas/minhas` | MESTRE, JOGADOR | Somente minhas fichas |
| POST | `/api/v1/jogos/{jogoId}/fichas` | MESTRE, JOGADOR | Criar ficha de jogador |
| GET | `/api/v1/jogos/{jogoId}/npcs` | MESTRE | Listar NPCs do jogo |
| POST | `/api/v1/jogos/{jogoId}/npcs` | MESTRE | Criar NPC (endpoint dedicado) |
| GET | `/api/v1/fichas/{id}` | MESTRE, JOGADOR | Buscar ficha por ID |
| PUT | `/api/v1/fichas/{id}` | MESTRE, JOGADOR | Atualizar ficha |
| DELETE | `/api/v1/fichas/{id}` | MESTRE | Soft delete da ficha |
| GET | `/api/v1/fichas/{id}/resumo` | MESTRE, JOGADOR | Resumo calculado (atributos, vida, essência, ameaça) |
| POST | `/api/v1/fichas/{id}/preview` | MESTRE, JOGADOR | Simular mudanças sem persistir |
| POST | `/api/v1/fichas/{id}/duplicar` | MESTRE, JOGADOR | Duplicar ficha |
| GET | `/api/v1/fichas/{id}/vantagens` | MESTRE, JOGADOR | Listar vantagens da ficha |
| POST | `/api/v1/fichas/{id}/vantagens` | MESTRE, JOGADOR | Comprar vantagem (nível 1) |
| PUT | `/api/v1/fichas/{id}/vantagens/{vid}` | MESTRE, JOGADOR | Aumentar nível de vantagem |
| PUT | `/api/v1/fichas/{id}/atributos` | MESTRE, JOGADOR | Atualizar atributos em lote |
| PUT | `/api/v1/fichas/{id}/aptidoes` | MESTRE, JOGADOR | Atualizar aptidões em lote |

### Filtros para GET /fichas (query params)
`?nome=&classeId=&racaId=&nivel=`

### POST /api/v1/jogos/{jogoId}/fichas — Body (CreateFichaRequest)
```json
{
  "nome": "Aldric",
  "jogadorId": 2,
  "racaId": 1,
  "classeId": 3,
  "generoId": 1,
  "indoleId": 2,
  "presencaId": 1,
  "isNpc": false
}
```

### POST /api/v1/jogos/{jogoId}/npcs — Body (NpcCreateRequest)
```json
{
  "jogoId": 5,
  "nome": "Goblin Arqueiro",
  "racaId": 4,
  "classeId": 2,
  "generoId": null,
  "indoleId": null,
  "presencaId": null
}
```

### POST /api/v1/fichas/{id}/duplicar — Body
```json
{ "novoNome": "Aldric Cópia", "manterJogador": true }
```

### Response: DuplicarFichaResponse
```json
{ "fichaId": 42, "nome": "Aldric Cópia", "isNpc": false }
```

### PUT /api/v1/fichas/{id}/atributos — Body
```json
[
  { "atributoConfigId": 1, "base": 10, "nivel": 2, "outros": 0 },
  { "atributoConfigId": 2, "base": 8, "nivel": 1, "outros": 1 }
]
```

### Response: FichaAtributoResponse[]
```json
[
  {
    "id": 101,
    "atributoConfigId": 1,
    "atributoNome": "Força",
    "atributoAbreviacao": "FOR",
    "base": 10,
    "nivel": 2,
    "outros": 0,
    "total": 12,
    "impeto": 6.0
  }
]
```

### PUT /api/v1/fichas/{id}/aptidoes — Body
```json
[
  { "aptidaoConfigId": 5, "base": 3, "sorte": 1, "classe": 2 }
]
```

### Response: FichaAptidaoResponse[]
```json
[
  {
    "id": 201,
    "aptidaoConfigId": 5,
    "aptidaoNome": "Espadas",
    "base": 3,
    "sorte": 1,
    "classe": 2,
    "total": 6
  }
]
```

---

## 5. Anotações de Ficha

| Método | Path | Roles | Descrição |
|--------|------|-------|-----------|
| GET | `/api/v1/fichas/{fichaId}/anotacoes` | MESTRE, JOGADOR | Listar anotações (Mestre vê todas; Jogador vê próprias + Mestre visíveis) |
| POST | `/api/v1/fichas/{fichaId}/anotacoes` | MESTRE, JOGADOR | Criar anotação |
| DELETE | `/api/v1/fichas/{fichaId}/anotacoes/{id}` | MESTRE, JOGADOR | Deletar (Mestre: qualquer; Jogador: só suas) |

### POST /api/v1/fichas/{fichaId}/anotacoes — Body (CriarAnotacaoRequest)
```json
{
  "titulo": "Segredo do personagem",
  "conteudo": "Aldric é filho do vilão principal.",
  "tipoAnotacao": "MESTRE",
  "visivelParaJogador": false
}
```
- `tipoAnotacao`: `JOGADOR` | `MESTRE`
- `visivelParaJogador`: opcional, default `false`
- Jogadores só podem criar anotações do tipo `JOGADOR`

### Response: AnotacaoResponse
```json
{
  "id": 7,
  "fichaId": 42,
  "autorId": 1,
  "autorNome": "Carlos",
  "titulo": "Segredo do personagem",
  "conteudo": "Aldric é filho do vilão principal.",
  "tipoAnotacao": "MESTRE",
  "visivelParaJogador": false,
  "dataCriacao": "2026-03-31T10:00:00",
  "dataUltimaAtualizacao": "2026-03-31T10:00:00"
}
```

---

## 6. Configurações — Visão Geral

Todas as configurações seguem o padrão:

| Método | Path | Roles |
|--------|------|-------|
| GET | `/api/v1/configuracoes/{tipo}?jogoId={id}` | MESTRE, JOGADOR |
| GET | `/api/v1/configuracoes/{tipo}/{id}` | MESTRE, JOGADOR |
| POST | `/api/v1/configuracoes/{tipo}` | MESTRE |
| PUT | `/api/v1/configuracoes/{tipo}/{id}` | MESTRE |
| DELETE | `/api/v1/configuracoes/{tipo}/{id}` | MESTRE |
| PUT | `/api/v1/configuracoes/{tipo}/reordenar?jogoId={id}` | MESTRE |

### Tipos disponíveis
| `{tipo}` | Entidade | Filtro ?nome= |
|----------|----------|---------------|
| `atributos` | AtributoConfig | sim |
| `aptidoes` | AptidaoConfig | sim |
| `tipos-aptidao` | TipoAptidao | não |
| `niveis` | NivelConfig | não |
| `classes` | ClassePersonagem | sim |
| `vantagens` | VantagemConfig | sim |
| `racas` | Raca | sim |
| `dados-prospeccao` | DadoProspeccaoConfig | não |
| `presencas` | PresencaConfig | sim |
| `generos` | GeneroConfig | sim |
| `indoles` | IndoleConfig | sim |
| `membros-corpo` | MembroCorpoConfig | sim |
| `bonus` | BonusConfig | sim |

### Corpo da reordenação batch (todos os tipos)
```json
{ "itens": [{ "id": 1, "ordemExibicao": 0 }, { "id": 2, "ordemExibicao": 1 }] }
```

---

## 7. Configurações — NivelConfig

### Response: NivelConfig
```json
{
  "id": 3,
  "jogoId": 5,
  "nivel": 3,
  "xpNecessaria": 3000,
  "pontosAtributo": 5,
  "pontosAptidao": 10,
  "limitadorAtributo": 20,
  "permitirRenascimento": true,
  "dataCriacao": "2026-01-01T00:00:00",
  "dataUltimaAtualizacao": "2026-01-01T00:00:00"
}
```
**Nota:** `permitirRenascimento` é campo novo — inclua no formulário de criação/edição de NivelConfig.

### POST /api/v1/configuracoes/niveis — Body
```json
{
  "jogoId": 5,
  "nivel": 3,
  "xpNecessaria": 3000,
  "pontosAtributo": 5,
  "pontosAptidao": 10,
  "limitadorAtributo": 20,
  "permitirRenascimento": true
}
```

---

## 8. Configurações — VantagemConfig

### Response: VantagemConfig (inclui `efeitos`)
```json
{
  "id": 12,
  "jogoId": 5,
  "nome": "Força Bruta",
  "sigla": "FB",
  "descricao": "Aumenta Força base",
  "categoriaVantagemId": 2,
  "categoriaNome": "Físico",
  "nivelMaximo": 3,
  "formulaCusto": "nivel * 2",
  "descricaoEfeito": "Bônus de Força crescente",
  "ordemExibicao": 0,
  "preRequisitos": [],
  "efeitos": [
    {
      "id": 5,
      "vantagemConfigId": 12,
      "tipoEfeito": "BONUS_ATRIBUTO",
      "atributoAlvoId": 1,
      "atributoAlvoNome": "Força",
      "valorPorNivel": 2,
      "dataCriacao": "2026-01-15T00:00:00"
    }
  ],
  "dataCriacao": "2026-01-15T00:00:00",
  "dataUltimaAtualizacao": "2026-01-15T00:00:00"
}
```

---

## 9. Configurações — Efeitos de Vantagem

**URL base:** `/api/v1/jogos/{jogoId}/configuracoes/vantagens/{vantagemId}/efeitos`

> Atenção: URL diferente do padrão geral de configurações — inclui `jogoId` no path.

| Método | Path | Roles | Descrição |
|--------|------|-------|-----------|
| GET | `.../efeitos` | MESTRE, JOGADOR | Listar efeitos da vantagem |
| POST | `.../efeitos` | MESTRE | Adicionar efeito |
| DELETE | `.../efeitos/{efeitoId}` | MESTRE | Remover efeito |

### POST — Body (CriarVantagemEfeitoRequest)
```json
{
  "tipoEfeito": "BONUS_ATRIBUTO",
  "atributoAlvoId": 1,
  "valorFixo": null,
  "valorPorNivel": 2,
  "formula": null,
  "descricaoEfeito": "+2 de Força por nível de vantagem"
}
```

### Tipos de efeito (TipoEfeito)
| Valor | Campo alvo obrigatório | Campo valor |
|-------|----------------------|-------------|
| `BONUS_ATRIBUTO` | `atributoAlvoId` | `valorFixo` ou `valorPorNivel` |
| `BONUS_APTIDAO` | `aptidaoAlvoId` | `valorFixo` ou `valorPorNivel` |
| `BONUS_DERIVADO` | `bonusAlvoId` | `valorFixo` ou `valorPorNivel` |
| `BONUS_VIDA` | nenhum | `valorFixo` ou `valorPorNivel` |
| `BONUS_VIDA_MEMBRO` | `membroAlvoId` | `valorFixo` ou `valorPorNivel` |
| `BONUS_ESSENCIA` | nenhum | `valorFixo` ou `valorPorNivel` |
| `DADO_UP` | nenhum | nenhum |
| `FORMULA_CUSTOMIZADA` | nenhum | `formula` obrigatório |

### Response: VantagemEfeitoResponse
```json
{
  "id": 5,
  "vantagemConfigId": 12,
  "tipoEfeito": "BONUS_ATRIBUTO",
  "atributoAlvoId": 1,
  "atributoAlvoNome": "Força",
  "aptidaoAlvoId": null,
  "aptidaoAlvoNome": null,
  "bonusAlvoId": null,
  "bonusAlvoNome": null,
  "membroAlvoId": null,
  "membroAlvoNome": null,
  "valorFixo": null,
  "valorPorNivel": 2.0,
  "formula": null,
  "descricaoEfeito": "+2 de Força por nível de vantagem",
  "dataCriacao": "2026-01-15T00:00:00"
}
```

---

## 10. Configurações — Sub-recursos de Classe

| Método | Path | Roles | Descrição |
|--------|------|-------|-----------|
| GET | `/api/v1/configuracoes/classes/{classeId}/bonus` | MESTRE, JOGADOR | Listar bônus da classe |
| POST | `/api/v1/configuracoes/classes/{classeId}/bonus` | MESTRE | Adicionar bônus |
| DELETE | `/api/v1/configuracoes/classes/{classeId}/bonus/{bonusId}` | MESTRE | Remover bônus |
| GET | `/api/v1/configuracoes/classes/{classeId}/aptidao-bonus` | MESTRE, JOGADOR | Listar aptidão-bônus |
| POST | `/api/v1/configuracoes/classes/{classeId}/aptidao-bonus` | MESTRE | Adicionar aptidão-bônus |
| DELETE | `/api/v1/configuracoes/classes/{classeId}/aptidao-bonus/{id}` | MESTRE | Remover aptidão-bônus |

---

## 11. Configurações — Sub-recursos de Raça

| Método | Path | Roles | Descrição |
|--------|------|-------|-----------|
| GET | `/api/v1/configuracoes/racas/{racaId}/bonus-atributos` | MESTRE, JOGADOR | Listar bônus de atributo por raça |
| POST | `/api/v1/configuracoes/racas/{racaId}/bonus-atributos` | MESTRE | Adicionar bônus de atributo |
| DELETE | `/api/v1/configuracoes/racas/{racaId}/bonus-atributos/{id}` | MESTRE | Remover bônus de atributo |
| GET | `/api/v1/configuracoes/racas/{racaId}/classes-permitidas` | MESTRE, JOGADOR | Listar classes permitidas |
| POST | `/api/v1/configuracoes/racas/{racaId}/classes-permitidas` | MESTRE | Adicionar classe permitida |
| DELETE | `/api/v1/configuracoes/racas/{racaId}/classes-permitidas/{id}` | MESTRE | Remover classe permitida |

---

## 12. Configurações — CategoriaVantagem

> URL diferente: sem `/v1/` e com `jogoId` no path.

| Método | Path | Roles | Descrição |
|--------|------|-------|-----------|
| GET | `/api/jogos/{jogoId}/config/categorias-vantagem` | MESTRE, JOGADOR | Listar categorias |
| GET | `/api/jogos/{jogoId}/config/categorias-vantagem/{id}` | MESTRE, JOGADOR | Buscar categoria |
| POST | `/api/jogos/{jogoId}/config/categorias-vantagem` | MESTRE | Criar categoria |
| PUT | `/api/jogos/{jogoId}/config/categorias-vantagem/{id}` | MESTRE | Atualizar categoria |
| DELETE | `/api/jogos/{jogoId}/config/categorias-vantagem/{id}` | MESTRE | Deletar categoria |

---

## 13. Configurações — Sub-recursos de Vantagem (pré-requisitos)

| Método | Path | Roles | Descrição |
|--------|------|-------|-----------|
| GET | `/api/v1/configuracoes/vantagens/{vantagemId}/prerequisitos` | MESTRE, JOGADOR | Listar pré-requisitos |
| POST | `/api/v1/configuracoes/vantagens/{vantagemId}/prerequisitos` | MESTRE | Adicionar pré-requisito |
| DELETE | `/api/v1/configuracoes/vantagens/{vantagemId}/prerequisitos/{prId}` | MESTRE | Remover pré-requisito |

---

## 14. Dashboard e Siglas

| Método | Path | Roles | Descrição |
|--------|------|-------|-----------|
| GET | `/api/v1/dashboard/mestre` | MESTRE | Dashboard do Mestre |
| GET | `/api/v1/configuracoes/siglas?jogoId={id}` | MESTRE | Listar siglas em uso |
| GET | `/api/v1/configuracoes/siglas/{sigla}?jogoId={id}` | MESTRE | Info de uma sigla |
| GET | `/api/v1/configuracoes/formulas/variaveis?jogoId={id}` | MESTRE | Variáveis disponíveis para fórmulas |
| POST | `/api/v1/configuracoes/formulas/validar?jogoId={id}` | MESTRE | Validar fórmula |
| POST | `/api/v1/configuracoes/formulas/preview?jogoId={id}` | MESTRE | Preview de valor calculado |

---

## Exemplos de uso no Frontend (Angular)

### Criar um efeito de vantagem
```typescript
this.configApiService
  .criarVantagemEfeito(jogoId, vantagemId, {
    tipoEfeito: 'BONUS_ATRIBUTO',
    atributoAlvoId: 1,
    valorPorNivel: 2
  })
  .subscribe(efeito => console.log('Efeito criado:', efeito));
```

### Atualizar atributos em lote
```typescript
this.fichasApiService
  .atualizarAtributos(fichaId, [
    { atributoConfigId: 1, base: 10, nivel: 2, outros: 0 }
  ])
  .subscribe(atributos => console.log('Atributos atualizados:', atributos));
```

### Criar anotação do Mestre
```typescript
this.fichasApiService
  .criarAnotacao(fichaId, {
    titulo: 'Segredo',
    conteudo: 'Texto secreto',
    tipoAnotacao: 'MESTRE',
    visivelParaJogador: false
  })
  .subscribe(anotacao => console.log('Anotação criada:', anotacao));
```

### Criar NPC via endpoint dedicado
```typescript
this.fichasApiService
  .criarNpc(jogoId, { jogoId, nome: 'Goblin Arqueiro', racaId: 4 })
  .subscribe(npc => console.log('NPC criado:', npc));
```

### Duplicar ficha
```typescript
this.fichasApiService
  .duplicarFicha(fichaId, { novoNome: 'Aldric Cópia', manterJogador: true })
  .subscribe(resp => console.log('Ficha duplicada, novo ID:', resp.fichaId));
```
