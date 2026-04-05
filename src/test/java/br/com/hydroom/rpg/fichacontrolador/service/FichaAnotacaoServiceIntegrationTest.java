package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarAnotacaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoAnotacao;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para FichaAnotacaoService.
 *
 * <p>Cobre regras de visibilidade e controle de acesso:</p>
 * <ul>
 *   <li>MESTRE vê todas as anotações (tipo JOGADOR e MESTRE)</li>
 *   <li>JOGADOR vê apenas as próprias + anotações do Mestre com visivelParaJogador=true</li>
 *   <li>JOGADOR só cria anotações do tipo JOGADOR</li>
 *   <li>MESTRE pode deletar qualquer anotação; JOGADOR apenas as suas</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaAnotacaoService - Testes de Integração")
class FichaAnotacaoServiceIntegrationTest {

    @Autowired
    private FichaAnotacaoService fichaAnotacaoService;

    @Autowired
    private FichaService fichaService;

    @Autowired
    private JogoService jogoService;

    @Autowired
    private FichaAnotacaoRepository fichaAnotacaoRepository;

    @Autowired
    private FichaRepository fichaRepository;

    @Autowired
    private FichaAtributoRepository fichaAtributoRepository;

    @Autowired
    private FichaAptidaoRepository fichaAptidaoRepository;

    @Autowired
    private FichaBonusRepository fichaBonusRepository;

    @Autowired
    private FichaVidaRepository fichaVidaRepository;

    @Autowired
    private FichaVidaMembroRepository fichaVidaMembroRepository;

    @Autowired
    private FichaEssenciaRepository fichaEssenciaRepository;

    @Autowired
    private FichaAmeacaRepository fichaAmeacaRepository;

    @Autowired
    private FichaProspeccaoRepository fichaProspeccaoRepository;

    @Autowired
    private FichaDescricaoFisicaRepository fichaDescricaoFisicaRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Usuario jogador;
    private Usuario outroJogador;
    private Jogo jogo;
    private Ficha fichaDoJogador;

    @BeforeEach
    void setUp() {
        // Limpar na ordem correta (dependências primeiro)
        fichaAnotacaoRepository.deleteAll();
        fichaDescricaoFisicaRepository.deleteAll();
        fichaProspeccaoRepository.deleteAll();
        fichaAmeacaRepository.deleteAll();
        fichaEssenciaRepository.deleteAll();
        fichaVidaMembroRepository.deleteAll();
        fichaVidaRepository.deleteAll();
        fichaBonusRepository.deleteAll();
        fichaAptidaoRepository.deleteAll();
        fichaAtributoRepository.deleteAll();
        fichaRepository.deleteAll();
        jogoParticipanteRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre Anotacao")
                .email("mestre.anotacao" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-anot-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador Anotacao")
                .email("jogador.anotacao" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-anot-" + n)
                .role("JOGADOR")
                .build());

        outroJogador = usuarioRepository.save(Usuario.builder()
                .nome("Outro Jogador")
                .email("outro.jogador" + n + "@test.com")
                .provider("google")
                .providerId("google-outro-jogador-" + n)
                .role("JOGADOR")
                .build());

        // Criar jogo como Mestre (inicializa configs automaticamente)
        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Campanha Anotacao " + n)
                .build());

        // Adicionar jogador como participante aprovado
        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(jogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        // Adicionar outroJogador como participante aprovado
        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(outroJogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        // Criar ficha do jogador como Mestre
        fichaDoJogador = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Personagem do Jogador",
                        jogador.getId(), null, null, null, null, null, false));
    }

    // =========================================================
    // TESTES DE LISTAGEM
    // =========================================================

    @Test
    @DisplayName("Mestre deve ver todas as anotações da ficha (tipo JOGADOR e MESTRE)")
    void deveListarAnotacoesParaMestre() {
        // Arrange - criar anotação do JOGADOR
        autenticarComo(jogador);
        fichaAnotacaoService.criar(fichaDoJogador.getId(),
                new CriarAnotacaoRequest("Nota do Jogador", "Conteúdo do jogador", TipoAnotacao.JOGADOR, false),
                jogador.getId());

        // Criar anotação do MESTRE (não visível para jogador)
        autenticarComo(mestre);
        fichaAnotacaoService.criar(fichaDoJogador.getId(),
                new CriarAnotacaoRequest("Nota Secreta do Mestre", "Estratégia secreta", TipoAnotacao.MESTRE, false),
                mestre.getId());

        // Act - listar como MESTRE
        autenticarComo(mestre);
        List<FichaAnotacao> anotacoes = fichaAnotacaoService.listar(fichaDoJogador.getId());

        // Assert - Mestre vê as 2 anotações
        assertThat(anotacoes).hasSize(2);
        assertThat(anotacoes).extracting(FichaAnotacao::getTipoAnotacao)
                .containsExactlyInAnyOrder(TipoAnotacao.JOGADOR, TipoAnotacao.MESTRE);
    }

    @Test
    @DisplayName("Jogador deve ver apenas as próprias anotações e as do Mestre marcadas como visíveis")
    void deveListarApenasAnotacoesVisiveisParaJogador() {
        // Arrange - anotação do próprio jogador
        autenticarComo(jogador);
        fichaAnotacaoService.criar(fichaDoJogador.getId(),
                new CriarAnotacaoRequest("Minha Nota", "Conteúdo pessoal", TipoAnotacao.JOGADOR, false),
                jogador.getId());

        // Anotação do Mestre VISÍVEL para jogador
        autenticarComo(mestre);
        fichaAnotacaoService.criar(fichaDoJogador.getId(),
                new CriarAnotacaoRequest("Dica do Mestre", "Dica útil", TipoAnotacao.MESTRE, true),
                mestre.getId());

        // Anotação do Mestre INVISÍVEL para jogador
        fichaAnotacaoService.criar(fichaDoJogador.getId(),
                new CriarAnotacaoRequest("Nota Secreta", "Ninguém pode ver", TipoAnotacao.MESTRE, false),
                mestre.getId());

        // Act - listar como JOGADOR
        autenticarComo(jogador);
        List<FichaAnotacao> anotacoes = fichaAnotacaoService.listar(fichaDoJogador.getId());

        // Assert - Jogador vê 2: a própria + a do Mestre marcada como visível
        assertThat(anotacoes).hasSize(2);
        assertThat(anotacoes).extracting(FichaAnotacao::getTitulo)
                .containsExactlyInAnyOrder("Minha Nota", "Dica do Mestre");
        assertThat(anotacoes).noneMatch(a -> "Nota Secreta".equals(a.getTitulo()));
    }

    @Test
    @DisplayName("Jogador não deve ver anotações de outro jogador na mesma ficha")
    void deveListarApenasPropriasSemAnotacoesDeOutroJogador() {
        // Arrange - criar ficha atribuída a outroJogador
        autenticarComo(mestre);
        Ficha fichaOutroJogador = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Personagem do Outro",
                        outroJogador.getId(), null, null, null, null, null, false));

        // Anotação do próprio jogador na SUA ficha
        autenticarComo(jogador);
        fichaAnotacaoService.criar(fichaDoJogador.getId(),
                new CriarAnotacaoRequest("Nota Própria", "Conteúdo", TipoAnotacao.JOGADOR, false),
                jogador.getId());

        // Act - jogador tenta listar a ficha de outro jogador
        autenticarComo(jogador);

        // Assert - deve lançar ForbiddenException
        assertThrows(ForbiddenException.class,
                () -> fichaAnotacaoService.listar(fichaOutroJogador.getId()));
    }

    // =========================================================
    // TESTES DE CRIAÇÃO
    // =========================================================

    @Test
    @DisplayName("Jogador deve criar anotação do tipo JOGADOR com sucesso")
    void deveCriarAnotacaoComoJogador() {
        // Arrange
        autenticarComo(jogador);
        var request = new CriarAnotacaoRequest(
                "Observação do Personagem",
                "O personagem observou algo interessante",
                TipoAnotacao.JOGADOR,
                false);

        // Act
        FichaAnotacao anotacao = fichaAnotacaoService.criar(fichaDoJogador.getId(), request, jogador.getId());

        // Assert
        assertThat(anotacao.getId()).isNotNull();
        assertThat(anotacao.getTitulo()).isEqualTo("Observação do Personagem");
        assertThat(anotacao.getTipoAnotacao()).isEqualTo(TipoAnotacao.JOGADOR);
        assertThat(anotacao.getAutor().getId()).isEqualTo(jogador.getId());
        assertThat(anotacao.getVisivelParaJogador()).isFalse();
    }

    @Test
    @DisplayName("Mestre deve criar anotação do tipo MESTRE com visivelParaJogador=true")
    void deveCriarAnotacaoComoMestre() {
        // Arrange
        autenticarComo(mestre);
        var request = new CriarAnotacaoRequest(
                "Missão Secreta",
                "O personagem deve investigar a torre",
                TipoAnotacao.MESTRE,
                true);

        // Act
        FichaAnotacao anotacao = fichaAnotacaoService.criar(fichaDoJogador.getId(), request, mestre.getId());

        // Assert
        assertThat(anotacao.getId()).isNotNull();
        assertThat(anotacao.getTitulo()).isEqualTo("Missão Secreta");
        assertThat(anotacao.getTipoAnotacao()).isEqualTo(TipoAnotacao.MESTRE);
        assertThat(anotacao.getAutor().getId()).isEqualTo(mestre.getId());
        assertThat(anotacao.getVisivelParaJogador()).isTrue();
    }

    @Test
    @DisplayName("Jogador não pode criar anotação do tipo MESTRE")
    void deveImpedirJogadorDeCriarAnotacaoDoTipoMestre() {
        // Arrange
        autenticarComo(jogador);
        var request = new CriarAnotacaoRequest(
                "Tentativa Indevida",
                "Tentando criar anotação de Mestre",
                TipoAnotacao.MESTRE,
                false);

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> fichaAnotacaoService.criar(fichaDoJogador.getId(), request, jogador.getId()));
    }

    @Test
    @DisplayName("Jogador não pode criar anotação em ficha de outro jogador")
    void deveImpedirJogadorDeCriarAnotacaoEmFichaDeOutro() {
        // Arrange - criar ficha para outroJogador
        autenticarComo(mestre);
        Ficha fichaOutroJogador = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Personagem Alheio",
                        outroJogador.getId(), null, null, null, null, null, false));

        // Act - jogador tenta criar anotação em ficha que não é dele
        autenticarComo(jogador);
        var request = new CriarAnotacaoRequest("Nota Invasiva", "Conteúdo", TipoAnotacao.JOGADOR, false);

        // Assert
        assertThrows(ForbiddenException.class,
                () -> fichaAnotacaoService.criar(fichaOutroJogador.getId(), request, jogador.getId()));
    }

    // =========================================================
    // TESTES DE DELEÇÃO
    // =========================================================

    @Test
    @DisplayName("Jogador não pode deletar anotação de outro jogador")
    void deveImpedirJogadorDeDeletarAnotacaoDeOutro() {
        // Arrange - criar ficha para outroJogador e sua anotação
        autenticarComo(mestre);
        Ficha fichaOutroJogador = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Personagem Alheio",
                        outroJogador.getId(), null, null, null, null, null, false));

        // outroJogador cria uma anotação na sua ficha
        autenticarComo(outroJogador);
        FichaAnotacao anotacaoDoOutro = fichaAnotacaoService.criar(fichaOutroJogador.getId(),
                new CriarAnotacaoRequest("Nota do Outro", "Conteúdo", TipoAnotacao.JOGADOR, false),
                outroJogador.getId());

        // Act - jogador tenta deletar anotação de outroJogador
        autenticarComo(jogador);

        // Assert - deve lançar ForbiddenException
        assertThrows(ForbiddenException.class,
                () -> fichaAnotacaoService.deletar(fichaOutroJogador.getId(), anotacaoDoOutro.getId()));
    }

    @Test
    @DisplayName("Mestre pode deletar qualquer anotação da ficha")
    void deveMestrePoderDeletarQualquerAnotacao() {
        // Arrange - jogador cria anotação
        autenticarComo(jogador);
        FichaAnotacao anotacaoDoJogador = fichaAnotacaoService.criar(fichaDoJogador.getId(),
                new CriarAnotacaoRequest("Nota do Jogador", "Conteúdo", TipoAnotacao.JOGADOR, false),
                jogador.getId());

        // Act - Mestre deleta a anotação do jogador
        autenticarComo(mestre);
        fichaAnotacaoService.deletar(fichaDoJogador.getId(), anotacaoDoJogador.getId());

        // Assert - a anotação foi deletada (soft delete: deletedAt preenchido)
        FichaAnotacao deletada = fichaAnotacaoRepository.findById(anotacaoDoJogador.getId()).orElse(null);
        if (deletada != null) {
            assertThat(deletada.getDeletedAt()).isNotNull();
        }
        // Se null, o @SQLRestriction filtrou — comportamento correto de soft delete
    }

    @Test
    @DisplayName("Jogador pode deletar suas próprias anotações")
    void deveJogadorPoderDeletarSuaAnotacao() {
        // Arrange - jogador cria uma anotação
        autenticarComo(jogador);
        FichaAnotacao anotacao = fichaAnotacaoService.criar(fichaDoJogador.getId(),
                new CriarAnotacaoRequest("Anotação Própria", "Conteúdo", TipoAnotacao.JOGADOR, false),
                jogador.getId());

        // Act - jogador deleta sua própria anotação
        fichaAnotacaoService.deletar(fichaDoJogador.getId(), anotacao.getId());

        // Assert - a anotação foi deletada (soft delete)
        FichaAnotacao deletada = fichaAnotacaoRepository.findById(anotacao.getId()).orElse(null);
        if (deletada != null) {
            assertThat(deletada.getDeletedAt()).isNotNull();
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
