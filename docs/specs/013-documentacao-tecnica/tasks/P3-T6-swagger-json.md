# T6 — Exportar swagger.json Versionado + Script de Geracao

> Fase: Shared | Dependencias: T2 (OpenAPI annotations prontas) | Bloqueia: Nenhuma
> Estimativa: 1–2 horas

---

## Objetivo

Configurar geracao automatica do arquivo `swagger.json` (OpenAPI spec) a partir da aplicacao, commitar no repositorio para versionamento, e criar script reutilizavel para regeneracao.

---

## Motivacao

- **Contract testing:** Frontend pode validar seus API services contra a spec oficial
- **Versionamento:** Diff no swagger.json mostra exatamente quais endpoints mudaram entre commits
- **Onboarding:** Novos devs podem ler a spec sem rodar o backend
- **Code generation:** Possibilita geracao futura de types TypeScript a partir da spec

---

## Passos de Implementacao

### Passo 1 — Verificar configuracao SpringDoc existente

O projeto ja usa SpringDoc/OpenAPI (Swagger UI funciona em `/swagger-ui.html`). Verificar se existe um endpoint JSON:
- `GET /v3/api-docs` — padrao SpringDoc
- Se nao existe, habilitar em `application.properties`:
  ```properties
  springdoc.api-docs.enabled=true
  springdoc.api-docs.path=/v3/api-docs
  ```

### Passo 2 — Criar script de exportacao

`scripts/export-swagger.sh`:
```bash
#!/bin/bash
# Exporta a spec OpenAPI do backend rodando localmente
# Pre-requisito: backend rodando em localhost:8080

set -euo pipefail

OUTPUT="docs/api/openapi.json"
URL="http://localhost:8080/v3/api-docs"

echo "Exportando OpenAPI spec de $URL..."
curl -s "$URL" | python3 -m json.tool > "$OUTPUT"
echo "Spec exportada para $OUTPUT"
echo "Endpoints: $(grep -c '"operationId"' "$OUTPUT")"
```

### Passo 3 — Criar diretorio e arquivo inicial

```bash
mkdir -p docs/api/
# Rodar o script para gerar o primeiro swagger.json
./scripts/export-swagger.sh
```

### Passo 4 — Atualizar MASTER.md

Adicionar na secao "Documentos de Referencia":
```markdown
| [`api/openapi.json`](api/openapi.json) | OpenAPI spec versionada (regenerar via `scripts/export-swagger.sh`) |
```

### Passo 5 — Adicionar ao .gitignore (NAO ignorar)

Verificar que `docs/api/openapi.json` NAO esta no `.gitignore`. O arquivo deve ser commitado.

---

## Alternativa: Geracao via Maven Plugin

Se preferir gerar sem rodar o backend:

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-maven-plugin</artifactId>
    <version>1.4</version>
    <executions>
        <execution>
            <goals><goal>generate</goal></goals>
            <phase>integration-test</phase>
        </execution>
    </executions>
    <configuration>
        <apiDocsUrl>http://localhost:8080/v3/api-docs</apiDocsUrl>
        <outputFileName>openapi.json</outputFileName>
        <outputDir>${project.basedir}/docs/api</outputDir>
    </configuration>
</plugin>
```

**Recomendacao:** Iniciar com o script manual (Passo 2). O plugin Maven pode ser adicionado em uma iteracao futura.

---

## Criterios de Aceitacao

- [ ] `docs/api/openapi.json` existe e contem a spec completa
- [ ] `scripts/export-swagger.sh` funciona quando o backend esta rodando
- [ ] O JSON e valido e pode ser importado no Swagger Editor (editor.swagger.io)
- [ ] `docs/MASTER.md` atualizado com link para `openapi.json`
- [ ] O arquivo nao esta no `.gitignore`
