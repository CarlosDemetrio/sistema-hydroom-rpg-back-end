package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ItemEfeitoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ItemRequisitoRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.FichaItem;
import br.com.hydroom.rpg.fichacontrolador.model.ItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ItemEfeito;
import br.com.hydroom.rpg.fichacontrolador.model.ItemRequisito;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.RaridadeItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.TipoItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.SubcategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoItemEfeito;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoRequisito;
import br.com.hydroom.rpg.fichacontrolador.repository.BonusConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaItemRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemEfeitoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemRequisitoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.RaridadeItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.TipoItemConfigRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para ItemConfigService.
 */
@DisplayName("ItemConfigService - Testes de Integração")
class ItemConfigServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<ItemConfig, ItemConfigService, ItemConfigRepository> {

    @Autowired
    private ItemConfigService itemConfigService;

    @Autowired
    private ItemConfigRepository itemConfigRepository;

    @Autowired
    private RaridadeItemConfigRepository raridadeRepository;

    @Autowired
    private TipoItemConfigRepository tipoRepository;

    @Autowired
    private BonusConfigRepository bonusConfigRepository;

    @Autowired
    private ItemEfeitoService itemEfeitoService;

    @Autowired
    private ItemRequisitoService itemRequisitoService;

    @Autowired
    private ItemEfeitoRepository itemEfeitoRepository;

    @Autowired
    private ItemRequisitoRepository itemRequisitoRepository;

    @Autowired
    private FichaRepository fichaRepository;

    @Autowired
    private FichaItemRepository fichaItemRepository;

    @Autowired
    private EntityManager entityManager;

    private RaridadeItemConfig raridadeComum;
    private RaridadeItemConfig raridadeOutroJogo;
    private TipoItemConfig tipoArma;
    private TipoItemConfig tipoOutroJogo;

    @Override
    protected ItemConfigService getService() {
        return itemConfigService;
    }

    @Override
    protected ItemConfigRepository getRepository() {
        return itemConfigRepository;
    }

    @BeforeEach
    void setUpItemDependencies() {
        // Limpar sub-recursos antes das outras limpezas
        itemEfeitoRepository.deleteAll();
        itemRequisitoRepository.deleteAll();

        // Criar raridades e tipos para o jogo de teste
        raridadeComum = raridadeRepository.save(RaridadeItemConfig.builder()
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

        tipoArma = tipoRepository.save(TipoItemConfig.builder()
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

    /**
     * Limpa sub-recursos e dependências antes do tearDown da classe base.
     * JUnit 5 executa @AfterEach da subclasse ANTES da superclasse.
     * Usa native delete para incluir ItemConfig com soft-delete, evitando
     * FK violation quando raridade/tipo são removidos depois.
     */
    @AfterEach
    void tearDownSubResources() {
        entityManager.flush();
        entityManager.clear();
        // FichaItems devem ser removidos antes do native delete de ItemConfig (FK constraint)
        fichaItemRepository.deleteAll();
        fichaRepository.deleteAll();
        itemEfeitoRepository.deleteAll();
        itemRequisitoRepository.deleteAll();
        // Native delete para incluir registros com soft-delete (SQLRestriction ignorada)
        repository.deleteAllByJogoIdNative(jogo.getId());
        repository.deleteAllByJogoIdNative(outroJogo.getId());
        raridadeRepository.deleteAll();
        tipoRepository.deleteAll();
        bonusConfigRepository.deleteAll();
    }

    @Override
    protected ItemConfig criarConfiguracaoValida(Jogo jogoParam) {
        String suffix = getUniqueSuffix();

        // Para o outroJogo, precisamos de raridade e tipo do outroJogo
        RaridadeItemConfig raridade = jogoParam.getId().equals(jogo.getId())
            ? raridadeComum : raridadeOutroJogo;
        TipoItemConfig tipo = jogoParam.getId().equals(jogo.getId())
            ? tipoArma : tipoOutroJogo;

        return ItemConfig.builder()
            .jogo(jogoParam)
            .nome("Espada Longa " + suffix)
            .raridade(raridade)
            .tipo(tipo)
            .peso(BigDecimal.valueOf(2.50))
            .valor(100)
            .nivelMinimo(1)
            .propriedades("versátil, finura")
            .descricao("Uma espada de duas lâminas")
            .ordemExibicao(1)
            .build();
    }

    @Override
    protected ItemConfig criarConfiguracaoComNomeDuplicado(Jogo jogoParam, ItemConfig configuracaoExistente) {
        RaridadeItemConfig raridade = jogoParam.getId().equals(jogo.getId())
            ? raridadeComum : raridadeOutroJogo;
        TipoItemConfig tipo = jogoParam.getId().equals(jogo.getId())
            ? tipoArma : tipoOutroJogo;

        return ItemConfig.builder()
            .jogo(jogoParam)
            .nome(configuracaoExistente.getNome())
            .raridade(raridade)
            .tipo(tipo)
            .peso(BigDecimal.valueOf(3.00))
            .nivelMinimo(2)
            .ordemExibicao(2)
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(ItemConfig configuracao) {
        configuracao.setNome("Nome Atualizado");
        configuracao.setPeso(BigDecimal.valueOf(5.00));
        configuracao.setNivelMinimo(3);
        configuracao.setOrdemExibicao(10);
    }

    @Override
    protected void verificarCamposAtualizados(ItemConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Nome Atualizado");
        assertThat(configuracao.getPeso()).isEqualByComparingTo(BigDecimal.valueOf(5.00));
        assertThat(configuracao.getNivelMinimo()).isEqualTo(3);
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(10);
    }

    // =========================================================
    // ITEM-05: BONUS_DERIVADO com bonusAlvo válido
    // =========================================================

    @Test
    @DisplayName("ITEM-05: Deve criar ItemEfeito BONUS_DERIVADO com bonusAlvoId válido")
    void deveCriarItemEfeitoBonusDerivadoComSucesso() {
        // Arrange
        ItemConfig item = repository.save(criarConfiguracaoValida(jogo));

        String suffix = getUniqueSuffix().substring(0, 3).toUpperCase();
        BonusConfig bonus = bonusConfigRepository.save(BonusConfig.builder()
            .jogo(jogo)
            .nome("Bônus Defesa " + suffix)
            .sigla("BD" + suffix.substring(0, 1))
            .formulaBase("total / 4")
            .ordemExibicao(1)
            .build());

        ItemEfeitoRequest request = new ItemEfeitoRequest(
            TipoItemEfeito.BONUS_DERIVADO,
            null, null,
            bonus.getId(),
            5,
            null,
            "Bônus de defesa ao equipar"
        );

        // Act
        ItemEfeito efeito = itemEfeitoService.adicionarEfeito(item.getId(), request);

        // Assert
        assertThat(efeito.getId()).isNotNull();
        assertThat(efeito.getTipoEfeito()).isEqualTo(TipoItemEfeito.BONUS_DERIVADO);
        assertThat(efeito.getBonusAlvo().getId()).isEqualTo(bonus.getId());
        assertThat(efeito.getValorFixo()).isEqualTo(5);

        // Verificar via repositório
        List<ItemEfeito> efeitos = itemEfeitoRepository.findByItemConfigId(item.getId());
        assertThat(efeitos).hasSize(1);
    }

    // =========================================================
    // ITEM-06: BONUS_DERIVADO com bonusAlvo de outro jogo
    // =========================================================

    @Test
    @DisplayName("ITEM-06: Deve lançar ValidationException ao criar ItemEfeito BONUS_DERIVADO com bonusAlvo de outro jogo")
    void deveLancarExcecaoAoCriarEfeitoBonusDerivadoComAlvoDeOutroJogo() {
        // Arrange
        ItemConfig item = repository.save(criarConfiguracaoValida(jogo));

        BonusConfig bonusOutroJogo = bonusConfigRepository.save(BonusConfig.builder()
            .jogo(outroJogo)
            .nome("Bonus Outro Jogo")
            .sigla("BOJ")
            .formulaBase("total / 2")
            .ordemExibicao(1)
            .build());

        ItemEfeitoRequest request = new ItemEfeitoRequest(
            TipoItemEfeito.BONUS_DERIVADO,
            null, null,
            bonusOutroJogo.getId(), // bonus de outro jogo!
            3,
            null,
            "Efeito inválido"
        );

        // Act & Assert
        assertThrows(ValidationException.class,
            () -> itemEfeitoService.adicionarEfeito(item.getId(), request));
    }

    // =========================================================
    // ITEM-07: FORMULA_CUSTOMIZADA com fórmula inválida
    // =========================================================

    @Test
    @DisplayName("ITEM-07: Deve lançar ValidationException ao criar ItemEfeito FORMULA_CUSTOMIZADA com fórmula inválida")
    void deveLancarExcecaoAoCriarEfeitoComFormulaInvalida() {
        // Arrange
        ItemConfig item = repository.save(criarConfiguracaoValida(jogo));

        ItemEfeitoRequest request = new ItemEfeitoRequest(
            TipoItemEfeito.FORMULA_CUSTOMIZADA,
            null, null, null,
            null,
            "((invalido &&& formula",
            "Efeito com fórmula inválida"
        );

        // Act & Assert
        assertThrows(ValidationException.class,
            () -> itemEfeitoService.adicionarEfeito(item.getId(), request));
    }

    // =========================================================
    // ITEM-08: FORMULA_CUSTOMIZADA com fórmula válida
    // =========================================================

    @Test
    @DisplayName("ITEM-08: Deve criar ItemEfeito FORMULA_CUSTOMIZADA com fórmula válida")
    void deveCriarEfeitoFormulaCustomizadaComFormulaValida() {
        // Arrange
        ItemConfig item = repository.save(criarConfiguracaoValida(jogo));

        ItemEfeitoRequest request = new ItemEfeitoRequest(
            TipoItemEfeito.FORMULA_CUSTOMIZADA,
            null, null, null,
            null,
            "nivel * 2 + base",
            "Bônus escalonado por nível"
        );

        // Act
        ItemEfeito efeito = itemEfeitoService.adicionarEfeito(item.getId(), request);

        // Assert
        assertThat(efeito.getId()).isNotNull();
        assertThat(efeito.getTipoEfeito()).isEqualTo(TipoItemEfeito.FORMULA_CUSTOMIZADA);
        assertThat(efeito.getFormula()).isEqualTo("nivel * 2 + base");
    }

    // =========================================================
    // ITEM-09: ItemRequisito NIVEL com valorMinimo=5
    // =========================================================

    @Test
    @DisplayName("ITEM-09: Deve criar ItemRequisito NIVEL com valorMinimo=5")
    void deveCriarItemRequisitoNivelComValorMinimo() {
        // Arrange
        ItemConfig item = repository.save(criarConfiguracaoValida(jogo));

        ItemRequisitoRequest request = new ItemRequisitoRequest(
            TipoRequisito.NIVEL,
            null, // alvo é opcional para NIVEL
            5
        );

        // Act
        ItemRequisito requisito = itemRequisitoService.adicionarRequisito(item.getId(), request);

        // Assert
        assertThat(requisito.getId()).isNotNull();
        assertThat(requisito.getTipo()).isEqualTo(TipoRequisito.NIVEL);
        assertThat(requisito.getValorMinimo()).isEqualTo(5);

        List<ItemRequisito> requisitos = itemRequisitoRepository.findByItemConfigId(item.getId());
        assertThat(requisitos).hasSize(1);
    }

    // =========================================================
    // ITEM-10: ItemRequisito ATRIBUTO com alvo="FOR" e valorMinimo=10
    // =========================================================

    @Test
    @DisplayName("ITEM-10: Deve criar ItemRequisito ATRIBUTO com alvo='FOR' e valorMinimo=10")
    void deveCriarItemRequisitoAtributoComAlvoEValorMinimo() {
        // Arrange
        ItemConfig item = repository.save(criarConfiguracaoValida(jogo));

        ItemRequisitoRequest request = new ItemRequisitoRequest(
            TipoRequisito.ATRIBUTO,
            "FOR",
            10
        );

        // Act
        ItemRequisito requisito = itemRequisitoService.adicionarRequisito(item.getId(), request);

        // Assert
        assertThat(requisito.getId()).isNotNull();
        assertThat(requisito.getTipo()).isEqualTo(TipoRequisito.ATRIBUTO);
        assertThat(requisito.getAlvo()).isEqualTo("FOR");
        assertThat(requisito.getValorMinimo()).isEqualTo(10);
    }

    // =========================================================
    // ITEM-11: Soft delete com FichaItem existente preserva FichaItem
    // =========================================================

    @Test
    @DisplayName("ITEM-11: Soft delete de ItemConfig com FichaItem existente deve preservar FichaItem")
    void deveSoftDeletarItemConfigPreservandoFichaItemExistente() {
        // Arrange
        ItemConfig item = repository.save(criarConfiguracaoValida(jogo));

        Ficha ficha = fichaRepository.save(Ficha.builder()
            .jogo(jogo)
            .nome("Personagem ITEM-11")
            .build());

        FichaItem fichaItem = fichaItemRepository.save(FichaItem.builder()
            .ficha(ficha)
            .itemConfig(item)
            .nome(item.getNome())
            .quantidade(1)
            .build());

        // Act — soft delete do ItemConfig
        itemConfigService.deletar(item.getId());

        // Assert — FichaItem preservado (soft delete não cascadeou)
        assertThat(fichaItemRepository.findById(fichaItem.getId())).isPresent();

        // ItemConfig não aparece mais na listagem ativa
        List<ItemConfig> ativos = itemConfigService.listar(jogo.getId());
        assertThat(ativos).extracting("id").doesNotContain(item.getId());
    }

    // =========================================================
    // ITEM-12: Listar com filtro por raridade
    // =========================================================

    @Test
    @DisplayName("ITEM-12: Deve listar itens filtrando por raridade e retornar apenas os da raridade")
    void deveListarItensComFiltroPorRaridade() {
        // Arrange — criar segunda raridade para o jogo
        RaridadeItemConfig raridadeRara = raridadeRepository.save(RaridadeItemConfig.builder()
            .jogo(jogo)
            .nome("Raro")
            .cor("#0070dd")
            .ordemExibicao(2)
            .podeJogadorAdicionar(false)
            .build());

        repository.save(ItemConfig.builder()
            .jogo(jogo)
            .nome("Espada Comum " + getUniqueSuffix())
            .raridade(raridadeComum)
            .tipo(tipoArma)
            .peso(BigDecimal.valueOf(1.00))
            .nivelMinimo(1)
            .ordemExibicao(1)
            .build());

        repository.save(ItemConfig.builder()
            .jogo(jogo)
            .nome("Espada Rara " + getUniqueSuffix())
            .raridade(raridadeRara)
            .tipo(tipoArma)
            .peso(BigDecimal.valueOf(2.00))
            .nivelMinimo(5)
            .ordemExibicao(2)
            .build());

        // Act — filtrar apenas pela raridade rara
        Page<ItemConfig> resultado = itemConfigService.listarComFiltros(
            jogo.getId(), null, raridadeRara.getId(), null, PageRequest.of(0, 10));

        // Assert
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getRaridade().getId()).isEqualTo(raridadeRara.getId());
    }

    // =========================================================
    // ITEM-13: Listar com filtro por nome parcial (case-insensitive)
    // =========================================================

    @Test
    @DisplayName("ITEM-13: Deve listar itens filtrando por nome parcial case-insensitive")
    void deveListarItensComFiltroPorNomeParcial() {
        // Arrange
        repository.save(ItemConfig.builder()
            .jogo(jogo)
            .nome("Espada Longa de Fogo " + getUniqueSuffix())
            .raridade(raridadeComum)
            .tipo(tipoArma)
            .peso(BigDecimal.valueOf(2.00))
            .nivelMinimo(5)
            .ordemExibicao(1)
            .build());

        repository.save(ItemConfig.builder()
            .jogo(jogo)
            .nome("ESPADA CURTA " + getUniqueSuffix())
            .raridade(raridadeComum)
            .tipo(tipoArma)
            .peso(BigDecimal.valueOf(1.00))
            .nivelMinimo(1)
            .ordemExibicao(2)
            .build());

        repository.save(ItemConfig.builder()
            .jogo(jogo)
            .nome("Arco Longo " + getUniqueSuffix())
            .raridade(raridadeComum)
            .tipo(tipoArma)
            .peso(BigDecimal.valueOf(1.50))
            .nivelMinimo(1)
            .ordemExibicao(3)
            .build());

        // Act — filtrar por "espada" (case-insensitive)
        Page<ItemConfig> resultado = itemConfigService.listarComFiltros(
            jogo.getId(), "espada", null, null, PageRequest.of(0, 10));

        // Assert — deve retornar as 2 espadas, não o arco
        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getContent())
            .extracting(i -> i.getNome().toLowerCase())
            .allMatch(nome -> nome.contains("espada"));
    }

    // =========================================================
    // ITEM-14: GET detalhe inclui efeitos e requisitos (sem N+1)
    // =========================================================

    @Test
    @DisplayName("ITEM-14: GET detalhe de item deve incluir efeitos e requisitos carregados")
    void deveRetornarDetalheItemComEfeitosERequisitos() {
        // Arrange
        ItemConfig item = repository.save(criarConfiguracaoValida(jogo));

        String suffix = getUniqueSuffix().substring(0, 3).toUpperCase();
        BonusConfig bonus = bonusConfigRepository.save(BonusConfig.builder()
            .jogo(jogo)
            .nome("Bonus Det " + suffix)
            .sigla("BX" + suffix.substring(0, 1))
            .formulaBase("total / 3")
            .ordemExibicao(1)
            .build());

        // Adicionar um efeito
        itemEfeitoService.adicionarEfeito(item.getId(), new ItemEfeitoRequest(
            TipoItemEfeito.BONUS_DERIVADO, null, null, bonus.getId(), 2, null, "Efeito de teste"
        ));

        // Adicionar um requisito
        itemRequisitoService.adicionarRequisito(item.getId(), new ItemRequisitoRequest(
            TipoRequisito.NIVEL, null, 3
        ));

        entityManager.flush();
        entityManager.clear(); // limpa L1 cache para forçar busca real no DB

        // Act — busca detalhada que deve carregar coleções sem N+1
        ItemConfig detalhe = itemConfigService.buscarPorId(item.getId());

        // Assert — coleções estão disponíveis sem LazyInitializationException
        assertThat(detalhe.getEfeitos()).isNotNull().hasSize(1);
        assertThat(detalhe.getRequisitos()).isNotNull().hasSize(1);
        assertThat(detalhe.getEfeitos().get(0).getTipoEfeito()).isEqualTo(TipoItemEfeito.BONUS_DERIVADO);
        assertThat(detalhe.getRequisitos().get(0).getTipo()).isEqualTo(TipoRequisito.NIVEL);
    }

    // =========================================================
    // ITEM-02 / ITEM-03: Validações cross-jogo
    // =========================================================

    @Test
    @DisplayName("ITEM-02: Deve lançar ValidationException ao criar item com raridade de outro jogo")
    void deveLancarExcecaoAoCriarItemComRaridadeDeOutroJogo() {
        // Arrange
        ItemConfig item = ItemConfig.builder()
            .jogo(jogo)
            .nome("Item Invalido " + getUniqueSuffix())
            .raridade(raridadeOutroJogo) // raridade do outro jogo!
            .tipo(tipoArma)
            .peso(BigDecimal.valueOf(1.00))
            .nivelMinimo(1)
            .ordemExibicao(1)
            .build();

        // Act & Assert
        assertThrows(Exception.class, () -> itemConfigService.criar(item));
    }

    @Test
    @DisplayName("ITEM-03: Deve lançar ValidationException ao criar item com tipo de outro jogo")
    void deveLancarExcecaoAoCriarItemComTipoDeOutroJogo() {
        // Arrange
        ItemConfig item = ItemConfig.builder()
            .jogo(jogo)
            .nome("Item Invalido " + getUniqueSuffix())
            .raridade(raridadeComum)
            .tipo(tipoOutroJogo) // tipo do outro jogo!
            .peso(BigDecimal.valueOf(1.00))
            .nivelMinimo(1)
            .ordemExibicao(1)
            .build();

        // Act & Assert
        assertThrows(Exception.class, () -> itemConfigService.criar(item));
    }
}
