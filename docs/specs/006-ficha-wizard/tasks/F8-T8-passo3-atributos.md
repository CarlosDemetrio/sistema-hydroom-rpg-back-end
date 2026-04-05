# T8 — Passo 3: Distribuicao de Atributos

> Fase: Frontend
> Complexidade: Alta
> Prerequisito: T5 (pontosAtributoDisponiveis no FichaResumoResponse), T6 (Passo 1)
> Bloqueia: T11 (Revisao)
> Estimativa: 6–8 horas

---

## Objetivo

Implementar o Passo 3 do wizard onde o jogador distribui seus pontos de base nos atributos configurados dinamicamente pelo Mestre. Os atributos sao carregados de `GET /configuracoes/atributos?jogoId=`, nunca hardcoded. O passo exibe contador de pontos disponiveis, barra visual por atributo e respeita o limitador do NivelConfig.

---

## Contexto

Este e o passo mais complexo do wizard por envolver:
- Lista dinamica de atributos (configurada pelo Mestre)
- Limitador por atributo (do NivelConfig nivel 1)
- Contador global de pontos disponiveis
- Bônus de raca aplicados automaticamente ao campo `outros` (nao editavel)
- Auto-save via `PUT /fichas/{id}/atributos`

**Regras criticas:**
- O campo `base` de cada atributo e o unico editavel pelo jogador neste passo
- O campo `outros` exibe o bonus de raca (RacaBonusAtributo) — calculado pelo backend, apenas leitura
- `base` nao pode exceder `NivelConfig.limitadorAtributo` do nivel 1
- A soma de todos os `base` nao pode exceder `pontosAtributoDisponiveis` (de T5)
- Pontos nao precisam ser todos alocados — jogador pode reservar pontos para niveis futuros

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `ficha-form/ficha-wizard.component.ts` | Adicionar formPasso3, carregamento de atributos e NivelConfig |
| `ficha-form/steps/step-atributos/step-atributos.component.ts` | Criar componente dumb |
| `ficha-form/steps/step-atributos/step-atributos.component.html` | Template com grid de atributos |
| `fichas-api.service.ts` | Verificar metodo para PUT /fichas/{id}/atributos e GET /fichas/{id}/atributos |

---

## Dados Carregados pelo Wizard para este Passo

O `FichaWizardComponent` deve carregar ao entrar no Passo 3:
1. `GET /api/v1/fichas/{fichaId}/atributos` — estado atual dos FichaAtributo (ja inicializados na criacao)
2. `GET /api/v1/fichas/{fichaId}/resumo` — para `pontosAtributoDisponiveis` e `nivel`
3. `GET /api/v1/configuracoes/niveis?jogoId={id}` — para `limitadorAtributo` do nivel atual

Os atributos de configuracao (`AtributoConfig`) nao precisam ser buscados separadamente — a resposta de `GET /fichas/{id}/atributos` ja inclui `atributoNome`, `atributoAbreviacao` via `FichaAtributoResponse`.

---

## Wireframe do Passo 3

```
┌──────────────────────────────────────────────────────────────────┐
│  H2: Atributos                                                   │
│  Distribua seus pontos entre os atributos do personagem.         │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ [p-message severity="info"]                              │   │
│  │ Voce tem 15 pontos para distribuir.                      │   │
│  │ Limite por atributo: 20. Pontos nao usados sao mantidos. │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  [Pontos utilizados] ██████████░░░░░  10 / 15                   │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Forca (FOR)          [  -  ] [ 8 ] [  +  ]              │   │
│  │ Base: 8 / Limite: 20                                     │   │
│  │ [████████░░░░░░░░░░░░]                                   │   │
│  │ Bonus de Raca: +2 (exibido, nao editavel)               │   │
│  ├──────────────────────────────────────────────────────────┤   │
│  │ Agilidade (AGI)      [  -  ] [ 2 ] [  +  ]              │   │
│  │ Base: 2 / Limite: 20                                     │   │
│  │ [██░░░░░░░░░░░░░░░░░░]                                   │   │
│  ├──────────────────────────────────────────────────────────┤   │
│  │ ... (outros atributos dinamicamente)                     │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

---

## Estrutura do Componente

### `StepAtributosComponent` (Dumb)

```typescript
// Inputs
atributos = input.required<FichaAtributoEditavel[]>();
pontosDisponiveis = input.required<number>();
limitadorAtributo = input.required<number>();

// Outputs
atributosChanged = output<FichaAtributoEditavel[]>();
```

**Tipo auxiliar:**
```typescript
interface FichaAtributoEditavel {
  fichaAtributoId: number;
  atributoId: number;
  atributoNome: string;
  atributoAbreviacao: string;
  base: number;
  outros: number;      // bonus de raca — somente leitura
}
```

**Calculo de pontos utilizados:**
```typescript
readonly pontosUtilizados = computed(() =>
  this.atributos().reduce((sum, a) => sum + a.base, 0)
);

readonly pontosRestantes = computed(() =>
  Math.max(0, this.pontosDisponiveis() - this.pontosUtilizados())
);
```

**Incrementar atributo:**
```typescript
incrementar(atributoId: number): void {
  const atualizado = this.atributos().map(a => {
    if (a.atributoId !== atributoId) return a;
    if (a.base >= this.limitadorAtributo()) return a;  // Limite por atributo
    if (this.pontosRestantes() <= 0) return a;          // Sem pontos globais
    return { ...a, base: a.base + 1 };
  });
  this.atributosChanged.emit(atualizado);
}

decrementar(atributoId: number): void {
  const atualizado = this.atributos().map(a => {
    if (a.atributoId !== atributoId) return a;
    if (a.base <= 0) return a;  // Minimo 0
    return { ...a, base: a.base - 1 };
  });
  this.atributosChanged.emit(atualizado);
}
```

### Logica de Auto-save no Wizard

```typescript
salvarPasso3(): Observable<FichaAtributoResponse[]> {
  const requests = this.formPasso3().map(a => ({
    fichaAtributoId: a.fichaAtributoId,
    base: a.base
  }));
  return this.fichasApiService.atualizarAtributos(this.fichaId()!, requests);
}
```

---

## Testes Obrigatorios

| Cenario | Descricao |
|---------|-----------|
| Renderizacao dinamica | Exibe um card por AtributoConfig do jogo (nao hardcoded) |
| Contador global | Exibe "X / Y pontos utilizados" atualizado em tempo real |
| Incrementar atributo | Clicar [+] aumenta base e atualiza contador |
| Decrementar atributo | Clicar [-] diminui base e atualiza contador |
| Limite por atributo | Botao [+] desabilitado quando base == limitadorAtributo |
| Sem pontos globais | Botao [+] de todos os atributos desabilitado quando pontosRestantes == 0 |
| Bonus de raca exibido | Campo `outros` exibido como texto readonly (nao spinner) |
| Avanco com pontos reservados | Botao "Proximo" habilitado mesmo com pontosRestantes > 0 |
| Auto-save | Ao clicar "Proximo", chama PUT /fichas/{id}/atributos com todos os valores |
| Erro no save | Estado 'erro' exibido, nao avanca para Passo 4 |
| Pre-preenchimento | Se atributos ja existem na ficha (rascunho), valores pre-carregados |

---

## Criterios de Aceitacao

- [ ] Atributos carregados dinamicamente (nao hardcoded FOR/DES/CON)
- [ ] Contador global de pontos disponiveis vs utilizados visivel
- [ ] Cada atributo tem spinner com botoes [-] e [+]
- [ ] Botao [+] desabilitado quando atributo atingiu o limitador
- [ ] Botao [+] de todos desabilitado quando pontos globais esgotados
- [ ] Bonus de raca exibido como somente leitura (campo `outros`)
- [ ] Botao "Proximo" sempre habilitado (jogador pode reservar pontos)
- [ ] Ao avancar, PUT /fichas/{id}/atributos chamado com todos os valores atuais
- [ ] Testes cobrindo incremento, decremento, limites e auto-save

---

## Observacoes

- O campo `nivel` dos atributos (pontos distribuidos ao subir de nivel depois da criacao) e sempre 0 neste passo. Nao exibir.
- O campo `outros` e preenchido pelo backend com bonus de raca na criacao da ficha — nao e editavel aqui.
- O `total` calculado (base + nivel + outros) pode ser exibido como informacao, mas nao e editavel.
- Barra visual de progresso por atributo: `value = (base / limitadorAtributo) * 100`.
