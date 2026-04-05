# T4 — Testes: API service sub-recursos + componentes

> **Complexidade:** media
> **Depende de:** T1, T2, T3 (tipagem + componentes corrigidos)
> **Bloqueia:** —
> **Arquivo principal:** `config-api.service.spec.ts`

---

## Objetivo

Adicionar cobertura de testes para os metodos de sub-recurso do `ConfigApiService` e
validar o comportamento dos novos campos nos componentes de classes e racas.

---

## Contexto

O `config-api.service.spec.ts` ja existe e provavelmente cobre os metodos CRUD base.
Os metodos de sub-recurso (`addClasseBonus`, `removeClasseBonus`, `addClasseAptidaoBonus`,
`addRacaBonusAtributo`, `addRacaClassePermitida`, etc.) podem nao ter cobertura.

Verificar o arquivo antes de implementar para evitar duplicacao.

---

## Arquivos Afetados

1. `src/app/core/services/api/config-api.service.spec.ts` — novos testes de sub-recurso
2. (opcional) `src/app/features/mestre/pages/config/configs/classes-config/classes-config.component.spec.ts` — se nao existir, criar
3. (opcional) `src/app/features/mestre/pages/config/configs/racas-config/racas-config.component.spec.ts` — se nao existir, criar

---

## Testes Prioritarios

### 1. `config-api.service.spec.ts` — Sub-recursos de Classe

```typescript
describe('Sub-recursos de ClassePersonagem', () => {
  describe('addClasseBonus', () => {
    it('deve enviar POST com bonusConfigId e valorPorNivel', () => {
      // Arrange: mock HttpTestingController
      // Act: chamar addClasseBonus(1, { bonusConfigId: 2, valorPorNivel: 0.5 })
      // Assert: verificar que a requisicao foi para /classes/1/bonus
      //         com body { bonusConfigId: 2, valorPorNivel: 0.5 }
    });

    it('deve enviar valorPorNivel fracionario corretamente', () => {
      // valorPorNivel = 0.25 deve ser serializado como 0.25, nao como 0
    });
  });

  describe('addClasseAptidaoBonus', () => {
    it('deve enviar POST com aptidaoConfigId e bonus', () => {
      // Arrange/Act/Assert: verificar body { aptidaoConfigId: 5, bonus: 2 }
    });
  });

  describe('removeClasseBonus', () => {
    it('deve enviar DELETE para /classes/{classeId}/bonus/{bonusId}', () => {
      // Verificar URL correta
    });
  });

  describe('removeClasseAptidaoBonus', () => {
    it('deve enviar DELETE para /classes/{classeId}/aptidao-bonus/{id}', () => {
      // Verificar URL correta
    });
  });
});

describe('Sub-recursos de Raca', () => {
  describe('addRacaBonusAtributo', () => {
    it('deve enviar POST com atributoConfigId e bonus positivo', () => {
      // body: { atributoConfigId: 3, bonus: 2 }
    });

    it('deve enviar bonus negativo (penalidade) corretamente', () => {
      // body: { atributoConfigId: 3, bonus: -1 }
      // Verificar que o valor negativo e preservado na requisicao
    });
  });

  describe('addRacaClassePermitida', () => {
    it('deve enviar POST com classeId', () => {
      // body: { classeId: 7 }
    });
  });

  describe('removeRacaBonusAtributo', () => {
    it('deve enviar DELETE para /racas/{racaId}/bonus-atributos/{id}', () => {});
  });

  describe('removeRacaClassePermitida', () => {
    it('deve enviar DELETE para /racas/{racaId}/classes-permitidas/{id}', () => {});
  });
});
```

### 2. Testes de componente — ClassesConfigComponent

**Verificar se `classes-config.component.spec.ts` existe.** Se nao existir, criar com testes basicos.

Testes prioritarios:
```typescript
describe('ClassesConfigComponent', () => {
  it('deve desabilitar botão Adicionar Bonus quando valorPorNivel <= 0', () => {
    // Arrange: montar componente com selectedBonusId definido
    // Act: setar valorPorNivelInput = 0
    // Assert: botao "Adicionar" tem atributo disabled
  });

  it('deve exibir valorPorNivel na lista de bonusConfig', () => {
    // Arrange: selectedClasse com bonusConfig[{bonusNome: "B.B.A", valorPorNivel: 1.5}]
    // Assert: template contem o texto "1.5 por nível" ou equivalente
  });

  it('deve exibir bonus na lista de aptidaoBonus', () => {
    // Arrange: selectedClasse com aptidaoBonus[{aptidaoNome: "Furtividade", bonus: 2}]
    // Assert: template contem "+2"
  });

  it('deve exibir preview "Exemplo no nível 5" calculado corretamente', () => {
    // valorPorNivelInput = 2.0 -> preview deve mostrar "+10"
  });
});
```

### 3. Testes de componente — RacasConfigComponent

**Verificar se `racas-config.component.spec.ts` existe.** Se nao existir, criar com testes basicos.

Testes prioritarios:
```typescript
describe('RacasConfigComponent', () => {
  it('deve exibir "(penalidade)" para RacaBonusAtributo com bonus negativo', () => {
    // Arrange: selectedRaca com bonusAtributos[{atributoNome: "Vigor", bonus: -1}]
    // Assert: template contem "(penalidade)"
  });

  it('nao deve exibir "(penalidade)" para bonus positivo', () => {
    // bonus = 2 -> nao deve ter "(penalidade)" no texto
  });

  it('deve exibir "Todas as classes são permitidas" quando classesPermitidas vazio', () => {
    // Arrange: selectedRaca com classesPermitidas = []
    // Assert: texto de estado vazio presente
  });

  it('deve exibir coluna Classes com "Sem restrições" para raca sem classesPermitidas', () => {
    // Verifica que a tabela principal tem a coluna de restricao
  });
});
```

---

## Instrucoes de Execucao

```bash
# Rodar apenas os testes de API service
npx vitest run src/app/core/services/api/config-api.service.spec.ts

# Rodar apenas os testes de ClassesConfig
npx vitest run src/app/features/mestre/pages/config/configs/classes-config/

# Rodar todos os testes (baseline deve ser >= 271)
npx vitest run
```

---

## Criterios de Aceitacao

- [ ] Novos testes cobrem todos os 8 metodos de sub-recurso do `ConfigApiService`
- [ ] Teste verifica que `valorPorNivel` fracionario (0.5) e serializado corretamente no POST
- [ ] Teste verifica que `bonus` negativo (-1) de RacaBonusAtributo e preservado no POST
- [ ] Testes de componente existem para ClassesConfig e RacasConfig (novos ou existentes expandidos)
- [ ] `npx vitest run` passa com >= 271 testes (baseline) + novos testes adicionados
- [ ] Nenhum teste anterior regrediu

---

## Observacoes

- Usar `vi.fn()` (Vitest) para mocks de service, nao `jest.fn()`
- Usar `HttpTestingController` do `@angular/common/http/testing` para testes de API service
- Seguir padrao Arrange-Act-Assert com `@testing-library/angular` para testes de componente
- Se `classes-config.component.spec.ts` nao existir, criacao minima com 3-4 testes e aceitavel
- Nao criar spec files para classes que ja tem cobertura adequada — verificar antes
