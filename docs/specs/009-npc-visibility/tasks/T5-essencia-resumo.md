# T5 — Verificar essenciaAtual no FichaResumoResponse

> Tipo: Backend
> Dependencias: nenhuma
> Desbloqueia: T8 (Frontend essencia reativa)

---

## Objetivo

Confirmar que o `FichaResumoResponse` expoe `essenciaAtual` e `essenciaTotal` separadamente, de forma que o frontend possa renderizar a barra de essencia como `essenciaAtual / essenciaTotal` sem calcular nada no cliente.

---

## Contexto

O campo `essenciaAtual` ja existe em `FichaEssencia` (confirmado em T1). O endpoint `PUT /fichas/{id}/vida` ja persiste `essenciaAtual`. A questao e: o `GET /fichas/{id}/resumo` retorna esses dois valores explicitamente?

---

## Arquivos a Verificar

| Arquivo | O que verificar |
|---------|----------------|
| `dto/response/FichaResumoResponse.java` | Campos `essenciaAtual` e `essenciaTotal` presentes? |
| `service/FichaResumoService.java` | `essenciaAtual` e populado no response? |
| `dto/response/FichaResponse.java` | `essenciaAtual` no response basico da ficha? |

---

## Passos

### Passo 1 — Ler FichaResumoResponse e FichaResumoService

Verificar os campos retornados. Se `essenciaAtual` nao estiver presente:

**Adicionar em FichaResumoResponse:**
```java
Integer essenciaAtual   // de FichaEssencia.essenciaAtual
Integer essenciaTotal   // de FichaEssencia.total (ja deve existir como vidaTotal, ameacaTotal, etc.)
```

**Adicionar em FichaResumoService.getResumo():**
```java
FichaEssencia essencia = fichaEssenciaRepository.findByFichaId(fichaId)...;
// resumo.setEssenciaAtual(essencia.getEssenciaAtual());
// resumo.setEssenciaTotal(essencia.getTotal());
```

### Passo 2 — Verificar FichaResponse (resposta basica)

O `GET /fichas/{id}` tambem deve retornar `essenciaAtual` e `essenciaTotal` para que o header da ficha possa exibir a barra sem chamar o endpoint `/resumo` separado.

Se ausente, adicionar ao `FichaResponse` — ou documentar explicitamente que o frontend deve usar `/resumo` para esses valores.

> **Decisao de arquitetura a confirmar:** O frontend usa `/resumo` para todos os valores de estado de combate (vida, essencia) ou espera esses valores no GET /fichas/{id}? Preferencia: usar `/resumo` para evitar duplicacao de logica.

---

## Criterios de Aceitacao

- [ ] `GET /fichas/{id}/resumo` retorna `essenciaAtual` (inteiro, >= 0)
- [ ] `GET /fichas/{id}/resumo` retorna `essenciaTotal` (inteiro, >= 0)
- [ ] `essenciaAtual <= essenciaTotal` (validar no service — nao deve ser possivel ter mais essencia que o maximo)
- [ ] Documentado no Swagger que `/resumo` e a fonte autoritativa de vida e essencia atuais
