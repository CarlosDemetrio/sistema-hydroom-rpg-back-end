# T11 — Passo 6: Revisao e Confirmacao

> Fase: Frontend
> Complexidade: Media
> Prerequisito: T1 (endpoint /completar), T6 (Passo 1)
> Recomendado ter antes: T8, T9, T10 (para exibir dados completos)
> Bloqueia: Nenhum (e o passo final)
> Estimativa: 3–4 horas

---

## Objetivo

Implementar o Passo 6 (Revisao e Confirmacao) do wizard, que exibe um resumo de todos os dados preenchidos nos passos anteriores e oferece o botao "Criar Personagem" que transiciona a ficha de RASCUNHO para COMPLETA via `PUT /fichas/{id}/completar`.

---

## Contexto

Este e o ultimo passo do wizard. O usuario nao precisa preencher nada — apenas revisar e confirmar. Se quiser corrigir algo, pode usar o botao "Voltar" para retornar ao passo anterior.

Ao clicar "Criar Personagem":
1. Frontend chama `PUT /fichas/{id}/completar`
2. Backend valida todos os campos obrigatorios
3. Se OK: `status = COMPLETA`, frontend navega para `/fichas/{id}` (FichaDetailPage)
4. Se erro: toast com mensagem clara, usuario fica no Passo 6

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `ficha-form/ficha-wizard.component.ts` | Adicionar logica de confirmacao e navegacao |
| `ficha-form/steps/step-revisao/step-revisao.component.ts` | Criar componente dumb |
| `ficha-form/steps/step-revisao/step-revisao.component.html` | Template de revisao |
| `fichas-api.service.ts` | Adicionar metodo `completar(fichaId)` |

---

## Wireframe do Passo 6

```
┌──────────────────────────────────────────────────────────────────┐
│  H2: Revisao                                                     │
│  Tudo pronto. Revise os dados do seu personagem antes de        │
│  confirmar.                                                      │
│                                                                  │
│  ┌─── Identificacao ──────────────────────────────────────┐    │
│  │ Nome:     Aldric, o Guardiao          [Editar Passo 1] │    │
│  │ Raca:     Humano                                        │    │
│  │ Classe:   Guerreiro                                     │    │
│  │ Genero:   Masculino                                     │    │
│  │ Indole:   Leal                                          │    │
│  │ Presenca: Austero                                       │    │
│  └──────────────────────────────────────────────────────── ┘    │
│                                                                  │
│  ┌─── Atributos ──────────────────────────────────────────┐    │
│  │ FOR: 8 (base) + 2 (raca) = 10    [Editar Passo 3]     │    │
│  │ AGI: 5 (base)              =  5                         │    │
│  │ VIG: 2 (base)              =  2                         │    │
│  │ ...                                                     │    │
│  │ Pontos nao usados: 0                                    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─── Aptidoes ────────────────────────────────────────────┐    │
│  │ Acrobacia: 3  Atletismo: 5  ...   [Editar Passo 4]     │    │
│  │ Pontos nao usados: 4                                    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─── Vantagens (1 comprada) ──────────────────────────────┐   │
│  │ Combate Pesado (Nivel 1) — Custo: 2 pts  [Editar P.5]  │   │
│  │ Pontos nao usados: 1                                    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                  │
│  [p-button "Criar Personagem" severity="success" size="large"]  │
└──────────────────────────────────────────────────────────────────┘
```

---

## Dados Exibidos no Resumo

O componente recebe os dados ja coletados nos passos anteriores via signals do `FichaWizardComponent`. Nao faz chamadas API adicionais.

| Secao | Dados exibidos | Fonte |
|---|---|---|
| Identificacao | nome, raca, classe, genero, indole, presenca | `formPasso1()` |
| Descricao | descricao (se preenchida) | `formPasso2()` |
| Atributos | base + outros por atributo, pontos nao usados | `formPasso3()` + `pontosAtributoDisponiveis` |
| Aptidoes | base por aptidao agrupada, pontos nao usados | `formPasso4()` + `pontosAptidaoDisponiveis` |
| Vantagens | nome, nivel 1, custo por vantagem comprada | `vantagensCompradas()` + `pontosVantagemDisponiveis` |

---

## Estrutura do Componente

### `StepRevisaoComponent` (Dumb)

```typescript
// Inputs
formPasso1 = input.required<FormPasso1>();
formPasso2 = input.required<{ descricao: string | null }>();
atributos = input.required<FichaAtributoEditavel[]>();
aptidoesAgrupadas = input.required<TipoAptidaoComAptidoes[]>();
vantagensCompradas = input.required<FichaVantagemResponse[]>();
pontosAtributoNaoUsados = input.required<number>();
pontosAptidaoNaoUsados = input.required<number>();
pontosVantagemNaoUsados = input.required<number>();
criando = input.required<boolean>();

// Outputs
editarPasso = output<number>();   // Emite o numero do passo para editar
confirmar = output<void>();
```

### Logica de confirmacao no `FichaWizardComponent`

```typescript
confirmarCriacao(): void {
  if (this.criando()) return;
  this.criando.set(true);
  this.estadoSalvamento.set('salvando');

  this.fichasApiService.completar(this.fichaId()!).subscribe({
    next: (ficha) => {
      this.criando.set(false);
      this.estadoSalvamento.set('salvo');
      // Navegar para FichaDetailPage
      this.router.navigate(['/fichas', ficha.id]);
    },
    error: (err) => {
      this.criando.set(false);
      this.estadoSalvamento.set('erro');
      this.messageService.add({
        severity: 'error',
        summary: 'Erro ao criar personagem',
        detail: err.error?.message ?? 'Verifique os dados e tente novamente.'
      });
    }
  });
}
```

### Adicionar `completar()` ao `FichasApiService`

```typescript
completar(fichaId: number): Observable<FichaResponse> {
  return this.http.put<FichaResponse>(`${this.baseUrl}/fichas/${fichaId}/completar`, {});
}
```

---

## Testes Obrigatorios

| Cenario | Descricao |
|---------|-----------|
| Renderizacao do resumo | Exibe todos os dados dos passos anteriores organizados |
| Editar Passo X | Clicar em "Editar" navega para o passo correto |
| Botao "Criar Personagem" habilitado | Quando todos os dados obrigatorios preenchidos |
| Loading no botao | Durante request, botao mostra spinner e fica desabilitado |
| Sucesso | PUT /fichas/{id}/completar retorna 200 → navega para /fichas/{id} |
| Erro de validacao | Backend retorna 422 → toast com mensagem, permanece no Passo 6 |
| Pontos nao usados exibidos | "X pontos de atributo nao utilizados" visivel |
| Descricao omitida | Se descricao for nula, secao nao aparece no resumo |
| Vantagens vazias | Se nenhuma vantagem foi comprada, secao exibe "Nenhuma vantagem comprada" |

---

## Criterios de Aceitacao

- [ ] Resumo exibe dados de todos os passos: identificacao, atributos, aptidoes, vantagens
- [ ] Botoes "Editar Passo X" presentes em cada secao
- [ ] Pontos nao utilizados exibidos em cada secao relevante
- [ ] Botao "Criar Personagem" com loading state durante request
- [ ] Apos sucesso: navega para FichaDetailPage (`/fichas/{id}`)
- [ ] Apos erro 422: toast com mensagem do backend, fica no Passo 6
- [ ] FichasApiService tem metodo `completar(fichaId)`
