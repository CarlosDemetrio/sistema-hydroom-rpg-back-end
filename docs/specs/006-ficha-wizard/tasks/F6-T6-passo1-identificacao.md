# T6 — Passo 1: Identificacao do Personagem (Rewrite do Wizard)

> Fase: Frontend
> Complexidade: Alta
> Prerequisito: T1 (campo status na FichaResponse)
> Bloqueia: T7, T8, T9, T10, T11, T12
> Estimativa: 6–8 horas

---

## Objetivo

Destruir o `FichaFormComponent` atual e criar o `FichaWizardComponent` do zero, com o Passo 1 completo (Identificacao: nome, genero, raca, classe, indole, presenca). O componente deve:

- Criar a ficha via `POST /fichas` ao avancar do Passo 1 pela primeira vez
- Salvar via `PUT /fichas/{id}` nas edicoes subsequentes
- Navegar para o Passo 2 apos save bem-sucedido
- Ser retomavel: se `fichaId` existir na query string, pre-preenche com dados existentes
- Suportar criacao de NPC para o Mestre (isNpc toggle)

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `ficha-form/ficha-form.component.ts` | Destruir e reescrever como `FichaWizardComponent` |
| `ficha-form/ficha-form.component.html` | Reescrever com estrutura de wizard |
| `ficha-form/steps/step-identificacao/step-identificacao.component.ts` | Criar componente dumb |
| `ficha-form/steps/step-identificacao/step-identificacao.component.html` | Template do Passo 1 |
| `fichas.routes.ts` | Atualizar rotas para wizard |
| `fichas-api.service.ts` | Verificar/adicionar metodos criar() e atualizar() |

---

## Estrutura do Componente

### `FichaWizardComponent` (Smart — orquestrador)

**Localizado em:** `src/app/features/fichas/ficha-form/ficha-wizard.component.ts`

**Responsabilidades:**
- Gerenciar `passoAtual: Signal<number>` (1 a 6)
- Gerenciar `fichaId: Signal<number | null>` (null antes de criar, preenchido apos POST)
- Gerenciar `estadoSalvamento: Signal<EstadoSalvamento>`
- Coordenar auto-save ao avancar de passo
- Carregar dados de rascunho ao iniciar (se `fichaId` na query)

**Sinais:**
```typescript
readonly passoAtual = signal<number>(1);
readonly fichaId = signal<number | null>(null);
readonly estadoSalvamento = signal<EstadoSalvamento>('idle');
readonly criando = signal<boolean>(false);

// Dados carregados de config
readonly generos = signal<GeneroConfig[]>([]);
readonly racas = signal<Raca[]>([]);
readonly classes = signal<ClassePersonagem[]>([]);
readonly classesFiltradas = computed(() => {
  // Filtrar classes pela raca selecionada (RacaClassePermitida)
  // Se raca nao tem restricoes, retornar todas as classes
});
readonly indoles = signal<IndoleConfig[]>([]);
readonly presencas = signal<PresencaConfig[]>([]);

// Dados do formulario
readonly formPasso1 = signal<FormPasso1>({
  nome: '',
  generoId: null,
  racaId: null,
  classeId: null,
  indoleId: null,
  presencaId: null,
  isNpc: false,
  descricao: null
});
```

**Metodos:**
```typescript
avancarPasso(): void
voltarPasso(): void
salvarPasso1(): Observable<FichaResponse>
confirmarCriacao(): void  // Passo 6 — chama /completar
```

### `StepIdentificacaoComponent` (Dumb)

**Inputs:**
```typescript
jogoId = input.required<number>();
generos = input.required<GeneroConfig[]>();
racas = input.required<Raca[]>();
classesFiltradas = input.required<ClassePersonagem[]>();
indoles = input.required<IndoleConfig[]>();
presencas = input.required<PresencaConfig[]>();
isMestre = input<boolean>(false);
dadosIniciais = input<FormPasso1 | null>(null);
```

**Outputs:**
```typescript
formChanged = output<FormPasso1>();
racaSelecionada = output<number | null>();  // Para o wizard filtrar classes
```

---

## Logica de Auto-save no Passo 1

```typescript
avancarPasso(): void {
  if (!this.passoAtualValido()) return;

  this.estadoSalvamento.set('salvando');

  const salvar$ = this.fichaId() === null
    ? this.fichasApiService.criar(this.jogoId(), this.formPasso1())  // POST
    : this.fichasApiService.atualizar(this.fichaId()!, this.formPasso1());  // PUT

  salvar$.subscribe({
    next: (ficha) => {
      this.fichaId.set(ficha.id);
      this.estadoSalvamento.set('salvo');
      this.passoAtual.set(2);
      setTimeout(() => this.estadoSalvamento.set('idle'), 3000);
    },
    error: () => {
      this.estadoSalvamento.set('erro');
    }
  });
}
```

## Validacao de Completude do Passo 1

```typescript
readonly passoAtualValido = computed(() => {
  if (this.passoAtual() !== 1) return true;  // Outros passos validam no proprio step
  const f = this.formPasso1();
  return !!(
    f.nome?.trim().length >= 2 &&
    f.generoId !== null &&
    f.racaId !== null &&
    f.classeId !== null &&
    f.indoleId !== null &&
    f.presencaId !== null
  );
});
```

---

## Rotas

```typescript
// fichas.routes.ts
{
  path: 'criar',
  component: FichaWizardComponent,
  // Query param opcional: ?fichaId=42 para retomar rascunho
},
{
  path: 'criar-npc',
  component: FichaWizardComponent,
  data: { npc: true }
  // Apenas MESTRE tem acesso (guard de role)
}
```

## Retomada de Rascunho

```typescript
ngOnInit(): void {
  const fichaIdParam = this.route.snapshot.queryParamMap.get('fichaId');
  if (fichaIdParam) {
    this.fichaId.set(+fichaIdParam);
    this.carregarRascunho(+fichaIdParam);
  }
  this.carregarConfigs();
}

carregarRascunho(fichaId: number): void {
  this.fichasApiService.buscarPorId(fichaId).subscribe(ficha => {
    this.formPasso1.set({
      nome: ficha.nome,
      generoId: ficha.generoId,
      racaId: ficha.racaId,
      classeId: ficha.classeId,
      indoleId: ficha.indoleId,
      presencaId: ficha.presencaId,
      isNpc: ficha.isNpc,
      descricao: ficha.descricao ?? null
    });
    // Determinar passo inicial pelo primeiro campo nulo
    const passoInicial = this.determinarPassoInicial(ficha);
    this.passoAtual.set(passoInicial);
  });
}
```

---

## Testes Obrigatorios

### Testes de componente (`ficha-wizard.component.spec.ts`)

| Cenario | Descricao |
|---------|-----------|
| Renderizacao inicial | Exibe Passo 1 com todos os campos de selecao carregados |
| Botao Proximo desabilitado | Enquanto campos obrigatorios do Passo 1 estiverem vazios |
| Botao Proximo habilitado | Quando todos os campos obrigatorios do Passo 1 forem preenchidos |
| Auto-save no avanco | Ao clicar "Proximo", chama POST /fichas (primeira vez) |
| Navegacao para Passo 2 | Apos save bem-sucedido, passoAtual = 2 |
| Estado 'salvando' durante request | spinner visivel, botao desabilitado |
| Estado 'salvo' apos request | check verde por 3s |
| Estado 'erro' em falha | icone de aviso visivel, sem avancar |
| Retomada de rascunho | Com fichaId na query, campos pre-preenchidos |
| Toggle NPC visivel apenas para MESTRE | JOGADOR nao ve o toggle |
| Classe filtrada por raca | Ao selecionar raca, classes incompativeis sao removidas da lista |

---

## Criterios de Aceitacao

- [ ] `FichaFormComponent` antigo substituido por `FichaWizardComponent`
- [ ] Passo 1 exibe: nome (input), genero (select), raca (select ou cards), classe (select filtrado), indole (select), presenca (select)
- [ ] Todos os dropdowns carregam dados de configs do jogo (nao hardcoded)
- [ ] Botao "Proximo" desabilitado ate todos os campos obrigatorios serem preenchidos
- [ ] Ao clicar "Proximo", cria ficha via POST com status RASCUNHO
- [ ] Indicador de auto-save (ver T12) integrado
- [ ] Retomada de rascunho funciona: `?fichaId=42` pre-preenche campos e determina passo correto
- [ ] Toggle NPC visivel apenas para role MESTRE
- [ ] Descricao NPC visivel apenas quando isNpc = true
- [ ] Classes sao filtradas de acordo com a Raca selecionada
- [ ] Guard de saida exibe dialogo quando usuario navega para fora com rascunho

---

## Observacoes

- O campo `origem` do formulario atual **nao existe no backend** — nao incluir.
- O campo `descricaoFisica` do formulario atual **nao existe como entidade separada relevante neste passo** — sera tratado no Passo 2 (T7) como campo opcional.
- Priorizar uso de `signal()`, `computed()`, `input()`, `output()` — nenhuma variavel mutable com `= valor` sem signal.
- Usar `@if` e `@for`, nao `*ngIf` nem `*ngFor`.
- Sem `CommonModule` — usar apenas imports especificos do Angular e PrimeNG.
