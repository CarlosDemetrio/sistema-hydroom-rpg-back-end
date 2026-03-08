# Glossário e Contextualização — Klayrah RPG

> 📖 **LEIA PRIMEIRO**: documento de referência para IA (Claude, Copilot) e desenvolvedores.
> Explica **o que é**, **para que serve** e **como se relaciona** cada conceito do sistema Klayrah RPG.

---

## Resumo do Projeto

**Klayrah RPG** é um sistema de RPG de mesa com regras próprias. O **Ficha Controlador** é a aplicação web que digitaliza fichas de personagem com cálculos automáticos, validações e controle de acesso.

- **Mestre** — narrador/administrador que cria o Jogo, define regras e gerencia participantes
- **Jogador** — participante que cria e controla seu personagem
- **Princípio central**: tudo configurável pelo Mestre, nada hardcoded

---

## Referência Rápida de Siglas

| Sigla | Significado |
|-------|-------------|
| **B.B.A** | Bônus Base de Ataque |
| **B.B.M** | Bônus Base Mágico |
| **RD** | Redução de Dano (físico) |
| **RDM** | Redução de Dano Mágico |
| **VG** | Vida do Vigor |
| **VT** | Vida de Vantagens |
| **XP** | Experiência (pontos de progressão) |
| **D.UP** | Dado Up (progressão de dado) |
| **TCO/TCD/TCE** | Treinamentos de Combate (Ofensivo/Defensivo/Evasivo) |
| **TM** | Treinamento Mágico |
| **CFM** | Capacidade de Força Máxima |

> ⚠️ **Regra crítica**: toda sigla/abreviação deve ser **EXCLUSIVA por jogo, cross-entity**. Se `FOR` existe como sigla de atributo, nenhuma outra configuração do mesmo jogo pode usar `FOR`. Isso é obrigatório para o motor de fórmulas funcionar corretamente. Detalhes em [04-siglas-formulas.md](glossario/04-siglas-formulas.md).

---

## Conceitos-Chave (resumo)

| Conceito | O que é em uma frase |
|----------|----------------------|
| **Jogo** | Campanha de RPG — contêiner raiz de todas as configurações e fichas |
| **Ficha** | Documento com todos os dados de um personagem, referenciando configurações do Jogo |
| **Atributo** | Característica fundamental (Força, Agilidade, Vigor...) — pilar de todos os cálculos |
| **Ímpeto** | Efeito derivado calculado a partir do Total de um atributo |
| **Aptidão** | Habilidade treinável (Furtividade, Diplomacia, Acrobacia...) |
| **Bônus** | Valor calculado por fórmula a partir dos atributos (B.B.A, Reflexo, Percepção...) |
| **Vantagem** | Poder especial comprado com pontos — permanente, só sobe de nível |
| **Prospecção** | Dado extra raro e limitado que o Mestre concede para jogadas de alto risco |
| **Insólitus** | Traço especial com impacto mecânico real — pode liberar bônus, vantagens, raças, qualquer coisa que o Mestre decidir |
| **Renascimento** | Ciclo de transcendência a partir do nível 31 — desbloqueia poderes exclusivos |
| **Limitador** | Teto máximo de atributos por nível |
| **Fórmula** | Expressão matemática configurável que usa siglas como variáveis (avaliada via exp4j) |

---

## 📚 Documentação Detalhada (módulos)

O glossário completo está dividido em partes para facilitar a consulta:

| # | Arquivo | Conteúdo |
|---|---------|----------|
| 1 | [01-contexto-geral.md](glossario/01-contexto-geral.md) | O que é o Klayrah RPG, quem usa, conceitos estruturais (Jogo, Ficha, NPC, Configuração) |
| 2 | [02-configuracoes-jogo.md](glossario/02-configuracoes-jogo.md) | As 13+1 configurações detalhadas (Atributo, Nível, Aptidão, Bônus, Classe, Raça, Vantagem, etc.) |
| 3 | [03-termos-dominio.md](glossario/03-termos-dominio.md) | Glossário completo de termos por sistema (Atributos, Bônus, Vida, Essência, Progressão, Ameaça, Prospecção, Identidade, Vantagens, Aptidões) |
| 4 | [04-siglas-formulas.md](glossario/04-siglas-formulas.md) | Sistema de siglas, regra de unicidade por jogo, motor de fórmulas, tabela de siglas padrão |
| 5 | [05-termos-tecnicos-fluxo.md](glossario/05-termos-tecnicos-fluxo.md) | Termos técnicos (Soft Delete, Template, Auditoria), fluxo configuração→ficha, status do desenvolvimento |

---

## Fluxo Resumido

```
Mestre cria Jogo → Template Klayrah Padrão aplicado (13 configs)
  → Mestre ajusta configurações (opcional)
    → Jogador cria Ficha (referencia configs do Jogo)
      → Frontend calcula preview → Backend recalcula ao salvar (fonte oficial)
```

---

*Última atualização: Março 2026*
