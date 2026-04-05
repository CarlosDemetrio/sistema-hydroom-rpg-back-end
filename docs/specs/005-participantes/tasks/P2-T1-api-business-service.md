# P2-T1 — Alinhar API service e Business service (Frontend)

> Fase: 2 — Frontend
> Complexidade: 🟢 pequena
> Depende de: P1-T2 (backend deve ter os endpoints antes de implementar o frontend)
> Bloqueia: P2-T2, P2-T3

---

## Objetivo

Adicionar ao `JogosApiService` e ao `ParticipanteBusinessService` os metodos correspondentes aos novos endpoints do backend: `desbanir`, `meuStatus`, `cancelarSolicitacao` e `listar com filtro`. Corrigir tambem a chamada de `banir` que estava sendo usada para remocao.

---

## Contexto — Arquivos a Ler Antes de Comecar

- `core/services/api/jogos-api.service.ts` — API service atual
- `core/services/business/participante-business.service.ts` — business service atual
- `core/models/participante.model.ts` — model Participante (verificar se precisa de campo `emailUsuario`)
- `core/stores/jogos.store.ts` — verificar metodos disponiveis: `setParticipantes`, `updateParticipanteInState`, etc.
- `docs/specs/005-participantes/spec.md` — secao 6 (contrato de API) e ParticipanteResponse

---

## Diagnostico: Estado Atual do API Service

### Metodos existentes (corretos)

```typescript
listParticipantes(jogoId: number): Observable<Participante[]>
solicitarParticipacao(jogoId: number): Observable<Participante>
aprovarParticipante(jogoId: number, participanteId: number): Observable<Participante>
rejeitarParticipante(jogoId: number, participanteId: number): Observable<Participante>
banirParticipante(jogoId: number, participanteId: number): Observable<Participante>
// Atencao: banirParticipante usa DELETE — sera renomeado para removerParticipante
// Um novo banirParticipante usara PUT /{pid}/banir
```

### O que falta adicionar

| Metodo | Endpoint | Role |
|--------|----------|------|
| `removerParticipante` (renomear o atual `banirParticipante`) | `DELETE /{pid}` | MESTRE |
| `banirParticipante` (novo) | `PUT /{pid}/banir` | MESTRE |
| `desbanirParticipante` | `PUT /{pid}/desbanir` | MESTRE |
| `meuStatusParticipacao` | `GET /meu-status` | JOGADOR |
| `cancelarSolicitacao` | `DELETE /minha-solicitacao` | JOGADOR |
| `listParticipantes` com filtro | `GET /?status=` | MESTRE |

---

## Passos de Implementacao

### Passo 1: Atualizar o model `Participante`

Verificar se `participante.model.ts` tem todos os campos de `ParticipanteResponse`:

```typescript
// Spec diz que a response tem: id, jogoId, usuarioId, nomeUsuario, role, status, dataCriacao, dataUltimaAtualizacao
// Verificar se o campo se chama nomeUsuario ou usuarioNome no modelo atual
```

A spec original do BA usou `usuarioNome` e `usuarioEmail`, mas o backend retorna `nomeUsuario` (conforme `ParticipanteResponse.java`). O modelo atual ja usa `nomeUsuario`. Nenhuma mudanca necessaria.

### Passo 2: Atualizar `jogos-api.service.ts`

Localizar a secao de Participantes e:

1. Renomear `banirParticipante` para `removerParticipante` (DELETE /{pid})
2. Adicionar `banirParticipante` (PUT /{pid}/banir)
3. Adicionar `desbanirParticipante` (PUT /{pid}/desbanir)
4. Adicionar `meuStatusParticipacao` (GET /meu-status)
5. Adicionar `cancelarSolicitacao` (DELETE /minha-solicitacao)
6. Atualizar `listParticipantes` para aceitar `status` opcional

```typescript
// Atualizar listParticipantes para aceitar filtro
listParticipantes(jogoId: number, status?: StatusParticipante): Observable<Participante[]> {
  const params = status ? { params: { status } } : {};
  return this.http.get<Participante[]>(`${this.baseUrl}/${jogoId}/participantes`, params);
}

// Renomear o DELETE atual para remover (provisorio)
removerParticipante(jogoId: number, participanteId: number): Observable<void> {
  return this.http.delete<void>(
    `${this.baseUrl}/${jogoId}/participantes/${participanteId}`
  );
}

// Novo banir via PUT
banirParticipante(jogoId: number, participanteId: number): Observable<Participante> {
  return this.http.put<Participante>(
    `${this.baseUrl}/${jogoId}/participantes/${participanteId}/banir`,
    {}
  );
}

// Novo desbanir
desbanirParticipante(jogoId: number, participanteId: number): Observable<Participante> {
  return this.http.put<Participante>(
    `${this.baseUrl}/${jogoId}/participantes/${participanteId}/desbanir`,
    {}
  );
}

// Meu status
meuStatusParticipacao(jogoId: number): Observable<Participante | null> {
  return this.http.get<Participante>(
    `${this.baseUrl}/${jogoId}/participantes/meu-status`
  ).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 404) return of(null);
      throw err;
    })
  );
}

// Cancelar propria solicitacao
cancelarSolicitacao(jogoId: number): Observable<void> {
  return this.http.delete<void>(
    `${this.baseUrl}/${jogoId}/participantes/minha-solicitacao`
  );
}
```

**Import necessario:** `of` do rxjs, `catchError` do rxjs/operators, `HttpErrorResponse` de `@angular/common/http`.

### Passo 3: Atualizar `participante-business.service.ts`

```typescript
// Atualizar removerParticipante (era banirParticipante)
removerParticipante(jogoId: number, participanteId: number): Observable<void> {
  return this.jogosApi.removerParticipante(jogoId, participanteId).pipe(
    tap(() => this.jogosStore.removeParticipanteFromState(jogoId, participanteId))
  );
}

// Novo banir
banirParticipante(jogoId: number, participanteId: number): Observable<Participante> {
  return this.jogosApi.banirParticipante(jogoId, participanteId).pipe(
    tap(updated => this.jogosStore.updateParticipanteInState(jogoId, participanteId, updated))
  );
}

// Novo desbanir
desbanirParticipante(jogoId: number, participanteId: number): Observable<Participante> {
  return this.jogosApi.desbanirParticipante(jogoId, participanteId).pipe(
    tap(updated => this.jogosStore.updateParticipanteInState(jogoId, participanteId, updated))
  );
}

// Meu status
meuStatus(jogoId: number): Observable<Participante | null> {
  return this.jogosApi.meuStatusParticipacao(jogoId);
}

// Cancelar solicitacao
cancelarSolicitacao(jogoId: number): Observable<void> {
  return this.jogosApi.cancelarSolicitacao(jogoId).pipe(
    tap(() => this.jogosStore.clearMeuStatus(jogoId))
  );
}
```

**Atencao:** Verificar se `JogosStore` tem os metodos `removeParticipanteFromState` e `clearMeuStatus`. Se nao existirem, adiciona-los ao store.

### Passo 4: Verificar JogosStore

```bash
# Verificar metodos existentes
grep -n "removeParticipante\|clearMeuStatus\|updateParticipante" \
  src/app/core/stores/jogos.store.ts
```

Se `removeParticipanteFromState` nao existir, adicionar em `jogos.store.ts`:

```typescript
removeParticipanteFromState(jogoId: number, participanteId: number): void {
  patchState(this, state => ({
    participantesPorJogo: {
      ...state.participantesPorJogo,
      [jogoId]: (state.participantesPorJogo[jogoId] ?? [])
        .filter(p => p.id !== participanteId)
    }
  }));
}
```

---

## Testes a Atualizar/Adicionar

### `participante-business.service.spec.ts`

Adicionar testes para os novos metodos:

```typescript
describe('desbanirParticipante', () => {
  it('deve chamar o endpoint correto e atualizar o store', () => {
    const mockParticipante = { id: 1, status: 'APROVADO' } as Participante;
    vi.spyOn(jogosApi, 'desbanirParticipante').mockReturnValue(of(mockParticipante));
    const updateSpy = vi.spyOn(jogosStore, 'updateParticipanteInState');

    service.desbanirParticipante(5, 1).subscribe();

    expect(jogosApi.desbanirParticipante).toHaveBeenCalledWith(5, 1);
    expect(updateSpy).toHaveBeenCalledWith(5, 1, mockParticipante);
  });
});

describe('meuStatus', () => {
  it('deve retornar null se nao ha participacao', () => {
    vi.spyOn(jogosApi, 'meuStatusParticipacao').mockReturnValue(of(null));
    service.meuStatus(5).subscribe(status => {
      expect(status).toBeNull();
    });
  });
});
```

---

## Criterios de Aceitacao

- [ ] `removerParticipante` chama `DELETE /{pid}` (nao mais banir via DELETE)
- [ ] `banirParticipante` chama `PUT /{pid}/banir`
- [ ] `desbanirParticipante` chama `PUT /{pid}/desbanir`
- [ ] `meuStatusParticipacao` retorna `null` em caso de 404 (sem lancar erro)
- [ ] `cancelarSolicitacao` chama `DELETE /minha-solicitacao`
- [ ] `listParticipantes(jogoId, 'PENDENTE')` passa `?status=PENDENTE` na query string
- [ ] JogosStore tem `removeParticipanteFromState` funcionando
- [ ] Testes do business service passando (vitest run)
- [ ] Build sem erros TypeScript (`npm run build`)
