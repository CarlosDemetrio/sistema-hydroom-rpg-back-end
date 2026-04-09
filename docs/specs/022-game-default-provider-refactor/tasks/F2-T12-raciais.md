# F2-T12 — buildRaciais() · 17 vantagens INSOLITUS

## Objetivo
Preencher `buildRaciais()` no `DefaultVantagensProvider`.

## Arquivo modificado
`config/defaults/DefaultVantagensProvider.java`

## Regras especiais
- **Todas** com `tipoVantagem = "INSOLITUS"`
- **Todas** com `categoriaNome = "Vantagem Racial"`
- **Todas** com `formulaCusto = "0"`
- **Todas** com `nivelMaximoVantagem = 1` **EXCETO** VCAL = 3

## Dados (fonte: `17-vantagem-config.csv` — valores exatos, hardcoded)

| ordem | sigla | nome | descricao | nivelMax | raca |
|-------|-------|------|-----------|----------|------|
| 48 | VENF | Elemento Natural: Fogo | Os Karzarcryer possuem afinidade elemental inata com o fogo. Seu sangue draconiano reage ao fogo como fonte de vida, convertendo exposição ao calor extremo em energia vital. | 1 | Karzarcryer |
| 49 | VIEF | Imunidade Elemental: Fogo | Herança draconiana confere imunidade total a qualquer forma de dano do elemento fogo, sejam chamas naturais, feitiços ígneos ou ambientes vulcânicos. | 1 | Karzarcryer |
| 50 | VESD | Estomago de Dragao | O sistema digestivo dos Karzarcryer pode processar substâncias tóxicas, ígneas ou corrosivas sem qualquer efeito adverso. Podem consumir metal fundido, venenos ou compostos ácidos. | 1 | Karzarcryer |
| 51 | VASA | Membro Adicional: Asas | Os Ikarúz possuem asas funcionais que permitem voo sustentado. Apesar do nanismo corporal, suas asas são proporcionalmente grandes e poderosas. | 1 | Ikaruz |
| 52 | VADA | Adaptacao Atmosferica | Biologicamente adaptados a qualquer altitude e pressão atmosférica, os Ikarúz não sofrem penalidades por ambiente de altitude extrema, pressão reduzida ou ventos violentos. | 1 | Ikaruz |
| 53 | VCAL | Combate Alado | Técnicas de combate que exploram a mobilidade aérea única dos Ikarúz. Utilizam asas como armas auxiliares e para manobras evasivas em combate. | **3** | Ikaruz |
| 54 | VPIR | Piercings Raciais | Os Hankráz nascem com piercings de metal rúnico integrados ao corpo. Estes piercings amplificam capacidades sobrenaturais e funcionam como condutores de energia mística. | 1 | Hankraz |
| 55 | VCEG | Corpo Esguio | A estrutura corporal extremamente delgada dos Hankráz confere vantagens naturais em furtividade, passagens estreitas e esquiva de ataques. | 1 | Hankraz |
| 56 | VVEM | Vagante entre Mundos | Habilidade única dos Hankráz de perceber e, em alguns casos, acessar brevemente planos de existência paralelos. Podem sentir ecos de outros mundos e criaturas planares. | 1 | Hankraz |
| 57 | VAHU | Adaptabilidade Humana | Os humanos são mestres da adaptação. Esta vantagem reflete sua capacidade única de desenvolver proficiência em qualquer área de conhecimento ou habilidade. | 1 | Humano |
| 58 | VRHU | Resiliencia Humana | A força de vontade humana permite recuperação acelerada de condições adversas como veneno, doenças, fadiga e condições debilitantes. | 1 | Humano |
| 59 | VVHU | Versatilidade Humana | Os humanos não possuem restrições raciais no aprendizado. Podem aprender vantagens de qualquer categoria sem pré-requisitos relacionados à raça ou linhagem. | 1 | Humano |
| 60 | VEIN | Espirito Inabalavel | A mente humana possui resistência natural a influências sobrenaturais. Efeitos de medo, encantamento e manipulação mental têm eficácia reduzida contra humanos. | 1 | Humano |
| 61 | VLCI | Legado de Civilizacao | Os humanos construíram as maiores civilizações de Klayrah. Esta herança confere habilidades naturais em negociação, diplomacia e navegação em estruturas sociais. | 1 | Humano |
| 62 | VANA | Armas Naturais Aprimoradas | Os Anakarys possuem garras, presas e membros naturais de combate aprimorados. Suas armas naturais são mais afiadas e poderosas que as de outras raças. | 1 | Anakarys |
| 63 | VDES | Deslocamento Especial | Os Anakarys possuem um modo único de movimento — escalar, rastejar ou saltar com eficiência sobrenatural, navegando terrenos intransponíveis para outras raças. | 1 | Anakarys |
| 64 | VAAR | Ataque Adicional Racial | Versão racial e limitada do Ataque Adicional. Os Anakarys podem realizar um ataque extra por turno, mas apenas com armas naturais e somente contra alvos de tamanho igual ou menor. | 1 | Anakarys |

> ⚠️ VCAL (Combate Alado) tem `nivelMaximoVantagem = 3` — unica excecao.
> ⚠️ Todas as siglas de vantagens começam com V (RN-08).

## Commit
```
feat(defaults): vantagens Raciais INSOLITUS (Karzarcryer, Ikaruz, Hankraz, Humano, Anakarys) [Copilot R07 T12]
```
