package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.ConcederXpRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.UpdateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaResumoResponse;
import br.com.hydroom.rpg.fichacontrolador.model.*;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para os bugs corrigidos no motor de cálculos da ficha (Spec-007 T0).
 *
 * <p>Cobre:</p>
 * <ul>
 *   <li>T0-01: ClasseBonus aplicado corretamente em FichaBonus.classe (GAP-CALC-01)</li>
 *   <li>T0-02: ClasseAptidaoBonus aplicado corretamente em FichaAptidao.classe (GAP-CALC-02)</li>
 *   <li>T0-03: RacaBonusAtributo positivo aplicado em FichaAtributo.outros (GAP-CALC-03)</li>
 *   <li>T0-04: RacaBonusAtributo negativo (penalidade) aplicado em FichaAtributo.outros</li>
 *   <li>T0-05: Idempotência — recalcular duas vezes não acumula bônus</li>
 *   <li>T0-06: Nível recalculado ao ganhar XP via endpoint /xp (GAP-CALC-06)</li>
 *   <li>T0-07: Nível não regride com XP insuficiente para próximo nível</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("Motor de Cálculos - Correções de Bugs (Spec-007 T0)")
class FichaCalculationServiceBugsIntegrationTest {

    @Autowired
    private FichaService fichaService;

    @Autowired
    private JogoService jogoService;

    @Autowired
    private FichaRepository fichaRepository;
    @Autowired
    private FichaAtributoRepository fichaAtributoRepository;
    @Autowired
    private FichaBonusRepository fichaBonusRepository;
    @Autowired
    private FichaAptidaoRepository fichaAptidaoRepository;
    @Autowired
    private FichaVidaRepository fichaVidaRepository;
    @Autowired
    private FichaEssenciaRepository fichaEssenciaRepository;
    @Autowired
    private FichaAmeacaRepository fichaAmeacaRepository;
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
    private ConfiguracaoRacaRepository racaRepository;
    @Autowired
    private ConfiguracaoClasseRepository classeRepository;
    @Autowired
    private BonusConfigRepository bonusConfigRepository;
    @Autowired
    private ConfiguracaoAptidaoRepository aptidaoConfigRepository;
    @Autowired
    private ConfiguracaoAtributoRepository atributoConfigRepository;
    @Autowired
    private ClasseBonusRepository classeBonusRepository;
    @Autowired
    private ClasseAptidaoBonusRepository classeAptidaoBonusRepository;
    @Autowired
    private ConfiguracaoNivelRepository nivelConfigRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
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
        nivelConfigRepository.deleteAll();
        jogoParticipanteRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre Bugs")
                .email("mestre.bugs" + n + "@test.com")
                .provider("google")
                .providerId("google-bugs-" + n)
                .role("MESTRE")
                .build());

        autenticarComo(mestre);

        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Jogo Bugs Calc " + n)
                .build());
    }

    // =========================================================
    // T0-01: ClasseBonus aplicado em FichaBonus.classe (GAP-CALC-01)
    // =========================================================

    @Test
    @DisplayName("T0-01: ClasseBonus com valorPorNivel=2 e nivel=5 deve resultar em FichaBonus.classe=10")
    void t001_classeBonus_deveSerAplicadoEmFichaBonus() {
        // Arrange: obter classe Guerreiro do jogo (criada pelo GameConfigInitializerService)
        ClassePersonagem guerreiro = classeRepository.findByJogoIdAndNomeIgnoreCase(jogo.getId(), "Guerreiro");
        assertThat(guerreiro).as("Classe Guerreiro deve existir após inicialização do jogo").isNotNull();

        // Buscar BonusConfig 'BBA' criado pelo GameConfigInitializerService
        BonusConfig bba = bonusConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).stream()
                .filter(b -> "BBA".equals(b.getSigla()))
                .findFirst()
                .orElseGet(() -> bonusConfigRepository.save(BonusConfig.builder()
                        .jogo(jogo)
                        .nome("B.B.A.")
                        .sigla("BBA")
                        .formulaBase("")
                        .ordemExibicao(1)
                        .build()));

        // Criar FichaBonus manualmente para o guerreiro ter esse bonus na ficha
        // e criar ClasseBonus: Guerreiro +2 por nível em B.B.A.
        ClasseBonus classeBonus = classeBonusRepository.save(ClasseBonus.builder()
                .classe(guerreiro)
                .bonus(bba)
                .valorPorNivel(new BigDecimal("2"))
                .build());

        // Criar ficha com classe=Guerreiro
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Guerreiro Teste", null, null, guerreiro.getId(),
                null, null, null, false));

        // Definir nivel=5 diretamente no banco
        ficha.setNivel(5);
        ficha = fichaRepository.save(ficha);

        // Act: recalcular via atualizar
        fichaService.atualizar(ficha.getId(), new UpdateFichaRequest(
                null, null, null, null, null, null, null, null));

        // Assert: FichaBonus de B.B.A. deve ter classe=10 (2 * 5)
        List<FichaBonus> bonusList = fichaBonusRepository.findByFichaIdWithConfig(ficha.getId());
        FichaBonus alvo = bonusList.stream()
                .filter(b -> b.getBonusConfig() != null
                        && b.getBonusConfig().getId().equals(bba.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("FichaBonus para B.B.A. não encontrado"));

        assertThat(alvo.getClasse())
                .as("ClasseBonus.valorPorNivel(2) * nivel(5) deve resultar em classe=10")
                .isEqualTo(10);
        assertThat(alvo.getTotal())
                .as("Total deve incluir os 10 pontos de classe")
                .isGreaterThanOrEqualTo(10);
    }

    // =========================================================
    // T0-02: ClasseAptidaoBonus aplicado em FichaAptidao.classe (GAP-CALC-02)
    // =========================================================

    @Test
    @DisplayName("T0-02: ClasseAptidaoBonus com bonus=3 deve resultar em FichaAptidao.classe=3")
    void t002_classeAptidaoBonus_deveSerAplicadoEmFichaAptidao() {
        // Arrange: usar primeira classe disponível
        ClassePersonagem primeiraClasse = classeRepository.findAll().stream()
                .filter(c -> c.getJogo().getId().equals(jogo.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhuma classe encontrada no jogo"));

        // Usar primeira aptidão disponível
        AptidaoConfig primeiraAptidao = aptidaoConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId())
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhuma AptidaoConfig encontrada"));

        // Criar ClasseAptidaoBonus: +3 em primeiraAptidao
        classeAptidaoBonusRepository.save(ClasseAptidaoBonus.builder()
                .classe(primeiraClasse)
                .aptidao(primeiraAptidao)
                .bonus(3)
                .build());

        // Criar ficha com a primeira classe
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Aptidao Bonus Teste", null, null, primeiraClasse.getId(),
                null, null, null, false));

        // Act: recalcular via atualizar
        fichaService.atualizar(ficha.getId(), new UpdateFichaRequest(
                null, null, null, null, null, null, null, null));

        // Assert: FichaAptidao deve ter classe=3
        List<FichaAptidao> aptidoes = fichaAptidaoRepository.findByFichaIdWithConfig(ficha.getId());
        FichaAptidao alvo = aptidoes.stream()
                .filter(a -> a.getAptidaoConfig() != null
                        && a.getAptidaoConfig().getId().equals(primeiraAptidao.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("FichaAptidao não encontrada"));

        assertThat(alvo.getClasse())
                .as("ClasseAptidaoBonus.bonus(3) deve resultar em aptidao.classe=3")
                .isEqualTo(3);
        assertThat(alvo.getTotal())
                .as("Total deve incluir os 3 pontos de classe")
                .isGreaterThanOrEqualTo(3);
    }

    // =========================================================
    // T0-03: RacaBonusAtributo positivo aplicado (GAP-CALC-03)
    // =========================================================

    @Test
    @DisplayName("T0-03: Raça Anakarys com bonus=+3 em AGI deve resultar em FichaAtributo.outros=3")
    void t003_racaBonusAtributo_positivo_deveSerAplicadoEmFichaAtributoOutros() {
        // Arrange: Anakarys já existe com AGI+3 configurado pelo DefaultGameConfigProvider
        Raca anakarys = racaRepository.findByJogoIdAndNomeIgnoreCase(jogo.getId(), "Anakarys");
        assertThat(anakarys).as("Raça Anakarys deve existir após inicialização do jogo").isNotNull();

        AtributoConfig agi = atributoConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId())
                .stream()
                .filter(a -> "AGI".equalsIgnoreCase(a.getAbreviacao()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("AtributoConfig AGI não encontrado"));

        // Criar ficha com raca=Anakarys
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Anakarys Teste", null, anakarys.getId(), null, null, null, null, false));

        // Act: recalcular via atualizar
        fichaService.atualizar(ficha.getId(), new UpdateFichaRequest(
                null, null, null, null, null, null, null, null));

        // Assert: FichaAtributo.AGI.outros deve ser 3 (bônus da raça Anakarys)
        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaIdWithConfig(ficha.getId());
        FichaAtributo atributoAgi = atributos.stream()
                .filter(a -> a.getAtributoConfig() != null
                        && a.getAtributoConfig().getId().equals(agi.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("FichaAtributo AGI não encontrado"));

        assertThat(atributoAgi.getOutros())
                .as("RacaBonusAtributo Anakarys+3 em AGI deve ser aplicado em outros=3")
                .isEqualTo(3);
        assertThat(atributoAgi.getTotal())
                .as("Total de AGI deve ser 3 (base=0 + nivel=0 + outros=3)")
                .isEqualTo(3);
    }

    // =========================================================
    // T0-04: RacaBonusAtributo negativo (penalidade)
    // =========================================================

    @Test
    @DisplayName("T0-04: Raça Ikaruz com penalidade=-9 em VIG deve resultar em FichaAtributo.outros=-9")
    void t004_racaBonusAtributo_negativo_deveAplicarPenalidade() {
        // Arrange: Ikaruz tem VIG-9 configurado pelo DefaultGameConfigProvider
        Raca ikaruz = racaRepository.findByJogoIdAndNomeIgnoreCase(jogo.getId(), "Ikaruz");
        assertThat(ikaruz).as("Raça Ikaruz deve existir após inicialização do jogo").isNotNull();

        AtributoConfig vig = atributoConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId())
                .stream()
                .filter(a -> "VIG".equalsIgnoreCase(a.getAbreviacao()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("AtributoConfig VIG não encontrado"));

        // Criar ficha com raca=Ikaruz
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Ikaruz Penalidade", null, ikaruz.getId(), null, null, null, null, false));

        // Act: recalcular via atualizar
        fichaService.atualizar(ficha.getId(), new UpdateFichaRequest(
                null, null, null, null, null, null, null, null));

        // Assert: FichaAtributo.VIG.outros deve ser -9 (penalidade da raça Ikaruz)
        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaIdWithConfig(ficha.getId());
        FichaAtributo atributoVig = atributos.stream()
                .filter(a -> a.getAtributoConfig() != null
                        && a.getAtributoConfig().getId().equals(vig.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("FichaAtributo VIG não encontrado"));

        assertThat(atributoVig.getOutros())
                .as("RacaBonusAtributo Ikaruz-9 em VIG deve ser aplicado como penalidade outros=-9")
                .isEqualTo(-9);
        assertThat(atributoVig.getTotal())
                .as("Total de VIG deve ser -9 (base=0 + nivel=0 + outros=-9)")
                .isEqualTo(-9);
    }

    // =========================================================
    // T0-05: Idempotência — recalcular duas vezes não acumula
    // =========================================================

    @Test
    @DisplayName("T0-05: Recalcular duas vezes não deve acumular bônus (idempotência garantida pelo reset)")
    void t005_recalcular_duasVezes_naoDeveAcumularBonus() {
        // Arrange: mesma configuração do T0-01
        ClassePersonagem guerreiro = classeRepository.findByJogoIdAndNomeIgnoreCase(jogo.getId(), "Guerreiro");
        assertThat(guerreiro).isNotNull();

        BonusConfig bba = bonusConfigRepository.save(BonusConfig.builder()
                .jogo(jogo)
                .nome("B.B.A.Idempotencia")
                .sigla("BBAI")
                .formulaBase("")
                .ordemExibicao(1)
                .build());

        classeBonusRepository.save(ClasseBonus.builder()
                .classe(guerreiro)
                .bonus(bba)
                .valorPorNivel(new BigDecimal("2"))
                .build());

        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Idempotencia Teste", null, null, guerreiro.getId(),
                null, null, null, false));

        ficha.setNivel(5);
        ficha = fichaRepository.save(ficha);

        var updateRequest = new UpdateFichaRequest(null, null, null, null, null, null, null, null);

        // Act: recalcular DUAS vezes
        fichaService.atualizar(ficha.getId(), updateRequest);
        fichaService.atualizar(ficha.getId(), updateRequest);

        // Assert: FichaBonus.classe deve ser 10 (2 * 5), não 20
        List<FichaBonus> bonusList = fichaBonusRepository.findByFichaIdWithConfig(ficha.getId());
        FichaBonus alvo = bonusList.stream()
                .filter(b -> b.getBonusConfig() != null
                        && b.getBonusConfig().getId().equals(bba.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("FichaBonus não encontrado"));

        assertThat(alvo.getClasse())
                .as("Recalcular duas vezes não deve dobrar o bônus: esperado 10 (2*5), não 20")
                .isEqualTo(10);
    }

    // =========================================================
    // T0-06: Nível recalculado ao ganhar XP via endpoint /xp
    // =========================================================

    @Test
    @DisplayName("T0-06: Conceder XP suficiente para nível 2 deve recalcular nível e retornar FichaResumoResponse")
    void t006_concederXp_levelUp_deveRecalcularNivelERetornarResumo() {
        // Arrange: usar NivelConfigs do jogo (padrão: nivel=2 requer xp=3000)
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Level Up Teste", null, null, null, null, null, null, false));
        assertThat(ficha.getNivel()).isEqualTo(1);

        // Act: conceder XP suficiente para nível 2 (3000 XP conforme DefaultGameConfigProvider)
        // Ficha começa com xp=0, após concessão: 0 + 3000 = 3000
        FichaResumoResponse resumo = fichaService.concederXp(ficha.getId(), new ConcederXpRequest(3000L, null));

        // Assert
        assertThat(resumo.xp()).isEqualTo(3000L);
        assertThat(resumo.nivel()).isEqualTo(2);

        // Verificar persistência no banco
        Ficha fichaAtualizada = fichaRepository.findById(ficha.getId()).orElseThrow();
        assertThat(fichaAtualizada.getNivel()).isEqualTo(2);
        assertThat(fichaAtualizada.getXp()).isEqualTo(3000L);
    }

    // =========================================================
    // T0-07: Nível não regride com XP insuficiente
    // =========================================================

    @Test
    @DisplayName("T0-07: XP acumula e nível não desce — segunda concessão soma ao total existente")
    void t007_concederXp_acumulativo_nivelNaoRegrideAbaixoDoAtual() {
        // Arrange: criar ficha e levá-la para nível 2 (3000 XP acumulado)
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Sem Level Up", null, null, null, null, null, null, false));

        fichaService.concederXp(ficha.getId(), new ConcederXpRequest(3000L, null));
        Ficha fichaAposLevelUp = fichaRepository.findById(ficha.getId()).orElseThrow();
        assertThat(fichaAposLevelUp.getNivel()).isEqualTo(2);
        assertThat(fichaAposLevelUp.getXp()).isEqualTo(3000L);

        // Act: conceder mais 500 XP — total acumulado = 3500, abaixo dos 6000 necessários para nivel=3
        FichaResumoResponse resumo = fichaService.concederXp(ficha.getId(), new ConcederXpRequest(500L, null));

        // Assert: XP acumulou (3000 + 500 = 3500), nível permanece 2
        assertThat(resumo.xp())
                .as("XP deve ser acumulativo: 3000 + 500 = 3500")
                .isEqualTo(3500L);
        assertThat(resumo.nivel())
                .as("Com XP=3500, nível deve permanecer 2 (necessário 6000 para nivel=3)")
                .isEqualTo(2);

        // Verificar persistência
        Ficha fichaFinal = fichaRepository.findById(ficha.getId()).orElseThrow();
        assertThat(fichaFinal.getXp()).isEqualTo(3500L);
        assertThat(fichaFinal.getNivel()).isEqualTo(2);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
