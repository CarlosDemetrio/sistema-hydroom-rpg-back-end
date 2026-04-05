# Spec 011 — Galeria de Imagens e Anotacoes

> Spec: `011-galeria-anotacoes`
> Epic: EPIC 9 — Galeria e Anotacoes de Ficha
> Status: Pronto para implementacao (revisado com decisoes do PO em 2026-04-03)
> Depende de: Spec 006 (Ficha COMPLETA implementada)
> Bloqueia: nenhuma spec posterior

---

## 1. Visao Geral do Negocio

**Problema resolvido:** Fichas de RPG existem no sistema como estruturas numericas (atributos, calculos, vantagens) mas carecem de identidade visual e de contexto narrativo. Jogadores precisam registrar observacoes de partida, segredos do personagem e referencia visual do seu personagem. Mestres precisam criar notas administrativas sobre cada ficha — anotacoes que o jogador nao pode ver — e associar imagens a NPCs para referencia rapida em sessao.

**Objetivo:** Adicionar duas capacidades complementares a fichas ja criadas:
1. **Galeria de Imagens** — upload real de imagens via Cloudinary. Mestre sobe imagens de qualquer ficha; Jogador sobe imagens da propria ficha. Suporte a avatar principal e galeria secundaria.
2. **Anotacoes em Markdown** — notas textuais com editor Markdown, organizadas em pastas hierarquicas (ate 3 niveis), com controle de visibilidade por papel.

**Valor entregue:**
- Jogador: ficha com identidade visual (avatar via upload real) e diario de campanha em Markdown com pastas organizadas.
- Mestre: controle narrativo via anotacoes ocultas com pastas; galeria de referencia para NPCs com imagens proprias.
- NPCs: Jogadores veem apenas nome, titulo e imagem principal do NPC — nenhuma stat ou informacao de ficha.

**Decisao de armazenamento:** Usar **Cloudinary** (tier gratuito: 25 GB storage, 25 GB bandwidth/mes, 25 creditos). Justificativa: melhor tier gratuito entre os concorrentes, SDK oficial para Java e Angular, suporte a transformacoes de imagem nativas (crop, resize para thumbnails automaticos).

---

## 2. Configuracao Cloudinary

### Variaveis de Ambiente (Backend)

O backend usa o SDK oficial do Cloudinary para Java (`com.cloudinary:cloudinary-http5`). As credenciais sao injetadas via variaveis de ambiente — nunca hardcoded.

```
CLOUDINARY_CLOUD_NAME=<seu-cloud-name>
CLOUDINARY_API_KEY=<sua-api-key>
CLOUDINARY_API_SECRET=<seu-api-secret>
```

Configuracao no `application.properties` (referenciando envs):

```properties
cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api-key=${CLOUDINARY_API_KEY}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}
```

Bean de configuracao (`config/CloudinaryConfig.java`):

```java
@Configuration
public class CloudinaryConfig {
    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        ));
    }
}
```

Dependencia Maven a adicionar:

```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http5</artifactId>
    <version>1.39.0</version>
</dependency>
```

### Variaveis de Ambiente (Frontend Angular)

```
CLOUDINARY_CLOUD_NAME=<seu-cloud-name>
```

O frontend nao usa a API Key nem o API Secret — esses valores ficam exclusivamente no backend. O Angular apenas exibe as URLs retornadas pelo backend e usa o `cloudName` para construir URLs de transformacao (thumbnails).

### Pasta de Upload no Cloudinary

Uploads de imagens de fichas serao organizados na pasta:
```
rpg-fichas/{jogoId}/fichas/{fichaId}/
```

Isso facilita auditoria e remocao em lote por jogo.

---

## 3. Estado do Backend — O que ja existe

### Anotacoes: PARCIALMENTE IMPLEMENTADO

A entidade `FichaAnotacao`, seu service, controller, mapper, DTOs e testes de integracao estao implementados no branch `feature/009-npc-fichas-mestre` **sem suporte a pastas e sem editor Markdown** (conteudo e texto plano). Esta spec adiciona estrutura de pastas e adapta o conteudo para Markdown.

| Artefato | Status |
|----------|--------|
| `model/FichaAnotacao.java` | Implementado — precisa de novos campos (`pastaPaiId`) |
| `model/enums/TipoAnotacao.java` | Implementado (`JOGADOR`, `MESTRE`) |
| `repository/FichaAnotacaoRepository.java` | Implementado — precisa de queries por pasta |
| `service/FichaAnotacaoService.java` | Implementado (listar, criar, deletar) |
| `controller/FichaAnotacaoController.java` | Implementado (GET, POST, DELETE) |
| `mapper/FichaAnotacaoMapper.java` | Implementado |
| `dto/request/CriarAnotacaoRequest.java` | Implementado — precisa de campo `pastaPaiId` |
| `dto/response/AnotacaoResponse.java` | Implementado — precisa de campo `pastaPaiId` |
| `FichaAnotacaoServiceIntegrationTest.java` | Implementado (9 testes) |

**Lacunas de backend em anotacoes:**
1. Falta endpoint `PUT /fichas/{fichaId}/anotacoes/{id}` para edicao.
2. Falta entidade `AnotacaoPasta` com suporte hierarquico (max 3 niveis).
3. Falta campo `pastaPaiId` na entidade `FichaAnotacao`.
4. Conteudo precisa aceitar Markdown (mudanca transparente — `TEXT` ja suporta).

### Galeria: NAO IMPLEMENTADO

Nenhuma entidade, service ou controller de imagem existe no backend. Esta spec cria tudo do zero, usando Cloudinary como servico de armazenamento.

---

## 4. Atores Envolvidos

| Ator | Role | Acoes permitidas |
|------|------|-----------------|
| Jogador | JOGADOR | Upload/remocao de imagens da propria ficha; criar/editar/deletar proprias anotacoes; ver anotacoes do Mestre marcadas como visiveis |
| Mestre | MESTRE | Upload/remocao de imagens em qualquer ficha (jogador ou NPC); criar/editar/deletar qualquer anotacao; controlar visibilidade de anotacoes |
| Jogador (visao NPC) | JOGADOR | Ver apenas nome, titulo e imagemPrincipalUrl de NPCs — sem acesso a galeria completa ou anotacoes de NPC |

---

## 5. Modelo de Dados — Galeria de Imagens

### Entidade `FichaImagem` (nova — a criar)

| Campo | Tipo | Restricao | Descricao |
|-------|------|-----------|-----------|
| `id` | Long (PK) | Auto-gerado | — |
| `ficha` | FK → Ficha | NOT NULL | Ficha dona da imagem |
| `urlCloudinary` | VARCHAR(2048) | NOT NULL | URL completa retornada pelo Cloudinary (https://res.cloudinary.com/...) |
| `publicId` | VARCHAR(512) | NOT NULL | ID publico no Cloudinary (ex: `rpg-fichas/1/fichas/42/abc123`) — usado para deletar no Cloudinary |
| `titulo` | VARCHAR(200) | Opcional | Legenda ou nome da imagem |
| `tipoImagem` | ENUM | NOT NULL | `AVATAR`, `GALERIA` |
| `ordemExibicao` | INTEGER | NOT NULL, default 0 | Ordem na galeria |
| `deleted_at` | TIMESTAMP | — | Soft delete via BaseEntity |
| `created_at` | TIMESTAMP | — | Audit via BaseEntity |
| `updated_at` | TIMESTAMP | — | Audit via BaseEntity |

> Nota: o campo `descricao` do modelo anterior foi removido para simplificar o MVP de upload. O `titulo` cobre o caso de uso de legenda.

**Enum `TipoImagem`:**
- `AVATAR` — imagem principal do personagem (portrait/avatar). Apenas uma por ficha pode ser `AVATAR` ativo.
- `GALERIA` — imagens secundarias (cenas, equipamentos, locais, referencias visuais).

**Restricoes de negocio:**
- Apenas uma imagem com `tipoImagem=AVATAR` pode existir por ficha (nao deletada). Se o usuario envia nova imagem de avatar, a anterior e promovida para `GALERIA` automaticamente (nao deletada).
- Limite de 20 imagens por ficha (AVATAR + GALERIA combinados, contando apenas nao deletadas).
- `publicId` e obrigatorio — necessario para deletar do Cloudinary ao fazer soft delete.
- Ao fazer soft delete de uma imagem, o backend **tambem deve deletar o arquivo do Cloudinary** via SDK (chamada `cloudinary.uploader().destroy(publicId)`).

### Tabela: `ficha_imagens`

```sql
CREATE TABLE ficha_imagens (
    id                BIGSERIAL PRIMARY KEY,
    ficha_id          BIGINT NOT NULL REFERENCES fichas(id),
    url_cloudinary    VARCHAR(2048) NOT NULL,
    public_id         VARCHAR(512) NOT NULL,
    titulo            VARCHAR(200),
    tipo_imagem       VARCHAR(20) NOT NULL DEFAULT 'GALERIA',
    ordem_exibicao    INTEGER NOT NULL DEFAULT 0,
    deleted_at        TIMESTAMP,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by        VARCHAR(255),
    updated_by        VARCHAR(255)
);

CREATE INDEX idx_ficha_imagem_ficha ON ficha_imagens(ficha_id);
CREATE INDEX idx_ficha_imagem_tipo  ON ficha_imagens(tipo_imagem);
```

### Fluxo de Upload (Backend)

O upload e feito via `multipart/form-data` — nao via URL externa. O fluxo e:

1. Cliente envia `POST /fichas/{fichaId}/imagens` com `Content-Type: multipart/form-data`, campo `arquivo` (binario) + campo `tipoImagem` + campo `titulo` (opcional).
2. Backend valida acesso (role e ownership da ficha).
3. Backend valida tamanho maximo do arquivo (limite: 10 MB).
4. Backend faz upload via `cloudinary.uploader().upload(arquivo, options)` com `folder = "rpg-fichas/{jogoId}/fichas/{fichaId}"`.
5. Cloudinary retorna `url` e `public_id`.
6. Backend persiste `FichaImagem` com `urlCloudinary` e `publicId`.
7. Backend retorna `FichaImagemResponse` com a URL publica.

### Fluxo de Delecao (Backend)

1. Cliente envia `DELETE /fichas/{fichaId}/imagens/{id}`.
2. Backend faz soft delete local (`imagem.delete()`).
3. Backend chama `cloudinary.uploader().destroy(imagem.getPublicId())` para remover o arquivo do Cloudinary.
4. Se a chamada ao Cloudinary falhar: logar o erro mas **nao reverter** o soft delete local (imagem ficara "orfada" no Cloudinary — aceitavel para MVP, pode ser limpa manualmente).

---

## 6. Endpoints de Galeria (novos)

| Metodo | Path | Role | Content-Type | Descricao |
|--------|------|------|-------------|-----------|
| GET | `/api/v1/fichas/{fichaId}/imagens` | MESTRE, JOGADOR | application/json | Lista imagens (AVATAR primeiro, depois GALERIA por ordem) |
| POST | `/api/v1/fichas/{fichaId}/imagens` | MESTRE, JOGADOR (propria) | multipart/form-data | Faz upload de nova imagem |
| PUT | `/api/v1/fichas/{fichaId}/imagens/{id}` | MESTRE, JOGADOR (propria) | application/json | Edita titulo e ordem (nao troca o arquivo) |
| DELETE | `/api/v1/fichas/{fichaId}/imagens/{id}` | MESTRE, JOGADOR (propria) | — | Remove imagem (soft delete + delete no Cloudinary) |

**Regras de acesso:**
- MESTRE: pode operar em qualquer ficha (jogador ou NPC).
- JOGADOR: somente na propria ficha (`fichaId` onde `jogadorId == usuarioAtual.id`). Nao pode operar em fichas de NPC nem de outros jogadores.

### FichaImagemResponse

```json
{
  "id": 1,
  "fichaId": 42,
  "urlCloudinary": "https://res.cloudinary.com/my-cloud/image/upload/v1234567890/rpg-fichas/1/fichas/42/abc123.jpg",
  "publicId": "rpg-fichas/1/fichas/42/abc123",
  "titulo": "Aldric, o Guardiao",
  "tipoImagem": "AVATAR",
  "ordemExibicao": 0,
  "dataCriacao": "2026-04-03T10:00:00",
  "dataUltimaAtualizacao": "2026-04-03T10:00:00"
}
```

### UploadImagemRequest (multipart/form-data)

| Campo | Tipo | Obrigatorio | Descricao |
|-------|------|------------|-----------|
| `arquivo` | `MultipartFile` | Sim | Arquivo de imagem (JPEG, PNG, WebP, GIF). Max 10 MB |
| `tipoImagem` | String | Sim | `AVATAR` ou `GALERIA` |
| `titulo` | String | Nao | Legenda da imagem, max 200 chars |

### AtualizarImagemRequest (JSON)

```json
{
  "titulo": "Novo titulo",
  "ordemExibicao": 2
}
```

> `urlCloudinary`, `publicId` e `tipoImagem` sao imutaveis apos o upload. Para trocar a imagem, deletar e fazer novo upload.

### Endpoint especial para NPCs — visao do Jogador

O endpoint `GET /api/v1/npcs` (ou equivalente da Spec 009) deve retornar, para Jogadores, apenas:

```json
{
  "id": 10,
  "nome": "Gorrath, o Senhor das Trevas",
  "titulo": "Antagonista Principal",
  "imagemPrincipalUrl": "https://res.cloudinary.com/..."
}
```

O campo `imagemPrincipalUrl` e resolvido pelo backend buscando a `FichaImagem` com `tipoImagem=AVATAR` da ficha NPC. Se nao existe avatar, retorna `null`.

---

## 7. Modelo de Dados — Anotacoes com Pastas

### Entidade `AnotacaoPasta` (nova — a criar)

Pastas sao entidades independentes vinculadas a uma ficha. Suportam hierarquia auto-referencial de ate 3 niveis.

| Campo | Tipo | Restricao | Descricao |
|-------|------|-----------|-----------|
| `id` | Long (PK) | Auto-gerado | — |
| `ficha` | FK → Ficha | NOT NULL | Ficha dona da pasta |
| `nome` | VARCHAR(100) | NOT NULL | Nome da pasta |
| `pastaPaiId` | FK → AnotacaoPasta (nullable) | Opcional | Pasta pai (null = raiz) |
| `ordemExibicao` | INTEGER | NOT NULL, default 0 | Ordem entre irmaos |
| `deleted_at` | TIMESTAMP | — | Soft delete via BaseEntity |
| `created_at` | TIMESTAMP | — | Audit |
| `updated_at` | TIMESTAMP | — | Audit |

**Regras de hierarquia:**
- `pastaPaiId = null` → pasta de nivel 1 (raiz).
- Nivel maximo: 3. O backend valida: se `pastaPai.nivel >= 3`, rejeitar criacao de sub-pasta com HTTP 422.
- O backend calcula `nivel` em tempo de validacao (nao armazenado — derivado via traversal).
- Nomes de pastas devem ser unicos no mesmo pai dentro da mesma ficha. Unique constraint: `(ficha_id, pasta_pai_id, nome)`.
- Soft delete em pasta NAO deleta as anotacoes filhas — anotacoes ficam "sem pasta" (`pastaPaiId = null` efetivo apos delete da pasta).

### Tabela: `anotacao_pastas`

```sql
CREATE TABLE anotacao_pastas (
    id                BIGSERIAL PRIMARY KEY,
    ficha_id          BIGINT NOT NULL REFERENCES fichas(id),
    nome              VARCHAR(100) NOT NULL,
    pasta_pai_id      BIGINT REFERENCES anotacao_pastas(id),
    ordem_exibicao    INTEGER NOT NULL DEFAULT 0,
    deleted_at        TIMESTAMP,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by        VARCHAR(255),
    updated_by        VARCHAR(255),
    UNIQUE (ficha_id, pasta_pai_id, nome)
);
```

### Entidade `FichaAnotacao` — campos adicionados

Ao modelo ja existente, adicionar:

| Campo novo | Tipo | Restricao | Descricao |
|------------|------|-----------|-----------|
| `pastaPaiId` | FK → AnotacaoPasta (nullable) | Opcional | Pasta onde a anotacao vive. Null = raiz (sem pasta) |
| `conteudoMarkdown` | TEXT | NOT NULL | Conteudo da anotacao em formato Markdown. Substitui o campo `conteudo` existente (renomear na migracao ou adicionar alias) |
| `visivelParaTodos` | BOOLEAN | NOT NULL, default false | Marca a anotacao como compartilhada — visivel para todos os participantes do jogo com acesso a ficha |

> Nota sobre migracao: o campo existente `conteudo` pode ser renomeado para `conteudo_markdown` via migracao DDL. O conteudo anterior (texto plano) continua valido como Markdown (texto plano e Markdown valido).

### Tabela alterada: `ficha_anotacoes`

```sql
ALTER TABLE ficha_anotacoes
    ADD COLUMN pasta_pai_id     BIGINT REFERENCES anotacao_pastas(id),
    ADD COLUMN visivel_para_todos BOOLEAN NOT NULL DEFAULT false;

-- Se necessario renomear coluna existente:
-- ALTER TABLE ficha_anotacoes RENAME COLUMN conteudo TO conteudo_markdown;
```

### Regras de visibilidade (atualizado)

| Quem le | O que ve |
|---------|---------|
| MESTRE | Todas as anotacoes de qualquer ficha do jogo (tipo JOGADOR e MESTRE, qualquer pasta) |
| JOGADOR (dono da ficha) | Proprias anotacoes (tipo JOGADOR, autor == ele) + anotacoes do Mestre com `visivelParaJogador=true` + qualquer anotacao com `visivelParaTodos=true` da sua ficha |
| JOGADOR (outra ficha) | HTTP 403 — nao ve anotacoes de fichas alheias |
| NPCs | Apenas MESTRE pode criar/ver anotacoes de fichas NPC |

---

## 8. Endpoints de Anotacoes (completo apos implementacao)

### Endpoints de Pastas (novos)

| Metodo | Path | Role | Descricao |
|--------|------|------|-----------|
| GET | `/api/v1/fichas/{fichaId}/anotacao-pastas` | MESTRE, JOGADOR | Lista arvore de pastas da ficha |
| POST | `/api/v1/fichas/{fichaId}/anotacao-pastas` | MESTRE, JOGADOR (propria) | Cria nova pasta |
| PUT | `/api/v1/fichas/{fichaId}/anotacao-pastas/{id}` | MESTRE, JOGADOR (propria) | Renomeia pasta |
| DELETE | `/api/v1/fichas/{fichaId}/anotacao-pastas/{id}` | MESTRE, JOGADOR (propria) | Deleta pasta (soft delete; anotacoes filhas ficam sem pasta) |

### Endpoints de Anotacoes (ja existentes + novos)

| Metodo | Path | Role | Descricao |
|--------|------|------|-----------|
| GET | `/api/v1/fichas/{fichaId}/anotacoes` | MESTRE, JOGADOR | Listagem com filtro de visibilidade e pasta opcional `?pastaPaiId=X` |
| POST | `/api/v1/fichas/{fichaId}/anotacoes` | MESTRE, JOGADOR | Criar anotacao (ja implementado — adicionar campo `pastaPaiId`) |
| PUT | `/api/v1/fichas/{fichaId}/anotacoes/{id}` | MESTRE, JOGADOR (autor) | Editar anotacao — a implementar |
| DELETE | `/api/v1/fichas/{fichaId}/anotacoes/{id}` | MESTRE, JOGADOR (autor) | Deletar anotacao (ja implementado) |

---

## 9. Editor Markdown — Recomendacao para Angular 21

### Decisao: separar renderizacao de edicao

O problema com editores "WYSIWYG que geram Markdown" (como `ngx-quill` configurado para Markdown) e que eles sao complexos de manter e frequentemente tem inconsistencias com o padrao CommonMark. Para Angular 21, a abordagem recomendada e:

**Para renderizacao (visualizacao):** `ngx-markdown`
- Biblioteca madura, atualizada para Angular 18+ (signals-compatible).
- Instalacao: `npm install ngx-markdown marked`
- Uso: `<markdown [data]="anotacao.conteudoMarkdown" />` ou pipe `{{ conteudo | markdown }}`.
- Suporte a sanitizacao automatica via Angular DomSanitizer.

**Para edicao:** `textarea` nativo + preview ao lado (split view)
- A opcao mais robusta para Angular 21 com Signals.
- Abordagem: dois paineis lado a lado — esquerda: `<textarea>` com o Markdown bruto; direita: `<markdown [data]="conteudoSignal()" />` renderizando o preview em tempo real.
- Alternativa com syntax highlighting leve: `@uiw/ngx-codemirror` com extensao Markdown do CodeMirror 6. Mais complexo mas oferece highlight de sintaxe Markdown no editor.

**Recomendacao final para MVP:** `ngx-markdown` para renderizacao + `<textarea>` nativo para edicao, sem split view no MVP (split view e enhancement posterior). O textarea nativo e simples, testavel, e nao adiciona dependencias pesadas. O preview renderizado e exibido no modo de visualizacao (fora do modo edicao).

**Instalacao:**
```bash
npm install ngx-markdown marked
```

**Configuracao no AppModule / providers:**
```typescript
import { provideMarkdown } from 'ngx-markdown';

// Em app.config.ts
export const appConfig: ApplicationConfig = {
  providers: [
    provideMarkdown()
  ]
};
```

**Ponto de atencao PA-006:** Verificar compatibilidade exata de `ngx-markdown` com Angular 21 antes de instalar. A versao esperada e `ngx-markdown >= 18` (compativel com Angular 18+). Confirmar no npm a versao mais recente antes de instalar. Se houver problema de compatibilidade, a alternativa e usar `marked` diretamente e renderizar via `[innerHTML]` com sanitizacao manual via `DomSanitizer.bypassSecurityTrustHtml()`.

---

## 10. Regras de Negocio Criticas

### RN-001: TipoAnotacao e imutavel apos criacao
Uma vez criada como tipo MESTRE ou JOGADOR, a anotacao nao pode mudar de tipo.

### RN-002: Jogador nao pode alterar visivelParaJogador
O campo `visivelParaJogador` e controlado exclusivamente pelo MESTRE. Se um Jogador enviar este campo no PUT, e ignorado silenciosamente.

### RN-003: Mestre pode editar qualquer anotacao; Jogador apenas as proprias
Se Jogador tentar editar anotacao de outro jogador ou anotacao do tipo MESTRE: HTTP 403.

### RN-004: Avatar unico por ficha
Se uma nova imagem e criada com `tipoImagem=AVATAR` e ja existe um avatar ativo, o avatar anterior e promovido para tipo `GALERIA` automaticamente. O sistema nunca apaga o avatar anterior automaticamente.

### RN-005: Limite de 20 imagens por ficha
Ao tentar adicionar a 21a imagem (contando apenas nao deletadas), o backend retorna HTTP 422 com mensagem "Limite de 20 imagens por ficha atingido."

### RN-006: Arquivo de imagem — validacao
- Tipos aceitos: `image/jpeg`, `image/png`, `image/webp`, `image/gif`
- Tamanho maximo: 10 MB
- Qualquer outro tipo ou tamanho: HTTP 400

### RN-007: JOGADOR nao acessa fichas de outros jogadores nem NPCs
Qualquer operacao de imagem ou anotacao em ficha onde `fichaId.jogadorId != usuarioAtual.id` (e nao e MESTRE): HTTP 403.

### RN-008: Anotacoes de NPC sao exclusivas do MESTRE
JOGADOR nao pode criar, editar, deletar nem listar anotacoes de fichas com `isNpc=true`.

### RN-009: Imagens de NPC sao exclusivas do MESTRE
JOGADOR nao pode fazer upload, editar, deletar nem listar imagens de fichas com `isNpc=true`. Para visualizacao de NPC, o Jogador usa o endpoint de listagem de NPCs que retorna apenas `imagemPrincipalUrl`.

### RN-010: Soft delete em imagens deleta tambem do Cloudinary
Ao deletar uma imagem (soft delete local), o backend chama `cloudinary.uploader().destroy(publicId)`. Falha na chamada ao Cloudinary e logada mas nao reverte o soft delete local.

### RN-011: Hierarquia de pastas — maximo 3 niveis
`Raiz > Pasta Nivel 1 > Pasta Nivel 2 > Pasta Nivel 3`. Tentar criar pasta filha de uma pasta de nivel 3: HTTP 422 com mensagem "Nivel maximo de hierarquia de pastas atingido (3 niveis)."

### RN-012: Deletar pasta nao deleta anotacoes filhas
Ao deletar uma pasta, as anotacoes vinculadas a ela tem seu `pastaPaiId` setado para `null` (ficam na raiz). Anotacoes nao sao deletadas em cascata.

### RN-013: visivelParaTodos e controlado pelo autor ou pelo MESTRE
Qualquer autor pode marcar a propria anotacao como `visivelParaTodos=true`. MESTRE pode alterar este campo em qualquer anotacao. Jogador nao pode alterar `visivelParaTodos` de anotacao de outro jogador.

### RN-014: NPCs — visao restrita do Jogador
Jogadores que listam NPCs do jogo recebem apenas `id`, `nome`, `titulo` e `imagemPrincipalUrl`. Nenhuma stat, atributo, ficha completa ou anotacao de NPC e exposta ao Jogador.

---

## 11. Requisitos Funcionais

**RF-001** Jogador pode criar anotacoes do tipo JOGADOR na propria ficha, com conteudo em Markdown.

**RF-002** Mestre pode criar anotacoes do tipo MESTRE em qualquer ficha, controlando visibilidade com `visivelParaJogador`.

**RF-003** Jogador ve apenas: proprias anotacoes + anotacoes do Mestre com `visivelParaJogador=true` + anotacoes com `visivelParaTodos=true` da sua ficha.

**RF-004** Mestre ve todas as anotacoes de qualquer ficha do jogo.

**RF-005** Autor de uma anotacao pode editar titulo e conteudo. Mestre pode editar qualquer anotacao e alterar `visivelParaJogador`.

**RF-006** Mestre pode deletar qualquer anotacao. Autor pode deletar a propria anotacao.

**RF-007** Anotacoes podem ser organizadas em pastas com ate 3 niveis de hierarquia.

**RF-008** O frontend renderiza o conteudo das anotacoes como Markdown (usando ngx-markdown).

**RF-009** O frontend permite editar o conteudo em Markdown via textarea nativo.

**RF-010** Cada ficha pode ter um avatar principal (upload via Cloudinary). Ao definir novo avatar, o anterior vira item de galeria.

**RF-011** Cada ficha pode ter ate 20 imagens no total (AVATAR + GALERIA).

**RF-012** Mestre pode fazer upload de imagens em qualquer ficha (jogador ou NPC).

**RF-013** Jogador pode fazer upload de imagens apenas na propria ficha.

**RF-014** A galeria e listada com o AVATAR primeiro, depois as imagens de GALERIA por `ordemExibicao` crescente.

**RF-015** Ao deletar imagem, o arquivo e removido do Cloudinary.

**RF-016** Jogadores que listam NPCs do jogo recebem apenas nome, titulo e imagemPrincipalUrl.

---

## 12. Requisitos Nao Funcionais

- **RNF-001 Performance:** `GET /fichas/{fichaId}/imagens` deve usar indice em `ficha_id`. Upload Cloudinary e assincrono — usar `@Async` se necessario para nao bloquear a thread HTTP.
- **RNF-002 Seguranca:** API Key e API Secret do Cloudinary nunca expostos no frontend. Upload feito sempre pelo backend. `ngx-markdown` sanitiza o HTML renderizado por default — nao desabilitar a sanitizacao.
- **RNF-003 Idempotencia:** `PUT /imagens/{id}` e `PUT /anotacoes/{id}` aceitam campos nulos silenciosamente (nao alteram o valor existente — `NullValuePropertyMappingStrategy.IGNORE`).
- **RNF-004 Consistencia:** O avatar da ficha deve aparecer no `FichaHeader` como fallback visual (se null, exibir inicial do nome em circulo colorido).
- **RNF-005 Tamanho de arquivo:** Limite de 10 MB por imagem validado no backend antes de chamar o Cloudinary.
- **RNF-006 Variaveis de ambiente:** As credenciais Cloudinary nunca devem ser commitadas no repositorio. Usar `.env` local e configurar no CI/CD como secrets.

---

## 13. Epico e User Stories

### Epic 9 — Galeria de Imagens e Anotacoes

---

**US-011-01: Jogador cria e gerencia proprias anotacoes em Markdown com pastas**
Como Jogador com ficha no jogo,
Quero criar, editar e deletar anotacoes em Markdown organizadas em pastas na minha ficha,
Para manter um diario do meu personagem bem organizado durante a campanha.

**Criterios de Aceite:**

Cenario 1: Criacao com sucesso em pasta especifica
  Dado que sou Jogador autenticado na minha ficha
  E existe uma pasta "Sessao 1" na minha ficha
  Quando envio POST /fichas/{id}/anotacoes com titulo, conteudoMarkdown e pastaPaiId da pasta
  Entao recebo HTTP 201 com a anotacao criada
  E a anotacao aparece na pasta "Sessao 1" na listagem

Cenario 2: Renderizacao Markdown
  Dado que a anotacao tem conteudo "**Negrito** e _italico_"
  Quando visualizo o card da anotacao
  Entao o conteudo e renderizado como HTML formatado (negrito e italico)

Cenario 3: Edicao da propria anotacao
  Dado que sou Jogador e a anotacao pertence a mim
  Quando envio PUT /fichas/{id}/anotacoes/{aid} com novo conteudoMarkdown
  Entao recebo HTTP 200 com a anotacao atualizada

Cenario 4: Tentativa de criar anotacao do tipo MESTRE
  Dado que sou Jogador autenticado
  Quando envio POST com tipoAnotacao=MESTRE
  Entao recebo HTTP 403 Forbidden

---

**US-011-02: Mestre gerencia anotacoes administrativas com pastas**
Como Mestre do jogo,
Quero criar anotacoes em pastas organizadas em qualquer ficha e controlar quais o jogador ve,
Para organizar informacoes de campanha sem expor segredos da narrativa.

**Criterios de Aceite:**

Cenario 1: Anotacao privada em pasta aninhada
  Dado que sou Mestre autenticado
  E existe hierarquia "Campanha > Sessao 1 > Segredos"
  Quando crio anotacao com visivelParaJogador=false na pasta "Segredos"
  Entao ao listar como Jogador, essa anotacao NAO aparece
  E ao listar como Mestre, essa anotacao APARECE na pasta correta

Cenario 2: Limite de hierarquia
  Dado que existe pasta de nivel 3 (Raiz > Nivel1 > Nivel2 > Nivel3)
  Quando Mestre tenta criar sub-pasta dentro da pasta de nivel 3
  Entao recebo HTTP 422 com mensagem "Nivel maximo de hierarquia de pastas atingido (3 niveis)"

Cenario 3: Deletar pasta nao deleta anotacoes
  Dado que a pasta "Sessao 1" tem 3 anotacoes
  Quando Mestre deleta a pasta "Sessao 1"
  Entao as 3 anotacoes permanecem existindo, agora sem pasta (raiz)

---

**US-011-03: Jogador faz upload do avatar da ficha**
Como Jogador com ficha no jogo,
Quero fazer upload de uma imagem como avatar do meu personagem,
Para dar identidade visual ao meu personagem com uma imagem propria.

**Criterios de Aceite:**

Cenario 1: Primeiro upload de avatar
  Dado que a ficha nao tem nenhuma imagem
  Quando envio POST /fichas/{id}/imagens com arquivo JPEG valido e tipoImagem=AVATAR
  Entao recebo HTTP 201 com urlCloudinary e publicId
  E a imagem aparece no FichaHeader como avatar

Cenario 2: Substituir avatar existente
  Dado que a ficha ja tem avatar
  Quando envio POST /fichas/{id}/imagens com novo arquivo e tipoImagem=AVATAR
  Entao a imagem anterior muda para tipoImagem=GALERIA automaticamente
  E a nova imagem e o avatar ativo

Cenario 3: Arquivo muito grande
  Dado que envio arquivo com mais de 10 MB
  Entao recebo HTTP 400 com mensagem de validacao de tamanho

Cenario 4: Tipo de arquivo invalido
  Dado que envio arquivo PDF (application/pdf)
  Entao recebo HTTP 400 com mensagem de tipo invalido

Cenario 5: Tentativa de adicionar 21a imagem
  Dado que a ficha ja tem 20 imagens ativas
  Quando tento fazer upload de mais uma
  Entao recebo HTTP 422 com mensagem "Limite de 20 imagens por ficha atingido"

---

**US-011-04: Mestre gerencia galeria de NPC com imagens**
Como Mestre do jogo,
Quero fazer upload de imagens de referencia a fichas de NPC,
Para ter material visual disponivel durante as sessoes.

**Criterios de Aceite:**

Cenario 1: Upload de imagem para NPC
  Dado que sou Mestre e a ficha tem isNpc=true
  Quando envio POST /fichas/{npcId}/imagens com arquivo PNG valido e tipoImagem=GALERIA
  Entao recebo HTTP 201 com urlCloudinary

Cenario 2: Tentativa de Jogador fazer upload em NPC
  Dado que sou Jogador autenticado
  Quando envio POST /fichas/{npcId}/imagens
  Entao recebo HTTP 403 Forbidden

Cenario 3: Jogador ve apenas imagem principal do NPC
  Dado que NPC tem avatar e 3 imagens de galeria
  Quando Jogador busca a lista de NPCs do jogo
  Entao ve apenas nome, titulo e imagemPrincipalUrl de cada NPC
  E nao ve a galeria completa nem atributos do NPC

---

**US-011-05: Visualizar galeria no FichaDetail**
Como usuario autenticado com acesso a ficha,
Quero ver uma aba de galeria no detalhe da ficha,
Para visualizar e gerenciar as imagens do personagem.

**Criterios de Aceite:**

Cenario 1: Galeria com imagens
  Dado que a ficha tem avatar e 2 imagens de galeria
  Quando abro a aba Galeria no FichaDetailPage
  Entao vejo o avatar em destaque no topo
  E abaixo as imagens de galeria em grade
  E cada imagem exibe titulo se disponivel

Cenario 2: Upload inline
  Dado que tenho permissao (MESTRE ou dono da ficha)
  Quando clico "Adicionar imagem"
  Entao abre form com campos: seletor de arquivo, titulo (opcional), tipo (AVATAR ou GALERIA)
  E ao confirmar, o upload e feito para o Cloudinary via backend
  E a imagem aparece na grade sem recarregar a pagina

Cenario 3: Empty state
  Dado que a ficha nao tem nenhuma imagem
  Quando abro a aba Galeria
  Entao vejo empty state com CTA "Adicionar primeira imagem" (se tenho permissao de edicao)

---

## 14. Pontos de Atencao Tecnicos

| ID | Questao | Solucao Proposta |
|----|---------|-----------------|
| PA-001 | Cloudinary SDK para Java — versao correta para Spring Boot 4 / Java 25 | Usar `cloudinary-http5` (versao 1.39.0+) que usa Jakarta EE. Verificar compatibilidade antes de adicionar ao pom.xml |
| PA-002 | Timeout em uploads grandes | Configurar timeout do Cloudinary SDK (parametro `upload_timeout`). Default do SDK pode ser insuficiente para arquivos de 10 MB em conexao lenta |
| PA-003 | Testes de integracao com Cloudinary | Mockar `Cloudinary` bean nos testes (H2 in-memory). Usar `@MockBean Cloudinary` nos testes de integracao do `FichaImagemService`. Nunca chamar Cloudinary real em testes automatizados |
| PA-004 | ngx-markdown e Angular 21 | Verificar versao compativel no npm antes de instalar. Alternativa: usar `marked` diretamente com `DomSanitizer.bypassSecurityTrustHtml()` se houver problema de compatibilidade |
| PA-005 | Renderizacao de Markdown no backend | O backend nao renderiza Markdown — apenas armazena e retorna o texto bruto. Renderizacao e responsabilidade exclusiva do frontend |
| PA-006 | Orfaos no Cloudinary | Se soft delete local ocorre mas a chamada de destroy no Cloudinary falha, o arquivo fica orfao. Para MVP, aceitar e logar. Implementar job de limpeza em spec futura |
| PA-007 | CORS para Cloudinary | O frontend nao chama o Cloudinary diretamente — todo upload passa pelo backend. CORS nao e problema neste modelo |
| PA-008 | Soft delete de pasta e filhos | Ao deletar pasta, decidir se sub-pastas tambem vao para raiz ou se sao deletadas em cascata. Decisao MVP: sub-pastas tambem ficam na raiz (cascata de "desaninhamento") — confirmar com PO antes de implementar |

---

## 15. Pontos em Aberto

| ID | Questao | Impacto |
|----|---------|---------|
| PA-008 | Ao deletar pasta com sub-pastas: sub-pastas ficam na raiz ou sao deletadas em cascata? | Afeta regra RN-012 e a implementacao do service de pastas |
| PA-009 | O limite de 20 imagens por ficha e o ideal para Mestres com muitos NPCs? | Ajustar constante se necessario antes de implementar |
| PA-010 | Reordenacao drag-and-drop das imagens de galeria e necessaria no MVP? | Afeta complexidade do frontend; `ordemExibicao` ja suporta mas precisa de UI de drag |
| PA-011 | Deve existir endpoint separado `GET /fichas/{id}/avatar` para uso no FichaHeader, ou o avatar e sempre buscado junto com a lista completa? | Afeta performance — o FichaHeader pode querer so o avatar, nao a galeria inteira |
| PA-012 | O campo `visivelParaTodos` tambem se aplica a pastas, ou apenas a anotacoes individuais? | Se pastas tambem tem visibilidade, o modelo de dados de `AnotacaoPasta` precisa de `visivelParaTodos` |

---

## 16. Checklist de Validacao UX

- [ ] Avatar no FichaHeader: exibir foto ou inicial do nome como fallback (sem avatar = inicial em circulo colorido)
- [ ] Upload: indicador de progresso durante o upload (barra ou spinner); feedback de sucesso/erro
- [ ] Aba Galeria: layout em grade para imagens de GALERIA; destaque maior para o AVATAR
- [ ] Ao clicar em imagem da galeria: abrir lightbox/dialog para ver em tamanho maior
- [ ] Botao "Definir como avatar": presente em cada imagem de GALERIA para promover
- [ ] Limite de 20 imagens: exibir contador "X/20 imagens" no cabecalho da aba Galeria
- [ ] Anotacoes do Mestre ocultas: fundo visual diferenciado (badge "Privado") para o Mestre
- [ ] Edicao inline de anotacao: card com botao "Editar" que abre textarea com Markdown bruto + preview renderizado ao lado (split view opcional no MVP)
- [ ] Arvore de pastas: usar `p-tree` do PrimeNG para exibir hierarquia; pastas expansiveis/recolhiveis
- [ ] Loading states: skeleton em anotacoes e galeria enquanto carrega
- [ ] Empty states distintos: galeria sem imagens vs anotacoes sem conteudo

---

## 17. Dependencias

- **Depende de:** Spec 006 (entidade Ficha deve estar COMPLETA)
- **Nao depende de:** Spec 007 (motor de calculos) — galeria e anotacoes sao dados narrativos
- **Relaciona-se com:** Spec 009 (NPCs) — o endpoint de listagem de NPCs para Jogadores deve incluir `imagemPrincipalUrl`
- **Nao bloqueia** nenhuma spec posterior

---

*Produzido por: Business Analyst/PO | 2026-04-03 | Spec 011 (revisado com decisoes do PO)*
