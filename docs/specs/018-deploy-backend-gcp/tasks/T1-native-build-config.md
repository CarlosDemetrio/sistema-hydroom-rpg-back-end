# T1 — pom.xml: Profile Native + GraalVM + RuntimeHints

> Fase: Backend/Config | Prioridade: P0
> Dependencias: Nenhuma (backlog funcional concluido)
> Bloqueia: T2 (Dockerfile.native)
> Estimativa: 1-2 dias

---

## Objetivo

Adicionar suporte a compilacao nativa (GraalVM AOT) ao projeto Spring Boot existente, criando um Maven profile `native` que gera um executavel standalone sem dependencia de JVM. Registrar hints de reflection para bibliotecas que usam reflection em runtime (exp4j, Bucket4j).

---

## Arquivos a Editar

| Arquivo | Acao | Descricao |
|---------|------|-----------|
| `pom.xml` | EDITAR | Adicionar profile `native` com `native-maven-plugin` + `spring-boot-maven-plugin` configurado |
| `src/main/java/.../config/NativeConfig.java` | CRIAR | Classe `@Configuration` com `@ImportRuntimeHints` |
| `src/main/java/.../config/NativeHintsRegistrar.java` | CRIAR | Implementacao de `RuntimeHintsRegistrar` para exp4j e Bucket4j |

---

## Mudancas no pom.xml

### Adicionar property

```xml
<properties>
    <!-- ...existentes... -->
    <native.maven.plugin.version>0.10.6</native.maven.plugin.version>
</properties>
```

### Adicionar profile `native` (apos `</build>`)

```xml
<profiles>
    <profile>
        <id>native</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.graalvm.buildtools</groupId>
                    <artifactId>native-maven-plugin</artifactId>
                    <version>${native.maven.plugin.version}</version>
                    <executions>
                        <execution>
                            <id>build-native</id>
                            <goals>
                                <goal>compile-no-fork</goal>
                            </goals>
                            <phase>package</phase>
                        </execution>
                    </executions>
                    <configuration>
                        <buildArgs>
                            <buildArg>--no-fallback</buildArg>
                            <buildArg>-H:+ReportExceptionStackTraces</buildArg>
                        </buildArgs>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <configuration>
                        <excludes>
                            <exclude>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                            </exclude>
                        </excludes>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

---

## NativeConfig.java

```java
package br.com.hydroom.rpg.config;

import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(NativeHintsRegistrar.class)
public class NativeConfig {
    // Ativado apenas pelo Spring AOT processing (build nativo)
}
```

---

## NativeHintsRegistrar.java

```java
package br.com.hydroom.rpg.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class NativeHintsRegistrar implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // exp4j: usa reflection para parsear expressoes matematicas
        hints.reflection().registerType(
            net.objecthunter.exp4j.Expression.class,
            MemberCategory.values()
        );
        hints.reflection().registerType(
            net.objecthunter.exp4j.ExpressionBuilder.class,
            MemberCategory.values()
        );

        // Bucket4j: rate limiting interno
        // Adicionar mais classes aqui conforme necessario
        // durante os testes do build nativo
    }
}
```

---

## Validacao Local

```bash
# 1. Verificar que build JVM normal nao quebrou
./mvnw clean package -DskipTests
java -jar target/ficha-controlador-0.0.1-SNAPSHOT.jar  # deve iniciar

# 2. Build nativo (requer GraalVM 25 instalado localmente OU Docker)
./mvnw clean package -Pnative -DskipTests

# 3. Executar native image
./target/ficha-controlador  # deve iniciar em < 500ms

# 4. Testar endpoints criticos
curl http://localhost:8081/actuator/health
# Testar um endpoint com formula: FormulaEvaluatorService
# Testar rate limiting: enviar muitas requests
```

> **NOTA:** Se nao tiver GraalVM local, o build nativo sera testado via Docker (T2).

---

## Troubleshooting Native Build

Se o build falhar com erros de reflection:

1. Executar com tracing agent:
   ```bash
   java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image \
     -jar target/ficha-controlador-0.0.1-SNAPSHOT.jar
   # Exercitar os endpoints manualmente
   # O agent gera reflect-config.json, resource-config.json etc.
   ```

2. Os arquivos gerados em `META-INF/native-image/` sao automaticamente incluidos no build nativo.

3. Se exp4j falhar: considerar substituir por implementacao de formula sem reflection (pos-MVP).

---

## Criterios de Aceitacao

- [ ] `./mvnw clean package -DskipTests` continua funcionando (sem regressao no build JVM)
- [ ] `./mvnw test` continua com 613+ testes passando
- [ ] Profile `native` adicionado ao pom.xml sem erros de sintaxe
- [ ] `NativeConfig.java` e `NativeHintsRegistrar.java` compilam sem erros
- [ ] Build nativo compila (localmente ou via Docker) — se falhar, documentar o erro

---

*Produzido por: Tech Lead | 2026-04-07*
