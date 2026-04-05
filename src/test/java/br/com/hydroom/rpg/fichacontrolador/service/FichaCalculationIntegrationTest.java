package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaPreviewRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.UpdateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaPreviewResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.BusinessException;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para o motor de cálculos da ficha.
 *
 * <p>Cobre:</p>
 * <ul>
 *   <li>Recálculo automático ao criar ficha</li>
 *   <li>Recálculo ao atualizar XP com avanço de nível</li>
 *   <li>Preview de cálculos sem persistir</li>
 *   <li>Validação de pontos de atributo excedidos</li>
 *   <li>Validação de limitador de atributo</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("Motor de Cálculos - Testes de Integração")
class FichaCalculationIntegrationTest {

    @Autowired
    private FichaService fichaService;

    @Autowired
    private FichaPreviewService fichaPreviewService;

    @Autowired
    private JogoService jogoService;

    @Autowired
    private FichaRepository fichaRepository;

    @Autowired
    private FichaAtributoRepository fichaAtributoRepository;

    @Autowired
    private FichaVidaRepository fichaVidaRepository;

    @Autowired
    private FichaEssenciaRepository fichaEssenciaRepository;

    @Autowired
    private FichaAmeacaRepository fichaAmeacaRepository;

    @Autowired
    private FichaBonusRepository fichaBonusRepository;

    @Autowired
    private FichaAptidaoRepository fichaAptidaoRepository;

    @Autowired
    private FichaVidaMembroRepository fichaVidaMembroRepository;

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

    @Autowired
    private ConfiguracaoAtributoRepository atributoConfigRepository;

    @Autowired
    private ConfiguracaoNivelRepository nivelConfigRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Jogo jogo;

    @BeforeEach
    void setUp() {
        // Limpar na ordem correta
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
        nivelConfigRepository.deleteAll();
        jogoParticipanteRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre Calc")
                .email("mestre.calc" + n + "@test.com")
                .provider("google")
                .providerId("google-calc-" + n)
                .role("MESTRE")
                .build());

        autenticarComo(mestre);

        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Jogo Motor Calculos " + n)
                .build());
    }

    // =========================================================
    // CENÁRIO 1: Criar ficha → totais inicializados em zero
    // =========================================================

    @Test
    @DisplayName("Ao criar ficha, atributo total deve ser calculado (base 0 + nivel 0 + outros 0 = 0)")
    void criarFicha_atributoTotalDeveSerCalculado() {
        // Arrange
        var request = new CreateFichaRequest(jogo.getId(), "Personagem Calc", null, null, null, null, null, null, false);

        // Act
        Ficha ficha = fichaService.criar(request);
        Long fichaId = ficha.getId();

        // Assert - atributos inicializados com total 0
        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaId(fichaId);
        assertThat(atributos).isNotEmpty();
        assertThat(atributos).allMatch(a -> a.getTotal() == 0);

        // Vida, essência e ameaça criadas
        assertThat(fichaVidaRepository.findByFichaId(fichaId)).isPresent();
        assertThat(fichaEssenciaRepository.findByFichaId(fichaId)).isPresent();
        assertThat(fichaAmeacaRepository.findByFichaId(fichaId)).isPresent();
    }

    // =========================================================
    // CENÁRIO 2: Atualizar base de atributo e recalcular
    // =========================================================

    @Test
    @DisplayName("Ao atualizar base de atributo diretamente, recalcular deve refletir novo total")
    void atualizarBaseAtributo_recalcularDeveRefletirNovoTotal() {
        // Arrange - usar XP suficiente para NivelConfig de nivel=5 (xp=15000, pontosAtributo=3 cada nível)
        // Como pontosAtributo é cumulativo precisamos ter total acumulado.
        // Nível 5 default tem pontosAtributo=3, mas a validação é pelo NivelConfig atual.
        // Vamos atualizar o NivelConfig de nivel=1 para ter pontosAtributo alto.
        NivelConfig nc1 = nivelConfigRepository.findByJogoIdAndNivel(jogo.getId(), 1).orElseThrow();
        nc1.setPontosAtributo(100); // pontos suficientes
        nc1.setLimitadorAtributo(999);
        nivelConfigRepository.save(nc1);

        var request = new CreateFichaRequest(jogo.getId(), "Heroi", null, null, null, null, null, null, false);
        Ficha ficha = fichaService.criar(request);
        Long fichaId = ficha.getId();

        // Avançar para nível 1 primeiro (xp=1000)
        fichaService.atualizar(fichaId, new UpdateFichaRequest(null, null, null, null, null, null, 1000L, null));

        // Buscar o primeiro atributo e mudar sua base
        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaId(fichaId);
        assertThat(atributos).isNotEmpty();

        FichaAtributo atributo = atributos.get(0);
        atributo.setBase(7);
        fichaAtributoRepository.save(atributo);

        // Act - atualizar a ficha para triggar recalcular
        var updateRequest = new UpdateFichaRequest(null, null, null, null, null, null, null, null);
        fichaService.atualizar(fichaId, updateRequest);

        // Assert - total deve ser recalculado: 7 + 0 + 0 = 7
        List<FichaAtributo> atributosApos = fichaAtributoRepository.findByFichaId(fichaId);
        FichaAtributo atributoAtualizado = atributosApos.stream()
                .filter(a -> a.getId().equals(atributo.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(atributoAtualizado.getTotal()).isEqualTo(7);
    }

    // =========================================================
    // CENÁRIO 3: Atualizar XP → nível atualizado via NivelConfig
    // =========================================================

    @Test
    @DisplayName("Atualizar XP deve avançar nível automaticamente com base em NivelConfig")
    void atualizarXP_deveAvancarNivel_comNivelConfig() {
        // Arrange - usar NivelConfigs já criados pelo GameConfigInitializerService
        // Nível 5 requer 15000 XP conforme DefaultGameConfigProviderImpl
        var request = new CreateFichaRequest(jogo.getId(), "Guerreiro XP", null, null, null, null, null, null, false);
        Ficha ficha = fichaService.criar(request);
        assertThat(ficha.getNivel()).isEqualTo(1); // nível inicial (xp=0, nivel0 < 1, mantém 1)

        // Act - dar XP suficiente para nível 5 (xpNecessaria=15000)
        var updateRequest = new UpdateFichaRequest(null, null, null, null, null, null, 15000L, null);
        Ficha fichaAtualizada = fichaService.atualizar(ficha.getId(), updateRequest);

        // Assert
        assertThat(fichaAtualizada.getXp()).isEqualTo(15000L);
        assertThat(fichaAtualizada.getNivel()).isEqualTo(5);
    }

    @Test
    @DisplayName("Atualizar XP para 1000 deve ser nível 1 conforme NivelConfig padrão")
    void atualizarXP_deveAvancarParaNivel1_com1000xp() {
        // Arrange - NivelConfig padrão: nivel=1 requer xp=1000
        var request = new CreateFichaRequest(jogo.getId(), "Iniciante", null, null, null, null, null, null, false);
        Ficha ficha = fichaService.criar(request);

        // Act
        var updateRequest = new UpdateFichaRequest(null, null, null, null, null, null, 1000L, null);
        Ficha fichaAtualizada = fichaService.atualizar(ficha.getId(), updateRequest);

        // Assert - xp=1000 exatamente = nível 1
        assertThat(fichaAtualizada.getNivel()).isEqualTo(1);
        assertThat(fichaAtualizada.getXp()).isEqualTo(1000L);
    }

    // =========================================================
    // CENÁRIO 4: Preview → valores em memória, original não mudou
    // =========================================================

    @Test
    @DisplayName("Preview deve retornar valores recalculados sem persistir")
    void preview_deveRetornarValoresRecalculados_semPersistir() {
        // Arrange
        var request = new CreateFichaRequest(jogo.getId(), "Preview Test", null, null, null, null, null, null, false);
        Ficha ficha = fichaService.criar(request);
        Long fichaId = ficha.getId();

        // Pegar o primeiro atributo
        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaId(fichaId);
        assertThat(atributos).isNotEmpty();
        FichaAtributo atributo = atributos.get(0);
        Integer baseOriginal = atributo.getBase(); // deve ser 0

        // Act - simular mudança de base para 10
        FichaPreviewRequest previewRequest = new FichaPreviewRequest(
                Map.of(atributo.getId(), 10),
                null,
                null
        );
        FichaPreviewResponse preview = fichaPreviewService.simular(fichaId, previewRequest);

        // Assert - preview mostra total calculado com nova base
        FichaPreviewResponse.AtributoPreview atributoPrev = preview.atributos().stream()
                .filter(a -> a.fichaAtributoId().equals(atributo.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(atributoPrev.base()).isEqualTo(10);
        assertThat(atributoPrev.total()).isEqualTo(10); // 10 + 0 + 0

        // Assert - original não mudou no banco
        FichaAtributo atributoOriginal = fichaAtributoRepository.findById(atributo.getId()).orElseThrow();
        assertThat(atributoOriginal.getBase()).isEqualTo(baseOriginal);
        assertThat(atributoOriginal.getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Preview com nova XP deve simular novo nível sem persistir")
    void preview_comNovaXP_deveSimularNovoNivel() {
        // Arrange - usar NivelConfigs padrão do jogo
        // nivel=3 requer 6000 XP conforme DefaultGameConfigProviderImpl
        var request = new CreateFichaRequest(jogo.getId(), "Preview XP Test", null, null, null, null, null, null, false);
        Ficha ficha = fichaService.criar(request);
        assertThat(ficha.getNivel()).isEqualTo(1);

        // Act - preview com XP para nível 3 (xpNecessaria=6000)
        FichaPreviewRequest previewRequest = new FichaPreviewRequest(null, null, 6000L);
        FichaPreviewResponse preview = fichaPreviewService.simular(ficha.getId(), previewRequest);

        // Assert - preview mostra nível 3
        assertThat(preview.nivel()).isEqualTo(3);
        assertThat(preview.xp()).isEqualTo(6000L);

        // Assert - ficha original não mudou
        Ficha fichaOriginal = fichaRepository.findById(ficha.getId()).orElseThrow();
        assertThat(fichaOriginal.getNivel()).isEqualTo(1);
        assertThat(fichaOriginal.getXp()).isEqualTo(0L);
    }

    // =========================================================
    // CENÁRIO 5: Validação → atributo acima do limitador
    // =========================================================

    @Test
    @DisplayName("Atualizar com atributo acima do limitador deve lançar BusinessException")
    void atualizarComAtributoAcimaDoLimitador_deveLancarBusinessException() {
        // Arrange - atualizar NivelConfig do nível 1 para ter limitador baixo
        // O NivelConfig de nivel=1 tem xpNecessaria=1000 (padrão), precisamos que a ficha esteja nesse nível
        NivelConfig nivelConfig1 = nivelConfigRepository.findByJogoIdAndNivel(jogo.getId(), 1).orElseThrow();
        nivelConfig1.setLimitadorAtributo(5); // máximo 5 por atributo
        nivelConfig1.setPontosAtributo(100);  // pontos suficientes para não conflitar com outra validação
        nivelConfigRepository.save(nivelConfig1);

        // Criar ficha e atualizar para nível 1 (xp=1000)
        var request = new CreateFichaRequest(jogo.getId(), "Heroi Limitado", null, null, null, null, null, null, false);
        Ficha ficha = fichaService.criar(request);
        Long fichaId = ficha.getId();

        // Atualizar XP para ficar no nível 1
        fichaService.atualizar(fichaId, new UpdateFichaRequest(null, null, null, null, null, null, 1000L, null));

        // Colocar atributo com valor acima do limitador
        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaId(fichaId);
        FichaAtributo atributo = atributos.get(0);
        atributo.setBase(8); // acima do limitador (5)
        fichaAtributoRepository.save(atributo);

        // Act & Assert
        var updateRequest = new UpdateFichaRequest(null, null, null, null, null, null, null, null);
        assertThrows(BusinessException.class,
                () -> fichaService.atualizar(fichaId, updateRequest));
    }

    // =========================================================
    // CENÁRIO 6: Validação → pontos de atributo excedidos
    // =========================================================

    @Test
    @DisplayName("Atualizar com pontos de atributo excedidos deve lançar BusinessException")
    void atualizarComPontosAtributoExcedidos_deveLancarBusinessException() {
        // Arrange - atualizar NivelConfig do nível 1 para ter pontos limitados
        NivelConfig nivelConfig1 = nivelConfigRepository.findByJogoIdAndNivel(jogo.getId(), 1).orElseThrow();
        nivelConfig1.setPontosAtributo(3); // só 3 pontos totais
        nivelConfig1.setLimitadorAtributo(999); // sem limitador de atributo individual
        nivelConfigRepository.save(nivelConfig1);

        // Criar ficha e atualizar para nível 1 (xp=1000)
        var request = new CreateFichaRequest(jogo.getId(), "Heroi Sem Pontos", null, null, null, null, null, null, false);
        Ficha ficha = fichaService.criar(request);
        Long fichaId = ficha.getId();

        // Atualizar XP para ficar no nível 1
        fichaService.atualizar(fichaId, new UpdateFichaRequest(null, null, null, null, null, null, 1000L, null));

        // Distribuir mais pontos do que o disponível
        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaId(fichaId);
        // Colocar 2 pontos em cada um dos primeiros 2 atributos = 4 pontos > 3 disponíveis
        if (atributos.size() >= 2) {
            atributos.get(0).setBase(2);
            atributos.get(1).setBase(2);
            fichaAtributoRepository.saveAll(List.of(atributos.get(0), atributos.get(1)));

            // Act & Assert
            var updateRequest = new UpdateFichaRequest(null, null, null, null, null, null, null, null);
            assertThrows(BusinessException.class,
                    () -> fichaService.atualizar(fichaId, updateRequest));
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
