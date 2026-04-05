# T1 — Configurar JaCoCo no pom.xml com Threshold 75% Branch

> Fase: Backend Infraestrutura | Dependencias: Nenhuma | Bloqueia: T2, T3, T4
> Estimativa: 1–2 horas

---

## Objetivo

Configurar o plugin JaCoCo no `pom.xml` para gerar relatorios de cobertura de codigo e falhar o build se a cobertura de branch ficar abaixo de 75%.

---

## Passos de Implementacao

### Passo 1 — Adicionar plugin JaCoCo ao pom.xml

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <!-- Prepara o agente JaCoCo antes dos testes -->
        <execution>
            <id>prepare-agent</id>
            <goals><goal>prepare-agent</goal></goals>
        </execution>

        <!-- Gera relatorio apos os testes -->
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals><goal>report</goal></goals>
        </execution>

        <!-- Verifica threshold de cobertura -->
        <execution>
            <id>check</id>
            <phase>verify</phase>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.75</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>

    <!-- Exclusoes globais -->
    <configuration>
        <excludes>
            <exclude>**/dto/**</exclude>
            <exclude>**/mapper/**</exclude>
            <exclude>**/config/**</exclude>
            <exclude>**/*Application.class</exclude>
            <exclude>**/model/**</exclude>
        </excludes>
    </configuration>
</plugin>
```

### Passo 2 — Verificar geracao de relatorio

```bash
./mvnw verify
# Relatorio em: target/site/jacoco/index.html
```

### Passo 3 — Ajustar threshold inicial

Se a cobertura atual for menor que 75%, iniciar com um threshold mais baixo e subir progressivamente:

```xml
<!-- Fase 1: descobrir cobertura atual -->
<minimum>0.50</minimum>

<!-- Fase 2: apos T2-T4 adicionarem testes -->
<minimum>0.65</minimum>

<!-- Fase 3: target final -->
<minimum>0.75</minimum>
```

### Passo 4 — Verificar exclusoes

Executar `./mvnw verify` e abrir o relatorio HTML. Verificar que:
- Classes de DTOs (records) nao aparecem no relatorio
- Classes de mappers (MapStruct gerado) nao aparecem
- Classes de model (Lombok entities) nao aparecem
- Services e controllers aparecem com cobertura real

### Passo 5 — Adicionar ao .gitignore

```
# JaCoCo
target/site/jacoco/
```

---

## Exclusoes Justificadas

| Exclusao | Justificativa |
|----------|-------------|
| `**/dto/**` | Records Java sem logica — apenas estrutura de dados |
| `**/mapper/**` | Gerados em compile-time por MapStruct — nao ha codigo manual |
| `**/config/**` | Configuracao Spring (beans, security, CORS) — testados indiretamente |
| `**/*Application.class` | Main class — apenas bootstrap |
| `**/model/**` | Entities Lombok (getters/setters gerados) — metodos com logica cobertos via services |

---

## Criterios de Aceitacao

- [ ] `./mvnw verify` gera relatorio JaCoCo em `target/site/jacoco/index.html`
- [ ] Relatorio exclui DTOs, mappers, config e model
- [ ] Threshold configurado (inicialmente 50%, target final 75%)
- [ ] Build falha se cobertura abaixo do threshold
- [ ] Relatorio HTML abre corretamente no browser e mostra cobertura por classe
- [ ] `./mvnw test` continua passando (JaCoCo nao interfere nos testes)
