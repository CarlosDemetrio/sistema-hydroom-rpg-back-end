# T9 — Testes de integracao para ADMIN em endpoints criticos

> Spec: 010 | Tipo: Teste | Depende de: T1, T2, T3, T4, T5 | Bloqueia: nenhuma

---

## Objetivo

Verificar que a role ADMIN tem acesso correto a todos os endpoints criticos, que usuarios sem role recebem HTTP 403 onde esperado, e que o onboarding funciona corretamente de ponta a ponta.

---

## Estrategia de Testes

Prioridade: **testes de integracao** (80%) com H2 in-memory. Seguir o padrao `@ActiveProfiles("test")` + `@Transactional` + `@BeforeEach` com limpeza.

Os testes devem cobrir tres dimensoes:
1. **ADMIN acessa o que MESTRE pode** — testa que ADMIN nao recebe 403 em endpoints de escrita
2. **ADMIN acessa o que JOGADOR pode** — testa que ADMIN nao recebe 403 em endpoints de leitura
3. **Usuario sem role nao acessa nada** — testa que role=null recebe 403 em endpoints de negocio
4. **Endpoints de admin funcionam** — testa `/admin/usuarios` e `/admin/usuarios/{id}/role`
5. **Onboarding funciona** — testa `POST /me/role` em todos os cenarios

---

## Testes por Classe

### `AdminEndpointAccessIntegrationTest`

Verifica que ADMIN acessa endpoints historicamente de MESTRE:

```java
@Test
@DisplayName("ADMIN deve acessar POST /api/v1/jogos (endpoint de MESTRE)")
void adminDeveAcessarPostJogos() { ... }

@Test
@DisplayName("ADMIN deve acessar POST /api/v1/jogos/{id}/atributos (endpoint de MESTRE)")
void adminDeveAcessarPostAtributos() { ... }

@Test
@DisplayName("ADMIN deve acessar DELETE /api/v1/jogos/{id} (endpoint de MESTRE)")
void adminDeveAcessarDeleteJogo() { ... }

@Test
@DisplayName("ADMIN deve acessar GET /api/v1/jogos/{id}/fichas (endpoint de MESTRE+JOGADOR)")
void adminDeveAcessarGetFichas() { ... }
```

### `AdminControllerIntegrationTest`

```java
@Test
@DisplayName("ADMIN deve listar usuarios com paginacao")
void adminDeveListarUsuarios() { ... }

@Test
@DisplayName("MESTRE nao deve acessar GET /admin/usuarios")
void mestreNaoDeveListarUsuarios() { ... }

@Test
@DisplayName("ADMIN deve alterar role de usuario")
void adminDeveAlterarRole() { ... }

@Test
@DisplayName("ADMIN nao deve revogar ultimo ADMIN")
void adminNaoDeveRevogarUltimoAdmin() { ... }
```

### `OnboardingIntegrationTest`

```java
@Test
@DisplayName("Usuario sem role deve definir como JOGADOR com sucesso")
void usuarioSemRoleDeveDefinirJogador() { ... }

@Test
@DisplayName("Usuario sem role deve definir como MESTRE com sucesso")
void usuarioSemRoleDeveDefinirMestre() { ... }

@Test
@DisplayName("Usuario sem role nao deve autodefinir ADMIN")
void usuarioNaoDeveAutodefinirAdmin() { ... }

@Test
@DisplayName("Usuario com role ja definida nao deve redefinir via onboarding")
void usuarioComRoleNaoDeveRedefinir() { ... }

@Test
@DisplayName("Usuario sem role deve receber 403 ao acessar endpoint de negocio")
void usuarioSemRoleNaoDeveAcessarEndpointsNegocio() { ... }
```

---

## Setup do Usuario de Teste ADMIN

Para criar um usuario ADMIN nos testes:

```java
// Em um metodo auxiliar da classe base de teste ou em um TestUtils
protected Usuario criarUsuarioAdmin() {
    return usuarioRepository.save(
        Usuario.builder()
            .nome("Admin Teste")
            .email("admin@teste.com")
            .provider("GOOGLE")
            .providerId("google-admin-123")
            .role("ADMIN")
            .build()
    );
}

protected Usuario criarUsuarioSemRole() {
    return usuarioRepository.save(
        Usuario.builder()
            .nome("Novo Usuario")
            .email("novo@teste.com")
            .provider("GOOGLE")
            .providerId("google-novo-456")
            .role(null)
            .build()
    );
}
```

---

## Cenarios de Regressao

Verificar que os testes existentes continuam passando apos T4 (atualizacao dos @PreAuthorize).

Testes que podem precisar de atualizacao:
- Qualquer teste que verificava que um usuario com role MESTRE recebe HTTP 2xx em endpoint de escrita — esses testes continuam validos
- Qualquer teste que verificava que apenas MESTRE pode acessar um endpoint especifico — precisam ser atualizados para aceitar ADMIN tambem

---

## Criterios de Aceitacao

- [ ] `AdminEndpointAccessIntegrationTest`: ADMIN acessa endpoints de MESTRE e JOGADOR sem 403
- [ ] `AdminControllerIntegrationTest`: CRUD de admin funciona, lock-out previne remocao do ultimo ADMIN
- [ ] `OnboardingIntegrationTest`: todos os 5 cenarios passando
- [ ] Testes existentes: zero regressao (todos continuam passando)
- [ ] `./mvnw test` retorna BUILD SUCCESS com todos os testes (incluindo os novos)
