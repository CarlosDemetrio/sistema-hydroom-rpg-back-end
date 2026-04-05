# T10 — Passo 5: Compra de Vantagens Iniciais

> Fase: Frontend
> Complexidade: Alta
> Prerequisito: T5 (pontosVantagemDisponiveis no FichaResumoResponse), T6 (Passo 1)
> Bloqueia: T11 (Revisao)
> Estimativa: 6–8 horas

---

## Objetivo

Implementar o Passo 5 do wizard onde o jogador pode comprar vantagens com seus pontos iniciais. Nenhuma vantagem e obrigatoria — o passo e opcional. O passo exibe as vantagens disponíveis agrupadas por `CategoriaVantagem`, com filtro por categoria e busca por nome.

---

## Contexto

Vantagens sao irreversiveis — uma vez comprada, nunca pode ser removida. O passo exibe as vantagens configuradas pelo Mestre agrupadas por categoria. O jogador pode gastar seus pontos de vantagem (da `PontosVantagemConfig`) comprando vantagens no nivel 1.

**Regras criticas:**
- Pre-requisitos sao validados pelo backend antes de confirmar a compra
- Uma vantagem ja comprada nao pode ser decomprada — botao muda para "Comprada" e fica desabilitado
- O custo e calculado pela `formulaCusto` da `VantagemConfig` (avaliada pelo backend)
- Pontos nao gastos sao mantidos para niveis futuros
- Auto-save e diferente dos outros passos: a compra acontece ao clicar "Comprar" na vantagem, nao ao clicar "Proximo"

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `ficha-form/ficha-wizard.component.ts` | Adicionar logica de compra de vantagens |
| `ficha-form/steps/step-vantagens/step-vantagens.component.ts` | Criar componente smart (tem chamadas API proprias) |
| `ficha-form/steps/step-vantagens/step-vantagens.component.html` | Template com grid de vantagens |
| `fichas-api.service.ts` | Verificar metodos para GET /vantagens e POST /fichas/{id}/vantagens |

---

## Wireframe do Passo 5

```
┌──────────────────────────────────────────────────────────────────┐
│  H2: Vantagens                                                   │
│  (opcional) Use seus pontos de vantagem para fortalecer o        │
│  personagem. Pontos nao gastos sao mantidos para o futuro.       │
│                                                                  │
│  Pontos disponiveis: 3   [Buscar vantagem...  ] [Todas ▼]       │
│                                                                  │
│  ── Treinamento Fisico (vermelho) ─────────────────────────     │
│  ┌──────────────────────┐  ┌──────────────────────┐            │
│  │ Combate Pesado       │  │ Esquiva Avancada      │            │
│  │ "Bonus em ataques.." │  │ "+5 em Defesa..."     │            │
│  │ Custo: 2 pts         │  │ Custo: 1 pt           │            │
│  │ [Comprar] ────────── │  │ [Comprada ✓] ──────── │            │
│  └──────────────────────┘  └──────────────────────┘            │
│                                                                  │
│  ── Magia ──────────────────────────────────────────────────    │
│  ┌──────────────────────┐                                       │
│  │ Canalizar Essencia   │                                       │
│  │ Req: INT >= 15       │                                       │
│  │ Custo: 3 pts         │                                       │
│  │ [Sem pontos] ─────── │  ← Desabilitado sem pontos           │
│  └──────────────────────┘                                       │
└──────────────────────────────────────────────────────────────────┘
```

---

## Estrutura do Componente

### `StepVantagensComponent` (Smart — tem chamadas proprias ao API)

Este passo e Smart porque as compras acontecem incrementalmente (uma por vez, com feedback imediato) — diferente dos outros passos que salvam tudo no avanco.

```typescript
// Inputs
fichaId = input.required<number>();
jogoId = input.required<number>();
pontosDisponiveis = input.required<number>();

// Outputs
pontosAtualizados = output<number>();  // Emite o novo saldo apos cada compra
```

**Sinais internos:**
```typescript
readonly vantagensConfig = signal<VantagemConfig[]>([]);
readonly vantagensCompradas = signal<FichaVantagemResponse[]>([]);
readonly comprando = signal<number | null>(null);  // ID da vantagem em processo de compra
readonly filtroCategoria = signal<number | null>(null);
readonly termoBusca = signal<string>('');
```

**Computed — vantagens filtradas:**
```typescript
readonly vantagensExibidas = computed(() => {
  let lista = this.vantagensConfig();
  if (this.filtroCategoria() !== null) {
    lista = lista.filter(v => v.categoriaVantagemId === this.filtroCategoria());
  }
  if (this.termoBusca()) {
    const termo = this.termoBusca().toLowerCase();
    lista = lista.filter(v => v.nome.toLowerCase().includes(termo));
  }
  return lista;
});

readonly vantagensAgrupadasPorCategoria = computed(() => {
  const grupos = new Map<string, VantagemConfig[]>();
  for (const v of this.vantagensExibidas()) {
    const cat = v.categoriaNome ?? 'Sem categoria';
    if (!grupos.has(cat)) grupos.set(cat, []);
    grupos.get(cat)!.push(v);
  }
  return Array.from(grupos.entries()).map(([cat, vantagens]) => ({ cat, vantagens }));
});

readonly idsComprados = computed(() =>
  new Set(this.vantagensCompradas().map(v => v.vantagemConfigId))
);
```

**Comprar vantagem:**
```typescript
comprar(vantagemConfigId: number): void {
  if (this.comprando() !== null) return;  // Evitar duplo clique
  this.comprando.set(vantagemConfigId);

  this.fichasApiService.comprarVantagem(this.fichaId(), vantagemConfigId).subscribe({
    next: (fichaVantagem) => {
      this.vantagensCompradas.update(v => [...v, fichaVantagem]);
      // Emitir saldo atualizado (re-buscar resumo)
      this.fichasApiService.getResumo(this.fichaId()).subscribe(r =>
        this.pontosAtualizados.emit(r.pontosVantagemDisponiveis)
      );
      this.comprando.set(null);
    },
    error: (err) => {
      // Exibir toast com mensagem do erro (pre-requisitos, pontos insuficientes, etc.)
      this.messageService.add({
        severity: 'error',
        summary: 'Erro ao comprar vantagem',
        detail: err.error?.message ?? 'Tente novamente.'
      });
      this.comprando.set(null);
    }
  });
}
```

---

## Card de Vantagem

Cada vantagem e exibida como `p-card` com:

| Campo exibido | Origem |
|---|---|
| Nome | `VantagemConfig.nome` |
| Descricao do efeito | `VantagemConfig.descricaoEfeito` (truncado a 100 chars) |
| Custo | Calculado pela formula ou fixo (exibir o custo do nivel 1) |
| Pre-requisitos | Lista de `VantagemPreRequisito` (se houver) |
| Estado do botao | "Comprar", "Comprada v", "Sem pontos", "Req. nao atendido" |

**Logica do botao:**
```typescript
estadoBotao(vantagemId: number, custo: number): 'comprar' | 'comprada' | 'sem-pontos' | 'comprando' {
  if (this.idsComprados().has(vantagemId)) return 'comprada';
  if (this.comprando() === vantagemId) return 'comprando';
  if (this.pontosDisponiveis() < custo) return 'sem-pontos';
  return 'comprar';
}
```

---

## Testes Obrigatorios

| Cenario | Descricao |
|---------|-----------|
| Renderizacao | Vantagens agrupadas por categoria; sem hardcode |
| Filtro por categoria | Dropdown filtra vantagens exibidas |
| Busca por nome | Input de texto filtra vantagens em tempo real |
| Comprar vantagem | Clique em "Comprar" chama POST /fichas/{id}/vantagens |
| Vantagem comprada | Botao muda para "Comprada v" e fica desabilitado |
| Saldo atualizado | Apos compra, contador de pontos atualiza |
| Sem pontos | Botao "Sem pontos" quando custo > pontosDisponiveis |
| Erro na compra | Toast com mensagem de erro do backend (pre-req, pontos insuf.) |
| Avanco sem comprar | Botao "Proximo" sempre habilitado |
| Anti-duplo-clique | Durante request, botao de loading; outros botoes nao afetados |

---

## Criterios de Aceitacao

- [ ] Vantagens carregadas dinamicamente de `GET /configuracoes/vantagens?jogoId=`
- [ ] Agrupamento por CategoriaVantagem com cores de categoria
- [ ] Filtro por categoria funcional
- [ ] Busca por nome funcional
- [ ] Pontos disponiveis visivel e atualizado apos cada compra
- [ ] Compra via POST /fichas/{id}/vantagens com feedback visual
- [ ] Vantagem comprada: botao muda estado e fica desabilitado
- [ ] Erros do backend exibidos em toast com mensagem legivel
- [ ] Botao "Proximo" sempre habilitado (passo opcional)
- [ ] Compras ja realizadas (rascunho retomado) pre-marcadas como "Comprada"

---

## Observacoes

- O custo exibido no card e o custo do nivel 1. Se a formula e complexa, o backend precisa de um endpoint de preview de custo — ou exibir o custo configurado diretamente via `VantagemConfig.formulaCusto` simplificado. Verificar com o backend se existe calculo de custo por nivel.
- Pre-requisitos exibidos como informacao ("Requer: INT >= 15") — mas a validacao e feita pelo backend. O frontend nao valida pre-requisitos localmente.
- Este passo e Smart (nao Dumb) porque as compras persistem individualmente. O "Proximo" nao precisa chamar nenhum endpoint adicional — as compras ja foram persistidas ao longo do passo.
