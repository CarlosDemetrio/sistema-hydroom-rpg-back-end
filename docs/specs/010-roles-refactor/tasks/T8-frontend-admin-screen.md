# T8 — Frontend: Tela de administracao de usuarios

> Spec: 010 | Tipo: Frontend | Depende de: T6, T3 (backend) | Bloqueia: nenhuma

---

## Objetivo

Criar a tela `/admin/usuarios` que permite ao ADMIN visualizar todos os usuarios cadastrados e alterar suas roles via interface grafica.

---

## Arquivos Afetados (Angular)

| Arquivo | Acao |
|---------|------|
| `src/app/features/admin/usuarios/admin-usuarios.component.ts` | Criar componente |
| `src/app/features/admin/usuarios/admin-usuarios.component.html` | Criar template |
| `src/app/core/services/admin.service.ts` | Criar service com chamadas de admin |
| `src/app/shared/components/navbar/navbar.component.html` | Adicionar link "Admin" visivel apenas para ADMIN |

---

## Passos

### 1. Criar `AdminService`

```typescript
// core/services/admin.service.ts
@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);

  listarUsuarios(page = 0, size = 20): Observable<PageResponse<Usuario>> {
    return this.http.get<PageResponse<Usuario>>('/api/v1/admin/usuarios', {
      params: { page, size, sort: 'nome' }
    });
  }

  alterarRole(usuarioId: number, novaRole: 'ADMIN' | 'MESTRE' | 'JOGADOR'): Observable<Usuario> {
    return this.http.put<Usuario>(`/api/v1/admin/usuarios/${usuarioId}/role`, { role: novaRole });
  }
}
```

### 2. Criar `AdminUsuariosComponent`

**Funcionalidades da tela:**
- Tabela paginada com colunas: Nome, Email, Role atual, Data de cadastro, Acoes
- Coluna "Acoes": botao "Alterar role" que abre um dialog/modal de confirmacao
- Dialog de confirmacao: dropdown com as 3 opcoes de role + botao Confirmar
- Feedback de sucesso (toast) apos alterar role
- Feedback de erro (toast) se a operacao falhar (ex: ultimo ADMIN)
- Paginacao via PrimeNG Paginator
- Badge colorido para role: ADMIN (vermelho/laranja), MESTRE (azul), JOGADOR (verde), sem role (cinza)

```typescript
// features/admin/usuarios/admin-usuarios.component.ts
@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  // imports: TableModule, ButtonModule, TagModule, DialogModule, DropdownModule, etc.
})
export class AdminUsuariosComponent {
  private adminService = inject(AdminService);
  private messageService = inject(MessageService); // PrimeNG toast

  usuarios = signal<Usuario[]>([]);
  totalRecords = signal(0);
  carregando = signal(false);

  // Estado do dialog de alteracao de role
  dialogVisivel = signal(false);
  usuarioSelecionado = signal<Usuario | null>(null);
  novaRoleSelecionada = signal<string | null>(null);
  salvando = signal(false);

  readonly opcoeRole = [
    { label: 'ADMIN', value: 'ADMIN' },
    { label: 'MESTRE', value: 'MESTRE' },
    { label: 'JOGADOR', value: 'JOGADOR' },
  ];

  carregarUsuarios(event: TableLazyLoadEvent) {
    // implementacao...
  }

  abrirDialogAlterarRole(usuario: Usuario) {
    this.usuarioSelecionado.set(usuario);
    this.novaRoleSelecionada.set(usuario.role);
    this.dialogVisivel.set(true);
  }

  confirmarAlteracaoRole() {
    // implementacao...
  }
}
```

### 3. Adicionar link na NavBar

Adicionar link "Administracao" ou icone de engrenagem na navbar visivel apenas para ADMIN:

```html
<!-- navbar.component.html - dentro do menu de navegacao -->
@if (authService.isAdmin()) {
  <a routerLink="/admin/usuarios" routerLinkActive="active">
    Administracao
  </a>
}
```

---

## Regras de UX Criticas

1. **Confirmacao obrigatoria:** A alteracao de role deve sempre exigir um passo de confirmacao (dialog) para prevenir cliques acidentais. Nao alterar diretamente ao clicar no botao.

2. **Feedback imediato:** Apos alterar role com sucesso, a tabela deve refletir a nova role sem precisar de reload da pagina.

3. **Tratamento de erro do ultimo ADMIN:** Se o backend retornar HTTP 409 (ultimo ADMIN), exibir mensagem clara: "Nao e possivel alterar: este e o unico ADMIN do sistema."

4. **Indicacao visual de role:** Usar badges coloridos para facilitar a identificacao rapida. Sugestao: ADMIN = vermelho, MESTRE = azul escuro, JOGADOR = verde, sem role = cinza.

---

## Criterios de Aceitacao

- [ ] Tabela carrega lista paginada de usuarios via GET /api/v1/admin/usuarios
- [ ] Clique em "Alterar role" abre dialog com dropdown de opcoes
- [ ] Confirmacao no dialog chama PUT /api/v1/admin/usuarios/{id}/role
- [ ] Sucesso exibe toast de confirmacao e atualiza a linha na tabela
- [ ] Erro HTTP 409 exibe mensagem especifica sobre ultimo ADMIN
- [ ] Link "Administracao" na navbar aparece apenas para ADMIN
- [ ] Rota /admin/usuarios retorna /403 para nao-ADMIN (testado pelo AdminGuard)
- [ ] Testes Vitest: ao menos 4 cenarios (listagem, alterar role com sucesso, erro 409, acesso negado)
