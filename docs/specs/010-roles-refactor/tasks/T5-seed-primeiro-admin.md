# T5 — Banco: Seed SQL do primeiro ADMIN

> Spec: 010 | Tipo: Banco de dados | Depende de: T1 | Bloqueia: T9

---

## Objetivo

Provisionar o primeiro usuario ADMIN via seed SQL, permitindo o bootstrap inicial do sistema sem acesso direto ao banco. O email do ADMIN deve ser configuravel via application properties.

---

## Arquivos Afetados

| Arquivo | Acao |
|---------|------|
| `src/main/resources/data-dev.sql` | Adicionar seed do ADMIN (ambiente dev, comentado por padrao) |
| `src/main/resources/db/migration/` | Criar script Flyway se o projeto usar Flyway em prod (verificar) |
| `src/main/resources/application.properties` | Adicionar property `app.admin.email` |
| `service/CustomOAuth2UserService.java` | Verificar logica de merge providerId (ja existe, deve funcionar) |

---

## Passos

### 1. Verificar se Flyway esta em uso

```bash
grep -r "flyway" src/main/resources --include="*.properties" --include="*.yml"
find src/main/resources -name "*.sql" -path "*/migration/*"
```

O `CLAUDE.md` indica que testes usam `ddl-auto=create-drop` sem Flyway. Verificar se producao usa Flyway ou tambem e create-drop/update.

**Se usar Flyway:** Criar `V010__seed_admin.sql` na pasta de migrations.
**Se nao usar Flyway:** Adicionar ao `data-dev.sql` com instrucoes de como executar em producao.

### 2. SQL do seed ADMIN

```sql
-- ========================================
-- SEED: PRIMEIRO ADMIN DO SISTEMA
-- ========================================
-- Executar apenas na instalacao inicial ou para provisionar novo ADMIN.
-- O providerId sera preenchido automaticamente no primeiro login OAuth2.
-- ========================================

INSERT INTO usuarios (nome, email, provider, provider_id, role, created_at, updated_at)
VALUES (
    'Administrador',                    -- nome inicial (sera atualizado no login OAuth2)
    'SEU_EMAIL_ADMIN@gmail.com',        -- substituir pelo email real
    'GOOGLE',
    null,                               -- sera preenchido no primeiro login OAuth2
    'ADMIN',
    NOW(),
    NOW()
)
ON CONFLICT (email) DO UPDATE SET role = 'ADMIN', updated_at = NOW();
```

**Por que `ON CONFLICT DO UPDATE`:** Se o usuario ja existia como JOGADOR ou MESTRE (criado antes desta spec), ele deve ser promovido a ADMIN sem duplicar o registro.

**Por que `provider_id = null`:** O providerId e preenchido automaticamente pelo `CustomOAuth2UserService` no primeiro login. O codigo ja trata este caso:
```java
// Ja existente no CustomOAuth2UserService:
if (usuario.getProviderId() == null || !usuario.getProviderId().equals(providerId)) {
    usuario.setProviderId(providerId);
    // ...
}
```

### 3. Adicionar property em `application.properties`

```properties
# Email do primeiro administrador do sistema (usado no seed SQL)
# Deve ser o mesmo email da conta Google que fara o primeiro login como ADMIN
app.admin.email=admin@seudominio.com
```

**Nota:** Esta property e apenas documentacional — o seed SQL usa o valor hardcoded. Se desejar um seed dinamico, criar um `ApplicationRunner` ou `CommandLineRunner` que leia a property e insira o usuario programaticamente.

### 4. Alternativa: ApplicationRunner para seed dinamico

Para evitar SQL hardcoded, criar um bean que executa no startup:

```java
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank()) return;

        usuarioRepository.findByEmail(adminEmail).ifPresentOrElse(
            usuario -> {
                if (!"ADMIN".equals(usuario.getRole())) {
                    usuario.setRole("ADMIN");
                    usuarioRepository.save(usuario);
                    log.info("Usuario {} promovido a ADMIN", adminEmail);
                }
            },
            () -> {
                Usuario admin = Usuario.builder()
                    .nome("Administrador")
                    .email(adminEmail)
                    .provider("GOOGLE")
                    .role("ADMIN")
                    .build();
                usuarioRepository.save(admin);
                log.info("ADMIN criado: {}", adminEmail);
            }
        );
    }
}
```

**Vantagem do ApplicationRunner:** Sem SQL manual, funciona em qualquer ambiente, loga o que fez.

---

## Criterios de Aceitacao

- [ ] Seed SQL documentado e funcional (ou ApplicationRunner implementado)
- [ ] Apos executar o seed, usuario com email configurado faz login via OAuth2 e recebe role ADMIN
- [ ] Seed e idempotente (executar duas vezes nao duplica usuario)
- [ ] Usuario que ja existia como JOGADOR e promovido a ADMIN pelo seed
- [ ] `CustomOAuth2UserService` preenche providerId corretamente no primeiro login do ADMIN seedado
