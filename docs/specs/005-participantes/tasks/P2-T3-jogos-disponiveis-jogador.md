# P2-T3 — Completar JogosDisponiveis do Jogador

> Fase: 2 — Frontend
> Complexidade: 🟡 media
> Depende de: P2-T1
> Bloqueia: nada

---

## Objetivo

Completar o `JogosDisponiveisComponent` para que o Jogador possa:
1. Ver o status da propria participacao em cada jogo listado
2. Solicitar entrada em jogos onde ainda nao e participante
3. Cancelar uma solicitacao pendente propria
4. Entender claramente porque nao pode acessar um jogo (REJEITADO, BANIDO, PENDENTE)

---

## Contexto — Arquivos a Ler Antes de Comecar

- `features/jogador/pages/jogos-disponiveis/jogos-disponiveis.component.ts` — componente atual
- `core/services/api/jogos-api.service.ts` — apos P2-T1 (tem `meuStatusParticipacao`, `cancelarSolicitacao`, `solicitarParticipacao`)
- `core/services/business/participante-business.service.ts` — apos P2-T1
- `core/models/participante.model.ts` — tipo `StatusParticipante`
- `core/models/jogo.model.ts` — tipo `JogoResumo` e campo `meuRole`

---

## Diagnostico do Componente Atual

### Limitacoes atuais

1. `JogosDisponiveisComponent` usa `jogosApi.listJogos()` que retorna apenas jogos onde o usuario ja e participante. **Jogos onde o usuario ainda nao solicitou entrada nao aparecem.** O TODO no arquivo confirma isso.

2. Para jogos listados, nao exibe o status de participacao (PENDENTE, REJEITADO, BANIDO).

3. Nao ha botao "Solicitar Entrada" para nenhum jogo.

4. Nao ha botao "Cancelar Solicitacao" para solicitacoes PENDENTE.

### Limitacao arquitetural (fora do escopo desta spec)

O backend nao tem `GET /api/v1/jogos/publicos` para o Jogador descobrir jogos onde pode solicitar entrada. **Esta spec cobre apenas a interacao com jogos ja listados** (onde o usuario ja tem alguma participacao, mesmo REJEITADO/BANIDO).

A descoberta de novos jogos (sem nenhuma participacao previa) e escopo de spec futura. O Jogador precisaria de um link compartilhado pelo Mestre para solicitar entrada num jogo desconhecido — o fluxo e: Mestre compartilha jogoId, Jogador acessa pagina de solicitacao direta.

---

## Passos de Implementacao

### Passo 1: Adicionar signal para status de participacao por jogo

O componente atual carrega apenas a lista de jogos. Precisamos tambem carregar o status de participacao do usuario em cada jogo.

**Opcao A (recomendada — simples):** O `JogoResumo` que vem de `listJogos()` pode ja incluir o status de participacao. Verificar o backend se `JogoResumo` tem campo de status.

**Opcao B (alternativa):** Carregar o status separadamente via `meuStatusParticipacao(jogoId)` para cada jogo.

**Verificar em `JogoController.java`** o que `GET /api/v1/jogos` retorna para cada jogo do Jogador. Se ja inclui status, usar diretamente. Se nao, implementar Opcao B.

**Para esta task, assumir Opcao B** (mais segura, sem depender de mudancas no backend de jogo):

```typescript
protected statusPorJogo = signal<Map<number, StatusParticipante | null>>(new Map());

private carregarStatusParticipacao(jogos: JogoResumo[]): void {
  // Carregar status para jogos onde meuRole === 'JOGADOR'
  jogos
    .filter(j => j.meuRole === 'JOGADOR')
    .forEach(j => {
      this.participanteService.meuStatus(j.id).subscribe(status => {
        this.statusPorJogo.update(map => {
          const novoMap = new Map(map);
          novoMap.set(j.id, status?.status ?? null);
          return novoMap;
        });
      });
    });
}
```

Chamar `carregarStatusParticipacao` apos carregar os jogos.

### Passo 2: Computed helpers para acoes por jogo

```typescript
protected getMeuStatus(jogoId: number): StatusParticipante | null {
  return this.statusPorJogo().get(jogoId) ?? null;
}

protected podeEntrar(jogo: JogoResumo): boolean {
  return jogo.meuRole === 'JOGADOR'
    && jogo.ativo
    && this.getMeuStatus(jogo.id) === 'APROVADO';
}

protected podeSolicitar(jogo: JogoResumo): boolean {
  const status = this.getMeuStatus(jogo.id);
  return jogo.meuRole === 'JOGADOR'
    && jogo.ativo
    && (status === null || status === 'REJEITADO');
  // PENDENTE: nao pode solicitar de novo; BANIDO: nao pode; APROVADO: ja esta dentro
}

protected podeCancelar(jogo: JogoResumo): boolean {
  return jogo.meuRole === 'JOGADOR'
    && this.getMeuStatus(jogo.id) === 'PENDENTE';
}
```

### Passo 3: Atualizar template — acoes por status

Substituir o bloco de acoes atual:

```html
<!-- Bloco de acoes (substituir o @if atual) -->
<div class="flex items-center justify-between mt-2 pt-3 border-t border-surface-200">
  <!-- Badge de status de participacao -->
  @if (jogo.meuRole === 'JOGADOR') {
    @switch (getMeuStatus(jogo.id)) {
      @case ('PENDENTE') {
        <p-tag severity="warn" icon="pi pi-clock" value="Aguardando aprovacao"
          pTooltip="Sua solicitacao esta aguardando o Mestre" />
      }
      @case ('APROVADO') {
        @if (jogoAtivo()?.id === jogo.id) {
          <p-tag severity="success" icon="pi pi-check" value="Jogo atual" />
        } @else {
          <p-tag severity="success" icon="pi pi-check" value="Aprovado" />
        }
      }
      @case ('REJEITADO') {
        <p-tag severity="danger" icon="pi pi-times" value="Solicitacao rejeitada"
          pTooltip="Voce pode re-solicitar entrada" />
      }
      @case ('BANIDO') {
        <p-tag severity="secondary" icon="pi pi-ban" value="Banido"
          pTooltip="O Mestre te baniu deste jogo" />
      }
      @default {
        <p-tag severity="info" icon="pi pi-question" value="Sem participacao" />
      }
    }
  } @else {
    <!-- MESTRE -->
    <p-tag severity="warn" icon="pi pi-crown" value="Mestre" />
  }

  <!-- Botoes de acao -->
  <div class="flex gap-2">
    <!-- Entrar no jogo (APROVADO) -->
    @if (podeEntrar(jogo)) {
      <p-button label="Entrar" icon="pi pi-play" size="small"
        (onClick)="selecionarJogo(jogo)" />
    }

    <!-- Solicitar entrada (sem participacao ou REJEITADO) -->
    @if (podeSolicitar(jogo)) {
      <p-button
        label="Solicitar Entrada"
        icon="pi pi-send"
        size="small"
        [outlined]="true"
        severity="info"
        [loading]="solicitandoJogo() === jogo.id"
        (onClick)="solicitarEntrada(jogo)"
      />
    }

    <!-- Cancelar solicitacao (PENDENTE) -->
    @if (podeCancelar(jogo)) {
      <p-button
        label="Cancelar Solicitacao"
        icon="pi pi-times"
        size="small"
        [outlined]="true"
        severity="warn"
        (onClick)="cancelarSolicitacao(jogo)"
      />
    }

    <!-- Gerenciar jogo (MESTRE) -->
    @if (jogo.meuRole === 'MESTRE') {
      <p-button label="Gerenciar" icon="pi pi-cog" size="small" outlined
        (onClick)="irParaJogo(jogo)" />
    }
  </div>
</div>
```

### Passo 4: Adicionar signal de loading por jogo

```typescript
protected solicitandoJogo = signal<number | null>(null);
```

### Passo 5: Implementar metodos de acao

```typescript
protected solicitarEntrada(jogo: JogoResumo): void {
  this.solicitandoJogo.set(jogo.id);
  this.participanteService.solicitarParticipacao(jogo.id)
    .subscribe({
      next: () => {
        this.statusPorJogo.update(map => {
          const novoMap = new Map(map);
          novoMap.set(jogo.id, 'PENDENTE');
          return novoMap;
        });
        this.toastService.success('Solicitacao enviada! Aguarde a aprovacao do Mestre.');
        this.solicitandoJogo.set(null);
      },
      error: (err) => {
        const msg = err?.error?.message ?? 'Erro ao solicitar entrada';
        this.toastService.error(msg);
        this.solicitandoJogo.set(null);
      }
    });
}

protected cancelarSolicitacao(jogo: JogoResumo): void {
  this.participanteService.cancelarSolicitacao(jogo.id)
    .subscribe({
      next: () => {
        this.statusPorJogo.update(map => {
          const novoMap = new Map(map);
          novoMap.set(jogo.id, null);
          return novoMap;
        });
        this.toastService.success('Solicitacao cancelada.');
      },
      error: () => this.toastService.error('Erro ao cancelar solicitacao')
    });
}
```

### Passo 6: Adicionar imports necessarios

- `inject(ParticipanteBusinessService)` no componente
- `pTooltip` do `TooltipModule` (se nao estiver nos imports)
- `TooltipModule` nos imports do componente

---

## UX — Consideracoes Importantes

### Estado de "solicitacao enviada"

Apos solicitar entrada, o card do jogo deve mostrar "Aguardando aprovacao" imediatamente (via update local do signal `statusPorJogo`), sem precisar recarregar a pagina.

### BANIDO — mensagem clara

Jogador BANIDO ve o card com "Banido" em cinza, sem botoes de acao. O tooltip explica "O Mestre te baniu deste jogo". Sem botao de solicitar — o backend retornaria 409, mas a UI nao deve nem mostrar o botao.

### REJEITADO — re-solicitacao possivel

Jogador REJEITADO ve "Solicitacao rejeitada" com o botao "Solicitar Novamente" visivel. O backend (apos P1-T1) aceita re-solicitacao sem cooldown.

---

## Testes a Adicionar

Criar ou expandir `jogos-disponiveis.component.spec.ts`:

```typescript
describe('JogosDisponiveisComponent', () => {
  it('deve exibir badge PENDENTE para jogo com solicitacao pendente', ...);
  it('deve exibir botao Solicitar Entrada se sem participacao', ...);
  it('deve exibir botao Solicitar Entrada se REJEITADO', ...);
  it('deve NAO exibir botao Solicitar para BANIDO', ...);
  it('deve exibir botao Cancelar Solicitacao se PENDENTE', ...);
  it('deve atualizar status local apos solicitar sem recarregar', ...);
  it('deve exibir toast de erro quando backend retorna 409', ...);
});
```

---

## Criterios de Aceitacao

- [ ] Jogador ve status de participacao (PENDENTE/APROVADO/REJEITADO/BANIDO) em cada card de jogo
- [ ] Botao "Solicitar Entrada" aparece apenas para jogos sem participacao ativa ou com status REJEITADO
- [ ] Botao "Cancelar Solicitacao" aparece apenas para status PENDENTE
- [ ] Apos solicitar, o card atualiza status para PENDENTE sem recarregar a pagina
- [ ] BANIDO nao ve botao de solicitar (protecao UX, alem da protecao do backend)
- [ ] Mensagem de erro clara quando backend retorna 409 (ja banido, ja aprovado)
- [ ] Testes Vitest passando
- [ ] Build sem erros TypeScript
