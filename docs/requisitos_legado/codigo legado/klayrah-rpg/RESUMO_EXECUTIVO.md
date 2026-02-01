# RESUMO EXECUTIVO - KLAYRAH RPG

## VISÃO GERAL

**Sistema:** Klayrah RPG - Gerenciador de Fichas de Personagens  
**Tipo:** Aplicação Web SPA (Single Page Application)  
**Armazenamento:** LocalStorage (cliente)  
**Dependências Externas:** Google Gemini AI (opcional)

---

## FUNCIONALIDADES PRINCIPAIS

### 1. **Gerenciamento de Fichas** ⭐ CORE
- Criar múltiplas fichas de personagens
- Editar fichas com auto-save
- Alternar entre fichas
- Deletar fichas com proteção (sempre mantém ao menos uma)
- Exportar ficha como PDF

### 2. **Ficha de Personagem Completa** ⭐ CORE
- **Informações Pessoais:** Nome, jogador, origem, classe, etc.
- **Atributos:** 7 atributos com sistema de pontos por nível
- **Sistema de Desenvolvimento:** XP, níveis (0-35), renascimentos
- **Sistema de Combate:** Bônus de ataque, defesa, reflexo
- **Aptidões:** 24 aptidões (12 físicas + 12 mentais)
- **Vida:** Sistema de dano por membro do corpo
- **Essência:** Recurso mágico com gastos rastreados
- **Ameaça:** Indicador de poder do personagem
- **Prospecção:** Contador auxiliar de recursos

### 3. **Galerias de Imagens** ⭐ FEATURE
- Galeria do personagem
- Galeria de itens
- Upload múltiplo de imagens
- Análise de imagens com IA (descrição automática)
- Armazenamento em Base64

### 4. **Sistema de Anotações** ⭐ FEATURE
- Anotações ilimitadas por personagem
- Timestamp automático
- Preservação de formatação

### 5. **Integração com IA** 🤖 OPTIONAL
- Sugestão de interpretação de personagem
- Análise de imagens (personagem e itens)
- Modelo: Google Gemini 2.5 Flash

---

## ARQUITETURA DE DADOS

```
LocalStorage
├── allCharacterSheets         (Array de fichas)
├── activeCharacterSheetId     (ID da ficha ativa)
├── character_{id}             (Galeria de personagem)
├── items_{id}                 (Galeria de itens)
└── notes_{id}                 (Anotações)
```

---

## ESTRUTURA DE UMA FICHA (CharacterSheetData)

```
Ficha de Personagem
│
├── Identificação
│   ├── id (único)
│   ├── player
│   ├── character
│   ├── tituloHeroico
│   ├── insolitus
│   ├── origem
│   ├── genero
│   ├── classe
│   └── customClasse
│
├── Personalidade
│   ├── indole (Bom/Mau/Neutro)
│   ├── presenca (Bom/Leal/Caótico/Neutro)
│   └── arquetipo
│
├── Descrição Física
│   ├── idade
│   ├── altura
│   ├── peso (auto-calculado)
│   ├── cabeloCor
│   ├── cabeloTamanho
│   ├── olhosCor
│   └── characterImage (Base64)
│
├── Desenvolvimento
│   ├── level (auto-calculado via XP)
│   ├── experience
│   ├── renascimentos
│   └── limitador (auto-calculado)
│
├── Atributos (7 total)
│   ├── Força → Ímpeto: carga em kg
│   ├── Agilidade → Ímpeto: deslocamento em metros
│   ├── Vigor → Ímpeto: redução de dano
│   ├── Sabedoria → Ímpeto: redução de dano mágico
│   ├── Intuição → Ímpeto: pontos de sorte
│   ├── Inteligência → Ímpeto: comando
│   └── Astúcia → Ímpeto: estratégia
│   
│   Cada atributo tem:
│   ├── base (valor inicial)
│   ├── nivel (pontos distribuídos: 3 por nível)
│   ├── outros (bônus diversos)
│   ├── total (soma automática)
│   └── impeto (cálculo derivado)
│
├── Bônus (6 tipos)
│   ├── B.B.A (Força + Agilidade) / 3
│   ├── Bloqueio (Força + Vigor) / 3
│   ├── Reflexo (Agilidade + Astúcia) / 3
│   ├── B.B.M (Sabedoria + Inteligência) / 3
│   ├── Percepção (Inteligência + Intuição) / 3
│   └── Raciocínio (Inteligência + Astúcia) / 3
│   
│   Cada bônus tem 5 modificadores:
│   ├── vantagens
│   ├── classe
│   ├── itens
│   ├── gloria
│   └── outros
│
├── Aptidões (24 total)
│   ├── Físicas (12)
│   └── Mentais (12)
│   
│   Cada aptidão tem:
│   ├── base
│   ├── sorte
│   ├── classe
│   └── total (soma)
│
├── Vida
│   ├── Fórmula: Vigor + Nível + VT + Renascimentos + OUT
│   └── Membros (7)
│       ├── Cabeça (75%)
│       ├── Tronco (100%)
│       ├── Braço D (25%)
│       ├── Braço E (25%)
│       ├── Perna D (25%)
│       ├── Perna E (25%)
│       └── Sangue (100%)
│
├── Essência
│   ├── Fórmula: (Vigor+Sabedoria)/2 + Nível + Renasc + Vant + Outros
│   ├── gastos (rastreamento)
│   └── restante (total - gastos)
│
└── Ameaça
    └── Fórmula: Nível + Itens + Títulos + Renascimentos + Outros
```

---

## CÁLCULOS AUTOMÁTICOS CRÍTICOS

### Sistema de Experiência → Nível
```
XP < 1.000           → Nível 0
XP 1.000 - 2.999     → Nível 1
XP 3.000 - 5.999     → Nível 2
...
XP ≥ 595.000         → Nível 35
```

### Sistema de Limitador
```
Nível 0-1    → Limitador: 10
Nível 2-20   → Limitador: 50
Nível 21-25  → Limitador: 75
Nível 26-30  → Limitador: 100
Nível 31-35  → Limitador: 120
Nível 36+    → "Renascimento"
```

### Distribuição de Pontos de Atributo
```
Pontos disponíveis = Nível × 3
Sistema valida e notifica se há excesso ou falta
```

### Cálculo de Peso Automático
```
Altura alterada OU Gênero alterado → Recalcula peso
Peso = BMI_base × (altura_em_metros)²

BMI_base:
- Masculino: 22.5
- Feminino: 21.0
- Outro: mantém atual
```

---

## REGRAS DE NEGÓCIO IMPORTANTES

### 🔒 Proteções
1. **Sempre há pelo menos uma ficha** - deletar a última cria uma nova
2. **Auto-save contínuo** - todas as alterações são salvas imediatamente
3. **Confirmação de exclusão** - fichas, imagens e notas requerem confirmação
4. **Validação de tamanho** - uploads limitados a 20MB
5. **Validação de pontos** - sistema alerta sobre pontos de atributo

### 🎲 Dependências entre Campos
1. **XP → Nível** - atualização automática
2. **Nível → Limitador** - atualização automática
3. **Nível → Pontos de Atributo** - validação automática
4. **Altura/Gênero → Peso** - recálculo automático
5. **Atributos → Ímpeto** - cálculo automático
6. **Atributos → Bônus Base** - cálculo automático
7. **Vida Total → Vida por Membro** - cálculo automático
8. **Renascimentos → Vida/Essência/Ameaça** - afeta múltiplos cálculos

### 📊 Fórmulas de Cálculo
```javascript
// Atributos
Total = BASE + NÍVEL + OUTROS

// Ímpetos
Força_Ímpeto = Total × 3 (kg)
Agilidade_Ímpeto = floor(Total / 3) (metros)
Vigor_Ímpeto = floor(Total / 10) (RD)
Sabedoria_Ímpeto = floor(Total / 10) (RDM)
Intuição_Ímpeto = min(floor(Total / 20), 3) (Sorte)
Inteligência_Ímpeto = floor(Total / 20) (Comando)
Astúcia_Ímpeto = floor(Total / 10) (Estratégia)

// Bônus
BBA = floor((Força_Total + Agilidade_Total) / 3)
Bloqueio = floor((Força_Total + Vigor_Total) / 3)
Reflexo = floor((Agilidade_Total + Astúcia_Total) / 3)
BBM = floor((Sabedoria_Total + Inteligência_Total) / 3)
Percepção = floor((Inteligência_Total + Intuição_Total) / 3)
Raciocínio = floor((Inteligência_Total + Astúcia_Total) / 3)

Total_Bônus = Base + Vantagens + Classe + Itens + Glória + Outros

// Vida
Vida_Total = Vigor_Total + Nível + VT + Renascimentos + OUT
Vida_Membro = floor(Vida_Total × Porcentagem) - Danos_Recebidos

// Essência
Essência_Base = floor((Vigor_Total + Sabedoria_Total) / 2)
Essência_Total = Essência_Base + Nível + Renasc + Vant + Outros
Essência_Restante = Essência_Total - Gastos

// Ameaça
Ameaça_Total = Nível + Itens + Títulos + Renascimentos + Outros

// Aptidões
Aptidão_Total = BASE + SORTE + CLASSE
```

---

## FLUXO DE DADOS

```
┌─────────────────┐
│   INICIALIZAÇÃO │
└────────┬────────┘
         │
         ├─→ Carregar dados do localStorage
         │   ├─→ allCharacterSheets existe?
         │   │   ├─→ SIM: Carregar fichas
         │   │   └─→ NÃO: Criar ficha padrão
         │   └─→ activeCharacterSheetId válido?
         │       ├─→ SIM: Selecionar ficha
         │       └─→ NÃO: Selecionar primeira ficha
         │
         ↓
┌─────────────────┐
│  MODO OPERAÇÃO  │
└────────┬────────┘
         │
         ├─→ Usuário edita campo
         │   ├─→ Atualizar estado
         │   ├─→ Recalcular dependências
         │   └─→ Salvar no localStorage
         │
         ├─→ Usuário troca de ficha
         │   ├─→ Salvar referência ativa
         │   ├─→ Carregar dados da ficha
         │   ├─→ Carregar galeria específica
         │   └─→ Carregar notas específicas
         │
         ├─→ Usuário cria nova ficha
         │   ├─→ Gerar ID único
         │   ├─→ Aplicar valores padrão
         │   ├─→ Adicionar à lista
         │   ├─→ Definir como ativa
         │   └─→ Salvar tudo
         │
         ├─→ Usuário deleta ficha
         │   ├─→ Confirmar ação
         │   ├─→ Remover da lista
         │   ├─→ Verificar se lista vazia
         │   │   ├─→ SIM: Criar nova ficha
         │   │   └─→ NÃO: Selecionar outra ficha
         │   └─→ Salvar alterações
         │
         ├─→ Usuário faz upload de imagem
         │   ├─→ Validar tamanho (<20MB)
         │   ├─→ Converter para Base64
         │   ├─→ Gerar ID único
         │   ├─→ Adicionar à galeria específica
         │   └─→ Salvar no localStorage
         │
         ├─→ Usuário solicita análise IA
         │   ├─→ Verificar API_KEY
         │   ├─→ Preparar dados (imagem/texto)
         │   ├─→ Enviar para Gemini API
         │   ├─→ Receber resposta
         │   ├─→ Exibir resultado
         │   └─→ Salvar análise
         │
         └─→ Usuário exporta PDF
             ├─→ Capturar elemento HTML
             ├─→ Aplicar estilos de impressão
             ├─→ Gerar PDF (html2pdf.js)
             └─→ Iniciar download
```

---

## TABELAS DE REFERÊNCIA

### Tabela de XP por Nível
| Nível | XP Mínimo | XP Máximo |
|-------|-----------|-----------|
| 0 | 0 | 999 |
| 1 | 1.000 | 2.999 |
| 2 | 3.000 | 5.999 |
| 5 | 15.000 | 20.999 |
| 10 | 55.000 | 65.999 |
| 15 | 120.000 | 135.999 |
| 20 | 210.000 | 230.999 |
| 25 | 325.000 | 350.999 |
| 30 | 465.000 | 495.999 |
| 35 | 595.000 | ∞ |

### Tabela de Classes
| Classe | Tipo |
|--------|------|
| Guerreiro | Físico |
| Arqueiro | Físico |
| Monge | Físico |
| Berserker | Físico |
| Assassino | Físico |
| Fauno (Herdeiro) | Especial |
| Mago | Mágico |
| Feiticeiro | Mágico |
| Necromance | Mágico |
| Sacerdote | Mágico |
| Ladrão | Habilidade |
| Negociante | Habilidade |
| Outra... | Customizado |

### Tabela de Aptidões Físicas
| Aptidão | Descrição Provável |
|---------|-------------------|
| Acrobacia | Movimentos acrobáticos |
| Guarda | Defender-se |
| Aparar | Bloquear ataques |
| Atletismo | Força e resistência física |
| Resvalar | Esquivar-se |
| Resistência | Suportar danos |
| Perseguição | Perseguir alvos |
| Natação | Nadar |
| Furtividade | Mover-se silenciosamente |
| Prestidigitação | Mãos ágeis |
| Conduzir | Pilotar veículos/montarias |
| Arte da Fuga | Escapar de amarras |

### Tabela de Aptidões Mentais
| Aptidão | Descrição Provável |
|---------|-------------------|
| Idiomas | Conhecimento de línguas |
| Observação | Perceber detalhes |
| Falsificar | Criar falsificações |
| Prontidão | Reação rápida |
| Auto Controle | Controlar emoções |
| Sentir Motivação | Ler intenções |
| Sobrevivência | Sobreviver na natureza |
| Investigar | Descobrir informações |
| Blefar | Enganar outros |
| Atuação | Interpretar papéis |
| Diplomacia | Negociar |
| Operação de Mecanismos | Operar dispositivos |

### Tabela de Porcentagem de Vida por Membro
| Membro | Porcentagem | Observação |
|--------|-------------|------------|
| Cabeça | 75% | Área vital reduzida |
| Tronco | 100% | Área principal |
| Braço Direito | 25% | Membro |
| Braço Esquerdo | 25% | Membro |
| Perna Direita | 25% | Membro |
| Perna Esquerda | 25% | Membro |
| Sangue | 100% | Sistema circulatório |

---

## INTEGRAÇÕES EXTERNAS

### Google Gemini API
**Endpoint:** Via SDK @google/genai  
**Modelo:** gemini-2.5-flash  
**Autenticação:** API Key via variável de ambiente  
**Uso 1:** Análise de imagens de personagem  
**Uso 2:** Análise de imagens de itens  
**Uso 3:** Sugestão de interpretação de personagem  

**Prompts:**
1. **Personagem/Cena:** "Describe this character or scene. What is happening? What is the mood? What story does this image tell?"
2. **Item:** "Describe this item from a fantasy RPG. What could it be? What are its potential powers or history? Be creative."
3. **Interpretação:** "Baseado nos seguintes traços de um personagem de RPG, forneça uma sugestão curta e criativa de como interpretá-lo: Índole: {indole}, Presença: {presenca}, Arquétipo: {arquetipo}"

### html2pdf.js
**CDN:** cdnjs.cloudflare.com  
**Versão:** 0.10.1  
**Uso:** Exportação de ficha em PDF  
**Configuração:**
- Margem: 0.5 inch
- Formato: A4
- Orientação: Portrait
- Qualidade de imagem: 98%
- Escala html2canvas: 2

### Tailwind CSS
**CDN:** cdn.tailwindcss.com  
**Uso:** Framework CSS para toda a estilização  
**Observação:** Sem arquivo de configuração customizado

---

## REQUISITOS NÃO-FUNCIONAIS

### Performance
- ✅ Carregamento instantâneo (dados locais)
- ✅ Auto-save sem lag perceptível
- ⚠️ Limite de tamanho do localStorage (~5-10MB dependendo do navegador)
- ⚠️ Imagens em Base64 aumentam uso de memória

### Segurança
- ✅ Dados armazenados localmente (privacidade)
- ⚠️ Sem backup automático em nuvem
- ⚠️ API Key do Gemini pode ser exposta no código cliente
- ⚠️ Dados podem ser perdidos ao limpar cache do navegador

### Usabilidade
- ✅ Interface intuitiva com labels claros
- ✅ Feedback visual imediato
- ✅ Confirmações para ações destrutivas
- ✅ Validações e alertas informativos
- ✅ Responsivo para mobile/tablet/desktop

### Compatibilidade
- ✅ Navegadores modernos (Chrome, Firefox, Safari, Edge)
- ⚠️ Requer JavaScript habilitado
- ⚠️ Requer localStorage disponível
- ✅ Funciona offline (exceto funcionalidades de IA)

### Acessibilidade
- ⚠️ Parcial - aria-labels em alguns elementos
- ⚠️ Foco visual em campos
- ⚠️ Não testado com leitores de tela
- ⚠️ Contraste de cores adequado

---

## LIMITAÇÕES CONHECIDAS

1. **Armazenamento limitado** - localStorage tem limite de ~5-10MB
2. **Sem sincronização** - dados não sincronizam entre dispositivos
3. **Sem backup** - dados podem ser perdidos ao limpar cache
4. **Dependência de API externa** - funcionalidades de IA requerem conexão
5. **Segurança da API Key** - chave exposta no código cliente
6. **Sem versionamento** - não há histórico de alterações
7. **Sem colaboração** - apenas um usuário por navegador
8. **Imagens grandes** - Base64 aumenta significativamente o tamanho dos dados

---

## PRÓXIMOS PASSOS PARA MIGRAÇÃO ANGULAR

### Fase 1: Setup e Estrutura
1. Criar projeto Angular
2. Configurar Tailwind CSS
3. Definir estrutura de pastas
4. Criar modelos/interfaces TypeScript

### Fase 2: Serviços Core
1. StorageService (gerenciar localStorage)
2. SheetService (estado das fichas)
3. GeminiService (integração IA)
4. PdfService (exportação PDF)

### Fase 3: Componentes Base
1. AppComponent (raiz)
2. HeaderComponent (cabeçalho)
3. NavigationComponent (abas)

### Fase 4: Componentes Principais
1. CharacterSheetComponent (ficha completa)
2. SheetManagerComponent (gerenciador)

### Fase 5: Componentes Auxiliares
1. GalleryComponent (galeria)
2. NotesComponent (anotações)
3. ProspeccaoComponent (prospecção)

### Fase 6: Integração e Refinamento
1. Pipes customizados para cálculos
2. Diretivas para validações
3. Tratamento de erros global
4. Otimizações de performance

### Fase 7: Testes e Deploy
1. Testes unitários
2. Testes de integração
3. Testes E2E
4. Build de produção

---

## CHECKLIST DE FUNCIONALIDADES

### Gerenciamento de Fichas
- [ ] Criar nova ficha
- [ ] Listar todas as fichas
- [ ] Selecionar ficha ativa
- [ ] Editar ficha com auto-save
- [ ] Deletar ficha (com proteção)
- [ ] Exportar ficha como PDF

### Dados da Ficha
- [ ] Informações pessoais (9 campos)
- [ ] Personalidade (3 campos + IA)
- [ ] Descrição física (7 campos + imagem)
- [ ] Desenvolvimento (3 campos + limitador)
- [ ] Atributos (7 × 5 campos = 35 campos)
- [ ] Bônus (6 × 6 campos = 36 campos)
- [ ] Aptidões (24 × 4 campos = 96 campos)
- [ ] Vida (2 campos + 7 membros = 9 campos)
- [ ] Essência (5 campos)
- [ ] Ameaça (5 campos)
- [ ] Prospecção (contador auxiliar)

### Cálculos Automáticos
- [ ] Nível baseado em XP
- [ ] Limitador baseado em nível
- [ ] Totais de atributos
- [ ] Ímpetos de atributos (7 cálculos)
- [ ] Bônus base (6 cálculos)
- [ ] Totais de bônus (6 cálculos)
- [ ] Totais de aptidões (24 cálculos)
- [ ] Vida total e por membro (8 cálculos)
- [ ] Essência total e restante (2 cálculos)
- [ ] Ameaça total (1 cálculo)
- [ ] Peso baseado em altura/gênero
- [ ] Validação de pontos de atributo

### Galerias
- [ ] Upload múltiplo de imagens
- [ ] Validação de tamanho (20MB)
- [ ] Conversão para Base64
- [ ] Listagem de imagens
- [ ] Seleção de imagem
- [ ] Visualização detalhada
- [ ] Deletar imagem
- [ ] Análise com IA
- [ ] Galeria independente por ficha

### Anotações
- [ ] Criar nota
- [ ] Listar notas
- [ ] Exibir timestamp
- [ ] Deletar nota
- [ ] Anotações independentes por ficha

### Integração IA
- [ ] Análise de imagem de personagem
- [ ] Análise de imagem de item
- [ ] Sugestão de interpretação
- [ ] Tratamento de erros
- [ ] Loading states

### Persistência
- [ ] Auto-save em todas as edições
- [ ] Carregar dados na inicialização
- [ ] Persistir ficha ativa
- [ ] Persistir galerias por ficha
- [ ] Persistir notas por ficha

### Interface
- [ ] Layout responsivo
- [ ] Navegação por abas
- [ ] Feedback visual
- [ ] Confirmações de exclusão
- [ ] Validações com alertas
- [ ] Loading states
- [ ] Disabled states
- [ ] Hover states
- [ ] Estilos de impressão/PDF

---

**Documento gerado para migração do sistema Klayrah RPG de React para Angular**  
**Versão:** 1.0  
**Data:** Janeiro 2026
