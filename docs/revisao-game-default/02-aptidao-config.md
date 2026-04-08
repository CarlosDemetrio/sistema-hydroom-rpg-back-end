# AptidaoConfig (+ TipoAptidao)

> Aptidões que o personagem pode evoluir (ex: Atletismo, Furtividade, Diplomacia). Separadas em Físicas e Mentais.

---

## Entidade: `TipoAptidao`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nome` | String | ✅ | — |
| `descricao` | String | — | — |
| `ordemExibicao` | Integer | — | `0` |

> ⚠️ **TipoAptidao é criado diretamente no `GameConfigInitializerService`**, não passa pelo provider. Hardcoded: FISICA (ordem 1) e MENTAL (ordem 2).

---

## Entidade: `AptidaoConfig`

| campo | tipo | obrigatório | default |
|---|---|---|---|
| `nome` | String(50) | ✅ | — |
| `tipoAptidao` | FK → TipoAptidao | — | — | resolvido por nome no initializer |
| `descricao` | String(500) | — | — |
| `ordemExibicao` | Integer | — | `0` |

---

## DTO atual: `AptidaoConfigDTO`

| campo DTO | campo entidade | status |
|---|---|---|
| `nome` | `nome` | ✅ populado |
| `tipo` (string "FISICA"/"MENTAL") | `tipoAptidao` (FK) | ✅ resolvido pelo initializer |
| `descricao` | `descricao` | ✅ populado |
| `ordemExibicao` | `ordemExibicao` | ✅ populado |

---

## Dados atuais no provider

**FISICA (12):**

| nome | descricao | ordem |
|---|---|---|
| Acrobacia | Habilidade de realizar manobras acrobáticas | 1 |
| Guarda | Capacidade de defesa e bloqueio | 2 |
| Aparar | Habilidade de desviar ataques | 3 |
| Atletismo | Força física e condicionamento | 4 |
| Resvalar | Capacidade de esquiva e movimento ágil | 5 |
| Resistência | Capacidade de resistir a condições adversas | 6 |
| Perseguição | Habilidade de perseguir ou fugir | 7 |
| Natação | Capacidade de nadar e manobras aquáticas | 8 |
| Furtividade | Capacidade de se mover sem ser detectado | 9 |
| Prestidigitação | Destreza manual e truques | 10 |
| Conduzir | Habilidade de pilotar veículos e montarias | 11 |
| Arte da Fuga | Capacidade de escapar de restrições | 12 |

**MENTAL (12):**

| nome | descricao | ordem |
|---|---|---|
| Idiomas | Conhecimento de línguas | 13 |
| Observação | Capacidade de notar detalhes | 14 |
| Falsificar | Habilidade de criar falsificações | 15 |
| Prontidão | Capacidade de reagir rapidamente | 16 |
| Auto Controle | Controle emocional e mental | 17 |
| Sentir Motivação | Capacidade de ler intenções | 18 |
| Sobrevivência | Conhecimento de técnicas de sobrevivência | 19 |
| Investigar | Habilidade de coletar e analisar informações | 20 |
| Blefar | Capacidade de enganar e mentir | 21 |
| Atuação | Habilidade de interpretar personagens | 22 |
| Diplomacia | Capacidade de negociação e persuasão | 23 |
| Operação de Mecanismos | Conhecimento de dispositivos mecânicos | 24 |

---

## O que falta / revisar

- [ ] **TipoAptidao hardcoded** — não customizável sem editar o serviço. Considerar mover para o provider se quiser flexibilidade.
- [ ] **Lista de aptidões** — as 24 atuais representam bem o sistema Klayrah? Adicionar, remover ou renomear alguma?
- [ ] **Descrições** — revisar se as descrições estão adequadas ao sistema
- [ ] **Atributo base** — existe relação aptidão→atributo? Não aparece na entidade mas pode ser necessário para cálculos de teste
