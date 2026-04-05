# T3 — CategoriaVantagemConfigComponent com Color Picker

> Spec: 012 | Fase: 1 | Tipo: Frontend | Prioridade: MEDIO
> Depende de: nada (API service já existe em ConfigApiService)
> Bloqueia: T14 (sidebar + rotas)

---

## Objetivo

Criar um componente dedicado para CRUD de `CategoriaVantagem` com color picker visual. Hoje o CRUD não existe — apenas um `<p-select>` dentro do drawer de Vantagens permite escolher categorias existentes, mas não criá-las ou editá-las.

## Contexto

O `ConfigApiService` já tem todos os 5 métodos HTTP para CategoriaVantagem:
- `listCategoriasVantagem(jogoId)`
- `getCategoriaVantagem(jogoId, id)`
- `createCategoriaVantagem(jogoId, dto)`
- `updateCategoriaVantagem(jogoId, id, dto)`
- `deleteCategoriaVantagem(jogoId, id)`

O modelo `CategoriaVantagem` existe em `core/models/categoria-vantagem.model.ts`.

A peculiaridade desta entidade é que o endpoint usa `/api/jogos/{jogoId}/config/categorias-vantagem` (sem `/v1/`, com `jogoId` no path), diferente do padrão dos outros configs.

## Arquivos

- `src/app/features/mestre/pages/config/configs/categorias-vantagem-config/categorias-vantagem-config.component.ts` — criar
- `src/app/features/mestre/pages/config/configs/categorias-vantagem-config/categorias-vantagem-config.component.spec.ts` — criar

## Passos

### 1. Modelo de dados

Verificar se `CategoriaVantagem` em `categoria-vantagem.model.ts` tem os campos:
- `id`, `jogoId`, `nome`, `descricao`, `cor`, `ordemExibicao`, `dataCriacao`, `dataUltimaAtualizacao`

Se faltar algum campo, atualizar a interface.

### 2. Business service

Verificar se existe `CategoriaVantagemService` em `core/services/business/config/`. Se não existir, criar seguindo o padrão de `NivelConfigService`. Nota: como o endpoint é diferente (jogoId no path), o service pode precisar de tratamento especial.

### 3. Estrutura do componente

Diferente dos outros configs, este componente não pode estender `BaseConfigComponent` diretamente se o endpoint tiver assinatura diferente. Criar como componente standalone sem a herança, implementando os padrões manualmente ou adaptando o BaseConfigService.

### 4. Colunas da tabela

| Campo | Exibição |
|-------|---------|
| `nome` | Texto direto |
| `cor` | Chip colorido (`<span>` com background-color inline) |
| `descricao` | Texto truncado a 60 chars com tooltip para ver completo |
| `ordemExibicao` | Número |
| Contagem de vantagens | Computed: `vantagensConfig().filter(v => v.categoriaVantagemId === cat.id).length` |

### 5. Formulário no drawer — Campos

```
Nome*           [InputText, max=100, obrigatório]
Descrição       [Textarea, opcional]
Cor             [p-colorpicker] + [InputText hex manual]
                Preview: [chip colorido com nome]
Ordem Exib.     [p-inputnumber, min=0, default=0]
```

**Color picker implementation:**

```typescript
protected corValue = signal<string>('#6c757d');  // default cinza

protected onColorChange(hexValue: string): void {
  // p-colorpicker retorna valor sem # em alguns modos — normalizar
  const normalized = hexValue.startsWith('#') ? hexValue : `#${hexValue}`;
  this.corValue.set(normalized);
  this.form.patchValue({ cor: normalized });
}
```

Preview em tempo real:
```html
<div class="flex align-items-center gap-2 mt-2">
  <span class="p-tag"
        [style.background-color]="corValue()"
        [style.color]="getContrastColor(corValue())">
    {{ form.get('nome')?.value || 'Preview' }}
  </span>
  <small class="text-color-secondary">Preview do chip</small>
</div>
```

Função `getContrastColor()`: retorna `#fff` ou `#000` com base na luminosidade da cor de fundo (fórmula simples: `(R*299 + G*587 + B*114) / 1000 > 128 ? '#000' : '#fff'`).

### 6. Validação do campo cor

Regex client-side: `/^#[0-9A-Fa-f]{6}$/`

O campo cor é opcional. Se vazio ou null, o chip usa a cor padrão da UI (sem style inline).

### 7. Excluir categoria com vantagens vinculadas

Ao clicar em Excluir:
1. Verificar `this.configStore.vantagens().filter(v => v.categoriaVantagemId === cat.id).length`
2. Se > 0: `p-confirmDialog` com mensagem "X vantagens perderão a categoria ao excluir. Deseja continuar?"
3. Se = 0: `p-confirmDialog` padrão de exclusão.

### 8. Importações PrimeNG necessárias

```typescript
import { ColorPickerModule } from 'primeng/colorpicker';
import { TagModule } from 'primeng/tag';
import { TextareaModule } from 'primeng/textarea';
// + os módulos padrão: Button, Card, InputText, InputNumber, Drawer, ConfirmDialog
```

## Critérios de Aceitação

- [ ] Tabela exibe: nome, chip colorido, descrição truncada, ordem, contagem de vantagens vinculadas
- [ ] Drawer com color picker visual + campo hex manual sincronizados
- [ ] Preview do chip atualiza em tempo real ao digitar nome ou selecionar cor
- [ ] Contraste automático no texto do chip (branco/preto conforme luminosidade)
- [ ] Ao excluir categoria com vantagens: modal com contagem
- [ ] Ao excluir categoria sem vantagens: confirmação padrão
- [ ] Validação de campo cor: regex #RRGGBB
- [ ] CRUD completo funcional (criar, editar, excluir)
- [ ] Build sem erros

## Premissas

- `p-colorpicker` da PrimeNG 21 retorna valor hex sem `#` no evento `onChange` — verificar e normalizar
- A contagem de vantagens vinculadas usa o `ConfigStore` já carregado (não faz request adicional)
- `CategoriaVantagemService` precisa ser criado se não existir; verificar antes de criar para evitar duplicação
