package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.VantagemPreRequisitoRequest;
import br.com.hydroom.rpg.fichacontrolador.model.AptidaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
import br.com.hydroom.rpg.fichacontrolador.model.CategoriaVantagem;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.model.DadoProspeccaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.FocoNpc;
import br.com.hydroom.rpg.fichacontrolador.model.GeneroConfig;
import br.com.hydroom.rpg.fichacontrolador.model.HabilidadeConfig;
import br.com.hydroom.rpg.fichacontrolador.model.IndoleConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.MembroCorpoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.NpcDificuldadeConfig;
import br.com.hydroom.rpg.fichacontrolador.model.PresencaConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import br.com.hydroom.rpg.fichacontrolador.model.RaridadeItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.TipoAptidao;
import br.com.hydroom.rpg.fichacontrolador.model.TipoItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemPreRequisito;
import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.SubcategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoPreRequisito;
import br.com.hydroom.rpg.fichacontrolador.repository.CategoriaVantagemRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAptidaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoClasseRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoRacaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.DadoProspeccaoConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.GeneroConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.HabilidadeConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.IndoleConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.MembroCorpoConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.NpcDificuldadeConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.PresencaConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.RaridadeItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.TipoAptidaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.TipoItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemPreRequisitoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.BonusConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes que cobrem os bugs P0 de backend:
 * - BUG-003/BUG-007: ItemConfig listar com nomeQuery=null lançava 500 no PostgreSQL
 * - BUG-SCHEMA: VantagemPreRequisito.tipo deve persistir com valor DEFAULT 'VANTAGEM'
 * - BUG-006: ordemExibicao auto-calculado quando null/0 ao criar configuração
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("BugFix - Testes de Regressão P0")
class BugFixIntegrationTest {

    @Autowired
    private ItemConfigService itemConfigService;

    @Autowired
    private ItemConfigRepository itemConfigRepository;

    @Autowired
    private RaridadeItemConfigRepository raridadeRepository;

    @Autowired
    private TipoItemConfigRepository tipoRepository;

    @Autowired
    private AtributoConfiguracaoService atributoService;

    @Autowired
    private ConfiguracaoAtributoRepository atributoRepository;

    @Autowired
    private VantagemConfiguracaoService vantagemService;

    @Autowired
    private VantagemConfigRepository vantagemConfigRepository;

    @Autowired
    private VantagemPreRequisitoRepository vantagemPreRequisitoRepository;

    @Autowired
    private JogoRepository jogoRepository;

    // BUG-006 additional services
    @Autowired
    private AptidaoConfiguracaoService aptidaoService;
    @Autowired
    private ConfiguracaoAptidaoRepository aptidaoRepository;
    @Autowired
    private TipoAptidaoConfiguracaoService tipoAptidaoService;
    @Autowired
    private TipoAptidaoRepository tipoAptidaoRepository;
    @Autowired
    private BonusConfiguracaoService bonusService;
    @Autowired
    private BonusConfigRepository bonusRepository;
    @Autowired
    private ClasseConfiguracaoService classeService;
    @Autowired
    private ConfiguracaoClasseRepository classeRepository;
    @Autowired
    private GeneroConfiguracaoService generoService;
    @Autowired
    private GeneroConfigRepository generoRepository;
    @Autowired
    private IndoleConfiguracaoService indoleService;
    @Autowired
    private IndoleConfigRepository indoleRepository;
    @Autowired
    private MembroCorpoConfiguracaoService membroCorpoService;
    @Autowired
    private MembroCorpoConfigRepository membroCorpoRepository;
    @Autowired
    private PresencaConfiguracaoService presencaService;
    @Autowired
    private PresencaConfigRepository presencaRepository;
    @Autowired
    private RacaConfiguracaoService racaService;
    @Autowired
    private ConfiguracaoRacaRepository racaRepository;
    @Autowired
    private CategoriaVantagemService categoriaVantagemService;
    @Autowired
    private CategoriaVantagemRepository categoriaVantagemRepository;
    @Autowired
    private HabilidadeConfigService habilidadeService;
    @Autowired
    private HabilidadeConfigRepository habilidadeRepository;
    @Autowired
    private RaridadeItemConfigService raridadeItemService;
    @Autowired
    private TipoItemConfigService tipoItemService;
    @Autowired
    private NpcDificuldadeConfiguracaoService npcDificuldadeService;
    @Autowired
    private NpcDificuldadeConfigRepository npcDificuldadeRepository;
    @Autowired
    private DadoProspeccaoConfiguracaoService dadoProspeccaoService;
    @Autowired
    private DadoProspeccaoConfigRepository dadoProspeccaoRepository;

    private static final AtomicInteger counter = new AtomicInteger(100);

    private Jogo jogo;
    private RaridadeItemConfig raridade;
    private TipoItemConfig tipoArma;

    @BeforeEach
    void setUp() {
        vantagemPreRequisitoRepository.deleteAll();
        vantagemConfigRepository.deleteAll();
        categoriaVantagemRepository.deleteAll();
        itemConfigRepository.deleteAll();
        atributoRepository.deleteAll();
        aptidaoRepository.deleteAll();
        tipoAptidaoRepository.deleteAll();
        bonusRepository.deleteAll();
        classeRepository.deleteAll();
        racaRepository.deleteAll();
        generoRepository.deleteAll();
        indoleRepository.deleteAll();
        membroCorpoRepository.deleteAll();
        presencaRepository.deleteAll();
        habilidadeRepository.deleteAll();
        npcDificuldadeRepository.deleteAll();
        dadoProspeccaoRepository.deleteAll();
        tipoRepository.deleteAll();
        raridadeRepository.deleteAll();
        jogoRepository.deleteAll();

        int n = counter.getAndIncrement();
        jogo = jogoRepository.save(Jogo.builder()
            .nome("Jogo BugFix " + n)
            .jogoAtivo(true)
            .dataInicio(LocalDate.now())
            .build());

        raridade = raridadeRepository.save(RaridadeItemConfig.builder()
            .jogo(jogo)
            .nome("Comum")
            .cor("#9d9d9d")
            .ordemExibicao(1)
            .podeJogadorAdicionar(true)
            .build());

        tipoArma = tipoRepository.save(TipoItemConfig.builder()
            .jogo(jogo)
            .nome("Espada")
            .categoria(CategoriaItem.ARMA)
            .subcategoria(SubcategoriaItem.ESPADA)
            .requerDuasMaos(false)
            .ordemExibicao(1)
            .build());
    }

    // =========================================================
    // BUG-003 / BUG-007: LOWER(null) no PostgreSQL → 500
    // =========================================================

    @Test
    @DisplayName("BUG-003: listarComFiltros com nomeQuery=null deve retornar todos os itens sem erro")
    void deveListarItensComNomeQueryNullSemErro() {
        // Arrange
        itemConfigRepository.save(criarItem("Espada Longa", 1));
        itemConfigRepository.save(criarItem("Arco Curto", 2));

        // Act — nomeQuery=null dispara o caminho que causava function lower(bytea)
        Page<ItemConfig> resultado = itemConfigService.listarComFiltros(
            jogo.getId(), null, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(resultado.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("BUG-003: listarComFiltros com nomeQuery vazio deve retornar todos os itens")
    void deveListarItensComNomeQueryVazioSemErro() {
        // Arrange
        itemConfigRepository.save(criarItem("Machado de Guerra", 1));

        // Act — string vazia tratada como null no service
        Page<ItemConfig> resultado = itemConfigService.listarComFiltros(
            jogo.getId(), "", null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(resultado.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("BUG-003: listarComFiltros com nomeQuery preenchido deve filtrar corretamente (case-insensitive)")
    void deveListarItensComNomeFiltrado() {
        // Arrange
        itemConfigRepository.save(criarItem("Espada Longa de Fogo", 1));
        itemConfigRepository.save(criarItem("ESPADA CURTA", 2));
        itemConfigRepository.save(criarItem("Arco Curto", 3));

        // Act — filtro deve funcionar case-insensitive
        Page<ItemConfig> resultado = itemConfigService.listarComFiltros(
            jogo.getId(), "espada", null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getContent())
            .extracting(i -> i.getNome().toLowerCase())
            .allMatch(nome -> nome.contains("espada"));
    }

    // =========================================================
    // BUG-SCHEMA: VantagemPreRequisito.tipo com DEFAULT no DDL
    // =========================================================

    @Test
    @DisplayName("BUG-SCHEMA: Pré-requisito tipo VANTAGEM deve persistir campo tipo corretamente")
    void devePersistirVantagemPreRequisitoComTipoVantagem() {
        // Arrange
        VantagemConfig v1 = criarVantagem("Força Bruta");
        VantagemConfig v2 = criarVantagem("Golpe Poderoso");

        // Act — cria pré-requisito via service (tipo=VANTAGEM)
        VantagemPreRequisito pr = vantagemService.adicionarPreRequisito(v2.getId(), v1.getId(), 1);

        // Assert — tipo deve ser VANTAGEM e persistir corretamente
        assertThat(pr.getTipo()).isEqualTo(TipoPreRequisito.VANTAGEM);

        VantagemPreRequisito recuperado = vantagemPreRequisitoRepository.findById(pr.getId()).orElseThrow();
        assertThat(recuperado.getTipo()).isEqualTo(TipoPreRequisito.VANTAGEM);
    }

    @Test
    @DisplayName("BUG-SCHEMA: Pré-requisito tipo NIVEL deve persistir campo tipo e valorMinimo corretamente")
    void devePersistirVantagemPreRequisitoComTipoNivel() {
        // Arrange
        VantagemConfig v = criarVantagem("Mestre Luta");

        // Act — cria pré-requisito via service (tipo=NIVEL)
        // Record: tipo, requisitoId, nivelMinimo, racaId, classeId, atributoId, aptidaoId, valorMinimo
        VantagemPreRequisitoRequest request = new VantagemPreRequisitoRequest(
            TipoPreRequisito.NIVEL, null, null, null, null, null, null, 5
        );
        VantagemPreRequisito pr = vantagemService.adicionarPreRequisito(v.getId(), request);

        // Assert — tipo NIVEL e valorMinimo=5 devem estar no banco
        assertThat(pr.getTipo()).isEqualTo(TipoPreRequisito.NIVEL);
        assertThat(pr.getValorMinimo()).isEqualTo(5);

        VantagemPreRequisito recuperado = vantagemPreRequisitoRepository.findById(pr.getId()).orElseThrow();
        assertThat(recuperado.getTipo()).isEqualTo(TipoPreRequisito.NIVEL);
        assertThat(recuperado.getValorMinimo()).isEqualTo(5);
    }

    // =========================================================
    // BUG-006: ordemExibicao auto-calculado quando null/0
    // =========================================================

    @Test
    @DisplayName("BUG-006: Criar AtributoConfig sem ordemExibicao deve definir como MAX+1 automaticamente")
    void deveCriarAtributoComOrdemExibicaoAutomatica() {
        // Arrange — criar dois atributos com ordem explícita
        atributoRepository.save(AtributoConfig.builder()
            .jogo(jogo).nome("Força").abreviacao("FOR").ordemExibicao(1).build());
        atributoRepository.save(AtributoConfig.builder()
            .jogo(jogo).nome("Destreza").abreviacao("DES").ordemExibicao(3).build());

        // Act — criar atributo sem ordemExibicao (null → deve ser MAX+1 = 4)
        AtributoConfig novo = AtributoConfig.builder()
            .jogo(jogo)
            .nome("Vigor")
            .abreviacao("VIG")
            .ordemExibicao(0) // 0 = "não informado"
            .build();
        AtributoConfig criado = atributoService.criar(novo);

        // Assert — ordemExibicao deve ser 4 (MAX(1,3) + 1)
        assertThat(criado.getOrdemExibicao()).isEqualTo(4);
    }

    @Test
    @DisplayName("BUG-006: Criar primeiro AtributoConfig sem ordemExibicao deve definir como 1")
    void deveCriarPrimeiroAtributoComOrdemExibicao1() {
        // Arrange — nenhum atributo no jogo ainda

        // Act — criar sem ordemExibicao
        AtributoConfig novo = AtributoConfig.builder()
            .jogo(jogo)
            .nome("Sabedoria")
            .abreviacao("SAB")
            .ordemExibicao(0)
            .build();
        AtributoConfig criado = atributoService.criar(novo);

        // Assert — primeiro item = ordemExibicao 1
        assertThat(criado.getOrdemExibicao()).isEqualTo(1);
    }

    @Test
    @DisplayName("BUG-006: Criar AtributoConfig com ordemExibicao explícita deve manter o valor")
    void deveCriarAtributoComOrdemExibicaoExplicita() {
        // Arrange
        AtributoConfig novo = AtributoConfig.builder()
            .jogo(jogo)
            .nome("Inteligência")
            .abreviacao("INT")
            .ordemExibicao(7)
            .build();

        // Act
        AtributoConfig criado = atributoService.criar(novo);

        // Assert — ordemExibicao explícita mantida
        assertThat(criado.getOrdemExibicao()).isEqualTo(7);
    }

    @Test
    @DisplayName("BUG-006: Criar ItemConfig sem ordemExibicao deve calcular MAX+1 automaticamente")
    void deveCriarItemComOrdemExibicaoAutomatica() {
        // Arrange — criar itens com ordens 2 e 5
        itemConfigRepository.save(criarItemComOrdem("Espada Simples", 2));
        itemConfigRepository.save(criarItemComOrdem("Escudo", 5));

        // Act — criar item sem ordem (0)
        ItemConfig novo = ItemConfig.builder()
            .jogo(jogo)
            .nome("Adaga")
            .raridade(raridade)
            .tipo(tipoArma)
            .peso(BigDecimal.valueOf(0.5))
            .nivelMinimo(1)
            .ordemExibicao(0)
            .build();
        ItemConfig criado = itemConfigService.criar(novo);

        // Assert — MAX(2,5)+1 = 6
        assertThat(criado.getOrdemExibicao()).isEqualTo(6);
    }

    // =========================================================
    // BUG-006: ordemExibicao auto-calculado — demais services
    // =========================================================

    @Test
    @DisplayName("BUG-006: Criar AptidaoConfig sem ordemExibicao deve calcular MAX+1")
    void deveCriarAptidaoComOrdemExibicaoAutomatica() {
        TipoAptidao tipo = tipoAptidaoRepository.save(TipoAptidao.builder()
            .jogo(jogo).nome("Combate" + counter.getAndIncrement()).ordemExibicao(1).build());
        aptidaoRepository.save(AptidaoConfig.builder()
            .jogo(jogo).nome("Esgrima").tipoAptidao(tipo).ordemExibicao(2).build());

        AptidaoConfig nova = AptidaoConfig.builder()
            .jogo(jogo).nome("Luta").tipoAptidao(tipo).ordemExibicao(0).build();
        AptidaoConfig criada = aptidaoService.criar(nova);

        assertThat(criada.getOrdemExibicao()).isEqualTo(3);
    }

    @Test
    @DisplayName("BUG-006: Criar TipoAptidao sem ordemExibicao deve calcular MAX+1")
    void deveCriarTipoAptidaoComOrdemExibicaoAutomatica() {
        tipoAptidaoRepository.save(TipoAptidao.builder()
            .jogo(jogo).nome("Social" + counter.getAndIncrement()).ordemExibicao(4).build());

        TipoAptidao novo = TipoAptidao.builder()
            .jogo(jogo).nome("Magia" + counter.getAndIncrement()).ordemExibicao(0).build();
        TipoAptidao criado = tipoAptidaoService.criar(novo);

        assertThat(criado.getOrdemExibicao()).isEqualTo(5);
    }

    @Test
    @DisplayName("BUG-006: Criar BonusConfig sem ordemExibicao deve calcular MAX+1")
    void deveCriarBonusComOrdemExibicaoAutomatica() {
        bonusRepository.save(BonusConfig.builder()
            .jogo(jogo).nome("Defesa Fisica").sigla("DEF")
            .ordemExibicao(5).build());

        BonusConfig novo = BonusConfig.builder()
            .jogo(jogo).nome("Esquiva Basica").sigla("ESQ")
            .ordemExibicao(0).build();
        BonusConfig criado = bonusService.criar(novo);

        assertThat(criado.getOrdemExibicao()).isEqualTo(6);
    }

    @Test
    @DisplayName("BUG-006: Criar ClassePersonagem sem ordemExibicao deve calcular MAX+1")
    void deveCriarClasseComOrdemExibicaoAutomatica() {
        classeRepository.save(ClassePersonagem.builder()
            .jogo(jogo).nome("Mago" + counter.getAndIncrement()).ordemExibicao(3).build());

        ClassePersonagem nova = ClassePersonagem.builder()
            .jogo(jogo).nome("Ladino" + counter.getAndIncrement()).ordemExibicao(0).build();
        ClassePersonagem criada = classeService.criar(nova);

        assertThat(criada.getOrdemExibicao()).isEqualTo(4);
    }

    @Test
    @DisplayName("BUG-006: Criar GeneroConfig sem ordemExibicao deve calcular MAX+1")
    void deveCriarGeneroComOrdemExibicaoAutomatica() {
        generoRepository.save(GeneroConfig.builder()
            .jogo(jogo).nome("Masculino").ordemExibicao(1).build());

        GeneroConfig novo = GeneroConfig.builder()
            .jogo(jogo).nome("Feminino").ordemExibicao(0).build();
        GeneroConfig criado = generoService.criar(novo);

        assertThat(criado.getOrdemExibicao()).isEqualTo(2);
    }

    @Test
    @DisplayName("BUG-006: Criar IndoleConfig sem ordemExibicao deve calcular MAX+1")
    void deveCriarIndoleComOrdemExibicaoAutomatica() {
        indoleRepository.save(IndoleConfig.builder()
            .jogo(jogo).nome("Leal").ordemExibicao(2).build());

        IndoleConfig novo = IndoleConfig.builder()
            .jogo(jogo).nome("Caótico").ordemExibicao(0).build();
        IndoleConfig criado = indoleService.criar(novo);

        assertThat(criado.getOrdemExibicao()).isEqualTo(3);
    }

    @Test
    @DisplayName("BUG-006: Criar MembroCorpoConfig sem ordemExibicao deve calcular MAX+1")
    void deveCriarMembroCorpoComOrdemExibicaoAutomatica() {
        membroCorpoRepository.save(MembroCorpoConfig.builder()
            .jogo(jogo).nome("Cabeça")
            .porcentagemVida(java.math.BigDecimal.valueOf(0.10)).ordemExibicao(1).build());

        MembroCorpoConfig novo = MembroCorpoConfig.builder()
            .jogo(jogo).nome("Tronco")
            .porcentagemVida(java.math.BigDecimal.valueOf(0.40)).ordemExibicao(0).build();
        MembroCorpoConfig criado = membroCorpoService.criar(novo);

        assertThat(criado.getOrdemExibicao()).isEqualTo(2);
    }

    @Test
    @DisplayName("BUG-006: Criar PresencaConfig sem ordemExibicao deve calcular MAX+1")
    void deveCriarPresencaComOrdemExibicaoAutomatica() {
        presencaRepository.save(PresencaConfig.builder()
            .jogo(jogo).nome("Carismatico").ordemExibicao(3).build());

        PresencaConfig novo = PresencaConfig.builder()
            .jogo(jogo).nome("Intimidador").ordemExibicao(0).build();
        PresencaConfig criado = presencaService.criar(novo);

        assertThat(criado.getOrdemExibicao()).isEqualTo(4);
    }

    @Test
    @DisplayName("BUG-006: Criar Raca sem ordemExibicao deve calcular MAX+1")
    void deveCriarRacaComOrdemExibicaoAutomatica() {
        racaRepository.save(Raca.builder()
            .jogo(jogo).nome("Humano").ordemExibicao(1).build());

        Raca nova = Raca.builder()
            .jogo(jogo).nome("Elfo").ordemExibicao(0).build();
        Raca criada = racaService.criar(nova);

        assertThat(criada.getOrdemExibicao()).isEqualTo(2);
    }

    @Test
    @DisplayName("BUG-006: Criar CategoriaVantagem sem ordemExibicao deve calcular MAX+1")
    void deveCriarCategoriaVantagemComOrdemExibicaoAutomatica() {
        categoriaVantagemRepository.save(CategoriaVantagem.builder()
            .jogo(jogo).nome("Combate " + counter.getAndIncrement())
            .cor("#ff0000").ordemExibicao(2).build());

        CategoriaVantagem nova = CategoriaVantagem.builder()
            .jogo(jogo).nome("Magia " + counter.getAndIncrement())
            .cor("#0000ff").ordemExibicao(0).build();
        CategoriaVantagem criada = categoriaVantagemService.criar(nova);

        assertThat(criada.getOrdemExibicao()).isEqualTo(3);
    }

    @Test
    @DisplayName("BUG-006: Criar HabilidadeConfig sem ordemExibicao deve calcular MAX+1")
    void deveCriarHabilidadeComOrdemExibicaoAutomatica() {
        habilidadeRepository.save(HabilidadeConfig.builder()
            .jogo(jogo).nome("Golpe Preciso").ordemExibicao(5).build());

        HabilidadeConfig nova = HabilidadeConfig.builder()
            .jogo(jogo).nome("Evasão").ordemExibicao(0).build();
        HabilidadeConfig criada = habilidadeService.criar(nova);

        assertThat(criada.getOrdemExibicao()).isEqualTo(6);
    }

    @Test
    @DisplayName("BUG-006: Criar VantagemConfig sem ordemExibicao deve calcular MAX+1")
    void deveCriarVantagemComOrdemExibicaoAutomatica() {
        vantagemConfigRepository.save(VantagemConfig.builder()
            .jogo(jogo).nome("Força Sobrenatural").sigla("FSB")
            .nivelMaximo(3).formulaCusto("nivel_vantagem").ordemExibicao(4).build());

        VantagemConfig nova = VantagemConfig.builder()
            .jogo(jogo).nome("Sentidos Aguçados").sigla("SA")
            .nivelMaximo(2).formulaCusto("nivel_vantagem").ordemExibicao(0).build();
        VantagemConfig criada = vantagemService.criar(nova);

        assertThat(criada.getOrdemExibicao()).isEqualTo(5);
    }

    @Test
    @DisplayName("BUG-006: Criar TipoItemConfig sem ordemExibicao deve calcular MAX+1")
    void deveCriarTipoItemComOrdemExibicaoAutomatica() {
        tipoRepository.save(TipoItemConfig.builder()
            .jogo(jogo).nome("Espada Simples").categoria(CategoriaItem.ARMA)
            .requerDuasMaos(false).ordemExibicao(2).build());

        TipoItemConfig novo = TipoItemConfig.builder()
            .jogo(jogo).nome("Escudo Redondo").categoria(CategoriaItem.ARMADURA)
            .requerDuasMaos(false).ordemExibicao(0).build();
        TipoItemConfig criado = tipoItemService.criar(novo);

        assertThat(criado.getOrdemExibicao()).isEqualTo(3);
    }

    @Test
    @DisplayName("BUG-006: Criar RaridadeItemConfig sem ordemExibicao deve calcular MAX+1")
    void deveCriarRaridadeComOrdemExibicaoAutomatica() {
        // Arrange — raridade criada no setUp tem ordemExibicao=1
        // Act — criar sem ordem explícita
        RaridadeItemConfig nova = RaridadeItemConfig.builder()
            .jogo(jogo).nome("Incomum " + counter.getAndIncrement())
            .cor("#1eff00").ordemExibicao(0).podeJogadorAdicionar(false).build();
        RaridadeItemConfig criada = raridadeItemService.criar(nova);

        assertThat(criada.getOrdemExibicao()).isEqualTo(2);
    }

    @Test
    @DisplayName("BUG-006: Criar NpcDificuldadeConfig sem ordemExibicao deve calcular MAX+1")
    void deveCriarNpcDificuldadeComOrdemExibicaoAutomatica() {
        npcDificuldadeRepository.save(NpcDificuldadeConfig.builder()
            .jogo(jogo).nome("Facil").foco(FocoNpc.FISICO).ordemExibicao(1).build());

        NpcDificuldadeConfig novo = NpcDificuldadeConfig.builder()
            .jogo(jogo).nome("Medio").foco(FocoNpc.FISICO).ordemExibicao(0).build();
        NpcDificuldadeConfig criado = npcDificuldadeService.criar(novo);

        assertThat(criado.getOrdemExibicao()).isEqualTo(2);
    }

    @Test
    @DisplayName("BUG-006: Criar DadoProspeccaoConfig sem ordemExibicao deve calcular MAX+1")
    void deveCriarDadoProspeccaoComOrdemExibicaoAutomatica() {
        dadoProspeccaoRepository.save(DadoProspeccaoConfig.builder()
            .jogo(jogo).nome("D6 Sorte").numeroFaces(6).ordemExibicao(3).build());

        DadoProspeccaoConfig novo = DadoProspeccaoConfig.builder()
            .jogo(jogo).nome("D8 Fortuna").numeroFaces(8).ordemExibicao(0).build();
        DadoProspeccaoConfig criado = dadoProspeccaoService.criar(novo);

        assertThat(criado.getOrdemExibicao()).isEqualTo(4);
    }

    // ===== HELPERS =====

    private ItemConfig criarItem(String nome, int ordem) {
        return ItemConfig.builder()
            .jogo(jogo)
            .nome(nome)
            .raridade(raridade)
            .tipo(tipoArma)
            .peso(BigDecimal.valueOf(1.0))
            .nivelMinimo(1)
            .ordemExibicao(ordem)
            .build();
    }

    private ItemConfig criarItemComOrdem(String nome, int ordem) {
        return itemConfigRepository.save(criarItem(nome, ordem));
    }

    private VantagemConfig criarVantagem(String nome) {
        int n = counter.getAndIncrement();
        String sigla = (nome.replaceAll("[^A-Za-z]", "").substring(0, Math.min(2, nome.replaceAll("[^A-Za-z]", "").length())).toUpperCase()) + n;
        return vantagemService.criar(VantagemConfig.builder()
            .jogo(jogo)
            .nome(nome)
            .sigla(sigla)
            .formulaCusto("nivel_vantagem")
            .nivelMaximo(5)
            .ordemExibicao(1)
            .build());
    }
}
