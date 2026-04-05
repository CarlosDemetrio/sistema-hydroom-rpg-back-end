# T5 — Corrigir DefaultProvider (BUG-DC-02..09) + Adicionar Defaults Ausentes

> Fase: Backend | Prioridade: P2
> Dependencias: nenhuma (INDEPENDENTE — paralelizavel com T1-T4)
> Bloqueia: nenhuma diretamente (mas todo jogo novo sera criado com dados incorretos ate esta fix)
> Estimativa: 6–8 horas

---

## Objetivo

Corrigir todos os bugs identificados na auditoria `docs/analises/DEFAULT-CONFIG-AUDITORIA.md` no `DefaultGameConfigProviderImpl.java` e `GameConfigInitializerService.java`. Adicionar defaults ausentes (BonusConfig, PontosVantagemConfig, CategoriaVantagem, vantagens canonicas). Garantir coerencia completa com o glossario Klayrah.

---

## Contexto

A auditoria (2026-04-04) revelou que todo jogo novo e criado com:
- Membros do corpo com porcentagens erradas (Cabeca 25% em vez de 75%)
- Indole com 9 alinhamentos D&D em vez de 3 valores Klayrah
- Presenca com escala de intensidade em vez de postura etica
- Genero com 4 valores em vez de 3
- limitadorAtributo hardcoded = 50 (ignora DTO)
- Limitadores completamente desabilitados (codigo comentado)
- SEM BonusConfig (B.B.A, B.B.M, Defesa, etc.)
- SEM PontosVantagemConfig (pontos de vantagem por nivel)
- SEM CategoriaVantagem (categorias de vantagens)
- "Necromance" em vez de "Necromante"
- Membro "Sangue" ausente

Referencia completa: `docs/analises/DEFAULT-CONFIG-AUDITORIA.md`

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `config/DefaultGameConfigProviderImpl.java` | EDITAR (correcoes + adicoes) |
| `config/GameDefaultConfigProvider.java` (interface) | EDITAR — adicionar metodos para novos defaults |
| `service/GameConfigInitializerService.java` | EDITAR — corrigir bugs + chamar novos metodos |
| `dto/defaults/BonusConfigDTO.java` | CRIAR (se nao existir) |
| `dto/defaults/CategoriaVantagemDTO.java` | CRIAR (se nao existir) |
| Teste: `DefaultGameConfigProviderImplTest.java` | CRIAR |

---

## Passos de Implementacao

### Passo 1 — Corrigir BUG-DC-06: Cabeca porcentagem

**Arquivo:** `DefaultGameConfigProviderImpl.java`

```java
// ERRADO:
MembroCorpoConfigDTO.of("Cabeca", new BigDecimal("0.25"), 1)
// CORRETO:
MembroCorpoConfigDTO.of("Cabeca", new BigDecimal("0.75"), 1)
```

---

### Passo 2 — Corrigir BUG-DC-07: Indole simplificada

**Arquivo:** `DefaultGameConfigProviderImpl.java`

Substituir os 9 alinhamentos D&D por:
```java
List.of(
    IndoleConfigDTO.of("Bom",    1),
    IndoleConfigDTO.of("Mau",    2),
    IndoleConfigDTO.of("Neutro", 3)
)
```

---

### Passo 3 — Corrigir BUG-DC-08: Presenca como postura etica

**Arquivo:** `DefaultGameConfigProviderImpl.java`

Substituir escala de intensidade por:
```java
List.of(
    PresencaConfigDTO.of("Bom",     1),
    PresencaConfigDTO.of("Leal",    2),
    PresencaConfigDTO.of("Caotico", 3),
    PresencaConfigDTO.of("Neutro",  4)
)
```

---

### Passo 4 — Corrigir BUG-DC-09: Genero simplificado

**Arquivo:** `DefaultGameConfigProviderImpl.java`

Substituir por:
```java
List.of(
    GeneroConfigDTO.of("Masculino", 1),
    GeneroConfigDTO.of("Feminino",  2),
    GeneroConfigDTO.of("Outro",     3)
)
```

---

### Passo 5 — Corrigir DIV-01: "Necromance" para "Necromante"

**Arquivo:** `DefaultGameConfigProviderImpl.java`

Alterar `"Necromance"` para `"Necromante"` na lista de classes.

---

### Passo 6 — Corrigir DIV-05: Adicionar membro "Sangue"

**Arquivo:** `DefaultGameConfigProviderImpl.java`

Adicionar ao final da lista de membros do corpo:
```java
MembroCorpoConfigDTO.of("Sangue", new BigDecimal("1.00"), 9)  // 100% da vida total
```

---

### Passo 7 — Corrigir BUG-DC-02: limitadorAtributo do DTO

**Arquivo:** `GameConfigInitializerService.java`

```java
// ERRADO:
nivel.setLimitadorAtributo(50); // Default limitador (will be configurable later)
// CORRETO:
nivel.setLimitadorAtributo(dto.getLimitadorAtributo());
```

---

### Passo 8 — Corrigir BUG-DC-03: Descomentar limitadores

**Arquivo:** `GameConfigInitializerService.java`

Descomentar as linhas 98-101:
```java
log.debug("Criando limitadores...");
List<LimitadorConfig> limitadores = createLimitadores(jogo, defaultProvider.getDefaultLimitadores());
```

Verificar se o metodo `createLimitadores()` existe. Se nao existir, implementar seguindo o padrao dos outros metodos `create*()`.

---

### Passo 9 — Adicionar BonusConfig defaults (PRIORIDADE ALTA)

**9.1 — Adicionar na interface `GameDefaultConfigProvider`:**
```java
List<BonusConfigDTO> getDefaultBonus();
```

**9.2 — Criar DTO (se nao existir): `dto/defaults/BonusConfigDTO.java`:**
```java
public record BonusConfigDTO(
    String nome,
    String sigla,
    String formulaBase,
    int ordemExibicao
) {
    public static BonusConfigDTO of(String nome, String sigla, String formulaBase, int ordem) {
        return new BonusConfigDTO(nome, sigla, formulaBase, ordem);
    }
}
```

**9.3 — Implementar no `DefaultGameConfigProviderImpl`:**
```java
@Override
public List<BonusConfigDTO> getDefaultBonus() {
    return List.of(
        BonusConfigDTO.of("B.B.A",      "BBA", "(FOR + AGI) / 3", 1),
        BonusConfigDTO.of("B.B.M",      "BBM", "(SAB + INT) / 3", 2),
        BonusConfigDTO.of("Defesa",     "DEF", "VIG / 5",         3),
        BonusConfigDTO.of("Esquiva",    "ESQ", "AGI / 5",         4),
        BonusConfigDTO.of("Iniciativa", "INI", "INTU / 5",        5),
        BonusConfigDTO.of("Percepcao",  "PER", "INTU / 3",        6),
        BonusConfigDTO.of("Raciocinio", "RAC", "INT / 3",         7),
        BonusConfigDTO.of("Bloqueio",   "BLO", "VIG / 3",         8),
        BonusConfigDTO.of("Reflexo",    "REF", "AGI / 3",         9)
    );
}
```

**9.4 — Implementar `createBonus()` no `GameConfigInitializerService`:**
```java
private List<BonusConfig> createBonus(Jogo jogo, List<BonusConfigDTO> defaults) {
    return defaults.stream().map(dto -> {
        BonusConfig bonus = BonusConfig.builder()
            .jogo(jogo)
            .nome(dto.nome())
            .sigla(dto.sigla())
            .formulaBase(dto.formulaBase())
            .ordemExibicao(dto.ordemExibicao())
            .build();
        return bonusConfigRepository.save(bonus);
    }).toList();
}
```

Chamar no metodo de inicializacao principal, ANTES das classes e racas (para que ClasseBonus possa referenciar BonusConfig).

---

### Passo 10 — Adicionar PontosVantagemConfig defaults

**10.1 — Verificar se DTO ja existe.** Se nao, criar `PontosVantagemConfigDTO.java`:
```java
public record PontosVantagemConfigDTO(
    int nivel,
    int pontos
) {
    public static PontosVantagemConfigDTO of(int nivel, int pontos) {
        return new PontosVantagemConfigDTO(nivel, pontos);
    }
}
```

**10.2 — Adicionar na interface e implementar:**
```java
@Override
public List<PontosVantagemConfigDTO> getDefaultPontosVantagem() {
    return List.of(
        PontosVantagemConfigDTO.of(1,  6),   // Nivel 1: 6 pontos iniciais
        PontosVantagemConfigDTO.of(5,  3),   // A cada 5 niveis: +3
        PontosVantagemConfigDTO.of(10, 10),  // Marco nivel 10: +10
        PontosVantagemConfigDTO.of(15, 3),
        PontosVantagemConfigDTO.of(20, 10),  // Marco nivel 20: +10
        PontosVantagemConfigDTO.of(25, 3),
        PontosVantagemConfigDTO.of(30, 15),  // Marco nivel 30: +15
        PontosVantagemConfigDTO.of(35, 3)
    );
}
```

**10.3 — Implementar `createPontosVantagem()` no initializer.**

---

### Passo 11 — Adicionar CategoriaVantagem defaults

**11.1 — Verificar se DTO ja existe.** Se nao, criar `CategoriaVantagemDTO.java`:
```java
public record CategoriaVantagemDTO(
    String nome,
    String cor,
    int ordemExibicao
) {
    public static CategoriaVantagemDTO of(String nome, String cor, int ordem) {
        return new CategoriaVantagemDTO(nome, cor, ordem);
    }
}
```

**11.2 — Implementar no provider:**
```java
@Override
public List<CategoriaVantagemDTO> getDefaultCategoriasVantagem() {
    return List.of(
        CategoriaVantagemDTO.of("Treinamento Fisico",      "#e74c3c", 1),  // TCO, TCD, TCE
        CategoriaVantagemDTO.of("Treinamento Mental",       "#8e44ad", 2),  // TM, TPM, TL, T.M
        CategoriaVantagemDTO.of("Acao",                     "#e67e22", 3),  // Ataque Adicional, Ataque Sentai
        CategoriaVantagemDTO.of("Reacao",                   "#27ae60", 4),  // Contra-Ataque, Intercepcao
        CategoriaVantagemDTO.of("Vantagem de Atributo",     "#2980b9", 5),  // CFM, DM, DF, DV, SG, IN
        CategoriaVantagemDTO.of("Vantagem Geral",           "#95a5a6", 6),  // Saude de Ferro, Ambidestria
        CategoriaVantagemDTO.of("Vantagem Historica",       "#f39c12", 7),  // Riqueza, Capangas
        CategoriaVantagemDTO.of("Vantagem de Renascimento", "#1abc9c", 8)   // Ultimo Sigilo, Pensamento Bifurcado
    );
}
```

**11.3 — Implementar `createCategoriasVantagem()` no initializer.** Chamar ANTES de `createVantagens()`.

---

### Passo 12 — Adicionar vantagens canonicas

Adicionar as vantagens que faltam ao provider. Alem das ~11 ja existentes, adicionar:

| Sigla | Nome completo | Categoria |
|-------|---------------|-----------|
| TCO | Treinamento de Combate Ofensivo | Treinamento Fisico |
| TCD | Treinamento de Combate Defensivo | Treinamento Fisico |
| TCE | Treinamento de Combate com Escudo | Treinamento Fisico |
| TM | Treinamento Magico | Treinamento Mental |
| TPM | Treinamento de Poder Mental | Treinamento Mental |
| TL | Treinamento de Lideranca | Treinamento Mental |
| T.M | Treinamento de Meditacao | Treinamento Mental |
| CFM | Corpo Fechado (Magico) | Vantagem de Atributo |
| DM | Destreza Mental | Vantagem de Atributo |
| DF | Destreza Fisica | Vantagem de Atributo |
| DV | Determinacao Vital | Vantagem de Atributo |
| SG | Sexto Sentido | Vantagem de Atributo |
| IN | Inspiracao Natural | Vantagem de Atributo |
| — | Saude de Ferro | Vantagem Geral |
| — | Ataque Adicional | Acao |
| — | Ataque Sentai | Acao |
| — | Contra-Ataque | Reacao |
| — | Intercepcao | Reacao |
| — | Riqueza | Vantagem Historica |
| — | Capangas | Vantagem Historica |
| — | Ultimo Sigilo | Vantagem de Renascimento |
| — | Pensamento Bifurcado | Vantagem de Renascimento |

> **IMPORTANTE:** Verificar quais vantagens ja existem no provider para nao duplicar. As 11 existentes incluem Ambidestria e possivelmente algumas das listadas acima.

Cada vantagem deve ser associada a sua CategoriaVantagem correspondente. Usar `Map<String, CategoriaVantagem>` pelo nome para fazer o lookup.

---

### Passo 13 — Adicionar ClassePontosConfig defaults (PA-015-01)

Se confirmado pelo PO, adicionar defaults de pontos por classe. Exemplo:

```java
@Override
public Map<String, List<ClassePontosConfigDTO>> getDefaultClassePontos() {
    return Map.of(
        "Guerreiro", List.of(
            ClassePontosConfigDTO.of(1, 2, 0, 0)  // nivel 1: +2 atributo
        ),
        "Mago", List.of(
            ClassePontosConfigDTO.of(1, 0, 2, 0)  // nivel 1: +2 aptidao
        )
        // ... demais classes
    );
}
```

> **SE PA-015-01 NAO RESPONDIDO:** Implementar o metodo na interface com retorno vazio (`Map.of()`). Mestre pode configurar manualmente apos criar o jogo. Adicionar TODO no codigo.

---

### Passo 14 — Adicionar RacaPontosConfig defaults (PA-015-02)

Se confirmado pelo PO, adicionar defaults de pontos por raca. Exemplo:

```java
@Override
public Map<String, List<RacaPontosConfigDTO>> getDefaultRacaPontos() {
    return Map.of(
        "Elfo", List.of(
            RacaPontosConfigDTO.of(1, 0, 1, 0)  // nivel 1: +1 aptidao
        )
        // ... demais racas
    );
}
```

> **SE PA-015-02 NAO RESPONDIDO:** Implementar com retorno vazio. Mestre configura manualmente.

---

### Passo 15 — Atualizar ordem de inicializacao no GameConfigInitializerService

Seguir a ordem correta (secao 6 da auditoria):

```
1.  AtributoConfig
2.  TipoAptidao
3.  AptidaoConfig
4.  BonusConfig             ← NOVO
5.  NivelConfig
6.  PontosVantagemConfig    ← NOVO
7.  CategoriaVantagem       ← NOVO
8.  ClassePersonagem
9.  ClasseBonus
10. ClasseAptidaoBonus
11. ClassePontosConfig      ← NOVO (apos T1)
12. Raca
13. RacaBonusAtributo
14. RacaPontosConfig        ← NOVO (apos T1)
15. DadoProspeccaoConfig
16. GeneroConfig
17. IndoleConfig
18. PresencaConfig
19. MembroCorpoConfig
20. VantagemConfig (depende de CategoriaVantagem)
21. LimitadorConfig         ← DESCOMENTAR
```

---

## Testes

### Cenario T5-01 — Provider retorna 9 BonusConfig defaults

```
Dado: DefaultGameConfigProviderImpl
Quando: getDefaultBonus() e chamado
Entao: Retorna lista com 9 itens (B.B.A, B.B.M, Defesa, Esquiva, Iniciativa, Percepcao, Raciocinio, Bloqueio, Reflexo)
E: Cada item tem nome, sigla e formulaBase nao vazios
```

### Cenario T5-02 — Provider retorna 8 PontosVantagemConfig defaults

```
Dado: DefaultGameConfigProviderImpl
Quando: getDefaultPontosVantagem() e chamado
Entao: Retorna lista com 8 itens
E: Nivel 1 tem 6 pontos, nivel 10 tem 10 pontos, nivel 30 tem 15 pontos
```

### Cenario T5-03 — Cabeca com 75% da vida

```
Dado: DefaultGameConfigProviderImpl
Quando: getDefaultMembrosCorpo() e chamado
Entao: "Cabeca" tem porcentagemVida == 0.75
```

### Cenario T5-04 — Indole com 3 valores

```
Dado: DefaultGameConfigProviderImpl
Quando: getDefaultIndole() e chamado
Entao: Retorna exatamente 3 itens: Bom, Mau, Neutro
```

### Cenario T5-05 — Presenca com 4 valores eticos

```
Dado: DefaultGameConfigProviderImpl
Quando: getDefaultPresenca() e chamado
Entao: Retorna exatamente 4 itens: Bom, Leal, Caotico, Neutro
```

### Cenario T5-06 — Genero com 3 valores

```
Dado: DefaultGameConfigProviderImpl
Quando: getDefaultGenero() e chamado
Entao: Retorna exatamente 3 itens: Masculino, Feminino, Outro
```

### Cenario T5-07 — "Necromante" (nao "Necromance")

```
Dado: DefaultGameConfigProviderImpl
Quando: getDefaultClasses() e chamado
Entao: Nenhuma classe com nome "Necromance"
E: Existe classe "Necromante"
```

### Cenario T5-08 — Membro "Sangue" existe com 100%

```
Dado: DefaultGameConfigProviderImpl
Quando: getDefaultMembrosCorpo() e chamado
Entao: Existe membro "Sangue" com porcentagemVida == 1.00
```

### Cenario T5-09 — 8 CategoriaVantagem defaults

```
Dado: DefaultGameConfigProviderImpl
Quando: getDefaultCategoriasVantagem() e chamado
Entao: Retorna 8 categorias com nomes e cores corretos
```

### Cenario T5-10 — Inicializar jogo com todos os defaults

```
Dado: GameConfigInitializerService configurado com o provider corrigido
Quando: inicializarJogo(jogo) e chamado
Entao: Jogo criado com BonusConfig (9), PontosVantagemConfig (8), CategoriaVantagem (8)
E: MembroCorpo "Cabeca" com porcentagem 0.75
E: Indole com 3 valores
E: Presenca com 4 valores
E: Genero com 3 valores
E: Classe "Necromante" (nao "Necromance")
E: LimitadorConfig criados (nao comentado)
E: NivelConfig com limitadorAtributo correto (nao hardcoded 50)
```

---

## Criterios de Aceitacao

- [ ] BUG-DC-02 corrigido: limitadorAtributo lido do DTO
- [ ] BUG-DC-03 corrigido: createLimitadores() descomentado e funcional
- [ ] BUG-DC-06 corrigido: Cabeca = 0.75 (75% da vida)
- [ ] BUG-DC-07 corrigido: Indole = Bom, Mau, Neutro (3 valores)
- [ ] BUG-DC-08 corrigido: Presenca = Bom, Leal, Caotico, Neutro (4 valores)
- [ ] BUG-DC-09 corrigido: Genero = Masculino, Feminino, Outro (3 valores)
- [ ] DIV-01 corrigido: "Necromante" (nao "Necromance")
- [ ] DIV-05 corrigido: Membro "Sangue" adicionado com porcentagem 1.00
- [ ] BonusConfig defaults adicionados (9 bonus com formulas)
- [ ] PontosVantagemConfig defaults adicionados (8 entradas por nivel)
- [ ] CategoriaVantagem defaults adicionados (8 categorias com cores)
- [ ] Vantagens canonicas adicionadas ao provider (~22 novas)
- [ ] Ordem de inicializacao correta (BonusConfig antes de ClasseBonus, etc.)
- [ ] Cenarios T5-01 a T5-10 passam como testes
- [ ] `./mvnw test` passa (testes existentes nao quebram)

---

*Produzido por: PM/Scrum Master | 2026-04-04*
*Base: docs/analises/DEFAULT-CONFIG-AUDITORIA.md (rev. 2026-04-04)*
