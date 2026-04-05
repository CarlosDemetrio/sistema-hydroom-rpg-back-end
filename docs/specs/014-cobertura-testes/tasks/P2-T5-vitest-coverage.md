# T5 — Configurar Vitest Coverage com Relatorio HTML

> Fase: Frontend Infraestrutura | Dependencias: Nenhuma | Bloqueia: T6
> Estimativa: 1–2 horas

---

## Objetivo

Configurar o Vitest para gerar relatorios de cobertura de codigo no frontend Angular, identificando componentes e services sem cobertura adequada.

---

## Passos de Implementacao

### Passo 1 — Instalar provider de coverage

```bash
cd /Users/carlosdemetrio/IdeaProjects/ficha-controlador-front-end/ficha-controlador-front-end
npm install -D @vitest/coverage-v8
```

**Alternativa:** `@vitest/coverage-istanbul` se v8 tiver problemas com Angular.

### Passo 2 — Configurar vitest.config.ts

```typescript
// vitest.config.ts (ou dentro de defineConfig existente)
export default defineConfig({
  test: {
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      reportsDirectory: './coverage',
      include: ['src/**/*.ts'],
      exclude: [
        'src/**/*.spec.ts',
        'src/**/*.test.ts',
        'src/**/index.ts',
        'src/main.ts',
        'src/environments/**',
        'src/**/*.module.ts',
        'src/**/*.routes.ts',
      ],
    },
  },
});
```

### Passo 3 — Adicionar script ao package.json

```json
{
  "scripts": {
    "test:coverage": "vitest run --coverage"
  }
}
```

### Passo 4 — Executar e analisar

```bash
npm run test:coverage
# Relatorio HTML em: coverage/index.html
```

### Passo 5 — Adicionar ao .gitignore

```
# Vitest coverage reports
coverage/
```

### Passo 6 — Documentar gaps identificados

Apos gerar o primeiro relatorio, listar os componentes com 0% de cobertura em um arquivo temporario para priorizar T6:

```markdown
## Componentes sem cobertura (prioridade para T6)
- [ ] ficha-header.component.ts — 0%
- [ ] ficha-vantagens-tab.component.ts — 0%
- [ ] wizard step components — 0%
- [ ] signal stores — verificar
```

---

## Exclusoes Justificadas

| Exclusao | Justificativa |
|----------|-------------|
| `*.spec.ts` / `*.test.ts` | Arquivos de teste nao devem ser medidos |
| `index.ts` | Barrel exports sem logica |
| `main.ts` | Bootstrap da aplicacao |
| `environments/**` | Configuracao de ambiente |
| `*.module.ts` | Modulos Angular (declaracoes, sem logica) |
| `*.routes.ts` | Definicoes de rotas (configuracao, sem logica) |

---

## Criterios de Aceitacao

- [ ] `npm run test:coverage` gera relatorio HTML em `coverage/index.html`
- [ ] Relatorio mostra cobertura por arquivo e por funcao
- [ ] Exclusoes aplicadas (specs, environments, modules nao aparecem)
- [ ] `coverage/` adicionado ao `.gitignore`
- [ ] Lista de componentes com 0% cobertura documentada para T6
- [ ] Testes existentes continuam passando (`npx vitest run`)
