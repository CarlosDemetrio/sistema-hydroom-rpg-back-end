package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaItemAdicionarRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaItemCustomizadoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaItemDuracaoRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.BusinessException;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.ItemConfigService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.RaridadeItemConfigService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.TipoItemConfigService;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para FichaItemService.
 *
 * <p>Cobre as regras de negócio do inventário de itens:</p>
 * <ul>
 *   <li>Jogador pode adicionar itens de raridade Comum (podeJogadorAdicionar=true)</li>
 *   <li>Jogador não pode adicionar itens de raridade restrita</li>
 *   <li>Mestre pode forçar adição de qualquer item</li>
 *   <li>Item quebrado (duracaoAtual=0) não pode ser equipado</li>
 *   <li>Durabilidade 0 auto-desequipa o item</li>
 *   <li>Mestre pode adicionar item customizado (sem itemConfig)</li>
 *   <li>Soft delete no removerItem</li>
 *   <li>Controle de acesso: jogador só acessa suas fichas</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaItemService - Testes de Integração")
class FichaItemServiceIntegrationTest {

    @Autowired
    private FichaItemService fichaItemService;

    @Autowired
    private FichaService fichaService;

    @Autowired
    private JogoService jogoService;

    @Autowired
    private ItemConfigService itemConfigService;

    @Autowired
    private FichaItemRepository fichaItemRepository;
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
    @Autowired
    private RaridadeItemConfigRepository raridadeItemConfigRepository;
    @Autowired
    private TipoItemConfigRepository tipoItemConfigRepository;
    @Autowired
    private ItemConfigRepository itemConfigRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Usuario jogador;
    private Usuario outroJogador;
    private Jogo jogo;
    private Ficha fichaDoJogador;
    private RaridadeItemConfig raridadeComum;
    private RaridadeItemConfig raridadeRara;
    private TipoItemConfig tipoItem;
    private ItemConfig itemComum;
    private ItemConfig itemRaro;

    @BeforeEach
    void setUp() {
        // Limpar na ordem correta (dependências primeiro)
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
        jogoParticipanteRepository.deleteAll();
        itemConfigRepository.deleteAll();
        raridadeItemConfigRepository.deleteAll();
        tipoItemConfigRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre Item")
                .email("mestre.item" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-item-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador Item")
                .email("jogador.item" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-item-" + n)
                .role("JOGADOR")
                .build());

        outroJogador = usuarioRepository.save(Usuario.builder()
                .nome("Outro Jogador Item")
                .email("outro.jogador.item" + n + "@test.com")
                .provider("google")
                .providerId("google-outro-jogador-item-" + n)
                .role("JOGADOR")
                .build());

        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Campanha Itens " + n)
                .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(jogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(outroJogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        fichaDoJogador = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Personagem do Jogador",
                        jogador.getId(), null, null, null, null, null, false));

        // Criar raridades e tipo diretamente (sem JogoService para ter controle total)
        raridadeComum = raridadeItemConfigRepository.save(RaridadeItemConfig.builder()
                .jogo(jogo)
                .nome("Comum Teste " + n)
                .cor("#9d9d9d")
                .ordemExibicao(1)
                .podeJogadorAdicionar(true)
                .build());

        raridadeRara = raridadeItemConfigRepository.save(RaridadeItemConfig.builder()
                .jogo(jogo)
                .nome("Raro Teste " + n)
                .cor("#0070dd")
                .ordemExibicao(2)
                .podeJogadorAdicionar(false)
                .build());

        tipoItem = tipoItemConfigRepository.save(TipoItemConfig.builder()
                .jogo(jogo)
                .nome("Espada Teste " + n)
                .categoria(CategoriaItem.ARMA)
                .requerDuasMaos(false)
                .ordemExibicao(1)
                .build());

        itemComum = itemConfigRepository.save(ItemConfig.builder()
                .jogo(jogo)
                .nome("Espada Curta Comum " + n)
                .raridade(raridadeComum)
                .tipo(tipoItem)
                .peso(new BigDecimal("1.50"))
                .nivelMinimo(1)
                .ordemExibicao(1)
                .build());

        itemRaro = itemConfigRepository.save(ItemConfig.builder()
                .jogo(jogo)
                .nome("Espada Longa Rara " + n)
                .raridade(raridadeRara)
                .tipo(tipoItem)
                .peso(new BigDecimal("2.50"))
                .nivelMinimo(1)
                .ordemExibicao(2)
                .build());
    }

    // =========================================================
    // TESTES DE ADIÇÃO DE ITEM
    // =========================================================

    @Test
    @DisplayName("Jogador deve adicionar item de raridade que permite jogador adicionar")
    void deveJogadorAdicionarItemDeRaridadePermitida() {
        // Arrange
        autenticarComo(jogador);
        var request = new FichaItemAdicionarRequest(itemComum.getId(), 1, "Minha primeira espada", false);

        // Act
        FichaItem resultado = fichaItemService.adicionarItem(fichaDoJogador.getId(), request, jogador.getId());

        // Assert
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getNome()).isEqualTo(itemComum.getNome());
        assertThat(resultado.getItemConfig().getId()).isEqualTo(itemComum.getId());
        assertThat(resultado.getQuantidade()).isEqualTo(1);
        assertThat(resultado.getNotas()).isEqualTo("Minha primeira espada");
        assertThat(resultado.isEquipado()).isFalse();
    }

    @Test
    @DisplayName("Jogador não deve adicionar item de raridade restrita")
    void deveImpedirJogadorDeAdicionarItemDeRaridadeRestrita() {
        // Arrange
        autenticarComo(jogador);
        var request = new FichaItemAdicionarRequest(itemRaro.getId(), 1, null, false);

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> fichaItemService.adicionarItem(fichaDoJogador.getId(), request, jogador.getId()));
    }

    @Test
    @DisplayName("Mestre deve adicionar item de raridade restrita sem forçar")
    void deveMestreAdicionarItemDeRaridadeRestrita() {
        // Arrange
        autenticarComo(mestre);
        var request = new FichaItemAdicionarRequest(itemRaro.getId(), 1, "Presente do Mestre", false);

        // Act
        FichaItem resultado = fichaItemService.adicionarItem(fichaDoJogador.getId(), request, mestre.getId());

        // Assert
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getNome()).isEqualTo(itemRaro.getNome());
        assertThat(resultado.getNotas()).isEqualTo("Presente do Mestre");
    }

    @Test
    @DisplayName("Jogador não deve adicionar item em ficha de outro jogador")
    void deveImpedirJogadorDeAdicionarItemEmFichaDeOutro() {
        // Arrange
        autenticarComo(mestre);
        Ficha fichaOutroJogador = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Ficha do Outro",
                        outroJogador.getId(), null, null, null, null, null, false));

        autenticarComo(jogador);
        var request = new FichaItemAdicionarRequest(itemComum.getId(), 1, null, false);

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> fichaItemService.adicionarItem(fichaOutroJogador.getId(), request, jogador.getId()));
    }

    // =========================================================
    // TESTES DE ITEM CUSTOMIZADO
    // =========================================================

    @Test
    @DisplayName("Mestre deve adicionar item customizado com sucesso")
    void deveMestreAdicionarItemCustomizado() {
        // Arrange
        autenticarComo(mestre);
        var request = new FichaItemCustomizadoRequest(
                "Amuleto Místico", raridadeComum.getId(), new BigDecimal("0.10"), 1, "Item mágico único");

        // Act
        FichaItem resultado = fichaItemService.adicionarItemCustomizado(
                fichaDoJogador.getId(), request, mestre.getId());

        // Assert
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Amuleto Místico");
        assertThat(resultado.getItemConfig()).isNull();
        assertThat(resultado.getRaridade().getId()).isEqualTo(raridadeComum.getId());
        assertThat(resultado.getPeso()).isEqualByComparingTo("0.10");
        assertThat(resultado.getNotas()).isEqualTo("Item mágico único");
    }

    @Test
    @DisplayName("Jogador não deve adicionar item customizado")
    void deveImpedirJogadorDeAdicionarItemCustomizado() {
        // Arrange
        autenticarComo(jogador);
        var request = new FichaItemCustomizadoRequest(
                "Item Indevido", raridadeComum.getId(), new BigDecimal("0.10"), 1, null);

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> fichaItemService.adicionarItemCustomizado(fichaDoJogador.getId(), request, jogador.getId()));
    }

    // =========================================================
    // TESTES DE EQUIPAR / DESEQUIPAR
    // =========================================================

    @Test
    @DisplayName("Deve equipar item com sucesso")
    void deveEquiparItem() {
        // Arrange
        autenticarComo(jogador);
        FichaItem fichaItem = fichaItemService.adicionarItem(fichaDoJogador.getId(),
                new FichaItemAdicionarRequest(itemComum.getId(), 1, null, false), jogador.getId());

        // Act
        FichaItem equipado = fichaItemService.equiparItem(fichaDoJogador.getId(), fichaItem.getId(), jogador.getId());

        // Assert
        assertThat(equipado.isEquipado()).isTrue();
    }

    @Test
    @DisplayName("Item com durabilidade zero não pode ser equipado")
    void deveImpedirEquiparItemComDuracaoZero() {
        // Arrange
        autenticarComo(mestre);
        FichaItem fichaItem = fichaItemService.adicionarItem(fichaDoJogador.getId(),
                new FichaItemAdicionarRequest(itemComum.getId(), 1, null, false), mestre.getId());

        // Setar durabilidade 0 diretamente
        fichaItem.setDuracaoAtual(0);
        fichaItemRepository.save(fichaItem);

        // Act & Assert
        autenticarComo(jogador);
        assertThrows(BusinessException.class,
                () -> fichaItemService.equiparItem(fichaDoJogador.getId(), fichaItem.getId(), jogador.getId()));
    }

    @Test
    @DisplayName("Deve desequipar item com sucesso")
    void deveDesequiparItem() {
        // Arrange
        autenticarComo(jogador);
        FichaItem fichaItem = fichaItemService.adicionarItem(fichaDoJogador.getId(),
                new FichaItemAdicionarRequest(itemComum.getId(), 1, null, false), jogador.getId());
        fichaItemService.equiparItem(fichaDoJogador.getId(), fichaItem.getId(), jogador.getId());

        // Act
        FichaItem desequipado = fichaItemService.desequiparItem(fichaDoJogador.getId(), fichaItem.getId(), jogador.getId());

        // Assert
        assertThat(desequipado.isEquipado()).isFalse();
    }

    // =========================================================
    // TESTES DE DURABILIDADE
    // =========================================================

    @Test
    @DisplayName("Mestre deve decrementar durabilidade de item")
    void deveMestreDecrementarDurabilidade() {
        // Arrange
        autenticarComo(mestre);
        FichaItem fichaItem = fichaItemService.adicionarItem(fichaDoJogador.getId(),
                new FichaItemAdicionarRequest(itemComum.getId(), 1, null, false), mestre.getId());
        fichaItem.setDuracaoAtual(10);
        fichaItemRepository.save(fichaItem);

        // Act
        FichaItem resultado = fichaItemService.decrementarDurabilidade(
                fichaDoJogador.getId(), fichaItem.getId(),
                new FichaItemDuracaoRequest(3, false), mestre.getId());

        // Assert
        assertThat(resultado.getDuracaoAtual()).isEqualTo(7);
    }

    @Test
    @DisplayName("Durabilidade não deve ficar negativa ao decrementar além do mínimo")
    void deveLimitarDuracaoAoMinimo() {
        // Arrange
        autenticarComo(mestre);
        FichaItem fichaItem = fichaItemService.adicionarItem(fichaDoJogador.getId(),
                new FichaItemAdicionarRequest(itemComum.getId(), 1, null, false), mestre.getId());
        fichaItem.setDuracaoAtual(2);
        fichaItemRepository.save(fichaItem);

        // Act — decremento maior do que a durabilidade atual
        FichaItem resultado = fichaItemService.decrementarDurabilidade(
                fichaDoJogador.getId(), fichaItem.getId(),
                new FichaItemDuracaoRequest(10, false), mestre.getId());

        // Assert
        assertThat(resultado.getDuracaoAtual()).isEqualTo(0);
    }

    @Test
    @DisplayName("Item equipado deve ser desequipado automaticamente ao atingir durabilidade zero")
    void deveDesequiparAutoAoDuracaoZero() {
        // Arrange
        autenticarComo(mestre);
        FichaItem fichaItem = fichaItemService.adicionarItem(fichaDoJogador.getId(),
                new FichaItemAdicionarRequest(itemComum.getId(), 1, null, false), mestre.getId());
        fichaItem.setDuracaoAtual(2);
        fichaItem.setEquipado(true);
        fichaItemRepository.save(fichaItem);

        // Act — decremento leva a 0
        FichaItem resultado = fichaItemService.decrementarDurabilidade(
                fichaDoJogador.getId(), fichaItem.getId(),
                new FichaItemDuracaoRequest(5, false), mestre.getId());

        // Assert
        assertThat(resultado.getDuracaoAtual()).isEqualTo(0);
        assertThat(resultado.isEquipado()).isFalse();
    }

    @Test
    @DisplayName("Mestre deve restaurar durabilidade de item")
    void deveMestreRestaurarDurabilidade() {
        // Arrange — item com duracaoPadrao definido via ItemConfig
        ItemConfig itemComDuracao = itemConfigRepository.save(ItemConfig.builder()
                .jogo(jogo)
                .nome("Escudo Teste " + counter.get())
                .raridade(raridadeComum)
                .tipo(tipoItem)
                .peso(new BigDecimal("3.00"))
                .nivelMinimo(1)
                .duracaoPadrao(20)
                .ordemExibicao(10)
                .build());

        autenticarComo(mestre);
        FichaItem fichaItem = fichaItemService.adicionarItem(fichaDoJogador.getId(),
                new FichaItemAdicionarRequest(itemComDuracao.getId(), 1, null, false), mestre.getId());
        fichaItem.setDuracaoAtual(5);
        fichaItemRepository.save(fichaItem);

        // Act — restaurar
        FichaItem resultado = fichaItemService.decrementarDurabilidade(
                fichaDoJogador.getId(), fichaItem.getId(),
                new FichaItemDuracaoRequest(1, true), mestre.getId());

        // Assert — durabilidade restaurada ao padrão do itemConfig
        assertThat(resultado.getDuracaoAtual()).isEqualTo(20);
    }

    // =========================================================
    // TESTES DE LISTAGEM
    // =========================================================

    @Test
    @DisplayName("Deve listar itens do inventário da ficha separados por equipado e em estoque")
    void deveListarItensDoInventario() {
        // Arrange
        autenticarComo(mestre);
        FichaItem item1 = fichaItemService.adicionarItem(fichaDoJogador.getId(),
                new FichaItemAdicionarRequest(itemComum.getId(), 2, "Equipada", false), mestre.getId());
        FichaItem item2 = fichaItemService.adicionarItem(fichaDoJogador.getId(),
                new FichaItemAdicionarRequest(itemRaro.getId(), 1, "Em estoque", false), mestre.getId());

        fichaItemService.equiparItem(fichaDoJogador.getId(), item1.getId(), mestre.getId());

        // Act
        autenticarComo(jogador);
        List<FichaItem> itens = fichaItemService.listarItens(fichaDoJogador.getId(), jogador.getId());

        // Assert
        assertThat(itens).hasSize(2);
        assertThat(itens.stream().filter(FichaItem::isEquipado)).hasSize(1);
        assertThat(itens.stream().filter(i -> !i.isEquipado())).hasSize(1);
    }

    @Test
    @DisplayName("Jogador não deve listar itens de ficha de outro jogador")
    void deveImpedirJogadorDeListarInventarioDeOutro() {
        // Arrange
        autenticarComo(mestre);
        Ficha fichaOutroJogador = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Ficha do Outro",
                        outroJogador.getId(), null, null, null, null, null, false));

        // Act & Assert
        autenticarComo(jogador);
        assertThrows(ForbiddenException.class,
                () -> fichaItemService.listarItens(fichaOutroJogador.getId(), jogador.getId()));
    }

    // =========================================================
    // TESTES DE REMOÇÃO
    // =========================================================

    @Test
    @DisplayName("Deve remover item com soft delete")
    void deveRemoverItemComSoftDelete() {
        // Arrange
        autenticarComo(jogador);
        FichaItem fichaItem = fichaItemService.adicionarItem(fichaDoJogador.getId(),
                new FichaItemAdicionarRequest(itemComum.getId(), 1, null, false), jogador.getId());
        Long itemId = fichaItem.getId();

        // Act
        fichaItemService.removerItem(fichaDoJogador.getId(), itemId, jogador.getId());

        // Assert — item não aparece na listagem (SQLRestriction filtra)
        List<FichaItem> itens = fichaItemService.listarItens(fichaDoJogador.getId(), jogador.getId());
        assertThat(itens).noneMatch(i -> i.getId().equals(itemId));
    }

    @Test
    @DisplayName("Item pertencente a outra ficha não pode ser manipulado")
    void deveImpedirManipulacaoDeItemDeOutraFicha() {
        // Arrange
        autenticarComo(mestre);
        Ficha fichaOutroJogador = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Ficha do Outro",
                        outroJogador.getId(), null, null, null, null, null, false));

        FichaItem itemOutraFicha = fichaItemService.adicionarItem(fichaOutroJogador.getId(),
                new FichaItemAdicionarRequest(itemComum.getId(), 1, null, false), mestre.getId());

        // Act & Assert — tentar equipar item de outra ficha
        autenticarComo(jogador);
        assertThrows(ForbiddenException.class,
                () -> fichaItemService.equiparItem(fichaDoJogador.getId(), itemOutraFicha.getId(), jogador.getId()));
    }

    // =========================================================
    // TESTES DE PESO
    // =========================================================

    @Test
    @DisplayName("Deve calcular peso total do inventário corretamente")
    void deveCalcularPesoTotal() {
        // Arrange — item1 usa peso do itemConfig (1.50), item2 tem peso sobrescrito (0.50)
        autenticarComo(mestre);
        FichaItem item1 = fichaItemService.adicionarItem(fichaDoJogador.getId(),
                new FichaItemAdicionarRequest(itemComum.getId(), 2, null, false), mestre.getId());
        // item1 usa itemConfig.peso=1.50 * quantidade=2 = 3.00

        FichaItem itemCustomizado = fichaItemService.adicionarItemCustomizado(fichaDoJogador.getId(),
                new FichaItemCustomizadoRequest("Anel", raridadeComum.getId(), new BigDecimal("0.10"), 3, null),
                mestre.getId());
        // itemCustomizado.peso=0.10 * quantidade=3 = 0.30

        // Act
        List<FichaItem> itens = fichaItemService.listarItens(fichaDoJogador.getId(), mestre.getId());
        BigDecimal pesoTotal = fichaItemService.calcularPesoTotal(itens);

        // Assert: 3.00 + 0.30 = 3.30
        assertThat(pesoTotal).isEqualByComparingTo("3.30");
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
