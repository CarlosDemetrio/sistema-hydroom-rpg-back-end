# Tasks — Spec 010: Refatoracao de Roles ADMIN / MESTRE / JOGADOR

> Total: 9 tasks | Backend: 5 | Frontend: 3 | Testes: 1

## Sequencia de Execucao

```
T1 → T2 → T3 → T4 (pode ser paralelo com T2/T3)
              → T5 (pode ser paralelo com T2/T3)
T6 (requer T2/T3)
T7 (requer T6)
T8 (requer T6)
T9 (requer T1-T8)
```

## Indice

| Task | Titulo | Tipo | Depende de | Status |
|------|--------|------|------------|--------|
| [T1](T1-backend-model-enum-admin.md) | Adicionar ADMIN ao model Usuario e enum | Backend | — | Pendente |
| [T2](T2-backend-endpoint-onboarding-role.md) | Endpoint POST /me/role — onboarding | Backend | T1 | Pendente |
| [T3](T3-backend-admin-controller.md) | Endpoints admin: listar usuarios e alterar role | Backend | T1 | Pendente |
| [T4](T4-backend-preauthorize-admin.md) | Atualizar todos os @PreAuthorize para incluir ADMIN | Backend | T1 | Pendente |
| [T5](T5-seed-primeiro-admin.md) | Seed SQL do primeiro ADMIN | Banco | T1 | Pendente |
| [T6](T6-frontend-guards-onboarding-redirect.md) | Frontend: guards e redirect para onboarding | Frontend | T2 | Pendente |
| [T7](T7-frontend-wizard-onboarding.md) | Frontend: wizard de onboarding | Frontend | T6 | Pendente |
| [T8](T8-frontend-admin-screen.md) | Frontend: tela de administracao de usuarios | Frontend | T6 | Pendente |
| [T9](T9-testes-integracao-admin.md) | Testes de integracao para ADMIN em endpoints criticos | Teste | T1-T5 | Pendente |

## Criterio de Conclusao da Spec 010

- [ ] Todos os 9 tasks com status Concluido
- [ ] Build backend sem erros (`./mvnw package -DskipTests`)
- [ ] Build frontend sem erros e warnings (`ng build`)
- [ ] Testes passando: `./mvnw test` (todos os testes existentes + novos de T9)
- [ ] ADMIN provisionado via seed consegue logar e acessar todos os endpoints
- [ ] Usuario novo sem role e redirecionado para onboarding e nao consegue acessar endpoints de negocio
- [ ] CLAUDE.md atualizado com novos padroes de @PreAuthorize
