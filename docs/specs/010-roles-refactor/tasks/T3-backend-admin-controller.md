# T3 — Backend: AdminController — Listar usuarios e alterar role

> Spec: 010 | Tipo: Backend | Depende de: T1 | Bloqueia: T8

---

## Objetivo

Criar o `AdminController` com dois endpoints exclusivos para ADMIN:
- `GET /api/v1/admin/usuarios` — lista paginada de todos os usuarios
- `PUT /api/v1/admin/usuarios/{id}/role` — promove ou revoga a role de um usuario

---

## Arquivos Afetados

| Arquivo | Acao |
|---------|------|
| `controller/AdminController.java` | Criar novo controller |
| `service/AdminService.java` | Criar novo service |
| `dto/request/AlterarRoleRequest.java` | Criar record novo |
| `dto/response/UsuarioAdminResponse.java` | Criar record com campos extras (opcional; pode reusar UsuarioResponse) |
| `repository/UsuarioRepository.java` | Adicionar `findAll(Pageable)` e `countByRole(String role)` |

---

## Passos

### 1. Criar `AlterarRoleRequest.java`

```java
public record AlterarRoleRequest(
    @NotBlank
    @Pattern(regexp = "JOGADOR|MESTRE|ADMIN", message = "Role deve ser JOGADOR, MESTRE ou ADMIN")
    String role
) {}
```

Diferente de `DefinirRoleRequest`, aqui ADMIN e um valor valido — apenas ADMIN pode promover outros usuarios para ADMIN.

### 2. Criar `AdminService.java`

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminService {

    private final UsuarioRepository usuarioRepository;

    public Page<Usuario> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    @Transactional
    public Usuario alterarRole(Long usuarioId, String novaRole, Long adminId) {
        Usuario usuarioAlvo = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado: " + usuarioId));

        // Prevenir revogacao do ultimo ADMIN
        if ("ADMIN".equals(usuarioAlvo.getRole()) && !"ADMIN".equals(novaRole)) {
            long totalAdmins = usuarioRepository.countByRole("ADMIN");
            if (totalAdmins <= 1) {
                throw new ConflictException(
                    "Operacao recusada: nao e possivel revogar o ultimo ADMIN do sistema.");
            }
        }

        usuarioAlvo.setRole(novaRole.toUpperCase());
        return usuarioRepository.save(usuarioAlvo);
    }
}
```

### 3. Adicionar metodos em `UsuarioRepository.java`

```java
// Suporte a paginacao (Spring Data JPA ja fornece findAll(Pageable) via PagingAndSortingRepository)
// Verificar se UsuarioRepository ja estende JpaRepository — se sim, findAll(Pageable) ja existe.

long countByRole(String role);
```

### 4. Criar `AdminController.java`

```java
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Administracao", description = "Endpoints exclusivos para o perfil ADMIN")
public class AdminController {

    private final AdminService adminService;
    private final UsuarioMapper usuarioMapper;

    @GetMapping("/usuarios")
    @Operation(summary = "Listar todos os usuarios (Apenas ADMIN)",
               description = "Retorna lista paginada de todos os usuarios com nome, email e role atual.")
    public ResponseEntity<Page<UsuarioResponse>> listarUsuarios(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        var page = adminService.listarUsuarios(pageable)
            .map(usuarioMapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/usuarios/{id}/role")
    @Operation(summary = "Alterar role de usuario (Apenas ADMIN)",
               description = "Promove ou revoga a role de qualquer usuario. " +
                             "Nao e possivel revogar o ultimo ADMIN do sistema.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Role alterada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
        @ApiResponse(responseCode = "409", description = "Tentativa de revogar o ultimo ADMIN")
    })
    public ResponseEntity<UsuarioResponse> alterarRole(
            @PathVariable Long id,
            @Valid @RequestBody AlterarRoleRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        // Obter id do ADMIN autenticado para auditoria
        // (implementacao depende de como buscarAtual() funciona no UsuarioService)
        var usuarioAtualizado = adminService.alterarRole(id, request.role(), /* adminId */ null);
        return ResponseEntity.ok(usuarioMapper.toResponse(usuarioAtualizado));
    }
}
```

**Nota:** O `@PreAuthorize("hasRole('ADMIN')")` esta na classe, nao nos metodos — todos os endpoints do controller sao restritos a ADMIN.

---

## Regras de Negocio Criticas

1. **Prevencao de lock-out:** Antes de alterar role de qualquer ADMIN, verificar `countByRole("ADMIN")`. Se for 1 e a nova role nao for ADMIN, rejeitar com HTTP 409.
2. **ADMIN pode promover qualquer usuario** (JOGADOR -> MESTRE, JOGADOR -> ADMIN, MESTRE -> ADMIN).
3. **ADMIN pode revogar qualquer usuario** (ADMIN -> MESTRE, ADMIN -> JOGADOR, MESTRE -> JOGADOR) — desde que nao seja o ultimo ADMIN.
4. **Nao ha validacao de "auto-alteracao":** ADMIN pode alterar a propria role, mas se for o ultimo ADMIN, a prevencao de lock-out se aplica.

---

## Criterios de Aceitacao

- [ ] GET /api/v1/admin/usuarios retorna HTTP 200 com lista paginada para ADMIN
- [ ] GET /api/v1/admin/usuarios retorna HTTP 403 para MESTRE ou JOGADOR
- [ ] PUT /api/v1/admin/usuarios/{id}/role altera role com sucesso para ADMIN
- [ ] PUT /api/v1/admin/usuarios/{id}/role retorna HTTP 403 para nao-ADMIN
- [ ] PUT /api/v1/admin/usuarios/{id}/role retorna HTTP 404 para id inexistente
- [ ] PUT /api/v1/admin/usuarios/{id}/role retorna HTTP 409 ao tentar revogar o ultimo ADMIN
- [ ] Apos alterar role, o usuario recebe a nova authority no proximo login OAuth2
