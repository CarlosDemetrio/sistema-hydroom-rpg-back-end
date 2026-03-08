# Termos Técnicos e Fluxo do Sistema

> 📖 Parte 5 do Glossário. Voltar ao [índice](../GLOSSARIO.md).

---

## Termos Técnicos do Sistema

| Termo | O que é |
|-------|---------|
| **Fórmula** | Expressão matemática armazenada como texto que o sistema avalia em tempo de execução usando a biblioteca exp4j. Permite que o Mestre customize cálculos sem alterar código. As fórmulas usam **siglas/abreviações** das configurações como variáveis (ex: `FOR`, `AGI`), criando um sistema interligado onde alterar uma sigla pode afetar múltiplas fórmulas. Ex: `FLOOR((FOR + AGI) / 3)` |
| **Template Klayrah Padrão** | Conjunto de configurações default que é aplicado automaticamente quando o Mestre cria um novo Jogo. Inclui os 7 atributos, 35 níveis, 24 aptidões, 12 classes, 6 bônus, 7 membros do corpo, etc. O Mestre pode modificar tudo depois |
| **Soft Delete** | Exclusão lógica — o registro não é removido do banco, apenas marcado com `deleted_at`. Permite recuperação e auditoria |
| **Ordem de Exibição** | Campo numérico presente em todas as configurações que define em que ordem elas aparecem na interface. Permite que o Mestre reorganize a ficha visualmente |
| **Configuração por Jogo** | Cada jogo tem suas próprias configurações independentes. Alterar os atributos do Jogo A não afeta o Jogo B |
| **Auditoria (Envers)** | Todas as alterações em fichas são registradas automaticamente pelo Hibernate Envers, permitindo ver o histórico completo de mudanças |

---

## Fluxo Resumido: Da Configuração à Ficha

```
1. Mestre cria Jogo
   └─→ Sistema aplica Template Klayrah Padrão
       └─→ 13 tipos de configuração são populados automaticamente

2. Mestre ajusta configurações (opcional)
   └─→ Pode renomear atributos, mudar fórmulas, adicionar classes, etc.

3. Jogador entra no Jogo
   └─→ Cria sua Ficha de Personagem
       └─→ Ficha referencia as configurações do Jogo
           ├─→ Atributos da ficha apontam para AtributoConfig
           ├─→ Aptidões da ficha apontam para AptidaoConfig
           ├─→ Bônus da ficha apontam para BonusConfig
           ├─→ Vida por membro aponta para MembroCorpoConfig
           ├─→ Prospecção aponta para DadoProspeccaoConfig
           └─→ Vantagens apontam para VantagemConfig

4. Jogador edita ficha
   └─→ Frontend calcula valores derivados em tempo real (preview)
       └─→ Ao salvar, Backend recalcula tudo (fonte oficial)
           └─→ Frontend substitui valores temporários pelos oficiais
```

---

## Status Atual do Desenvolvimento

### ✅ Implementado (Configurações — CRUD completo)
- AtributoConfig
- NivelConfig
- AptidaoConfig
- TipoAptidao
- BonusConfig
- ClassePersonagem
- Raca (com RacaBonusAtributo)
- MembroCorpoConfig
- DadoProspeccaoConfig
- GeneroConfig
- IndoleConfig
- PresencaConfig
- VantagemConfig

### 🔜 Próximos passos
- Motor de cálculos (FormulaEvaluatorService avaliando fórmulas configuráveis)
- CRUD de Ficha de Personagem e todos os seus sub-componentes (FichaAtributo, FichaAptidao, FichaBonus, FichaVida, FichaVidaMembro, FichaEssencia, FichaAmeaca, FichaProspeccao, FichaVantagem)
- Galerias de imagens e anotações
- Frontend Angular com PrimeNG

---

*Última atualização: Março 2026*
