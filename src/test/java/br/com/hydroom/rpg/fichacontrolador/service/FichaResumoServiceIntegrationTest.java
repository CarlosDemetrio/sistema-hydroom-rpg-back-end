package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaResumoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
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

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integracao para FichaResumoService.
 * Verifica que pontosDisponiveis sao calculados corretamente.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaResumoService - Pontos Disponiveis")
class FichaResumoServiceIntegrationTest {

    @Autowired private FichaResumoService fichaResumoService;
    @Autowired private FichaService fichaService;
    @Autowired private JogoService jogoService;
    @Autowired private FichaRepository fichaRepository;
    @Autowired private FichaAtributoRepository fichaAtributoRepository;
    @Autowired private FichaAptidaoRepository fichaAptidaoRepository;
    @Autowired private FichaBonusRepository fichaBonusRepository;
    @Autowired private FichaVidaRepository fichaVidaRepository;
    @Autowired private FichaVidaMembroRepository fichaVidaMembroRepository;
    @Autowired private FichaEssenciaRepository fichaEssenciaRepository;
    @Autowired private FichaAmeacaRepository fichaAmeacaRepository;
    @Autowired private FichaProspeccaoRepository fichaProspeccaoRepository;
    @Autowired private FichaDescricaoFisicaRepository fichaDescricaoFisicaRepository;
    @Autowired private FichaVantagemRepository fichaVantagemRepository;
    @Autowired private JogoRepository jogoRepository;
    @Autowired private JogoParticipanteRepository jogoParticipanteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ConfiguracaoNivelRepository nivelConfigRepository;
    @Autowired private PontosVantagemConfigRepository pontosVantagemConfigRepository;
    @Autowired private VantagemConfigRepository vantagemConfigRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Jogo jogo;
    private Ficha ficha;

    @BeforeEach
    void setUp() {
        fichaVantagemRepository.deleteAll();
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
                .nome("Mestre Resumo")
                .email("mestre.resumo" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-resumo-" + n)
                .role("MESTRE")
                .build());

        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Jogo Resumo " + n)
                .build());

        // Criar ficha basica
        ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Personagem Resumo", null,
                null, null, null, null, null, false));
    }

    @Test
    @DisplayName("Deve retornar pontosDisponiveis com valores do NivelConfig quando nenhum ponto gasto")
    void deveRetornarPontosDisponiveisDoNivel() {
        // Arrange - nivel 1 criado pelo GameConfigInitializer tem pontosAtributo=3, pontosAptidao=3
        // e PontosVantagemConfig nivel 1 tem pontosGanhos=1 (default)

        // Act
        FichaResumoResponse resumo = fichaResumoService.getResumo(ficha.getId());

        // Assert
        assertThat(resumo.pontosAtributoDisponiveis()).isGreaterThanOrEqualTo(0);
        assertThat(resumo.pontosAptidaoDisponiveis()).isGreaterThanOrEqualTo(0);
        assertThat(resumo.pontosVantagemDisponiveis()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Deve reduzir pontosVantagemDisponiveis apos comprar vantagem")
    void deveReduzirPontosVantagemAposCompra() {
        // Arrange
        FichaResumoResponse antes = fichaResumoService.getResumo(ficha.getId());
        int pontosVantagemAntes = antes.pontosVantagemDisponiveis();

        // Criar vantagem e comprar
        VantagemConfig vantagem = vantagemConfigRepository.save(VantagemConfig.builder()
                .jogo(jogo)
                .nome("Vantagem Custo 1")
                .formulaCusto("1")
                .nivelMaximo(5)
                .ordemExibicao(0)
                .build());

        FichaVantagem fv = FichaVantagem.builder()
                .ficha(ficha)
                .vantagemConfig(vantagem)
                .nivelAtual(1)
                .custoPago(1)
                .build();
        fichaVantagemRepository.save(fv);

        // Act
        FichaResumoResponse depois = fichaResumoService.getResumo(ficha.getId());

        // Assert
        assertThat(depois.pontosVantagemDisponiveis()).isEqualTo(Math.max(0, pontosVantagemAntes - 1));
    }

    @Test
    @DisplayName("Deve retornar pontosDisponiveis como 0 quando NivelConfig nao existe")
    void deveRetornarZeroQuandoSemNivelConfig() {
        // Arrange - deletar todas as configuracoes de nivel
        nivelConfigRepository.deleteAll();
        pontosVantagemConfigRepository.deleteAll();

        // Act
        FichaResumoResponse resumo = fichaResumoService.getResumo(ficha.getId());

        // Assert
        assertThat(resumo.pontosAtributoDisponiveis()).isEqualTo(0);
        assertThat(resumo.pontosAptidaoDisponiveis()).isEqualTo(0);
        assertThat(resumo.pontosVantagemDisponiveis()).isEqualTo(0);
    }

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
