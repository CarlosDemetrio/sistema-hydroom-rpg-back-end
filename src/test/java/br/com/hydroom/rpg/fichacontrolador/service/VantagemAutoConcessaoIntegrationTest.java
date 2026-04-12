package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.ConcederXpRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.OrigemVantagem;
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

/**
 * Testes de integração para VantagemAutoConcessaoService.
 *
 * <p>Cobre os cenários T4-01 a T4-06 da spec 015-T4:</p>
 * <ul>
 *   <li>T4-01: Vantagem auto-concedida na criação (nível 1)</li>
 *   <li>T4-02: Vantagem auto-concedida no level up</li>
 *   <li>T4-03: Não duplicar vantagem já existente</li>
 *   <li>T4-04: Vantagens de classe E raça somam</li>
 *   <li>T4-05: Level up com pulo de níveis</li>
 *   <li>T4-06: Ficha sem classe e sem raça (nullable)</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("VantagemAutoConcessaoService - Testes de Integração (Spec 015 T4)")
class VantagemAutoConcessaoIntegrationTest {

    @Autowired
    private FichaService fichaService;

    @Autowired
    private JogoService jogoService;

    @Autowired
    private VantagemAutoConcessaoService vantagemAutoConcessaoService;

    @Autowired
    private FichaVantagemRepository fichaVantagemRepository;

    @Autowired
    private ClasseVantagemPreDefinidaRepository classeVantagemPreDefinidaRepository;

    @Autowired
    private RacaVantagemPreDefinidaRepository racaVantagemPreDefinidaRepository;

    @Autowired
    private VantagemConfigRepository vantagemConfigRepository;

    @Autowired
    private ConfiguracaoClasseRepository classeRepository;

    @Autowired
    private ConfiguracaoRacaRepository racaRepository;

    @Autowired
    private FichaRepository fichaRepository;

    @Autowired
    private FichaDescricaoFisicaRepository fichaDescricaoFisicaRepository;

    @Autowired
    private FichaProspeccaoRepository fichaProspeccaoRepository;

    @Autowired
    private FichaAmeacaRepository fichaAmeacaRepository;

    @Autowired
    private FichaEssenciaRepository fichaEssenciaRepository;

    @Autowired
    private FichaVidaMembroRepository fichaVidaMembroRepository;

    @Autowired
    private FichaVidaRepository fichaVidaRepository;

    @Autowired
    private FichaBonusRepository fichaBonusRepository;

    @Autowired
    private FichaAptidaoRepository fichaAptidaoRepository;

    @Autowired
    private FichaAtributoRepository fichaAtributoRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Jogo jogo;

    @BeforeEach
    void setUp() {
        // Limpar na ordem correta (dependências primeiro)
        classeVantagemPreDefinidaRepository.deleteAll();
        racaVantagemPreDefinidaRepository.deleteAll();
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
        vantagemConfigRepository.deleteAll();
        jogoParticipanteRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre Auto")
                .email("mestre.auto" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-auto-" + n)
                .role("MESTRE")
                .build());

        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Jogo Auto Vantagem " + n)
                .build());
    }

    // =========================================================
    // T4-01: Vantagem auto-concedida na criação (nível 1)
    // =========================================================

    @Test
    @DisplayName("T4-01: deve auto-conceder vantagem de classe no nivel 1 ao criar ficha")
    void deveAutoConcederVantagemDeClasseNoCriacao() {
        // Arrange
        ClassePersonagem guerreiro = criarClasse("Guerreiro Auto T4-01");
        VantagemConfig tco = criarVantagem("Treinamento Combate Ofensivo");
        criarClasseVantagemPreDefinida(guerreiro, 1, tco);

        // Act
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Aragorn", null,
                null, guerreiro.getId(), null, null, null, false));

        // Assert
        List<FichaVantagem> vantagens = fichaVantagemRepository.findByFichaId(ficha.getId());
        assertThat(vantagens).hasSize(1);

        FichaVantagem fv = vantagens.get(0);
        assertThat(fv.getVantagemConfig().getId()).isEqualTo(tco.getId());
        assertThat(fv.getCustoPago()).isEqualTo(0);
        assertThat(fv.getOrigem()).isEqualTo(OrigemVantagem.SISTEMA);
        assertThat(fv.getNivelAtual()).isEqualTo(1);
    }

    // =========================================================
    // T4-02: Vantagem auto-concedida no level up
    // =========================================================

    @Test
    @DisplayName("T4-02: deve auto-conceder vantagem de classe no level up para nivel 5")
    void deveAutoConcederVantagemNoLevelUp() {
        // Arrange
        ClassePersonagem guerreiro = criarClasse("Guerreiro Auto T4-02");
        VantagemConfig ataqueAdicional = criarVantagem("Ataque Devastador Nivel5");
        criarClasseVantagemPreDefinida(guerreiro, 5, ataqueAdicional);

        // NivelConfigs já existem — criados pelo GameConfigInitializerService ao criar o jogo.
        // nivel=5 requer 15000 XP no default Klayrah.

        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Boromir", null,
                null, guerreiro.getId(), null, null, null, false));

        // Verificar que a ficha começa no nível 1 sem a vantagem de nível 5
        List<FichaVantagem> vantagensAntes = fichaVantagemRepository.findByFichaId(ficha.getId());
        assertThat(vantagensAntes).isEmpty();

        // Act — conceder XP suficiente para atingir nível 5 (15000 XP no default Klayrah)
        autenticarComo(mestre);
        fichaService.concederXp(ficha.getId(), new ConcederXpRequest(15000L, "level up teste"));

        // Assert
        List<FichaVantagem> vantagens = fichaVantagemRepository.findByFichaId(ficha.getId());
        assertThat(vantagens).hasSize(1);

        FichaVantagem fv = vantagens.get(0);
        assertThat(fv.getVantagemConfig().getId()).isEqualTo(ataqueAdicional.getId());
        assertThat(fv.getCustoPago()).isEqualTo(0);
        assertThat(fv.getOrigem()).isEqualTo(OrigemVantagem.SISTEMA);
    }

    // =========================================================
    // T4-03: Não duplicar vantagem já existente
    // =========================================================

    @Test
    @DisplayName("T4-03: deve ignorar vantagem pre-definida se ficha ja possui aquela vantagem")
    void deveIgnorarVantagemJaExistente() {
        // Arrange — criar ficha com classe que tem vantagem pré-definida no nível 1
        ClassePersonagem guerreiro = criarClasse("Guerreiro Auto T4-03");
        VantagemConfig tco = criarVantagem("TCO");
        criarClasseVantagemPreDefinida(guerreiro, 1, tco);

        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Legolas", null,
                null, guerreiro.getId(), null, null, null, false));

        // Verificar que a auto-concessão criou 1 vantagem na criação
        List<FichaVantagem> vantagensAposCriacao = fichaVantagemRepository.findByFichaId(ficha.getId());
        assertThat(vantagensAposCriacao).hasSize(1);
        assertThat(vantagensAposCriacao.get(0).getOrigem()).isEqualTo(OrigemVantagem.SISTEMA);

        // Act — chamar novamente a auto-concessão para nível 1
        List<FichaVantagem> novaChamada = vantagemAutoConcessaoService.concederVantagensParaNivel(ficha, 1);

        // Assert — deve retornar lista vazia (vantagem já existe, não duplica)
        assertThat(novaChamada).isEmpty();

        // E o total de vantagens na ficha deve continuar sendo 1
        List<FichaVantagem> vantagensDepois = fichaVantagemRepository.findByFichaId(ficha.getId());
        assertThat(vantagensDepois).hasSize(1);
        assertThat(vantagensDepois.get(0).getCustoPago()).isEqualTo(0); // custo não alterado
    }

    @Test
    @DisplayName("T4-03b: nao deve sobrescrever vantagem comprada pelo jogador com a auto-concessao")
    void naoDeveSobrescreverVantagemCompradaPeloJogador() {
        // Arrange — vantagem comprada manualmente antes da auto-concessão
        ClassePersonagem guerreiro = criarClasse("Guerreiro Auto T4-03b");
        VantagemConfig tco = criarVantagem("TCO Manual");
        criarClasseVantagemPreDefinida(guerreiro, 1, tco);

        // Salvar manualmente uma FichaVantagem com custoPago=6 e origem=JOGADOR
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Gimli", null,
                null, null, null, null, null, false)); // sem classe ainda

        FichaVantagem fichaVantagemManual = FichaVantagem.builder()
                .ficha(ficha)
                .vantagemConfig(tco)
                .nivelAtual(1)
                .custoPago(6)
                .origem(OrigemVantagem.JOGADOR)
                .build();
        fichaVantagemRepository.save(fichaVantagemManual);

        // Agora atribuir a classe à ficha e executar auto-concessão
        ficha.setClasse(guerreiro);
        fichaRepository.save(ficha);

        // Act
        List<FichaVantagem> autoVantagens = vantagemAutoConcessaoService.concederVantagensParaNivel(ficha, 1);

        // Assert — deve ignorar (vantagem já existe)
        assertThat(autoVantagens).isEmpty();

        // Verificar que o custoPago original foi mantido
        FichaVantagem vantaOriginal = fichaVantagemRepository
                .findByFichaIdAndVantagemConfigId(ficha.getId(), tco.getId())
                .orElseThrow();
        assertThat(vantaOriginal.getCustoPago()).isEqualTo(6);
        assertThat(vantaOriginal.getOrigem()).isEqualTo(OrigemVantagem.JOGADOR);
    }

    // =========================================================
    // T4-04: Vantagens de classe E raça somam
    // =========================================================

    @Test
    @DisplayName("T4-04: deve auto-conceder vantagens de classe E raca ao criar ficha")
    void deveAutoConcederVantagensDeClasseERaca() {
        // Arrange
        ClassePersonagem guerreiro = criarClasse("Guerreiro Auto T4-04");
        Raca humano = criarRaca("Humano Auto T4-04");
        VantagemConfig tco = criarVantagem("TCO-AutoTeste");
        VantagemConfig destreza = criarVantagem("Destreza-AutoTeste");

        criarClasseVantagemPreDefinida(guerreiro, 1, tco);
        criarRacaVantagemPreDefinida(humano, 1, destreza);

        // Act
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Heroi Humano", null,
                humano.getId(), guerreiro.getId(), null, null, null, false));

        // Assert
        List<FichaVantagem> vantagens = fichaVantagemRepository.findByFichaId(ficha.getId());
        assertThat(vantagens).hasSize(2);

        assertThat(vantagens).allMatch(fv -> fv.getOrigem() == OrigemVantagem.SISTEMA);
        assertThat(vantagens).allMatch(fv -> fv.getCustoPago() == 0);

        List<Long> ids = vantagens.stream()
                .map(fv -> fv.getVantagemConfig().getId())
                .toList();
        assertThat(ids).containsExactlyInAnyOrder(tco.getId(), destreza.getId());
    }

    // =========================================================
    // T4-05: Level up com pulo de níveis
    // =========================================================

    @Test
    @DisplayName("T4-05: deve auto-conceder vantagens de todos os niveis pulados no level up")
    void deveAutoConcederVantagensDeNiveisPulados() {
        // Arrange
        ClassePersonagem guerreiro = criarClasse("Guerreiro Auto T4-05");
        VantagemConfig tcd = criarVantagem("TCD");
        VantagemConfig tce = criarVantagem("TCE");
        criarClasseVantagemPreDefinida(guerreiro, 2, tcd);
        criarClasseVantagemPreDefinida(guerreiro, 3, tce);

        // NivelConfigs já existem — criados pelo GameConfigInitializerService ao criar o jogo.
        // nivel=2 requer 3000 XP, nivel=3 requer 6000 XP no default Klayrah.

        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Gandalf", null,
                null, guerreiro.getId(), null, null, null, false));

        // Verificar que nível 1 não tem TCD nem TCE
        assertThat(fichaVantagemRepository.findByFichaId(ficha.getId())).isEmpty();

        // Act — conceder XP para pular de nível 1 diretamente para nível 3 (6000 XP)
        autenticarComo(mestre);
        fichaService.concederXp(ficha.getId(), new ConcederXpRequest(6000L, "pulo de nivel teste"));

        // Assert — TCD (nível 2) e TCE (nível 3) devem ter sido concedidas
        List<FichaVantagem> vantagens = fichaVantagemRepository.findByFichaId(ficha.getId());
        assertThat(vantagens).hasSize(2);

        assertThat(vantagens).allMatch(fv -> fv.getOrigem() == OrigemVantagem.SISTEMA);
        assertThat(vantagens).allMatch(fv -> fv.getCustoPago() == 0);

        List<Long> ids = vantagens.stream()
                .map(fv -> fv.getVantagemConfig().getId())
                .toList();
        assertThat(ids).containsExactlyInAnyOrder(tcd.getId(), tce.getId());
    }

    // =========================================================
    // T4-06: Ficha sem classe e sem raça (nullable)
    // =========================================================

    @Test
    @DisplayName("T4-06: deve retornar lista vazia sem erro quando ficha nao tem classe nem raca")
    void deveRetornarListaVaziaParaFichaSemClasseERaca() {
        // Arrange
        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Personagem Incompleto", null,
                null, null, null, null, null, false));

        // Act — chamar diretamente o serviço de auto-concessão
        List<FichaVantagem> resultado = vantagemAutoConcessaoService.concederVantagensParaNivel(ficha, 1);

        // Assert
        assertThat(resultado).isEmpty();

        // E nenhuma FichaVantagem deve existir para esta ficha
        List<FichaVantagem> vantagens = fichaVantagemRepository.findByFichaId(ficha.getId());
        assertThat(vantagens).isEmpty();
    }

    @Test
    @DisplayName("T4-06b: deve retornar lista vazia sem erro quando ficha so tem classe mas sem pre-definidas")
    void deveRetornarListaVaziaQuandoSemPreDefinidas() {
        // Arrange
        ClassePersonagem mago = criarClasse("Mago Sem Predef T4-06b");
        // Sem ClasseVantagemPreDefinida para o Mago

        autenticarComo(mestre);
        Ficha ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Saruman", null,
                null, mago.getId(), null, null, null, false));

        // Act
        List<FichaVantagem> resultado = vantagemAutoConcessaoService.concederVantagensParaNivel(ficha, 1);

        // Assert
        assertThat(resultado).isEmpty();
        assertThat(fichaVantagemRepository.findByFichaId(ficha.getId())).isEmpty();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    /**
     * Retorna classe existente por nome (criada pelo GameConfigInitializerService) ou cria nova se não existir.
     */
    private ClassePersonagem criarClasse(String nome) {
        ClassePersonagem existente = classeRepository.findByJogoIdAndNomeIgnoreCase(jogo.getId(), nome);
        if (existente != null) {
            return existente;
        }
        ClassePersonagem classe = ClassePersonagem.builder()
                .jogo(jogo)
                .nome(nome)
                .ordemExibicao(99)
                .build();
        return classeRepository.save(classe);
    }

    /**
     * Retorna raça existente por nome (criada pelo GameConfigInitializerService) ou cria nova se não existir.
     */
    private Raca criarRaca(String nome) {
        Raca existente = racaRepository.findByJogoIdAndNomeIgnoreCase(jogo.getId(), nome);
        if (existente != null) {
            return existente;
        }
        Raca raca = Raca.builder()
                .jogo(jogo)
                .nome(nome)
                .ordemExibicao(99)
                .build();
        return racaRepository.save(raca);
    }

    private VantagemConfig criarVantagem(String nome) {
        VantagemConfig vantagem = VantagemConfig.builder()
                .jogo(jogo)
                .nome(nome)
                .formulaCusto("1")
                .nivelMaximo(5)
                .ordemExibicao(0)
                .build();
        return vantagemConfigRepository.save(vantagem);
    }

    private ClasseVantagemPreDefinida criarClasseVantagemPreDefinida(
            ClassePersonagem classe, int nivel, VantagemConfig vantagem) {
        ClasseVantagemPreDefinida cvp = ClasseVantagemPreDefinida.builder()
                .classePersonagem(classe)
                .nivel(nivel)
                .vantagemConfig(vantagem)
                .build();
        return classeVantagemPreDefinidaRepository.save(cvp);
    }

    private RacaVantagemPreDefinida criarRacaVantagemPreDefinida(
            Raca raca, int nivel, VantagemConfig vantagem) {
        RacaVantagemPreDefinida rvp = RacaVantagemPreDefinida.builder()
                .raca(raca)
                .nivel(nivel)
                .vantagemConfig(vantagem)
                .build();
        return racaVantagemPreDefinidaRepository.save(rvp);
    }

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
