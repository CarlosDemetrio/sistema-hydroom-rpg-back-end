package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarVidaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaResumoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.FichaEssencia;
import br.com.hydroom.rpg.fichacontrolador.model.FichaVida;
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

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para FichaResumoResponse — campos essenciaAtual e essenciaTotal.
 *
 * <p>Confirma que GET /fichas/{id}/resumo retorna os dois campos de essência separadamente,
 * permitindo ao frontend renderizar a barra de essência como essenciaAtual/essenciaTotal
 * sem calcular nada no cliente.</p>
 *
 * <p>Este endpoint é a fonte autoritativa para vida e essência atuais.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaResumoResponse — Campos essenciaAtual e essenciaTotal")
class FichaEssenciaResumoIntegrationTest {

    @Autowired
    private FichaResumoService fichaResumoService;

    @Autowired
    private FichaVidaService fichaVidaService;

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
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Jogo jogo;
    private Ficha ficha;

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
        fichaRepository.deleteAll();
        jogoParticipanteRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre Ess " + n)
                .email("mestre-ess" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-ess-" + n)
                .role("MESTRE")
                .build());

        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Jogo Essencia " + n)
                .build());

        ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Personagem Ess", null, null, null, null, null, null, true));
    }

    // =========================================================
    // CAMPOS ESSENCIA NO RESUMO
    // =========================================================

    @Test
    @DisplayName("GET /resumo retorna essenciaAtual >= 0")
    void deveRetornarEssenciaAtualNoResumo() {
        // Act
        autenticarComo(mestre);
        FichaResumoResponse resumo = fichaResumoService.getResumo(ficha.getId());

        // Assert
        assertThat(resumo.essenciaAtual()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("GET /resumo retorna essenciaTotal >= 0")
    void deveRetornarEssenciaTotalNoResumo() {
        // Act
        autenticarComo(mestre);
        FichaResumoResponse resumo = fichaResumoService.getResumo(ficha.getId());

        // Assert
        assertThat(resumo.essenciaTotal()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Após atualizar essenciaAtual=5 via PUT /vida, GET /resumo retorna essenciaAtual=5")
    void deveRefletirEssenciaAtualAtualizadaNoResumo() {
        // Arrange: setar essenciaAtual para um valor conhecido
        autenticarComo(mestre);
        FichaEssencia essencia = fichaEssenciaRepository.findByFichaId(ficha.getId()).orElseThrow();
        // Setar total >= 5 para que essenciaAtual=5 seja válido
        if (essencia.getTotal() < 5) {
            essencia.setTotal(10);
            fichaEssenciaRepository.save(essencia);
        }
        FichaVida vida = fichaVidaRepository.findByFichaId(ficha.getId()).orElseThrow();

        // Act
        fichaVidaService.atualizarVida(ficha.getId(), new AtualizarVidaRequest(vida.getVidaAtual(), 5, null));

        // Assert
        FichaResumoResponse resumo = fichaResumoService.getResumo(ficha.getId());
        assertThat(resumo.essenciaAtual()).isEqualTo(5);
    }

    @Test
    @DisplayName("GET /resumo retorna vidaAtual separado de vidaTotal")
    void deveRetornarVidaAtualSeparadoDeVidaTotal() {
        // Arrange: reduzir a vida atual
        autenticarComo(mestre);
        FichaVida vida = fichaVidaRepository.findByFichaId(ficha.getId()).orElseThrow();
        int vidaTotal = vida.getVidaTotal();
        int vidaReduzida = Math.max(0, vidaTotal / 2);
        fichaVidaService.atualizarVida(ficha.getId(), new AtualizarVidaRequest(vidaReduzida, 0, null));

        // Act
        FichaResumoResponse resumo = fichaResumoService.getResumo(ficha.getId());

        // Assert
        assertThat(resumo.vidaAtual()).isEqualTo(vidaReduzida);
        assertThat(resumo.vidaTotal()).isEqualTo(vidaTotal);
        assertThat(resumo.vidaAtual()).isLessThanOrEqualTo(resumo.vidaTotal());
    }

    @Test
    @DisplayName("essenciaAtual deve ser menor ou igual a essenciaTotal no resumo")
    void essenciaAtualDeveSeMenorOuIgualATotal() {
        // Act
        autenticarComo(mestre);
        FichaResumoResponse resumo = fichaResumoService.getResumo(ficha.getId());

        // Assert: invariante de domínio
        assertThat(resumo.essenciaAtual()).isLessThanOrEqualTo(resumo.essenciaTotal());
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
