# T12 — Auto-save Visual (Indicador de Salvamento)

> Fase: Frontend
> Complexidade: Baixa
> Prerequisito: T6 (FichaWizardComponent criado)
> Bloqueia: Nenhum
> Estimativa: 1–2 horas

---

## Objetivo

Implementar o indicador visual de estado do auto-save no rodape do wizard, comunicando ao usuario quando os dados estao sendo salvos, foram salvos com sucesso ou tiveram erro. O indicador e compartilhado por todos os passos.

---

## Contexto

O estado de salvamento e gerenciado pelo `FichaWizardComponent` via `signal<EstadoSalvamento>`. Esta task se concentra exclusivamente no **componente visual** e na **integracao com o rodape do wizard**, garantindo que:

- O botao "Proximo" fica desabilitado durante o salvamento
- O usuario ve feedback claro em todos os momentos
- O componente de rodape e reutilizavel por todos os passos

---

## Tipo `EstadoSalvamento`

```typescript
// Definido em shared/types/wizard.types.ts
export type EstadoSalvamento = 'idle' | 'salvando' | 'salvo' | 'erro';
```

---

## Componente: `WizardRodapeComponent` (Dumb)

**Localizacao:** `src/app/shared/components/wizard-rodape/wizard-rodape.component.ts`

**Reutilizavel** em qualquer wizard futuro.

```typescript
// Inputs
estadoSalvamento = input.required<EstadoSalvamento>();
passoAtual = input.required<number>();
totalPassos = input.required<number>();
podeAvancar = input.required<boolean>();
podeCriar = input<boolean>(false);  // true no ultimo passo
criando = input<boolean>(false);

// Outputs
avancar = output<void>();
voltar = output<void>();
criar = output<void>();
```

---

## Template

```html
<div class="flex items-center justify-between mt-4 pt-4 border-t border-surface-border">

  <!-- Indicador de auto-save (esquerda) -->
  <div class="flex items-center gap-2 text-sm text-color-secondary min-h-[1.5rem]">
    @switch (estadoSalvamento()) {
      @case ('salvando') {
        <p-progressSpinner styleClass="w-4 h-4" strokeWidth="6" aria-label="Salvando..." />
        <span>Salvando...</span>
      }
      @case ('salvo') {
        <i class="pi pi-check-circle text-green-500"></i>
        <span>Salvo automaticamente</span>
      }
      @case ('erro') {
        <i class="pi pi-exclamation-triangle text-yellow-500"></i>
        <span class="text-yellow-600">Erro ao salvar. Tente novamente.</span>
      }
      @default {
        <!-- idle: espaco reservado para nao pular layout -->
        <span class="invisible">Salvo</span>
      }
    }
  </div>

  <!-- Navegacao (direita) -->
  <div class="flex gap-2">
    @if (passoAtual() > 1) {
      <p-button
        label="Voltar"
        icon="pi pi-arrow-left"
        outlined
        [disabled]="estadoSalvamento() === 'salvando'"
        (onClick)="voltar.emit()"
        aria-label="Voltar para o passo anterior" />
    }

    @if (!podeCriar()) {
      <p-button
        label="Proximo"
        icon="pi pi-arrow-right"
        iconPos="right"
        [disabled]="!podeAvancar() || estadoSalvamento() === 'salvando'"
        [loading]="estadoSalvamento() === 'salvando'"
        (onClick)="avancar.emit()"
        aria-label="Avancar para o proximo passo" />
    } @else {
      <p-button
        label="Criar Personagem"
        icon="pi pi-check"
        severity="success"
        [disabled]="estadoSalvamento() === 'salvando'"
        [loading]="criando()"
        (onClick)="criar.emit()"
        aria-label="Confirmar criacao do personagem" />
    }
  </div>
</div>
```

---

## Integracao no `FichaWizardComponent`

```html
<!-- No template do wizard, apos o conteudo do passo ativo -->
<app-wizard-rodape
  [estadoSalvamento]="estadoSalvamento()"
  [passoAtual]="passoAtual()"
  [totalPassos]="6"
  [podeAvancar]="passoAtualValido()"
  [podeCriar]="passoAtual() === 6"
  [criando]="criando()"
  (avancar)="avancarPasso()"
  (voltar)="voltarPasso()"
  (criar)="confirmarCriacao()" />
```

---

## Comportamento do Timer "Salvo"

O estado `salvo` deve ser exibido por 3 segundos e depois voltar para `idle`:

```typescript
// No FichaWizardComponent, ao receber resposta de sucesso:
this.estadoSalvamento.set('salvo');
setTimeout(() => {
  if (this.estadoSalvamento() === 'salvo') {
    this.estadoSalvamento.set('idle');
  }
}, 3000);
```

---

## Testes Obrigatorios

| Cenario | Descricao |
|---------|-----------|
| Estado idle | Nenhum indicador visivel (espaco reservado) |
| Estado salvando | Spinner + texto "Salvando..." visivel |
| Estado salvo | Check verde + "Salvo automaticamente" |
| Estado erro | Triangulo amarelo + "Erro ao salvar" |
| Botao Proximo bloqueado | Desabilitado durante estado "salvando" |
| Botao Voltar bloqueado | Desabilitado durante estado "salvando" |
| Reset automatico | Estado salvo volta para idle apos 3s |
| Passo 1: sem Voltar | Botao "Voltar" nao aparece no primeiro passo |
| Passo 6: botao Criar | Exibe "Criar Personagem" em vez de "Proximo" |

---

## Criterios de Aceitacao

- [ ] `WizardRodapeComponent` criado como componente standalone em `shared/components/`
- [ ] Indicador de auto-save com 4 estados: idle, salvando, salvo, erro
- [ ] Botoes "Proximo" e "Voltar" desabilitados durante estado "salvando"
- [ ] Estado "salvo" auto-reseta para "idle" apos 3 segundos
- [ ] Passo 1 nao exibe botao "Voltar"
- [ ] Passo 6 exibe "Criar Personagem" em vez de "Proximo"
- [ ] Testes unitarios cobrindo todos os estados e comportamentos dos botoes
