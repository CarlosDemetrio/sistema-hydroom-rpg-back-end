# T7 — Frontend: Wizard de onboarding

> Spec: 010 | Tipo: Frontend | Depende de: T6 | Bloqueia: nenhuma

---

## Objetivo

Criar o componente `OnboardingComponent` em `/onboarding` que permite ao usuario recém-autenticado escolher seu perfil (MESTRE ou JOGADOR) antes de acessar o sistema.

---

## Arquivos Afetados (Angular)

| Arquivo | Acao |
|---------|------|
| `src/app/features/onboarding/onboarding.component.ts` | Criar componente |
| `src/app/features/onboarding/onboarding.component.html` | Criar template |
| `src/app/core/services/usuario.service.ts` | Adicionar metodo `definirRole(role: string)` |

---

## Passos

### 1. Criar metodo no `UsuarioService`

```typescript
// core/services/usuario.service.ts
definirRole(role: 'MESTRE' | 'JOGADOR'): Observable<Usuario> {
  return this.http.post<Usuario>('/api/v1/usuarios/me/role', { role });
}
```

### 2. Criar `OnboardingComponent`

**Comportamento:**
- Exibe boas-vindas com nome do usuario (obtido do AuthService)
- Duas opcoes: "Quero jogar" (JOGADOR) e "Quero criar campanhas" (MESTRE)
- Botao de confirmacao desabilitado ate que uma opcao seja selecionada
- Apos confirmacao bem-sucedida: atualiza o usuario no AuthService e redireciona para `/`

**Regras da tela:**
- Nao deve ter botao "Voltar" — o onboarding e obrigatorio
- O link para `/login` deve estar disponivel para o caso de o usuario querer trocar de conta

```typescript
// features/onboarding/onboarding.component.ts
@Component({
  selector: 'app-onboarding',
  standalone: true,
  templateUrl: './onboarding.component.html',
  // imports PrimeNG: ButtonModule, RadioButtonModule, CardModule, etc.
})
export class OnboardingComponent {
  private usuarioService = inject(UsuarioService);
  private authService = inject(AuthService);
  private router = inject(Router);

  roleSelecionada = signal<'MESTRE' | 'JOGADOR' | null>(null);
  carregando = signal(false);
  erro = signal<string | null>(null);

  nomeUsuario = computed(() => this.authService.usuarioAtual()?.nome ?? 'usuario');

  confirmar() {
    const role = this.roleSelecionada();
    if (!role) return;

    this.carregando.set(true);
    this.erro.set(null);

    this.usuarioService.definirRole(role).subscribe({
      next: (usuario) => {
        this.authService.atualizarUsuarioAtual(usuario); // atualiza o signal no AuthService
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.carregando.set(false);
        this.erro.set('Nao foi possivel definir seu perfil. Tente novamente.');
      }
    });
  }
}
```

### 3. Template do onboarding

Elementos obrigatorios na tela:
- Titulo: "Bem-vindo(a), [nome]!"
- Subtitulo: "Como voce pretende usar o sistema?"
- Card/opcao 1: "Quero jogar" com descricao "Participe de campanhas criadas por outros Mestres"
- Card/opcao 2: "Quero criar campanhas" com descricao "Crie e conduza suas proprias aventuras"
- Botao "Confirmar" — desabilitado enquanto nenhuma opcao selecionada e durante loading
- Link discreto: "Sair e trocar de conta" (aponta para /logout ou equivalente)
- Mensagem de erro se a chamada falhar

---

## Criterios de Aceitacao

- [ ] Componente exibe nome do usuario da sessao atual
- [ ] Botao "Confirmar" fica desabilitado ate que uma opcao seja selecionada
- [ ] Botao fica em estado de loading durante a chamada POST /me/role
- [ ] Apos sucesso, usuario e redirecionado para a pagina inicial (/)
- [ ] Erro de rede exibe mensagem de retry (nao crashar silenciosamente)
- [ ] Nao ha loop: apos definir role, o AuthGuard nao redireciona de volta para /onboarding
- [ ] Testes Vitest: ao menos 3 cenarios (sucesso MESTRE, sucesso JOGADOR, erro de rede)
