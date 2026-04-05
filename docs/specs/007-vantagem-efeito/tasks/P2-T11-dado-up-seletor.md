# T11 — Seletor Visual de Dado para DADO_UP

> Fase: Frontend | Dependencias: T9 | Bloqueia: nada
> Estimativa: 2–3 horas

---

## Objetivo

Substituir o texto informativo do tipo `DADO_UP` no formulario de efeito por uma representacao visual da sequencia de dados, mostrando qual dado o personagem teria disponivel em cada nivel da vantagem.

---

## Contexto

`DADO_UP` nao tem campos numericos para preencher — o calculo e posicional. A UI deve explicar visualmente a semantica para o Mestre: "no nivel 1 voce tem d3, no nivel 3 voce tem d6" etc. Tambem deve carregar a sequencia real configurada via `DadoProspeccaoConfig` do jogo.

---

## Arquivos Afetados

| Arquivo | Tipo de mudanca |
|---------|----------------|
| `features/configuracoes/vantagens/efeito-form/efeito-form.component.ts` | Substituir texto fixo por componente visual |
| `features/configuracoes/vantagens/efeito-form/dado-up-preview/dado-up-preview.component.ts` | Novo componente de preview de dados |

---

## DadoUpPreviewComponent

```typescript
// dado-up-preview.component.ts

@Component({
  selector: 'app-dado-up-preview',
  standalone: true,
  template: `
    <div class="dado-up-preview">
      <p class="descricao">
        Cada nivel desta vantagem avanca o dado de prospeccao na sequencia abaixo.
        O personagem usa o dado correspondente ao nivel atual da vantagem.
      </p>

      <div class="sequencia-dados">
        @for (dado of dadosOrdenados(); track dado.id; let i = $index) {
          <div class="dado-item"
               [class.dado-ativo]="i === nivelPreview() - 1"
               [class.dado-cap]="i === dadosOrdenados().length - 1 && nivelPreview() > dadosOrdenados().length">
            <div class="dado-face">d{{ dado.numeroFaces }}</div>
            <div class="dado-nivel">Nivel {{ i + 1 }}</div>
          </div>
        }
      </div>

      <p-slider [(ngModel)]="nivelPreview"
        [min]="1" [max]="nivelMaximo()"
        label="Nivel de preview: {{ nivelPreview() }}" />
    </div>
  `
})
export class DadoUpPreviewComponent {
  dadosOrdenados = input.required<DadoProspeccaoConfig[]>();
  nivelMaximo    = input<number>(10);
  nivelPreview   = signal<number>(1);
}
```

### Estilizacao

- Dado ativo (nivel do preview): destaque visual (borda azul, fundo azul-claro)
- Dado cap: badge "maximo" se o nivel excede a sequencia
- Sequencia horizontal com scroll em telas pequenas

---

## Integracao no EfeitoFormComponent

```html
@if (isDadoUp()) {
  <app-dado-up-preview
    [dadosOrdenados]="dadosConfig()"
    [nivelMaximo]="nivelMaximoVantagem()" />
}
```

```typescript
// Carregar dados ordenados do jogo ao inicializar o componente
dadosConfig = signal<DadoProspeccaoConfig[]>([]);

ngOnInit() {
  this.dadoProspeccaoService.listar().subscribe(dados => {
    // Ordenar por ordemExibicao ASC
    this.dadosConfig.set(dados.sort((a, b) => a.ordemExibicao - b.ordemExibicao));
  });
}
```

---

## Testes (Vitest)

```typescript
describe('DadoUpPreviewComponent', () => {
  it('deve destacar o dado correto para o nivel selecionado', () => {
    // nivelPreview=3, dado na posicao 2 (0-indexed) deve ter classe "dado-ativo"
  });

  it('deve usar o ultimo dado quando nivel excede a sequencia', () => {
    // 3 dados, nivelPreview=10 → classe "dado-cap" no ultimo dado
  });

  it('deve exibir todos os dados da sequencia', () => {
    // 5 dados → 5 elementos .dado-item
  });
});
```

---

## Criterios de Aceitacao

- [ ] Sequencia de dados carregada do DadoProspeccaoConfig do jogo
- [ ] Dado correspondente ao nivel de preview destacado visualmente
- [ ] Slider de preview funciona (nivel 1 a nivelMaximo da vantagem)
- [ ] Ultimo dado da sequencia mostrado como "maximo" quando nivel excede
- [ ] Exibido apenas para tipo DADO_UP (nao para outros tipos)
- [ ] Testes Vitest passando
