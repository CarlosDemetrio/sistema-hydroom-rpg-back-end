# T6 — Frontend: Model + API Service de Galeria (Cloudinary)

> Fase: P2 (Frontend)
> Estimativa: 0.5 dia
> Depende de: T3 (Backend: Service + Controller de Galeria com Cloudinary)
> Bloqueia: T7 (Componentes e aba Galeria), T8 (Testes)

---

## Objetivo

Criar o model de imagens de ficha alinhado com o novo contrato de upload via Cloudinary e adicionar os metodos de API e business service para a galeria. Esta task e puramente de dados/servicos, sem componentes visuais.

---

## Mudancas em relacao ao modelo anterior

O modelo original usava `url` (URL externa fornecida pelo usuario). Com Cloudinary, o modelo muda:

| Campo (antes) | Campo (agora) | Motivo |
|---------------|---------------|--------|
| `url: string` | `urlCloudinary: string` | URL gerada pelo Cloudinary |
| (nao existia) | `publicId: string` | ID do arquivo no Cloudinary (para referencia) |
| `descricao: string \| null` | Removido | Simplificado no MVP de upload |

O upload agora e via `FormData` (multipart/form-data) — nao via JSON com URL.

---

## Arquivos a Criar

### 1. `ficha-imagem.model.ts`

```
src/app/core/models/ficha-imagem.model.ts
```

```typescript
/**
 * Tipo de imagem em uma ficha.
 * Alinhado com backend TipoImagem enum.
 *
 * AVATAR: imagem principal do personagem (apenas uma ativa por ficha)
 * GALERIA: imagens secundarias de referencia
 */
export type TipoImagem = 'AVATAR' | 'GALERIA';

/**
 * Imagem associada a uma ficha de personagem.
 * Alinhado com backend FichaImagemResponse record.
 * URL e gerenciada pelo Cloudinary — nao fornecida pelo usuario.
 */
export interface FichaImagem {
  id: number;
  fichaId: number;
  urlCloudinary: string;   // URL publica retornada pelo Cloudinary
  publicId: string;         // ID do arquivo no Cloudinary
  titulo: string | null;
  tipoImagem: TipoImagem;
  ordemExibicao: number;
  dataCriacao: string;
  dataUltimaAtualizacao: string;
}

/**
 * DTO para fazer upload de nova imagem.
 * Enviado como FormData (multipart/form-data), nao como JSON.
 * Montar via FormData no service antes de enviar.
 */
export interface UploadImagemDto {
  arquivo: File;            // arquivo binario selecionado pelo usuario
  tipoImagem: TipoImagem;
  titulo?: string;
}

/**
 * DTO para editar imagem existente (apenas metadados — nao troca o arquivo).
 * Alinhado com backend AtualizarImagemRequest record.
 * Todos os campos opcionais.
 */
export interface AtualizarImagemDto {
  titulo?: string;
  ordemExibicao?: number;
}
```

---

## Arquivos a Modificar

### 2. `src/app/core/models/index.ts`

Adicionar exportacoes:

```typescript
export * from './ficha-imagem.model';
export * from './anotacao-pasta.model';  // se ainda nao exportado por T5
```

### 3. `FichasApiService` — adicionar metodos de imagem com FormData

```
src/app/core/services/api/fichas-api.service.ts
```

```typescript
// Galeria de imagens
getImagens(fichaId: number): Observable<FichaImagem[]> {
  return this.http.get<FichaImagem[]>(`${this.baseUrl}/${fichaId}/imagens`);
}

/**
 * Faz upload de nova imagem via multipart/form-data.
 * O backend envia o arquivo ao Cloudinary e retorna urlCloudinary e publicId.
 */
adicionarImagem(fichaId: number, dto: UploadImagemDto): Observable<FichaImagem> {
  const formData = new FormData();
  formData.append('arquivo',    dto.arquivo);
  formData.append('tipoImagem', dto.tipoImagem);
  if (dto.titulo) {
    formData.append('titulo', dto.titulo);
  }
  return this.http.post<FichaImagem>(`${this.baseUrl}/${fichaId}/imagens`, formData);
}

/**
 * Edita apenas metadados da imagem (titulo, ordem).
 * Nao permite trocar o arquivo — para isso: deletar e fazer novo upload.
 */
atualizarImagem(fichaId: number, imagemId: number, dto: AtualizarImagemDto): Observable<FichaImagem> {
  return this.http.put<FichaImagem>(
    `${this.baseUrl}/${fichaId}/imagens/${imagemId}`,
    dto
  );
}

deletarImagem(fichaId: number, imagemId: number): Observable<void> {
  return this.http.delete<void>(`${this.baseUrl}/${fichaId}/imagens/${imagemId}`);
}
```

> Nota: ao enviar `FormData`, o Angular `HttpClient` define automaticamente o `Content-Type: multipart/form-data` com boundary correto. NAO setar `Content-Type` manualmente no header — isso quebra o boundary.

### 4. `FichaBusinessService` — adicionar metodos de imagem

```
src/app/core/services/business/ficha-business.service.ts
```

```typescript
loadImagens(fichaId: number): Observable<FichaImagem[]> {
  return this.fichasApiService.getImagens(fichaId);
}

adicionarImagem(fichaId: number, dto: UploadImagemDto): Observable<FichaImagem> {
  return this.fichasApiService.adicionarImagem(fichaId, dto);
}

atualizarImagem(fichaId: number, imagemId: number, dto: AtualizarImagemDto): Observable<FichaImagem> {
  return this.fichasApiService.atualizarImagem(fichaId, imagemId, dto);
}

deletarImagem(fichaId: number, imagemId: number): Observable<void> {
  return this.fichasApiService.deletarImagem(fichaId, imagemId);
}
```

---

## Verificacoes

- Confirmar que `baseUrl` no `FichasApiService` aponta para `/api/v1/fichas`
- Verificar que imports de `FichaImagem`, `UploadImagemDto`, `AtualizarImagemDto` estao adicionados nos arquivos modificados
- Confirmar que `index.ts` exporta o novo model antes de T7 comecar
- NAO setar `Content-Type` manual no header do upload — Angular faz isso automaticamente

---

## Criterios de Aceite

- [ ] `ficha-imagem.model.ts` criado com `FichaImagem` (campos `urlCloudinary` e `publicId`), `UploadImagemDto` (campo `arquivo: File`), `AtualizarImagemDto`, `TipoImagem`
- [ ] `index.ts` exporta o novo model
- [ ] `FichasApiService.adicionarImagem()` monta `FormData` e envia como multipart
- [ ] `FichasApiService` tem 4 metodos novos: `getImagens`, `adicionarImagem`, `atualizarImagem`, `deletarImagem`
- [ ] `FichaBusinessService` tem 4 metodos novos correspondentes
- [ ] Nenhum erro de compilacao TypeScript (`npm run build` passa)
- [ ] Nenhum teste existente quebra
