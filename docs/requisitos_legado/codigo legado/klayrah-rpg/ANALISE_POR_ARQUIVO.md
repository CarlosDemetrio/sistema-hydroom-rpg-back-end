# ANÁLISE POR ARQUIVO - KLAYRAH RPG

## 📋 ÍNDICE DE ARQUIVOS

1. [App.tsx](#1-apptsx) - Aplicação Principal
2. [CharacterSheet.tsx](x#2-charactersheettsx) - Ficha de Personagem
3. [Gallery.tsx](#3-gallerytsx) - Galeria de Imagens
4. [Notes.tsx](#4-notestsx) - Sistema de Anotações
5. [SheetManager.tsx](#5-sheetmanagertsx) - Gerenciador de Fichas
6. [Prospeccao.tsx](#6-prospeccaotsx) - Sistema de Prospecção
7. [DiceRoller.tsx](#7-dicerollertsx) - Rolador de Dados (vazio)

---

## 1. App.tsx

### 📌 RESPONSABILIDADE
Componente raiz que orquestra toda a aplicação.

### 🎯 O QUE FAZ
- Gerencia a navegação entre 5 abas diferentes
- Mantém o estado global de todas as fichas
- Controla qual ficha está ativa
- Persiste dados no localStorage
- Renderiza o layout principal (header + navegação + conteúdo)

### 📊 ESTADO GERENCIADO
```typescript
activeTab: 'sheet' | 'character' | 'items' | 'notes' | 'mySheets'
allSheets: CharacterSheetData[]
activeSheetId: string | null
```

### 🔄 CICLO DE VIDA

#### Inicialização
```
1. Carregar 'allCharacterSheets' do localStorage
2. Carregar 'activeCharacterSheetId' do localStorage
3. Se não há fichas:
   → Criar ficha padrão "Meu Primeiro Personagem"
4. Se há fichas mas ID inválido:
   → Selecionar primeira ficha
5. Definir estado inicial
```

#### A Cada Alteração
```
1. Estado de fichas muda
2. Salvar 'allCharacterSheets' no localStorage
3. Salvar 'activeCharacterSheetId' no localStorage
```

### ⚙️ FUNÇÕES PRINCIPAIS

#### `handleNewSheet()`
**Entrada:** Nenhuma  
**Processo:**
1. Criar nova ficha com DEFAULT_SHEET
2. Gerar ID único (Date.now())
3. Adicionar à lista de fichas
4. Definir como ativa
5. Mudar para aba 'sheet'

**Saída:** Nova ficha criada e ativada

---

#### `handleUpdateSheet(updatedSheet)`
**Entrada:** Ficha modificada  
**Processo:**
1. Encontrar ficha pelo ID
2. Substituir pelos novos dados

**Saída:** Lista de fichas atualizada

---

#### `handleSelectSheet(id)`
**Entrada:** ID da ficha  
**Processo:**
1. Definir como ativa
2. Mudar para aba 'sheet'

**Saída:** Ficha selecionada exibida

---

#### `handleDeleteSheet(id)`
**Entrada:** ID da ficha  
**Processo:**
1. Solicitar confirmação
2. Se confirmado:
   - Remover da lista
   - Se era a ativa:
     * Se há outras fichas → selecionar primeira
     * Se não há outras → criar nova ficha

**Saída:** Ficha removida, sempre mantém ao menos uma

---

#### `handleDownloadPdf()`
**Entrada:** Nenhuma  
**Processo:**
1. Encontrar elemento #character-sheet-container
2. Obter nome do personagem da ficha ativa
3. Sanitizar nome para nome de arquivo
4. Configurar html2pdf
5. Gerar e baixar PDF

**Saída:** Arquivo PDF baixado

---

### 🎨 INTERFACE

#### Header (sempre visível, exceto impressão)
```
┌────────────────────────────────────────────────────┐
│ Klayrah    [+ Nova Ficha] [↓ Baixar Ficha como PDF]│
└────────────────────────────────────────────────────┘
```

#### Navegação (abas)
```
┌────────────────────────────────────────────────────┐
│ [Ficha] [Meu Personagem] [Meus Itens] [Anotações] [Minhas Fichas]
└────────────────────────────────────────────────────┘
```

#### Área de Conteúdo
```
┌────────────────────────────────────────────────────┐
│                                                    │
│        Conteúdo da aba selecionada                │
│                                                    │
└────────────────────────────────────────────────────┘
```

### 🔗 RENDERIZAÇÃO CONDICIONAL

| Aba | Componente Renderizado |
|-----|------------------------|
| sheet | `<CharacterSheet>` |
| character | `<Gallery galleryId="character_{id}">` |
| items | `<Gallery galleryId="items_{id}">` |
| notes | `<Notes notesId="notes_{id}">` |
| mySheets | `<SheetManager>` |

### 📝 REGRAS DE NEGÓCIO

1. ✅ **Sempre há uma ficha ativa**
2. ✅ **Sempre há pelo menos uma ficha no sistema**
3. ✅ **Auto-save automático** (via useEffect)
4. ✅ **Confirmação para deletar ficha**
5. ✅ **IDs únicos** gerados via timestamp
6. ✅ **Galeria e notas específicas por ficha** (via sufixo no ID)

---

## 2. CharacterSheet.tsx

### 📌 RESPONSABILIDADE
Componente mais complexo - renderiza e gerencia toda a ficha de personagem.

### 🎯 O QUE FAZ
- Exibe todos os campos da ficha
- Realiza cálculos automáticos em tempo real
- Valida distribuição de pontos
- Integra com IA para sugestões
- Upload de imagem do personagem
- Estrutura otimizada para impressão/PDF

### 📊 ESTADO INTERNO
```typescript
interpretationSuggestion: string  // Resultado da IA
isLoading: boolean                // IA processando
isUploading: boolean              // Upload de imagem
```

### 🧮 CÁLCULOS AUTOMÁTICOS (COMPUTADOS)

#### Totais de Atributos (7)
```typescript
total = base + nivel + outros
```

#### Ímpetos de Atributos (7)
```typescript
força_impeto = total × 3          // kg
agilidade_impeto = floor(total / 3)    // metros
vigor_impeto = floor(total / 10)       // RD
sabedoria_impeto = floor(total / 10)   // RDM
intuicao_impeto = min(floor(total / 20), 3)  // Sorte
inteligencia_impeto = floor(total / 20)      // Comando
astucia_impeto = floor(total / 10)           // Estratégia
```

#### Bônus Base (6)
```typescript
bba = floor((força + agilidade) / 3)
bloqueio = floor((força + vigor) / 3)
reflexo = floor((agilidade + astucia) / 3)
bbm = floor((sabedoria + inteligencia) / 3)
percep = floor((inteligencia + intuicao) / 3)
racionc = floor((inteligencia + astucia) / 3)
```

#### Vida
```typescript
vidaVg = vigor_total
totalVida = vidaVg + level + vida.vt + renascimentos + vida.out

// Por membro:
vida_cabeca = floor(totalVida × 0.75) - danos.cabeca
vida_tronco = floor(totalVida × 1.0) - danos.tronco
vida_bracoD = floor(totalVida × 0.25) - danos.bracoD
vida_bracoE = floor(totalVida × 0.25) - danos.bracoE
vida_pernaD = floor(totalVida × 0.25) - danos.pernaD
vida_pernaE = floor(totalVida × 0.25) - danos.pernaE
vida_sangue = floor(totalVida × 1.0) - danos.sangue
```

#### Essência
```typescript
essenciaBase = floor((vigor + sabedoria) / 2)
totalEssencia = essenciaBase + level + essencia.renasc + essencia.vant + essencia.outros
essenciaRestante = totalEssencia - essencia.gastos
```

#### Ameaça
```typescript
totalAmeaca = level + ameaca.itens + ameaca.titulos + renascimentos + ameaca.outros
```

#### Validação de Pontos de Atributo
```typescript
spentAttributePoints = soma de todos atributo.nivel
expectedAttributePoints = level × 3

if (spentAttributePoints > expectedAttributePoints)
  → Alerta: "Você distribuiu X ponto(s) a mais!"
  
if (spentAttributePoints < expectedAttributePoints)
  → Alerta: "Você tem X ponto(s) para distribuir!"
```

#### Limitador
```typescript
if (level <= 1) return 10
if (level <= 20) return 50
if (level <= 25) return 75
if (level <= 30) return 100
if (level <= 35) return 120
return "Renascimento"
```

#### Nível baseado em XP
```typescript
// Tabela de 35 níveis
XP_LEVELS = [0, 1000, 3000, 6000, ..., 595000]

function getLevelForXp(xp):
  if xp < 1000: return 0
  for i from 35 to 1:
    if xp >= XP_LEVELS[i]: return i
  return 0
```

### 🎨 LAYOUT DA FICHA

```
┌─────────────────────────────────────────────────────────────┐
│ ┌─────────────────────────────────────────────────────────┐ │
│ │    [Imagem]      Personalidade                          │ │
│ │    Upload        - Índole, Presença, Arquétipo          │ │
│ │    Descrição     - [Gerar Sugestão de Interpretação]   │ │
│ │    Física        - Resultado da IA                       │ │
│ └─────────────────────────────────────────────────────────┘ │
│                                                             │
│ ┌──────────────────────────┐  ┌──────────────────────────┐│
│ │ COLUNA ESQUERDA          │  │ COLUNA DIREITA           ││
│ │                          │  │                          ││
│ │ ┌──────────────────────┐ │  │ ┌──────────────────────┐││
│ │ │ Informações Pessoais │ │  │ │ Desenvolvimento      │││
│ │ └──────────────────────┘ │  │ └──────────────────────┘││
│ │                          │  │                          ││
│ │ ┌──────────────────────┐ │  │ ┌──────────────────────┐││
│ │ │ Atributos (tabela)   │ │  │ │ Ameaça               │││
│ │ └──────────────────────┘ │  │ └──────────────────────┘││
│ │                          │  │                          ││
│ │ ┌──────────────────────┐ │  │ ┌──────────────────────┐││
│ │ │ Bônus (6 cards)      │ │  │ │ Vida                 │││
│ │ └──────────────────────┘ │  │ │ - Total              │││
│ │                          │  │ │ - Membros (tabela)   │││
│ │ ┌──────────────────────┐ │  │ └──────────────────────┘││
│ │ │ Aptidões             │ │  │                          ││
│ │ │ - Físicas (12)       │ │  │ ┌──────────────────────┐││
│ │ │ - Mentais (12)       │ │  │ │ Essência             │││
│ │ └──────────────────────┘ │  │ └──────────────────────┘││
│ │                          │  │                          ││
│ │                          │  │ ┌──────────────────────┐││
│ │                          │  │ │ Prospecção           │││
│ │                          │  │ └──────────────────────┘││
│ └──────────────────────────┘  └──────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

### ⚙️ FUNÇÕES PRINCIPAIS

#### `handleAttrChange(attr, field, value)`
**O que faz:** Atualiza campo de atributo  
**Cálculos disparados:** Total → Ímpeto → Bônus Base

#### `handleSheetChange(section, field, value)`
**O que faz:** Atualiza campo de vida/danos/essência/ameaça  
**Cálculos disparados:** Totais específicos da seção

#### `handleBonusChange(bonusType, field, value)`
**O que faz:** Atualiza modificador de bônus  
**Cálculos disparados:** Total do bônus específico

#### `handleExperienceChange(e)`
**O que faz:** Atualiza XP  
**Cálculos disparados:** Nível → Limitador → Validação de pontos

#### `handleRenascimentosChange(e)`
**O que faz:** Atualiza renascimentos  
**Cálculos disparados:** Vida, Essência, Ameaça

#### `handleAptidaoChange(aptidao, field, value)`
**O que faz:** Atualiza campo de aptidão  
**Cálculos disparados:** Total da aptidão

#### `handleAlturaChange(e)`
**O que faz:** Atualiza altura  
**Cálculos disparados:** Peso automático

#### `handleGeneroChange(e)`
**O que faz:** Atualiza gênero  
**Cálculos disparados:** Peso automático

#### `handleImageUpload(e)`
**O que faz:** Upload de imagem do personagem
**Processo:**
1. Validar tamanho (20MB max)
2. Converter para Base64
3. Atualizar characterImage

#### `generateInterpretation()`
**O que faz:** Chama IA para sugestão de interpretação
**Entrada:** Índole, Presença, Arquétipo
**Prompt:** "Baseado nos seguintes traços... forneça uma sugestão curta e criativa"
**Saída:** Texto de sugestão exibido

### 📊 ESTRUTURA DE DADOS (DEFAULT_SHEET)

```typescript
{
  // Identificação
  player: '',
  character: 'Novo Personagem',
  tituloHeroico: '',
  insolitus: '',
  origem: '',
  genero: 'Masculino',
  classe: 'Guerreiro',
  customClasse: '',
  
  // Personalidade
  indole: 'Neutro',
  presenca: 'Neutro',
  arquetipo: '',
  
  // Descrição física
  descricaoFisica: {
    idade: 25,
    altura: 170,
    peso: 65,
    cabeloCor: '',
    cabeloTamanho: '',
    olhosCor: ''
  },
  
  // Desenvolvimento
  level: 0,
  experience: 0,
  renascimentos: 0,
  
  // Atributos (7 × este padrão)
  attributes: {
    forca: { base: 0, nivel: 0, outros: 0, impeto: 0 },
    agilidade: { base: 0, nivel: 0, outros: 0, impeto: 0 },
    vigor: { base: 0, nivel: 0, outros: 0, impeto: 0 },
    sabedoria: { base: 0, nivel: 0, outros: 0, impeto: 0 },
    intuicao: { base: 0, nivel: 0, outros: 0, impeto: 0 },
    inteligencia: { base: 0, nivel: 0, outros: 0, impeto: 0 },
    astucia: { base: 0, nivel: 0, outros: 0, impeto: 0 }
  },
  
  // Imagem
  characterImage: '',
  
  // Vida
  vida: { vt: 0, out: 0 },
  danos: { cabeça: 0, tronco: 0, bracoD: 0, bracoE: 0, pernaD: 0, pernaE: 0, sangue: 0 },
  
  // Essência
  essencia: { renasc: 0, vant: 0, outros: 0, gastos: 0 },
  
  // Aptidões (24 × este padrão)
  aptidoes: {
    acrobacia: { base: 0, sorte: 0, classe: 0 },
    // ... 23 outras
  },
  
  // Ameaça
  ameaca: { itens: 0, titulos: 0, outros: 0 },
  
  // Bônus (6 × este padrão)
  bonus: {
    bba: { vantagens: 0, classe: 0, itens: 0, gloria: 0, outros: 0 },
    bloqueio: { vantagens: 0, classe: 0, itens: 0, gloria: 0, outros: 0 },
    reflexo: { vantagens: 0, classe: 0, itens: 0, gloria: 0, outros: 0 },
    bbm: { vantagens: 0, classe: 0, itens: 0, gloria: 0, outros: 0 },
    percep: { vantagens: 0, classe: 0, itens: 0, gloria: 0, outros: 0 },
    racionc: { vantagens: 0, classe: 0, itens: 0, gloria: 0, outros: 0 }
  }
}
```

### 📝 REGRAS DE NEGÓCIO

1. ✅ **3 pontos de atributo por nível**
2. ✅ **Limitador baseado em nível**
3. ✅ **Nível calculado automaticamente pelo XP**
4. ✅ **Peso calculado automaticamente por altura/gênero**
5. ✅ **Todos os cálculos em tempo real**
6. ✅ **Notificação visual de pontos disponíveis/excedidos**
7. ✅ **Integração opcional com IA**
8. ✅ **Upload de imagem com validação de tamanho**
9. ✅ **Layout otimizado para impressão**

### 🎨 COMPONENTES AUXILIARES

#### `SectionTitle`
Título preto com fundo preto e texto branco

#### `RedSectionTitle`
Título vermelho com fundo vermelho e texto branco

#### `InputField`
Campo de input padrão com label

#### `SelectField`
Campo de seleção padrão com label

#### `ValueBox`
Display somente leitura com label e valor

#### `AptidaoInput`
Linha de aptidão com 3 inputs + total calculado

#### `BonusField`
Card de bônus com base + 5 modificadores + total

---

## 3. Gallery.tsx

### 📌 RESPONSABILIDADE
Gerenciar galerias de imagens (personagem ou itens) com análise de IA.

### 🎯 O QUE FAZ
- Upload múltiplo de imagens
- Armazenar imagens em Base64
- Exibir miniaturas em grid
- Visualização detalhada de imagem selecionada
- Deletar imagens
- Análise de imagens com IA (Gemini)

### 📊 PROPS
```typescript
galleryId: string    // 'character_{id}' ou 'items_{id}'
title: string        // 'Meu Personagem' ou 'Meus Itens'
```

### 📊 ESTADO
```typescript
images: ImageItem[]           // Lista de imagens
selectedImage: ImageItem | null  // Imagem selecionada
isLoading: boolean            // IA processando
isUploading: boolean          // Upload em andamento
```

### 📊 ESTRUTURA DE IMAGEM
```typescript
interface ImageItem {
  id: string          // timestamp + nome do arquivo
  src: string         // Base64 da imagem
  name: string        // Nome original do arquivo
  analysis?: string   // Análise da IA (opcional)
}
```

### 🎨 LAYOUT

```
┌───────────────────────────────────────────────────────┐
│ Meu Personagem / Meus Itens                           │
│ [Upload Image(s)]                                     │
├───────────────┬───────────────────────────────────────┤
│ MINIATURAS    │  VISUALIZAÇÃO DETALHADA               │
│               │                                       │
│ ┌───┐ ┌───┐  │  ┌─────────┐                          │
│ │img│ │img│  │  │         │                          │
│ └───┘ └───┘  │  │  Img    │  Nome do arquivo         │
│               │  │  Maior  │  [Delete]                │
│ ┌───┐ ┌───┐  │  │         │  [Analyze with Gemini]   │
│ │img│ │img│  │  └─────────┘                          │
│ └───┘ └───┘  │                                       │
│               │  ┌─────────────────────────────────┐  │
│ ┌───┐ ┌───┐  │  │ Gemini Analysis:                │  │
│ │img│ │img│  │  │ Texto da análise...             │  │
│ └───┘ └───┘  │  └─────────────────────────────────┘  │
└───────────────┴───────────────────────────────────────┘
```

### ⚙️ FUNÇÕES PRINCIPAIS

#### `handleImageUpload(e)`
**Processo:**
1. Receber arquivos selecionados
2. Validar tamanho de cada arquivo (20MB max)
3. Se algum exceder → alertar e não processar
4. Para cada arquivo válido:
   - Ler como DataURL (Base64)
   - Criar objeto ImageItem
   - Gerar ID único
5. Adicionar todos ao array de imagens
6. Salvar no localStorage

**Validação:**
```typescript
largeFiles = files.filter(file => file.size > 20MB)
if (largeFiles.length > 0)
  alert("Arquivos muito grandes: " + nomes)
```

#### `handleDeleteImage(id)`
**Processo:**
1. Confirmar exclusão
2. Filtrar imagem do array
3. Se era a selecionada → limpar seleção
4. Salvar no localStorage

#### `handleAnalyzeImage(image)`
**Processo:**
1. Verificar API_KEY
2. Extrair Base64 da imagem
3. Detectar MIME type
4. Montar prompt baseado no tipo:
   - `galleryId.startsWith('items')` → Prompt de item
   - Caso contrário → Prompt de personagem
5. Enviar para Gemini API
6. Receber análise
7. Atualizar imagem com análise
8. Salvar no localStorage

**Prompts:**
```
PERSONAGEM:
"Describe this character or scene. What is happening? 
What is the mood? What story does this image tell?"

ITEM:
"Describe this item from a fantasy RPG. What could it be? 
What are its potential powers or history? Be creative."
```

### 🔄 CICLO DE VIDA

**Ao montar / trocar galleryId:**
```
1. Carregar images do localStorage[galleryId]
2. Se não existe → array vazio
3. Limpar seleção
```

**Ao salvar:**
```
1. Atualizar estado local
2. Salvar localStorage[galleryId] = JSON.stringify(images)
```

### 📝 REGRAS DE NEGÓCIO

1. ✅ **Limite de 20MB por arquivo**
2. ✅ **Upload múltiplo permitido**
3. ✅ **Validação antes do processamento**
4. ✅ **Galeria independente por ficha**
5. ✅ **Análise persiste com a imagem**
6. ✅ **Confirmação para deletar**
7. ✅ **Loading states visuais**

---

## 4. Notes.tsx

### 📌 RESPONSABILIDADE
Sistema de anotações organizado por ficha de personagem.

### 🎯 O QUE FAZ
- Criar notas com timestamp
- Listar notas (mais recente primeiro)
- Deletar notas
- Armazenar no localStorage por ficha

### 📊 PROPS
```typescript
notesId: string    // 'notes_{id}'
```

### 📊 ESTADO
```typescript
notes: Note[]         // Lista de notas
currentNote: string   // Texto sendo digitado
```

### 📊 ESTRUTURA DE NOTA
```typescript
interface Note {
  id: number        // timestamp único
  content: string   // conteúdo da nota
  timestamp: string // data/hora formatada
}
```

### 🎨 LAYOUT

```
┌───────────────────────────────────────────────────┐
│ Anotações                                         │
│                                                   │
│ ┌───────────────────────────────────────────────┐ │
│ │ Write your note here...                       │ │
│ │                                               │ │
│ │                                               │ │
│ └───────────────────────────────────────────────┘ │
│ [Save Note]                                       │
│                                                   │
│ ┌───────────────────────────────────────────────┐ │
│ │ Conteúdo da nota mais recente     [X]         │ │
│ │ 31/01/2026 14:30                              │ │
│ └───────────────────────────────────────────────┘ │
│                                                   │
│ ┌───────────────────────────────────────────────┐ │
│ │ Conteúdo da nota anterior         [X]         │ │
│ │ 30/01/2026 10:15                              │ │
│ └───────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────┘
```

### ⚙️ FUNÇÕES PRINCIPAIS

#### `handleAddNote()`
**Validação:**
```typescript
if (currentNote.trim() === '') return
```

**Processo:**
1. Criar objeto Note:
   - id = Date.now()
   - content = currentNote
   - timestamp = new Date().toLocaleString()
2. Adicionar no INÍCIO do array (mais recente primeiro)
3. Limpar campo de texto
4. Salvar no localStorage

#### `handleDeleteNote(id)`
**Processo:**
1. Confirmar exclusão
2. Filtrar nota do array
3. Salvar no localStorage

### 🔄 CICLO DE VIDA

**Ao montar / trocar notesId:**
```
1. Carregar notes do localStorage[notesId]
2. Se não existe → array vazio
```

**Ao salvar:**
```
1. Atualizar estado local
2. Salvar localStorage[notesId] = JSON.stringify(notes)
```

### 📝 REGRAS DE NEGÓCIO

1. ✅ **Não permite notas vazias** (trim)
2. ✅ **Notas independentes por ficha**
3. ✅ **Ordenação: mais recente primeiro**
4. ✅ **Timestamp automático**
5. ✅ **Confirmação para deletar**
6. ✅ **Preserva quebras de linha** (whitespace-pre-wrap)

---

## 5. SheetManager.tsx

### 📌 RESPONSABILIDADE
Interface para visualizar e gerenciar todas as fichas criadas.

### 🎯 O QUE FAZ
- Listar todas as fichas em cards
- Destacar ficha ativa
- Permitir seleção de ficha
- Permitir exclusão de ficha

### 📊 PROPS
```typescript
sheets: CharacterSheetData[]
activeSheetId: string | null
onSelectSheet: (id: string) => void
onDeleteSheet: (id: string) => void
```

### 🎨 LAYOUT

```
┌──────────────────────────────────────────────────┐
│ Minhas Fichas                                    │
│                                                  │
│ ┌──────────────────────────────────────────────┐ │
│ │ Aragorn                          [🗑️]        │ │ ← Ativa
│ │ Jogador: João                                │ │
│ └──────────────────────────────────────────────┘ │
│                                                  │
│ ┌──────────────────────────────────────────────┐ │
│ │ Gandalf                          [🗑️]        │ │
│ │ Jogador: Maria                               │ │
│ └──────────────────────────────────────────────┘ │
│                                                  │
│ ┌──────────────────────────────────────────────┐ │
│ │ Legolas                          [🗑️]        │ │
│ │ Jogador: Pedro                               │ │
│ └──────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────┘
```

### 🎨 ESTILOS VISUAIS

#### Ficha Ativa
- Fundo: vermelho claro (red-50)
- Borda: vermelha (red-500)
- Sombra: média

#### Ficha Inativa
- Fundo: cinza claro (gray-50)
- Borda: cinza (gray-200)
- Hover: borda vermelha clara + sombra grande

### ⚙️ INTERAÇÕES

#### Click no Card
**Ação:** Selecionar ficha e ir para aba "Ficha"  
**Implementação:** `onClick={() => onSelectSheet(sheet.id)}`

#### Click no Botão Delete
**Ação:** Deletar ficha  
**Implementação:** `onClick={(e) => { e.stopPropagation(); onDeleteSheet(sheet.id); }}`  
**Nota:** stopPropagation evita que também selecione a ficha

### 📊 INFORMAÇÕES EXIBIDAS

```typescript
Nome do Personagem: sheet.character || 'Personagem Sem Nome'
Nome do Jogador: sheet.player || 'N/A'
Indicador Visual: activeSheetId === sheet.id
```

### 📝 REGRAS DE NEGÓCIO

1. ✅ **Destaque visual da ficha ativa**
2. ✅ **Click no card seleciona ficha**
3. ✅ **Delete não seleciona ficha** (stopPropagation)
4. ✅ **Fallbacks para dados vazios**
5. ✅ **Responsivo para mobile/desktop**

---

## 6. Prospeccao.tsx

### 📌 RESPONSABILIDADE
Contador auxiliar para rastreamento de recursos de prospecção durante o jogo.

### 🎯 O QUE FAZ
- Permite selecionar tipo de dado (d3 a d12)
- Incrementar/decrementar contador
- Limitar contador ao máximo do dado
- Resetar ao trocar de dado

### 📊 ESTADO
```typescript
selectedDie: number    // 3, 4, 6, 8, 10 ou 12
count: number          // 0 a selectedDie
```

### 🎨 LAYOUT

```
┌───────────────────────────────────────┐
│ PROSPECÇÃO                            │
├───────────────────────────────────────┤
│ Dado de Prospecção                    │
│ [d3] [d4] [d6] [d8] [d10] [d12]      │
│                                       │
│ Contador (Max: 12)                    │
│       [-]    [ 5 ]    [+]            │
└───────────────────────────────────────┘
```

### ⚙️ FUNÇÕES PRINCIPAIS

#### `handleDieSelect(sides)`
**Processo:**
1. Atualizar selectedDie
2. Resetar count para 0

#### `handleCountChange(amount)`
**Processo:**
1. Calcular novo valor: count + amount
2. Limitar entre 0 e selectedDie
3. Atualizar count

**Limites:**
```typescript
if (newValue < 0) return 0
if (newValue > selectedDie) return selectedDie
return newValue
```

### 🎨 ESTILOS VISUAIS

#### Dado Selecionado
- Fundo: vermelho (red-600)
- Anel: vermelho (ring-red-400)
- Texto: branco, bold

#### Dado Não Selecionado
- Fundo: cinza (gray-200)
- Hover: cinza escuro (gray-300)
- Texto: preto, bold

#### Botões +/-
- Circular
- Cinza com hover
- Desabilitado quando no limite

### 📝 REGRAS DE NEGÓCIO

1. ✅ **Contador reseta ao trocar dado**
2. ✅ **Máximo = lados do dado**
3. ✅ **Mínimo = 0**
4. ✅ **Botões desabilitados nos limites**
5. ✅ **Oculto na impressão**
6. ✅ **Não persiste entre sessões** (estado local)

---

## 7. DiceRoller.tsx

### 📌 STATUS
❌ **ARQUIVO VAZIO - NÃO IMPLEMENTADO**

### 🎯 POSSÍVEL OBJETIVO FUTURO
Sistema para rolar dados durante o jogo (especulação).

---

## 📊 MATRIZ DE DEPENDÊNCIAS ENTRE ARQUIVOS

```
App.tsx
├─→ CharacterSheet.tsx
│   └─→ Prospeccao.tsx
├─→ Gallery.tsx
├─→ Notes.tsx
├─→ SheetManager.tsx
└─→ DiceRoller.tsx (vazio)
```

---

## 🔄 FLUXO DE DADOS ENTRE COMPONENTES

```
                    ┌─────────────┐
                    │   App.tsx   │
                    │   (Estado   │
                    │    Global)  │
                    └──────┬──────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
         ↓                 ↓                 ↓
  ┌──────────┐      ┌──────────┐     ┌──────────┐
  │ Character│      │ Gallery  │     │  Notes   │
  │  Sheet   │      │          │     │          │
  └────┬─────┘      └─────┬────┘     └────┬─────┘
       │                  │                │
       │                  │                │
       ↓                  ↓                ↓
  localStorage      localStorage     localStorage
  allSheets      character_{id}     notes_{id}
                 items_{id}
```

---

## 📝 CHECKLIST DE IMPLEMENTAÇÃO POR ARQUIVO

### App.tsx
- [ ] Sistema de abas (5)
- [ ] Gerenciamento de estado de fichas
- [ ] Persistência no localStorage
- [ ] CRUD de fichas
- [ ] Exportação PDF
- [ ] Header e navegação
- [ ] Renderização condicional

### CharacterSheet.tsx
- [ ] 170+ campos de entrada
- [ ] 50+ cálculos automáticos
- [ ] Sistema de validação de pontos
- [ ] Upload de imagem
- [ ] Integração com IA (interpretação)
- [ ] Layout responsivo
- [ ] Estilos de impressão
- [ ] Componente Prospeccao integrado

### Gallery.tsx
- [ ] Upload múltiplo de imagens
- [ ] Validação de tamanho
- [ ] Conversão Base64
- [ ] Grid de miniaturas
- [ ] Visualização detalhada
- [ ] Delete de imagens
- [ ] Integração com IA (análise)
- [ ] Persistência por ficha

### Notes.tsx
- [ ] Campo de texto para nota
- [ ] Validação (não vazio)
- [ ] Timestamp automático
- [ ] Lista de notas
- [ ] Delete de notas
- [ ] Persistência por ficha
- [ ] Preservação de formatação

### SheetManager.tsx
- [ ] Lista de fichas
- [ ] Destaque de ficha ativa
- [ ] Seleção de ficha
- [ ] Delete de ficha
- [ ] Fallbacks de dados
- [ ] Responsividade

### Prospeccao.tsx
- [ ] Seleção de dado
- [ ] Contador com limites
- [ ] Botões +/-
- [ ] Reset ao trocar dado
- [ ] Estados desabilitados

---

**Documento de Análise por Arquivo**  
**Versão:** 1.0  
**Data:** Janeiro 2026  
**Projeto:** Klayrah RPG - Migração para Angular
