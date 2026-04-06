---
name: Wizard de Ficha — estrutura e padrões
description: Estrutura do FichaWizardComponent, passos implementados, contratos entre smart/dumb e padrão de auto-save por passo
type: project
---

O wizard de criação de ficha vive em `src/app/features/jogador/pages/ficha-form/`.

**Passos implementados:**
- Passo 1 (T6): `StepIdentificacaoComponent` — nome, gênero, raça, classe, índole, presença (obrigatórios)
- Passo 2 (T7): `StepDescricaoComponent` — descrição livre (opcional, max 2000 chars)
- Passo 3 (T8): `StepAtributosComponent` — distribuição de pontos de atributo (dumb)
- Passo 4 (T9): `StepAptidoesComponent` — distribuição de pontos de aptidão (dumb)
- Passo 5 (T10): `StepVantagensComponent` — compra de vantagens iniciais (SMART)
- Passo 6: placeholder (T11)

**Auto-save por passo:**
- Passo 1: POST /fichas (novo) ou PUT /fichas/{id} (rascunho)
- Passo 2: PUT /fichas/{id} com `{ descricao }` — sempre executa mesmo com campo vazio
- Passo 3: PUT /fichas/{id}/atributos (batch)
- Passo 4: PUT /fichas/{id}/aptidoes (batch)
- Passo 5: SEM save ao avançar — compras já persistidas individualmente via POST /fichas/{id}/vantagens

**Padrão do Passo 5 (Smart step com feedback imediato):**
- Compra individual via `comprarVantagem(fichaId, { vantagemConfigId })`
- Após compra: chama `getFichaResumo` para atualizar saldo e emite `pontosAtualizados = output<number>()`
- Wizard mantém `pontosVantagemDisponiveis = signal<number>(0)` e effect carrega na entrada do passo 5
- `effect(() => { if (passoAtual() === 5 && fichaId() !== null) { fichasApi.getFichaResumo... } })`

**Tipos em `ficha-wizard.types.ts`:**
- `EstadoSalvamento`: 'idle' | 'salvando' | 'salvo' | 'erro'
- `FormPasso1`: dados de identificação + isNpc + descricao
- `FormPasso2`: `{ descricao: string | null }`
- `FichaAtributoEditavel`: atributoConfigId, atributoNome, atributoAbreviacao, base, outros
- `FichaAptidaoEditavel`: aptidaoConfigId, aptidaoNome, tipoAptidaoNome, base, sorte, classe

**Passo 5 é OPCIONAL:** `passoAtualValido()` retorna true para qualquer passo != 1.

**Determinar passo inicial na retomada:**
- Se passo 1 incompleto (qualquer campo nulo): retorna passo 1
- Se passo 1 completo: retorna passo 3 (passo 2 é opcional e considerado visitado)

**Armadilha de testes no wizard spec:**
- `ficha-wizard.component.spec.ts` sofre worker crash quando todos os 61 testes rodam juntos
- Causa: `setTimeout(3000)` em `salvarPasso3()` e `salvarPasso4()` acumulam em workers de teste
- Workaround: rodar subconjuntos com `--testNamePattern`; individualmente todos passam
- Problema pre-existente do Agente T9 — não introduzido pelo passo 5

**Why:** wizard incremental — cada task adiciona um passo mantendo os anteriores intactos.

**How to apply:** ao criar novos steps (T11+), seguir padrão: se o passo é DUMB recebe dados via input(), se é SMART injeta services. Para steps opcionais: não alterar `passoAtualValido()`, apenas adicionar `else if (passoAtual() === N) { passoAtual.set(N+1); }` em `avancarPasso()`.
