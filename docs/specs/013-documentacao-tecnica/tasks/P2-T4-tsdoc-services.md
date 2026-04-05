# T4 — TSDoc nos Services Angular e Signal Stores

> Fase: Frontend | Dependencias: Nenhuma | Bloqueia: Nenhuma
> Estimativa: 2–3 horas

---

## Objetivo

Adicionar TSDoc em todos os services Angular e signal stores do frontend. Cada metodo publico deve documentar: o que faz, qual endpoint backend chama (para API services), e quais signals/computed sao derivados (para stores).

---

## Arquivos Afetados

### API Services (camada HTTP)

| Service | Metodos estimados | Prioridade |
|---------|------------------|-----------|
| `ficha-api.service.ts` | ~15 (CRUD, wizard, XP, status, duplicar) | **P0** |
| `jogo-api.service.ts` | ~8 (CRUD, participantes) | **P1** |
| Services de configuracao (13) | ~5 cada | **P2** |

### Business Services (logica)

| Service | Metodos estimados | Prioridade |
|---------|------------------|-----------|
| `ficha-business.service.ts` | ~10 (transformacao, validacao, calculos UI) | **P0** |
| `jogo-business.service.ts` | ~5 | **P1** |

### Signal Stores

| Store | Signals estimados | Prioridade |
|-------|------------------|-----------|
| Ficha store | ~10 (state signals + computed) | **P0** |
| Jogo store | ~5 | **P1** |
| Config stores | ~3 cada | **P2** |

---

## Padrao de TSDoc

### API Service — mapear metodo → endpoint:
```typescript
/**
 * Busca a ficha completa por ID, incluindo atributos, aptidoes e vantagens.
 *
 * @endpoint GET /api/v1/fichas/{fichaId}
 * @requires Role MESTRE ou JOGADOR dono da ficha
 * @param fichaId ID da ficha
 * @returns FichaDetailResponse com todos os sub-recursos carregados
 * @throws 404 se ficha nao encontrada ou sem permissao de acesso
 */
getFichaById(fichaId: number): Observable<FichaDetailResponse>
```

### Business Service — documentar transformacao:
```typescript
/**
 * Calcula os pontos de atributo disponiveis para distribuicao.
 *
 * Formula: pontosAtributoDoNivel - SUM(fichaAtributos.base)
 * Onde pontosAtributoDoNivel vem de NivelConfig para o nivel atual.
 *
 * @param ficha resumo da ficha com nivel e atributos
 * @returns numero de pontos restantes (pode ser 0, nunca negativo)
 */
calcularPontosAtributoDisponiveis(ficha: FichaResumo): number
```

### Signal Store — documentar derivacao:
```typescript
/**
 * Estado da ficha atualmente selecionada.
 *
 * Derivado de: fichaId signal + chamada API getFichaById.
 * Atualizado quando: fichaId muda, ou apos save/update com sucesso.
 * null quando: nenhuma ficha selecionada ou loading.
 */
readonly fichaDetail = computed(() => ...);
```

---

## O que NAO documentar

- Metodos triviais de delegacao (ex: `create(data) { return this.http.post(...) }`) — documentar apenas o `@endpoint`
- Getters simples de signals
- Metodos de lifecycle Angular (`ngOnInit`, `ngOnDestroy`)

---

## Criterios de Aceitacao

- [ ] `ficha-api.service.ts`: todos os metodos com TSDoc incluindo `@endpoint`
- [ ] `ficha-business.service.ts`: todos os metodos com TSDoc incluindo logica de calculo
- [ ] Signal stores: signals e computed com descricao de derivacao
- [ ] `jogo-api.service.ts` e `jogo-business.service.ts` com TSDoc
- [ ] Pelo menos 3 services de configuracao com TSDoc (como template para os demais)
- [ ] Build frontend passa sem erros (`ng build`)
- [ ] Testes frontend passam (`npx vitest run`)
