# T2 — Validacao `RacaClassePermitida` na Criacao de Ficha

> Fase: Backend
> Complexidade: Baixa
> Prerequisito: Nenhum
> Bloqueia: T1 (enriquece a validacao de completude), T6 (Passo 2 do wizard)
> Estimativa: 2 horas

---

## Objetivo

Garantir que, ao criar ou atualizar uma ficha com `racaId` e `classeId`, o backend valide que a `ClassePersonagem` selecionada e permitida pela `Raca` selecionada via a tabela `RacaClassePermitida`. Sem esta validacao, o sistema aceita combinacoes invalidas de raca/classe.

---

## Contexto

A entidade `RacaClassePermitida` ja existe no modelo (`model/RacaClassePermitida.java`). A validacao ainda nao e aplicada no `FichaService.criar()` nem no `FichaService.atualizar()`. Esta task adiciona essa verificacao de forma centralizada em `FichaValidationService`.

**Regra de negocio:**
- Se a Raca tem registros em `RacaClassePermitida`, entao a Classe DEVE estar na lista
- Se a Raca nao tem nenhum registro em `RacaClassePermitida` (lista vazia), entao qualquer Classe e permitida
- Esta validacao so ocorre quando AMBOS `racaId` e `classeId` estao presentes na requisicao

---

## Arquivos Afetados

| Arquivo | Operacao |
|---------|----------|
| `service/FichaValidationService.java` | Adicionar `validarClassePermitidaPelaRaca()` |
| `service/FichaService.java` | Chamar validacao no `criar()` e `atualizar()` |
| `repository/RacaClassePermitidaRepository.java` | Adicionar query de verificacao |

---

## Passos de Implementacao

### 1. Adicionar query no `RacaClassePermitidaRepository`

```java
// Retorna true se a Raca nao tem restricoes (lista vazia) OU se a Classe esta na lista
boolean existsByRacaIdAndClasseId(Long racaId, Long classeId);

// Retorna se a Raca tem alguma restricao configurada
boolean existsByRacaId(Long racaId);
```

### 2. Adicionar `validarClassePermitidaPelaRaca()` em `FichaValidationService`

```java
public void validarClassePermitidaPelaRaca(Raca raca, ClassePersonagem classe) {
    if (raca == null || classe == null) {
        return; // Validacao so aplica quando ambos estao presentes
    }

    boolean racaTemRestricoes = racaClassePermitidaRepository.existsByRacaId(raca.getId());
    if (!racaTemRestricoes) {
        return; // Sem restricoes: qualquer classe e permitida
    }

    boolean classePermitida = racaClassePermitidaRepository
            .existsByRacaIdAndClasseId(raca.getId(), classe.getId());
    if (!classePermitida) {
        throw new ValidationException(
                "A classe '" + classe.getNome() + "' nao e permitida para a raca '" + raca.getNome() + "'.");
    }
}
```

### 3. Chamar a validacao em `FichaService.criar()`

Em `FichaService.criar()`, apos resolver as FKs de Raca e Classe (passo 5 do metodo existente), adicionar:

```java
fichaValidationService.validarClassePermitidaPelaRaca(raca, classe);
```

### 4. Chamar a validacao em `FichaService.atualizar()`

Em `FichaService.atualizar()`, apos resolver as FKs de Raca e Classe do request de atualizacao, adicionar a mesma chamada.

### 5. Integrar com `validarCompletude()` (T1)

Em `FichaValidationService.validarCompletude()`, apos validar que raca e classe nao sao nulos, chamar:

```java
validarClassePermitidaPelaRaca(ficha.getRaca(), ficha.getClasse());
```

---

## Testes Obrigatorios

| Cenario | Given | When | Then |
|---------|-------|------|------|
| Classe permitida pela Raca | Raca "Elfo" permite Classe "Mago" | `criar()` com racaId=Elfo, classeId=Mago | Ficha criada com sucesso |
| Classe proibida pela Raca | Raca "Elfo" NAO permite Classe "Guerreiro" | `criar()` com racaId=Elfo, classeId=Guerreiro | `ValidationException` com mensagem clara |
| Raca sem restricoes | Raca "Humano" sem RacaClassePermitida | `criar()` com qualquer Classe | Ficha criada com sucesso |
| Somente Raca informada | `classeId = null`, `racaId = Elfo` | `criar()` | Ficha criada (validacao nao aplica) |
| Somente Classe informada | `racaId = null`, `classeId = Guerreiro` | `criar()` | Ficha criada (validacao nao aplica) |

---

## Criterios de Aceitacao

- [ ] `POST /fichas` retorna 422 quando Classe nao e permitida pela Raca selecionada
- [ ] `PUT /fichas/{id}` retorna 422 ao tentar atualizar com combinacao invalida
- [ ] `PUT /fichas/{id}/completar` retorna 422 quando a ficha tem combinacao invalida
- [ ] Quando a Raca nao tem restricoes, qualquer Classe e aceita
- [ ] Quando um dos campos e nulo, a validacao e ignorada
- [ ] Mensagem de erro clara: "A classe 'X' nao e permitida para a raca 'Y'"
- [ ] Testes de integracao cobrindo todos os cenarios acima

---

## Observacoes

- Esta validacao nao e retroativa — fichas ja existentes com combinacoes invalidas nao sao afetadas. O backend valida apenas na criacao e atualizacao.
- O Mestre pode criar qualquer combinacao? **Decisao: Nao** — a restricao se aplica a todos os atores, incluindo o Mestre. O Mestre configura as regras e deve respeitd-las.
- Se o PO quiser que o Mestre possa "forcar" combinacoes invalidas, adicionar parametro `forcar=true` como ponto em aberto (PA-005).
