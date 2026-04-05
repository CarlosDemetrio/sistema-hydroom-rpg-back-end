# T4 — Auto-concessao de Vantagens Pre-definidas na Criacao e Level Up

> Fase: Backend | Prioridade: P1
> Dependencias: T1 (repositories ClasseVantagemPreDefinida, RacaVantagemPreDefinida)
> Bloqueia: nenhuma diretamente
> Estimativa: 4–6 horas

---

## Objetivo

Implementar o mecanismo de auto-concessao de vantagens: quando um personagem e criado (nivel 1) ou sobe de nivel (level up), o sistema verifica `ClasseVantagemPreDefinida` e `RacaVantagemPreDefinida` e cria automaticamente as `FichaVantagem` correspondentes com custo zero.

Adicionar campo `origem` (enum) em `FichaVantagem` para distinguir a fonte da vantagem (JOGADOR, MESTRE, SISTEMA).

---

## Contexto

**Triggers de auto-concessao:**
1. **Criacao da ficha (nivel 1):** `FichaService.criarFicha()` → verificar vantagens pre-definidas para nivel = 1
2. **Level up:** `FichaService.recalcularNivel()` → se houve level up, verificar vantagens para o novo nivel

**Semantica:** Vantagens pre-definidas sao "gratuitas" — o personagem recebe automaticamente sem gastar pontos. Representam habilidades inerentes a classe/raca naquele nivel de experiencia.

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `model/OrigemVantagem.java` | CRIAR (enum) |
| `model/FichaVantagem.java` | EDITAR — adicionar campo `origem` |
| `service/VantagemAutoConcessaoService.java` | CRIAR |
| `service/FichaService.java` | EDITAR — adicionar chamadas de auto-concessao |
| `repository/FichaVantagemRepository.java` | EDITAR — adicionar query para verificar existencia |
| Teste de integracao | CRIAR |

---

## Passos de Implementacao

### Passo 1 — Criar enum OrigemVantagem

```java
/**
 * Origem de uma FichaVantagem — distingue como a vantagem foi adquirida.
 */
public enum OrigemVantagem {
    /** Comprada pelo jogador com pontos de vantagem */
    JOGADOR,
    /** Concedida pelo Mestre (Insolitus ou manual) */
    MESTRE,
    /** Auto-concedida pelo sistema (ClasseVantagemPreDefinida ou RacaVantagemPreDefinida) */
    SISTEMA
}
```

---

### Passo 2 — Adicionar campo `origem` em FichaVantagem

```java
@Enumerated(EnumType.STRING)
@Column(name = "origem", nullable = false, length = 20)
@Builder.Default
private OrigemVantagem origem = OrigemVantagem.JOGADOR;
```

> **Retrocompatibilidade:** O default `JOGADOR` garante que todas as FichaVantagem existentes (criadas antes desta spec) sejam tratadas como compradas pelo jogador. Nao e necessaria migration de dados.

---

### Passo 3 — Adicionar query em FichaVantagemRepository

```java
/**
 * Verifica se a ficha ja possui uma vantagem especifica (independente da origem).
 * Usado para evitar duplicatas na auto-concessao.
 */
boolean existsByFichaIdAndVantagemConfigId(Long fichaId, Long vantagemConfigId);
```

---

### Passo 4 — Criar VantagemAutoConcessaoService

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class VantagemAutoConcessaoService {

    private final ClasseVantagemPreDefinidaRepository classeVantagemRepo;
    private final RacaVantagemPreDefinidaRepository racaVantagemRepo;
    private final FichaVantagemRepository fichaVantagemRepository;

    /**
     * Concede automaticamente as vantagens pre-definidas para um nivel especifico.
     * Verifica tanto ClasseVantagemPreDefinida quanto RacaVantagemPreDefinida.
     * NAO duplica: se a FichaVantagem ja existe, ignora.
     *
     * @param ficha ficha do personagem
     * @param nivel nivel a verificar (ex: 1 para criacao, novo nivel para level up)
     * @return lista de FichaVantagem auto-criadas (pode ser vazia)
     */
    @Transactional
    public List<FichaVantagem> concederVantagensParaNivel(Ficha ficha, int nivel) {
        List<FichaVantagem> concedidas = new ArrayList<>();

        // Fonte 1: ClasseVantagemPreDefinida
        if (ficha.getClasse() != null) {
            List<ClasseVantagemPreDefinida> classeVantagens =
                classeVantagemRepo.findByClasseIdAndNivelWithVantagem(
                    ficha.getClasse().getId(), nivel);

            for (ClasseVantagemPreDefinida cvp : classeVantagens) {
                FichaVantagem criada = concederSeNaoExiste(ficha, cvp.getVantagemConfig());
                if (criada != null) {
                    concedidas.add(criada);
                    log.info("Auto-concedida vantagem '{}' (classe '{}', nivel {}) para ficha {}",
                        cvp.getVantagemConfig().getNome(),
                        ficha.getClasse().getNome(),
                        nivel, ficha.getId());
                }
            }
        }

        // Fonte 2: RacaVantagemPreDefinida
        if (ficha.getRaca() != null) {
            List<RacaVantagemPreDefinida> racaVantagens =
                racaVantagemRepo.findByRacaIdAndNivelWithVantagem(
                    ficha.getRaca().getId(), nivel);

            for (RacaVantagemPreDefinida rvp : racaVantagens) {
                FichaVantagem criada = concederSeNaoExiste(ficha, rvp.getVantagemConfig());
                if (criada != null) {
                    concedidas.add(criada);
                    log.info("Auto-concedida vantagem '{}' (raca '{}', nivel {}) para ficha {}",
                        rvp.getVantagemConfig().getNome(),
                        ficha.getRaca().getNome(),
                        nivel, ficha.getId());
                }
            }
        }

        return concedidas;
    }

    /**
     * Concede a vantagem se a ficha ainda nao possui.
     * Cria FichaVantagem com custoPago=0 e origem=SISTEMA.
     *
     * @return FichaVantagem criada, ou null se ja existia
     */
    private FichaVantagem concederSeNaoExiste(Ficha ficha, VantagemConfig vantagemConfig) {
        // RN-015-03: NAO duplicar
        if (fichaVantagemRepository.existsByFichaIdAndVantagemConfigId(
                ficha.getId(), vantagemConfig.getId())) {
            log.debug("Vantagem '{}' ja existe na ficha {} — ignorando auto-concessao",
                vantagemConfig.getNome(), ficha.getId());
            return null;
        }

        FichaVantagem fichaVantagem = FichaVantagem.builder()
            .ficha(ficha)
            .vantagemConfig(vantagemConfig)
            .nivelAtual(1) // nivel inicial da vantagem = 1
            .custoPago(0)  // RN-015-04: custo zero para vantagens do sistema
            .origem(OrigemVantagem.SISTEMA)
            .build();

        return fichaVantagemRepository.save(fichaVantagem);
    }
}
```

---

### Passo 5 — Integrar no FichaService

**5.1 Na criacao da ficha (nivel 1):**

```java
// Apos salvar a ficha com nivel 1:
Ficha fichaSalva = fichaRepository.save(ficha);

// Auto-conceder vantagens pre-definidas para nivel 1
List<FichaVantagem> autoVantagens =
    vantagemAutoConcessaoService.concederVantagensParaNivel(fichaSalva, 1);

// Se houve auto-concessao, recalcular ficha (vantagens podem ter efeitos)
if (!autoVantagens.isEmpty()) {
    // Disparar recalcular() para aplicar efeitos das vantagens concedidas
}
```

**5.2 No level up (apos recalcularNivel):**

```java
// No metodo que processa concessao de XP:
boolean levelUp = recalcularNivel(ficha);
ficha = fichaRepository.save(ficha);

if (levelUp) {
    // Auto-conceder vantagens pre-definidas para o novo nivel
    List<FichaVantagem> autoVantagens =
        vantagemAutoConcessaoService.concederVantagensParaNivel(ficha, ficha.getNivel());

    // Recalcular ficha completa (nivel mudou + possiveis novas vantagens)
    // ... chamar recalcular() ...
}
```

> **Nota:** Se o personagem pulou de nivel 1 para nivel 3 (ganhou XP suficiente de uma vez), as vantagens de nivel 2 e 3 devem ser concedidas. Implementar loop:

```java
if (levelUp) {
    // Conceder vantagens para cada nivel pulado
    for (int n = nivelAnterior + 1; n <= ficha.getNivel(); n++) {
        vantagemAutoConcessaoService.concederVantagensParaNivel(ficha, n);
    }
}
```

---

## Testes de Integracao

### Cenario T4-01 — Vantagem auto-concedida na criacao (nivel 1)

```
Dado: Jogo com ClasseVantagemPreDefinida: Guerreiro + nivel 1 + vantagem "TCO" (Treinamento de Combate Ofensivo)
Quando: FichaService cria ficha com classe=Guerreiro
Entao: FichaVantagem "TCO" existe na ficha
E: FichaVantagem.custoPago == 0
E: FichaVantagem.origem == SISTEMA
E: FichaVantagem.nivelAtual == 1
```

### Cenario T4-02 — Vantagem auto-concedida no level up

```
Dado: Jogo com ClasseVantagemPreDefinida: Guerreiro + nivel 5 + vantagem "Ataque Adicional"
E: Ficha com classe=Guerreiro, nivel=4
Quando: Mestre concede XP suficiente para nivel 5
Entao: FichaVantagem "Ataque Adicional" auto-criada
E: FichaVantagem.custoPago == 0
E: FichaVantagem.origem == SISTEMA
```

### Cenario T4-03 — Nao duplicar vantagem ja existente

```
Dado: Ficha que ja tem FichaVantagem "TCO" (comprada pelo jogador, custoPago=6)
E: ClasseVantagemPreDefinida: classe da ficha + nivel 1 + vantagem "TCO"
Quando: Auto-concessao para nivel 1 e executada
Entao: FichaVantagem "TCO" permanece a original (nao duplicada)
E: custoPago permanece 6 (nao zerado)
```

### Cenario T4-04 — Vantagens de classe E raca somam

```
Dado: ClasseVantagemPreDefinida: Guerreiro + nivel 1 + vantagem "TCO"
E: RacaVantagemPreDefinida: Humano + nivel 1 + vantagem "Ambidestria"
E: Ficha com classe=Guerreiro, raca=Humano
Quando: Criacao da ficha (nivel 1)
Entao: FichaVantagem "TCO" existe (origem=SISTEMA)
E: FichaVantagem "Ambidestria" existe (origem=SISTEMA)
```

### Cenario T4-05 — Level up com pulo de niveis

```
Dado: ClasseVantagemPreDefinida: Guerreiro + nivel 2 + vantagem "TCD"
E: ClasseVantagemPreDefinida: Guerreiro + nivel 3 + vantagem "TCE"
E: Ficha com classe=Guerreiro, nivel=1
Quando: Mestre concede XP suficiente para pular de nivel 1 para nivel 3
Entao: FichaVantagem "TCD" auto-criada (nivel 2)
E: FichaVantagem "TCE" auto-criada (nivel 3)
```

### Cenario T4-06 — Ficha sem classe e sem raca (nullable)

```
Dado: Ficha sem classe e sem raca definidas (wizard incompleto)
Quando: Auto-concessao para nivel 1 e executada
Entao: Nenhuma FichaVantagem auto-criada (lista vazia, sem erro)
```

---

## Regras de Negocio

- **RN-015-03:** NAO duplicar — se `FichaVantagem` para aquela `vantagemConfig` ja existe na ficha (qualquer origem), ignorar
- **RN-015-04:** Custo zero — `custoPago = 0` para vantagens do sistema
- **RN-015-05:** Campo `origem = SISTEMA` distingue de vantagens compradas ou concedidas pelo Mestre
- Pulo de niveis: conceder vantagens para CADA nivel intermediario
- Classe e raca nullable: ficha pode nao ter classe ou raca

---

## Criterios de Aceitacao

- [ ] Enum `OrigemVantagem` criado com valores JOGADOR, MESTRE, SISTEMA
- [ ] Campo `origem` adicionado em `FichaVantagem` com default JOGADOR
- [ ] `VantagemAutoConcessaoService` criado com metodo `concederVantagensParaNivel()`
- [ ] Trigger 1: criacao de ficha concede vantagens de nivel 1
- [ ] Trigger 2: level up concede vantagens do novo nivel (e niveis intermediarios se pulou)
- [ ] Vantagens ja existentes nao sao duplicadas
- [ ] `FichaVantagem.custoPago = 0` e `origem = SISTEMA` para vantagens auto-concedidas
- [ ] Cenarios T4-01 a T4-06 passam como testes de integracao
- [ ] `./mvnw test` passa (testes existentes nao quebram)

---

*Produzido por: PM/Scrum Master | 2026-04-04*
