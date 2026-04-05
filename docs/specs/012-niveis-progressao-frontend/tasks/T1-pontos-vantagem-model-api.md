# T1 — Modelo TypeScript + API Service para PontosVantagemConfig

> Spec: 012 | Fase: 1 | Tipo: Frontend | Prioridade: CRITICO
> Depende de: nada
> Bloqueia: T2 (PontosVantagemConfigComponent)

---

## Objetivo

Criar a camada de dados para `PontosVantagemConfig` no frontend: modelo TypeScript, métodos no `ConfigApiService` e business service seguindo o padrão existente. Hoje há zero cobertura dessas entidades no frontend.

## Contexto

Endpoint backend: `/api/v1/jogos/{jogoId}/configuracoes/pontos-vantagem`
Segue o padrão dos outros configs (jogoId no path, não no body da query).

A estrutura de um registro:
```json
{
  "id": 10,
  "jogoId": 5,
  "nivel": 10,
  "pontosGanhos": 3,
  "dataCriacao": "2026-01-01T00:00:00",
  "dataUltimaAtualizacao": "2026-01-01T00:00:00"
}
```

## Arquivos Afetados

- `src/app/core/models/config.models.ts` — adicionar interface `PontosVantagemConfig`
- `src/app/core/services/api/config-api.service.ts` — adicionar 5 métodos
- `src/app/core/services/business/config/pontos-vantagem-config.service.ts` — criar arquivo
- `src/app/core/services/business/config/index.ts` — exportar novo service

## Passos

### 1. Adicionar interface TypeScript em `config.models.ts`

Adicionar após a interface `NivelConfig`:

```typescript
export interface PontosVantagemConfig {
  id: number;
  jogoId: number;
  nivel: number;
  pontosGanhos: number;
  dataCriacao: string;
  dataUltimaAtualizacao: string;
}
```

### 2. Adicionar métodos em `config-api.service.ts`

Verificar qual é o path exato do endpoint no backend (confirmar se é `/api/v1/jogos/{jogoId}/configuracoes/pontos-vantagem` ou variante). Adicionar bloco de métodos seguindo o padrão dos outros configs:

```typescript
// Base: /api/v1/jogos/{jogoId}/configuracoes/pontos-vantagem
listPontosVantagem(jogoId: number): Observable<PontosVantagemConfig[]>
getPontosVantagem(id: number): Observable<PontosVantagemConfig>
createPontosVantagem(dto: { jogoId: number; nivel: number; pontosGanhos: number }): Observable<PontosVantagemConfig>
updatePontosVantagem(id: number, dto: { nivel?: number; pontosGanhos?: number }): Observable<PontosVantagemConfig>
deletePontosVantagem(id: number): Observable<void>
```

**Atenção:** Verificar se o endpoint usa `jogoId` como parâmetro de query (como NivelConfig usa `?jogoId=`) ou no path. Consultar o `NivelConfigController` do backend para confirmar o padrão exato.

### 3. Criar `pontos-vantagem-config.service.ts`

Seguir o padrão de `nivel-config.service.ts`:

```typescript
@Injectable({ providedIn: 'root' })
export class PontosVantagemConfigService extends BaseConfigService<PontosVantagemConfig> {
  private configApi = inject(ConfigApiService);

  protected getApiListMethod() {
    return (jogoId: number) => this.configApi.listPontosVantagem(jogoId);
  }
  protected getApiCreateMethod() {
    return (data: any) => this.configApi.createPontosVantagem(data);
  }
  protected getApiUpdateMethod() {
    return (id: number, data: any) => this.configApi.updatePontosVantagem(id, data);
  }
  protected getApiDeleteMethod() {
    return (id: number) => this.configApi.deletePontosVantagem(id);
  }
}
```

### 4. Exportar no `index.ts`

Adicionar: `export * from './pontos-vantagem-config.service';`

## Critérios de Aceitação

- [ ] Interface `PontosVantagemConfig` tipada corretamente com todos os campos do backend
- [ ] 5 métodos HTTP implementados em `ConfigApiService` sem erros de TypeScript
- [ ] `PontosVantagemConfigService` criado, estendendo `BaseConfigService`
- [ ] Service exportado no barrel de `config/`
- [ ] Build Angular sem erros (0 warnings, 0 errors)
- [ ] Verificar endpoint real do backend antes de hardcodar URL

## Premissas

- O path do endpoint backend para PontosVantagemConfig segue o padrão `/api/v1/jogos/{jogoId}/configuracoes/pontos-vantagem` (verificar na implementação real antes de codificar)
- O campo `nivel` em PontosVantagemConfig pode variar de 1 a 35 (nível 0 não tem pontos de vantagem)
