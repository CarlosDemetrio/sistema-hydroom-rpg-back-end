# P1-T3 — FichaCalculationService: Vida, Essência, Ameaça

**Fase:** 1 — Cálculos Core
**Complexidade:** 🟡 Média
**Depende de:** P1-T1 (mesmo arquivo)
**Bloqueia:** P2-T1

## Objetivo

Calcular vida total, vida por membro, essência total e ameaça.

## Checklist

### 1. Métodos no FichaCalculationService

- [ ] `calcularVidaTotal(Ficha ficha, FichaVida vida, int vigorTotal)` → int:
  - Formula: `vigorTotal + ficha.nivel + vida.vt + ficha.renascimentos + vida.outros`
  - Atualiza `vida.vidaTotal`

- [ ] `calcularVidaMembro(FichaVidaMembro membro, int vidaTotal, BigDecimal porcentagem)` → double:
  - Formula: `vidaTotal * porcentagem.doubleValue()`
  - Arredondar para 2 casas decimais
  - Atualiza `membro.vida`

- [ ] `calcularEssenciaTotal(Ficha ficha, FichaEssencia essencia, int vigorTotal, int sabedoriaTotal)` → int:
  - Formula: `FLOOR((vigorTotal + sabedoriaTotal) / 2) + ficha.nivel + ficha.renascimentos + essencia.vantagens + essencia.outros`
  - Usa Math.floor()
  - Atualiza `essencia.essenciaTotal`

- [ ] `calcularAmeacaTotal(Ficha ficha, FichaAmeaca ameaca)` → int:
  - Formula: `ficha.nivel + ameaca.itens + ameaca.titulos + ficha.renascimentos + ameaca.outros`
  - Atualiza `ameaca.total`

- [ ] `recalcularEstado(Ficha ficha, FichaVida vida, List<FichaVidaMembro> membros, FichaEssencia essencia, FichaAmeaca ameaca, int vigorTotal, int sabedoriaTotal)`:
  - Chamar todos os métodos acima em sequência

**Nota:** Vigor (VIG) e Sabedoria (SAB) identificados via sigla/abreviacao nos FichaAtributo. Buscar por abreviacao convencional ou primeira ocorrência configurada.

## Arquivos afetados
- `service/FichaCalculationService.java` (MODIFICAR)

## Verificações de aceitação
- [ ] calcularVidaTotal(vigorTotal=10, nivel=3, vt=2, renascimentos=1, outros=0) → 16
- [ ] calcularEssenciaTotal(vigor=10, sab=8, nivel=3, renascimentos=1, vantagens=2, outros=0) → FLOOR(18/2)+3+1+2 = 9+3+1+2 = 15
- [ ] calcularAmeacaTotal correto com os campos somados
- [ ] `./mvnw test` passa
