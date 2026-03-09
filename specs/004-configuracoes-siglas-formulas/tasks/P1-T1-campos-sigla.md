# P1-T1 — Campos sigla em BonusConfig e VantagemConfig

## Objetivo
Adicionar o campo `sigla` nas entities BonusConfig e VantagemConfig, atualizar DTOs e mappers.

## Regras de negócio
- `BonusConfig.sigla`: **obrigatório** (`@NotBlank`), 2-5 chars, unique por jogo
- `VantagemConfig.sigla`: **opcional** (nullable), 2-5 chars, unique por jogo (nulos não contam para unicidade)
- Padrão de referência: `AtributoConfig.abreviacao` e `AtributoResponse.abreviacao`

## Steps

### 1. BonusConfig.java
```java
@NotBlank(message = ValidationMessages.BonusConfig.SIGLA_OBRIGATORIA)
@Size(min = 2, max = 5, message = ValidationMessages.BonusConfig.SIGLA_TAMANHO)
@Column(name = "sigla", nullable = false, length = 5)
private String sigla;
```
Adicionar unique constraint na anotação `@Table`:
```java
@UniqueConstraint(name = "uk_bonus_config_jogo_sigla", columnNames = {"jogo_id", "sigla"})
```

### 2. VantagemConfig.java
```java
@Size(min = 2, max = 5, message = ValidationMessages.VantagemConfig.SIGLA_TAMANHO)
@Column(name = "sigla", nullable = true, length = 5)
private String sigla;
```
Unique constraint com `nullable=true` — o banco ignora nulos na constraint:
```java
@UniqueConstraint(name = "uk_vantagem_config_jogo_sigla", columnNames = {"jogo_id", "sigla"})
```

### 3. ValidationMessages
Adicionar constantes de mensagem em `ValidationMessages` para os dois campos.

### 4. DTOs
- `CreateBonusRequest`: adicionar `@NotBlank @Size(min=2,max=5) String sigla`
- `UpdateBonusRequest`: adicionar `@Size(min=2,max=5) String sigla` (nullable para update parcial)
- `BonusResponse`: adicionar `String sigla`
- `CreateVantagemRequest`: adicionar `@Size(min=2,max=5) String sigla` (nullable)
- `UpdateVantagemRequest`: idem
- `VantagemResponse`: adicionar `String sigla`

### 5. Mappers
- `BonusConfigMapper`: mapear `sigla` bidirecionalmente (entity ↔ DTO)
  - No update mapper: `@Mapping(target="sigla", source="sigla", nullValuePropertyMappingStrategy=IGNORE)`
- `VantagemConfigMapper`: idem

### 6. BonusConfiguracaoService.atualizarCampos()
Incluir atualização de `sigla` (se não nula no `atualizado`).

### 7. AtributoConfiguracaoService — campos esquecidos
Aproveitar este PR para corrigir: `atualizarCampos()` atualmente ignora `abreviacao`, `valorMinimo`, `valorMaximo`. Incluí-los.

## Acceptance Checks
- [ ] BonusConfig com sigla nula é rejeitado com ValidationException
- [ ] BonusConfig aceito com sigla válida de 2-5 chars
- [ ] VantagemConfig aceito sem sigla (nullable)
- [ ] VantagemConfig aceito com sigla válida
- [ ] Sigla aparece nos responses de BonusResponse e VantagemResponse
- [ ] `./mvnw test` passa

## File Checklist
- `model/BonusConfig.java`
- `model/VantagemConfig.java`
- `exception/ValidationMessages.java`
- `dto/request/configuracao/CreateBonusRequest.java`
- `dto/request/configuracao/UpdateBonusRequest.java`
- `dto/response/configuracao/BonusResponse.java`
- `dto/request/configuracao/CreateVantagemRequest.java`
- `dto/request/configuracao/UpdateVantagemRequest.java`
- `dto/response/configuracao/VantagemResponse.java`
- `mapper/configuracao/BonusConfigMapper.java`
- `mapper/configuracao/VantagemConfigMapper.java`
- `service/configuracao/BonusConfiguracaoService.java`
- `service/configuracao/AtributoConfiguracaoService.java`

## References
- `model/AtributoConfig.java` — padrão de campo abreviacao
- `docs/backend/02-entities-dtos.md`
- `docs/backend/06-mappers.md`
