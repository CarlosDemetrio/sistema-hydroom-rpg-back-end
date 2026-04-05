# T3 — Bloquear XP no `PUT /fichas/{id}` para JOGADOR

> Fase: Backend
> Complexidade: Baixa
> Prerequisito: Nenhum
> Bloqueia: Nenhum (seguranca)
> Estimativa: 1–2 horas

---

## Objetivo

Garantir que o campo `xp` em `UpdateFichaRequest` seja ignorado quando o ator for JOGADOR. Atualmente, o `PUT /fichas/{id}` aceita `xp` no body sem verificar a role do usuario — um JOGADOR poderia editar o proprio XP diretamente.

---

## Contexto

A decisao do PO (2026-04-02) e clara: **XP e campo somente MESTRE**. O campo existe no `UpdateFichaRequest` e e processado pelo `FichaService.atualizar()` sem verificacao de role. Este e um gap de seguranca que deve ser corrigido antes que qualquer frontend seja implementado.

O endpoint dedicado `PUT /fichas/{id}/xp` sera criado na T4. Esta task apenas garante que o campo `xp` no endpoint generico de atualizacao seja bloqueado para JOGADOR.

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `service/FichaService.java` | Adicionar verificacao de role antes de aplicar xp no `atualizar()` |
| `dto/request/UpdateFichaRequest.java` | Verificar se o campo xp existe — se nao, nao ha nada a fazer |

---

## Passos de Implementacao

### 1. Verificar o `UpdateFichaRequest`

Ler o conteudo do record para confirmar se o campo `xp` existe no DTO. Se existir, prosseguir com a implementacao abaixo.

### 2. Adicionar verificacao de role em `FichaService.atualizar()`

No metodo `atualizar()` do `FichaService`, antes de aplicar o campo `xp` ao objeto `ficha`, verificar se o usuario atual e JOGADOR:

```java
// Se o request inclui xp e o ator nao e MESTRE: ignorar o campo xp silenciosamente
// (nao lancar 403 — apenas nao aplicar o valor)
boolean isMestre = checkMestre(jogoId);
if (request.xp() != null && !isMestre) {
    log.warn("JOGADOR tentou atualizar XP da ficha {}. Campo ignorado.", fichaId);
    // Nao aplicar o xp — prosseguir com os outros campos
}
```

**Opcao alternativa (mais estrita):** lancar `ForbiddenException` se o campo `xp` vier preenchido por um JOGADOR. Esta opcao e mais segura pois avisa o frontend do erro em vez de ignorar silenciosamente.

**Decisao recomendada:** Lancar 403. O frontend que esta sendo reescrito (T6+) nunca enviara `xp` pelo endpoint generico. O 403 expoe tentativas de manipulacao.

```java
if (request.xp() != null && !isMestre) {
    throw new ForbiddenException("Apenas o Mestre pode alterar o XP de uma ficha.");
}
```

### 3. Verificar campos `renascimentos` e `nivel`

Aproveitar esta task para verificar se `renascimentos` e `nivel` tambem sao editaveis por JOGADOR no `UpdateFichaRequest`. Se sim, aplicar a mesma restricao — estes campos sao exclusivos do Mestre.

---

## Testes Obrigatorios

| Cenario | Given | When | Then |
|---------|-------|------|------|
| JOGADOR tenta atualizar XP | Ficha do JOGADOR logado | `PUT /fichas/{id}` com `{ xp: 5000 }` | HTTP 403 Forbidden |
| MESTRE atualiza XP via PUT | MESTRE logado | `PUT /fichas/{id}` com `{ xp: 5000 }` | HTTP 200, xp atualizado |
| JOGADOR atualiza nome (sem xp) | Ficha do JOGADOR | `PUT /fichas/{id}` com `{ nome: "Novo Nome" }` | HTTP 200, nome atualizado |
| JOGADOR tenta atualizar renascimentos | Ficha do JOGADOR | `PUT /fichas/{id}` com `{ renascimentos: 1 }` | HTTP 403 |

---

## Criterios de Aceitacao

- [ ] `PUT /fichas/{id}` retorna HTTP 403 quando JOGADOR envia campo `xp` no body
- [ ] `PUT /fichas/{id}` retorna HTTP 200 quando MESTRE envia campo `xp`
- [ ] `PUT /fichas/{id}` retorna HTTP 200 quando JOGADOR atualiza campos permitidos (nome, raca, classe, etc.) sem enviar xp
- [ ] Campos `renascimentos` e `nivel` tambem bloqueados para JOGADOR (se editaveis no DTO)
- [ ] Testes de integracao para todos os cenarios acima

---

## Observacoes

- Esta task e uma correccao de seguranca, nao uma feature nova. Deve ser implementada o quanto antes, antes de qualquer trabalho de frontend.
- O campo `xp` no endpoint generico `PUT /fichas/{id}` pode ser removido completamente em uma refatoracao futura — o endpoint `/xp` (T4) sera o unico caminho. Por ora, mantenha o campo mas bloqueie para JOGADOR.
