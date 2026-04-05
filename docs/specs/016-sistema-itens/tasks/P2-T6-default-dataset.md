# T6 — DefaultGameConfigProviderImpl: Dataset Completo de Itens

> Fase: Backend — Dataset | Prioridade: P2
> Dependencias: T1, T2, T3 concluidos
> Bloqueia: T7 (testes de integracao com dataset)
> Estimativa: 1-2 dias

---

## Objetivo

Adicionar ao `DefaultGameConfigProviderImpl` o dataset completo de itens para novos jogos: 7 raridades, 20 tipos, 40 itens baseados no D&D 5e SRD adaptados ao Klayrah, e equipamentos iniciais para as 12 classes. Este dataset e criado automaticamente quando um novo Jogo e inicializado via `GameConfigInitializerService`.

---

## Dataset Completo

### 7 Raridades (RaridadeItemConfig)

| ordem | nome | cor | podeJogadorAdicionar | bonusAtributoMin | bonusAtributoMax | bonusDerivadoMin | bonusDerivadoMax | descricao |
|-------|------|-----|---------------------|-----------------|-----------------|-----------------|-----------------|-----------|
| 1 | Comum | #9d9d9d | true | 0 | 0 | 0 | 0 | Itens mundanos sem encantamento |
| 2 | Incomum | #1eff00 | false | 1 | 1 | 1 | 1 | Levemente encantado ou de qualidade excepcional |
| 3 | Raro | #0070dd | false | 1 | 2 | 1 | 2 | Encantamento moderado, raramente encontrado |
| 4 | Muito Raro | #a335ee | false | 2 | 3 | 2 | 3 | Encantamento poderoso, obra de artesao mestre |
| 5 | Epico | #ff8000 | false | 3 | 4 | 3 | 4 | Artefato de grande poder, historia propria |
| 6 | Lendario | #e6cc80 | false | 4 | 5 | 4 | 5 | Um dos poucos existentes no mundo |
| 7 | Unico | #e268a8 | false | 0 | 0 | 0 | 0 | Criacao unica do Mestre, sem referencia de custo |

---

### 20 Tipos (TipoItemConfig)

| ordem | nome | categoria | subcategoria | requerDuasMaos |
|-------|------|-----------|-------------|----------------|
| 1 | Espada Curta | ARMA | ESPADA | false |
| 2 | Espada Longa | ARMA | ESPADA | false |
| 3 | Espada Dupla | ARMA | ESPADA | true |
| 4 | Arco Curto | ARMA | ARCO | true |
| 5 | Arco Longo | ARMA | ARCO | true |
| 6 | Adaga | ARMA | ADAGA | false |
| 7 | Machado de Batalha | ARMA | MACHADO | false |
| 8 | Machado Grande | ARMA | MACHADO | true |
| 9 | Martelo de Guerra | ARMA | MARTELO | false |
| 10 | Cajado | ARMA | CAJADO | true |
| 11 | Lanca | ARMA | LANCA | false |
| 12 | Armadura Leve | ARMADURA | ARMADURA_LEVE | false |
| 13 | Armadura Media | ARMADURA | ARMADURA_MEDIA | false |
| 14 | Armadura Pesada | ARMADURA | ARMADURA_PESADA | false |
| 15 | Escudo | ARMADURA | ESCUDO | false |
| 16 | Anel | ACESSORIO | ANEL | false |
| 17 | Amuleto | ACESSORIO | AMULETO | false |
| 18 | Pocao | CONSUMIVEL | POCAO | false |
| 19 | Municao | CONSUMIVEL | MUNICAO | false |
| 20 | Equipamento de Aventura | AVENTURA | OUTROS | false |

---

### 40 Itens (ItemConfig) — Dataset D&D 5e SRD Adaptado ao Klayrah

#### ARMAS (15 itens)

| # | nome | raridade | tipo | peso(kg) | valor(po) | duracaoPadrao | nivelMin | propriedades | efeitos |
|---|------|----------|------|----------|-----------|--------------|----------|-------------|---------|
| 1 | Adaga | Comum | Adaga | 0.45 | 2 | null | 1 | finura, arremesso, leve | — |
| 2 | Espada Curta | Comum | Espada Curta | 0.9 | 10 | null | 1 | finura, leve | — |
| 3 | Espada Longa | Comum | Espada Longa | 1.36 | 15 | null | 1 | versatil | — |
| 4 | Espada Longa +1 | Incomum | Espada Longa | 1.36 | 500 | 10 | 1 | versatil, magica | BONUS_DERIVADO(B.B.A, +1) |
| 5 | Espada Longa +2 | Raro | Espada Longa | 1.36 | 5000 | 15 | 5 | versatil, magica | BONUS_DERIVADO(B.B.A, +2) |
| 6 | Machadinha | Comum | Machado de Batalha | 0.9 | 5 | null | 1 | leve, arremesso | — |
| 7 | Machado de Batalha | Comum | Machado de Batalha | 1.8 | 10 | null | 1 | versatil | — |
| 8 | Machado Grande | Comum | Machado Grande | 3.17 | 30 | null | 3 | pesado, duas maos | — |
| 9 | Martelo de Guerra | Comum | Martelo de Guerra | 2.27 | 15 | null | 1 | versatil | — |
| 10 | Arco Curto | Comum | Arco Curto | 0.9 | 25 | null | 1 | duas maos, municao | — |
| 11 | Arco Longo | Comum | Arco Longo | 1.8 | 50 | null | 2 | duas maos, municao, pesado | — |
| 12 | Arco Longo +1 | Incomum | Arco Longo | 1.8 | 500 | 10 | 4 | duas maos, municao, magico | BONUS_DERIVADO(B.B.A, +1) |
| 13 | Cajado de Madeira | Comum | Cajado | 1.8 | 5 | null | 1 | versatil, duas maos | — |
| 14 | Cajado Arcano +1 | Incomum | Cajado | 2.0 | 500 | 10 | 3 | magico, foco arcano | BONUS_DERIVADO(B.B.M, +1) |
| 15 | Lanca | Comum | Lanca | 1.36 | 1 | null | 1 | arremesso, versatil | — |

#### ARMADURAS E ESCUDOS (10 itens)

| # | nome | raridade | tipo | peso(kg) | valor(po) | duracaoPadrao | nivelMin | propriedades | efeitos |
|---|------|----------|------|----------|-----------|--------------|----------|-------------|---------|
| 16 | Gibao de Couro | Comum | Armadura Leve | 4.5 | 10 | null | 1 | armadura leve | BONUS_DERIVADO(Defesa, +1) |
| 17 | Couro Batido | Comum | Armadura Leve | 11.3 | 45 | null | 1 | armadura leve | BONUS_DERIVADO(Defesa, +2) |
| 18 | Camisao de Malha | Comum | Armadura Media | 13.6 | 50 | null | 2 | armadura media | BONUS_DERIVADO(Defesa, +3) |
| 19 | Cota de Escamas | Comum | Armadura Media | 20.4 | 50 | null | 3 | armadura media, desvantagem Furtividade | BONUS_DERIVADO(Defesa, +4) |
| 20 | Cota de Malha | Comum | Armadura Pesada | 27.2 | 75 | null | 4 | armadura pesada, Forca minima | BONUS_DERIVADO(Defesa, +5) |
| 21 | Meia Placa | Comum | Armadura Pesada | 19.9 | 750 | null | 5 | armadura pesada | BONUS_DERIVADO(Defesa, +5), BONUS_DERIVADO(Reflexo, +1) |
| 22 | Placa Completa | Raro | Armadura Pesada | 29.5 | 1500 | 15 | 7 | armadura pesada, magica | BONUS_DERIVADO(Defesa, +6) |
| 23 | Escudo de Madeira | Comum | Escudo | 2.72 | 10 | null | 1 | escudo | BONUS_DERIVADO(Bloqueio, +1) |
| 24 | Escudo de Aco | Comum | Escudo | 2.72 | 20 | null | 1 | escudo | BONUS_DERIVADO(Bloqueio, +2) |
| 25 | Escudo Enfeiticado +1 | Incomum | Escudo | 2.72 | 500 | 10 | 3 | escudo, magico | BONUS_DERIVADO(Bloqueio, +2), BONUS_DERIVADO(Defesa, +1) |

#### ACESSORIOS E ITENS MAGICOS (5 itens)

| # | nome | raridade | tipo | peso(kg) | valor(po) | duracaoPadrao | nivelMin | propriedades | efeitos |
|---|------|----------|------|----------|-----------|--------------|----------|-------------|---------|
| 26 | Anel da Forca +1 | Raro | Anel | 0.01 | 2000 | null | 5 | magico, unico | BONUS_ATRIBUTO(FOR, +1) |
| 27 | Anel de Protecao +1 | Raro | Anel | 0.01 | 2000 | null | 5 | magico | BONUS_DERIVADO(Defesa, +1), BONUS_DERIVADO(Bloqueio, +1) |
| 28 | Amuleto de Saude | Incomum | Amuleto | 0.05 | 500 | null | 3 | magico | BONUS_VIDA(+5) |
| 29 | Amuleto da Essencia | Incomum | Amuleto | 0.05 | 500 | null | 3 | magico | BONUS_ESSENCIA(+5) |
| 30 | Manto de Elvenkind | Muito Raro | Amuleto | 0.45 | 5000 | null | 7 | magico | BONUS_DERIVADO(Esquiva, +3), BONUS_DERIVADO(Percepcao, +2) |

#### CONSUMIVEIS (5 itens)

| # | nome | raridade | tipo | peso(kg) | valor(po) | duracaoPadrao | nivelMin | propriedades | efeitos |
|---|------|----------|------|----------|-----------|--------------|----------|-------------|---------|
| 31 | Pocao de Cura Menor | Comum | Pocao | 0.45 | 25 | 1 | 1 | consumivel, recupera 5 de vida | — |
| 32 | Pocao de Cura | Comum | Pocao | 0.45 | 50 | 1 | 1 | consumivel, recupera 10 de vida | — |
| 33 | Pocao de Cura Superior | Incomum | Pocao | 0.45 | 200 | 1 | 3 | consumivel, recupera 25 de vida | — |
| 34 | Flecha Comum (20) | Comum | Municao | 0.45 | 1 | null | 1 | municao para arcos | — |
| 35 | Virote (20) | Comum | Municao | 0.36 | 1 | null | 1 | municao para bestas | — |

#### EQUIPAMENTOS DE AVENTURA (5 itens)

| # | nome | raridade | tipo | peso(kg) | valor(po) | duracaoPadrao | nivelMin | propriedades | efeitos |
|---|------|----------|------|----------|-----------|--------------|----------|-------------|---------|
| 36 | Kit de Aventureiro | Comum | Equipamento de Aventura | 12.0 | 12 | null | 1 | mochila, racao 10 dias, corda, archote | — |
| 37 | Kit de Curandeiro | Comum | Equipamento de Aventura | 1.5 | 5 | 10 | 1 | 10 usos de bandagem, 5 usos de antidoto | — |
| 38 | Kit de Ladroa | Comum | Equipamento de Aventura | 0.9 | 25 | null | 1 | ferramentas de ladroa, forcado VIG para abrir fechaduras | — |
| 39 | Lantena Bullseye | Comum | Equipamento de Aventura | 1.0 | 10 | null | 1 | iluminacao direcional 18m, 6h de oleo | — |
| 40 | Tomo Arcano | Comum | Equipamento de Aventura | 1.5 | 25 | null | 1 | livro de feiticos para Magos e Feiticeiros | — |

---

### Equipamentos Iniciais por Classe (ClasseEquipamentoInicial)

| Classe | Item | obrigatorio | grupoEscolha | qtd |
|--------|------|------------|-------------|-----|
| Guerreiro | Cota de Malha | true | null | 1 |
| Guerreiro | Escudo de Aco | true | null | 1 |
| Guerreiro | Espada Longa | false | 1 | 1 |
| Guerreiro | Machado de Batalha | false | 1 | 1 |
| Guerreiro | Martelo de Guerra | false | 1 | 1 |
| Arqueiro | Couro Batido | true | null | 1 |
| Arqueiro | Arco Longo | true | null | 1 |
| Arqueiro | Flecha Comum (20) | true | null | 1 |
| Arqueiro | Adaga | true | null | 1 |
| Monge | Gibao de Couro | true | null | 1 |
| Monge | Cajado de Madeira | false | 1 | 1 |
| Monge | Adaga | false | 1 | 2 |
| Berserker | Couro Batido | true | null | 1 |
| Berserker | Machado Grande | false | 1 | 1 |
| Berserker | Espada Longa | false | 1 | 1 |
| Assassino | Gibao de Couro | true | null | 1 |
| Assassino | Adaga | true | null | 2 |
| Assassino | Arco Curto | true | null | 1 |
| Assassino | Flecha Comum (20) | true | null | 1 |
| Fauno (Herdeiro) | Gibao de Couro | true | null | 1 |
| Fauno (Herdeiro) | Arco Curto | false | 1 | 1 |
| Fauno (Herdeiro) | Espada Curta | false | 1 | 1 |
| Mago | Tomo Arcano | true | null | 1 |
| Mago | Cajado de Madeira | false | 1 | 1 |
| Mago | Adaga | false | 1 | 1 |
| Feiticeiro | Tomo Arcano | false | 1 | 1 |
| Feiticeiro | Cajado de Madeira | false | 1 | 1 |
| Feiticeiro | Adaga | false | 1 | 1 |
| Sacerdote | Camisao de Malha | true | null | 1 |
| Sacerdote | Escudo de Madeira | true | null | 1 |
| Sacerdote | Martelo de Guerra | false | 1 | 1 |
| Sacerdote | Lanca | false | 1 | 1 |
| Ladrao | Couro Batido | true | null | 1 |
| Ladrao | Adaga | true | null | 2 |
| Ladrao | Kit de Ladroa | true | null | 1 |
| Negociante | Gibao de Couro | true | null | 1 |
| Negociante | Espada Curta | false | 1 | 1 |
| Negociante | Adaga | false | 1 | 1 |
| Necromante | Cajado de Madeira | true | null | 1 |
| Necromante | Tomo Arcano | true | null | 1 |

---

## Implementacao no DefaultGameConfigProviderImpl

```java
// Adicionar metodos ao DefaultGameConfigProviderImpl:

public List<RaridadeItemConfigDefault> getDefaultRaridades() { ... }

public List<TipoItemConfigDefault> getDefaultTipos() { ... }

public List<ItemConfigDefault> getDefaultItens(
    Map<String, Long> raridadesIds,  // "Comum" -> id, etc.
    Map<String, Long> tiposIds       // "Espada Longa" -> id, etc.
) { ... }

public List<ClasseEquipamentoInicialDefault> getDefaultEquipamentosIniciais(
    Map<String, Long> classesIds,  // "Guerreiro" -> id, etc.
    Map<String, Long> itensIds     // "Cota de Malha" -> id, etc.
) { ... }
```

**Ordem de chamada em GameConfigInitializerService:**
```
1. criar raridades → salvar ids em Map<nome, id>
2. criar tipos → salvar ids em Map<nome, id>
3. criar itens (com referencias por nome as raridades e tipos acima)
4. salvar ids de itens em Map<nome, id>
5. criar ClasseEquipamentoInicial (com referencias por nome a classes e itens)
```

---

## Regras de Negocio

- **RN-T6-01:** Dataset e criado apenas quando o Jogo e inicializado (flag `defaultsCreated` em Jogo, ou via `GameConfigInitializerService`)
- **RN-T6-02:** Efeitos de itens magicos (+1/+2) referem-se a BonusConfig pelo **nome** ("B.B.A", "Defesa", etc.) — o DefaultProvider deve usar os ids criados na mesma inicializacao
- **RN-T6-03:** Itens com `duracaoPadrao = null` sao indestrutíveis (ex: armaduras basicas — o desgaste e narrativo no Klayrah)
- **RN-T6-04:** Pocoes tem `duracaoPadrao = 1` (usadas uma vez)
- **RN-T6-05:** Valores de efeito (valorFixo) dos itens +1/+2 sao FIXOS e nao escalam com nivel

---

## Criterios de Aceitacao

- [ ] Criar novo Jogo gera 7 raridades, 20 tipos, 40 itens e equipamentos iniciais para as 12 classes
- [ ] Todos os itens magicos (+1/+2) tem `ItemEfeito` correto criado com referencia ao BonusConfig do mesmo jogo
- [ ] Equipamentos iniciais de todas as 12 classes sao criados corretamente
- [ ] Grupos de escolha configurados para classes que tem selecao (Guerreiro, Monge, Berserker, etc.)
- [ ] Dataset e idempotente: criar o mesmo jogo duas vezes nao duplica itens
- [ ] `./mvnw test` inclui teste que verifica dataset completo ao criar Jogo padrao

---

> Ver: `dataset/dataset-itens-default.md` para listagem completa dos 40 itens com todos os campos

---

*Produzido por: Business Analyst/PO | 2026-04-04*
