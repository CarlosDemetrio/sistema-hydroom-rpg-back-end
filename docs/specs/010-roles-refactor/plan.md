# Plano de Implementacao — Spec 010: Roles ADMIN / MESTRE / JOGADOR

> Status: Aguardando inicio
> Prioridade: Alta — afeta seguranca de todos os endpoints existentes
> Estimativa total: 8 tasks (4 backend, 1 banco, 3 frontend)

---

## Estrategia Geral

### Principio central: zero-downtime, zero breaking changes

A migracao deve ser completamente retrocompativel:
- Usuarios existentes com `role = MESTRE` continuam funcionando sem nenhuma alteracao
- Usuarios existentes com `role = JOGADOR` continuam funcionando sem nenhuma alteracao
- Apenas usuarios novos (criados apos esta spec) recebem `role = null` e passam pelo onboarding
- A adicao de ADMIN nos `@PreAuthorize` e puramente aditiva — nao remove nenhuma permissao existente

### Sequencia de entrega

```
T1 (banco)     → T2 (backend model/enum)  → T3 (backend endpoints)
                                          → T4 (backend @PreAuthorize)
                                                    |
                                          T5 (seed SQL primeiro ADMIN)
                                                    |
                                          T6 (frontend guards)
                                                    |
                                          T7 (frontend onboarding)
                                                    |
                                          T8 (frontend admin)
                                                    |
                                          T9 (testes de integracao)
```

**T2 e T3 podem ser desenvolvidos em paralelo com T4**, pois T4 (atualizar @PreAuthorize) nao tem dependencia funcional com os novos endpoints — e apenas uma atualizacao de strings.

---

## Decisoes de Design

### Como tratar `role = null` no CustomOAuth2UserService

**Situacao atual:** `CustomOAuth2UserService.criarNovoUsuario()` nao define `role`, mas o default do `@Builder.Default` no `Usuario` e `"JOGADOR"`. Isso significa que todos os usuarios novos hoje recebem JOGADOR automaticamente.

**Mudanca necessaria:** Remover o `@Builder.Default` de `"JOGADOR"` e deixar `role = null`. O `CustomOAuth2UserService` nao deve adicionar `ROLE_JOGADOR` como authority se o usuario nao tiver role definida. O sistema deve retornar HTTP 403 com mensagem orientando o onboarding quando usuario sem role acessa endpoint de negocio.

**Compatibilidade retroativa:** Usuarios existentes ja tem `role = MESTRE` ou `role = JOGADOR` no banco — o valor null so aparece para usuarios criados apos esta mudanca.

### Como adicionar ADMIN ao CustomOAuth2UserService

O `CustomOAuth2UserService` ja le `usuario.getRole()` e adiciona como `SimpleGrantedAuthority("ROLE_" + usuario.getRole())`. Basta que o banco tenha `role = ADMIN` para que a authority `ROLE_ADMIN` seja automaticamente concedida no login. Nenhuma mudanca logica necessaria no service — apenas o enum e o seed.

### Como garantir que ADMIN acessa endpoints de MESTRE e JOGADOR

A abordagem mais simples e mais segura e atualizar os `@PreAuthorize` para incluir `ADMIN`:
- `hasRole('MESTRE')` -> `hasAnyRole('ADMIN', 'MESTRE')`
- `hasAnyRole('MESTRE', 'JOGADOR')` -> `hasAnyRole('ADMIN', 'MESTRE', 'JOGADOR')`

**Alternativa descartada: hierarquia de roles via RoleHierarchy do Spring Security.** Embora tecnicamente possivel, a hierarquia implicita e mais dificil de auditar e pode causar comportamentos surpresa. Preferir explicito sobre implicito.

### Bypass de ParticipanteSecurityService para ADMIN

**Decisao pendente do PO (ver P-03 na spec).** Enquanto nao definido, a implementacao deve:
1. Adicionar metodo `isAdmin(Long usuarioId)` no `ParticipanteSecurityService` ou em um `SecurityHelper`
2. Nos services que verificam `canAccessJogo()`, verificar primeiro se e ADMIN antes de checar participacao
3. Documentar este comportamento explicitamente no Swagger (ADMIN bypassa verificacao de participante)

**Exemplo de implementacao sugerida:**
```java
// Em ParticipanteSecurityService
public boolean canAccessJogo(Long jogoId, Long usuarioId) {
    if (isAdmin(usuarioId)) return true; // ADMIN bypassa
    return isMestreDoJogo(jogoId, usuarioId) || isParticipanteAprovado(jogoId, usuarioId);
}

private boolean isAdmin(Long usuarioId) {
    return usuarioRepository.findById(usuarioId)
        .map(u -> "ADMIN".equals(u.getRole()))
        .orElse(false);
}
```

### Modelo de dados — nenhuma migracao de schema necessaria

O campo `role` na tabela `usuarios` ja e `VARCHAR(20)`. O valor `ADMIN` cabe sem alteracao de schema. A unica migracao necessaria e o seed do primeiro admin (T5).

---

## Riscos e Mitigacoes

| Risco | Probabilidade | Impacto | Mitigacao |
|-------|--------------|---------|-----------|
| Usuarios existentes perdem acesso | Baixa | Alto | T4 e puramente aditivo; nenhuma role existente e removida |
| ADMIN faz bypass indevido em servicos que nao esperavam | Media | Medio | Auditar todos os services com `assertMestreDoJogo()` antes de implementar bypass |
| Lock-out: ultimo ADMIN revoga propria role | Media | Alto | Validacao no service: count de ADMINs ativos antes de revogar |
| Frontend manda usuario para onboarding em loop | Baixa | Medio | Guard deve verificar `role != null` apos POST /me/role retornar 200 |
| Seed SQL sobrescreve role de usuario existente | Baixa | Baixo | Usar `ON CONFLICT (email) DO UPDATE SET role = 'ADMIN'` — intencional |

---

## Compatibilidade com Specs Futuras

Esta spec e transversal. Todas as specs futuras (011, 012, etc.) devem usar os novos padroes de `@PreAuthorize` com ADMIN incluido. O documento `CLAUDE.md` deve ser atualizado para refletir os novos padroes.

Padroes a atualizar no `CLAUDE.md`:
- `@PreAuthorize("hasRole('MESTRE')")` — agora e `hasAnyRole('ADMIN', 'MESTRE')`
- `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")` — agora e `hasAnyRole('ADMIN', 'MESTRE', 'JOGADOR')`
- Secao "Security" deve documentar as tres roles e seus escopos
