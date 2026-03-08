# Siglas, Abreviações e Sistema de Fórmulas

> 📖 Parte 4 do Glossário. Voltar ao [índice](../GLOSSARIO.md).

---

## Por que tudo tem sigla?

No Klayrah RPG, praticamente toda configuração pode ter uma **sigla ou abreviação** (campo `abreviacao` nos atributos, `sigla` nas vantagens, etc.). Isso não é apenas uma conveniência visual — é uma peça fundamental da arquitetura do sistema.

O sistema permite que o Mestre crie **fórmulas matemáticas personalizáveis** em diversas partes do jogo. Essas fórmulas usam as siglas como **variáveis**. É assim que o sistema consegue ser totalmente configurável sem código hardcoded.

## ⚠️ Regra crítica: sigla EXCLUSIVA por jogo

**Toda sigla/abreviação deve ser ÚNICA dentro de um mesmo Jogo, independente do tipo de configuração.** Se o atributo "Força" já usa a sigla `FOR`, nenhuma vantagem, bônus, aptidão ou qualquer outra configuração daquele jogo pode usar `FOR`. Isso é obrigatório porque o motor de fórmulas resolve variáveis por nome — se duas configurações diferentes tivessem a mesma sigla, o sistema não saberia qual valor usar, gerando cálculos incorretos ou ambíguos.

> ⚠️ **Para a IA**: ao implementar validações de criação/edição de qualquer configuração que tenha sigla ou abreviação, SEMPRE verificar unicidade da sigla **no escopo do jogo inteiro** (cross-entity), não apenas dentro da mesma tabela.

## Como funciona

1. O Mestre cria um atributo "Força" com abreviação `FOR`
2. O Mestre cria um bônus "B.B.A" com fórmula `FLOOR((FOR + AGI) / 3)`
3. Quando o sistema calcula o B.B.A de um personagem, ele substitui `FOR` e `AGI` pelos valores totais daqueles atributos na ficha e avalia a expressão
4. Se outra configuração usasse `FOR` como sigla, o motor não saberia se usar o valor do atributo ou da outra configuração — por isso a unicidade é obrigatória

## Onde fórmulas são usadas

| Onde | Exemplo de fórmula | Variáveis disponíveis |
|------|---------------------|-----------------------|
| **Ímpeto de atributo** | `TOTAL * 3` (Força → carga em kg) | `TOTAL` (valor total do atributo) |
| **Bônus calculado** | `FLOOR((FOR + AGI) / 3)` (B.B.A) | Abreviações de todos os atributos do jogo (`FOR`, `AGI`, `VIG`, `SAB`, `INT`, `INTU`, `AST`) |
| **Custo de vantagem** | `CUSTO_BASE * NIVEL_VANTAGEM` | `CUSTO_BASE`, `NIVEL_VANTAGEM` |
| **Vida total** | `VIGOR + NIVEL + VANTAGENS + RENASCIMENTOS + OUTROS` | Componentes de vida |
| **Essência total** | `FLOOR((VIGOR + SABEDORIA) / 2) + NIVEL + RENASCIMENTOS + VANTAGENS + OUTROS` | Componentes de essência |
| **Ameaça** | `NIVEL + ITENS + TITULOS + RENASCIMENTOS + OUTROS` | Componentes de ameaça |

## Funções matemáticas disponíveis

O motor de fórmulas (exp4j) suporta: `FLOOR()`, `CEIL()`, `MIN()`, `MAX()`, `ABS()`, `SQRT()` e operadores `+`, `-`, `*`, `/`, `^`, `%`.

## Tabela de siglas padrão (Template Klayrah)

| Sigla | Significado | Tipo |
|-------|-------------|------|
| `FOR` | Força | Atributo |
| `AGI` | Agilidade | Atributo |
| `VIG` | Vigor | Atributo |
| `SAB` | Sabedoria | Atributo |
| `INT` | Inteligência | Atributo |
| `INTU` | Intuição | Atributo |
| `AST` | Astúcia | Atributo |
| `TCO` | Treinamento em Combate Ofensivo | Vantagem |
| `TCD` | Treinamento em Combate Defensivo | Vantagem |
| `TCE` | Treinamento em Combate Evasivo | Vantagem |
| `TM` | Treinamento Mágico | Vantagem |
| `TPM` | Treinamento em Percepção Mágica | Vantagem |
| `TL` | Treinamento Lógico | Vantagem |
| `T.M` | Treinamento em Manipulação | Vantagem |
| `CFM` | Capacidade de Força Máxima | Vantagem de Atributo |
| `DM` | Domínio de Força | Vantagem de Atributo |
| `DF` | Destreza Felina | Vantagem de Atributo |
| `DV` | Domínio de Vigor | Vantagem de Atributo |
| `SG` | Sabedoria de Gamaiel | Vantagem de Atributo |
| `IN` | Inteligência de Nyck | Vantagem de Atributo |
| `B.B.A` | Bônus Base de Ataque | Bônus |
| `B.B.M` | Bônus Base Mágico | Bônus |
| `RD` | Redução de Dano | Derivado (Ímpeto do Vigor) |
| `RDM` | Redução de Dano Mágico | Derivado (Ímpeto da Sabedoria) |
| `VG` | Vida do Vigor | Componente de Vida |
| `VT` | Vida de Vantagens | Componente de Vida |
| `D.UP` | Dado Up (progressão de dado) | Mecânica de progressão |
| `NVS` | Nível de Vida Superior | Progressão |

> **Importante para a IA**: quando o Mestre cria um atributo ou vantagem nova, ele define a sigla. Essa sigla pode então ser usada em fórmulas de outros elementos. Todo o sistema é interligado por essas referências textuais. **A sigla DEVE ser única por jogo, cross-entity** (não pode existir a mesma sigla em atributos, bônus, vantagens ou qualquer outra configuração do mesmo jogo).

---

*Última atualização: Março 2026*
