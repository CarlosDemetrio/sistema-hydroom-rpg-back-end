# ANÁLISE DE REQUISITOS - KLAYRAH RPG

## VISÃO GERAL DO SISTEMA

O sistema Klayrah RPG é uma aplicação web para gerenciamento de fichas de personagens de RPG (Role-Playing Game). Permite criar, editar, visualizar e exportar múltiplas fichas de personagens, além de funcionalidades auxiliares como galeria de imagens, anotações e integração com IA para análise e sugestões.

---

## 1. ARQUIVO: App.tsx (APLICAÇÃO PRINCIPAL)

### 1.1 OBJETIVO
Componente principal que gerencia a navegação entre abas, o estado global das fichas de personagens e a persistência de dados.

### 1.2 FUNCIONALIDADES

#### 1.2.1 Sistema de Navegação por Abas
- **O que faz:** Permite alternar entre 5 abas diferentes
- **Abas disponíveis:**
  - "Ficha" - Visualização e edição da ficha de personagem ativa
  - "Meu Personagem" - Galeria de imagens do personagem
  - "Meus Itens" - Galeria de imagens dos itens do personagem
  - "Anotações" - Bloco de notas para o personagem
  - "Minhas Fichas" - Gerenciador de todas as fichas criadas

#### 1.2.2 Gerenciamento de Fichas Múltiplas
- **O que faz:** Permite criar e gerenciar múltiplas fichas de personagens simultaneamente
- **Regras de negócio:**
  - Cada ficha possui um ID único gerado pelo timestamp
  - Sempre deve existir pelo menos uma ficha no sistema
  - Ao deletar a última ficha, uma nova é criada automaticamente
  - Uma ficha está sempre "ativa" (sendo visualizada/editada)

#### 1.2.3 Persistência de Dados (LocalStorage)
- **O que faz:** Salva automaticamente todas as fichas e o ID da ficha ativa no navegador
- **Dados persistidos:**
  - Lista completa de todas as fichas criadas
  - ID da ficha atualmente ativa
- **Regras de negócio:**
  - Os dados são carregados ao iniciar a aplicação
  - Os dados são salvos automaticamente a cada alteração
  - Se não houver dados salvos, cria uma ficha padrão chamada "Meu Primeiro Personagem"

#### 1.2.4 Criação de Nova Ficha
- **O que faz:** Cria uma nova ficha de personagem com valores padrão
- **Comportamento:**
  - Gera um ID único usando timestamp
  - Usa valores padrão predefinidos (DEFAULT_SHEET)
  - Automaticamente seleciona a nova ficha como ativa
  - Redireciona para a aba "Ficha"

#### 1.2.5 Seleção de Ficha Ativa
- **O que faz:** Permite trocar qual ficha está sendo visualizada/editada
- **Comportamento:**
  - Define a ficha selecionada como ativa
  - Redireciona para a aba "Ficha"

#### 1.2.6 Exclusão de Ficha
- **O que faz:** Remove uma ficha do sistema
- **Regras de negócio:**
  - Solicita confirmação antes de deletar
  - Se a ficha deletada for a ativa, seleciona a primeira ficha restante
  - Se for a última ficha, cria uma nova automaticamente
  - Não é possível ficar sem nenhuma ficha no sistema

#### 1.2.7 Exportação de Ficha em PDF
- **O que faz:** Gera e baixa um PDF da ficha ativa
- **Comportamento:**
  - Usa a biblioteca html2pdf.js
  - Captura o elemento HTML com ID "character-sheet-container"
  - Nome do arquivo: nome do personagem (sanitizado) + ".pdf"
  - Configurações: A4, orientação retrato, margem 0.5in, qualidade de imagem 98%

#### 1.2.8 Interface do Cabeçalho
- **Elementos:**
  - Logo "Klayrah" (texto vermelho)
  - Botão "Nova Ficha" (verde com ícone +)
  - Botão "Baixar Ficha como PDF" (vermelho com ícone de download)
- **Visibilidade:** Oculto na impressão/PDF

#### 1.2.9 Navegação Responsiva
- **O que faz:** Interface adaptável para diferentes tamanhos de tela
- **Comportamento:**
  - Abas visíveis em todas as telas
  - Layout responsivo usando Tailwind CSS
  - Classes print:hidden para ocultar elementos na impressão

---

## 2. ARQUIVO: CharacterSheet.tsx (FICHA DE PERSONAGEM)

### 2.1 OBJETIVO
Componente complexo que exibe e gerencia todos os dados de uma ficha de personagem, incluindo atributos, aptidões, vida, essência e cálculos automáticos.

### 2.2 ESTRUTURA DE DADOS

#### 2.2.1 Dados Pessoais
- **Jogador** (texto livre)
- **Personagem** (texto livre, nome do personagem)
- **Título Heróico** (texto livre)
- **Insólitus** (texto livre)
- **Origem** (texto livre)
- **Gênero** (seleção: Masculino, Feminino, Outro)
- **Classe** (seleção predefinida + campo customizado)

#### 2.2.2 Classes Disponíveis
- Guerreiro
- Arqueiro
- Monge
- Berserker
- Assassino
- Fauno (Herdeiro)
- Mago
- Feiticeiro
- Necromance
- Sacerdote
- Ladrão
- Negociante
- Outra... (permite digitar nome customizado)

#### 2.2.3 Descrição Física
- **Idade** (número)
- **Altura em cm** (número)
- **Peso em kg** (número)
- **Cor do Cabelo** (texto livre)
- **Tamanho do Cabelo** (texto livre)
- **Cor dos Olhos** (texto livre)

#### 2.2.4 Personalidade
- **Índole** (seleção: Bom, Mau, Neutro)
- **Presença** (seleção: Bom, Leal, Caótico, Neutro)
- **Arquétipo de Referência** (texto livre)

#### 2.2.5 Atributos Principais
Cada atributo possui 4 campos:
- **BASE** - Valor base do atributo
- **NÍVEL** - Pontos distribuídos por nível
- **OUTROS** - Bônus diversos
- **TOTAL** - Soma automática de BASE + NÍVEL + OUTROS
- **ÍMPETO** - Cálculo derivado do total

**Atributos:**
1. **Força** - Ímpeto = Total × 3 kg (capacidade de carga)
2. **Agilidade** - Ímpeto = Total ÷ 3 metros (deslocamento)
3. **Vigor** - Ímpeto = Total ÷ 10 (Redução de Dano físico)
4. **Sabedoria** - Ímpeto = Total ÷ 10 (Redução de Dano Mágico)
5. **Intuição** - Ímpeto = min(Total ÷ 20, 3) (Pontos de Sorte)
6. **Inteligência** - Ímpeto = Total ÷ 20 (Comando)
7. **Astúcia** - Ímpeto = Total ÷ 10 (Estratégia)

### 2.3 SISTEMA DE DESENVOLVIMENTO

#### 2.3.1 Sistema de Experiência e Níveis
- **O que faz:** Calcula automaticamente o nível baseado na experiência
- **Tabela de XP:** 35 níveis, iniciando em 0 XP (nível 0)
  - Nível 1: 1.000 XP
  - Nível 2: 3.000 XP
  - Nível 5: 15.000 XP
  - Nível 10: 55.000 XP
  - Nível 20: 210.000 XP
  - Nível 35: 595.000 XP
- **Regra de negócio:** Ao inserir XP, o nível é calculado automaticamente

#### 2.3.2 Sistema de Distribuição de Pontos de Atributo
- **Regra:** 3 pontos por nível para distribuir no campo "NÍVEL" dos atributos
- **Validação:** 
  - Sistema notifica se há pontos disponíveis para distribuir
  - Sistema alerta se pontos foram gastos em excesso
  - Notificação exibe: pontos esperados vs pontos distribuídos
  - Cor amarela para alertas visuais

#### 2.3.3 Sistema de Limitador
- **O que faz:** Define o valor máximo que um atributo pode atingir
- **Regras:**
  - Nível 0-1: Limitador = 10
  - Nível 2-20: Limitador = 50
  - Nível 21-25: Limitador = 75
  - Nível 26-30: Limitador = 100
  - Nível 31-35: Limitador = 120
  - Acima de 35: "Renascimento"

#### 2.3.4 Sistema de Renascimentos
- **O que faz:** Contador de renascimentos do personagem
- **Influência:** Afeta Vida, Essência e Ameaça

### 2.4 SISTEMA DE BÔNUS

#### 2.4.1 Cálculos Base (derivados dos atributos)
- **B.B.A** (Bônus Base de Ataque) = (Força + Agilidade) ÷ 3
- **Bloqueio** = (Força + Vigor) ÷ 3
- **Reflexo** = (Agilidade + Astúcia) ÷ 3
- **B.B.M** (Bônus Base Mágico) = (Sabedoria + Inteligência) ÷ 3
- **Percep** (Percepção) = (Inteligência + Intuição) ÷ 3
- **Racionc.** (Raciocínio) = (Inteligência + Astúcia) ÷ 3

#### 2.4.2 Bônus Adicionais (para cada cálculo acima)
Cada bônus pode receber acréscimos de 5 fontes:
- **Vantagens** (número)
- **Classe** (número)
- **Itens** (número)
- **Glória** (número)
- **Outros** (número)
- **Total** = Base + soma de todos os bônus adicionais

### 2.5 SISTEMA DE APTIDÕES

#### 2.5.1 Estrutura de Aptidões
Cada aptidão possui 3 campos + total calculado:
- **BASE** - Valor base da aptidão
- **SORTE** - Bônus de sorte
- **CLASSE** - Bônus de classe
- **TOTAL** - Soma automática

#### 2.5.2 Aptidões Físicas (12 total)
1. Acrobacia
2. Guarda
3. Aparar
4. Atletismo
5. Resvalar
6. Resistência
7. Perseguição
8. Natação
9. Furtividade
10. Prestidigitação
11. Conduzir
12. Arte da Fuga

#### 2.5.3 Aptidões Mentais (12 total)
1. Idiomas
2. Observação
3. Falsificar
4. Prontidão
5. Auto Controle
6. Sentir Motivação
7. Sobrevivência
8. Investigar
9. Blefar
10. Atuação
11. Diplomacia
12. Operação de Mecanismos

### 2.6 SISTEMA DE VIDA

#### 2.6.1 Cálculo de Vida Total
**Fórmula:** VG + Nível + VT + Renascimentos + OUT

**Componentes:**
- **VG** (Vigor) = Total do atributo Vigor
- **NV** (Nível) = Nível atual do personagem
- **VT** (Vantagens) = Bônus de vantagens (campo editável)
- **RN** (Renascimentos) = Número de renascimentos
- **OUT** (Outros) = Outros bônus (campo editável)

#### 2.6.2 Sistema de Dano por Membro
Cada membro tem uma porcentagem da vida total:

| Membro | Porcentagem | Cálculo |
|--------|-------------|---------|
| Cabeça | 75% | Vida Total × 0.75 |
| Tronco | 100% | Vida Total × 1.0 |
| Braço Direito | 25% | Vida Total × 0.25 |
| Braço Esquerdo | 25% | Vida Total × 0.25 |
| Perna Direita | 25% | Vida Total × 0.25 |
| Perna Esquerda | 25% | Vida Total × 0.25 |
| Sangue | 100% | Vida Total × 1.0 |

**Regra de negócio:**
- Cada membro tem um campo "Danos" editável
- Valor exibido = Valor Base - Danos
- Permite rastrear dano recebido em cada parte do corpo

### 2.7 SISTEMA DE ESSÊNCIA

#### 2.7.1 Cálculo de Essência Total
**Fórmula:** (V+S)/2 + Nível + Renasc + Vant + Outros

**Componentes:**
- **V+S/2** = (Vigor + Sabedoria) ÷ 2
- **Nível** = Nível atual
- **Renasc** = Bônus de renascimentos (campo editável)
- **Vant** = Bônus de vantagens (campo editável)
- **Outros** = Outros bônus (campo editável)

#### 2.7.2 Sistema de Gastos de Essência
- **Gastos** (campo editável) - Essência já utilizada
- **Essência Restante** = Total - Gastos
- **Display:** Mostra "Restante / Total"

### 2.8 SISTEMA DE AMEAÇA

#### 2.8.1 Cálculo de Ameaça Total
**Fórmula:** Nível + Itens + Títulos + Renascimentos + Outros

**Componentes:**
- **Nível** = Nível atual (apenas exibição)
- **Itens** = Bônus de itens (campo editável)
- **Títulos** = Bônus de títulos (campo editável)
- **Renasc.** = Renascimentos (apenas exibição)
- **Outros** = Outros bônus (campo editável)

### 2.9 SISTEMA DE PROSPECÇÃO

#### 2.9.1 Funcionalidade
- **O que faz:** Contador para rastrear recursos de prospecção
- **Dados disponíveis:** d3, d4, d6, d8, d10, d12

#### 2.9.2 Comportamento
- Usuário seleciona o tipo de dado (d3 a d12)
- Contador inicia em 0
- Botão "-" decrementa (mínimo 0)
- Botão "+" incrementa (máximo = lados do dado)
- Display grande mostra o valor atual
- Oculto na impressão

### 2.10 SISTEMA DE IMAGEM DO PERSONAGEM

#### 2.10.1 Upload de Imagem
- **O que faz:** Permite fazer upload de uma imagem do personagem
- **Regras:**
  - Limite de tamanho: 20MB
  - Formatos aceitos: qualquer formato de imagem
  - Exibição: circular, 192px × 192px, borda vermelha
  - Armazenamento: Base64 no localStorage

#### 2.10.2 Cálculo Automático de Peso
- **Regra de negócio:** Ao alterar altura ou gênero, o peso é recalculado automaticamente
- **Fórmula:** BMI × (altura em metros)²
- **BMI Base:**
  - Feminino: 21
  - Masculino: 22.5
  - Outro: mantém valor atual

### 2.11 INTEGRAÇÃO COM IA (GEMINI)

#### 2.11.1 Sugestão de Interpretação de Personagem
- **O que faz:** Gera sugestão de como interpretar o personagem baseado nos traços
- **Dados enviados:**
  - Índole
  - Presença
  - Arquétipo de Referência
- **Prompt:** "Baseado nos seguintes traços de um personagem de RPG, forneça uma sugestão curta e criativa de como interpretá-lo"
- **Modelo:** gemini-2.5-flash
- **Comportamento:**
  - Botão "Gerar Sugestão de Interpretação"
  - Exibe loading durante processamento
  - Mostra resultado em área de texto
  - Tratamento de erros com mensagem ao usuário

### 2.12 SISTEMA DE IMPRESSÃO/PDF

#### 2.12.1 Otimizações para Impressão
- **Classes CSS especiais:**
  - `print-hidden` - Oculta elemento na impressão
  - `print-single-column` - Força layout de coluna única
  - `section-container` - Evita quebra de seção entre páginas
  - `print-break-before-page` - Força quebra de página antes
- **Elementos ocultos na impressão:**
  - Cabeçalho com botões
  - Navegação de abas
  - Botão de IA
  - Sistema de Prospecção
  - Notificações de pontos de atributo

---

## 3. ARQUIVO: Gallery.tsx (GALERIA DE IMAGENS)

### 3.1 OBJETIVO
Componente para gerenciar galeria de imagens (personagem ou itens) com análise de IA.

### 3.2 FUNCIONALIDADES

#### 3.2.1 Upload de Múltiplas Imagens
- **O que faz:** Permite fazer upload de uma ou várias imagens simultaneamente
- **Regras:**
  - Limite por arquivo: 20MB
  - Formatos aceitos: image/*
  - Conversão para Base64
  - Armazenamento no localStorage

#### 3.2.2 Validação de Arquivos
- **Comportamento:**
  - Verifica tamanho antes do upload
  - Lista arquivos que excedem o limite
  - Exibe alerta com nomes dos arquivos rejeitados
  - Não permite upload de arquivos muito grandes

#### 3.2.3 Identificação Única de Galeria
- **Regra de negócio:** Cada ficha tem 2 galerias independentes:
  - `character_{id}` - Galeria do personagem
  - `items_{id}` - Galeria de itens
- **Comportamento:** Ao trocar de ficha, carrega as imagens corretas

#### 3.2.4 Armazenamento de Imagens
Cada imagem possui:
- **id** - Timestamp + nome do arquivo
- **src** - Imagem em Base64
- **name** - Nome original do arquivo
- **analysis** - Texto da análise da IA (opcional)

#### 3.2.5 Interface de Galeria
- **Layout:**
  - Coluna esquerda: Grid de miniaturas (2-3 colunas)
  - Coluna direita: Visualização detalhada
- **Miniaturas:**
  - Imagem 96px de altura
  - Clicável para seleção
  - Borda vermelha quando selecionada
  - Hover com borda vermelha clara

#### 3.2.6 Visualização Detalhada
- **Elementos:**
  - Imagem grande (máx 192px altura)
  - Nome do arquivo
  - Botão "Delete" (cinza, texto vermelho)
  - Botão "Analyze with Gemini" (azul)
  - Área de análise da IA

#### 3.2.7 Exclusão de Imagens
- **O que faz:** Remove imagem da galeria
- **Regras:**
  - Solicita confirmação
  - Remove do localStorage
  - Se a imagem excluída estiver selecionada, limpa a seleção

#### 3.2.8 Análise de Imagem com IA (Gemini)

##### 3.2.8.1 Prompt para Itens
"Describe this item from a fantasy RPG. What could it be? What are its potential powers or history? Be creative."

##### 3.2.8.2 Prompt para Personagem
"Describe this character or scene. What is happening? What is the mood? What story does this image tell?"

##### 3.2.8.3 Comportamento
- Converte imagem Base64 para formato Gemini
- Detecta MIME type automaticamente
- Exibe "Analyzing..." durante processamento
- Salva análise junto com a imagem
- Análise persiste no localStorage
- Tratamento de erros

#### 3.2.9 Estado de Loading
- **Upload:** Botão mostra "Enviando..." e fica desabilitado
- **Análise:** Botão mostra "Analyzing..." e fica desabilitado
- **Visual:** Opacidade reduzida durante processamento

---

## 4. ARQUIVO: Notes.tsx (ANOTAÇÕES)

### 4.1 OBJETIVO
Sistema de anotações organizado por ficha de personagem.

### 4.2 FUNCIONALIDADES

#### 4.2.1 Estrutura de Nota
Cada nota contém:
- **id** - Timestamp único
- **content** - Conteúdo da anotação (texto livre)
- **timestamp** - Data/hora de criação formatada

#### 4.2.2 Identificação por Ficha
- **Regra:** Cada ficha tem seu próprio conjunto de notas
- **Storage key:** `notes_{id}` onde id é o ID da ficha
- **Comportamento:** Ao trocar de ficha, carrega as notas corretas

#### 4.2.3 Criação de Nota
- **Interface:** Textarea grande (altura 128px)
- **Validação:** Não permite salvar notas vazias (trim)
- **Comportamento:**
  - Gera timestamp automático
  - Adiciona no topo da lista (mais recente primeiro)
  - Limpa o campo de texto após salvar
  - Salva no localStorage

#### 4.2.4 Exibição de Notas
- **Layout:**
  - Lista vertical com espaçamento
  - Cada nota em card separado
  - Fundo cinza claro
  - Borda cinza
  - Sombra suave

#### 4.2.5 Conteúdo da Nota
- **Texto:** 
  - Preserva quebras de linha (whitespace-pre-wrap)
  - Cor cinza escuro
  - Scroll automático se muito longo
- **Timestamp:**
  - Texto pequeno
  - Cor cinza claro
  - Formato de locale do navegador

#### 4.2.6 Exclusão de Nota
- **Interface:** Botão X no canto superior direito
- **Estilo:** 
  - Cinza claro normal
  - Vermelho no hover
  - Ícone SVG de círculo com X
- **Comportamento:**
  - Solicita confirmação
  - Remove do localStorage
  - Atualiza lista imediatamente

#### 4.2.7 Estado Vazio
- **Mensagem:** "No notes yet. Add one above!"
- **Estilo:** Texto cinza claro

---

## 5. ARQUIVO: SheetManager.tsx (GERENCIADOR DE FICHAS)

### 5.1 OBJETIVO
Interface para visualizar, selecionar e gerenciar todas as fichas de personagens.

### 5.2 FUNCIONALIDADES

#### 5.2.1 Listagem de Fichas
- **O que faz:** Exibe todas as fichas criadas em cards
- **Informações exibidas:**
  - Nome do personagem (ou "Personagem Sem Nome")
  - Nome do jogador (ou "N/A")
  - Indicador visual da ficha ativa

#### 5.2.2 Visual da Ficha Ativa
- **Destaque:**
  - Fundo vermelho claro (red-50)
  - Borda vermelha (red-500)
  - Sombra média
- **Ficha não ativa:**
  - Fundo cinza claro (gray-50)
  - Borda cinza (gray-200)
  - Hover: sombra grande e borda vermelha clara

#### 5.2.3 Seleção de Ficha
- **Comportamento:**
  - Clique em qualquer parte do card (exceto botão delete)
  - Define como ficha ativa
  - Redireciona para aba "Ficha"
  - Cursor pointer para indicar clicabilidade

#### 5.2.4 Exclusão de Ficha
- **Interface:**
  - Botão circular no lado direito
  - Ícone de lixeira (trash)
  - Cinza normal, vermelho no hover
  - aria-label para acessibilidade

#### 5.2.5 Comportamento do Botão Delete
- **Regra:** stopPropagation no click
- **Motivo:** Evita que o clique no delete também selecione a ficha
- **Ação:** Chama função de delete do componente pai

#### 5.2.6 Estado Vazio
- **Mensagem:** "Nenhuma ficha encontrada. Clique em 'Nova Ficha' para criar uma."
- **Estilo:** Texto cinza claro
- **Observação:** Na prática, nunca deveria aparecer devido à regra de negócio

#### 5.2.7 Responsividade
- **Layout:** Lista vertical com espaçamento
- **Cards:** Largura completa, altura automática
- **Grid interno:** Flexbox com espaço entre elementos

---

## 6. ARQUIVO: Prospeccao.tsx (SISTEMA DE PROSPECÇÃO)

### 6.1 OBJETIVO
Componente auxiliar para rastrear recursos de prospecção durante o jogo.

### 6.2 FUNCIONALIDADES

#### 6.2.1 Seleção de Tipo de Dado
- **Opções disponíveis:** d3, d4, d6, d8, d10, d12
- **Interface:** Grid de 6 botões (3×2 em mobile, 1×6 em desktop)
- **Visual:**
  - Dado selecionado: vermelho com anel vermelho
  - Não selecionado: cinza com hover cinza escuro
  - Fonte bold

#### 6.2.2 Comportamento ao Trocar Dado
- **Regra:** Contador reseta para 0
- **Motivo:** Cada dado tem limite diferente

#### 6.2.3 Sistema de Contador
- **Valor inicial:** 0
- **Valor mínimo:** 0
- **Valor máximo:** Igual ao número de lados do dado selecionado
- **Display:** Número grande (text-5xl) em vermelho

#### 6.2.4 Botões de Controle
- **Botão "-":**
  - Decrementa em 1
  - Desabilitado quando contador = 0
  - Cinza claro com hover cinza
  - Circular
  
- **Botão "+":**
  - Incrementa em 1
  - Desabilitado quando contador = máximo do dado
  - Cinza claro com hover cinza
  - Circular

#### 6.2.5 Indicadores Visuais
- **Label:** "Dado de Prospecção"
- **Display de máximo:** "Contador (Max: X)"
- **Área do contador:** Fundo cinza claro, borda, arredondado

#### 6.2.6 Integração
- **Localização:** Coluna direita da ficha, após Essência
- **Visibilidade:** Oculto na impressão (print-hidden)
- **Persistência:** Não persiste entre sessões (estado local apenas)

---

## 7. ARQUIVO: DiceRoller.tsx (ROLADOR DE DADOS)

### 7.1 STATUS
Arquivo existe mas está vazio - funcionalidade não implementada.

### 7.2 POSSÍVEL OBJETIVO FUTURO
Sistema para rolar dados durante o jogo (não especificado).

---

## 8. REQUISITOS TÉCNICOS

### 8.1 ARMAZENAMENTO DE DADOS

#### 8.1.1 LocalStorage Keys
- `allCharacterSheets` - Array de todas as fichas
- `activeCharacterSheetId` - ID da ficha ativa
- `character_{id}` - Galeria de imagens do personagem
- `items_{id}` - Galeria de imagens dos itens
- `notes_{id}` - Anotações do personagem

#### 8.1.2 Formato de Dados
- JSON serializado para armazenamento
- Parse ao carregar
- Imagens em Base64

### 8.2 DEPENDÊNCIAS EXTERNAS

#### 8.2.1 Biblioteca de IA
- **Nome:** @google/genai
- **Versão:** ^1.29.0
- **Uso:** Análise de imagens e sugestões de interpretação

#### 8.2.2 Biblioteca de PDF
- **Nome:** html2pdf.js
- **Versão:** 0.10.1
- **CDN:** cdnjs.cloudflare.com
- **Uso:** Exportação de ficha em PDF

#### 8.2.3 Framework CSS
- **Nome:** Tailwind CSS
- **Versão:** CDN
- **Uso:** Toda a estilização da aplicação

#### 8.2.4 Configuração de API
- **Variável de ambiente:** API_KEY
- **Arquivo:** .env.local
- **Formato:** GEMINI_API_KEY=sua_chave_aqui

### 8.3 REGRAS DE FORMATAÇÃO

#### 8.3.1 Números
- Inputs numéricos com value || 0 (zero como padrão)
- Math.floor() para cálculos que devem ser inteiros
- Math.min() para limitar valores máximos
- Math.round() para peso calculado

#### 8.3.2 Strings
- trim() para validação de texto
- replace() para sanitização de nomes de arquivo
- toLowerCase() para nomes de arquivo PDF

#### 8.3.3 Timestamps
- Date.now() para IDs únicos
- toLocaleString() para exibição de data/hora

---

## 9. REGRAS DE NEGÓCIO TRANSVERSAIS

### 9.1 VALIDAÇÕES

#### 9.1.1 Validação de Upload
- Limite de 20MB por arquivo
- Verificação antes do processamento
- Mensagem de erro com lista de arquivos rejeitados

#### 9.1.2 Validação de Pontos de Atributo
- Notificação visual se pontos disponíveis
- Alerta se pontos excedidos
- Cor amarela para chamar atenção

#### 9.1.3 Validação de Notas
- Não permite salvar notas vazias
- trim() para remover espaços

### 9.2 CONFIRMAÇÕES

#### 9.2.1 Exclusões Requerem Confirmação
- Deletar ficha: "Tem certeza que deseja apagar esta ficha? Essa ação não pode ser desfeita."
- Deletar imagem: "Are you sure you want to delete this image?"
- Deletar nota: "Are you sure you want to delete this note?"

### 9.3 CÁLCULOS AUTOMÁTICOS

#### 9.3.1 Cálculos em Tempo Real
- Total de atributos (soma de 3 campos)
- Ímpeto de atributos (baseado no total)
- Bônus base (média de 2 atributos)
- Total de bônus (base + 5 modificadores)
- Total de aptidões (soma de 3 campos)
- Vida total (soma de 5 componentes)
- Vida por membro (porcentagem - danos)
- Essência total (fórmula específica)
- Essência restante (total - gastos)
- Ameaça total (soma de 5 componentes)
- Nível baseado em XP (tabela de referência)
- Peso baseado em altura e gênero (BMI)

### 9.4 SINCRONIZAÇÃO DE DADOS

#### 9.4.1 Auto-save
- Toda alteração é salva imediatamente no localStorage
- Não há botão "Salvar"
- Não há risco de perder dados não salvos

#### 9.4.2 Auto-load
- Dados carregados automaticamente ao iniciar
- Ao trocar de ficha, dados carregados automaticamente
- Galerias e notas específicas da ficha ativa

---

## 10. REQUISITOS DE INTERFACE

### 10.1 CORES E ESTILO

#### 10.1.1 Paleta de Cores Principal
- **Vermelho (tema):** #DC2626 (red-600)
- **Vermelho hover:** #B91C1C (red-700)
- **Vermelho claro:** #FEF2F2 (red-50)
- **Verde (nova ficha):** #059669 (green-600)
- **Azul (IA):** #2563EB (blue-600)
- **Cinza (background):** #F3F4F6 (gray-100)
- **Cinza escuro (texto):** #1F2937 (gray-800)
- **Branco:** #FFFFFF

#### 10.1.2 Títulos de Seção
- **SectionTitle:** Fundo preto, texto branco, uppercase, tracking-wider
- **RedSectionTitle:** Fundo vermelho, texto branco, uppercase, tracking-wider

#### 10.1.3 Campos de Input
- Fundo cinza claro
- Borda cinza
- Texto centralizado
- Focus: anel vermelho (ring-red-500)
- Padding: px-2 py-1

### 10.2 RESPONSIVIDADE

#### 10.2.1 Breakpoints
- **Mobile:** Layout de coluna única
- **Tablet (md):** Grid 2-3 colunas conforme necessário
- **Desktop (lg):** Layout completo de 2 colunas para ficha

#### 10.2.2 Impressão
- Layout otimizado para A4
- Elementos de UI ocultos
- Força coluna única em seções principais
- Evita quebra de seção entre páginas

### 10.3 ACESSIBILIDADE

#### 10.3.1 Elementos Implementados
- aria-label em botões de ícone
- Labels para todos os inputs
- Títulos hierárquicos adequados
- Foco visual em campos

#### 10.3.2 Feedback Visual
- Loading states em botões
- Disabled states visíveis
- Hover states em elementos clicáveis
- Cores de contraste adequadas

---

## 11. FLUXOS DE USUÁRIO PRINCIPAIS

### 11.1 FLUXO: Criar Nova Ficha
1. Usuário clica em "Nova Ficha"
2. Sistema cria ficha com valores padrão
3. Sistema define nova ficha como ativa
4. Sistema redireciona para aba "Ficha"
5. Usuário começa a preencher dados

### 11.2 FLUXO: Editar Ficha Existente
1. Usuário navega para "Minhas Fichas"
2. Usuário clica na ficha desejada
3. Sistema define ficha como ativa
4. Sistema redireciona para aba "Ficha"
5. Usuário edita campos
6. Sistema salva automaticamente cada alteração

### 11.3 FLUXO: Adicionar Imagem com Análise
1. Usuário navega para "Meu Personagem" ou "Meus Itens"
2. Usuário clica em "Upload Image(s)"
3. Sistema valida tamanho dos arquivos
4. Sistema converte para Base64
5. Sistema salva no localStorage
6. Usuário clica na imagem para selecioná-la
7. Usuário clica em "Analyze with Gemini"
8. Sistema envia imagem para API
9. Sistema exibe análise
10. Sistema salva análise com a imagem

### 11.4 FLUXO: Fazer Anotação
1. Usuário navega para "Anotações"
2. Usuário digita texto no campo
3. Usuário clica em "Save Note"
4. Sistema valida que não está vazio
5. Sistema adiciona timestamp
6. Sistema salva no topo da lista
7. Sistema limpa campo de texto

### 11.5 FLUXO: Exportar PDF
1. Usuário garante que está na aba "Ficha"
2. Usuário clica em "Baixar Ficha como PDF"
3. Sistema captura elemento HTML da ficha
4. Sistema converte para PDF
5. Sistema inicia download com nome do personagem

### 11.6 FLUXO: Ganhar Experiência
1. Usuário edita campo "Experiência"
2. Sistema calcula nível automaticamente
3. Sistema atualiza display de nível
4. Sistema recalcula limitador
5. Sistema mostra pontos de atributo disponíveis
6. Usuário distribui pontos nos atributos

---

## 12. CASOS DE USO ESPECIAIS

### 12.1 CASO: Última Ficha Deletada
1. Usuário tenta deletar a última ficha
2. Sistema mostra confirmação
3. Usuário confirma
4. Sistema deleta a ficha
5. Sistema detecta lista vazia
6. Sistema cria automaticamente nova ficha padrão
7. Sistema define nova ficha como ativa

### 12.2 CASO: Primeira Vez no Sistema
1. Usuário acessa aplicação pela primeira vez
2. Sistema não encontra dados no localStorage
3. Sistema cria ficha padrão "Meu Primeiro Personagem"
4. Sistema define como ficha ativa
5. Sistema exibe ficha para preenchimento

### 12.3 CASO: Arquivo Muito Grande no Upload
1. Usuário seleciona arquivos > 20MB
2. Sistema valida antes de processar
3. Sistema exibe alerta com nomes dos arquivos
4. Sistema não processa arquivos grandes
5. Sistema permite que usuário tente novamente

### 12.4 CASO: API do Gemini Falha
1. Usuário solicita análise de imagem ou interpretação
2. Sistema envia requisição para API
3. API retorna erro
4. Sistema captura erro
5. Sistema exibe mensagem amigável
6. Sistema registra erro no console
7. Usuário pode tentar novamente

### 12.5 CASO: Trocar de Ficha Durante Edição
1. Usuário está editando ficha A
2. Sistema salva automaticamente alterações
3. Usuário navega para "Minhas Fichas"
4. Usuário seleciona ficha B
5. Sistema salva referência da ficha ativa
6. Sistema carrega dados da ficha B
7. Sistema carrega galerias e notas da ficha B
8. Usuário pode voltar para ficha A sem perda de dados

---

## 13. RESUMO DE ENTIDADES E RELACIONAMENTOS

### 13.1 ENTIDADE: CharacterSheet (Ficha)
- **Atributos principais:** 50+ campos
- **Relacionamentos:**
  - 1:1 com Galeria de Personagem
  - 1:1 com Galeria de Itens
  - 1:N com Notas
- **Identificação:** ID único (timestamp)
- **Persistência:** localStorage

### 13.2 ENTIDADE: Gallery (Galeria)
- **Tipos:** Personagem ou Itens
- **Relacionamento:** N:1 com CharacterSheet
- **Contém:** N imagens
- **Identificação:** galleryId baseado no ID da ficha

### 13.3 ENTIDADE: Image (Imagem)
- **Relacionamento:** N:1 com Gallery
- **Atributos:** id, src (Base64), name, analysis
- **Identificação:** timestamp + nome do arquivo

### 13.4 ENTIDADE: Note (Nota)
- **Relacionamento:** N:1 com CharacterSheet
- **Atributos:** id, content, timestamp
- **Identificação:** timestamp único

---

## 14. CONSIDERAÇÕES PARA MIGRAÇÃO ANGULAR

### 14.1 ESTRUTURA DE COMPONENTES SUGERIDA
- AppComponent (componente raiz)
- CharacterSheetComponent (ficha)
- GalleryComponent (galeria reutilizável)
- NotesComponent (anotações)
- SheetManagerComponent (gerenciador)
- ProspeccaoComponent (prospecção)
- HeaderComponent (cabeçalho)
- NavigationComponent (abas)

### 14.2 SERVIÇOS NECESSÁRIOS
- **StorageService:** Gerenciar localStorage
- **SheetService:** Gerenciar estado das fichas
- **GeminiService:** Integração com API do Gemini
- **PdfService:** Exportação de PDF

### 14.3 MODELOS/INTERFACES
- CharacterSheetData
- Attribute, Attributes
- Aptidao
- BonusDetail, Bonuses
- ImageItem
- Note
- Ameaca

### 14.4 GUARDS E RESOLVERS
- Nenhum necessário (aplicação não tem autenticação)

### 14.5 ROTEAMENTO
- Não é necessário routing complexo
- Sistema de abas pode ser implementado com ngIf ou routing interno

### 14.6 DIRETIVAS CUSTOMIZADAS
- Possível diretiva para validação de pontos de atributo
- Possível diretiva para formatação de números

### 14.7 PIPES CUSTOMIZADOS
- Pipe para cálculo de ímpeto
- Pipe para cálculo de bônus
- Pipe para formatação de porcentagem de vida

### 14.8 OBSERVAÇÕES IMPORTANTES
- Sistema não usa autenticação
- Sistema não usa backend
- Todo armazenamento é local
- API externa apenas para IA (opcional)
- Foco em single-page application
- Estado é crítico - usar serviços com BehaviorSubject

---

## 15. GLOSSÁRIO DE TERMOS DO DOMÍNIO

- **B.B.A:** Bônus Base de Ataque
- **B.B.M:** Bônus Base Mágico
- **Aptidão:** Habilidade específica do personagem
- **Ameaça:** Nível de perigo representado pelo personagem
- **Essência:** Recurso mágico/espiritual do personagem
- **Ímpeto:** Efeito derivado de um atributo
- **Insólitus:** Característica especial ou incomum
- **Limitador:** Valor máximo que atributos podem atingir
- **Prospecção:** Sistema de rastreamento de recursos durante jogo
- **Renascimento:** Ciclo de ressurreição do personagem
- **VG:** Vida do Vigor
- **VT:** Vida de Vantagens
- **RD:** Redução de Dano
- **RDM:** Redução de Dano Mágico

---

## CONCLUSÃO

Este documento descreve completamente os requisitos funcionais e regras de negócio do sistema Klayrah RPG, sem referências a tecnologias específicas de React. Todos os comportamentos, cálculos e fluxos estão documentados de forma que possam ser implementados em qualquer framework, incluindo Angular.
