package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ClasseEquipamentoInicialRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.BusinessException;
import br.com.hydroom.rpg.fichacontrolador.model.ClasseEquipamentoInicial;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.model.ItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.RaridadeItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.TipoItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.SubcategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.repository.ClasseEquipamentoInicialRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoClasseRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.RaridadeItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.TipoItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.ClasseEquipamentoInicialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para ClasseEquipamentoInicialService.
 *
 * <p>Cobre regras de negócio de equipamentos iniciais de classes:</p>
 * <ul>
 *   <li>CEI-01: criar com item do mesmo jogo → sucesso</li>
 *   <li>CEI-02: criar com item de outro jogo → BusinessException (RN-T3-01)</li>
 *   <li>CEI-03: criar múltiplos itens em grupo de escolha → todos listados</li>
 *   <li>CEI-04: listar ordena obrigatório=true antes dos opcionais</li>
 *   <li>CEI-05: deletar remove item da listagem (soft delete)</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("ClasseEquipamentoInicialService - Testes de Integração")
class ClasseEquipamentoInicialServiceIntegrationTest {

    @Autowired
    private ClasseEquipamentoInicialService equipamentoService;

    @Autowired
    private ClasseEquipamentoInicialRepository equipamentoRepository;

    @Autowired
    private ConfiguracaoClasseRepository classeRepository;

    @Autowired
    private ItemConfigRepository itemConfigRepository;

    @Autowired
    private RaridadeItemConfigRepository raridadeRepository;

    @Autowired
    private TipoItemConfigRepository tipoRepository;

    @Autowired
    private JogoRepository jogoRepository;

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private Jogo jogo;
    private Jogo outroJogo;
    private ClassePersonagem classeJogo;
    private RaridadeItemConfig raridade;
    private RaridadeItemConfig raridadeOutroJogo;
    private TipoItemConfig tipo;
    private TipoItemConfig tipoOutroJogo;

    @BeforeEach
    void setUp() {
        // Limpar dados para isolamento
        equipamentoRepository.deleteAll();
        itemConfigRepository.deleteAll();
        raridadeRepository.deleteAll();
        tipoRepository.deleteAll();
        classeRepository.deleteAll();
        jogoRepository.deleteAll();

        jogo = jogoRepository.save(Jogo.builder()
            .nome("Campanha CEI")
            .descricao("Jogo para testes de equipamento inicial")
            .dataInicio(LocalDate.now())
            .jogoAtivo(true)
            .build());

        outroJogo = jogoRepository.save(Jogo.builder()
            .nome("Campanha CEI Outro")
            .descricao("Outro jogo para isolamento de testes")
            .dataInicio(LocalDate.now())
            .jogoAtivo(true)
            .build());

        classeJogo = classeRepository.save(ClassePersonagem.builder()
            .jogo(jogo)
            .nome("Guerreiro")
            .descricao("Classe de combate")
            .ordemExibicao(1)
            .build());

        raridade = raridadeRepository.save(RaridadeItemConfig.builder()
            .jogo(jogo)
            .nome("Comum")
            .cor("#9d9d9d")
            .ordemExibicao(1)
            .podeJogadorAdicionar(true)
            .build());

        raridadeOutroJogo = raridadeRepository.save(RaridadeItemConfig.builder()
            .jogo(outroJogo)
            .nome("Comum Outro")
            .cor("#9d9d9d")
            .ordemExibicao(1)
            .podeJogadorAdicionar(true)
            .build());

        tipo = tipoRepository.save(TipoItemConfig.builder()
            .jogo(jogo)
            .nome("Espada")
            .categoria(CategoriaItem.ARMA)
            .subcategoria(SubcategoriaItem.ESPADA)
            .requerDuasMaos(false)
            .ordemExibicao(1)
            .build());

        tipoOutroJogo = tipoRepository.save(TipoItemConfig.builder()
            .jogo(outroJogo)
            .nome("Espada Outro")
            .categoria(CategoriaItem.ARMA)
            .subcategoria(SubcategoriaItem.ESPADA)
            .requerDuasMaos(false)
            .ordemExibicao(1)
            .build());
    }

    private String getUniqueSuffix() {
        return String.valueOf(COUNTER.incrementAndGet());
    }

    private ItemConfig criarItem(Jogo jogoParam, RaridadeItemConfig raridadeParam, TipoItemConfig tipoParam, String nome, int ordem) {
        return itemConfigRepository.save(ItemConfig.builder()
            .jogo(jogoParam)
            .nome(nome)
            .raridade(raridadeParam)
            .tipo(tipoParam)
            .peso(BigDecimal.valueOf(1.00))
            .nivelMinimo(1)
            .ordemExibicao(ordem)
            .build());
    }

    // =========================================================
    // CEI-01: criar com item do mesmo jogo → sucesso
    // =========================================================

    @Test
    @DisplayName("CEI-01: Deve criar equipamento inicial com item do mesmo jogo")
    void deveCriarEquipamentoInicialComItemDoMesmoJogo() {
        // Arrange
        ItemConfig item = criarItem(jogo, raridade, tipo, "Espada Inicial " + getUniqueSuffix(), 1);

        ClasseEquipamentoInicialRequest request = new ClasseEquipamentoInicialRequest(
            item.getId(),
            true,  // obrigatorio
            null,  // grupoEscolha
            1      // quantidade
        );

        // Act
        ClasseEquipamentoInicial criado = equipamentoService.criar(classeJogo.getId(), request);

        // Assert
        assertThat(criado.getId()).isNotNull();
        assertThat(criado.getItemConfig().getId()).isEqualTo(item.getId());
        assertThat(criado.getClasse().getId()).isEqualTo(classeJogo.getId());
        assertThat(criado.isObrigatorio()).isTrue();
        assertThat(criado.getQuantidade()).isEqualTo(1);
    }

    // =========================================================
    // CEI-02: criar com item de outro jogo → BusinessException (RN-T3-01)
    // =========================================================

    @Test
    @DisplayName("CEI-02: Deve lançar BusinessException ao criar equipamento com item de outro jogo")
    void deveLancarExcecaoAoCriarEquipamentoComItemDeOutroJogo() {
        // Arrange
        ItemConfig itemOutroJogo = criarItem(outroJogo, raridadeOutroJogo, tipoOutroJogo,
            "Item Outro Jogo " + getUniqueSuffix(), 1);

        ClasseEquipamentoInicialRequest request = new ClasseEquipamentoInicialRequest(
            itemOutroJogo.getId(), // item de outro jogo!
            true,
            null,
            1
        );

        // Act & Assert
        assertThrows(BusinessException.class,
            () -> equipamentoService.criar(classeJogo.getId(), request));
    }

    // =========================================================
    // CEI-03: criar 3 items no mesmo grupo → todos listados
    // =========================================================

    @Test
    @DisplayName("CEI-03: Deve criar múltiplos equipamentos no mesmo grupo de escolha")
    void deveCriarMultiplosEquipamentosNoMesmoGrupo() {
        // Arrange — 3 itens diferentes do mesmo jogo
        String s = getUniqueSuffix();
        ItemConfig item1 = criarItem(jogo, raridade, tipo, "Espada A " + s, 1);
        ItemConfig item2 = criarItem(jogo, raridade, tipo, "Espada B " + s, 2);
        ItemConfig item3 = criarItem(jogo, raridade, tipo, "Espada C " + s, 3);

        // Act — criar 3 equipamentos no grupo 1
        equipamentoService.criar(classeJogo.getId(),
            new ClasseEquipamentoInicialRequest(item1.getId(), false, 1, 1));
        equipamentoService.criar(classeJogo.getId(),
            new ClasseEquipamentoInicialRequest(item2.getId(), false, 1, 1));
        equipamentoService.criar(classeJogo.getId(),
            new ClasseEquipamentoInicialRequest(item3.getId(), false, 1, 1));

        // Assert
        List<ClasseEquipamentoInicial> resultado = equipamentoService.listar(classeJogo.getId());
        assertThat(resultado).hasSize(3);
        assertThat(resultado)
            .extracting(e -> e.getGrupoEscolha())
            .allMatch(grupo -> grupo != null && grupo == 1);
    }

    // =========================================================
    // CEI-04: listar ordena obrigatório=true antes dos opcionais
    // =========================================================

    @Test
    @DisplayName("CEI-04: listar deve retornar itens obrigatórios antes dos opcionais")
    void deveListarComItensObrigatoriosPrimeiro() {
        // Arrange — criar items na ordem inversa para testar a ordenação
        String s = getUniqueSuffix();
        ItemConfig itemOpcional1 = criarItem(jogo, raridade, tipo, "Adaga " + s, 1);
        ItemConfig itemObrigatorio = criarItem(jogo, raridade, tipo, "Cota de Malha " + s, 2);
        ItemConfig itemOpcional2 = criarItem(jogo, raridade, tipo, "Escudo " + s, 3);

        // Criar primeiro os opcionais, depois o obrigatório
        equipamentoService.criar(classeJogo.getId(),
            new ClasseEquipamentoInicialRequest(itemOpcional1.getId(), false, 1, 1));
        equipamentoService.criar(classeJogo.getId(),
            new ClasseEquipamentoInicialRequest(itemObrigatorio.getId(), true, null, 1));
        equipamentoService.criar(classeJogo.getId(),
            new ClasseEquipamentoInicialRequest(itemOpcional2.getId(), false, 2, 1));

        // Act
        List<ClasseEquipamentoInicial> resultado = equipamentoService.listar(classeJogo.getId());

        // Assert — obrigatório deve ser o primeiro
        assertThat(resultado).hasSize(3);
        assertThat(resultado.get(0).isObrigatorio()).isTrue();
        // Os opcionais vêm depois
        assertThat(resultado.subList(1, 3))
            .extracting(ClasseEquipamentoInicial::isObrigatorio)
            .allMatch(obr -> !obr);
    }

    // =========================================================
    // CEI-05: deletar remove item da listagem (soft delete)
    // =========================================================

    @Test
    @DisplayName("CEI-05: Deve remover equipamento da listagem após soft delete")
    void deveSoftDeletarEquipamentoERemoverDaListagem() {
        // Arrange
        ItemConfig item = criarItem(jogo, raridade, tipo, "Lanca Inicial " + getUniqueSuffix(), 1);
        ClasseEquipamentoInicial criado = equipamentoService.criar(classeJogo.getId(),
            new ClasseEquipamentoInicialRequest(item.getId(), true, null, 1));

        assertThat(equipamentoService.listar(classeJogo.getId())).hasSize(1);

        // Act
        equipamentoService.deletar(classeJogo.getId(), criado.getId());

        // Assert — soft-deleted item não aparece na listagem
        List<ClasseEquipamentoInicial> resultado = equipamentoService.listar(classeJogo.getId());
        assertThat(resultado).isEmpty();

        // Mas ainda existe no banco (soft delete, não hard delete)
        assertThat(equipamentoRepository.findById(criado.getId())).isPresent();
    }
}
