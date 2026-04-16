package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.VantagemPreRequisitoRequest;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.RaridadeItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.TipoItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemPreRequisito;
import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.SubcategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoPreRequisito;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.RaridadeItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.TipoItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemPreRequisitoRepository;
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

    private static final AtomicInteger counter = new AtomicInteger(100);

    private Jogo jogo;
    private RaridadeItemConfig raridade;
    private TipoItemConfig tipoArma;

    @BeforeEach
    void setUp() {
        vantagemPreRequisitoRepository.deleteAll();
        vantagemConfigRepository.deleteAll();
        itemConfigRepository.deleteAll();
        atributoRepository.deleteAll();
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
