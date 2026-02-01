# 📊 VISÃO RÁPIDA - KLAYRAH RPG

```
╔══════════════════════════════════════════════════════════════╗
║                    KLAYRAH RPG - SISTEMA DE                  ║
║               GERENCIAMENTO DE FICHAS DE PERSONAGENS         ║
╚══════════════════════════════════════════════════════════════╝
```

## 🎯 O QUE É?

Aplicação web para criar e gerenciar fichas de personagens de RPG com:
- ✅ Múltiplas fichas por usuário
- ✅ 170+ campos editáveis
- ✅ 50+ cálculos automáticos
- ✅ Galerias de imagens com IA
- ✅ Sistema de anotações
- ✅ Exportação em PDF
- ✅ 100% offline (exceto IA)

---

## 📈 ESTATÍSTICAS DO PROJETO

| Métrica | Valor |
|---------|-------|
| **Linhas de Código (React)** | ~1.400 |
| **Componentes** | 7 |
| **Cálculos Automáticos** | 50+ |
| **Campos de Entrada** | 170+ |
| **Integrações Externas** | 2 (Gemini, html2pdf) |
| **Tempo Estimado (Angular)** | 6-8 semanas |
| **Complexidade** | Média-Alta |

---

## 🗂️ DOCUMENTOS CRIADOS

```
📁 klayrah-rpg/
│
├── 📄 README_DOCUMENTACAO.md ⭐ COMECE AQUI
│   └── Índice geral + Guia de leitura
│
├── 📘 RESUMO_EXECUTIVO.md
│   ├── Visão geral do sistema
│   ├── Estrutura de dados
│   ├── Tabelas de referência
│   ├── Fórmulas de cálculo
│   └── Checklist de implementação
│   📏 ~10.000 palavras
│
├── 📕 ANALISE_REQUISITOS.md
│   ├── Requisitos funcionais completos
│   ├── Regras de negócio detalhadas
│   ├── Fluxos de usuário
│   ├── Casos especiais
│   └── Glossário de termos
│   📏 ~15.000 palavras
│
├── 📗 ANALISE_POR_ARQUIVO.md
│   ├── Análise técnica por componente
│   ├── Funções e algoritmos
│   ├── Layouts visuais (ASCII)
│   ├── Matriz de dependências
│   └── Checklist por arquivo
│   📏 ~12.000 palavras
│
└── 📙 DIAGRAMAS_CASOS_USO.md
    ├── Diagramas de fluxo (ASCII)
    ├── Casos de uso passo a passo
    ├── Exemplos de dados (JSON)
    ├── Testes de validação
    └── Cenários de erro
    📏 ~11.000 palavras

Total: ~48.000 palavras | ~200 páginas
```

---

## 🎓 COMO LER A DOCUMENTAÇÃO

### Para Líderes/Gestores (30 min)
```
1. README_DOCUMENTACAO.md
2. RESUMO_EXECUTIVO.md → até "Integrações Externas"
```

### Para Desenvolvedores Novos no Projeto (2h)
```
1. README_DOCUMENTACAO.md
2. RESUMO_EXECUTIVO.md (completo)
3. ANALISE_REQUISITOS.md → seções principais
4. ANALISE_POR_ARQUIVO.md → componente que vai trabalhar
```

### Para Implementar uma Funcionalidade Específica
```
1. README_DOCUMENTACAO.md → Índice Geral
2. ANALISE_REQUISITOS.md → seção específica
3. ANALISE_POR_ARQUIVO.md → componente relacionado
4. DIAGRAMAS_CASOS_USO.md → fluxos e testes
```

### Para Escrever Testes
```
1. DIAGRAMAS_CASOS_USO.md → Casos de Uso
2. DIAGRAMAS_CASOS_USO.md → Testes de Validação
3. ANALISE_REQUISITOS.md → Regras de Negócio
```

---

## 🏗️ ARQUITETURA DO SISTEMA

```
┌────────────────────────────────────────────────────┐
│                   INTERFACE                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │  Ficha   │ │ Galeria  │ │Anotações │  ...     │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘          │
└───────┼────────────┼────────────┼─────────────────┘
        │            │            │
        └────────────┴────────────┘
                     │
        ┌────────────▼────────────┐
        │   GERENCIADOR DE        │
        │   ESTADO (Fichas)       │
        └────────────┬────────────┘
                     │
        ┌────────────▼────────────┐
        │    PERSISTÊNCIA         │
        │    (localStorage)       │
        └─────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              INTEGRAÇÕES EXTERNAS                   │
│  ┌──────────────────┐    ┌──────────────────┐     │
│  │   Gemini AI      │    │   html2pdf.js    │     │
│  │   (Análises)     │    │   (Exportar)     │     │
│  └──────────────────┘    └──────────────────┘     │
└─────────────────────────────────────────────────────┘
```

---

## 📊 ESTRUTURA DE UMA FICHA

```
FICHA DE PERSONAGEM
│
├─ 📝 IDENTIFICAÇÃO (9 campos)
│   ├─ Jogador
│   ├─ Personagem
│   ├─ Título Heróico
│   ├─ Insólitus
│   ├─ Origem
│   ├─ Gênero
│   ├─ Classe
│   └─ ...
│
├─ 💪 ATRIBUTOS (7 × 5 campos = 35)
│   ├─ Força ────────┬──→ Ímpeto: 72 kg
│   ├─ Agilidade ────┼──→ Ímpeto: 6 m
│   ├─ Vigor ────────┼──→ Ímpeto: 2 RD
│   ├─ Sabedoria ────┼──→ Ímpeto: 1 RDM
│   ├─ Intuição ─────┼──→ Ímpeto: 0 Sorte
│   ├─ Inteligência ─┼──→ Ímpeto: 0 Cmd
│   └─ Astúcia ──────┴──→ Ímpeto: 0 Est
│
├─ ⚔️ BÔNUS (6 × 6 campos = 36)
│   ├─ B.B.A ──────→ 14 + modificadores
│   ├─ Bloqueio ───→ 14 + modificadores
│   ├─ Reflexo ────→ 8 + modificadores
│   ├─ B.B.M ──────→ 7 + modificadores
│   ├─ Percepção ──→ 8 + modificadores
│   └─ Raciocínio ─→ 6 + modificadores
│
├─ 🎯 APTIDÕES (24 × 4 campos = 96)
│   ├─ Físicas (12)
│   │   ├─ Acrobacia: 5+2+3 = 10
│   │   ├─ Guarda: 8+2+5 = 15
│   │   └─ ...
│   └─ Mentais (12)
│       ├─ Idiomas: 8+2+3 = 13
│       ├─ Observação: 10+2+5 = 17
│       └─ ...
│
├─ ❤️ VIDA (8 campos)
│   ├─ Total: 40 pts
│   └─ Por membro:
│       ├─ Cabeça (75%): 30 - 5 dano = 25
│       ├─ Tronco (100%): 40 - 0 dano = 40
│       ├─ Braço D (25%): 10 - 8 dano = 2
│       └─ ...
│
├─ ✨ ESSÊNCIA (5 campos)
│   ├─ Total: 30 pts
│   ├─ Gastos: 12 pts
│   └─ Restante: 18 pts
│
├─ 💀 AMEAÇA (5 campos)
│   └─ Total: 32 pts
│
└─ 🎲 DESENVOLVIMENTO (3 campos)
    ├─ XP: 55.000 ────→ Nível: 10
    ├─ Renascimentos: 0
    └─ Limitador: 50
```

---

## 🔄 FLUXO PRINCIPAL DO USUÁRIO

```
1️⃣ CRIAR PERSONAGEM
   │
   ├─→ "Nova Ficha"
   ├─→ Preencher dados básicos
   ├─→ Inserir XP → Nível calculado
   ├─→ Distribuir pontos de atributo
   ├─→ Auto-save contínuo
   └─→ ✅ Ficha pronta

2️⃣ GERENCIAR MÚLTIPLAS FICHAS
   │
   ├─→ "Minhas Fichas"
   ├─→ Ver lista de todas
   ├─→ Clicar para trocar
   └─→ ✅ Ficha ativa mudada

3️⃣ ADICIONAR IMAGENS
   │
   ├─→ Aba "Meu Personagem" ou "Meus Itens"
   ├─→ Upload de imagem(ns)
   ├─→ Selecionar imagem
   ├─→ "Analyze with Gemini" (opcional)
   └─→ ✅ Análise salva

4️⃣ FAZER ANOTAÇÕES
   │
   ├─→ Aba "Anotações"
   ├─→ Digitar texto
   ├─→ "Save Note"
   └─→ ✅ Nota salva com timestamp

5️⃣ EXPORTAR FICHA
   │
   ├─→ "Baixar Ficha como PDF"
   ├─→ Sistema gera PDF
   └─→ ✅ Download iniciado
```

---

## 🧮 CÁLCULOS PRINCIPAIS

### Nível baseado em XP
```
XP → Tabela de 35 níveis → Nível
```

### Atributos
```
Total = BASE + NÍVEL + OUTROS
Ímpeto = função(Total, tipo_atributo)
```

### Bônus Base
```
BBA = floor((Força + Agilidade) / 3)
Bloqueio = floor((Força + Vigor) / 3)
Reflexo = floor((Agilidade + Astúcia) / 3)
BBM = floor((Sabedoria + Inteligência) / 3)
Percepção = floor((Inteligência + Intuição) / 3)
Raciocínio = floor((Inteligência + Astúcia) / 3)
```

### Vida
```
Total = Vigor + Nível + VT + Renascimentos + OUT
Por_membro = floor(Total × Porcentagem) - Danos
```

### Essência
```
Base = floor((Vigor + Sabedoria) / 2)
Total = Base + Nível + Renasc + Vant + Outros
Restante = Total - Gastos
```

### Ameaça
```
Total = Nível + Itens + Títulos + Renascimentos + Outros
```

---

## 🎨 STACK TECNOLÓGICO

### Atual (React)
```
├─ React 19.2.0
├─ TypeScript 5.8.2
├─ Vite 6.2.0
├─ Tailwind CSS (CDN)
├─ @google/genai 1.29.0
└─ html2pdf.js 0.10.1
```

### Proposto (Angular)
```
├─ Angular 17+ (Standalone Components)
├─ TypeScript 5.x
├─ RxJS ou Signals
├─ Tailwind CSS
├─ @google/generative-ai
└─ html2pdf.js
```

---

## ⚡ REGRAS DE NEGÓCIO CRÍTICAS

| # | Regra | Impacto |
|---|-------|---------|
| 1 | **Sempre há pelo menos uma ficha** | Deletar última cria nova |
| 2 | **Auto-save contínuo** | Sem botão salvar |
| 3 | **3 pontos por nível** | Validação visual |
| 4 | **Nível automático via XP** | Não editável |
| 5 | **Peso automático** | Baseado em altura/gênero |
| 6 | **Upload máx 20MB** | Validação antes de processar |
| 7 | **Galerias por ficha** | ID único no localStorage |
| 8 | **Confirmação de exclusão** | Fichas, imagens, notas |

---

## 🔐 DEPENDÊNCIAS ENTRE CAMPOS

```
XP ──────→ Nível ──────┬──→ Limitador
                       ├──→ Pontos de Atributo (validação)
                       ├──→ Vida Total
                       ├──→ Essência Total
                       └──→ Ameaça Total

Atributos ─┬──→ Totais ──→ Ímpetos
           └──→ Bônus Base ──→ Bônus Totais

Altura ────┬──→ Peso (auto-calculado)
Gênero ────┘

Renascimentos ──┬──→ Vida Total
                ├──→ Essência Total
                └──→ Ameaça Total
```

---

## 📦 5 STORAGE KEYS

```javascript
localStorage = {
  "allCharacterSheets": [{ ficha1 }, { ficha2 }, ...],
  "activeCharacterSheetId": "1738339200000",
  "character_1738339200000": [{ img1 }, { img2 }, ...],
  "items_1738339200000": [{ img1 }, { img2 }, ...],
  "notes_1738339200000": [{ nota1 }, { nota2 }, ...]
}
```

---

## 🚀 ROADMAP DE MIGRAÇÃO

| Fase | Duração | Atividades |
|------|---------|------------|
| **1. Setup** | 1 semana | Criar projeto, estrutura, configurações |
| **2. Services** | 1 semana | Storage, Sheet, Gemini, PDF |
| **3. Models** | 2 dias | Interfaces, constantes, defaults |
| **4. Shared** | 3 dias | Input, Select, ValueBox components |
| **5. Pipes** | 2 dias | Cálculos como pipes |
| **6. Features** | 2 semanas | App, Sheet, Gallery, Notes, Manager |
| **7. Integração** | 1 semana | Validações, errors, loading |
| **8. Estilos** | 3 dias | Tailwind, responsividade, print |
| **9. Testes** | 1 semana | Unit, integration, E2E |
| **10. Deploy** | 3 dias | Otimização, build, deploy |

**TOTAL:** 6-8 semanas (1 dev full-time)

---

## 💡 DICAS PARA IMPLEMENTAÇÃO

### ✅ FAZER
- Usar Signals ou BehaviorSubject para estado
- Quebrar CharacterSheet em subcomponentes
- Criar pipes para cálculos reutilizáveis
- Implementar debounce no auto-save
- Comprimir imagens antes de salvar
- Validar antes de processar

### ❌ EVITAR
- NgRx (overkill para este projeto)
- Backend (não é necessário)
- Muitos níveis de componentes aninhados
- Recriar cálculos em cada componente
- Ignorar limites do localStorage

---

## 🎯 PRÓXIMOS PASSOS

1. ✅ **Ler README_DOCUMENTACAO.md** (você está aqui!)
2. ✅ **Ler RESUMO_EXECUTIVO.md** (visão geral)
3. ✅ **Planejar sprints** baseado no roadmap
4. ✅ **Configurar ambiente Angular**
5. ✅ **Começar pelos services** (base do sistema)
6. ✅ **Implementar componentes** um a um
7. ✅ **Testar continuamente**
8. ✅ **Deploy**

---

## 📞 PRECISA DE AJUDA?

### Consulte:
1. 📄 **README_DOCUMENTACAO.md** - Índice completo
2. 📘 **RESUMO_EXECUTIVO.md** - Visão geral
3. 📕 **ANALISE_REQUISITOS.md** - Requisitos detalhados
4. 📗 **ANALISE_POR_ARQUIVO.md** - Análise técnica
5. 📙 **DIAGRAMAS_CASOS_USO.md** - Fluxos e exemplos

### Dúvidas Específicas:
- **Cálculo não funciona?** → RESUMO_EXECUTIVO.md → Fórmulas
- **Não sei implementar X?** → ANALISE_POR_ARQUIVO.md → Componente X
- **Teste falhou?** → DIAGRAMAS_CASOS_USO.md → Testes de Validação
- **Fluxo confuso?** → DIAGRAMAS_CASOS_USO.md → Diagramas

---

## 🎉 CONCLUSÃO

Você tem em mãos uma documentação completa e detalhada de **~48.000 palavras** que cobre:

✅ Todos os requisitos funcionais  
✅ Todas as regras de negócio  
✅ Todos os cálculos e fórmulas  
✅ Todos os fluxos de usuário  
✅ Exemplos práticos de dados  
✅ Testes de validação  
✅ Roadmap de implementação  
✅ Estimativas de tempo  

**Pronto para começar a migração! 🚀**

---

```
╔══════════════════════════════════════════════════════════════╗
║         DOCUMENTAÇÃO COMPLETA E PRONTA PARA USO              ║
║                      BOA SORTE! 🍀                           ║
╚══════════════════════════════════════════════════════════════╝
```

---

**Criado em:** Janeiro 2026  
**Versão:** 1.0  
**Status:** ✅ Completo
