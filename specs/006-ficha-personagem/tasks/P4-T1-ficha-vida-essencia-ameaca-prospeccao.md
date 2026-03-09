# P4-T1 — FichaVida, FichaEssencia, FichaAmeaca, FichaProspeccao

**Fase:** 4 — Estado do Personagem
**Complexidade:** 🟡 Média
**Depende de:** P1-T2
**Bloqueia:** P4-T2

## Objetivo

Criar as cinco entities de estado do personagem: FichaVida, FichaVidaMembro, FichaEssencia, FichaAmeaca, FichaProspeccao.

## Checklist

### 1. FichaVida entity (1:1 com Ficha)
- [ ] Campos: `@OneToOne Ficha ficha`, `int vt`, `int outros`, `int vidaTotal` (calculado)

### 2. FichaVidaMembro entity (N por FichaVida)
- [ ] Campos: `@ManyToOne FichaVida fichaVida`, `@ManyToOne MembroCorpoConfig membroConfig`
- [ ] `double vida` (calculado: vidaTotal × porcentagemVida), `int danoRecebido`
- [ ] Unique constraint: `(ficha_vida_id, membro_config_id)`

### 3. FichaEssencia entity (1:1 com Ficha)
- [ ] Campos: `@OneToOne Ficha ficha`, `int vantagens`, `int outros`, `int essenciaTotal` (calculado), `int essenciaRestante`

### 4. FichaAmeaca entity (1:1 com Ficha)
- [ ] Campos: `@OneToOne Ficha ficha`, `int itens`, `int titulos`, `int outros`, `int total` (calculado)

### 5. FichaProspeccao entity (N por Ficha)
- [ ] Campos: `@ManyToOne Ficha ficha`, `@ManyToOne DadoProspeccaoConfig dadoConfig`, `int quantidade`
- [ ] Unique constraint: `(ficha_id, dado_config_id)`

### 6. Repositories para cada entity

### 7. Response DTOs para cada entity

## Arquivos afetados (10 novos)
- `model/FichaVida.java`, `model/FichaVidaMembro.java`, `model/FichaEssencia.java`, `model/FichaAmeaca.java`, `model/FichaProspeccao.java`
- Repositories e Response DTOs correspondentes

## Verificações de aceitação
- [ ] Entities compilam sem erros
- [ ] FichaVida, FichaEssencia, FichaAmeaca criados ao criar Ficha
- [ ] FichaVidaMembro criado por MembroCorpoConfig do jogo
- [ ] FichaProspeccao criado por DadoProspeccaoConfig do jogo
- [ ] `./mvnw test` passa
