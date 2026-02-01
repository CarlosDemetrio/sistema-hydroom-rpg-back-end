# DOCUMENTAÇÃO COMPLETA - KLAYRAH RPG

## 📚 BIBLIOTECA DE DOCUMENTOS

Este projeto foi completamente analisado e documentado em 4 documentos principais, organizados para facilitar a migração para Angular.

---

## 📖 GUIA DE LEITURA

### Para Entender o Sistema Completo
Leia os documentos nesta ordem:

1. **RESUMO_EXECUTIVO.md** - Visão geral rápida (15 min)
2. **ANALISE_REQUISITOS.md** - Detalhamento completo (45 min)
3. **ANALISE_POR_ARQUIVO.md** - Análise técnica por componente (30 min)
4. **DIAGRAMAS_CASOS_USO.md** - Fluxos e exemplos práticos (30 min)

### Para Implementar uma Funcionalidade Específica
1. Localize a funcionalidade no índice abaixo
2. Consulte a seção correspondente em ANALISE_REQUISITOS.md
3. Veja o arquivo responsável em ANALISE_POR_ARQUIVO.md
4. Revise os fluxos relacionados em DIAGRAMAS_CASOS_USO.md

---

## 📋 ÍNDICE GERAL DE FUNCIONALIDADES

### 🎯 FUNCIONALIDADES CORE

#### Gerenciamento de Fichas
- **Criar Nova Ficha**
  - Localização: ANALISE_REQUISITOS.md → Seção 1.2.4
  - Componente: App.tsx
  - Fluxo: DIAGRAMAS_CASOS_USO.md → Fluxo 3
  
- **Listar Fichas**
  - Localização: ANALISE_REQUISITOS.md → Seção 5
  - Componente: SheetManager.tsx
  - Caso de Uso: DIAGRAMAS_CASOS_USO.md → Caso 2
  
- **Selecionar Ficha Ativa**
  - Localização: ANALISE_REQUISITOS.md → Seção 1.2.5
  - Componente: App.tsx
  - Fluxo: DIAGRAMAS_CASOS_USO.md → Caso 2, passo 14-20
  
- **Deletar Ficha**
  - Localização: ANALISE_REQUISITOS.md → Seção 1.2.6
  - Componente: App.tsx
  - Fluxo: DIAGRAMAS_CASOS_USO.md → Fluxo 4
  - Teste: DIAGRAMAS_CASOS_USO.md → Teste 5
  
- **Exportar PDF**
  - Localização: ANALISE_REQUISITOS.md → Seção 1.2.7
  - Componente: App.tsx
  - Caso de Uso: DIAGRAMAS_CASOS_USO.md → Caso 1, passo 25-27

#### Dados do Personagem

- **Informações Pessoais**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.2.1
  - Componente: CharacterSheet.tsx
  - Campos: 9 campos editáveis
  
- **Descrição Física**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.2.3
  - Componente: CharacterSheet.tsx
  - Cálculo automático: Peso baseado em altura/gênero
  
- **Personalidade**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.2.4
  - Componente: CharacterSheet.tsx
  - Integração IA: Sugestão de interpretação

#### Sistema de Atributos

- **7 Atributos Principais**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.2.5
  - Componente: CharacterSheet.tsx
  - Cálculos: RESUMO_EXECUTIVO.md → Fórmulas de Cálculo
  - Fluxo: DIAGRAMAS_CASOS_USO.md → Fluxo 2
  
- **Sistema de Distribuição de Pontos**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.3.2
  - Regra: 3 pontos por nível
  - Teste: DIAGRAMAS_CASOS_USO.md → Teste 1
  - Caso de Uso: DIAGRAMAS_CASOS_USO.md → Caso 5

#### Sistema de Desenvolvimento

- **Experiência e Níveis**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.3.1
  - Tabela XP: RESUMO_EXECUTIVO.md → Tabela de XP
  - Fluxo: DIAGRAMAS_CASOS_USO.md → Fluxo 6
  - Teste: DIAGRAMAS_CASOS_USO.md → Teste 2
  
- **Limitador**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.3.3
  - Cálculo: baseado em faixas de nível
  
- **Renascimentos**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.3.4
  - Impacto: Vida, Essência, Ameaça

#### Sistema de Combate

- **Bônus Base (6 tipos)**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.4.1
  - Fórmulas: RESUMO_EXECUTIVO.md → Cálculos
  
- **Modificadores de Bônus**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.4.2
  - Campos: 5 fontes de bônus por tipo
  
- **Aptidões (24 total)**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.5
  - Tabelas: RESUMO_EXECUTIVO.md → Tabelas de Aptidões

#### Sistema de Vida

- **Cálculo de Vida Total**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.6.1
  - Fórmula: RESUMO_EXECUTIVO.md → Fórmulas
  
- **Dano por Membro**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.6.2
  - Tabela: RESUMO_EXECUTIVO.md → Tabela de Porcentagens
  - Caso de Uso: DIAGRAMAS_CASOS_USO.md → Caso 4

#### Sistema de Essência

- **Cálculo de Essência**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.7
  - Fórmula: RESUMO_EXECUTIVO.md → Fórmulas
  - Rastreamento de gastos incluído

#### Sistema de Ameaça

- **Cálculo de Ameaça**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.8
  - Fórmula: soma de múltiplos fatores

---

### 🎨 FUNCIONALIDADES AUXILIARES

#### Galerias de Imagens

- **Upload de Imagens**
  - Localização: ANALISE_REQUISITOS.md → Seção 3.2.1
  - Componente: Gallery.tsx
  - Validação: 20MB limite
  - Teste: DIAGRAMAS_CASOS_USO.md → Teste 4
  
- **Análise com IA**
  - Localização: ANALISE_REQUISITOS.md → Seção 3.2.8
  - Fluxo: DIAGRAMAS_CASOS_USO.md → Fluxo 5
  - Caso de Uso: DIAGRAMAS_CASOS_USO.md → Caso 3
  - Prompts diferentes para personagem vs itens

#### Sistema de Anotações

- **Criar Anotação**
  - Localização: ANALISE_REQUISITOS.md → Seção 4.2.3
  - Componente: Notes.tsx
  - Timestamp automático
  
- **Listar/Deletar Anotações**
  - Localização: ANALISE_REQUISITOS.md → Seção 4.2
  - Ordenação: mais recente primeiro

#### Sistema de Prospecção

- **Contador de Recursos**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.9
  - Componente: Prospeccao.tsx
  - Análise: ANALISE_POR_ARQUIVO.md → Seção 6

---

### 🔧 ASPECTOS TÉCNICOS

#### Persistência de Dados

- **LocalStorage**
  - Localização: ANALISE_REQUISITOS.md → Seção 8.1
  - Estrutura: RESUMO_EXECUTIVO.md → Arquitetura de Dados
  - Exemplo: DIAGRAMAS_CASOS_USO.md → Exemplo 2

#### Integrações Externas

- **Google Gemini API**
  - Localização: ANALISE_REQUISITOS.md → Seção 2.11
  - Configuração: RESUMO_EXECUTIVO.md → Integrações Externas
  
- **html2pdf.js**
  - Localização: ANALISE_REQUISITOS.md → Seção 1.2.7
  - Configuração: RESUMO_EXECUTIVO.md → Integrações Externas

#### Cálculos Automáticos

- **Lista Completa**
  - Localização: ANALISE_REQUISITOS.md → Seção 9.3
  - Fórmulas: RESUMO_EXECUTIVO.md → Fórmulas de Cálculo
  - Exemplos: DIAGRAMAS_CASOS_USO.md → Exemplo 1

---

## 🗂️ ESTRUTURA DOS DOCUMENTOS

### RESUMO_EXECUTIVO.md (10.000+ palavras)

**Conteúdo:**
- Visão geral do sistema
- Funcionalidades principais (bullet points)
- Estrutura de dados (diagrama)
- Tabelas de referência
- Cálculos em formato de fórmula
- Checklist de implementação
- Próximos passos para Angular

**Melhor para:**
- Apresentar o projeto para stakeholders
- Onboarding rápido de desenvolvedores
- Referência rápida de fórmulas e tabelas

---

### ANALISE_REQUISITOS.md (15.000+ palavras)

**Conteúdo:**
- Análise detalhada por funcionalidade
- Regras de negócio completas
- Estruturas de dados TypeScript
- Fluxos de usuário principais
- Casos de uso especiais
- Resumo de entidades
- Glossário de termos do domínio
- Considerações para migração Angular

**Melhor para:**
- Entender requisitos completos
- Implementar funcionalidades
- Validar comportamentos
- Documentação de referência

---

### ANALISE_POR_ARQUIVO.md (12.000+ palavras)

**Conteúdo:**
- Análise de cada arquivo/componente
- Responsabilidades de cada componente
- Funções principais e seus algoritmos
- Estrutura de dados específica
- Layout visual (ASCII art)
- Regras de negócio por componente
- Matriz de dependências
- Checklist de implementação

**Melhor para:**
- Implementar componentes Angular
- Entender arquitetura do código
- Dividir trabalho por componente
- Debugging de funcionalidades

---

### DIAGRAMAS_CASOS_USO.md (11.000+ palavras)

**Conteúdo:**
- Diagramas de fluxo (ASCII)
- Casos de uso detalhados passo a passo
- Exemplos de dados reais (JSON)
- Testes de validação
- Fluxos alternativos
- Cenários de erro

**Melhor para:**
- Visualizar fluxos completos
- Escrever testes automatizados
- Entender edge cases
- Validar implementação

---

## 🎯 PRÓXIMOS PASSOS PARA MIGRAÇÃO ANGULAR

### FASE 1: PLANEJAMENTO E SETUP (1 semana)

#### 1.1 Decisões Arquiteturais
- [ ] Definir versão do Angular (17+)
- [ ] Decidir: Standalone Components ou NgModules
- [ ] Decidir: Signals ou RxJS para estado
- [ ] Definir estratégia de estilização (Tailwind via CDN ou instalado)

#### 1.2 Setup do Projeto
```bash
ng new klayrah-rpg-angular
cd klayrah-rpg-angular
npm install @google/generative-ai
npm install html2pdf.js
npx tailwindcss init
```

#### 1.3 Estrutura de Pastas Sugerida
```
src/
├── app/
│   ├── core/
│   │   ├── models/
│   │   │   ├── character-sheet.model.ts
│   │   │   ├── image-item.model.ts
│   │   │   └── note.model.ts
│   │   ├── services/
│   │   │   ├── storage.service.ts
│   │   │   ├── sheet.service.ts
│   │   │   ├── gemini.service.ts
│   │   │   └── pdf.service.ts
│   │   └── constants/
│   │       ├── xp-levels.const.ts
│   │       ├── classes.const.ts
│   │       └── default-sheet.const.ts
│   ├── shared/
│   │   ├── components/
│   │   │   ├── input-field/
│   │   │   ├── select-field/
│   │   │   └── value-box/
│   │   └── pipes/
│   │       ├── calculate-total.pipe.ts
│   │       └── calculate-impeto.pipe.ts
│   ├── features/
│   │   ├── character-sheet/
│   │   │   ├── character-sheet.component.ts
│   │   │   └── components/
│   │   │       ├── attributes-section/
│   │   │       ├── bonus-section/
│   │   │       ├── aptitudes-section/
│   │   │       ├── life-section/
│   │   │       ├── essence-section/
│   │   │       └── prospeccao/
│   │   ├── gallery/
│   │   │   └── gallery.component.ts
│   │   ├── notes/
│   │   │   └── notes.component.ts
│   │   └── sheet-manager/
│   │       └── sheet-manager.component.ts
│   ├── layout/
│   │   ├── header/
│   │   └── navigation/
│   └── app.component.ts
```

---

### FASE 2: SERVIÇOS CORE (1 semana)

#### 2.1 StorageService
**Responsabilidade:** Abstrair acesso ao localStorage

**Métodos principais:**
```typescript
class StorageService {
  get<T>(key: string): T | null
  set<T>(key: string, value: T): void
  remove(key: string): void
  clear(): void
}
```

**Referência:** ANALISE_REQUISITOS.md → Seção 8.1

---

#### 2.2 SheetService
**Responsabilidade:** Gerenciar estado das fichas

**Métodos principais:**
```typescript
class SheetService {
  // Observables/Signals
  allSheets$: Observable<CharacterSheetData[]>
  activeSheet$: Observable<CharacterSheetData | null>
  
  // CRUD
  loadSheets(): void
  createSheet(): CharacterSheetData
  updateSheet(sheet: CharacterSheetData): void
  deleteSheet(id: string): void
  setActiveSheet(id: string): void
}
```

**Referência:** ANALISE_POR_ARQUIVO.md → Seção 1

---

#### 2.3 GeminiService
**Responsabilidade:** Integração com Google Gemini AI

**Métodos principais:**
```typescript
class GeminiService {
  analyzeImage(image: ImageItem, type: 'character' | 'item'): Observable<string>
  generateInterpretation(traits: PersonalityTraits): Observable<string>
}
```

**Referência:** ANALISE_REQUISITOS.md → Seção 2.11 e 3.2.8

---

#### 2.4 PdfService
**Responsabilidade:** Exportação de PDF

**Métodos principais:**
```typescript
class PdfService {
  exportToPdf(elementId: string, filename: string): Promise<void>
}
```

**Referência:** ANALISE_REQUISITOS.md → Seção 1.2.7

---

### FASE 3: MODELOS E CONSTANTES (2 dias)

#### 3.1 Criar Interfaces TypeScript
Copiar todas as interfaces de CharacterSheet.tsx:
- `CharacterSheetData`
- `Attribute`, `Attributes`
- `Aptidao`
- `BonusDetail`, `Bonuses`
- `Ameaca`
- `ImageItem`
- `Note`

**Referência:** ANALISE_POR_ARQUIVO.md → Seção 2

---

#### 3.2 Criar Constantes
- `XP_LEVELS` (array de 35 níveis)
- `CLASSES` (array de classes)
- `DEFAULT_SHEET` (ficha padrão)
- `DICE_SIDES` (para Prospecção)

**Referência:** ANALISE_REQUISITOS.md → Várias seções

---

### FASE 4: COMPONENTES COMPARTILHADOS (3 dias)

#### 4.1 InputFieldComponent
Campo de input reutilizável com label

#### 4.2 SelectFieldComponent
Campo de seleção reutilizável com label

#### 4.3 ValueBoxComponent
Display somente leitura com label e valor

**Referência:** ANALISE_POR_ARQUIVO.md → Seção 2.12

---

### FASE 5: PIPES CUSTOMIZADOS (2 dias)

#### 5.1 CalculateTotalPipe
Calcula total de atributo (base + nivel + outros)

#### 5.2 CalculateImpetoPipe
Calcula ímpeto baseado no atributo e tipo

#### 5.3 CalculateBonusPipe
Calcula bônus base de dois atributos

**Referência:** RESUMO_EXECUTIVO.md → Fórmulas de Cálculo

---

### FASE 6: COMPONENTES PRINCIPAIS (2 semanas)

#### 6.1 AppComponent
- Sistema de abas
- Gerenciamento de estado global
- Renderização condicional

**Referência:** ANALISE_POR_ARQUIVO.md → Seção 1

---

#### 6.2 CharacterSheetComponent
**Subcomponentes:**
- AttributesSectionComponent (tabela de atributos)
- BonusSectionComponent (grid de bônus)
- AptitudesSectionComponent (listas de aptidões)
- LifeSectionComponent (vida e membros)
- EssenceSectionComponent (essência)
- ProspeccaoComponent (contador)

**Referência:** ANALISE_POR_ARQUIVO.md → Seção 2

---

#### 6.3 GalleryComponent
- Upload múltiplo
- Validação de tamanho
- Grid de miniaturas
- Visualização detalhada
- Integração com GeminiService

**Referência:** ANALISE_POR_ARQUIVO.md → Seção 3

---

#### 6.4 NotesComponent
- Campo de texto
- Lista de notas
- CRUD de notas

**Referência:** ANALISE_POR_ARQUIVO.md → Seção 4

---

#### 6.5 SheetManagerComponent
- Lista de fichas
- Destaque de ativa
- Seleção e exclusão

**Referência:** ANALISE_POR_ARQUIVO.md → Seção 5

---

### FASE 7: INTEGRAÇÃO E REFINAMENTO (1 semana)

#### 7.1 Validações
- [ ] Validação de pontos de atributo
- [ ] Validação de upload de arquivo
- [ ] Confirmações de exclusão

#### 7.2 Loading States
- [ ] Upload de imagem
- [ ] Análise com IA
- [ ] Exportação PDF

#### 7.3 Tratamento de Erros
- [ ] API do Gemini indisponível
- [ ] LocalStorage cheio
- [ ] Arquivo muito grande

**Referência:** ANALISE_REQUISITOS.md → Seção 9

---

### FASE 8: ESTILOS E RESPONSIVIDADE (3 dias)

#### 8.1 Configurar Tailwind
- [ ] Instalar e configurar Tailwind CSS
- [ ] Criar classes customizadas se necessário
- [ ] Estilos de impressão (@media print)

#### 8.2 Testar Responsividade
- [ ] Mobile (320px - 768px)
- [ ] Tablet (768px - 1024px)
- [ ] Desktop (1024px+)

**Referência:** ANALISE_REQUISITOS.md → Seção 10

---

### FASE 9: TESTES (1 semana)

#### 9.1 Testes Unitários
- [ ] Serviços (Storage, Sheet, Gemini, Pdf)
- [ ] Pipes (cálculos)
- [ ] Componentes (lógica)

#### 9.2 Testes de Integração
- [ ] Fluxo de criação de ficha
- [ ] Fluxo de edição e auto-save
- [ ] Fluxo de exclusão com proteção
- [ ] Fluxo de upload e análise

#### 9.3 Testes E2E
- [ ] Caso de uso 1: Criar primeiro personagem
- [ ] Caso de uso 2: Gerenciar múltiplas fichas
- [ ] Caso de uso 3: Upload e análise de imagem
- [ ] Caso de uso 4: Rastrear dano em combate
- [ ] Caso de uso 5: Ganhar nível e distribuir pontos

**Referência:** DIAGRAMAS_CASOS_USO.md → Todos os casos de uso

---

### FASE 10: OTIMIZAÇÃO E DEPLOY (3 dias)

#### 10.1 Otimizações
- [ ] Lazy loading de componentes
- [ ] OnPush change detection
- [ ] Debounce em campos de input
- [ ] Compression de imagens antes do Base64

#### 10.2 Build de Produção
```bash
ng build --configuration production
```

#### 10.3 Deploy
Opções:
- GitHub Pages (estático)
- Vercel (estático)
- Netlify (estático)
- Firebase Hosting (estático)

**Observação:** Sistema é 100% frontend, não requer backend

---

## 📊 MÉTRICAS DO PROJETO ORIGINAL

### Linhas de Código
- **App.tsx:** 202 linhas
- **CharacterSheet.tsx:** 767 linhas
- **Gallery.tsx:** 205 linhas
- **Notes.tsx:** 93 linhas
- **SheetManager.tsx:** 55 linhas
- **Prospeccao.tsx:** 79 linhas
- **DiceRoller.tsx:** 0 linhas (vazio)
- **Total:** ~1.401 linhas

### Complexidade
- **Cálculos automáticos:** 50+
- **Estados gerenciados:** 15+
- **Componentes:** 7
- **Integrações externas:** 2 (Gemini, html2pdf)
- **Storage keys:** 5 tipos

### Estimativa Angular
**Linhas de código esperadas:** ~2.500-3.000 linhas
- Services: ~500 linhas
- Components: ~1.800 linhas
- Models/Interfaces: ~300 linhas
- Pipes: ~200 linhas
- Tests: ~1.500 linhas (se 50% coverage)

**Tempo estimado:** 6-8 semanas (1 desenvolvedor full-time)

---

## ✅ CHECKLIST FINAL DE MIGRAÇÃO

### Funcionalidades Obrigatórias
- [ ] Criar múltiplas fichas
- [ ] Auto-save em localStorage
- [ ] Todos os 170+ campos da ficha
- [ ] 50+ cálculos automáticos
- [ ] Sistema de validação de pontos
- [ ] Exportação de PDF
- [ ] Upload de imagens (20MB limit)
- [ ] 2 galerias independentes por ficha
- [ ] Sistema de anotações por ficha
- [ ] Análise de imagem com IA (opcional)
- [ ] Sugestão de interpretação com IA (opcional)
- [ ] Sistema de prospecção
- [ ] Layout responsivo
- [ ] Estilos de impressão

### Funcionalidades Opcionais (Melhorias)
- [ ] Modo offline completo (PWA)
- [ ] Backup/restore em JSON
- [ ] Importação de fichas
- [ ] Sistema de temas (dark mode)
- [ ] Histórico de alterações (undo/redo)
- [ ] Calculadora de combate integrada
- [ ] Sistema de dados virtuais (DiceRoller)
- [ ] Compartilhamento de fichas (via link/QR)
- [ ] Sincronização em nuvem
- [ ] Autenticação e multi-device

### Qualidade de Código
- [ ] Testes unitários (>50% coverage)
- [ ] Testes E2E (casos principais)
- [ ] Documentação de componentes
- [ ] README atualizado
- [ ] CHANGELOG
- [ ] ESLint configurado
- [ ] Prettier configurado
- [ ] Build sem warnings

---

## 🎓 CONHECIMENTOS NECESSÁRIOS

### Angular
- ✅ Components e Templates
- ✅ Services e Dependency Injection
- ✅ RxJS ou Signals
- ✅ Forms (Reactive ou Template-driven)
- ✅ Pipes
- ✅ Change Detection
- ⚠️ Routing (simples, apenas para abas)
- ❌ Guards (não necessário)
- ❌ Interceptors (não necessário)
- ❌ Backend/API (não necessário)

### TypeScript
- ✅ Interfaces e Types
- ✅ Generics
- ✅ Type Guards
- ✅ Utility Types
- ✅ Enums e Const Assertions

### CSS/Tailwind
- ✅ Flexbox e Grid
- ✅ Responsividade
- ✅ Media Queries (@media print)
- ✅ Tailwind utility classes

### APIs Externas
- ⚠️ Google Gemini AI (opcional)
- ⚠️ html2pdf.js

---

## 📚 RECURSOS ADICIONAIS

### Documentação Angular
- https://angular.io/docs
- https://angular.io/guide/standalone-components
- https://angular.io/guide/signals

### Tailwind CSS
- https://tailwindcss.com/docs/installation/framework-guides#angular

### Google Gemini AI
- https://ai.google.dev/gemini-api/docs

### html2pdf.js
- https://github.com/eKoopmans/html2pdf.js

---

## 🆘 FAQ - DÚVIDAS COMUNS

### P: Posso usar NgRx ou Akita para estado?
**R:** Não é necessário. Um service com BehaviorSubject ou Signals é suficiente. O estado é simples (lista de fichas + ficha ativa).

### P: Devo quebrar CharacterSheet em componentes menores?
**R:** Sim, recomendado! Sugerimos no mínimo 6 subcomponentes (Atributos, Bônus, Aptidões, Vida, Essência, Prospecção).

### P: Como lidar com a limitação de 5-10MB do localStorage?
**R:** 
1. Comprimir imagens antes de converter para Base64
2. Implementar sistema de limpeza de fichas antigas
3. Avisar usuário quando próximo do limite
4. Considerar IndexedDB para imagens grandes

### P: A API do Gemini é obrigatória?
**R:** Não. As funcionalidades de IA são opcionais. O sistema funciona completamente sem elas.

### P: Como testar sem perder meus dados de teste?
**R:** 
1. Usar diferente localStorage key (ex: `test_allCharacterSheets`)
2. Implementar botão de import/export JSON
3. Usar Chrome DevTools para copiar localStorage

### P: Devo usar Standalone Components ou NgModules?
**R:** Recomendamos Standalone Components (Angular 17+) para projeto novo. Código mais limpo e menos boilerplate.

### P: Como implementar o auto-save sem causar lag?
**R:** Use debounce no RxJS:
```typescript
this.sheet$.pipe(
  debounceTime(500),
  distinctUntilChanged()
).subscribe(sheet => this.storageService.save(sheet))
```

---

## 📞 SUPORTE

Para dúvidas sobre a documentação ou o processo de migração, consulte:
1. Os 4 documentos principais
2. O código-fonte original React
3. A documentação oficial do Angular

---

## 📝 CONCLUSÃO

Esta documentação completa fornece tudo que você precisa para migrar o sistema Klayrah RPG de React para Angular:

✅ **Requisitos detalhados** sem linguagem específica de React  
✅ **Análise por arquivo** com responsabilidades claras  
✅ **Diagramas de fluxo** para visualizar comportamentos  
✅ **Casos de uso** passo a passo  
✅ **Exemplos de dados** reais  
✅ **Testes de validação** para garantir qualidade  
✅ **Roadmap de implementação** fase por fase  
✅ **Checklists** de funcionalidades  
✅ **Estimativas** de tempo e esforço  

**Tempo total estimado:** 6-8 semanas (1 desenvolvedor)  
**Complexidade:** Média-Alta (devido à quantidade de cálculos)  
**Risco:** Baixo (requisitos bem definidos, sem backend)

Boa sorte com a migração! 🚀

---

**Versão da Documentação:** 1.0  
**Data:** Janeiro 2026  
**Projeto:** Klayrah RPG  
**Status:** Completo e Pronto para Implementação
