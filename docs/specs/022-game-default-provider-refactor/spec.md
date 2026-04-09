# Spec 022 — Refatoracao do GameDefaultConfigProvider

> Spec: `022-game-default-provider-refactor`
> Epic: Qualidade interna — manutenibilidade do provider de dados default
> Status: PLANEJADO — spec+plan+tasks PRONTOS, implementacao PENDENTE
> Depende de: nada (refatoracao interna, sem alteracao de API)
> Bloqueia: R07 Wave 1 (64 vantagens), futuras configs default
> Prioridade: P1 — bloqueante para R07

---

## 1. Visao Geral do Negocio

**Problema resolvido:** O `DefaultGameConfigProviderImpl` e um monolito de ~887 linhas que mistura 18+ tipos de configuracao num unico arquivo. 33 vantagens usam campos errados (`tipoBonus`, `custoBase`) ignorados pelo `GameConfigInitializerService.createVantagens()`. Editar dados de um tipo exige navegar centenas de linhas de outros tipos. Merge conflicts sao frequentes. Dois testes ja estao quebrados desde R06.

**Objetivo:** Extrair cada grupo de configuracao para um provider dedicado (`@Component`) dentro de um sub-pacote `config/defaults/`. O `DefaultGameConfigProviderImpl` vira uma thin facade que delega. As 64 vantagens sao preenchidas corretamente a partir do CSV canonico. Testes quebrados sao corrigidos e novos sao adicionados.

**Principio central:** Dados hardcoded em Java puro — sem leitura de CSV/YAML/JSON em runtime. Os CSVs em `docs/revisao-game-default/csv/` sao referencia de consulta do PO, nao fonte de dados do sistema.

**Valor entregue:**
- Cada tipo de config isolado em arquivo proprio (~50-350 linhas cada)
- Facade com ~80 linhas (apenas delegacoes)
- 64 vantagens com campos corretos, prontas para uso
- Testes verdes + 9 testes novos de invariantes
- Padrao claro para adicionar/remover/alterar configs default

---

## 2. Atores Envolvidos

| Ator | Role | Acoes |
|------|------|-------|
| Desenvolvedor | DEV | Edita providers individuais, roda testes |
| Copilot/Agente | DEV | Implementa tasks em paralelo (Wave 1 — 9 providers de vantagens) |
| Sistema | — | `GameConfigInitializerService` consome `GameDefaultConfigProvider` sem alteracao |

---

## 3. Arquitetura

### 3.1 Estrutura de Pacotes (resultado final)

```
config/
├── GameDefaultConfigProvider.java              ← interface (NAO MUDA)
├── DefaultGameConfigProviderImpl.java          ← thin facade (~80 linhas)
└── defaults/                                   ← NOVO sub-pacote
    ├── DefaultAtributosProvider.java           ← 7 atributos
    ├── DefaultAptidoesProvider.java            ← 24 aptidoes
    ├── DefaultNiveisProvider.java              ← 36 niveis + 5 limitadores
    ├── DefaultClassesProvider.java             ← 12 classes
    ├── DefaultRacasProvider.java               ← 6 racas + bonus raciais
    ├── DefaultProspeccoesProvider.java         ← 6 dados de prospeccao
    ├── DefaultConfigSimpleProvider.java        ← generos (3) + indoles (3) + presencas (4) + membros corpo (7)
    ├── DefaultBonusProvider.java               ← 9 bonus calculados
    ├── DefaultPontosVantagemProvider.java      ← 35 pontos de vantagem
    ├── DefaultVantagensProvider.java           ← 9 categorias + 64 vantagens
    └── DefaultItensProvider.java               ← 7 raridades + 20 tipos + 40 itens
```

### 3.2 Padrao de Cada Provider

Regras obrigatorias:

1. Classe `@Component` — Spring gerencia ciclo de vida
2. Sem estado mutavel — todos os metodos retornam `List.of()` / `Map.of()` imutaveis
3. Dados inline em Java puro — sem arquivo externo
4. Factory methods estaticos nos DTOs — usar `DTO.of(...)` ou `DTO.builder()...build()`
5. Comentarios de secao `// === Categoria ===` para agrupar itens

### 3.3 Padrao da Facade

```java
@Component
@RequiredArgsConstructor
public class DefaultGameConfigProviderImpl implements GameDefaultConfigProvider {

    private final DefaultAtributosProvider atributosProvider;
    private final DefaultAptidoesProvider aptidoesProvider;
    // ... 11 providers

    @Override
    public List<AtributoConfigDTO> getDefaultAtributos() {
        return atributosProvider.get();
    }
    // ... um delegate por metodo da interface
}
```

### 3.4 Padrao do DefaultVantagensProvider

O provider de vantagens e o mais complexo — 64 vantagens em 9 categorias:

- Metodo publico `getCategorias()` → 9 categorias
- Metodo publico `getVantagens()` → agrega 9 metodos privados `build{Categoria}()`
- Metodo helper privado `vantagem(sigla, nome, descricao, nivelMax, formulaCusto, efeito, tipo, categoria, ordem)` para reduzir boilerplate

---

## 4. Regras de Negocio

### RN-01 — Interface intocavel

A interface `GameDefaultConfigProvider` NAO DEVE ser alterada. Todos os metodos `getDefault*()` devem continuar existindo com mesma assinatura e mesmo comportamento.

### RN-02 — Initializer intocavel

`GameConfigInitializerService` NAO DEVE ser alterado. A refatoracao e transparente para os consumidores.

### RN-03 — Campos corretos de vantagens

Cada vantagem DEVE usar apenas os campos consumidos por `createVantagens()`:
- `sigla` — 2-5 chars, unica cross-entity por jogo. **REGRA: toda sigla de vantagem DEVE comecar com "V"** para evitar colisao com siglas de atributos/bonus
- `nome` — obrigatorio
- `descricao` — obrigatorio (texto completo do CSV, NAO abreviado)
- `nivelMaximoVantagem` — >= 1
- `formulaCusto` — exp4j valida (variaveis: `nivel`, `base`, `total`, siglas de atributos). **NAO usar** `custo_base` ou `nivel_vantagem`
- `valorBonusFormula` — texto livre (mapeado para `descricaoEfeito` na entity)
- `tipoVantagem` — `"VANTAGEM"` ou `"INSOLITUS"`
- `categoriaNome` — deve existir em `getDefaultCategoriasVantagem()`
- `nivelMinimoPersonagem` — >= 1
- `podeEvoluir` — `true` se nivelMaximo > 1
- `ordemExibicao` — sequencial global (1-64)

### RN-04 — Campos legados proibidos

Nenhuma vantagem DEVE usar `custoBase` ou `tipoBonus` — sao campos legados ignorados pelo initializer.

### RN-05 — Invariantes de INSOLITUS

Todas as 17 vantagens INSOLITUS DEVEM ter `formulaCusto = "0"`, `tipoVantagem = "INSOLITUS"`, `categoriaNome = "Vantagem Racial"`. Suas siglas tambem seguem o prefixo V (ex: `VENF`, `VIEF`, `VCAL`).

### RN-06 — CSV como referencia, nao como fonte

Os CSVs em `docs/revisao-game-default/csv/` sao referencia de consulta do PO. Os dados DEVEM estar hardcoded em Java. Nunca ler CSV em runtime. Os valores hardcoded DEVEM corresponder exatamente aos dados do CSV.

### RN-07 — Stubs vazios mantidos

Os metodos `getDefaultClassePontos()` e `getDefaultRacaPontos()` continuam retornando `Map.of()` vazio diretamente na facade, sem provider dedicado.

### RN-08 — Prefixo V obrigatorio para siglas de vantagens

Toda sigla de vantagem DEVE iniciar com a letra "V" (ex: `VTCO`, `VAA`, `VCFM`). Isso evita conflito com siglas de atributos (`FOR`, `AGI`, etc.) e bonus (`BBA`, `BBM`, etc.) que compartilham o mesmo namespace de unicidade cross-entity por jogo.

---

## 5. Dados Default — Referência Canônica (fonte: CSVs em `docs/revisao-game-default/csv/`)

> **REGRA GERAL**: Dados abaixo sao os valores exatos dos CSVs — devem ser copiados fielmente para o codigo Java hardcoded.
> Quando ha divergencia entre este doc e o CSV, o **CSV e a referencia canonica**. O codigo deve ser atualizado para refletir o CSV.
> Cada subsecao corresponde a um Provider em `config/defaults/`.

### Vantagens (64) — `DefaultVantagensProvider`

> **REGRA**: Toda sigla de vantagem comeca com V. Dados abaixo sao os valores exatos do CSV — devem ser copiados fielmente para o codigo Java hardcoded.

### 5.1 Treinamento Fisico (3)

| sigla | nome | descricao | nivelMax | formulaCusto | efeito |
|-------|------|-----------|----------|-------------|--------|
| VTCO | Treinamento em Combate Ofensivo | Treinamento especializado em técnicas ofensivas de combate físico. O dado é elevado até D10 onde um novo dado se inicia no D3. | 10 | 4 | +1 B.B.A e 1 dado de dano (D3→D.UP) por nivel |
| VTCD | Treinamento em Combate Defensivo | Treinamento especializado em técnicas defensivas. Os dados geram RD natural resistente a danos por contusão. | 10 | 4 | +1 Bloqueio e 1 dado de RD natural (D3→D.UP) por nivel |
| VTCE | Treinamento em Combate Evasivo | Treinamento especializado em evasão. Recebe 2 de bônus de Reflexo por nível ao invés de 1. | 10 | 2 | +2 Reflexo por nivel |

### 5.2 Treinamento Mental (4)

| sigla | nome | descricao | nivelMax | formulaCusto | efeito |
|-------|------|-----------|----------|-------------|--------|
| VTM | Treinamento Magico | Treinamento especializado em técnicas mágicas. O dado é elevado até D10 onde um novo dado se inicia no D3. | 10 | 4 | +1 B.B.M e 1 dado de dano magico (D3→D.UP) por nivel |
| VTPM | Treinamento em Percepcao Magica | Treinamento em percepção de auras e manifestações mágicas. Recebe 2 de bônus por nível. | 10 | 2 | +2 Percepcao por nivel |
| VTL | Treinamento Logico | Treinamento especializado em raciocínio lógico e dedutivo. Requer B.RAC 5+. | 5 | 4 | +1 B.B.M por nivel |
| VTMA | Treinamento em Manipulacao | Treinamento em técnicas mentais de manipulação. Requer B.B.M 8+. | 3 | 3 | +2 em Aptidoes Mentais por nivel |

### 5.3 Acao (2)

| sigla | nome | descricao | nivelMax | formulaCusto | efeito |
|-------|------|-----------|----------|-------------|--------|
| VAA | Ataque Adicional | Permite realizar um ataque extra após a ação ofensiva principal. Requer Bônus Ofensivo 15+. | 1 | 10 | Um ataque adicional apos a acao ofensiva |
| VAS | Ataque Sentai | Em ataque conjunto força percepção do alvo usando a maior soma dos atacantes. Requer Raciocínio 5+. | 1 | 10 | Ataque conjunto usa maior soma e forca percepcao do alvo |

### 5.4 Reacao (7)

| sigla | nome | descricao | nivelMax | formulaCusto | efeito |
|-------|------|-----------|----------|-------------|--------|
| VCA | Contra-Ataque | Pode reagir a um ataque atacando de volta com dificuldade +5. Requer Bônus Base 10+. | 1 | 5 | Reacao: atacar de volta com dificuldade +5 |
| VITC | Interceptacao | Interrompe uma ação antes dela de fato acontecer. Requer Bônus Base 10+. | 1 | 5 | Reacao: interrompe acao do oponente antes de ocorrer |
| VRE | Reflexos Especiais | Reações padrão podem ser executadas utilizando habilidades especiais. Requer B.REF ou P.MAG 30+. | 3 | 5 | Reacoes padrao executadas com habilidades por nivel |
| VIH | Instinto Heroico | Usar a ação padrão para salvar um aliado em perigo iminente. Requer Bônus Ofensivo 18+. | 1 | 5 | Acao padrao usada para salvar um aliado |
| VDH | Deflexao Heroica | Usar a ação padrão para salvar a si mesmo e outro com dificuldade +5. Requer Bônus Ofensivo 18+. | 1 | 5 | Acao padrao para salvar a si e outro com dificuldade +5 |
| VISB | Instinto de Sobrevivencia | Reduz somas de dificuldade para desviar ataques de múltiplos alvos. Requer Base Reflexos 7+. | 3 | 3 | -1 por nivel na dificuldade ao desviar de ataques multiplos |
| VRA | Reflexos Aprimorados | Reduz somas de dificuldade para reduzir dano pela metade. Requer Base Reflexos 7+. | 3 | 3 | -1 por nivel na dificuldade ao reduzir dano pela metade |

### 5.5 Vantagem de Atributo (8)

| sigla | nome | descricao | nivelMax | formulaCusto | efeito |
|-------|------|-----------|----------|-------------|--------|
| VCFM | Capacidade de Forca Maxima | Desbloqueia uso da Força máxima em danos por contusão. Desbloqueada a cada 10 em Força. | 1 | 6 | Concede 1D3 de dano por contusao com Forca maxima |
| VDM | Dominio de Forca | Eleva o dado de dano por contusão concedido pela Capacidade de Força Máxima. Requer CFM. | 6 | 2 | Eleva dado de dano por contusao 1x por nivel (D3→D4→...→D9) |
| VTEN | Tenacidade | Desbloqueia uso do Vigor máximo em RD por contusão. Desbloqueada a cada 10 em Vigor. | 1 | 6 | Concede 1D3 de RD por contusao com Vigor maximo |
| VDV | Dominio de Vigor | Eleva o dado de RD por contusão concedido pela Tenacidade. Requer Tenacidade. | 2 | 2 | Eleva dado de RD por contusao 1x por nivel (D3→D4→D5) |
| VDF | Destreza Felina | Reduz penalidades em locais e terrenos difíceis. Desbloqueada a cada 10 em Agilidade. | 1 | 5 | -1 em penalidade de local ou terreno dificil |
| VSG | Sabedoria de Gamaiel | Aumenta um aspecto mágico por nível. Aspectos: Dano, Defesa, Bônus, Duração ou Área. Desbloqueada a cada 10 em SAB. | 3 | 3 | +1 nivel em aspecto magico por nivel de vantagem |
| VSAG | Sentidos Agucados | Aguça um sentido específico concedendo bônus de percepção. Requer Sabedoria 3+. | 5 | 3 | +2 de Percepcao em 1 sentido especifico por nivel |
| VIN | Inteligencia de Nyck | Aumenta o multiplicador de raciocínio em 0.5x por nível. Requer Base de RAC 7+. | 3 | 2 | +0.5x no multiplicador de Raciocinio por nivel |

### 5.6 Vantagem Geral (5)

| sigla | nome | descricao | nivelMax | formulaCusto | efeito |
|-------|------|-----------|----------|-------------|--------|
| VSFE | Saude de Ferro | Aumenta os pontos de vida do personagem. Requer Vigor 3+. | 4 | 3 | +5 de Vida por nivel |
| VCON | Concentracao | Aumenta os pontos de animus (essência) do personagem. | 4 | 3 | +5 de Animus por nivel |
| VSRQ | Saque Rapido | Permite sacar armas sem gastar Ponto de Ação. Requer Agilidade 10+. | 2 | 3 | Saca armas sem custo de P.A por nivel |
| VAMB | Ambidestria | Remove penalidade de usar mão não dominante. Domínio bilateral de armas e ações. | 1 | 5 | Dominio bilateral sem penalidade para mao nao dominante |
| VMF | Memoria Fotografica | Possibilita memória visual plena de tudo que foi visto. Requer Raciocínio 10+. | 1 | 10 | Memoria visual completa e fotografica |

### 5.7 Vantagem Historica (7)

| sigla | nome | descricao | nivelMax | formulaCusto | efeito |
|-------|------|-----------|----------|-------------|--------|
| VHER | Heranca | O personagem herdou bens e recursos de família ou mentor. Rola 1D3 de Riqueza aplicada. | 1 | 5 | Rola 1D3 de Riqueza inicial aplicada |
| VRIQ | Riqueza | Representa acumulação de bens materiais e riquezas. Cada nível custa mais PN que o anterior. | 3 | nivel * 5 | +1 Grade de Riqueza por nivel (Nv1=5PN, Nv2=10PN, Nv3=15PN) |
| VIA | Indole Aplicada | Permite alterar a índole do personagem em relação a um alvo adicional por nível. | 5 | 2 | Muda indole com 1 alvo adicional por nivel |
| VOFI | Oficios | O personagem conhece profissões e ofícios variados do mundo. | 2 | 2 | +1 Profissao ou Oficio conhecida por nivel |
| VTOF | Treino de Oficio | Aprimora a performance do personagem no exercício de ofícios e profissões. | 10 | 4 | +1 por nivel em testes para exercer oficios |
| VVO | Vinculo com Organizacao | O personagem possui influência e contatos em uma organização com dezenas de membros. | 3 | 7 | Influencia em organizacao com dezenas de membros por nivel |
| VCAP | Capangas | O personagem possui aliados ou capangas leais que o seguem. | 5 | 5 | +1 aliado ou capanga leal por nivel |

### 5.8 Vantagem de Renascimento (11)

| sigla | nome | descricao | nivelMax | formulaCusto | efeito |
|-------|------|-----------|----------|-------------|--------|
| VCDA | Controle de Dano | Permite rolar o dado de dano e decidir quanto efetivamente aplicar. Requer 1 Renascimento. | 1 | 5 | Decide quanto do dano rolado aplicar ao alvo |
| VUSI | Ultimo Sigilo | Oculta completamente a manifestação visual e sensorial de habilidades mágicas. Requer 1 Renascimento. | 1 | 5 | Oculta manifestacao magica de habilidades |
| VESC | Escaramuca | Permite realizar ataques falsos e manobras avançadas de combate. Requer 1 Renascimento. | 3 | 3 | Ataques falsos e manobras avancadas de combate por nivel |
| VPCO | Previsao em Combate | Antecipa ações em combate afetando Defesa, Ofensiva ou Reatividade. Requer 2 Renascimentos. | 3 | 15 | Por nivel: +1 em Defesa, Ofensiva ou Reatividade (escolha) |
| VAI | Armas Improvisadas | Permite usar armas improvisadas com eficácia crescente. Requer 1 Renascimento. | 10 | 4 | +1 B.B.A e 1 dado (D3→D.UP) por nivel com armas improvisadas |
| VDNL | Dano Nao Letal | Converte todos os danos causados em danos não letais por contusão. Requer 1 Renascimento. | 1 | 5 | Converte danos para tipo contusao (nao letal) |
| VAEC | Acao em Cadeia | Permite agir e atacar durante o Ataque Adicional no mesmo turno. Requer 1 Renascimento e Ataque Adicional. | 1 | 10 | Agir e atacar durante o Ataque Adicional |
| VATD | Atencao Difusa | Expande o raio de atenção ao redor do personagem em 1 metro por nível. Requer 1 Renascimento. | 10 | 5 | +1 metro de raio de atencao ao redor por nivel |
| VSNM | Senso Numerico | Habilidade de precisão numérica excepcional para cálculos instantâneos. Requer 1 Renascimento. | 1 | 10 | Precisao numerica instantanea em qualquer calculo |
| VPBF | Pensamento Bifurcado | Permite executar ações Independente e Padrão simultaneamente no mesmo turno. Requer 1 Renascimento. | 1 | 10 | Executa acoes Independente e Padrao ao mesmo tempo |
| VMEI | Memoria Eidetica | Lembra de experiências com detalhes completos de todos os sentidos. Requer 1 Renascimento. | 1 | 10 | Memoria completa e precisa com todos os sentidos |

### 5.9 Vantagem Racial — INSOLITUS (17)

Todas com `tipoVantagem = "INSOLITUS"`, `categoriaNome = "Vantagem Racial"`, `formulaCusto = "0"`, `nivelMaximoVantagem = 1` (exceto VCAL = 3).

| sigla | nome | descricao | raca | nivelMax |
|-------|------|-----------|------|----------|
| VENF | Elemento Natural: Fogo | Os Karzarcryer possuem afinidade elemental inata com o fogo. Seu sangue draconiano reage ao fogo como fonte de vida, convertendo exposição ao calor extremo em energia vital. | Karzarcryer | 1 |
| VIEF | Imunidade Elemental: Fogo | Herança draconiana confere imunidade total a qualquer forma de dano do elemento fogo, sejam chamas naturais, feitiços ígneos ou ambientes vulcânicos. | Karzarcryer | 1 |
| VESD | Estomago de Dragao | O sistema digestivo dos Karzarcryer pode processar substâncias tóxicas, ígneas ou corrosivas sem qualquer efeito adverso. Podem consumir metal fundido, venenos ou compostos ácidos. | Karzarcryer | 1 |
| VASA | Membro Adicional: Asas | Os Ikarúz possuem asas funcionais que permitem voo sustentado. Apesar do nanismo corporal, suas asas são proporcionalmente grandes e poderosas. | Ikaruz | 1 |
| VADA | Adaptacao Atmosferica | Biologicamente adaptados a qualquer altitude e pressão atmosférica, os Ikarúz não sofrem penalidades por ambiente de altitude extrema, pressão reduzida ou ventos violentos. | Ikaruz | 1 |
| VCAL | Combate Alado | Técnicas de combate que exploram a mobilidade aérea única dos Ikarúz. Utilizam asas como armas auxiliares e para manobras evasivas em combate. | Ikaruz | **3** |
| VPIR | Piercings Raciais | Os Hankráz nascem com piercings de metal rúnico integrados ao corpo. Estes piercings amplificam capacidades sobrenaturais e funcionam como condutores de energia mística. | Hankraz | 1 |
| VCEG | Corpo Esguio | A estrutura corporal extremamente delgada dos Hankráz confere vantagens naturais em furtividade, passagens estreitas e esquiva de ataques. | Hankraz | 1 |
| VVEM | Vagante entre Mundos | Habilidade única dos Hankráz de perceber e, em alguns casos, acessar brevemente planos de existência paralelos. Podem sentir ecos de outros mundos e criaturas planares. | Hankraz | 1 |
| VAHU | Adaptabilidade Humana | Os humanos são mestres da adaptação. Esta vantagem reflete sua capacidade única de desenvolver proficiência em qualquer área de conhecimento ou habilidade. | Humano | 1 |
| VRHU | Resiliencia Humana | A força de vontade humana permite recuperação acelerada de condições adversas como veneno, doenças, fadiga e condições debilitantes. | Humano | 1 |
| VVHU | Versatilidade Humana | Os humanos não possuem restrições raciais no aprendizado. Podem aprender vantagens de qualquer categoria sem pré-requisitos relacionados à raça ou linhagem. | Humano | 1 |
| VEIN | Espirito Inabalavel | A mente humana possui resistência natural a influências sobrenaturais. Efeitos de medo, encantamento e manipulação mental têm eficácia reduzida contra humanos. | Humano | 1 |
| VLCI | Legado de Civilizacao | Os humanos construíram as maiores civilizações de Klayrah. Esta herança confere habilidades naturais em negociação, diplomacia e navegação em estruturas sociais. | Humano | 1 |
| VANA | Armas Naturais Aprimoradas | Os Anakarys possuem garras, presas e membros naturais de combate aprimorados. Suas armas naturais são mais afiadas e poderosas que as de outras raças. | Anakarys | 1 |
| VDES | Deslocamento Especial | Os Anakarys possuem um modo único de movimento — escalar, rastejar ou saltar com eficiência sobrenatural, navegando terrenos intransponíveis para outras raças. | Anakarys | 1 |
| VAAR | Ataque Adicional Racial | Versão racial e limitada do Ataque Adicional. Os Anakarys podem realizar um ataque extra por turno, mas apenas com armas naturais e somente contra alvos de tamanho igual ou menor. | Anakarys | 1 |

---

### 5.10 Atributos (7) — `DefaultAtributosProvider`

> CSV fonte: `08-atributo-config.csv`

| sigla | nome | descricao | formulaImpeto | unidadeImpeto | valorMinimo | valorMaximo | ordem |
|-------|------|-----------|---------------|---------------|-------------|-------------|-------|
| FOR | Força | Capacidade física bruta, determina capacidade de carga | total * 3 | kg | 1 | 120 | 1 |
| AGI | Agilidade | Velocidade e reflexos, determina deslocamento | total / 3 | metros | 1 | 120 | 2 |
| VIG | Vigor | Resistência física, redução de dano físico | total / 10 | RD | 1 | 120 | 3 |
| SAB | Sabedoria | Resistência mágica, redução de dano mágico | total / 10 | RDM | 1 | 120 | 4 |
| INTU | Intuição | Sorte e percepção instintiva, pontos de sorte | min(total / 20, 3) | pontos | 1 | 120 | 5 |
| INT | Inteligência | Capacidade de comando e raciocínio | total / 20 | comando | 1 | 120 | 6 |
| AST | Astúcia | Pensamento estratégico e tático | total / 10 | estratégia | 1 | 120 | 7 |

**Invariantes:**
- 7 siglas unicas, 2-4 chars
- Siglas cross-entity: NAO devem colidir com siglas de bonus (BBA, BBM...) nem vantagens (V...)
- `formulaImpeto` usa variavel `total` (valor do atributo)

---

### 5.11 Bonus Calculados (9) — `DefaultBonusProvider`

> CSV fonte: `09-bonus-config.csv`

| sigla | nome | formulaBase | descricao | ordem |
|-------|------|-------------|-----------|-------|
| BBA | B.B.A | (FOR + AGI) / 3 | Bônus base de ataque físico; derivado da Força e Agilidade | 1 |
| BBM | B.B.M | (SAB + INT) / 3 | Bônus base de ação mental ou mágica; derivado da Sabedoria e Inteligência | 2 |
| DEF | Defesa | VIG / 5 | Redução passiva de dano físico recebido; derivada do Vigor | 3 |
| ESQ | Esquiva | AGI / 5 | Valor de referência para desviar de ataques; derivado da Agilidade | 4 |
| INI | Iniciativa | INTU / 5 | Determina a ordem de ação em combate; derivada da Intuição | 5 |
| PER | Percepção | INTU / 3 | Capacidade de notar detalhes, ameaças e pistas no ambiente; derivada da Intuição | 6 |
| RAC | Raciocínio | INT / 3 | Qualidade do pensamento analítico e resolução de problemas; derivado da Inteligência | 7 |
| BLO | Bloqueio | VIG / 3 | Capacidade de absorver impactos com escudo ou arma; derivado do Vigor | 8 |
| REF | Reflexo | AGI / 3 | Velocidade de reação a eventos imprevistos ou ataques surpresa; derivado da Agilidade | 9 |

**Invariantes:**
- 9 siglas unicas, 2-3 chars
- `formulaBase` usa APENAS siglas de atributos de 5.10 (FOR, AGI, VIG, SAB, INTU, INT, AST)
- Siglas cross-entity: NAO devem colidir com siglas de atributos nem vantagens

---

### 5.12 Aptidoes (24) — `DefaultAptidoesProvider`

> CSV fonte: `13-aptidao-config.csv` + `01-tipo-aptidao.csv`

**Tipos de Aptidao:** `FISICA`, `MENTAL`

#### Aptidoes Fisicas (12)

| nome | tipo | descricao | ordem |
|------|------|-----------|-------|
| Acrobacia | FISICA | Execução de manobras ágeis como saltos, rolamentos e equilíbrio em terreno difícil | 1 |
| Guarda | FISICA | Técnica defensiva de posicionar corpo e armas para absorver ou redirecionar impactos | 2 |
| Aparar | FISICA | Desviar ou neutralizar ataques com arma ou escudo com precisão de timing | 3 |
| Atletismo | FISICA | Força física bruta aplicada a escalada, arremesso, luta corporal e resistência de esforço | 4 |
| Resvalar | FISICA | Técnica de esquiva dinâmica que usa o movimento do corpo para evitar golpes e projéteis | 5 |
| Resistência | FISICA | Capacidade de suportar condições extremas: fome, veneno, dor, temperatura e exaustão | 6 |
| Perseguição | FISICA | Habilidade de rastrear e perseguir alvos em movimento, ou de fugir eficientemente | 7 |
| Natação | FISICA | Habilidade de nadar e se mover em ambientes aquáticos, incluindo mergulho e combate na água | 8 |
| Furtividade | FISICA | Habilidade de mover-se silenciosamente, esconder-se e realizar ações sem ser detectado | 9 |
| Prestidigitação | FISICA | Destreza manual para realizar truques, pickpocket, esconder objetos e manipulação fina | 10 |
| Conduzir | FISICA | Habilidade de montar animais ou pilotar veículos, incluindo manobras em alta velocidade | 11 |
| Arte da Fuga | FISICA | Habilidade de escapar de amarras, algemas, prisões e outras situações de captura | 12 |

#### Aptidoes Mentais (12)

| nome | tipo | descricao | ordem |
|------|------|-----------|-------|
| Idiomas | MENTAL | Conhecimento de idiomas estrangeiros, dialetos e sistemas de escrita do mundo de Klayrah | 13 |
| Observação | MENTAL | Percepção aguçada para notar detalhes, pistas ocultas e anomalias no ambiente | 14 |
| Falsificar | MENTAL | Habilidade de criar documentos falsos, imitar assinaturas e forjar selos oficiais | 15 |
| Prontidão | MENTAL | Estado de alerta elevado; evita ser surpreendido e age rapidamente em situações de crise | 16 |
| Auto Controle | MENTAL | Domínio das emoções e resistência à manipulação, medo, sedução e pressão psicológica | 17 |
| Sentir Motivação | MENTAL | Habilidade de perceber as verdadeiras intenções e emoções ocultas de outras pessoas | 18 |
| Sobrevivência | MENTAL | Conhecimento de orientação, caça, armadilhas e sobrevivência em ambientes hostis | 19 |
| Investigar | MENTAL | Habilidade de reunir pistas, interrogar testemunhas e deduzir conclusões a partir de evidências | 20 |
| Blefar | MENTAL | Capacidade de mentir convincentemente, criar desvios e manipular a percepção alheia | 21 |
| Atuação | MENTAL | Habilidade de interpretar personagens, disfarçar-se e convencer por meio de performance | 22 |
| Diplomacia | MENTAL | Arte da negociação, persuasão e mediação de conflitos com argumentos e charme | 23 |
| Operação de Mecanismos | MENTAL | Habilidade de operar, reparar e arrombar mecanismos, fechaduras e engenhocas | 24 |

**Invariantes:**
- 24 aptidoes: 12 FISICA + 12 MENTAL
- `tipo_aptidao_nome` deve existir em TipoAptidao (FISICA ou MENTAL)
- Nomes unicos

---

### 5.13 Niveis (36) — `DefaultNiveisProvider`

> CSV fonte: `11-nivel-config.csv`

#### Regras de Progressao

- **Formula XP:** `xp(N) = N*(N+1)/2 * 1000` (triangular)
- **Pontos por nivel:** 3 atributo + 1 aptidao por nivel (exceto nivel 0)
- **Nivel 0:** Estado base — XP=0, todos os pontos=0, limitador=10

#### Tiers de Limitador

| Faixa | Limitador | Descricao |
|-------|-----------|-----------|
| 0-1 | 10 | Tutorial — atributos muito restritos |
| 2-20 | 50 | Progressao normal — faixa principal do jogo |
| 21-25 | 75 | Tier intermediario — personagens veteranos |
| 26-30 | 100 | Tier avancado — herois de elite |
| 31-35 | 120 | Tier de renascimento — personagens lendarios |

#### Tabela Completa

| nivel | xpNecessaria | pontosAtributo | pontosAptidao | limitadorAtributo |
|-------|-------------|----------------|---------------|-------------------|
| 0 | 0 | 0 | 0 | 10 |
| 1 | 1000 | 3 | 1 | 10 |
| 2 | 3000 | 3 | 1 | 50 |
| 3 | 6000 | 3 | 1 | 50 |
| 4 | 10000 | 3 | 1 | 50 |
| 5 | 15000 | 3 | 1 | 50 |
| 6 | 21000 | 3 | 1 | 50 |
| 7 | 28000 | 3 | 1 | 50 |
| 8 | 36000 | 3 | 1 | 50 |
| 9 | 45000 | 3 | 1 | 50 |
| 10 | 55000 | 3 | 1 | 50 |
| 11 | 66000 | 3 | 1 | 50 |
| 12 | 78000 | 3 | 1 | 50 |
| 13 | 91000 | 3 | 1 | 50 |
| 14 | 105000 | 3 | 1 | 50 |
| 15 | 120000 | 3 | 1 | 50 |
| 16 | 136000 | 3 | 1 | 50 |
| 17 | 153000 | 3 | 1 | 50 |
| 18 | 171000 | 3 | 1 | 50 |
| 19 | 190000 | 3 | 1 | 50 |
| 20 | 210000 | 3 | 1 | 50 |
| 21 | 231000 | 3 | 1 | 75 |
| 22 | 253000 | 3 | 1 | 75 |
| 23 | 276000 | 3 | 1 | 75 |
| 24 | 300000 | 3 | 1 | 75 |
| 25 | 325000 | 3 | 1 | 75 |
| 26 | 351000 | 3 | 1 | 100 |
| 27 | 378000 | 3 | 1 | 100 |
| 28 | 406000 | 3 | 1 | 100 |
| 29 | 435000 | 3 | 1 | 100 |
| 30 | 465000 | 3 | 1 | 100 |
| 31 | 496000 | 3 | 1 | 120 |
| 32 | 528000 | 3 | 1 | 120 |
| 33 | 561000 | 3 | 1 | 120 |
| 34 | 595000 | 3 | 1 | 120 |
| 35 | 630000 | 3 | 1 | 120 |

**Invariantes:**
- 36 niveis (0-35)
- XP estritamente crescente
- Nivel 0 tem todos os pontos = 0
- `pontosAtributo` = 3 e `pontosAptidao` = 1 para niveis >= 1

---

### 5.14 Pontos de Vantagem por Nivel (35) — `DefaultPontosVantagemProvider`

> CSV fonte: `12-pontos-vantagem-config.csv`
> Apenas niveis com pontos > 0 sao milestone. Demais niveis concedem 0 pontos.

#### Milestones

| nivel | pontosGanhos | acumulado |
|-------|-------------|-----------|
| 1 | 6 | 6 |
| 5 | 3 | 9 |
| 10 | 10 | 19 |
| 15 | 3 | 22 |
| 20 | 10 | 32 |
| 25 | 3 | 35 |
| 30 | 15 | 50 |
| 35 | 3 | 53 |

#### Tabela Completa (todos os niveis)

| nivel | pontosGanhos |
|-------|-------------|
| 1 | 6 |
| 2 | 0 |
| 3 | 0 |
| 4 | 0 |
| 5 | 3 |
| 6 | 0 |
| 7 | 0 |
| 8 | 0 |
| 9 | 0 |
| 10 | 10 |
| 11 | 0 |
| 12 | 0 |
| 13 | 0 |
| 14 | 0 |
| 15 | 3 |
| 16 | 0 |
| 17 | 0 |
| 18 | 0 |
| 19 | 0 |
| 20 | 10 |
| 21 | 0 |
| 22 | 0 |
| 23 | 0 |
| 24 | 0 |
| 25 | 3 |
| 26 | 0 |
| 27 | 0 |
| 28 | 0 |
| 29 | 0 |
| 30 | 15 |
| 31 | 0 |
| 32 | 0 |
| 33 | 0 |
| 34 | 0 |
| 35 | 3 |

**Invariantes:**
- 35 entradas (niveis 1-35, nivel 0 nao tem pontos)
- Total acumulado: 53 pontos de vantagem no nivel 35
- Padrao de milestones: niveis 1, 5, 10, 15, 20, 25, 30, 35

---

### 5.15 Classes (12) — `DefaultClassesProvider`

> CSV fonte: `15-classe-personagem.csv`

| nome | descricao | ordem |
|------|-----------|-------|
| Guerreiro | Especialista em combate corpo a corpo | 1 |
| Arqueiro | Mestre em combate à distância | 2 |
| Monge | Lutador desarmado com disciplina espiritual | 3 |
| Berserker | Guerreiro selvagem de fúria incontrolável | 4 |
| Assassino | Especialista em ataques furtivos e letais | 5 |
| Fauno (Herdeiro) | Herdeiro com poderes especiais | 6 |
| Mago | Conjurador de magias arcanas | 7 |
| Feiticeiro | Usuário de magia inata | 8 |
| Necromante | Manipulador de forças da morte | 9 |
| Sacerdote | Servo divino com poderes sagrados | 10 |
| Ladrão | Especialista em subterfúgio e furto | 11 |
| Negociante | Mestre em comércio e persuasão | 12 |

**Sub-entidades de Classe (TODO — a ser preenchido pelo PO):**
- `15b-classe-bonus.csv` — bonus de derivados por classe (vazio)
- `15c-classe-aptidao-bonus.csv` — aptidoes bonus por classe (vazio)
- `15d-classe-pontos-config.csv` — pontos por nivel por classe (vazio)
- `15e-classe-vantagem-predefinida.csv` — vantagens pre-definidas por classe (vazio)

**Invariantes:**
- 12 classes com nomes unicos
- `ordemExibicao` sequencial 1-12

---

### 5.16 Racas (6) — `DefaultRacasProvider`

> CSV fonte: `16-raca.csv` + `16b-raca-bonus-atributo.csv` + `16e-raca-vantagem-predefinida.csv`

#### 5.16.1 Racas Base

| nome | descricao | ordem |
|------|-----------|-------|
| Humano | Raça versátil e adaptável, capaz de aprender qualquer arte ou ofício. Sua maior força é a adaptabilidade e a capacidade de superação. Não possuem vantagens raciais físicas marcantes, mas compensam com 5 vantagens especiais que refletem a resiliência e o potencial ilimitado da humanidade. | 1 |
| Karzarcryer | Descendentes de dragões do plano do fogo, os Karzarcryer possuem escamas ígnicas e sangue quente. Bônus: +8 Resistência (+24 VIG), -3 Percepção (-9 INTU). São conhecidos por seu temperamento explosivo (Antecedente: Falta de autocontrole) e pela Ignomia inicial 5, que representa sua reputação de destruição instintiva. Dominam o elemento fogo e possuem resistência sobrenatural ao calor. | 2 |
| Ikarúz | Raça de seres alados com afinidade à sabedoria celestial. Possuem asas funcionais e adaptação a diferentes altitudes. Bônus: +5 Sabedoria (+5 SAB), +3 Percepção (+9 INTU), -3 Resistência (-9 VIG). Antecedente: Nanismo — apesar das asas grandiosas, seu corpo é pequeno e delicado, tornando-os vulneráveis em combate corpo a corpo. | 3 |
| Hankráz | Seres esguios de inteligência aguçada que habitam entre planos de existência paralelos. Seus piercings mágicos amplificam capacidades sobrenaturais. Bônus: +5 Inteligência (+5 INT), +3 Percepção (+9 INTU), -3 Resistência (-9 VIG). Antecedente: Baixo vigor — corpos frágeis que compensam com mente analítica e habilidades multidimensionais. | 4 |
| Atlas | Gigantes de força incomparável, os Atlas são a raça mais fisicamente poderosa de Klayrah. Bônus: +8 Força (+8 FOR), -3 Inteligência (-3 INT). Antecedente: É burro — já representado pela penalidade em INT. São guerreiros natos mas carecem de sofisticação intelectual. Possuem capacidade de força máxima inata, ambidestria natural e um ataque adicional devastador. | 5 |
| Anakarys | Raça ágil de predadores naturais com garras, presas e instintos aguçados. Bônus: +3 Agilidade (+3 AGI), +2 Percepção (+6 INTU). Possuem armas naturais aprimoradas, formas únicas de deslocamento e um ataque adicional racial com restrições específicas de uso. | 6 |

#### 5.16.2 Bonus Raciais de Atributo

> Mapeamento de derivados: "Percepção" (PER = INTU/3) → bonus em INTU. Ex: +3 Percepção = +9 INTU.
> "Resistência" (≈ Bloqueio = VIG/3) → bonus em VIG. Ex: +8 Resistência = +24 VIG.

| raca | atributoSigla | bonus | nota |
|------|--------------|-------|------|
| Humano | — | — | Sem bonus/penalidades (compensado pelas 5 vantagens raciais) |
| Karzarcryer | VIG | +24 | +8 Resistência |
| Karzarcryer | INTU | -9 | -3 Percepção |
| Ikarúz | SAB | +5 | +5 Sabedoria |
| Ikarúz | INTU | +9 | +3 Percepção |
| Ikarúz | VIG | -9 | -3 Resistência |
| Hankráz | INT | +5 | +5 Inteligência |
| Hankráz | INTU | +9 | +3 Percepção |
| Hankráz | VIG | -9 | -3 Resistência |
| Atlas | FOR | +8 | +8 Força |
| Atlas | INT | -3 | -3 Inteligência |
| Anakarys | AGI | +3 | +3 Agilidade |
| Anakarys | INTU | +6 | +2 Percepção |

#### 5.16.3 Vantagens Pre-definidas por Raca

| raca | vantagemNome | nivelInicial |
|------|-------------|-------------|
| Humano | Adaptabilidade Humana | 1 |
| Humano | Resiliência Humana | 1 |
| Humano | Versatilidade Humana | 1 |
| Humano | Espírito Inabalável | 1 |
| Humano | Legado de Civilização | 1 |
| Karzarcryer | Elemento Natural: Fogo | 1 |
| Karzarcryer | Imunidade Elemental: Fogo | 1 |
| Karzarcryer | Estômago de Dragão | 1 |
| Ikarúz | Membro Adicional: Asas | 1 |
| Ikarúz | Adaptação Atmosférica | 1 |
| Ikarúz | Combate Alado | 1 |
| Hankráz | Piercings Raciais | 1 |
| Hankráz | Corpo Esguio | 1 |
| Hankráz | Vagante entre Mundos | 1 |
| Atlas | Capacidade de Força Máxima | 1 |
| Atlas | Ambidestria | 1 |
| Atlas | Ataque Adicional | 1 |
| Anakarys | Armas Naturais Aprimoradas | 1 |
| Anakarys | Deslocamento Especial | 1 |
| Anakarys | Ataque Adicional Racial | 1 |

> **NOTA:** Atlas usa vantagens JA EXISTENTES no sistema (VCFM, VAMB, VAA) — nao sao INSOLITUS.

**Sub-entidades de Raca (TODO — a ser preenchido pelo PO):**
- `16c-raca-classe-permitida.csv` — classes permitidas por raca (vazio = todas permitidas)
- `16d-raca-pontos-config.csv` — pontos adicionais por nivel por raca (vazio)

**Invariantes:**
- 6 racas com nomes unicos
- Cada raca tem 3 vantagens pre-definidas (exceto Humano que tem 5)
- Bonus de atributo podem ser negativos
- `atributoSigla` deve existir em 5.10

---

### 5.17 Dados de Prospeccao (6) — `DefaultProspeccoesProvider`

> CSV fonte: `05-dado-prospeccao-config.csv`

| nome | numLados | descricao | ordem |
|------|----------|-----------|-------|
| d3 | 3 | Dado de 3 faces; incerteza mínima, usado em situações triviais ou de baixíssimo risco | 1 |
| d4 | 4 | Dado de 4 faces; pequena variação, para situações simples e controladas | 2 |
| d6 | 6 | Dado de 6 faces; o dado padrão do sistema, para situações cotidianas e moderadas | 3 |
| d8 | 8 | Dado de 8 faces; dificuldade moderada, para desafios com risco real | 4 |
| d10 | 10 | Dado de 10 faces; alta dificuldade, para situações arriscadas ou complexas | 5 |
| d12 | 12 | Dado de 12 faces; extremo ou raro, reservado para proezas épicas e eventos críticos | 6 |

**Invariantes:**
- 6 dados com `numLados` crescente (3, 4, 6, 8, 10, 12)
- Nomes unicos no formato `d{N}`

---

### 5.18 Configuracoes Simples — `DefaultConfigSimpleProvider`

#### 5.18.1 Generos (3)

> CSV fonte: `02-genero-config.csv`

| nome | descricao | ordem |
|------|-----------|-------|
| Masculino | Personagem de identidade masculina | 1 |
| Feminino | Personagem de identidade feminina | 2 |
| Outro | Personagem com identidade de gênero não binária ou indefinida | 3 |

#### 5.18.2 Indoles (3)

> CSV fonte: `03-indole-config.csv`

| nome | descricao | ordem |
|------|-----------|-------|
| Bom | Movido por compaixão e altruísmo; tende a ajudar os necessitados e defender os fracos | 1 |
| Mau | Guiado por ambição cruel ou egoísmo; usa os outros como meios para seus próprios fins | 2 |
| Neutro | Sem viés moral definido; age conforme as circunstâncias, nem bondoso nem cruel | 3 |

#### 5.18.3 Presencas (4)

> CSV fonte: `04-presenca-config.csv`

| nome | descricao | ordem |
|------|-----------|-------|
| Bom | Aura de benevolência e proteção; aqueles próximos sentem conforto e confiança | 1 |
| Leal | Aura de ordem e autoridade; transmite disciplina e respeito pelas leis e hierarquias | 2 |
| Caótico | Aura imprevisível e perturbadora; semeia instabilidade e desconforto ao redor | 3 |
| Neutro | Aura equilibrada, sem inclinação evidente; passa despercebido pela maioria | 4 |

#### 5.18.4 Membros do Corpo (7)

> CSV fonte: `10-membro-corpo-config.csv`
> `porcentagemVida`: BigDecimal de 0.01 a 1.00 — pool de VT do membro como fracao da VT total.
> Ordens 7 e 8 estao disponiveis intencionalmente para o Mestre adicionar Pescoço e Coração.

| nome | porcentagemVida | descricao | ordem |
|------|----------------|-----------|-------|
| Cabeça | 0.75 | Ao chegar a 0 VT, o personagem fica incapacitado; pode resultar em morte instantânea ou estado vegetativo dependendo do dano excedente | 1 |
| Tronco | 0.35 | Ao chegar a 0 VT, o personagem entra em colapso e fica totalmente incapaz de agir | 2 |
| Braço Direito | 0.10 | Ao chegar a 0 VT, o membro fica inutilizado; ações que o exigem sofrem penalidade grave | 3 |
| Braço Esquerdo | 0.10 | Ao chegar a 0 VT, o membro fica inutilizado; ações que o exigem sofrem penalidade grave | 4 |
| Perna Direita | 0.10 | Ao chegar a 0 VT, a mobilidade é severamente reduzida; penalidade no deslocamento e ações de esquiva | 5 |
| Perna Esquerda | 0.10 | Ao chegar a 0 VT, a mobilidade é severamente reduzida; penalidade no deslocamento e ações de esquiva | 6 |
| Sangue | 1.00 | Representa o pool total de vida do personagem; ao chegar a 0 o personagem morre por sangramento crítico | 9 |

**Invariantes (ConfigSimple):**
- 3 generos, 3 indoles, 4 presencas, 7 membros do corpo
- Nomes unicos dentro de cada tipo
- `porcentagemVida` entre 0.01 e 1.00

---

### 5.19 Categorias de Vantagem (9) — `DefaultVantagensProvider.getCategorias()`

> CSV fonte: `06-categoria-vantagem.csv`

| nome | cor | descricao | ordem |
|------|-----|-----------|-------|
| Treinamento Físico | #e74c3c | | 1 |
| Treinamento Mental | #8e44ad | | 2 |
| Ação | #e67e22 | | 3 |
| Reação | #27ae60 | | 4 |
| Vantagem de Atributo | #2980b9 | | 5 |
| Vantagem Geral | #95a5a6 | | 6 |
| Vantagem Histórica | #f39c12 | | 7 |
| Vantagem de Renascimento | #1abc9c | | 8 |
| Vantagem Racial | #7f8c8d | Vantagens concedidas por herança racial, não compráveis com pontos de vantagem | 9 |

**Invariantes:**
- 9 categorias com nomes unicos
- Toda `categoriaNome` em vantagens (5.1-5.9) deve existir aqui
- Cores em formato hex `#RRGGBB`

---

### 5.20 Raridades de Item (7) — `DefaultItensProvider.getRaridades()`

> CSV fonte: `07-raridade-item-config.csv`

| nome | cor | ordem | podeJogadorAdicionar | bonusAtribMin | bonusAtribMax | bonusDeriMin | bonusDeriMax | descricao |
|------|-----|-------|---------------------|---------------|---------------|-------------|-------------|-----------|
| Comum | #9d9d9d | 1 | true | 0 | 0 | 0 | 0 | Itens mundanos sem encantamento |
| Incomum | #1eff00 | 2 | false | 1 | 1 | 1 | 1 | Levemente encantado ou de qualidade excepcional |
| Raro | #0070dd | 3 | false | 1 | 2 | 1 | 2 | Encantamento moderado, raramente encontrado |
| Muito Raro | #a335ee | 4 | false | 2 | 3 | 2 | 3 | Encantamento poderoso, obra de artesão mestre |
| Épico | #ff8000 | 5 | false | 3 | 4 | 3 | 4 | Artefato de grande poder, história própria |
| Lendário | #e6cc80 | 6 | false | 4 | 5 | 4 | 5 | Um dos poucos existentes no mundo |
| Único | #e268a8 | 7 | false | 0 | 0 | 0 | 0 | Criação única do Mestre, sem referência de custo |

**Invariantes:**
- 7 raridades com nomes unicos
- Apenas `Comum` tem `podeJogadorAdicionar = true`
- Ranges de bonus crescentes (exceto Unico que e 0/0)

---

### 5.21 Tipos de Item (20) — `DefaultItensProvider.getTipos()`

> CSV fonte: `14-tipo-item-config.csv`

| nome | categoria | subcategoria | requerDuasMaos | ordem |
|------|-----------|-------------|----------------|-------|
| Espada Curta | ARMA | ESPADA | false | 1 |
| Espada Longa | ARMA | ESPADA | false | 2 |
| Espada Dupla | ARMA | ESPADA | true | 3 |
| Arco Curto | ARMA | ARCO | true | 4 |
| Arco Longo | ARMA | ARCO | true | 5 |
| Adaga | ARMA | ADAGA | false | 6 |
| Machado de Batalha | ARMA | MACHADO | false | 7 |
| Machado Grande | ARMA | MACHADO | true | 8 |
| Martelo de Guerra | ARMA | MARTELO | false | 9 |
| Cajado | ARMA | CAJADO | true | 10 |
| Lança | ARMA | LANCA | false | 11 |
| Armadura Leve | ARMADURA | ARMADURA_LEVE | false | 12 |
| Armadura Média | ARMADURA | ARMADURA_MEDIA | false | 13 |
| Armadura Pesada | ARMADURA | ARMADURA_PESADA | false | 14 |
| Escudo | ARMADURA | ESCUDO | false | 15 |
| Anel | ACESSORIO | ANEL | false | 16 |
| Amuleto | ACESSORIO | AMULETO | false | 17 |
| Poção | CONSUMIVEL | POCAO | false | 18 |
| Munição | CONSUMIVEL | MUNICAO | false | 19 |
| Equipamento de Aventura | AVENTURA | OUTROS | false | 20 |

**Categorias validas:** ARMA, ARMADURA, ACESSORIO, CONSUMIVEL, FERRAMENTA, AVENTURA
**Subcategorias validas:** ESPADA, ARCO, LANCA, MACHADO, MARTELO, CAJADO, ADAGA, ARREMESSO, BESTA, ARMADURA_LEVE, ARMADURA_MEDIA, ARMADURA_PESADA, ESCUDO, ANEL, AMULETO, BOTAS, CAPA, LUVAS, POCAO, MUNICAO, KIT, OUTROS

**Invariantes:**
- 20 tipos com nomes unicos
- 11 ARMA + 4 ARMADURA + 2 ACESSORIO + 2 CONSUMIVEL + 1 AVENTURA

---

### 5.22 Itens (40) — `DefaultItensProvider.getItens()`

> CSV fonte: `18-item-config.csv` + `18b-item-efeito.csv` + `18c-item-requisito.csv`

#### 5.22.1 Armas (15)

| nome | raridadeNome | tipoNome | peso | valor | duracaoPadrao | nivelMinimo | propriedades | ordem |
|------|-------------|----------|------|-------|--------------|-------------|-------------|-------|
| Adaga | Comum | Adaga | 0.45 | 2 | | 1 | finura, arremesso, leve | 1 |
| Espada Curta | Comum | Espada Curta | 0.90 | 10 | | 1 | finura, leve | 2 |
| Espada Longa | Comum | Espada Longa | 1.36 | 15 | | 1 | versátil | 3 |
| Espada Longa +1 | Incomum | Espada Longa | 1.36 | 500 | 10 | 1 | versátil, mágica | 4 |
| Espada Longa +2 | Raro | Espada Longa | 1.36 | 5000 | 15 | 5 | versátil, mágica | 5 |
| Machadinha | Comum | Machado de Batalha | 0.90 | 5 | | 1 | leve, arremesso | 6 |
| Machado de Batalha | Comum | Machado de Batalha | 1.80 | 10 | | 1 | versátil | 7 |
| Machado Grande | Comum | Machado Grande | 3.17 | 30 | | 3 | pesado, duas mãos | 8 |
| Martelo de Guerra | Comum | Martelo de Guerra | 2.27 | 15 | | 1 | versátil | 9 |
| Arco Curto | Comum | Arco Curto | 0.90 | 25 | | 1 | duas mãos, munição | 10 |
| Arco Longo | Comum | Arco Longo | 1.80 | 50 | | 2 | duas mãos, munição, pesado | 11 |
| Arco Longo +1 | Incomum | Arco Longo | 1.80 | 500 | 10 | 4 | duas mãos, munição, mágico | 12 |
| Cajado de Madeira | Comum | Cajado | 1.80 | 5 | | 1 | versátil, duas mãos | 13 |
| Cajado Arcano +1 | Incomum | Cajado | 2.00 | 500 | 10 | 3 | mágico, foco arcano | 14 |
| Lança | Comum | Lança | 1.36 | 1 | | 1 | arremesso, versátil | 15 |

#### 5.22.2 Armaduras e Escudos (10)

| nome | raridadeNome | tipoNome | peso | valor | duracaoPadrao | nivelMinimo | propriedades | ordem |
|------|-------------|----------|------|-------|--------------|-------------|-------------|-------|
| Gibão de Couro | Comum | Armadura Leve | 4.50 | 10 | | 1 | armadura leve | 16 |
| Couro Batido | Comum | Armadura Leve | 11.30 | 45 | | 1 | armadura leve | 17 |
| Camisão de Malha | Comum | Armadura Média | 13.60 | 50 | | 2 | armadura média | 18 |
| Cota de Escamas | Comum | Armadura Média | 20.40 | 50 | | 3 | armadura média, desvantagem Furtividade | 19 |
| Cota de Malha | Comum | Armadura Pesada | 27.20 | 75 | | 4 | armadura pesada, Força mínima | 20 |
| Meia Placa | Comum | Armadura Pesada | 19.90 | 750 | | 5 | armadura pesada | 21 |
| Placa Completa | Raro | Armadura Pesada | 29.50 | 1500 | 15 | 7 | armadura pesada, mágica | 22 |
| Escudo de Madeira | Comum | Escudo | 2.72 | 10 | | 1 | escudo | 23 |
| Escudo de Aço | Comum | Escudo | 2.72 | 20 | | 1 | escudo | 24 |
| Escudo Enfeitiçado +1 | Incomum | Escudo | 2.72 | 500 | 10 | 3 | escudo, mágico | 25 |

#### 5.22.3 Acessorios e Itens Magicos (5)

| nome | raridadeNome | tipoNome | peso | valor | duracaoPadrao | nivelMinimo | propriedades | ordem |
|------|-------------|----------|------|-------|--------------|-------------|-------------|-------|
| Anel da Força +1 | Raro | Anel | 0.01 | 2000 | | 5 | mágico, único | 26 |
| Anel de Proteção +1 | Raro | Anel | 0.01 | 2000 | | 5 | mágico | 27 |
| Amuleto de Saúde | Incomum | Amuleto | 0.05 | 500 | | 3 | mágico | 28 |
| Amuleto da Essência | Incomum | Amuleto | 0.05 | 500 | | 3 | mágico | 29 |
| Manto de Elvenkind | Muito Raro | Amuleto | 0.45 | 5000 | | 7 | mágico | 30 |

#### 5.22.4 Consumiveis (5)

| nome | raridadeNome | tipoNome | peso | valor | duracaoPadrao | nivelMinimo | propriedades | ordem |
|------|-------------|----------|------|-------|--------------|-------------|-------------|-------|
| Poção de Cura Menor | Comum | Poção | 0.45 | 25 | 1 | 1 | consumível, recupera 5 de vida | 31 |
| Poção de Cura | Comum | Poção | 0.45 | 50 | 1 | 1 | consumível, recupera 10 de vida | 32 |
| Poção de Cura Superior | Incomum | Poção | 0.45 | 200 | 1 | 3 | consumível, recupera 25 de vida | 33 |
| Flecha Comum (20) | Comum | Munição | 0.45 | 1 | | 1 | munição para arcos | 34 |
| Virote (20) | Comum | Munição | 0.36 | 1 | | 1 | munição para bestas | 35 |

#### 5.22.5 Equipamentos de Aventura (5)

| nome | raridadeNome | tipoNome | peso | valor | duracaoPadrao | nivelMinimo | propriedades | ordem |
|------|-------------|----------|------|-------|--------------|-------------|-------------|-------|
| Kit de Aventureiro | Comum | Equipamento de Aventura | 12.00 | 12 | | 1 | mochila, ração 10 dias, corda, archote | 36 |
| Kit de Curandeiro | Comum | Equipamento de Aventura | 1.50 | 5 | 10 | 1 | 10 usos de bandagem, 5 usos de antídoto | 37 |
| Kit de Ladrão | Comum | Equipamento de Aventura | 0.90 | 25 | | 1 | ferramentas de ladrão, forçado VIG para abrir fechaduras | 38 |
| Lanterna Bullseye | Comum | Equipamento de Aventura | 1.00 | 10 | | 1 | iluminação direcional 18m, 6h de óleo | 39 |
| Tomo Arcano | Comum | Equipamento de Aventura | 1.50 | 25 | | 1 | livro de feitiços para Magos e Feiticeiros | 40 |

#### 5.22.6 Efeitos de Itens

> CSV fonte: `18b-item-efeito.csv`

| itemNome | tipoEfeito | bonusNome | atributoSigla | valorFixo | descricaoEfeito |
|----------|-----------|-----------|---------------|-----------|-----------------|
| Espada Longa +1 | BONUS_DERIVADO | B.B.A | | 1 | |
| Espada Longa +2 | BONUS_DERIVADO | B.B.A | | 2 | |
| Arco Longo +1 | BONUS_DERIVADO | B.B.A | | 1 | |
| Cajado Arcano +1 | BONUS_DERIVADO | B.B.M | | 1 | |
| Gibão de Couro | BONUS_DERIVADO | Defesa | | 1 | |
| Couro Batido | BONUS_DERIVADO | Defesa | | 2 | |
| Camisão de Malha | BONUS_DERIVADO | Defesa | | 3 | |
| Cota de Escamas | BONUS_DERIVADO | Defesa | | 4 | |
| Cota de Malha | BONUS_DERIVADO | Defesa | | 5 | |
| Meia Placa | BONUS_DERIVADO | Defesa | | 5 | |
| Meia Placa | BONUS_DERIVADO | Reflexo | | 1 | |
| Placa Completa | BONUS_DERIVADO | Defesa | | 6 | |
| Escudo de Madeira | BONUS_DERIVADO | Bloqueio | | 1 | |
| Escudo de Aço | BONUS_DERIVADO | Bloqueio | | 2 | |
| Escudo Enfeitiçado +1 | BONUS_DERIVADO | Bloqueio | | 2 | |
| Escudo Enfeitiçado +1 | BONUS_DERIVADO | Defesa | | 1 | |
| Anel da Força +1 | BONUS_ATRIBUTO | | FOR | 1 | |
| Anel de Proteção +1 | BONUS_DERIVADO | Defesa | | 1 | |
| Anel de Proteção +1 | BONUS_DERIVADO | Bloqueio | | 1 | |
| Amuleto de Saúde | BONUS_VIDA | | | 5 | |
| Amuleto da Essência | BONUS_ESSENCIA | | | 5 | |
| Manto de Elvenkind | BONUS_DERIVADO | Esquiva | | 3 | |
| Manto de Elvenkind | BONUS_DERIVADO | Percepção | | 2 | |
| Poção de Cura Menor | BONUS_VIDA | | | 5 | Recupera 5 pontos de vida ao ser consumida |
| Poção de Cura | BONUS_VIDA | | | 10 | Recupera 10 pontos de vida ao ser consumida |
| Poção de Cura Superior | BONUS_VIDA | | | 25 | Recupera 25 pontos de vida ao ser consumida |
| Kit de Curandeiro | FORMULA_CUSTOMIZADA | | | | Bônus em testes de medicina e primeiros socorros |
| Kit de Ladrão | FORMULA_CUSTOMIZADA | | | | Bônus em testes de furtividade e arrombamento |
| Tomo Arcano | BONUS_ESSENCIA | | | 2 | Potencializa a reserva arcana de magos e feiticeiros |

**Tipos de efeito validos:** BONUS_ATRIBUTO, BONUS_APTIDAO, BONUS_DERIVADO, BONUS_VIDA, BONUS_ESSENCIA, FORMULA_CUSTOMIZADA, EFEITO_DADO

#### 5.22.7 Requisitos de Itens

> CSV fonte: `18c-item-requisito.csv`

| itemNome | tipoRequisito | alvo | valorMinimo |
|----------|--------------|------|-------------|
| Cota de Escamas | ATRIBUTO | FOR | 12 |
| Cota de Malha | ATRIBUTO | FOR | 15 |
| Meia Placa | ATRIBUTO | FOR | 15 |
| Placa Completa | ATRIBUTO | FOR | 17 |
| Machado Grande | ATRIBUTO | FOR | 13 |
| Arco Longo | ATRIBUTO | FOR | 12 |
| Cajado Arcano +1 | ATRIBUTO | INT | 12 |

**Tipos de requisito validos:** NIVEL, ATRIBUTO, BONUS, APTIDAO, VANTAGEM, CLASSE, RACA

**Invariantes (Itens):**
- 40 itens com nomes unicos: 15 armas + 10 armaduras/escudos + 5 acessorios + 5 consumiveis + 5 aventura
- `raridadeNome` deve existir em 5.20
- `tipoNome` deve existir em 5.21
- Itens podem ter multiplos efeitos (ex: Meia Placa tem 2)

---

### 5.23 Pre-requisitos entre Vantagens

> CSV fonte: `17c-vantagem-prerequisito.csv`

| vantagemNome | requisitoNome | nivelMinimo |
|-------------|--------------|-------------|
| Domínio de Força | Capacidade de Força Máxima | 1 |
| Domínio de Vigor | Tenacidade | 1 |
| Ação em Cadeia | Ataque Adicional | 1 |

**Nota:** Pre-requisitos de bonus/atributos minimos (ex: BBA 5+) sao tratados em mecanica separada, nao nesta tabela.

---

## 6. Testes

### 6.1 Testes Quebrados (corrigir)

| Teste | Problema | Correcao |
|-------|----------|----------|
| T5-02 | `hasSize(8)` para pontos de vantagem | `hasSize(35)` |
| T5-09 | `hasSize(8)` para categorias, falta "Vantagem Racial" | `hasSize(9)` + incluir "Vantagem Racial" |

### 6.2 Testes Novos

| ID | Descricao |
|----|-----------|
| T5-11 | `getDefaultVantagens()` retorna exatamente 64 vantagens |
| T5-12 | Siglas das vantagens sao unicas, tem 2-5 caracteres e comecam com V |
| T5-13 | Todos os INSOLITUS tem formulaCusto = "0" |
| T5-14 | Todas as `categoriaNome` existem em `getDefaultCategoriasVantagem()` |
| T5-15 | Nenhuma formulaCusto usa "custo_base" (variavel invalida) |
| T5-16 | 7 atributos com abreviacoes unicas |
| T5-17 | 6 racas com nomes unicos |
| T5-18 | 36 niveis (0-35) com XP crescente |
| T5-19 | 40 itens com raridade e tipo existentes |

---

## 7. Fonte de Verdade dos Dados

Os CSVs em `docs/revisao-game-default/csv/` sao referencia canonica para o PO consultar e editar:

| CSV | Config |
|-----|--------|
| `01-tipo-aptidao.csv` | TipoAptidao |
| `02-genero-config.csv` | GeneroConfig |
| `03-indole-config.csv` | IndoleConfig |
| `04-presenca-config.csv` | PresencaConfig |
| `05-dado-prospeccao-config.csv` | DadoProspeccaoConfig |
| `06-categoria-vantagem.csv` | CategoriaVantagem |
| `07-raridade-item-config.csv` | RaridadeItemConfig |
| `08-atributo-config.csv` | AtributoConfig |
| `09-bonus-config.csv` | BonusConfig |
| `10-membro-corpo-config.csv` | MembroCorpoConfig |
| `11-nivel-config.csv` | NivelConfig |
| `12-pontos-vantagem-config.csv` | PontosVantagemConfig |
| `13-aptidao-config.csv` | AptidaoConfig |
| `14-tipo-item-config.csv` | TipoItemConfig |
| `15-classe-personagem.csv` | ClassePersonagem |
| `16-raca.csv` | Raca + bonus |
| `17-vantagem-config.csv` | VantagemConfig (64 vantagens) |
| `18-item-config.csv` | ItemConfig + efeitos |

Quando ha divergencia entre CSV e codigo Java, o **CSV e a referencia canonica**. O codigo deve ser atualizado para refletir o CSV.

---

## 8. Como Editar Configuracoes (guia para desenvolvedores)

### Adicionar/remover/alterar um Atributo
1. Abrir `DefaultAtributosProvider.java`
2. Adicionar/remover/editar entrada na lista `get()`
3. Se alterar `sigla`, verificar unicidade cross-entity (nao colidir com bonus, vantagens)
4. Atualizar teste T5-16 (contagem = 7)
5. Rodar `./mvnw test -Dtest=DefaultGameConfigProviderImplTest`

### Adicionar/remover/alterar um Bonus Calculado
1. Abrir `DefaultBonusProvider.java`
2. Adicionar/remover/editar entrada na lista `get()`
3. `formulaBase` deve usar apenas siglas de atributos existentes
4. Se alterar `sigla`, verificar unicidade cross-entity

### Adicionar/remover/alterar uma Aptidao
1. Abrir `DefaultAptidoesProvider.java`
2. Adicionar/editar entrada na lista `get()`
3. `tipo_aptidao_nome` deve ser `FISICA` ou `MENTAL`
4. Rodar `./mvnw test -Dtest=DefaultGameConfigProviderImplTest`

### Adicionar/remover/alterar um Nivel
1. Abrir `DefaultNiveisProvider.java`
2. Adicionar/editar entrada em `getNiveis()`
3. Manter XP estritamente crescente e `nivel` sequencial
4. Se adicionar nivel, tambem adicionar em `DefaultPontosVantagemProvider`
5. Atualizar teste T5-18 (contagem = 36, XP crescente)

### Adicionar/remover/alterar uma Classe
1. Abrir `DefaultClassesProvider.java`
2. Adicionar/editar entrada na lista `get()`
3. Nomes devem ser unicos

### Adicionar/remover/alterar uma Raca
1. Abrir `DefaultRacasProvider.java`
2. Para a raca base: editar `getRacas()`
3. Para bonus raciais: editar `getBonusRaciais()` — `atributoSigla` deve existir em atributos
4. Para vantagens pre-definidas: editar a lista correspondente
5. Atualizar teste T5-17 (contagem = 6)

### Adicionar/remover/alterar um Item
1. Abrir `DefaultItensProvider.java`
2. Para o item: editar `getItens()`
3. `raridadeNome` deve existir em `getRaridades()`
4. `tipoNome` deve existir em `getTipos()`
5. Atualizar teste T5-19 (contagem = 40)

### Adicionar/remover/alterar uma Config Simples (genero, indole, presenca, membro do corpo)
1. Abrir `DefaultConfigSimpleProvider.java`
2. Editar o metodo correspondente: `getGeneros()`, `getIndoles()`, `getPresencas()`, `getMembrosCorpo()`
3. `porcentagemVida` (membros) deve ser BigDecimal entre 0.01 e 1.00

### Adicionar uma nova vantagem
1. Abrir `DefaultVantagensProvider.java`
2. Encontrar o metodo `build{Categoria}()` correto
3. Adicionar entrada com helper `vantagem(...)` — **sigla DEVE comecar com V**
4. Incrementar `ordemExibicao` (sequencial global)
5. Rodar `./mvnw test -Dtest=DefaultGameConfigProviderImplTest`

### Remover uma vantagem
1. Deletar a entrada do `build{Categoria}()` correspondente
2. NAO precisa ajustar `ordemExibicao` dos restantes (gaps aceitos)
3. Atualizar teste T5-11 (contagem total)

### Alterar campos de uma vantagem
1. Editar diretamente no `build{Categoria}()` correspondente
2. Se alterar `sigla`, verificar unicidade cross-entity **e manter prefixo V**
3. Se alterar `categoriaNome`, confirmar que existe em `getCategorias()`

### Adicionar novo tipo de config default
1. Criar `Default{Nome}Provider.java` em `config/defaults/`
2. Adicionar metodo na interface `GameDefaultConfigProvider`
3. Adicionar delegacao em `DefaultGameConfigProviderImpl`
4. Implementar em `GameConfigInitializerService.initializeGameConfigs()`
5. Adicionar testes em `DefaultGameConfigProviderImplTest`

---

## 9. Decisoes de Design

### Por que nao carregar de CSV/YAML em runtime?
- Simplicidade — codigo Java e type-safe, IDE ajuda com refactoring
- Performance — sem I/O de arquivo na inicializacao
- Testabilidade — testar e instanciar a classe, sem mock de filesystem
- Rastreabilidade — git diff mostra exatamente o que mudou

### Por que composicao e nao heranca?
- Providers sao independentes, nao compartilham estado
- Composicao permite injetar/mockar providers individuais nos testes
- Sem acoplamento entre configs de tipos diferentes

### Por que `@Component` e nao classes estaticas?
- Spring DI permite substituir providers em testes ou customizacoes
- `@Primary` permite que o Mestre crie seu proprio provider sem alterar o original
- Consistente com o pattern do projeto (tudo gerenciado pelo Spring)

---

## 10. Riscos e Mitigacoes

| Risco | Impacto | Mitigacao |
|-------|---------|-----------|
| Testes quebram durante migracao | Medio | Migrar um provider por vez, rodar testes apos cada |
| `GameConfigInitializerService` quebra | Alto | NAO alterar interface `GameDefaultConfigProvider` |
| Dados copiados com erro | Medio | Testes de invariantes (T5-11 a T5-19) pegam divergencias |
| Provider esquecido na facade | Baixo | `@RequiredArgsConstructor` + `private final` = erro no startup |

---

*Produzido por: Tech Lead / Copilot | 2026-04-09*
