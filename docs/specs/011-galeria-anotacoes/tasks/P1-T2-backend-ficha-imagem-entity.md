# T2 — Backend: Entity FichaImagem + Repository + Configuracao Cloudinary

> Fase: P1 (Backend)
> Estimativa: 0.5 dia
> Depende de: nenhuma (paralela a T0 e T1)
> Bloqueia: T3 (Service + Controller de Galeria)

---

## Objetivo

Criar a entidade `FichaImagem`, o enum `TipoImagem` e o `FichaImagemRepository`. Adicionar a dependencia do SDK Cloudinary ao `pom.xml` e criar o bean de configuracao `CloudinaryConfig`. Esta task estabelece o modelo de dados e a infraestrutura de integracao com o Cloudinary sem ainda expor endpoints (isso e T3).

---

## Mudanca em relacao ao modelo anterior

O modelo original usava URL externa fornecida pelo usuario. A decisao do PO alterou para **upload real via Cloudinary**. As principais diferencas:

| Campo (antes) | Campo (agora) | Motivo |
|---------------|---------------|--------|
| `url` VARCHAR(2048) | `urlCloudinary` VARCHAR(2048) | URL gerada pelo Cloudinary, nao pelo usuario |
| (nao existia) | `publicId` VARCHAR(512) | Obrigatorio para deletar o arquivo no Cloudinary |
| `descricao` TEXT | Removido | Simplificado para MVP de upload — titulo cobre o caso de uso |

---

## Arquivos a Criar

### 1. Dependencia Maven

Adicionar no `pom.xml`:

```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http5</artifactId>
    <version>1.39.0</version>
</dependency>
```

> Usar `cloudinary-http5` (nao `cloudinary-http44`) — compativel com Jakarta EE e Java 25.
> Verificar se ha versao mais recente em https://mvnrepository.com/artifact/com.cloudinary/cloudinary-http5 antes de commitar.

### 2. `application.properties` — adicionar propriedades Cloudinary

```properties
# Cloudinary (credenciais injetadas via variaveis de ambiente)
cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api-key=${CLOUDINARY_API_KEY}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}
```

Para `application-test.properties` (testes H2):

```properties
# Cloudinary mockado em testes — valores ficticios (o bean sera mockado via @MockBean)
cloudinary.cloud-name=test-cloud
cloudinary.api-key=test-key
cloudinary.api-secret=test-secret
```

### 3. `CloudinaryConfig.java`

```
src/main/java/br/com/hydroom/rpg/fichacontrolador/config/CloudinaryConfig.java
```

```java
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key",    apiKey,
            "api_secret", apiSecret,
            "secure",     true
        ));
    }
}
```

> Import: `import com.cloudinary.Cloudinary; import com.cloudinary.utils.ObjectUtils;`

### 4. `TipoImagem.java`

```
src/main/java/br/com/hydroom/rpg/fichacontrolador/model/enums/TipoImagem.java
```

```java
/**
 * Tipo de imagem associada a uma ficha.
 * AVATAR: imagem principal do personagem (apenas uma ativa por ficha).
 * GALERIA: imagens secundarias de referencia.
 */
public enum TipoImagem {
    AVATAR,
    GALERIA
}
```

### 5. `FichaImagem.java`

```
src/main/java/br/com/hydroom/rpg/fichacontrolador/model/FichaImagem.java
```

Campos:

| Campo | Tipo JPA | Coluna | Restricoes |
|-------|----------|--------|-----------|
| `id` | `@Id @GeneratedValue` | `id` | PK |
| `ficha` | `@ManyToOne(LAZY)` | `ficha_id` | NOT NULL |
| `urlCloudinary` | `@Column(name="url_cloudinary", length=2048)` | `url_cloudinary` | NOT NULL |
| `publicId` | `@Column(name="public_id", length=512)` | `public_id` | NOT NULL — usado para deletar no Cloudinary |
| `titulo` | `@Column(length=200)` | `titulo` | nullable |
| `tipoImagem` | `@Enumerated(STRING)` | `tipo_imagem` | NOT NULL, length=20 |
| `ordemExibicao` | `@Column(name="ordem_exibicao")` | `ordem_exibicao` | NOT NULL, default 0 |

Anotacoes de classe:

```java
@Entity
@Table(name = "ficha_imagens", indexes = {
    @Index(name = "idx_ficha_imagem_ficha", columnList = "ficha_id"),
    @Index(name = "idx_ficha_imagem_tipo",  columnList = "tipo_imagem")
})
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaImagem extends BaseEntity { ... }
```

### 6. `FichaImagemRepository.java`

```
src/main/java/br/com/hydroom/rpg/fichacontrolador/repository/FichaImagemRepository.java
```

```java
public interface FichaImagemRepository extends JpaRepository<FichaImagem, Long> {

    // Lista todas as imagens de uma ficha (sem deletadas, via @SQLRestriction)
    List<FichaImagem> findByFichaIdOrderByTipoImagemAscOrdemExibicaoAsc(Long fichaId);

    // Busca o avatar ativo de uma ficha (para verificar substituicao antes de novo upload)
    Optional<FichaImagem> findByFichaIdAndTipoImagem(Long fichaId, TipoImagem tipoImagem);

    // Conta imagens ativas de uma ficha (para verificar limite de 20)
    long countByFichaId(Long fichaId);
}
```

> Nota: `findByFichaIdOrderByTipoImagemAscOrdemExibicaoAsc` ordena AVATAR (A) antes de GALERIA (G) lexicograficamente. Se necessario, o service pode reordenar em Java para garantia absoluta.

---

## Convencoes de Nomenclatura

- Tabela: `ficha_imagens` (plural, snake_case)
- Enum no banco: `VARCHAR(20)` com `EnumType.STRING`
- Campos de audit: herdados de `BaseEntity`
- Soft delete: `deleted_at` (herdado, `@SQLRestriction` na entity)
- `publicId`: campo obrigatorio — nunca pode ser null em registros persistidos

---

## Criterios de Aceite

- [ ] Dependencia `cloudinary-http5` adicionada no `pom.xml` e resolve sem conflitos
- [ ] `CloudinaryConfig.java` criado com bean `Cloudinary` configurado via variaveis de ambiente
- [ ] `TipoImagem.java` criado com valores `AVATAR` e `GALERIA`
- [ ] `FichaImagem.java` estende `BaseEntity`, tem campos `urlCloudinary` e `publicId` (NOT NULL)
- [ ] Anotacoes `@SQLRestriction("deleted_at IS NULL")` e indexes presentes
- [ ] `FichaImagemRepository.java` com os 3 metodos de query
- [ ] `application-test.properties` tem valores ficticios para Cloudinary (evita NPE ao subir contexto)
- [ ] Testes H2 existentes nao quebram ao subir o contexto (schema criado automaticamente)
- [ ] `./mvnw test` continua passando apos adicionar a dependencia
