package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarVisibilidadeRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaVisibilidadeResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaAmeacaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaAptidaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaBonusRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaDescricaoFisicaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaEssenciaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaProspeccaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVidaMembroRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVidaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVisibilidadeRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ProspeccaoUsoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
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
 * Testes de integração para FichaVisibilidadeService.
 *
 * <p>Cobre:</p>
 * <ul>
 *   <li>Mestre revela NPC para jogador específico</li>
 *   <li>Idempotência: revelar para jogador que já tem acesso não duplica</li>
 *   <li>Revogar acesso de jogador</li>
 *   <li>Revelar ficha de jogador (isNpc=false) retorna ValidationException</li>
 *   <li>Revelar para jogador não participante retorna ValidationException</li>
 *   <li>jogadorTemAcessoStats refletido no serviço</li>
 *   <li>NPCs com visivelGlobalmente=true aparecem na listagem para Jogadores</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaVisibilidadeService — Testes de Integração")
class FichaVisibilidadeServiceIntegrationTest {

    @Autowired
    private FichaVisibilidadeService fichaVisibilidadeService;

    @Autowired
    private FichaService fichaService;

    @Autowired
    private JogoService jogoService;

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
    private ProspeccaoUsoRepository prospeccaoUsoRepository;

    @Autowired
    private FichaDescricaoFisicaRepository fichaDescricaoFisicaRepository;

    @Autowired
    private FichaVisibilidadeRepository fichaVisibilidadeRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Usuario jogador;
    private Usuario jogadorNaoParticipante;
    private Jogo jogo;
    private Ficha npc;
    private Ficha fichaJogador;

    @BeforeEach
    void setUp() {
        prospeccaoUsoRepository.deleteAll();
        fichaDescricaoFisicaRepository.deleteAll();
        fichaProspeccaoRepository.deleteAll();
        fichaAmeacaRepository.deleteAll();
        fichaEssenciaRepository.deleteAll();
        fichaVidaMembroRepository.deleteAll();
        fichaVidaRepository.deleteAll();
        fichaBonusRepository.deleteAll();
        fichaAptidaoRepository.deleteAll();
        fichaAtributoRepository.deleteAll();
        fichaVisibilidadeRepository.deleteAll();
        fichaRepository.deleteAll();
        jogoParticipanteRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre Vis " + n)
                .email("mestre-vis" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-vis-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador Vis " + n)
                .email("jogador-vis" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-vis-" + n)
                .role("JOGADOR")
                .build());

        jogadorNaoParticipante = usuarioRepository.save(Usuario.builder()
                .nome("Forasteiro " + n)
                .email("forasteiro" + n + "@test.com")
                .provider("google")
                .providerId("google-forasteiro-" + n)
                .role("JOGADOR")
                .build());

        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Jogo Vis " + n)
                .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(jogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        npc = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Goblin Chefe", null, null, null, null, null, null, true));

        fichaJogador = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Herói do Povo", jogador.getId(), null, null, null, null, null, false));
    }

    // =========================================================
    // REVELAR NPC
    // =========================================================

    @Test
    @DisplayName("Mestre revela NPC para jogador específico")
    void deveRevelarNpcParaJogadorEspecifico() {
        // Arrange
        autenticarComo(mestre);

        // Act
        FichaVisibilidadeResponse response = fichaVisibilidadeService.atualizar(
                npc.getId(),
                new AtualizarVisibilidadeRequest(List.of(jogador.getId()), false));

        // Assert
        assertThat(response.fichaId()).isEqualTo(npc.getId());
        assertThat(response.jogadoresComAcesso()).hasSize(1);
        assertThat(response.jogadoresComAcesso().get(0).jogadorId()).isEqualTo(jogador.getId());
        assertThat(fichaVisibilidadeRepository.existsByFichaIdAndJogadorId(npc.getId(), jogador.getId())).isTrue();
    }

    @Test
    @DisplayName("Idempotência: revelar para jogador que já tem acesso não duplica o registro")
    void deveSerIdempotenteCasoJogadorJaTenhaAcesso() {
        // Arrange: revelar uma vez
        autenticarComo(mestre);
        fichaVisibilidadeService.atualizar(npc.getId(),
                new AtualizarVisibilidadeRequest(List.of(jogador.getId()), false));

        // Act: revelar novamente
        fichaVisibilidadeService.atualizar(npc.getId(),
                new AtualizarVisibilidadeRequest(List.of(jogador.getId()), false));

        // Assert: apenas 1 registro ativo
        assertThat(fichaVisibilidadeRepository.findByFichaId(npc.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Revogar acesso de jogador remove o registro de visibilidade")
    void deveRevogarAcessoDeJogador() {
        // Arrange
        autenticarComo(mestre);
        fichaVisibilidadeService.atualizar(npc.getId(),
                new AtualizarVisibilidadeRequest(List.of(jogador.getId()), false));

        // Act
        fichaVisibilidadeService.revogar(npc.getId(), jogador.getId());

        // Assert
        assertThat(fichaVisibilidadeRepository.existsByFichaIdAndJogadorId(npc.getId(), jogador.getId())).isFalse();
    }

    @Test
    @DisplayName("Revelar ficha de jogador (isNpc=false) retorna ValidationException")
    void deveLancarExcecaoAoRevelarFichaDeJogador() {
        // Arrange
        autenticarComo(mestre);

        // Act & Assert
        assertThrows(ValidationException.class, () ->
                fichaVisibilidadeService.atualizar(fichaJogador.getId(),
                        new AtualizarVisibilidadeRequest(List.of(jogador.getId()), false)));
    }

    @Test
    @DisplayName("Revelar para jogador não participante retorna ValidationException")
    void deveLancarExcecaoParaJogadorNaoParticipante() {
        // Arrange
        autenticarComo(mestre);

        // Act & Assert: jogadorNaoParticipante não é participante do jogo
        assertThrows(ValidationException.class, () ->
                fichaVisibilidadeService.atualizar(npc.getId(),
                        new AtualizarVisibilidadeRequest(List.of(jogadorNaoParticipante.getId()), false)));
    }

    @Test
    @DisplayName("jogadorTemAcesso=true quando FichaVisibilidade ativa para o usuário")
    void deveRetornarTemAcessoQuandoVisibilidadeAtiva() {
        // Arrange
        autenticarComo(mestre);
        fichaVisibilidadeService.atualizar(npc.getId(),
                new AtualizarVisibilidadeRequest(List.of(jogador.getId()), false));

        // Assert: verificar via repositório (evita problema de L1 cache com temAcessoUsuarioAtual)
        // O método temAcesso(fichaId, jogadorId) verifica diretamente no repositório
        boolean temAcesso = fichaVisibilidadeService.temAcesso(npc.getId(), jogador.getId());
        assertThat(temAcesso).isTrue();
    }

    @Test
    @DisplayName("jogadorTemAcesso=false quando sem FichaVisibilidade")
    void deveRetornarSemAcessoQuandoSemVisibilidade() {
        // Act: verificar antes de qualquer visibilidade ser concedida
        boolean temAcesso = fichaVisibilidadeService.temAcesso(npc.getId(), jogador.getId());

        // Assert
        assertThat(temAcesso).isFalse();
    }

    @Test
    @DisplayName("NPC com visivelGlobalmente=true aparece na listagem para Jogadores")
    void deveListarNpcComVisivelGlobalmenteParaJogadores() {
        // Arrange
        autenticarComo(mestre);
        fichaService.atualizarVisivelGlobalmente(npc.getId(), true);

        // Act
        autenticarComo(jogador);
        var fichas = fichaService.listarComFiltros(jogo.getId(), null, null, null, null);

        // Assert: NPC com visivelGlobalmente=true está na listagem
        assertThat(fichas.stream().anyMatch(f -> f.getId().equals(npc.getId()))).isTrue();
    }

    @Test
    @DisplayName("NPC com visivelGlobalmente=false não aparece na listagem para Jogadores")
    void deveOcultarNpcSemVisivelGlobalmenteParaJogadores() {
        // Arrange: NPC com visivelGlobalmente=false (padrão)
        autenticarComo(mestre);
        fichaService.atualizarVisivelGlobalmente(npc.getId(), false);

        // Act
        autenticarComo(jogador);
        var fichas = fichaService.listarComFiltros(jogo.getId(), null, null, null, null);

        // Assert: NPC sem visivelGlobalmente não está na listagem do jogador
        assertThat(fichas.stream().noneMatch(f -> f.getId().equals(npc.getId()))).isTrue();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
