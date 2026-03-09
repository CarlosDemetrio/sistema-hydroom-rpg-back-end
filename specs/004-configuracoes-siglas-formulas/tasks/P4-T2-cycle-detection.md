# P4-T2 — Detecção de ciclos em VantagemPreRequisito

## Objetivo
Implementar a lógica de detecção de ciclos (diretos e transitivos) na criação de pré-requisitos de vantagens.

## Depende de
P4-T1 (VantagemPreRequisito entity + VantagemPreRequisitoRepository)

## O problema

Um ciclo acontece quando a cadeia de pré-requisitos cria uma dependência circular:

```
Ciclo direto:   A requer B,  B requer A
Ciclo transitivo: A requer B, B requer C, C requer A
```

Se ocorrer, é impossível "começar" — não há vantagem que possa ser comprada primeiro.

## Algoritmo (DFS — Depth-First Search)

```
Para adicionar pré-requisito: vantagemA requer vantagemB

1. Se A == B → rejeitar (auto-referência)
2. Coletar todos os requisitos transitivos de B (quem B requer, quem esses requerem, etc.)
3. Se A aparecer nesse conjunto → ciclo detectado → rejeitar
4. Caso contrário → adicionar o pré-requisito
```

### Implementação em Java

```java
// No VantagemConfiguracaoService (ou num PreRequisitoService dedicado)

private void verificarCiclo(Long vantagemId, Long requisitoId) {
    // Auto-referência
    if (vantagemId.equals(requisitoId)) {
        throw new ConflictException(
            ValidationMessages.VantagemPreRequisito.AUTO_REFERENCIA);
    }

    // DFS: percorrer todos os pré-requisitos de 'requisitoId' em profundidade
    // Se encontrar vantagemId, é ciclo
    Set<Long> visitados = new HashSet<>();
    Deque<Long> fila = new ArrayDeque<>();
    fila.push(requisitoId);

    while (!fila.isEmpty()) {
        Long atual = fila.pop();
        if (!visitados.add(atual)) continue; // já visitado

        // Buscar o que 'atual' requer
        List<Long> requisitosDoAtual = prerequisitoRepository.findByVantagemId(atual)
            .stream()
            .map(p -> p.getRequisito().getId())
            .toList();

        for (Long req : requisitosDoAtual) {
            if (req.equals(vantagemId)) {
                throw new ConflictException(
                    ValidationMessages.VantagemPreRequisito.CICLO_DETECTADO);
            }
            fila.push(req);
        }
    }
}
```

### Exemplo de execução

Estado inicial: `A requer B`, `B requer C`

Tentativa: adicionar `C requer A`
```
fila = [A]
pop A → requisitosDeA = [B]
  B == C? Não → push B
fila = [B]
pop B → requisitosDeB = [C]
  C == C? Sim → CICLO DETECTADO → ConflictException
```

Tentativa: adicionar `D requer A` (D é nova, sem pré-requisitos)
```
fila = [A]
pop A → requisitosDeA = [B]
  B == D? Não → push B
fila = [B]
pop B → requisitosDeB = [C]
  C == D? Não → push C
fila = [C]
pop C → requisitosDeC = []
fila vazia → sem ciclo → OK
```

## Onde chamar verificarCiclo

No método de criação de pré-requisito em `VantagemConfiguracaoService`:

```java
@Transactional
public VantagemPreRequisito adicionarPreRequisito(Long vantagemId, Long requisitoId, Integer nivelMinimo) {
    VantagemConfig vantagem = buscarPorId(vantagemId);
    VantagemConfig requisito = buscarPorId(requisitoId);

    // Validar mesmo jogo
    if (!vantagem.getJogo().getId().equals(requisito.getJogo().getId())) {
        throw new ValidationException(ValidationMessages.VantagemPreRequisito.JOGOS_DIFERENTES);
    }

    // Verificar duplicata
    if (prerequisitoRepository.existsByVantagemIdAndRequisitoId(vantagemId, requisitoId)) {
        throw new ConflictException(ValidationMessages.VantagemPreRequisito.JA_EXISTE);
    }

    // Detectar ciclo antes de persistir
    verificarCiclo(vantagemId, requisitoId);

    VantagemPreRequisito pr = VantagemPreRequisito.builder()
        .vantagem(vantagem)
        .requisito(requisito)
        .nivelMinimo(nivelMinimo != null ? nivelMinimo : 1)
        .build();
    return prerequisitoRepository.save(pr);
}
```

## Mensagens de erro (ValidationMessages)

```java
public static final class VantagemPreRequisito {
    public static final String AUTO_REFERENCIA =
        "Uma vantagem não pode ser pré-requisito de si mesma.";
    public static final String CICLO_DETECTADO =
        "Adicionar este pré-requisito criaria uma dependência circular entre vantagens.";
    public static final String JA_EXISTE =
        "Este pré-requisito já está registrado para esta vantagem.";
    public static final String JOGOS_DIFERENTES =
        "O pré-requisito deve pertencer ao mesmo jogo da vantagem.";
}
```

## Acceptance Checks

| Cenário | Resultado esperado |
|---|---|
| A requer B (sem ciclo) | Aceito |
| B requer A (já tem A requer B) | ConflictException ciclo direto |
| A requer B, B requer C, C requer A | ConflictException ciclo transitivo |
| A requer A | ConflictException auto-referência |
| A requer B (novamente) | ConflictException duplicata |
| A requer B de outro jogo | ValidationException jogos diferentes |

## Considerações de performance

- A DFS carrega pré-requisitos do banco iterativamente. Para grafos rasos (poucos níveis de pré-requisito), O(n) queries onde n é o número de nós no grafo.
- Para jogos com muitas vantagens aninhadas, pode ser lento. Solução futura: memoização ou carga eager do grafo.
- Na prática, grafos de pré-requisitos de vantagens são rasos (2-3 níveis), então não é problema.

## File Checklist
- `service/configuracao/VantagemConfiguracaoService.java` (ou novo `VantagemPreRequisitoService.java`)
- `exception/ValidationMessages.java`
- `test/.../VantagemPreRequisitoIntegrationTest.java` — cenários de ciclo

## References
- `service/configuracao/AbstractConfiguracaoService.java`
- `docs/backend/05-services.md`
