# Análise de Requisitos Legado vs Novo Planejamento

**Data**: 2026-02-01  
**Revisado**: 2026-02-01 - Ajuste de escopo  
**Objetivo**: Identificar funcionalidades do sistema legado que devem ser incorporadas ao novo planejamento

---

## ✅ Funcionalidades JÁ COBERTAS no Novo Planejamento

| Funcionalidade Legado | Status | Onde está no novo |
|----------------------|--------|-------------------|
| CRUD de Fichas | ✅ Coberto | User Story 3 |
| Cálculo de Atributos (base + nivel + outros) | ✅ Coberto | data-model.md - FichaAtributo |
| Cálculo de Ímpeto | ✅ Coberto | AtributoConfig.formula_impeto |
| Cálculo de Nível por XP | ✅ Coberto | NivelConfig por jogo |
| Sistema de Limitador por nível | ✅ Coberto | NivelConfig.limitador_atributo |
| Pontos de atributo por nível | ✅ Coberto | NivelConfig.pontos_atributo |
| Sistema de Bônus (BBA, Bloqueio, etc) | ✅ Coberto | BonusConfig + FichaBonus |
| Sistema de Aptidões (24) | ✅ Coberto | AptidaoConfig + FichaAptidao |
| Sistema de Vida por Membro | ✅ Coberto | MembroCorpoConfig + FichaVidaMembro |
| Sistema de Essência | ✅ Coberto | EssenciaConfig + FichaEssencia |
| Sistema de Ameaça | ✅ Coberto | AmeacaConfig + FichaAmeaca |
| Sistema de Prospecção | ✅ Coberto | DadoProspeccaoConfig + FichaProspeccao |
| Sistema de Anotações | ✅ Coberto | Anotacao entity |
| Classes de Personagem | ✅ Coberto | ClassePersonagem (configurável) |
| Validação de Pontos de Atributo | ✅ Coberto | User Story 9, FR-054/FR-055 |
| Campo de Peso | ✅ Coberto | Ficha.peso_kg (campo simples) |

---

## ⏳ Funcionalidades para IMPLEMENTAÇÃO FUTURA

| Funcionalidade | Motivo do Adiamento | Quando Implementar |
|----------------|---------------------|-------------------|
| Exportação de Ficha em PDF | Escopo reduzido | Fase 2 |
| Galeria de Imagens (Personagem/Itens) | Escopo reduzido | Fase 2 |
| Upload de Arquivos | Escopo reduzido | Fase 2 |
| Análise de Imagem com IA (Gemini) | Escopo reduzido | Fase 2 |
| Sugestão de Interpretação com IA | Escopo reduzido | Fase 2 |
| Cálculo automático de peso | Não necessário | Opcional |

---

## ❌ Funcionalidades REMOVIDAS do Escopo Atual

### 1. Exportação PDF
**Legado**: html2pdf.js gerava PDF da ficha  
**Status**: Adiado para Fase 2  
**Motivo**: Foco no core do sistema primeiro

### 2. Galerias de Imagens
**Legado**: Upload e armazenamento de imagens em Base64  
**Status**: Adiado para Fase 2  
**Motivo**: Requer storage externo (S3), foco no core primeiro

### 3. Integração com IA (Gemini)
**Legado**: Análise de imagens e sugestão de interpretação  
**Status**: Adiado para Fase 2  
**Motivo**: Funcionalidade auxiliar, não essencial

### 4. Cálculo Automático de Peso
**Legado**: Peso = BMI × altura²  
**Status**: Não implementar  
**Motivo**: Campo `peso_kg` existe para preenchimento manual

---

## 🆕 Melhorias sobre o Legado (Novo Sistema)

| Melhoria | Descrição |
|----------|-----------|
| Sistema Multi-tenant | Múltiplos jogos com configurações independentes |
| Tudo Configurável | Atributos, aptidões, níveis, classes, raças, prospecção |
| Histórico/Auditoria | Mestre vê todas alterações (Hibernate Envers) |
| Raças com Bônus | Sistema novo de raças |
| Prospecção Persistida | No legado era apenas frontend, agora é backend |
| Banco Normalizado | Performance e queries flexíveis |
| Backend com API REST | Escalável, seguro, multi-usuário |

---

## 📊 Dados de Seed para Template Klayrah

### Tabela XP (para seed)

| Nível | XP | Limitador | Pontos |
|-------|-----|-----------|--------|
| 0 | 0 | 10 | 0 |
| 1 | 1000 | 10 | 3 |
| 2 | 3000 | 50 | 3 |
| 5 | 15000 | 50 | 3 |
| 10 | 55000 | 50 | 3 |
| 20 | 210000 | 50 | 3 |
| 21 | 231000 | 75 | 3 |
| 25 | 325000 | 75 | 3 |
| 26 | 351000 | 100 | 3 |
| 30 | 465000 | 100 | 3 |
| 31 | 496000 | 120 | 3 |
| 35 | 595000 | 120 | 3 |

### Dados de Prospecção (para seed)

| Dado | Valor Máximo |
|------|--------------|
| d3 | 3 |
| d4 | 4 |
| d6 | 6 |
| d8 | 8 |
| d10 | 10 |
| d12 | 12 |

### Porcentagens de Membros (para seed)

| Membro | Porcentagem |
|--------|-------------|
| Cabeça | 75% |
| Tronco | 100% |
| Braço Direito | 25% |
| Braço Esquerdo | 25% |
| Perna Direita | 25% |
| Perna Esquerda | 25% |
| Sangue | 100% |

---

## Conclusão

O novo planejamento está **completo para a Fase 1** com:

- ✅ 9 User Stories definidas
- ✅ ~34 tabelas modeladas
- ✅ Sistema de Prospecção configurável incluído
- ✅ Validação de pontos de atributo
- ✅ Histórico de alterações

**Funcionalidades adiadas para Fase 2:**
- PDF, Imagens, IA
