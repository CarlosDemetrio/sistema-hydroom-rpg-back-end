# T8 — Frontend: Testes de Galeria, Anotacao e Pastas

> Fase: P2 (Frontend)
> Estimativa: 1.5 dias
> Depende de: T5 (Edicao inline + Markdown + Pastas), T7 (Componentes e aba Galeria)
> Bloqueia: nenhuma

---

## Objetivo

Cobrir com testes Vitest os novos comportamentos de edicao de anotacao (Markdown, visivelParaTodos), arvore de pastas, upload de imagem via Cloudinary e os componentes de galeria. Garantir que regras de negocio de acesso e UX estejam corretas.

---

## Padrao de Testes do Projeto

- Framework: **Vitest** + `@testing-library/angular`
- Mocks: `vi.fn()` (equivalente a `jest.fn()`)
- Padrao: Arrange-Act-Assert
- Executar: `npx vitest run src/path/to/file.spec.ts`

---

## Suite 1: `anotacao-card.component.spec.ts`

```
src/app/features/jogador/pages/ficha-detail/components/anotacao-card/anotacao-card.component.spec.ts
```

### Setup

```typescript
const anotacaoMock: Anotacao = {
  id: 1,
  fichaId: 42,
  autorId: 10,
  autorNome: 'Aldric',
  titulo: 'Minha Nota',
  conteudo: '**Negrito** e _italico_',  // Markdown
  tipoAnotacao: 'JOGADOR',
  visivelParaJogador: false,
  visivelParaTodos: false,
  pastaPaiId: null,
  dataCriacao: '2026-04-03T10:00:00',
  dataUltimaAtualizacao: '2026-04-03T10:00:00',
};
```

### Cenarios

| Cenario | O que testar |
|---------|-------------|
| Renderiza titulo da anotacao | Titulo renderizado no DOM |
| Renderiza conteudo Markdown | `ngx-markdown` presente no DOM (ou o elemento `<markdown>`) |
| Botao "Editar" visivel para MESTRE | `userRole=MESTRE` → botao "Editar" presente |
| Botao "Editar" visivel para autor JOGADOR | `userId == anotacao.autorId` → botao presente |
| Botao "Editar" oculto para JOGADOR sem ser autor | `userId != anotacao.autorId` → botao ausente |
| Ativar modo edicao | Clicar "Editar" → textarea com Markdown bruto aparece |
| Textarea pre-preenchida com conteudo atual | Valor do textarea == `anotacao.conteudo` |
| Cancelar edicao | Clicar "Cancelar" → textarea some; titulo e conteudo originais mantidos |
| Toggle visivelParaJogador visivel para MESTRE | `userRole=MESTRE`, tipo MESTRE, modo edicao → toggle presente |
| Toggle visivelParaJogador oculto para JOGADOR | `userRole=JOGADOR`, modo edicao → toggle ausente |
| Toggle visivelParaTodos visivel para autor | Autor em modo edicao → checkbox `visivelParaTodos` presente |
| Emite evento editar com DTO correto | Clicar "Salvar" → output `editar` emitido com `titulo`, `conteudo`, `visivelParaTodos` |
| Badge "Privado" visivel para MESTRE em anotacao privada | `tipoAnotacao=MESTRE`, `visivelParaJogador=false`, `userRole=MESTRE` → badge presente |
| Badge "Compartilhado" visivel quando visivelParaTodos=true | `visivelParaTodos=true` → badge "Compartilhado" presente |

---

## Suite 2: `imagem-card.component.spec.ts`

```
src/app/features/jogador/pages/ficha-detail/components/imagem-card/imagem-card.component.spec.ts
```

### Setup

```typescript
const imagemAvatarMock: FichaImagem = {
  id: 1,
  fichaId: 42,
  urlCloudinary: 'https://res.cloudinary.com/test/image/upload/avatar.jpg',
  publicId: 'rpg-fichas/1/fichas/42/avatar',
  titulo: 'Aldric, o Guardiao',
  tipoImagem: 'AVATAR',
  ordemExibicao: 0,
  dataCriacao: '2026-04-03T10:00:00',
  dataUltimaAtualizacao: '2026-04-03T10:00:00',
};

const imagemGaleriaMock: FichaImagem = {
  ...imagemAvatarMock,
  id: 2,
  urlCloudinary: 'https://res.cloudinary.com/test/image/upload/galeria.jpg',
  publicId: 'rpg-fichas/1/fichas/42/galeria',
  titulo: null,
  tipoImagem: 'GALERIA',
};
```

### Cenarios

| Cenario | O que testar |
|---------|-------------|
| Renderiza imagem com src correto | `<img>` tem `src` igual a `urlCloudinary` |
| Exibe titulo quando disponivel | `titulo` renderizado abaixo da imagem |
| Nao exibe titulo quando null | Sem titulo no DOM quando `titulo=null` |
| Badge "Avatar" visivel para AVATAR | `tipoImagem=AVATAR` → badge "Avatar" presente |
| Badge "Avatar" ausente para GALERIA | `tipoImagem=GALERIA` → badge "Avatar" ausente |
| Botao delete visivel quando podeDeletar=true | Botao trash presente |
| Botao delete ausente quando podeDeletar=false | Botao trash ausente |
| Emite `deletar` ao clicar trash | Click → output `deletar` emitido com `imagem.id` |
| Emite `expandir` ao clicar na imagem | Click na `<img>` → output `expandir` emitido com a imagem |

---

## Suite 3: `ficha-galeria-tab.component.spec.ts`

```
src/app/features/jogador/pages/ficha-detail/components/ficha-galeria-tab/ficha-galeria-tab.component.spec.ts
```

### Mock do FichaBusinessService

```typescript
const fichaBusinessServiceMock = {
  loadImagens:     vi.fn(),
  adicionarImagem: vi.fn(),
  atualizarImagem: vi.fn(),
  deletarImagem:   vi.fn(),
};
```

### Cenarios

| Cenario | O que testar |
|---------|-------------|
| Exibe loading skeleton | `loadImagens` pendente (usando Subject) → skeletons no DOM |
| Exibe lista de imagens | `loadImagens` resolve com 3 imagens → 3 `app-imagem-card` no DOM |
| Avatar em secao destacada | Imagem AVATAR em secao "Avatar" separada das outras |
| Imagens GALERIA em grade | Imagens GALERIA dentro da secao "Galeria" |
| Empty state para galeria vazia | `loadImagens` resolve com `[]` → empty state visivel |
| CTA no empty state so para usuario com permissao | `fichaJogadorId == userId` → CTA visivel; diferente → CTA ausente |
| Contador "X/20" correto | 3 imagens → "3/20 imagens" no DOM |
| Botao adicionar desabilitado com 20 imagens | `imagens.length == 20` → botao desabilitado |
| Form abre ao clicar "Adicionar imagem" | Click → form de upload visivel |
| Form fecha ao clicar "Cancelar" | Click cancelar → form oculto |
| Selecao de arquivo habilita botao "Fazer Upload" | Sem arquivo selecionado: botao desabilitado; com arquivo: habilitado |
| Upload chama adicionarImagem com FormData correto | `adicionarImagem` chamado com `{ arquivo, tipoImagem, titulo }` |
| Nova imagem GALERIA aparece na lista apos upload | `adicionarImagem` resolve → nova imagem na grade |
| Novo upload AVATAR atualiza avatar anterior para GALERIA | `adicionarImagem` resolve com AVATAR → avatar anterior vira GALERIA na lista |
| Erro 422 exibe toast de limite | `adicionarImagem` rejeita com status 422 → toast "Limite de 20 imagens atingido" |
| Lightbox abre ao clicar em imagem | Output `expandir` de `imagem-card` → `p-dialog` visivel |
| Deletar remove imagem da lista | `deletarImagem` resolve → imagem removida |

---

## Suite 4: `ficha-anotacoes-tab.component.spec.ts` (extensao)

Adicionar ao arquivo existente (ou criar se nao existir):

| Cenario | O que testar |
|---------|-------------|
| Exibe arvore de pastas com `p-tree` | `listarPastas` resolve com 2 pastas → arvore visivel |
| Selecionar pasta filtra anotacoes | Click em pasta → `listarAnotacoes` chamado com `pastaPaiId` correto |
| "Todas" remove filtro de pasta | Click em "Todas" → `listarAnotacoes` chamado sem `pastaPaiId` |
| Anotacao atualizada via evento `editar` | Output `editar` do card → `editarAnotacao` chamado; lista atualizada |
| Conteudo Markdown renderizado | Card com `conteudo="**bold**"` → elemento `<markdown>` no DOM |

---

## Suite 5: Extensao `ficha-business.service.spec.ts`

Adicionar ao arquivo existente:

| Cenario | O que testar |
|---------|-------------|
| `loadImagens` delega para `fichasApiService.getImagens` | Verifica chamada com `fichaId` correto |
| `adicionarImagem` delega para `fichasApiService.adicionarImagem` com DTO correto | Verifica `UploadImagemDto` passado |
| `atualizarImagem` delega para `fichasApiService.atualizarImagem` | Verifica parametros |
| `deletarImagem` delega para `fichasApiService.deletarImagem` | Verifica `fichaId` e `imagemId` |
| `editarAnotacao` delega para `fichasApiService.editarAnotacao` | Verifica `AtualizarAnotacaoDto` passado |
| `listarPastas` delega para `fichasApiService.listarPastas` | Verifica chamada com `fichaId` |
| `criarPasta` delega para `fichasApiService.criarPasta` | Verifica `CriarPastaDto` passado |

---

## Notas de Implementacao

- Usar `render()` do `@testing-library/angular` com `inputs` adequados
- Mockar `FichaBusinessService` com `vi.fn()` retornando `of(...)` para chamadas OK
- Para testar loading state: usar `Subject` em vez de `of()` para controlar quando o observable resolve
- Para testar lightbox: verificar se `p-dialog` tem atributo `[visible]="true"` ou se o elemento interno esta visivel
- Para testar ngx-markdown: verificar presenca do elemento `<markdown>` ou `[data-markdown]` no DOM; nao testar HTML renderizado internamente
- Para testar file picker (`p-fileupload`): simular evento `onSelect` via `fireEvent` ou chamar o handler diretamente
- Lembrar de importar `MarkdownModule` (ou `provideMarkdown`) no setup de cada teste que usa `<markdown>`

---

## Criterios de Aceite

- [ ] Todas as suites acima implementadas
- [ ] `npx vitest run` passa sem erros para cada arquivo de spec
- [ ] Nenhum teste existente quebra (rodar suite completa apos adicionar os novos)
- [ ] Cobertura inclui: renderizacao, upload, controle de acesso, pastas, Markdown, estados de UI
- [ ] Total de testes do frontend cresce em pelo menos 40 novos testes
- [ ] `npx vitest run` (suite completa) termina sem falhas
