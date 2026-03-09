# P2-T1 — Filtros nas Configurações

**Fase:** 2 — Filtros
**Complexidade:** 🟢 Baixa
**Depende de:** nada
**Bloqueia:** nada

## Objetivo

Adicionar parâmetro de busca por nome em todos os endpoints GET de configuração.

## Checklist

### 1. Para cada um dos 13 tipos de config:
- [ ] Adicionar `@RequestParam(required=false) String nome` no método `listar()` do controller
- [ ] Passar para o service e repository
- [ ] Repository: adicionar `findByJogoIdAndNomeContainingIgnoreCaseOrderByOrdemExibicao(Long jogoId, String nome)` — ou usar Specification se preferir

### Tipos afetados:
AtributoConfig, AptidaoConfig, BonusConfig, ClassePersonagem, DadoProspeccaoConfig, GeneroConfig, IndoleConfig, MembroCorpoConfig, NivelConfig, PresencaConfig, Raca, TipoAptidao, VantagemConfig

### 2. Comportamento
- [ ] Se `nome` for null ou blank → retorna todos (comportamento atual)
- [ ] Se `nome` for fornecido → filtra com LIKE (case-insensitive)
- [ ] Sempre ordenado por `ordemExibicao`

## Arquivos afetados
- 13 controllers (MODIFICAR — adicionar param)
- 13 services (MODIFICAR — passar param)
- 13 repositories (MODIFICAR — adicionar query method)

## Verificações de aceitação
- [ ] GET /api/jogos/{id}/atributos?nome=for → retorna apenas atributos com "for" no nome
- [ ] GET /api/jogos/{id}/atributos (sem param) → retorna todos
- [ ] `./mvnw test` passa
