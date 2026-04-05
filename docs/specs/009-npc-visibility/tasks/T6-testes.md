# T6 — Testes de Integracao

> Tipo: Backend
> Dependencias: T1, T2, T3, T4, T5
> Desbloqueia: T7, T8, T9, T10 (confirmacao dos contratos antes de criar UI)

---

## Objetivo

Cobertura de testes de integracao para todas as funcionalidades implementadas em T1-T5. Usar o padrao H2 in-memory com `@ActiveProfiles("test")` e `@Transactional` nos testes (rollback automatico).

---

## Classes de Teste a Criar

### FichaVisibilidadeServiceIntegrationTest

Cenarios obrigatorios:

| Cenario | Tipo |
|---------|------|
| Mestre revela NPC para jogador especifico | Fluxo feliz |
| Idempotencia: revelar para jogador que ja tem acesso nao duplica | Idempotencia |
| Revogar acesso de jogador | Fluxo feliz |
| Revelar ficha de jogador (isNpc=false) retorna ValidationException | Excecao |
| GET /fichas/{id} por Jogador com FichaVisibilidade retorna ficha | Permissao |
| GET /fichas/{id} por Jogador sem FichaVisibilidade retorna ForbiddenException | Permissao |
| GET /jogos/{id}/fichas por Jogador retorna NPCs com visivelGlobalmente=true | Listagem |
| GET /jogos/{id}/fichas por Jogador NAO retorna NPCs com visivelGlobalmente=false | Listagem |
| jogadorTemAcessoStats = true quando FichaVisibilidade ativa | Campo calculado |
| Revelar para jogador nao participante retorna ValidationException | Validacao |

### ProspeccaoServiceIntegrationTest

Cenarios obrigatorios:

| Cenario | Tipo |
|---------|------|
| Jogador usa dado disponivel — cria ProspeccaoUso PENDENTE e decrementa | Fluxo feliz |
| Jogador usa dado com quantidade=0 — retorna ValidationException | Validacao |
| Jogador nao pode usar dado de ficha de outro jogador | Permissao |
| Mestre reverte uso PENDENTE — status REVERTIDO, incrementa quantidade | Fluxo feliz |
| Mestre confirma uso PENDENTE — status CONFIRMADO, quantidade inalterada | Fluxo feliz |
| Tentativa de reverter uso CONFIRMADO retorna ValidationException | Excecao de estado |
| Tentativa de confirmar uso REVERTIDO retorna ValidationException | Excecao de estado |
| Jogador nao pode reverter proprio uso | Permissao |
| Mestre concede dado — incrementa quantidade | Fluxo feliz |
| GET /fichas/{id}/prospeccao/usos por Jogador retorna apenas usos da propria ficha | Filtro por actor |
| GET /jogos/{id}/prospeccao/pendentes retorna todos os PENDENTES do jogo | Listagem global |

### FichaVidaServiceResetIntegrationTest

Cenarios obrigatorios:

| Cenario | Tipo |
|---------|------|
| Reset restaura vidaAtual=vidaTotal | Fluxo feliz |
| Reset restaura essenciaAtual=total | Fluxo feliz |
| Reset zera danoRecebido de todos os membros | Fluxo feliz |
| Reset NAO altera FichaProspeccao.quantidade | Invariante |
| Reset NAO altera nivel, xp, atributos | Invariante |
| Jogador nao pode resetar | Permissao |
| Mestre de outro jogo nao pode resetar | Permissao |
| Reset e atomico: verificar que todos os campos foram resetados ou nenhum | Atomicidade |

### FichaEssenciaResumoIntegrationTest

Cenarios obrigatorios:

| Cenario | Tipo |
|---------|------|
| GET /resumo retorna essenciaAtual | Campo presente |
| GET /resumo retorna essenciaTotal | Campo presente |
| Apos PUT /vida com essenciaAtual=5, GET /resumo retorna essenciaAtual=5 | Consistencia |

---

## Template de Teste (Padrao do Projeto)

```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("FichaVisibilidadeService — Testes de Integracao")
class FichaVisibilidadeServiceIntegrationTest {

    @Autowired
    private FichaVisibilidadeService fichaVisibilidadeService;

    @Autowired
    private FichaRepository fichaRepository;

    @Autowired
    private FichaVisibilidadeRepository fichaVisibilidadeRepository;

    // Dados de setup criados no @BeforeEach

    @BeforeEach
    void setUp() {
        // limpar dados manualmente antes de cada teste se necessario
        // criar jogo, mestre, jogador, NPC
    }

    @Test
    @DisplayName("deve revelar NPC para jogador especifico")
    void deveRevelarNpcParaJogadorEspecifico() {
        // Arrange: NPC criado, jogador participante aprovado
        // Act: fichaVisibilidadeService.atualizar(fichaId, request)
        // Assert: fichaVisibilidadeRepository.existsByFichaIdAndJogadorId == true
    }

    @Test
    @DisplayName("deve ser idempotente ao revelar NPC para jogador que ja tem acesso")
    void deveSerIdempotente() {
        // Arrange: revelar uma vez
        // Act: revelar novamente com mesmo jogador
        // Assert: apenas 1 registro em FichaVisibilidade
    }
}
```

---

## Criterios de Aceitacao da Task

- [ ] Todos os cenarios listados acima implementados e passando
- [ ] Nenhum teste usa `@MockBean` para FichaVisibilidadeRepository, ProspeccaoUsoRepository ou FichaVidaRepository (testes de integracao reais com H2)
- [ ] `@DisplayName` descritivo em todos os testes
- [ ] Padrao Arrange-Act-Assert seguido
- [ ] Contagem de testes do projeto cresce apos esta task (indicador de saude)
