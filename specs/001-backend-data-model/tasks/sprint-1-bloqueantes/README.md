# 🔴 SPRINT 1: Bloqueantes

**Duração**: 2 dias (12 horas)  
**Status**: ❌ Não Iniciado  
**Prioridade**: P0 - CRÍTICO

---

## 🎯 OBJETIVO DO SPRINT

Corrigir todos os problemas **CRÍTICOS** que impedem o avanço do projeto:
1. Migrations Flyway duplicadas/conflitantes
2. Campos JSON em configurações (viola requisito)
3. Auditoria Envers desnecessária

---

## 📋 TASKS

| # | Task | Estimativa | Status |
|---|------|------------|--------|
| #001 | Reorganizar Migrations Flyway | 2h | ❌ |
| #002 | Remover JSON de ClassePersonagem | 3h | ❌ |
| #003 | Remover JSON de Raca | 2h | ❌ |
| #004 | Remover JSON de VantagemConfig | 3h | ❌ |
| #005 | Remover Hibernate Envers | 1h | ❌ |
| **TOTAL** | | **11h** | **0%** |

---

## 🚀 ORDEM DE EXECUÇÃO

### Dia 1 (6h)
```
08:00 - 10:00  →  TASK-001 (Migrations)          [2h]
10:00 - 10:15  →  Break
10:15 - 13:15  →  TASK-002 (JSON Classe)         [3h]
13:15 - 14:00  →  Almoço
14:00 - 16:00  →  TASK-003 (JSON Raca)           [2h]
```

### Dia 2 (5h)
```
08:00 - 11:00  →  TASK-004 (JSON Vantagem)       [3h]
11:00 - 11:15  →  Break
11:15 - 12:15  →  TASK-005 (Remover Envers)      [1h]
12:15 - 13:00  →  Validação Final
```

---

## ✅ DEFINITION OF DONE - SPRINT

### Técnico
- [ ] Build passa sem erros
- [ ] Build passa sem warnings
- [ ] Todos os testes passando (100%)
- [ ] Flyway migrate SUCCESS
- [ ] Zero campos JSON no código
- [ ] Zero `@Audited` no código
- [ ] H2 console acessível
- [ ] Todas tabelas criadas corretamente

### Documentação
- [ ] Cada task 100% completa
- [ ] INDEX.md atualizado
- [ ] GAPS_E_ISSUES.md atualizado
- [ ] Commits organizados

### Validação
```bash
# Deve passar 100%
./mvnw clean compile      # Build
./mvnw test               # Testes
./mvnw flyway:info        # Migrations
./mvnw flyway:validate    # Validar schemas
```

---

## 📊 PROGRESSO

### Por Task
```
TASK-001:  ⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜  0/10  (0%)
TASK-002:  ⬜⬜⬜⬜⬜⬜⬜⬜⬜   0/9   (0%)
TASK-003:  ⬜⬜⬜⬜⬜⬜         0/6   (0%)
TASK-004:  ⬜⬜⬜⬜⬜⬜⬜       0/7   (0%)
TASK-005:  ⬜⬜⬜⬜⬜⬜⬜⬜     0/8   (0%)
──────────────────────────────────
TOTAL:     ⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜  0/40  (0%)
```

### Métricas
```
Horas Gastas:      0/11
Tasks Completas:   0/5
Testes Passando:   24/24 (100%)  ← Manter!
Build Status:      ✅ (Manter)
Warnings:          3 (JSON fields) → 0
```

---

## 🎁 ENTREGÁVEIS

### Ao final do Sprint 1, teremos:

#### Migrations
- ✅ 45+ migrations organizadas com timestamp
- ✅ Ordem de execução garantida
- ✅ Sem conflitos de versionamento
- ✅ Flyway funcional

#### Modelo de Dados
- ✅ Zero campos JSON
- ✅ Tudo em tabelas relacionadas
- ✅ Totalmente configurável
- ✅ Consultável por SQL

#### Código
- ✅ Sem Hibernate Envers
- ✅ Auditoria simples (timestamps)
- ✅ Código limpo
- ✅ Performance melhorada

#### Qualidade
- ✅ Build sem warnings
- ✅ Testes 100% passando
- ✅ Documentação atualizada

---

## 🚨 RISCOS E MITIGAÇÕES

### Risco 1: Migrations complexas
**Impacto**: Alto  
**Probabilidade**: Média  
**Mitigação**: 
- Fazer backup antes
- Testar com Flyway clean + migrate
- H2 permite reset rápido

### Risco 2: Testes quebrarem
**Impacto**: Alto  
**Probabilidade**: Alta  
**Mitigação**:
- Rodar testes após cada task
- Corrigir imediatamente
- Manter testes atualizados

### Risco 3: Performance ao remover JSON
**Impacto**: Baixo  
**Probabilidade**: Baixa  
**Mitigação**:
- Usar LAZY loading
- Índices nas FKs
- Queries otimizadas

---

## 📝 DEPENDÊNCIAS

### Pré-requisitos
- ✅ Java 25 instalado
- ✅ Maven configurado
- ✅ H2 funcionando
- ✅ Git configurado

### Não pode começar sem
- ✅ Backup do código atual
- ✅ Testes passando
- ✅ Build funcionando

---

## 🔄 PRÓXIMO SPRINT

Após completar Sprint 1:
→ **Sprint 2**: Entidades Faltantes (1 dia)
  - FichaPersonalidade
  - FichaBonusAdicional

---

## 📚 REFERÊNCIAS

- [TASK-001](./TASK-001-migrations.md) - Migrations
- [TASK-002](./TASK-002-remove-json-classe.md) - Classe
- [TASK-003](./TASK-003-remove-json-raca.md) - Raça
- [TASK-004](./TASK-004-remove-json-vantagem.md) - Vantagem
- [TASK-005](./TASK-005-remove-envers.md) - Envers

---

**Criado**: 2026-02-01  
**Início Previsto**: 2026-02-01  
**Fim Previsto**: 2026-02-03
