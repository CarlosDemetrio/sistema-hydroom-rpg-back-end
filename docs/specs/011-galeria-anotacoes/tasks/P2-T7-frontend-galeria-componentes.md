# T7 — Frontend: Componentes e Aba Galeria + Upload Cloudinary

> Fase: P2 (Frontend)
> Estimativa: 2 dias
> Depende de: T6 (Model + API Service de Galeria)
> Bloqueia: T8 (Testes)

---

## Objetivo

Criar os componentes visuais da galeria (`ImagemCardComponent` e `FichaGaleriaTabComponent`) e integrar a nova aba "Galeria" ao `FichaDetailPage`. O fluxo de upload usa `p-fileupload` do PrimeNG com envio via `FormData` ao backend, que repassa ao Cloudinary. O componente exibe as URLs retornadas pelo Cloudinary.

---

## Regras de UX

- O AVATAR e exibido em destaque (tamanho maior) no topo da aba
- As imagens de GALERIA sao exibidas em grade responsiva (2-3 colunas)
- Cada imagem tem titulo abaixo se disponivel
- AVATAR tem badge "Avatar" no canto superior esquerdo
- Ao clicar em qualquer imagem: abre `p-dialog` (lightbox) para ver em tamanho maior
- Botao "Definir como avatar": visivel em imagens de GALERIA (cria novo AVATAR com mesmo arquivo via novo upload — ver nota abaixo)
- Botoes de edicao e remocao visiveis apenas para usuarios com permissao
- Contador "X/20 imagens" no cabecalho da aba
- Indicador de progresso durante o upload
- Empty state com CTA "Adicionar primeira imagem" se galeria vazia e usuario tem permissao

> Nota sobre "Definir como avatar": Como `tipoImagem` e imutavel no backend apos o upload, a unica forma de promover uma imagem de GALERIA para AVATAR e fazer um novo upload do mesmo arquivo com `tipoImagem=AVATAR`. O botao "Definir como avatar" deve abrir o file picker pre-configurado com `tipoImagem=AVATAR` e orientar o usuario. Alternativa UX mais clara: mostrar tooltip "Para definir como avatar, faca upload de uma nova imagem como Avatar".

---

## Componente 1: `ImagemCardComponent` [DUMB]

```
src/app/features/jogador/pages/ficha-detail/components/imagem-card/imagem-card.component.ts
```

### Inputs/Outputs

```typescript
imagem     = input.required<FichaImagem>();
podeEditar = input.required<boolean>();
podeDeletar = input.required<boolean>();

deletar     = output<number>();         // emite imagem.id
expandir    = output<FichaImagem>();    // abre lightbox
```

> Nota: "Definir como avatar" foi removido como output separado. A UX de promover imagem para avatar e feita via novo upload na tela principal — simplifica o card e evita confusao com a imutabilidade do `tipoImagem`.

### Template

```html
<div class="imagem-card relative overflow-hidden rounded-md border border-surface-200">
  <div class="relative cursor-pointer" (click)="expandir.emit(imagem())">
    <img
      [src]="imagem().urlCloudinary"
      [alt]="imagem().titulo ?? 'Imagem do personagem'"
      class="w-full object-cover"
      style="aspect-ratio: 1 / 1;"
      (error)="onImageError($event)"
    />
    @if (imagem().tipoImagem === 'AVATAR') {
      <span class="absolute top-2 left-2 text-xs bg-primary text-white px-2 py-0.5 rounded">
        Avatar
      </span>
    }
  </div>

  @if (imagem().titulo) {
    <p class="text-xs text-center px-1 py-1 truncate m-0">{{ imagem().titulo }}</p>
  }

  @if (podeEditar() || podeDeletar()) {
    <div class="flex gap-1 justify-end px-1 pb-1">
      @if (podeDeletar()) {
        <p-button icon="pi pi-trash" size="small" text severity="danger"
          (onClick)="confirmarDelete()" />
      }
    </div>
  }
</div>
```

### Metodos

```typescript
onImageError(event: Event): void {
  const img = event.target as HTMLImageElement;
  img.style.display = 'none'; // ocultar imagem quebrada
  // Opcionalmente: substituir por icone placeholder via CSS
}

confirmarDelete(): void {
  this.deletar.emit(this.imagem().id);
}
```

---

## Componente 2: `FichaGaleriaTabComponent` [SMART]

```
src/app/features/jogador/pages/ficha-detail/components/ficha-galeria-tab/ficha-galeria-tab.component.ts
```

### Inputs

```typescript
fichaId         = input.required<number>();
userRole        = input.required<'MESTRE' | 'JOGADOR'>();
userId          = input.required<number>();
fichaJogadorId  = input.required<number | null>();
```

### Estado interno

```typescript
imagens      = signal<FichaImagem[]>([]);
loading      = signal(false);
uploading    = signal(false);
showAddForm  = signal(false);
imagemExpandida = signal<FichaImagem | null>(null);

// Form de upload
arquivoSelecionado  = signal<File | null>(null);
novoTipo            = signal<TipoImagem>('GALERIA');
novoTitulo          = signal('');

// Computed
avatar          = computed(() => this.imagens().find(i => i.tipoImagem === 'AVATAR') ?? null);
galeria         = computed(() => this.imagens().filter(i => i.tipoImagem === 'GALERIA'));
totalImagens    = computed(() => this.imagens().length);
podeAdicionarImagem = computed(() =>
  this.userRole() === 'MESTRE' ||
  (this.userId() !== null && this.userId() === this.fichaJogadorId())
);

tipoOptions = [
  { label: 'Imagem de Galeria', value: 'GALERIA' },
  { label: 'Avatar (imagem principal)', value: 'AVATAR' }
];
```

### Ciclo de vida

```typescript
ngOnInit(): void {
  this.carregarImagens();
}

carregarImagens(): void {
  this.loading.set(true);
  this.fichaBusinessService.loadImagens(this.fichaId()).pipe(
    finalize(() => this.loading.set(false))
  ).subscribe({
    next:  imagens => this.imagens.set(imagens),
    error: () => this.messageService.add({ severity: 'error', summary: 'Erro ao carregar imagens' })
  });
}
```

### Template

```html
<div class="p-3 flex flex-col gap-4">

  <!-- Cabecalho: contador + botao adicionar -->
  <div class="flex justify-between items-center">
    <span class="text-sm text-color-secondary">{{ totalImagens() }}/20 imagens</span>
    @if (podeAdicionarImagem()) {
      <p-button
        label="Adicionar imagem"
        icon="pi pi-upload"
        outlined
        size="small"
        [disabled]="totalImagens() >= 20"
        (onClick)="showAddForm.set(!showAddForm())" />
    }
  </div>

  <!-- Form de upload -->
  @if (showAddForm()) {
    <p-card styleClass="border border-primary-200">
      <div class="flex flex-col gap-3">
        <p-fileupload
          mode="basic"
          accept="image/jpeg,image/png,image/webp,image/gif"
          [maxFileSize]="10485760"
          chooseLabel="Selecionar imagem (max 10MB)"
          (onSelect)="onArquivoSelecionado($event)"
          [auto]="false" />
        <input pInputText placeholder="Titulo (opcional)" class="w-full"
          [ngModel]="novoTitulo()" (ngModelChange)="novoTitulo.set($event)" />
        <p-selectbutton
          [options]="tipoOptions"
          [ngModel]="novoTipo()"
          (ngModelChange)="novoTipo.set($event)"
          optionLabel="label"
          optionValue="value" />
        <div class="flex gap-2 justify-end">
          <p-button label="Cancelar" text (onClick)="cancelarUpload()" />
          <p-button
            label="Fazer Upload"
            icon="pi pi-cloud-upload"
            [loading]="uploading()"
            [disabled]="arquivoSelecionado() === null"
            (onClick)="salvarImagem()" />
        </div>
      </div>
    </p-card>
  }

  <!-- Loading skeleton -->
  @if (loading()) {
    <div class="grid grid-cols-3 gap-3">
      @for (_ of [1,2,3,4,5,6]; track $index) {
        <p-skeleton height="8rem" />
      }
    </div>
  }

  <!-- Empty state -->
  @else if (imagens().length === 0) {
    <div class="flex flex-col items-center py-10 gap-3 text-center">
      <i class="pi pi-images" style="font-size: 3rem; color: var(--text-color-secondary)"></i>
      <p class="text-color-secondary m-0">Nenhuma imagem adicionada ainda.</p>
      @if (podeAdicionarImagem()) {
        <p-button label="Adicionar primeira imagem" icon="pi pi-upload" outlined
          (onClick)="showAddForm.set(true)" />
      }
    </div>
  }

  <!-- Galeria com imagens -->
  @else {
    <!-- Avatar em destaque -->
    @if (avatar()) {
      <div>
        <p class="text-sm font-semibold text-color-secondary mb-2 m-0">Avatar</p>
        <div style="max-width: 280px;">
          <app-imagem-card
            [imagem]="avatar()!"
            [podeEditar]="podeAdicionarImagem()"
            [podeDeletar]="podeAdicionarImagem()"
            (deletar)="deletarImagem($event)"
            (expandir)="imagemExpandida.set($event)"
          />
        </div>
      </div>
    }

    <!-- Grade de galeria -->
    @if (galeria().length > 0) {
      <div>
        <p class="text-sm font-semibold text-color-secondary mb-2 m-0">Galeria</p>
        <div class="grid grid-cols-2 md:grid-cols-3 gap-3">
          @for (imagem of galeria(); track imagem.id) {
            <app-imagem-card
              [imagem]="imagem"
              [podeEditar]="podeAdicionarImagem()"
              [podeDeletar]="podeAdicionarImagem()"
              (deletar)="deletarImagem($event)"
              (expandir)="imagemExpandida.set($event)"
            />
          }
        </div>
      </div>
    }
  }
</div>

<!-- Lightbox -->
<p-dialog
  [visible]="imagemExpandida() !== null"
  (onHide)="imagemExpandida.set(null)"
  [modal]="true"
  [maximizable]="true"
  [header]="imagemExpandida()?.titulo ?? 'Imagem'">
  @if (imagemExpandida()) {
    <div class="flex justify-center">
      <img [src]="imagemExpandida()!.urlCloudinary"
           [alt]="imagemExpandida()!.titulo ?? ''"
           class="max-w-full max-h-screen" />
    </div>
  }
</p-dialog>
```

### Metodos do componente SMART

```typescript
onArquivoSelecionado(event: any): void {
  const file: File = event.files?.[0] ?? null;
  this.arquivoSelecionado.set(file);
}

cancelarUpload(): void {
  this.showAddForm.set(false);
  this.arquivoSelecionado.set(null);
  this.novoTitulo.set('');
  this.novoTipo.set('GALERIA');
}

salvarImagem(): void {
  const arquivo = this.arquivoSelecionado();
  if (!arquivo) return;
  this.uploading.set(true);
  this.fichaBusinessService.adicionarImagem(this.fichaId(), {
    arquivo,
    tipoImagem: this.novoTipo(),
    titulo: this.novoTitulo().trim() || undefined
  }).pipe(
    finalize(() => this.uploading.set(false))
  ).subscribe({
    next: novaImagem => {
      // Se novo upload for AVATAR, atualizar tipo do avatar anterior localmente
      if (novaImagem.tipoImagem === 'AVATAR') {
        this.imagens.update(list =>
          list.map(i => i.tipoImagem === 'AVATAR' ? { ...i, tipoImagem: 'GALERIA' as TipoImagem } : i)
        );
      }
      this.imagens.update(list => [...list, novaImagem]);
      this.cancelarUpload();
      this.messageService.add({ severity: 'success', summary: 'Imagem adicionada!' });
    },
    error: err => {
      const msg = err.status === 422 ? 'Limite de 20 imagens atingido' : 'Erro ao fazer upload';
      this.messageService.add({ severity: 'error', summary: msg });
    }
  });
}

deletarImagem(imagemId: number): void {
  this.fichaBusinessService.deletarImagem(this.fichaId(), imagemId).subscribe({
    next: () => {
      this.imagens.update(list => list.filter(i => i.id !== imagemId));
      this.messageService.add({ severity: 'success', summary: 'Imagem removida' });
    },
    error: () => this.messageService.add({ severity: 'error', summary: 'Erro ao remover imagem' })
  });
}
```

---

## Integracao no FichaDetailPage

```
src/app/features/jogador/pages/ficha-detail/ficha-detail.component.ts
```

Adicionar aba "Galeria" ao TabView existente:

```html
<p-tabpanel header="Galeria" leftIcon="pi pi-images">
  <app-ficha-galeria-tab
    [fichaId]="fichaId()"
    [userRole]="userRole()"
    [userId]="currentUserId()"
    [fichaJogadorId]="ficha()?.jogadorId ?? null"
  />
</p-tabpanel>
```

Verificar que `userRole` e `currentUserId` ja estao disponíveis no `FichaDetailPage`.

---

## Imports PrimeNG necessarios

```typescript
import { FileUploadModule } from 'primeng/fileupload';
import { DialogModule    } from 'primeng/dialog';
import { SelectButtonModule } from 'primeng/selectbutton';
import { SkeletonModule  } from 'primeng/skeleton';
```

Verificar se esses modulos ja estao importados no projeto (PrimeNG 21 usa imports standalone por componente).

---

## Criterios de Aceite

- [ ] Aba "Galeria" aparece no `FichaDetailPage`
- [ ] Avatar e exibido em secao destacada separada da galeria
- [ ] Grade de galeria responsiva (2-3 colunas)
- [ ] Contador "X/20 imagens" atualiza em tempo real
- [ ] Botao "Adicionar imagem" abre form com file picker
- [ ] File picker aceita apenas JPEG, PNG, WebP, GIF e rejeita arquivos > 10 MB
- [ ] Durante o upload: botao exibe estado de loading com icone de spinner
- [ ] Apos upload bem-sucedido: imagem aparece na grade sem recarregar a pagina
- [ ] Erro HTTP 422 (limite): toast "Limite de 20 imagens atingido"
- [ ] Lightbox abre ao clicar em qualquer imagem; exibe `urlCloudinary`
- [ ] Deletar imagem: remove da lista localmente apos resposta bem-sucedida
- [ ] Empty state com CTA apenas para usuarios com permissao
- [ ] Loading skeleton durante carregamento inicial
- [ ] `npm run build` sem erros de compilacao TypeScript
