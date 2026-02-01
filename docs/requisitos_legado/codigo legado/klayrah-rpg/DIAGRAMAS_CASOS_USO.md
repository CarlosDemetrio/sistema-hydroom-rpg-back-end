# DIAGRAMAS E CASOS DE USO - KLAYRAH RPG

## 📋 ÍNDICE

1. [Diagramas de Fluxo](#diagramas-de-fluxo)
2. [Casos de Uso Detalhados](#casos-de-uso-detalhados)
3. [Exemplos de Dados](#exemplos-de-dados)
4. [Testes de Validação](#testes-de-validação)

---

## DIAGRAMAS DE FLUXO

### 1. FLUXO DE INICIALIZAÇÃO DA APLICAÇÃO

```
┌─────────────────────────────────────────────────────────┐
│                    INÍCIO                               │
└───────────────────────┬─────────────────────────────────┘
                        │
                        ↓
            ┌───────────────────────┐
            │ Carregar localStorage │
            └───────────┬───────────┘
                        │
                        ↓
            ┌─────────────────────────┐
            │ allCharacterSheets      │
            │ existe?                 │
            └────┬──────────────┬─────┘
                 │ NÃO          │ SIM
                 ↓              ↓
    ┌────────────────────┐  ┌──────────────────┐
    │ Criar ficha padrão │  │ Carregar fichas  │
    │ "Meu Primeiro      │  │ existentes       │
    │  Personagem"       │  │                  │
    └────────┬───────────┘  └────────┬─────────┘
             │                       │
             └───────────┬───────────┘
                         │
                         ↓
            ┌─────────────────────────┐
            │ activeCharacterSheetId  │
            │ é válido?               │
            └────┬──────────────┬─────┘
                 │ NÃO          │ SIM
                 ↓              ↓
    ┌────────────────────┐  ┌──────────────────┐
    │ Selecionar         │  │ Manter seleção   │
    │ primeira ficha     │  │ existente        │
    └────────┬───────────┘  └────────┬─────────┘
             │                       │
             └───────────┬───────────┘
                         │
                         ↓
            ┌─────────────────────────┐
            │ Carregar galeria de     │
            │ personagem da ficha     │
            └────────────┬────────────┘
                         │
                         ↓
            ┌─────────────────────────┐
            │ Carregar galeria de     │
            │ itens da ficha          │
            └────────────┬────────────┘
                         │
                         ↓
            ┌─────────────────────────┐
            │ Carregar anotações      │
            │ da ficha                │
            └────────────┬────────────┘
                         │
                         ↓
            ┌─────────────────────────┐
            │ Renderizar interface    │
            │ Aba "Ficha" ativa       │
            └─────────────────────────┘
```

---

### 2. FLUXO DE EDIÇÃO DE ATRIBUTO COM CÁLCULOS EM CASCATA

```
┌──────────────────────────────────────────┐
│ Usuário altera Força.nivel de 0 para 5  │
└──────────────────┬───────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ handleAttrChange('forca', 'nivel', '5')  │
└──────────────────┬───────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ Atualizar sheet.attributes.forca.nivel=5 │
└──────────────────┬───────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ CÁLCULO 1: Total de Força                │
│ total = base + nivel + outros            │
│ Exemplo: 10 + 5 + 0 = 15                 │
└──────────────────┬───────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ CÁLCULO 2: Ímpeto de Força               │
│ impeto = total × 3                       │
│ Exemplo: 15 × 3 = 45 kg                  │
└──────────────────┬───────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ CÁLCULO 3: B.B.A                         │
│ bba = floor((força + agilidade) / 3)     │
│ Exemplo: floor((15 + 12) / 3) = 9       │
└──────────────────┬───────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ CÁLCULO 4: Bloqueio                      │
│ bloqueio = floor((força + vigor) / 3)    │
│ Exemplo: floor((15 + 14) / 3) = 9       │
└──────────────────┬───────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ CÁLCULO 5: Pontos de Atributo            │
│ spentPoints = soma de todos .nivel       │
│ expectedPoints = level × 3               │
└──────────────────┬───────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│ Interface re-renderiza com novos valores │
└──────────────────────────────────────────┘
```

---

### 3. FLUXO DE CRIAÇÃO DE NOVA FICHA

```
┌─────────────────────────────┐
│ Usuário clica "Nova Ficha"  │
└──────────────┬──────────────┘
               │
               ↓
┌─────────────────────────────┐
│ handleNewSheet()             │
└──────────────┬──────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│ 1. Copiar DEFAULT_SHEET             │
│ 2. Gerar ID único: Date.now()       │
│ 3. id = "1738339200000"             │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│ Adicionar à lista: allSheets.push() │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│ Definir como ativa:                 │
│ setActiveSheetId("1738339200000")   │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│ Salvar no localStorage:             │
│ - allCharacterSheets                │
│ - activeCharacterSheetId            │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│ Mudar para aba 'sheet'              │
└──────────────┬──────────────────────┘
               │
               ↓
┌─────────────────────────────────────┐
│ Interface exibe nova ficha vazia    │
└─────────────────────────────────────┘
```

---

### 4. FLUXO DE EXCLUSÃO DE FICHA COM PROTEÇÃO

```
┌──────────────────────────────────────┐
│ Usuário clica delete na ficha "ABC" │
└──────────────┬───────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│ handleDeleteSheet("ABC")                 │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│ window.confirm("Tem certeza?")           │
└────────┬───────────────────┬────────────┘
         │ CANCELAR          │ CONFIRMAR
         ↓                   ↓
┌────────────────┐  ┌─────────────────────┐
│ Abortar        │  │ Continuar exclusão  │
└────────────────┘  └──────────┬──────────┘
                               │
                               ↓
                  ┌────────────────────────┐
                  │ Remover "ABC" da lista │
                  └──────────┬─────────────┘
                             │
                             ↓
                  ┌────────────────────────┐
                  │ "ABC" era a ativa?     │
                  └────┬──────────────┬────┘
                       │ NÃO          │ SIM
                       ↓              ↓
          ┌────────────────┐  ┌──────────────────┐
          │ Manter ativa   │  │ Há outras fichas?│
          │ atual          │  └────┬────────┬────┘
          └────────────────┘       │ SIM    │ NÃO
                                   ↓        ↓
                       ┌──────────────┐  ┌────────────┐
                       │ Selecionar   │  │ Criar nova │
                       │ primeira     │  │ ficha      │
                       └──────────────┘  └────────────┘
                                   │        │
                                   └────┬───┘
                                        │
                                        ↓
                          ┌──────────────────────┐
                          │ Salvar localStorage  │
                          └──────────────────────┘
```

---

### 5. FLUXO DE UPLOAD E ANÁLISE DE IMAGEM COM IA

```
┌────────────────────────────────────┐
│ Usuário seleciona arquivo(s)       │
└──────────────┬─────────────────────┘
               │
               ↓
┌────────────────────────────────────┐
│ handleImageUpload(event)            │
└──────────────┬─────────────────────┘
               │
               ↓
┌──────────────────────────────────────┐
│ Validar tamanho de cada arquivo      │
│ file.size > 20MB ?                   │
└────────┬───────────────────┬─────────┘
         │ ALGUNS SIM        │ TODOS OK
         ↓                   ↓
┌────────────────┐  ┌────────────────────┐
│ Alert com      │  │ Processar todos    │
│ arquivos       │  └─────────┬──────────┘
│ rejeitados     │            │
└────────────────┘            ↓
                  ┌────────────────────────┐
                  │ Para cada arquivo:     │
                  │ - FileReader()         │
                  │ - readAsDataURL()      │
                  └─────────┬──────────────┘
                            │
                            ↓
                  ┌─────────────────────────┐
                  │ Criar ImageItem:        │
                  │ - id: timestamp + nome  │
                  │ - src: Base64           │
                  │ - name: nome original   │
                  └─────────┬───────────────┘
                            │
                            ↓
                  ┌─────────────────────────┐
                  │ Adicionar ao array      │
                  └─────────┬───────────────┘
                            │
                            ↓
                  ┌─────────────────────────┐
                  │ Salvar localStorage     │
                  │ [galleryId]             │
                  └─────────┬───────────────┘
                            │
                            ↓
                  ┌─────────────────────────┐
                  │ Exibir miniatura        │
                  └─────────┬───────────────┘
                            │
                            ↓
┌──────────────────────────────────────────────────┐
│ Usuário clica na miniatura → seleciona          │
└──────────────────────┬───────────────────────────┘
                       │
                       ↓
┌──────────────────────────────────────────────────┐
│ Usuário clica "Analyze with Gemini"              │
└──────────────────────┬───────────────────────────┘
                       │
                       ↓
┌──────────────────────────────────────────────────┐
│ handleAnalyzeImage(image)                         │
└──────────────────────┬───────────────────────────┘
                       │
                       ↓
┌──────────────────────────────────────────────────┐
│ 1. Extrair Base64 da imagem                       │
│ 2. Detectar MIME type                             │
│ 3. Montar prompt (item vs personagem)            │
└──────────────────────┬───────────────────────────┘
                       │
                       ↓
┌──────────────────────────────────────────────────┐
│ Enviar para Gemini API                            │
│ - model: gemini-2.5-flash                         │
│ - contents: [imagePart, textPart]                 │
└────────┬──────────────────────┬──────────────────┘
         │ ERRO                 │ SUCESSO
         ↓                      ↓
┌────────────────┐  ┌───────────────────────────────┐
│ Salvar erro    │  │ Salvar análise:               │
│ como análise   │  │ image.analysis = response.text│
└────────────────┘  └──────────┬────────────────────┘
         │                     │
         └──────────┬──────────┘
                    │
                    ↓
         ┌────────────────────────┐
         │ Salvar localStorage    │
         │ com análise incluída   │
         └────────────────────────┘
```

---

### 6. FLUXO DE CÁLCULO DE NÍVEL BASEADO EM XP

```
┌───────────────────────────────────────┐
│ Usuário digita XP: 25000              │
└──────────────┬────────────────────────┘
               │
               ↓
┌───────────────────────────────────────┐
│ handleExperienceChange(event)          │
└──────────────┬────────────────────────┘
               │
               ↓
┌───────────────────────────────────────┐
│ newXp = parseInt("25000") = 25000     │
└──────────────┬────────────────────────┘
               │
               ↓
┌───────────────────────────────────────┐
│ getLevelForXp(25000)                   │
└──────────────┬────────────────────────┘
               │
               ↓
┌───────────────────────────────────────┐
│ Tabela XP_LEVELS:                      │
│ [0, 1000, 3000, 6000, 10000, 15000,   │
│  21000, 28000, ...]                    │
└──────────────┬────────────────────────┘
               │
               ↓
┌───────────────────────────────────────┐
│ Loop reverso pela tabela:              │
│ - 25000 >= 21000 ? SIM → Nível 6      │
└──────────────┬────────────────────────┘
               │
               ↓
┌───────────────────────────────────────┐
│ newLevel = 6                           │
└──────────────┬────────────────────────┘
               │
               ↓
┌───────────────────────────────────────┐
│ Atualizar sheet:                       │
│ - experience = 25000                   │
│ - level = 6                            │
└──────────────┬────────────────────────┘
               │
               ↓
┌───────────────────────────────────────┐
│ CÁLCULO DERIVADO 1: Limitador          │
│ level 6 → Limitador = 50              │
└──────────────┬────────────────────────┘
               │
               ↓
┌───────────────────────────────────────┐
│ CÁLCULO DERIVADO 2: Pontos esperados   │
│ expectedPoints = 6 × 3 = 18           │
└──────────────┬────────────────────────┘
               │
               ↓
┌───────────────────────────────────────┐
│ CÁLCULO DERIVADO 3: Validação          │
│ Comparar spentPoints com 18           │
│ Exibir notificação se diferente       │
└──────────────┬────────────────────────┘
               │
               ↓
┌───────────────────────────────────────┐
│ CÁLCULO DERIVADO 4: Vida               │
│ vidaTotal inclui level                │
└──────────────┬────────────────────────┘
               │
               ↓
┌───────────────────────────────────────┐
│ CÁLCULO DERIVADO 5: Essência           │
│ essenciaTotal inclui level            │
└──────────────┬────────────────────────┘
               │
               ↓
┌───────────────────────────────────────┐
│ CÁLCULO DERIVADO 6: Ameaça             │
│ ameacaTotal inclui level              │
└──────────────┬────────────────────────┘
               │
               ↓
┌───────────────────────────────────────┐
│ Interface re-renderiza tudo            │
└───────────────────────────────────────┘
```

---

## CASOS DE USO DETALHADOS

### CASO DE USO 1: Criar Primeiro Personagem

**Ator:** Jogador iniciante  
**Pré-condição:** Primeira vez acessando a aplicação  
**Pós-condição:** Ficha de personagem preenchida e salva

#### Fluxo Principal

1. Jogador abre a aplicação
2. Sistema detecta ausência de dados
3. Sistema cria ficha "Meu Primeiro Personagem"
4. Sistema exibe ficha vazia na tela
5. Jogador preenche nome: "Aragorn"
6. Sistema salva automaticamente
7. Jogador preenche jogador: "João"
8. Sistema salva automaticamente
9. Jogador seleciona classe: "Guerreiro"
10. Sistema salva automaticamente
11. Jogador insere XP: 50000
12. Sistema calcula nível: 10
13. Sistema atualiza limitador: 50
14. Sistema mostra: "18 pontos para distribuir" (nível 10 × 3 = 30, mas nível atual é 10)
15. Jogador distribui pontos em Força.nivel: 10
16. Jogador distribui pontos em Agilidade.nivel: 8
17. Jogador distribui pontos em Vigor.nivel: 8
18. Sistema recalcula totais
19. Sistema recalcula ímpetos
20. Sistema recalcula bônus
21. Sistema valida: 26 pontos distribuídos, 30 esperados
22. Sistema mostra: "4 pontos para distribuir"
23. Jogador distribui restante
24. Sistema valida: OK
25. Jogador clica "Baixar Ficha como PDF"
26. Sistema gera PDF com nome "aragorn.pdf"
27. Navegador inicia download

#### Fluxo Alternativo 1: Jogador esquece de distribuir pontos

No passo 25:
- Sistema gera PDF mesmo com notificação ativa
- Notificação aparece no PDF (não está oculta na impressão)

---

### CASO DE USO 2: Gerenciar Múltiplas Fichas

**Ator:** Jogador experiente  
**Pré-condição:** Já possui 1 ou mais fichas  
**Pós-condição:** Nova ficha criada e ativa

#### Fluxo Principal

1. Jogador está na aba "Ficha" editando "Aragorn"
2. Jogador clica "Nova Ficha"
3. Sistema cria nova ficha com DEFAULT_SHEET
4. Sistema gera ID: "1738339250000"
5. Sistema adiciona à lista
6. Sistema define como ativa
7. Sistema salva tudo no localStorage
8. Sistema exibe nova ficha vazia
9. Jogador preenche dados da segunda ficha "Gandalf"
10. Sistema salva automaticamente cada campo
11. Jogador quer voltar para "Aragorn"
12. Jogador clica aba "Minhas Fichas"
13. Sistema lista:
    - Gandalf (ativo - destacado)
    - Aragorn
14. Jogador clica em "Aragorn"
15. Sistema define "Aragorn" como ativo
16. Sistema muda para aba "Ficha"
17. Sistema carrega dados de "Aragorn"
18. Sistema carrega galeria de "Aragorn"
19. Sistema carrega notas de "Aragorn"
20. Jogador continua editando "Aragorn"

#### Fluxo Alternativo 1: Deletar ficha não ativa

No passo 14:
- Jogador clica botão delete em "Aragorn"
- Sistema pergunta: "Tem certeza?"
- Jogador confirma
- Sistema remove "Aragorn"
- Sistema mantém "Gandalf" ativo
- Sistema salva alteração
- Sistema continua exibindo "Gandalf"

#### Fluxo Alternativo 2: Deletar ficha ativa

No passo 13:
- Jogador clica delete em "Gandalf" (ativo)
- Sistema pergunta: "Tem certeza?"
- Jogador confirma
- Sistema remove "Gandalf"
- Sistema seleciona "Aragorn" como ativo
- Sistema salva alteração
- Sistema muda para aba "Ficha"
- Sistema exibe "Aragorn"

---

### CASO DE USO 3: Upload e Análise de Imagem de Item

**Ator:** Jogador  
**Pré-condição:** Ficha ativa existente, API key configurada  
**Pós-condição:** Imagem salva com análise da IA

#### Fluxo Principal

1. Jogador navega para aba "Meus Itens"
2. Sistema carrega galeria items_{fichaAtual.id}
3. Sistema exibe galeria vazia ou com itens existentes
4. Jogador clica "Upload Image(s)"
5. Sistema abre diálogo de seleção
6. Jogador seleciona "espada.jpg" (3MB)
7. Sistema valida: 3MB < 20MB → OK
8. Sistema lê arquivo como Base64
9. Sistema cria ImageItem:
   ```
   {
     id: "1738339300000-espada.jpg",
     src: "data:image/jpeg;base64,/9j/4AAQ...",
     name: "espada.jpg"
   }
   ```
10. Sistema adiciona ao array de imagens
11. Sistema salva em localStorage["items_1738339200000"]
12. Sistema exibe miniatura na grade
13. Jogador clica na miniatura
14. Sistema exibe visualização detalhada:
    - Imagem grande
    - Nome: "espada.jpg"
    - Botão "Analyze with Gemini"
    - Área de análise vazia
15. Jogador clica "Analyze with Gemini"
16. Sistema desabilita botão
17. Sistema mostra "Analyzing..."
18. Sistema extrai Base64
19. Sistema detecta MIME: "image/jpeg"
20. Sistema monta prompt (galleryId começa com "items"):
    ```
    "Describe this item from a fantasy RPG. 
    What could it be? What are its potential 
    powers or history? Be creative."
    ```
21. Sistema envia para Gemini API
22. API processa (3-5 segundos)
23. API retorna:
    ```
    "This appears to be an ancient elven blade, 
    with intricate runes etched along the fuller. 
    The pommel features a blue gem that may contain 
    trapped moonlight, suggesting the blade could 
    deal extra damage to undead creatures or glow 
    in the presence of darkness..."
    ```
24. Sistema atualiza ImageItem.analysis
25. Sistema salva no localStorage
26. Sistema exibe análise na área de texto
27. Sistema reabilita botão (permite re-análise)

#### Fluxo Alternativo 1: Arquivo muito grande

No passo 7:
- Jogador seleciona "mapa.jpg" (25MB)
- Sistema valida: 25MB > 20MB → ERRO
- Sistema exibe: "Arquivos muito grandes: mapa.jpg"
- Sistema não processa o arquivo
- Sistema mantém botão habilitado

#### Fluxo Alternativo 2: Múltiplos arquivos

No passo 6:
- Jogador seleciona 3 arquivos (espada.jpg, escudo.jpg, mapa.jpg[25MB])
- Sistema valida cada um
- Sistema alerta: "Arquivos muito grandes: mapa.jpg"
- Sistema processa apenas espada.jpg e escudo.jpg
- Sistema adiciona ambos à galeria

#### Fluxo Alternativo 3: API falha

No passo 22:
- API retorna erro (sem internet / API key inválida)
- Sistema captura erro
- Sistema salva como análise: "Failed to analyze image..."
- Sistema exibe mensagem de erro
- Sistema registra no console

---

### CASO DE USO 4: Rastrear Dano Recebido em Combate

**Ator:** Jogador durante sessão de RPG  
**Pré-condição:** Ficha com vida configurada  
**Pós-condição:** Danos rastreados por membro

#### Fluxo Principal

**Setup inicial:**
1. Personagem "Aragorn" nível 10
2. Vigor total: 20
3. VT: 5
4. Renascimentos: 0
5. OUT: 0
6. Vida Total = 20 + 10 + 5 + 0 + 0 = 35

**Valores por membro (sem dano):**
```
Cabeça:  35 × 0.75 = 26
Tronco:  35 × 1.00 = 35
Braço D: 35 × 0.25 = 8
Braço E: 35 × 0.25 = 8
Perna D: 35 × 0.25 = 8
Perna E: 35 × 0.25 = 8
Sangue:  35 × 1.00 = 35
```

**Durante o jogo:**

7. Mestre: "O orc acerta seu braço direito, 5 de dano"
8. Jogador digita no campo "Braço D > Danos": 5
9. Sistema calcula: 8 - 5 = 3
10. Sistema exibe na coluna "Valor": 3

11. Mestre: "A flecha acerta sua cabeça, 10 de dano"
12. Jogador digita no campo "Cabeça > Danos": 10
13. Sistema calcula: 26 - 10 = 16
14. Sistema exibe na coluna "Valor": 16

15. Mestre: "Você recebe cura de 5 pontos no braço"
16. Jogador atualiza campo "Braço D > Danos": 0
17. Sistema calcula: 8 - 0 = 8
18. Sistema exibe na coluna "Valor": 8 (recuperado)

#### Fluxo Alternativo 1: Dano excessivo

No passo 11:
- Mestre: "Ataque crítico na cabeça, 30 de dano"
- Jogador digita no campo "Cabeça > Danos": 30
- Sistema calcula: 26 - 30 = -4
- Sistema exibe na coluna "Valor": -4
- Jogador sabe que está inconsciente/morto

---

### CASO DE USO 5: Ganhar Nível e Distribuir Pontos

**Ator:** Jogador após sessão  
**Pré-condição:** Ficha existente  
**Pós-condição:** Nível atualizado, pontos distribuídos

#### Situação Inicial
- Personagem nível 5 (XP: 15000)
- Força: base 10, nivel 9, outros 0 = Total 19
- Agilidade: base 8, nivel 6, outros 0 = Total 14
- Pontos distribuídos: 15 (9+6)
- Pontos esperados: 5 × 3 = 15
- Status: OK ✓

#### Fluxo Principal

1. Mestre: "Vocês ganharam 6500 XP"
2. Jogador calcula: 15000 + 6500 = 21500
3. Jogador altera campo "Experiência": 21500
4. Sistema executa getLevelForXp(21500)
5. Sistema identifica: 21500 >= 21000 → Nível 6
6. Sistema atualiza level: 6
7. Sistema recalcula limitador: 50 (nível 2-20)
8. Sistema recalcula pontos esperados: 6 × 3 = 18
9. Sistema compara: distribuído 15, esperado 18
10. Sistema exibe notificação amarela:
    ```
    "Você tem 3 ponto(s) de nível para distribuir!
    (Esperado: 18, Distribuído: 15)"
    ```
11. Jogador adiciona 2 em Força.nivel (9 → 11)
12. Sistema recalcula: distribuído 17, esperado 18
13. Sistema atualiza notificação: "1 ponto para distribuir"
14. Sistema recalcula Força total: 10 + 11 + 0 = 21
15. Sistema recalcula Força ímpeto: 21 × 3 = 63 kg
16. Sistema recalcula B.B.A: floor((21 + 14) / 3) = 11
17. Sistema recalcula Bloqueio: floor((21 + vigor) / 3)
18. Jogador adiciona 1 em Agilidade.nivel (6 → 7)
19. Sistema recalcula: distribuído 18, esperado 18
20. Sistema remove notificação ✓
21. Sistema recalcula Agilidade total: 8 + 7 + 0 = 15
22. Sistema recalcula Agilidade ímpeto: floor(15 / 3) = 5 m
23. Sistema recalcula B.B.A: floor((21 + 15) / 3) = 12
24. Sistema recalcula Reflexo: floor((15 + astucia) / 3)
25. Todos os valores atualizados e salvos

#### Fluxo Alternativo 1: Distribuir pontos a mais

No passo 11:
- Jogador adiciona 5 em Força.nivel (9 → 14)
- Sistema recalcula: distribuído 20, esperado 18
- Sistema exibe notificação vermelha:
  ```
  "Você distribuiu 2 ponto(s) de nível a mais!
  (Esperado: 18, Distribuído: 20)"
  ```
- Jogador corrige removendo pontos

---

## EXEMPLOS DE DADOS

### EXEMPLO 1: Ficha Completa de Personagem Nível 10

```json
{
  "id": "1738339200000",
  "player": "João Silva",
  "character": "Aragorn, o Andarilho",
  "tituloHeroico": "Protetor do Norte",
  "insolitus": "Descendente de Reis",
  "origem": "Valfenda",
  "level": 10,
  "experience": 55000,
  "renascimentos": 0,
  "indole": "Bom",
  "presenca": "Leal",
  "arquetipo": "Herói Relutante",
  "genero": "Masculino",
  "classe": "Guerreiro",
  "customClasse": "",
  "descricaoFisica": {
    "idade": 87,
    "altura": 185,
    "peso": 77,
    "cabeloCor": "Castanho Escuro",
    "cabeloTamanho": "Longo",
    "olhosCor": "Cinza"
  },
  "attributes": {
    "forca": { "base": 12, "nivel": 10, "outros": 2, "impeto": 0 },
    "agilidade": { "base": 10, "nivel": 8, "outros": 0, "impeto": 0 },
    "vigor": { "base": 14, "nivel": 6, "outros": 0, "impeto": 0 },
    "sabedoria": { "base": 8, "nivel": 2, "outros": 0, "impeto": 0 },
    "intuicao": { "base": 12, "nivel": 2, "outros": 0, "impeto": 0 },
    "inteligencia": { "base": 10, "nivel": 2, "outros": 0, "impeto": 0 },
    "astucia": { "base": 8, "nivel": 0, "outros": 0, "impeto": 0 }
  },
  "characterImage": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
  "vida": { "vt": 10, "out": 0 },
  "danos": { 
    "cabeça": 5, 
    "tronco": 0, 
    "bracoD": 8, 
    "bracoE": 0, 
    "pernaD": 3, 
    "pernaE": 0, 
    "sangue": 2 
  },
  "essencia": { "renasc": 0, "vant": 5, "outros": 0, "gastos": 12 },
  "aptidoes": {
    "acrobacia": { "base": 5, "sorte": 2, "classe": 3 },
    "guarda": { "base": 8, "sorte": 2, "classe": 5 },
    "aparar": { "base": 8, "sorte": 2, "classe": 5 },
    "atletismo": { "base": 10, "sorte": 2, "classe": 5 },
    "resvalar": { "base": 6, "sorte": 2, "classe": 3 },
    "resistencia": { "base": 8, "sorte": 2, "classe": 5 },
    "perseguicao": { "base": 7, "sorte": 2, "classe": 4 },
    "natacao": { "base": 5, "sorte": 2, "classe": 2 },
    "furtividade": { "base": 8, "sorte": 2, "classe": 4 },
    "prestidigit": { "base": 3, "sorte": 2, "classe": 1 },
    "conduzir": { "base": 6, "sorte": 2, "classe": 3 },
    "arte da fuga": { "base": 4, "sorte": 2, "classe": 2 },
    "idiomas": { "base": 8, "sorte": 2, "classe": 3 },
    "observacao": { "base": 10, "sorte": 2, "classe": 5 },
    "falsificar": { "base": 2, "sorte": 2, "classe": 0 },
    "prontidao": { "base": 10, "sorte": 2, "classe": 5 },
    "auto controle": { "base": 8, "sorte": 2, "classe": 4 },
    "sentir motiv.": { "base": 7, "sorte": 2, "classe": 3 },
    "sobrevivencia": { "base": 10, "sorte": 2, "classe": 5 },
    "investigar": { "base": 6, "sorte": 2, "classe": 3 },
    "blefar": { "base": 4, "sorte": 2, "classe": 1 },
    "atuacao": { "base": 3, "sorte": 2, "classe": 1 },
    "diplomacia": { "base": 8, "sorte": 2, "classe": 4 },
    "op. mecanis": { "base": 5, "sorte": 2, "classe": 2 }
  },
  "ameaca": { "itens": 15, "titulos": 5, "outros": 2 },
  "bonus": {
    "bba": { "vantagens": 2, "classe": 5, "itens": 3, "gloria": 1, "outros": 0 },
    "bloqueio": { "vantagens": 1, "classe": 3, "itens": 5, "gloria": 1, "outros": 0 },
    "reflexo": { "vantagens": 2, "classe": 3, "itens": 2, "gloria": 1, "outros": 0 },
    "bbm": { "vantagens": 0, "classe": 1, "itens": 2, "gloria": 0, "outros": 0 },
    "percep": { "vantagens": 3, "classe": 2, "itens": 1, "gloria": 1, "outros": 0 },
    "racionc": { "vantagens": 2, "classe": 2, "itens": 1, "gloria": 1, "outros": 0 }
  }
}
```

**Cálculos Derivados:**
```
ATRIBUTOS:
Força: 12+10+2 = 24 → Ímpeto: 72kg
Agilidade: 10+8+0 = 18 → Ímpeto: 6m
Vigor: 14+6+0 = 20 → Ímpeto: 2 RD
Sabedoria: 8+2+0 = 10 → Ímpeto: 1 RDM
Intuição: 12+2+0 = 14 → Ímpeto: 0 Sorte
Inteligência: 10+2+0 = 12 → Ímpeto: 0 Comando
Astúcia: 8+0+0 = 8 → Ímpeto: 0 Estratégia

BÔNUS BASE:
BBA: floor((24+18)/3) = 14
Bloqueio: floor((24+20)/3) = 14
Reflexo: floor((18+8)/3) = 8
BBM: floor((10+12)/3) = 7
Percepção: floor((12+14)/3) = 8
Raciocínio: floor((12+8)/3) = 6

BÔNUS TOTAL:
BBA: 14+2+5+3+1+0 = 25
Bloqueio: 14+1+3+5+1+0 = 24
Reflexo: 8+2+3+2+1+0 = 16
BBM: 7+0+1+2+0+0 = 10
Percepção: 8+3+2+1+1+0 = 15
Raciocínio: 6+2+2+1+1+0 = 12

VIDA:
Total: 20+10+10+0+0 = 40
Cabeça: floor(40×0.75)-5 = 25
Tronco: floor(40×1.00)-0 = 40
Braço D: floor(40×0.25)-8 = 2
Braço E: floor(40×0.25)-0 = 10
Perna D: floor(40×0.25)-3 = 7
Perna E: floor(40×0.25)-0 = 10
Sangue: floor(40×1.00)-2 = 38

ESSÊNCIA:
Base: floor((20+10)/2) = 15
Total: 15+10+0+5+0 = 30
Restante: 30-12 = 18

AMEAÇA:
Total: 10+15+5+0+2 = 32

VALIDAÇÃO:
Pontos distribuídos: 10+8+6+2+2+2+0 = 30
Pontos esperados: 10×3 = 30
Status: ✓ OK
```

---

### EXEMPLO 2: Estado do LocalStorage

```javascript
// localStorage["allCharacterSheets"]
[
  { 
    id: "1738339200000", 
    character: "Aragorn", 
    player: "João",
    // ... resto dos dados
  },
  { 
    id: "1738339250000", 
    character: "Gandalf", 
    player: "Maria",
    // ... resto dos dados
  },
  { 
    id: "1738339300000", 
    character: "Legolas", 
    player: "Pedro",
    // ... resto dos dados
  }
]

// localStorage["activeCharacterSheetId"]
"1738339200000"

// localStorage["character_1738339200000"]
[
  {
    id: "1738339400000-aragorn_portrait.jpg",
    src: "data:image/jpeg;base64,/9j/4AAQ...",
    name: "aragorn_portrait.jpg",
    analysis: "A weathered ranger with a noble bearing..."
  }
]

// localStorage["items_1738339200000"]
[
  {
    id: "1738339450000-anduril.jpg",
    src: "data:image/jpeg;base64,iVBOR...",
    name: "anduril.jpg",
    analysis: "This legendary blade appears to be..."
  },
  {
    id: "1738339500000-ring.jpg",
    src: "data:image/jpeg;base64,R0lG...",
    name: "ring.jpg",
    analysis: "A simple silver ring with elven inscriptions..."
  }
]

// localStorage["notes_1738339200000"]
[
  {
    id: 1738339600000,
    content: "Sessão 15: Enfrentamos o Balrog nas Minas de Moria.\nGandalf sacrificou-se para nos salvar.",
    timestamp: "31/01/2026, 14:30:00"
  },
  {
    id: 1738338000000,
    content: "Sessão 14: Chegamos a Moria. Algo não está certo...",
    timestamp: "24/01/2026, 15:00:00"
  }
]
```

---

## TESTES DE VALIDAÇÃO

### TESTE 1: Validação de Pontos de Atributo

**Cenário 1: Distribuição correta**
```
Input:
- Level: 5
- Força.nivel: 6
- Agilidade.nivel: 5
- Vigor.nivel: 4
- Outros atributos: 0

Cálculo:
- Esperado: 5 × 3 = 15
- Distribuído: 6+5+4+0+0+0+0 = 15

Output:
✓ Sem notificação
```

**Cenário 2: Pontos restantes**
```
Input:
- Level: 5
- Força.nivel: 6
- Agilidade.nivel: 5
- Outros: 0

Cálculo:
- Esperado: 15
- Distribuído: 11

Output:
⚠️ "Você tem 4 ponto(s) de nível para distribuir!"
```

**Cenário 3: Pontos excedidos**
```
Input:
- Level: 5
- Total distribuído: 18

Cálculo:
- Esperado: 15
- Distribuído: 18

Output:
❌ "Você distribuiu 3 ponto(s) de nível a mais!"
```

---

### TESTE 2: Cálculo de Nível por XP

| XP Input | Nível Esperado | Limitador |
|----------|----------------|-----------|
| 0 | 0 | 10 |
| 500 | 0 | 10 |
| 1000 | 1 | 10 |
| 2999 | 1 | 10 |
| 3000 | 2 | 50 |
| 10000 | 4 | 50 |
| 55000 | 10 | 50 |
| 120000 | 15 | 50 |
| 210000 | 20 | 50 |
| 231000 | 21 | 75 |
| 325000 | 25 | 75 |
| 351000 | 26 | 100 |
| 465000 | 30 | 100 |
| 496000 | 31 | 120 |
| 595000 | 35 | 120 |
| 1000000 | 35 | 120 |

---

### TESTE 3: Cálculo de Peso Automático

**Cenário 1: Masculino, 180cm**
```
Input:
- altura: 180
- genero: "Masculino"

Cálculo:
- BMI_base: 22.5
- altura_metros: 1.80
- peso = 22.5 × (1.80)² = 22.5 × 3.24 = 72.9

Output:
peso: 73 kg (arredondado)
```

**Cenário 2: Feminino, 165cm**
```
Input:
- altura: 165
- genero: "Feminino"

Cálculo:
- BMI_base: 21
- altura_metros: 1.65
- peso = 21 × (1.65)² = 21 × 2.7225 = 57.17

Output:
peso: 57 kg
```

**Cenário 3: Trocar gênero**
```
Estado Inicial:
- altura: 170
- genero: "Masculino"
- peso: 65

Ação:
- Alterar genero para "Feminino"

Cálculo:
- BMI_base: 21 (antes era 22.5)
- peso = 21 × (1.70)² = 60.69

Output:
peso: 61 kg (recalculado automaticamente)
```

---

### TESTE 4: Validação de Upload de Arquivo

**Cenário 1: Arquivo válido**
```
Input:
- arquivo: "personagem.jpg"
- tamanho: 5MB

Validação:
5MB < 20MB → OK

Output:
✓ Arquivo processado e salvo
```

**Cenário 2: Arquivo muito grande**
```
Input:
- arquivo: "screenshot.png"
- tamanho: 25MB

Validação:
25MB > 20MB → ERRO

Output:
❌ Alert: "Arquivos muito grandes: screenshot.png"
❌ Arquivo não processado
```

**Cenário 3: Múltiplos arquivos mistos**
```
Input:
- arquivo1: "item1.jpg" (3MB)
- arquivo2: "item2.png" (18MB)
- arquivo3: "mapa.jpg" (30MB)

Validação:
- 3MB < 20MB → OK
- 18MB < 20MB → OK
- 30MB > 20MB → ERRO

Output:
✓ item1.jpg processado
✓ item2.png processado
❌ Alert: "Arquivos muito grandes: mapa.jpg"
```

---

### TESTE 5: Proteção de Exclusão de Última Ficha

**Setup:**
```
allSheets = [{ id: "ABC", character: "Aragorn" }]
activeSheetId = "ABC"
```

**Ação:**
```
handleDeleteSheet("ABC")
→ Usuário confirma
```

**Processo:**
```
1. Remover "ABC" → allSheets = []
2. Detectar lista vazia
3. Criar nova ficha:
   {
     id: "1738339999999",
     character: "Novo Personagem",
     ...DEFAULT_SHEET
   }
4. allSheets = [{ id: "1738339999999", ... }]
5. activeSheetId = "1738339999999"
```

**Resultado:**
```
✓ Sempre há pelo menos uma ficha
✓ Nova ficha criada automaticamente
✓ Nova ficha definida como ativa
```

---

**Documento de Diagramas e Casos de Uso**  
**Versão:** 1.0  
**Data:** Janeiro 2026  
**Projeto:** Klayrah RPG - Migração para Angular
