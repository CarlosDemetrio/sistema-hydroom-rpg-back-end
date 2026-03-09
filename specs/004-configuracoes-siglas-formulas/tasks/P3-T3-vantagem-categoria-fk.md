# P3-T3 — VantagemConfig → CategoriaVantagem FK

## Objetivo
Adicionar FK opcional de VantagemConfig para CategoriaVantagem e atualizar DTOs e mapper.

## Depende de
P3-T1 (CategoriaVantagem entity corrigida e persistível)

## Steps

### 1. VantagemConfig.java — adicionar FK

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "categoria_vantagem_id", nullable = true)
private CategoriaVantagem categoriaVantagem;
```

### 2. DTOs

**CreateVantagemRequest**: adicionar `Long categoriaVantagemId` (nullable)
**UpdateVantagemRequest**: idem
**VantagemResponse**: adicionar `Long categoriaVantagemId` e `String categoriaNome` (para evitar nested object)

### 3. Mapper

```java
// VantagemConfigMapper — adicionar:
@Mapping(target = "categoriaVantagemId", source = "categoriaVantagem.id")
@Mapping(target = "categoriaNome", source = "categoriaVantagem.nome")
VantagemResponse toResponse(VantagemConfig entity);

@Mapping(target = "categoriaVantagem", ignore = true)  // resolvido no controller
@Mapping(target = "jogo", ignore = true)
VantagemConfig toEntity(CreateVantagemRequest request);
```

### 4. Controller — resolver CategoriaVantagem

No controller, ao criar/atualizar VantagemConfig, se `categoriaVantagemId` não nulo:
```java
if (request.categoriaVantagemId() != null) {
    CategoriaVantagem cat = categoriaVantagemService.buscarPorId(request.categoriaVantagemId());
    entity.setCategoriaVantagem(cat);
}
```

**Validação extra**: garantir que a CategoriaVantagem pertence ao mesmo jogo da VantagemConfig.
```java
if (!cat.getJogo().getId().equals(jogoId)) {
    throw new ValidationException("Categoria não pertence ao jogo informado.");
}
```

### 5. VantagemConfiguracaoService — não precisa mudar

A FK é nullable; a lógica de negócio (verificar jogo) fica no controller. O service não vê DTOs.

## Acceptance Checks
- [ ] Criar VantagemConfig sem categoria → aceito (null FK)
- [ ] Criar VantagemConfig com categoriaVantagemId válido → FK salva
- [ ] VantagemResponse inclui categoriaVantagemId e categoriaNome
- [ ] Tentar usar categoriaVantagemId de outro jogo → ValidationException

## File Checklist
- `model/VantagemConfig.java`
- `dto/request/configuracao/CreateVantagemRequest.java`
- `dto/request/configuracao/UpdateVantagemRequest.java`
- `dto/response/configuracao/VantagemResponse.java`
- `mapper/configuracao/VantagemConfigMapper.java`
- `controller/configuracao/VantagemController.java`
