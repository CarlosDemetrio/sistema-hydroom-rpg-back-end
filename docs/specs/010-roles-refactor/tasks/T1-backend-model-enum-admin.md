# T1 — Backend: Adicionar ADMIN ao model Usuario e enum

> Spec: 010 | Tipo: Backend | Depende de: nenhuma | Bloqueia: T2, T3, T4, T5

---

## Objetivo

Suporte ao valor `ADMIN` na entidade `Usuario` e garantir que usuarios novos recebam `role = null` ate completarem o onboarding. Nenhuma mudanca de schema SQL necessaria — o campo `role VARCHAR(20)` ja suporta o novo valor.

---

## Arquivos Afetados

| Arquivo | Acao |
|---------|------|
| `model/Usuario.java` | Remover `@Builder.Default` de `"JOGADOR"` no campo `role`; adicionar validacao de enum |
| `service/CustomOAuth2UserService.java` | Nao adicionar authority se `role = null`; adicionar ROLE_ADMIN quando aplicavel |
| `constants/ValidationMessages.java` | Adicionar mensagem para role nao definida (se nao existir) |
| `exception/` | Verificar se existe `ForbiddenException` adequada para "complete o onboarding" |

---

## Passos

### 1. Atualizar `Usuario.java`

Remover o `@Builder.Default` do campo `role`:
```java
// ANTES:
@Builder.Default
@Column(nullable = false, length = 20)
private String role = "JOGADOR";

// DEPOIS:
@Column(length = 20)
private String role; // null = onboarding pendente, valores: JOGADOR, MESTRE, ADMIN
```

Adicionar metodo helper:
```java
public boolean hasRole() {
    return role != null;
}

public boolean isAdmin() {
    return "ADMIN".equals(role);
}
```

### 2. Atualizar `CustomOAuth2UserService.java`

Na construcao das authorities, verificar se role e null:
```java
// Adicionar role do banco de dados como authority APENAS se definida
if (usuario.getRole() != null) {
    authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRole()));
}
```

**Efeito:** Usuario sem role nao recebe nenhuma authority de role. O Spring Security ainda o autentica, mas `hasRole('JOGADOR')`, `hasRole('MESTRE')` e `hasRole('ADMIN')` retornam false. Apenas `isAuthenticated()` retorna true.

### 3. Verificar compatibilidade retroativa

Usuarios existentes com `role = MESTRE` ou `role = JOGADOR` no banco continuam recebendo suas authorities normalmente — o `if (role != null)` so filtra o caso null.

---

## Testes

- Nenhum teste de integracao novo nesta task — T9 cobre os cenarios de autorizacao
- Verificar que build compila: `./mvnw package -DskipTests`
- Verificar que testes existentes continuam passando: `./mvnw test`

---

## Criterios de Aceitacao

- [ ] Campo `role` em `Usuario` aceita null sem constraint violation
- [ ] `CustomOAuth2UserService` nao adiciona authority para usuarios com role null
- [ ] Usuarios existentes com MESTRE ou JOGADOR nao sao afetados
- [ ] Build compila sem erros
- [ ] Testes existentes passam (zero regressao)
