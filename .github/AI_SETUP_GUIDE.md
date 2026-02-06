# AI Assistant Configuration Guide

> 🤖 Guia de configuração para GitHub Copilot e Google Gemini

## 📋 Arquivos de Configuração

Este projeto está configurado para funcionar com múltiplos assistentes de IA:

### ✅ Configurados
- **GitHub Copilot** → `.github/copilot-instructions-backend.md` + `.github/copilot-instructions.md`
- **Google Gemini** → `.github/gemini-instructions.md`
- **IntelliJ IDEA** → `.idea/ai-assistant.xml`

### 📁 Estrutura de Arquivos
```
ficha-controlador/
├── .github/
│   ├── copilot-instructions-backend.md    # Copilot (backend)
│   ├── copilot-instructions.md            # Copilot (geral)
│   └── gemini-instructions.md             # Gemini
├── .idea/
│   └── ai-assistant.xml                   # IntelliJ AI settings
├── .vscode/
│   └── settings.json                      # VSCode settings
├── .context/
│   └── README.md                          # Context files index
└── docs/
    ├── AI_GUIDELINES_BACKEND.md           # Hub principal
    └── backend/                           # Guias modulares
        ├── 01-architecture.md
        ├── 02-entities-dtos.md
        ├── ...
        └── 11-owasp-security.md
```

## 🚀 Setup por IDE

### IntelliJ IDEA

#### GitHub Copilot
1. Instale o plugin: `Settings` → `Plugins` → Busque "GitHub Copilot"
2. Faça login com sua conta GitHub
3. O arquivo `.idea/ai-assistant.xml` já está configurado
4. Reinicie o IntelliJ

**Verificação**:
```
Settings → Tools → GitHub Copilot
- ✅ Enable Copilot: ON
- ✅ Show inline suggestions: ON
```

#### Google Gemini Code Assist
1. Instale o plugin: `Settings` → `Plugins` → Busque "Gemini Code Assist"
2. Configure API key (se necessário)
3. O arquivo `.idea/ai-assistant.xml` já contém as configurações
4. Reinicie o IntelliJ

**Verificação**:
```
Settings → Tools → Gemini Code Assist
- ✅ Enable Gemini: ON
- ✅ Context files loaded: 3 files
```

### VSCode

#### GitHub Copilot
1. Instale a extensão: GitHub Copilot
2. Faça login
3. O arquivo `.vscode/settings.json` já está configurado

**Verificação**:
```json
{
  "github.copilot.enable": {
    "*": true,
    "java": true
  }
}
```

## 📖 Documentação Primária

Todos os assistentes de IA estão configurados para consultar:

1. **Hub Principal**: `/docs/AI_GUIDELINES_BACKEND.md`
2. **Guias Modulares**: `/docs/backend/*.md`
3. **Context Files**: `.context/*.md`

### Ordem de Consulta
```
1. .github/[ai]-instructions.md     (Instruções específicas da IA)
2. docs/AI_GUIDELINES_BACKEND.md    (Hub central)
3. docs/backend/*.md                (Guias detalhados)
4. .context/*.md                    (Context técnico)
```

## 🎯 Como Funcionam as Instruções

### GitHub Copilot
- Lê automaticamente `.github/copilot-instructions.md`
- Usa contexto dos arquivos abertos
- Consulta `.vscode/settings.json` (VSCode) ou `.idea/ai-assistant.xml` (IntelliJ)

### Google Gemini
- Lê `.github/gemini-instructions.md`
- Carrega context files configurados em `.idea/ai-assistant.xml`
- Usa project context definido

### Templates e Exemplos
Todos os arquivos de instrução incluem:
- ✅ Templates de código prontos
- ✅ Exemplos práticos
- ✅ Links para documentação detalhada
- ✅ Lista de "nunca faça"
- ✅ Padrões de nomenclatura

## 🧪 Testando a Configuração

### Teste 1: Criar uma Entity
Digite no editor:
```java
// Create a Product entity with name, price, and timestamps
```

**Esperado**: O assistente deve sugerir código seguindo o padrão:
- Lombok annotations
- Snake_case table name
- Audit fields (createdAt, updatedAt)

### Teste 2: Criar um Service
Digite:
```java
// Create ProductService with findById and create methods
```

**Esperado**:
- `@Service` + `@RequiredArgsConstructor` + `@Transactional(readOnly = true)`
- Método findById com `orElseThrow`
- Método create com `@Transactional`
- Exceptions específicas

### Teste 3: Criar um Controller
Digite:
```java
// Create ProductController with REST endpoints
```

**Esperado**:
- `@RestController` + Swagger annotations
- Mappers na controller (não no service!)
- `@Valid` para validação
- `ResponseEntity` nos retornos

## 🔍 Troubleshooting

### Copilot não está seguindo os padrões
1. Verifique se o arquivo `.github/copilot-instructions-backend.md` existe
2. Reinicie o IDE
3. Abra um dos arquivos de documentação (`/docs/backend/*.md`) para dar contexto

### Gemini não carrega context files
1. Verifique `.idea/ai-assistant.xml`
2. Reconfigure em `Settings → Tools → Gemini Code Assist`
3. Adicione manualmente os context files

### Sugestões genéricas
1. Abra `/docs/AI_GUIDELINES_BACKEND.md` no editor
2. Abra o guia específico relevante (ex: `05-services.md`)
3. Faça perguntas mais específicas no chat

## 📚 Recursos

### Documentação Completa
- **Hub**: [AI_GUIDELINES_BACKEND.md](./docs/AI_GUIDELINES_BACKEND.md)
- **Modulares**: [docs/backend/](./docs/backend/)

### Arquivos de Instrução
- **Copilot**: [.github/copilot-instructions-backend.md](./.github/copilot-instructions-backend.md)
- **Gemini**: [.github/gemini-instructions.md](./.github/gemini-instructions.md)

### Context Files
- **Index**: [.context/README.md](./.context/README.md)

## 🎓 Dicas de Uso

### Para Melhores Resultados
1. **Seja específico** nos comentários
2. **Abra arquivos de contexto** relevantes
3. **Use o chat** para perguntas complexas
4. **Revise as sugestões** sempre (não aceite cegamente)
5. **Consulte a documentação** quando em dúvida

### Exemplos de Prompts Efetivos
```java
// Create a GameService following the project patterns with:
// - @Transactional(readOnly=true) at class level
// - findById with ResourceNotFoundException
// - create with business validations

// Generate integration test for GameController create endpoint
// following the 80/20 rule (integration over unit)

// Create GameMapper with toResponseDTO and toEntity methods
// following the project mapper pattern
```

---

**Lembre-se**: As IAs são assistentes, não substituem o conhecimento dos padrões do projeto. Sempre revise e valide as sugestões!
