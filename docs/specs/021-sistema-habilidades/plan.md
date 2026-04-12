# Spec 021 — Plano de Implementacao: Sistema de Habilidades (HabilidadeConfig)

> Spec: `021-sistema-habilidades`
> Status: Pendente
> Dependencias: Spec 001 (Jogo entity — implementado)
> Estimativa total: ~5–7 horas (Backend 2–3h + Frontend 3–4h)

---

## 1. Visao Geral das Fases

```
Fase 1 — Backend (T1)
  HabilidadeConfig entity + Repository + Service + DTOs + Mapper + Controller + Testes

Fase 2 — Frontend (T2) — depende de T1 estar completo e deployado/disponivel
  Tela CRUD no painel de configuracoes + Signal Store + Testes Vitest
```

---

## 2. Estado Atual

- Nenhuma implementacao existe para `HabilidadeConfig`.
- O diretorio `docs/specs/021-sistema-habilidades/` e o unico artefato existente.
- A entidade nao aparece em nenhuma outra spec como dependencia.

---

## 3. Fase 1 — Backend

### T1 — HabilidadeConfig CRUD Completo

**Escopo:**
1. Entity `HabilidadeConfig` extends `BaseEntity` implements `ConfiguracaoEntity`
2. `HabilidadeConfigRepository` extends `JpaRepository`
3. `HabilidadeConfigService` extends `AbstractConfiguracaoService`
4. DTOs: `CreateHabilidadeConfigDTO`, `UpdateHabilidadeConfigDTO`, `HabilidadeConfigResponseDTO`
5. `HabilidadeConfigMapper` (MapStruct)
6. `HabilidadeConfigController` — seguir padrao de `AtributoController`
7. `HabilidadeConfigServiceIntegrationTest` extends `BaseConfiguracaoServiceIntegrationTest`

**Permissoes especiais (diferente do padrao):**
- Todos os endpoints — GET, POST, PUT, DELETE — usam `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")`
- Nenhum endpoint e exclusivo de MESTRE nesta entidade

**Nota sobre schema:** O projeto usa `spring.jpa.hibernate.ddl-auto=update`. Nao ha Flyway. A tabela `habilidade_config` sera criada automaticamente pelo Hibernate ao subir a aplicacao. Para producao, o `ddl-auto=update` tambem aplica as mudancas automaticamente.

**Estimativa:** 2–3 horas

---

## 4. Fase 2 — Frontend

### T2 — Tela CRUD HabilidadeConfig

**Escopo:**
1. Service HTTP `HabilidadeConfigService` com metodos para todos os 5 endpoints
2. Signal Store `HabilidadeConfigStore` com estado reativo (loading, error, lista)
3. Componente `HabilidadeConfigListComponent` — tabela com nome, dano/efeito, acoes
4. Formulario modal/inline: nome (obrigatorio), descricao (textarea opcional), dano/efeito (texto livre opcional)
5. Roteamento: integrar ao painel de configuracoes existente
6. Testes Vitest para Service e Store

**Consideracao de UX — acesso do Jogador:**
- Habilidades sao por jogo (`jogo_id`), nao por ficha. Sao configuracoes globais do jogo.
- Como JOGADOR pode criar/editar/deletar, a tela deve ser acessivel tanto no contexto do Mestre quanto do Jogador.
- Verificar com UX Architect onde a tela aparece para cada role (PA-021-03 em aberto).

**Estimativa:** 3–4 horas

---

## 5. Grafo de Dependencias

```
T1 (Backend — entity, CRUD, testes)
  └── T2 (Frontend — tela, store, testes)
```

Nenhuma dependencia externa com outras specs.

---

## 6. Criterios de Pronto (Definition of Done)

### T1 — Backend
- [ ] Entity com todos os campos mapeados, `@SQLRestriction`, unique constraint `(jogo_id, nome)`
- [ ] Repository com `findByJogoIdOrderByOrdemExibicao(Long jogoId)`
- [ ] Service: `atualizarCampos()` implementado; `validarAntesCriar()` valida nome duplicado (HTTP 409)
- [ ] Controller com `@PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")` em todos os metodos
- [ ] Todos os 5 endpoints testados e funcionais
- [ ] `BaseConfiguracaoServiceIntegrationTest` passando (~10 testes automaticos)
- [ ] Testes adicionais: nome duplicado, campos opcionais null, soft delete
- [ ] `./mvnw test` 100% verde

### T2 — Frontend
- [ ] Service HTTP com todos os 5 metodos
- [ ] Signal Store com estado loading/error/data
- [ ] Tabela exibe nome e danoEfeito (truncado se longo)
- [ ] Formulario valida nome obrigatorio no client-side
- [ ] Tela acessivel para roles MESTRE e JOGADOR
- [ ] Testes Vitest passando

---

*Produzido por: Business Analyst/PO | 2026-04-12*
