---
name: Design da JogosDisponiveisComponent
description: Padrão de card de jogo com 6 estados de participação para tela de listagem do Jogador
type: project
---

## Componentes estabelecidos

| Componente | Tipo | Responsabilidade |
|-----------|------|-----------------|
| `JogosDisponiveisComponent` | Smart | Carrega lista, filtra, orquestra solicitar acesso |
| `JogoCardComponent` | Dumb | Card individual com 6 estados de participação |

## Interface JogoComParticipacao

Estende `JogoResumo` com campo de status de participação:

```typescript
interface JogoComParticipacao extends JogoResumo {
  meuStatus: StatusParticipante | null; // null = nunca solicitou
  mestreNome: string | null;            // requer campo no backend (ver limitação)
}
```

## 6 estados do card de jogo

| Estado | Condição | Visual |
|--------|----------|--------|
| Sem participação + ativo | meuStatus=null, ativo=true | Botão azul "Solicitar Acesso" |
| Sem participação + inativo | meuStatus=null, ativo=false | Mensagem info "Jogo inativo" |
| PENDENTE | meuStatus='PENDENTE' | Tag amarelo "Aguardando aprovação" |
| APROVADO | meuStatus='APROVADO' | Botão verde "Entrar no Jogo" |
| REJEITADO | meuStatus='REJEITADO' | Tag vermelho + botão text "Solicitar novamente" |
| BANIDO | meuStatus='BANIDO' | Tag vermelho "Banido", sem ação |

## Otimistic update ao solicitar

Ao chamar `POST /jogos/{id}/participantes/solicitar`, atualizar imediatamente o signal para PENDENTE sem esperar nova chamada de lista. Em caso de erro (409 = já solicitado), mostrar toast específico.

## Filtros

- Busca local (não faz nova chamada API) por `nome` e `descricao`
- Toggle "Apenas Ativos" filtro independente da busca

## Paginação dual

- Desktop/Tablet: `p-paginator` (12 cards por página)
- Mobile: "Ver mais" incrementando 6 por vez
- Detectado via `signal(window.innerWidth < 768)`

## Spec completo
- Arquivo: `/Users/carlosdemetrio/IdeaProjects/ficha-controlador/docs/design/JOGOS-DISPONIVEIS-DESIGN.md`
