---
name: Design da FichaDetailPage
description: Decisões de UX e componentes para a tela central de visualização de ficha de personagem
type: project
---

## Decisões de arquitetura de componentes

### Componentes estabelecidos

| Componente | Tipo | Responsabilidade |
|-----------|------|-----------------|
| `FichaDetailComponent` | Smart | Carrega ficha + resumo via forkJoin, orquestra abas |
| `FichaHeaderComponent` | Dumb | Avatar, nome, raça/classe, badges, barras de vida/essência, botões de ação |
| `FichaResumoTabComponent` | Dumb | Grid de atributos totais + bônus derivados |
| `FichaAtributosTabComponent` | Dumb | p-table read-only de atributos (base, nivel, outros, total, ímpeto) |
| `FichaAptidoesTabComponent` | Dumb | p-fieldset por TipoAptidao + tabela interna |
| `FichaVantagensTabComponent` | Dumb | Cards de vantagens agrupados por categoria |
| `FichaAnotacoesTabComponent` | Smart | CRUD inline de anotações (faz chamadas API diretamente) |
| `AnotacaoCardComponent` | Dumb | Exibe uma anotação com badge de tipo e botão deletar |

### Abas (5 no total, ordem)
1. Resumo — stats calculados pelo backend
2. Atributos — tabela read-only de atributos
3. Aptidoes — agrupadas por TipoAptidao
4. Vantagens — cards por CategoriaVantagem
5. Anotacoes — CRUD inline

### Lazy loading de abas
- Carregar dados de atributos, aptidoes e vantagens APENAS ao abrir a aba correspondente
- Ficha + resumo carregados em paralelo via forkJoin no ngOnInit

## Regras de permissão por role na tela

| Elemento | MESTRE | JOGADOR (dono) | JOGADOR (outro) |
|----------|--------|----------------|-----------------|
| Botão Editar | Sim | Sim | Não |
| Botão Duplicar | Sim | Sim | Não |
| Botão Deletar | Sim | Não | Não |
| Criar anotação MESTRE | Sim | Não | — |
| Ver anotações MESTRE ocultas | Sim | Não | — |
| Deletar anotação | Sim (qualquer) | Sim (só as suas) | Não |
| Subir nível de vantagem | Sim | Sim | Não |

## Anotações: regras visuais

- Anotações do tipo MESTRE com `visivelParaJogador=false`: fundo amarelo `var(--yellow-100)`, borda `var(--yellow-400)`
- Jogadores não veem essas anotações (filtrado pelo backend)
- Badge de tipo sempre com texto (nunca só cor): "JOGADOR" verde, "MESTRE" amarelo

## Vantagens: interface esperada (não alinhada com model atual)

O model atual `FichaVantagem` está desalinhado. A interface correta esperada da aba de vantagens:

```typescript
interface FichaVantagemResponse {
  id: number;
  fichaId: number;
  vantagemConfigId: number;
  vantagemNome: string;
  vantagemSigla: string | null;
  categoriaNome: string;
  nivelAtual: number;
  nivelMaximo: number;
  custoPago: number;
  descricaoEfeito: string | null;
}
```

O `id` aqui é o `vid` usado no endpoint `PUT /fichas/{id}/vantagens/{vid}`.

## Membros do corpo (Fase 2)

- Reservar seção na aba Resumo abaixo dos bônus derivados
- Fase 1: não exibir (sem dados de vida por membro no backend atual)
- Interface preparatória: `MembroCorpoComVida { membroId, membroNome, porcentagemVida, vidaCalculada, vidaAtual }`

## vidaAtual / essenciaAtual

- Campos não existem na Ficha nem no FichaResumo do backend atual
- Para MVP: exibir barras de vida/essência completamente cheias (valor total apenas)
- Preparar componente para receber `vidaAtual` e `essenciaAtual` como inputs opcionais (Fase 2)

## Dialog de duplicar ficha

- Input de nome obrigatório para habilitar o botão confirmar
- Corpo do request: `{ novoNome, manterJogador: true }`
- Após sucesso: navegar para a nova ficha via `resp.fichaId`

## Spec completo
- Arquivo: `/Users/carlosdemetrio/IdeaProjects/ficha-controlador/docs/design/FICHA-DETAIL-DESIGN.md`
