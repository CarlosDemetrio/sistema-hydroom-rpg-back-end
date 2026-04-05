# T7 — Passo 2: Descricao Fisica (campo opcional)

> Fase: Frontend
> Complexidade: Baixa
> Prerequisito: T6 (wizard base + Passo 1)
> Bloqueia: Nenhum
> Estimativa: 2–3 horas

---

## Objetivo

Implementar o Passo 2 do wizard com o campo `descricao` do personagem. Este passo e **opcional** — o usuario pode deixar em branco e avancar sem problema. O campo ja existe na entidade `Ficha` como `descricao` (TEXT, max 2000 chars).

---

## Contexto

O design do wizard original previa "Descricao Fisica" como um passo separado com campos como altura, peso, cor dos olhos, etc. Esses campos **nao existem no backend** como entidade separada. O backend tem apenas o campo `descricao` (texto livre) na entidade `Ficha`.

**Decisao desta task:** O Passo 2 coleta apenas o `descricao` (texto livre) onde o usuario pode descrever o personagem como quiser. O passo e opcional — se o usuario nao quiser preencher, clica "Proximo" e segue.

**Auto-save ao avancar:** `PUT /fichas/{id}` com o campo `descricao` atualizado.

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `ficha-form/ficha-wizard.component.ts` | Adicionar formPasso2 e logica de auto-save do Passo 2 |
| `ficha-form/steps/step-descricao/step-descricao.component.ts` | Criar componente dumb |
| `ficha-form/steps/step-descricao/step-descricao.component.html` | Template do Passo 2 |

---

## Wireframe do Passo 2

```
┌──────────────────────────────────────────────┐
│  H2: Descricao do Personagem                  │
│  (opcional) Como e seu personagem?            │
│                                               │
│  Descricao livre                              │
│  ┌──────────────────────────────────────┐     │
│  │ Descreva a aparencia, personalidade, │     │
│  │ historia ou qualquer detalhe que     │     │
│  │ queira registrar sobre seu           │     │
│  │ personagem...                        │     │
│  │                                      │     │
│  └──────────────────────────────────────┘     │
│  0/2000                                       │
│                                               │
│  [p-message severity="info"]                  │
│  Este campo e opcional. Voce pode preencher   │
│  agora ou depois, na tela de detalhes.        │
│                                               │
└──────────────────────────────────────────────┘
```

---

## Estrutura do Componente

### `StepDescricaoComponent` (Dumb)

```typescript
// Inputs
descricao = input<string | null>(null);

// Outputs
descricaoChanged = output<string | null>();
```

```html
<div class="flex flex-col gap-4 max-w-lg mx-auto">
  <div class="flex flex-col gap-1">
    <label for="descricao" class="font-medium text-sm">
      Descricao do Personagem
      <span class="text-color-secondary font-normal">(opcional)</span>
    </label>
    <p-textarea
      id="descricao"
      [ngModel]="descricao()"
      (ngModelChange)="descricaoChanged.emit($event)"
      placeholder="Descreva a aparencia, personalidade, historia ou qualquer detalhe..."
      autoResize="true"
      rows="6"
      maxlength="2000"
      styleClass="w-full"
      aria-label="Descricao do personagem" />
    <small class="text-color-secondary text-right">
      {{ (descricao()?.length ?? 0) }}/2000
    </small>
  </div>

  <p-message
    severity="info"
    text="Este campo e opcional. Voce pode preencher agora ou editar depois na tela do personagem." />
</div>
```

### Logica de Auto-save no `FichaWizardComponent` para o Passo 2

```typescript
readonly formPasso2 = signal<{ descricao: string | null }>({ descricao: null });

salvarPasso2(): Observable<FichaResponse> {
  // Sempre usa PUT pois fichaId ja existe apos Passo 1
  return this.fichasApiService.atualizar(this.fichaId()!, {
    descricao: this.formPasso2().descricao
  });
}
```

O auto-save do Passo 2 usa o mesmo `avancarPasso()` do wizard orquestrador, que determina qual metodo de save chamar baseado em `passoAtual()`.

---

## Testes Obrigatorios

| Cenario | Descricao |
|---------|-----------|
| Renderizacao | Exibe textarea com placeholder e contador 0/2000 |
| Avanco sem preencher | Botao "Proximo" habilitado (campo opcional); salva `descricao: null` |
| Avanco com texto | Textarea preenchida; salva `descricao: "Texto"` via PUT |
| Contador em tempo real | Ao digitar, contador atualiza imediatamente |
| Limite de caracteres | Nao permite digitar alem de 2000 chars |
| Pre-preenchimento (rascunho) | Se descricao existir na ficha, textarea ja preenchida |

---

## Criterios de Aceitacao

- [ ] Passo 2 exibe textarea de descricao livre (nao campos separados de altura, peso, etc.)
- [ ] Campo e opcional — botao "Proximo" sempre habilitado neste passo
- [ ] Contador de caracteres visivel (X/2000)
- [ ] Ao avancar, PUT /fichas/{id} e chamado com o campo `descricao`
- [ ] Se descricao for nula/vazia, envia `null` (nao string vazia) para o backend
- [ ] Mensagem informativa sobre opcionalidade do campo

---

## Premissa

- O campo `origem` (texto livre) do design do WIZARD-CRIACAO-FICHA.md era chamado de "Origem" no passo 1, mas nao existe no backend. Esta task consolida a descricao como um unico campo livre no Passo 2.
- Campos como altura, peso, cor dos olhos, cor do cabelo — **nao implementar**. Esses campos nao existem no backend e seriam dados sem uso mecanico. Se o PO desejar esses campos, requer spec dedicada para `FichaDescricaoFisica`.
