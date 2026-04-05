# T4 — Backend: Atualizar todos os @PreAuthorize para incluir ADMIN

> Spec: 010 | Tipo: Backend | Depende de: T1 | Bloqueia: T9

---

## Objetivo

Atualizar todos os `@PreAuthorize` existentes nos 25 controllers para incluir a role ADMIN. Esta task e puramente textual — nenhuma logica e alterada, apenas as strings de autorizacao.

---

## Escopo

Total de ocorrencias a atualizar: aproximadamente 80 anotacoes em 25 controllers.

### Padroes de substituicao

| Padrao atual | Novo padrao |
|---|---|
| `@PreAuthorize("hasRole('MESTRE')")` | `@PreAuthorize("hasAnyRole('ADMIN', 'MESTRE')")` |
| `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")` | `@PreAuthorize("hasAnyRole('ADMIN', 'MESTRE', 'JOGADOR')")` |
| `@PreAuthorize("isAuthenticated()")` | sem alteracao |

### Controllers afetados

**Controllers principais:**
- `JogoController` — 11 ocorrencias (7x MESTRE, 4x MESTRE+JOGADOR)
- `JogoParticipanteController` — 5 ocorrencias (3x MESTRE, 2x MESTRE+JOGADOR)
- `FichaController` — 20+ ocorrencias (3x MESTRE, 17x MESTRE+JOGADOR)
- `FichaAnotacaoController` — 3 ocorrencias (todas MESTRE+JOGADOR)
- `DashboardController` — 1 ocorrencia (MESTRE)
- `UsuarioController` — sem alteracao (usa isAuthenticated())

**Controllers de configuracao:**
- `AtributoController` — 6 ocorrencias
- `AptidaoController` — 6 ocorrencias
- `BonusController` — 6 ocorrencias
- `ClasseController` — 11 ocorrencias
- `DadoProspeccaoController` — 6 ocorrencias
- `FormulaController` — 2 ocorrencias
- `GeneroController` — 6 ocorrencias
- `IndoleController` — 6 ocorrencias
- `MembroCorpoController` — 6 ocorrencias
- `NivelController` — 5 ocorrencias
- `PresencaController` — 6 ocorrencias
- `PontosVantagemController` — 5 ocorrencias
- `RacaController` — 12 ocorrencias
- `SiglaController` — 1 ocorrencia
- `TipoAptidaoController` — 6 ocorrencias
- `VantagemController` — 9 ocorrencias
- `VantagemEfeitoController` — 3 ocorrencias
- `CategoriaVantagemController` — 5 ocorrencias

---

## Passos

### 1. Abordagem recomendada: substituicao global com grep/sed

**Verificar antes de executar:**
```bash
# Contar ocorrencias do padrao 1
grep -r "hasRole('MESTRE')" src/main/java --include="*.java" | wc -l

# Contar ocorrencias do padrao 2
grep -r "hasAnyRole('MESTRE', 'JOGADOR')" src/main/java --include="*.java" | wc -l
```

**Substituicao:**
```bash
# Padrao 1: hasRole('MESTRE') -> hasAnyRole('ADMIN', 'MESTRE')
find src/main/java -name "*.java" -exec sed -i '' \
  "s/hasRole('MESTRE')/hasAnyRole('ADMIN', 'MESTRE')/g" {} +

# Padrao 2: hasAnyRole('MESTRE', 'JOGADOR') -> hasAnyRole('ADMIN', 'MESTRE', 'JOGADOR')
find src/main/java -name "*.java" -exec sed -i '' \
  "s/hasAnyRole('MESTRE', 'JOGADOR')/hasAnyRole('ADMIN', 'MESTRE', 'JOGADOR')/g" {} +
```

**Verificar resultado:**
```bash
# Deve retornar zero ocorrencias apos substituicao
grep -r "hasRole('MESTRE')" src/main/java --include="*.java"
grep -r "hasAnyRole('MESTRE', 'JOGADOR')" src/main/java --include="*.java"
```

### 2. Excecoes — verificar manualmente

Os seguintes arquivos NAO devem ser alterados:
- `AdminController.java` — usa `@PreAuthorize("hasRole('ADMIN')")` (a ser criado em T3)
- `UsuarioController.java` — usa `isAuthenticated()`
- Arquivos de teste — os testes de seguranca existentes terao que ser atualizados separadamente (coberto em T9)

### 3. Atualizar anotacoes Swagger

Onde o `@Operation(description = "...")` menciona "Apenas MESTRE", atualizar para "Apenas MESTRE ou ADMIN".

### 4. Atualizar `ParticipanteSecurityService` — bypass ADMIN

**Decisao do PO (2026-04-03):** ADMIN pode tudo — bypass total de `canAccessJogo()`.

Em `ParticipanteSecurityService` (ou equivalente), nos metodos `assertMestreDoJogo()` e `assertCanAccessJogo()`, adicionar verificacao inicial:

```java
// No inicio de cada metodo de verificacao de acesso:
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
    return; // ADMIN bypassa todas as verificacoes de participacao
}
// ... resto da logica normal
```

O foco do ADMIN e gerenciar usuarios, mas tecnicamente tem acesso irrestrito a todos os recursos.

---

## Verificacao Pos-Substituicao

```bash
# Compilar para garantir que nenhuma substituicao gerou syntax error
./mvnw compile

# Rodar testes (podem falhar em testes de seguranca que esperam 403 para MESTRE)
./mvnw test
```

**Atencao:** Testes de integracao que testam que um usuario com role MESTRE recebe HTTP 403 em algum endpoint estao errados apos esta spec — o padrao correto agora e que ADMIN tambem recebe 2xx. Os testes existentes nao devem ser reescritos nesta task — isso e responsabilidade de T9.

---

## Criterios de Aceitacao

- [ ] Zero ocorrencias de `hasRole('MESTRE')` em controllers (exceto AdminController)
- [ ] Zero ocorrencias de `hasAnyRole('MESTRE', 'JOGADOR')` em controllers
- [ ] `./mvnw compile` sem erros
- [ ] Nenhuma anotacao `isAuthenticated()` foi alterada
- [ ] CLAUDE.md atualizado com novos padroes de @PreAuthorize (ver nota abaixo)

**Nota:** Ao concluir T4, tambem atualizar a secao "Security" do `CLAUDE.md` para documentar os novos padroes. Isso garante que specs futuras ja usem o padrao correto.
