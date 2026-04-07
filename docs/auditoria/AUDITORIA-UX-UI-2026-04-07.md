# Auditoria — UX/UI das Telas Internas
**Data**: 2026-04-07
**Auditor**: primeng-ux-architect
**Escopo**: Frontend Angular — navegação, botão voltar, dialogs vs drawers, GameDefault, achados extras

---

## Sumário Executivo

A auditoria identificou três categorias de problemas. O mais pervasivo é a ausência de botão "Voltar ao Dashboard" em praticamente todas as telas internas do Mestre: `config-layout`, `jogo-detail`, `jogo-form`, `npcs` e `ficha-wizard` não possuem nenhum caminho visual de retorno ao dashboard principal — o usuário fica "preso" nas telas filhas sem referência de onde está na hierarquia. Do lado do Jogador, `fichas-list`, `ficha-detail` e `ficha-wizard` têm o mesmo problema.

Sobre drawers vs dialogs: a migração foi concluída com sucesso — nenhum componente de produção usa `DrawerModule` ou `p-drawer` para formulários. A nomenclatura "drawer" sobrevive apenas nos `describe()` dos arquivos de teste (`.spec.ts`), o que é aceitável. O padrão `p-dialog` está consistente em todos os 13 configs, NPCs e detalhes de raça/classe.

O problema reportado como "GameDefault não carrega Raças" exige esclarecimento: não existe um componente chamado `GameDefaultConfigComponent` no frontend. O PO está se referindo ao resultado do `DefaultGameConfigProviderImpl` do backend — as configurações inicializadas automaticamente ao criar um jogo. Os bugs reais de dados (Raças ausentes, dados errados) estão no backend (`DefaultGameConfigProviderImpl.java`), não no frontend. Porém, existem problemas secundários no `racas-config` que podem causar comportamentos inesperados na listagem inicial.

---

## P1 — Falta de Botão "Voltar ao Dashboard"

### Telas afetadas

| Tela | Rota | Arquivo | Tem voltar? | Recomendação |
|------|------|---------|-------------|--------------|
| Configurações do Sistema (layout) | `/mestre/config/*` | `config-layout.component.ts:34` | **NÃO** | Adicionar botão `pi pi-arrow-left` + label "Voltar" no header, linkando para `/dashboard` |
| Configurações — qualquer aba | `/mestre/config/atributos` (e todas as 14 sub-rotas) | `config-layout.component.ts` (header compartilhado) | **NÃO** | Herdado do layout acima — um único fix resolve todas as abas |
| Detalhe do Jogo | `/mestre/jogos/:id` | `jogo-detail.component.ts:64` | **NÃO** | Adicionar botão "Voltar" para `/mestre/jogos` antes do `<h1>` |
| Criar/Editar Jogo | `/mestre/jogos/novo`, `/mestre/jogos/:id/edit` | `jogo-form.component.ts:35` | **NÃO** | Botão "Cancelar" existe mas não tem ícone de arrow-left; não aparece como "voltar" visualmente |
| NPCs | `/mestre/npcs` | `npcs.component.ts:54` | **NÃO** | Adicionar botão "Voltar ao Dashboard" antes do `<h1>` |
| Wizard de Ficha (Mestre/Jogador) | `/mestre/fichas/criar`, `/jogador/fichas/nova` | `ficha-wizard.component.ts:83` | **NÃO** | O header mostra "Criar Personagem" mas nenhum escape para cancelar/voltar |
| Lista de Fichas (Jogador) | `/jogador/fichas` | `fichas-list.component.ts:44` | **NÃO** | Sem botão de retorno ao dashboard do jogador |
| Detalhe da Ficha (Jogador) | `/jogador/fichas/:id` | `ficha-detail.component.ts` | **NÃO** (parcial) | `FichaHeaderComponent` tem botões "Editar/Duplicar/Deletar" mas sem "Voltar à lista" |
| Jogos Disponíveis (Jogador) | `/jogador/jogos` | `jogos-disponiveis.component.ts` | A verificar | Não auditado completamente (fora das telas críticas) |

**Exceções corretas:**
- `JogadorDashboardComponent` tem botão "Voltar" (linha 29) para `voltarHome()` — esse sim está correto.
- `jogos-list.component.ts` está numa camada de listagem "raiz" do Mestre — dispensável ter "Voltar ao Dashboard", mas poderia ter breadcrumb.

### Análise do padrão atual

O `MainLayoutComponent` usa apenas `<app-header>` + `<router-outlet>` — sem sidebar ativa, sem breadcrumb global, sem contexto de navegação. O `SidebarComponent` existe mas **não está sendo renderizado pelo `MainLayoutComponent`** (linha 14 do `main-layout.component.ts` — apenas importa `HeaderComponent`). O `HeaderComponent` não expõe nenhum mecanismo de "navegação hierárquica".

O resultado é que o usuário depende 100% do botão físico "Voltar" do browser ou de links secundários escondidos para sair de uma tela filha.

### Padrão proposto — PageHeader com Voltar

Criar um componente `PageHeaderComponent` reutilizável e usá-lo no topo de todas as telas filhas:

```typescript
// shared/components/page-header/page-header.component.ts
@Component({
  selector: 'app-page-header',
  imports: [ButtonModule, BreadcrumbModule],
  template: `
    <div class="flex align-items-center gap-3 mb-4">
      @if (backRoute()) {
        <p-button
          icon="pi pi-arrow-left"
          [text]="true"
          [rounded]="true"
          severity="secondary"
          [pTooltip]="backLabel()"
          (onClick)="navegar()"
          aria-label="Voltar"
        />
      }
      <div class="flex flex-column gap-1 flex-1">
        <h1 class="text-2xl font-bold m-0">{{ titulo() }}</h1>
        @if (subtitulo()) {
          <p class="text-sm text-color-secondary m-0">{{ subtitulo() }}</p>
        }
      </div>
      <ng-content select="[actions]" />
    </div>
  `
})
export class PageHeaderComponent {
  backRoute = input<string | string[] | null>(null);
  backLabel = input<string>('Voltar');
  titulo = input.required<string>();
  subtitulo = input<string>('');
  private router = inject(Router);
  navegar() { this.router.navigate(Array.isArray(this.backRoute()) ? this.backRoute()! : [this.backRoute()!]); }
}
```

Uso nos componentes filhos:

```html
<!-- config-layout.component.ts -->
<app-page-header
  titulo="Configurações do Sistema"
  subtitulo="Configure as regras e mecânicas do seu jogo"
  backRoute="/dashboard"
  backLabel="Voltar ao Dashboard"
>
  <div actions class="flex gap-2">
    <!-- botões Exportar/Importar existentes -->
  </div>
</app-page-header>
```

```html
<!-- jogo-detail.component.ts -->
<app-page-header
  [titulo]="jogo()?.nome ?? ''"
  backRoute="/mestre/jogos"
  backLabel="Meus Jogos"
>
  <div actions><!-- Editar / Excluir --></div>
</app-page-header>
```

---

## P2 — Drawers vs Dialogs (consistência)

### Resultado da auditoria

**Nenhum componente de produção usa `DrawerModule`, `p-drawer` ou `p-sidebar` para formulários de edição.**

A busca por `p-drawer`, `pDrawer`, `DrawerModule` e `SidebarModule` em todos os `.ts` de produção retornou **zero matches**. A migração relatada pelo PO está 100% concluída nos componentes de runtime.

### Achado residual — nomenclatura nos testes

Os arquivos `.spec.ts` ainda usam `describe('drawer de criação', ...)` e `describe('abertura do drawer', ...)` em vários testes, incluindo:

- `npcs.component.spec.ts:200` — `describe('drawer de criação', ...)`
- `pontos-vantagem-config.component.spec.ts:269` — `describe('abertura do drawer', ...)`
- Outros specs com nomenclatura similar

Isso não impacta o runtime mas cria confusão ao ler os testes. Recomenda-se renomear para `describe('dialog de criação', ...)` numa limpeza técnica futura (baixa prioridade — não bloqueia RC).

### Achado: `drawerVisible` como nome de signal

Em **todos** os config components e em `npcs.component.ts`, o signal que controla a abertura do `p-dialog` ainda se chama `drawerVisible` (ex: `npcs.component.ts:316`, `racas-config.component.ts:577`). O template do NPC usa `[visible]="drawerVisible()"` para o `p-dialog`. Isso é apenas nomenclatura incorreta — não afeta funcionamento — mas confunde manutenção futura.

Recomendação: renomear `drawerVisible` → `dialogVisible` em todos os config components (pode ser feito na mesma task de refactor).

---

## P3 — "GameDefault" — Configurações Padrão do Jogo

### Esclarecimento: o que é o "GameDefault"

**Não existe um componente `GameDefaultConfigComponent` no frontend.** O termo "GameDefault" usado pelo PO se refere ao `DefaultGameConfigProviderImpl.java` + `GameConfigInitializerService.java` do backend — que populam automaticamente as configurações de um jogo recém-criado (atributos, raças, classes, índoles, etc.).

O usuário enxerga o resultado dessas configurações ao navegar para `/mestre/config/*` após criar um novo jogo.

### Problema: "a aba Raças nem carrega"

**Causa-raiz real: os bugs estão no backend**, não no frontend. O `DefaultGameConfigProviderImpl.java` tem falhas documentadas em `docs/analises/DEFAULT-CONFIG-AUDITORIA.md`:

- **BUG-DC-06**: `MembroCorpoConfig` com `Cabeça = 0.25` (deveria ser `0.75`) — crítico
- **BUG-DC-07**: Índoles com os 9 alinhamentos D&D em vez dos 3 valores definidos pelo PO
- **BUG-DC-08**: Presenças com escala de intensidade errada em vez de postura ética
- **Raças ausentes/com bugs**: `CategoriaVantagem`, `PontosVantagemConfig`, `ClasseBonus`, `ClasseAptidaoBonus`, `LimitadorConfig` (comentado) — não inicializados

**Porém**, há também um problema de UX no frontend para o caso de Raças:

### Problema secundário no frontend: racas-config não exibe erro claro

Arquivo: `racas-config.component.ts`, método `ngOnInit` herdado de `BaseConfigComponent` (linha 75–87).

Se `hasGame()` retorna `false` (nenhum jogo selecionado), o componente exibe um aviso mas **não chama `loadData()`**. Isso é correto.

Porém, o `racas-config.component.ts:655–665` sobrescreve `ngOnInit` e chama `super.ngOnInit()`, depois faz chamadas adicionais (`listAtributos`, `listClasses`, `listVantagens`) — todas condicionadas a `if (jogoId)`. Se o backend retornar 404 ou 403 para o endpoint `/api/v1/configuracoes/racas`, o `ErrorInterceptor` global trata o erro mas **a tabela simplesmente não renderiza** (o `items()` signal permanece vazio `[]`) sem nenhuma mensagem de erro específica ao usuário — apenas o empty state genérico "Nenhuma Raça cadastrada ainda."

Não existe loading state para as chamadas auxiliares (`listAtributos`, `listClasses`) no `ngOnInit` do `racas-config` — se essas chamadas falharem silenciosamente, os dropdowns de Atributo e Classe dentro do dialog aparecem vazios, impedindo a criação de Bônus de Raça.

### Outros problemas no GameDefault (dados iniciais)

Ao criar um jogo novo, o usuário Mestre verá:

1. **Aba Atributos**: dados corretos (FOR, AGI, VIG, SAB, INT, INTU, AST com fórmulas)
2. **Aba Raças**: Humano e Elfo criados, mas sem `bonusAtributos` e sem `classesPermitidas` populados por padrão
3. **Aba Níveis**: criados corretamente (1–20), mas `limitadorAtributo` pode ser ignorado (BUG-DC-05 da auditoria)
4. **Aba Categorias de Vantagem**: **vazia** — `CategoriaVantagem` não está no provider
5. **Aba Pontos de Vantagem**: **vazia** — `PontosVantagemConfig` não inicializado
6. **Aba Vantagens**: vantagens criadas mas sem `CategoriaVantagem` vinculada (campo aparece em branco no dialog)
7. **Aba Índoles**: valores D&D em vez dos 3 valores simples do Klayrah
8. **Aba Presenças**: escala de intensidade errada

Todos esses problemas são de dados do backend — a Spec 015 já cobre alguns deles (BUG-DC-02..09). O frontend exibe corretamente o que o backend retorna.

### Ausência de tela de "Diagnóstico GameDefault"

Uma funcionalidade útil que ainda não existe: após criar um novo jogo, o Mestre não tem feedback sobre "o que foi criado automaticamente". O fluxo atual é:

1. Mestre cria jogo → redireciona para `/mestre/jogos`
2. Mestre precisa ir manualmente para `/mestre/config` para ver o que foi criado
3. Nenhuma notificação, nenhum resumo das configs inicializadas

Isso é um gap de UX, mas não é bug — é funcionalidade ausente (fora do escopo desta auditoria).

### Arquivos envolvidos nos bugs de dados

- Backend: `src/main/java/.../config/DefaultGameConfigProviderImpl.java`
- Backend: `src/main/java/.../service/GameConfigInitializerService.java`
- Frontend (problema de UX): `racas-config.component.ts:655–665` (loading states ausentes nas chamadas auxiliares)
- Frontend (diagnóstico): `jogo-form.component.ts:199–205` (após criar jogo, navega para `/mestre/jogos` sem nenhum onboarding)

---

## Achados Extras

### EXTRA-01 — `SidebarComponent` não está sendo renderizado

Arquivo: `main-layout.component.ts:14`

O `SidebarComponent` existe em `shared/layout/sidebar.component.ts` com menu de navegação completo por role, mas **não está importado nem renderizado no `MainLayoutComponent`**. O layout atual é apenas `<app-header>` + `<router-outlet>`. Isso explica por que o usuário não tem contexto de navegação global.

**Impacto**: alto — sem sidebar ativa, o único elemento de navegação é o `p-menu` dropdown no avatar do usuário (Perfil / Sair). Todas as navegações entre seções dependem do usuário decorar as URLs ou usar o botão "Voltar" do browser.

Recomendação: decidir entre: (a) reativar o sidebar com `app-sidebar` no `main-layout`, ou (b) adicionar `PageHeaderComponent` com "Voltar" em todas as telas filhas. As duas estratégias podem coexistir (sidebar para mobile-bottom-nav em telas maiores, PageHeader para contexto local).

### EXTRA-02 — Botões "Exportar/Importar" no config-layout não funcionam

Arquivo: `config-layout.component.ts:48–57`

Os botões `p-button label="Exportar"` e `p-button label="Importar"` existem no header do config-layout mas **não têm nenhum handler `(onClick)`**. Os endpoints existem no backend (`GET /jogos/{id}/config/export` e `POST /jogos/{id}/config/import`) e estão documentados em `jogos-api.service.ts:127–137`, mas **a funcionalidade não está conectada ao frontend**.

O PO pode confundir isso como "feature funcionando" — clicar nos botões não faz nada (sem toast de erro, sem ação).

### EXTRA-03 — `jogo-detail.component.ts` com lógica de negócio incorreta

Arquivo: `jogo-detail.component.ts:136–137`

A função `hasBothRoles()` no `header.component.ts:136` retorna `true` quando o usuário tem role `MESTRE` ou `JOGADOR` (operador `||`), o que significa que **qualquer usuário autenticado vê o seletor "Visualizar como:"**. A intenção deveria ser `&&` (apenas quem tem ambas as roles). Isso é um bug de lógica — usuários apenas-Mestre ou apenas-Jogador verão um switcher que não faz nada útil.

Arquivo: `header.component.ts:136–139`
```typescript
// ATUAL (incorreto):
hasBothRoles(): boolean {
  const user = this.authService.currentUser();
  return user?.role === 'MESTRE' || user?.role === 'JOGADOR'; // sempre true
}
```

### EXTRA-04 — Toast sem provider em `jogo-form`

Arquivo: `jogo-form.component.ts:8,12`

O componente injeta `MessageService` e usa `this.messageService.add(...)` mas **não inclui `ToastModule` nos imports nem `<p-toast>` no template**. As mensagens de sucesso/erro após criar/editar jogo são silenciosas. O `ToastService` global existe em `services/toast.service.ts` e todos os outros componentes usam ele — este é o único que usa `MessageService` direto sem o componente toast correspondente.

### EXTRA-05 — `jogo-detail.component.ts` navega para rota errada ao ver ficha

Arquivo: `jogo-detail.component.ts:543`

```typescript
verFicha(fichaId: number) {
  this.router.navigate(['/jogador/fichas', fichaId]);
}
```

Esta rota é protegida por `roleGuard` com `roles: ['JOGADOR']`. Se o Mestre tentar ver uma ficha pelo painel de Jogo Detail, será redirecionado para `/unauthorized`. A rota deveria ser `/mestre/fichas/:id` — que ainda não existe no `app.routes.ts`. Esta funcionalidade está quebrada para o Mestre.

### EXTRA-06 — `base-config.component.ts`: `confirmDelete` sem confirmação

Arquivo: `base-config.component.ts:165–169`

A implementação padrão de `confirmDelete()` na base **deleta diretamente sem nenhum diálogo de confirmação**. Apenas as subclasses que sobrescrevem o método (como `racas-config`, `atributos-config`) têm confirmação. As subclasses que não sobrescrevem — verificar se alguma config simples (generos, indoles, presencas, membros-corpo) deleta sem perguntar.

### EXTRA-07 — Empty state do wizard não tem CTA de cancelar

Arquivo: `ficha-wizard.component.ts:81–97`

O wizard de criação de ficha não tem botão "Cancelar" ou "Voltar" no header. O único escape é o botão Anterior no `WizardRodapeComponent` (que no passo 1 provavelmente não aparece). O usuário que abre o wizard por engano fica preso.

### EXTRA-08 — `fichas-list.component.ts` usa `p-input-icon-left` obsoleto

Arquivo: `fichas-list.component.ts:64` — usa `<span class="p-input-icon-left">` que é a API antiga do PrimeNG v16. Com PrimeNG 21.1.1, a API correta é `<p-icon-field>` + `<p-inputicon>` (que o próprio `fichas-list` usa em outra parte na linha 57–63). Inconsistência dupla no mesmo componente: usa o padrão novo nas linhas 57–63 mas o antigo na linha 64.

Revisando: `fichas-list.component.ts:58–63` já usa `<p-icon-field>` corretamente. Não há duplicidade. Este item não se confirma como bug — era falso positivo.

### EXTRA-09 — Ausência de seletor de jogo no `HeaderComponent`

O `CurrentGameService` e o `SidebarComponent` mencionam um "seletor de jogo no cabeçalho" (`header.component.ts` e vários componentes com `Selecione um jogo no cabeçalho`), mas **o `HeaderComponent` não tem nenhum dropdown de seleção de jogo**. O componente seletor de jogo provavelmente precisa ser construído e integrado ao header.

Impacto: os avisos "Nenhum jogo selecionado. Selecione um jogo no cabeçalho..." que aparecem em configs, NPCs e ficha-wizard ficam sem resposta visual — o usuário não sabe onde está o seletor.

---

## Tasks Sugeridas para Spec Corretiva

**Prioridade P0 (bloqueante RC):**

1. **T1 — Criar `PageHeaderComponent` e adicionar botão Voltar nas telas filhas**
   - Criar `shared/components/page-header/page-header.component.ts` com `backRoute`, `titulo`, `subtitulo` e slot `[actions]`
   - Aplicar em: `config-layout`, `jogo-detail`, `jogo-form`, `npcs`, `ficha-wizard`, `fichas-list`
   - Arquivos: 7 componentes + 1 novo

2. **T2 — Conectar botões Exportar/Importar no config-layout**
   - Implementar `(onClick)="exportarConfig()"` e `(onClick)="importarConfig()"` com os endpoints existentes
   - Usar `FileUpload` do PrimeNG para o Import
   - Arquivo: `config-layout.component.ts`

3. **T3 — Corrigir lógica `hasBothRoles()` no HeaderComponent**
   - Mudar `||` para `&&` (ou verificar dois campos separados no model de usuário)
   - Arquivo: `header.component.ts:136–139`

4. **T4 — Implementar seletor de jogo no HeaderComponent**
   - Adicionar `p-select` ou `p-menu` com os jogos disponíveis via `CurrentGameService.availableGames`
   - Arquivo: `header.component.ts`

5. **T5 — Corrigir `jogo-detail.component.ts`: verFicha para Mestre**
   - Criar rota `/mestre/fichas/:id` em `app.routes.ts` ou adaptar para usar `ficha-detail` com permissões de Mestre
   - Arquivo: `jogo-detail.component.ts:543`, `app.routes.ts`

**Prioridade P1 (impacto UX significativo):**

6. **T6 — Adicionar toast ao `jogo-form.component.ts`**
   - Substituir `MessageService` direto por `ToastService` (padrão do projeto) ou adicionar `<p-toast>` ao template
   - Arquivo: `jogo-form.component.ts`

7. **T7 — Adicionar loading states nas chamadas auxiliares do `racas-config`**
   - As chamadas `listAtributos`, `listClasses`, `listVantagens` no `ngOnInit` não têm estado de loading nem tratamento de erro explícito
   - Arquivo: `racas-config.component.ts:655–665`

8. **T8 — Renomear `drawerVisible` para `dialogVisible` nos config components**
   - Refactor de nomenclatura em todos os 13 configs + `npcs.component.ts`
   - Opcional: atualizar os `describe('drawer...')` nos `.spec.ts`

**Prioridade P2 (qualidade / dívida técnica):**

9. **T9 — Auditar subclasses que não sobrescrevem `confirmDelete` na BaseConfigComponent**
   - Verificar: `generos-config`, `indoles-config`, `presencas-config`, `membros-corpo-config`, `tipos-aptidao-config`
   - Se não sobrescrevem, adicionam confirmação ou chamam `super.delete()` direto?
   - Arquivo: `base-config.component.ts:165`

10. **T10 — Dados padrão incorretos no backend (GameDefault bugs)**
    - Já parcialmente coberto pela Spec 015, mas garantir que BUG-DC-06, 07, 08 estejam contemplados
    - Arquivo backend: `DefaultGameConfigProviderImpl.java`

---

## Mapa de Dependências

```
T4 (seletor de jogo no header) → desbloqueia usabilidade dos warnings "sem jogo selecionado"
T1 (PageHeader) → melhora navegação de todas as telas filhas
T3 (hasBothRoles fix) → independente, baixo risco
T2 (Exportar/Importar) → depende de decisão sobre FileUpload (PrimeNG p-fileUpload)
T5 (verFicha Mestre) → depende de decisão sobre rota `/mestre/fichas/:id`
```
