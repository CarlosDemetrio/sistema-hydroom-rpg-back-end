# T2 — Backend: Teste de Integracao 401 vs 302

> Fase: Backend | Prioridade: P0 (BLOQUEANTE PRE-RC)
> Dependencias: T1
> Bloqueia: nenhuma
> Estimativa: 1h
> Agente sugerido: senior-backend-dev

---

## Contexto

Adicionar teste de integracao Spring MVC para garantir que o `HttpStatusEntryPoint` instalado em T1 funciona corretamente. O teste serve como **regression guard** para evitar que o bug volte se alguem alterar `SecurityConfig` no futuro.

---

## Arquivos Envolvidos

| Arquivo | Mudanca |
|---------|---------|
| `src/test/java/br/com/hydroom/rpg/fichacontrolador/config/SecurityConfigIntegrationTest.java` | CRIAR (novo) |

---

## Passos Sugeridos

### Passo 1 — Criar a classe de teste

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/jogos sem sessao deve retornar 401, nao 302")
    void deveRetornar401EmApiSemSessao() throws Exception {
        mockMvc.perform(get("/api/v1/jogos"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/jogos sem sessao deve retornar 401, nao 302")
    void deveRetornar401EmApiPostSemSessao() throws Exception {
        mockMvc.perform(post("/api/v1/jogos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/auth/me sem sessao deve retornar 401")
    void deveRetornar401EmAuthMeSemSessao() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /oauth2/authorization/google deve retornar 302 (fluxo OAuth2 intacto)")
    void deveManter302EmOauth2() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
            .andExpect(status().is3xxRedirection());
    }
}
```

### Passo 2 — Rodar os testes

```
./mvnw test -Dtest=SecurityConfigIntegrationTest
```

Esperado: 4 testes passando.

### Passo 3 — Suite completa

```
./mvnw test
```

Esperado: 614 testes (613 anteriores + 1 grupo novo = 4 novos), 0 falhas.

---

## Criterios de Aceite

- [ ] Arquivo `SecurityConfigIntegrationTest.java` criado em `src/test/java/.../config/`
- [ ] 4 testes definidos cobrindo: GET `/api/**`, POST `/api/**`, GET `/api/v1/auth/me`, GET `/oauth2/authorization/google`
- [ ] `./mvnw test -Dtest=SecurityConfigIntegrationTest` passa (4/4)
- [ ] `./mvnw test` passa com >= 617 testes (613 + 4 novos)
- [ ] Zero falhas

---

## Notas

- Importar `MockMvcRequestBuilders.get`, `MockMvcRequestBuilders.post`, `MockMvcResultMatchers.status`
- Sem precisar de `@WithMockUser` (estamos justamente testando o caso SEM autenticacao)
- Se o teste OAuth2 redirect falhar com 401 em vez de 302, significa que a configuracao em T1 esta capturando rotas demais — revisar o `AntPathRequestMatcher("/api/**")`

---

## Referencias

- `docs/auditoria/AUDITORIA-ROTAS-ERROS-2026-04-07.md` § Tasks Sugeridas T9 (linha 202-203)
- T1 (deve estar concluida antes)
