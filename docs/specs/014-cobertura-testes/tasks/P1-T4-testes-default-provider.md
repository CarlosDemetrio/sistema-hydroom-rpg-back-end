# T4 — Testes para DefaultGameConfigProviderImpl

> Fase: Backend Testes | Dependencias: T1 (JaCoCo setup) | Bloqueia: Nenhuma
> Estimativa: 1–2 horas

---

## Objetivo

Criar testes unitarios para `DefaultGameConfigProviderImpl`, verificando que as configuracoes padrao retornadas sao coerentes, completas e sem conflitos.

---

## Contexto

`DefaultGameConfigProviderImpl` e o provider que retorna as configuracoes padrao para um novo jogo. Quando o Mestre cria um jogo, o `GameConfigInitializerService` chama este provider para popular 13+ tipos de configuracao com valores do sistema Klayrah.

Erros neste provider (ex: abreviacoes duplicadas, formulas invalidas, valores fora do range) propagam para TODAS as fichas do jogo.

---

## Arquivo de Teste

`test/java/.../service/DefaultGameConfigProviderImplTest.java`

Tipo: **UNITARIO** — o provider nao depende de banco de dados.

---

## Cenarios

### Grupo 1 — Completude

| Cenario | Descricao | Validacao |
|---------|-----------|-----------|
| TC-DP-01 | Provider retorna atributos padrao | Lista nao vazia, contem FOR, AGI, VIG, SAB, INT, INTU, AST |
| TC-DP-02 | Provider retorna aptidoes padrao | Lista nao vazia |
| TC-DP-03 | Provider retorna bonus padrao | Lista nao vazia |
| TC-DP-04 | Provider retorna todas as 13+ configs | Verificar que cada tipo de config e retornado |

### Grupo 2 — Coerencia

| Cenario | Descricao | Validacao |
|---------|-----------|-----------|
| TC-DP-05 | Abreviacoes de atributo sao unicas | Nenhuma abreviacao repetida no Set retornado |
| TC-DP-06 | Abreviacoes tem tamanho valido (2-5 chars) | Todas entre 2 e 5 caracteres |
| TC-DP-07 | Nomes nao sao vazios | Nenhum nome null ou blank |
| TC-DP-08 | OrdemExibicao sequencial e sem gaps | Para cada lista, ordem comeca em 1 e incrementa |

### Grupo 3 — Formulas

| Cenario | Descricao | Validacao |
|---------|-----------|-----------|
| TC-DP-09 | Formulas de impeto sao validas | Para cada AtributoConfig com formulaImpeto, formula parseia sem erro |
| TC-DP-10 | Formulas de bonus sao validas | Para cada BonusConfig com formulaBase, formula parseia sem erro |
| TC-DP-11 | Formulas referenciam apenas abreviacoes existentes | Variaveis usadas nas formulas existem como abreviacoes de atributo |

### Grupo 4 — Valores

| Cenario | Descricao | Validacao |
|---------|-----------|-----------|
| TC-DP-12 | AtributoConfig: valorMinimo <= valorMaximo | Para cada atributo, min <= max |
| TC-DP-13 | MembroCorpoConfig: porcentagens somam ~1.0 | Soma das porcentagens entre 0.95 e 1.05 (tolerancia) |
| TC-DP-14 | NivelConfig: xpNecessaria cresce monotonicamente | nivel[n].xpNecessaria > nivel[n-1].xpNecessaria |
| TC-DP-15 | DadoProspeccaoConfig: faces > 0 | Todas as faces sao positivas |

---

## Estimativa de Testes

- Grupo 1: 4 testes
- Grupo 2: 4 testes
- Grupo 3: 3 testes
- Grupo 4: 4 testes
- **Total: ~15 testes**

---

## Criterios de Aceitacao

- [ ] 15 testes cobrindo completude, coerencia, formulas e valores
- [ ] `DefaultGameConfigProviderImpl` com >= 80% branch coverage
- [ ] Nenhum teste faz acesso ao banco de dados (unitario puro)
- [ ] `./mvnw test` passa com todos os testes existentes + novos
