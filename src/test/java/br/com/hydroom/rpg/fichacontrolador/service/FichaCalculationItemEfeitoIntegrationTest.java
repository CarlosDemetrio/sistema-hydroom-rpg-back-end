package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaItemAdicionarRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaItemDuracaoRequest;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoItemEfeito;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import jakarta.persistence.EntityManager;
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
 * Testes de integração para cálculo de fichas considerando efeitos de itens equipados.
 *
 * <p>Cobre os cenários da Spec 016 T5:</p>
 * <ul>
 *   <li>CALC-ITEM-01: Equipar item com BONUS_DERIVADO aplica bônus no FichaBonus.itens</li>
 *   <li>CALC-ITEM-02: Desequipar item reverte o bônus (idempotência)</li>
 *   <li>CALC-ITEM-03: Dois itens com mesmo BONUS_DERIVADO acumulam</li>
 *   <li>CALC-ITEM-04: Recalcular() duas vezes não acumula (reset idempotente)</li>
 *   <li>CALC-ITEM-05: Item com duracaoAtual=0 ignorado mesmo com equipado=true</li>
 *   <li>CALC-ITEM-06: Item customizado (itemConfig=null) não causa NPE</li>
 *   <li>CALC-ITEM-07: BONUS_VIDA aplica bônus correto</li>
 *   <li>CALC-ITEM-08: BONUS_ESSENCIA aplica bônus correto</li>
 *   <li>CALC-ITEM-09: Remover item equipado dispara recálculo</li>
 *   <li>CALC-ITEM-10: Durabilidade 0 auto-desequipa e recalcula</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaCalculationService + ItemEfeito - Testes de Integração (Spec 016 T5)")
class FichaCalculationItemEfeitoIntegrationTest {

    @Autowired
    private FichaItemService fichaItemService;

    @Autowired
    private FichaService fichaService;

    @Autowired
    private JogoService jogoService;

    @Autowired
    private FichaItemRepository fichaItemRepository;

    @Autowired
    private FichaRepository fichaRepository;

    @Autowired
    private FichaAtributoRepository fichaAtributoRepository;

    @Autowired
    private FichaBonusRepository fichaBonusRepository;

    @Autowired
    private FichaVidaRepository fichaVidaRepository;

    @Autowired
    private FichaEssenciaRepository fichaEssenciaRepository;

    @Autowired
    private FichaAmeacaRepository fichaAmeacaRepository;

    @Autowired
    private FichaVidaMembroRepository fichaVidaMembroRepository;

    @Autowired
    private FichaAptidaoRepository fichaAptidaoRepository;

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
    private BonusConfigRepository bonusConfigRepository;

    @Autowired
    private RaridadeItemConfigRepository raridadeItemConfigRepository;

    @Autowired
    private TipoItemConfigRepository tipoItemConfigRepository;

    @Autowired
    private ItemConfigRepository itemConfigRepository;

    @Autowired
    private ItemEfeitoRepository itemEfeitoRepository;

    @Autowired
    private EntityManager entityManager;

    private static final AtomicInteger counter = new AtomicInteger(1);
    private int itemCounter = 0;

    private Usuario mestre;
    private Jogo jogo;
    private Ficha ficha;
    private RaridadeItemConfig raridadeComum;
    private TipoItemConfig tipoItem;

    @BeforeEach
    void setUp() {
        // Limpar na ordem correta
        fichaItemRepository.deleteAll();
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
        itemConfigRepository.deleteAll();
        raridadeItemConfigRepository.deleteAll();
        tipoItemConfigRepository.deleteAll();
        jogoParticipanteRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre Calculo Item")
                .email("mestre.calcitem" + n + "@test.com")
                .provider("google")
                .providerId("google-calcitem-" + n)
                .role("MESTRE")
                .build());

        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Campanha Calculo Itens " + n)
                .build());

        ficha = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Personagem Teste " + n,
                        null, null, null, null, null, null, false));

        raridadeComum = raridadeItemConfigRepository.save(RaridadeItemConfig.builder()
                .jogo(jogo)
                .nome("Comum " + n)
                .cor("#9d9d9d")
                .ordemExibicao(1)
                .podeJogadorAdicionar(true)
                .build());

        tipoItem = tipoItemConfigRepository.save(TipoItemConfig.builder()
                .jogo(jogo)
                .nome("Espada " + n)
                .categoria(CategoriaItem.ARMA)
                .requerDuasMaos(false)
                .ordemExibicao(1)
                .build());
    }

    // =========================================================
    // CALC-ITEM-01: BONUS_DERIVADO aplica no FichaBonus.itens
    // =========================================================

    @Test
    @DisplayName("CALC-ITEM-01: Equipar item com BONUS_DERIVADO valorFixo=2 deve incrementar FichaBonus.itens em 2")
    void equiparItemComBonusDerivado_deveIncrementarFichaBonusItens() {
        // Arrange — obter um BonusConfig criado pelo GameConfigInitializerService
        List<BonusConfig> bonusConfigs = bonusConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId());
        assertThat(bonusConfigs).isNotEmpty();
        BonusConfig bonusAlvo = bonusConfigs.get(0);

        ItemConfig itemConfig = criarItemConfigComBonusDerivado(bonusAlvo, 2);

        FichaItem fichaItem = fichaItemService.adicionarItem(ficha.getId(),
                new FichaItemAdicionarRequest(itemConfig.getId(), 1, null, false), mestre.getId());

        // Act — equipar o item (deve disparar recálculo)
        fichaItemService.equiparItem(ficha.getId(), fichaItem.getId(), mestre.getId());

        // Assert — verificar FichaBonus.itens == 2
        List<FichaBonus> bonusApos = fichaBonusRepository.findByFichaIdWithConfig(ficha.getId());
        FichaBonus fichaBonus = bonusApos.stream()
                .filter(b -> b.getBonusConfig().getId().equals(bonusAlvo.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("FichaBonus não encontrado para o BonusConfig alvo"));

        assertThat(fichaBonus.getItens()).isEqualTo(2);
    }

    // =========================================================
    // CALC-ITEM-02: Desequipar reverte bônus (idempotência)
    // =========================================================

    @Test
    @DisplayName("CALC-ITEM-02: Desequipar item com BONUS_DERIVADO deve reverter FichaBonus.itens para 0")
    void desequiparItem_deveReverterFichaBonusItens() {
        // Arrange
        List<BonusConfig> bonusConfigs = bonusConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId());
        BonusConfig bonusAlvo = bonusConfigs.get(0);

        ItemConfig itemConfig = criarItemConfigComBonusDerivado(bonusAlvo, 3);

        FichaItem fichaItem = fichaItemService.adicionarItem(ficha.getId(),
                new FichaItemAdicionarRequest(itemConfig.getId(), 1, null, false), mestre.getId());

        fichaItemService.equiparItem(ficha.getId(), fichaItem.getId(), mestre.getId());

        // Verificar que o bônus foi aplicado
        FichaBonus bonusEquipado = fichaBonusRepository.findByFichaIdWithConfig(ficha.getId()).stream()
                .filter(b -> b.getBonusConfig().getId().equals(bonusAlvo.getId()))
                .findFirst().orElseThrow();
        assertThat(bonusEquipado.getItens()).isEqualTo(3);

        // Act — desequipar
        fichaItemService.desequiparItem(ficha.getId(), fichaItem.getId(), mestre.getId());

        // Assert — FichaBonus.itens deve ser 0 após desequipar
        List<FichaBonus> bonusApos = fichaBonusRepository.findByFichaIdWithConfig(ficha.getId());
        FichaBonus fichaBonus = bonusApos.stream()
                .filter(b -> b.getBonusConfig().getId().equals(bonusAlvo.getId()))
                .findFirst().orElseThrow();

        assertThat(fichaBonus.getItens()).isEqualTo(0);
    }

    // =========================================================
    // CALC-ITEM-03: Dois itens acumulam no mesmo alvo
    // =========================================================

    @Test
    @DisplayName("CALC-ITEM-03: Dois itens equipados com BONUS_DERIVADO valorFixo=1 cada devem resultar em FichaBonus.itens == 2")
    void doisItensEquipados_devemAcumularBonusDerivado() {
        // Arrange
        List<BonusConfig> bonusConfigs = bonusConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId());
        BonusConfig bonusAlvo = bonusConfigs.get(0);

        ItemConfig item1Config = criarItemConfigComBonusDerivado(bonusAlvo, 1);
        ItemConfig item2Config = criarItemConfigComBonusDerivado(bonusAlvo, 1);

        FichaItem item1 = fichaItemService.adicionarItem(ficha.getId(),
                new FichaItemAdicionarRequest(item1Config.getId(), 1, null, false), mestre.getId());
        FichaItem item2 = fichaItemService.adicionarItem(ficha.getId(),
                new FichaItemAdicionarRequest(item2Config.getId(), 1, null, false), mestre.getId());

        // Act — equipar os dois itens
        fichaItemService.equiparItem(ficha.getId(), item1.getId(), mestre.getId());
        fichaItemService.equiparItem(ficha.getId(), item2.getId(), mestre.getId());

        // Assert — FichaBonus.itens deve ser 1+1 = 2
        FichaBonus fichaBonus = fichaBonusRepository.findByFichaIdWithConfig(ficha.getId()).stream()
                .filter(b -> b.getBonusConfig().getId().equals(bonusAlvo.getId()))
                .findFirst().orElseThrow();

        assertThat(fichaBonus.getItens()).isEqualTo(2);
    }

    // =========================================================
    // CALC-ITEM-04: Recalcular duas vezes não acumula
    // =========================================================

    @Test
    @DisplayName("CALC-ITEM-04: Chamar recalcularFicha() duas vezes não deve acumular bônus (idempotência)")
    void recalcularDuasVezes_naoDeveAcumular() {
        // Arrange
        List<BonusConfig> bonusConfigs = bonusConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId());
        BonusConfig bonusAlvo = bonusConfigs.get(0);

        ItemConfig itemConfig = criarItemConfigComBonusDerivado(bonusAlvo, 5);

        FichaItem fichaItem = fichaItemService.adicionarItem(ficha.getId(),
                new FichaItemAdicionarRequest(itemConfig.getId(), 1, null, false), mestre.getId());
        fichaItemService.equiparItem(ficha.getId(), fichaItem.getId(), mestre.getId());

        // Verificar estado após primeiro recalculo (disparado por equiparItem)
        FichaBonus bonusApos1 = fichaBonusRepository.findByFichaIdWithConfig(ficha.getId()).stream()
                .filter(b -> b.getBonusConfig().getId().equals(bonusAlvo.getId()))
                .findFirst().orElseThrow();
        assertThat(bonusApos1.getItens()).isEqualTo(5);

        // Act — recalcular novamente diretamente
        fichaService.recalcularFicha(ficha.getId());

        // Assert — FichaBonus.itens ainda deve ser 5, não 10
        FichaBonus bonusApos2 = fichaBonusRepository.findByFichaIdWithConfig(ficha.getId()).stream()
                .filter(b -> b.getBonusConfig().getId().equals(bonusAlvo.getId()))
                .findFirst().orElseThrow();
        assertThat(bonusApos2.getItens()).isEqualTo(5);
    }

    // =========================================================
    // CALC-ITEM-05: Item com duracaoAtual=0 é ignorado
    // =========================================================

    @Test
    @DisplayName("CALC-ITEM-05: Item com duracaoAtual=0 não deve contribuir para o cálculo mesmo com equipado=true")
    void itemComDuracaoZero_deveSerIgnorado() {
        // Arrange
        List<BonusConfig> bonusConfigs = bonusConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId());
        BonusConfig bonusAlvo = bonusConfigs.get(0);

        ItemConfig itemConfig = criarItemConfigComBonusDerivado(bonusAlvo, 4);

        // Adicionar item e marcar como equipado=true + duracaoAtual=0 diretamente no repositório
        // (simula cenário de item quebrado que ficou com equipado=true por algum motivo)
        FichaItem fichaItem = fichaItemRepository.save(FichaItem.builder()
                .ficha(ficha)
                .itemConfig(itemConfig)
                .nome(itemConfig.getNome())
                .equipado(true)
                .duracaoAtual(0)
                .quantidade(1)
                .build());

        entityManager.flush();
        entityManager.clear();

        // Act — recalcular
        fichaService.recalcularFicha(ficha.getId());

        // Assert — FichaBonus.itens deve ser 0 (item quebrado ignorado)
        FichaBonus fichaBonus = fichaBonusRepository.findByFichaIdWithConfig(ficha.getId()).stream()
                .filter(b -> b.getBonusConfig().getId().equals(bonusAlvo.getId()))
                .findFirst().orElseThrow();

        assertThat(fichaBonus.getItens()).isEqualTo(0);
        // Confirmar que o item ainda existe como equipado=true no banco (não foi modificado)
        FichaItem itemNoBanco = fichaItemRepository.findById(fichaItem.getId()).orElseThrow();
        assertThat(itemNoBanco.isEquipado()).isTrue();
        assertThat(itemNoBanco.getDuracaoAtual()).isEqualTo(0);
    }

    // =========================================================
    // CALC-ITEM-06: Item customizado (itemConfig=null) não causa NPE
    // =========================================================

    @Test
    @DisplayName("CALC-ITEM-06: Item customizado (itemConfig=null) equipado não deve causar NullPointerException")
    void itemCustomizadoEquipado_naoDeveCausarNPE() {
        // Arrange — criar item customizado diretamente, marcado como equipado
        fichaItemRepository.save(FichaItem.builder()
                .ficha(ficha)
                .itemConfig(null)  // item customizado sem ItemConfig
                .nome("Talismã Mágico")
                .equipado(true)
                .quantidade(1)
                .build());

        entityManager.flush();
        entityManager.clear();

        // Act & Assert — não deve lançar exceção
        fichaService.recalcularFicha(ficha.getId());

        // Confirmar que a ficha ainda existe e o recálculo completou sem erro
        assertThat(fichaRepository.findById(ficha.getId())).isPresent();
    }

    // =========================================================
    // CALC-ITEM-07: BONUS_VIDA aplica bônus correto
    // =========================================================

    @Test
    @DisplayName("CALC-ITEM-07: Equipar item com BONUS_VIDA valorFixo=5 deve incrementar FichaVida.itens em 5")
    void equiparItemComBonusVida_deveIncrementarFichaVidaItens() {
        // Arrange
        ItemConfig itemConfig = criarItemConfigComBonusVida(5);

        FichaItem fichaItem = fichaItemService.adicionarItem(ficha.getId(),
                new FichaItemAdicionarRequest(itemConfig.getId(), 1, null, false), mestre.getId());

        // Act
        fichaItemService.equiparItem(ficha.getId(), fichaItem.getId(), mestre.getId());

        // Assert
        FichaVida vidaApos = fichaVidaRepository.findByFichaId(ficha.getId()).orElseThrow();
        assertThat(vidaApos.getItens()).isEqualTo(5);
    }

    // =========================================================
    // CALC-ITEM-08: BONUS_ESSENCIA aplica bônus correto
    // =========================================================

    @Test
    @DisplayName("CALC-ITEM-08: Equipar item com BONUS_ESSENCIA valorFixo=3 deve incrementar FichaEssencia.itens em 3")
    void equiparItemComBonusEssencia_deveIncrementarFichaEssenciaItens() {
        // Arrange
        ItemConfig itemConfig = criarItemConfigComBonusEssencia(3);

        FichaItem fichaItem = fichaItemService.adicionarItem(ficha.getId(),
                new FichaItemAdicionarRequest(itemConfig.getId(), 1, null, false), mestre.getId());

        // Act
        fichaItemService.equiparItem(ficha.getId(), fichaItem.getId(), mestre.getId());

        // Assert
        FichaEssencia essenciaApos = fichaEssenciaRepository.findByFichaId(ficha.getId()).orElseThrow();
        assertThat(essenciaApos.getItens()).isEqualTo(3);
    }

    // =========================================================
    // CALC-ITEM-09: Remover item equipado dispara recálculo
    // =========================================================

    @Test
    @DisplayName("CALC-ITEM-09: Remover item equipado com BONUS_DERIVADO deve zerar FichaBonus.itens")
    void removerItemEquipado_deveZerarBonusItens() {
        // Arrange
        List<BonusConfig> bonusConfigs = bonusConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId());
        BonusConfig bonusAlvo = bonusConfigs.get(0);

        ItemConfig itemConfig = criarItemConfigComBonusDerivado(bonusAlvo, 6);

        FichaItem fichaItem = fichaItemService.adicionarItem(ficha.getId(),
                new FichaItemAdicionarRequest(itemConfig.getId(), 1, null, false), mestre.getId());
        fichaItemService.equiparItem(ficha.getId(), fichaItem.getId(), mestre.getId());

        // Confirmar bônus aplicado
        FichaBonus bonusAntes = fichaBonusRepository.findByFichaIdWithConfig(ficha.getId()).stream()
                .filter(b -> b.getBonusConfig().getId().equals(bonusAlvo.getId()))
                .findFirst().orElseThrow();
        assertThat(bonusAntes.getItens()).isEqualTo(6);

        // Act — remover o item equipado (deve disparar recálculo)
        fichaItemService.removerItem(ficha.getId(), fichaItem.getId(), mestre.getId());

        // Assert — FichaBonus.itens deve ser 0
        FichaBonus bonusApos = fichaBonusRepository.findByFichaIdWithConfig(ficha.getId()).stream()
                .filter(b -> b.getBonusConfig().getId().equals(bonusAlvo.getId()))
                .findFirst().orElseThrow();
        assertThat(bonusApos.getItens()).isEqualTo(0);
    }

    // =========================================================
    // CALC-ITEM-10: Durabilidade 0 auto-desequipa e recalcula
    // =========================================================

    @Test
    @DisplayName("CALC-ITEM-10: Item com durabilidade decrementada a 0 deve ser desequipado e FichaBonus.itens zerado")
    void decrementarDuracaoAZero_deveDesequiparERecalcular() {
        // Arrange
        List<BonusConfig> bonusConfigs = bonusConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId());
        BonusConfig bonusAlvo = bonusConfigs.get(0);

        itemCounter++;
        ItemConfig itemConfig = itemConfigRepository.save(ItemConfig.builder()
                .jogo(jogo)
                .nome("Espada Quebrável v" + itemCounter)
                .raridade(raridadeComum)
                .tipo(tipoItem)
                .peso(new BigDecimal("1.50"))
                .nivelMinimo(1)
                .duracaoPadrao(10)
                .ordemExibicao(itemCounter)
                .build());

        itemEfeitoRepository.save(ItemEfeito.builder()
                .itemConfig(itemConfig)
                .tipoEfeito(TipoItemEfeito.BONUS_DERIVADO)
                .bonusAlvo(bonusAlvo)
                .valorFixo(7)
                .build());

        entityManager.flush();
        entityManager.clear();

        FichaItem fichaItem = fichaItemService.adicionarItem(ficha.getId(),
                new FichaItemAdicionarRequest(itemConfig.getId(), 1, null, false), mestre.getId());
        fichaItem.setDuracaoAtual(3);
        fichaItemRepository.save(fichaItem);
        entityManager.flush();
        entityManager.clear();

        fichaItemService.equiparItem(ficha.getId(), fichaItem.getId(), mestre.getId());

        // Confirmar bônus aplicado
        FichaBonus bonusAntes = fichaBonusRepository.findByFichaIdWithConfig(ficha.getId()).stream()
                .filter(b -> b.getBonusConfig().getId().equals(bonusAlvo.getId()))
                .findFirst().orElseThrow();
        assertThat(bonusAntes.getItens()).isEqualTo(7);

        // Act — decrementar durabilidade até 0 (auto-desequipa)
        fichaItemService.decrementarDurabilidade(
                ficha.getId(), fichaItem.getId(),
                new FichaItemDuracaoRequest(10, false), mestre.getId());

        // Assert — item desequipado e FichaBonus.itens zerado
        FichaItem itemAtualizado = fichaItemRepository.findById(fichaItem.getId()).orElseThrow();
        assertThat(itemAtualizado.isEquipado()).isFalse();
        assertThat(itemAtualizado.getDuracaoAtual()).isEqualTo(0);

        FichaBonus bonusApos = fichaBonusRepository.findByFichaIdWithConfig(ficha.getId()).stream()
                .filter(b -> b.getBonusConfig().getId().equals(bonusAlvo.getId()))
                .findFirst().orElseThrow();
        assertThat(bonusApos.getItens()).isEqualTo(0);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private ItemConfig criarItemConfigComBonusDerivado(BonusConfig bonusAlvo, int valorFixo) {
        itemCounter++;
        ItemConfig itemConfig = itemConfigRepository.save(ItemConfig.builder()
                .jogo(jogo)
                .nome("Item Derivado " + valorFixo + " v" + itemCounter)
                .raridade(raridadeComum)
                .tipo(tipoItem)
                .peso(new BigDecimal("0.50"))
                .nivelMinimo(1)
                .ordemExibicao(itemCounter)
                .build());

        itemEfeitoRepository.save(ItemEfeito.builder()
                .itemConfig(itemConfig)
                .tipoEfeito(TipoItemEfeito.BONUS_DERIVADO)
                .bonusAlvo(bonusAlvo)
                .valorFixo(valorFixo)
                .build());

        // Flush e clear para garantir que a query findEquipadosWithEfeitos
        // encontre os efeitos recém-criados no banco (evita L1 cache sem efeitos)
        entityManager.flush();
        entityManager.clear();

        return itemConfigRepository.findById(itemConfig.getId()).orElseThrow();
    }

    private ItemConfig criarItemConfigComBonusVida(int valorFixo) {
        itemCounter++;
        ItemConfig itemConfig = itemConfigRepository.save(ItemConfig.builder()
                .jogo(jogo)
                .nome("Item Vida " + valorFixo + " v" + itemCounter)
                .raridade(raridadeComum)
                .tipo(tipoItem)
                .peso(new BigDecimal("0.30"))
                .nivelMinimo(1)
                .ordemExibicao(itemCounter)
                .build());

        itemEfeitoRepository.save(ItemEfeito.builder()
                .itemConfig(itemConfig)
                .tipoEfeito(TipoItemEfeito.BONUS_VIDA)
                .valorFixo(valorFixo)
                .build());

        entityManager.flush();
        entityManager.clear();

        return itemConfigRepository.findById(itemConfig.getId()).orElseThrow();
    }

    private ItemConfig criarItemConfigComBonusEssencia(int valorFixo) {
        itemCounter++;
        ItemConfig itemConfig = itemConfigRepository.save(ItemConfig.builder()
                .jogo(jogo)
                .nome("Item Essencia " + valorFixo + " v" + itemCounter)
                .raridade(raridadeComum)
                .tipo(tipoItem)
                .peso(new BigDecimal("0.20"))
                .nivelMinimo(1)
                .ordemExibicao(itemCounter)
                .build());

        itemEfeitoRepository.save(ItemEfeito.builder()
                .itemConfig(itemConfig)
                .tipoEfeito(TipoItemEfeito.BONUS_ESSENCIA)
                .valorFixo(valorFixo)
                .build());

        entityManager.flush();
        entityManager.clear();

        return itemConfigRepository.findById(itemConfig.getId()).orElseThrow();
    }

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
