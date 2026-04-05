# T2 — Backend: Endpoint POST /api/v1/usuarios/me/role (Onboarding)

> Spec: 010 | Tipo: Backend | Depende de: T1 | Bloqueia: T6

---

## Objetivo

Criar o endpoint que permite ao usuario sem role definir seu perfil inicial (MESTRE ou JOGADOR). Tambem atualizar o `GET /api/v1/usuarios/me` para incluir o campo `role` no response, permitindo que o frontend detecte o estado de onboarding.

---

## Arquivos Afetados

| Arquivo | Acao |
|---------|------|
| `controller/UsuarioController.java` | Adicionar endpoint POST /me/role |
| `service/UsuarioService.java` | Adicionar metodo `definirRole(String role)` |
| `dto/request/DefinirRoleRequest.java` | Criar record novo |
| `dto/response/UsuarioResponse.java` | Verificar se campo `role` ja esta presente; adicionar se nao |

---

## Passos

### 1. Criar `DefinirRoleRequest.java`

```java
// dto/request/DefinirRoleRequest.java
public record DefinirRoleRequest(
    @NotBlank
    @Pattern(regexp = "MESTRE|JOGADOR", message = "Role deve ser MESTRE ou JOGADOR")
    String role
) {}
```

**Regra critica:** O pattern `MESTRE|JOGADOR` rejeita `ADMIN` no nivel de validacao de request. Mesmo que o service tenha a mesma checagem, a validacao de DTO e a primeira linha de defesa.

### 2. Atualizar `UsuarioResponse.java`

Verificar se o campo `role` ja esta presente no record. Se nao:
```java
public record UsuarioResponse(
    Long id,
    String nome,
    String email,
    String imagemUrl,
    String role,    // null se onboarding pendente
    // ... outros campos
) {}
```

### 3. Adicionar metodo em `UsuarioService.java`

```java
@Transactional
public Usuario definirRole(String novaRole) {
    Usuario usuario = buscarAtual();

    // Impedir autodefinicao de ADMIN
    if ("ADMIN".equalsIgnoreCase(novaRole)) {
        throw new ForbiddenException("Role ADMIN nao pode ser autodefinida.");
    }

    // Impedir alteracao de role ja definida (onboarding e apenas para role = null)
    if (usuario.getRole() != null) {
        throw new ConflictException(
            "Perfil ja definido como " + usuario.getRole() + ". Apenas um ADMIN pode alterar sua role.");
    }

    usuario.setRole(novaRole.toUpperCase());
    return usuarioRepository.save(usuario);
}
```

### 4. Adicionar endpoint em `UsuarioController.java`

```java
@PostMapping("/me/role")
@PreAuthorize("isAuthenticated()")
@Operation(
    summary = "Definir role no onboarding (primeiro acesso)",
    description = "Permite que usuario sem role defina seu perfil como MESTRE ou JOGADOR. " +
                  "Pode ser chamado apenas uma vez. Role ADMIN nao pode ser autodefinida."
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Role definida com sucesso"),
    @ApiResponse(responseCode = "400", description = "Role invalida (deve ser MESTRE ou JOGADOR)"),
    @ApiResponse(responseCode = "403", description = "Tentativa de definir role ADMIN"),
    @ApiResponse(responseCode = "409", description = "Role ja foi definida anteriormente")
})
public ResponseEntity<UsuarioResponse> definirRole(@Valid @RequestBody DefinirRoleRequest request) {
    var usuario = usuarioService.definirRole(request.role());
    var response = usuarioMapper.toResponse(usuario);
    return ResponseEntity.ok(response);
}
```

**Nota:** `@PreAuthorize("isAuthenticated()")` permite que usuarios sem role acessem este endpoint — e exatamente o que o onboarding precisa.

---

## Impacto no `GET /api/v1/usuarios/me`

O frontend precisa do campo `role` para detectar se o onboarding ja foi feito. Verificar `UsuarioResponse` e garantir que inclui `role`. Se o mapper nao mapeia o campo, atualizar `UsuarioMapper`.

---

## Testes Unitarios (UsuarioService)

```
Cenario 1: Definir JOGADOR com sucesso
- Given: usuario com role = null
- When: definirRole("JOGADOR")
- Then: usuario.role = "JOGADOR", retorna usuario atualizado

Cenario 2: Definir MESTRE com sucesso
- Given: usuario com role = null
- When: definirRole("MESTRE")
- Then: usuario.role = "MESTRE"

Cenario 3: Tentativa de definir ADMIN
- Given: qualquer usuario
- When: definirRole("ADMIN")
- Then: ForbiddenException lancada

Cenario 4: Role ja definida
- Given: usuario com role = "JOGADOR"
- When: definirRole("MESTRE")
- Then: ConflictException lancada
```

---

## Criterios de Aceitacao

- [ ] POST /me/role com `{ "role": "JOGADOR" }` retorna HTTP 200 para usuario sem role
- [ ] POST /me/role com `{ "role": "MESTRE" }` retorna HTTP 200 para usuario sem role
- [ ] POST /me/role com `{ "role": "ADMIN" }` retorna HTTP 403
- [ ] POST /me/role para usuario que ja tem role retorna HTTP 409
- [ ] GET /me retorna campo `role` (null se onboarding pendente)
- [ ] Apos POST /me/role, o usuario recebe a authority correta no proximo login
