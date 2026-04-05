package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
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
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
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

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de segurança para FichaPreviewService.
 *
 * <p>Valida que o endpoint de preview (POST /fichas/{id}/preview) respeita
 * as regras de controle de acesso — auditoria SP1-T18.</p>
 *
 * <p>Vulnerabilidade corrigida: antes da correção, qualquer jogador autenticado
 * podia simular o preview de qualquer ficha do sistema, incluindo fichas de
 * outros jogadores e NPCs, pois o service não verificava o acesso.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaPreviewService - Testes de Segurança (SP1-T18)")
class FichaPreviewServiceSecurityTest {

    @Autowired
    private FichaPreviewService fichaPreviewService;

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

    @BeforeEach
    void setUp() {
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
                .nome("Mestre Preview")
                .email("mestre.preview" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-preview-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador Preview")
                .email("jogador.preview" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-preview-" + n)
                .role("JOGADOR")
                .build());

        outroJogador = usuarioRepository.save(Usuario.builder()
                .nome("Outro Jogador Preview")
                .email("outro.preview" + n + "@test.com")
                .provider("google")
                .providerId("google-outro-preview-" + n)
                .role("JOGADOR")
                .build());

        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Jogo Preview " + n)
                .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(jogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());
    }

    @Test
    @DisplayName("Mestre pode simular preview de qualquer ficha")
    void mestrePodeSimularPreviewDeQualquerFicha() {
        // Arrange
        autenticarComo(mestre);
        Ficha fichaDoJogador = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Ficha do Jogador", jogador.getId(), null, null, null, null, null, false));

        // Act — Mestre simula preview da ficha do jogador
        var resultado = fichaPreviewService.simular(fichaDoJogador.getId(), null);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.fichaId()).isEqualTo(fichaDoJogador.getId());
    }

    @Test
    @DisplayName("Jogador pode simular preview de sua própria ficha")
    void jogadorPodeSimularPreviewDaSuaFicha() {
        // Arrange
        autenticarComo(mestre);
        Ficha minhaFicha = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Minha Ficha", jogador.getId(), null, null, null, null, null, false));

        // Act — Jogador simula preview da própria ficha
        autenticarComo(jogador);
        var resultado = fichaPreviewService.simular(minhaFicha.getId(), null);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.fichaId()).isEqualTo(minhaFicha.getId());
    }

    @Test
    @DisplayName("Jogador não pode simular preview de ficha de outro jogador (SP1-T18)")
    void jogadorNaoPodeSimularPreviewDeFichaDeOutroJogador() {
        // Arrange — Mestre cria ficha atribuída ao outroJogador
        autenticarComo(mestre);
        Ficha fichaDeOutro = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Ficha do Outro", outroJogador.getId(), null, null, null, null, null, false));

        // Act — jogador (participante diferente) tenta simular preview
        autenticarComo(jogador);

        // Assert — vulnerabilidade corrigida: jogador não pode acessar ficha alheia via preview
        assertThrows(ForbiddenException.class, () -> fichaPreviewService.simular(fichaDeOutro.getId(), null));
    }

    @Test
    @DisplayName("Jogador não pode simular preview de NPC (SP1-T18)")
    void jogadorNaoPodeSimularPreviewDeNpc() {
        // Arrange — Mestre cria NPC
        autenticarComo(mestre);
        Ficha npc = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "NPC Dragão", null, null, null, null, null, null, true));

        // Act — Jogador tenta simular preview do NPC
        autenticarComo(jogador);

        // Assert — NPC tem jogadorId=null, portanto jogador não é o dono
        assertThrows(ForbiddenException.class, () -> fichaPreviewService.simular(npc.getId(), null));
    }

    @Test
    @DisplayName("Mestre pode simular preview de NPC")
    void mestrePodeSimularPreviewDeNpc() {
        // Arrange
        autenticarComo(mestre);
        Ficha npc = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "NPC Goblin", null, null, null, null, null, null, true));

        // Act
        var resultado = fichaPreviewService.simular(npc.getId(), null);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.fichaId()).isEqualTo(npc.getId());
    }

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
