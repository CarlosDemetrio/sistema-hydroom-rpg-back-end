package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ItemEfeitoRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ItemEfeito;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.RaridadeItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.TipoItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.SubcategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoItemEfeito;
import br.com.hydroom.rpg.fichacontrolador.repository.BonusConfigRepository;
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
    private ItemEfeitoRepository itemEfeitoRepository;

    @Autowired
    private ItemRequisitoRepository itemRequisitoRepository;

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

    @Test
    @DisplayName("Deve criar ItemEfeito BONUS_DERIVADO com bonusAlvoId válido")
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

    @Test
    @DisplayName("Deve lançar ValidationException ao criar ItemEfeito FORMULA_CUSTOMIZADA com fórmula inválida")
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

    @Test
    @DisplayName("Deve lançar ValidationException ao criar item com raridade de outro jogo")
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
    @DisplayName("Deve lançar ValidationException ao criar item com tipo de outro jogo")
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
