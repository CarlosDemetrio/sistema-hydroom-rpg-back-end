# Spec 006 — Plan de Implementacao: Wizard de Criacao de Ficha

> Spec: `006-ficha-wizard`
> Status: Pronto para implementacao
> Gerado em: 2026-04-02
> Depende de: Spec 005 (JogoParticipante com status APROVADO ja implementado na model)
> Bloqueia: Spec 008, Spec 009

---

## 1. Estado Atual do Codigo

### 1.1 O que ja existe e pode ser reutilizado

| Artefato | Localizacao | Estado |
|---|---|---|
| `Ficha` (model) | `model/Ficha.java` | Completo — sem campo `status` |
| `FichaService.criar()` | `service/FichaService.java` | Funciona, mas cria ficha sem status |
| `FichaService.atualizar()` | `service/FichaService.java` | Funciona, aceita XP do request (bug a corrigir) |
| `FichaService.atualizarAtributos()` | `service/FichaService.java` | Implementado |
| `FichaService.atualizarAptidoes()` | `service/FichaService.java` | Implementado |
| `FichaVantagemService.comprar()` | `service/FichaVantagemService.java` | Implementado |
| `FichaResumoService.getResumo()` | `service/FichaResumoService.java` | Implementado, sem pontosDisponiveis |
| `FichaPreviewService.simular()` | `service/FichaPreviewService.java` | Implementado |
| `CreateFichaRequest` (record) | `dto/request/CreateFichaRequest.java` | Completo |
| `UpdateFichaRequest` (record) | `dto/request/UpdateFichaRequest.java` | Verificar se aceita xp |
| `FichaResponse` (record) | `dto/response/FichaResponse.java` | Sem campo `status` |
| `FichaResumoResponse` (record) | `dto/response/FichaResumoResponse.java` | Sem pontosDisponiveis |
| `FichaController` | `controller/FichaController.java` | Sem endpoints /completar e /xp |
| `FichaAtributo`, `FichaAptidao` | model | Completos |
| `NivelConfig`, `PontosVantagemConfig` | model | Completos |
| `RacaClassePermitida` | model | Implementado (validacao a adicionar no criar) |

### 1.2 O que esta faltando (gaps que esta spec resolve)

| Gap | Impacto | Onde resolver |
|---|---|---|
| `Ficha.status` (enum RASCUNHO/COMPLETA) ausente | Ficha criada nunca sabe se esta incompleta | T1 — Model + Migration |
| `PUT /fichas/{id}/completar` ausente | Nao ha como transicionar RASCUNHO -> COMPLETA | T1 — Controller |
| `PUT /fichas/{id}/xp` ausente | Mestre nao tem endpoint dedicado para XP | T4 — Controller |
| `FichaResumoResponse` sem pontosDisponiveis | Frontend nao sabe quantos pontos o jogador tem | T5 — Response + Service |
| `FichaResponse` sem `status` | Frontend nao sabe exibir badge "Incompleta" | T1 — Response |
| Validacao `RacaClassePermitida` na criacao | Classe incompativel com Raca pode ser salva | T2 — Service |
| JOGADOR pode passar XP no `UpdateFichaRequest` | Falha de seguranca | T3 — Service |
| Wizard completo no frontend | Formulario atual envia apenas {nome} | T6 a T13 |

---

## 2. Modelagem do Conceito de Rascunho

### 2.1 Campo `status` na entidade Ficha

**Decisao:** Usar enum `FichaStatus` com dois valores: `RASCUNHO` e `COMPLETA`.

**Racional:** Um boolean `fichaCompleta` seria mais simples, mas o enum e extensivel (futuramente `ARQUIVADA`, `SUSPENSA`) e semanticamente mais claro para o dominio.

**Definicao do enum:**
```java
public enum FichaStatus {
    RASCUNHO,   // campos obrigatorios nao preenchidos; nao pode entrar em sessao
    COMPLETA    // todos os campos obrigatorios validos; pode entrar em sessao
}
```

**Adicao na entity `Ficha`:**
```java
@Builder.Default
@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false, length = 20)
private FichaStatus status = FichaStatus.RASCUNHO;
```

**Regra de transicao:**
- `RASCUNHO -> COMPLETA`: via `PUT /fichas/{id}/completar` (backend valida pre-condicoes)
- `COMPLETA -> RASCUNHO`: nunca — a transicao e unidirecional no MVP
- Uma ficha criada via `POST /fichas` sempre inicia como `RASCUNHO`

### 2.2 Criterio de completude (validado no /completar)

Para uma ficha transicionar para COMPLETA, o backend verifica:
1. `nome` preenchido (ja garantido pelo @NotBlank na criacao)
2. `raca` nao nulo
3. `classe` nao nulo
4. `genero` nao nulo
5. `indole` nao nulo
6. `presenca` nao nulo
7. `classe` permitida pela `raca` (RacaClassePermitida)

Pontos de atributo/aptidao/vantagem nao sao pre-condicao para COMPLETA — jogador pode reservar pontos.

---

## 3. Estrategia de Auto-save

### 3.1 Conceito

O auto-save acontece ao avancar de passo, nao por campo nem por debounce. O frontend dispara uma chamada HTTP ao clicar em "Proximo".

**Racional:** Debounce por campo geraria muitas requisicoes e precisaria de logica de retry complexa. Auto-save por passo e mais simples, previsivel e alinhado com o modelo mental do usuario.

### 3.2 Sequencia de chamadas por passo

| Passo | Acao ao clicar "Proximo" | Endpoint chamado | Condicao para avancar |
|-------|--------------------------|------------------|-----------------------|
| Passo 1 (1a vez) | Cria ficha com RASCUNHO | `POST /jogos/{jogoId}/fichas` | Validacao client-side OK + HTTP 201 |
| Passo 1 (edicao) | Atualiza identificacao | `PUT /fichas/{id}` | HTTP 200 |
| Passo 2 | Salva atributos | `PUT /fichas/{id}/atributos` | HTTP 200 |
| Passo 3 | Salva aptidoes | `PUT /fichas/{id}/aptidoes` | HTTP 200 |
| Passo 4 | Compra vantagens (se houver) | `POST /fichas/{id}/vantagens` (uma por vantagem) | HTTP 201 para cada |
| Passo 5 | Confirma criacao | `PUT /fichas/{id}/completar` | HTTP 200, redireciona |

### 3.3 Estados visuais do auto-save (frontend)

```
tipo EstadoSalvamento = 'idle' | 'salvando' | 'salvo' | 'erro'
```

- `idle`: estado inicial, nenhuma acao ainda
- `salvando`: HTTP request em andamento (mostra spinner)
- `salvo`: resposta 2xx recebida (mostra icone check verde por 3s)
- `erro`: resposta 4xx/5xx (mostra icone de aviso + mensagem)

Nenhum passo pode ser avancado enquanto `estadoSalvamento === 'salvando'`.

### 3.4 Retomada de rascunho

Quando o usuario abre o wizard com `fichaId` na query string (ex: `/criar-personagem?fichaId=42`), o frontend:
1. Faz `GET /fichas/42` para carregar os dados ja salvos
2. Faz `GET /fichas/42/atributos` para carregar o Passo 2
3. Faz `GET /fichas/42/aptidoes` para carregar o Passo 3
4. Faz `GET /fichas/42/vantagens` para carregar o Passo 4
5. Determina o passo inicial pelo primeiro campo nulo na sequencia obrigatoria

---

## 4. Sequencia de Implementacao

### Regra geral: backend sempre antes do frontend

O frontend nao pode ser implementado sem os endpoints correspondentes. A ordem de implementacao segue:

```
T1 (status + /completar)
  |
  +-- T2 (validacao RacaClassePermitida)
  |
  +-- T3 (bloquear XP no UpdateFichaRequest para JOGADOR)
  |
  +-- T4 (/xp endpoint MESTRE-only)
  |
  +-- T5 (pontosDisponiveis no FichaResumoResponse)
        |
        +-- T6 (Passo 1: Identificacao)
              |
              +-- T7 (Passo 2: Descricao fisica)
              |
              +-- T8 (Passo 3: Atributos)
              |
              +-- T9 (Passo 4: Aptidoes)
              |
              +-- T10 (Passo 5: Vantagens)
              |
              +-- T11 (Passo 6: Revisao e confirmacao)
                    |
                    +-- T12 (Auto-save visual)
                    |
                    +-- T13 (Badge incompleta na listagem)
```

### Dependencias criticas entre tasks

- T8 (Atributos) depende de T5 (pontosDisponiveis) estar disponivel no backend
- T10 (Vantagens) depende de T5 (pontosVantagemDisponiveis) estar disponivel no backend
- T11 (Revisao) depende de T1 (/completar endpoint) estar disponivel
- T13 (Badge incompleta) depende de T1 (campo status na FichaResponse) estar disponivel

---

## 5. Mapeamento das Tasks

| ID | Fase | Titulo | Complexidade | Prerequisito |
|----|------|--------|--------------|--------------|
| T1 | Backend | Campo status + endpoint /completar | Media | — |
| T2 | Backend | Validacao RacaClassePermitida na criacao | Baixa | — |
| T3 | Backend | Bloquear XP no PUT /fichas/{id} para JOGADOR | Baixa | — |
| T4 | Backend | Endpoint PUT /fichas/{id}/xp (MESTRE-only) | Media | — |
| T5 | Backend | pontosDisponiveis no FichaResumoResponse | Alta | — |
| T6 | Frontend | Passo 1: Identificacao (rewrite) | Alta | T1 |
| T7 | Frontend | Passo 2: Descricao fisica (campo opcional) | Baixa | T6 |
| T8 | Frontend | Passo 3: Distribuicao de atributos | Alta | T5, T6 |
| T9 | Frontend | Passo 4: Distribuicao de aptidoes | Media | T5, T6 |
| T10 | Frontend | Passo 5: Compra de vantagens iniciais | Alta | T5, T6 |
| T11 | Frontend | Passo 6: Revisao e confirmacao | Media | T1, T6 |
| T12 | Frontend | Auto-save visual (indicador de salvamento) | Baixa | T6 |
| T13 | Frontend | Badge "incompleta" na listagem de fichas | Baixa | T1 |

---

## 6. Consideracoes Tecnicas

### Backend

- **Migration de banco**: adicionar coluna `status VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO'` na tabela `fichas`. Fichas existentes ficam como `RASCUNHO` ate o Mestre completar manualmente.
- **NivelConfig inicial**: o nivel 1 e o nivel inicial para calculo de pontos. Se o NivelConfig para nivel=1 nao existir, lancar `ValidationException("Configuracao de nivel inicial ausente")`.
- **pontosAtributoDisponiveis**: calcular com `SUM(pontosAtributo para niveis 1..nivelAtual) - SUM(base de FichaAtributos)`. O campo `base` de FichaAtributo e o unico editavel pelo jogador no MVP.
- **RacaClassePermitida**: a tabela `raca_classe_permitida` ja existe. Adicionar query no `FichaValidationService` antes de persistir.
- **Idempotencia do /completar**: se a ficha ja esta COMPLETA, retornar 200 (nao lancar erro).

### Frontend

- **Destruir `FichaFormComponent` atual**: o componente atual tem estrutura de dados irreconciliavel com o backend. Reescrever do zero e mais seguro que refatorar.
- **Wizard como componente standalone**: `FichaWizardComponent` em rota dedicada `/jogos/:jogoId/fichas/novo` para criacao e `/fichas/:fichaId/wizard` para retomada de rascunho.
- **Signals para estado local**: `fichaId = signal<number | null>(null)`, `passoAtual = signal<number>(1)`, `estadoSalvamento = signal<EstadoSalvamento>('idle')`.
- **Guard de saida**: ao navegar para fora do wizard com rascunho nao confirmado, exibir `ConfirmDialog` ("Seu rascunho foi salvo. Voce pode continuar de onde parou.").
