package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.UpdateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import jakarta.persistence.EntityManager;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoEfeito;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoVantagem;
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
 * Testes de integração para os tipos de efeito de vantagem no motor de cálculos (Spec 007 T8).
 *
 * <p>Cobre os 7 tipos implementados de VantagemEfeito:</p>
 * <ul>
 *   <li>BONUS_ATRIBUTO — adiciona a FichaAtributo.outros</li>
 *   <li>BONUS_APTIDAO — adiciona a FichaAptidao.outros</li>
 *   <li>BONUS_DERIVADO — adiciona a FichaBonus.vantagens</li>
 *   <li>BONUS_VIDA — adiciona a FichaVida.vt</li>
 *   <li>BONUS_VIDA_MEMBRO — adiciona a FichaVidaMembro.bonusVantagens</li>
 *   <li>BONUS_ESSENCIA — adiciona a FichaEssencia.vantagens</li>
 *   <li>DADO_UP — seleciona dado na sequência de prospecção</li>
 * </ul>
 *
 * <p>FORMULA_CUSTOMIZADA omitida — bloqueada por PA-004 (semântica indefinida).</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("Efeitos de Vantagem - Testes de Integração (Spec 007 T8)")
class FichaEfeitosCalculationIntegrationTest {

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
    private FichaVantagemRepository fichaVantagemRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ConfiguracaoAtributoRepository atributoConfigRepository;

    @Autowired
    private ConfiguracaoAptidaoRepository aptidaoConfigRepository;

    @Autowired
    private BonusConfigRepository bonusConfigRepository;

    @Autowired
    private MembroCorpoConfigRepository membroCorpoConfigRepository;

    @Autowired
    private DadoProspeccaoConfigRepository dadoProspeccaoConfigRepository;

    @Autowired
    private VantagemConfigRepository vantagemConfigRepository;

    @Autowired
    private VantagemEfeitoRepository vantagemEfeitoRepository;

    @Autowired
    private TipoAptidaoRepository tipoAptidaoRepository;

    @Autowired
    private ConfiguracaoNivelRepository nivelConfigRepository;

    @Autowired
    private EntityManager entityManager;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Jogo jogo;
    private Ficha ficha;

    // Configs de atributo criados pelo DefaultGameConfigProvider
    private AtributoConfig atributoFOR;
    private AtributoConfig atributoAGI;
    private AtributoConfig atributoVIG;
    private AtributoConfig atributoSAB;

    // BonusConfig BBA criado pelo DefaultGameConfigProvider
    private BonusConfig bonusBBA;

    // AptidaoConfig para testes — usamos a primeira aptidão existente do jogo
    private AptidaoConfig aptidaoFurtividade;

    // MembroCorpoConfig — usamos o primeiro membro existente do jogo
    private MembroCorpoConfig membroCabeca;

    // Dados de prospecção criados pelo DefaultGameConfigProvider (d3=idx0, d4=idx1, d6=idx2, d8=idx3, d10=idx4, d12=idx5)
    private List<DadoProspeccaoConfig> dadosOrdenados;

    @BeforeEach
    void setUp() {
        // Limpeza na ordem correta para evitar FK violations
        fichaVantagemRepository.deleteAll();
        vantagemEfeitoRepository.deleteAll();
        vantagemConfigRepository.deleteAll();
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
                .nome("Mestre Efeitos " + n)
                .email("mestre.efeitos" + n + "@test.com")
                .provider("google")
                .providerId("google-efeitos-" + n)
                .role("MESTRE")
                .build());

        autenticarComo(mestre);

        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Jogo Efeitos " + n)
                .build());

        // Carregar configs criados pelo DefaultGameConfigProvider
        List<AtributoConfig> atributos = atributoConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId());
        atributoFOR = atributos.stream()
                .filter(a -> "FOR".equalsIgnoreCase(a.getAbreviacao()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("AtributoConfig FOR não encontrado"));
        atributoAGI = atributos.stream()
                .filter(a -> "AGI".equalsIgnoreCase(a.getAbreviacao()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("AtributoConfig AGI não encontrado"));
        atributoVIG = atributos.stream()
                .filter(a -> "VIG".equalsIgnoreCase(a.getAbreviacao()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("AtributoConfig VIG não encontrado"));
        atributoSAB = atributos.stream()
                .filter(a -> "SAB".equalsIgnoreCase(a.getAbreviacao()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("AtributoConfig SAB não encontrado"));

        bonusBBA = bonusConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).stream()
                .filter(b -> "BBA".equalsIgnoreCase(b.getSigla()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("BonusConfig BBA não encontrado"));

        aptidaoFurtividade = aptidaoConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId())
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhuma AptidaoConfig encontrada no jogo"));

        membroCabeca = membroCorpoConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId())
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhum MembroCorpoConfig encontrado no jogo"));

        dadosOrdenados = dadoProspeccaoConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId());
        assertThat(dadosOrdenados).as("Dados de prospecção devem existir após criarJogo").hasSizeGreaterThanOrEqualTo(3);

        // Liberar pontos de atributo no NivelConfig nivel=0 (usado quando xp=0)
        // e nos níveis 1 a 5 para permitir distribuição dos valores base do teste
        nivelConfigRepository.findAll().stream()
                .filter(nc -> nc.getJogo().getId().equals(jogo.getId()) && nc.getNivel() <= 5)
                .forEach(nc -> {
                    nc.setPontosAtributo(999);
                    nc.setLimitadorAtributo(999);
                    nivelConfigRepository.save(nc);
                });

        // Criar ficha — nivel=1 inicial (xp=0)
        ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Personagem Efeitos", null, null, null, null, null, null, false));

        // Definir base dos atributos para os testes
        fichaAtributoRepository.findByFichaId(ficha.getId()).forEach(fa -> {
            if (fa.getAtributoConfig() != null) {
                switch (fa.getAtributoConfig().getAbreviacao().toUpperCase()) {
                    case "FOR" -> { fa.setBase(15); fa.setNivel(3); }
                    case "AGI" -> { fa.setBase(12); fa.setNivel(2); }
                    case "VIG" -> { fa.setBase(20); fa.setNivel(4); }
                    case "SAB" -> { fa.setBase(10); fa.setNivel(1); }
                    default -> {}
                }
                fichaAtributoRepository.save(fa);
            }
        });

        // Definir nível 5 na ficha diretamente para os testes de cálculo
        ficha.setNivel(5);
        ficha = fichaRepository.save(ficha);
    }

    // =========================================================
    // TC-1: BONUS_ATRIBUTO
    // =========================================================

    @Test
    @DisplayName("TC-1a: BONUS_ATRIBUTO com valorPorNivel=2 e nivel=3 adiciona 6 ao FichaAtributo.outros de FOR")
    void tc1a_bonusAtributo_nivelTres_adicionaSeis() {
        // Arrange
        VantagemConfig vantagem = criarVantagem("Treinamento Fisico");
        criarEfeito(vantagem, TipoEfeito.BONUS_ATRIBUTO,
                atributoFOR, null, null, null,
                null, BigDecimal.valueOf(2), null);
        criarFichaVantagem(ficha, vantagem, 3);

        // Act
        triggerRecalcular();

        // Assert
        FichaAtributo fichaFOR = buscarFichaAtributoPorAbreviacao("FOR");
        assertThat(fichaFOR.getOutros())
                .as("valorPorNivel(2) * nivelVantagem(3) deve resultar em outros=6")
                .isEqualTo(6);
        assertThat(fichaFOR.getTotal())
                .as("total = base(15) + nivel(3) + outros(6) = 24")
                .isEqualTo(24);
    }

    @Test
    @DisplayName("TC-1b: BONUS_ATRIBUTO com efeito soft-deleted deve ser ignorado no calculo")
    void tc1b_bonusAtributo_efeitoSoftDeleted_deveSerIgnorado() {
        // Arrange
        VantagemConfig vantagem = criarVantagem("Treinamento Ignorado");
        VantagemEfeito efeito = VantagemEfeito.builder()
                .vantagemConfig(vantagem)
                .tipoEfeito(TipoEfeito.BONUS_ATRIBUTO)
                .atributoAlvo(atributoFOR)
                .valorPorNivel(BigDecimal.valueOf(5))
                .build();
        efeito = vantagemEfeitoRepository.save(efeito);
        // Soft delete do efeito antes de recalcular
        efeito.delete();
        vantagemEfeitoRepository.save(efeito);
        criarFichaVantagem(ficha, vantagem, 3);

        // Act
        triggerRecalcular();

        // Assert
        FichaAtributo fichaFOR = buscarFichaAtributoPorAbreviacao("FOR");
        assertThat(fichaFOR.getOutros())
                .as("Efeito soft-deleted deve ser ignorado: outros deve ser 0")
                .isEqualTo(0);
    }

    // =========================================================
    // TC-2: BONUS_APTIDAO
    // =========================================================

    @Test
    @DisplayName("TC-2a: BONUS_APTIDAO com valorFixo=3 adiciona 3 ao FichaAptidao.outros")
    void tc2a_bonusAptidao_valorFixo_adicionaTres() {
        // Arrange
        VantagemConfig vantagem = criarVantagem("Especialista Furtividade");
        criarEfeito(vantagem, TipoEfeito.BONUS_APTIDAO,
                null, aptidaoFurtividade, null, null,
                BigDecimal.valueOf(3), null, null);
        criarFichaVantagem(ficha, vantagem, 1);

        // Act
        triggerRecalcular();

        // Assert
        FichaAptidao fichaAptidao = buscarFichaAptidaoPorConfig(aptidaoFurtividade.getId());
        assertThat(fichaAptidao.getOutros())
                .as("valorFixo(3) deve resultar em outros=3")
                .isEqualTo(3);
        assertThat(fichaAptidao.getTotal())
                .as("total = base(0) + sorte(0) + classe(0) + outros(3) = 3")
                .isEqualTo(3);
    }

    @Test
    @DisplayName("TC-2b: BONUS_APTIDAO com valorFixo e valorPorNivel acumulam corretamente")
    void tc2b_bonusAptidao_fixoEPorNivel_acumulam() {
        // Arrange: valorFixo=2 + valorPorNivel=1, nivelVantagem=4 => 2 + 4 = 6
        VantagemConfig vantagem = criarVantagem("Mestre Furtividade");
        criarEfeito(vantagem, TipoEfeito.BONUS_APTIDAO,
                null, aptidaoFurtividade, null, null,
                BigDecimal.valueOf(2), BigDecimal.valueOf(1), null);
        criarFichaVantagem(ficha, vantagem, 4);

        // Act
        triggerRecalcular();

        // Assert
        FichaAptidao fichaAptidao = buscarFichaAptidaoPorConfig(aptidaoFurtividade.getId());
        assertThat(fichaAptidao.getOutros())
                .as("valorFixo(2) + valorPorNivel(1) * nivel(4) = 6")
                .isEqualTo(6);
    }

    // =========================================================
    // TC-3: BONUS_DERIVADO
    // =========================================================

    @Test
    @DisplayName("TC-3a: BONUS_DERIVADO com valorPorNivel=1 e nivel=5 adiciona 5 ao FichaBonus.vantagens")
    void tc3a_bonusDerivado_nivelCinco_adicionaCinco() {
        // Arrange
        VantagemConfig vantagem = criarVantagem("Treinamento Combate Ofensivo");
        criarEfeito(vantagem, TipoEfeito.BONUS_DERIVADO,
                null, null, bonusBBA, null,
                null, BigDecimal.valueOf(1), null);
        criarFichaVantagem(ficha, vantagem, 5);

        // Act
        triggerRecalcular();

        // Assert
        FichaBonus fichaBonus = buscarFichaBonus(bonusBBA.getId());
        assertThat(fichaBonus.getVantagens())
                .as("valorPorNivel(1) * nivelVantagem(5) deve resultar em vantagens=5")
                .isEqualTo(5);
        assertThat(fichaBonus.getTotal())
                .as("Total deve incluir os 5 pontos de vantagem")
                .isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("TC-3b: Multiplas vantagens BONUS_DERIVADO no mesmo alvo acumulam corretamente")
    void tc3b_bonusDerivado_multiplos_acumulam() {
        // Arrange: vantagem A +3 e vantagem B +2 fixo, ambas no BBA
        VantagemConfig vantagemA = criarVantagem("Combate A");
        criarEfeito(vantagemA, TipoEfeito.BONUS_DERIVADO,
                null, null, bonusBBA, null,
                null, BigDecimal.valueOf(1), null); // +1/nivel, nivel=3 => +3
        criarFichaVantagem(ficha, vantagemA, 3);

        VantagemConfig vantagemB = criarVantagem("Combate B");
        criarEfeito(vantagemB, TipoEfeito.BONUS_DERIVADO,
                null, null, bonusBBA, null,
                BigDecimal.valueOf(2), null, null); // +2 fixo
        criarFichaVantagem(ficha, vantagemB, 1);

        // Act
        triggerRecalcular();

        // Assert
        FichaBonus fichaBonus = buscarFichaBonus(bonusBBA.getId());
        assertThat(fichaBonus.getVantagens())
                .as("3 (A: 1*3) + 2 (B: fixo=2) = 5")
                .isEqualTo(5);
    }

    // =========================================================
    // TC-4: BONUS_VIDA
    // =========================================================

    @Test
    @DisplayName("TC-4a: BONUS_VIDA com valorPorNivel=5 e nivelVantagem=4 adiciona 20 ao FichaVida.vt")
    void tc4a_bonusVida_nivelQuatro_adicionaVinte() {
        // Arrange
        VantagemConfig vantagem = criarVantagem("Saude de Ferro Teste");
        criarEfeito(vantagem, TipoEfeito.BONUS_VIDA,
                null, null, null, null,
                null, BigDecimal.valueOf(5), null);
        criarFichaVantagem(ficha, vantagem, 4);

        // Act
        triggerRecalcular();

        // Assert: vt = 5 * 4 = 20
        FichaVida fichaVida = fichaVidaRepository.findByFichaId(ficha.getId()).orElseThrow();
        assertThat(fichaVida.getVt())
                .as("valorPorNivel(5) * nivelVantagem(4) deve resultar em vt=20")
                .isEqualTo(20);

        // vidaTotal = vigTotal + ficha.nivel(5) + vt(20) + renascimentos(0) + outros(0)
        // VIG: base=20, nivel=4, outros=0 (sem raca) => total=24
        int vigTotal = buscarFichaAtributoPorAbreviacao("VIG").getTotal();
        assertThat(fichaVida.getVidaTotal())
                .as("vidaTotal = vig(%d) + nivel(5) + vt(20) + renascimentos(0) + outros(0)".formatted(vigTotal))
                .isEqualTo(vigTotal + 5 + 20 + 0 + 0);
    }

    @Test
    @DisplayName("TC-4b: Sem vantagem BONUS_VIDA, FichaVida.vt deve ser zerado ao recalcular")
    void tc4b_bonusVida_semVantagem_vtDeveSerZero() {
        // Arrange: setar vt manualmente para garantir que o recalculo limpa
        FichaVida fichaVida = fichaVidaRepository.findByFichaId(ficha.getId()).orElseThrow();
        fichaVida.setVt(50);
        fichaVidaRepository.save(fichaVida);

        // Act: recalcular sem nenhuma FichaVantagem com BONUS_VIDA
        triggerRecalcular();

        // Assert
        FichaVida fichaVidaApos = fichaVidaRepository.findByFichaId(ficha.getId()).orElseThrow();
        assertThat(fichaVidaApos.getVt())
                .as("Sem vantagem BONUS_VIDA, vt deve ser zerado pelo recalculo")
                .isEqualTo(0);
    }

    // =========================================================
    // TC-5: BONUS_VIDA_MEMBRO
    // =========================================================

    @Test
    @DisplayName("TC-5a: BONUS_VIDA_MEMBRO com valorFixo=10 adiciona ao membro sem alterar vt global")
    void tc5a_bonusVidaMembro_valorFixo_adicionaAoMembro() {
        // Arrange
        VantagemConfig vantagem = criarVantagem("Cabeca Dura");
        criarEfeito(vantagem, TipoEfeito.BONUS_VIDA_MEMBRO,
                null, null, null, membroCabeca,
                BigDecimal.valueOf(10), null, null);
        criarFichaVantagem(ficha, vantagem, 1);

        // Act
        triggerRecalcular();

        // Assert: bonusVantagens do membro = 10
        FichaVidaMembro fichaVidaMembro = buscarFichaVidaMembro(membroCabeca.getId());
        assertThat(fichaVidaMembro.getBonusVantagens())
                .as("valorFixo(10) deve resultar em bonusVantagens=10")
                .isEqualTo(10);

        // Assert: pool global (vt) NAO deve ser alterado
        FichaVida fichaVida = fichaVidaRepository.findByFichaId(ficha.getId()).orElseThrow();
        assertThat(fichaVida.getVt())
                .as("vt global nao deve ser alterado por BONUS_VIDA_MEMBRO")
                .isEqualTo(0);

        // Assert: vida do membro inclui o bonus
        // vida = floor(vidaTotal * porcentagem) + bonusVantagens
        int vidaTotal = fichaVida.getVidaTotal();
        BigDecimal porcentagem = membroCabeca.getPorcentagemVida();
        int vidaBaseMembro = BigDecimal.valueOf(vidaTotal).multiply(porcentagem)
                .setScale(0, java.math.RoundingMode.FLOOR).intValue();
        assertThat(fichaVidaMembro.getVida())
                .as("vida do membro = piso(vidaTotal * porcentagem) + bonusVantagens(10)")
                .isEqualTo(vidaBaseMembro + 10);
    }

    @Test
    @DisplayName("TC-5b: Sem vantagem BONUS_VIDA_MEMBRO, bonusVantagens do membro deve ser zero")
    void tc5b_bonusVidaMembro_semVantagem_bonusDeveSerZero() {
        // Arrange: setar bonusVantagens manualmente
        FichaVidaMembro membro = buscarFichaVidaMembro(membroCabeca.getId());
        membro.setBonusVantagens(25);
        fichaVidaMembroRepository.save(membro);

        // Act: recalcular sem vantagem BONUS_VIDA_MEMBRO
        triggerRecalcular();

        // Assert
        FichaVidaMembro membroApos = buscarFichaVidaMembro(membroCabeca.getId());
        assertThat(membroApos.getBonusVantagens())
                .as("Sem vantagem BONUS_VIDA_MEMBRO, bonusVantagens deve ser zerado")
                .isEqualTo(0);
    }

    // =========================================================
    // TC-6: BONUS_ESSENCIA
    // =========================================================

    @Test
    @DisplayName("TC-6a: BONUS_ESSENCIA com valorFixo=5 e valorPorNivel=2 a nivel=2 adiciona 9 a FichaEssencia.vantagens")
    void tc6a_bonusEssencia_nivelDois_adicionaNove() {
        // Arrange: valorFixo=5 + valorPorNivel=2 * nivel=2 => 5 + 4 = 9
        VantagemConfig vantagem = criarVantagem("Reserva Essencial");
        criarEfeito(vantagem, TipoEfeito.BONUS_ESSENCIA,
                null, null, null, null,
                BigDecimal.valueOf(5), BigDecimal.valueOf(2), null);
        criarFichaVantagem(ficha, vantagem, 2);

        // Act
        triggerRecalcular();

        // Assert
        FichaEssencia fichaEssencia = fichaEssenciaRepository.findByFichaId(ficha.getId()).orElseThrow();
        assertThat(fichaEssencia.getVantagens())
                .as("valorFixo(5) + valorPorNivel(2) * nivelVantagem(2) = 9")
                .isEqualTo(9);
    }

    @Test
    @DisplayName("TC-6b: Multiplas vantagens BONUS_ESSENCIA acumulam no mesmo campo")
    void tc6b_bonusEssencia_multiplos_acumulam() {
        // Arrange: vantagem A valorFixo=3 + vantagem B valorFixo=4 => total 7
        VantagemConfig vantagemA = criarVantagem("Essencia A");
        criarEfeito(vantagemA, TipoEfeito.BONUS_ESSENCIA,
                null, null, null, null,
                BigDecimal.valueOf(3), null, null);
        criarFichaVantagem(ficha, vantagemA, 1);

        VantagemConfig vantagemB = criarVantagem("Essencia B");
        criarEfeito(vantagemB, TipoEfeito.BONUS_ESSENCIA,
                null, null, null, null,
                BigDecimal.valueOf(4), null, null);
        criarFichaVantagem(ficha, vantagemB, 1);

        // Act
        triggerRecalcular();

        // Assert
        FichaEssencia fichaEssencia = fichaEssenciaRepository.findByFichaId(ficha.getId()).orElseThrow();
        assertThat(fichaEssencia.getVantagens())
                .as("3 + 4 = 7 — multiplas vantagens BONUS_ESSENCIA acumulam")
                .isEqualTo(7);
    }

    // =========================================================
    // TC-7: DADO_UP
    // =========================================================

    @Test
    @DisplayName("TC-7a: DADO_UP nivelVantagem=1 seleciona o primeiro dado da sequencia (posicao 0)")
    void tc7a_dadoUp_nivel1_selecionaPrimeiroDado() {
        // Arrange: nivel=1 => posicao 0 => dadosOrdenados[0] => d3 (faces=3)
        VantagemConfig vantagem = criarVantagem("Dado Especial Basico");
        criarEfeito(vantagem, TipoEfeito.DADO_UP,
                null, null, null, null,
                null, null, null);
        criarFichaVantagem(ficha, vantagem, 1);

        // Act
        triggerRecalcular();

        // Assert
        FichaProspeccao prospeccao = buscarFichaProspeccao();
        assertThat(prospeccao.getDadoDisponivel()).isNotNull();
        assertThat(prospeccao.getDadoDisponivel().getNumeroFaces())
                .as("DADO_UP nivel=1 -> posicao 0 -> d3 (faces=3)")
                .isEqualTo(dadosOrdenados.get(0).getNumeroFaces());
    }

    @Test
    @DisplayName("TC-7b: DADO_UP nivelVantagem=3 seleciona o terceiro dado da sequencia (posicao 2)")
    void tc7b_dadoUp_nivel3_selecionaTerceiroDado() {
        // Arrange: nivel=3 => posicao 2 => dadosOrdenados[2] => d6 (faces=6)
        VantagemConfig vantagem = criarVantagem("Dado Especial Avancado");
        criarEfeito(vantagem, TipoEfeito.DADO_UP,
                null, null, null, null,
                null, null, null);
        criarFichaVantagem(ficha, vantagem, 3);

        // Act
        triggerRecalcular();

        // Assert
        FichaProspeccao prospeccao = buscarFichaProspeccao();
        assertThat(prospeccao.getDadoDisponivel()).isNotNull();
        assertThat(prospeccao.getDadoDisponivel().getNumeroFaces())
                .as("DADO_UP nivel=3 -> posicao 2 -> dadosOrdenados[2]")
                .isEqualTo(dadosOrdenados.get(2).getNumeroFaces());
    }

    @Test
    @DisplayName("TC-7c: Multiplas vantagens DADO_UP usa a maior posicao (nao acumula)")
    void tc7c_dadoUp_multiplos_usaMaiorPosicao() {
        // Arrange: vantagem A nivel=2 (posicao 1), vantagem B nivel=5 (posicao 4) => vence posicao 4
        VantagemConfig vantagemA = criarVantagem("Dado A");
        criarEfeito(vantagemA, TipoEfeito.DADO_UP,
                null, null, null, null,
                null, null, null);
        criarFichaVantagem(ficha, vantagemA, 2);

        VantagemConfig vantagemB = criarVantagem("Dado B Superior");
        criarEfeito(vantagemB, TipoEfeito.DADO_UP,
                null, null, null, null,
                null, null, null);
        criarFichaVantagem(ficha, vantagemB, 5);

        // Act
        triggerRecalcular();

        // Assert: posicao 4 => dadosOrdenados[4]
        FichaProspeccao prospeccao = buscarFichaProspeccao();
        assertThat(prospeccao.getDadoDisponivel()).isNotNull();
        assertThat(prospeccao.getDadoDisponivel().getNumeroFaces())
                .as("Maior posicao (nivel=5 => posicao 4) deve vencer")
                .isEqualTo(dadosOrdenados.get(4).getNumeroFaces());
    }

    @Test
    @DisplayName("TC-7d: DADO_UP com nivel acima do tamanho da sequencia usa o ultimo dado (cap)")
    void tc7d_dadoUp_nivelAcimaSequencia_usaUltimoDado() {
        // Arrange: nivel=100 => posicao 99 => cap em dadosOrdenados.size()-1 => ultimo dado
        VantagemConfig vantagem = criarVantagem("Dado Lendario");
        criarEfeito(vantagem, TipoEfeito.DADO_UP,
                null, null, null, null,
                null, null, null);
        criarFichaVantagem(ficha, vantagem, 100);

        // Act
        triggerRecalcular();

        // Assert: ultimo dado da sequencia
        FichaProspeccao prospeccao = buscarFichaProspeccao();
        assertThat(prospeccao.getDadoDisponivel()).isNotNull();
        assertThat(prospeccao.getDadoDisponivel().getNumeroFaces())
                .as("Nivel acima da sequencia deve usar o ultimo dado disponivel")
                .isEqualTo(dadosOrdenados.get(dadosOrdenados.size() - 1).getNumeroFaces());
    }

    @Test
    @DisplayName("TC-7e: Sem vantagem DADO_UP ativa, dadoDisponivel deve ser null")
    void tc7e_dadoUp_semVantagem_dadoDisponivelNull() {
        // Arrange: sem nenhuma FichaVantagem com DADO_UP

        // Act
        triggerRecalcular();

        // Assert
        FichaProspeccao prospeccao = buscarFichaProspeccao();
        assertThat(prospeccao.getDadoDisponivel())
                .as("Sem DADO_UP, dadoDisponivel deve ser null")
                .isNull();
    }

    // =========================================================
    // Cenarios de borda (todos os tipos)
    // =========================================================

    @Test
    @DisplayName("Recalcular e idempotente: 3 chamadas com os mesmos dados produzem o mesmo resultado")
    void borda_recalcularIdempotente() {
        // Arrange
        VantagemConfig vantagem = criarVantagem("Treinamento Idempotencia");
        criarEfeito(vantagem, TipoEfeito.BONUS_ATRIBUTO,
                atributoFOR, null, null, null,
                null, BigDecimal.valueOf(2), null);
        criarFichaVantagem(ficha, vantagem, 3);

        // Act: recalcular 3 vezes
        triggerRecalcular();
        int outrosPrimeira = buscarFichaAtributoPorAbreviacao("FOR").getOutros();

        triggerRecalcular();
        int outrosSegunda = buscarFichaAtributoPorAbreviacao("FOR").getOutros();

        triggerRecalcular();
        int outrosTerceira = buscarFichaAtributoPorAbreviacao("FOR").getOutros();

        // Assert: mesmo valor nas 3 chamadas (sem dupla contagem)
        assertThat(outrosPrimeira)
                .as("Valor apos primeira recalculacao")
                .isEqualTo(6);
        assertThat(outrosSegunda)
                .as("Segunda recalculacao nao deve alterar o resultado")
                .isEqualTo(outrosPrimeira);
        assertThat(outrosTerceira)
                .as("Terceira recalculacao nao deve alterar o resultado")
                .isEqualTo(outrosPrimeira);
    }

    @Test
    @DisplayName("zerarContribuicoes: remover vantagem e recalcular deve zerar o campo de contribuicao")
    void borda_removerVantagem_campoDeveSerZerado() {
        // Arrange: primeira passagem com vantagem
        VantagemConfig vantagem = criarVantagem("Vantagem Temporaria");
        criarEfeito(vantagem, TipoEfeito.BONUS_ATRIBUTO,
                atributoFOR, null, null, null,
                null, BigDecimal.valueOf(2), null);
        FichaVantagem fichaVantagem = criarFichaVantagem(ficha, vantagem, 3);

        triggerRecalcular();
        assertThat(buscarFichaAtributoPorAbreviacao("FOR").getOutros()).isEqualTo(6);

        // Remove a vantagem (soft delete)
        fichaVantagem.delete();
        fichaVantagemRepository.save(fichaVantagem);

        // Act: recalcular sem a vantagem
        triggerRecalcular();

        // Assert: outros deve ser zerado
        assertThat(buscarFichaAtributoPorAbreviacao("FOR").getOutros())
                .as("Apos remover a vantagem, outros deve ser zerado")
                .isEqualTo(0);
    }

    @Test
    @DisplayName("Lista vazia de vantagens: campos de contribuicao devem permanecer zerados")
    void borda_semVantagens_camposZerados() {
        // Arrange: sem FichaVantagem no setup

        // Act
        triggerRecalcular();

        // Assert
        FichaAtributo fichaFOR = buscarFichaAtributoPorAbreviacao("FOR");
        assertThat(fichaFOR.getOutros())
                .as("Sem vantagens, atributo.outros deve ser 0")
                .isEqualTo(0);

        FichaBonus fichaBonus = buscarFichaBonus(bonusBBA.getId());
        assertThat(fichaBonus.getVantagens())
                .as("Sem vantagens, bonus.vantagens deve ser 0")
                .isEqualTo(0);

        FichaVida fichaVida = fichaVidaRepository.findByFichaId(ficha.getId()).orElseThrow();
        assertThat(fichaVida.getVt())
                .as("Sem vantagens, vida.vt deve ser 0")
                .isEqualTo(0);

        FichaEssencia fichaEssencia = fichaEssenciaRepository.findByFichaId(ficha.getId()).orElseThrow();
        assertThat(fichaEssencia.getVantagens())
                .as("Sem vantagens, essencia.vantagens deve ser 0")
                .isEqualTo(0);

        FichaProspeccao prospeccao = buscarFichaProspeccao();
        assertThat(prospeccao.getDadoDisponivel())
                .as("Sem DADO_UP, dadoDisponivel deve ser null")
                .isNull();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * Dispara recalculo via atualizar() — replica o fluxo real do FichaService.
     *
     * <p>O flush + clear do EntityManager antes da chamada é necessário para forçar
     * o Hibernate a recarregar as entidades do banco (incluindo os efeitos de vantagem
     * recém-persistidos), descartando o cache L1 que pode conter versões desatualizadas
     * de VantagemConfig.efeitos.</p>
     */
    private void triggerRecalcular() {
        entityManager.flush();
        entityManager.clear();
        fichaService.atualizar(ficha.getId(),
                new UpdateFichaRequest(null, null, null, null, null, null, null, null));
    }

    private VantagemConfig criarVantagem(String nome) {
        VantagemConfig vc = VantagemConfig.builder()
                .jogo(jogo)
                .nome(nome)
                .nivelMaximo(10)
                .formulaCusto("NIVEL")
                .tipoVantagem(TipoVantagem.VANTAGEM)
                .ordemExibicao(1)
                .build();
        return vantagemConfigRepository.save(vc);
    }

    private VantagemEfeito criarEfeito(
            VantagemConfig vantagem,
            TipoEfeito tipo,
            AtributoConfig atributoAlvo,
            AptidaoConfig aptidaoAlvo,
            BonusConfig bonusAlvo,
            MembroCorpoConfig membroAlvo,
            BigDecimal valorFixo,
            BigDecimal valorPorNivel,
            String formula) {

        VantagemEfeito efeito = VantagemEfeito.builder()
                .vantagemConfig(vantagem)
                .tipoEfeito(tipo)
                .atributoAlvo(atributoAlvo)
                .aptidaoAlvo(aptidaoAlvo)
                .bonusAlvo(bonusAlvo)
                .membroAlvo(membroAlvo)
                .valorFixo(valorFixo)
                .valorPorNivel(valorPorNivel)
                .formula(formula)
                .build();
        return vantagemEfeitoRepository.save(efeito);
    }

    private FichaVantagem criarFichaVantagem(Ficha fichaAlvo, VantagemConfig vantagemConfig, int nivelAtual) {
        FichaVantagem fv = FichaVantagem.builder()
                .ficha(fichaAlvo)
                .vantagemConfig(vantagemConfig)
                .nivelAtual(nivelAtual)
                .custoPago(0)
                .concedidoPeloMestre(true)
                .build();
        return fichaVantagemRepository.save(fv);
    }

    private FichaAtributo buscarFichaAtributoPorAbreviacao(String abreviacao) {
        return fichaAtributoRepository.findByFichaIdWithConfig(ficha.getId()).stream()
                .filter(fa -> fa.getAtributoConfig() != null
                        && abreviacao.equalsIgnoreCase(fa.getAtributoConfig().getAbreviacao()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "FichaAtributo %s nao encontrado".formatted(abreviacao)));
    }

    private FichaAptidao buscarFichaAptidaoPorConfig(Long aptidaoConfigId) {
        return fichaAptidaoRepository.findByFichaIdWithConfig(ficha.getId()).stream()
                .filter(fa -> fa.getAptidaoConfig() != null
                        && aptidaoConfigId.equals(fa.getAptidaoConfig().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "FichaAptidao para config ID %d nao encontrada".formatted(aptidaoConfigId)));
    }

    private FichaBonus buscarFichaBonus(Long bonusConfigId) {
        return fichaBonusRepository.findByFichaIdWithConfig(ficha.getId()).stream()
                .filter(fb -> fb.getBonusConfig() != null
                        && bonusConfigId.equals(fb.getBonusConfig().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "FichaBonus para config ID %d nao encontrado".formatted(bonusConfigId)));
    }

    private FichaVidaMembro buscarFichaVidaMembro(Long membroConfigId) {
        return fichaVidaMembroRepository.findByFichaIdWithConfig(ficha.getId()).stream()
                .filter(m -> m.getMembroCorpoConfig() != null
                        && membroConfigId.equals(m.getMembroCorpoConfig().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "FichaVidaMembro para config ID %d nao encontrado".formatted(membroConfigId)));
    }

    private FichaProspeccao buscarFichaProspeccao() {
        List<FichaProspeccao> prospeccoes = fichaProspeccaoRepository.findByFichaId(ficha.getId());
        assertThat(prospeccoes)
                .as("Ficha deve ter pelo menos uma FichaProspeccao")
                .isNotEmpty();
        return prospeccoes.get(0);
    }
}
