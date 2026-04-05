# T5 — FichaCalculationService: DADO_UP

> Fase: Backend | Dependencias: T1 | Bloqueia: T8
> Estimativa: 2–3 horas

---

## Objetivo

Implementar no `FichaCalculationService` o processamento do efeito `DADO_UP`, determinando qual dado de prospeccao o personagem pode utilizar com base no maior nivel de vantagem DADO_UP ativo na ficha.

---

## Contexto

`FichaProspeccao` representa o dado que o personagem rola para tarefas de prospeccao (explorar, rastrear, etc.). O efeito DADO_UP "melhora" o dado disponivel — cada nivel da vantagem avanca uma posicao na sequencia de dados configurada pelo Mestre via `DadoProspeccaoConfig`.

**Estado atual:** `FichaProspeccao` tem FK `dadoProspeccaoConfig` (o dado *base* do personagem) e `quantidade`. Apos T1, tambem tera `dadoDisponivel` (FK nullable) que representa o dado resultante do DADO_UP.

**Semantica:** O dado base (`dadoProspeccaoConfig`) e o dado que o personagem tem naturalmente. O `dadoDisponivel` e o dado apos aplicar DADO_UP. Se o personagem nao tem nenhuma vantagem DADO_UP, `dadoDisponivel` deve ser `null` (ou igual ao dado base, a confirmar com PO).

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `service/FichaCalculationService.java` | Novo metodo `calcularDadoUp()` + case DADO_UP em `aplicarEfeitosVantagens()` |
| `repository/DadoProspeccaoConfigRepository.java` | Verificar se existe query por jogo ordenada por ordemExibicao |
| `service/FichaCalculationService.java` | Adicionar `List<DadoProspeccaoConfig>` ao contexto de calculo (via parametro ou lookup) |

---

## Passos de Implementacao

### Passo 1 — Entender a sequencia de dados

`DadoProspeccaoConfig` tem campo `ordemExibicao` (integer) e `numeroFaces`. A sequencia de dados do jogo e determinada pela ordenacao crescente de `ordemExibicao`.

Exemplo de sequencia configurada pelo Mestre:
```
ordemExibicao=0 → d3  (numeroFaces=3)
ordemExibicao=1 → d4  (numeroFaces=4)
ordemExibicao=2 → d6  (numeroFaces=6)
ordemExibicao=3 → d8  (numeroFaces=8)
ordemExibicao=4 → d10 (numeroFaces=10)
ordemExibicao=5 → d12 (numeroFaces=12)
ordemExibicao=6 → d20 (numeroFaces=20)
```

### Passo 2 — Adicionar lista de dados ao recalcular()

```java
// Adicionar parametro ao recalcular() e aplicarEfeitosVantagens():
List<DadoProspeccaoConfig> dadosOrdenados, // ordenados por ordemExibicao ASC
List<FichaProspeccao> prospeccoes          // lista de prospecções da ficha
```

O chamador (FichaService) deve buscar os dados ja ordenados:
```java
// No FichaService (quando implementado em Spec 006):
List<DadoProspeccaoConfig> dadosOrdenados =
    dadoProspeccaoRepository.findByJogoIdOrderByOrdemExibicaoAsc(jogoId);
```

### Passo 3 — Metodo `calcularDadoUp()`

```java
/**
 * Calcula o dado disponivel resultante de todas as vantagens DADO_UP ativas.
 * Retorna o DadoProspeccaoConfig correspondente a maior posicao alcanada,
 * ou null se nao ha vantagem DADO_UP ativa.
 *
 * Logica: cada nivel de DADO_UP avanca uma posicao na sequencia.
 * Multiplas vantagens DADO_UP: vence a maior posicao (MAX, nao acumulam).
 *
 * @param vantagens vantagens da ficha com efeitos carregados
 * @param dadosOrdenados lista de DadoProspeccaoConfig ordenada por ordemExibicao ASC
 */
public DadoProspeccaoConfig calcularDadoUp(
        List<FichaVantagem> vantagens,
        List<DadoProspeccaoConfig> dadosOrdenados) {

    if (dadosOrdenados == null || dadosOrdenados.isEmpty()) {
        return null;
    }

    int posicaoMaxima = -1;

    for (FichaVantagem fichaVantagem : vantagens) {
        if (fichaVantagem.getVantagemConfig() == null) continue;
        int nivel = fichaVantagem.getNivelAtual() != null ? fichaVantagem.getNivelAtual() : 1;

        for (VantagemEfeito efeito : fichaVantagem.getVantagemConfig().getEfeitos()) {
            if (efeito.getDeletedAt() != null) continue;
            if (efeito.getTipoEfeito() != TipoEfeito.DADO_UP) continue;

            int posicaoCandidata = nivel - 1; // 0-indexed
            if (posicaoCandidata > posicaoMaxima) {
                posicaoMaxima = posicaoCandidata;
            }
        }
    }

    if (posicaoMaxima < 0) {
        return null; // sem DADO_UP ativo
    }

    int indiceFinal = Math.min(posicaoMaxima, dadosOrdenados.size() - 1);
    return dadosOrdenados.get(indiceFinal);
}
```

### Passo 4 — Aplicar resultado em FichaProspeccao

```java
// Dentro de aplicarEfeitosVantagens() ou em metodo separado chamado por recalcular():

DadoProspeccaoConfig dadoResultante = calcularDadoUp(vantagens, dadosOrdenados);

for (FichaProspeccao prospeccao : prospeccoes) {
    prospeccao.setDadoDisponivel(dadoResultante);
}
```

**Nota:** Se a ficha tem multiplos `FichaProspeccao` (um por dado disponivel no jogo), todos recebem o mesmo `dadoDisponivel`. A semantica e: "o melhor dado que voce pode usar agora e este".

---

## Regras de Negocio

- **RN-002 (spec):** O dado resultante e posicional, nao numerico — o calculo e pela posicao na sequencia, nao por soma de valores
- **Multiplas vantagens DADO_UP:** Usa MAX (maior posicao), nao soma — PA-003 confirmado pelo spec
- **Sem DADO_UP:** `dadoDisponivel = null` — UI mostra dado base do personagem
- **Cap na ultima posicao:** Se nivel da vantagem excede o tamanho da sequencia, usa o ultimo dado disponivel
- **Sequencia configuravel:** Usa `DadoProspeccaoConfig.ordemExibicao` — nao ha sequencia hardcoded (PA-005 resolvido)

---

## Exemplo de Calculo

Vantagem "Dado Aprimorado" DADO_UP nivel 3:
- `posicaoCandidata = 3 - 1 = 2` (0-indexed)
- Sequencia: `[d3, d4, d6, d8, d10, d12, d20]`
- `dadosOrdenados[2] = d6`
- Resultado: `FichaProspeccao.dadoDisponivel = DadoProspeccaoConfig(d6)`

Se o personagem tambem tem "Reflexos Aguados" DADO_UP nivel 5:
- `posicaoCandidata = 5 - 1 = 4`
- `posicaoMaxima = MAX(2, 4) = 4`
- `dadosOrdenados[4] = d10`
- Resultado: `FichaProspeccao.dadoDisponivel = DadoProspeccaoConfig(d10)`

---

## Criterios de Aceitacao

- [ ] DADO_UP nivel 1 → posicao 0 → primeiro dado da sequencia
- [ ] DADO_UP nivel 3 → posicao 2 → terceiro dado da sequencia
- [ ] Multiplas vantagens DADO_UP → maior posicao vence
- [ ] Nivel acima do tamanho da sequencia → ultimo dado da sequencia (cap)
- [ ] Sem vantagem DADO_UP ativa → `FichaProspeccao.dadoDisponivel = null`
- [ ] Efeitos com soft delete ignorados
- [ ] `./mvnw test` passa
