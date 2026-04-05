# T2 — PontosVantagemConfigComponent

> Spec: 012 | Fase: 2 | Tipo: Frontend | Prioridade: CRITICO
> Depende de: T1 (modelo + API service)
> Bloqueia: T14 (sidebar + rotas)

---

## Objetivo

Criar do zero o componente de configuração de Pontos de Vantagem por nível. Hoje não existe nenhuma tela para gerenciar quando e quantos pontos de vantagem os personagens ganham.

## Contexto

`PontosVantagemConfig` é uma tabela esparsa: o Mestre só cadastra entradas para níveis com ganhos. Ausência de registro = 0 pontos naquele nível. O componente deve exibir uma tabela com os registros cadastrados e uma coluna calculada "Acumulado" (soma dos pontosGanhos do nível 1 até o nível da linha).

Decisão de UX pendente (P-03 em spec.md): rota própria ou seção dentro de NivelConfig? Por padrão, criar como componente autônomo com rota própria.

## Arquivos

- `src/app/features/mestre/pages/config/configs/pontos-vantagem-config/pontos-vantagem-config.component.ts` — criar
- `src/app/features/mestre/pages/config/configs/pontos-vantagem-config/pontos-vantagem-config.component.spec.ts` — criar

## Passos

### 1. Estrutura do componente

Seguir o padrão de `NiveisConfigComponent`:
- `extends BaseConfigComponent<PontosVantagemConfig, PontosVantagemConfigService>`
- `service = inject(PontosVantagemConfigService)`
- `drawerVisible = signal(false)`
- `loading = signal(false)`
- `searchQuery = signal('')`

### 2. Colunas da tabela

```typescript
readonly columns: ConfigTableColumn[] = [
  { field: 'nivel',       header: 'Nível',          width: '8rem' },
  { field: 'pontosGanhos', header: 'Pontos Ganhos',  width: '10rem' },
  // coluna "Acumulado" — calculada, não do campo direto
];
```

A coluna "Acumulado" não pode usar o `field` direto — precisa de template customizado no `BaseConfigTableComponent` ou, se não suportar, calcular via um computed:

```typescript
protected acumuladoPorNivel = computed(() => {
  const sorted = [...this.items()].sort((a, b) => a.nivel - b.nivel);
  let acumulado = 0;
  const mapa = new Map<number, number>();
  for (const item of sorted) {
    acumulado += item.pontosGanhos;
    mapa.set(item.nivel, acumulado);
  }
  return mapa;
});
```

### 3. Formulário no drawer

```
Nível          [p-inputnumber, min=1, max=35, obrigatório]
Pontos Ganhos  [p-inputnumber, min=0, default=1, obrigatório]
```

Ao criar: se o nível já existir para o jogo, o backend retorna 409/422. Exibir mensagem de erro.

### 4. Destaque de marcos especiais

Linhas com `pontosGanhos >= 2` recebem badge dourado (severity="warn") na coluna "Pontos Ganhos".

Usar `p-tag` com severity="warn" e value do pontosGanhos quando >= 2; texto simples quando = 1.

### 5. Texto explicativo

Adicionar bloco informativo abaixo do título da tabela:
"Níveis sem registro ganham 0 pontos de vantagem. Apenas os níveis aqui cadastrados concedem pontos."

### 6. Botão excluir — comportamento

Ao excluir: apenas soft delete via `this.delete(id)`. Aviso padrão no confirmDialog.

Nota: pontos já ganhos por fichas não são afetados pelo soft delete (os cálculos de saldo no backend consideram o histórico de níveis atingidos, não os registros atuais de config).

## Critérios de Aceitação

- [ ] Tabela exibe: nível, pontosGanhos, acumulado calculado
- [ ] Linhas com pontosGanhos >= 2 têm badge dourado (marco especial)
- [ ] Drawer de criação com campos nivel (1-35) e pontosGanhos (>= 0)
- [ ] Erro exibido quando nível duplicado para o jogo
- [ ] Texto explicativo sobre ausência de registro = 0 pontos
- [ ] Busca funcional na tabela (filtro por nivel)
- [ ] Build sem erros de TypeScript
- [ ] Teste de integração básico (pelo menos: listar, criar, excluir)

## Premissas

- `BaseConfigTableComponent` aceita ou pode ser adaptado para exibir colunas calculadas (acumulado)
- Nível mínimo é 1 (nível 0 não tem pontos de vantagem)
- O Mestre pode criar entradas apenas para níveis existentes em NivelConfig (premissa: o backend valida isso — confirmar)
