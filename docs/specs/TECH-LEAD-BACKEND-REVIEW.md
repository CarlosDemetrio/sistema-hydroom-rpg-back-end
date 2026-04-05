# TECH LEAD BACKEND REVIEW -- Decisoes do PO (BA-GAPS-2026-04-02)

> Analise tecnica das decisoes do PO e mapeamento de impactos no backend.
> Branch: `feature/009-npc-fichas-mestre` | Data: 2026-04-02
> Base de testes atual: 457 testes passando

---

## INDICE

1. [GAP-02: XP -- Seguranca e Concessao](#gap-02-xp)
2. [GAP-03: VantagemEfeito -- Motor de Calculo](#gap-03-vantagem-efeito)
3. [GAP-05: NPC -- Visibilidade por Jogador](#gap-05-npc-visibilidade)
4. [GAP-06: Nivel -- Pontos Disponiveis](#gap-06-nivel-pontos)
5. [GAP-07: Essencia -- Estado Persistido + Reset](#gap-07-essencia)
6. [GAP-08: Prospeccao -- Endpoints Semanticos](#gap-08-prospeccao)
7. [GAP-09: Insolitus -- Flag em VantagemConfig](#gap-09-insolitus)
8. [GAP-10: Roles -- ADMIN + Refatoracao](#gap-10-roles)
9. [GAP-12: Duplicacao -- Multiplas Fichas por Jogador](#gap-12-duplicacao)
10. [Matriz de Dependencias](#dependencias)
11. [Resumo de Impacto nos @PreAuthorize](#impacto-preauthorize)
12. [Riscos e Trade-offs Gerais](#riscos)

---

<a name="gap-02-xp"></a>
## 1. GAP-02: XP -- Seguranca e Concessao

### 1.1 Bug de Seguranca (CRITICO)

**Problema encontrado:** `PUT /fichas/{id}` aceita `xp` e `renascimentos` no `UpdateFichaRequest` com `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`. O service `FichaService.atualizar()` (linhas 284-292) aplica `request.xp()` sem verificar se o usuario e Mestre. Um jogador pode alterar a propria XP arbitrariamente.

**Correcao necessaria:**
- Remover campos `xp` e `renascimentos` do `UpdateFichaRequest`
- OU: No `FichaService.atualizar()`, ignorar `request.xp()` e `request.renascimentos()` quando o usuario nao for Mestre

**Recomendacao:** Remover os campos do DTO. Se um campo nao pode ser usado por jogadores, nao deveria estar no contrato publico. A concessao de XP deve ter endpoint dedicado com semantica clara.

**Complexidade:** P1 (pequeno)
**Prioridade:** URGENTE -- vulnerabilidade de seguranca ativa

### 1.2 Endpoint Dedicado de Concessao de XP

**Novo endpoint:**

| Metodo | Path | Role | Request Body | Response |
|--------|------|------|--------------|----------|
| `POST` | `/api/v1/fichas/{id}/xp` | MESTRE | `ConcederXpRequest(Long quantidade)` | `FichaResponse` |

**Logica no service:**
1. Verificar que o usuario e Mestre do jogo da ficha
2. Somar `quantidade` ao `xp` atual da ficha (`ficha.xp += request.quantidade()`)
3. Auto-nivel: buscar `NivelConfig` com `findNivelPorExperiencia(jogoId, novaXp)` e atualizar `ficha.nivel`
4. Recalcular valores derivados (vida, essencia, ameaca dependem do nivel)
5. Retornar ficha atualizada

**Mudancas em entidades:** Nenhuma. `Ficha.xp` e `Ficha.nivel` ja existem.

**Mudancas em DTOs:**
- Novo: `ConcederXpRequest` (record com `@NotNull @Min(1) Long quantidade`)
- Remover `xp` e `renascimentos` do `UpdateFichaRequest`

**Complexidade:** P1 (pequeno)

### 1.3 Concessao de XP em Lote (Desejavel, nao bloqueador)

| Metodo | Path | Role | Request Body | Response |
|--------|------|------|--------------|----------|
| `POST` | `/api/v1/jogos/{jogoId}/xp/lote` | MESTRE | `ConcederXpLoteRequest(Long quantidade, List<Long> fichaIds)` | `List<FichaResponse>` |

**Logica:** Reutilizar a mesma logica do endpoint individual, iterando sobre as fichas. Se `fichaIds` for null/vazio, aplicar a todas as fichas do jogo (NPCs + jogadores). Transacao atomica.

**Complexidade:** P2 (medio) -- tratamento de erros em lote, decisao sobre atomicidade vs parcial

---

<a name="gap-03-vantagem-efeito"></a>
## 2. GAP-03: VantagemEfeito -- Motor de Calculo (PRIORIDADE)

### 2.1 Estado Atual

`FichaCalculationService` calcula:
- Atributos: `base + nivel + outros` (FichaAtributo.recalcularTotal)
- Bonus: `formulaBase` via exp4j + componentes manuais
- Vida: `vigorTotal + nivel + vt + renascimentos + outros`
- Essencia: `floor((VIG + SAB) / 2) + nivel + renascimentos + vantagens + outros`
- Ameaca: `nivel + itens + titulos + renascimentos + outros`

**Problema:** Os campos `vt` (FichaVida), `vantagens` (FichaEssencia/FichaBonus) sao preenchidos manualmente e NAO sao alimentados pelos `VantagemEfeito`. O CRUD de VantagemEfeito existe, mas o motor de calculo ignora completamente os efeitos. Resultado: fichas com vantagens de bonus tem valores matematicamente errados.

### 2.2 Mudancas Necessarias no FichaCalculationService

Novo metodo: `aplicarEfeitosVantagens(List<FichaVantagem> fichaVantagens, ...)` que para cada FichaVantagem ativa:

1. Carregar `vantagemConfig.efeitos` (JOIN FETCH para evitar N+1)
2. Para cada `VantagemEfeito`, calcular o valor do efeito com base no `nivelAtual` da FichaVantagem:
   - `valorTotal = (valorFixo ?? 0) + (valorPorNivel ?? 0) * nivelAtual`
3. Distribuir o valor no alvo correto por `TipoEfeito`:

| TipoEfeito | Alvo | Campo Impactado |
|---|---|---|
| `BONUS_ATRIBUTO` | `FichaAtributo` (via `atributoAlvo`) | Novo campo `vantagens` em FichaAtributo, ou somar em `outros` |
| `BONUS_APTIDAO` | `FichaAptidao` (via `aptidaoAlvo`) | Campo existente? Precisa de campo `vantagens` |
| `BONUS_DERIVADO` | `FichaBonus` (via `bonusAlvo`) | `fichaBonus.vantagens` (campo ja existe) |
| `BONUS_VIDA` | `FichaVida` | `fichaVida.vt` (campo ja existe) |
| `BONUS_VIDA_MEMBRO` | `FichaVidaMembro` (via `membroAlvo`) | Necessita novo campo |
| `BONUS_ESSENCIA` | `FichaEssencia` | `fichaEssencia.vantagens` (campo ja existe) |
| `DADO_UP` | Conceitual | Nao afeta calculo numerico -- apenas define qual dado usar |
| `FORMULA_CUSTOMIZADA` | Variavel | Avaliar formula via `FormulaEvaluatorService` com `nivel_vantagem` + atributos |

### 2.3 Mudancas em Entidades

**FichaAtributo** -- verificar se campo `outros` absorve bonus de vantagem, ou criar campo dedicado `vantagens`:
- Recomendacao: criar campo `vantagens` (Integer, default 0) para separar origens dos bonus. O total passa a ser `base + nivel + vantagens + outros`.

**FichaAptidao** -- mesmo cenario: verificar campos existentes. Atualmente: `base`, `sorte`, `classe`, `total`.
- Necessita novo campo: `vantagens` (Integer, default 0). Total = `base + sorte + classe + vantagens`.

**FichaVidaMembro** -- atualmente: `vida` (calculado), `danoRecebido`.
- Necessita: `bonusVantagem` (Integer, default 0) para somar ao calculo de vida do membro.

**FichaBonus** -- ja possui campo `vantagens`. OK.
**FichaVida** -- ja possui campo `vt` (sera alimentado por BONUS_VIDA). OK.
**FichaEssencia** -- ja possui campo `vantagens`. OK.

### 2.4 DADO_UP -- Modelagem

Decisao do PO: "o dado e para setar apenas o dado que sera jogado". Nao tem valor calculado. O efeito DADO_UP de uma vantagem altera qual dado de prospeccao o jogador usa (ex: de d6 para d8). Isso nao e um calculo numerico; e uma mudanca de configuracao.

**Opcoes:**
- A: Ignorar DADO_UP no motor de calculo (apenas informativo no frontend)
- B: Criar campo `dadoUpgrade` em `FichaProspeccao` que e incrementado pelos efeitos DADO_UP

**Recomendacao:** Opcao A para MVP. DADO_UP e apresentacional e nao numerico. O frontend mostra "d8" no lugar de "d6". Nao bloqueia nenhum calculo.

### 2.5 FORMULA_CUSTOMIZADA

Variaveis disponiveis para formulas customizadas:
- Todas as abreviacoes de atributos do jogo (ex: `FOR`, `AGI`, `VIG`, `SAB`)
- `nivel_vantagem` -- nivel atual da FichaVantagem
- `nivel` -- nivel do personagem
- `base`, `total` -- se fizerem sentido no contexto

O `FormulaEvaluatorService` ja suporta variaveis arbitrarias. Basta injetar o mapa correto ao avaliar.

**Mudancas no FormulaEvaluatorService:** Nenhuma estrutural. Novo metodo de conveniencia `calcularEfeitoCustomizado(String formula, Map<String, Double> variaveis)` ou reutilizar `calcularDerivado`.

### 2.6 Fluxo de Recalculo Atualizado

```
recalcular(ficha):
  1. recalcularAtributos(atributos)         -- base + nivel + outros
  2. aplicarEfeitosVantagens(...)            -- NOVO: alimenta campos de vantagem
  3. recalcularAtributosTotais(atributos)    -- base + nivel + vantagens + outros
  4. recalcularBonus(atributos, bonus)       -- formulas + vantagens
  5. recalcularEstado(vida, essencia, ameaca) -- inclui vantagens
```

A ordem importa: efeitos de vantagem que alteram atributos devem ser aplicados ANTES do calculo de bonus que dependem de atributos.

**Complexidade:** P3 (grande)
**Estimativa:** ~20-30 arquivos impactados (entities, repos, services, testes)
**Risco:** Regressao nos calculos existentes. Necessita cobertura de testes extensa.

---

<a name="gap-05-npc-visibilidade"></a>
## 3. GAP-05: NPC -- Visibilidade por Jogador

### 3.1 Nova Entidade: FichaVisibilidade

```java
@Entity
@Table(name = "ficha_visibilidade", uniqueConstraints = {
    @UniqueConstraint(name = "uk_ficha_visibilidade", columnNames = {"ficha_id", "jogador_id"})
})
public class FichaVisibilidade extends BaseEntity {
    Long id;
    Ficha ficha;       // FK -> fichas (ManyToOne)
    Long jogadorId;    // ID do jogador que pode ver esta ficha
}
```

**Regra:** Apenas fichas com `isNpc=true` podem ter registros de visibilidade. Validar no service.

### 3.2 Novos Endpoints

| Metodo | Path | Role | Body | Response | Descricao |
|--------|------|------|------|----------|-----------|
| `POST` | `/api/v1/fichas/{fichaId}/visibilidade/{jogadorId}` | MESTRE | -- | 201 Created | Revelar NPC para jogador |
| `DELETE` | `/api/v1/fichas/{fichaId}/visibilidade/{jogadorId}` | MESTRE | -- | 204 No Content | Ocultar NPC de jogador |
| `GET` | `/api/v1/fichas/{fichaId}/visibilidade` | MESTRE | -- | `List<FichaVisibilidadeResponse>` | Listar quem pode ver |

### 3.3 Mudancas em Endpoints Existentes

**`verificarAcessoLeitura(Ficha ficha)` em FichaService, FichaResumoService, FichaVidaService:**
Atualmente, NPCs so sao visiveis para o Mestre (`if (ficha.isNpc()) throw ForbiddenException`).

**Nova logica:**
```
if (ficha.isNpc()) {
    if (!isMestre) {
        // Verificar se jogador tem visibilidade concedida
        boolean temVisibilidade = fichaVisibilidadeRepository
            .existsByFichaIdAndJogadorId(ficha.getId(), usuarioAtual.getId());
        if (!temVisibilidade) {
            throw new ForbiddenException("Acesso negado: NPC nao visivel para voce.");
        }
    }
}
```

**Impacto:** 3 services com `verificarAcessoLeitura` duplicado (FichaService, FichaResumoService, FichaVidaService). Oportunidade de refatorar para um service compartilhado `FichaAccessService`.

### 3.4 Mudanca na Listagem de Fichas do Jogador

O endpoint `GET /api/v1/jogos/{jogoId}/fichas` para jogadores retorna apenas `findByJogoIdAndJogadorId`. Precisa incluir NPCs com visibilidade concedida:

```sql
SELECT f FROM Ficha f
WHERE f.jogo.id = :jogoId
AND (f.jogadorId = :jogadorId
     OR (f.isNpc = true AND EXISTS (
         SELECT 1 FROM FichaVisibilidade fv
         WHERE fv.ficha.id = f.id AND fv.jogadorId = :jogadorId
     )))
```

**Complexidade:** P2 (medio)

---

<a name="gap-06-nivel-pontos"></a>
## 4. GAP-06: Nivel -- Pontos Disponiveis

### 4.1 Calculo de Pontos Disponiveis

**Algoritmo:**
```
pontosAtributoDisponiveis = somaGanhos - somaGastos

somaGanhos = SUM(nivelConfig.pontosAtributo) para todos niveis de 1 ate ficha.nivel
somaGastos = SUM(fichaAtributo.base) para todos atributos da ficha

pontosAptidaoDisponiveis = SUM(nivelConfig.pontosAptidao[1..nivel]) - SUM(fichaAptidao.base)
pontosVantagemDisponiveis = SUM(pontosVantagemConfig.pontosGanhos[1..nivel]) - SUM(fichaVantagem.custoPago)
```

### 4.2 Mudancas no FichaResumoResponse

Adicionar 3 novos campos ao record:

```java
public record FichaResumoResponse(
    // ... campos existentes ...
    int vidaAtual,          // NOVO -- ja existe no model, faltava no DTO
    int essenciaAtual,      // NOVO -- ja existe no model, faltava no DTO
    int pontosAtributoDisponiveis,   // NOVO
    int pontosAptidaoDisponiveis,    // NOVO
    int pontosVantagemDisponiveis    // NOVO
) {}
```

### 4.3 Mudancas no FichaResumoService

Novo metodo `calcularPontosDisponiveis(Ficha ficha, Long jogoId)` que:
1. Busca todos `NivelConfig` do jogo com nivel <= ficha.nivel
2. Soma `pontosAtributo` e `pontosAptidao` desses niveis
3. Busca todos `PontosVantagemConfig` do jogo com nivel <= ficha.nivel
4. Soma `pontosGanhos`
5. Busca soma de `base` de todos `FichaAtributo` da ficha
6. Busca soma de `base` de todas `FichaAptidao` da ficha
7. Busca soma de `custoPago` de todas `FichaVantagem` da ficha
8. Retorna: ganhos - gastos para cada tipo

**Novos metodos nos repositories:**
- `NivelConfigRepository.findByJogoIdAndNivelLessThanEqual(Long jogoId, Integer nivel)`
- `PontosVantagemConfigRepository.findByJogoIdAndNivelLessThanEqual(Long jogoId, Integer nivel)`

**Complexidade:** P2 (medio) -- calculo puro, sem mudancas em entidades

---

<a name="gap-07-essencia"></a>
## 5. GAP-07: Essencia -- Estado Persistido + Reset

### 5.1 Estado Atual

`FichaEssencia.essenciaAtual` ja existe no model (campo adicionado recentemente). O endpoint `PUT /fichas/{id}/vida` ja atualiza `essenciaAtual`. Portanto, a decisao do PO ("endpoint simetrico ao vida") ja esta parcialmente implementada.

**Faltante:** Endpoint dedicado `PUT /fichas/{id}/essencia` que atualiza APENAS a essencia atual (sem mexer na vida).

### 5.2 Novo Endpoint: Atualizar Essencia

| Metodo | Path | Role | Body | Response |
|--------|------|------|------|----------|
| `PUT` | `/api/v1/fichas/{id}/essencia` | MESTRE, JOGADOR | `AtualizarEssenciaRequest(Integer essenciaAtual)` | `FichaResumoResponse` |

**Complexidade:** P1 (pequeno) -- reutiliza padrao existente do `PUT /fichas/{id}/vida`

### 5.3 Novo Endpoint: Reset Completo

Decisao do PO: "o mestre pode resetar todos os estados, incluindo vida, essencia e etc."

| Metodo | Path | Role | Body | Response | Descricao |
|--------|------|------|------|----------|-----------|
| `POST` | `/api/v1/fichas/{id}/resetar` | MESTRE | -- | `FichaResumoResponse` | Reseta vida, essencia e prospeccao |

**Logica:**
1. Verificar que usuario e Mestre
2. `fichaVida.vidaAtual = fichaVida.vidaTotal`
3. `fichaEssencia.essenciaAtual = fichaEssencia.total`
4. Para cada `FichaVidaMembro`: `danoRecebido = 0`
5. Para cada `FichaProspeccao`: resetar para valor padrao (0? ou valor inicial?)
6. Retornar resumo atualizado

**Pergunta em aberto:** Prospeccao reseta para 0 ou para o valor configurado inicialmente? Assumir 0 por padrao (Mestre pode conceder novamente via endpoint de conceder).

**Complexidade:** P1 (pequeno)

---

<a name="gap-08-prospeccao"></a>
## 6. GAP-08: Prospeccao -- Endpoints Semanticos

### 6.1 Estado Atual

Endpoint unico `PUT /fichas/{id}/prospeccao` aceita `AtualizarProspeccaoRequest(dadoProspeccaoConfigId, quantidade)` com `hasAnyRole('MESTRE', 'JOGADOR')`. Permite que ambos settem qualquer quantidade. Nao ha semantica de "usar" vs "conceder".

### 6.2 Novos Endpoints (Substituir o Existente)

| Metodo | Path | Role | Body | Response | Descricao |
|--------|------|------|------|----------|-----------|
| `POST` | `/api/v1/fichas/{id}/prospeccao/usar` | MESTRE, JOGADOR | `UsarProspeccaoRequest(Long dadoProspeccaoConfigId)` | `FichaResumoResponse` | Decrementa 1 dado |
| `POST` | `/api/v1/fichas/{id}/prospeccao/conceder` | MESTRE | `ConcederProspeccaoRequest(Long dadoProspeccaoConfigId, Integer quantidade)` | `FichaResumoResponse` | Incrementa N dados |
| `POST` | `/api/v1/fichas/{id}/prospeccao/reverter` | MESTRE | `ReverterProspeccaoRequest(Long dadoProspeccaoConfigId)` | `FichaResumoResponse` | Incrementa 1 (reverte uso) |

**Logica do "usar":**
1. Verificar acesso de escrita
2. Buscar `FichaProspeccao` pelo fichaId + dadoProspeccaoConfigId
3. Se `quantidade <= 0`, lancar `ValidationException("Sem dados de prospeccao disponiveis")`
4. Decrementar: `quantidade -= 1`
5. Salvar e retornar resumo

**Logica do "conceder":**
1. Verificar que e Mestre
2. Incrementar `quantidade += request.quantidade()`
3. Salvar e retornar resumo

**Logica do "reverter":**
1. Verificar que e Mestre
2. Incrementar `quantidade += 1`
3. Salvar e retornar resumo

**Endpoint antigo** `PUT /fichas/{id}/prospeccao`: deprecar ou remover. Recomendacao: remover. O endpoint generico que seta quantidade absoluta e perigoso (jogador pode se dar dados infinitos).

**Complexidade:** P2 (medio) -- 3 novos endpoints, remocao de 1

---

<a name="gap-09-insolitus"></a>
## 7. GAP-09: Insolitus -- Flag em VantagemConfig

### 7.1 Decisao do PO

"Insolitus entra na mesma categoria das vantagens, pode permitir praticamente qualquer coisa."

### 7.2 Abordagem Recomendada: Flag em VantagemConfig

**Por que flag e melhor que entidade separada:**
- Insolitus compartilha 100% da mecanica de VantagemConfig (nivelMaximo, formulaCusto, efeitos, pre-requisitos)
- Criar entidade separada duplicaria toda a infraestrutura (CRUD, efeitos, motor de calculo)
- Uma flag permite reutilizar TODA a logica existente
- O Mestre ja gerencia vantagens; Insolitus e apenas uma vantagem com concessao diferenciada

**Mudanca na entidade VantagemConfig:**
```java
@Builder.Default
@Column(name = "is_insolitus", nullable = false)
private boolean isInsolitus = false;
```

**Impacto nos endpoints existentes:**
- `GET /api/v1/jogos/{jogoId}/vantagens-config` -- adicionar filtro opcional `?isInsolitus=true/false`
- `POST /fichas/{id}/vantagens` (comprar vantagem) -- se VantagemConfig.isInsolitus=true, apenas MESTRE pode conceder
- Resposta VantagemConfigResponse: adicionar campo `isInsolitus`

**Novo endpoint de concessao:**

| Metodo | Path | Role | Body | Response | Descricao |
|--------|------|------|------|----------|-----------|
| `POST` | `/api/v1/fichas/{id}/insolitus` | MESTRE | `ConcederInsolitusRequest(Long vantagemConfigId)` | `FichaVantagemResponse` | Mestre concede insolitus |

Ou reutilizar `POST /fichas/{id}/vantagens` com validacao: se `isInsolitus=true`, exigir MESTRE. Mais limpo que endpoint separado.

**Recomendacao:** Reutilizar o endpoint de compra de vantagem com validacao de role. Nao precisa de endpoint separado.

**Complexidade:** P1 (pequeno) -- 1 campo novo + validacao no service

---

<a name="gap-10-roles"></a>
## 8. GAP-10: Roles -- ADMIN + Refatoracao

### 8.1 Estado Atual do Sistema de Roles

**Duas camadas de role coexistem:**

1. **Role Global (Usuario.role):** `JOGADOR` ou `MESTRE`. Definida no campo `role` da tabela `usuarios`. Usada pelo Spring Security via `CustomOAuth2UserService` que injeta `ROLE_JOGADOR` ou `ROLE_MESTRE` nas authorities. Todos os `@PreAuthorize` usam esta role.

2. **Role por Jogo (JogoParticipante.role):** `MESTRE` ou `JOGADOR` (enum `RoleJogo`). Define o papel do usuario em um jogo especifico. Usada nos services para verificacao de acesso granular (ex: `jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole`).

**Problema:** As duas camadas estao desacopladas e inconsistentes. Um usuario com `role=JOGADOR` global pode ser `MESTRE` em um jogo via `JogoParticipante`. Mas os `@PreAuthorize` no controller verificam a role global, nao a role por jogo.

### 8.2 Decisao do PO

Adicionar role `ADMIN` com acesso total. Tornar Mestre/Jogador por jogo (nao global).

### 8.3 Mudancas Necessarias

**Entidade Usuario:**
```java
// Antes:
private String role = "JOGADOR"; // JOGADOR ou MESTRE

// Depois:
private String role = "JOGADOR"; // ADMIN, JOGADOR (padrão)
// MESTRE não e mais role global. E role por jogo.
```

**Enum RoleUsuario (novo):**
```java
public enum RoleUsuario {
    ADMIN,    // acesso total (superuser)
    JOGADOR   // acesso padrao
}
```

**Nota:** `MESTRE` SAI da role global e fica apenas no `JogoParticipante.role`.

**CustomOAuth2UserService:**
- Injetar `ROLE_ADMIN` se `usuario.role == "ADMIN"`
- Injetar `ROLE_JOGADOR` para todos os outros
- NAO injetar mais `ROLE_MESTRE` global

**Impacto nos @PreAuthorize -- MASSIVO:**

Atualmente temos ~50+ usos de `@PreAuthorize` nos controllers. Todos precisam ser revisados:

| Padrao Atual | Padrao Novo |
|---|---|
| `hasRole('MESTRE')` | `hasAnyRole('ADMIN', 'MESTRE', 'JOGADOR')` + verificacao no service |
| `hasAnyRole('MESTRE', 'JOGADOR')` | `hasAnyRole('ADMIN', 'MESTRE', 'JOGADOR')` + verificacao no service |

**Problema critico:** Os `@PreAuthorize` atuais usam a role GLOBAL, mas a logica real de permissao esta nos services (que verificam `JogoParticipante.role`). Se tirarmos MESTRE da role global, todos os endpoints de configuracao que usam `@PreAuthorize("hasRole('MESTRE')")` vao quebrar para Mestres que nao sao ADMIN.

**Solucao proposta:**

1. Mudar TODOS os `@PreAuthorize` para `@PreAuthorize("isAuthenticated()")` ou `@PreAuthorize("hasAnyRole('ADMIN', 'JOGADOR')")`
2. A verificacao de Mestre vs Jogador fica NO SERVICE, onde ja e feita via `JogoParticipante`
3. ADMIN bypassa todas as verificacoes de role por jogo

**Alternativa mais segura:**
- Manter `@PreAuthorize` para filtrar endpoints publicos vs autenticados
- Criar um `@PreAuthorize("hasRole('ADMIN')")` para endpoints administrativos globais (gerenciamento de usuarios, etc.)
- Para endpoints de jogo: `@PreAuthorize("isAuthenticated()")` + verificacao granular no service

### 8.4 Atribuicao da Role ADMIN

Decisao do PO nao especificou. Opcoes:
- **A: Apenas via banco** -- UPDATE direto na tabela `usuarios`. Simples, seguro, adequado para MVP.
- **B: Endpoint de admin** -- `PUT /api/v1/admin/usuarios/{id}/role` com `hasRole('ADMIN')`. Mais usavel.
- **C: Primeiro usuario vira ADMIN** -- Inseguro em producao.

**Recomendacao:** Opcao A para MVP, B como segunda iteracao. Exige seed data ou migracao para atribuir o primeiro ADMIN.

### 8.5 Novos Endpoints Administrativos (Futuros)

| Metodo | Path | Role | Descricao |
|--------|------|------|-----------|
| `GET` | `/api/v1/admin/usuarios` | ADMIN | Listar todos os usuarios |
| `PUT` | `/api/v1/admin/usuarios/{id}/role` | ADMIN | Alterar role de usuario |

**Complexidade:** P3 (grande) -- impacta TODOS os controllers e services. Requer migracao de dados.
**Risco:** ALTO. Mudanca transversal. Se feita errada, todo o sistema de permissoes quebra.
**Recomendacao:** Implementar por ultimo, apos todos os outros GAPs. Fazer em branch dedicada com cobertura de testes extensiva.

---

<a name="gap-12-duplicacao"></a>
## 9. GAP-12: Duplicacao -- Multiplas Fichas por Jogador

### 9.1 Verificacao de Constraints

Nao existe `UniqueConstraint` em `Ficha` para `(jogo_id, jogador_id)`. O campo `jogador_id` nao tem restricao de unicidade. Portanto:

**Nenhuma constraint impede multiplas fichas por jogador no mesmo jogo.**

A modelagem atual ja suporta este cenario. O `FichaService.criar()` permite criar fichas para o mesmo jogador sem restricao.

### 9.2 Verificacao do Endpoint de Duplicacao

`FichaService.duplicar()` ja funciona corretamente para este cenario:
- Cria uma copia com novo nome
- `manterJogador=true` mantem o mesmo jogadorId
- Nao ha validacao que impeca duplicacao para o mesmo jogador

### 9.3 Mudancas Necessarias

**Nenhuma.** O sistema ja suporta multiplas fichas por jogador. Apenas documentar o comportamento.

**Complexidade:** P0 (nenhuma mudanca de codigo)

---

<a name="dependencias"></a>
## 10. Matriz de Dependencias

```
GAP-02 (XP bug)       ─── independente, URGENTE
GAP-12 (Duplicacao)    ─── independente, ja funciona

GAP-09 (Insolitus)     ─── depende de: nada (1 campo novo)
                       └── bloqueado por: nada

GAP-07 (Essencia)      ─── depende de: nada
                       └── bloqueado por: nada

GAP-08 (Prospeccao)    ─── depende de: nada
                       └── bloqueado por: nada

GAP-06 (Pontos)        ─── depende de: nada
                       └── bloqueado por: nada (mas a logica de pontos de vantagem 
                           sera mais precisa DEPOIS do GAP-03)

GAP-05 (NPC Visib.)    ─── depende de: nada
                       └── bloqueado por: nada

GAP-03 (VantagemEfeito)─── depende de: GAP-09 (para saber se Insolitus e flag)
                       └── maior esforco, impacto transversal

GAP-10 (Roles)         ─── depende de: nada tecnico
                       └── bloqueado por: deveria ser o ULTIMO
                           (impacta todos os outros controllers)
```

### Ordem Recomendada de Implementacao

| Fase | GAP | Justificativa |
|------|-----|---------------|
| 1 | **GAP-02 (bug XP)** | Vulnerabilidade de seguranca -- corrigir IMEDIATAMENTE |
| 2 | **GAP-09 (Insolitus)** | 1 campo novo, desbloqueie GAP-03 |
| 2 | **GAP-07 (Essencia)** | Pequeno, independente |
| 2 | **GAP-06 (Pontos)** | Medio, independente |
| 3 | **GAP-08 (Prospeccao)** | Medio, endpoints semanticos |
| 3 | **GAP-05 (NPC Visib.)** | Medio, nova entidade |
| 4 | **GAP-03 (VantagemEfeito)** | Grande, motor de calculo, depende de GAP-09 |
| 5 | **GAP-10 (Roles)** | Grande, transversal, implementar POR ULTIMO |

---

<a name="impacto-preauthorize"></a>
## 11. Resumo de Impacto nos @PreAuthorize

### Mudancas Imediatas (sem GAP-10)

| Endpoint | Antes | Depois | Motivo |
|----------|-------|--------|--------|
| `PUT /fichas/{id}` | MESTRE, JOGADOR | MESTRE, JOGADOR | Remover xp/renascimentos do DTO |
| `PUT /fichas/{id}/prospeccao` | MESTRE, JOGADOR | **REMOVER** | Substituido por 3 endpoints semanticos |
| `POST /fichas/{id}/prospeccao/usar` | -- | MESTRE, JOGADOR | **NOVO** |
| `POST /fichas/{id}/prospeccao/conceder` | -- | MESTRE | **NOVO** |
| `POST /fichas/{id}/prospeccao/reverter` | -- | MESTRE | **NOVO** |
| `POST /fichas/{id}/xp` | -- | MESTRE | **NOVO** |
| `POST /jogos/{id}/xp/lote` | -- | MESTRE | **NOVO** (desejavel) |
| `PUT /fichas/{id}/essencia` | -- | MESTRE, JOGADOR | **NOVO** |
| `POST /fichas/{id}/resetar` | -- | MESTRE | **NOVO** |
| `POST /fichas/{fichaId}/visibilidade/{jogadorId}` | -- | MESTRE | **NOVO** |
| `DELETE /fichas/{fichaId}/visibilidade/{jogadorId}` | -- | MESTRE | **NOVO** |
| `GET /fichas/{fichaId}/visibilidade` | -- | MESTRE | **NOVO** |

### Mudancas Futuras (com GAP-10 -- Roles)

TODOS os controllers que usam `hasRole('MESTRE')` ou `hasAnyRole('MESTRE', 'JOGADOR')` precisarao ser revisados para incluir `ADMIN` e/ou migrar para `isAuthenticated()` + verificacao no service.

Contagem estimada: ~50+ anotacoes @PreAuthorize em ~15 controllers.

---

<a name="riscos"></a>
## 12. Riscos e Trade-offs Gerais

### Riscos Altos

| # | Risco | Mitigacao |
|---|-------|----------|
| R1 | **GAP-02 e vulnerabilidade ativa** -- jogador pode alterar propria XP | Corrigir na proxima release. P1, nao precisa esperar nada. |
| R2 | **GAP-03 altera motor de calculo** -- regressao em todos os valores derivados | Cobertura de testes extensa antes e depois. Testes de snapshot dos calculos. |
| R3 | **GAP-10 e transversal** -- alterar roles quebra todo o sistema de permissoes | Implementar por ultimo. Feature flag se possivel. Branch dedicada. |
| R4 | **Verificacao de acesso duplicada** em 3+ services | Refatorar para `FichaAccessService` compartilhado antes de adicionar visibilidade NPC |

### Trade-offs

| Decisao | Pro | Contra |
|---------|-----|--------|
| Insolitus como flag (vs entidade) | Reutiliza 100% da infra de VantagemConfig | Mistura conceitos no mesmo CRUD |
| DADO_UP informativo (vs calculado) | Simples, nao bloqueia MVP | Frontend precisa interpretar o efeito |
| Remover PUT /prospeccao generico | Semantica clara, seguro | Breaking change para frontend |
| ADMIN apenas via banco | Simples, seguro | Ruim para UX do administrador |

### Impacto no Frontend

Todos os GAPs tem impacto no frontend. Os mais criticos:
- GAP-02: Frontend ja envia `xp` no UpdateFichaRequest -- precisa parar
- GAP-03: FichaResumo vai ter valores diferentes (corretos)
- GAP-05: Jogador passa a ver NPCs com visibilidade concedida -- listar fichas muda
- GAP-08: 3 endpoints novos substituem 1 -- breaking change
- GAP-10: Se roles mudam, o frontend precisa se adaptar (guarda de rotas, menus)

---

## Resumo Quantitativo

| Tipo | Quantidade |
|------|-----------|
| Novos endpoints | 11 |
| Endpoints removidos/deprecados | 1 |
| Endpoints alterados | 3 |
| Novas entidades | 1 (FichaVisibilidade) |
| Entidades alteradas | 4 (VantagemConfig, FichaAtributo, FichaAptidao, FichaVidaMembro) |
| Novos DTOs | ~8 (requests + responses) |
| DTOs alterados | 3 (UpdateFichaRequest, FichaResumoResponse, VantagemConfigResponse) |
| Novos services/metodos | ~6 |
| Impacto em testes | 50+ novos testes estimados |
| Migracoes Flyway (prod) | 3-4 (novas colunas, nova tabela) |

---

*Produzido por: Tech Lead Backend | 2026-04-02*
*Proxima acao: Alinhar prioridades com PO e iniciar GAP-02 (bug de seguranca)*

---

## Revisao 2 — 2026-04-03

> Revisao cruzada: specs 005, 006, 007, 010 vs. codigo atual (branch `feature/009-npc-fichas-mestre`, 457 testes).
> Objetivo: identificar tudo que as specs exigem e que ainda NAO existe no backend.

---

### 1. Endpoints faltantes por spec

#### 1.1 Spec 005 — Gestao de Participantes

O `JogoParticipanteController` implementa: `POST /solicitar`, `GET /` (listar), `PUT /{pid}/aprovar`, `PUT /{pid}/rejeitar`, `DELETE /{pid}` (bane, nao remove).

**Faltam 4 endpoints exigidos pela spec:**

| # | Metodo | Path | Role | Status | Descricao |
|---|--------|------|------|--------|-----------|
| 1 | `PUT` | `/api/v1/jogos/{jogoId}/participantes/{pid}/banir` | MESTRE | **NAO EXISTE** | Banir participante APROVADO. O DELETE atual faz banimento, nao remocao provisoria (conflito semantico com a spec). |
| 2 | `PUT` | `/api/v1/jogos/{jogoId}/participantes/{pid}/desbanir` | MESTRE | **NAO EXISTE** | Desbanir participante BANIDO → APROVADO direto. Nenhum metodo `desbanir()` no service. |
| 3 | `GET` | `/api/v1/jogos/{jogoId}/participantes/meu-status` | JOGADOR | **NAO EXISTE** | Jogador consulta proprio status. Nenhum metodo no service. |
| 4 | `DELETE` | `/api/v1/jogos/{jogoId}/participantes/minha-solicitacao` | JOGADOR | **NAO EXISTE** | Jogador cancela propria solicitacao PENDENTE. Nenhum metodo no service. |

**Problemas no codigo existente:**

| # | Problema | Arquivo | Gravidade |
|---|---------|---------|-----------|
| P1 | `DELETE /{pid}` faz banimento (status = BANIDO), mas a spec diz que DELETE deve fazer remocao provisoria (soft delete). O endpoint de banimento deveria ser `PUT /{pid}/banir`. | `JogoParticipanteController.java:106-123` | MEDIA — semantica incorreta, breaking change para frontend |
| P2 | Filtro por status no GET nao implementado (`?status=PENDENTE`). `listar()` retorna tudo para Mestre ou apenas APROVADOS para Jogador, sem filtro. | `JogoParticipanteService.java:121-130` | BAIXA — funcional, falta filtro |
| P3 | A spec exige que BANIDO bloqueie re-solicitacao mesmo via registros soft-deleted. `solicitar()` usa `existsByJogoIdAndUsuarioId` que nao detecta registros com `deleted_at != null` (por causa do @SQLRestriction). | `JogoParticipanteService.java:50` | ALTA — jogador banido+removido pode re-solicitar |
| P4 | `solicitar()` nao distingue entre participacao APROVADA e BANIDA na rejeicao (409). A spec exige mensagens diferenciadas: "Voce ja e participante aprovado" vs "Voce foi banido". | `JogoParticipanteService.java:50-52` | BAIXA — UX |
| P5 | RF-012: Jogador nao aprovado nao deve acessar recursos do jogo. O `ParticipanteSecurityService.canAccessJogo()` valida isso, mas NEM TODOS os controllers de configuracao usam esse service — a maioria usa apenas `@PreAuthorize` global. | Todos os controllers de configuracao | ALTA — brecha de seguranca |

#### 1.2 Spec 006 — Wizard de Criacao de Ficha

**Entidade Ficha — campo `status` nao existe:**

A spec exige status `RASCUNHO` / `COMPLETA` no modelo Ficha. O campo nao existe na entidade `Ficha.java`. Isso bloqueia todo o fluxo de wizard.

**Endpoints faltantes:**

| # | Metodo | Path | Role | Status | Descricao |
|---|--------|------|------|--------|-----------|
| 1 | `PUT` | `/api/v1/fichas/{id}/completar` | MESTRE, JOGADOR(dono) | **NAO EXISTE** | Transicionar RASCUNHO → COMPLETA apos validacao de campos obrigatorios |
| 2 | `POST` | `/api/v1/fichas/{id}/xp` | MESTRE | **NAO EXISTE** | Conceder XP (aditivo). Nenhum metodo dedicado no service. A XP esta no `UpdateFichaRequest` — BUG DE SEGURANCA. |
| 3 | `PUT` | `/api/v1/fichas/{id}/essencia` | MESTRE, JOGADOR | **NAO EXISTE** | Atualizar apenas essencia atual (sem mexer em vida). Nenhum metodo no service. |
| 4 | `POST` | `/api/v1/fichas/{id}/resetar` | MESTRE | **NAO EXISTE** | Reset completo: vida, essencia, dano membros, prospeccao. Nenhum metodo no service. |

**Endpoints existentes que precisam de mudanca:**

| # | Endpoint | Mudanca necessaria |
|---|----------|--------------------|
| 1 | `POST /jogos/{jogoId}/fichas` | Deve setar `status = RASCUNHO`. Deve validar RacaClassePermitida. |
| 2 | `PUT /fichas/{id}` | Remover `xp` e `renascimentos` do `UpdateFichaRequest`. Adicionar campo `descricao`. |
| 3 | `GET /jogos/{jogoId}/fichas` | Fichas RASCUNHO devem aparecer na lista com badge para o dono, mas nao para sessao. |

#### 1.3 Spec 007 — VantagemEfeito e Motor de Calculos

**CRUD de VantagemEfeito ja existe** (VantagemEfeitoController + VantagemEfeitoService). O problema e que o `FichaCalculationService` ignora completamente os efeitos.

**Faltam:**

| # | Metodo | Path | Role | Status | Descricao |
|---|--------|------|------|--------|-----------|
| 1 | `POST` | `/api/v1/fichas/{id}/insolitus` | MESTRE | **NAO EXISTE** | Conceder insolitus (vantagem gratuita concedida pelo Mestre) |
| 2 | `GET` | `/api/v1/fichas/{id}/insolitus` | MESTRE, JOGADOR(dono) | **NAO EXISTE** | Listar insolitus da ficha |
| 3 | `DELETE` | `/api/v1/fichas/{id}/insolitus/{iid}` | MESTRE | **NAO EXISTE** | Remover insolitus (reversivel pelo Mestre) |

**Mudancas criticas no `FichaCalculationService`:**

| # | Mudanca | Status |
|---|---------|--------|
| 1 | `aplicarEfeitosVantagens()` — novo metodo que itera FichaVantagem → VantagemConfig.efeitos → distribui por TipoEfeito | **NAO EXISTE** |
| 2 | `recalcular()` — precisa incluir passo 2 (aplicar efeitos) entre atributos e bonus | **NAO EXISTE** — fluxo atual: atributos → bonus → estado |
| 3 | DADO_UP — calculo posicional baseado em `DadoProspeccaoConfig` | **NAO EXISTE** |
| 4 | FORMULA_CUSTOMIZADA — avaliacao via `FormulaEvaluatorService` com variaveis dinamicas | **NAO EXISTE** |

**Endpoints de prospeccao semanticos (substituem `PUT /fichas/{id}/prospeccao`):**

| # | Metodo | Path | Role | Status | Descricao |
|---|--------|------|------|--------|-----------|
| 1 | `POST` | `/api/v1/fichas/{id}/prospeccao/usar` | MESTRE, JOGADOR | **NAO EXISTE** | Decrementa 1 dado |
| 2 | `POST` | `/api/v1/fichas/{id}/prospeccao/conceder` | MESTRE | **NAO EXISTE** | Incrementa N dados |
| 3 | `POST` | `/api/v1/fichas/{id}/prospeccao/reverter` | MESTRE | **NAO EXISTE** | Incrementa 1 (reverte uso) |

#### 1.4 Spec 010 — Refatoracao de Roles

**Endpoints faltantes:**

| # | Metodo | Path | Role | Status | Descricao |
|---|--------|------|------|--------|-----------|
| 1 | `POST` | `/api/v1/usuarios/me/role` | Autenticado (sem role) | **NAO EXISTE** | Onboarding: usuario define propria role como MESTRE ou JOGADOR |
| 2 | `GET` | `/api/v1/admin/usuarios` | ADMIN | **NAO EXISTE** | Listar todos os usuarios (paginado) |
| 3 | `PUT` | `/api/v1/admin/usuarios/{id}/role` | ADMIN | **NAO EXISTE** | Promover/revogar role de usuario |

**Impacto transversal:** ~80 anotacoes `@PreAuthorize` em ~25 controllers precisam ser atualizadas:
- `hasRole('MESTRE')` → `hasAnyRole('ADMIN', 'MESTRE')`
- `hasAnyRole('MESTRE', 'JOGADOR')` → `hasAnyRole('ADMIN', 'MESTRE', 'JOGADOR')`

#### 1.5 GAPs transversais (nao associados a uma spec unica)

| # | Metodo | Path | Role | Status | Descricao |
|---|--------|------|------|--------|-----------|
| 1 | `POST` | `/api/v1/fichas/{fichaId}/visibilidade/{jogadorId}` | MESTRE | **NAO EXISTE** | Revelar NPC para jogador especifico |
| 2 | `DELETE` | `/api/v1/fichas/{fichaId}/visibilidade/{jogadorId}` | MESTRE | **NAO EXISTE** | Ocultar NPC de jogador |
| 3 | `GET` | `/api/v1/fichas/{fichaId}/visibilidade` | MESTRE | **NAO EXISTE** | Listar jogadores que podem ver NPC |
| 4 | `POST` | `/api/v1/jogos/{jogoId}/xp/lote` | MESTRE | **NAO EXISTE** | Conceder XP em lote (desejavel) |

---

### 2. Entidades a criar/modificar

#### 2.1 Novas entidades

| Entidade | Spec | Campos | Descricao |
|----------|------|--------|-----------|
| `FichaVisibilidade` | GAP-05 | `id`, `ficha` (FK), `jogadorId` (Long), UK(ficha_id, jogador_id) | Controla quais jogadores podem ver um NPC |

#### 2.2 Entidades a modificar

| Entidade | Spec | Mudanca | Status atual |
|----------|------|---------|--------------|
| **`Ficha`** | 006 | Adicionar campo `status` (enum: RASCUNHO, COMPLETA) com default RASCUNHO | **NAO EXISTE** — campo ausente |
| **`Ficha`** | 006 | Adicionar campos opcionais: `tituloHeroico` (String, max 100), `arquetipo` (String, max 100) | **NAO EXISTEM** |
| **`VantagemConfig`** | 007/GAP-09 | Adicionar campo `isInsolitus` (boolean, default false) | **NAO EXISTE** — apenas em docs |
| **`FichaAtributo`** | 007 | Adicionar campo `vantagens` (Integer, default 0). Total passa a ser `base + nivel + vantagens + outros` | **NAO EXISTE** — `recalcularTotal()` calcula `base + nivel + outros` |
| **`FichaAptidao`** | 007 | Adicionar campo `vantagens` (Integer, default 0). Total passa a ser `base + sorte + classe + vantagens` | **NAO EXISTE** — `recalcularTotal()` calcula `base + sorte + classe` |
| **`FichaVidaMembro`** | 007 | Adicionar campo `bonusVantagem` (Integer, default 0). Vida = floor(vidaTotal * porcentagem) + bonusVantagem | **NAO EXISTE** |
| **`Usuario`** | 010 | Campo `role` aceitar `null` (onboarding incompleto) e valor `ADMIN`. Atualmente default "JOGADOR", nao aceita null. | Parcial — campo existe como String, nao tem ADMIN |
| **`FichaResumoResponse`** (DTO) | 006 | Adicionar: `vidaAtual`, `essenciaAtual`, `pontosAtributoDisponiveis`, `pontosAptidaoDisponiveis`, `pontosVantagemDisponiveis`, `status` | **FALTAM 6 campos** — DTO atual tem apenas 11 campos |
| **`UpdateFichaRequest`** (DTO) | 006 | Remover `xp` e `renascimentos`. Adicionar `descricao` (String, opcional). | BUG de seguranca ativo |

#### 2.3 Novos enums

| Enum | Spec | Valores |
|------|------|---------|
| `StatusFicha` | 006 | `RASCUNHO`, `COMPLETA` |
| `RoleUsuario` (refatorar) | 010 | `ADMIN`, `MESTRE`, `JOGADOR` |

---

### 3. Bugs de seguranca identificados

#### BUG-01: Jogador pode alterar propria XP (CRITICO)

**Arquivo:** `FichaService.java` linhas 284-292
**Endpoint:** `PUT /api/v1/fichas/{id}`
**DTO:** `UpdateFichaRequest` contem campos `xp` e `renascimentos`
**`@PreAuthorize`:** `hasAnyRole('MESTRE', 'JOGADOR')`

**Vetor de ataque:** Jogador autenticado envia `PUT /fichas/{id} { "xp": 999999 }` e obtém nivel maximo.

**Correcao:** Remover `xp` e `renascimentos` de `UpdateFichaRequest`. Criar endpoint dedicado `POST /fichas/{id}/xp` com `@PreAuthorize("hasRole('MESTRE')")`.

**Gravidade:** CRITICA — exploravel por qualquer jogador autenticado.
**Esforco:** P1 (pequeno, 30min)

#### BUG-02: Jogador nao aprovado acessa recursos do jogo (ALTO)

**Arquivo:** Todos os controllers em `controller/configuracao/` (AtributoController, BonusController, etc.)
**Problema:** Os 13+ controllers de configuracao usam apenas `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")` sem validar participacao aprovada no jogo. Um jogador PENDENTE ou REJEITADO pode acessar `GET /jogos/{jogoId}/atributos` de qualquer jogo.

**Impacto:** Vazamento de informacao — jogador pode ver todas as configuracoes de um jogo antes de ser aprovado (ou mesmo se foi rejeitado/banido).

**Correcao:** Adicionar chamada a `ParticipanteSecurityService.assertCanAccessJogo(jogoId, usuarioId)` no inicio de cada service de configuracao, OU refatorar para um interceptor/filter que valide automaticamente.

**Gravidade:** ALTA — vazamento de dados de jogo.
**Esforco:** P2 (medio — 13+ services afetados)

#### BUG-03: Jogador banido com soft delete pode re-solicitar (MEDIO)

**Arquivo:** `JogoParticipanteService.java:50`
**Problema:** `existsByJogoIdAndUsuarioId()` nao detecta registros com `deleted_at != null` (por causa do `@SQLRestriction`). Se um jogador e banido e depois removido (soft delete), a verificacao de existencia retorna `false`, permitindo re-solicitacao.

**Correcao:** Usar query nativa que ignora `@SQLRestriction` para checar banimento, OU nunca soft-deletar registros BANIDOS.

**Gravidade:** MEDIA — cenario improvavel (requer banimento + remocao).
**Esforco:** P1 (pequeno)

#### BUG-04: PUT /fichas/{id}/prospeccao sem restricao de role (MEDIO)

**Arquivo:** `FichaController.java:299-309`
**Problema:** `PUT /fichas/{id}/prospeccao` com `hasAnyRole('MESTRE', 'JOGADOR')` permite que jogador sete quantidade arbitraria de dados de prospeccao. O endpoint aceita `quantidade` absoluta sem validar incremento/decremento.

**Correcao:** Substituir por 3 endpoints semanticos: `/usar` (MESTRE+JOGADOR, -1), `/conceder` (MESTRE, +N), `/reverter` (MESTRE, +1).

**Gravidade:** MEDIA — jogador pode se dar dados infinitos.
**Esforco:** P2 (medio)

#### BUG-05: DELETE /participantes/{pid} faz banimento, nao remocao (BAIXO)

**Arquivo:** `JogoParticipanteController.java:106-123`
**Problema:** O `DELETE` atual faz banimento (status = BANIDO), mas a spec 005 define DELETE como remocao provisoria (soft delete, jogador pode re-solicitar). A spec reserva `PUT /{pid}/banir` para banimento.

**Impacto:** Semantica errada — acao destrutiva demais para DELETE que deveria ser reversivel.
**Esforco:** P1 (refatorar endpoint + criar PUT /banir)

---

### 4. Campos faltantes no FichaResumoResponse

O DTO atual:

```java
// ATUAL -- FichaResumoResponse.java
public record FichaResumoResponse(
    Long id, String nome, int nivel, long xp,
    String racaNome, String classeNome,
    Map<String, Integer> atributosTotais,
    Map<String, Integer> bonusTotais,
    int vidaTotal, int essenciaTotal, int ameacaTotal
) {}
```

**Campos faltantes exigidos pelas specs:**

| Campo | Tipo | Spec | Descricao |
|-------|------|------|-----------|
| `vidaAtual` | int | 006/007 | Estado de combate — vida restante |
| `essenciaAtual` | int | 006/007 | Estado de combate — essencia restante |
| `pontosAtributoDisponiveis` | int | 006 | Ganhos (NivelConfig) - gastos (soma base FichaAtributo) |
| `pontosAptidaoDisponiveis` | int | 006 | Ganhos (NivelConfig) - gastos (soma base FichaAptidao) |
| `pontosVantagemDisponiveis` | int | 006 | Ganhos (PontosVantagemConfig) - gastos (soma custoPago FichaVantagem) |
| `status` | String | 006 | RASCUNHO ou COMPLETA |
| `renascimentos` | int | transversal | Ja existe na Ficha, falta no resumo |
| `isNpc` | boolean | transversal | Diferenciacao NPC/jogador no resumo |

---

### 5. Verificacao de acesso duplicada — oportunidade de refatoracao

O metodo `verificarAcessoLeitura()` esta duplicado em 3 services com logica identica:

| Service | Linhas | Logica |
|---------|--------|--------|
| `FichaService` | verificarAcessoLeitura (inline) | isMestre? NPC? dono? |
| `FichaResumoService` | 105-124 | isMestre? NPC? dono? |
| `FichaVidaService` | (verificar) | isMestre? NPC? dono? |

**Proposta:** Extrair para `FichaAccessService` compartilhado ANTES de implementar visibilidade NPC (GAP-05). Motivo: a logica de visibilidade NPC vai precisar ser adicionada em TODOS os pontos de verificacao — se estiver duplicada, o risco de inconsistencia e alto.

---

### 6. Estimativa de esforco total

| Item | Spec | Complexidade | Arquivos impactados | Testes estimados |
|------|------|-------------|---------------------|-----------------|
| BUG-01: Remover xp/renascimentos do UpdateFichaRequest + endpoint POST /xp | 006 | **P1** | 3 (DTO, Service, Controller) | 5 |
| BUG-02: Validacao de participacao nos config controllers | 005 | **P2** | 13+ services | 13 |
| BUG-04: Endpoints semanticos de prospeccao | 007 | **P2** | 4 (DTOs, Service, Controller) | 8 |
| BUG-05: Corrigir DELETE participantes + criar PUT /banir | 005 | **P1** | 2 (Controller, Service) | 4 |
| Endpoints faltantes spec 005 (desbanir, meu-status, minha-solicitacao, filtro) | 005 | **P2** | 3 (Controller, Service, Repository) | 10 |
| Campo `status` em Ficha + enum StatusFicha + endpoint /completar | 006 | **P2** | 5 (Entity, Enum, Service, Controller, DTO) | 8 |
| Endpoint PUT /essencia + POST /resetar | 006/007 | **P1** | 3 (DTOs, Service, Controller) | 6 |
| Campo isInsolitus em VantagemConfig + validacao | 007 | **P1** | 4 (Entity, DTO, Service, Response) | 4 |
| Campos vantagens em FichaAtributo/FichaAptidao/FichaVidaMembro | 007 | **P2** | 6 (3 entities, 3 recalcularTotal) | 10 |
| Motor de calculo: aplicarEfeitosVantagens no FichaCalculationService | 007 | **P3** | 8 (Service, 3 entities, repos, testes) | 20+ |
| FichaResumoResponse: adicionar 8 campos + calcularPontosDisponiveis | 006 | **P2** | 4 (DTO, Service, Repository queries) | 8 |
| FichaVisibilidade: entity + repo + service + controller + endpoints | GAP-05 | **P2** | 6 (Entity, Repo, Service, Controller, DTOs) | 10 |
| FichaAccessService: refatorar verificarAcessoLeitura | Refactor | **P1** | 4 (novo service, 3 services existentes) | 5 |
| Endpoints insolitus (POST/GET/DELETE) | 007 | **P2** | 4 (Controller, Service, DTOs) | 8 |
| Spec 010: campo ADMIN + onboarding + admin endpoints | 010 | **P3** | 30+ (Entity, Enum, CustomOAuth2, 25 controllers, SecurityConfig) | 25+ |
| **TOTAL** | | | **~100 arquivos** | **~145 testes** |

---

### 7. Ordem de implementacao recomendada

```
FASE 0 — Correcoes de seguranca urgentes (pode ser feito em qualquer branch)
├── BUG-01: Remover xp/renascimentos do UpdateFichaRequest      [P1, 30min]
├── BUG-04: Endpoints semanticos de prospeccao (substitui PUT)   [P2, 3h]
└── BUG-05: Corrigir DELETE /participantes (remocao vs banir)    [P1, 1h]

FASE 1 — Fundacao (pre-requisitos para todas as specs)
├── FichaAccessService: extrair verificarAcessoLeitura           [P1, 1h]
├── BUG-02: Validar participacao aprovada nos config controllers [P2, 3h]
├── Campo status em Ficha + enum StatusFicha                     [P2, 2h]
└── Endpoint POST /fichas/{id}/xp + nivel automatico             [P1, 2h]

FASE 2 — Spec 005 completa
├── PUT /{pid}/banir (separar de DELETE)                         [P1, 1h]
├── PUT /{pid}/desbanir                                          [P1, 1h]
├── GET /meu-status                                              [P1, 30min]
├── DELETE /minha-solicitacao                                     [P1, 30min]
├── Filtro ?status=X na listagem                                 [P1, 30min]
└── BUG-03: Verificacao de BANIDO ignorando soft delete          [P1, 1h]

FASE 3 — Spec 007 (motor de calculos)
├── isInsolitus em VantagemConfig                                [P1, 1h]
├── Campos vantagens em FichaAtributo/FichaAptidao/FichaVidaMembro [P2, 2h]
├── FichaCalculationService.aplicarEfeitosVantagens()            [P3, 8h]
│   ├── BONUS_ATRIBUTO, BONUS_APTIDAO, BONUS_DERIVADO
│   ├── BONUS_VIDA, BONUS_VIDA_MEMBRO, BONUS_ESSENCIA
│   ├── DADO_UP (posicional, MVP: informativo)
│   └── FORMULA_CUSTOMIZADA (exp4j + variaveis dinamicas)
├── Endpoints insolitus (POST/GET/DELETE /fichas/{id}/insolitus) [P2, 3h]
└── Testes de regressao extensivos no motor de calculo           [P3, 4h]

FASE 4 — Spec 006 (wizard)
├── PUT /fichas/{id}/completar (RASCUNHO → COMPLETA)             [P2, 2h]
├── PUT /fichas/{id}/essencia                                    [P1, 1h]
├── POST /fichas/{id}/resetar                                    [P1, 1h]
├── FichaResumoResponse: 8 campos novos + calcularPontosDisponiveis [P2, 3h]
├── Validacao RacaClassePermitida no POST /fichas                [P1, 1h]
├── Campos tituloHeroico + arquetipo em Ficha                    [P1, 30min]
└── Concessao XP em lote (desejavel)                             [P2, 2h]

FASE 5 — NPC Visibilidade
├── FichaVisibilidade: entity + repo + controller + endpoints    [P2, 3h]
├── Integrar visibilidade no FichaAccessService                  [P1, 1h]
├── Query de listagem de fichas incluir NPCs visiveis para jogador [P2, 2h]
└── Testes de integracao                                         [P2, 2h]

FASE 6 — Spec 010 (Roles) — ULTIMO
├── Enum RoleUsuario: ADMIN, MESTRE, JOGADOR                    [P1, 30min]
├── Usuario.role aceitar null + ADMIN                            [P1, 30min]
├── CustomOAuth2UserService: adaptar injecao de authorities      [P2, 2h]
├── Endpoint POST /usuarios/me/role (onboarding)                 [P2, 2h]
├── Endpoints admin (GET /admin/usuarios, PUT .../role)          [P2, 3h]
├── Atualizar ~80 @PreAuthorize em ~25 controllers               [P2, 3h]
├── Seed SQL do primeiro ADMIN                                   [P1, 30min]
└── Testes extensivos de permissao                               [P3, 4h]
```

### Justificativa da ordem

1. **Fase 0** e urgente: BUG-01 e uma vulnerabilidade de seguranca ativa.
2. **Fase 1** cria a fundacao: `FichaAccessService` e ponto de entrada para GAP-05; `status` na Ficha e ponto de entrada para wizard; endpoint de XP e necessidade imediata.
3. **Fase 2** completa spec 005 que bloqueia spec 006 (participacao aprovada e pre-condicao para criar fichas).
4. **Fase 3** implementa o motor de calculos que a spec 006 assume como funcional.
5. **Fase 4** implementa o wizard com todos os pre-requisitos satisfeitos.
6. **Fase 5** implementa visibilidade de NPC (independente, pode ser paralela a fase 4).
7. **Fase 6** e a ultima por ser transversal e de alto risco. Se feita antes, atrasa tudo; se feita cedo e quebrada, impacta todos os testes.

### Riscos criticos

| # | Risco | Mitigacao |
|---|-------|----------|
| R1 | Motor de calculos (Fase 3) e o maior esforco e maior risco de regressao | Snapshot tests: salvar calculos de 5+ fichas de referencia antes e verificar depois |
| R2 | Spec 010 impacta ~80 anotacoes em ~25 controllers | Usar regex de busca+replace com revisao manual; testes de integracao para cada controller |
| R3 | Campo status em Ficha pode quebrar frontend que nao espera fichas RASCUNHO | Comunicar ao frontend antes; default COMPLETA para fichas existentes na migracao |
| R4 | BUG-02 (participacao nos configs) pode quebrar testes existentes que nao criam participante | Atualizar setup dos testes para criar participante aprovado |
| R5 | Mudanca semantica do DELETE /participantes (banir → remover) e breaking change | Coordenar com frontend; implementar PUT /banir no mesmo PR |

---

*Produzido por: Tech Lead Backend | 2026-04-03*
*Revisao baseada em: Specs 005, 006, 007, 010 + codigo atual (457 testes)*
*Proxima acao: Implementar Fase 0 (correcoes de seguranca urgentes)*
